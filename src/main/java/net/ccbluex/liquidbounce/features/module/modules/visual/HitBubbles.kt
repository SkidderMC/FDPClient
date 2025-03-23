/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.customRotatedObject2D
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object HitBubbles : Module("HitBubbles", Category.VISUAL, gameDetecting = false) {

    init {
        state = true
    }

    private val followHit by boolean("Follow Hit", true)
    private val dynamicRotation by boolean("Dynamic Rotation", false)

    private const val MAX_LIFETIME = 1000.0f
    private val bubbles = arrayListOf<Bubble>()

    private val tessellator = Tessellator.getInstance()
    private val buffer = tessellator.worldRenderer

    private val alphaPercentage: Float get() = 1f
    private val bubbleColor: Int get() = ClientThemesUtils.getColor().rgb
    private val icon = ResourceLocation("${CLIENT_NAME.lowercase()}/bubble.png")

    val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity as? EntityLivingBase ?: return@handler

        val bubblePosition = target.positionVector.addVector(0.0, target.height / 1.6, 0.0)

        val hitLocation = if (followHit) {
            val playerEyes = mc.thePlayer?.getPositionEyes(1.0f) ?: return@handler
            val playerLook = mc.thePlayer?.getLook(1.0f) ?: return@handler
            playerEyes.addVector(
                playerLook.xCoord * 3.0,
                playerLook.yCoord * 3.0,
                playerLook.zCoord * 3.0
            )
        } else {
            bubblePosition
        }

        addBubble(bubblePosition, hitLocation)
    }

    val onRender3D = handler<Render3DEvent> {
        val alpha = alphaPercentage
        if (alpha < 0.05f || bubbles.isEmpty()) return@handler

        removeExpiredBubbles()

        setupBubbleRendering {
            bubbles.forEach { bubble ->
                if (bubble.deltaTime < 1.0f) {
                    drawBubble(bubble, alpha)
                }
            }
        }
    }

    private fun setupBubbleRendering(render: () -> Unit) {
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        pushMatrix()
        try {
            enableBlend()
            disableAlpha()
            depthMask(false)
            disableCull()
            if (glIsEnabled(GL_LIGHTING)) disableLighting()
            glShadeModel(GL_SMOOTH)
            tryBlendFuncSeparate(770, 32772, 1, 0)

            val renderManager = mc.renderManager
            val offset = Vec3(renderManager.renderPosX, renderManager.renderPosY, renderManager.renderPosZ)
            glTranslated(-offset.xCoord, -offset.yCoord, -offset.zCoord)
            mc.textureManager.bindTexture(icon)

            render()

            glTranslated(offset.xCoord, offset.yCoord, offset.zCoord)
            resetColor()
            enableCull()
            depthMask(true)
            enableAlpha()
        } finally {
            popMatrix()
            glPopAttrib()
        }
    }

    private fun drawBubble(bubble: Bubble, alpha: Float) {
        glPushMatrix()
        glTranslated(bubble.position.xCoord, bubble.position.yCoord, bubble.position.zCoord)

        val expansion = bubble.deltaTime
        translate(
            -sin(Math.toRadians(bubble.viewPitch.toDouble())) * expansion / 3.0,
            sin(Math.toRadians(bubble.viewYaw.toDouble())) * expansion / 2.0,
            -cos(Math.toRadians(bubble.viewPitch.toDouble())) * expansion / 3.0
        )

        glNormal3d(1.0, 1.0, 1.0)

        glRotated(bubble.viewPitch.toDouble(), 0.0, 1.0, 0.0)
        glRotated(bubble.viewYaw.toDouble(), if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0, 0.0, 0.0)
        glScaled(-0.1, -0.1, 0.1)

        drawBubbleGraphics(bubble, alpha)
        glPopMatrix()
    }

    private fun calculateDynamicRotation(bubble: Bubble): Double {
        val player = mc.thePlayer ?: return 0.0
        val deltaX = bubble.position.xCoord - player.posX
        val deltaZ = bubble.position.zCoord - player.posZ
        if (deltaX == 0.0 && deltaZ == 0.0) return 0.0
        val angle = Math.toDegrees(atan2(deltaZ, deltaX))
        return angle - player.rotationYaw
    }

    private fun drawBubbleGraphics(bubble: Bubble, alpha: Float) {
        val radius = 50.0f * bubble.deltaTime * (1.0f - bubble.deltaTime)
        val rotationAngle = if (dynamicRotation) calculateDynamicRotation(bubble) else 0.0

        customRotatedObject2D(-radius / 2, -radius / 2, radius, radius, rotationAngle)
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

        val red = ((bubbleColor shr 16) and 0xFF) / 255.0f
        val green = ((bubbleColor shr 8) and 0xFF) / 255.0f
        val blue = (bubbleColor and 0xFF) / 255.0f

        buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(0.0, radius.toDouble(), 0.0).tex(0.0, 1.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(radius.toDouble(), radius.toDouble(), 0.0).tex(1.0, 1.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(radius.toDouble(), 0.0, 0.0).tex(1.0, 0.0).color(red, green, blue, alpha).endVertex()

        tessellator.draw()
    }

    private fun removeExpiredBubbles() {
        bubbles.removeIf { it.deltaTime >= 1.0f }
    }

    private fun addBubble(position: Vec3, hitLocation: Vec3? = null) {
        val renderManager = mc.renderManager
        val finalPosition = if (followHit && hitLocation != null) hitLocation else position
        bubbles.add(Bubble(renderManager.playerViewX, -renderManager.playerViewY, finalPosition))
    }

    data class Bubble(val viewYaw: Float, val viewPitch: Float, val position: Vec3) {
        private val creationTime: Long = System.currentTimeMillis()
        val deltaTime: Float
            get() = (System.currentTimeMillis() - creationTime).toFloat() / MAX_LIFETIME
    }
}
