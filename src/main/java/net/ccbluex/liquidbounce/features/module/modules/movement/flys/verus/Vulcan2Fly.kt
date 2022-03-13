package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.stats.StatList
import net.minecraft.util.BlockPos

class Vulcan2Fly : FlyMode("Vulcan2") {

    private val timerValue = FloatValue("${valuePrefix}Speed", 1f, 0.1f, 3f)

    private var isSuccess = false
    private var vticks = 0
    private var doCancel = false
    private var stage = FlyStage.FLYING

    override fun onEnable() {
        vticks = 0
        doCancel = false
        if(mc.thePlayer.posY % 1 != 0.0) {
            fly.state = false
            ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §cPlease stand on a solid block to fly!")
            isSuccess = true
            return
        }
        stage = FlyStage.FLYING
        isSuccess = false
        ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §aPlease press Sneak before you land on ground!")
    }
    
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        if (!isSuccess) {
            ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §cFly attempt Failed...")
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        when(stage) {
            FlyStage.FLYING -> {
                isSuccess = false
                
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionZ = 0.0
                
                MovementUtils.strafe(timerValue.get())
                doCancel = true
                
                if(mc.gameSettings.keyBindSneak.pressed && mc.thePlayer.ticksExisted % 2 == 1) {
                    val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                    val underBlock2 = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, fixedY - 1, mc.thePlayer.posZ)) ?: return
                    if(underBlock2.isFullBlock) {
                        stage = FlyStage.WAIT_APPLY
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = 0.0
                        mc.thePlayer.jumpMovementFactor = 0.00f
                        doCancel = false
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY , mc.thePlayer.posZ, true))
                        doCancel = true
                    } else {
                        ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §cYou can only land on a solid block!")
                    }
                }
            }
            FlyStage.WAIT_APPLY -> {
                vticks++
                doCancel = false
                if(vticks == 80) {
                    ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §cSeems took a long time! Please turn off the Fly manually")
                }
                mc.timer.timerSpeed = 1f
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.jumpMovementFactor = 0.00f
                val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY - 10, mc.thePlayer.posZ, true))
                doCancel = true
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if(packet is C03PacketPlayer) {
            if(doCancel) {
                event.cancelEvent()
                doCancel = false
            }
            packet.onGround = true
        } else if(packet is S08PacketPlayerPosLook) {
            if (stage == FlyStage.WAIT_APPLY) {
                if(Math.sqrt((packet.x-mc.thePlayer.posX)*(packet.x-mc.thePlayer.posX)
                             +(packet.y-mc.thePlayer.posY)*(packet.y-mc.thePlayer.posY)
                             +(packet.z-mc.thePlayer.posZ)*(packet.z-mc.thePlayer.posZ)) < 1.0) {
                    isSuccess = true
                    fly.state = false
                    return
                }
            }
            event.cancelEvent()
        }
    }

    enum class FlyStage {
        FLYING,
        WAIT_APPLY
    }
}
