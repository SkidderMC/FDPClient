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
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawExhiEnchants
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.material.Material
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.item.ItemStack

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Armor")
class Armor(
    x: Double = -8.0, y: Double = 57.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    private val enchantValue by BoolValue("Enchant", true)
    private val modeValue by ListValue("Alignment", arrayOf("Horizontal", "Vertical"), "Vertical")
    private val showAttributes by ListValue("Attributes", arrayOf("None", "Value", "Percentage", "All"), "Percentage")
    private val minimalMode by BoolValue("Minimal Mode", false)
    private val percentageY by IntegerValue("Attributes - PositionY", -19, -50..50)
    private val percentageX by IntegerValue("Attributes - PositionX", 21, -50..50)
    private val red by IntegerValue("Red", 255, 0..255)
    private val green by IntegerValue("Green", 255, 0..255)
    private val blue by IntegerValue("Blue", 255, 0..255)
    private val alpha by IntegerValue("Alpha", 255, 0..255)
    private val mc = MinecraftInstance.mc

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

        val color = java.awt.Color(red, green, blue, alpha).rgb
        val isInsideWater = player.isInsideOfMaterial(Material.water)
        val adjustedY = if (isInsideWater) -10 else 0

        if (mode.equals("Horizontal", ignoreCase = true)) {
            enableCull()
            drawArmorItems(1, adjustedY, color, player, horizontal = true)
        } else if (mode.equals("Vertical", ignoreCase = true)) {
            enableCull()
            drawArmorItems(1, adjustedY, color, player, horizontal = false)
        }

        return if (mode.equals("Horizontal", ignoreCase = true)) {
            Border(0.0f, 0.0f, 72.0f, 17.0f)
        } else {
            Border(0.0f, 0.0f, 18.0f, 72.0f)
        }
    }

    private fun drawArmorItems(xStart: Int, yStart: Int, color: Int, player: EntityPlayerSP, horizontal: Boolean) {
        var x = xStart
        var y = yStart
        val renderItem = mc.renderItem

        for (i in 3 downTo 0) {
            val stack = player.inventory.armorInventory[i] ?: continue

            renderItem.renderItemIntoGUI(stack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)

            pushMatrix()
            drawAttributes(stack, x, y, color)
            if (enchantValue) {
                drawExhiEnchants(stack, x.toFloat(), y.toFloat())
            }
            popMatrix()

            if (horizontal) {
                x += 18
            } else {
                y += 18
            }
        }

        enableAlpha()
        enableBlend()
        disableLighting()
        disableCull()
    }

    private fun drawAttributes(stack: ItemStack, x: Int, y: Int, color: Int) {
        val percentageXOffset = percentageX.toFloat()
        val percentageYOffset = percentageY.toFloat()

        when (showAttributes) {
            "Value" -> {
                val valueText = (stack.maxDamage - stack.itemDamage).toString()
                Fonts.fontSmall.drawString(
                    valueText,
                    x + percentageXOffset,
                    y + 15.0f + Fonts.fontSmall.height + percentageYOffset,
                    color
                )
            }
            "Percentage" -> {
                val percentage = calculatePercentage(stack)
                val percentageText = if (minimalMode) {
                    String.format("%.2f%%", percentage)
                } else {
                    String.format("%.0f%%", percentage)
                }
                Fonts.fontSmall.drawString(
                    percentageText,
                    x + percentageXOffset,
                    y + 15.0f + Fonts.fontSmall.height + percentageYOffset,
                    color
                )
            }
            "All" -> {
                val value = stack.maxDamage - stack.itemDamage
                val percentage = calculatePercentage(stack)
                val damageText = String.format("%d/%d (%.0f%%)", value, stack.maxDamage, percentage)
                Fonts.fontSmall.drawString(
                    damageText,
                    x + percentageXOffset,
                    y + 15.0f + Fonts.fontSmall.height + percentageYOffset,
                    color
                )
            }
        }
    }

    private fun calculatePercentage(stack: ItemStack): Float {
        return (stack.maxDamage - stack.itemDamage).toFloat() / stack.maxDamage.toFloat() * 100.0f
    }
}
