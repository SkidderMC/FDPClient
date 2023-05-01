/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "Tracers", category = ModuleCategory.RENDER)
class Tracers : Module() {

    private val colorModeValue = ListValue("Color", arrayOf("Custom", "DistanceColor", "Rainbow"), "Custom")
    private val thicknessValue = FloatValue("Thickness", 2F, 1F, 5F)
    private val colorRedValue = IntegerValue("R", 0, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val colorGreenValue = IntegerValue("G", 160, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val colorBlueValue = IntegerValue("B", 255, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val colorAlphaValue = IntegerValue("A", 150, 0, 255)
    private val distanceMultplierValue = FloatValue("DistanceMultiplier", 5F, 0.1F, 10F).displayable { colorModeValue.equals("DistanceColor") }
    private val playerHeightValue = BoolValue("PlayerHeight", true)
    private val entityHeightValue = BoolValue("EntityHeight", true)
    private val directLineValue = BoolValue("Directline", false)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(thicknessValue.get())
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity != null && entity != mc.thePlayer && EntityUtils.isSelected(entity, false)) {
                var dist = (mc.thePlayer.getDistanceToEntity(entity) * distanceMultplierValue.get()).toInt()

                if (dist > 255) dist = 255

                val colorMode = colorModeValue.get().lowercase()
                val color = when {
                    EntityUtils.isFriend(entity) -> Color(0, 0, 255)
                    colorMode == "custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
                    colorMode == "distancecolor" -> Color(255 - dist, dist, 0)
                    colorMode == "rainbow" -> ColorUtils.rainbow()
                    else -> Color.WHITE
                }

                drawTraces(entity, color, !directLineValue.get())
            }
        }
        GL11.glEnd()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.resetColor()
    }

    private fun drawTraces(entity: Entity, color: Color) {
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks -
                mc.renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks -
                mc.renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks -
                mc.renderManager.renderPosZ)
        val eyeVector = Vec3(0.0, 0.0, 1.0)
            .rotatePitch((-Math.toRadians(mc.thePlayer.rotationPitch.toDouble())).toFloat())
            .rotateYaw((-Math.toRadians(mc.thePlayer.rotationYaw.toDouble())).toFloat())

        RenderUtils.glColor(color, colorAlphaValue.get())

        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glVertex3d(eyeVector.xCoord,
            if(playerHeightValue.get()) { mc.thePlayer.getEyeHeight().toDouble() } else { 0.0 } + eyeVector.yCoord,
            eyeVector.zCoord)
        GL11.glVertex3d(x, y, z)
        if(entityHeightValue.get()) {
            GL11.glVertex3d(x, y + entity.height, z)
        }
        GL11.glEnd()
    }

    fun drawTraces(entity: Entity, color: Color, drawHeight: Boolean) {
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosZ)

        val eyeVector = Vec3(0.0, 0.0, 1.0)
            .rotatePitch((-Math.toRadians(mc.thePlayer.rotationPitch.toDouble())).toFloat())
            .rotateYaw((-Math.toRadians(mc.thePlayer.rotationYaw.toDouble())).toFloat())

        RenderUtils.glColor(color)

        GL11.glVertex3d(eyeVector.xCoord, mc.thePlayer.getEyeHeight().toDouble() + eyeVector.yCoord, eyeVector.zCoord)
        if (drawHeight) {
            GL11.glVertex3d(x, y, z)
            GL11.glVertex3d(x, y, z)
            GL11.glVertex3d(x, y + entity.height, z)
        } else {
            GL11.glVertex3d(x, y + entity.height / 2.0, z)
        }

    }

}
