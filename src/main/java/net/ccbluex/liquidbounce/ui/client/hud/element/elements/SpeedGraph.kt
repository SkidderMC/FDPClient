package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import kotlin.math.sqrt

/**
 * CustomHUD text element
 *
 * Allows to draw custom text
 */
@ElementInfo(name = "SpeedGraph", blur = true)
class SpeedGraph(
    x: Double = 75.0,
    y: Double = 110.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    private val yMultiplier = FloatValue("yMultiplier", 7F, 1F, 20F)
    private val height = IntegerValue("Height", 50, 30, 150)
    private val width = IntegerValue("Width", 150, 100, 300)
    private val thickness = FloatValue("Thickness", 2F, 1F, 3F)
    private val smoothness = FloatValue("Smoothness", 0.5F, 0F, 1F)
    private val colorRedValue = IntegerValue("R", 0, 0, 255)
    private val colorGreenValue = IntegerValue("G", 111, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)
    private val bgRedValue = IntegerValue("BGRed", 0, 0, 255)
    private val bgGreenValue = IntegerValue("BGGreen", 0, 0, 255)
    private val bgBlueValue = IntegerValue("BGBlue", 0, 0, 255)
    private val bgAlphaValue = IntegerValue("BGAlpha", 150, 0, 255)
    private val bdRedValue = IntegerValue("BDRed", 255, 0, 255)
    private val bdGreenValue = IntegerValue("BDGreen", 255, 0, 255)
    private val bdBlueValue = IntegerValue("BDBlue", 255, 0, 255)
    private val boarderValue = BoolValue("Boarder", false)
    private val currentLineValue = BoolValue("CurrentLine", false)
    private val clRedValue = IntegerValue("CLRed", 0, 0, 255)
    private val clGreenValue = IntegerValue("CLGreen", 255, 0, 255)
    private val clBlueValue = IntegerValue("CLBlue", 0, 0, 255)

    private val speedList = ArrayList<Double>()
    private var lastTick = -1
    private var lastSpeed = 0.01

    override fun drawElement(partialTicks: Float): Border {
        val width = width.get()
        if (lastTick != mc.thePlayer.ticksExisted) {
            lastTick = mc.thePlayer.ticksExisted
            val z2 = mc.thePlayer.posZ
            val z1 = mc.thePlayer.prevPosZ
            val x2 = mc.thePlayer.posX
            val x1 = mc.thePlayer.prevPosX
            var speed = sqrt((z2 - z1) * (z2 - z1) + (x2 - x1) * (x2 - x1))
            if (speed < 0) {
                speed = -speed
            }
            speed = (lastSpeed * 0.9 + speed * 0.1) * smoothness.get() + speed * (1 - smoothness.get())
            lastSpeed = speed
            speedList.add(speed)
            while (speedList.size > width) {
                speedList.removeAt(0)
            }
        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(thickness.get())
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)

        RenderUtils.glColor(bgRedValue.get(), bgGreenValue.get(), bgBlueValue.get(), bgAlphaValue.get())
        RenderUtils.quickDrawRect(0f, 0f, width.toFloat(), height.get() + 2f)

        GL11.glBegin(GL11.GL_LINES)
        val size = speedList.size
        RenderUtils.glColor(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), 255)
        val start = (if (size > width) size - width else 0)
        for (i in start until size - 1) {
            val y = speedList[i] * 10 * yMultiplier.get()
            val y1 = speedList[i + 1] * 10 * yMultiplier.get()

            GL11.glVertex2d(i.toDouble() - start, height.get() + 1 - y.coerceAtMost(height.get().toDouble()))
            GL11.glVertex2d(i + 1.0 - start, height.get() + 1 - y1.coerceAtMost(height.get().toDouble()))
        }
        GL11.glEnd()

        if (currentLineValue.get()) {
            val y = (speedList.lastOrNull() ?: 0.0) * 10 * yMultiplier.get()
            RenderUtils.glColor(clRedValue.get(), clGreenValue.get(), clBlueValue.get(), 255)
            GL11.glBegin(GL11.GL_LINES)
            GL11.glVertex2d(0.0, height.get() + 1 - y.coerceAtMost(height.get().toDouble()))
            GL11.glVertex2d(width.toDouble(), height.get() + 1 - y.coerceAtMost(height.get().toDouble()))
            GL11.glEnd()
        }

        if (boarderValue.get()) {
            RenderUtils.glColor(bdRedValue.get(), bdGreenValue.get(), bdBlueValue.get(), 255)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex2d(0.0, 0.0)
            GL11.glVertex2d(width.toDouble(), 0.0)
            GL11.glVertex2d(width.toDouble(), height.get().toDouble() + 2)
            GL11.glVertex2d(0.0, height.get().toDouble() + 2)
            GL11.glVertex2d(0.0, 0.0)
            GL11.glEnd()
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.resetColor()

        return Border(0F, 0F, width.toFloat(), height.get().toFloat() + 2)
    }
}
