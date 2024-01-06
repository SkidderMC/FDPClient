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
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation
@ModuleInfo(name = "VanillaTweaks", description = "Vanilla Utilities.", category = ModuleCategory.VISUAL)
class VanillaTweaks : Module() {

    // NoAchievements
    val noAchievements = BoolValue("NoAchievements", false)

    // CameraClip
    val cameraClipValue = BoolValue("CameraClip", false)

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
    val fovValue = FloatValue("FOV", 1.2F, 0.8F, 1.5F).displayable  { noFov.get() }

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

    private val motionBlur = BoolValue("MotionBlur", false)
    private val blurAmount = IntegerValue("BlurAmount", 0, 0, 10) { motionBlur.get() }

    private var prevGamma = -1f

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


    @EventTarget
    fun onFogColor(event: FogColorEvent) {
        event.setRed(customFogRValue.get())
        event.setGreen(customFogGValue.get())
        event.setBlue(customFogBValue.get())
    }

    override fun handleEvents() = true
}
