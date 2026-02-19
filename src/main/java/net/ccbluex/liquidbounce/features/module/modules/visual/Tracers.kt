/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.client.Teams
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.pow

object Tracers : Module("Tracers", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {

    private val colorMode by choices("ColorMode", arrayOf("Custom", "DistanceColor"), "Custom")
    private val color by color("Color", Color(0, 160, 255, 150)) { colorMode == "Custom" }

    private val thickness by float("Thickness", 2F, 1F..5F)

    private val maxRenderDistance by int("MaxRenderDistance", 100, 1..200).onChanged {
        maxRenderDistanceSq = (it * it).toDouble()
    }

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    private val bot by boolean("Bots", true)
    private val teams by boolean("Teams", false)

    private val onLook by boolean("OnLook", false)
    private val maxAngleDifference by float("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by boolean("ThruBlocks", true)

    private val entities by EntityLookup<EntityLivingBase>()
        .filter { isSelected(it, false) }
        .filter { mc.thePlayer.getDistanceSqToEntity(it) <= maxRenderDistanceSq }
        .filter { bot || !isBot(it) }
        .filter { !onLook || mc.thePlayer.isLookingOnEntity(it, maxAngleDifference.toDouble()) }
        .filter { thruBlocks || isEntityHeightVisible(it) }

    // Priority must be set lower than every other Listenable class that also listens to this event.
    // We re-apply camera transformation, which would affect NameTags if the priority was normal.
    val onRender3D = handler<Render3DEvent>(priority = -5) {
        mc.thePlayer ?: return@handler

        val originalViewBobbing = mc.gameSettings.viewBobbing

        // Temporarily disable view bobbing and re-apply camera transformation
        mc.gameSettings.viewBobbing = false
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0)

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(thickness)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        glBegin(GL_LINES)

        for (entity in entities) {
            val dist = mc.thePlayer.getDistanceSqToEntity(entity).coerceAtMost(255.0).toInt()

            val colorMode = colorMode.lowercase()
            val color = when {
                entity is EntityPlayer && entity.isClientFriend() -> Color(0, 0, 255, 150)
                teams && Teams.handleEvents() && Teams.isInYourTeam(entity) -> Color(0, 162, 232)
                colorMode == "custom" -> color
                colorMode == "distancecolor" -> Color(255 - dist, dist, 0, 150)
                else -> Color(255, 255, 255, 150)
            }

            drawTraces(entity, color)
        }

        glEnd()

        mc.gameSettings.viewBobbing = originalViewBobbing

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawTraces(entity: Entity, color: Color) {
        val player = mc.thePlayer ?: return

        val (x, y, z) = entity.interpolatedPosition(entity.lastTickPos) - mc.renderManager.renderPos

        val yaw = (player.prevRotationYaw..player.rotationYaw).lerpWith(mc.timer.renderPartialTicks)
        val pitch = (player.prevRotationPitch..player.rotationPitch).lerpWith(mc.timer.renderPartialTicks)

        val eyeVector = Vec3(0.0, 0.0, 1.0).rotatePitch(-pitch.toRadians()).rotateYaw(-yaw.toRadians())

        glColor(color)

        glVertex3d(eyeVector.xCoord, player.getEyeHeight() + eyeVector.yCoord, eyeVector.zCoord)
        glVertex3d(x, y, z)
        glVertex3d(x, y, z)
        glVertex3d(x, y + entity.height, z)
    }
}
