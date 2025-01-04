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
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTexturedModalRect
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.font
import net.ccbluex.liquidbounce.config.choices
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@ElementInfo(name = "Effects")
class Effects(
    x: Double = 2.0,
    y: Double = 10.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    private val modeValue by choices("Mode", arrayOf("Classic", "FDP", "Default"), "Classic")
    private val font by font("Font", Fonts.font35)
    private val shadow by boolean("Shadow", true)

    private val iconValue by boolean("Icon", true)
    private val nameValue by boolean("Name", true)
    private val colorValue by boolean("Color", false)

    private val potionMap: MutableMap<Potion, PotionData> = HashMap()

    override fun drawElement(): Border {
        return when (modeValue) {
            "Default" -> drawDefaultMode()
            "Classic" -> drawClassicMode()
            "FDP"     -> drawFDPMode()
            else      -> Border(2F, font.FONT_HEIGHT.toFloat(), 0F, 0F)
        }
    }

    private fun drawDefaultMode(): Border {
        var maxWidth = 0f
        var yOffset = 0

        val activePotions = mc.thePlayer?.activePotionEffects ?: return Border(0F, 0F, 0F, 0F)
        if (activePotions.isEmpty()) return Border(0F, 0F, 0F, 0F)

        val sortedPotions = activePotions.sortedByDescending { it.duration }
        val fontRenderer = font
        val iconSize = 18

        for (potionEffect in sortedPotions) {
            val potion = Potion.potionTypes[potionEffect.potionID] ?: continue
            val rowHeight = fontRenderer.FONT_HEIGHT + 2

            if (iconValue && potion.hasStatusIcon()) {
                val tx = potion.statusIconIndex % 8 * iconSize
                val ty = 198 + potion.statusIconIndex / 8 * iconSize
                mc.textureManager.bindTexture(ResourceLocation("textures/gui/container/inventory.png"))
                // Draw icon at (x, y + yOffset)
                drawTexturedModalRect(
                    x.toInt(),
                    (y + yOffset).toInt(),
                    tx,
                    ty,
                    iconSize,
                    iconSize,
                    0f
                )
            }

            val textOffset = if (iconValue && potion.hasStatusIcon()) iconSize + 3 else 0

            if (nameValue) {
                val nameStr = buildString {
                    append(I18n.format(potion.name))
                    if (potionEffect.amplifier > 0) {
                        append(" ")
                        append(intToRoman(potionEffect.amplifier + 1))
                    }
                }
                val color = if (colorValue) potion.liquidColor else 0xFFFFFF
                fontRenderer.drawString(
                    nameStr,
                    (x + textOffset).toFloat(),
                    (y + yOffset).toFloat(),
                    color,
                    shadow
                )
                val usedWidth = textOffset + fontRenderer.getStringWidth(nameStr)
                if (usedWidth > maxWidth) maxWidth = usedWidth.toFloat()
            }

            val durationStr = Potion.getDurationString(potionEffect)
            fontRenderer.drawString(
                durationStr,
                (x + textOffset).toFloat(),
                (y + yOffset + fontRenderer.FONT_HEIGHT).toFloat(),
                0x7F7F7F,
                shadow
            )
            val usedWidthDur = textOffset + fontRenderer.getStringWidth(durationStr)
            if (usedWidthDur > maxWidth) maxWidth = usedWidthDur.toFloat()

            yOffset += rowHeight * 2
        }

        // Return a border that covers the potions drawn
        return Border(
            x.toFloat(),
            y.toFloat(),
            maxWidth,
            (yOffset - 2).coerceAtLeast(0).toFloat()
        )
    }

    private fun drawFDPMode(): Border {
        GlStateManager.pushMatrix()
        var yPos = 0

        val activePotions = mc.thePlayer?.activePotionEffects ?: return Border(0F, 0F, 120F, 30F)
        if (activePotions.isEmpty()) {
            GlStateManager.popMatrix()
            return Border(0F, 0F, 120F, 30F)
        }

        for (potionEffect in activePotions) {
            val potion = Potion.potionTypes[potionEffect.potionID] ?: continue
            val name = I18n.format(potion.name)

            val data: PotionData = potionMap[potion]?.takeIf { it.level == potionEffect.amplifier }
                ?: PotionData(TranslatePotionData(0F, -40F + yPos), potionEffect.amplifier).also {
                    potionMap[potion] = it
                }

            if (activePotions.none { it.amplifier == data.level }) {
                potionMap.remove(potion)
            }

            val (potionTime, potionMaxTime) = try {
                val (m, s) = Potion.getDurationString(potionEffect).split(":").map { it.toInt() }
                m to s
            } catch (ignored: Exception) {
                100 to 1000
            }

            val lifeTime = potionTime * 60 + potionMaxTime
            if (data.potionMaxTimer == 0 || lifeTime > data.potionMaxTimer) {
                data.potionMaxTimer = lifeTime
            }

            val state = (lifeTime / data.potionMaxTimer.toDouble() * 100.0).toFloat().coerceAtLeast(2.0F)
            data.translate.interpolate(0F, yPos.toFloat(), 0.1)
            data.potionAnimationX = getAnimationState(
                data.potionAnimationX.toDouble(),
                (1.2F * state).toDouble(),
                max(10.0F, abs(data.potionAnimationX - 1.2F * state) * 15.0F) * 0.3
            ).toFloat()

            RenderUtils.drawRected(
                0F,
                data.translate.y,
                120F,
                data.translate.y + 30F,
                potionlpha(ColorUtils.potionColor.GREY.c, 0.1F)
            )
            RenderUtils.drawRected(
                0F,
                data.translate.y,
                data.potionAnimationX,
                data.translate.y + 30F,
                potionlpha(Color(34, 24, 20).brighter().rgb, 0.3F)
            )
            RenderUtils.drawShadow(
                0F,
                data.translate.y.roundToInt().toFloat(),
                120F,
                30F
            )

            val pY = data.translate.y + 13F
            font.drawString(
                "$name ${intToRoman(potionEffect.amplifier + 1)}",
                29,
                (pY - mc.fontRendererObj.FONT_HEIGHT).roundToInt(),
                potionlpha(ColorUtils.potionColor.WHITE.c, 0.8F)
            )
            Fonts.font35.drawString(
                Potion.getDurationString(potionEffect),
                29F,
                pY + 4.0F,
                potionlpha(Color(200, 200, 200).rgb, 0.5F)
            )

            if (potion.hasStatusIcon()) {
                GlStateManager.pushMatrix()
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDepthMask(false)
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
                val idx = potion.statusIconIndex
                mc.textureManager.bindTexture(ResourceLocation("textures/gui/container/inventory.png"))
                mc.ingameGUI.drawTexturedModalRect(
                    6F,
                    (data.translate.y + 1).roundToInt().toFloat(),
                    idx % 8 * 18,
                    198 + idx / 8 * 18,
                    18,
                    18
                )
                GL11.glDepthMask(true)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GlStateManager.popMatrix()
            }

            yPos -= 35
        }

        GlStateManager.popMatrix()
        return Border(0F, 0F, 120F, 30F)
    }

    private fun drawClassicMode(): Border {
        var yPos = 0F
        var widest = 0F
        val lineHeight = ((font as? GameFontRenderer)?.height ?: font.FONT_HEIGHT).toFloat()

        assumeNonVolatile {
            val activeEffects = mc.thePlayer?.activePotionEffects ?: return@assumeNonVolatile
            if (activeEffects.isEmpty()) return@assumeNonVolatile

            for (effect in activeEffects) {
                val potion = Potion.potionTypes[effect.potionID] ?: continue
                val roman = when {
                    effect.amplifier == 1 -> "II"
                    effect.amplifier == 2 -> "III"
                    effect.amplifier == 3 -> "IV"
                    effect.amplifier == 4 -> "V"
                    effect.amplifier == 5 -> "VI"
                    effect.amplifier == 6 -> "VII"
                    effect.amplifier == 7 -> "VIII"
                    effect.amplifier == 8 -> "IX"
                    effect.amplifier == 9 -> "X"
                    effect.amplifier > 10 -> "X+"
                    else -> "I"
                }

                val fullStr = "${I18n.format(potion.name)} $roman§f: §7${Potion.getDurationString(effect)}"
                val strWidth = font.getStringWidth(fullStr).toFloat()
                if (strWidth > widest) {
                    widest = strWidth
                }

                font.drawString(
                    fullStr,
                    -(strWidth),
                    yPos,
                    potion.liquidColor,
                    shadow
                )
                yPos -= lineHeight
            }
        }

        if (widest == 0F) widest = 40F
        if (yPos == 0F) yPos = -10F

        return Border(2F, lineHeight, -widest - 2F, yPos + lineHeight - 2F)
    }

    private fun potionlpha(colorInt: Int, alpha: Float): Int {
        val baseColor = Color(colorInt)
        return Color(
            baseColor.red / 255f,
            baseColor.green / 255f,
            baseColor.blue / 255f,
            alpha
        ).rgb
    }

    private fun intToRoman(num: Int): String {
        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val symbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        var tmp = num
        val sb = StringBuilder()
        var i = 0
        while (i < values.size && tmp >= 0) {
            while (values[i] <= tmp) {
                tmp -= values[i]
                sb.append(symbols[i])
            }
            i++
        }
        return sb.toString()
    }

    private fun getAnimationState(current: Double, target: Double, speed: Double): Double {
        val inc = 0.01 * speed
        return when {
            current < target -> min(current + inc, target)
            current > target -> max(current - inc, target)
            else -> target
        }
    }

    private class PotionData(
        val translate: TranslatePotionData,
        val level: Int
    ) {
        var potionMaxTimer: Int = 0
        var potionAnimationX: Float = 0.0F
    }

    private data class TranslatePotionData(var x: Float, var y: Float) {
        fun interpolate(tX: Float, tY: Float, speed: Double) {
            x = (x + (tX - x) * speed).toFloat()
            y = (y + (tY - y) * speed).toFloat()
        }
    }
}