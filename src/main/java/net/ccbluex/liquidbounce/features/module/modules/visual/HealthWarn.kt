/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.event.handler

object HealthWarn: Module("HealthWarn", Category.VISUAL, gameDetecting = false) {

    private val healthValue by int("Health", 7, 1.. 20)

    private var canWarn = true

    override fun onEnable() {
        canWarn = true
    }

    override fun onDisable() {
        canWarn = true
    }


    val onUpdate = handler<UpdateEvent> {
        if (mc.thePlayer.health <= healthValue) {
            if (canWarn) {
                addNotification(Notification("HP Warning","YOU ARE AT LOW HP!", Type.ERROR, 3000))

                canWarn = false
            }
        } else {
            canWarn = true
        }
    }
}
