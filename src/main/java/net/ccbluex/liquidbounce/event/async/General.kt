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

    cont.invokeOnCancellation {
        TickScheduler.cancel(cont)
    }

    val scheduled = TickScheduler.scheduleConditional(requester = cont) {
        if (!cont.isActive) {
            return@scheduleConditional true
        }

        waitingTick++
        runCatching(condition).fold(
            onSuccess = { complete ->
                if (complete) cont.resume(waitingTick)
                complete
            },
            onFailure = { throwable ->
                cont.resumeWithException(throwable)
                true
            },
        )
    }

    if (!scheduled && cont.isActive) {
        cont.resumeWithException(IllegalStateException("Tick scheduler capacity exhausted"))
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
    var interrupted = false
    waitUntil {
        elapsedTicks++
        interrupted = callback(elapsedTicks)
        interrupted || elapsedTicks >= ticks
    }

    return !interrupted && elapsedTicks >= ticks
}

/** Waits a fixed amount of client-tick seconds. */
suspend fun waitSeconds(seconds: Int) {
    require(seconds >= 0) { "Negative seconds: $seconds" }
    waitTicks(Math.multiplyExact(seconds, 20))
}
