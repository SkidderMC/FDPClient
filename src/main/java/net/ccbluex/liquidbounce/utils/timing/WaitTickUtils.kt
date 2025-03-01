/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.kotlin.removeEach

//@Deprecated("Use TickScheduler instead")
object WaitTickUtils : MinecraftInstance, Listenable {

    private val scheduledActions = ArrayDeque<ScheduledAction>()

    inline fun schedule(ticks: Int, requester: Any? = null, crossinline action: () -> Unit = { }) =
        conditionalSchedule(requester, ticks, false) { action(); null }

    fun conditionalSchedule(
        requester: Any? = null,
        ticks: Int? = null,
        isConditional: Boolean = true,
        action: (tick: Int) -> Boolean?
    ) {
        if (ticks == 0) {
            action(0)

            return
        }

        val time = ticks ?: 0

        scheduledActions += ScheduledAction(requester, time, isConditional, ClientUtils.runTimeTicks + time, action)
    }

    fun hasScheduled(obj: Any) = scheduledActions.any { it.requester == obj }

    val onTick = handler<GameTickEvent>(priority = -1) {
        val currentTick = ClientUtils.runTimeTicks

        scheduledActions.removeEach { action ->
            val elapsed = action.duration - (action.ticks - currentTick)
            val shouldRemove = currentTick >= action.ticks

            if (!action.isConditional) {
                if (shouldRemove) {
                    action.action(elapsed) ?: true
                } else {
                    false
                }
            } else {
                action.action(elapsed) ?: shouldRemove
            }
        }
    }

    private data class ScheduledAction(
        val requester: Any?,
        val duration: Int,
        val isConditional: Boolean,
        val ticks: Int,
        val action: (tick: Int) -> Boolean?
    )

}