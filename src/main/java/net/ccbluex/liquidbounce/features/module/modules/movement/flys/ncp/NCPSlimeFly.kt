package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.network.play.client.*
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

class NCPSlimeFly : FlyMode("NCPSlime") {

    private var placed = false
    private var onSlime = false
    private var startY = 0.0
    private var shouldFly = false
  
    @EventTarget
    override fun onEnable() {
        if (mc.thePlayer.onGround && mc.thePlayer.posY % 1 == 0.0) {
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + -0.5 , mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + -0.5 , mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY , mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY , mc.thePlayer.posZ, false))
            mc.thePlayer.jump()
            onSlime = false
            startY = mc.thePlayer.posY
            placed = false
            shouldFly = true
        } else {
            shouldFly = false
            FDPClient.hud.addNotification(Notification("Fly Failed", "To fly you need to be on a full block", NotifyType.WARNING, 8000))
            return
        }
            
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (!shouldFly) return

        if (!placed) {
            if (mc.thePlayer.posY - 1 > startY) {
                mc.thePlayer.motionY = -0.2
                mc.thePlayer.posY = startY + 0.1
                RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, -90f))
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                placed = true
            }
        } else {
           if (mc.thePlayer.onGround && !onSlime) {
                onSlime = true
                mc.thePlayer.motionY = 0.0
           }
        }
    }
    
    
    @EventTarget
    override fun onPacket(event: PacketEvent){
        if (!shouldFly) return
        if (mc.theWorld == null || mc.thePlayer == null) return

        val packet = event.packet
      
        if ( (packet is C00PacketKeepAlive || packet is C0FPacketConfirmTransaction) && ! (mc.thePlayer.ticksExisted % 3 == 0) ) {
            event.cancelEvent()
        }
    }
    
    
    @EventTarget
    override fun onBlockBB(event: BlockBBEvent) {
        if (!shouldFly) return
        if (event.block is BlockAir && event.y <= startY + 1 && onSlime) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
    
}
                


