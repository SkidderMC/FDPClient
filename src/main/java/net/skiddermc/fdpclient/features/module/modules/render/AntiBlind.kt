/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.render

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.ClientShutdownEvent
import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.FloatValue
import net.skiddermc.fdpclient.value.ListValue
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect

@ModuleInfo(name = "AntiBlind", category = ModuleCategory.RENDER)
class AntiBlind : Module() {
    val confusionEffectValue = BoolValue("Confusion", true)
    val pumpkinEffectValue = BoolValue("Pumpkin", true)
    val fireEffectValue = FloatValue("FireAlpha", 0.3f, 0f, 1f)
    private val brightValue = ListValue("Bright", arrayOf("None", "Gamma", "NightVision"), "Gamma")
    val bossHealth = BoolValue("Boss-Health", true)

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
            when (brightValue.get().lowercase()) {
                "gamma" -> if (mc.gameSettings.gammaSetting <= 100f) mc.gameSettings.gammaSetting++
                "nightvision" -> mc.thePlayer.addPotionEffect(PotionEffect(Potion.nightVision.id, 1337, 1))
            }
        } else if (prevGamma != -1f) {
            mc.gameSettings.gammaSetting = prevGamma
            prevGamma = -1f
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onShutdown(event: ClientShutdownEvent) {
        onDisable()
    }
}