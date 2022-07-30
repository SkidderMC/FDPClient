/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.misc

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.ui.client.hud.element.elements.Notification
import net.skiddermc.fdpclient.ui.client.hud.element.elements.NotifyType
import net.skiddermc.fdpclient.value.IntegerValue

@ModuleInfo(name = "HealthWarn", category = ModuleCategory.MISC, array = false, defaultOn = true)
class HealthWarn : Module() {

    private val healthValue = IntegerValue("Health", 7, 1, 20)

    private var canWarn = true

    override fun onEnable() {
        canWarn = true
    }

    override fun onDisable() {
        canWarn = true
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.health <= healthValue.get()) {
            if (canWarn) {
                FDPClient.hud.addNotification(
                    Notification("HP Warning", "YOU ARE AT LOW HP!", NotifyType.ERROR, 3000))
                canWarn = false
            }
        } else {
            canWarn = true
        }
    }
}
