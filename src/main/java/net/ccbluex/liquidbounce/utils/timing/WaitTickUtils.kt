/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance

object WaitTickUtils : MinecraftInstance(), Listenable {

    private val scheduledActions = ArrayDeque<ScheduledAction>()

    fun schedule(ticks: Int, requester: Any? = null, action: () -> Unit = { }) =
        conditionalSchedule(requester, ticks) { action(); null }

    fun conditionalSchedule(requester: Any? = null, ticks: Int? = null, action: (Int) -> Boolean?) {
        if (ticks == 0) {
            action(0)

            return
        }

        val time = ticks ?: 0

        scheduledActions += ScheduledAction(requester, time, ClientUtils.runTimeTicks + time, action)
    }

    fun hasScheduled(obj: Any) = scheduledActions.firstOrNull { it.requester == obj } != null

    val onTick = handler<GameTickEvent>(priority = -1) {
        val currentTick = ClientUtils.runTimeTicks
        val iterator = scheduledActions.iterator()

        while (iterator.hasNext()) {
            val action = iterator.next()

            if (action.action(action.duration - (action.ticks - currentTick)) ?: (currentTick >= action.ticks)) {
                iterator.remove()
            }
        }
    }

    private data class ScheduledAction(val requester: Any?, val duration: Int, val ticks: Int, val action: (Int) -> Boolean?)

}