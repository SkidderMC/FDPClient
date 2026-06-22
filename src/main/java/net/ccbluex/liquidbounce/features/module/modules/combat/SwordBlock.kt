package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemSword

/**
 * Keeps the right-click block active while holding a sword, so the block
 * animation/use stays up during automatic attacks.
 *
 * @author Zywl
 */
object SwordBlock : Module("SwordBlock", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Always", "WhileAttacking"), "WhileAttacking")
        .describe("When to keep the sword block active.")
    private val keepWhileManualUse by boolean("KeepWhileManualUse", true)
        .describe("Do not override a manual right-click hold.")

    private var forcedBlocking = false

    override fun onDisable() {
        releaseBlock()
    }

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        val holdingSword = thePlayer.heldItem?.item is ItemSword
        val manualUse = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)

        // Never fight against a manual right-click hold.
        if (manualUse && keepWhileManualUse) {
            forcedBlocking = false
            return@handler
        }

        val shouldBlock = holdingSword && when (mode) {
            "Always" -> true
            else -> KillAura.handleEvents() || KillAura.blockStatus
        }

        if (shouldBlock) {
            forcedBlocking = true
            mc.gameSettings.keyBindUseItem.pressed = true
        } else {
            releaseBlock()
        }
    }

    private fun releaseBlock() {
        if (!forcedBlocking) return

        forcedBlocking = false

        // Only drop the key if the user is not physically holding it.
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
            mc.gameSettings.keyBindUseItem.pressed = false
        }
    }
}
