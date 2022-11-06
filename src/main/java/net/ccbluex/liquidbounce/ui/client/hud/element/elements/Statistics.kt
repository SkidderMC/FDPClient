/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.utils.SessionUtils
import net.ccbluex.liquidbounce.utils.StatisticsUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import org.lwjgl.opengl.GL11
import java.awt.Color

@ElementInfo(name = "Statistics", blur = true)
class Statistics(
    x: Double = 3.39,
    y: Double = 24.48,
    scale: Float = 1F,
    side: Side = Side.default()
) : Element(x, y, scale, side) {
    
    private val rectR = IntegerValue("Rect-R", 0, 0, 255)
    private val rectG = IntegerValue("Rect-G", 0, 0, 255)
    private val rectB = IntegerValue("Rect-B", 0, 0, 255)
    private val rectAlpha = IntegerValue("RectAlpha", 150, 0, 255)
    private val rectShadow = BoolValue("RectShadow", false)
    
    private val textR = IntegerValue("Label-R", 255, 0, 255)
    private val textG = IntegerValue("Label-G", 255, 0, 255)
    private val textB = IntegerValue("Label-B", 255, 0, 255)
    private val textAlpha = IntegerValue("LabelAlpha", 200, 0, 255)
    
    private val infoR = IntegerValue("Info-R", 200, 0, 255)
    private val infoG = IntegerValue("Info-G", 200, 0, 255)
    private val infoB = IntegerValue("Info-B", 200, 0, 255)
    private val infoAlpha = IntegerValue("InfoAlpha", 180, 0, 255)
    
    
    override fun drawElement(partialTicks: Float): Border {
        val height = FontLoaders.F22.getStringHeight("Statistics") + 14.0f +
                41.0F
        
        if (rectShadow.get()) {
            RenderUtils.drawShadow(0f, 0f, 140f, height)
            RenderUtils.drawShadow(-1f, -1f, 141f, height + 1f)
            RenderUtils.drawRect(0f, 0f, 140f, height, Color(rectR.get(), rectG.get(), rectB.get(), rectAlpha.get()).rgb)
        } else {
            RenderUtils.drawRoundedCornerRect(0f, 0f, 140f, height, 5f, Color(rectR.get(), rectG.get(), rectB.get(), rectAlpha.get()).rgb)
        }

        FontLoaders.F22.drawCenteredString("Statistics", (140f / 2f).toDouble(), 5.0, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
        RenderUtils.drawLine(
            0f.toDouble(),
            FontLoaders.F22.getStringHeight("Statistics") + 7.0f.toDouble(),
            140f.toDouble(),
            FontLoaders.F22.getStringHeight("Statistics") + 7.0f.toDouble(),
            0.4f
        )

        GL11.glPushMatrix()
        GL11.glTranslated(0.0, FontLoaders.F22.getStringHeight("Statistics") + 14.0f.toDouble(), 0.0)

        // Player name
        FontLoaders.C16.drawString("Player Name", 5.0F, 0.0F, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
        FontLoaders.C16.drawString(mc.thePlayer.name, 135F - FontLoaders.C16.getStringWidth(mc.thePlayer.name), 0.0f, Color(infoR.get(), infoG.get(), infoB.get(),infoAlpha.get()).rgb)

        // Play time
        FontLoaders.C16.drawString("Play Time", 5.0F, 10.0F, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
        if (!mc.isSingleplayer) FontLoaders.C16.drawString(SessionUtils.getFormatSessionTime(), 135F - FontLoaders.C16.getStringWidth(SessionUtils.getFormatSessionTime()), 10.0f, Color(infoR.get(), infoG.get(), infoB.get(),infoAlpha.get()).rgb)
        else FontLoaders.C16.drawString("Singleplayer", 135F - FontLoaders.C16.getStringWidth("Singleplayer"), 10.0f, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)


        // Kills
        FontLoaders.C16.drawString("Kills", 5.0F, 20.0F, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
        FontLoaders.C16.drawString(StatisticsUtils.getKills().toString(), 135F - FontLoaders.C16.getStringWidth(StatisticsUtils.getKills().toString()), 20.0f, Color(infoR.get(), infoG.get(), infoB.get(),infoAlpha.get()).rgb)

        // Deaths
        FontLoaders.C16.drawString("Deaths", 5.0F, 30.0F, Color(textR.get(), textG.get(), textB.get(),textAlpha.get()).rgb)
        FontLoaders.C16.drawString(StatisticsUtils.getDeaths().toString(), 135F - FontLoaders.C16.getStringWidth(StatisticsUtils.getDeaths().toString()), 30.0f, Color(infoR.get(), infoG.get(), infoB.get(),infoAlpha.get()).rgb)
        GL11.glPopMatrix()

        return Border(0f, 0f, 140f, height)
    }
}
