package net.skiddermc.fdpclient.features.special

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.Listenable
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.event.WorldEvent
import net.skiddermc.fdpclient.features.module.EnumAutoDisableType
import net.skiddermc.fdpclient.features.module.EnumTriggerType
import net.skiddermc.fdpclient.ui.client.hud.element.elements.Notification
import net.skiddermc.fdpclient.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.server.S08PacketPlayerPosLook

object AutoDisable : Listenable {
    private const val name = "AutoDisable"

    @EventTarget
    fun onWorld(event: WorldEvent) {
        FDPClient.moduleManager.modules
            .filter { it.state && it.autoDisable == EnumAutoDisableType.RESPAWN && it.triggerType == EnumTriggerType.TOGGLE }
            .forEach { module ->
                module.state = false
                FDPClient.hud.addNotification(Notification(this.name, "Disabled ${module.name} due to respawn.", NotifyType.WARNING, 2000))
            }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S08PacketPlayerPosLook) {
            FDPClient.moduleManager.modules
                .filter { it.state && it.autoDisable == EnumAutoDisableType.FLAG && it.triggerType == EnumTriggerType.TOGGLE }
                .forEach { module ->
                    module.state = false
                    FDPClient.hud.addNotification(Notification(this.name, "Disabled ${module.name} due to flags.", NotifyType.WARNING, 2000))
                }
        }
    }

    fun handleGameEnd() {
        FDPClient.moduleManager.modules
            .filter { it.state && it.autoDisable == EnumAutoDisableType.GAME_END }
            .forEach { module ->
                module.state = false
                FDPClient.hud.addNotification(Notification(this.name, "Disabled ${module.name} due to game end.", NotifyType.WARNING, 2000))
            }
    }

    override fun handleEvents() = true
}
