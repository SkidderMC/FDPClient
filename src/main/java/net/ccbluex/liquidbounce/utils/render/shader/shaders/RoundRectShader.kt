/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render.shader.shaders

import net.ccbluex.liquidbounce.utils.render.shader.Shader
import org.lwjgl.opengl.GL20

object RoundRectShader : Shader("roundrect.frag") {

    override fun setupUniforms() {
        setupUniform("color")
        setupUniform("size")
        setupUniform("radius")
        setupUniform("smoothness")
    }

    override fun updateUniforms() {
        GL20.glUniform4f(getUniform("color"), 1f, 1f, 1f, 1f)
        GL20.glUniform2f(getUniform("size"), 100f, 100f)
        GL20.glUniform1f(getUniform("radius"), 1f)
        GL20.glUniform1f(getUniform("smoothness"), 0.5f)
    }
}