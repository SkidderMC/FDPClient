package net.ccbluex.liquidbounce.features.module.modules.movement.flys.default

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer

class CreativeFly : FlyMode("Creative") {
    override fun onEnable() {
        mc.thePlayer.capabilities.isFlying = true
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.capabilities.isFlying = true
    }

    override fun onDisable() {
        mc.thePlayer.capabilities.isFlying = false
    }
}