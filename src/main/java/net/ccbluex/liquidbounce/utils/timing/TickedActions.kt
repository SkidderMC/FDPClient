/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.event.async.waitUntil
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.item.ItemStack
import java.util.concurrent.ConcurrentLinkedQueue

object TickedActions : Listenable {
    private class Action(
        val owner: Module,
        val id: Int,
        val action: Runnable
    )

    private val actions = ConcurrentLinkedQueue<Action>()

    private val calledThisTick = LinkedHashSet<Action>()

    private fun schedule(id: Int, module: Module, allowDuplicates: Boolean = false, action: Runnable) =
        if (allowDuplicates || !isScheduled(id, module)) {
            actions += Action(module, id, action)
            true
        } else false

    private fun isScheduled(id: Int, module: Module) =
        actions.any { it.owner == module && it.id == id && it !in calledThisTick }

    private fun clear(module: Module) = actions.removeIf { it.owner == module }

    private fun size(module: Module) = actions.count { it.owner == module }

    private fun isEmpty(module: Module) = size(module) == 0

    private val onTick = handler<GameTickEvent>(priority = 1) {
        // Prevent new scheduled ids from getting marked as duplicates even if they are going to be called next tick
        actions.toCollection(calledThisTick)

        calledThisTick.forEach {
            it.action.run()
            if (actions.isNotEmpty()) {
                actions.remove()
            }
        }

        calledThisTick.clear()
    }

    private val onWorld = handler<WorldEvent> {
        actions.clear()
    }

    /**
     * Perform window click with given parameters at next tick and run callback with click result.
     */
    inline fun Module.clickNextTick(
        slot: Int, button: Int, mode: Int,
        allowDuplicates: Boolean = false, windowId: Int = mc.thePlayer.openContainer.windowId,
        crossinline callback: (ItemStack?) -> Unit = {}
    ) = nextTick(slot, allowDuplicates) {
        val newStack = mc.playerController?.windowClick(windowId, slot, button, mode, mc.thePlayer)
        callback.invoke(newStack)
    }

    fun Module.nextTick(id: Int = -1, allowDuplicates: Boolean = true, action: Runnable) =
        schedule(id, this, allowDuplicates, action)

    suspend fun Module.awaitTicked() {
        waitUntil { hasNoTicked() }
    }

    fun Module.isTicked(id: Int) = isScheduled(id, this)

    fun Module.clearTicked() = clear(this)

    fun Module.countTicked() = size(this)

    fun Module.hasNoTicked() = isEmpty(this)
}
