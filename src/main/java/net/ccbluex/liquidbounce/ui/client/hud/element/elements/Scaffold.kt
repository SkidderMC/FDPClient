package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemBlock
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.roundToInt

@ElementInfo(name = "ScaffoldCounter")
class ScaffoldCounter : Element(-46.0, -40.0, 1F, Side(Side.Horizontal.MIDDLE, Side.Vertical.MIDDLE)) {
    private val modeValue = ListValue("Mode", arrayOf("LB", "DMCA6"), "LB")
    private val barrier = ItemStack(Item.getItemById(166), 0, 0)
    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock && InventoryUtils.canPlaceBlock((itemStack.item as ItemBlock).block)) {
                    amount += itemStack.stackSize
                }
            }
            return amount
        }

    override fun drawElement(partialTicks: Float): Border? {

   if (mc.currentScreen is GuiHudDesigner) {
        }

        when (modeValue.get().lowercase()) {
            "dmca6" -> drawLies()
            "lb" -> drawLB()
        }
        return getTBorder()
    }

    private fun drawLies() {
    //render
    }

    private fun drawLB() {
        val sc = ScaledResolution(mc)
        val info = LanguageManager.getAndFormat("ui.scaffold.blocks", blocksAmount)
        val slot = InventoryUtils.findAutoBlockBlock()
        val height = sc.getScaledHeight()
        val width = sc.getScaledWidth()
        var stack = barrier
        if (slot != -1) {
            if (mc.thePlayer.inventory.getCurrentItem() != null) {
                val handItem = mc.thePlayer.inventory.getCurrentItem().item
                if (handItem is ItemBlock && InventoryUtils.canPlaceBlock(handItem.block)) {
                    stack = mc.thePlayer.inventory.getCurrentItem()
                }
            }
            if (stack == barrier) {
                stack = mc.thePlayer.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                if (stack == null) {
                    stack = barrier
                }
            }
        }
    GlStateManager.pushMatrix()
    RenderHelper.enableGUIStandardItemLighting()
    mc.renderItem.renderItemIntoGUI(stack, width / 2 - mc.fontRendererObj.getStringWidth(info), (height * 0.6 - mc.fontRendererObj.FONT_HEIGHT * 0.5).toInt())
    RenderHelper.disableStandardItemLighting()
    mc.fontRendererObj.drawCenteredString(info, width / 2f, height * 0.6f, Color.WHITE.rgb, false)
    GlStateManager.popMatrix()
    }

    private fun getTBorder(): Border? {
        return when (modeValue.get().lowercase()) {
            "lb" -> Border(0F, 0F, 120F, 40F)
            else -> null
        }
    }
}
