/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*

object PointerESP : Module("PointerESP", Category.VISUAL, gameDetecting = false, hideModule = false) {

    // Display settings
    private val dimensionValue by ListValue("Dimension", arrayOf("2d", "3d"), "2d")
    private val modeValue by ListValue("Mode", arrayOf("Solid", "Line", "LoopLine"), "Solid")
    private val lineWidthValue by FloatValue("LineWidth", 4f, 1f..10f) { modeValue.contains("Line") }

    // Color settings
    private val colorMode by ListValue("Color Mode", arrayOf("Custom", "Theme Client", "Rainbow", "Fade"), "Custom")
    private val colorRedValue by IntegerValue("Red", 255, 0..255) { colorMode == "Custom" }
    private val colorGreenValue by IntegerValue("Green", 179, 0..255) { colorMode == "Custom" }
    private val colorBlueValue by IntegerValue("Blue", 72, 0..255) { colorMode == "Custom" }
    private val rainbowSpeed by FloatValue("Rainbow Speed", 1.0f, 0.5f..5.0f) { colorMode == "Rainbow" }
    private val fadeDistanceValue by IntegerValue("Fade-Distance", 50, 0..100) { colorMode == "Fade" }

    // Damage color settings
    private val damageColorValue by BoolValue("DamageColor", true)
    private val smoothDamageColorValue by BoolValue("SmoothDamageColor", false)
    private val dmgRedValue by IntegerValue("DamageRed", 255, 0..255) {  damageColorValue }
    private val dmgGreenValue by IntegerValue("DamageGreen", 0, 0..255) {  damageColorValue }
    private val dmgBlueValue by IntegerValue("DamageBlue", 0, 0..255) { damageColorValue }

