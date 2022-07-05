package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.utils.SessionUtils
import net.ccbluex.liquidbounce.utils.StatisticsUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import org.lwjgl.opengl.GL11
import java.awt.Color

@ElementInfo(name = "Statistics", blur = true)
class Statistics(
    x: Double = 3.39,
    y: Double = 24.48,
    scale: Float = 1F,
    side: Side = Side.default()
) : Element(x, y, scale, side) {
    
    private val rectAlpha = IntegerValue("RectAlpha", 150, 0, 255)
    private val textAlpha = IntegerValue("TextAlpha", 200, 0, 255)
    private val rectR = IntegerValue("Rect-R", 0, 0, 255)
    private val rectG = IntegerValue("Rect-G", 0, 0, 255)
    private val rectB = IntegerValue("Rect-B", 0, 0, 255)
    
    override fun drawElement(partialTicks: Float): Border {
        val height = FontLoaders.F22.getStringHeight("Statistics") + 14.0f +
                41.0F

        RenderUtils.drawRoundedCornerRect(0f, 0f, 140f, height, 4f, Color(rectR.get(), rectG.get(), rectB.get(), rectAlpha.get()).rgb)

        FontLoaders.F22.drawCenteredString("Statistics", (140f / 2f).toDouble(), 5.0, Color(255,255,255,textAlpha.get()).rgb)
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
        FontLoaders.C16.drawString("Player Name", 5.0F, 0.0F, Color(255,255,255,textAlpha.get()).rgb)
        FontLoaders.C16.drawString(mc.thePlayer.name, 135F - FontLoaders.C16.getStringWidth(mc.thePlayer.name), 0.0f, Color(255,255,255,textAlpha.get()).rgb)

        // Play time
        FontLoaders.C16.drawString("Play Time", 5.0F, 10.0F, Color(255,255,255,textAlpha.get()).rgb)
        FontLoaders.C16.drawString(SessionUtils.getFormatSessionTime(), 135F - FontLoaders.C16.getStringWidth(SessionUtils.getFormatSessionTime()), 10.0f, Color(255,255,255,180).rgb)

        // Kills
        FontLoaders.C16.drawString("Kills", 5.0F, 20.0F, Color(255,255,255,textAlpha.get()).rgb)
        FontLoaders.C16.drawString(StatisticsUtils.getKills().toString(), 135F - FontLoaders.C16.getStringWidth(StatisticsUtils.getKills().toString()), 20.0f, Color(255,255,255,180).rgb)

        // Deaths
        FontLoaders.C16.drawString("Deaths", 5.0F, 30.0F, Color(255,255,255,textAlpha.get()).rgb)
        FontLoaders.C16.drawString(StatisticsUtils.getDeaths().toString(), 135F - FontLoaders.C16.getStringWidth(StatisticsUtils.getDeaths().toString()), 30.0f, Color(255,255,255,180).rgb)
        GL11.glPopMatrix()

        return Border(0f, 0f, 140f, height)
    }
}
