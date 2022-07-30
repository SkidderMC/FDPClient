package net.skiddermc.fdpclient.ui.client.hud.element.elements

import net.skiddermc.fdpclient.ui.client.hud.element.Border
import net.skiddermc.fdpclient.ui.client.hud.element.Element
import net.skiddermc.fdpclient.ui.client.hud.element.ElementInfo
import net.skiddermc.fdpclient.ui.client.hud.element.Side
import net.skiddermc.fdpclient.ui.font.Fonts
import net.skiddermc.fdpclient.utils.extensions.drawCenteredString
import net.skiddermc.fdpclient.utils.render.ColorUtils
import net.skiddermc.fdpclient.utils.render.RenderUtils
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.FontValue
import net.skiddermc.fdpclient.value.IntegerValue
import net.skiddermc.fdpclient.value.ListValue
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import java.awt.Color

/**
 * @author liulihaocai
 * InventoryHUD
 */
@ElementInfo(name = "Inventory", blur = true)
class Inventory : Element(300.0, 50.0, 1F, Side(Side.Horizontal.RIGHT, Side.Vertical.UP)) {

    private val themeValue = ListValue("Theme", arrayOf("Default", "CS:GO"/*, "Vanilla"*/), "CS:GO")
    private val bgRedValue = IntegerValue("BGRed", 0, 0, 255)
    private val bgGreenValue = IntegerValue("BGGreen", 0, 0, 255)
    private val bgBlueValue = IntegerValue("BGBlue", 0, 0, 255)
    private val bgAlphaValue = IntegerValue("BGAlpha", 150, 0, 255)
    private val bdRedValue = IntegerValue("BDRed", 255, 0, 255)
    private val bdGreenValue = IntegerValue("BDGreen", 255, 0, 255)
    private val bdBlueValue = IntegerValue("BDBlue", 255, 0, 255)
    private val fontRedValue = IntegerValue("FontRed", 255, 0, 255)
    private val fontGreenValue = IntegerValue("FontGreen", 255, 0, 255)
    private val fontBlueValue = IntegerValue("FontBlue", 255, 0, 255)
    private val titleValue = ListValue("Title", arrayOf("Center", "Left", "Right", "None"), "Left")
    private val bdRainbow = BoolValue("BDRainbow", false)
    private val fontRainbow = BoolValue("FontRainbow", false)
    private val fontValue = FontValue("Font", Fonts.font35)

    override fun drawElement(partialTicks: Float): Border {
        val borderColor = if (bdRainbow.get()) { ColorUtils.rainbow() } else { Color(bdRedValue.get(), bdGreenValue.get(), bdBlueValue.get()) }
        val fontColor = if (fontRainbow.get()) { ColorUtils.rainbow() } else { Color(fontRedValue.get(), fontGreenValue.get(), fontBlueValue.get()) }
        val backgroundColor = Color(bgRedValue.get(), bgGreenValue.get(), bgBlueValue.get(), bgAlphaValue.get())
        val font = fontValue.get()
        val startY = if (!titleValue.equals("None")) { -(6 + font.FONT_HEIGHT) } else { 0 }.toFloat()

        // draw rect
        RenderUtils.drawRect(0F, startY, 174F, 66F, backgroundColor)

        if(themeValue.equals("CS:GO")) {
            RenderUtils.drawRect(0F, startY, 174F, startY + 1f, borderColor)
        } else {
            RenderUtils.drawBorder(0f, startY, 174f, 66f, 3f, borderColor.rgb)
            RenderUtils.drawRect(0F, 0f, 174F, 1f, borderColor)
        }

        val invDisplayName = mc.thePlayer.inventory.displayName.formattedText
        when(titleValue.get().lowercase()) {
            "center" -> font.drawCenteredString(invDisplayName, 174f / 2, -(font.FONT_HEIGHT).toFloat(), fontColor.rgb, false)
            "left" -> font.drawString(invDisplayName, 6f, -(font.FONT_HEIGHT).toFloat(), fontColor.rgb, false)
            "right" -> font.drawString(invDisplayName, 168f - font.getStringWidth(invDisplayName), -(font.FONT_HEIGHT).toFloat(), fontColor.rgb, false)
        }

        // render item
        RenderHelper.enableGUIStandardItemLighting()
        renderInv(9, 17, 6, 6, font)
        renderInv(18, 26, 6, 24, font)
        renderInv(27, 35, 6, 42, font)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()

        return Border(0F, startY, 174F, 66F)
    }

    /**
     * render single line of inventory
     * @param endSlot slot+9
     */
    private fun renderInv(slot: Int, endSlot: Int, x: Int, y: Int, font: FontRenderer) {
        var xOffset = x
        for (i in slot..endSlot) {
            xOffset += 18
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue

            mc.renderItem.renderItemAndEffectIntoGUI(stack, xOffset - 18, y)
            mc.renderItem.renderItemOverlays(font, stack, xOffset - 18, y)
        }
    }
}