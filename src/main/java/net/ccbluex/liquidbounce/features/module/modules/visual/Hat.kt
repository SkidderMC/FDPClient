/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.choices
import net.ccbluex.liquidbounce.config.float
import net.ccbluex.liquidbounce.config.int
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

object Hat : Module("Hat", Category.VISUAL, hideModule = false, subjective = true) {

    private val heightValue by float("Height", 0.3f, 0.1f.. 0.7f)
    private val radiusValue by float("Radius", 0.7f, 0.3f.. 1.5f)
    private val yPosValue by float("YPos", 0f, -1f.. 1f)
    private val rotateSpeedValue by float("RotateSpeed", 2f, 0f.. 5f)
    private val drawThePlayerValue by boolean("DrawThePlayer", true)
    private val onlyThirdPersonValue by boolean("OnlyThirdPerson", true) { drawThePlayerValue }
    private val drawTargetsValue by boolean("DrawTargets", true)

    private val colorMode by choices("Color Mode", arrayOf("Custom", "Theme", "Rainbow"), "Theme")
    private val colorRedValue by int("Red", 255, 0..255) { colorMode == "Custom" }
    private val colorGreenValue by int("Green", 179, 0..255) { colorMode == "Custom" }
    private val colorBlueValue by int("Blue", 72, 0..255) { colorMode == "Custom" }
    private val rainbowSpeed by float("Rainbow Speed", 1.0f, 0.5f..5.0f) { colorMode == "Rainbow" }
    private val colorAlphaValue by int("Alpha", 255, 0..255)


    val onRender3D = handler<Render3DEvent> {
        if (drawThePlayerValue && !(onlyThirdPersonValue && mc.gameSettings.thirdPersonView == 0)) {
            drawChinaHatFor(mc.thePlayer)
        }
        if (drawTargetsValue) {
            mc.theWorld.loadedEntityList.forEach {
                if (EntityUtils.isSelected(it, true)) {
                    drawChinaHatFor(it as EntityLivingBase)
                }
            }
        }
    }

    private fun drawChinaHatFor(entity: EntityLivingBase) {
        glPushMatrix()
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glDisable(GL_CULL_FACE)

        val color = when (colorMode) {
            "Custom" -> Color(colorRedValue, colorGreenValue, colorBlueValue, colorAlphaValue)
            "Theme" -> ClientThemesUtils.getColorWithAlpha(1, colorAlphaValue)
            "Rainbow" -> ColorUtils.rainbow(rainbowSpeed)
            else -> Color(255, 255, 255, colorAlphaValue)
        }

        glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        glTranslated(
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + entity.height + yPosValue,
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        glRotatef((entity.ticksExisted + mc.timer.renderPartialTicks) * rotateSpeedValue, 0f, 1f, 0f)

        glBegin(GL_TRIANGLE_FAN)
        glVertex3d(0.0, heightValue.toDouble(), 0.0)
        val radius = radiusValue.toDouble()
        for (i in 0..360 step 5) {
            glVertex3d(
                cos(i.toDouble() * Math.PI / 180.0) * radius,
                0.0,
                sin(i.toDouble() * Math.PI / 180.0) * radius
            )
        }
        glVertex3d(0.0, heightValue.toDouble(), 0.0)
        glEnd()

        glEnable(GL_CULL_FACE)
        GlStateManager.resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glPopMatrix()
    }
}
