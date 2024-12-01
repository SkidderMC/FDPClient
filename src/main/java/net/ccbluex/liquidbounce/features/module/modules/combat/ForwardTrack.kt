/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.float
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object ForwardTrack : Module("ForwardTrack", Category.COMBAT) {
    private val espMode by choices("ESP-Mode", arrayOf("Box", "Model", "Wireframe"), "Model", subjective = true)
    private val wireframeWidth by float("WireFrame-Width", 1f, 0.5f..5f) { espMode == "WireFrame" }

    private val espColorMode by choices("ESP-Color", arrayOf("Custom", "Rainbow"), "Custom") { espMode != "Model" }
    private val espColor = ColorSettingsInteger(this, "ESP", withAlpha = false)
    { espColorMode == "Custom" && espMode != "Model" }.with(0, 255, 0)

    val color
        get() = if (espColorMode == "Rainbow") rainbow() else Color(espColor.color().rgb)

    /**
     * Any good anti-cheat will easily detect this module.
     */
    fun includeEntityTruePos(entity: Entity, action: () -> Unit) {
        if (!handleEvents() || entity !is EntityLivingBase || entity is EntityPlayerSP)
            return

        // Would be more fun if we simulated instead.
        Backtrack.runWithSimulatedPosition(entity, usePosition(entity)) {
            action()

            null
        }
    }

    private fun usePosition(entity: Entity): Vec3 {
        entity.run {
            return if (!mc.isSingleplayer) {
                val iEntity = entity as IMixinEntity

                if (iEntity.truePos) iEntity.interpolatedPosition else positionVector
            } else if (this is EntityLivingBase) {
                Vec3(newPosX, newPosY, newPosZ)
            } else positionVector
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val world = mc.theWorld ?: return

        val renderManager = mc.renderManager

        for (target in world.loadedEntityList) {
            if (target is EntityPlayerSP)
                continue

            target?.run {
                val vec = usePosition(this)

                val (x, y, z) = vec - renderManager.renderPos

                when (espMode.lowercase()) {
                    "box" -> {
                        val axisAlignedBB = entityBoundingBox.offset(-posX, -posY, -posZ).offset(x, y, z)

                        drawBacktrackBox(axisAlignedBB, color)
                    }

                    "model" -> {
                        glPushMatrix()

                        color(0.6f, 0.6f, 0.6f, 1f)
                        renderManager.doRenderEntity(
                            this,
                            x, y, z,
                            (prevRotationYaw..rotationYaw).lerpWith(event.partialTicks),
                            event.partialTicks,
                            true
                        )

                        glPopMatrix()
                    }

                    "wireframe" -> {
                        val color = if (espColorMode == "Rainbow") rainbow() else Color(espColor.color().rgb)

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
                            this,
                            x, y, z,
                            (prevRotationYaw..rotationYaw).lerpWith(event.partialTicks),
                            event.partialTicks,
                            true
                        )
                        glColor(color)
                        renderManager.doRenderEntity(
                            this,
                            x, y, z,
                            (prevRotationYaw..rotationYaw).lerpWith(event.partialTicks),
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
}