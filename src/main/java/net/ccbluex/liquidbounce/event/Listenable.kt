/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

interface Listenable {
    fun handleEvents() = true

    val subListeners: Array<Listenable>
        get() = emptyArray()

    val parent: Listenable?
        get() = null

    fun unregister() {
        EventManager.unregisterListener(this)
        subListeners.forEach { it.unregister() }
    }
}

inline fun <reified T : Event> Listenable.handler(
    always: Boolean = false,
    priority: Byte = 0,
    noinline action: (T) -> Unit
) {
    EventManager.registerEventHook(T::class.java, EventHook.Blocking(this, always, priority, action))
}

inline fun <reified T : Event> Listenable.handler(
    dispatcher: CoroutineDispatcher,
    always: Boolean = false,
    priority: Byte = 0,
    noinline action: suspend CoroutineScope.(T) -> Unit
) {
    EventManager.registerEventHook(T::class.java, EventHook.Async(this, dispatcher, always, priority, action))
}

fun Listenable.loopHandler(
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    always: Boolean = false,
    priority: Byte = 0,
    action: suspend CoroutineScope.(UpdateEvent) -> Unit
) {
    LoopManager += EventHook.Async(this, dispatcher, always, priority, action)
}