/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.ccbluex.liquidbounce.event.async.TickScheduler
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

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

internal inline fun <T : Any> createEventMap(valueSelector: (Class<out Event>) -> T): Map<Class<out Event>, T> =
    ALL_EVENT_CLASSES.associateWithTo(IdentityHashMap(ALL_EVENT_CLASSES.size), valueSelector)

/**
 * @author MukjepScarlet
 */
object EventManager : CoroutineScope by CoroutineScope(SupervisorJob()) {
    /**
     * All normal handlers (except of scripts) should be initialized at startup on the main thread
     */
    private val registry = createEventMap { CopyOnWriteArrayList<EventHook<in Event>>() }

    init {
        TickScheduler
    }

    fun <T : Event> unregisterEventHook(eventClass: Class<out T>, eventHook: EventHook<in T>): Boolean =
        registry[eventClass]!!.remove(eventHook)

    // Only called from main thread
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

    fun <T : Event> call(event: T): T {
        val eventClass = event::class.java

        val hooks = registry[eventClass]!!

        hooks.forEach {
            it.processEvent(event)
        }

        return event
    }

    fun <T : Event> call(event: T, listener: Listenable): T {
        val eventClass = event::class.java

        val hooks = registry[eventClass]!!

        hooks.forEach {
            if (it.owner === listener) {
                it.processEvent(event)
            }
        }

        return event
    }

}