/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.Vec3

@ElementInfo(name = "RearView")
class RearView : Element() {
    var pos: Vec3

    var yaw: Float

    var pitch: Float

    private var isRecording: Boolean = false

    private var isValid: Boolean = false

    private var isRendering: Boolean = false

    private var firstUpdate = false

    private val frameBuffer: Framebuffer

    private val WIDTH_RESOLUTION = 800
    private val HEIGHT_RESOLUTION = 600

    init {
        this.pos = Vec3(0.0, 0.0, 0.0)
        this.yaw = 0f
        this.pitch = 0f
        this.frameBuffer = Framebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION, true)
    }

    override fun updateElement() {
        update()
    }

    override fun drawElement(): Border {
        val xOffset = 2f
        val yOffset = 100f
        val sr = ScaledResolution(mc)
        this.isRendering = true
        drawRect(
            sr.scaledWidth - xOffset - 201,
            sr.scaledHeight - yOffset - 121,
            sr.scaledWidth - xOffset + 1,
            sr.scaledHeight - yOffset + 1,
            -1
        ) //background
        if (this.isValid) {
            this.pos =
                mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks).subtract(0.0, 1.0, 0.0)
            this.yaw = mc.thePlayer.rotationYaw - 180.0f
            this.pitch = 0.0f
            this.render(
                sr.scaledWidth - xOffset - 200,

                sr.scaledHeight - yOffset - 120,

                sr.scaledWidth - xOffset,

                sr.scaledHeight - yOffset
            )
        }
        return Border(
            sr.scaledWidth - xOffset - 201,
            sr.scaledHeight - yOffset - 121,
            sr.scaledWidth - xOffset + 1,
            sr.scaledHeight - yOffset + 1
        )
    }

    fun render(x: Float, y: Float, w: Float, h: Float) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            GlStateManager.pushMatrix()
            GlStateManager.enableTexture2D()
            GlStateManager.disableLighting()
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            frameBuffer.bindFramebufferTexture()
            val tessellator = Tessellator.getInstance()
            val worldRenderer = tessellator.worldRenderer
            worldRenderer.begin(6, DefaultVertexFormats.POSITION_TEX)
            worldRenderer.pos(x.toDouble(), h.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
            worldRenderer.pos(w.toDouble(), h.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
            worldRenderer.pos(w.toDouble(), y.toDouble(), 0.0).tex(1.0, 1.0).endVertex()
            worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 1.0).endVertex()
            tessellator.draw()
            frameBuffer.unbindFramebufferTexture()
            GlStateManager.popMatrix()
        }
    }

    fun update() {
        if (!isRecording && isRendering) {
            updateFbo()
        }
    }

    private fun updateFbo() {
        if (!this.firstUpdate) {
            mc.renderGlobal.loadRenderers()
            this.firstUpdate = true
        }
        if (mc.thePlayer != null) {
            val posX = mc.thePlayer.posX
            val posY = mc.thePlayer.posY
            val posZ = mc.thePlayer.posZ
            val prevPosX = mc.thePlayer.prevPosX
            val prevPosY = mc.thePlayer.prevPosY
            val prevPosZ = mc.thePlayer.prevPosZ
            val lastTickPosX = mc.thePlayer.lastTickPosX
            val lastTickPosY = mc.thePlayer.lastTickPosY
            val lastTickPosZ = mc.thePlayer.lastTickPosZ

            val rotationYaw = mc.thePlayer.rotationYaw
            val prevRotationYaw = mc.thePlayer.prevRotationYaw
            val rotationPitch = mc.thePlayer.rotationPitch
            val prevRotationPitch = mc.thePlayer.prevRotationPitch
            val sprinting = mc.thePlayer.isSprinting

            val hideGUI = mc.gameSettings.hideGUI
            val clouds = mc.gameSettings.clouds
            val thirdPersonView = mc.gameSettings.thirdPersonView
            val gamma = mc.gameSettings.gammaSetting
            val ambientOcclusion = mc.gameSettings.ambientOcclusion
            val viewBobbing = mc.gameSettings.viewBobbing
            val particles = mc.gameSettings.particleSetting
            val displayWidth = mc.displayWidth
            val displayHeight = mc.displayHeight

            val frameLimit = mc.gameSettings.limitFramerate
            val fovSetting = mc.gameSettings.fovSetting

            mc.thePlayer.posX = pos.xCoord
            mc.thePlayer.posY = pos.yCoord
            mc.thePlayer.posZ = pos.zCoord

            mc.thePlayer.prevPosX = pos.xCoord
            mc.thePlayer.prevPosY = pos.yCoord
            mc.thePlayer.prevPosZ = pos.zCoord

            mc.thePlayer.lastTickPosX = pos.xCoord
            mc.thePlayer.lastTickPosY = pos.yCoord
            mc.thePlayer.lastTickPosZ = pos.zCoord

            mc.thePlayer.rotationYaw = this.yaw
            mc.thePlayer.prevRotationYaw = this.yaw
            mc.thePlayer.rotationPitch = this.pitch
            mc.thePlayer.prevRotationPitch = this.pitch
            mc.thePlayer.isSprinting = false

            mc.gameSettings.hideGUI = true
            mc.gameSettings.clouds = 0
            mc.gameSettings.thirdPersonView = 0
            mc.gameSettings.ambientOcclusion = 0
            mc.gameSettings.viewBobbing = false
            mc.gameSettings.particleSetting = 0
            mc.displayWidth = WIDTH_RESOLUTION
            mc.displayHeight = HEIGHT_RESOLUTION

            mc.gameSettings.limitFramerate = 10
            mc.gameSettings.fovSetting = 110f

            this.isRecording = true
            frameBuffer.bindFramebuffer(true)

            mc.entityRenderer.renderWorld(mc.timer.renderPartialTicks, System.nanoTime())
            mc.entityRenderer.setupOverlayRendering()

            frameBuffer.unbindFramebuffer()
            this.isRecording = false

            mc.thePlayer.posX = posX
            mc.thePlayer.posY = posY
            mc.thePlayer.posZ = posZ

            mc.thePlayer.prevPosX = prevPosX
            mc.thePlayer.prevPosY = prevPosY
            mc.thePlayer.prevPosZ = prevPosZ

            mc.thePlayer.lastTickPosX = lastTickPosX
            mc.thePlayer.lastTickPosY = lastTickPosY
            mc.thePlayer.lastTickPosZ = lastTickPosZ

            mc.thePlayer.rotationYaw = rotationYaw
            mc.thePlayer.prevRotationYaw = prevRotationYaw
            mc.thePlayer.rotationPitch = rotationPitch
            mc.thePlayer.prevRotationPitch = prevRotationPitch
            mc.thePlayer.isSprinting = sprinting

            mc.gameSettings.hideGUI = hideGUI
            mc.gameSettings.clouds = clouds
            mc.gameSettings.thirdPersonView = thirdPersonView
            mc.gameSettings.gammaSetting = gamma
            mc.gameSettings.ambientOcclusion = ambientOcclusion
            mc.gameSettings.viewBobbing = viewBobbing
            mc.gameSettings.particleSetting = particles
            mc.displayWidth = displayWidth
            mc.displayHeight = displayHeight
            mc.gameSettings.limitFramerate = frameLimit
            mc.gameSettings.fovSetting = fovSetting

            this.isValid = true
            this.isRendering = false
        }
    }
}