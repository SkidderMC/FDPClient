/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isRotationFaced
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.ResourceLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import org.lwjgl.opengl.GL11

object AntiFireball : Module("AntiFireball", Category.PLAYER) {

    private val indicators by boolean("Indicator", true)
    private val range by float("Range", 4.5f, 3f..8f)
    private val swing by choices("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val options = RotationSettings(this).withoutKeepRotation()
    private val fireballTickCheck by boolean("FireballTickCheck", true)
    private val minFireballTick by int("MinFireballTick", 10, 1..20) { fireballTickCheck }
    private val scale by float("Size", 0.7f, 0.65f..1.25f) { indicators }
    private val radius by float("Radius", 50f, 15f..150f) { indicators }

    private var target: Entity? = null
    var distance = 0f
    lateinit var displayName: String

    val onRotationUpdate = handler<RotationUpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        target = null

        for (entity in world.loadedEntityList.filterIsInstance<EntityFireball>()
            .sortedBy { player.getDistanceToBox(it.hitBox) }) {
            val nearestPoint = getNearestPointBB(player.eyes, entity.hitBox)
            val entityPrediction = entity.currPos - entity.prevPos
            val normalDistance = player.getDistanceToBox(entity.hitBox)
            val predictedDistance = player.getDistanceToBox(entity.hitBox.offset(entityPrediction))

            // Skip if the predicted distance is further than (or the same as) the normal distance
            // or the predicted distance is out of reach
            if (predictedDistance >= normalDistance || predictedDistance > range) {
                continue
            }

            // Skip if the fireball entity tick exist is lower than minFireballTick
            if (fireballTickCheck && entity.ticksExisted <= minFireballTick) {
                continue
            }

            if (options.rotationsActive) {
                setTargetRotation(toRotation(nearestPoint, true), options = options)
            }

            target = entity
            break
        }
    }

    val onRender2D = handler<Render2DEvent> {
        val t = ScaledResolution(mc)
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity.name == "Fireball") {
                distance = floor(mc.thePlayer.getDistanceToEntity(entity))
                displayName = entity.name

                val scaleFactor = scale
                val entX = entity.posX
                val entZ = entity.posZ
                val px = mc.thePlayer.posX
                val pz = mc.thePlayer.posZ
                val pYaw = mc.thePlayer.rotationYaw
                val radiusFactor = radius
                val yaw = Math.toRadians(getRotations(entX, entZ, px, pz) - pYaw)
                val arrowX = t.scaledWidth / 2 + radiusFactor * sin(yaw)
                val arrowY = t.scaledHeight / 2 - radiusFactor * cos(yaw)
                val textX = t.scaledWidth / 2 + (radiusFactor - 13) * sin(yaw)
                val textY = t.scaledHeight / 2 - (radiusFactor - 13) * cos(yaw)
                val imgX = t.scaledWidth / 2 + (radiusFactor - 18) * sin(yaw)
                val imgY = t.scaledHeight / 2 - (radiusFactor - 18) * cos(yaw)
                val arrowAngle = atan2(arrowY - t.scaledHeight / 2, arrowX - t.scaledWidth / 2)

                drawArrow(arrowX, arrowY, arrowAngle, 3.0, 100.0)
                GlStateManager.color(255f, 255f, 255f, 255f)

                if (displayName == "Fireball" && indicators) {
                    GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor)
                    RenderUtils.drawImage(
                        ResourceLocation("textures/items/fireball.png"),
                        (imgX / scaleFactor - 5).toInt(),
                        (imgY / scaleFactor - 5).toInt(),
                        32,
                        32
                    )
                    GlStateManager.scale(1 / scaleFactor, 1 / scaleFactor, 1 / scaleFactor)
                }
                GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor)
                Fonts.minecraftFont.drawStringWithShadow(
                    "$distance" + "m",
                    (textX / scaleFactor - (Fonts.minecraftFont.getStringWidth("$distance" + "m") / 2)).toFloat(),
                    (textY / scaleFactor - 4).toFloat(),
                    -1
                )
                GlStateManager.scale(1 / scaleFactor, 1 / scaleFactor, 1 / scaleFactor)
            }
        }
    }

    private fun drawArrow(x: Double, y: Double, angle: Double, size: Double, degrees: Double) {
        // Enable OpenGL line smoothing
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        try {
            val arrowSize = size * 2
            val arrowX = x - arrowSize * cos(angle)
            val arrowY = y - arrowSize * sin(angle)
            val arrowAngle1 = angle + Math.toRadians(degrees)
            val arrowAngle2 = angle - Math.toRadians(degrees)
            RenderUtils.drawLine(
                x,
                y,
                arrowX + arrowSize * cos(arrowAngle1),
                arrowY + arrowSize * sin(arrowAngle1),
                size.toFloat()
            )
            RenderUtils.drawLine(
                x,
                y,
                arrowX + arrowSize * cos(arrowAngle2),
                arrowY + arrowSize * sin(arrowAngle2),
                size.toFloat()
            )
        } finally {
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
        }
    }

    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler
        val entity = target ?: return@handler
        val rotation = currentRotation ?: player.rotation

        if (!options.rotationsActive && player.getDistanceToBox(entity.hitBox) <= range
            || isRotationFaced(entity, range.toDouble(), rotation)
        ) {
            player.attackEntityWithModifiedSprint(entity) {
                when (swing) {
                    "Normal" -> mc.thePlayer.swingItem()
                    "Packet" -> sendPacket(C0APacketAnimation())
                }
            }
            target = null
        }
    }

    private fun getRotations(eX: Double, eZ: Double, x: Double, z: Double): Double {
        val xDiff = eX - x
        val zDiff = eZ - z
        return -(atan2(xDiff, zDiff) * 57.29577951308232)
    }
}