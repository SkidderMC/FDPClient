/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.handler.lang.translationMenu
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.newCall
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.IOException

class GuiServerStatus(private val prevGui: GuiScreen) : AbstractScreen() {
    private val status = hashMapOf<String, String?>(
        "https://api.mojang.com" to null,
        "https://authserver.mojang.com" to null,
        "https://session.minecraft.net" to null,
        "https://textures.minecraft.net" to null,
        "https://minecraft.net" to null,
        "https://account.mojang.com" to null,
        "https://sessionserver.mojang.com" to null,
        "https://mojang.com" to null
    )

    override fun initGui() {
        +GuiButton(1, width / 2 - 100, height / 4 + 145, "Back")

        loadInformation()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        assumeNonVolatile {
            drawBackground(0)

            var i = height / 4 + 40
            drawRect(
                width / 2f - 115,
                i - 5f,
                width / 2f + 115,
                height / 4f + 43 + if (status.keys.isEmpty()) 10 else status.keys.size * Fonts.fontSemibold40.fontHeight,
                Integer.MIN_VALUE
            )

            for (server in status.keys) {
                val color = status[server] ?: "yellow"
                Fonts.fontSemibold40.drawCenteredStringWithShadow(
                    "${server.replaceFirst("^http[s]?://".toRegex(), "")}: ${
                        if (color.equals(
                                "red",
                                ignoreCase = true
                            )
                        ) "§c" else if (color.equals("yellow", ignoreCase = true)) "§e" else "§a"
                    }${
                        if (color.equals("red", ignoreCase = true)) "Offline" else if (color.equals(
                                "yellow",
                                ignoreCase = true
                            )
                        ) "Loading..." else "Online"
                    }", width / 2f, i.toFloat(), Color.WHITE.rgb
                )
                i += Fonts.fontSemibold40.fontHeight
            }

            Fonts.fontBold180.drawCenteredString(
                translationMenu("serverStatus"),
                width / 2F,
                height / 8f + 5F,
                4673984,
                true
            )
        }

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun loadInformation() {
        for (url in status.keys) {
            status[url] = null
            screenScope.launch(Dispatchers.IO) {
                try {
                    status[url] = HttpClient.newCall {
                        url(url).head()
                    }.execute().use {
                        if (it.code in 200..499) "green" else "red"
                    }
                } catch (e: IOException) {
                    status[url] = "red"
                }
            }
        }
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 1) mc.displayGuiScreen(prevGui)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        super.keyTyped(typedChar, keyCode)
    }
}