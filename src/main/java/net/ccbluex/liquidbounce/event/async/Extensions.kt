/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event.async

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.event.*
import kotlin.properties.ReadOnlyProperty

/**
 * Start a tick sequence job for given [Listenable]
 * which will be cancelled if [Listenable.handleEvents] of the owner returns false
 */
fun Listenable.launchSequence(
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    always: Boolean = false,
    body: suspend CoroutineScope.() -> Unit
) {
    val job = EventManager.launch(dispatcher, block = body)

    TickScheduler.schedule {
        if (!always && !this@launchSequence.handleEvents()) {
            job.cancel()
            true
        } else {
            job.isCompleted
        }
    }
}

/**
 * Start a **looped** tick sequence job for given [Listenable]
 * which will be cancelled if [Listenable.handleEvents] of the owner returns false
 */
fun <T : Listenable> T.loopSequence(
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    always: Boolean = false,
    priority: Byte = 0,
    body: suspend CoroutineScope.() -> Unit
): ReadOnlyProperty<Any?, Job?> {
    var job: Job? = null

    handler<GameTickEvent>(always = true, priority) {
        if (!always && !this@loopSequence.handleEvents()) {
            job?.cancel()
            job = null
        } else if (job == null || !job!!.isActive) {
            job = EventManager.launch(dispatcher, block = body)
        }
    }

    return ReadOnlyProperty { _, _ -> job }
}