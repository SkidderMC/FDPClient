/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.TickEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect

@ModuleInfo(name = "VanillaTweaks", description = "Vanilla Utilities.", category = ModuleCategory.VISUAL, defaultOn = true)
class VanillaTweaks : Module() {

    // NoAchievements
    val noAchievements = BoolValue("NoAchievements", true)

    // CameraClip
    val cameraClipValue = BoolValue("CameraClip", true)

    //AntiBlind
    val antiBlindValue = BoolValue("AntiBlind", false)
    val confusionEffectValue = BoolValue("Confusion", false).displayable { antiBlindValue.get() }
    val pumpkinEffectValue = BoolValue("Pumpkin", true).displayable { antiBlindValue.get() }
    val fireEffectValue = FloatValue("FireAlpha", 0.3f, 0f, 1f).displayable { antiBlindValue.get() }
    private val fullBrightValue = BoolValue("FullBright", true).displayable { antiBlindValue.get() }
    private val fullBrightModeValue = ListValue("FullBrightMode", arrayOf("None", "Gamma", "NightVision"), "Gamma").displayable { fullBrightValue.get() }
    val bossHealthValue = BoolValue("Boss-Health", false).displayable { antiBlindValue.get() }

    //NoFOV
    val noFov = BoolValue("NoFOV", false)
    val fovValue = FloatValue("FOV", 1.2F, 0.8F, 1.5F).displayable  { noFov.get() }


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
            if(fullBrightValue.get() && antiBlindValue.get()) {
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
    }

    override fun handleEvents() = true
}
