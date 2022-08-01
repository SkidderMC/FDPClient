/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import net.ccbluex.liquidbounce.utils.MinecraftInstance

class EventManager : MinecraftInstance() {

    private val registry = HashMap<Class<out Event>, MutableList<EventHook>>()

//    private val counter = HashMap<Class<out Event>, Int>()
//    private var lastSyncTime = System.currentTimeMillis()

    /**
     * Register [listener]
     */
    fun registerListener(listener: Listenable) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(EventTarget::class.java) && method.parameterTypes.size == 1) {
                try {
                    if (!method.isAccessible) {
                        method.isAccessible = true
                    }

                    val eventClass = method.parameterTypes[0] as Class<out Event>
                    val eventTarget = method.getAnnotation(EventTarget::class.java)

                    val invokableEventTargets = registry.getOrPut(eventClass) { mutableListOf() }
                    invokableEventTargets.add(EventHook(listener, method, eventTarget))
                    registry[eventClass] = invokableEventTargets
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    /**
     * Unregister listener
     *
     * @param listenable for unregister
     */
    fun unregisterListener(listenable: Listenable) {
        for ((key, targets) in registry) {
            targets.removeIf { it.eventClass == listenable }

            registry[key] = targets
        }
    }

//    private fun printProfiler() {
//        println("--- Event Profiler(${Date()}) ---")
//
//        var total = 0
//        for((key, value) in counter.toList().sortedBy { it.second }) {
//            println("${key.simpleName}: $value")
//            total += value
//        }
//        println("total: $total")
//
//        counter.clear()
//    }

    /**
     * Call event to listeners
     *
     * @param event to call
     */
    fun callEvent(event: Event) {
//        if(System.currentTimeMillis() - lastSyncTime > 1000) {
//            printProfiler()
//            lastSyncTime = System.currentTimeMillis()
//        }
//        counter[event.javaClass] = counter.getOrDefault(event.javaClass, 0) + 1

        val targets = registry[event.javaClass] ?: return
        try {
            for (invokableEventTarget in targets) {
                try {
                    if (!invokableEventTarget.eventClass.handleEvents() && !invokableEventTarget.isIgnoreCondition) {
                        continue
                    }

                    invokableEventTarget.method.invoke(invokableEventTarget.eventClass, event)
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            }
        }catch (e :Exception){
            e.printStackTrace();
        }
    }
}
