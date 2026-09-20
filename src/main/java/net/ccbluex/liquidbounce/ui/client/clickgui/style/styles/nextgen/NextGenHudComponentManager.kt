/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.writeTextAtomic
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID

/**
 * Stateful adapter for the web HUD component protocol.
 *
 * LiquidBounce Nextgen stores web component instances in its modern value tree. FDP targets
 * Forge 1.8.9, so sharing that implementation would also pull in Fabric, modern registries and
 * renderer classes. This manager preserves the wire contract while keeping the state isolated
 * from the native [net.ccbluex.liquidbounce.file.configs.HudConfig].
 */
@Suppress("TooManyFunctions")
object NextGenHudComponentManager {

    const val THEME_ID = "fdpclient"

    private const val CONFIG_VERSION = 1
    private const val MAX_COMPONENTS = 128
    private const val MAX_LIST_ENTRIES = 256
    private const val MAX_TEXT_LENGTH = 4096
    private const val MAX_OFFSET = 1_000_000.0
    private const val BROKEN_WATERMARK_BACKGROUND = 2_852_450_320L
    private const val WATERMARK_BACKGROUND = 2_853_180_704L

    enum class MutationResult {
        UPDATED,
        NOT_FOUND,
        CONFLICT,
        INVALID,
    }

    private data class ComponentState(val id: String, val definition: JsonObject) {
        val name: String
            get() = definition.get("name")?.asString.orEmpty()
    }

    private val parser = JsonParser()
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()
    private val definitions = linkedMapOf<String, JsonObject>()
    private val componentStates = mutableListOf<ComponentState>()
    private var testStateFile: File? = null
    private val stateFile: File
        get() = testStateFile ?: File(FileManager.guiLayoutsDir, "nextgen-hud-components.json")

    @Volatile
    var hudEditorSelected = false
        private set

    private var loaded = false

    fun setHudEditorSelected(body: String): MutationResult {
        val selected = runCatching {
            parser.parse(body).asJsonObject.get("selected").asBoolean
        }.getOrNull() ?: return MutationResult.INVALID

        setHudEditorSelected(selected)
        return MutationResult.UPDATED
    }

    fun setHudEditorSelected(selected: Boolean) {
        hudEditorSelected = selected
    }

    @Synchronized
    fun components(themeId: String? = THEME_ID): JsonArray {
        ensureLoaded()
        if (themeId != null && !themeId.equals(THEME_ID, ignoreCase = true)) {
            return JsonArray()
        }

        return JsonArray().apply {
            componentStates.forEach { add(componentJson(it)) }
        }
    }

    fun nativeComponents(): JsonArray = JsonArray()

    @Synchronized
    fun catalog(themeId: String): JsonArray {
        ensureLoaded()
        if (!themeId.equals(THEME_ID, ignoreCase = true)) return JsonArray()

        return JsonArray().apply {
            definitions.values.forEach { definition ->
                val name = definition.get("name")?.asString ?: return@forEach
                val source = componentStates.firstOrNull { it.name == name } ?: return@forEach
                val singleton = definition.get("singleton")?.asBoolean ?: true
                add(JsonObject().apply {
                    addProperty("name", name)
                    addProperty("description", definition.get("description")?.asString ?: "")
                    addProperty("id", source.id)
                    addProperty("singleton", singleton)
                    addProperty(
                        "canAdd",
                        !singleton || componentStates.none { it.name == name && isEnabled(it.definition) }
                    )
                })
            }
        }
    }

    fun addComponent(id: String): MutationResult {
        val result = synchronized(this) {
            ensureLoaded()
            val source = componentStates.firstOrNull { it.id == id }
                ?: return@synchronized MutationResult.NOT_FOUND
            val factory = definitions[source.name]
                ?: return@synchronized MutationResult.NOT_FOUND
            val singleton = factory.get("singleton")?.asBoolean ?: true

            if (singleton && componentStates.any { it.name == source.name && isEnabled(it.definition) }) {
                return@synchronized MutationResult.CONFLICT
            }

            val component = componentStates.firstOrNull { it.name == source.name && !isEnabled(it.definition) }
                ?: if (componentStates.size < MAX_COMPONENTS) {
                    ComponentState(UUID.randomUUID().toString(), copyObject(factory)).also(componentStates::add)
                } else {
                    return@synchronized MutationResult.CONFLICT
                }

            component.definition.addProperty("enabled", true)
            saveLocked()
            MutationResult.UPDATED
        }

        if (result == MutationResult.UPDATED) publishComponents()
        return result
    }

