/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance

object WaitTickUtils : MinecraftInstance(), Listenable {

    private val scheduledActions = mutableListOf<ScheduledAction>()

    fun schedule(ticks: Int, requester: Any? = null, action: () -> Unit = { }) =
        conditionalSchedule(requester, ticks) { action(); true }

    fun conditionalSchedule(requester: Any? = null, ticks: Int? = null, action: () -> Boolean) {
        if (ticks == 0) {
            action()

            return
        }

        scheduledActions += ScheduledAction(requester, ClientUtils.runTimeTicks + (ticks ?: 0), action)
    }

    fun hasScheduled(obj: Any) = scheduledActions.firstOrNull { it.requester == obj } != null

    @EventTarget(priority = -1)
    fun onTick(event: GameTickEvent) {
        val currentTick = ClientUtils.runTimeTicks
        val iterator = scheduledActions.iterator()

        while (iterator.hasNext()) {
            val scheduledAction = iterator.next()

            if (currentTick >= scheduledAction.ticks && scheduledAction.action()) {
                iterator.remove()
            }
        }
    }

    private data class ScheduledAction(val requester: Any?, val ticks: Int, val action: () -> Boolean)

}