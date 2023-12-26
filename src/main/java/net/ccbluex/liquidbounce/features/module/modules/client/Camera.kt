/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.FogColorEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color


@ModuleInfo(name = "Camera", description = "Allows you to see through walls in third person view.", category = ModuleCategory.VISUAL)
class Camera : Module(){
    //
    var alpha2 = 0
    //
    val cameraClipValue = BoolValue("CameraClip", false)
    val noHurtCam = BoolValue("NoHurtCam", false)
    val antiBlindValue = BoolValue("AntiBlind", false)

    //AntiBlind
    val confusionEffectValue = BoolValue("Confusion", false).displayable { antiBlindValue.get() }
    val pumpkinEffect = BoolValue("Pumpkin", false).displayable  { antiBlindValue.get() }
    val fireEffect = BoolValue("Fire", false).displayable  { antiBlindValue.get() }
    val scoreBoard = BoolValue("Scoreboard", false).displayable  { antiBlindValue.get() }
    val bossHealth = BoolValue("Boss-Health", false).displayable  { antiBlindValue.get() }

    //NoFOV
    val noFov = BoolValue("NoFOV", false)
    val fovValue = FloatValue("FOV", 1f, 0f, 1.5f).displayable  { noFov.get() }

    //WorldColor
    val worldColorValue = BoolValue("WorldColor", false)
    val worldColorRValue = IntegerValue("WorldRed", 255, 0, 255) { worldColorValue.get() }
    val worldColorGValue = IntegerValue("WorldGreen", 255, 0, 255) { worldColorValue.get() }
    val worldColorBValue = IntegerValue("WorldBlue", 255, 0, 255) { worldColorValue.get() }

    //HitColor
    val hitColorValue = BoolValue("HitColor", false)
    val hitColorRValue = IntegerValue("HitRed", 255, 0, 255) { hitColorValue.get() }
    val hitColorGValue = IntegerValue("HitGreen", 255, 0, 255) { hitColorValue.get() }
    val hitColorBValue = IntegerValue("HitBlue", 255, 0, 255) { hitColorValue.get() }
    val hitColorAlphaValue = IntegerValue("HitAlpha", 255, 0, 255) { hitColorValue.get() }

    //CustomFog
    val customFog = BoolValue("CustomFog", false)
    val customFogDistance = FloatValue("FogDistance", 0.10f, 0.001f, 2.0f).displayable  { customFog.get() }
    val customFogRValue = IntegerValue("FogRed", 255, 0, 255) { customFog.get() }
    val customFogGValue = IntegerValue("FogGreen", 255, 0, 255) { customFog.get() }
    val customFogBValue = IntegerValue("FogBlue", 255, 0, 255) { customFog.get() }

    //CameraPosition
    val smoothCamera = BoolValue("Smooth", false)
    val cameraPositionValue = BoolValue("CameraPosition", false)
    val cameraPositionYawValue = FloatValue("Yaw", 10F, -50F, 50F).displayable  { cameraPositionValue.get() }
    val cameraPositionPitchValue = FloatValue("Pitch", 10F, -50F, 50F).displayable  { cameraPositionValue.get() }
    val cameraPositionFovValue = FloatValue("DistanceFov", 4F, 1F, 50F).displayable  { cameraPositionValue.get() }