    @Synchronized
    fun componentSettings(id: String): JsonObject? {
        ensureLoaded()
        val component = componentStates.firstOrNull { it.id == id } ?: return null
        return editorSettings(component)
    }

    fun updateAlignment(id: String, body: String): MutationResult {
        val incoming = runCatching { parser.parse(body).asJsonObject }.getOrNull()
            ?: return MutationResult.INVALID

        val result = synchronized(this) {
            ensureLoaded()
            val component = componentStates.firstOrNull { it.id == id }
                ?: return@synchronized MutationResult.NOT_FOUND
            val fallback = component.definition.getAsJsonObject("alignment") ?: JsonObject()
            val alignment = sanitizeAlignment(incoming, fallback) ?: return@synchronized MutationResult.INVALID
            component.definition.add("alignment", alignment)
            saveLocked()
            MutationResult.UPDATED
        }

        if (result == MutationResult.UPDATED) publishComponents()
        return result
    }

    @Suppress("CyclomaticComplexMethod")
    fun updateSettings(id: String, body: String): MutationResult {
        val incoming = runCatching { parser.parse(body).asJsonObject }.getOrNull()
            ?: return MutationResult.INVALID
        val incomingValues = incoming.getAsJsonArray("value") ?: return MutationResult.INVALID

        val result = synchronized(this) {
            ensureLoaded()
            val componentIndex = componentStates.indexOfFirst { it.id == id }
            if (componentIndex < 0) return@synchronized MutationResult.NOT_FOUND
            val component = componentStates[componentIndex]
            val updatedDefinition = copyObject(component.definition)
            if (incomingValues.size() > MAX_LIST_ENTRIES) return@synchronized MutationResult.INVALID

            val enabledSetting = incomingValues.findSetting("Enabled")
            if (enabledSetting != null) {
                val enabled = enabledSetting.get("value")?.takeIf(JsonElement::isJsonPrimitive)
                    ?.asJsonPrimitive?.takeIf { it.isBoolean }?.asBoolean
                    ?: return@synchronized MutationResult.INVALID
                val wasEnabled = isEnabled(updatedDefinition)
                updatedDefinition.addProperty("enabled", enabled)
                if (wasEnabled && !enabled) {
                    val defaultAlignment = definitions[component.name]?.getAsJsonObject("alignment")
                    if (defaultAlignment != null) updatedDefinition.add("alignment", copy(defaultAlignment))
                }
            }

            val targetValues = updatedDefinition.getAsJsonArray("values") ?: JsonArray().also {
                updatedDefinition.add("values", it)
            }
            if (!applyInteropValues(targetValues, incomingValues)) {
                return@synchronized MutationResult.INVALID
            }

            componentStates[componentIndex] = ComponentState(component.id, updatedDefinition)
            val saveResult = runCatching(::saveLocked)
            if (saveResult.isFailure) {
                componentStates[componentIndex] = component
                saveResult.getOrThrow()
            }
            MutationResult.UPDATED
        }

        if (result == MutationResult.UPDATED) publishComponents()
        return result
    }

    @Synchronized
    internal fun resetForTests(stateFile: File? = null) {
        definitions.clear()
        componentStates.clear()
        loaded = false
        hudEditorSelected = false
        testStateFile = stateFile
    }

    private fun ensureLoaded() {
        if (loaded) return

        definitions.clear()
        componentStates.clear()
        loadDefinitions()
        loadState()
        loaded = true
    }

