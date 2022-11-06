/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 * This file is a skid of https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */

package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketCounterUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.lang.Math.pow
import kotlin.math.sqrt

/**
 * CustomHUD text element
 *
 * Allows to draw custom text
 */
@ElementInfo(name = "Graph")
class Graph(x: Double = 75.0, y: Double = 110.0, scale: Float = 1F,
            side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    // general
    private val graphValue = ListValue("Graph-Value", arrayOf("Speed", "BPS", "Packet-In", "Packet-Out"), "Speed")
    private val updateDelay = IntegerValue("Update-Delay", 1000, 0, 5000)
    private val xMultiplier = FloatValue("xMultiplier", 7F, 1F, 20F)
    private val yMultiplier = FloatValue("yMultiplier", 7F, 0.1F, 20F)
    private val maxGraphValues = IntegerValue("MaxGraphValues", 100, 1, 300)
    private val maxHeight = FloatValue("MaxHeight", 50F, 30F, 150F)
    private val thickness = FloatValue("Thickness", 2F, 1F, 3F)
    private val displayGraphName = BoolValue("Display-Name", true)
    private val nameValue = BoolValue("Name-Value", true)
    private val fontValue = FontValue("Font", Fonts.minecraftFont)

    // average settings
    private val showAverageLine = BoolValue("Show-Average", true)
    private val averageLayer = ListValue("Average-Layer", arrayOf("Top", "Bottom"), "Bottom")
    private val avgUpdateDelay = IntegerValue("Average-Update-Delay", 1000, 0, 5000)

    // bg color
    private val bgredValue = IntegerValue("Background-Red", 0, 0, 255)
    private val bggreenValue = IntegerValue("Background-Green", 0, 0, 255)
    private val bgblueValue = IntegerValue("Background-Blue", 0, 0, 255)
    private val bgalphaValue = IntegerValue("Background-Alpha", 120, 0, 255)
    private val bordredValue = IntegerValue("Border-Red", 255, 0, 255)
    private val bordgreenValue = IntegerValue("Border-Green", 255, 0, 255)
    private val bordblueValue = IntegerValue("Border-Blue", 255, 0, 255)
    private val bordalpha = IntegerValue("Border-Alpha", 255, 0, 255)
    private val bordRad = FloatValue("Border-Width", 3F, 0F, 10F)

    private val valueStore = arrayListOf<Float>()
    private val timer = MSTimer()
    private val avgtimer = MSTimer()
    private var averageNumber = 0F

    private var lastX = 0.0
    private var lastZ = 0.0
    private var speedVal = 0F

    private var lastValue = ""

    override fun updateElement() {
        if (mc.thePlayer == null) return
        speedVal = sqrt(pow(lastX - mc.thePlayer.posX, 2.0) + pow(lastZ - mc.thePlayer.posZ, 2.0)).toFloat() * 20F * mc.timer.timerSpeed
        lastX = mc.thePlayer.posX
        lastZ = mc.thePlayer.posZ
    }

    override fun drawElement(partialTicks: Float): Border {
        val font = fontValue.get()
        val width = maxGraphValues.get() * xMultiplier.get()
        val markColor = Color(0.1F, 1F, 0.1F).rgb
        val bgColor = Color(bgredValue.get(), bggreenValue.get(), bgblueValue.get(), bgalphaValue.get()).rgb
        val borderColor = Color(bordredValue.get(), bordgreenValue.get(), bordblueValue.get(), bordalpha.get()).rgb

        var defaultX = 0F

        if (mc.thePlayer == null || lastValue != graphValue.get()) {
            valueStore.clear()
            averageNumber = 0F
        }

        lastValue = graphValue.get()

        if (timer.hasTimePassed(updateDelay.get().toLong())) {
            when (graphValue.get().lowercase()) {
                "speed" -> valueStore.add(MovementUtils.getSpeed() * 10F)
                "bps" -> valueStore.add(speedVal)
                "packet-in" -> valueStore.add(PacketCounterUtils.avgInBound.toFloat())
                "packet-out" -> valueStore.add(PacketCounterUtils.avgOutBound.toFloat())
            }
            while (valueStore.size > maxGraphValues.get())
                valueStore.removeAt(0)
            timer.reset()
        }

        if (avgtimer.hasTimePassed(avgUpdateDelay.get().toLong())) {
            averageNumber = (averageNumber + valueStore[valueStore.size - 1]) / 2F
            avgtimer.reset()
        }

        val working = if (graphValue.get().startsWith("packet", true)) valueStore[valueStore.size - 1].toInt().toString() else String.format("%.2f", valueStore[valueStore.size - 1])
        val average = if (graphValue.get().startsWith("packet", true)) averageNumber.toInt().toString() else String.format("%.2f", averageNumber)

        if (displayGraphName.get()) {
            var displayString = if (nameValue.get()) when (graphValue.get().lowercase()) {
                "speed" -> "Player speed ($working blocks/tick)"
                "bps" -> "Player speed ($working blocks/s)"
                "packet-in" -> "Inbound packets ($working packets/s)"
                else -> "Outbound packets ($working packets/s)"
            } else when (graphValue.get().lowercase()) {
                "speed" -> "Player speed"
                "bps" -> "Player blocks/s"
                "packet-in" -> "Inbound packets"
                else -> "Outbound packets"
            }
            GlStateManager.pushMatrix()
            GlStateManager.translate(0.5, -6.0 - font.FONT_HEIGHT.toDouble() / 2.0, 0.0)
            GlStateManager.scale(0.75F, 0.75F, 0.75F)
            font.drawStringWithShadow(displayString, 0F, 0F, -1)
            GlStateManager.popMatrix()
        }

        if (bgalphaValue.get() > 0F)
            RenderUtils.drawRect(-1F, -1F, width - xMultiplier.get() + 1F, maxHeight.get() + 1F, bgColor)

        if (bordalpha.get() > 0F)
            RenderUtils.drawBorder(-1F, -1F, width - xMultiplier.get() + 1F, maxHeight.get() + 1F, bordRad.get(), borderColor)

        val avgheight = Math.min(averageNumber * yMultiplier.get(), maxHeight.get())
        val firstheight = Math.min(valueStore[valueStore.size - 1] * yMultiplier.get(), maxHeight.get())

        if (showAverageLine.get() && !nameValue.get()) font.drawStringWithShadow(average, -font.getStringWidth(average) - 3F, maxHeight.get() - avgheight - font.FONT_HEIGHT / 2F, markColor)

        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(thickness.get())
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.getWorldRenderer()
        if (showAverageLine.get() && averageLayer.get().equals("bottom", true)) {
            GlStateManager.color(0.1F, 1F, 0.1F, 1F)
            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            worldRenderer.pos(0.0, (maxHeight.get() - avgheight).toDouble(), 0.0).endVertex()
            worldRenderer.pos((width - xMultiplier.get()).toDouble(), (maxHeight.get() - avgheight).toDouble(), 0.0).endVertex()
            tessellator.draw()
        }
        GlStateManager.color(1F, 1F, 1F, 1F)
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        for (valu in valueStore) {
            val height = Math.min(valu * yMultiplier.get(), maxHeight.get())
            worldRenderer.pos(defaultX.toDouble(), (maxHeight.get() - height).toDouble(), 0.0).endVertex()
            defaultX += xMultiplier.get()
        }
        tessellator.draw()
        if (showAverageLine.get() && averageLayer.get().equals("top", true)) {
            GlStateManager.color(0.1F, 1F, 0.1F, 1F)
            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            worldRenderer.pos(0.0, (maxHeight.get() - avgheight).toDouble(), 0.0).endVertex()
            worldRenderer.pos((width - xMultiplier.get()).toDouble(), (maxHeight.get() - avgheight).toDouble(), 0.0).endVertex()
            tessellator.draw()
        }
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()

        if (nameValue.get()) font.drawStringWithShadow(average, defaultX - xMultiplier.get() + 3F, maxHeight.get() - avgheight - font.FONT_HEIGHT / 2F, markColor)
        else font.drawStringWithShadow(working, defaultX - xMultiplier.get() + 3F, maxHeight.get() - firstheight - font.FONT_HEIGHT / 2F, -1)

        return Border(0F, 0F, width, maxHeight.get() + 2F)
    }
}