/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.InvMove
import net.ccbluex.liquidbounce.injection.forge.mixins.gui.MixinGuiContainerAccessor
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Mouse
import java.awt.Color

object BetterInventory : Module("BetterInventory", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val moveWhileOpen by boolean("MoveWhileOpen", true)
        .describe("Keep walking, sprinting and jumping while an inventory or chest screen is open.")

    val highlightClicked by boolean("HighlightClicked", true)
        .describe("Highlight the last slot you clicked.")
    val highlightMode by choices("HighlightMode", arrayOf("Border", "Fill"), "Border") { highlightClicked }
        .describe("Draw the highlight as a border or a fill.")
    val highlightColor by color("HighlightColor", Color(0, 255, 0)) { highlightClicked }
        .describe("Color used for the slot highlight.")
    val borderWidth by float("BorderWidth", 2F, 1F..5F) { highlightClicked && highlightMode == "Border" }
        .describe("Thickness of the highlight border.")

    var clickedSlot = -1
        private set

    private var leftHeld = false
    private var rightHeld = false
    private var drivingMovement = false

    private val movementBindings = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindLeft,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindJump,
        mc.gameSettings.keyBindSprint
    )

    val onUpdate = handler<UpdateEvent> {
        val screen = mc.currentScreen as? GuiContainer ?: run {
            clickedSlot = -1
            leftHeld = false
            rightHeld = false
            releaseMovement()
            return@handler
        }

        driveMovement()

        val leftDown = Mouse.isButtonDown(0)
        val rightDown = Mouse.isButtonDown(1)

        if ((leftDown && !leftHeld) || (rightDown && !rightHeld)) {
            val slot = (screen as MixinGuiContainerAccessor).hoveredSlot
            if (slot != null) {
                clickedSlot = slot.slotNumber
            }
        }

        leftHeld = leftDown
        rightHeld = rightDown
    }

    /** Presses the movement bindings while a container is open, yielding to the dedicated InvMove module. */
    private fun driveMovement() {
        if (!moveWhileOpen || InvMove.handleEvents()) {
            releaseMovement()
            return
        }
        drivingMovement = true
        for (binding in movementBindings) binding.pressed = isPhysicallyDown(binding)
    }

    private fun releaseMovement() {
        if (!drivingMovement) return
        drivingMovement = false
        for (binding in movementBindings) binding.pressed = isPhysicallyDown(binding)
    }

    private fun isPhysicallyDown(binding: KeyBinding): Boolean =
        if (binding.keyCode < 0) Mouse.isButtonDown(binding.keyCode + 100) else GameSettings.isKeyDown(binding)

    override fun onDisable() {
        releaseMovement()
    }
}
