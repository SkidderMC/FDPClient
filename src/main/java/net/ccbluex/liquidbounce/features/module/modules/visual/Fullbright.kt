/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect

object Fullbright : Module("Fullbright", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {
    private val mode by choices("Mode", arrayOf("Gamma", "NightVision"), "Gamma")
    private val brightness by int("Brightness", 100, 0..100) { mode.equals("Gamma", ignoreCase = true) }
    private var prevGamma = -1f

    override fun onEnable() {
        prevGamma = mc.gameSettings.gammaSetting
    }

    override fun onDisable() {
        if (prevGamma == -1f)
            return

        mc.gameSettings.gammaSetting = prevGamma
        prevGamma = -1f

        mc.thePlayer?.removePotionEffectClient(Potion.nightVision.id)
    }

    val onUpdate = handler<UpdateEvent>(always = true) {
        if (state || XRay.handleEvents()) {
            when (mode.lowercase()) {
                "gamma" -> {
                    val target = brightness.toFloat()
                    when {
                        mc.gameSettings.gammaSetting < target -> mc.gameSettings.gammaSetting =
                            (mc.gameSettings.gammaSetting + 1f).coerceAtMost(target)

                        mc.gameSettings.gammaSetting > target -> mc.gameSettings.gammaSetting = target
                    }
                }

                "nightvision" -> mc.thePlayer?.addPotionEffect(PotionEffect(Potion.nightVision.id, 1337, 1))
            }
        } else if (prevGamma != -1f) {
            mc.gameSettings.gammaSetting = prevGamma
            prevGamma = -1f
        }
    }

    val onShutdown = handler<ClientShutdownEvent>(always = true) {
        onDisable()
    }
}