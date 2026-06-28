/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.center
import net.ccbluex.liquidbounce.utils.extensions.currPos
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToBox
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.getPing
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.lastTickPos
import net.ccbluex.liquidbounce.utils.extensions.minus
import net.ccbluex.liquidbounce.utils.extensions.offset
import net.ccbluex.liquidbounce.utils.extensions.plus
import net.ccbluex.liquidbounce.utils.extensions.times
import net.ccbluex.liquidbounce.utils.extensions.Vec3_ZERO
import net.ccbluex.liquidbounce.utils.extensions.isLookingOn
import net.ccbluex.liquidbounce.utils.extensions.toTicks
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.math.geometry.Ray
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.distance
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.utils.simulation.PredictFeature
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max

/**
 * HitSelect — Latency-aware click filter that reduces average CPS while preserving DPS.
 * Works with AutoClicker, legitimate clicking, or external clicker programs.
 *
 * @author itsakc-me
 */
object HitSelect : Module("HitSelect", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    // ────────────────────────────────────────────────────────────────────────────
    // Settings
    // ────────────────────────────────────────────────────────────────────────────

    private val mode                by choices("Mode", arrayOf("Burst", "Criticals"), "Burst")
        .describe("Mode to use for click filtering.")
    private val fakeSwing           by choices("FakeSwing", arrayOf("Off", "Client", "Server"), "Off")
        .describe("Whether to fake swing animations for cancelled clicks.")
    private val useServerAttackTime by boolean("UseServerAttackTime", false)
        .describe("Whether to use the server's attack cooldown instead of prediction.")
    private val pingCompensation    by boolean("PingCompensation", true) { !useServerAttackTime }
        .describe("Whether to predict filtering based on the player ping.")
    private val extraPingBuffer     by int("PingBuffer", 0, 0..5) { !useServerAttackTime }
        .describe("Extra ticks to add to the filtering prediction. Or compensate instead when ping compensation is disabled.")
    private val burstCount          by int("BurstCount", 1, 1..10)
        .describe("Number of consecutive clicks to allow through when a click is predicted to deal damage.")
    private val pauseDuration       by int("PauseDuration", 10, 0..20)
        .describe("Number of ticks to wait after a successful hit before allowing the next hit.")
    private val waitForFirstHit     by boolean("WaitForFirstHit", false)
        .describe("Whether to hold all attacks until the player is hit first.")

    // ── Burst ─────────────────────────────────────────────────────────────────
    private val hitLaterInTrades by int("HitLaterInTrades", 0, 0..500) { mode == "Burst" }
        .describe("Delay the next attack for this many milliseconds after being hit in a trade.")

    // ── Criticals ─────────────────────────────────────────────────────────────
    private val predictCritWindow      by boolean("PredictCritWindow", true)       { mode == "Criticals" }
        .describe("Whether to predict the critical window instead of static check.")
    private val disableDuringKnockback by boolean("DisableDuringKnockback", false) { mode == "Criticals" }
        .describe("Whether to disable filtering while the player is in knockback.")

    // ── Cancel rates ──────────────────────────────────────────────────────────
    private val cancelRate        by int("CancelRate", 100, 0..100)
        .describe("Percentage chance to cancel a click that is predicted to deal no damage.")
    private val missedSwingCancel by int("MissedSwingCancel", 100, 0..100)
        .describe("Percentage chance to cancel a click that is missed.")

    // ── Miss guard ────────────────────────────────────────────────────────────
    private val missGuard  by boolean("MissGuard", true)
        .describe("Whether to cancel clicks that are predicted to miss due to knockback.")
    private val missBuffer by float("MissBuffer", 0.15f, 0f..0.5f) { missGuard }
        .describe("Extra reach buffer to allow for missed clicks due to knockback.")

    // ── Click Prediction ──────────────────────────────────────────────────────
    private val clickPredMode         by choices("ClickPredMode", arrayOf("Static", "Prediction", "Adaptive", "Smart", "Semi", "Angular", "Strict"), "Smart")
        .describe("Mode to use for 'will hit land actually' prediction.")
    private val clickPredRange        by float("ClickPredRange", 3.2f, 0.5f..6.0f)          { clickPredMode != "Static" }
        .describe("Reach distance to use for click prediction.")
    private val angularTolerance      by float("AngularTolerance", 3.0f, 0.5f..15.0f)       { clickPredMode == "Angular" }
        .describe("Extra degrees of tolerance to allow for angular prediction.")
    private val adaptiveBaseLevel     by int("AdaptiveBase", 50, 0..100)                    { clickPredMode == "Adaptive" }
        .describe("Base level of adaptive prediction. Higher values mean more aggressive prediction.")
    private val semiVelocityThreshold by float("SemiVelocityThreshold", 0.06f, 0.01f..0.3f) { clickPredMode == "Semi" }
        .describe("Velocity threshold to use for semi prediction. Entities moving slower than this are always considered hittable.")

    // ────────────────────────────────────────────────────────────────────────────
    // EntitySnapshot — per-entity ring-buffer
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Records per-entity combat state for the last 24 ticks.
     */
    private class EntitySnapshot {

        private val hurtTimeRing   = IntArray(24)
        private val pos            = arrayOfNulls<Vec3>(10)
        private val mot            = arrayOfNulls<Vec3>(10)
        private val del            = arrayOfNulls<Vec3>(10)
        private val kbAbsorbedRing = BooleanArray(10) { true }

        private var head = 0
        var count        = 0; private set

        var lastObservedHT       = 0;  private set
        var knockbackTimestampMs = -1L; private set
        var swingStartedThisTick = false; private set
        private var prevSwingProgress = 0f
        var lastKbAbsorbed   = true; private set
        var kbPendingTicks   = 0;    private set
        var avgSpeedEma      = 0.0; private set
        var speedVarianceEma = 0.0; private set

        fun record(e: EntityLivingBase) {
            val i          = head % 10
            val currentPos = e.currPos
            val currentMot = Vec3(e.motionX, 0.0, e.motionZ)
            val tickDelta  = currentPos - e.lastTickPos

            val delta = Vec3(
                tickDelta.xCoord.coerceIn(-4.0, 4.0),
                0.0,
                tickDelta.zCoord.coerceIn(-4.0, 4.0)
            )

            del[i] = delta
            pos[i] = currentPos
            mot[i] = currentMot
            hurtTimeRing[head % 24] = e.hurtResistantTime

            val isKbFrame = e.hurtResistantTime > lastObservedHT + 5 && lastObservedHT <= 3
            if (isKbFrame) {
                knockbackTimestampMs = System.currentTimeMillis()
                val impulseMag = horizontalLength(currentMot)
                val deltaMag   = horizontalLength(delta)
                val absorbed   = impulseMag < 0.02 || deltaMag >= impulseMag * 0.30
                kbAbsorbedRing[i] = absorbed
                lastKbAbsorbed    = absorbed
                kbPendingTicks    = if (absorbed) 0 else 1
            } else {
                kbAbsorbedRing[i] = true
                if (kbPendingTicks > 0) {
                    kbPendingTicks++
                    if (kbPendingTicks >= 4) { kbPendingTicks = 0; lastKbAbsorbed = true }
                }
            }

            lastObservedHT       = e.hurtResistantTime
            swingStartedThisTick = e.swingProgress > 0.06f && prevSwingProgress <= 0.06f
            prevSwingProgress    = e.swingProgress

            val speed = horizontalLength(delta)
            val diff  = speed - avgSpeedEma
            avgSpeedEma     += diff * 0.15
            speedVarianceEma = speedVarianceEma * 0.85 + diff * diff * 0.15

            head++
            if (count < 24) count++
        }

        fun predictHurtTime(ticks: Int): Int =
            max(0, hurtTimeRing[(head - 1 + 24) % 24] - ticks)

        fun predictPosition(ticks: Int): Vec3 {
            val i = (head - 1 + 10) % 10
            var position = pos[i] ?: Vec3_ZERO
            var velocity: Vec3

            when {
                recentlyKnockedBack && !lastKbAbsorbed && kbPendingTicks > 0 -> {
                    val pending = kbPendingTicks.toDouble()
                    val motion = mot[i] ?: Vec3_ZERO
                    position += (motion * (pending * 0.5))
                    velocity = motion * 0.65
                }
                recentlyKnockedBack && lastKbAbsorbed -> {
                    velocity = mot[i] ?: Vec3_ZERO
                }
                else -> velocity = del[i] ?: Vec3_ZERO
            }

            return positionPredictor
                .predict(HorizontalPredictionState(position, velocity), ticks.coerceIn(0, positionPredictor.maximumTicks))
                .finalState
                .position
        }

        fun latestSpeed(): Double {
            val idx = (head - 1 + 10) % 10
            return horizontalLength(del[idx] ?: Vec3_ZERO)
        }

        val recentlyKnockedBack: Boolean
            get() = knockbackTimestampMs >= 0 &&
                    System.currentTimeMillis() - knockbackTimestampMs < 200L

        private data class HorizontalPredictionState(val position: Vec3, val velocity: Vec3)

        companion object {
            private val positionPredictor = PredictFeature(16) { state: HorizontalPredictionState ->
                HorizontalPredictionState(
                    state.position + state.velocity,
                    Vec3(state.velocity.xCoord * 0.91, 0.0, state.velocity.zCoord * 0.91)
                )
            }

            private fun horizontalLength(vec: Vec3): Double = distance(0.0, 0.0, vec.xCoord, vec.zCoord)
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Module state
    // ────────────────────────────────────────────────────────────────────────────

    private val snapshots = HashMap<Int, EntitySnapshot>()
    private val ping: Int
        get() = mc.thePlayer?.run { getPing().toTicks() } ?: 1

    private val hitLaterTimer    = MSTimer()
    private var burstDamageHits  = 0
    private var isBursting       = false
    private var isHitLaterActive = false
    private var gotHitFirst      = false
    private var lastPlayerHT     = 0
    private var currentTargetId  = -1

    // ────────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ────────────────────────────────────────────────────────────────────────────

    override val tag
        get() = mode

    override fun onDisable() {
        snapshots.clear()
        hitLaterTimer.reset()
        burstDamageHits           = 0
        isHitLaterActive          = false
        gotHitFirst               = false
        lastPlayerHT              = 0
        currentTargetId           = -1
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Tick update — snapshot recording and cross-tick detection
    // ────────────────────────────────────────────────────────────────────────────

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world  = mc.theWorld  ?: return@handler

        // Detect when the local player takes a hit (rising edge on hurtResistantTime)
        val curPlayerHT = player.hurtResistantTime
        if (curPlayerHT > lastPlayerHT && lastPlayerHT == 0) {
            gotHitFirst = true
            // HitLaterInTrades: arm delay timer on each hit while in Burst mode
            if (mode == "Burst" && hitLaterInTrades > 0) {
                isHitLaterActive = true
                hitLaterTimer.reset()
            }
        }
        lastPlayerHT = curPlayerHT

        // Record snapshots for all nearby relevant entities
        for (obj in world.loadedEntityList) {
            val entity = obj as? EntityLivingBase ?: continue
            if (entity == player || entity.isDead || isBot(entity))  continue
            if (entity.getDistanceToEntityBox(player) > 12.0) continue
            snapshots.getOrPut(entity.entityId) { EntitySnapshot() }.record(entity)
        }

        val iter = snapshots.iterator()
        while (iter.hasNext()) {
            val (id, _) = iter.next()
            val e = world.getEntityByID(id) as? EntityLivingBase
            if (e == null || e.isDead) { iter.remove(); continue }
            if (player.getDistanceToEntityBox(e) > 12.0) iter.remove()
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Public API — called from mixin
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Primary click gate.
     *
     * Static mode: vanilla objectMouseOver is fully authoritative.
     *
     * All prediction modes:
     *   • The entity from objectMouseOver is a candidate hint, not a confirmed hit.
     *   • Prediction validates it (server rotation + predicted hitbox).
     *   • If rejected or absent: [scanWithMode] scans all tracked entities using
     *     the same active mode, so a vanilla MISS can be upgraded for any mode.
     */
    @JvmStatic
    fun shouldCancelClick(objectMouseOver: MovingObjectPosition?): Boolean {
        if (!handleEvents() || objectMouseOver == null) return false

        val hint = if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
            objectMouseOver.entityHit as? EntityLivingBase
        else null

        if (clickPredMode == "Static") {
            return if (hint == null) shouldCancelAirClick()
                   else shouldCancelEntityClick(hint)
        }

        val target = hint?.takeIf { predictedClickWillHit(it) } ?: scanWithMode()
        return shouldCancelEntityClick(target ?: return shouldCancelAirClick())
    }

    fun shouldCancelEntityClick(target: EntityLivingBase): Boolean {
        val player = mc.thePlayer ?: return true
        handleTargetSwitch(target)
        val snap = snapshots[target.entityId]

        if (missGuard && snap != null && willMissDueToKnockback(target, snap)) {
            performFakeSwing(); return true
        }

        val cancel = when (mode) {
            "Burst"      -> decideBurst(target, player, snap)
            "Criticals"  -> decideCriticals(target, player, snap)
            else         -> false
        }

        if (cancel) performFakeSwing()
        return cancel
    }

    fun shouldCancelAirClick(): Boolean {
        if (missedSwingCancel == 0) return false
        if (nextInt(0, 100) >= missedSwingCancel) return false
        performFakeSwing()
        return true
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Target resolution
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Scans all tracked entities with the active prediction mode.
     *
     * All modes participate — NOT just Smart. Semi uses [clickPredPrediction]
     * instead of its speed-threshold shortcut because the shortcut only makes
     * sense when vanilla has already confirmed an entity is under the crosshair.
     * Candidates are sorted by distance; the nearest confirmed hit is returned.
     */
    private fun scanWithMode(): EntityLivingBase? {
        val world  = mc.theWorld  ?: return null
        val player = mc.thePlayer ?: return null

        return snapshots.keys
            .mapNotNull { world.getEntityByID(it) as? EntityLivingBase }
            .filter     { !it.isDead && it != player }
            .sortedBy   { player.getDistanceToEntityBox(it) }
            .firstOrNull { entity ->
                if (clickPredMode == "Semi") clickPredPrediction(entity)
                else predictedClickWillHit(entity)
            }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Click prediction — dispatch + per-mode implementations
    // ────────────────────────────────────────────────────────────────────────────

    private fun predictedClickWillHit(entity: EntityLivingBase): Boolean =
        when (clickPredMode) {
            "Prediction" -> clickPredPrediction(entity)
            "Adaptive"   -> clickPredAdaptive(entity)
            "Smart"      -> clickPredSmart(entity)
            "Semi"       -> clickPredSemi(entity)
            "Angular"    -> clickPredAngular(entity)
            "Strict"     -> clickPredStrict(entity)
            else         -> true
        }

    private fun positionPredictionHelper(entity: EntityLivingBase, reach: Double): Boolean {
        val renderView = mc.renderViewEntity ?: return true
        val snap       = snapshots[entity.entityId] ?: return true
        return isAimIntersecting(renderView.eyes, predictedHitBox(entity, snap, ping), reach)
    }

    private fun clickPredPrediction(entity: EntityLivingBase): Boolean =
        positionPredictionHelper(entity, clickPredRange.toDouble())

    private fun clickPredAdaptive(entity: EntityLivingBase): Boolean {
        val snap = snapshots[entity.entityId] ?: return true
        val variancePenalty = (snap.speedVarianceEma * 20.0).coerceIn(0.0, 0.5)
        val baselineShift   = (adaptiveBaseLevel - 50) / 100.0 * 0.25 * clickPredRange
        val adaptiveReach   = (clickPredRange * (1.0 - variancePenalty) + baselineShift).coerceAtLeast(0.5)
        return positionPredictionHelper(entity, adaptiveReach)
    }

    private fun clickPredSmart(entity: EntityLivingBase): Boolean {
        if (clickPredPrediction(entity)) return true
        if (Backtrack.mode == "Legacy") {
            var hit = false
            Backtrack.loopThroughBacktrackData(entity) {
                val rv = mc.renderViewEntity ?: return@loopThroughBacktrackData false
                if (isAimIntersecting(rv.eyes, entity.hitBox)) { hit = true; true } else false
            }
            if (hit) return true
        }
        return false
    }

    private fun clickPredSemi(entity: EntityLivingBase): Boolean {
        val snap = snapshots[entity.entityId] ?: return true
        return if (snap.latestSpeed() < semiVelocityThreshold) true
        else clickPredPrediction(entity)
    }

    private fun clickPredAngular(entity: EntityLivingBase): Boolean {
        val renderView      = mc.renderViewEntity ?: return true
        val snap            = snapshots[entity.entityId] ?: return true
        val predictedCenter = predictedHitBox(entity, snap, ping).center
        val eyePos          = renderView.eyes
        val lookVec         = getVectorForRotation(serverRotation)
        val cx              = predictedCenter.xCoord - eyePos.xCoord
        val cy              = predictedCenter.yCoord - eyePos.yCoord
        val cz              = predictedCenter.zCoord - eyePos.zCoord
        val dist            = eyePos.distanceTo(predictedCenter)

        if (dist < 0.001) return true
        val cosAngle = ((lookVec.xCoord * cx + lookVec.yCoord * cy + lookVec.zCoord * cz) / dist).coerceIn(-1.0, 1.0)
        val angleDeg  = Math.toDegrees(acos(cosAngle))
        val halfAngDeg = Math.toDegrees(atan2(entity.width / 2.0, dist))
        return angleDeg <= halfAngDeg + angularTolerance
    }

    private fun clickPredStrict(entity: EntityLivingBase): Boolean {
        val renderView = mc.renderViewEntity ?: return true
        val snap       = snapshots[entity.entityId] ?: return true
        val eyePos     = renderView.eyes
        if (!isAimIntersecting(eyePos, entity.hitBox)) return false
        return isAimIntersecting(eyePos, predictedHitBox(entity, snap, ping))
    }

    private fun isAimIntersecting(eyePos: Vec3, bb: AxisAlignedBB, reach: Double = clickPredRange.toDouble()): Boolean {
        val lookVec = getVectorForRotation(serverRotation)
        return bb.isVecInside(eyePos) || Ray(eyePos, lookVec).intersectionDistance(bb)?.let { it <= reach } == true
    }

    private fun predictedHitBox(entity: EntityLivingBase, snap: EntitySnapshot, ticks: Int): AxisAlignedBB =
        entity.hitBox.offset(snap.predictPosition(ticks) - entity.currPos)

    // ────────────────────────────────────────────────────────────────────────────
    // Mode: Burst — true per-click immunity prediction
    // ────────────────────────────────────────────────────────────────────────────

    private fun decideBurst(target: EntityLivingBase, player: EntityPlayerSP, snap: EntitySnapshot?): Boolean {
        // Gate 1: WaitForFirstHit — hold all attacks until player is hit first
        if (waitForFirstHit && isWorthWaiting(target, player)) return true

        // Gate 2: HitLaterInTrades — delay next attack after being hit in a trade
        if (isHitLaterActive) {
            if (!hitLaterTimer.hasTimePassed(hitLaterInTrades.toLong())) return true
            isHitLaterActive = false
        }

        return evaluateBurstClick(target, snap)
    }

    /**
     * Core per-click evaluation for Burst mode.
     *
     * Checks whether THIS click is predicted to deal damage.
     *
     *   Will deal damage:
     *     → ALWAYS let through. Count toward burstCount.
     *
     *   Will NOT deal damage:
     *     → Apply cancelRate probability.
     *         100% = always cancel (maximum filtering, fastest movement)
     *         50%  = probabilistic pass-through (more legit CPS appearance)
     *         0%   = never cancel (module effectively disabled in combat)
     */
    private fun evaluateBurstClick(target: EntityLivingBase, snap: EntitySnapshot?): Boolean {
        val canDamage =
            if (useServerAttackTime || snap == null) canTargetTakeDamageSimple(target)
            else canHitWithPrediction(target, snap)

        return if (canDamage || isBursting) {
            // ── Damage hit — always let through ──────────────────────────────
            // Count toward burst
            burstDamageHits++
            isBursting = true
            if (burstDamageHits >= burstCount) {
                isBursting = false
                burstDamageHits = 0
            }
            false  // don't cancel
        } else nextInt(0, 100) < cancelRate // Apply cancel rate
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Mode: Criticals
    // ────────────────────────────────────────────────────────────────────────────

    private fun decideCriticals(target: EntityLivingBase, player: EntityPlayerSP, snap: EntitySnapshot?): Boolean {
        if (disableDuringKnockback && player.hurtResistantTime > 0) return false
        if (waitForFirstHit && isWorthWaiting(target, player)) return true

        val inCrit = if (predictCritWindow)
            willBeInCriticalPosition(player, effectivePingTicks())
        else
            isInCriticalPosition(player)

        if (inCrit) return false

        return evaluateBurstClick(target, snap) // Not in critical position, so evaluate burst logic
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Shared prediction helpers
    // ────────────────────────────────────────────────────────────────────────────

    private fun canHitWithPrediction(target: EntityLivingBase, snap: EntitySnapshot): Boolean =
        snap.predictHurtTime(effectivePingTicks()) <= max(0, target.maxHurtResistantTime - pauseDuration)

    private fun canTargetTakeDamageSimple(target: EntityLivingBase): Boolean =
        target.hurtResistantTime <= max(0, target.maxHurtResistantTime - pauseDuration)

    private fun willMissDueToKnockback(target: EntityLivingBase, snap: EntitySnapshot): Boolean {
        if (!snap.recentlyKnockedBack) return false
        val player    = mc.thePlayer ?: return false
        val reach     = 3.0 + missBuffer
        val predicted = predictedHitBox(target, snap, max(1, effectivePingTicks() / 2))
        return player.getDistanceToBox(predicted) > reach
    }

    private fun willBeInCriticalPosition(player: EntityPlayerSP, ticks: Int): Boolean {
        if (player.isInLiquid || player.isRiding || player.isOnLadder || player.onGround) return false
        var motionY  = player.motionY
        var grounded = false
        repeat(ticks) {
            if (grounded) return@repeat
            motionY = (motionY - 0.08) * 0.98
            if (abs(motionY) < 0.003) grounded = true
        }
        return !grounded && motionY < -0.005
    }

    private fun isInCriticalPosition(player: EntityPlayerSP): Boolean =
        player.motionY < 0 && !player.onGround && player.fallDistance > 0 &&
                !player.isOnLadder && !player.isInLiquid && !player.isRiding

    private fun effectivePingTicks(): Int =
        extraPingBuffer.takeIf { !pingCompensation } ?: (ping + extraPingBuffer)

    private fun isWorthWaiting(target: EntityLivingBase, player: EntityPlayerSP): Boolean =
        if (!target.isLookingOn(player, 45.0)) false
        else !gotHitFirst

    // ────────────────────────────────────────────────────────────────────────────
    // State helpers
    // ────────────────────────────────────────────────────────────────────────────

    private fun handleTargetSwitch(target: EntityLivingBase) {
        if (target.entityId == currentTargetId) return
        currentTargetId  = target.entityId
        gotHitFirst      = false
        isHitLaterActive = false
        burstDamageHits  = 0 // Reset burst damage counter for new target.
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Fake swing
    // ────────────────────────────────────────────────────────────────────────────

    private fun performFakeSwing() {
        val player = mc.thePlayer ?: return
        when (fakeSwing) {
            "Client" -> player.animateSwingItem()
            "Server" -> sendPacket(C0APacketAnimation())
        }
    }

    private fun EntityPlayerSP.animateSwingItem() {
        val armSwingAnimationEnd = when {
            isPotionActive(Potion.digSpeed)    -> 6 - (1 + getActivePotionEffect(Potion.digSpeed).amplifier)
            isPotionActive(Potion.digSlowdown) -> 6 + (1 + getActivePotionEffect(Potion.digSlowdown).amplifier) * 2
            else -> 6
        }
        if (heldItem == null || heldItem.item == null || !heldItem.item.onEntitySwing(this, heldItem)) {
            if (!isSwingInProgress || swingProgressInt >= armSwingAnimationEnd / 2 || swingProgressInt < 0) {
                swingProgressInt  = -1
                isSwingInProgress = true
            }
        }
    }
}
