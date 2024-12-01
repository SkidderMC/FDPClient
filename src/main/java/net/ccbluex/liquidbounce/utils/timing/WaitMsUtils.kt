/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameLoopEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.utils.MinecraftInstance

object WaitMsUtils : MinecraftInstance(), Listenable {

    private val scheduledActions = mutableListOf<ScheduledAction>()

    fun schedule(ms: Long, requester: Any? = null, action: () -> Unit = { }) =
        conditionalSchedule(requester, ms) { action(); true }

    fun conditionalSchedule(requester: Any? = null, ms: Long? = null, action: () -> Boolean) {
        if (ms == 0L) {
            action()

            return
        }

        synchronized(scheduledActions) {
            scheduledActions += ScheduledAction(requester, System.currentTimeMillis() + (ms ?: 0L), action)
        }
    }

    fun hasScheduled(obj: Any) = scheduledActions.firstOrNull { it.requester == obj } != null

    @EventTarget(priority = -1)
    fun onRender(event: GameLoopEvent) {
        synchronized(scheduledActions) {
            scheduledActions.removeIf {
                System.currentTimeMillis() >= it.ms && it.action()
            }
        }
    }

    private data class ScheduledAction(val requester: Any?, val ms: Long, val action: () -> Boolean)

}