    private fun loadDefinitions() {
        val metadata = readJson("metadata.json")
            ?: error("Missing NextGen HUD metadata")
        val names = metadata.getAsJsonArray("components") ?: JsonArray()

        names.forEach { element ->
            val declaredName = runCatching { element.asString }.getOrNull() ?: return@forEach
            val definition = readJson("components/${declaredName.lowercase(Locale.ROOT)}.json")
                ?: return@forEach
            val name = definition.get("name")?.asString?.takeIf(String::isNotBlank) ?: return@forEach
            if (definitions.containsKey(name)) {
                LOGGER.warn("[NextGenHUD] Ignoring duplicate component definition '$name'.")
                return@forEach
            }
            definition.addProperty("description", definition.get("description")?.asString ?: "")
            definition.addProperty("singleton", definition.get("singleton")?.asBoolean ?: true)
            definition.addProperty("enabled", definition.get("enabled")?.asBoolean ?: false)
            definition.add("alignment", sanitizeAlignment(
                definition.getAsJsonObject("alignment") ?: JsonObject(),
                defaultAlignment()
            ) ?: defaultAlignment())
            if (!definition.has("values")) definition.add("values", JsonArray())
            NextGenHudVisualDefaults.enrich(definition)
            definitions[name] = definition
        }

        check(definitions.isNotEmpty()) { "No NextGen HUD component definitions could be loaded" }
    }

