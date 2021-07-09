/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.features.module.modules.client.LegitSpoof
import net.ccbluex.liquidbounce.utils.MinecraftInstance

class EventManager : MinecraftInstance() {

    private val registry = HashMap<Class<out Event>, MutableList<EventHook>>()

    /**
     * Register [listener]
     */
    fun registerListener(listener: Listenable) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(EventTarget::class.java) && method.parameterTypes.size == 1) {
                if (!method.isAccessible)
                    method.isAccessible = true

                val eventClass = method.parameterTypes[0] as Class<out Event>
                val eventTarget = method.getAnnotation(EventTarget::class.java)

                val invokableEventTargets = registry.getOrDefault(eventClass, ArrayList())
                invokableEventTargets.add(EventHook(listener, method, eventTarget))
                registry[eventClass] = invokableEventTargets
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

    /**
     * Call event to listeners
     *
     * @param event to call
     */
    fun callEvent(event: Event) {
        if(event is Render2DEvent||event is Render3DEvent){
            val legitSpoof=LiquidBounce.moduleManager[LegitSpoof::class.java] as LegitSpoof
            if(legitSpoof.state&&legitSpoof.render.get()){
                if(event is Render2DEvent) {
                    val hud=LiquidBounce.moduleManager[HUD::class.java] as HUD
                    if(hud.state)
                        hud.onRender2D(event)
                }
                return
            }
        }

        val targets = registry[event.javaClass] ?: return

        for (invokableEventTarget in targets) {
            try {
                if (!invokableEventTarget.eventClass.handleEvents() && !invokableEventTarget.isIgnoreCondition)
                    continue

                invokableEventTarget.method.invoke(invokableEventTarget.eventClass, event)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }
}
