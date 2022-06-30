/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.files.animations.Animation
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.files.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.utils.math.MathUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ESPUtil
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShaderUtil
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.OpenGlHelper.glUniform1
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13

import java.awt.Color
import java.util.function.Consumer

@ModuleInfo(name = "GlowESP", category = ModuleCategory.RENDER)
class GlowESP : Module() {
    val radius = FloatValue("Radius", 2f, 1f, 30f)
    val exposure = FloatValue("Exposure", 2.2f, 1f, 3.5f)
    val seperate = BoolValue("Seperate Texture", false)
    val Players = BoolValue("Players", false)
    val Animals = BoolValue("Animals", false)
    val Mobs = BoolValue("Mobs", false)
    private val outlineShader: ShaderUtil = ShaderUtil("shaders/outline.frag")
    private val glowShader: ShaderUtil = ShaderUtil("shaders/glow.frag")
    var framebuffer: Framebuffer? = null
    var outlineFrameBuffer: Framebuffer? = null
    var glowFrameBuffer: Framebuffer? = null
    private val frustum = Frustum()
    private val entities: MutableList<Entity> = ArrayList()
    override fun onEnable() {
        super.onEnable()
        fadeIn = DecelerateAnimation(250, 1.0)
    }

    fun createFrameBuffers() {
        framebuffer = RenderUtils.createFrameBuffer(framebuffer)
        outlineFrameBuffer = RenderUtils.createFrameBuffer(outlineFrameBuffer)
        glowFrameBuffer = RenderUtils.createFrameBuffer(glowFrameBuffer)
    }

    @EventTarget
    fun onrender3D(event: Render3DEvent) {
        createFrameBuffers()
        collectEntities()
        framebuffer!!.framebufferClear()
        framebuffer!!.bindFramebuffer(true)
        renderEntities(event.partialTicks)
        framebuffer!!.unbindFramebuffer()
        mc.framebuffer.bindFramebuffer(true)
        GlStateManager.disableLighting()
    }

    @EventTarget
    fun onrender2D(event: Render2DEvent?) {
        val sr = ScaledResolution(mc)
        if (framebuffer != null && outlineFrameBuffer != null && entities.size > 0) {
            GlStateManager.enableAlpha()
            GlStateManager.alphaFunc(516, 0.0f)
            GlStateManager.enableBlend()
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            outlineFrameBuffer!!.framebufferClear()
            outlineFrameBuffer!!.bindFramebuffer(true)
            outlineShader.init()
            setupOutlineUniforms(0f, 1f)
            RenderUtils.bindTexture(framebuffer!!.framebufferTexture)
            ShaderUtil.drawQuads()
            outlineShader.init()
            setupOutlineUniforms(1f, 0f)
            RenderUtils.bindTexture(framebuffer!!.framebufferTexture)
            ShaderUtil.drawQuads()
            outlineShader.unload()
            outlineFrameBuffer!!.unbindFramebuffer()
            GlStateManager.color(1f, 1f, 1f, 1f)
            glowFrameBuffer!!.framebufferClear()
            glowFrameBuffer!!.bindFramebuffer(true)
            glowShader.init()
            setupGlowUniforms(1f, 0f)
            RenderUtils.bindTexture(outlineFrameBuffer!!.framebufferTexture)
            ShaderUtil.drawQuads()
            glowShader.unload()
            glowFrameBuffer!!.unbindFramebuffer()
            mc.framebuffer.bindFramebuffer(true)
            glowShader.init()
            setupGlowUniforms(0f, 1f)
            if (seperate.get()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE16)
                RenderUtils.bindTexture(framebuffer!!.framebufferTexture)
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            RenderUtils.bindTexture(glowFrameBuffer!!.framebufferTexture)
            ShaderUtil.drawQuads()
            glowShader.unload()
        }
    }

    fun setupGlowUniforms(dir1: Float, dir2: Float) {
        val color = color
        glowShader.setUniformi("texture", 0)
        if (seperate.get()) {
            glowShader.setUniformi("textureToCheck", 16)
        }
        glowShader.setUniformf("radius", radius.get())
        glowShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight)
        glowShader.setUniformf("direction", dir1, dir2)
        glowShader.setUniformf("color", color!!.red / 255f, color.green / 255f, color.blue / 255f)
        glowShader.setUniformf("exposure", (exposure.get() * fadeIn!!.getOutput()) as Float)
        glowShader.setUniformi("avoidTexture", if (seperate.get()) 1 else 0)
        val buffer = BufferUtils.createFloatBuffer(256)
        for (i in 1..radius.value.toInt()) {
            buffer.put(MathUtils.calculateGaussianValue(i.toFloat(), radius.get() / 2))
        }
        buffer.rewind()
        glUniform1(glowShader.getUniform("weights"), buffer)
    }

    fun setupOutlineUniforms(dir1: Float, dir2: Float) {
        val color = color
        outlineShader.setUniformi("texture", 0)
        outlineShader.setUniformf("radius", radius.get() / 1.5f)
        outlineShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight)
        outlineShader.setUniformf("direction", dir1, dir2)
        outlineShader.setUniformf("color", color!!.red / 255f, color.green / 255f, color.blue / 255f)
    }

    fun renderEntities(ticks: Float) {
        entities.forEach(Consumer { entity: Entity? ->
            renderNameTags = false
            mc.renderManager.renderEntityStatic(entity, ticks, false)
            renderNameTags = true
        })
    }

    private val color: Color?
        private get() {
            val hudMod: HUD? = LiquidBounce.moduleManager.getModule(HUD::class.java) as HUD?
            val colors: Array<Color> = hudMod!!.getClientColors()!!
            return if (hudMod!!.movingcolors.get()) {
                colors[0]
            } else {
                ColorUtils.interpolateColorsBackAndForth(15, 0, colors[0], colors[1], hudMod!!.hueInterpolation.get())
            }
        }

    fun collectEntities() {
        entities.clear()
        for (entity in mc.theWorld.getLoadedEntityList()) {
            if (!ESPUtil.isInView(entity)) continue
            if (entity === mc.thePlayer && mc.gameSettings.thirdPersonView == 0) continue
            if (entity is EntityAnimal && Animals.get()) {
                entities.add(entity)
            }
            if (entity is EntityPlayer && Players.get()) {
                entities.add(entity)
            }
            if (entity is EntityMob && Mobs.get()) {
                entities.add(entity)
            }
        }
    }

    companion object {
        var renderNameTags = true
        var fadeIn: Animation? = null
    }
}