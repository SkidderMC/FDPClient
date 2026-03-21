/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.event.async.loopSequence
import net.ccbluex.liquidbounce.event.async.waitTicks
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.exploit.Phase
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.direction
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.init.Blocks.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.stats.StatList
import net.minecraft.item.ItemStack
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Step : Module("Step", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, gameDetecting = false) {

    /**
     * OPTIONS
     */

    private val mode by choices(
        "Mode",
        arrayOf(
            "Vanilla", "Jump", "Matrix6.7.0", "NCP", "NCPNew", "MotionNCP",
            "OldNCP", "AAC", "OldAAC", "AAC4.4.0", "LAAC", "AAC3.3.4",
            "Spartan", "Rewinside", "Vulcan", "Verus", "BlocksMC", "BlocksMCTimer"
        ),
        "NCP"
    )

    private val height by float("Height", 1F, 0.6F..10F)
    { mode !in arrayOf("Jump", "MotionNCP", "LAAC", "AAC3.3.4", "BlocksMCTimer") }
    private val jumpHeight by float("JumpHeight", 0.42F, 0.37F..0.42F)
    { mode == "Jump" }

    private val delay by int("Delay", 0, 0..500)
    private val timerValue by float("Timer", 1F, 0.05F..1F) {
        mode !in arrayOf("Matrix6.7.0", "Verus")
    }
    private val timerDynValue by boolean("UseDynamicTimer", false) {
        mode !in arrayOf("Matrix6.7.0", "Verus")
    }

    /**
     * VALUES
     */

    private var isStep = false
    private var stepX = 0.0
    private var stepY = 0.0
    private var stepZ = 0.0

    private var ncpNextStep = 0
    private var spartanSwitch = false
    private var isAACStep = false
    private var wasTimer = false
    private var lastOnGround = false
    private var canStep = false

    private val timer = MSTimer()

    override fun onDisable() {
        val thePlayer = mc.thePlayer ?: return

        // Change step height back to default (0.6 is default)
        thePlayer.stepHeight = 0.6F
        mc.timer.timerSpeed = 1f
        wasTimer = false
        canStep = false
        lastOnGround = thePlayer.onGround
    }

    val onUpdate = loopSequence {
        val mode = mode
        val thePlayer = mc.thePlayer ?: return@loopSequence

        if (wasTimer) {
            mc.timer.timerSpeed = 1f
            wasTimer = false
        }

        if (mode in arrayOf("AAC4.4.0", "NCPNew", "Matrix6.7.0")) {
            if (thePlayer.isCollidedHorizontally && thePlayer.onGround && lastOnGround) {
                canStep = true
                thePlayer.stepHeight = height
            } else {
                canStep = false
                thePlayer.stepHeight = 0.6F
            }

            lastOnGround = thePlayer.onGround
        } else {
            canStep = false
        }

        if (thePlayer.isOnLadder || thePlayer.isInLiquid || thePlayer.isInWeb) return@loopSequence

        if (!thePlayer.isMoving) return@loopSequence

        // Motion steps
        when (mode) {
            "Jump" ->
                if (thePlayer.isCollidedHorizontally && thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown) {
                    fakeJump()
                    thePlayer.motionY = jumpHeight.toDouble()
                }

            "BlocksMCTimer" ->
                if (thePlayer.onGround && thePlayer.isCollidedHorizontally) {
                    val chest = BlockUtils.searchBlocks(2, setOf(chest, ender_chest, trapped_chest))

                    if (!couldStep() || chest.isNotEmpty()) {
                        mc.timer.timerSpeed = 1f
                        return@loopSequence
                    }

                    fakeJump()
                    thePlayer.tryJump()

                    // TODO: Improve Timer Balancing
                    mc.timer.timerSpeed = 5f
                    waitTicks(1)
                    mc.timer.timerSpeed = 0.2f
                    waitTicks(1)
                    mc.timer.timerSpeed = 4f
                    waitTicks(1)
                    strafe(0.27F)
                    mc.timer.timerSpeed = 1f
                }

            "LAAC" ->
                if (thePlayer.isCollidedHorizontally) {
                    if (thePlayer.onGround && timer.hasTimePassed(delay)) {
                        isStep = true

                        fakeJump()
                        thePlayer.motionY += 0.620000001490116

                        val yaw = direction
                        thePlayer.motionX -= sin(yaw) * 0.2
                        thePlayer.motionZ += cos(yaw) * 0.2
                        timer.reset()
                    }

                    thePlayer.onGround = true
                } else isStep = false

            "AAC3.3.4" ->
                if (thePlayer.isCollidedHorizontally && thePlayer.isMoving) {
                    if (thePlayer.onGround && couldStep()) {
                        thePlayer.motionX *= 1.26
                        thePlayer.motionZ *= 1.26
                        thePlayer.tryJump()
                        isAACStep = true
                    }

                    if (isAACStep) {
                        thePlayer.motionY -= 0.015

                        if (!thePlayer.isUsingItem && thePlayer.movementInput.moveStrafe == 0F)
                            thePlayer.jumpMovementFactor = 0.3F
                    }
                } else isAACStep = false
        }
    }

    val onMove = handler<MoveEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        if (mode != "MotionNCP" || !thePlayer.isCollidedHorizontally || mc.gameSettings.keyBindJump.isKeyDown)
            return@handler

        // Motion steps
        when {
            thePlayer.onGround && couldStep() -> {
                fakeJump()
                thePlayer.motionY = 0.0
                event.y = 0.41999998688698
                ncpNextStep = 1
            }

            ncpNextStep == 1 -> {
                event.y = 0.7531999805212 - 0.41999998688698
                ncpNextStep = 2
            }

            ncpNextStep == 2 -> {
                val yaw = direction

                event.y = 1.001335979112147 - 0.7531999805212
                event.x = -sin(yaw) * 0.7
                event.z = cos(yaw) * 0.7

                ncpNextStep = 0
            }
        }
    }

    val onStep = handler<StepEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        // Phase should disable step
        if (Phase.handleEvents()) {
            event.stepHeight = 0F
            return@handler
        }

        // Some fly modes should disable step
        if (Flight.handleEvents() && Flight.mode in arrayOf(
                "Hypixel",
                "OtherHypixel",
                "LatestHypixel",
                "Rewinside",
                "Mineplex"
            )
            && thePlayer.inventory.getCurrentItem() == null
        ) {
            event.stepHeight = 0F
            return@handler
        }

        val mode = mode

        if (mode in arrayOf("AAC4.4.0", "NCPNew", "Matrix6.7.0")) {
            if (!canStep || event.stepHeight <= 0.6F) {
                thePlayer.stepHeight = 0.6F
                event.stepHeight = 0.6F
                return@handler
            }
        }

        // Set step to default in some cases
        if (!thePlayer.onGround || !timer.hasTimePassed(delay) ||
            mode in arrayOf("Jump", "MotionNCP", "LAAC", "AAC3.3.4", "BlocksMCTimer")
        ) {
            thePlayer.stepHeight = 0.6F
            event.stepHeight = 0.6F
            return@handler
        }

        // Set step height
        val height = height
        thePlayer.stepHeight = height
        event.stepHeight = height

        // Detect possible step
        if (event.stepHeight > 0.6F) {
            isStep = true
            stepX = thePlayer.posX
            stepY = thePlayer.posY
            stepZ = thePlayer.posZ
        }
    }

    val onStepConfirm = handler<StepConfirmEvent>(always = true) {
        val thePlayer = mc.thePlayer

        if (thePlayer == null || !isStep) // Check if step
            return@handler

        if (thePlayer.entityBoundingBox.minY - stepY > 0.6) { // Check if full block step
            if (timerValue < 1.0f) {
                wasTimer = true
                mc.timer.timerSpeed = timerValue

                if (timerDynValue) {
                    mc.timer.timerSpeed = (mc.timer.timerSpeed / sqrt(thePlayer.entityBoundingBox.minY - stepY)).toFloat()
                }
            }

            when (mode) {
                "NCP", "AAC", "OldAAC" -> {
                    fakeJump()

                    // Half legit step (1 packet missing) [COULD TRIGGER TOO MANY PACKETS]
                    sendPackets(
                        C04PacketPlayerPosition(stepX, stepY + 0.41999998688698, stepZ, false),
                        C04PacketPlayerPosition(stepX, stepY + 0.7531999805212, stepZ, false)
                    )
                    timer.reset()
                }

                "AAC4.4.0" -> {
                    val rstepHeight = thePlayer.entityBoundingBox.minY - stepY

                    fakeJump()
                    timer.reset()

                    when {
                        rstepHeight >= 1.0 - 0.015625 && rstepHeight < 1.5 - 0.015625 -> {
                            arrayOf(0.4, 0.7, 0.9).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                            sendPacket(C04PacketPlayerPosition(stepX, stepY + 1.0, stepZ, true))
                        }

                        rstepHeight >= 1.5 - 0.015625 && rstepHeight < 2.0 - 0.015625 -> {
                            arrayOf(0.42, 0.7718, 1.0556, 1.2714, 1.412).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                            sendPacket(C04PacketPlayerPosition(stepX, stepY + 1.50, stepZ, true))
                        }

                        rstepHeight >= 2.0 - 0.015625 -> {
                            arrayOf(0.45, 0.84375, 1.18125, 1.4625, 1.6875, 1.85625, 1.96875).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                            sendPacket(
                                C04PacketPlayerPosition(
                                    stepX + thePlayer.motionX * 0.5,
                                    stepY + 2.0,
                                    stepZ + thePlayer.motionZ * 0.5,
                                    true
                                )
                            )
                        }
                    }
                }

                "NCPNew" -> {
                    val rstepHeight = thePlayer.entityBoundingBox.minY - stepY

                    fakeJump()

                    when {
                        rstepHeight > 2.019 -> {
                            arrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.919).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                            thePlayer.motionX = 0.0
                            thePlayer.motionZ = 0.0
                        }

                        rstepHeight <= 2.019 && rstepHeight > 1.869 -> {
                            arrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                            thePlayer.motionX = 0.0
                            thePlayer.motionZ = 0.0
                        }

                        rstepHeight <= 1.869 && rstepHeight > 1.5 -> {
                            arrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                            thePlayer.motionX = 0.0
                            thePlayer.motionZ = 0.0
                        }

                        rstepHeight <= 1.5 && rstepHeight > 1.015 -> {
                            arrayOf(0.42, 0.7532, 1.01, 1.093, 1.015).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                        }

                        rstepHeight <= 1.015 && rstepHeight > 0.875 -> {
                            sendPackets(
                                C04PacketPlayerPosition(stepX, stepY + 0.41999998688698, stepZ, false),
                                C04PacketPlayerPosition(stepX, stepY + 0.7531999805212, stepZ, false)
                            )
                        }

                        rstepHeight <= 0.875 && rstepHeight > 0.6 -> {
                            arrayOf(0.39, 0.6938).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                        }
                    }

                    timer.reset()
                }

                "Spartan" -> {
                    fakeJump()

                    if (spartanSwitch) {
                        // Vanilla step (3 packets) [COULD TRIGGER TOO MANY PACKETS]
                        sendPackets(
                            C04PacketPlayerPosition(stepX, stepY + 0.41999998688698, stepZ, false),
                            C04PacketPlayerPosition(stepX, stepY + 0.7531999805212, stepZ, false),
                            C04PacketPlayerPosition(stepX, stepY + 1.001335979112147, stepZ, false)
                        )
                    } else // Force step
                        sendPacket(C04PacketPlayerPosition(stepX, stepY + 0.6, stepZ, false))

                    // Spartan allows one unlegit step so just swap between legit and unlegit
                    spartanSwitch = !spartanSwitch

                    // Reset timer
                    timer.reset()
                }

                "Rewinside" -> {
                    fakeJump()

                    // Vanilla step (3 packets) [COULD TRIGGER TOO MANY PACKETS]
                    sendPackets(
                        C04PacketPlayerPosition(stepX, stepY + 0.41999998688698, stepZ, false),
                        C04PacketPlayerPosition(stepX, stepY + 0.7531999805212, stepZ, false),
                        C04PacketPlayerPosition(stepX, stepY + 1.001335979112147, stepZ, false)
                    )

                    // Reset timer
                    timer.reset()
                }

                "Matrix6.7.0" -> {
                    val rstepHeight = thePlayer.entityBoundingBox.minY - stepY

                    fakeJump()

                    when {
                        rstepHeight <= 3.0042 && rstepHeight > 2.95 -> {
                            arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289, 2.04032, 2.23371, 2.35453, 2.40423).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, offset in 0.9..1.01))
                            }
                            mc.timer.timerSpeed = 0.11f
                            wasTimer = true
                        }

                        rstepHeight <= 2.95 && rstepHeight > 2.83 -> {
                            arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289, 2.04032, 2.23371, 2.35453).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, offset in 0.9..1.01))
                            }
                            mc.timer.timerSpeed = 0.12f
                            wasTimer = true
                        }

                        rstepHeight <= 2.83 && rstepHeight > 2.64 -> {
                            arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289, 2.04032, 2.23371).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, offset in 0.9..1.01))
                            }
                            mc.timer.timerSpeed = 0.13f
                            wasTimer = true
                        }

                        rstepHeight <= 2.64 && rstepHeight > 2.37 -> {
                            arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289, 2.04032).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, offset in 0.9..1.01))
                            }
                            mc.timer.timerSpeed = 0.14f
                            wasTimer = true
                        }

                        rstepHeight <= 2.37 && rstepHeight > 2.02 -> {
                            arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, offset in 0.9..1.01))
                            }
                            mc.timer.timerSpeed = 0.16f
                            wasTimer = true
                        }

                        rstepHeight <= 2.02 && rstepHeight > 1.77 -> {
                            arrayOf(0.41951, 0.75223, 0.99990, 1.42989).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, offset in 0.9..1.01))
                            }
                            mc.timer.timerSpeed = 0.21f
                            wasTimer = true
                        }

                        rstepHeight <= 1.77 && rstepHeight > 1.6 -> {
                            arrayOf(0.41999998688698, 0.7531999805212, 1.17319996740818).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, offset in 0.753..0.754))
                            }
                            mc.timer.timerSpeed = 0.28f
                            wasTimer = true
                        }

                        rstepHeight <= 1.6 && rstepHeight > 1.3525 -> {
                            arrayOf(0.41999998688698, 0.7531999805212, 1.001335979112147).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                            mc.timer.timerSpeed = 0.28f
                            wasTimer = true
                        }

                        rstepHeight <= 1.3525 && rstepHeight > 1.02 -> {
                            arrayOf(0.41999998688698, 0.7531999805212).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, false))
                            }
                            mc.timer.timerSpeed = 0.34f
                            wasTimer = true
                        }

                        rstepHeight <= 1.02 && rstepHeight > 0.6 -> {
                            sendPacket(C04PacketPlayerPosition(stepX, stepY + 0.41999998688698, stepZ, false))
                            mc.timer.timerSpeed = 0.5f
                            wasTimer = true
                        }
                    }

                    timer.reset()
                }

                "Verus" -> {
                    val rstepHeight = thePlayer.entityBoundingBox.minY - stepY
                    var stepHeightOffset = 0.0

                    mc.timer.timerSpeed = 1f / ceil(rstepHeight * 2.0).toFloat()
                    fakeJump()

                    repeat((ceil(rstepHeight * 2.0) - 1.0).toInt()) {
                        stepHeightOffset += 0.5
                        sendPacket(C04PacketPlayerPosition(stepX, stepY + stepHeightOffset, stepZ, true))
                    }

                    wasTimer = true
                }

                "Vulcan" -> {
                    val rstepHeight = thePlayer.entityBoundingBox.minY - stepY

                    fakeJump()

                    when {
                        rstepHeight > 2.0 -> {
                            arrayOf(0.5, 1.0, 1.5, 2.0).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, true))
                            }
                        }

                        rstepHeight <= 2.0 && rstepHeight > 1.5 -> {
                            arrayOf(0.5, 1.0, 1.5).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, true))
                            }
                        }

                        rstepHeight <= 1.5 && rstepHeight > 1.0 -> {
                            arrayOf(0.5, 1.0).forEach { offset ->
                                sendPacket(C04PacketPlayerPosition(stepX, stepY + offset, stepZ, true))
                            }
                        }

                        rstepHeight <= 1.0 && rstepHeight > 0.6 -> {
                            sendPacket(C04PacketPlayerPosition(stepX, stepY + 0.5, stepZ, true))
                        }
                    }

                    timer.reset()
                }

                "BlocksMC" -> {
                    val pos = thePlayer.position.add(0.0, -1.5, 0.0)

                    fakeJump()
                    sendPacket(
                        C08PacketPlayerBlockPlacement(
                            pos,
                            1,
                            ItemStack(stone.getItem(mc.theWorld, pos)),
                            0.0F,
                            0.5F + Math.random().toFloat() * 0.44F,
                            0.0F
                        )
                    )
                    sendPackets(
                        C04PacketPlayerPosition(stepX, stepY + 0.41999998688698, stepZ, false),
                        C04PacketPlayerPosition(stepX, stepY + 0.7531999805212, stepZ, false),
                        C04PacketPlayerPosition(stepX, stepY + 1.0, stepZ, true)
                    )
                    timer.reset()
                }
            }
        }

        isStep = false
        stepX = 0.0
        stepY = 0.0
        stepZ = 0.0
    }

    val onPacket = handler<PacketEvent>(always = true) { event ->
        val packet = event.packet

        if (packet is C03PacketPlayer && isStep && mode == "OldNCP") {
            packet.y += 0.07
            isStep = false
        }
    }

    // There could be some anti cheats which tries to detect step by checking for achievements and stuff
    private fun fakeJump() {
        val thePlayer = mc.thePlayer ?: return

        thePlayer.isAirBorne = true
        thePlayer.triggerAchievement(StatList.jumpStat)
    }

    private fun couldStep(): Boolean {
        val player = mc.thePlayer ?: return false

        if (player.isSneaking || mc.gameSettings.keyBindJump.isKeyDown)
            return false

        val yaw = direction
        val heightOffset = 1.001335979112147

        for (i in -10..10) {
            val adjustedYaw = yaw + (i * Math.toRadians(8.0))
            val x = -sin(adjustedYaw) * 0.2
            val z = cos(adjustedYaw) * 0.2

            if (mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(x, heightOffset, z)).isNotEmpty()) {
                return false
            }
        }

        return true
    }

    override val tag
        get() = mode
}
