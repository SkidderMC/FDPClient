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
import net.ccbluex.liquidbounce.utils.timer.MSTimer
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
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.ceil
import kotlin.math.sqrt

@ModuleInfo(name = "NoFall", category = ModuleCategory.PLAYER)
class NoFall : Module() {
    val modeValue = ListValue("Mode", arrayOf("SpoofGround", "AlwaysSpoofGround", "NoGround", "Packet", "Packet1", "Packet2", "MLG", "OldAAC", "LAAC", "AAC3.3.11", "AAC3.3.15", "AACv4", "AAC4.4.X-Flag", "LoyisaAAC4.4.2", "AAC5.0.4", "AAC5.0.14", "Spartan", "CubeCraft", "Hypixel", "HypSpoof", "Phase", "Verus", "Damage", "MotionFlag", "OldMatrix", "Matrix", "MatrixPacket"), "SpoofGround")
    private val phaseOffsetValue = IntegerValue("PhaseOffset", 1, 0, 5).displayable { modeValue.equals("Phase") }
    private val minFallDistance = FloatValue("MinMLGHeight", 5f, 2f, 50f).displayable { modeValue.equals("MLG") }
    private val flySpeed = FloatValue("MotionSpeed", -0.01f, -5f, 5f).displayable { modeValue.equals("MotionFlag") }

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
    private var matrixIsFall = false
    private var matrixCanSpoof = false
    private var matrixFallTicks = 0
    private var matrixLastMotionY = 0.0
    private var isDmgFalling = false
    private var matrixFlagWait = 0
    private val aac4FlagCooldown = MSTimer()
    private var aac4FlagCount = 0

    override fun onEnable() {
        aac4FlagCount = 0
        aac4Fakelag = false
        aac5Check = false
        packetModify = false
        aac4Packets.clear()
        needSpoof = false
        aac5doFlag = false
        aac5Timer = 0
        packet1Count = 0
        oldaacState = 0
        matrixIsFall = false
        matrixCanSpoof = false
        matrixFallTicks = 0
        matrixLastMotionY = 0.0
        isDmgFalling = false
        matrixFlagWait = 0
        aac4FlagCooldown.reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (matrixFlagWait > 0) {
            matrixFlagWait--
            if(matrixFlagWait == 0) {
                mc.timer.timerSpeed = 1f
            }
        }
        if (mc.thePlayer.onGround) {
            jumped = false
        }

        if (mc.thePlayer.motionY > 0) {
            jumped = true
        }

        if (!state || LiquidBounce.moduleManager[FreeCam::class.java]!!.state) {
            return
        }

        if (mc.thePlayer.isSpectator || mc.thePlayer.capabilities.allowFlying || mc.thePlayer.capabilities.disableDamage) {
            return
        }

        if (BlockUtils.collideBlock(mc.thePlayer.entityBoundingBox) { it is BlockLiquid } || BlockUtils.collideBlock(AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY, mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.01, mc.thePlayer.entityBoundingBox.minZ)) { it is BlockLiquid }) {
            return
        }

