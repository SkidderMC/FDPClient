/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event.async

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * Wait until given [condition] returns true.
 *
 * @param condition It will be called on [Dispatchers.Main] (the Render thread)
 * @return Total ticks during waiting
 */
suspend inline fun waitUntil(
    crossinline condition: () -> Boolean
): Int = suspendCancellableCoroutine { cont ->
    var waitingTick = 0
    TickScheduler.schedule {
        waitingTick++
        try {
            if (condition()) {
                cont.resume(waitingTick)
                true
            } else {
                false
            }
        } catch (e: Throwable) {
            cont.resumeWithException(e)
            true
        }
    }
}

/**
 * Wait for given [ticks].
 */
suspend fun waitTicks(ticks: Int) {
    require(ticks >= 0) { "Negative tick: $ticks" }

    if (ticks == 0) {
        return
    }

    var remainingTick = ticks
    waitUntil { --remainingTick == 0 }
}

/**
 * Waits until the fixed amount of [ticks] ran out or the [callback] returns true.
 */
suspend inline fun waitConditional(
    ticks: Int,
    crossinline callback: (elapsedTicks: Int) -> Boolean
): Boolean {
    require(ticks >= 0) { "Negative tick: $ticks" }

    if (ticks == 0) {
        return true
    }

    var elapsedTicks = 0
    // `elapsedTicks` in 0 until `ticks`
    waitUntil { elapsedTicks >= ticks || callback(elapsedTicks++) }

    return elapsedTicks >= ticks
}