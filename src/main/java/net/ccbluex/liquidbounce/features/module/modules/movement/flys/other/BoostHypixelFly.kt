package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.direction
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class BoostHypixelFly : FlyMode("BoostHypixel") {
    private var boostState = 1
    private var moveSpeed = 0.0
    private var lastDistance = 0.0
    private var failedStart = false

    private val timer = TickTimer()

    override fun onEnable() {
        if (!mc.thePlayer.onGround) {
            return
        }

        repeat(10) { // Imagine flagging to NCP.
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        }

        var fallDistance = 3.0125 // add 0.0125 to ensure we get the fall dmg

        while (fallDistance > 0) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688698, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.7531999805212, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000013579, mc.thePlayer.posZ, false))
            fallDistance -= 0.7531999805212
        }
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))

        mc.thePlayer.jump()
        mc.thePlayer.posY += 0.42 // Visual

        boostState = 1
        moveSpeed = 0.1
        lastDistance = 0.0
        failedStart = false
    }

    override fun onMotion(event: MotionEvent) {
        if (event.eventState === EventState.PRE) {
            timer.update()
            if (timer.hasTimePassed(2)) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ)
                timer.reset()
            }
            if (!failedStart) mc.thePlayer.motionY = 0.0
        } else {
            val xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX
            val zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
            lastDistance = sqrt(xDist * xDist + zDist * zDist)
        }
    }

    override fun onMove(event: MoveEvent) {
        if (!isMoving()) {
            event.zeroXZ()
            return
        }

        if (failedStart) {
            return
        }

        val amplifier = 1 + if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) { 0.2 *
                (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1) } else { 0.0 }
        val baseSpeed = 0.29 * amplifier

        when (boostState) {
            1 -> {
                moveSpeed = (if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 1.56 else 2.034) * baseSpeed
                boostState = 2
            }
            2 -> {
                moveSpeed *= 2.16
                boostState = 3
            }
            3 -> {
                moveSpeed = lastDistance - (if (mc.thePlayer.ticksExisted % 2 == 0) 0.0103 else 0.0123) * (lastDistance - baseSpeed)
                boostState = 4
            }
            else -> {
                moveSpeed = lastDistance - lastDistance / 159.8
            }
        }

        moveSpeed = moveSpeed.coerceAtLeast(0.3)

        val yaw = direction
        event.x = -sin(yaw) * moveSpeed
        event.z = cos(yaw) * moveSpeed
        mc.thePlayer.motionX = event.x
        mc.thePlayer.motionZ = event.z
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= mc.thePlayer.posY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, mc.thePlayer.posY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }

    override fun onStep(event: StepEvent) {
        event.stepHeight = 0f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = false
        } else if (packet is S08PacketPlayerPosLook) {
            failedStart = true
            ClientUtils.displayChatMessage("§8[§c§lBoostHypixel-§a§lFly§8] §cSetback detected.")
        }
    }
}