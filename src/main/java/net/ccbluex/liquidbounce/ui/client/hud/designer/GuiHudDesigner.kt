/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.designer

import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.file.FileManager.hudConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.designer.EditorPanel.ElementEditableText
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.min

class GuiHudDesigner : GuiScreen() {

    private var editorPanel = EditorPanel(this, 2, 2)

    var selectedElement: Element? = null
        set(value) {
            if (elementEditableText?.element != value) {
                elementEditableText = null
            }
            field = value
        }
    private var buttonAction = false

    var elementEditableText: ElementEditableText? = null

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        editorPanel = EditorPanel(this, width / 2, height / 2)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        HUD.render(true)
        HUD.handleMouseMove(mouseX, mouseY)

        if (selectedElement !in HUD.elements)
            selectedElement = null

        val wheel = Mouse.getDWheel()

        editorPanel.drawPanel(mouseX, mouseY, wheel)

        if (wheel != 0) {
            for (element in HUD.elements) {
                if (element.isInBorder(
                        mouseX / element.scale - element.renderX,
                        mouseY / element.scale - element.renderY
                    )
                ) {
                    element.scale += if (wheel > 0) 0.05f else -0.05f
                    break
                }
            }
        }

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        if (buttonAction) {
            buttonAction = false
            return
        }

        HUD.handleMouseClick(mouseX, mouseY, mouseButton)

        if (!(mouseX in editorPanel.x..editorPanel.x + editorPanel.width
                    && mouseY in editorPanel.y..editorPanel.y + min(editorPanel.realHeight, 200))
        ) {
            selectedElement = null
            editorPanel.create = false
        }

        if (mouseButton == 0) {
            for (element in HUD.elements) {
                if (element.isInBorder(
                        mouseX / element.scale - element.renderX,
                        mouseY / element.scale - element.renderY
                    )
                ) {
                    selectedElement = element
                    break
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)

        HUD.handleMouseReleased()
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
        elementEditableText = null
        saveConfig(hudConfig)

        super.onGuiClosed()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_DELETE -> if (selectedElement != null) {
                HUD.removeElement(this, selectedElement!!)
            }

            Keyboard.KEY_ESCAPE -> {
                if (elementEditableText != null) {
                    elementEditableText = null
                } else {
                    selectedElement = null
                    editorPanel.create = false
                }
            }

            else -> HUD.handleKey(typedChar, keyCode)
        }

        elementEditableText?.chosenText?.processInput(typedChar, keyCode) { editorPanel.moveRGBAIndexBy(it) }

        super.keyTyped(typedChar, keyCode)
    }
}