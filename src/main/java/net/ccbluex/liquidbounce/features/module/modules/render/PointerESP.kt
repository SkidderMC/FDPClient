/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

@ModuleInfo(name = "PointerESP", category = ModuleCategory.RENDER)
object PointerESP : Module() {

    private val dimensionValue = ListValue("Dimension", arrayOf("2d", "3d"), "2d")
    private val modeValue = ListValue("Mode", arrayOf("Solid", "Line", "LoopLine"), "Solid")
    private val lineWidthValue = FloatValue("LineWidth", 4f, 1f, 10f).displayable { modeValue.get().contains("Line") }
    private val redValue = IntegerValue("Red", 255, 0, 255).displayable { !rainbowValue.get() }
    private val greenValue = IntegerValue("Green", 255, 0, 255).displayable { !rainbowValue.get() }
    private val blueValue = IntegerValue("Blue", 255, 0, 255).displayable { !rainbowValue.get() }
    private val rainbowValue = BoolValue("Rainbow", false)
    private val damageColorValue = BoolValue("DamageColor", true)
    private val smoothDamageColorValue = BoolValue("SmoothDamageColor", false)
    private val dmgRedValue = IntegerValue("DamageRed", 255, 0, 255).displayable { !dmgRainbowValue.get() && damageColorValue.get() }
    private val dmgGreenValue = IntegerValue("DamageGreen", 0, 0, 255).displayable { !dmgRainbowValue.get() && damageColorValue.get() }
    private val dmgBlueValue = IntegerValue("DamageBlue", 0, 0, 255).displayable { !dmgRainbowValue.get() && damageColorValue.get() }
    private val dmgRainbowValue = BoolValue("DamageRainbow", false).displayable { damageColorValue.get() }
    private val alphaValue : IntegerValue = object : IntegerValue("Alpha", 255, 0, 255) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (distanceAlphaValue.get() && newValue < distanceValue.get()) {
                set(newValue)
            }
        }
    }
    private val distanceAlphaValue = BoolValue("DistanceAlpha", true)
    private val distanceValue = IntegerValue("Distance", 70, 0, 128).displayable { distanceAlphaValue.get() }
    private val alphaMinValue = IntegerValue("AlphaMin", 100, 0, 255).displayable { distanceAlphaValue.get() }
    private val sizeValue = IntegerValue("ArrowSize", 10, 1, 30)
    private val angleValue = IntegerValue("AngleSize", 50, 10, 90)
    private val radiusValue = IntegerValue("Radius", 70, 10, 100)

    @EventTarget
    fun onRender2d(event: Render2DEvent) {
        if(!dimensionValue.equals("2d"))
            return

        GL11.glPushMatrix()
        GL11.glTranslatef(event.scaledResolution.scaledWidth / 2f, event.scaledResolution.scaledHeight / 2f, 0.0f)

        draw()

        GL11.glPopMatrix()
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        if(dimensionValue.equals("2d"))
            return

        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL)
        GL11.glPolygonOffset(1.0f, -1000000f)

        GL11.glPushMatrix()
        GL11.glScaled(0.01, 0.01, 0.01)
        GL11.glRotatef(90f, 1f, 0f, 0f)
        GL11.glRotatef(180f + mc.thePlayer.rotationYaw, 0f, 0f, 1f)
        draw()
        GL11.glPopMatrix()

        GL11.glPolygonOffset(1.0f, 1000000f)
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL)
        GL11.glEnable(GL11.GL_CULL_FACE)
    }

    private fun draw() {
        val halfAngle = angleValue.get() / 2
        val radius = -radiusValue.get()
        val size = sizeValue.get()
        val playerPosX = mc.thePlayer.posX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks
        val playerPosZ = mc.thePlayer.posZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks
        val color = if(rainbowValue.get()) { ColorUtils.rainbow() } else { Color(redValue.get(), greenValue.get(), blueValue.get()) }
        val damageColor = if(damageColorValue.get()) {
            if(dmgRainbowValue.get()) { ColorUtils.reverseColor(ColorUtils.rainbow()) } else { Color(dmgRedValue.get(), dmgGreenValue.get(), dmgBlueValue.get()) }
        } else {
            color
        }

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)

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
                RenderUtils.glColor(if(entity.hurtTime > 0) { if(smoothDamageColorValue.get()) {
                    val percent = entity.hurtPercent.let { if(it > 0.5) { it - 0.5f } else { 0.5f - it } } * 2
                    ColorUtils.mixColors(damageColor, color, percent)
                } else {
                    damageColor
                } } else { color },
                    if (distanceAlphaValue.get()) {
                        (alphaValue.get() - (sqrt((playerPosX - entX).pow(2) + (playerPosZ - entZ).pow(2)) / distanceValue.get()).coerceAtMost(1.0) * (alphaValue.get() - alphaMinValue.get())).toInt()
                    } else {
                        alphaValue.get()
                    })
                GL11.glRotatef(angle, 0.0f, 0.0f, 1.0f)
                when(modeValue.get().lowercase()) {
                    "solid" -> {
                        GL11.glBegin(GL11.GL_TRIANGLES)
                        GL11.glVertex2f(0f, radius.toFloat())
                        GL11.glVertex2d(sin(-halfAngle * Math.PI / 180) * size, radius + cos(-halfAngle * Math.PI / 180) * size)
                        GL11.glVertex2d(sin(halfAngle * Math.PI / 180) * size, radius + cos(halfAngle * Math.PI / 180) * size)
                    }
                    "line","loopline" -> {
                        GL11.glLineWidth(lineWidthValue.get())
                        GL11.glBegin(GL11.GL_LINE_STRIP)
                        GL11.glVertex2d(sin(-halfAngle * Math.PI / 180) * size, radius + cos(-halfAngle * Math.PI / 180) * size)
                        GL11.glVertex2f(0f, radius.toFloat())
                        GL11.glVertex2d(sin(halfAngle * Math.PI / 180) * size, radius + cos(halfAngle * Math.PI / 180) * size)
                        if(modeValue.equals("LoopLine")) {
                            GL11.glVertex2d(sin(-halfAngle * Math.PI / 180) * size, radius + cos(-halfAngle * Math.PI / 180) * size)
                        }
                    }
                }
                GL11.glEnd()
                GL11.glRotatef(-angle, 0.0f, 0.0f, 1.0f)
            }
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
    }
}
