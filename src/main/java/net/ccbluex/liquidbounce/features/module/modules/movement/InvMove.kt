/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object InvMove : Module("InventoryMove", Category.MOVEMENT, gameDetecting = false, hideModule = false) {

    private val fullMovements by BoolValue("FullMovements", false)
    private val notInChests by BoolValue("NotInChests", false)
    private val noDetectableValue by BoolValue("NoDetectable", false)
    private val noMoveClicksValue by BoolValue("NoMoveClicks", false)

    private val rotate by BoolValue("Rotate", false)

    private val intave by BoolValue("Intave", false)
    val aacAdditionPro by BoolValue("AACAdditionPro", false)

    private val isIntave = (mc.currentScreen is GuiInventory || mc.currentScreen is GuiChest) && intave

    private val noMove by InventoryManager.noMoveValue
    private val noMoveAir by InventoryManager.noMoveAirValue
    private val noMoveGround by InventoryManager.noMoveGroundValue
    private val undetectable by InventoryManager.undetectableValue

    private val silentlyCloseAndReopen by BoolValue("SilentlyCloseAndReopen", false) {
        noMove && (noMoveAir || noMoveGround)
    }

    private val reopenOnClick by BoolValue("ReopenOnClick", false) {
        silentlyCloseAndReopen && noMove && (noMoveAir || noMoveGround)
    }

    private val affectedBindings = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindLeft,
        mc.gameSettings.keyBindJump,
        mc.gameSettings.keyBindSprint
    )

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val screen = mc.currentScreen

        if (!fullMovements && (screen is GuiChat || screen is GuiIngameMenu)) return

        if (undetectable && screen != null && screen !is GuiHudDesigner && screen !is ClickGui) return

        if (notInChests && screen is GuiChest) return

        if (silentlyCloseAndReopen && screen is GuiInventory) {
            if (canClickInventory(closeWhenViolating = true) && !reopenOnClick)
                serverOpenInventory = true
        }

        for (affectedBinding in affectedBindings) {
            affectedBinding.pressed = isButtonPressed(affectedBinding) ||
                    (affectedBinding == mc.gameSettings.keyBindSprint && Sprint.handleEvents() && Sprint.mode == "Legit" && (!Sprint.onlyOnSprintPress || mc.thePlayer.isSprinting))
        }
    }

    private fun updateKeyState() {
        if (mc.currentScreen != null && mc.currentScreen !is GuiChat &&
            (!noDetectableValue || mc.currentScreen !is GuiContainer)
        ) {
            MovementUtils.updateControls()

            if (rotate) {
                when {
                    Keyboard.isKeyDown(Keyboard.KEY_UP) && mc.thePlayer.rotationPitch > -90 -> {
                        mc.thePlayer.rotationPitch -= 5
                    }
                    Keyboard.isKeyDown(Keyboard.KEY_DOWN) && mc.thePlayer.rotationPitch < 90 -> {
                        mc.thePlayer.rotationPitch += 5
                    }
                    Keyboard.isKeyDown(Keyboard.KEY_LEFT) -> {
                        mc.thePlayer.rotationYaw -= 5
                    }
                    Keyboard.isKeyDown(Keyboard.KEY_RIGHT) -> {
                        mc.thePlayer.rotationYaw += 5
                    }
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (isIntave) {
            mc.gameSettings.keyBindSneak.pressed = true
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (isIntave) event.cancelEvent()
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        if (!canClickInventory()) {
            event.cancelEvent()
        } else if (reopenOnClick) {
            serverOpenInventory = true
        }

        if (noMoveClicksValue && MovementUtils.isMoving) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        updateKeyState()
    }

    @EventTarget
    fun onScreen(event: ScreenEvent) {
        updateKeyState()
    }

    override fun onDisable() {
        for (affectedBinding in affectedBindings) {
            affectedBinding.pressed = isButtonPressed(affectedBinding)
        }
    }

    private fun isButtonPressed(keyBinding: KeyBinding): Boolean {
        return if (keyBinding.keyCode < 0) {
            Mouse.isButtonDown(keyBinding.keyCode + 100)
        } else {
            GameSettings.isKeyDown(keyBinding)
        }
    }

    override val tag
        get() = if (aacAdditionPro) "AACAdditionPro" else null
}
