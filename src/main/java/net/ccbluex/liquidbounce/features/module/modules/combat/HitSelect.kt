/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.currPos
import net.ccbluex.liquidbounce.utils.extensions.eyes
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
import net.ccbluex.liquidbounce.utils.extensions.getNearestPointBB
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
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sqrt

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
        .describe("Whether to use the server information instead of prediction.")
    private val pingCompensation    by boolean("PingCompensation", true) { !useServerAttackTime }
        .describe("Whether to predict based on the player ping.")
    private val extraPingBuffer     by int("PingBuffer", 0, 0..5) { !useServerAttackTime }
        .describe("Extra ticks to add to the prediction. Or compensate instead when ping compensation is disabled.")
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
    private val disableDuringKnockback by boolean("DisableDuringKnockback", false) { mode == "Criticals" }
        .describe("Whether to disable filtering while the player is in knockback.")

    // ── Cancel rates ──────────────────────────────────────────────────────────
    private val cancelRate        by int("CancelRate", 100, 0..100)
        .describe("Percentage chance to cancel a click that is predicted to deal no damage.")
    private val missedSwingCancel by int("MissedSwingCancel", 100, 0..100)
        .describe("Percentage chance to cancel a click that is missed.")

    // ── Miss guard ────────────────────────────────────────────────────────────
    private val missGuard  by boolean("MissGuard", true)                       { !useServerAttackTime }
        .describe("Whether to cancel clicks that are predicted to miss due to knockback.")
    private val missBuffer by float("MissBuffer", 0.15f, 0f..0.5f) { !useServerAttackTime && missGuard }
        .describe("Extra reach buffer to allow for missed clicks due to knockback.")

    // ── Click Prediction ──────────────────────────────────────────────────────
    private val clickPredMode         by choices("ClickPredMode", arrayOf("Static", "Prediction", "Adaptive", "Smart", "Semi", "Angular", "Strict"), "Smart") { !useServerAttackTime }
        .describe("Mode to use for 'will hit land actually' prediction.")
    private val clickPredRange        by float("ClickPredRange", 3.2f, 0.5f..6.0f)          { !useServerAttackTime && clickPredMode != "Static" }
        .describe("Reach distance to use for click prediction.")
    private val angularTolerance      by float("AngularTolerance", 3.0f, 0.5f..15.0f)       { !useServerAttackTime && clickPredMode == "Angular" }
        .describe("Extra degrees of tolerance to allow for angular prediction.")
    private val adaptiveBaseLevel     by int("AdaptiveBase", 50, 0..100)                    { !useServerAttackTime && clickPredMode == "Adaptive" }
        .describe("Base level of adaptive prediction. Higher values mean more aggressive prediction.")
    private val semiVelocityThreshold by float("SemiVelocityThreshold", 0.06f, 0.01f..0.3f) { !useServerAttackTime && clickPredMode == "Semi" }
        .describe("Velocity threshold to use for semi prediction. Entities moving slower than this are always considered hittable.")

    private val generalGroup = Configurable("General")
    private val modesGroup = Configurable("Modes")
    private val cancelRatesGroup = Configurable("CancelRates")
    private val missGuardGroup = Configurable("MissGuard")
    private val clickPredictionGroup = Configurable("ClickPrediction")

    init {
        moveValues(generalGroup,
            "Mode", "FakeSwing", "UseServerAttackTime", "PingCompensation", "PingBuffer",
            "BurstCount", "PauseDuration", "WaitForFirstHit")

        moveValues(modesGroup, "HitLaterInTrades", "DisableDuringKnockback")

        moveValues(cancelRatesGroup, "CancelRate", "MissedSwingCancel")

        moveValues(missGuardGroup, "MissGuard", "MissBuffer")

        moveValues(clickPredictionGroup,
            "ClickPredMode", "ClickPredRange", "AngularTolerance", "AdaptiveBase",
            "SemiVelocityThreshold")

        addValues(listOf(
            generalGroup, modesGroup, cancelRatesGroup, missGuardGroup, clickPredictionGroup
        ))
    }
    // ────────────────────────────────────────────────────────────────────────────
    // EntitySnapshot — per-entity ring-buffer
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Records per-entity combat state, tick-driven with no ring buffers.
     */
    private class EntitySnapshot {

        private var lastPos    = Vec3_ZERO
        private var lastMotion = Vec3_ZERO
        private var lastDelta  = Vec3_ZERO

        var lastObservedHT       = 0;     private set
        var swingStartedThisTick = false; private set
        private var prevSwingProgress = 0f

        var lastKbAbsorbed   = true; private set
        var remainingKbTicks = 0;    private set

        var avgSpeedEma      = 0.0; private set
        var speedVarianceEma = 0.0; private set
        var lastSpeed        = 0.0; private set

        var hasData = false; private set

        // Tick counter — never wall-clock time
        private var ticksSinceKb = Int.MAX_VALUE / 2

        val recentlyKnockedBack: Boolean
            get() = ticksSinceKb <= 3

        fun record(e: EntityLivingBase) {
            val currentPos    = e.currPos
            val rawDelta      = currentPos - e.lastTickPos
            val currentMotion =
                if (recentlyKnockedBack)
                    Vec3(e.motionX, 0.0, e.motionZ)
                else
                    lastMotion

            // Teleport / server correction: reset instead of clamping.
            // Clamping silently manufactures fake motion; detecting and zeroing
            // is always preferable.
            val hDelta = Vec3(rawDelta.xCoord, 0.0, rawDelta.zCoord)
            lastDelta = if (horizontalLength(hDelta) > 3.0) Vec3_ZERO else hDelta

            // KB detection: vanilla 1.8.9 sets hurtResistantTime to exactly 20
            // immediately on a successful hit, so >= 19 is deterministic and
            // immune to skipped client ticks.
            val kbStarted = e.hurtResistantTime >= 19 && lastObservedHT <= 3
            if (kbStarted) {
                ticksSinceKb = 0
                val impulseMag = horizontalLength(currentMotion)
                val deltaMag   = horizontalLength(lastDelta)
                val absorbed   = impulseMag < 0.02 || deltaMag >= impulseMag * 0.30
                lastKbAbsorbed   = absorbed
                remainingKbTicks = if (absorbed) 0 else 3
            } else {
                if (remainingKbTicks > 0) {
                    remainingKbTicks--
                    if (remainingKbTicks == 0) lastKbAbsorbed = true
                }
            }

            // Increment after potential reset so the KB frame itself counts as tick 1.
            if (ticksSinceKb != Int.MAX_VALUE) ticksSinceKb++

            lastObservedHT       = e.hurtResistantTime
            swingStartedThisTick = e.swingProgress > 0.06f && prevSwingProgress <= 0.06f
            prevSwingProgress    = e.swingProgress

            val speed = horizontalLength(lastDelta)
            val diff  = speed - avgSpeedEma
            avgSpeedEma      += diff * 0.15
            speedVarianceEma  = speedVarianceEma * 0.85 + diff * diff * 0.15

            lastPos    = currentPos
            lastMotion = currentMotion
            lastSpeed  = speed
            hasData    = true
        }

        fun predictPosition(ticks: Int): Vec3 {
            var position = lastPos
            val velocity: Vec3

            when {
                // Active knockback that wasn't absorbed: project forward with the
                // remaining KB impulse, then hand lastMotion to the predictor and
                // let it decay naturally via the 0.91 multiplier.
                recentlyKnockedBack && !lastKbAbsorbed && remainingKbTicks > 0 -> {
                    position += lastMotion * (remainingKbTicks.toDouble() * 0.5)
                    velocity  = lastMotion
                }
                // KB was absorbed (strafed / blocked): motion still reflects the
                // player's intent, so use it directly.
                recentlyKnockedBack && lastKbAbsorbed -> {
                    velocity = lastMotion
                }
                // Default: actual displacement is the most reliable predictor
                // because it already accounts for collisions, friction, webs,
                // water, and anti-cheat corrections.
                else -> velocity = lastDelta
            }

            return positionPredictor
                .predict(
                    HorizontalPredictionState(position, velocity),
                    ticks.coerceIn(0, positionPredictor.maximumTicks)
                )
                .finalState
                .position
        }

        fun latestSpeed(): Double = lastSpeed

        private data class HorizontalPredictionState(val position: Vec3, val velocity: Vec3)

        companion object {
            // 0.91 models inertia rather than simulating full vanilla movement
            // (which multiplies by slipperiness * 0.91 then re-adds acceleration).
            // For extrapolation purposes this is intentional.
            private val positionPredictor = PredictFeature(16) { state: HorizontalPredictionState ->
                HorizontalPredictionState(
                    state.position + state.velocity,
                    Vec3(state.velocity.xCoord * 0.91, 0.0, state.velocity.zCoord * 0.91)
                )
            }

            private fun horizontalLength(vec: Vec3): Double =
                distance(0.0, 0.0, vec.xCoord, vec.zCoord)
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Module state
    // ────────────────────────────────────────────────────────────────────────────

    private val snapshots = HashMap<EntityLivingBase, EntitySnapshot>()
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
            if (entity.getDistanceToEntityBox(player) > 6) continue
            snapshots.getOrPut(entity) { EntitySnapshot() }.record(entity)
        }

        snapshots.entries.removeIf { (entity, _) ->
            entity.isDead || player.getDistanceToEntityBox(entity) > 6
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

        if (useServerAttackTime || clickPredMode == "Static") {
            return if (hint == null) shouldCancelAirClick()
                   else shouldCancelEntityClick(hint)
        }

        val target = hint?.takeIf { predictedClickWillHit(it) } ?: scanWithMode()
        return shouldCancelEntityClick(target ?: return shouldCancelAirClick())
    }

    fun shouldCancelEntityClick(target: EntityLivingBase): Boolean {
        val player = mc.thePlayer ?: return true
        handleTargetSwitch(target)

        if (!useServerAttackTime && missGuard && willMissDueToKnockback(target)) {
            performFakeSwing(); return true
        }

        val cancel = when (mode) {
            "Burst"      -> decideBurst(target, player)
            "Criticals"  -> decideCriticals(target, player)
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
     * Candidates are sorted by distance; the confirmed hit is chosen with a
     * stable preference so consecutive clicks agree on one entity: the aura's
     * active target first, then the previously chosen target, then the nearest.
     */
    private fun scanWithMode(): EntityLivingBase? {
        val player = mc.thePlayer ?: return null

        val confirmed = snapshots.keys
            .filter    { !it.isDead && it != player }
            .sortedBy  { player.getDistanceToEntityBox(it) }
            .filter    { entity ->
                if (clickPredMode == "Semi") clickPredPrediction(entity)
                else predictedClickWillHit(entity)
            }

        val auraTarget = KillAura.target?.takeIf { KillAura.handleEvents() }

        return confirmed.firstOrNull { it == auraTarget }
            ?: confirmed.firstOrNull { it.entityId == currentTargetId }
            ?: confirmed.firstOrNull()
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
        val snap = snapshots[entity] ?: return true
        if (!snap.hasData) return true
        return isAimIntersecting(renderView.eyes, predictedHitBox(entity, snap, effectivePingTicks()), reach)
    }

    private fun clickPredPrediction(entity: EntityLivingBase): Boolean =
        positionPredictionHelper(entity, clickPredRange.toDouble())

    private fun clickPredAdaptive(entity: EntityLivingBase): Boolean {
        val snap = snapshots[entity] ?: return true
        if (!snap.hasData) return true
        val variancePenalty = sqrt(snap.speedVarianceEma).coerceIn(0.0, 0.25)
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
        val snap = snapshots[entity] ?: return true
        if (!snap.hasData) return true
        return if (snap.latestSpeed() < semiVelocityThreshold) true
        else clickPredPrediction(entity)
    }

    private fun clickPredAngular(entity: EntityLivingBase): Boolean {
        val renderView      = mc.renderViewEntity ?: return true
        val snap = snapshots[entity] ?: return true
        if (!snap.hasData) return true
        val predictedHitbox = predictedHitBox(entity, snap, effectivePingTicks())
        val eyePos          = renderView.eyes
        val nearestPoint    = getNearestPointBB(eyePos, predictedHitbox)
        val lookVec         = getVectorForRotation(serverRotation)
        val cx              = nearestPoint.xCoord - eyePos.xCoord
        val cy              = nearestPoint.yCoord - eyePos.yCoord
        val cz              = nearestPoint.zCoord - eyePos.zCoord
        val dist            = eyePos.distanceTo(nearestPoint)

        if (dist < 0.001) return true
        val cosAngle = ((lookVec.xCoord * cx + lookVec.yCoord * cy + lookVec.zCoord * cz) / dist).coerceIn(-1.0, 1.0)
        val angleDeg  = Math.toDegrees(acos(cosAngle))
        val halfAngDeg = Math.toDegrees(atan2(entity.width / 2.0, dist))
        return angleDeg <= halfAngDeg + angularTolerance
    }

    private fun clickPredStrict(entity: EntityLivingBase): Boolean {
        val renderView = mc.renderViewEntity ?: return true
        val snap = snapshots[entity] ?: return true
        if (!snap.hasData) return true
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

    private fun decideBurst(target: EntityLivingBase, player: EntityPlayerSP): Boolean {
        // Gate 1: WaitForFirstHit — hold all attacks until player is hit first
        if (waitForFirstHit && isWorthWaiting(target, player)) return true

        // Gate 2: HitLaterInTrades — delay next attack after being hit in a trade
        if (isHitLaterActive) {
            if (!hitLaterTimer.hasTimePassed(hitLaterInTrades.toLong())) return true
            isHitLaterActive = false
        }

        return evaluateBurstClick(target)
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
    private fun evaluateBurstClick(target: EntityLivingBase): Boolean {
        val canDamage =
            if (useServerAttackTime) canTargetTakeDamageSimple(target)
            else canHitWithPrediction(target)

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

    private fun decideCriticals(target: EntityLivingBase, player: EntityPlayerSP): Boolean {
        if (disableDuringKnockback && player.hurtResistantTime > 0) return false
        if (waitForFirstHit && isWorthWaiting(target, player)) return true

        val inCrit = if (!useServerAttackTime)
            willBeInCriticalPosition(player, effectivePingTicks())
        else
            isInCriticalPosition(player)

        if (inCrit) return false

        return evaluateBurstClick(target) // Not in critical position, so evaluate burst logic
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Shared prediction helpers
    // ────────────────────────────────────────────────────────────────────────────

    private fun canHitWithPrediction(target: EntityLivingBase): Boolean =
        target.hurtResistantTime - effectivePingTicks() <= max(0, target.maxHurtResistantTime - pauseDuration)

    private fun canTargetTakeDamageSimple(target: EntityLivingBase): Boolean =
        target.hurtResistantTime <= max(0, target.maxHurtResistantTime - pauseDuration)

    private fun willMissDueToKnockback(target: EntityLivingBase): Boolean {
        val snap = snapshots[target] ?: return false
        if (!snap.hasData || !snap.recentlyKnockedBack) return false

        val player = mc.thePlayer ?: return false
        val ticks  = effectivePingTicks()

        val predictedTarget = predictedHitBox(target, snap, ticks)

        val predictedEyePos = player.eyes + Vec3(
            player.motionX * ticks,
            0.0,
            player.motionZ * ticks
        )

        return predictedEyePos.distanceTo(getNearestPointBB(predictedEyePos, predictedTarget)) >
                3.0 + missBuffer
    }

    private fun willBeInCriticalPosition(player: EntityPlayerSP, ticks: Int): Boolean {
        if (player.onGround ||
            player.isInLiquid ||
            player.isOnLadder ||
            player.isRiding)
            return false

        var motionY = player.motionY

        repeat(ticks) {
            motionY = (motionY - 0.08) * 0.98
            if (motionY < 0.0)
                return true
        }

        return false
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
