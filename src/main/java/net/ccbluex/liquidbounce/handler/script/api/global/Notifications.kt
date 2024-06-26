/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.script.api.global

import me.zywl.fdpclient.FDPClient
import net.ccbluex.liquidbounce.ui.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.hud.element.elements.NotifyType

object Notifications {

    @Suppress("unused")
    @JvmStatic
    fun create(name: String?, content: String?, notify: String?, time: Int?) {
        var notifytype = NotifyType.WARNING
        when(notify?.lowercase()) {
            "success" -> notifytype = NotifyType.SUCCESS

            "info" -> notifytype = NotifyType.INFO

            "error" -> notifytype = NotifyType.ERROR

            "warning" -> notifytype = NotifyType.WARNING
        }
        FDPClient.hud.addNotification(Notification(name ?: "ScriptAPI", content ?: "Notification register failed", notifytype, time ?: 1000))
    }
}