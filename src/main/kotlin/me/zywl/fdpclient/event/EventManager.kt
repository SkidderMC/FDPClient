/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package me.zywl.fdpclient.event

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.ref.WeakReference

class EventManager : MinecraftInstance() {

    // Event registry
    private val registry = hashMapOf<Class<out Event>, MutableList<EventHook>>()

    /**
     * Registers a listener to handle events.
     *
     * @param listener The listener to register.
     */
    fun registerListener(listener: Listenable) {
        runCatching {
            val listenerRef = WeakReference(listener)
            for (method in listener.javaClass.declaredMethods) {
                if (method.isAnnotationPresent(EventTarget::class.java) && method.parameterTypes.size == 1) {
                    method.isAccessible = true

                    val eventClass = method.parameterTypes[0] as? Class<out Event>
                    requireNotNull(eventClass) { "The method parameter is not a subclass of Event" }

                    val eventTarget = method.getAnnotation(EventTarget::class.java)
                    val invokableEventTargets = registry.getOrPut(eventClass) { mutableListOf() }
                    val eventHook = EventHook(listenerRef, method, eventTarget)
                    invokableEventTargets.add(eventHook)
                    invokableEventTargets.sortByDescending { it.getPriority() } // Sort by priority
                }
            }
        }.onFailure { e ->
            logError("Error while registering the listener", e)
        }
    }

    /**
     * Unregisters a listener.
     *
     * @param listenable Listener to unregister.
     */
    fun unregisterListener(listenable: Listenable) {
        runCatching {
            registry.values.forEach { it.removeIf { hook -> hook.getListener() == null || hook.getListener() === listenable } }
            registry.entries.removeIf { it.value.isEmpty() }
        }.onFailure { e ->
            logError("Error while unregistering the listener", e)
        }
    }
    /**
     * Calls an event for the listeners.
     *
     * @param event Event to be called.
     */
    fun callEvent(event: Event) {
        runCatching {
            registry[event.javaClass]?.let { targets ->
                targets.forEach { eventHook ->
                    eventHook.invokeEvent(event)
                }
            }
        }.onFailure { e ->
            logError("Error while calling the event", e)
        }
    }

    /**
     * Logs an error.
     *
     * @param message Error message.
     * @param throwable Associated exception.
     */
    private fun logError(message: String, throwable: Throwable) {
        logger.error(message, throwable)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EventManager::class.java)
    }
}