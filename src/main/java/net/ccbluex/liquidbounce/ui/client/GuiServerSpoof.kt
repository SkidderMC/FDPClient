package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.special.ServerSpoof
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard

class GuiServerSpoof(private val prevGui: GuiScreen) : GuiScreen() {

    private lateinit var textField: GuiTextField
    private lateinit var stat: GuiButton

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        textField = GuiTextField(2, mc.fontRendererObj, width / 2 - 100, 60, 200, 20)
        textField.isFocused = true
        textField.text = ServerSpoof.address
        textField.maxStringLength = 114514
        buttonList.add(GuiButton(2, width / 2 - 100, height / 4 + 96, "STATUS").also { stat = it })
        buttonList.add(GuiButton(0, width / 2 - 100, height / 4 + 120, "%ui.back%"))
        updateButtonStat()
    }

    private fun updateButtonStat() {
        stat.displayString = "%ui.status%: " + if (ServerSpoof.enable) "§a%ui.on%" else "§c%ui.off%"
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        drawCenteredString(mc.fontRendererObj, "%ui.serverSpoof%", width / 2, 34, 0xffffff)
        textField.drawTextBox()
        if (textField.text.isEmpty() && !textField.isFocused) {
            drawString(mc.fontRendererObj, "§7%ui.serverSpoof.address%", width / 2 - 100, 66, 0xffffff)
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> {
                ServerSpoof.address = textField.text
                mc.displayGuiScreen(prevGui)
            }
            2 -> {
                ServerSpoof.enable = !ServerSpoof.enable
            }
        }
        updateButtonStat()
        FDPClient.fileManager.saveConfig(FDPClient.fileManager.specialConfig)
    }

    override fun onGuiClosed() {
        ServerSpoof.address = textField.text
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        if (textField.isFocused) {
            textField.textboxKeyTyped(typedChar, keyCode)
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        textField.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun updateScreen() {
        textField.updateCursorCounter()
        super.updateScreen()
    }
}