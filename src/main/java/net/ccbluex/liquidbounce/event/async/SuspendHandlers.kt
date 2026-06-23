/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import net.ccbluex.liquidbounce.event.Event
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import java.util.ArrayDeque

enum class SuspendHandlerBehavior {
    PARALLEL,
    QUEUE,
    CANCEL_PREVIOUS,
    DISCARD_LATEST
}

@PublishedApi
internal class SuspendEventController<T : Event>(
    private val owner: Listenable,
    private val dispatcher: CoroutineDispatcher,
    private val always: Boolean,
    private val behavior: SuspendHandlerBehavior,
    private val action: suspend CoroutineScope.(T) -> Unit
) {
    private val queue = ArrayDeque<T>()
    private var activeJob: Job? = null

    fun submit(event: T) {
        when (behavior) {
            SuspendHandlerBehavior.PARALLEL -> launch(event)
            SuspendHandlerBehavior.CANCEL_PREVIOUS -> {
                activeJob?.cancel()
                activeJob = launch(event)
            }
            SuspendHandlerBehavior.DISCARD_LATEST -> {
                if (activeJob?.isActive != true) activeJob = launch(event)
            }
            SuspendHandlerBehavior.QUEUE -> enqueue(event)
        }
    }

    private fun enqueue(event: T) {
        synchronized(queue) {
            queue.addLast(event)
            if (activeJob?.isActive == true) return
            activeJob = owner.launchSequence(dispatcher, always) {
                while (true) {
                    val next = synchronized(queue) {
                        if (queue.isEmpty()) null else queue.removeFirst()
                    } ?: break
                    action(next)
                }
            }
        }
    }

    private fun launch(event: T): Job = owner.launchSequence(dispatcher, always) { action(event) }
}

inline fun <reified T : Event> Listenable.suspendHandler(
    behavior: SuspendHandlerBehavior = SuspendHandlerBehavior.CANCEL_PREVIOUS,
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    always: Boolean = false,
    priority: Byte = 0,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) {
    val controller = SuspendEventController<T>(this, dispatcher, always, behavior) { event: T -> action(event) }
    handler<T>(always, priority) { controller.submit(it) }
}
