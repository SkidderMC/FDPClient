/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.block.material
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.isSplashPotion
import net.ccbluex.liquidbounce.utils.render.ColorUtils.interpolateHSB
import net.ccbluex.liquidbounce.utils.render.RenderUtils.disableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.enableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetCaps
import net.ccbluex.liquidbounce.utils.render.drawWithTessellatorWorldRenderer
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.item.EntityExpBottle
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.*
import net.minecraft.item.*
import net.minecraft.util.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Projectiles : Module("Projectiles", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {
    private val maxTrailSize by int("MaxTrailSize", 20, 1..100)

    private val colorMode by choices("ColorMode", arrayOf("Custom", "BowPower"), "Custom")
    private val color by color("Color", Color(0, 160, 255)) { colorMode == "Custom" }

    private val trailPositions = mutableMapOf<Entity, ArrayDeque<ProjectilePos>>()

    val onRender3D = handler<Render3DEvent> {
        val theWorld = mc.theWorld ?: return@handler
        val renderManager = mc.renderManager

        for (entity in theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase) continue
            val heldStack = entity.heldItem ?: continue

            // Check if the entity is selected
            if (!isSelected(entity, false)) {
                continue
            }

            val item = heldStack.item
            var isBow = false
            var motionFactor = 1.5F
            var motionSlowdown = 0.99F
            val gravity: Float
            val size: Float

            // Check items
            when (item) {
                is ItemBow -> {
                    isBow = true
                    gravity = 0.05F
                    size = 0.3F

                    if (entity is EntityPlayer) {
                        if (!entity.isUsingItem) continue

                        // Calculate power of bow
                        var power = entity.itemInUseDuration / 20f
                        power = (power * power + power * 2F) / 3F
                        if (power < 0.1F) continue
                        if (power > 1F) power = 1F
                        motionFactor = power * 3F
                    } else {
                        // Approximate bow power for other Entities (ex: Skeletons)
                        motionFactor = 3F
                    }
                }

                is ItemFishingRod -> {
                    gravity = 0.04F
                    size = 0.25F
                    motionSlowdown = 0.92F
                }

                is ItemPotion -> {
                    if (!heldStack.isSplashPotion()) continue
                    gravity = 0.05F
                    size = 0.25F
                    motionFactor = 0.5F
                }

                is ItemSnowball, is ItemEnderPearl, is ItemEgg -> {
                    gravity = 0.03F
                    size = 0.25F
                }

                else -> continue
            }

            // Yaw and pitch of player
            val (yaw, pitch) = entity.rotation

            val yawRadians = yaw.toRadiansD()
            val pitchRadians = pitch.toRadiansD()

            val pos = entity.interpolatedPosition(entity.lastTickPos)

            // Positions
            var posX = pos.xCoord - cos(yawRadians) * 0.16F
            var posY = pos.yCoord + entity.eyeHeight - 0.10000000149011612
            var posZ = pos.zCoord - sin(yawRadians) * 0.16F

            // Motions
            var motionX = -sin(yawRadians) * cos(pitchRadians) * if (isBow) 1.0 else 0.4
            var motionY = -sin((pitch + if (item is ItemPotion) -20 else 0).toRadians()) * if (isBow) 1.0 else 0.4
            var motionZ = cos(yawRadians) * cos(pitchRadians) * if (isBow) 1.0 else 0.4
            val distance = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)

            motionX /= distance
            motionY /= distance
            motionZ /= distance
            motionX *= motionFactor
            motionY *= motionFactor
            motionZ *= motionFactor

            // Landing
            var landingPosition: MovingObjectPosition? = null
            var hasLanded = false
            var hitEntity = false

            val tessellator = Tessellator.getInstance()
            val worldRenderer = tessellator.worldRenderer

            glPushMatrix()

            // Start drawing of path
            glDepthMask(false)
            enableGlCap(GL_BLEND, GL_LINE_SMOOTH)
            disableGlCap(GL_DEPTH_TEST, GL_ALPHA_TEST, GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
            glColor(
                when (colorMode.lowercase()) {
                    "bowpower" -> interpolateHSB(Color.RED, Color.GREEN, (motionFactor / 30) * 10)
                    else -> color
                }
            )
            glLineWidth(2f)

            worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

            while (!hasLanded && posY > 0.0) {
                // Set pos before and after
                var posBefore = Vec3(posX, posY, posZ)
                var posAfter = Vec3(posX + motionX, posY + motionY, posZ + motionZ)

                // Get landing position
                landingPosition = theWorld.rayTraceBlocks(
                    posBefore, posAfter, false,
                    true, false
                )

                // Set pos before and after
                posBefore = Vec3(posX, posY, posZ)
                posAfter = Vec3(posX + motionX, posY + motionY, posZ + motionZ)

                // Check if arrow is landing
                if (landingPosition != null) {
                    hasLanded = true
                    posAfter = landingPosition.hitVec.copy()
                }

                // Set arrow box
                val arrowBox = AxisAlignedBB(
                    posX - size, posY - size, posZ - size, posX + size,
                    posY + size, posZ + size
                ).addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0)

                val chunkMinX = ((arrowBox.minX - 2) / 16).toInt()
                val chunkMaxX = ((arrowBox.maxX + 2.0) / 16.0).toInt()
                val chunkMinZ = ((arrowBox.minZ - 2.0) / 16.0).toInt()
                val chunkMaxZ = ((arrowBox.maxZ + 2.0) / 16.0).toInt()

                // Check which entities colliding with the arrow
                val collidedEntities = mutableListOf<Entity>()

                for (x in chunkMinX..chunkMaxX)
                    for (z in chunkMinZ..chunkMaxZ)
                        theWorld.getChunkFromChunkCoords(x, z)
                            .getEntitiesWithinAABBForEntity(entity, arrowBox, collidedEntities, null)

                // Check all possible entities
                for (possibleEntity in collidedEntities) {
                    if (possibleEntity.canBeCollidedWith() && possibleEntity != entity) {
                        val possibleEntityBoundingBox = possibleEntity.entityBoundingBox
                            .expand(size.toDouble(), size.toDouble(), size.toDouble())

                        val possibleEntityLanding = possibleEntityBoundingBox
                            .calculateIntercept(posBefore, posAfter) ?: continue

                        hitEntity = true
                        hasLanded = true
                        landingPosition = possibleEntityLanding
                    }
                }

                // Affect motions of arrow
                posX += motionX
                posY += motionY
                posZ += motionZ

                // Check is next position water
                if (BlockPos(posX, posY, posZ).material === Material.water) {
                    // Update motion
                    motionX *= 0.6
                    motionY *= 0.6
                    motionZ *= 0.6
                } else { // Update motion
                    motionX *= motionSlowdown.toDouble()
                    motionY *= motionSlowdown.toDouble()
                    motionZ *= motionSlowdown.toDouble()
                }

                motionY -= gravity.toDouble()

                // Draw path
                worldRenderer.pos(
                    posX - renderManager.renderPosX, posY - renderManager.renderPosY,
                    posZ - renderManager.renderPosZ
                ).endVertex()
            }

            // End the rendering of the path
            tessellator.draw()

            glDepthMask(true)
            resetCaps()
            resetColor()

            glPopMatrix()

            glPushMatrix()

            glDepthMask(false)
            enableGlCap(GL_BLEND, GL_LINE_SMOOTH)
            disableGlCap(GL_DEPTH_TEST, GL_ALPHA_TEST, GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

            glTranslated(
                posX - renderManager.renderPosX,
                posY - renderManager.renderPosY,
                posZ - renderManager.renderPosZ
            )

            if (landingPosition != null) {
                // Accurate landing position checking
                when (landingPosition.sideHit!!) {
                    EnumFacing.DOWN -> glRotatef(90F, 0F, 1F, 0F)
                    EnumFacing.UP -> glRotatef(-90F, 0F, 1F, 0F)
                    EnumFacing.NORTH -> glRotatef(-90F, 1F, 0F, 0F)
                    EnumFacing.SOUTH -> glRotatef(90F, 1F, 0F, 0F)
                    EnumFacing.WEST -> glRotatef(-90F, 0F, 0F, 1F)
                    EnumFacing.EAST -> glRotatef(90F, 0F, 0F, 1F)
                    else -> glRotatef(90F, 0F, 0F, 1F)
                }

                // Check if hitting an entity
                if (hitEntity) {
                    glColor(Color(255, 0, 0, 150))
                }
            }

            // Rendering hit cylinder
            glRotatef(-90F, 1F, 0F, 0F)

            val cylinder = Cylinder()
            cylinder.drawStyle = GLU.GLU_LINE
            cylinder.draw(0.2F, 0F, 0F, 60, 1)

            glDepthMask(true)
            resetCaps()
            resetColor()

            glPopMatrix()
        }

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_LIGHTING)
        glLineWidth(2.0f)

        for ((entity, positions) in trailPositions) {
            if (positions.isEmpty()) continue

            drawWithTessellatorWorldRenderer {
                begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

                for ((_, pos, alpha) in positions) {
                    val interpolatePos = pos - renderManager.renderPos

                    val color = when (entity) {
                        is EntityArrow -> Color(255, 0, 0)
                        is EntityPotion -> Color(200, 150, 0)
                        is EntityEnderPearl -> Color(200, 0, 200)
                        is EntityFireball -> Color(255, 255, 0)
                        is EntityEgg, is EntitySnowball -> Color(200, 255, 200)
                        else -> Color(255, 255, 255)
                    }

                    glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, alpha)

                    pos(interpolatePos.xCoord, interpolatePos.yCoord, interpolatePos.zCoord).endVertex()
                }
            }
        }

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_LIGHTING)
        resetColor()

        glPopMatrix()
        glPopAttrib()
    }

    val onUpdate = handler<UpdateEvent> {
        val world = mc.theWorld ?: return@handler

        val currentTime = System.currentTimeMillis()

        for (entity in world.loadedEntityList) {
            if (entity == null) {
                trailPositions.clear()
                continue
            }

            when (entity) {
                is EntitySnowball, is EntityEnderPearl, is EntityEgg,
                is EntityArrow, is EntityPotion, is EntityExpBottle, is EntityFireball -> {
                    val positions = trailPositions.getOrPut(entity, ::ArrayDeque)

                    positions.removeIf { (timestamp, _, alpha) ->
                        currentTime - timestamp > 10000 || alpha <= 0
                    }

                    if (positions.size > maxTrailSize) {
                        positions.removeAt(0)
                    }

                    positions.add(ProjectilePos(currentTime, Vec3(entity.posX, entity.posY, entity.posZ), 1.0f))
                }
            }
        }

        // Gradually fade out trails of entities no longer in the world
        for (positions in trailPositions.values) {
            for (i in positions.indices) {
                val (timestamp, pos, alpha) = positions[i]
                positions[i] = ProjectilePos(timestamp, pos, alpha - 0.04f)
            }
        }

        // Remove entities that are no longer in the world
        val worldEntities = world.loadedEntityList.toHashSet()
        trailPositions.keys.removeIf { it !in worldEntities && trailPositions[it]?.all { (_, _, alpha) -> alpha <= 0 } == true }
    }

    private data class ProjectilePos(val timestamp: Long, val pos: Vec3, val alpha: Float)
}