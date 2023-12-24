/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO

class GuiBackground(private val prevGui: GuiScreen) : GuiScreen() {

    companion object {
        var enabled = true
        var particles = false
        var shader = false
    }

    private lateinit var enabledButton: GuiButton
    private lateinit var blurButton: GuiButton
    private lateinit var particlesButton: GuiButton
    private lateinit var typeButton: GuiButton
    private lateinit var lastButton: GuiButton
    private lateinit var nextButton: GuiButton
    private lateinit var animatedButton: GuiButton

    override fun initGui() {
        enabledButton = GuiButton(1, width / 2 - 100, height / 4 + 35, "")
        buttonList.add(enabledButton)
        blurButton = GuiButton(9, width / 2 - 100, height / 4, "")
        buttonList.add(blurButton)

        typeButton = GuiButton(5, width / 2 - 100, height / 4 + 40 + 25, "")
        buttonList.add(typeButton)
        lastButton = GuiButton(6, width / 2 - 100, height / 4 + 40 + 25 * 2, 20, 20, "<")
        buttonList.add(lastButton)
        nextButton = GuiButton(7, width / 2 + 80, height / 4 + 40 + 25 * 2, 20, 20, ">")
        buttonList.add(nextButton)
        animatedButton = GuiButton(8, width / 2 - 100, height / 4 + 40 + 25 * 3, "")
        buttonList.add(animatedButton)

        particlesButton = GuiButton(2, width / 2 - 100, height / 4 + 40 + 25 * 4, "")
        buttonList.add(particlesButton)
        buttonList.add(GuiButton(3, width / 2 - 100, height / 4 + 40 + 25 * 5, 98, 20, "%ui.background.change%"))
        buttonList.add(GuiButton(4, width / 2 + 2, height / 4 + 40 + 25 * 5, 98, 20, "%ui.background.reset%"))

        buttonList.add(GuiButton(0, width / 2 - 100, height / 4 + 40 + 25 * 6 + 10, "%ui.back%"))

        updateButtons()
    }

    private fun updateButtons() {
        enabledButton.displayString = "Status (${if (enabled) "ON" else "OFF"})"
        blurButton.displayString = " Shadow (${if (shader) "ON" else "OFF"})"
        particlesButton.displayString = "Particles (${if (particles) "ON" else "OFF"})"
        val hasCustomBackground = FDPClient.background != null
        lastButton.enabled = !hasCustomBackground
        nextButton.enabled = !hasCustomBackground
        typeButton.enabled = !hasCustomBackground
        animatedButton.enabled = !hasCustomBackground
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> {
                enabled = !enabled
            }
            9 -> {
                shader = !shader
            }
            2 -> {
                particles = !particles
            }
            3 -> {
                val file = MiscUtils.openFileChooser() ?: return
                if (file.isDirectory) return

                try {
                    Files.copy(file.toPath(), FileOutputStream(FDPClient.fileManager.backgroundFile))

                    val image = ImageIO.read(FileInputStream(FDPClient.fileManager.backgroundFile))
                    FDPClient.background = ResourceLocation(FDPClient.CLIENT_NAME.lowercase() + "/background.png")
                    mc.textureManager.loadTexture(FDPClient.background, DynamicTexture(image))
                } catch (e: Exception) {
                    e.printStackTrace()
                    MiscUtils.showErrorPopup("Error", "Exception class: " + e.javaClass.name + "\nMessage: " + e.message)
                    FDPClient.fileManager.backgroundFile.delete()
                }
            }
            4 -> {
                FDPClient.background = null
                FDPClient.fileManager.backgroundFile.delete()
            }
            // Remove the gradient-related cases (5, 6, 7, 8)

            0 -> mc.displayGuiScreen(prevGui)
        }

        updateButtons()

        FDPClient.fileManager.saveConfig(FDPClient.fileManager.specialConfig)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        mc.fontRendererObj.drawCenteredString("%ui.background%", this.width / 2F, height / 8F + 5F, 4673984, true)
        mc.fontRendererObj.drawCenteredString("%ui.background.gcurrent%: " + if(FDPClient.background == null) { "Customized" } else { "Customized" },
            this.width / 2F, height / 4 + 40 + 25 * 2f + (20 - mc.fontRendererObj.FONT_HEIGHT) * 0.5f, Color.WHITE.rgb, true)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        super.keyTyped(typedChar, keyCode)
    }
}