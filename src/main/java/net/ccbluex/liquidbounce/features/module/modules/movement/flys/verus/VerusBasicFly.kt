package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class VerusBasicFly : FlyMode("VerusBasic") {
    private val verusMode = ListValue("${valuePrefix}Mode", arrayOf("Packet1", "Packet2"), "Packet1")
    private var jumped = false
    override fun onEnable() {
        jumped = false
        sendLegacy()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if(packet is C03PacketPlayer) {
            if(verusMode.get() === "Packet1") {
                packet.onGround = true
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if(verusMode.get() === "Packet1") {
            if(mc.thePlayer.motionY < 0.4) {
                mc.thePlayer.motionY = 0.0
            }
            mc.thePlayer.onGround = true
        }
    }
    override fun onMove(event: MoveEvent) {
        if(verusMode.get() === "Packet2") {
            val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
            PacketUtils.sendPacketNoEvent(
                C08PacketPlayerBlockPlacement(pos, 1,
                    ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
            )
            if(mc.thePlayer.onGround && !jumped) {
                mc.thePlayer.jump()
                event.y = 0.42
                jumped = true
            }else {
                event.y = 0.0
                MovementUtils.strafe(0.35f)
            }
        }
    }
}
