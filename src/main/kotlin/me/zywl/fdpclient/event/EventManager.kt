/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package me.zywl.fdpclient.event

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import java.lang.ref.WeakReference
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventManager : MinecraftInstance() {

    // Event registry
    private val registry = hashMapOf<Class<out Event>, MutableList<EventHook>>()

    /**
     * Registers a listener to handle events.
     *
     * @param listener The listener to register.
     */
    fun registerListener(listener: Listenable) {
        try {
            val listenerRef = WeakReference(listener)
            for (method in listener.javaClass.declaredMethods) {
                if (method.isAnnotationPresent(EventTarget::class.java) && method.parameterTypes.size == 1) {
                    method.isAccessible = true

                    val eventClass = method.parameterTypes[0] as? Class<out Event>
                    requireNotNull(eventClass) { "The method parameter is not a subclass of Event" }

                    val eventTarget = method.getAnnotation(EventTarget::class.java)
                    val invokableEventTargets = registry.getOrPut(eventClass) { mutableListOf() }
                    invokableEventTargets.add(
                        EventHook(
                            listenerRef,
                            method,
                            eventTarget
                        )
                    )
                    invokableEventTargets.sortByDescending { it.priority } // Sort by priority
                }
            }
        } catch (e: Exception) {
            logError("Error while registering the listener", e)
        }
    }

    /**
     * Unregisters a listener.
     *
     * @param listenable Listener to unregister.
     */
    fun unregisterListener(listenable: Listenable) {
        try {
            registry.values.forEach { it.removeIf { hook -> hook.listenerRef.get() == null || hook.listenerRef.get() === listenable } }
            registry.entries.removeIf { it.value.isEmpty() }
        } catch (e: Exception) {
            logError("Error while unregistering the listener", e)
        }
    }

    /**
     * Calls an event for the listeners.
     *
     * @param event Event to be called.
     */
    fun callEvent(event: Event) {
        try {
            registry[event.javaClass]?.let { targets ->
                targets.toList().forEach { invokableEventTarget ->
                    invokableEventTarget.listenerRef.get()?.let { listener ->
                        if (invokableEventTarget.isIgnoreCondition || listener.handleEvents()) {
                            invokableEventTarget.method.invoke(listener, event)
                        }
                    }
                }
            }
        } catch (e: Exception) {
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
        val logger: Logger = LoggerFactory.getLogger(EventManager::class.java)
        logger.error(message, throwable)
    }
}
