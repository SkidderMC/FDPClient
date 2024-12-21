/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.kotlin.waitUntil
import net.minecraft.item.ItemStack
import java.util.concurrent.ConcurrentLinkedQueue

object TickedActions : Listenable {
    private data class Action(
        val owner: Module,
        val id: Int,
        val action: () -> Unit
    )

    private val actions = ConcurrentLinkedQueue<Action>()

    private val calledThisTick = LinkedHashSet<Action>()

    fun schedule(id: Int, module: Module, allowDuplicates: Boolean = false, action: () -> Unit) =
        if (allowDuplicates || !isScheduled(id, module)) {
            actions += Action(module, id, action)
            true
        } else false

    fun isScheduled(id: Int, module: Module) =
        actions.any { it.owner == module && it.id == id && it !in calledThisTick }

    fun clear(module: Module) = actions.removeIf { it.owner == module }

    fun size(module: Module) = actions.count { it.owner == module }

    fun isEmpty(module: Module) = size(module) == 0

    val onTick = handler<GameTickEvent>(priority = 1) {
        // Prevent new scheduled ids from getting marked as duplicates even if they are going to be called next tick
        actions.toCollection(calledThisTick)

        for (triple in calledThisTick) {
            triple.action.invoke()
            if (actions.isNotEmpty()) {
                actions.remove()
            }
        }

        calledThisTick.clear()
    }

    val onWorld = handler<WorldEvent> {
        actions.clear()
    }

    class TickScheduler(val module: Module) : MinecraftInstance {
        fun schedule(id: Int, allowDuplicates: Boolean = false, action: () -> Unit) =
            schedule(id, module, allowDuplicates, action)

        inline fun scheduleClick(slot: Int, button: Int, mode: Int, allowDuplicates: Boolean = false, windowId: Int = mc.thePlayer.openContainer.windowId, crossinline action: (ItemStack?) -> Unit = {}) =
            schedule(slot, module, allowDuplicates) {
                val newStack = mc.playerController?.windowClick(windowId, slot, button, mode, mc.thePlayer)
                action.invoke(newStack)
            }

        operator fun plusAssign(action: () -> Unit) {
            schedule(-1, module, true, action)
        }

        // Schedule actions to be executed in following ticks, one each tick
        // Thread is frozen until all actions were executed (suitable for coroutines)
        suspend fun scheduleAndSuspend(vararg actions: () -> Unit) =
            actions.forEach {
                this += it
                waitUntil { isEmpty() }
            }

        suspend fun scheduleAndSuspend(id: Int = -1, allowDuplicates: Boolean = true, action: () -> Unit) {
            schedule(id, module, allowDuplicates, action)
            waitUntil { isEmpty() }
        }

        // Checks if id click is scheduled: if (id in TickScheduler)
        operator fun contains(id: Int) = isScheduled(id, module)

        fun clear() = clear(module)

        val size
            get() = size(module)

        fun isEmpty() = isEmpty(module)
    }
}