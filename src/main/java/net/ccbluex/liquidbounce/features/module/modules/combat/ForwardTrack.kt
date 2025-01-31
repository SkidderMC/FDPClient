/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*

object ForwardTrack : Module("ForwardTrack", Category.COMBAT) {
    private val espMode by choices("ESP-Mode", arrayOf("Box", "Model", "Wireframe"), "Model").subjective()
    private val wireframeWidth by float("WireFrame-Width", 1f, 0.5f..5f) { espMode == "WireFrame" }

    private val espColor = ColorSettingsInteger(this, "ESPColor") { espMode != "Model" }.with(0, 255, 0)

    val color
        get() = espColor.color()

    /**
     * Any good anti-cheat will easily detect this module.
     */
    fun includeEntityTruePos(entity: Entity, action: () -> Unit) {
        if (!handleEvents() || !isSelected(entity, true))
            return

        // Would be more fun if we simulated instead.
        Backtrack.runWithSimulatedPosition(entity, usePosition(entity)) {
            action()

            null
        }
    }

    private fun usePosition(entity: Entity): Vec3 {
        entity.run {
            return when {
                !mc.isSingleplayer -> {
                    val iEntity = entity as IMixinEntity

                    if (iEntity.truePos) iEntity.interpolatedPosition else positionVector
                }

                this is EntityLivingBase -> {
                    Vec3(newPosX, newPosY, newPosZ)
                }

                else -> positionVector
            }
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        val renderManager = mc.renderManager
        val world = mc.theWorld ?: return@handler

        for (target in world.loadedEntityList) {
            if (!isSelected(target, true)) {
                return@handler
            }

            val vec = usePosition(target)

            val (x, y, z) = vec - renderManager.renderPos

            when (espMode.lowercase()) {
                "box" -> {
                    val axisAlignedBB = target.entityBoundingBox.offset(Vec3(x, y, z) - target.currPos)

                    drawBacktrackBox(axisAlignedBB, color)
                }

                "model" -> {
                    glPushMatrix()
                    glPushAttrib(GL_ALL_ATTRIB_BITS)

                    color(0.6f, 0.6f, 0.6f, 1f)
                    renderManager.doRenderEntity(
                        target,
                        x, y, z,
                        (target.prevRotationYaw..target.rotationYaw).lerpWith(event.partialTicks),
                        event.partialTicks,
                        true
                    )

                    glPopAttrib()
                    glPopMatrix()
                }

                "wireframe" -> {
                    glPushMatrix()
                    glPushAttrib(GL_ALL_ATTRIB_BITS)

                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                    glDisable(GL_TEXTURE_2D)
                    glDisable(GL_LIGHTING)
                    glDisable(GL_DEPTH_TEST)
                    glEnable(GL_LINE_SMOOTH)

                    glEnable(GL_BLEND)
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

                    glLineWidth(wireframeWidth)

                    glColor(color)
                    renderManager.doRenderEntity(
                        target,
                        x, y, z,
                        (target.prevRotationYaw..target.rotationYaw).lerpWith(event.partialTicks),
                        event.partialTicks,
                        true
                    )
                    glColor(color)
                    renderManager.doRenderEntity(
                        target,
                        x, y, z,
                        (target.prevRotationYaw..target.rotationYaw).lerpWith(event.partialTicks),
                        event.partialTicks,
                        true
                    )

                    glPopAttrib()
                    glPopMatrix()
                }
            }
        }
    }
}