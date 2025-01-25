/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSmall
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawExhiEnchants
import net.minecraft.block.material.Material
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting
import net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11.*

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Armor")
class Armor(
    x: Double = -8.0,
    y: Double = 57.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)
) : Element("Armor", x, y, scale, side) {

    private val modeValue by choices("Alignment", arrayOf("Horizontal", "Vertical"), "Vertical")
    private val showAttributes by choices("Attributes", arrayOf("None", "Value", "Percentage", "All"), "Percentage")
    private val enchantValue by boolean("Enchant", true)
    private val minimalMode by boolean("Minimal Mode", false)
    private val percentageY by int("Attributes - PositionY", -19, -50..50)
    private val percentageX by int("Attributes - PositionX", 21, -50..50)
    private val red by int("Red", 255, 0..255)
    private val green by int("Green", 255, 0..255)
    private val blue by int("Blue", 255, 0..255)
    private val alpha by int("Alpha", 255, 0..255)
    private val repairReminderThreshold by int("Alert Repair Reminder Threshold", 0, 0..100)
    private val durabilityThreshold by int("Alert Durability Threshold", 0, 0..100)

    private var blinkTimer = 0L
    private var blinkState = false

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
        val isInsideWater = player.isInsideOfMaterial(Material.water)
        val x = 1
        val y = if (isInsideWater) -10 else 0

        glColor4f(1F, 1F, 1F, 1F)

        val currentTime = System.currentTimeMillis()
        if (currentTime - blinkTimer >= 500) {
            blinkState = !blinkState
            blinkTimer = currentTime
        }

        if (mode.equals("Horizontal", ignoreCase = true)) {
            enableCull()
            drawArmorItems(x, y, color, player)
        } else if (mode.equals("Vertical", ignoreCase = true)) {
            enableCull()
            drawArmorItemsVertical(x, y, color, player)
        }

        return if (mode.equals("Horizontal", ignoreCase = true)) Border(0.0f, 0.0f, 72.0f, 17.0f) else Border(
            0.0f,
            0.0f,
            18.0f,
            72.0f
        )
    }

    private fun drawArmorItems(x: Int, y: Int, color: Int, player: EntityPlayerSP) {
        var x = x
        val renderItem = mc.renderItem

        for (i in 3 downTo 0) {
            val stack = player.inventory.armorInventory[i] ?: continue

            glPushMatrix()
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            enableGUIStandardItemLighting()
            renderItem.renderItemIntoGUI(stack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
            disableStandardItemLighting()
            glDisable(GL_BLEND)
            glPopMatrix()

            pushMatrix()
            drawAttributesAndEnchantments(stack, x, y, color)

            if (enchantValue) {
                drawExhiEnchants(stack, x.toFloat(), y.toFloat())
            }

            x += 18
        }

        enableAlpha()
        enableBlend()
        disableLighting()
        disableCull()
    }

    private fun drawArmorItemsVertical(x: Int, y: Int, color: Int, player: EntityPlayerSP) {
        var y = y
        val renderItem = mc.renderItem

        for (i in 3 downTo 0) {
            val stack = player.inventory.armorInventory[i] ?: continue

            glPushMatrix()
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            enableGUIStandardItemLighting()
            renderItem.renderItemIntoGUI(stack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
            disableStandardItemLighting()
            glDisable(GL_BLEND)
            glPopMatrix()

            pushMatrix()
            drawAttributesAndEnchantments(stack, x, y, color)

            if (enchantValue) {
                drawExhiEnchants(stack, x.toFloat(), y.toFloat())
            }

            y += 18
        }

        enableAlpha()
        enableBlend()
        disableLighting()
        disableCull()
    }

    private fun drawAttributesAndEnchantments(stack: ItemStack, x: Int, y: Int, color: Int) {
        if (showAttributes != "None") {
            val percentageXOffset = percentageX.toFloat()
            val percentageYOffset = percentageY.toFloat()
            val value = stack.maxDamage - stack.itemDamage
            val percentage = value.toFloat() / stack.maxDamage.toFloat() * 100.0f

            val displayColor = if (percentage <= durabilityThreshold && blinkState) {
                java.awt.Color.RED.rgb
            } else {
                color
            }

            when (showAttributes) {
                "Value" -> {
                    fontSmall.drawString(
                        value.toString(),
                        x + percentageXOffset,
                        y + 15.0f + fontSmall.height + percentageYOffset,
                        displayColor
                    )
                }
                "Percentage" -> {
                    val percentageText = if (minimalMode) {
                        String.format("%.2f%%", percentage)
                    } else {
                        String.format("%.0f%%", percentage)
                    }
                    fontSmall.drawString(
                        percentageText,
                        x + percentageXOffset,
                        y + 15.0f + fontSmall.height + percentageYOffset,
                        displayColor
                    )
                }
                "All" -> {
                    val damageText = String.format("%d/%d (%.0f%%)", value, stack.maxDamage, percentage)
                    fontSmall.drawString(
                        damageText,
                        x + percentageXOffset,
                        y + 15.0f + fontSmall.height + percentageYOffset,
                        displayColor
                    )
                }
            }

            if (percentage <= repairReminderThreshold) {
                Chat.print("!! ${stack.displayName} has low durability!")
            }
        }

        if (stack.itemDamage > stack.maxDamage * (1 - durabilityThreshold / 100.0)) {
            drawDurabilityAlert(stack, x, y, color)
        }

        popMatrix()
    }

    private fun drawDurabilityAlert(stack: ItemStack, x: Int, y: Int, color: Int) {
        val alertColor = java.awt.Color.RED.rgb
        fontSmall.drawString("⚠", x.toFloat(), y.toFloat(), alertColor)
    }
}