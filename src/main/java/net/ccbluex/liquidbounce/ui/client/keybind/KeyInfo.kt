/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.macroManager
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.macro.Macro
import net.ccbluex.liquidbounce.ui.font.Fonts.font35
import net.ccbluex.liquidbounce.ui.font.Fonts.font40
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSmall
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBindRect
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * @author Zywl And LiuLihaocai
 * FDPClient
 */
class KeyInfo(
    val posX: Float,
    val posY: Float,
    val width: Float,
    val height: Float,
    val key: Int,
    private val keyName: String,
    private val keyDisplayName: String
) : MinecraftInstance() {
    constructor(posX: Float, posY: Float, width: Float, height: Float, key: Int, keyName: String) :
            this(posX, posY, width, height, key, keyName, keyName)

    private val keyColor = Color(240, 240, 240).rgb
    private val shadowColor = Color(210, 210, 210).rgb
    private val unusedColor = Color(200, 200, 200).rgb
    private val usedColor = Color(0, 0, 0).rgb
    private val baseTabHeight = 150
    private val baseTabWidth = 100
    private val direction = posY >= 100

    private var modules = ArrayList<Module>()
    private var macros = ArrayList<Macro>()
    private var hasKeyBind = false
    private var stroll = 0
    private var maxStroll = 0

    fun render() {
        glPushMatrix()
        glTranslatef(posX, posY, 0F)
        drawRoundedBindRect(0F, 2F, width, height + 8, 6F, shadowColor)
        drawRoundedBindRect(0F, 0F, width, height, 6F, keyColor)
        font40.drawCenteredString(keyName, width * 0.5F, height * 0.9F * 0.5F - (font35.FONT_HEIGHT * 0.5F) + 3F, if (hasKeyBind) { usedColor } else { unusedColor }, false)
        glPopMatrix()
    }

    fun renderTab() {
        glPushMatrix()

        glTranslatef((posX + width * 0.5F) - baseTabWidth * 0.5F, if (direction) { posY - baseTabHeight } else { posY + height }, 0F)
        drawRoundedBindRect(0F, 0F, baseTabWidth.toFloat(), baseTabHeight.toFloat(), 4F, Color.WHITE.rgb)

        // render modules
        val fontHeight = 10F - font40.height * 0.5F
        var yOffset = (12F + font40.height + 10F) - stroll
        for (module in modules) {
            if (yOffset> 0 && (yOffset - 20) <100) {
                glPushMatrix()
                glTranslatef(0F, yOffset, 0F)

                fontSmall.drawString(module.name, 12F, fontHeight, Color.DARK_GRAY.rgb, false)
                font35.drawString(
                    "-", baseTabWidth - 12F - font40.getStringWidth("-"), fontHeight, Color.RED.rgb, false
                )

                glPopMatrix()
            }
            yOffset += 20
        }
        for (macro in macros) {
            if (yOffset> 0 && (yOffset - 20) <100) {
                glPushMatrix()
                glTranslatef(0F, yOffset, 0F)

                font40.drawString(macro.command, 12F, fontHeight, Color.DARK_GRAY.rgb, false)
                font35.drawString(
                    "-", baseTabWidth - 12F - font35.getStringWidth("-"), fontHeight, Color.RED.rgb, false
                )

                glPopMatrix()
            }
            yOffset += 20
            yOffset += 20
        }

        // cover the excess
        drawRoundedBindRect(0F, 0F, baseTabWidth.toFloat(), 12F + font40.height + 10F, 6F, Color.WHITE.rgb)
        drawRoundedBindRect(0F, baseTabHeight - 22F - font40.height, baseTabWidth.toFloat(), baseTabHeight.toFloat(), 6F, Color.WHITE.rgb)
        font40.drawString("Key $keyDisplayName", 12F, 12F, Color.BLACK.rgb, false)
        font40.drawString("Add", baseTabWidth - 12F - font40.getStringWidth("Add"), baseTabHeight - 12F - font40.height, Color(0, 191, 255).rgb,false)

        glPopMatrix()
    }

    fun stroll(mouseX: Float, mouseY: Float, wheel: Int) {
        val scaledMouseX = mouseX - ((posX + width * 0.5F) - baseTabWidth * 0.5F)
        val scaledMouseY = mouseY - (if (direction) { posY - baseTabHeight } else { posY + height })
        if (scaledMouseX <0 || scaledMouseY <0 || scaledMouseX> baseTabWidth || scaledMouseY> baseTabHeight) {
            return
        }

        val afterStroll = stroll - (wheel / 40)
        if (afterStroll> 0 && afterStroll <(maxStroll - 150)) {
            stroll = afterStroll
        }
    }

    fun update() {
        modules = moduleManager.getKeyBind(key) as ArrayList<Module>
        macros = macroManager.macros.filter { it.key == key } as ArrayList<Macro>
        hasKeyBind = (modules.size + macros.size)> 0
        stroll = 0
        maxStroll = modules.size * 30 + macros.size * 30
    }

    fun click(mouseX: Float, mouseY: Float) {
        val keyBindMgr = FDPClient.keyBindManager

        if (keyBindMgr.nowDisplayKey == null) {
            keyBindMgr.nowDisplayKey = this
            keyBindMgr.clicked = true
            mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.click"), 1F))
        } else {
            val scaledMouseX = mouseX - ((posX + width * 0.5F) - baseTabWidth * 0.5F)
            val scaledMouseY = mouseY - (if (direction) { posY - baseTabHeight } else { posY + height })
            if (scaledMouseX <0 || scaledMouseY <0 || scaledMouseX> baseTabWidth || scaledMouseY> baseTabHeight) {
                keyBindMgr.nowDisplayKey = null // close it when click out of area
                keyBindMgr.clicked = false
                return
            }

            if (scaledMouseY> 22F + font40.height &&
                scaledMouseX> baseTabWidth - 12F - font40.getStringWidth("Add")) {
                if (scaledMouseY> baseTabHeight - 22F - font35.height) {
                    keyBindMgr.popUI = KeySelectUI(this)
                } else {
                    var yOffset = (12F + font35.height + 10F) - stroll
                    for (module in modules) {
                        if (scaledMouseY> (yOffset + 5) && scaledMouseY <(yOffset + 15)) {
                            module.keyBind = Keyboard.KEY_NONE
                            update()
                            break
                        }
                        yOffset += 20
                    }
                   for (macro in macros) {
                        if (scaledMouseY> (yOffset + 5) && scaledMouseY <(yOffset + 15)) {
                            macroManager.macros.remove(macro)
                            update()
                            break
                        }
                        yOffset += 20
                    }
                }
            }
        }
    }
}