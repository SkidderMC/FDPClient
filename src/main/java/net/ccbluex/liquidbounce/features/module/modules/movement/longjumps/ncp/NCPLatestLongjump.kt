package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.TransferUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class NCPLatestLongjump : LongJumpMode("NCPLatest") {
    private val ncpBoostValue = FloatValue("${valuePrefix}Boost", 10f, 1f, 10f)
    private val warn = BoolValue("${valuePrefix}Warn", true)
    
    private var canBoost = false
    private var cancelTeleport = false
  
    override fun onEnable() {
        canBoost = false
        cancelTeleport = false
        if (warn.get())
            ClientUtils.displayChatMessage("§8[§c§lNCPLatest§8] §aYou must be under a block to bypass")
    }
    
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            if (canBoost) {
                mc.thePlayer.jump()
                MovementUtils.strafe(ncpBoostValue.get())
            }
        } else {
            if (canBoost) {
                MovementUtils.strafe(ncpBoostValue.get() * 0.96f)
                canBoost = false
                longjump.state = false
            }
        }
    }
    
    override fun onPacket(event: PacketEvent) {
        val = event.packet
        if (packet is S08PacketPlayerPosLook) {
            if (cancelTeleport) {
                TransferUtils.silentConfirm = true
                TransferUtils.noMotionSet = true
            }
        }
    }
    
    override fun onAttemptJump() {
        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ, false)) 
        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false)) 
        
        cancelTeleport = true
        canBoost = true
    }
    
    override fun onAttemptDisable() {
        longjump.state = false
    }
}