        when (modeValue.get().lowercase()) {
            "packet" -> {
                if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3f){
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                    mc.thePlayer.fallDistance = 0f
                }
            }
            "matrixpacket" -> {
//                mc.timer.timerSpeed = if(abs((FallingPlayer(mc.thePlayer).findCollision(100)?.y ?: 0) - mc.thePlayer.posY) > 3) {
//                    (mc.timer.timerSpeed * 0.8f).coerceAtLeast(0.3f)
//                } else { 1f }
                if(mc.thePlayer.onGround) {
                    mc.timer.timerSpeed = 1f
                } else if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3f){
                    mc.timer.timerSpeed = (mc.timer.timerSpeed * if(mc.timer.timerSpeed < 0.6) { 0.25f } else { 0.5f }).coerceAtLeast(0.2f)
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.thePlayer.fallDistance = 0f
                }
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
                if (!jumped && mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb) {
                    mc.thePlayer.motionY = -6.0
                }
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
                    if (!mc.isIntegratedServerRunning) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, Double.NaN, mc.thePlayer.posZ, false))
                    }
                    mc.thePlayer.fallDistance = -9999f
                }
            }
            "motionflag" -> {
                if (mc.thePlayer.fallDistance > 3) {
                    mc.thePlayer.motionY = flySpeed.get().toDouble()
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
            "aac5.0.4","oldmatrix","loyisaaac4.4.2" -> {
                if (mc.thePlayer.fallDistance > 3) {
                    isDmgFalling = true
                }
                if (modeValue.get() == "LoyisaAAC4.4.2") {
                    if(aac4FlagCount>=3 || aac4FlagCooldown.hasTimePassed(1500L)) {
                        return
                    }
                    if(!aac4FlagCooldown.hasTimePassed(1500L) && (mc.thePlayer.onGround || mc.thePlayer.fallDistance < 0.5)) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                        mc.thePlayer.onGround = false
                        mc.thePlayer.jumpMovementFactor = 0.00f
                    }
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
                    aac5Timer = 18
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
                    val fallPos = FallingPlayer(mc.thePlayer)
                        .findCollision(5) ?: return
                    if (fallPos.y - mc.thePlayer.motionY / 20.0 < mc.thePlayer.posY) {
                        mc.timer.timerSpeed = 0.05f
                        Timer().schedule(100L) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(fallPos.x.toDouble(), fallPos.y.toDouble(), fallPos.z.toDouble(), true))
                            mc.timer.timerSpeed = 1f
                        }
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
            "packet2" -> {
                if (mc.thePlayer.fallDistance.toInt() / 2 > packet1Count) {
                    packet1Count = mc.thePlayer.fallDistance.toInt() / 2
                    packetModify = true
                }
                if (mc.thePlayer.onGround) {
                    packet1Count = 0
                }
            }
            "matrix" -> {
                if(matrixIsFall) {
                    mc.thePlayer.motionX=0.0
                    mc.thePlayer.jumpMovementFactor=0f
                    mc.thePlayer.motionZ=0.0
                    if(mc.thePlayer.onGround) matrixIsFall = false
                }
                if(mc.thePlayer.fallDistance-mc.thePlayer.motionY>3) {
                    matrixIsFall = true
                    if(matrixFallTicks==0) matrixLastMotionY=mc.thePlayer.motionY
                    mc.thePlayer.motionY=0.0
                    mc.thePlayer.motionX=0.0
                    mc.thePlayer.jumpMovementFactor=0f
                    mc.thePlayer.motionZ=0.0
                    mc.thePlayer.fallDistance=3.2f
                    if(matrixFallTicks in 8..9) matrixCanSpoof=true
                    matrixFallTicks++
                }
                if(matrixFallTicks>12 && !mc.thePlayer.onGround) {
                    mc.thePlayer.motionY=matrixLastMotionY
                    mc.thePlayer.fallDistance = 0f
                    matrixFallTicks=0
                    matrixCanSpoof=false
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
        } else if (modeValue.equals("MLG")) {
            if (event.eventState == EventState.PRE) {
                currentMlgRotation = null

                mlgTimer.update()

                if (!mlgTimer.hasTimePassed(10)) {
                    return
                }

                if (mc.thePlayer.fallDistance > minFallDistance.get()) {
                    val fallingPlayer = FallingPlayer(mc.thePlayer)

                    val maxDist = mc.playerController.blockReachDistance + 1.5

                    val collision = fallingPlayer.findCollision(ceil(1.0 / mc.thePlayer.motionY * -maxDist).toInt()) ?: return

                    var ok = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ).distanceTo(Vec3(collision).addVector(0.5, 0.5, 0.5)) < mc.playerController.blockReachDistance + sqrt(0.75)

                    if (mc.thePlayer.motionY < collision.y + 1 - mc.thePlayer.posY) {
                        ok = true
                    }

                    if (!ok) {
                        return
                    }

                    var index = -1

                    for (i in 36..44) {
                        val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack

                        if (itemStack != null && (itemStack.item == Items.water_bucket || itemStack.item is ItemBlock && (itemStack.item as ItemBlock).block == Blocks.web)) {
                            index = i - 36

                            if (mc.thePlayer.inventory.currentItem == index) {
                                break
                            }
                        }
                    }
                    if (index == -1) {
                        return
                    }

                    currentMlgItemIndex = index
                    currentMlgBlock = collision

                    if (mc.thePlayer.inventory.currentItem != index) {
                        mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(index))
                    }

                    currentMlgRotation = RotationUtils.faceBlock(collision)
                    currentMlgRotation!!.rotation.toPlayer(mc.thePlayer)
                }
            } else if (currentMlgRotation != null) {
                val stack = mc.thePlayer.inventory.mainInventory[currentMlgItemIndex]

                if (stack.item is ItemBucket) {
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, stack)
                } else {
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, currentMlgBlock, EnumFacing.UP, Vec3(0.0,0.0,0.0))) {
                        mlgTimer.reset()
                    }
                }
                if (mc.thePlayer.inventory.currentItem != currentMlgItemIndex) {
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val mode = modeValue.get()
        if (event.packet is S12PacketEntityVelocity) {
            if (mode.equals("AAC4.4.X-Flag", ignoreCase = true) && mc.thePlayer.fallDistance > 1.8) {
                event.packet.motionY = (event.packet.motionY * -0.1).toInt()
            }
        }
        if (event.packet is S08PacketPlayerPosLook) {
            if (mode.equals("LoyisaAAC4.4.2", ignoreCase = true)) {
                aac4FlagCount++
                if(matrixFlagWait > 0) {
                    aac4FlagCooldown.reset()
                    aac4FlagCount = 1
                    event.cancelEvent()
                }
            }
            if (mode.equals("OldMatrix", ignoreCase = true) && matrixFlagWait > 0) {
                matrixFlagWait = 0
                mc.timer.timerSpeed = 1.00f
                event.cancelEvent()
            }
        }
        if (event.packet is C03PacketPlayer) {
            val packet = event.packet
            if (mode.equals("SpoofGround", ignoreCase = true) && mc.thePlayer.fallDistance > 2.5) {
                packet.onGround = true
            } else if (mode.equals("AlwaysSpoofGround", ignoreCase = true)) {
                packet.onGround = true
            } else if (mode.equals("NoGround", ignoreCase = true)) {
                packet.onGround = false
            } else if (mode.equals("Hypixel", ignoreCase = true) && mc.thePlayer != null && mc.thePlayer.fallDistance > 1.5) {
                packet.onGround = mc.thePlayer.ticksExisted % 2 == 0
            } else if (mode.equals("HypSpoof", ignoreCase = true)) {
                PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
            } else if (mode.equals("AACv4", ignoreCase = true) && aac4Fakelag) {
                event.cancelEvent()
                if (packetModify) {
                    packet.onGround = true
                    packetModify = false
                }
                aac4Packets.add(packet)
            } else if (mode.equals("Verus", ignoreCase = true) && needSpoof) {
                packet.onGround = true
                needSpoof = false
            } else if (mode.equals("Damage", ignoreCase = true) && mc.thePlayer != null && mc.thePlayer.fallDistance > 3.5) {
                packet.onGround = true
            } else if (mode.equals("Packet1", ignoreCase = true) && packetModify) {
                packet.onGround = true
                packetModify = false
            } else if (mode.equals("Packet2", ignoreCase = true) && packetModify) {
                packet.onGround = true
                packetModify = false
            } else if (mode.equals("Matrix", ignoreCase = true) && matrixCanSpoof) {
                packet.onGround = true
                matrixCanSpoof = false
            } else if (mode.equals("AAC4.4.X-Flag", ignoreCase = true) && mc.thePlayer.fallDistance > 1.6) {
                packet.onGround = true
            } else if (mode.equals("AAC5.0.4", ignoreCase = true) && isDmgFalling) {
                if (packet.onGround && mc.thePlayer.onGround) {
                    isDmgFalling = false
                    packet.onGround = true
                    mc.thePlayer.onGround = false
                    packet.y += 1.0
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y - 1.0784, packet.z, false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y - 0.5, packet.z, true))
                }
            } else if ((mode.equals("OldMatrix", ignoreCase = true) || mode.equals("LoyisaAAC4.4.2", ignoreCase = true)) && isDmgFalling) {
                if (packet.onGround && mc.thePlayer.onGround) {
                    matrixFlagWait = 2
                    isDmgFalling = false
                    event.cancelEvent()
                    mc.thePlayer.onGround = false
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y - 256, packet.z, false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(packet.x, (-10).toDouble() , packet.z, true))
                    mc.timer.timerSpeed = 0.18f
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (BlockUtils.collideBlock(mc.thePlayer.entityBoundingBox) { it is BlockLiquid } || BlockUtils.collideBlock(AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY, mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.01, mc.thePlayer.entityBoundingBox.minZ)) { it is BlockLiquid }) {
            return
        }
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
