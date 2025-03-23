/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.features.module.modules.client.Wings
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.io.APIConnectorUtils.callImage
import net.minecraft.client.Minecraft
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

class RenderWings : ModelBase() {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private var wingTexture: ResourceLocation? = null
    private val wing: ModelRenderer
    private val wingTip: ModelRenderer
    private val wingsModule = Wings

    init {
        updateWingTexture()

        this.setTextureOffset("wing.bone", 0, 0)
        this.setTextureOffset("wing.skin", -10, 8)
        this.setTextureOffset("wingtip.bone", 0, 5)
        this.setTextureOffset("wingtip.skin", -10, 18)

        this.wing = ModelRenderer(this, "wing").apply {
            setTextureSize(30, 30)
            setRotationPoint(-2.0f, 0.0f, 0.0f)
            addBox("bone", -10.0f, -1.0f, -1.0f, 10, 2, 2)
            addBox("skin", -10.0f, 0.0f, 0.5f, 10, 0, 10)
        }

        this.wingTip = ModelRenderer(this, "wingtip").apply {
            setTextureSize(30, 30)
            setRotationPoint(-10.0f, 0.0f, 0.0f)
            addBox("bone", -10.0f, -0.5f, -0.5f, 10, 1, 1)
            addBox("skin", -10.0f, 0.0f, 0.5f, 10, 0, 10)
        }

        wing.addChild(wingTip)
    }

    private fun updateWingTexture() {
        wingTexture = when (wingsModule.wingStyle) {
            "Dragon" -> callImage("dragonwings", "wings")
            "Simple" -> callImage("neonwings", "wings")
            else -> null
        }
    }

    fun renderWings(partialTicks: Float) {
        updateWingTexture()

        val scale = 1.0
        val rotation = interpolate(
            mc.thePlayer.prevRenderYawOffset,
            mc.thePlayer.renderYawOffset,
            partialTicks
        )

        GL11.glPushMatrix()
        GL11.glScaled(-scale, -scale, scale)
        GL11.glRotated(180.0 + rotation, 0.0, 1.0, 0.0)
        GL11.glTranslated(0.0, if (mc.thePlayer.isSneaking) -1.325 / scale else -1.45 / scale, 0.0)
        GL11.glTranslated(0.0, 0.0, 0.2 / scale)

        applyWingColor()

        mc.textureManager.bindTexture(wingTexture)

        for (side in 0..1) {
            GL11.glEnable(GL11.GL_CULL_FACE)
            applyWingRotations()
            wing.render(0.0625f)
            GL11.glScalef(-1.0f, 1.0f, 1.0f)

            if (side == 0) GL11.glCullFace(GL11.GL_FRONT)
        }

        resetRenderState()
        GL11.glPopMatrix()
    }

    private fun applyWingColor() {
        when (wingsModule.colorType) {
            "Theme" -> {
                val themeColor = ClientThemesUtils.getColor(0)
                GL11.glColor3f(
                    themeColor.red / 255f,
                    themeColor.green / 255f,
                    themeColor.blue / 255f
                )
            }
            "Custom" -> {
                val t = (System.currentTimeMillis() % 1000L) / 1000.0f
                val factor = ((sin(t * 2 * Math.PI) + 1) / 2).toFloat()
                val color = Color(wingsModule.color.rgb)

                GL11.glColor3f(color.red / 255f, color.green / 255f, color.blue / 255f)
            }
            else -> GL11.glColor3f(1f, 1f, 1f)
        }
    }

    private fun applyWingRotations() {
        val animationTime = (System.currentTimeMillis() % 1000L) / 1000.0 * Math.PI * 2.0
        wing.rotateAngleX = Math.toRadians(-80.0).toFloat() - cos(animationTime).toFloat() * 0.2f
        wing.rotateAngleY = Math.toRadians(20.0).toFloat() + sin(animationTime).toFloat() * 0.4f
        wing.rotateAngleZ = Math.toRadians(20.0).toFloat()
        wingTip.rotateAngleZ = -((sin(animationTime + 2.0) + 0.5) * 0.75).toFloat()
    }

    private fun resetRenderState() {
        GL11.glCullFace(GL11.GL_BACK)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glColor3f(1.0f, 1.0f, 1.0f)
    }

    private fun interpolate(yaw1: Float, yaw2: Float, percent: Float): Double {
        return ((yaw1 + (yaw2 - yaw1) * percent) % 360.0).let {
            if (it < 0.0) it + 360.0 else it
        }
    }
}