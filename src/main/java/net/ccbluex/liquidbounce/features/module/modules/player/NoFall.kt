/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.VecRotation
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemBucket
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.util.*
import kotlin.math.sqrt
import kotlin.math.ceil

@ModuleInfo(name = "NoFall", category = ModuleCategory.PLAYER)
class NoFall : Module() {
    val modeValue = ListValue("Mode", arrayOf("SpoofGround", "AlwaysSpoofGround", "NoGround", "Packet", "Packet1", "MLG", "OldAAC", "LAAC", "AAC3.3.11", "AAC3.3.15", "AACv4", "AAC5.0.14", "Spartan", "CubeCraft", "Hypixel","HypSpoof","Phase", "Verus", "Damage"), "SpoofGround")
    private val phaseOffsetValue = IntegerValue("PhaseOffset", 1, 0, 5).displayable { modeValue.equals("Phase") }
    private val minFallDistance = FloatValue("MinMLGHeight", 5f, 2f, 50f).displayable { modeValue.equals("MLG") }

    private var oldaacState = 0
    private var jumped = false
    private val spartanTimer = TickTimer()
    private var aac4Fakelag = false
    private var packetModify = false
    private var aac5doFlag = false
    private var aac5Check = false
    private var aac5Timer = 0
    private val aac4Packets = ArrayList<C03PacketPlayer>()
    private var needSpoof = false
    private var packet1Count = 0
    private val mlgTimer = TickTimer()
    private var currentMlgRotation: VecRotation? = null
    private var currentMlgItemIndex = 0
    private var currentMlgBlock: BlockPos? = null

    override fun onEnable() {
        aac4Fakelag = false
        aac5Check = false
        packetModify = false
        aac4Packets.clear()
        needSpoof = false
        aac5doFlag = false
        aac5Timer = 0
        packet1Count = 0
        oldaacState = 0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround)
            jumped = false

        if (mc.thePlayer.motionY > 0)
            jumped = true

        if (!state || LiquidBounce.moduleManager.getModule(FreeCam::class.java).state)
            return

        if (mc.thePlayer.isSpectator || mc.thePlayer.capabilities.allowFlying || mc.thePlayer.capabilities.disableDamage)
            return