    //FPSHurtCam
    private val fpsHurtCam = BoolValue("FPSHurtCam", false)
    val hurtcamColorRValue = IntegerValue("HurtColorRed", 255, 0, 255) { fpsHurtCam.get() }
    val hurtcamColorGValue = IntegerValue("HurtColorGreen", 255, 0, 255) { fpsHurtCam.get() }
    val hurtcamColorBValue = IntegerValue("HurtColorBlue", 255, 0, 255) { fpsHurtCam.get() }
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Slowly", "Fade"), "Custom").displayable { fpsHurtCam.get() }
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f).displayable  { fpsHurtCam.get() }
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f).displayable  { fpsHurtCam.get() }
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10).displayable  { fpsHurtCam.get() }

    private val motionBlur = BoolValue("Motionblur", false)
    private val blurAmount = IntegerValue("BlurAmount", 2, 1, 10) { motionBlur.get() }
    //FPSHurtCam
    @EventTarget
    private fun renderHud(event: Render2DEvent) {
        if (fpsHurtCam.get()) {
            val color = getColor( 0)
            run {
                val sr = ScaledResolution(mc)
                if (mc.thePlayer.hurtTime >= 1) {
                    if (alpha2 < 100) {
                        alpha2 += 5
                    }
                } else {
                    if (alpha2 > 0) {
                        alpha2 -= 5
                    }
                }
                this.drawGradientSidewaysV(
                    0.0,
                    0.0,
                    sr.scaledWidth.toDouble(),
                    25.0,
                    Color(color.red,color.green,color.blue,0).rgb,
                    Color(color.red,color.green,color.blue, alpha2).rgb
                )
                this.drawGradientSidewaysV(
                    0.0,
                    (sr.scaledHeight - 25).toDouble(),
                    sr.scaledWidth.toDouble(),
                    sr.scaledHeight.toDouble(),
                    Color(color.red,color.green,color.blue, alpha2).rgb,
                    Color(color.red,color.green,color.blue, 0).rgb
                )
            }
        }
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        try {

            if (mc.thePlayer != null) {
                if (motionBlur.get()) {
                    if (mc.entityRenderer.shaderGroup == null) mc.entityRenderer.loadShader(
                        ResourceLocation(
                            "minecraft",
                            "shaders/post/motion_blur.json"
                        )
                    )
                    val uniform = 1f - (blurAmount.get() / 10f).coerceAtMost(0.9f)
                    if (mc.entityRenderer.shaderGroup != null) {
                        mc.entityRenderer.shaderGroup.listShaders[0].shaderManager.getShaderUniform("Phosphor")
                            .set(uniform, 0f, 0f)
                    }
                } else {
                    if (mc.entityRenderer.isShaderActive) mc.entityRenderer.stopUseShader()
                }
            }

        } catch (a: Exception) {
            a.printStackTrace()
        }
    }

    fun drawGradientSidewaysV(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        if (fpsHurtCam.get()) {
            val f = (col1 shr 24 and 255).toFloat() / 255.0f
            val f1 = (col1 shr 16 and 255).toFloat() / 255.0f
            val f2 = (col1 shr 8 and 255).toFloat() / 255.0f
            val f3 = (col1 and 255).toFloat() / 255.0f
            val f4 = (col2 shr 24 and 255).toFloat() / 255.0f
            val f5 = (col2 shr 16 and 255).toFloat() / 255.0f
            val f6 = (col2 shr 8 and 255).toFloat() / 255.0f
            val f7 = (col2 and 255).toFloat() / 255.0f
            GL11.glEnable(3042)
            GL11.glDisable(3553)
            GL11.glBlendFunc(770, 771)
            GL11.glEnable(2848)
            GL11.glShadeModel(7425)
            GL11.glPushMatrix()
            GL11.glBegin(7)
            GL11.glColor4f(f1, f2, f3, f)
            GL11.glVertex2d(left, bottom)
            GL11.glVertex2d(right, bottom)
            GL11.glColor4f(f5, f6, f7, f4)
            GL11.glVertex2d(right, top)
            GL11.glVertex2d(left, top)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glEnable(3553)
            GL11.glDisable(3042)
            GL11.glDisable(2848)
            GL11.glShadeModel(7424)
            Gui.drawRect(0, 0, 0, 0, 0)
        }
    }
    fun getColor(index: Int): Color {
        var colorModeValue = colorModeValue.get()
        var colorRedValue = hurtcamColorRValue.get()
        var colorGreenValue = hurtcamColorGValue.get()
        var colorBlueValue = hurtcamColorBValue.get()
        var mixerSecondsValue = mixerSecondsValue.get()
        var saturationValue = saturationValue.get()
        var brightnessValue = brightnessValue.get()
        return when (colorModeValue) {
            "Custom" -> Color(colorRedValue, colorGreenValue, colorBlueValue)
            "Rainbow" -> Color(
                RenderUtils.getRainbowOpaque(
                    mixerSecondsValue,
                    saturationValue,
                    brightnessValue,
                    index
                )
            )

            "Slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index, saturationValue, brightnessValue)!!
            else -> ColorUtils.fade(Color(colorRedValue, colorGreenValue, colorBlueValue), index, 100)
        }
    }

    @EventTarget
    fun onFogColor(event: FogColorEvent) {
        event.setRed(customFogRValue.get())
        event.setGreen(customFogGValue.get())
        event.setBlue(customFogBValue.get())
    }
}
