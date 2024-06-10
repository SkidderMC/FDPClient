/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package me.zywl.fdpclient.event

import java.lang.ref.WeakReference
import java.lang.reflect.Method

interface Listenable {
    fun handleEvents(): Boolean = true
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventTarget(val ignoreCondition: Boolean = false, val priority: Int = 0)

internal class EventHook(private val listenerRef: WeakReference<Listenable>, private val method: Method, eventTarget: EventTarget) {
    private val isIgnoreCondition = eventTarget.ignoreCondition
    private val priority = eventTarget.priority

    constructor(listenerRef: WeakReference<Listenable>, method: Method) : this(listenerRef, method, method.getAnnotation(EventTarget::class.java))

    fun getListener(): Listenable? = listenerRef.get()

    fun getPriority(): Int = priority

    fun invokeEvent(event: Event) {
        getListener()?.let { listener ->
            if (isIgnoreCondition || listener.handleEvents()) {
                method.invoke(listener, event)
            }
        }
    }
}