        if (BlockUtils.collideBlock(mc.thePlayer.entityBoundingBox) { it is BlockLiquid } || BlockUtils.collideBlock(AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY, mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.01, mc.thePlayer.entityBoundingBox.minZ)) { it is BlockLiquid })
            return

        when (modeValue.get().lowercase()) {
            "packet" -> {
                if (mc.thePlayer.fallDistance > 2f) mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            }
            "cubecraft" -> {
                if (mc.thePlayer.fallDistance > 2f) {
                    mc.thePlayer.onGround = false
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                }
            }
            "oldaac" -> {
                if (mc.thePlayer.fallDistance > 2f) {
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                    oldaacState = 2
                } else if (oldaacState == 2 && mc.thePlayer.fallDistance < 2) {
                    mc.thePlayer.motionY = 0.1
                    oldaacState = 3
                    return
                }
                when (oldaacState) {
                    3 -> {
                        mc.thePlayer.motionY = 0.1
                        oldaacState = 4
                    }
                    4 -> {
                        mc.thePlayer.motionY = 0.1
                        oldaacState = 5
                    }
                    5 -> {
                        mc.thePlayer.motionY = 0.1
                        oldaacState = 1
                    }
                }
            }
            "laac" -> {
                if (!jumped && mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb)
                    mc.thePlayer.motionY = -6.0
            }
            "aac3.3.11" -> {
                if (mc.thePlayer.fallDistance > 2) {
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionX = mc.thePlayer.motionZ
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 10E-4, mc.thePlayer.posZ, mc.thePlayer.onGround))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                }
            }
            "aac3.3.15" -> {
                if (mc.thePlayer.fallDistance > 2) {
                    if (!mc.isIntegratedServerRunning)
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, Double.NaN, mc.thePlayer.posZ, false))
                    mc.thePlayer.fallDistance = -9999f
                }
            }
            "spartan" -> {
                spartanTimer.update()
                if (mc.thePlayer.fallDistance > 1.5 && spartanTimer.hasTimePassed(10)) {
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 10, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 10, mc.thePlayer.posZ, true))
                    spartanTimer.reset()
                }
            }
            "aac5.0.14" -> {
                var offsetYs = 0.0
                aac5Check = false
                while (mc.thePlayer.motionY - 1.5 < offsetYs) {
                    val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + offsetYs, mc.thePlayer.posZ)
                    val block = BlockUtils.getBlock(blockPos)
                    val axisAlignedBB = block!!.getCollisionBoundingBox(mc.theWorld, blockPos, BlockUtils.getState(blockPos))
                    if (axisAlignedBB != null) {
                        offsetYs = -999.9
                        aac5Check = true
                    }
                    offsetYs -= 0.5
                }
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.fallDistance = -2f
                    aac5Check = false
                }
                if (aac5Timer > 0) {
                    aac5Timer -= 1
                }
                if (aac5Check && mc.thePlayer.fallDistance > 2.5 && !mc.thePlayer.onGround) {
                    aac5doFlag = true
                    aac5Timer = 19
                } else {
                    if (aac5Timer < 2) aac5doFlag = false
                }
                if (aac5doFlag) {
                    if (mc.thePlayer.onGround) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ, true))
                    } else {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, true))
                    }
                }
            }
            "phase" -> {
                if (mc.thePlayer.fallDistance > 3 + phaseOffsetValue.get()) {
                    val fallPos = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ, mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)
                        .findCollision(5) ?: return
                    if (fallPos.y - mc.thePlayer.motionY / 20.0 < mc.thePlayer.posY) {
                        mc.timer.timerSpeed = 0.05f
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(fallPos.x.toDouble(), fallPos.y.toDouble(), fallPos.z.toDouble(), true))
                                mc.timer.timerSpeed = 1f
                            }
                        }, 100)
                    }
                }
            }
            "verus" -> {
                if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3) {
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.fallDistance = 0.0f
                    mc.thePlayer.motionX *= 0.6
                    mc.thePlayer.motionZ *= 0.6
                    needSpoof = true
                }

                if (mc.thePlayer.fallDistance.toInt() / 3 > packet1Count) {
                    packet1Count = mc.thePlayer.fallDistance.toInt() / 3
                    packetModify = true
                }
                if (mc.thePlayer.onGround) {
                    packet1Count = 0
                }
            }
            "packet1" -> {
                if (mc.thePlayer.fallDistance.toInt() / 3 > packet1Count) {
                    packet1Count = mc.thePlayer.fallDistance.toInt() / 3
                    packetModify = true
                }
                if (mc.thePlayer.onGround) {
                    packet1Count = 0
                }
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (modeValue.equals("AACv4") && event.eventState === EventState.PRE) {
            if (!inVoid()) {
                if (aac4Fakelag) {
                    aac4Fakelag = false
                    if (aac4Packets.size > 0) {
                        for (packet in aac4Packets) {
                            mc.thePlayer.sendQueue.addToSendQueue(packet)
                        }
                        aac4Packets.clear()
                    }
                }
                return
            }
            if (mc.thePlayer.onGround && aac4Fakelag) {
                aac4Fakelag = false
                if (aac4Packets.size > 0) {
                    for (packet in aac4Packets) {
                        mc.thePlayer.sendQueue.addToSendQueue(packet)
                    }
                    aac4Packets.clear()
                }
                return
            }
            if (mc.thePlayer.fallDistance > 2.5 && aac4Fakelag) {
                packetModify = true
                mc.thePlayer.fallDistance = 0f
            }
            if (inAir(4.0, 1.0)) {
                return
            }
            if (!aac4Fakelag) {
                aac4Fakelag = true
            }
        }else if (modeValue.equals("MLG")) {
            if (event.eventState == EventState.PRE) {
                currentMlgRotation = null

                mlgTimer.update()

                if (!mlgTimer.hasTimePassed(10))
                    return

                if (mc.thePlayer.fallDistance > minFallDistance.get()) {
                    val fallingPlayer = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ, mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)

                    val maxDist = mc.playerController.blockReachDistance + 1.5

                    val collision = fallingPlayer.findCollision(ceil(1.0 / mc.thePlayer.motionY * -maxDist).toInt()) ?: return

                    var ok = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ).distanceTo(Vec3(collision).addVector(0.5, 0.5, 0.5)) < mc.playerController.blockReachDistance + sqrt(0.75)

                    if (mc.thePlayer.motionY < collision.y + 1 - mc.thePlayer.posY) {
                        ok = true
                    }

                    if (!ok)
                        return

                    var index = -1

                    for (i in 36..44) {
                        val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack

                        if (itemStack != null && (itemStack.item == Items.water_bucket || itemStack.item is ItemBlock && (itemStack.item as ItemBlock).block == Blocks.web)) {
                            index = i - 36

                            if (mc.thePlayer.inventory.currentItem == index)
                                break
                        }
                    }
                    if (index == -1)
                        return

                    currentMlgItemIndex = index
                    currentMlgBlock = collision

                    if (mc.thePlayer.inventory.currentItem != index) {
                        mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(index))
                    }

                    currentMlgRotation = RotationUtils.faceBlock(collision)
                    currentMlgRotation!!.rotation.toPlayer(mc.thePlayer)
                }
            } else if (currentMlgRotation != null) {
                val stack = mc.thePlayer.inventory.getStackInSlot(currentMlgItemIndex + 36)

                if (stack.item is ItemBucket) {
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, stack)
                } else {
                    val dirVec = EnumFacing.UP.directionVec

                    if (mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, stack)) {
                        mlgTimer.reset()
                    }
                }
                if (mc.thePlayer.inventory.currentItem != currentMlgItemIndex)
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val mode = modeValue.get()
        if (event.packet is C03PacketPlayer) {
            val packet = event.packet
            if (mode.equals("SpoofGround", ignoreCase = true) && mc.thePlayer.fallDistance > 2.5){
                packet.onGround = true
            }else if (mode.equals("AlwaysSpoofGround", ignoreCase = true)){
                packet.onGround = true
            }else if (mode.equals("NoGround", ignoreCase = true)){
                packet.onGround = false
            }else if (mode.equals("Hypixel", ignoreCase = true) && mc.thePlayer != null && mc.thePlayer.fallDistance > 1.5){
                packet.onGround = mc.thePlayer.ticksExisted % 2 == 0
            }else if (mode.equals("HypSpoof", ignoreCase = true)) {
                PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
            }else if (mode.equals("AACv4", ignoreCase = true) && aac4Fakelag) {
                event.cancelEvent()
                if (packetModify) {
                    packet.onGround = true
                    packetModify = false
                }
                aac4Packets.add(packet)
            }else if (mode.equals("Verus", ignoreCase = true) && needSpoof) {
                packet.onGround = true
                needSpoof = false
            } else if (mode.equals("Damage", ignoreCase = true) && mc.thePlayer != null && mc.thePlayer.fallDistance > 3.5) {
                packet.onGround = true
            }else if (mode.equals("Packet1", ignoreCase = true) && packetModify) {
                packet.onGround = true
                packetModify = false
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (BlockUtils.collideBlock(mc.thePlayer.entityBoundingBox) { it is BlockLiquid } || BlockUtils.collideBlock(AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY, mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.01, mc.thePlayer.entityBoundingBox.minZ)) { it is BlockLiquid })
            return
        if (modeValue.equals("laac")) {
            if (!jumped && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb && mc.thePlayer.motionY < 0.0) {
                event.x = 0.0
                event.z = 0.0
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        jumped = true
    }

    private fun inVoid(): Boolean {
        if (mc.thePlayer.posY < 0) {
            return false
        }
        var off = 0
        while (off < mc.thePlayer.posY + 2) {
            val bb = AxisAlignedBB(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                mc.thePlayer.posX,
                off.toDouble(),
                mc.thePlayer.posZ
            )
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true
            }
            off += 2
        }
        return false
    }

    private fun inAir(height: Double, plus: Double): Boolean {
        if (mc.thePlayer.posY < 0) return false
        var off = 0
        while (off < height) {
            val bb = AxisAlignedBB(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                mc.thePlayer.posX,
                mc.thePlayer.posY - off,
                mc.thePlayer.posZ
            )
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true
            }
            off += plus.toInt()
        }
        return false
    }

    override val tag: String
        get() = modeValue.get()
}