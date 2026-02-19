/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Reach
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.client.PacketUtils
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.searchCenter
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C12PacketUpdateSign
import net.minecraft.network.play.client.C19PacketResourcePackStatus
import net.minecraft.network.play.server.S06PacketUpdateHealth
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import java.awt.Color

object TimerRange : Module("TimerRange", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    private var playerTicks = 0
    private var smartTick = 0
    private var cooldownTick = 0
    private var randomRange = 0f

    private val packets = mutableListOf<Packet<*>>()
    private val packetsReceived = mutableListOf<Packet<*>>()
    private var blinked = false

    // Condition to confirm
    private var shouldReset = false
    private var confirmTick = false
    private var confirmStop = false

    // Condition to prevent getting timer speed stuck
    private var confirmAttack = false

    private val timerBoostMode by choices("TimerMode", arrayOf("Normal", "Smart", "Modern"), "Modern")

    private val ticksValue by int("Ticks", 10, 1..20)

    // Min & Max Boost Delay Settings
    private val timerBoostValue by float("TimerBoost", 1.5f, 0.01f..35f)
    private val boostDelay by floatRange("BoostDelay", 0.5f..0.55f, 0.1f..1f)

    // Min & Max Charged Delay Settings
    private val timerChargedValue by float("TimerCharged", 0.45f, 0.05f..5f)
    private val chargedDelay by floatRange("ChargedDelay", 0.75f..0.9f, 0.1f..1.0f)

    // Normal Mode Settings
    private val rangeValue by float("Range", 3.5f, 1f..5f) { timerBoostMode == "Normal" }
    private val cooldownTickValue by int("CooldownTick", 10, 1..50) { timerBoostMode == "Normal" }

    // Smart & Modern Mode Range
    private val range by floatRange("Range", 2.5f..3f, 2f..8f) { timerBoostMode != "Normal" }

    private val scanRange by float("ScanRange", 8f, 2f..12f) { timerBoostMode != "Normal" }.onChange { _, new ->
        new.coerceAtLeast(range.endInclusive)
    }

    // Min & Max Tick Delay
    private val tickDelay by intRange("TickDelay", 30..60, 1..200) { timerBoostMode != "Normal" }

    // Blink Option
    private val blink by boolean("Blink", false)

    // Prediction Settings
    private val predictClientMovement by int("PredictClientMovement", 2, 0..5)
    private val predictEnemyPosition by float("PredictEnemyPosition", 1.5f, -1f..2f)

    private val maxAngleDifference by float("MaxAngleDifference", 5f, 5f..90f) { timerBoostMode == "Modern" }

    // Mark Option
    private val markMode by choices("Mark", arrayOf("Off", "Box", "Platform"), "Off") { timerBoostMode == "Modern" }
    private val outline by boolean("Outline", false) { timerBoostMode == "Modern" && markMode == "Box" }

    // Optional
    private val onWeb by boolean("OnWeb", false)
    private val onLiquid by boolean("onLiquid", false)
    private val onForwardOnly by boolean("OnForwardOnly", true)
    private val resetOnlagBack by boolean("ResetOnLagback", false)
    private val resetOnKnockback by boolean("ResetOnKnockback", false)
    private val chatDebug by boolean("ChatDebug", true) { resetOnlagBack || resetOnKnockback }
    private val notificationDebug by boolean("NotificationDebug", false) { resetOnlagBack || resetOnKnockback }

    private val entities by EntityLookup<EntityLivingBase>()
        .filter { isSelected(it, true) }
        .filter { entity ->
            Backtrack.runWithNearestTrackedDistance(entity) {
                val distance = mc.thePlayer.getDistanceToEntityBox(entity)

                when (timerBoostMode.lowercase()) {
                    "normal" -> distance <= rangeValue
                    "smart", "modern" -> distance <= scanRange + randomRange
                    else -> false
                }
            }
        }

    override fun onDisable() {
        shouldResetTimer()
        BlinkUtils.unblink()

        smartTick = 0
        cooldownTick = 0
        playerTicks = 0

        shouldReset = false
        blinked = false

        confirmTick = false
        confirmStop = false
        confirmAttack = false
    }

    /**
     * Attack event (Normal & Smart Mode)
     */
    val onAttack = handler<AttackEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (event.targetEntity !is EntityLivingBase && playerTicks >= 1) {
            shouldResetTimer()
            return@handler
        } else {
            confirmAttack = true
        }

        val targetEntity = event.targetEntity ?: return@handler
        val entityDistance = targetEntity.let { player.getDistanceToEntityBox(it) }
        val randomTickDelay = tickDelay.random()
        val shouldReturn = Backtrack.runWithNearestTrackedDistance(targetEntity) { !updateDistance(targetEntity) }

        if (shouldReturn || (player.isInWeb && !onWeb) || (player.isInLiquid && !onLiquid)) {
            return@handler
        }

        smartTick++
        cooldownTick++

        val shouldSlowed = when (timerBoostMode) {
            "Normal" -> cooldownTick >= cooldownTickValue && entityDistance <= rangeValue
            "Smart" -> smartTick >= randomTickDelay && entityDistance <= randomRange
            else -> false
        }

        if (shouldSlowed && confirmAttack) {
            if (updateDistance(targetEntity)) {
                confirmAttack = false
                playerTicks = ticksValue
                cooldownTick = 0
                smartTick = 0
            }
        } else {
            shouldResetTimer()
        }
    }

    /**
     * Move event (Modern Mode)
     */
    val onMove = handler<MoveEvent> {
        val player = mc.thePlayer ?: return@handler

        if (timerBoostMode != "Modern") return@handler

        val nearbyEntity = getNearestEntityInRange() ?: return@handler

        val randomTickDelay = tickDelay.random()

        val shouldReturn = Backtrack.runWithNearestTrackedDistance(nearbyEntity) { !updateDistance(nearbyEntity) }

        if (shouldReturn || (player.isInWeb && !onWeb) || (player.isInLiquid && !onLiquid)) {
            return@handler
        }

        if (isPlayerMoving()) {
            smartTick++

            if (smartTick >= randomTickDelay) {
                confirmTick = true
                smartTick = 0
            }
        } else {
            smartTick = 0
        }

        if (isPlayerMoving() && !confirmStop) {
            if (player.isLookingOnEntity(nearbyEntity, maxAngleDifference.toDouble())) {
                val entityDistance = player.getDistanceToEntityBox(nearbyEntity)
                if (confirmTick && entityDistance in randomRange..range.endInclusive) {
                    if (updateDistance(nearbyEntity)) {
                        playerTicks = ticksValue
                        confirmTick = false
                    }
                }
            } else {
                shouldResetTimer()
            }
        } else {
            shouldResetTimer()
        }
    }

    private fun updateDistance(entity: Entity): Boolean {
        val player = mc.thePlayer ?: return false

        val prediction = entity.currPos.subtract(entity.prevPos).times(2 + predictEnemyPosition.toDouble())

        val boundingBox = entity.hitBox.offset(prediction)
        val (currPos, oldPos) = player.currPos to player.prevPos

        val simPlayer = SimulatedPlayer.fromClientPlayer(player.movementInput)

        repeat(predictClientMovement + 1) {
            simPlayer.tick()
        }

        player.setPosAndPrevPos(simPlayer.pos)

        val distance = searchCenter(
            boundingBox,
            outborder = false,
            predict = true,
            lookRange = if (timerBoostMode == "Normal") rangeValue else randomRange,
            attackRange = if (Reach.handleEvents()) Reach.combatReach else 3f,
        )

        if (distance == null) {
            player.setPosAndPrevPos(currPos, oldPos)
            return false
        }

        player.setPosAndPrevPos(currPos, oldPos)

        return true
    }

    /**
     * Motion event
     * (Resets player speed when less/more than target distance)
     */
    val onMotion = handler<MotionEvent> { event ->
        if (blink && event.eventState == EventState.POST) {
            synchronized(packetsReceived) {
                PacketUtils.schedulePacketProcess(packetsReceived)
            }
            packetsReceived.clear()
        }
    }

    /**
     * World Event
     * (Clear packets on disconnect)
     */
    val onWorld = handler<WorldEvent> { event ->
        if (blink && event.worldClient == null) {
            packets.clear()
            packetsReceived.clear()
        }
    }

    /**
     * Update event
     */
    val onUpdate = handler<UpdateEvent> {
        // Randomize the timer & charged delay a bit, to bypass some AntiCheat
        val timerBoost = boostDelay.random()
        val charged = chargedDelay.random()

        if (mc.thePlayer != null && mc.theWorld != null) {
            randomRange = range.random()
        }

        if (playerTicks <= 0 || confirmStop) {
            shouldResetTimer()

            if (blink && blinked) {
                BlinkUtils.unblink()
                blinked = false
            }

            return@handler
        }

        val tickProgress = playerTicks.toDouble() / ticksValue.toDouble()
        val playerSpeed = when {
            tickProgress < timerBoost -> timerBoostValue
            tickProgress < charged -> timerChargedValue
            else -> 1f
        }

        val speedAdjustment = if (playerSpeed >= 0) playerSpeed else 1f + ticksValue - playerTicks
        val adjustedTimerSpeed = maxOf(speedAdjustment, 0f)

        mc.timer.timerSpeed = adjustedTimerSpeed

        playerTicks--
    }

    /**
     * Render event (Mark)
     */
    val onRender3D = handler<Render3DEvent> {
        val player = mc.thePlayer ?: return@handler

        if (timerBoostMode.lowercase() != "modern") return@handler

        getNearestEntityInRange()?.let { nearbyEntity ->
            val entityDistance = player.getDistanceToEntityBox(nearbyEntity)

            if (entityDistance > scanRange) return@let

            val color = if (player.isLookingOnEntity(nearbyEntity, maxAngleDifference.toDouble())) {
                Color(37, 126, 255, 70)
            } else {
                Color(210, 60, 60, 70)
            }

            if (markMode != "Off") {
                when (markMode) {
                    "Box" -> drawEntityBox(nearbyEntity, color, outline)
                    "Platform" -> drawPlatform(nearbyEntity, color)
                }
            }
        }
    }

    /**
     * Check if player is moving
     */
    private fun isPlayerMoving(): Boolean {
        return if (!onForwardOnly) mc.thePlayer?.isMoving == true else {
            mc.thePlayer?.moveForward != 0f && mc.thePlayer?.moveStrafing == 0f
        }
    }

    /**
     * Find the nearest entity in range.
     */
    private fun getNearestEntityInRange(): Entity? {
        val player = mc.thePlayer ?: return null
        return entities.minByOrNull { player.getDistanceToEntityBox(it) }
    }

    /**
     * Separate condition to make it cleaner
     */
    private fun shouldResetTimer() {
        val nearestEntity = getNearestEntityInRange()

        if (nearestEntity == null || nearestEntity.isDead) {
            if (!shouldReset) {
                mc.timer.timerSpeed = 1f
                shouldReset = true
            }
        } else {
            if (!shouldReset && mc.timer.timerSpeed != 1f) {
                mc.timer.timerSpeed = 1f
                shouldReset = true
            } else {
                shouldReset = false
            }
        }
    }

    /**
     * Lagback Reset is Inspired from Nextgen TimerRange
     * Reset Timer on Lagback & Knockback.
     */
    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (mc.thePlayer == null || mc.thePlayer.isDead) return@handler

        if (blink) {
            if (playerTicks > 0 && !blinked) {
                BlinkUtils.blink(packet, event, sent = false, receive = true)
                blinked = true
            }

            if (blink && blinked) {
                when (packet) {
                    // Flush on doing/getting action.
                    is S08PacketPlayerPosLook, is C07PacketPlayerDigging, is C12PacketUpdateSign, is C19PacketResourcePackStatus -> {
                        BlinkUtils.unblink()
                        return@handler
                    }

                    // Flush on explosion
                    is S27PacketExplosion -> {
                        if (packet.field_149153_g != 0f || packet.field_149152_f != 0f || packet.field_149159_h != 0f) {
                            BlinkUtils.unblink()
                            return@handler
                        }
                    }

                    // Flush on damage
                    is S06PacketUpdateHealth -> {
                        if (packet.health < mc.thePlayer.health) {
                            BlinkUtils.unblink()
                            return@handler
                        }
                    }
                }
            }
        }

        // Check for lagback
        if (resetOnlagBack && packet is S08PacketPlayerPosLook) {
            shouldResetTimer()

            if (shouldReset) {
                if (chatDebug) {
                    chat("Lagback Received | Timer Reset")
                }
                if (notificationDebug) {
                    hud.addNotification(Notification("Lagback Received | Timer Reset", "!!!", Type.INFO, 100))
                }

                shouldReset = false
            }
        }

        // Check for knockback
        if (resetOnKnockback && packet is S12PacketEntityVelocity && mc.thePlayer?.entityId == packet.entityID) {
            shouldResetTimer()

            if (shouldReset) {
                if (chatDebug) {
                    chat("Knockback Received | Timer Reset")
                }
                if (notificationDebug) {
                    hud.addNotification(Notification("Knockback Received | Timer Reset", "!!!", Type.INFO, 100))
                }

                shouldReset = false
            }
        }
    }

    /**
     * HUD Tag
     */
    override val tag
        get() = timerBoostMode
}
