package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.rayTraceWithCustomRotation
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.item.ItemBlock
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import java.util.*
class NCPSlimeFly : FlyMode("NCPSlime") {
    private val timerBoostValue = BoolValue("${valuePrefix}DoTimer", true)
    private val swingModeValue = ListValue("${valuePrefix}SwingMode", arrayOf("Normal","Packet"), "Normal")
    private var stage = Stage.WAITING
    private var ticks = 0
    private var packets = 0
    private val timer = MSTimer()
    private var firstlaunch = true
    private var needreset = false
    private var vanillabypass = 0
    private var test = 1.0
    private val packetBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
    override fun onEnable() {
        test = 1.0
        needreset = false
        firstlaunch = true
        vanillabypass = 0
        packets = 0
        ticks = 0
        packetBuffer.clear()
        timer.reset()
        if(mc.thePlayer.onGround) {
            stage = Stage.WAITING
            mc.thePlayer.jump()
        } else {
            stage = Stage.INFFLYING
        }
    }

    override fun onWorld(event: WorldEvent) {
        packetBuffer.clear()
        timer.reset()
    }

    override fun onPacket(event: PacketEvent) {
        if(stage == Stage.WAITING) return

        val packet = event.packet

        if(packet is C03PacketPlayer) {
            packet.onGround = true
            packetBuffer.add(packet)
            event.cancelEvent()
        }
        if(packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) return

            event.cancelEvent()
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        for (packet in packetBuffer) {
            PacketUtils.sendPacketNoEvent(packet)
        }
        packetBuffer.clear()
    }

    override fun onUpdate(event: UpdateEvent) {
        if(timer.hasTimePassed((Math.random() * 1000).toLong())) {
            timer.reset()
            for (packet in packetBuffer) {
                PacketUtils.sendPacketNoEvent(packet)
            }
            packetBuffer.clear()
        }
        when (stage) {
            Stage.WAITING -> {
                if(mc.thePlayer.posY >= fly.launchY + 0.8) {
                    if(mc.thePlayer.onGround) {
                        RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, 90f))
                        val movingObjectPosition = mc.thePlayer.rayTraceWithCustomRotation(4.5, mc.thePlayer.rotationYaw, 90.0f)
                        if (movingObjectPosition?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
                        val blockPos = movingObjectPosition.blockPos
                        val enumFacing = movingObjectPosition.sideHit
                        if(mc.playerController.onPlayerDamageBlock(blockPos, enumFacing)) {
                            stage = Stage.FLYING
                        }
                        mc.thePlayer.motionY = 0.0
                    } else {
                        RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, 90f))
                        var slot = -1
                        for (j in 0..8) {
                            if (mc.thePlayer.inventory.getStackInSlot(j) != null && mc.thePlayer.inventory
                                    .getStackInSlot(j).item is ItemBlock
                            ) {
                                slot = PlayerUtils.findSlimeBlock()!!
                                break
                            }
                        }

                        if(slot == -1) {
                            fly.state = false
                            FDPClient.hud.addNotification(Notification("NCPSlimeFly", "U need a slime blocks to use this fly", NotifyType.ERROR, 1000))
                            return
                        }

                        val oldSlot = mc.thePlayer.inventory.currentItem
                        mc.thePlayer.inventory.currentItem = slot
                        val movingObjectPosition = mc.thePlayer.rayTraceWithCustomRotation(4.5, mc.thePlayer.rotationYaw, 90.0f)
                        if (movingObjectPosition?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
                        val blockPos = movingObjectPosition.blockPos
                        val enumFacing = movingObjectPosition.sideHit
                        val hitVec: Vec3 = movingObjectPosition.hitVec
                        if (mc.playerController.onPlayerRightClick(
                                mc.thePlayer,
                                mc.theWorld,
                                mc.thePlayer.heldItem,
                                blockPos,
                                enumFacing,
                                hitVec
                            )
                        ) {
                            when (swingModeValue.get().lowercase()) {
                                "normal" -> mc.thePlayer.swingItem()

                                "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
                            }
                        }
                        mc.thePlayer.inventory.currentItem = oldSlot
                    }
                }
            }
            Stage.FLYING, Stage.INFFLYING -> {
                if(timerBoostValue.get()) {
                    ticks++
                    when(ticks) {
                        in 1..10 -> mc.timer.timerSpeed = 2f

                        in 10..15 -> mc.timer.timerSpeed = 0.4f
                    }
                    if(ticks>=15) {
                        ticks = 0
                        mc.timer.timerSpeed = 0.6f
                    }
                } else {
                    mc.timer.timerSpeed = 1.0f
                }
            }
        }
    }
    override fun onBlockBB(event: BlockBBEvent) {
        when(stage) {
            Stage.WAITING -> {
                if (event.block is BlockAir && event.y <= fly.launchY + 100) {
                    event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
                }
            }
            Stage.FLYING -> {
                if (event.block is BlockAir && event.y <= fly.launchY + 1) {
                    event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
                }
            }
            Stage.INFFLYING -> {
                if (event.block is BlockAir && event.y <= mc.thePlayer.posY) {
                    event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
                }
            }
        }
    }

    override fun onJump(event: JumpEvent) {
        if(stage == Stage.WAITING) return

        event.cancelEvent()
    }

    override fun onStep(event: StepEvent) {
        if(stage == Stage.WAITING) return

        event.stepHeight = 0.0f
    }

    enum class Stage {
        WAITING,
        FLYING,
        INFFLYING
    }
}