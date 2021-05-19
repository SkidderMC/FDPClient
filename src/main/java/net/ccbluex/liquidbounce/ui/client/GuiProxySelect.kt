package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.special.proxy.ProxyManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard

class GuiProxySelect(private val prevGui: GuiScreen) : GuiScreen() {
    private var textField: GuiTextField? = null
    private var type: GuiButton? = null

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)

        type = GuiButton(2, width / 2 - 100, height / 4 + 96, "TYPE")
        buttonList.add(type)

        buttonList.add(GuiButton(0, width / 2 - 100, height / 4 + 120, "Back"))

        textField = GuiTextField(1, Fonts.font40, width / 2 - 100, 60, 200, 20)
        textField!!.isFocused = true
        textField!!.text = ProxyManager.address
        textField!!.maxStringLength = 114514

        updateButtonStat()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        drawRect(30, 30, width - 30, height - 30, Int.MIN_VALUE)
        drawCenteredString(Fonts.font40, "ProxyManager", width / 2, 34, 0xffffff)

        textField!!.drawTextBox()
        if (textField!!.text.isEmpty() && !textField!!.isFocused)
            drawString(Fonts.font40, "§7Proxy Address", width / 2 - 100, 66, 0xffffff)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> {
                ProxyManager.address=textField!!.text
                mc.displayGuiScreen(prevGui)
            }
            2 -> {
                ProxyManager.type=when(ProxyManager.type){
                    ProxyManager.Type.DISABLE -> ProxyManager.Type.HTTP
                    ProxyManager.Type.HTTP -> ProxyManager.Type.SOCKS
                    ProxyManager.Type.SOCKS -> ProxyManager.Type.DISABLE
                }
            }
        }

        updateButtonStat()
        LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.valuesConfig)
    }

    private fun updateButtonStat() {
        type!!.displayString = "Type: ${ProxyManager.type.displayName}"
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }
        if (textField!!.isFocused) textField!!.textboxKeyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        textField!!.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun updateScreen() {
        textField!!.updateCursorCounter()
        super.updateScreen()
    }
}