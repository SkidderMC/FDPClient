/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
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
class RearView : Element("RearView") {

    private var Fov by int("Fov", 110, 30..170)
    private var framebufferWidth by int("Framebuffer Width", 800, 800..1920)
    private var framebufferHeight by int("Framebuffer Height", 600, 600..1080)
    private var thirdPersonView by boolean("Third Person View", false)

    private var pos: Vec3 = Vec3(0.0, 0.0, 0.0)
    private var yaw: Float = 0f
    private var pitch: Float = 0f

    private var isRecording: Boolean = false
    private var isValid: Boolean = false
    private var isRendering: Boolean = false
    private var firstUpdate = false

    private lateinit var frameBuffer: Framebuffer

    init {
        updateFramebuffer()
    }

    override fun updateElement() {
        update()
    }

    override fun drawElement(): Border {
        val xOffset = 2f
        val yOffset = 100f
        val sr = ScaledResolution(mc)
        isRendering = true

        drawRect(
            sr.scaledWidth - xOffset - 201,
            sr.scaledHeight - yOffset - 121,
            sr.scaledWidth - xOffset + 1,
            sr.scaledHeight - yOffset + 1,
            -1
        )

        if (isValid) {
            pos = mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks).subtract(0.0, 1.0, 0.0)
            yaw = mc.thePlayer.rotationYaw - 180.0f
            pitch = 0.0f

            render(
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

    private fun updateFramebuffer() {
        if (::frameBuffer.isInitialized) {
            frameBuffer.deleteFramebuffer()
        }
        frameBuffer = Framebuffer(framebufferWidth, framebufferHeight, true)
    }

    private fun updateFbo() {
        if (!firstUpdate) {
            mc.renderGlobal.loadRenderers()
            firstUpdate = true
        }

        mc.thePlayer?.let { player ->
            val originalState = saveState()
            try {
                player.posX = pos.xCoord
                player.posY = pos.yCoord
                player.posZ = pos.zCoord

                player.prevPosX = pos.xCoord
                player.prevPosY = pos.yCoord
                player.prevPosZ = pos.zCoord

                player.lastTickPosX = pos.xCoord
                player.lastTickPosY = pos.yCoord
                player.lastTickPosZ = pos.zCoord

                player.rotationYaw = yaw
                player.prevRotationYaw = yaw
                player.rotationPitch = pitch
                player.prevRotationPitch = pitch
                player.isSprinting = false

                mc.gameSettings.hideGUI = true
                mc.gameSettings.clouds = 0
                mc.gameSettings.thirdPersonView = if (thirdPersonView) 1 else 0
                mc.gameSettings.ambientOcclusion = 0
                mc.gameSettings.viewBobbing = false
                mc.gameSettings.particleSetting = 0
                mc.displayWidth = framebufferWidth
                mc.displayHeight = framebufferHeight

                mc.gameSettings.limitFramerate = 10
                mc.gameSettings.fovSetting = Fov.toFloat()

                isRecording = true
                frameBuffer.bindFramebuffer(true)

                mc.entityRenderer.renderWorld(mc.timer.renderPartialTicks, System.nanoTime())
                mc.entityRenderer.setupOverlayRendering()

                frameBuffer.unbindFramebuffer()
                isRecording = false

                isValid = true
            } finally {
                restoreState(originalState)
                isRendering = false
            }
        }
    }

    private data class RenderState(
        val posX: Double,
        val posY: Double,
        val posZ: Double,
        val prevPosX: Double,
        val prevPosY: Double,
        val prevPosZ: Double,
        val lastTickPosX: Double,
        val lastTickPosY: Double,
        val lastTickPosZ: Double,
        val rotationYaw: Float,
        val prevRotationYaw: Float,
        val rotationPitch: Float,
        val prevRotationPitch: Float,
        val isSprinting: Boolean,
        val hideGUI: Boolean,
        val clouds: Int,
        val thirdPersonView: Int,
        val gamma: Float,
        val ambientOcclusion: Int,
        val viewBobbing: Boolean,
        val particleSetting: Int,
        val displayWidth: Int,
        val displayHeight: Int,
        val limitFramerate: Int,
        val fovSetting: Float
    )

    private fun saveState(): RenderState {
        val settings = mc.gameSettings
        val player = mc.thePlayer
        return RenderState(
            posX = player.posX,
            posY = player.posY,
            posZ = player.posZ,
            prevPosX = player.prevPosX,
            prevPosY = player.prevPosY,
            prevPosZ = player.prevPosZ,
            lastTickPosX = player.lastTickPosX,
            lastTickPosY = player.lastTickPosY,
            lastTickPosZ = player.lastTickPosZ,
            rotationYaw = player.rotationYaw,
            prevRotationYaw = player.prevRotationYaw,
            rotationPitch = player.rotationPitch,
            prevRotationPitch = player.prevRotationPitch,
            isSprinting = player.isSprinting,
            hideGUI = settings.hideGUI,
            clouds = settings.clouds,
            thirdPersonView = settings.thirdPersonView,
            gamma = settings.gammaSetting,
            ambientOcclusion = settings.ambientOcclusion,
            viewBobbing = settings.viewBobbing,
            particleSetting = settings.particleSetting,
            displayWidth = mc.displayWidth,
            displayHeight = mc.displayHeight,
            limitFramerate = settings.limitFramerate,
            fovSetting = settings.fovSetting
        )
    }

    private fun restoreState(state: RenderState) {
        val settings = mc.gameSettings
        val player = mc.thePlayer

        player.posX = state.posX
        player.posY = state.posY
        player.posZ = state.posZ

        player.prevPosX = state.prevPosX
        player.prevPosY = state.prevPosY
        player.prevPosZ = state.prevPosZ

        player.lastTickPosX = state.lastTickPosX
        player.lastTickPosY = state.lastTickPosY
        player.lastTickPosZ = state.lastTickPosZ

        player.rotationYaw = state.rotationYaw
        player.prevRotationYaw = state.prevRotationYaw
        player.rotationPitch = state.rotationPitch
        player.prevRotationPitch = state.prevRotationPitch
        player.isSprinting = state.isSprinting

        settings.hideGUI = state.hideGUI
        settings.clouds = state.clouds
        settings.thirdPersonView = state.thirdPersonView
        settings.gammaSetting = state.gamma
        settings.ambientOcclusion = state.ambientOcclusion
        settings.viewBobbing = state.viewBobbing
        settings.particleSetting = state.particleSetting
        mc.displayWidth = state.displayWidth
        mc.displayHeight = state.displayHeight
        settings.limitFramerate = state.limitFramerate
        settings.fovSetting = state.fovSetting
    }

    fun changeFov(fov: Int) {
        if (fov in 30..170) {
            Fov = fov
        } else {
            throw IllegalArgumentException("FOV must be between 30 and 170")
        }
    }

    fun changeFramebufferResolution(width: Int, height: Int) {
        if (width in 100..1920 && height in 100..1080) {
            framebufferWidth = width
            framebufferHeight = height
            updateFramebuffer()
        } else {
            throw IllegalArgumentException("Resolution must be within valid range")
        }
    }

    fun toggleThirdPersonView(enabled: Boolean) {
        thirdPersonView = enabled
    }
}
