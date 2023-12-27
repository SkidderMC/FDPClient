/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
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
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "VanillaTweaks", description = "Vanilla Utilities.", category = ModuleCategory.VISUAL)
object VanillaTweaks : Module() {

    private var alpha2 = 0

    // NoAchievements
    val noAchievements = BoolValue("NoAchievements", false)

    // CameraClip
    val cameraClipValue = BoolValue("CameraClip", false)

    // HurtCam
    private val hurtCam = BoolValue("HurtCam", false)
    val modeValue = ListValue("Mode", arrayOf("Vanilla", "Cancel", "FPS"), "Vanilla").displayable { hurtCam.get() }
    private val colorRedValue = IntegerValue("R", 255, 0, 255).displayable { modeValue.equals("FPS") }
    private val colorGreenValue = IntegerValue("G", 0, 0, 255).displayable { modeValue.equals("FPS") }
    private val colorBlueValue = IntegerValue("B", 0, 0, 255).displayable { modeValue.equals("FPS") }
    private val colorRainbow = BoolValue("Rainbow", false).displayable { modeValue.equals("FPS") }
    private val timeValue = IntegerValue("FPSTime", 1000, 0, 1500).displayable { modeValue.equals("FPS") }
    private val fpsHeightValue = IntegerValue("FPSHeight", 25, 10, 50).displayable { modeValue.equals("FPS") }

    //AntiBlind
    private val antiBlindValue = BoolValue("AntiBlind", false)
    val confusionEffectValue = BoolValue("Confusion", false).displayable { antiBlindValue.get() }
    val pumpkinEffectValue = BoolValue("Pumpkin", true).displayable { antiBlindValue.get() }
    val fireEffectValue = FloatValue("FireAlpha", 0.3f, 0f, 1f).displayable { antiBlindValue.get() }
    private val fullBrightValue = BoolValue("FullBright", true).displayable { antiBlindValue.get() }
    private val fullBrightModeValue = ListValue("FullBrightMode", arrayOf("None", "Gamma", "NightVision"), "Gamma").displayable { fullBrightValue.get() }
    val bossHealthValue = BoolValue("Boss-Health", true).displayable { antiBlindValue.get() }

    //NoFOV
    private val noFov = BoolValue("NoFOV", false)
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
    private val customFogRValue = IntegerValue("FogRed", 255, 0, 255) { customFog.get() }
    private val customFogGValue = IntegerValue("FogGreen", 255, 0, 255) { customFog.get() }
    private val customFogBValue = IntegerValue("FogBlue", 255, 0, 255) { customFog.get() }

    //FPSHurtCam
    private val fpsHurtCam = BoolValue("FPSHurtCam", false)
    private val hurtcamColorRValue = IntegerValue("HurtColorRed", 255, 0, 255) { fpsHurtCam.get() }
    private val hurtcamColorGValue = IntegerValue("HurtColorGreen", 255, 0, 255) { fpsHurtCam.get() }
    private val hurtcamColorBValue = IntegerValue("HurtColorBlue", 255, 0, 255) { fpsHurtCam.get() }
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Slowly", "Fade"), "Custom").displayable { fpsHurtCam.get() }
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f).displayable  { fpsHurtCam.get() }
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f).displayable  { fpsHurtCam.get() }
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10).displayable  { fpsHurtCam.get() }

    private val motionBlur = BoolValue("Motionblur", false)
    private val blurAmount = IntegerValue("BlurAmount", 1, 1, 10) { motionBlur.get() }

    private var prevGamma = -1f
    private var hurt = 0L

    override fun onEnable() {
        prevGamma = mc.gameSettings.gammaSetting
    }

    override fun onDisable() {
        if (prevGamma == -1f) return
        mc.gameSettings.gammaSetting = prevGamma
        prevGamma = -1f
        if (mc.thePlayer != null) mc.thePlayer.removePotionEffectClient(Potion.nightVision.id)
    }

    @EventTarget(ignoreCondition = true)
    fun onUpdate(event: UpdateEvent) {
        if (state || FDPClient.moduleManager[XRay::class.java]!!.state) {
            if(fullBrightValue.get()) {
                when (fullBrightModeValue.get().lowercase()) {
                    "gamma" -> if (mc.gameSettings.gammaSetting <= 100f) mc.gameSettings.gammaSetting++
                    "nightvision" -> mc.thePlayer.addPotionEffect(PotionEffect(Potion.nightVision.id, 1337, 1))
                }
            }
        } else if (prevGamma != -1f) {
            mc.gameSettings.gammaSetting = prevGamma
            prevGamma = -1f
        }
    }

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
                drawGradientSidewaysV(
                    0.0,
                    0.0,
                    sr.scaledWidth.toDouble(),
                    25.0,
                    Color(color.red,color.green,color.blue,0).rgb,
                    Color(color.red,color.green,color.blue, alpha2).rgb
                )
                drawGradientSidewaysV(
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

        mc.guiAchievement.clearAchievements()
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

    @JvmStatic
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
        val colorModeValue = colorModeValue.get()
        val colorRedValue = hurtcamColorRValue.get()
        val colorGreenValue = hurtcamColorGValue.get()
        val colorBlueValue = hurtcamColorBValue.get()
        val mixerSecondsValue = mixerSecondsValue.get()
        val saturationValue = saturationValue.get()
        val brightnessValue = brightnessValue.get()
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

            "Slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), index, saturationValue, brightnessValue)
            else -> ColorUtils.fade(Color(colorRedValue, colorGreenValue, colorBlueValue), index, 100)
        }
    }

    @EventTarget
    fun onFogColor(event: FogColorEvent) {
        event.setRed(customFogRValue.get())
        event.setGreen(customFogGValue.get())
        event.setBlue(customFogBValue.get())
    }

    @EventTarget(ignoreCondition = true)
    fun onShutdown(event: ClientShutdownEvent) {
        onDisable()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (modeValue.get().lowercase()) {
            "fps" -> {
                if (packet is S19PacketEntityStatus) {
                    if (packet.opCode.toInt() == 2 && mc.thePlayer.equals(packet.getEntity(mc.theWorld))) {
                        hurt = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    @EventTarget
    fun onRender2d(event: Render2DEvent) {
        if (hurt == 0L) return

        val passedTime = System.currentTimeMillis() - hurt
        if (passedTime > timeValue.get()) {
            hurt = 0L
            return
        }

        val color = getHurtCamColor(
            (((timeValue.get() - passedTime) / timeValue.get().toFloat()) * 255).toInt()
        )
        val color1 = getHurtCamColor(0)
        val width = event.scaledResolution.scaledWidth_double
        val height = event.scaledResolution.scaledHeight_double

        RenderUtils.drawGradientSidewaysV(0.0, 0.0, width, fpsHeightValue.get().toDouble(), color.rgb, color1.rgb)
        RenderUtils.drawGradientSidewaysV(0.0, height - fpsHeightValue.get(), width, height, color1.rgb, color.rgb)
    }

    private fun getHurtCamColor(alpha: Int): Color {
        return if (colorRainbow.get()) ColorUtils.reAlpha(ColorUtils.rainbow(), alpha) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), alpha)
    }

    override fun handleEvents() = true
}
