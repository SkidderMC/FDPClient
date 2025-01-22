/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.isFirstInventoryClick
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ITEM
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object AutoSoup : Module("AutoSoup", Category.PLAYER) {

    private val health by float("Health", 15f, 0f..20f)
    private val delay by int("Delay", 150, 0..500)

    private val openInventory by boolean("OpenInv", true)
    private val startDelay by int("StartDelay", 100, 0..1000) { openInventory }
    private val autoClose by boolean("AutoClose", false) { openInventory }
    private val autoCloseNoSoup by boolean("AutoCloseNoSoup", true) { autoClose }
    private val autoCloseDelay by int("CloseDelay", 500, 0..1000) { openInventory && autoClose }

    private val simulateInventory by boolean("SimulateInventory", false) { !openInventory }

    private val bowl by choices("Bowl", arrayOf("Drop", "Move", "Stay"), "Drop")

    private val timer = MSTimer()
    private val startTimer = MSTimer()
    private val closeTimer = MSTimer()

    private var canCloseInventory = false

    override val tag
        get() = health.toString()

    val onGameTick = handler<GameTickEvent>(priority = -1) {
        val thePlayer = mc.thePlayer ?: return@handler

        if (!timer.hasTimePassed(delay))
            return@handler

        val soupInHotbar = InventoryUtils.findItem(36, 44, Items.mushroom_stew)

        if (thePlayer.health <= health && soupInHotbar != null) {
            SilentHotbar.selectSlotSilently(this, soupInHotbar, 1, true)

            thePlayer.sendUseItem(thePlayer.inventory.mainInventory[SilentHotbar.currentSlot])

            // Schedule slot switch the next tick as we violate vanilla logic if we do it now.
            nextTick {
                if (bowl == "Drop") {
                    if (!SilentHotbar.isSlotModified(this)) {
                        SilentHotbar.selectSlotSilently(this, soupInHotbar, 0, true)
                    }

                    sendPacket(C07PacketPlayerDigging(DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                }

                SilentHotbar.resetSlot(this)
            }

            timer.reset()
            return@handler
        }

        val bowlInHotbar = InventoryUtils.findItem(36, 44, Items.bowl)

        if (bowl == "Move" && bowlInHotbar != null) {
            if (openInventory && mc.currentScreen !is GuiInventory)
                return@handler

            var bowlMovable = false

            for (i in 9..36) {
                val itemStack = thePlayer.inventory.getStackInSlot(i)

                if (itemStack == null || (itemStack.item == Items.bowl && itemStack.stackSize < 64)) {
                    bowlMovable = true
                    break
                }
            }

            if (bowlMovable) {
                if (simulateInventory)
                    serverOpenInventory = true

                mc.playerController.windowClick(0, bowlInHotbar, 0, 1, thePlayer)
            }
        }

        val soupInInventory = InventoryUtils.findItem(9, 35, Items.mushroom_stew)

        if (soupInInventory != null && InventoryUtils.hasSpaceInHotbar()) {
            if (isFirstInventoryClick && !startTimer.hasTimePassed(startDelay)) {
                // GuiInventory checks, have to be put separately due to problem with reseting timer.
                if (mc.currentScreen is GuiInventory)
                    return@handler
            } else {
                // GuiInventory checks, have to be put separately due to problem with reseting timer.
                if (mc.currentScreen is GuiInventory)
                    isFirstInventoryClick = false

                startTimer.reset()
            }

            if (openInventory && mc.currentScreen !is GuiInventory)
                return@handler

            canCloseInventory = false

            if (simulateInventory)
                serverOpenInventory = true

            mc.playerController.windowClick(0, soupInInventory, 0, 1, thePlayer)

            if (simulateInventory && mc.currentScreen !is GuiInventory)
                serverOpenInventory = false

            timer.reset()
            closeTimer.reset()
        } else {
            canCloseInventory = true
        }

        if (autoClose && canCloseInventory && closeTimer.hasTimePassed(autoCloseDelay)) {
            if (!autoCloseNoSoup && soupInInventory == null) return@handler

            if (mc.currentScreen is GuiInventory) {
                mc.thePlayer?.closeScreen()
            }

            closeTimer.reset()
            canCloseInventory = false
        }
    }
}