/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.FDPClient.keyBindManager
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.macro.Macro
import net.ccbluex.liquidbounce.handler.macro.MacroManager
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSemibold35
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSemibold40
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * @author liulihaocai
 * FDPClient
 */
class KeySelectUI(val info: KeyInfo) : PopUI("Select a module to bind") {
    private var str = ""
    private var modules = moduleManager.toList()
    private val singleHeight = 4F + fontSemibold35.height
    private var stroll = 0
    private var maxStroll = modules.size * singleHeight
    private val height = 8F + fontSemibold40.height + fontSemibold35.height + 0.5F

    override fun render() {
        // modules
        var yOffset = height - stroll + 5F
        if (str.startsWith(".")) {
            fontSemibold35.drawString("Press ENTER to add macro.", 8F, singleHeight + yOffset, Color.BLACK.rgb, false)
        } else {
            for (module in modules) {
                if (yOffset> (height - singleHeight) && (yOffset - singleHeight) <190) {
                    glPushMatrix()
                    glTranslatef(0F, yOffset, 0F)

                    val name = module.name
                    fontSemibold35.drawString(if (str.isNotEmpty()) {
                        "§0" + name.substring(0, str.length) + "§7" + name.substring(str.length, name.length)
                    } else { "§0$name" }, 8F, singleHeight * 0.5F, Color.BLACK.rgb, false)

                    glPopMatrix()
                }
                yOffset += singleHeight
            }
        }
        drawRect(0F, 8F + fontSemibold40.height, baseWidth.toFloat(), height + 5F, Color.WHITE.rgb)
        drawRect(0F, baseHeight - singleHeight, baseWidth.toFloat(), baseHeight.toFloat(), Color.WHITE.rgb)

        // search bar
        fontSemibold35.drawString(str.ifEmpty { "Search..." }, 8F, 8F + fontSemibold40.height + 4F, Color.LIGHT_GRAY.rgb, false)
        drawRect(8F, height + 2F, baseWidth - 8F, height + 3F, Color.LIGHT_GRAY.rgb)
    }

    override fun key(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_BACK) {
            if (str.isNotEmpty()) {
                str = str.substring(0, str.length - 1)
                update()
            }
            return
        } else if (keyCode == Keyboard.KEY_RETURN) {
            if (str.startsWith(".")) {
                MacroManager.macros.add(Macro(info.key, str))
                keyBindManager.updateAllKeys()
                close()
            } else if (modules.isNotEmpty()) {
                apply(modules[0])
            }
            return
        }

        if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            str += typedChar
            update()
        }
    }

    override fun stroll(mouseX: Float, mouseY: Float, wheel: Int) {
        val afterStroll = stroll - (wheel / 10)
        if (afterStroll> 0 && afterStroll <(maxStroll - 100)) {
            stroll = afterStroll
        }
    }

    override fun click(mouseX: Float, mouseY: Float) {
        if (mouseX <8 || mouseX> (baseWidth - 8) || mouseY <height || mouseY> (baseHeight - singleHeight)) {
            return
        }

        var yOffset = height - stroll + 2F
        for (module in modules) {
            if (mouseY> yOffset && mouseY <(yOffset + singleHeight)) {
                apply(module)
                break
            }
            yOffset += singleHeight
        }
    }

    private fun apply(module: Module) {
        module.keyBind = info.key
        keyBindManager.updateAllKeys()
        close()
    }

    override fun close() {
        keyBindManager.popUI = null
    }

    private fun update() {
        modules = if (str.isNotEmpty()) {
            moduleManager.filter { it.name.startsWith(str, ignoreCase = true) }
        } else {
            moduleManager.toList()
        }
        maxStroll = modules.size * singleHeight
        stroll = 0
    }
}