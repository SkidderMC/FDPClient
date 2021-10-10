package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacketNoEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB


class AAC520VanillaFly : FlyMode("AAC5.2.0-Vanilla") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 5f)
    private val viewValue = BoolValue("${valuePrefix}BetterView", false)
    private val smoothValue = BoolValue("${valuePrefix}Smooth", false)
    private val purseValue = IntegerValue("${valuePrefix}Purse", 7, 3, 20)
    private val packetModeValue = ListValue("${valuePrefix}PacketMode", arrayOf("Old", "Rise"), "Old")
    private val useC04Value = BoolValue("${valuePrefix}UseC04", false)

    private val packets = mutableListOf<C03PacketPlayer>()
    private val timer=MSTimer()
    private var clonedPlayer: EntityOtherPlayerMP? = null
    private var nextFlag = false
    private var flyClip = false
    private var flyStart = false

    override fun onEnable() {
        if(mc.isSingleplayer) {
            LiquidBounce.hud.addNotification(Notification("Fly", "Use AAC5.2.0 Fly will crash single player", NotifyType.ERROR, 2000, 500))
            fly.state=false
            return
        }

        packets.clear()
        nextFlag=false
        flyClip=false
        flyStart=false
        timer.reset()

        if(smoothValue.get()){
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ)
        }else if (viewValue.get()) {
            clonedPlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
            clonedPlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
            clonedPlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
            mc.theWorld.addEntityToWorld((-(Math.random() * 10000)).toInt(), clonedPlayer)
            clonedPlayer!!.isInvisible = true
            mc.renderViewEntity = clonedPlayer
        }
    }

    override fun onDisable() {
        sendPackets()
        packets.clear()
        mc.thePlayer.noClip = false

        if(viewValue.get()){
            mc.renderViewEntity = mc.thePlayer
            mc.theWorld.removeEntityFromWorld(clonedPlayer?.entityId ?: return)
            clonedPlayer=null
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.noClip=!MovementUtils.isMoving()
        if(smoothValue.get()){
            if(!timer.hasTimePassed(1000) || !flyStart) {
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.jumpMovementFactor = 0.00f
                mc.timer.timerSpeed = 0.32F
                return
            }else {
                if(!flyClip) {
                    mc.timer.timerSpeed = 0.19F
                }else{
                    flyClip=false
                    mc.timer.timerSpeed = 1.2F
                }
            }
        }

        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        if (mc.gameSettings.keyBindJump.isKeyDown)
            mc.thePlayer.motionY += speedValue.get() * 0.5
        if (mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY -= speedValue.get() * 0.5
        MovementUtils.strafe(speedValue.get())

        clonedPlayer ?: return

        clonedPlayer!!.inventory.copyInventory(mc.thePlayer.inventory)
        clonedPlayer!!.health = mc.thePlayer.health
        clonedPlayer!!.rotationYaw=mc.thePlayer.rotationYaw
        clonedPlayer!!.rotationPitch=mc.thePlayer.rotationPitch
    }


    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            flyStart=true
            if(timer.hasTimePassed(2000)) {
                flyClip=true
                mc.timer.timerSpeed = 1.3F
            }
            nextFlag=true

            clonedPlayer?.setPosition(packet.x, packet.y, packet.z)
        }else if(packet is C03PacketPlayer){
            val f = mc.thePlayer.width / 2.0
            // need to no collide else will flag
            if (!mc.theWorld.checkBlockCollision(AxisAlignedBB(packet.x - f, packet.y, packet.z - f, packet.x + f, packet.y + mc.thePlayer.height, packet.z + f))) {
                packets.add(packet)
                nextFlag = false
                event.cancelEvent()
                if (!(smoothValue.get() && !timer.hasTimePassed(1000)) && packets.size > purseValue.get()) {
                    sendPackets()
                }
            }
        }
    }

    private fun sendPackets() {
        var yaw = mc.thePlayer.rotationYaw
        var pitch = mc.thePlayer.rotationPitch
        if (packetModeValue.get() == "Old") {
            for (packet in packets) {
                if (packet.isMoving) {
                    sendPacketNoEvent(packet)
                    if (packet.getRotating()) {
                        yaw = packet.yaw
                        pitch = packet.pitch
                    }
                    if (useC04Value.get()) {
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, 1e+308, packet.z, true))
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, 1e+308, packet.z, yaw, pitch, true))
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }
                }
            }
        } else {
            for (packet in packets) {
                if (packet.isMoving) {
                    sendPacketNoEvent(packet)
                    if (packet.getRotating()) {
                        yaw = packet.yaw
                        pitch = packet.pitch
                    }
                    if (useC04Value.get()) {
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, -1e+159, packet.z + 10, true))
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, -1e+159, packet.z + 10, yaw, pitch, true))
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }
                }
            }
        }
        packets.clear()
    }
}