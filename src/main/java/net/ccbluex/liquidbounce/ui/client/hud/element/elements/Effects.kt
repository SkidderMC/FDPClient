/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTexturedModalRect
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * CustomHUD effects element
 *
 * Shows a list of active potion effects
 */
@ElementInfo(name = "Effects")
class Effects(
    x: Double = 2.0,
    y: Double = 10.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    private val modeValue by ListValue("Mode", arrayOf("Classic", "FDP"), "Classic")
    private val font by FontValue("Font", Fonts.font35)
    private val shadow by BoolValue("Shadow", true)

    private val iconValue by BoolValue("Icon", true)
    private val nameValue by BoolValue("Name", true)
    private val colorValue by BoolValue("Color", false)

    private val potionMap: MutableMap<Potion, PotionData> = HashMap()

    override fun drawElement(): Border {
        return when (modeValue) {
            "Default" -> drawDefaultMode()
            "Classic" -> drawClassicMode()
            "FDP" -> drawFDPMode()
            else -> Border(2F, font.FONT_HEIGHT.toFloat(), 0F, 0F)
        }
    }

    private fun drawDefaultMode(): Border {
        val xOffset = 0
        var yOffset = 0

        val activePotions = mc.thePlayer.activePotionEffects
        val sortedPotions = activePotions.sortedByDescending { it.duration }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.disableLighting()

        val fontRenderer = font

        for (potion in sortedPotions) {
            val effect = Potion.potionTypes[potion.potionID] ?: continue

            if (effect.hasStatusIcon() && iconValue) {
                drawStatusIcon(xOffset, yOffset, effect.statusIconIndex % 8 * 18, 198 + effect.statusIconIndex / 8 * 18)
            }

            if (nameValue) {
                drawPotionName(potion, effect, xOffset, yOffset, fontRenderer)
            }

            drawPotionDuration(potion, xOffset, yOffset, fontRenderer)

            yOffset += fontRenderer.FONT_HEIGHT * 2 + 4
        }

        val height = (yOffset - 4).toFloat()
        val width = 100.0f
        return Border(0F, 0F, width, height)
    }

    private fun drawFDPMode(): Border {
        GlStateManager.pushMatrix()
        var y = 0

        for (potionEffect in mc.thePlayer.activePotionEffects) {
            val potion = Potion.potionTypes[potionEffect.potionID] ?: continue
            val name = I18n.format(potion.name)

            val potionData: PotionData = potionMap[potion]?.takeIf { it.level == potionEffect.amplifier }
                ?: PotionData(TranslatePotionData(0F, -40F + y), potionEffect.amplifier).also {
                    potionMap[potion] = it
                }

            if (mc.thePlayer.activePotionEffects.none { it.amplifier == potionData.level }) {
                potionMap.remove(potion)
            }

            val (potionTime, potionMaxTime) = try {
                val timeSplit = Potion.getDurationString(potionEffect).split(":").map { it.toInt() }
                timeSplit[0] to timeSplit[1]
            } catch (ignored: Exception) {
                100 to 1000
            }

            val lifeTime = potionTime * 60 + potionMaxTime
            if (potionData.potionMaxTimer == 0 || lifeTime > potionData.potionMaxTimer) potionData.potionMaxTimer = lifeTime

            val state = (lifeTime / potionData.potionMaxTimer.toDouble() * 100.0).toFloat().coerceAtLeast(2.0F)
            potionData.translate.interpolate(0F, y.toFloat(), 0.1)
            potionData.potionAnimationX = getAnimationState(
                potionData.potionAnimationX.toDouble(),
                (1.2F * state).toDouble(),
                max(10.0F, abs(potionData.potionAnimationX - 1.2F * state) * 15.0F) * 0.3
            ).toFloat()

            RenderUtils.drawRected(0F, potionData.translate.y, 120F, potionData.translate.y + 30F, potionlpha(ColorUtils.potionColor.GREY.c, 0.1F))
            RenderUtils.drawRected(0F, potionData.translate.y, potionData.potionAnimationX, potionData.translate.y + 30F, potionlpha(Color(34, 24, 20).brighter().rgb, 0.3F))
            RenderUtils.drawShadow(0F, potionData.translate.y.roundToInt().toFloat(), 120F, 30F)
            val posY = potionData.translate.y + 13F
            font.drawString("$name ${intToRoman(potionEffect.amplifier + 1)}", 29, (posY - mc.fontRendererObj.FONT_HEIGHT).roundToInt(), potionlpha(ColorUtils.potionColor.WHITE.c, 0.8F))
            Fonts.font35.drawString(Potion.getDurationString(potionEffect), 29F, posY + 4.0F, potionlpha(Color(200, 200, 200).rgb, 0.5F))

            if (potion.hasStatusIcon()) {
                GlStateManager.pushMatrix()
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDepthMask(false)
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
                val statusIconIndex = potion.statusIconIndex
                mc.textureManager.bindTexture(ResourceLocation("textures/gui/container/inventory.png"))
                mc.ingameGUI.drawTexturedModalRect(6F, (potionData.translate.y + 1).roundToInt().toFloat(), statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18)
                GL11.glDepthMask(true)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GlStateManager.popMatrix()
            }

            y -= 35
        }

        GlStateManager.popMatrix()
        return Border(0F, 0F, 120F, 30F)
    }

    private fun drawClassicMode(): Border {
        var y = 0F
        var width = 0F

        assumeNonVolatile = true

        for (effect in mc.thePlayer.activePotionEffects) {
            val potion = Potion.potionTypes[effect.potionID] ?: continue
            val level = intToRoman(effect.amplifier + 1)
            val name = "${I18n.format(potion.name)} $level§f: §7${Potion.getDurationString(effect)}"

            val stringWidth = font.getStringWidth(name).toFloat()
            if (width < stringWidth) width = stringWidth

            font.drawString(name, -stringWidth, y, potion.liquidColor, shadow)
            y -= font.FONT_HEIGHT
        }

        assumeNonVolatile = false
        if (width == 0F) width = 40F
        if (y == 0F) y = -10F

        return Border(2F, font.FONT_HEIGHT.toFloat(), -width - 2F, y + font.FONT_HEIGHT - 2F)
    }

    private fun intToRoman(num: Int): String {
        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val symbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        var number = num
        val stringBuilder = StringBuilder()
        var i = 0
        while (i < values.size && number >= 0) {
            while (values[i] <= number) {
                number -= values[i]
                stringBuilder.append(symbols[i])
            }
            i++
        }
        return stringBuilder.toString()
    }

    private fun getAnimationState(animation: Double, finalState: Double, speed: Double): Double {
        val add = 0.01 * speed
        return when {
            animation < finalState -> min(animation + add, finalState)
            animation > finalState -> max(animation - add, finalState)
            else -> finalState
        }
    }

    private fun drawStatusIcon(xOffset: Int, yOffset: Int, textureX: Int, textureY: Int) {
        mc.textureManager.bindTexture(ResourceLocation("textures/gui/container/inventory.png"))
        drawTexturedModalRect(x.toInt() + xOffset - 20, y.toInt() + yOffset, textureX, textureY, 18, 18, 0f)
    }

    private fun drawPotionName(
        potion: PotionEffect,
        effect: Potion,
        xOffset: Int,
        yOffset: Int,
        fontRenderer: FontRenderer
    ) {
        fontRenderer.drawString(
            I18n.format(effect.name) + if (potion.amplifier > 0) " " + intToRoman(potion.amplifier + 1) else "",
            (x.toInt() + xOffset).toFloat(), (y.toInt() + yOffset).toFloat(),
            if (colorValue) effect.liquidColor else 0xFFFFFF,
            shadow
        )
    }

    private fun drawPotionDuration(
        potion: PotionEffect,
        xOffset: Int,
        yOffset: Int,
        fontRenderer: FontRenderer
    ) {
        fontRenderer.drawString(Potion.getDurationString(potion),
            (x.toInt() + xOffset).toFloat(), (y.toInt() + yOffset + 10).toFloat(), 0x7F7F7F, shadow)
    }

    private fun potionlpha(n: Int, n2: Float): Int {
        val color = Color(n)
        return Color(0.003921569f * color.red, 0.003921569f * color.green, 0.003921569f * color.blue, n2).rgb
    }

    private class PotionData(
        val translate: TranslatePotionData,
        val level: Int
    ) {
        var potionMaxTimer: Int = 0
        var potionAnimationX: Float = 0.0F
    }

    private data class TranslatePotionData(var x: Float, var y: Float) {
        fun interpolate(targetX: Float, targetY: Float, speed: Double) {
            x = (x + (targetX - x) * speed).toFloat()
            y = (y + (targetY - y) * speed).toFloat()
        }
    }
}