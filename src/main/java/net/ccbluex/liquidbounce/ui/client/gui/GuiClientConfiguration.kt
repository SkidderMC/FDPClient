/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FDPClient.clientTitle
import net.ccbluex.liquidbounce.FDPClient.background
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.file.FileManager.backgroundImageFile
import net.ccbluex.liquidbounce.file.FileManager.backgroundShaderFile
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.altsLength
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.altsPrefix
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.customBackground
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.overrideLanguage
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.particles
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.stylisedAlts
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.unformattedAlts
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration.updateClientWindow
import net.ccbluex.liquidbounce.handler.lang.LanguageManager
import net.ccbluex.liquidbounce.handler.lang.translationMenu
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.io.FileFilters
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.io.MiscUtils.showErrorPopup
import net.ccbluex.liquidbounce.utils.io.MiscUtils.showMessageDialog
import net.ccbluex.liquidbounce.utils.render.IconUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.shader.Background
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraftforge.fml.client.config.GuiSlider
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display
import java.awt.Color

class GuiClientConfiguration(val prevGui: GuiScreen) : AbstractScreen() {

    private lateinit var languageButton: GuiButton

    private lateinit var backgroundButton: GuiButton
    private lateinit var particlesButton: GuiButton
    private lateinit var altsModeButton: GuiButton
    private lateinit var unformattedAltsButton: GuiButton
    private lateinit var altsSlider: GuiSlider

    private lateinit var titleButton: GuiButton

    private lateinit var altPrefixField: GuiTextField

