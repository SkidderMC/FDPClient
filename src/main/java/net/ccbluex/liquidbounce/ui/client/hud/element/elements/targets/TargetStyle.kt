/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.config.Value
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow

abstract class TargetStyle(
    val name: String,
    val targetInstance: Targets,
    val shaderSupport: Boolean
) : MinecraftInstance {

    var easingHealth = 0F
    var health = 0F
    val easingHP = 0f

    val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))
    val decimalFormat2 = DecimalFormat("##0.0", DecimalFormatSymbols(Locale.ENGLISH))
    val decimalFormat3 = DecimalFormat("0.#", DecimalFormatSymbols(Locale.ENGLISH))

    companion object {
        private const val HEAD_SOURCE_SIZE = 8F
        private const val HEAD_TEXTURE_SIZE = 64F
    }

    fun getHealth(entity: EntityLivingBase?): Float = entity?.health ?: 0f

    abstract fun drawTarget(entity: EntityLivingBase)

    abstract fun getBorder(entity: EntityLivingBase?): Border?

    open fun updateAnim(targetHealth: Float) {
        val animationFactor = 2.0F.pow(10.0F - targetInstance.globalAnimSpeed)
        easingHealth += ((targetHealth - easingHealth) / animationFactor) * RenderUtils.deltaTime
    }

    fun fadeAlpha(alpha: Int): Int =
        alpha - (targetInstance.getFadeProgress() * alpha).toInt()

    /**
     * Get all values of element
     */
    open val values: List<Value<*>>
        get() = javaClass.declaredFields.filter { it.name.endsWith("\$delegate") }
            .map {
                it.isAccessible = true
                it.get(this)
            }.filterIsInstance<Value<*>>()

    fun getColor(color: Color): Color =
        ColorUtils.targetReAlpha(color, (color.alpha / 255F) * (1F - targetInstance.getFadeProgress()))

    fun getColor(color: Int): Color = getColor(Color(color))

    fun drawHead(
        skin: ResourceLocation,
        x: Int = 2,
        y: Int = 2,
        width: Int,
        height: Int,
        alpha: Float = 1F
    ) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0F, 1.0F, 1.0F, alpha)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(
            x, y,
            HEAD_SOURCE_SIZE, HEAD_SOURCE_SIZE,
            HEAD_SOURCE_SIZE.toInt(), HEAD_SOURCE_SIZE.toInt(),
            width, height,
            HEAD_TEXTURE_SIZE, HEAD_TEXTURE_SIZE
        )
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawHead(
        skin: ResourceLocation,
        x: Float,
        y: Float,
        scale: Float,
        width: Int,
        height: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float = 1F
    ) {
        glPushMatrix()
        glTranslatef(x, y, 0F)
        glScalef(scale, scale, scale)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(
            red.coerceIn(0F, 1F),
            green.coerceIn(0F, 1F),
            blue.coerceIn(0F, 1F),
            alpha.coerceIn(0F, 1F)
        )
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(
            0, 0,
            HEAD_SOURCE_SIZE, HEAD_SOURCE_SIZE,
            HEAD_SOURCE_SIZE.toInt(), HEAD_SOURCE_SIZE.toInt(),
            width, height,
            HEAD_TEXTURE_SIZE, HEAD_TEXTURE_SIZE
        )
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glPopMatrix()
        glColor4f(1F, 1F, 1F, 1F)
    }
}
