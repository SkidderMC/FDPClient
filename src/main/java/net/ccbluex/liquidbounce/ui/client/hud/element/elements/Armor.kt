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
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Armor")
class Armor(x: Double = -8.0, y: Double = 57.0, scale: Float = 1F,
            side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    private val modeValue = ListValue("Mode", arrayOf("LiquidBounce", "Exhibition"), "Exhibition")
    private val alignment = ListValue("Alignment", arrayOf("Horizontal", "Vertical"), "Horizontal")

    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border {
        if (mc.playerController.isNotCreative) {
            val renderItem = mc.renderItem
            val isInsideWater = mc.thePlayer.isInsideOfMaterial(Material.water)
            val mode = modeValue.get()
            val align = alignment.get()

            var x = 1
            var y = if (isInsideWater) -10 else 0

            RenderHelper.enableGUIStandardItemLighting()

            for (index in 3 downTo 0) {
                val stack = mc.thePlayer.inventory.armorInventory[index] ?: continue

                renderItem.renderItemIntoGUI(stack, x, y)
                renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
                if (mode.equals("Exhibition", true)) {
                    RenderUtils.drawExhiEnchants(stack, x.toFloat(), y.toFloat())
                    if (align.equals("Horizontal", true))
                        x += 16
                    else if (align.equals("Vertical", true))
                        y += 16
                } else
                    if (align.equals("Horizontal", true))
                        x += 18
                    else if (align.equals("Vertical", true))
                        y += 18
            }

            if (mode.equals("Exhibition", true)) {
                val mainStack = mc.thePlayer.heldItem
                if (mainStack != null && mainStack.item != null) {
                    renderItem.renderItemIntoGUI(mainStack, x, y)
                    renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
                    RenderUtils.drawExhiEnchants(mainStack, x.toFloat(), y.toFloat())
                }
            }

            RenderHelper.disableStandardItemLighting()
            GlStateManager.enableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.disableLighting()
            GlStateManager.disableCull()
        }

        return if (modeValue.get().equals("Exhibition", true)) {
            if (alignment.get().equals("Horizontal", true))
                Border(0F, 0F, 80F, 17F)
            else
                Border(0F, 0F, 18F, 80F)
        } else if (alignment.get().equals("Horizontal", true))
            Border(0F, 0F, 72F, 17F)
        else
            Border(0F, 0F, 18F, 72F)
    }
}