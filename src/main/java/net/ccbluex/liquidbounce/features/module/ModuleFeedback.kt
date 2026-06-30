/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.FDPClient.isStarting
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.asResourceLocation
import net.ccbluex.liquidbounce.utils.client.playSound

/** UI and audio side effects for module lifecycle changes, kept out of the domain model. */
object ModuleFeedback : MinecraftInstance {

    fun toggled(moduleName: String, enabled: Boolean) {
        if (isStarting || ConfigSystem.isLoadingConfig) return

        mc.playSound("random.click".asResourceLocation())
        addNotification(
            Notification(
                moduleName,
                "${if (enabled) "Enabled" else "Disabled"} \u00A7r$moduleName",
                if (enabled) Type.SUCCESS else Type.ERROR,
                1000
            )
        )
    }

    fun settingsReset(moduleName: String) {
        val message = "Successfully reset all settings from $moduleName"
        addNotification(Notification(message, message, Type.SUCCESS, 1000))
    }
}