    // Opacity and distance settings
    private val alphaValue: IntegerValue = object : IntegerValue("Alpha", 255, 0..255) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (distanceAlphaValue && newValue < distanceValue) {
                set(newValue)
            }
        }
    }
    private val distanceAlphaValue by BoolValue("DistanceAlpha", true)
    private val distanceValue by IntegerValue("Distance", 70, 0..128) { distanceAlphaValue }
    private val alphaMinValue by IntegerValue("AlphaMin", 100, 0..255) { distanceAlphaValue }

    // Indicator size settings
    private val sizeValue by IntegerValue("ArrowSize", 10, 1..30)
    private val angleValue by IntegerValue("AngleSize", 50, 10..90)
    private val radiusValue by IntegerValue("Radius", 70, 10..100)

    // Method to render 2D pointer on screen
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (dimensionValue != "2d") return

        val mc = Minecraft.getMinecraft()
        val scaledResolution = ScaledResolution(mc)

        glPushMatrix()
        glTranslatef(scaledResolution.scaledWidth / 2f, scaledResolution.scaledHeight / 2f, 0.0f)

        draw()

        glPopMatrix()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (dimensionValue == "2d") return

        glDisable(GL_CULL_FACE)
        glEnable(GL_POLYGON_OFFSET_FILL)
        glPolygonOffset(1.0f, -1000000f)

        glPushMatrix()
        glScaled(0.01, 0.01, 0.01)
        glRotatef(90f, 1f, 0f, 0f)
        glRotatef(180f + Minecraft.getMinecraft().thePlayer.rotationYaw, 0f, 0f, 1f)

        draw() // Call method to draw the pointer

        glPopMatrix()

        glPolygonOffset(1.0f, 1000000f)
        glDisable(GL_POLYGON_OFFSET_FILL)
        glEnable(GL_CULL_FACE)
    }

    // Get the pointer color based on mode
    private fun getPointerColor(index: Int): Color {
        return when (colorMode) {
            "Custom" -> Color(colorRedValue, colorGreenValue, colorBlueValue)
            "Theme Client" -> ClientThemesUtils.getColor(1)
            "Rainbow" -> ColorUtils.rainbow(rainbowSpeed)
            "Fade" -> fade(Color(colorRedValue, colorGreenValue, colorBlueValue), index * fadeDistanceValue, 100)
            else -> Color(255, 255, 255)
        }
    }

    // Apply fade effect
    private fun fade(color: Color, distance: Int, maxDistance: Int): Color {
        val alpha = ((1.0 - (distance.toFloat() / maxDistance.toFloat())).coerceIn(0.0, 1.0) * 255).toInt()
        return Color(color.red, color.green, color.blue, alpha)
    }

    // Draw the pointer indicator on the screen (shared between 2D and 3D)
    private fun draw() {
        val halfAngle = angleValue / 2
        val radius = -radiusValue
        val size = sizeValue

        val playerPosX = mc.thePlayer.posX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks
        val playerPosZ = mc.thePlayer.posZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        // Iterate over entities to draw indicators
        val index = 0
        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, true) && entity is EntityLivingBase) {
                val entX = entity.posX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks
                val entZ = entity.posZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks
                val pos1 = (entX - playerPosX) * 0.2
                val pos2 = (entZ - playerPosZ) * 0.2

                val cos = cos(mc.thePlayer.rotationYaw * (Math.PI / 180))
                val sin = sin(mc.thePlayer.rotationYaw * (Math.PI / 180))
                val rotY = -(pos2 * cos - pos1 * sin)
                val rotX = -(pos1 * cos + pos2 * sin)
                val angle = (atan2(rotY, rotX) * 180 / Math.PI).toFloat() + 90f

                // Get the pointer color based on entity damage
                val color = getPointerColor(index)
                RenderUtils.glFloatColor(
                    if (entity.hurtTime > 0) {
                        if (smoothDamageColorValue) {
                            val percent = entity.hurtPercent.let {
                                if (it > 0.5) it - 0.5f else 0.5f - it
                            } * 2
                            ColorUtils.mixColors(Color(dmgRedValue, dmgGreenValue, dmgBlueValue), color, percent)
                        } else {
                            Color(dmgRedValue, dmgGreenValue, dmgBlueValue)
                        }
                    } else {
                        color
                    },
                    if (distanceAlphaValue) {
                        (alphaValue.get() - (sqrt((playerPosX - entX).pow(2) + (playerPosZ - entZ).pow(2)) / distanceValue).coerceAtMost(1.0) * (alphaValue.get() - alphaMinValue)).toInt()
                    } else {
                        alphaValue.get()
                    }
                )

                glRotatef(angle, 0.0f, 0.0f, 1.0f)

                // Draw pointer based on mode (Solid, Line, LoopLine)
                when (modeValue.lowercase()) {
                    "solid" -> {
                        glBegin(GL_TRIANGLES)
                        glVertex2f(0f, radius.toFloat())
                        glVertex2d(sin(-halfAngle * Math.PI / 180) * size, radius + cos(-halfAngle * Math.PI / 180) * size)
                        glVertex2d(sin(halfAngle * Math.PI / 180) * size, radius + cos(halfAngle * Math.PI / 180) * size)
                        glEnd()
                    }
                    "line", "loopline" -> {
                        glLineWidth(lineWidthValue)
                        glBegin(GL_LINE_STRIP)
                        glVertex2d(sin(-halfAngle * Math.PI / 180) * size, radius + cos(-halfAngle * Math.PI / 180) * size)
                        glVertex2f(0f, radius.toFloat())
                        glVertex2d(sin(halfAngle * Math.PI / 180) * size, radius + cos(halfAngle * Math.PI / 180) * size)
                        if (modeValue == "loopline") {
                            glVertex2d(sin(-halfAngle * Math.PI / 180) * size, radius + cos(-halfAngle * Math.PI / 180) * size)
                        }
                        glEnd()
                    }
                }

                glRotatef(-angle, 0.0f, 0.0f, 1.0f)
            }
        }

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }
}
