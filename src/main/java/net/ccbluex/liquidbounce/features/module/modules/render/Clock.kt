/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin


@ModuleInfo(name = "Clock", category = ModuleCategory.RENDER)
class Clock : Module() {
    private val anaStyleValue = ListValue("Style", arrayOf("Android", "Hands", "Minimal", "Classic"), "Android")
    private val widthValue = FloatValue("Width", 100f, 30f, 300f)
    private val xValue = FloatValue("X", 0f, 0f, 2000f)
    private val yValue = FloatValue("Y", 0f, 0f, 2000f)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val now = LocalDateTime.now()
        glPushMatrix()
        glTranslatef(xValue.get(), yValue.get(), 0F)
        when(anaStyleValue.get()){
            "Hands" -> {
                val minutes: Float = now.minute + (now.second + System.currentTimeMillis() % 1000F / 1000F) / 60.0F
                val hours: Float = now.hour + minutes / 60F
                simpleHands(3.0f, minutes / 60F * 100F, widthValue.get() * 0.90F, 5F, Color(255, 255, 255))
                simpleHands(3.0f, (hours % 12F) / 12F * 100F, widthValue.get() * 0.5F, 5F, Color(255, 255, 255))
            }
            "Android" -> {
                val minutes: Float = now.minute + (now.second + System.currentTimeMillis() % 1000F / 1000F) / 60.0F
                val hours: Float = now.hour + minutes / 60F
                simpleHands(3.0f, minutes / 60F * 100F, widthValue.get() * 0.90F, 5F, Color(255, 255, 255))
                simpleHands(3.0f, (hours % 12F) / 12F * 100F, widthValue.get() * 0.5F, 5F, Color(255, 255, 255))

                RenderUtils.drawCircle(0f, 0f, widthValue.get(), 3f, 0, 360, Color(255, 255, 255))
            }
            "Minimal" -> {
                val seconds: Float = now.second + System.currentTimeMillis() % 1000F / 1000F
                val minutes: Float = now.minute + seconds / 60.0F
                val hours: Float = now.hour + minutes / 60F
                simpleHands(2.0f, seconds / 60F * 100F, widthValue.get(), 0f, Color(255, 255, 255))
                simpleHands(4.0f, minutes / 60F * 100F, widthValue.get() * 0.70F, 0f, Color(255, 255, 255))
                simpleHands(6.0f, (hours % 12F) / 12F * 100F, widthValue.get() * 0.5F, 0f, Color(255, 255, 255))

                RenderUtils.drawCircle(0f, 0f, widthValue.get(), 3f, 0, 360, Color(255, 255, 255))
                RenderUtils.drawFilledCircle(0f, 0f, 4F, Color(255, 255, 255))
            }
            "Classic" -> {
                RenderUtils.drawFilledCircle(0f, 0f, widthValue.get() + 3, Color(255, 255, 255)) // bg
                Fonts.font32.drawCenteredString("XII", 0F, -widthValue.get() + 2F, Color(0, 0, 0).rgb, false)
                Fonts.font32.drawCenteredString("III",  widthValue.get() - 3, -2F, Color(0, 0, 0).rgb, false)
                Fonts.font32.drawCenteredString("VI", 0F, widthValue.get() - 7f, Color(0, 0, 0).rgb, false)
                Fonts.font32.drawCenteredString("IX",  -widthValue.get() + 5F, -2F, Color(0, 0, 0).rgb, false)
                simpleHands(2.0f,  (now.second + System.currentTimeMillis() % 1000F / 1000F) / 60F * 100F, widthValue.get() * 0.90F, 7f, Color(255, 0, 0, 200)) // Seconds
                simpleHands(4.0f, (now.minute + (now.second + System.currentTimeMillis() % 1000F / 1000F) / 60.0F) / 60F * 100F, widthValue.get() * 0.70F, 5f, Color(0, 0, 0))// Minutes
                simpleHands(6.0f, ((now.hour + (now.minute + (now.second + System.currentTimeMillis() % 1000F / 1000F) / 60.0F) / 60F) % 12F) / 12F * 100F, widthValue.get() * 0.5F, 5f, Color(0, 0, 0))  // Hours
                RenderUtils.drawCircle(0f, 0f, widthValue.get() + 2, 6f, 0, 360, Color(0, 0, 0)) // rim circle
                glTranslated(-xValue.get().toDouble(), -yValue.get().toDouble(), 0.0)
                glPushMatrix()
                ShadowUtils.shadow(15F, { glPushMatrix(); glTranslated(xValue.get().toDouble(), yValue.get().toDouble(), 0.0); RenderUtils.drawCircle(0f, 0f, widthValue.get() + 2, 6f, 0, 360, Color(0, 0, 0, 200)); glPopMatrix(); }, {})
                ShadowUtils.shadow(3F, { glPushMatrix(); glTranslated(xValue.get().toDouble(), yValue.get().toDouble(), 0.0);                 simpleHands(2.0f,  (now.second + System.currentTimeMillis() % 1000F / 1000F) / 60F * 100F, widthValue.get() * 0.90F, 7f, Color(255, 0, 0, 200)); simpleHands(4.0f, (now.minute + (now.second + System.currentTimeMillis() % 1000F / 1000F) / 60.0F) / 60F * 100F, widthValue.get() * 0.70F, 5f, Color(0, 0, 0)); simpleHands(6.0f, ((now.hour + (now.minute + (now.second + System.currentTimeMillis() % 1000F / 1000F) / 60.0F) / 60F) % 12F) / 12F * 100F, widthValue.get() * 0.5F, 5f, Color(0, 0, 0)); glPopMatrix(); }, {})
                glPopMatrix()
                glTranslated(xValue.get().toDouble(), yValue.get().toDouble(), 0.0)
                RenderUtils.drawFilledCircle(0f, 0f, 2F, Color(255, 255, 255))
            }
        }
       glPopMatrix()
    }
    private fun simpleHands(width: Float, percent: Float, length: Float, extend: Float, Colour: Color) {
        RenderUtils.glColor(Colour.rgb)
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2f(extend * sin((percent) / 100.0 * Math.PI * 2 + Math.PI).toFloat(), -extend * cos((percent) / 100.0 * Math.PI * 2 + Math.PI).toFloat())
        glVertex2f(-length * sin((percent) / 100.0 * Math.PI * 2 + Math.PI).toFloat(), length * cos((percent) / 100.0 * Math.PI * 2 + Math.PI).toFloat())
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

}