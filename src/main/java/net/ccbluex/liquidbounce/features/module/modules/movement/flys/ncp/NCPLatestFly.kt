/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack

class NCPLatestFly : FlyMode("NCPLatest") {

    private val verusBypass = BoolValue("VerusBypass", true)

    private var started = false 
    private var cancelTp = true

    private var wasOnGround = false
  
    override fun onEnable() {
        cancelTp = true
        if (verusBypass.get()) { 
            val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
            mc.netHandler.addToSendQueue(
                C08PacketPlayerBlockPlacement(pos, 1,
                    ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
            )
        }
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.motionY, mc.thePlayer.motionZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.motionY - 0.1, mc.thePlayer.motionZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.motionY, mc.thePlayer.motionZ, false))

        started = true
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onUpdate(event: UpdateEvent) {
        if (!started) return
        mc.timer.timerSpeed = 0.4f
        MovementUtils.strafe()
      
        if (mc.thePlayer.onGround) {
            wasOnGround = true
            mc.thePlayer.motionY = 0.42
            MovementUtils.strafe(10f)
        } else if (wasOnGround) {
            MovementUtils.strafe(9.6f)
            wasOnGround = false
        }
    }

    override fun onPacket(event: PacketEvent) {
       if(event.packet is S08PacketPlayerPosLook && cancelTp) {
           cancelTp = false
           event.cancelEvent()
       }
    }
}
