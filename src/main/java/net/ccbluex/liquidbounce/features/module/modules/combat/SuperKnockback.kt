/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.angleDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.*
import kotlin.math.abs

object SuperKnockback : Module("SuperKnockback", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    private val chance by int("Chance", 100, 0..100)
    private val delay by int("Delay", 0, 0..500)
    private val hurtTime by int("HurtTime", 10, 0..10)

    private val mode by choices(
        "Mode",
        arrayOf("WTap", "SprintTap", "SprintTap2", "Old", "Silent", "Packet", "SneakPacket"),
        "Old"
    )

    private val ticksUntilBlock by intRange("TicksUntilBlock", 0..2, 0..5) { mode == "WTap" }
    private val reSprintTicks by intRange("ReSprintTicks", 1..2, 1..5) { mode == "WTap" }

    private val targetDistance by int("TargetDistance", 3, 1..5) { mode == "WTap" }

    private val stopTicks: Value<Int> = int("PressBackTicks", 1, 1..5) {
        mode == "SprintTap2"
    }.onChange { _, new ->
        new.coerceAtMost(unSprintTicks.get())
    }
    private val unSprintTicks: Value<Int> = int("ReleaseBackTicks", 2, 1..5) {
        mode == "SprintTap2"
    }.onChange { _, new ->
        new.coerceAtLeast(stopTicks.get())
    }

    private val minEnemyRotDiffToIgnore by float("MinRotationDiffFromEnemyToIgnore", 180f, 0f..180f)

    private val onlyGround by boolean("OnlyGround", false)
    val onlyMove by boolean("OnlyMove", true)
    val onlyMoveForward by boolean("OnlyMoveForward", true) { onlyMove }
    private val onlyWhenTargetGoesBack by boolean("OnlyWhenTargetGoesBack", false)

    private var ticks = 0
    private var forceSprintState = 0
    private val timer = MSTimer()

    // WTap
    private var blockInputTicks = ticksUntilBlock.random()
    private var blockTicksElapsed = 0
    private var startWaiting = false
    private var blockInput = false
    private var allowInputTicks = reSprintTicks.random()
    private var ticksElapsed = 0

    // SprintTap2
    private var sprintTicks = 0

    override fun onToggle(state: Boolean) {
        // Make sure the user won't have their input forever blocked
        blockInput = false
        startWaiting = false
        blockTicksElapsed = 0
        ticksElapsed = 0
        sprintTicks = 0
    }

    val onAttack = handler<AttackEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val target = event.targetEntity as? EntityLivingBase ?: return@handler
        val distance = player.getDistanceToEntityBox(target)

        val rotationToPlayer = toRotation(player.hitBox.center, false, target).fixedSensitivity().yaw
        val angleDifferenceToPlayer = abs(angleDifference(rotationToPlayer, target.rotationYaw))

        if (event.targetEntity.hurtTime > hurtTime || !timer.hasTimePassed(delay) || onlyGround && !player.onGround || RandomUtils.nextInt(
                endExclusive = 100
            ) > chance
        ) return@handler

        if (onlyMove && (!player.isMoving || onlyMoveForward && player.movementInput.moveStrafe != 0f)) return@handler

        // Is the enemy facing their back on us?
        if (angleDifferenceToPlayer > minEnemyRotDiffToIgnore && !target.hitBox.isVecInside(player.eyes)) return@handler

        val pos = target.currPos - target.lastTickPos

        val distanceBasedOnMotion = player.getDistanceToBox(target.hitBox.offset(pos))

        // Is the entity's distance based on motion farther than the normal distance?
        if (onlyWhenTargetGoesBack && distanceBasedOnMotion >= player.getDistanceToEntityBox(target)) return@handler

        when (mode) {
            "Old" -> {
                // Users reported that this mode is better than the other ones
                if (player.isSprinting) {
                    sendPacket(C0BPacketEntityAction(player, STOP_SPRINTING))
                }

                sendPackets(
                    C0BPacketEntityAction(player, START_SPRINTING),
                    C0BPacketEntityAction(player, STOP_SPRINTING),
                    C0BPacketEntityAction(player, START_SPRINTING)
                )
                player.isSprinting = true
                player.serverSprintState = true
            }

            "SprintTap", "Silent" -> if (player.isSprinting && player.serverSprintState) ticks = 2

            "Packet" -> {
                sendPackets(
                    C0BPacketEntityAction(player, STOP_SPRINTING),
                    C0BPacketEntityAction(player, START_SPRINTING)
                )
            }

            "SneakPacket" -> {
                sendPackets(
                    C0BPacketEntityAction(player, STOP_SPRINTING),
                    C0BPacketEntityAction(player, START_SNEAKING),
                    C0BPacketEntityAction(player, START_SPRINTING),
                    C0BPacketEntityAction(player, STOP_SNEAKING)
                )
            }

            "WTap" -> {
                // We want the player to be sprinting before we block inputs
                if (player.isSprinting && player.serverSprintState && !blockInput && !startWaiting) {
                    val delayMultiplier = 1.0 / (abs(targetDistance - distance) + 1)

                    blockInputTicks = (ticksUntilBlock.random() * delayMultiplier).toInt()

                    blockInput = blockInputTicks == 0

                    if (!blockInput) {
                        startWaiting = true
                    }

                    allowInputTicks = (reSprintTicks.random() * delayMultiplier).toInt()
                }
            }

            "SprintTap2" -> {
                if (++sprintTicks == stopTicks.get()) {

                    if (player.isSprinting && player.serverSprintState) {
                        player.isSprinting = false
                        player.serverSprintState = false
                    } else {
                        player.isSprinting = true
                        player.serverSprintState = true
                    }

                    mc.thePlayer.stopXZ()

                } else if (sprintTicks >= unSprintTicks.get()) {

                    player.isSprinting = false
                    player.serverSprintState = false

                    sprintTicks = 0
                }
            }
        }

        timer.reset()
    }

    val onPostSprintUpdate = handler<PostSprintUpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        if (mode == "SprintTap") {
            when (ticks) {
                2 -> {
                    player.isSprinting = false
                    forceSprintState = 2
                    ticks--
                }

                1 -> {
                    if (player.movementInput.moveForward > 0.8) {
                        player.isSprinting = true
                    }
                    forceSprintState = 1
                    ticks--
                }

                else -> {
                    forceSprintState = 0
                }
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (mode == "WTap") {
            if (blockInput) {
                if (ticksElapsed++ >= allowInputTicks) {
                    blockInput = false
                    ticksElapsed = 0
                }
            } else {
                if (startWaiting) {
                    blockInput = blockTicksElapsed++ >= blockInputTicks

                    if (blockInput) {
                        startWaiting = false
                        blockTicksElapsed = 0
                    }
                }
            }
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val packet = event.packet
        if (packet is C03PacketPlayer && mode == "Silent") {
            if (ticks == 2) {
                sendPacket(C0BPacketEntityAction(player, STOP_SPRINTING))
                ticks--
            } else if (ticks == 1 && player.isSprinting) {
                sendPacket(C0BPacketEntityAction(player, START_SPRINTING))
                ticks--
            }
        }
    }

    fun shouldBlockInput() = handleEvents() && mode == "WTap" && blockInput

    override val tag
        get() = mode

    fun breakSprint() = handleEvents() && forceSprintState == 2 && mode == "SprintTap"
    fun startSprint() = handleEvents() && forceSprintState == 1 && mode == "SprintTap"
}