/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.event.GameLoopEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance

object WaitMsUtils : MinecraftInstance, Listenable {

    private val scheduledActions = mutableListOf<ScheduledAction>()

    fun schedule(requester: Any? = null, ms: Long = 1L, action: () -> Unit = { }) =
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

    val onRender = handler<GameLoopEvent>(priority = -1) {
        synchronized(scheduledActions) {
            scheduledActions.removeIf {
                System.currentTimeMillis() >= it.ms && it.action()
            }
        }
    }

    private data class ScheduledAction(val requester: Any?, val ms: Long, val action: () -> Boolean)

}