    private fun loadState() {
        val restored = runCatching {
            if (!stateFile.isFile || stateFile.length() > 2_000_000L) return@runCatching emptyList()
            val root = stateFile.bufferedReader(Charsets.UTF_8).use { parser.parse(it).asJsonObject }
            val version = root.get("version")?.asInt ?: 0
            if (version !in 1..CONFIG_VERSION) return@runCatching emptyList()
            restoreComponents(root.getAsJsonArray("components") ?: JsonArray())
        }.onFailure {
            LOGGER.warn("[NextGenHUD] Could not load persisted web HUD; defaults will be used.", it)
        }.getOrDefault(emptyList())

        componentStates += restored
        definitions.forEach { (name, definition) ->
            if (componentStates.none { it.name == name }) {
                componentStates += ComponentState(stableId(name, 0), copyObject(definition))
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun restoreComponents(stored: JsonArray): List<ComponentState> {
        val result = mutableListOf<ComponentState>()
        val usedIds = hashSetOf<String>()
        val counts = hashMapOf<String, Int>()

        stored.take(MAX_COMPONENTS).forEach { element ->
            val json = element.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return@forEach
            val name = json.get("name")?.asString ?: return@forEach
            val factory = definitions[name] ?: return@forEach
            val singleton = factory.get("singleton")?.asBoolean ?: true
            if (singleton && result.any { it.name == name }) return@forEach

            val ordinal = counts.getOrDefault(name, 0)
            counts[name] = ordinal + 1
            var id = json.get("id")?.asString?.takeIf(::validUuid) ?: stableId(name, ordinal)
            while (!usedIds.add(id)) id = UUID.randomUUID().toString()

            val definition = copyObject(factory)
            json.get("enabled")?.takeIf(JsonElement::isJsonPrimitive)?.asJsonPrimitive
                ?.takeIf { it.isBoolean }?.let { definition.addProperty("enabled", it.asBoolean) }
            json.getAsJsonObject("alignment")?.let { alignment ->
                sanitizeAlignment(alignment, definition.getAsJsonObject("alignment") ?: defaultAlignment())
                    ?.let { definition.add("alignment", it) }
            }
            val targetValues = definition.getAsJsonArray("values") ?: JsonArray()
            json.getAsJsonArray("values")?.let { mergePersistedValues(targetValues, it) }
            migrateKnownBrokenDefaults(name, targetValues)
            definition.add("values", targetValues)
            result += ComponentState(id, definition)
        }

        return result
    }

    private fun migrateKnownBrokenDefaults(name: String, values: JsonArray) {
        if (name != "Watermark") return
        val background = values.findSetting("Background Color") ?: return
        if (background.get("value")?.asLong == BROKEN_WATERMARK_BACKGROUND) {
            background.addProperty("value", WATERMARK_BACKGROUND)
        }
    }

    private fun saveLocked() {
        val root = JsonObject().apply {
            addProperty("version", CONFIG_VERSION)
            addProperty("themeId", THEME_ID)
            add("components", JsonArray().apply {
                componentStates.forEach { component ->
                    add(JsonObject().apply {
                        addProperty("id", component.id)
                        addProperty("name", component.name)
                        addProperty("enabled", isEnabled(component.definition))
                        add("alignment", copy(component.definition.get("alignment") ?: defaultAlignment()))
                        add("values", copy(component.definition.get("values") ?: JsonArray()))
                    })
                }
            })
        }
        stateFile.writeTextAtomic(prettyGson.toJson(root), Charsets.UTF_8)
    }

    private fun componentJson(component: ComponentState): JsonObject = JsonObject().apply {
        addProperty("name", component.name)
        addProperty("description", component.definition.get("description")?.asString ?: "")
        addProperty("id", component.id)
        add("settings", runtimeSettings(component.definition))
    }

    private fun runtimeSettings(definition: JsonObject): JsonObject = JsonObject().apply {
        addProperty("enabled", isEnabled(definition))
        add("alignment", copy(definition.get("alignment") ?: defaultAlignment()))
        definition.getAsJsonArray("values")?.forEach { element ->
            val setting = element.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return@forEach
            val name = setting.get("name")?.asString ?: return@forEach
            add(protocolName(name), runtimeSettingValue(setting))
        }
    }

    private fun runtimeSettingValue(setting: JsonObject): JsonElement {
        val type = setting.get("type")?.asString.orEmpty()
        val nested = setting.getAsJsonArray("values")
        if (nested != null) {
            return JsonObject().apply {
                if (type == "TOGGLEABLE") {
                    addProperty("enabled", setting.get("value")?.asBoolean ?: false)
                }
                nested.forEach { childElement ->
                    val child = childElement.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return@forEach
                    val name = child.get("name")?.asString ?: return@forEach
                    add(protocolName(name), runtimeSettingValue(child))
                }
            }
        }
        return setting.get("value")?.let(::copy) ?: JsonNull.INSTANCE
    }

    private fun editorSettings(component: ComponentState): JsonObject = settingBase("TOGGLEABLE", component.name).apply {
        add("value", JsonArray().apply {
            add(settingBase("BOOLEAN", "Enabled").apply {
                addProperty("value", isEnabled(component.definition))
            })
            add(alignmentSetting(component.definition.getAsJsonObject("alignment") ?: defaultAlignment()))
            component.definition.getAsJsonArray("values")?.forEach { setting ->
                setting.takeIf(JsonElement::isJsonObject)?.asJsonObject?.let { add(toInteropSetting(it)) }
            }
        })
    }

    private fun alignmentSetting(alignment: JsonObject): JsonObject = settingBase("CONFIGURABLE", "Alignment").apply {
        add("value", JsonArray().apply {
            add(choiceSetting(
                "HorizontalAlignment",
                alignment.get("horizontalAlignment")?.asString ?: "CenterTranslated",
                HORIZONTAL_ALIGNMENTS
            ))
            add(numberSetting("HorizontalOffset", alignment.get("horizontalOffset")?.asDouble ?: 0.0))
            add(choiceSetting(
                "VerticalAlignment",
                alignment.get("verticalAlignment")?.asString ?: "CenterTranslated",
                VERTICAL_ALIGNMENTS
            ))
            add(numberSetting("VerticalOffset", alignment.get("verticalOffset")?.asDouble ?: 0.0))
        })
    }

    /** Schema dispatch is intentionally centralized so unsupported public value types fail closed. */
    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    private fun toInteropSetting(setting: JsonObject): JsonObject {
        val type = setting.get("type")?.asString ?: "TEXT"
        return settingBase(type, setting.get("name")?.asString ?: "Setting").apply {
            setting.get("description")?.let { add("description", copy(it)) }
            when (type) {
                "CONFIGURABLE" -> add("value", JsonArray().apply {
                    setting.getAsJsonArray("values")?.forEach { child ->
                        child.takeIf(JsonElement::isJsonObject)?.asJsonObject?.let { add(toInteropSetting(it)) }
                    }
                })

                "TOGGLEABLE" -> add("value", JsonArray().apply {
                    add(settingBase("BOOLEAN", "Enabled").apply {
                        addProperty("value", setting.get("value")?.asBoolean ?: false)
                    })
                    setting.getAsJsonArray("values")?.forEach { child ->
                        child.takeIf(JsonElement::isJsonObject)?.asJsonObject?.let { add(toInteropSetting(it)) }
                    }
                })

                else -> {
                    add("value", copy(setting.get("value") ?: JsonNull.INSTANCE))
                    setting.get("choices")?.let { add("choices", copy(it)) }
                    setting.get("canBeNone")?.let { add("canBeNone", copy(it)) }
                    addProperty("isOrderSensitive", setting.get("isOrderSensitive")?.asBoolean ?: false)
                    setting.get("innerValueType")?.let { inner ->
                        add("innerValueType", copy(inner))
                        if (type == "REGISTRY_LIST") {
                            addProperty("registry", inner.asString.lowercase(Locale.ROOT))
                        }
                    }
                    setting.getAsJsonObject("range")?.let { sourceRange ->
                        add("range", JsonObject().apply {
                            addProperty("from", sourceRange.get("min")?.asDouble ?: 0.0)
                            addProperty("to", sourceRange.get("max")?.asDouble ?: 0.0)
                        })
                    }
                    if (type == "INT" || type == "FLOAT") {
                        addProperty("suffix", setting.get("suffix")?.asString ?: "")
                    }
                }
            }
        }
    }

    private fun settingBase(type: String, name: String): JsonObject = JsonObject().apply {
        addProperty("valueType", type)
        addProperty("name", name)
        add("description", JsonNull.INSTANCE)
        add("key", JsonNull.INSTANCE)
    }

    private fun choiceSetting(name: String, value: String, choices: Array<String>): JsonObject =
        settingBase("CHOOSE", name).apply {
            addProperty("value", value)
            add("choices", JsonArray().apply { choices.forEach { add(JsonPrimitive(it)) } })
        }

    private fun numberSetting(name: String, value: Double): JsonObject = settingBase("FLOAT", name).apply {
        addProperty("value", value)
        add("range", JsonObject().apply {
            addProperty("from", -MAX_OFFSET)
            addProperty("to", MAX_OFFSET)
        })
        addProperty("suffix", "px")
    }

    @Suppress("CyclomaticComplexMethod")
    private fun applyInteropValues(targetValues: JsonArray, incomingValues: JsonArray): Boolean {
        targetValues.forEach { targetElement ->
            val target = targetElement.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return@forEach
            val name = target.get("name")?.asString ?: return@forEach
            val incoming = incomingValues.findSetting(name) ?: return@forEach
            val type = target.get("type")?.asString ?: return false
            if (incoming.get("valueType")?.asString != type) return false

            when (type) {
                "CONFIGURABLE" -> {
                    val childInput = incoming.getAsJsonArray("value") ?: return false
                    val childTargets = target.getAsJsonArray("values") ?: return false
                    if (!applyInteropValues(childTargets, childInput)) return false
                }

                "TOGGLEABLE" -> {
                    val childInput = incoming.getAsJsonArray("value") ?: return false
                    val enabled = childInput.findSetting("Enabled")?.get("value")
                        ?.takeIf(JsonElement::isJsonPrimitive)?.asJsonPrimitive
                        ?.takeIf { it.isBoolean }?.asBoolean ?: return false
                    target.addProperty("value", enabled)
                    val nested = target.getAsJsonArray("values") ?: JsonArray()
                    if (!applyInteropValues(nested, childInput)) return false
                }

                else -> if (!applyScalar(target, incoming.get("value") ?: return false)) return false
            }
        }
        return true
    }

    /** Every branch validates one wire type and leaves the target untouched on failure. */
    @Suppress("CyclomaticComplexMethod")
    private fun applyScalar(target: JsonObject, incoming: JsonElement): Boolean {
        return when (target.get("type")?.asString) {
            "BOOLEAN" -> incoming.booleanOrNull()?.let { target.addProperty("value", it); true } ?: false
            "TEXT" -> incoming.stringOrNull()?.takeIf { it.length <= MAX_TEXT_LENGTH }
                ?.let { target.addProperty("value", it); true } ?: false
            "CHOOSE" -> incoming.stringOrNull()?.takeIf { choice ->
                target.getAsJsonArray("choices")?.any { it.asString == choice } == true
            }?.let { target.addProperty("value", it); true } ?: false
            "INT" -> incoming.numberOrNull()?.takeIf(Double::isFinite)?.let { number ->
                val range = target.getAsJsonObject("range")
                val min = range?.get("min")?.asDouble ?: Int.MIN_VALUE.toDouble()
                val max = range?.get("max")?.asDouble ?: Int.MAX_VALUE.toDouble()
                target.addProperty("value", number.coerceIn(min, max).toInt())
                true
            } ?: false
            "FLOAT" -> incoming.numberOrNull()?.takeIf(Double::isFinite)?.let { number ->
                val range = target.getAsJsonObject("range")
                val min = range?.get("min")?.asDouble ?: -Double.MAX_VALUE
                val max = range?.get("max")?.asDouble ?: Double.MAX_VALUE
                target.addProperty("value", number.coerceIn(min, max))
                true
            } ?: false
            "COLOR" -> incoming.numberOrNull()?.takeIf(Double::isFinite)?.let {
                target.addProperty("value", it.toLong().coerceIn(Int.MIN_VALUE.toLong(), 0xffff_ffffL))
                true
            } ?: false
            "MULTI_CHOOSE" -> applyStringList(target, incoming, enforceChoices = true)
            "REGISTRY_LIST" -> applyStringList(target, incoming, enforceChoices = false)
            else -> false
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun applyStringList(target: JsonObject, incoming: JsonElement, enforceChoices: Boolean): Boolean {
        if (!incoming.isJsonArray || incoming.asJsonArray.size() > MAX_LIST_ENTRIES) return false
        val choices = target.getAsJsonArray("choices")?.map { it.asString }?.toSet().orEmpty()
        val values = linkedSetOf<String>()
        incoming.asJsonArray.forEach { element ->
            val value = element.stringOrNull() ?: return false
            if (value.length > 256 || enforceChoices && value !in choices) return false
            if (!enforceChoices && !IDENTIFIER.matches(value)) return false
            values += value
        }
        if (enforceChoices && values.isEmpty() && target.get("canBeNone")?.asBoolean == false) return false
        target.add("value", JsonArray().apply { values.forEach { add(JsonPrimitive(it)) } })
        return true
    }

    @Suppress("NestedBlockDepth")
    private fun mergePersistedValues(targetValues: JsonArray, storedValues: JsonArray) {
        targetValues.forEach { targetElement ->
            val target = targetElement.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return@forEach
            val name = target.get("name")?.asString ?: return@forEach
            val stored = storedValues.firstOrNull { candidate ->
                candidate.isJsonObject && candidate.asJsonObject.get("name")?.asString == name &&
                    candidate.asJsonObject.get("type")?.asString == target.get("type")?.asString
            }?.asJsonObject ?: return@forEach

            val nestedTarget = target.getAsJsonArray("values")
            val nestedStored = stored.getAsJsonArray("values")
            if (nestedTarget != null && nestedStored != null) mergePersistedValues(nestedTarget, nestedStored)
            stored.get("value")?.let { value ->
                val interop = JsonObject().apply {
                    addProperty("valueType", target.get("type")?.asString)
                    add("value", copy(value))
                }
                applyScalar(target, interop.get("value"))
                if (target.get("type")?.asString == "TOGGLEABLE") {
                    value.booleanOrNull()?.let { target.addProperty("value", it) }
                }
            }
        }
    }

    private fun sanitizeAlignment(incoming: JsonObject, fallback: JsonObject): JsonObject? {
        val horizontal = incoming.get("horizontalAlignment")?.stringOrNull()
            ?: fallback.get("horizontalAlignment")?.stringOrNull() ?: "CenterTranslated"
        val vertical = incoming.get("verticalAlignment")?.stringOrNull()
            ?: fallback.get("verticalAlignment")?.stringOrNull() ?: "CenterTranslated"
        if (horizontal !in HORIZONTAL_ALIGNMENTS || vertical !in VERTICAL_ALIGNMENTS) return null

        val horizontalOffset = incoming.get("horizontalOffset")?.numberOrNull()
            ?: fallback.get("horizontalOffset")?.numberOrNull() ?: 0.0
        val verticalOffset = incoming.get("verticalOffset")?.numberOrNull()
            ?: fallback.get("verticalOffset")?.numberOrNull() ?: 0.0
        if (!horizontalOffset.isFinite() || !verticalOffset.isFinite()) return null

        return JsonObject().apply {
            addProperty("horizontalAlignment", horizontal)
            addProperty("horizontalOffset", horizontalOffset.coerceIn(-MAX_OFFSET, MAX_OFFSET))
            addProperty("verticalAlignment", vertical)
            addProperty("verticalOffset", verticalOffset.coerceIn(-MAX_OFFSET, MAX_OFFSET))
        }
    }

    private fun defaultAlignment(): JsonObject = JsonObject().apply {
        addProperty("horizontalAlignment", "CenterTranslated")
        addProperty("horizontalOffset", 0)
        addProperty("verticalAlignment", "CenterTranslated")
        addProperty("verticalOffset", 0)
    }

    private fun publishComponents() {
        UiEventSocket.publish("componentsUpdate", JsonObject().apply {
            addProperty("source", "theme")
            addProperty("themeId", THEME_ID)
            add("components", components(THEME_ID))
        })
    }

    private fun readJson(path: String): JsonObject? {
        val safePath = path.replace('\\', '/').split('/').filter { it.isNotBlank() && it != ".." }.joinToString("/")
        val developmentCandidates = arrayOf(
            File("nextgen-theme/public", safePath),
            File("nextgen-theme/dist", safePath),
            File(File(System.getProperty("user.dir")).parentFile ?: File("."), "nextgen-theme/public/$safePath"),
            File(File(System.getProperty("user.dir")).parentFile ?: File("."), "nextgen-theme/dist/$safePath"),
        )
        developmentCandidates.firstOrNull(File::isFile)?.let { file ->
            return runCatching { file.reader(Charsets.UTF_8).use { parser.parse(it).asJsonObject } }.getOrNull()
        }

        return runCatching {
            NextGenHudComponentManager::class.java.getResourceAsStream(
                "/assets/minecraft/fdpclient/nextgen-clickgui/$safePath"
            )?.bufferedReader(StandardCharsets.UTF_8)?.use { parser.parse(it).asJsonObject }
        }.getOrNull()
    }

    private fun JsonArray.findSetting(name: String): JsonObject? = firstOrNull { element ->
        element.isJsonObject && element.asJsonObject.get("name")?.asString == name
    }?.asJsonObject

    private fun JsonElement.booleanOrNull(): Boolean? = runCatching {
        asJsonPrimitive.takeIf { it.isBoolean }?.asBoolean
    }.getOrNull()

    private fun JsonElement.numberOrNull(): Double? = runCatching {
        asJsonPrimitive.takeIf { it.isNumber }?.asDouble
    }.getOrNull()

    private fun JsonElement.stringOrNull(): String? = runCatching {
        asJsonPrimitive.takeIf { it.isString }?.asString
    }.getOrNull()

    private fun isEnabled(definition: JsonObject): Boolean = definition.get("enabled")?.asBoolean ?: false

    private fun stableId(name: String, ordinal: Int): String = UUID.nameUUIDFromBytes(
        "$THEME_ID:$name:$ordinal".toByteArray(StandardCharsets.UTF_8)
    ).toString()

    private fun validUuid(value: String): Boolean = runCatching { UUID.fromString(value) }.isSuccess

    private fun protocolName(name: String): String =
        name.filter(Char::isLetterOrDigit).replaceFirstChar { it.lowercase(Locale.ROOT) }

    private fun copyObject(element: JsonObject): JsonObject = copy(element).asJsonObject

    private fun copy(element: JsonElement): JsonElement = parser.parse(element.toString())

    private val IDENTIFIER = Regex("[a-z0-9_.-]+:[a-z0-9_./-]+")
    private val HORIZONTAL_ALIGNMENTS = arrayOf("Left", "Right", "Center", "CenterTranslated")
    private val VERTICAL_ALIGNMENTS = arrayOf("Top", "Bottom", "Center", "CenterTranslated")
}