    override fun initGui() {
        // Title button
        // Location > 1st row
        titleButton = +GuiButton(
            4, width / 2 - 100, height / 4 + 25, "Client title (${if (ClientConfiguration.clientTitle) "On" else "Off"})"
        )

        languageButton = +GuiButton(
            7,
            width / 2 - 100,
            height / 4 + 50,
            "Language (${overrideLanguage.ifBlank { "Game" }})"
        )

        // Background configuration buttons
        // Button location > 2nd row
        backgroundButton = +GuiButton(
            0,
            width / 2 - 100,
            height / 4 + 25 + 75,
            "Enabled (${if (customBackground) "On" else "Off"})"
        )

        particlesButton = +GuiButton(
            1, width / 2 - 100, height / 4 + 25 + 75 + 25, "Particles (${if (particles) "On" else "Off"})"
        )

        +GuiButton(2, width / 2 - 100, height / 4 + 25 + 75 + 25 * 2, 98, 20, "Change wallpaper")

        +GuiButton(3, width / 2 + 2, height / 4 + 25 + 75 + 25 * 2, 98, 20, "Reset wallpaper")

        // AltManager configuration buttons
        // Location > 3rd row
        altsModeButton = +GuiButton(
            6,
            width / 2 - 100,
            height / 4 + 25 + 185,
            "Random alts mode (${if (stylisedAlts) "Stylised" else "Legacy"})"
        )

        altsSlider = +GuiSlider(
            -1,
            width / 2 - 100,
            height / 4 + 210 + 25,
            200,
            20,
            "${if (stylisedAlts && unformattedAlts) "Random alt max" else "Random alt"} length (",
            ")",
            6.0,
            16.0,
            altsLength.toDouble(),
            false,
            true
        ) {
            altsLength = it.valueInt
        }

        unformattedAltsButton = +GuiButton(
            5,
            width / 2 - 100,
            height / 4 + 235 + 25,
            "Unformatted alt names (${if (unformattedAlts) "On" else "Off"})"
        ).also {
            it.enabled = stylisedAlts
        }

        altPrefixField = GuiTextField(2, Fonts.fontSemibold35, width / 2 - 100, height / 4 + 260 + 25, 200, 20)
        altPrefixField.maxStringLength = 16

        // Back button
        +GuiButton(8, width / 2 - 100, height / 4 + 25 + 25 + 25 * 11, "Back")
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> {
                customBackground = !customBackground
                backgroundButton.displayString = "Enabled (${if (customBackground) "On" else "Off"})"
            }

            1 -> {
                particles = !particles
                particlesButton.displayString = "Particles (${if (particles) "On" else "Off"})"
            }

            4 -> {
                ClientConfiguration.clientTitle = !ClientConfiguration.clientTitle
                titleButton.displayString = "Client title (${if (ClientConfiguration.clientTitle) "On" else "Off"})"
                updateClientWindow()
            }

            5 -> {
                unformattedAlts = !unformattedAlts
                unformattedAltsButton.displayString = "Unformatted alt names (${if (unformattedAlts) "On" else "Off"})"
                altsSlider.dispString = "${if (unformattedAlts) "Max random alt" else "Random alt"} length ("
                altsSlider.updateSlider()
            }

            6 -> {
                stylisedAlts = !stylisedAlts
                altsModeButton.displayString = "Random alts mode (${if (stylisedAlts) "Stylised" else "Legacy"})"
                altsSlider.dispString =
                    "${if (stylisedAlts && unformattedAlts) "Max random alt" else "Random alt"} length ("
                altsSlider.updateSlider()
                unformattedAltsButton.enabled = stylisedAlts
            }

            2 -> {
                val file = MiscUtils.openFileChooser(FileFilters.IMAGE, FileFilters.SHADER) ?: return

                // Delete old files
                background = null
                if (backgroundImageFile.exists()) backgroundImageFile.deleteRecursively()
                if (backgroundShaderFile.exists()) backgroundShaderFile.deleteRecursively()

                // Copy new file
                val fileExtension = file.extension

                background = try {
                    val destFile = when (fileExtension.lowercase()) {
                        "png" -> backgroundImageFile
                        "frag", "glsl", "shader" -> backgroundShaderFile
                        else -> {
                            showMessageDialog("Error", "Invalid file extension: $fileExtension")
                            return
                        }
                    }

                    file.copyTo(destFile)

                    // Load new background
                    Background.fromFile(destFile)
                } catch (e: Exception) {
                    e.showErrorPopup()
                    if (backgroundImageFile.exists()) backgroundImageFile.deleteRecursively()
                    if (backgroundShaderFile.exists()) backgroundShaderFile.deleteRecursively()
                    null
                }
            }

            3 -> {
                background = null
                if (backgroundImageFile.exists()) backgroundImageFile.deleteRecursively()
                if (backgroundShaderFile.exists()) backgroundShaderFile.deleteRecursively()
            }

            7 -> {
                val languageIndex = LanguageManager.knownLanguages.indexOf(overrideLanguage)

                overrideLanguage = when (languageIndex) {
                    -1 -> {
                        // If the language is not found, set it to the first language
                        LanguageManager.knownLanguages.first()
                    }
                    LanguageManager.knownLanguages.size - 1 -> {
                        // If the language is the last one, set it to blank
                        ""
                    }
                    else -> {
                        // Otherwise, set it to the next language
                        LanguageManager.knownLanguages[languageIndex + 1]
                    }
                }

                languageButton.displayString = "Language (${overrideLanguage.ifBlank { "Game" }})"
            }

            8 -> mc.displayGuiScreen(prevGui)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        Fonts.fontBold180.drawCenteredString(
            translationMenu("configuration"), width / 2F, height / 8F + 5F, 4673984, true
        )

        Fonts.fontSemibold40.drawString(
            "Window", width / 2F - 98F, height / 4F + 15F, 0xFFFFFF, true
        )

        Fonts.fontSemibold40.drawString(
            "Background", width / 2F - 98F, height / 4F + 90F, 0xFFFFFF, true
        )
        Fonts.fontSemibold35.drawString(
            "Supported background types: (.png, .frag, .glsl)",
            width / 2F - 98F,
            height / 4F + 100 + 25 * 3,
            0xFFFFFF,
            true
        )

        Fonts.fontSemibold40.drawString(
            translationMenu("altManager"), width / 2F - 98F, height / 4F + 200F, 0xFFFFFF, true
        )

        altPrefixField.drawTextBox()
        if (altPrefixField.text.isEmpty() && !altPrefixField.isFocused) {
            Fonts.fontSemibold35.drawStringWithShadow(
                altsPrefix.ifEmpty { translationMenu("altManager.typeCustomPrefix") },
                altPrefixField.xPosition + 4f,
                altPrefixField.yPosition + (altPrefixField.height - Fonts.fontSemibold35.FONT_HEIGHT) / 2F,
                0xffffff
            )
        }

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        if (altPrefixField.isFocused) {
            altPrefixField.textboxKeyTyped(typedChar, keyCode)
            altsPrefix = altPrefixField.text
            saveConfig(valuesConfig)
        }

        super.keyTyped(typedChar, keyCode)
    }

    public override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        altPrefixField.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun onGuiClosed() {
        saveConfig(valuesConfig)
        super.onGuiClosed()
    }
}