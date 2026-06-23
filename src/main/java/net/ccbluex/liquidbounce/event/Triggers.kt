/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

fun interface Trigger<C> {
    fun test(context: C): Boolean
}

class Triggerer<C>(private val trigger: Trigger<C>) {
    fun evaluate(context: C, action: (C) -> Unit): Boolean {
        if (!trigger.test(context)) return false
        action(context)
        return true
    }
}

inline fun <reified E : Event, C> Listenable.triggeredHandler(
    triggerer: Triggerer<C>,
    always: Boolean = false,
    priority: Byte = 0,
    crossinline context: (E) -> C,
    crossinline action: (C) -> Unit
) = handler<E>(always, priority) { event -> triggerer.evaluate(context(event)) { action(it) } }

fun Listenable.onTick(
    interval: Int = 1,
    always: Boolean = false,
    priority: Byte = 0,
    action: (Long) -> Unit
) {
    require(interval > 0) { "Tick trigger interval must be positive" }
    var tick = 0L
    val triggerer = Triggerer<Long>(Trigger { current -> current % interval == 0L })
    triggeredHandler<GameTickEvent, Long>(triggerer, always, priority, { ++tick }, action)
}

fun Listenable.onMove(
    minimumDistance: Double = 0.0,
    always: Boolean = false,
    priority: Byte = 0,
    action: (MoveEvent) -> Unit
) {
    require(minimumDistance >= 0.0 && minimumDistance.isFinite()) {
        "Movement trigger distance must be finite and non-negative"
    }
    val squaredMinimum = minimumDistance * minimumDistance
    val triggerer = Triggerer<MoveEvent>(Trigger { event ->
        event.x * event.x + event.y * event.y + event.z * event.z > squaredMinimum
    })
    triggeredHandler<MoveEvent, MoveEvent>(triggerer, always, priority, { it }, action)
}

fun Listenable.onWorldChange(
    always: Boolean = false,
    priority: Byte = 0,
    action: (WorldEvent) -> Unit
) = handler<WorldEvent>(always, priority, action)
