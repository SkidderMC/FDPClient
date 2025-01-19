/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render.shader.shaders

import net.ccbluex.liquidbounce.utils.render.shader.FramebufferShader
import org.lwjgl.opengl.GL20.*
import java.awt.Color
import java.io.Closeable

object FrostShader : FramebufferShader("frost.frag"), Closeable {
    var isInUse = false
        private set

    var intensity = 0.3f
    var tintColor = Color.WHITE
    var blurRadius = 2f
    var frostAlpha = 0.6f

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("texelSize")
        setupUniform("radius")
        setupUniform("alpha")
        setupUniform("intensity")
        setupUniform("tintColor")
    }

    override fun updateUniforms() {
        glUniform1i(getUniform("texture"), 0)
        glUniform2f(getUniform("texelSize"),
            1f / mc.displayWidth * renderScale,
            1f / mc.displayHeight * renderScale
        )
        glUniform1f(getUniform("radius"), blurRadius)
        glUniform1f(getUniform("alpha"), frostAlpha)
        glUniform1f(getUniform("intensity"), intensity)
        glUniform3f(getUniform("tintColor"),
            tintColor.red / 255f,
            tintColor.green / 255f,
            tintColor.blue / 255f
        )
    }

    override fun startShader() {
        super.startShader()
        isInUse = true
    }

    override fun stopShader() {
        super.stopShader()
        isInUse = false
    }

    override fun close() {
        if (isInUse)
            stopShader()
    }

    fun begin(enable: Boolean, intensity: Float = 0.3f, tintColor: Color = Color.WHITE, radius: Float = 2f) = apply {
        if (!enable) return@apply
        this.intensity = intensity
        this.tintColor = tintColor
        this.blurRadius = radius
        this.frostAlpha = tintColor.alpha / 255f
        startShader()
    }
}