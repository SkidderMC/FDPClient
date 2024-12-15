/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

/**
 * @see List.binarySearchBy
 */
private fun List<EventHook<*>>.findIndexByPriority(item: EventHook<*>): Int {
    var low = 0
    var high = size - 1

    while (low <= high) {
        val mid = (low + high).ushr(1)
        val midVal = get(mid)

        if (item.priority < midVal.priority)
            low = mid + 1
        else if (item.priority > midVal.priority)
            high = mid - 1
        else
            return mid
    }

    return low.inv()
}
/**
 * This manager will start a job for each hook.
 *
 * Once the job is finished, the next [UpdateEvent] (any stateless event is OK for this) will start a new one.
 *
 * This is designed to run **asynchronous** tasks instead of tick loops.
 *
 * @author opZywl
 */
internal object LoopManager : Listenable, CoroutineScope by CoroutineScope(SupervisorJob()) {
    private val registry = IdentityHashMap<EventHook.Async<UpdateEvent>, Job?>()

    operator fun plusAssign(eventHook: EventHook.Async<UpdateEvent>) {
        registry[eventHook] = null
    }

    operator fun minusAssign(eventHook: EventHook.Async<UpdateEvent>) {
        registry.remove(eventHook)
    }

    init {
        handler<UpdateEvent>(priority = Byte.MAX_VALUE) {
            for ((eventHook, job) in registry) {
                if (eventHook.isActive) {
                    if (job == null || !job.isActive) {
                        registry[eventHook] = launch(eventHook.dispatcher) {
                            try {
                                eventHook.action(this, UpdateEvent)
                            } catch (e: CancellationException) {
                                // The job is canceled due to handler is no longer active
                                return@launch
                            } catch (e: Exception) {
                                ClientUtils.LOGGER.error("Exception during loop in", e)
                            }
                        }
                    }
                } else if (job != null) {
                    job.cancel()
                    registry[eventHook] = null
                }
            }
        }
    }
}
/**
 * @author opZywl
 */
object EventManager : CoroutineScope by CoroutineScope(SupervisorJob()) {
    private val registry = ALL_EVENT_CLASSES.associateWithTo(IdentityHashMap(ALL_EVENT_CLASSES.size)) {
        ArrayList<EventHook<in Event>>()
    }

    init {
        LoopManager
    }

    fun <T : Event> unregisterEventHook(eventClass: Class<out T>, eventHook: EventHook<in T>) {
        registry[eventClass]!!.remove(eventHook)
    }

    fun <T : Event> registerEventHook(eventClass: Class<out T>, eventHook: EventHook<T>): EventHook<T> {
        val container = registry[eventClass] ?: error("Unsupported Event type: ${eventClass.simpleName}")

        eventHook as EventHook<in Event>

        check(eventHook !in container) {
            "The EventHook of ${eventHook.owner} has already been registered"
        }

        val insertIndex = container.findIndexByPriority(eventHook).let {
            if (it < 0) it.inv() else it
        }
        container.add(insertIndex, eventHook)

        return eventHook
    }

    fun unregisterListener(listener: Listenable) {
        registry.values.forEach { it.removeIf { hook -> hook.owner == listener } }
    }

    private fun <T : Event> EventHook<T>.processEvent(event: T) {
        if (!this.isActive)
            return

        when (this) {
            is EventHook.Blocking -> {
                try {
                    action(event)
                } catch (e: Exception) {
                    ClientUtils.LOGGER.error("Exception during call event (blocking)", e)
                }
            }

            is EventHook.Async -> {
                launch(dispatcher) {
                    try {
                        action(this, event)
                    } catch (e: Exception) {
                        ClientUtils.LOGGER.error("Exception during call event (async)", e)
                    }
                }
            }
        }
    }

    fun <T : Event> call(event: T): T {
        val hooks = registry[event.javaClass]!!

        hooks.forEach {
            it.processEvent(event)
        }

        return event
    }

    fun <T : Event> call(event: T, listener: Listenable): T {
        val hooks = registry[event.javaClass]!!

        hooks.forEach {
            if (it.owner == listener) {
                it.processEvent(event)
            }
        }

        return event
    }

}
