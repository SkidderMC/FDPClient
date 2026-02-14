/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.aac.AACv1
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.aac.AACv2
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.aac.AACv3
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.ncp.NCP
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.Buzz
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.Hycraft
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.Redesky
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.VerusDamage
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.VerusDamage.damaged
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockStairs
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import kotlin.math.cos
import kotlin.math.sin

object LongJump : Module("LongJump", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    private val longJumpModes = arrayOf(
        NCP, AACv1, AACv2, AACv3, Redesky, Hycraft, Buzz, VerusDamage
    )

    private val modes = longJumpModes.map { it.modeName }.toMutableList().apply {
        add("Matrix")
        add("Slap")
    }.toTypedArray()

    val mode by choices("Mode", modes, "NCP")
    val ncpBoost by float("NCPBoost", 4.25f, 1f..10f) { mode == "NCP" }
    val matrixSpeed by float("MatrixSpeed", 2.0f, 0.1f..5.0f) { mode == "Matrix" }
    val matrixTicks by int("MatrixTicks", 20, 1..100) { mode == "Matrix" }

    private val autoJump by boolean("AutoJump", true)
    val autoDisable by boolean("AutoDisable", true) { mode == "VerusDamage" || mode == "Matrix" }

    @JvmField
    var jumped = false
    @JvmField
    var canBoost = false
    @JvmField
    var teleported = false // Чтобы AACv3 не ругался

    private var placed = false
    private var flag = false
    private var sent = false
    private var ticks = 0
    private var firstDir = 0.0f

    // Прямой расчет скорости без MoveUtils
    private fun strafe(speed: Double) {
        var yaw = mc.thePlayer.rotationYaw.toDouble()
        val forward = mc.thePlayer.movementInput.moveForward.toDouble()
        val strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
        if (forward == 0.0 && strafe == 0.0) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toDouble()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toDouble()
                }
            }
            val rad = Math.toRadians(yaw)
            mc.thePlayer.motionX = -sin(rad) * speed
            mc.thePlayer.motionZ = cos(rad) * speed
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val currentMode = mode

        if (currentMode == "Slap") {
            if (!mc.thePlayer.isInWater) {
                var slot = -1
                for (i in 0..8) {
                    val stack = mc.thePlayer.inventory.getStackInSlot(i)
                    if (stack != null && stack.item is net.minecraft.item.ItemBlock) {
                        slot = i
                        break
                    }
                }

                if (slot == -1) {
                    mc.thePlayer.addChatMessage(ChatComponentText("§c[LongJump] §fNo blocks!"))
                    state = false
                    return@handler
                }

                val oldSlot = mc.thePlayer.inventory.currentItem
                val trace = mc.thePlayer.rayTrace(2.0, 1.0f)

                if (trace != null && trace.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {
                    val pos = trace.blockPos
                    if (mc.thePlayer.isMoving) {
                        if (mc.thePlayer.fallDistance >= 0.8f && 
                            mc.theWorld.isAirBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) &&
                            !mc.theWorld.isAirBlock(pos) &&
                            mc.theWorld.getBlockState(pos).block !is BlockSlab &&
                            mc.theWorld.getBlockState(pos).block !is BlockStairs) {
                            
                            mc.thePlayer.inventory.currentItem = slot
                            placed = true
                            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem, pos, trace.sideHit, trace.hitVec)) {
                                mc.thePlayer.swingItem()
                            }
                            mc.thePlayer.inventory.currentItem = oldSlot
                            mc.thePlayer.fallDistance = 0f
                        }
                        mc.gameSettings.keyBindJump.pressed = false
                        if (mc.thePlayer.onGround && placed) {
                            placed = false
                        } else if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump()
                        }
                    }
                }
            }
        } else if (currentMode == "Matrix") {
            if (!canBoost) {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
            }

            if (!sent) {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
                if (ticks > matrixTicks) {
                    sent = true
                    ticks = 0
                    canBoost = true
                    mc.timer.timerSpeed = 1.0f
                }
            }

            if (canBoost) {
                strafe(matrixSpeed.toDouble())
                mc.thePlayer.motionY = 0.42
                if (flag) state = false
            }
            ticks++
        }

        if (jumped) {
            if (mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
                jumped = false
                if (currentMode == "NCP") {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
                return@handler
            }
            if (currentMode !in listOf("Matrix", "Slap")) {
                modeModule.onUpdate()
            }
        }

        if (autoJump && mc.thePlayer.onGround && mc.thePlayer.isMoving) {
            if (autoDisable && currentMode == "VerusDamage" && !damaged) return@handler
            jumped = true
            mc.thePlayer.tryJump()
        }
    }

    val onMove = handler<MoveEvent> { event ->
        if (mode == "Matrix" && !canBoost) {
            event.x = 0.0
            event.z = 0.0
        }
        if (mode !in listOf("Matrix", "Slap")) {
            modeModule.onMove(event)
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook) {
            if (mode == "Slap") placed = false
            if (mode == "Matrix") flag = true
        }
    }

    override fun onEnable() {
        placed = false
        jumped = false
        canBoost = false
        teleported = false
        if (mode == "Matrix") {
            flag = false
            sent = false
            ticks = 0
            firstDir = mc.thePlayer.rotationYaw
        }
        if (mode !in listOf("Matrix", "Slap")) {
            modeModule.onEnable()
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        if (mode !in listOf("Matrix", "Slap")) {
            modeModule.onDisable()
        }
    }

    val onJump = handler<JumpEvent>(always = true) { event ->
        jumped = true
        canBoost = true
        teleported = false

        if (handleEvents() && mode !in listOf("Matrix", "Slap")) {
            modeModule.onJump(event)
        }
    }

    override val tag get() = mode

    private val modeModule get() = longJumpModes.find { it.modeName == mode }!!
}
