/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.entity.Render
import net.minecraft.item.ItemStack
import kotlin.math.roundToInt

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Armor")
class Armor(x: Double = -8.0, y: Double = 57.0, scale: Float = 1F,
            side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    private val enchantValue by BoolValue("Enchant", true)
    private val modeValue by ListValue("Alignment", arrayOf("Horizontal", "Vertical"), "Vertical")
    private val showAttributes by ListValue("Attributes", arrayOf("None", "Value", "Percentage", "All"), "Percentage")
    private val minimalMode by BoolValue("Minimal Mode", false)
    private val percentageY by IntegerValue("PositionY", -19, -50..50)
    private val percentageX by IntegerValue("PositionX", 21, -50..50)
    var borderThickness by IntegerValue("Border", 0, 0..255)
    private val red by IntegerValue("Red", 255, 0..255)
    private val green by IntegerValue("Green", 255, 0..255)
    private val blue by IntegerValue("Blue", 255, 0..255)
    var secondaryRed by IntegerValue("Red-2", 0, 0..255)
    var secondaryGreen by IntegerValue("Green-2", 19, 0..255)
    var secondaryBlue by IntegerValue("Blue-2", 0, 0..255)
    private val alpha by IntegerValue("Alpha", 255, 0..255)
    private val mc = Minecraft.getMinecraft()

    override fun drawElement(): Border {
        val mode = modeValue
        val player = mc.thePlayer

        if (mc.playerController.isInCreativeMode) {
            return if (mode.equals("Horizontal", ignoreCase = true)) {
                Border(0.0f, 0.0f, 72.0f, 17.0f)
            } else {
                Border(0.0f, 0.0f, 18.0f, 72.0f)
            }
        }

        val color: Int = java.awt.Color(red, green, blue, alpha).rgb
        val isInsideWater = mc.thePlayer.isInsideOfMaterial(Material.water)
        val yOffset = if (isInsideWater) -10 else 0

        if (mode.equals("Horizontal", ignoreCase = true)) {
            drawHorizontal(x.toInt(), yOffset, color, player)
        } else if (mode.equals("Vertical", ignoreCase = true)) {
            drawVertical(x.toInt(), yOffset, color, player)
        }

        return if (mode.equals("Horizontal", ignoreCase = true)) {
            Border(0.0f, 0.0f, 72.0f, 17.0f)
        } else {
            Border(0.0f, 0.0f, 18.0f, 72.0f)
        }
    }

    private fun drawHorizontal(x: Int, y: Int, color: Int, player: EntityPlayerSP) {
        enableCull()
        drawBackgroundWithContour(x, y, color)
        drawArmorItems(x, y, color, player)
    }

    private fun drawVertical(x: Int, y: Int, color: Int, player: EntityPlayerSP) {
        enableCull()
        drawBackgroundWithContourVertical(x, y, color)
        drawArmorItemsVertical(x, y, color, player)
    }

    private fun drawBackgroundWithContour(x: Int, y: Int, color: Int) {
        val primaryColor = java.awt.Color(red, green, blue, alpha)
        val secondaryColor = java.awt.Color(secondaryRed, secondaryGreen, secondaryBlue, borderThickness)

      /*  Render.drawGradientRound(
            x - 2.0f,
            y - 12.0f,
            75.0f,
            40.0f,
            borderRadius,
            ColorUtils.applyOpacity(secondaryColor, 0.85f),
            primaryColor,
            secondaryColor,
            primaryColor
        )

       */

        Fonts.minecraftFont.drawString("Armor", x, (y - 8.0f).roundToInt(), color)
    }

    private fun drawArmorItems(x: Int, y: Int, color: Int, player: EntityPlayerSP) {
        var xOffset = x
        val renderItem = mc.renderItem

        for (i in 3 downTo 0) {
            val stack = player.inventory.armorInventory[i] ?: continue

            renderItem.renderItemIntoGUI(stack, xOffset, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, xOffset, y)

            drawAttributes(xOffset, y, color, stack)

            xOffset += 18
        }

        enableAlpha()
        enableBlend()
        disableLighting()
        disableCull()
    }

    private fun drawAttributes(x: Int, y: Int, color: Int, stack: ItemStack) {
        pushMatrix()
        when (showAttributes) {
            "Value" -> {
                val valueText = (stack.maxDamage - stack.itemDamage).toString()
                drawText(x, y, valueText, color)
            }
            "Percentage" -> {
                val percentage = ((stack.maxDamage - stack.itemDamage).toFloat() / stack.maxDamage * 100).format()
                drawText(x, y, percentage, color)
            }
            "All" -> {
                val value = stack.maxDamage - stack.itemDamage
                val percentage = ((value.toFloat() / stack.maxDamage) * 100).format()
                val text = "$value/${stack.maxDamage} ($percentage%)"
                drawText(x, y, text, color)
            }
        }
        if (enchantValue) {
            RenderUtils.drawExhiEnchants(stack, x.toFloat(), y.toFloat())
        }
        popMatrix()
    }

    private fun drawBackgroundWithContourVertical(x: Int, y: Int, color: Int) {
        val primaryColor = java.awt.Color(red, green, blue, alpha)
        val secondaryColor = java.awt.Color(secondaryRed, secondaryGreen, secondaryBlue, borderThickness)

       /* RenderUtils.drawGradientRound(
            x - 2.0f,
            y - 12.0f,
            18.0f,
            72.0f,
            borderRadius,
            ColorUtils.applyOpacity(secondaryColor, 0.85f),
            primaryColor,
            secondaryColor,
            primaryColor
        )
        */

        Fonts.minecraftFont.drawString("Armor", x, (y - 8.0f).roundToInt(), color)
    }

    private fun drawArmorItemsVertical(x: Int, y: Int, color: Int, player: EntityPlayerSP) {
        var yOffset = y
        val renderItem = mc.renderItem

        for (i in 3 downTo 0) {
            val stack = player.inventory.armorInventory[i] ?: continue

            renderItem.renderItemIntoGUI(stack, x, yOffset)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, yOffset)

            drawAttributes(x, yOffset, color, stack)

            yOffset += 18
        }

        enableAlpha()
        enableBlend()
        disableLighting()
        disableCull()
    }

    private fun drawText(x: Int, y: Int, text: String, color: Int) {
        val xOffset = percentageX.toFloat()
        val yOffset = percentageY.toFloat()
        val yPos = y + 15.0f + Fonts.font35.height + yOffset
        Fonts.font35.drawString(text, x + xOffset, yPos, color)
    }

    private fun Float.format(): String {
        return if (minimalMode) {
            String.format("%.2f%%", this)
        } else {
            String.format("%.0f%%", this)
        }
    }
}