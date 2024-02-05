/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.CPSCounterUtils
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

@ElementInfo(name = "CPSCounter")
class CPSCounter : Element() {
    private val fontValue = FontValue("Fonts:", Fonts.minecraftFont)
    private val rightValue = BoolValue("Right Click", false)
    private val fontColor = BoolValue("Color Theme", false)
    private val backGround = BoolValue("BackGround", true)
    private val backAlpha = IntegerValue("BackGround Alpha", 190,0,255).displayable {backGround.get()}
    private val backRounded = FloatValue("BackGround Rounded", 5F, 0F, 5F).displayable {backGround.get()}
    private val glowValue = BoolValue("Glow", false).displayable {backGround.get()}
    private val glowRadius = FloatValue("Glow Radius", 5F, 1F, 8F)
    override fun drawElement(partialTicks: Float): Border {
        val font = fontValue.get()
        val string: String = if (backGround.get()) "CPS : ${
            CPSCounterUtils.getCPS(
                CPSCounterUtils.MouseButton.LEFT)}" + if (rightValue.get()) " | ${
            CPSCounterUtils.getCPS(
                CPSCounterUtils.MouseButton.RIGHT)}" else "" else "[CPS : ${
            CPSCounterUtils.getCPS(
                CPSCounterUtils.MouseButton.LEFT)}" + if (rightValue.get()) " | ${
            CPSCounterUtils.getCPS(
                CPSCounterUtils.MouseButton.RIGHT)}]" else "]"
        if (!backGround.get()) {
        } else {
            RenderUtils.drawRoundedRect(0F, 0F, 10F + font.getStringWidth(string), 10F + font.FONT_HEIGHT, backRounded.get(), Color(0,0,0,backAlpha.get()).rgb)
        }
        if(glowValue.get() && backGround.get()) {
            GlowUtils.drawGlow(0F, 0F, 10F + font.getStringWidth(string), 10F + font.FONT_HEIGHT, glowRadius.get().toInt(), Color(0,0,0,backAlpha.get()))
        }
        font.drawString(string, if (backGround.get()) 5F else 0F, if (backGround.get()) 5F else 0F, if (fontColor.get()) ClientTheme.getColor(1).rgb else Color(255,255,255).rgb, true)
        return Border(0F, 0F, if (backGround.get()) 10F + font.getStringWidth(string) else font.getStringWidth(string).toFloat(), if (backGround.get()) 10F + font.FONT_HEIGHT else font.FONT_HEIGHT.toFloat())
    }
}