package net.skiddermc.fdpclient.features.module.modules.movement.flys.vanilla

import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer

class SmoothVanillaFly : FlyMode("SmoothVanilla") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 5f)
    private val kickBypassValue = BoolValue("${valuePrefix}KickBypass", false)

    private var packets = 0

    override fun onEnable() {
        packets = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.capabilities.isFlying = true
        mc.thePlayer.capabilities.flySpeed = speedValue.get() * 0.05f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packets++
            if (packets == 40 && kickBypassValue.get()) {
                MovementUtils.handleVanillaKickBypass()
                packets = 0
            }
        }
    }
}