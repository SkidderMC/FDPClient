/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonElement
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.event.ClientChange
import net.ccbluex.liquidbounce.event.ClientChangeBus
import net.ccbluex.liquidbounce.handler.lang.translation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.apache.logging.log4j.LogManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private typealias OnChangeInterceptor<T> = (old: T, new: T) -> T
private typealias OnChangedHandler<T> = (new: T) -> Unit

private val VALUE_LOGGER = LogManager.getLogger("ValueSystem")

internal fun Configurable.rootOwnerName(): String {
    var current = this
    repeat(64) {
        val parent = current.owner ?: return current.name
        if (parent === current) return current.name
        current = parent
    }
    return current.name
}

sealed class Value<T>(
    val name: String,
    var value: T,
    val suffix: String? = null,
    protected var default: T = value,
) : ReadWriteProperty<Any?, T> {

    private val stateFlow = MutableStateFlow(value)

    fun asStateFlow(): StateFlow<T> = stateFlow

    /**
     * The owner of this value.
     */
    var owner: Configurable? = null

    /**
     * Whether this value should be excluded from public configuration (text config)
     */
    var subjective: Boolean = false
        private set

    var hidden: Boolean = false
        private set

    var immutable: Boolean = false
        private set

    fun subjective() = apply { subjective = true }

    fun hide() = apply { hidden = true }

    fun immutable() = apply { immutable = true }

    var aliases: List<String> = emptyList()
        private set

    fun aliases(vararg names: String) = apply { aliases = names.toList() }

    /**
     * Optional human-readable help text shown as a hover tooltip in the ClickGUI styles that
     * support it. Mirrors the reference client's per-value descriptions. Open so a [Configurable]
     * subtype (e.g. a module) can override it with its own description source.
     */
    protected var descriptionField: String? = null

    open val description: String?
        get() = descriptionField ?: translation(
            "value.generic.description",
            name.replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), " ")
        )

    /**
     * Sets the hover description. Concrete value types override this to return their own type so
     * `val x = boolean(...).describe(...)` keeps the concrete type (and its members like toggle()).
     */
    open fun describe(text: String): Value<T> = apply { descriptionField = text }

    fun matchesKey(key: String): Boolean =
        name.equals(key, true) || aliases.any { it.equals(key, true) }

    var excluded: Boolean = false
        private set(value) {
            if (value) {
                owner?.get()?.remove(this)
            }
            field = value
        }

    fun exclude() = apply { excluded = true }

    fun excludeWhen(condition: Boolean) = apply {
        if (condition) {
            excluded = true
        }
    }

    fun setAndUpdateDefault(new: T): Boolean {
        if (new == value) {
            default = validate(new)
            return false
        }

        val changed = set(new)
        if (changed) default = value
        return changed
    }

    @Synchronized
    fun set(newValue: T, saveImmediately: Boolean = true): Boolean {
        if (newValue == value || excluded || hidden) {
            return false
        }
        if (immutable) {
            return false
        }

        val oldValue = value

        try {
            var handledValue = validate(newValue)
            onChangeInterceptors.forEach { handledValue = it(oldValue, handledValue) }

            if (handledValue == oldValue) {
                return false
            }

            return commitValue(oldValue, handledValue, saveImmediately)
        } catch (e: Exception) {
            VALUE_LOGGER.error("[$name]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]", e)
            return false
        }
    }

    /**
     * Excludes the chosen option [value] from the config system.
     *
     * [state] the value it will be set to before it is excluded.
     */
    fun excludeWithState(state: T = value) {
        setAndUpdateDefault(state)

        excluded = true
    }

    fun hideWithState(state: T = value) {
        setAndUpdateDefault(state)

        hidden = true
    }

    fun get() = value

    @Synchronized
    fun changeValue(newValue: T) {
        if (value == newValue) return
        value = newValue
        stateFlow.value = newValue
        publishChange()
    }

    private fun publishChange() {
        // Suppress per-value notifications during bulk config loading; a single Configuration
        // event is emitted once the load finishes, so the UI refreshes wholesale instead of per value.
        if (!ConfigSystem.isLoadingConfig) {
            owner?.let { ClientChangeBus.publish(ClientChange.ValueState(it.rootOwnerName(), name)) }
        }
    }

    /**
     * Commits one already validated mutation. Listeners observe the candidate value, but any listener
     * or persistence failure restores both the scalar and observable state before returning failure.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun commitValue(oldValue: T, newValue: T, saveImmediately: Boolean): Boolean {
        value = newValue
        return try {
            onChangedListeners.forEach { it.invoke(newValue) }
            if (saveImmediately && !ConfigSystem.isLoadingConfig) saveConfig(valuesConfig)
            stateFlow.value = newValue
            publishChange()
            true
        } catch (throwable: Throwable) {
            value = oldValue
            stateFlow.value = oldValue
            VALUE_LOGGER.error(
                "[$name]: mutation rolled back after ${throwable.javaClass.name} (${throwable.message}) " +
                    "[$oldValue >> $newValue]",
                throwable,
            )
            false
        }
    }

    // Serializations: JSON/Text

    abstract fun toJson(): JsonElement?
    open fun toText(): String = value.toString()

    protected abstract fun fromJsonF(element: JsonElement): T?
    protected abstract fun fromTextF(text: String): T?

    @Synchronized
    fun fromJson(element: JsonElement): Boolean {
        val raw = runCatching { fromJsonF(element) }.getOrNull() ?: return false
        return applyDeserialized(raw)
    }

    @Synchronized
    fun fromText(text: String): Boolean {
        val raw = runCatching { fromTextF(text) }.getOrNull() ?: return false
        return applyDeserialized(raw)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun applyDeserialized(raw: T): Boolean {
        val safe = runCatching { validate(raw) }.getOrElse {
            VALUE_LOGGER.error("[$name]: rejected serialized value '$raw' (${it.message})")
            return false
        }

        if (safe == value || immutable) return false

        val oldValue = value
        var handledValue = safe
        return try {
            onChangeInterceptors.forEach { handledValue = it(oldValue, handledValue) }
            if (handledValue == oldValue) false else commitValue(oldValue, handledValue, saveImmediately = false)
        } catch (throwable: Throwable) {
            VALUE_LOGGER.error("[$name]: rejected serialized value '$raw' (${throwable.message})", throwable)
            false
        }
    }

    // Serializations END

    private var onChangeInterceptors: Array<OnChangeInterceptor<T>> = emptyArray()
    private var onChangedListeners: Array<OnChangedHandler<T>> = emptyArray()

    fun onChange(interceptor: OnChangeInterceptor<T>) = apply {
        onChangeInterceptors += interceptor
    }

    fun onChanged(handler: OnChangedHandler<T>) = apply {
        this.onChangedListeners += handler
    }

    private var supportCondition = { true }

    fun isSupported(): Boolean = (owner?.isSupported() ?: true) && supportCondition.invoke()

    fun setSupport(condition: (Boolean) -> Boolean) = apply {
        val oldCondition = supportCondition
        supportCondition = { condition(oldCondition.invoke()) }
    }

    /**
     * Make the value able to set.
     */
    open fun validate(newValue: T): T = newValue

    // Support for delegating values using the `by` keyword.
    override operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        set(value)
    }

    fun shouldRender() = isSupported() && !excluded && !hidden

    fun resetValue() = set(default)
}
