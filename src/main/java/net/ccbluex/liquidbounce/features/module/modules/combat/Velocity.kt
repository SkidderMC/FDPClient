/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.*
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.isOnGround
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils.runWithModifiedRaycastResult
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.sin

private val VELOCITY_MODES = arrayOf(
    "Simple", "AAC", "AACPush", "AACZero", "AACv4",
    "Reverse", "SmoothReverse", "Jump", "Glitch", "Legit",
    "GhostBlock", "Vulcan", "S32Packet", "MatrixSimple", "MatrixReduce", "MatrixReverse",
    "IntaveReduce", "Intave", "Delay", "Delayed", "Grim", "GrimC03", "Grim1.17", "GrimC07", "GrimDamage",
    "Hypixel", "HypixelAir", "HypixelBoost",
    "Click", "BlocksMC", "GrimVertical", "AttackReduce", "Spoof", "Tick", "AAC4Reduce", "AAC5Reduce",
    "AAC5.2.0", "AAC5.2.0Combat", "Cancel", "Minemen", "Phase", "SideStrafe", "Polar", "Sentinel"
)

/**
 * Velocity module - Modifies knockback taken
 *
 * Reduces or modifies the knockback you take from attacks and explosions.
 * Supports various modes for different anti-cheat systems.
 */
object Velocity : Module("Velocity", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    /**
     * OPTIONS
     */
    private val antiCheatValue = choices(
        "AntiCheat", arrayOf("Auto", "All", "NCP", "AAC", "Grim", "Vulcan", "Watchdog", "Verus", "Matrix", "Intave", "Spartan", "Polar"), "Auto"
    ).onChanged { refreshModeChoices(force = true) }
        .describe("Filter modes by a detected or explicitly selected anti-cheat family.")
    private val antiCheat by antiCheatValue

    private val modeValue = choices("Mode", VELOCITY_MODES.copyOf(), "Simple")
        .describe("Knockback method. Legacy modes outside the selected profile are not presented as bypasses.")
        .apply { onChanged { if (state) warnAboutMode(it) } }
    private val mode by modeValue

    private val horizontal by float("Horizontal", 0F, -1F..1F) { mode in arrayOf("Simple", "AAC", "Legit", "Tick") }
        .describe("Horizontal knockback multiplier.")
    private val vertical by float("Vertical", 0F, -1F..1F) { mode in arrayOf("Simple", "Legit", "Tick") }
        .describe("Vertical knockback multiplier.")
    private val onlyGround by boolean("OnlyGround", false)
        .describe("Only modify knockback while on the ground.")
    private val onlyCombat by boolean("OnlyCombat", false)
        .describe("Only modify knockback while in combat.")
    private val noFire by boolean("noFire", false)
        .describe("Disable knockback modification while burning.")
    private val respectSetbacks by boolean("RespectSetbacks", true)
        .describe("Take full knockback for a short window after a server correction so the reduction is not what triggered it.")
    private val setbackGrace by int("SetbackGrace", 350, 0..3000) { respectSetbacks }
        .describe("How long, in milliseconds, to accept full knockback after a correction.")
    private val overrideDirection by choices("OverrideDirection", arrayOf("None", "Hard", "Offset"), "None")
        .describe("Force knockback in a chosen direction.")
    private val overrideDirectionYaw by float("OverrideDirectionYaw", 0F, -180F..180F) { overrideDirection != "None" }
        .describe("Yaw used by the direction override.")

    // Reverse
    private val reverseStrength by float("ReverseStrength", 1F, 0.1F..1F) { mode == "Reverse" }
        .describe("Strength of the reverse knockback effect.")
    private val reverse2Strength by float("SmoothReverseStrength", 0.05F, 0.02F..0.1F) { mode == "SmoothReverse" }
        .describe("Strength of the smooth reverse effect.")

    private val onLook by boolean("onLook", false) { mode in arrayOf("Reverse", "SmoothReverse") }
        .describe("Only reverse while looking at the enemy.")
    private val range by float("Range", 3.0F, 1F..5.0F) {
        onLook && mode in arrayOf("Reverse", "SmoothReverse")
    }
    private val maxAngleDifference by float("MaxAngleDifference", 45.0f, 5.0f..90f) {
        onLook && mode in arrayOf("Reverse", "SmoothReverse")
    }

    // AAC Push
    private val aacPushXZReducer by float("AACPushXZReducer", 2F, 1F..3F) { mode == "AACPush" }
        .describe("Horizontal motion divisor for AACPush.")
    private val aacPushYReducer by boolean("AACPushYReducer", true) { mode == "AACPush" }
        .describe("Reduce vertical motion for AACPush.")

    // AAC v4
    private val aacv4MotionReducer by float("AACv4MotionReducer", 0.62F, 0F..1F) { mode == "AACv4" }
        .describe("Motion multiplier for AACv4.")
    private val aac4ReduceAmount by float("AAC4ReduceAmount", 0.62f, 0f..1f) { mode == "AAC4Reduce" }
        .describe("Motion multiplier for AAC4Reduce.")
    private val aac5ReduceAmount by float("AAC5ReduceAmount", 0.81f, 0f..1f) { mode == "AAC5Reduce" }
        .describe("Motion multiplier for AAC5Reduce.")

    // Legit
    private val legitDisableInAir by boolean("DisableInAir", true) { mode == "Legit" }
        .describe("Disable Legit mode while airborne.")

    // Chance
    private val chance by int("Chance", 100, 0..100) { mode == "Jump" || mode == "Legit" }
        .describe("Percent chance to apply the effect.")

    // Jump
    private val jumpCooldownMode by choices("JumpCooldownMode", arrayOf("Ticks", "ReceivedHits"), "Ticks")
    { mode == "Jump" }
    private val ticksUntilJump by int("TicksUntilJump", 4, 0..20)
    { jumpCooldownMode == "Ticks" && mode == "Jump" }
    private val hitsUntilJump by int("ReceivedHitsUntilJump", 2, 0..5)
    { jumpCooldownMode == "ReceivedHits" && mode == "Jump" }

    // Ghost Block
    private val hurtTimeRange by intRange("GhostBlockHurtTime", 1..9, 1..10) {
        mode == "GhostBlock"
    }

    // AttackReduce
    private val attackReduceAmount by float("ReduceAmount", 0.8f, 0.3f..1f) { mode == "AttackReduce" }
        .describe("Motion multiplier applied when you attack.")

    // Delay
    private val spoofDelay by int("SpoofDelay", 500, 0..5000) { mode == "Delay" }
        .describe("How long to delay held velocity packets.")
    var delayMode = false
    private val delayedDelay by int("Delayed-Delay", 300, 50..1000) { mode == "Delayed" }
        .describe("How long to delay velocity in Delayed mode.")
    private val delayedBlink by boolean("Delayed-Blink", true) { mode == "Delayed" }
        .describe("Blink packets while delaying velocity.")
    private val delayedBlinkOutgoing by boolean("Delayed-BlinkOutgoing", true) {
        mode == "Delayed" && delayedBlink
    }
    private val delayedDelayTransaction by boolean("Delayed-DelayTransaction", true) {
        mode == "Delayed" && !delayedBlink
    }

    // Memory leak fix: Limit maximum packet queue size
    private const val MAX_PACKET_QUEUE = 100

    // IntaveReduce
    private val reduceFactor by float("Factor", 0.6f, 0.6f..1f) { mode == "IntaveReduce" }
        .describe("Motion multiplier for IntaveReduce.")
    private val hurtTime by int("IntaveHurtTime", 9, 1..10) { mode == "IntaveReduce" }
        .describe("Hurt-time at which IntaveReduce applies.")

    // Spoof
    private val spoofModifyTimer by boolean("ModifyTimer", true) { mode == "Spoof" }
        .describe("Slow the game timer during Spoof mode.")
    private val spoofTimerValue by float("Timer", 0.6f, 0.1f..1f) { mode == "Spoof" && spoofModifyTimer }
        .describe("Timer speed used by Spoof mode.")
    private val cancelHorizontal by boolean("CancelHorizontalVelocity", true) { mode == "Cancel" }
        .describe("Cancel horizontal knockback in Cancel mode.")
    private val cancelVertical by boolean("CancelVerticalVelocity", true) { mode == "Cancel" }
        .describe("Cancel vertical knockback in Cancel mode.")
    private val phaseHeight by float("PhaseHeight", 0.5f, 0f..1f) { mode == "Phase" }
        .describe("How far down to phase in Phase mode.")
    private val phaseOnlyGround by boolean("PhaseOnlyGround", true) { mode == "Phase" }
        .describe("Only phase while on the ground.")
    private val phaseMode by choices("PhaseMode", arrayOf("Normal", "Packet"), "Normal") { mode == "Phase" }
        .describe("How Phase mode moves the player.")
    private val sideStrafeStrafe by boolean("SideStrafeStrafe", false) { mode == "SideStrafe" }
        .describe("Strafe sideways during SideStrafe mode.")
    private val sideStrafeFace by boolean("SideStrafeFace", true) { mode == "SideStrafe" }
        .describe("Face the saved position during SideStrafe.")
    private val grimC07Always by boolean("GrimC07-Always", true) { mode == "GrimC07" }
        .describe("Always run the GrimC07 block trick.")
    private val grimC07OnlyBreakAir by boolean("GrimC07-OnlyBreakAir", true) { mode == "GrimC07" }
        .describe("Only target air blocks for GrimC07.")
    private val grimC07BreakOnWorld by boolean("GrimC07-BreakOnWorld", false) { mode == "GrimC07" }
        .describe("Also set the block to air client-side.")
    private val grimC07SendC03 by boolean("GrimC07-SendC03", false) { mode == "GrimC07" }
        .describe("Send a position packet during GrimC07.")
    private val grimC07SendC06 by boolean("GrimC07-Send1.17C06", false) { mode == "GrimC07" && grimC07SendC03 }
        .describe("Send a 1.17 pos-look packet during GrimC07.")
    private val grimC07FlagPauseTime by int("GrimC07-FlagPauseTime", 50, 0..5000) { mode == "GrimC07" }
        .describe("Pause GrimC07 after a flag for this long.")

    // Tick
    private val velocityTickValue by int("VelocityTick", 1, 0..20) { mode == "Tick" }
        .describe("Ticks to wait before reducing motion.")
    private val tickReductionAmount by float("TickReductionAmount", 1f, 0f..1f) { mode == "Tick" }
        .describe("Fraction of motion removed in Tick mode.")
    private val tickResetMotionY by boolean("ResetMotionY", true) { mode == "Tick" }
        .describe("Zero upward motion in Tick mode.")
    private val tickBypass by boolean("TickBypass", true) { mode == "Tick" }
        .describe("Apply an air-movement bypass in Tick mode.")

    private val pauseOnExplosion by boolean("PauseOnExplosion", true)
        .describe("Pause velocity handling after explosions.")
    private val ticksToPause by int("TicksToPause", 20, 1..50) { pauseOnExplosion }
        .describe("How many ticks to pause after an explosion.")

    private val simpleJitter by boolean("SimpleJitter", false) { mode == "Simple" }
        .describe("Add bounded per-hit variance so the reduction is not a fixed signature.")
    private val simpleJitterAmount by float("SimpleJitterAmount", 0.04f, 0f..0.2f) { mode == "Simple" && simpleJitter }
        .describe("Maximum random swing applied on top of the configured percentages.")
    private val simplePingScale by boolean("SimplePingScale", false) { mode == "Simple" }
        .describe("Let higher latency keep slightly more knockback so movement stays believable.")
    private val simplePingScaleAmount by float("SimplePingScaleAmount", 0.25f, 0f..1f) { mode == "Simple" && simplePingScale }
        .describe("How strongly latency relaxes the reduction (0 = off, 1 = full).")

    // TODO: Could this be useful in other modes? (Jump?)
    // Limits
    private val limitMaxMotionValue = boolean("LimitMaxMotion", false) { mode == "Simple" }
        .describe("Cap maximum knockback motion in Simple mode.")
    private val maxXZMotion by float("MaxXZMotion", 0.4f, 0f..1.9f) { limitMaxMotionValue.isActive() }
        .describe("Maximum horizontal knockback motion.")
    private val maxYMotion by float("MaxYMotion", 0.36f, 0f..0.46f) { limitMaxMotionValue.isActive() }
        .describe("Maximum vertical knockback motion.")

    //Grim
    private val grimAdaptiveHorizontal by float("GrimAdaptive-Horizontal", 0.82f, 0.6f..1f) { mode == "Grim" }
        .describe("Maximum horizontal reduction when telemetry is stable; vertical velocity is preserved.")
    private val grimAdaptiveUncertainty by float("GrimAdaptive-Uncertainty", 0.015f, 0f..0.05f) { mode == "Grim" }
        .describe("Small bounded variance used to avoid a fixed motion signature.")
    private val grimSetbackCooldown by int("GrimAdaptive-SetbackCooldown", 8000, 1000..30000) { mode == "Grim" }
        .describe("Accept full velocity for this many milliseconds after a server setback.")
    private val grimVerticalMode by choices("GrimVerticalMode", arrayOf("Reduce", "1.17", "Vertical"), "Reduce") { mode == "GrimVertical" }
        .describe("Sub-mode for the GrimVertical bypass.")
    private val smartVelo by boolean("SmartVelo", true) { mode == "GrimVertical" && grimVerticalMode == "Vertical" }
        .describe("Use adaptive motion scaling on the ground.")
    private val sendC0FValue by boolean("C0F", false) { mode == "GrimVertical" && grimVerticalMode == "Vertical" }
        .describe("Send transaction packets during GrimVertical.")
    private val c0fPacketAmount by int("C0FPacketAmount", 0, 1..40) { mode == "GrimVertical" && grimVerticalMode == "Vertical" && sendC0FValue }
        .describe("How many transaction packets to send.")
    private val callEvent by boolean("CallEvent", true) { mode == "GrimVertical" && grimVerticalMode == "Vertical" }
        .describe("Also send a swing animation when attacking.")
    private val via by boolean("Via", true) { mode == "GrimVertical" && (grimVerticalMode == "Vertical" || grimVerticalMode == "Reduce") }
        .describe("Send the attack before the swing animation.")


    //0.00075 is added silently

    // Vanilla XZ limits
    // Non-KB: 0.4 (no sprint), 0.9 (sprint)
    // KB 1: 0.9 (no sprint), 1.4 (sprint)
    // KB 2: 1.4 (no sprint), 1.9 (sprint)
    // Vanilla Y limits
    // 0.36075 (no sprint), 0.46075 (sprint)

    private val clicks by intRange("Clicks", 3..5, 1..20) { mode == "Click" }
        .describe("Number of extra attacks per knockback.")
    private val hurtTimeToClick by int("HurtTimeToClick", 10, 0..10) { mode == "Click" }
        .describe("Hurt-time at which Click mode fires.")
    private val whenFacingEnemyOnly by boolean("WhenFacingEnemyOnly", true) { mode == "Click" }
        .describe("Only click when facing an enemy.")
    private val ignoreBlocking by boolean("IgnoreBlocking", false) { mode == "Click" }
        .describe("Skip Click mode while blocking.")
    private val clickRange by float("ClickRange", 3f, 1f..6f) { mode == "Click" }
        .describe("Reach used to find a target for Click mode.")
    private val swingMode by choices("SwingMode", arrayOf("Off", "Normal", "Packet"), "Normal") { mode == "Click" }
        .describe("How to swing when clicking in Click mode.")

    private val generalGroup = Configurable("General")
    private val reverseGroup = Configurable("Reverse")
    private val aacGroup = Configurable("AAC")
    private val jumpGroup = Configurable("Jump")
    private val delayGroup = Configurable("Delay")
    private val tickGroup = Configurable("Tick")
    private val grimGroup = Configurable("Grim")
    private val miscGroup = Configurable("Misc")

    init {
        moveValues(generalGroup,
            "AntiCheat", "Mode", "Horizontal", "Vertical", "OnlyGround", "OnlyCombat", "noFire",
            "RespectSetbacks", "SetbackGrace",
            "OverrideDirection", "OverrideDirectionYaw", "PauseOnExplosion", "TicksToPause")

        moveValues(reverseGroup,
            "ReverseStrength", "SmoothReverseStrength", "onLook", "Range", "MaxAngleDifference")

        moveValues(aacGroup,
            "AACPushXZReducer", "AACPushYReducer", "AACv4MotionReducer", "AAC4ReduceAmount",
            "AAC5ReduceAmount", "ReduceAmount", "Factor", "IntaveHurtTime")

        moveValues(jumpGroup,
            "DisableInAir", "Chance", "JumpCooldownMode", "TicksUntilJump", "ReceivedHitsUntilJump")

        moveValues(delayGroup,
            "SpoofDelay", "Delayed-Delay", "Delayed-Blink", "Delayed-BlinkOutgoing",
            "Delayed-DelayTransaction")

        moveValues(tickGroup,
            "VelocityTick", "TickReductionAmount", "ResetMotionY", "TickBypass",
            "LimitMaxMotion", "MaxXZMotion", "MaxYMotion",
            "SimpleJitter", "SimpleJitterAmount", "SimplePingScale", "SimplePingScaleAmount")

        moveValues(grimGroup,
            "GrimAdaptive-Horizontal", "GrimAdaptive-Uncertainty", "GrimAdaptive-SetbackCooldown",
            "GrimC07-Always", "GrimC07-OnlyBreakAir", "GrimC07-BreakOnWorld", "GrimC07-SendC03",
            "GrimC07-Send1.17C06", "GrimC07-FlagPauseTime", "GrimVerticalMode", "SmartVelo",
            "C0F", "C0FPacketAmount", "CallEvent", "Via")

        moveValues(miscGroup,
            "ModifyTimer", "Timer", "CancelHorizontalVelocity", "CancelVerticalVelocity",
            "PhaseHeight", "PhaseOnlyGround", "PhaseMode", "SideStrafeStrafe", "SideStrafeFace",
            "Clicks", "HurtTimeToClick", "WhenFacingEnemyOnly", "IgnoreBlocking", "ClickRange",
            "SwingMode")

        addValues(listOf(
            generalGroup, reverseGroup, aacGroup, jumpGroup, delayGroup, tickGroup,
            grimGroup, miscGroup,
        ))
    }
    /**
     * VALUES
     */
    private val velocityTimer = MSTimer()
    private var hasReceivedVelocity = false

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // Jump
    private var limitUntilJump = 0

    // IntaveReduce
    private var intaveTick = 0
    private var lastAttackTime = 0L
    private var intaveDamageTick = 0
    private var intaveJumped = 0

    // Delay
    private val packets = LinkedHashMap<Packet<*>, Long>()
    private val delayedPackets = mutableListOf<Pair<Packet<*>, Long>>()
    private val delayedBlinkTimer = MSTimer()
    private var delayedBlinkActive = false
    private var delayedVelocityTick = -1

    // Tick / Spoof
    private var velocityTick = 0
    private var wasTimer = false
    private var aac520CombatTemplateX = 0
    private var aac520CombatTemplateY = 0
    private var aac520CombatTemplateZ = 0

    // Grim
    private var timerTicks = 0
    private var grimC07GotVelocity = false
    private val grimC07FlagTimer = MSTimer()
    private var profileRefreshTicks = 0
    private var lastProfileFingerprint = ""
    private var lastModeWarning = ""

    // Vulcan
    private var transaction = false

    // Hypixel
    private var absorbedVelocity = false
    private var minemenTicks = 0
    private var minemenLastCancel = false
    private var minemenCanCancel = false

    //GrimVertical Variables
    private var attack = false
    private var motionXZ = 0.0
    private var velocityInput = false
    private var canCancel = false
    private var canSpoof = false
    private var sideStrafePos: BlockPos? = null

    // Pause On Explosion
    private var pauseTicks = 0

    override val tag
        get() = if (mode == "Simple" || mode == "Legit") {
            val horizontalPercentage = (horizontal * 100).toInt()
            val verticalPercentage = (vertical * 100).toInt()

            "$horizontalPercentage% $verticalPercentage%"
        } else mode

    override fun onEnable() {
        refreshModeChoices(force = true)
        warnAboutMode(mode)
    }

    override fun onDisable() {
        pauseTicks = 0
        mc.thePlayer?.speedInAir = 0.02F
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (wasTimer) {
            mc.timer.timerSpeed = 1f
            wasTimer = false
        }
        timerTicks = 0
        canCancel = false
        canSpoof = false
        velocityTick = 0
        intaveJumped = 0
        grimC07GotVelocity = false
        sideStrafePos = null
        aac520CombatTemplateX = 0
        aac520CombatTemplateY = 0
        aac520CombatTemplateZ = 0
        minemenTicks = 0
        minemenLastCancel = false
        minemenCanCancel = false
        val shouldUnblink = delayedBlinkActive
        delayedPackets.clear()
        delayedBlinkActive = false
        delayedVelocityTick = -1
        if (shouldUnblink && BlinkUtils.isBlinking) {
            BlinkUtils.unblink()
        }
        reset()
    }

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler
        if (++profileRefreshTicks >= 20) {
            profileRefreshTicks = 0
            refreshModeChoices()
        }

        if (mode != "Intave") {
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        }

        if (wasTimer) {
            mc.timer.timerSpeed = 1f
            wasTimer = false
        }

        if (thePlayer.isInLiquid || thePlayer.isInWeb || thePlayer.isDead)
            return@handler

        if (mode != "Delayed") {
            flushDelayedPackets(force = true)
        }

        if (shouldBlockLegacyVelocity(thePlayer)) {
            return@handler
        }

        when (mode.lowercase()) {
            "tick" -> {
                if (velocityInput) {
                    velocityTick++
                }

                if (velocityTick > velocityTickValue) {
                    if (thePlayer.motionY > 0 && tickResetMotionY) {
                        thePlayer.motionY = 0.0
                    }

                    thePlayer.motionX *= 1.0 - tickReductionAmount.toDouble()
                    thePlayer.motionZ *= 1.0 - tickReductionAmount.toDouble()
                    thePlayer.jumpMovementFactor = if (tickBypass) -0.001f else 0.0f
                    velocityInput = false
                }

                if (thePlayer.onGround && velocityTick > 1) {
                    velocityInput = false
                }
            }

            "glitch" -> {
                thePlayer.noClip = hasReceivedVelocity

                if (thePlayer.hurtTime == 7)
                    thePlayer.motionY = 0.4

                hasReceivedVelocity = false
            }

            "reverse" -> {
                val nearbyEntity = getNearestEntityInRange()

                if (!hasReceivedVelocity)
                    return@handler

                if (nearbyEntity != null) {
                    if (!thePlayer.onGround) {
                        if (onLook && !thePlayer.isLookingOn(nearbyEntity, maxAngleDifference.toDouble())) {
                            return@handler
                        }

                        speed *= reverseStrength
                    } else if (velocityTimer.hasTimePassed(80))
                        hasReceivedVelocity = false
                }
            }

            "smoothreverse" -> {
                val nearbyEntity = getNearestEntityInRange()

                if (hasReceivedVelocity) {
                    if (nearbyEntity == null) {
                        thePlayer.speedInAir = 0.02F
                        reverseHurt = false
                    } else {
                        if (onLook && !thePlayer.isLookingOn(nearbyEntity, maxAngleDifference.toDouble())) {
                            hasReceivedVelocity = false
                            thePlayer.speedInAir = 0.02F
                            reverseHurt = false
                        } else {
                            if (thePlayer.hurtTime > 0) {
                                reverseHurt = true
                            }

                            if (!thePlayer.onGround) {
                                thePlayer.speedInAir = if (reverseHurt) reverse2Strength else 0.02F
                            } else if (velocityTimer.hasTimePassed(80)) {
                                hasReceivedVelocity = false
                                thePlayer.speedInAir = 0.02F
                                reverseHurt = false
                            }
                        }
                    }
                }
            }

            "aac" -> if (hasReceivedVelocity && velocityTimer.hasTimePassed(80)) {
                thePlayer.motionX *= horizontal
                thePlayer.motionZ *= horizontal
                //mc.thePlayer.motionY *= vertical ?
                hasReceivedVelocity = false
            }

            "aacv4" ->
                if (thePlayer.hurtTime > 0 && !thePlayer.onGround) {
                    val reduce = aacv4MotionReducer
                    thePlayer.motionX *= reduce
                    thePlayer.motionZ *= reduce
                }

            "aac4reduce" -> {
                if (thePlayer.hurtTime > 0 && !thePlayer.onGround && velocityInput && velocityTimer.hasTimePassed(80L)) {
                    thePlayer.motionX *= aac4ReduceAmount.toDouble()
                    thePlayer.motionZ *= aac4ReduceAmount.toDouble()
                }

                if (velocityInput && (thePlayer.hurtTime < 4 || thePlayer.onGround) && velocityTimer.hasTimePassed(120L)) {
                    velocityInput = false
                }
            }

            "aacpush" -> {
                if (jump) {
                    if (thePlayer.onGround)
                        jump = false
                } else {
                    // Strafe
                    if (thePlayer.hurtTime > 0 && thePlayer.motionX != 0.0 && thePlayer.motionZ != 0.0)
                        thePlayer.onGround = true

                    // Reduce Y
                    if (thePlayer.hurtResistantTime > 0 && aacPushYReducer && !Speed.handleEvents())
                        thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducer

                    thePlayer.motionX /= reduce
                    thePlayer.motionZ /= reduce
                }
            }

            "aaczero" ->
                if (thePlayer.hurtTime > 0) {
                    if (!hasReceivedVelocity || thePlayer.onGround || thePlayer.fallDistance > 2F)
                        return@handler

                    thePlayer.motionY -= 1.0
                    thePlayer.isAirBorne = true
                    thePlayer.onGround = true
                } else
                    hasReceivedVelocity = false

            "legit" -> {
                if (legitDisableInAir && !isOnGround(0.5))
                    return@handler

                if (mc.thePlayer.maxHurtResistantTime != mc.thePlayer.hurtResistantTime || mc.thePlayer.maxHurtResistantTime == 0)
                    return@handler

                if (nextInt(endExclusive = 100) < chance) {
                    val horizontal = horizontal / 100f
                    val vertical = vertical / 100f

                    thePlayer.motionX *= horizontal.toDouble()
                    thePlayer.motionZ *= horizontal.toDouble()
                    thePlayer.motionY *= vertical.toDouble()
                }
            }

            "intavereduce" -> {
                if (!hasReceivedVelocity) return@handler
                intaveTick++

                if (mc.thePlayer.hurtTime == 2) {
                    intaveDamageTick++
                    if (thePlayer.onGround && intaveTick % 2 == 0 && intaveDamageTick <= 10) {
                        thePlayer.tryJump()
                        intaveTick = 0
                    }
                    hasReceivedVelocity = false
                }
            }

            "intave" -> {
                if (mc.currentScreen != null) {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    return@handler
                }

                if (thePlayer.hurtTime == 9) {
                    if (++intaveJumped % 2 == 0 && thePlayer.onGround && thePlayer.isSprinting) {
                        mc.gameSettings.keyBindJump.pressed = true
                        intaveJumped = 0
                    }
                } else {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                }
            }

            "aac5reduce" -> {
                if (thePlayer.hurtTime > 1 && velocityInput) {
                    thePlayer.motionX *= aac5ReduceAmount.toDouble()
                    thePlayer.motionZ *= aac5ReduceAmount.toDouble()
                }

                if (velocityInput && (thePlayer.hurtTime < 5 || thePlayer.onGround) && velocityTimer.hasTimePassed(120L)) {
                    velocityInput = false
                }
            }

            "polar" -> {
                if (thePlayer.hurtTime == 9) {
                    thePlayer.motionX *= 0.6
                    thePlayer.motionZ *= 0.6
                }
            }

            "sentinel" -> {
                if (thePlayer.hurtTime == 9) {
                    thePlayer.motionX *= 0.5
                    thePlayer.motionZ *= 0.5
                    thePlayer.motionY *= 0.8
                }
            }

            "aac5.2.0combat" -> {
                if (thePlayer.hurtTime > 0 && velocityInput) {
                    velocityInput = false
                    thePlayer.motionX = 0.0
                    thePlayer.motionY = 0.0
                    thePlayer.motionZ = 0.0
                    thePlayer.jumpMovementFactor = -0.002f
                    sendPacket(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            thePlayer.posX,
                            Double.MAX_VALUE,
                            thePlayer.posZ,
                            true
                        )
                    )
                }

                if (velocityTimer.hasTimePassed(80L) && velocityInput) {
                    velocityInput = false
                    thePlayer.motionX = aac520CombatTemplateX / 8000.0
                    thePlayer.motionY = aac520CombatTemplateY / 8000.0
                    thePlayer.motionZ = aac520CombatTemplateZ / 8000.0
                    thePlayer.jumpMovementFactor = -0.002f
                }
            }

            "hypixel" -> {
                if (hasReceivedVelocity && thePlayer.onGround) {
                    absorbedVelocity = false
                }
            }

            "hypixelair" -> {
                if (hasReceivedVelocity) {
                    if (thePlayer.onGround) {
                        thePlayer.tryJump()
                    }
                    hasReceivedVelocity = false
                }
            }

            "grimvertical" -> {
                when(grimVerticalMode.lowercase()){
                    "1.17" -> {
                        if (canSpoof) {
                            sendPacket(C03PacketPlayer.C06PacketPlayerPosLook(thePlayer.posX, thePlayer.posY, thePlayer.posZ, thePlayer.rotationYaw, thePlayer.rotationPitch, thePlayer.onGround))
                            sendPacket(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos(thePlayer).down(), DOWN))
                            canSpoof = false
                        }
                    }
                    "vertical" -> {
                        if (attack) {
                            val entity = mc.thePlayer.entityId

                            if (via) {
                                sendPacket(C02PacketUseEntity(mc.theWorld.getEntityByID(entity), C02PacketUseEntity.Action.ATTACK))
                                if (callEvent)
                                    sendPacket(C0APacketAnimation())
                            }
                            else {
                                if (callEvent)
                                    sendPacket(C0APacketAnimation())
                                sendPacket(C02PacketUseEntity(mc.theWorld.getEntityByID(entity), C02PacketUseEntity.Action.ATTACK))
                            }


                            if (smartVelo && thePlayer.onGround) {
                                thePlayer.motionX *= motionXZ
                                thePlayer.motionZ *= motionXZ
                            } else {
                                thePlayer.motionX *= 0.077760000
                                thePlayer.motionZ *= 0.077760000
                            }
                            velocityInput = false
                            attack = false
                        }
                    }
                }
            }

            "hypixelboost" -> {
                if (thePlayer.hurtTime == 8) {
                    MovementUtils.strafe(MovementUtils.speed * 0.7f)
                }
            }

            "grim" -> {
                // A setback means the server rejected recent movement assumptions. Stop reducing
                // immediately instead of compounding the mismatch with transaction cancellation.
                if (hasReceivedVelocity && isInsideGrimSetbackCooldown()) {
                    hasReceivedVelocity = false
                }
            }

            "grimdamage" -> {
                if (thePlayer.hurtTime == 9) {
                    val target = CombatManager.target?.takeIf { !it.isDead && thePlayer.getDistanceToEntityBox(it) <= 3f }
                        ?: getNearestEntityInRange(3f)

                    if (target != null) {
                        repeat(12) {
                            sendPacket(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                            sendPacket(C0APacketAnimation())
                        }

                        thePlayer.motionX *= 0.077760000
                        thePlayer.motionZ *= 0.077760000
                    }
                }
            }

            "delayed" -> flushDelayedPackets()

            "minemen" -> {
                minemenTicks++

                if (minemenTicks > 23) {
                    minemenCanCancel = true
                }

                if (minemenTicks in 2..4 && !minemenLastCancel) {
                    thePlayer.motionX *= 0.99
                    thePlayer.motionZ *= 0.99
                } else if (minemenTicks == 5 && !minemenLastCancel) {
                    MovementUtils.strafe()
                }
            }
        }
    }

    private fun getMotionNoXZ(packetEntityVelocity: S12PacketEntityVelocity): Double {
        val vec = Vec3(
            packetEntityVelocity.motionX.toDouble(),
            packetEntityVelocity.motionY.toDouble(),
            packetEntityVelocity.motionZ.toDouble()
        )

        val strength = vec.lengthVector()

        val motionNoXZ: Double
        if (strength >= 20000.0) {
            motionNoXZ = if (mc.thePlayer.onGround) {
                0.06425
            } else {
                0.075
            }
        } else if (strength >= 5000.0) {
            motionNoXZ = if (mc.thePlayer.onGround) {
                0.02625
            } else {
                0.0552
            }
        } else {
            motionNoXZ = 0.0175
        }

        return motionNoXZ
    }

    /**
     * @see net.minecraft.entity.player.EntityPlayer.attackTargetEntityWithCurrentItem
     * Lines 1035 and 1058
     *
     * Minecraft only applies motion slow-down when you are sprinting and attacking, once per tick.
     * An example scenario: If you perform a mouse double-click on an entity, the game will only accept the first attack.
     *
     * This is where we come in clutch by making the player always sprint before dropping
     *
     * [clicks] amount of hits on the target [Entity]
     *
     * We also explicitly-cast the player as an [Entity] to avoid triggering any other things caused from setting new sprint status.
     *
     * @see net.minecraft.client.entity.EntityPlayerSP.setSprinting
     * @see net.minecraft.entity.EntityLivingBase.setSprinting
     */
    val onGameTick = handler<GameTickEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        mc.theWorld ?: return@handler

        if (mode != "Click" || thePlayer.hurtTime != hurtTimeToClick || ignoreBlocking && (thePlayer.isBlocking || KillAura.blockStatus))
            return@handler

        var entity = mc.objectMouseOver?.entityHit

        if (entity == null) {
            if (whenFacingEnemyOnly) {
                var result: Entity? = null

                runWithModifiedRaycastResult(
                    currentRotation ?: thePlayer.rotation,
                    clickRange.toDouble(),
                    0.0
                ) {
                    result = it.entityHit?.takeIf { isSelected(it, true) }
                }

                entity = result
            } else getNearestEntityInRange(clickRange)?.takeIf { isSelected(it, true) }
        }

        entity ?: return@handler

        val swingHand = {
            when (swingMode.lowercase()) {
                "normal" -> thePlayer.swingItem()
                "packet" -> sendPacket(C0APacketAnimation())
            }
        }

        repeat(clicks.random()) {
            thePlayer.attackEntityWithModifiedSprint(entity, true) { swingHand() }
        }
    }

    val onAttack = handler<AttackEvent> {
        val player = mc.thePlayer ?: return@handler

        if (mode == "AttackReduce") {
            if (player.hurtTime >= 3) {
                player.motionX *= attackReduceAmount.toDouble()
                player.motionZ *= attackReduceAmount.toDouble()
            }

            return@handler
        }

        if (mode != "IntaveReduce" || !hasReceivedVelocity) return@handler

        if (player.hurtTime == hurtTime && System.currentTimeMillis() - lastAttackTime <= 8000) {
            player.motionX *= reduceFactor
            player.motionZ *= reduceFactor
        }

        lastAttackTime = System.currentTimeMillis()
    }

    private fun checkAir(blockPos: BlockPos): Boolean {
        val world = mc.theWorld ?: return false

        if (!world.isAirBlock(blockPos)) {
            return false
        }

        timerTicks = 20

        sendPackets(
            C03PacketPlayer(true),
            C07PacketPlayerDigging(STOP_DESTROY_BLOCK, blockPos, DOWN)
        )

        world.setBlockToAir(blockPos)

        return true
    }

    val onPacket = handler<PacketEvent>(priority = 1) { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        val packet = event.packet

        if (!handleEvents())
            return@handler

        if (pauseTicks > 0) {
            pauseTicks--
            return@handler
        }

        if (event.isCancelled)
            return@handler

        if (mode == "GrimC07" && packet is S08PacketPlayerPosLook) {
            grimC07FlagTimer.reset()
            grimC07GotVelocity = false
        }

        if (shouldBlockLegacyVelocity(thePlayer)) {
            return@handler
        }

        if (mode == "Delayed") {
            handleDelayedPacket(event, thePlayer)

            if (event.isCancelled) {
                return@handler
            }
        }

        if (mode == "MatrixSimple") {
            when (packet) {
                is S12PacketEntityVelocity -> {
                    if (packet.entityID == thePlayer.entityId) {
                        packet.motionX = (packet.motionX * 0.36).toInt()
                        packet.motionZ = (packet.motionZ * 0.36).toInt()

                        if (thePlayer.onGround) {
                            packet.motionX = (packet.motionX * 0.9).toInt()
                            packet.motionZ = (packet.motionZ * 0.9).toInt()
                        }
                    }

                    return@handler
                }

                is S27PacketExplosion -> return@handler
            }
        }

        if ((packet is S12PacketEntityVelocity && thePlayer.entityId == packet.entityID && packet.motionY > 0 && (packet.motionX != 0 || packet.motionZ != 0))
            || (packet is S27PacketExplosion && (thePlayer.motionY + packet.field_149153_g) > 0.0
                    && ((thePlayer.motionX + packet.field_149152_f) != 0.0 || (thePlayer.motionZ + packet.field_149159_h) != 0.0))
        ) {
            velocityTimer.reset()

            if (packet is S12PacketEntityVelocity) {
                applyDirectionOverride(packet)
            }

            if (pauseOnExplosion && packet is S27PacketExplosion && (thePlayer.motionY + packet.field_149153_g) > 0.0
                && ((thePlayer.motionX + packet.field_149152_f) != 0.0 || (thePlayer.motionZ + packet.field_149159_h) != 0.0)
            ) {
                pauseTicks = ticksToPause
            }

            when (mode.lowercase()) {
                "simple" -> handleVelocity(event)

                "aac", "reverse", "smoothreverse", "aaczero", "ghostblock", "intavereduce" -> hasReceivedVelocity = true

                "aac4reduce" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        velocityInput = true
                        packet.motionX = (packet.motionX * 0.6).toInt()
                        packet.motionZ = (packet.motionZ * 0.6).toInt()
                    }
                }

                "jump" -> {
                    var packetDirection = 0.0
                    when (packet) {
                        is S12PacketEntityVelocity -> {
                            if (packet.entityID != thePlayer.entityId) return@handler
                            val motionX = packet.motionX.toDouble()
                            val motionZ = packet.motionZ.toDouble()
                            packetDirection = atan2(motionX, motionZ)
                        }
                        is S27PacketExplosion -> {
                            val motionX = thePlayer.motionX + packet.field_149152_f
                            val motionZ = thePlayer.motionZ + packet.field_149159_h
                            packetDirection = atan2(motionX, motionZ)
                        }
                    }
                    val degreePlayer = MovementUtils.direction
                    val degreePacket = Math.floorMod(packetDirection.toInt(), 360).toDouble()
                    var angle = abs(degreePacket + degreePlayer)
                    val threshold = 120.0
                    angle = Math.floorMod(angle.toInt(), 360).toDouble()
                    val inRange = angle in 180 - threshold / 2..180 + threshold / 2
                    if (inRange)
                        hasReceivedVelocity = true
                }

                "glitch" -> {
                    if (!thePlayer.onGround)
                        return@handler

                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "matrixreduce" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        packet.motionX = (packet.motionX * 0.33).toInt()
                        packet.motionZ = (packet.motionZ * 0.33).toInt()

                        if (thePlayer.onGround) {
                            packet.motionX = (packet.motionX * 0.86).toInt()
                            packet.motionZ = (packet.motionZ * 0.86).toInt()
                        }
                    }
                }

                "matrixreverse" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        event.cancelEvent()
                        thePlayer.motionX = packet.realMotionX
                        thePlayer.motionY = packet.realMotionY
                        thePlayer.motionZ = packet.realMotionZ
                        MovementUtils.strafe()
                    }
                }

                "blocksmc" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        hasReceivedVelocity = true
                        event.cancelEvent()

                        sendPacket(C0BPacketEntityAction(thePlayer, START_SNEAKING))
                        sendPacket(C0BPacketEntityAction(thePlayer, STOP_SNEAKING))
                    }
                }

                "grimc03" -> {
                    if (thePlayer.isMoving) {
                        hasReceivedVelocity = true
                        event.cancelEvent()
                    }
                }

                "hypixel" -> {
                    hasReceivedVelocity = true
                    if (!thePlayer.onGround) {
                        if (!absorbedVelocity) {
                            event.cancelEvent()
                            absorbedVelocity = true
                            return@handler
                        }
                    }

                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        packet.motionX = (thePlayer.motionX * 8000).toInt()
                        packet.motionZ = (thePlayer.motionZ * 8000).toInt()
                    }
                }

                "hypixelair" -> {
                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "spoof" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        event.cancelEvent()
                        sendPacket(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                thePlayer.posX + packet.realMotionX,
                                thePlayer.posY + packet.realMotionY,
                                thePlayer.posZ + packet.realMotionZ,
                                false
                            ),
                            false
                        )

                        if (spoofModifyTimer) {
                            mc.timer.timerSpeed = spoofTimerValue
                            wasTimer = true
                        }
                    }
                }

                "cancel" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        event.cancelEvent()

                        if (!cancelVertical) {
                            thePlayer.motionY = packet.realMotionY
                        }

                        if (!cancelHorizontal) {
                            thePlayer.motionX = packet.realMotionX
                            thePlayer.motionZ = packet.realMotionZ
                        }
                    }
                }

                "tick" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        velocityInput = true
                        velocityTick = 0

                        if (horizontal == 0f && vertical == 0f) {
                            event.cancelEvent()
                        }

                        packet.motionX = (packet.motionX * horizontal).toInt()
                        packet.motionY = (packet.motionY * vertical).toInt()
                        packet.motionZ = (packet.motionZ * horizontal).toInt()
                    }
                }

                "aac5reduce" -> velocityInput = true

                "aac5.2.0" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        event.cancelEvent()
                        sendPacket(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                thePlayer.posX,
                                Double.MAX_VALUE,
                                thePlayer.posZ,
                                true
                            )
                        )
                    }
                }

                "aac5.2.0combat" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        event.cancelEvent()
                        velocityInput = true
                        aac520CombatTemplateX = packet.motionX
                        aac520CombatTemplateY = packet.motionY
                        aac520CombatTemplateZ = packet.motionZ
                    }
                }

                "minemen" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        minemenTicks = 0

                        if (minemenCanCancel) {
                            event.cancelEvent()
                            minemenLastCancel = true
                            minemenCanCancel = false
                        } else {
                            thePlayer.tryJump()
                            minemenLastCancel = false
                        }
                    }
                }

                "phase" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        if (!thePlayer.onGround && phaseOnlyGround) {
                            return@handler
                        }

                        when (phaseMode.lowercase()) {
                            "normal" -> {
                                velocityInput = true
                                thePlayer.setPositionAndUpdate(thePlayer.posX, thePlayer.posY - phaseHeight, thePlayer.posZ)
                            }

                            "packet" -> {
                                if (packet.motionX < 500 && packet.motionY < 500) {
                                    return@handler
                                }

                                sendPacket(
                                    C03PacketPlayer.C04PacketPlayerPosition(
                                        thePlayer.posX,
                                        thePlayer.posY - phaseHeight,
                                        thePlayer.posZ,
                                        false
                                    )
                                )
                            }
                        }

                        event.cancelEvent()
                        packet.motionX = 0
                        packet.motionY = 0
                        packet.motionZ = 0
                    }
                }

                "sidestrafe" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        sideStrafePos = BlockPos(thePlayer.posX, thePlayer.posY, thePlayer.posZ)
                    }
                }

                "vulcan" -> {
                    event.cancelEvent()
                }

                "s32packet" -> {
                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "grimvertical" -> {
                    if (packet is S12PacketEntityVelocity) {
                        if (packet.entityID == thePlayer.entityId) {
                            when (grimVerticalMode.lowercase()) {
                                "reduce" -> {
                                    val velocityX = packet.motionX / 8000.0
                                    val velocityZ = packet.motionZ / 8000.0

                                    thePlayer.motionX = velocityX * 0.078
                                    thePlayer.motionZ = velocityZ * 0.078
                                    event.cancelEvent()
                                }

                                "1.17" -> {
                                    canCancel = true
                                    canSpoof = true
                                    event.cancelEvent()
                                }

                                "vertical" -> {
                                    if (packet.motionX == 0 && packet.motionZ == 0 ||
                                        mc.thePlayer == null ||
                                        mc.theWorld.getEntityByID(packet.entityID) != mc.thePlayer
                                    ) {
                                        return@handler
                                    }

                                    velocityInput = true
                                    motionXZ = getMotionNoXZ(packet)

                                    if (thePlayer.isSprinting && thePlayer.serverSprintState && thePlayer.isMoving) {
                                        for (i in 0 until c0fPacketAmount) {
                                            if (sendC0FValue) {
                                                mc.netHandler.addToSendQueue(
                                                    C0FPacketConfirmTransaction(
                                                        nextInt(102, 1000024123),
                                                        nextInt(102, 1000024123).toShort(),
                                                        true
                                                    )
                                                )
                                            }
                                        }
                                        attack = true
                                    }
                                    event.cancelEvent()
                                }
                            }
                        }
                    }
                }

                "grim" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        val factor = grimAdaptiveFactor()
                        packet.motionX = (packet.motionX * factor).toInt()
                        packet.motionZ = (packet.motionZ * factor).toInt()
                        hasReceivedVelocity = factor < 0.999
                    }
                }

                "grim1.17" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        repeat(4) {
                            sendPacket(
                                C03PacketPlayer.C06PacketPlayerPosLook(
                                    thePlayer.posX,
                                    thePlayer.posY,
                                    thePlayer.posZ,
                                    thePlayer.rotationYaw,
                                    thePlayer.rotationPitch,
                                    thePlayer.onGround
                                )
                            )
                        }

                        sendPacket(
                            C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                thePlayer.position,
                                DOWN
                            )
                        )
                        event.cancelEvent()
                    }
                }

                "grimc07" -> {
                    if (!grimC07FlagTimer.hasTimePassed(grimC07FlagPauseTime.toLong())) {
                        grimC07GotVelocity = false
                        return@handler
                    }

                    when (packet) {
                        is S12PacketEntityVelocity -> {
                            if (packet.entityID == thePlayer.entityId) {
                                event.cancelEvent()
                                grimC07GotVelocity = true
                            }
                        }

                        is S27PacketExplosion -> {
                            event.cancelEvent()
                            grimC07GotVelocity = true
                        }
                    }
                }
            }
        }

        if (mode == "BlocksMC" && hasReceivedVelocity) {
            if (packet is C0BPacketEntityAction) {
                hasReceivedVelocity = false
                event.cancelEvent()
            }
        }

        if (mode == "Vulcan") {
            if (Disabler.handleEvents() && Disabler.verusCombat && (!Disabler.onlyCombat || Disabler.isOnCombat)) return@handler

            if (packet is S32PacketConfirmTransaction) {
                event.cancelEvent()
                sendPacket(
                    C0FPacketConfirmTransaction(
                        if (transaction) 1 else -1,
                        if (transaction) -1 else 1,
                        transaction
                    ), false
                )
                transaction = !transaction
            }
        }

        if (mode == "S32Packet" && packet is S32PacketConfirmTransaction) {

            if (!hasReceivedVelocity)
                return@handler

            event.cancelEvent()
            hasReceivedVelocity = false
        }
    }

    /**
     * Tick Event (Abuse Timer Balance)
     */
    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler

        if (mode == "GrimC07") {
            if (!grimC07FlagTimer.hasTimePassed(grimC07FlagPauseTime.toLong())) {
                grimC07GotVelocity = false
                return@handler
            }

            val world = mc.theWorld ?: return@handler

            if (grimC07GotVelocity || grimC07Always) {
                val pos = BlockPos(player.posX, player.posY, player.posZ)
                if (checkGrimC07Block(pos, world) || checkGrimC07Block(pos.up(), world)) {
                    grimC07GotVelocity = false
                }
            }

            return@handler
        }

        if (mode != "GrimC03")
            return@handler

        // Timer Abuse (https://github.com/CCBlueX/LiquidBounce/issues/2519)
        if (timerTicks > 0 && mc.timer.timerSpeed <= 1) {
            val timerSpeed = 0.8f + (0.2f * (20 - timerTicks) / 20)
            mc.timer.timerSpeed = timerSpeed.coerceAtMost(1f)
            --timerTicks
        } else if (mc.timer.timerSpeed <= 1) {
            mc.timer.timerSpeed = 1f
        }

        if (hasReceivedVelocity) {
            val pos = BlockPos(player.posX, player.posY, player.posZ)

            if (checkAir(pos))
                hasReceivedVelocity = false
        }
    }

    /**
     * Delay Mode
     */
    val onDelayPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (event.isCancelled)
            return@handler

        if (mode == "Delay") {
            if (packet is S32PacketConfirmTransaction || packet is S12PacketEntityVelocity) {

                event.cancelEvent()

                // Delaying packet like PingSpoof
                synchronized(packets) {
                    // Memory leak fix: Enforce queue limit
                    if (packets.size >= MAX_PACKET_QUEUE) {
                        // Remove oldest packet
                        val oldestKey = packets.keys.firstOrNull()
                        if (oldestKey != null) {
                            packets.remove(oldestKey)
                        }
                    }
                    packets[packet] = System.currentTimeMillis()
                }
            }
            delayMode = true
        } else {
            delayMode = false
        }
    }

    /**
     * Reset on world change
     */
    val onWorld = handler<WorldEvent> {
        packets.clear()
        val shouldUnblink = delayedBlinkActive
        delayedPackets.clear()
        delayedBlinkActive = false
        delayedVelocityTick = -1
        sideStrafePos = null
        grimC07GotVelocity = false
        if (shouldUnblink && BlinkUtils.isBlinking) {
            BlinkUtils.unblink()
        }
    }

    val onGameLoop = handler<GameLoopEvent> {
        if (mode == "Delay")
            sendPacketsByOrder(false)
        if (mode == "Delayed") {
            flushDelayedPackets()
        }
    }

    private fun sendPacketsByOrder(velocity: Boolean) {
        synchronized(packets) {
            packets.entries.removeAll { (packet, timestamp) ->
                if (velocity || timestamp <= System.currentTimeMillis() - spoofDelay) {
                    PacketUtils.schedulePacketProcess(packet)
                    true
                } else false
            }
        }
    }

    private fun reset() {
        sendPacketsByOrder(true)

        packets.clear()
    }

    private fun shouldBlockLegacyVelocity(player: net.minecraft.client.entity.EntityPlayerSP): Boolean {
        if (onlyGround && !player.onGround) {
            return true
        }

        if (onlyCombat && !CombatManager.inCombatState) {
            return true
        }

        if (noFire && player.isBurning) {
            return true
        }

        return isInsideSetbackGrace()
    }

    private fun isInsideSetbackGrace(): Boolean {
        if (!respectSetbacks || setbackGrace <= 0 || ServerObserver.lastLagBackAt <= 0L) {
            return false
        }

        val latency = ServerObserver.ping.coerceAtLeast(0)
        val window = setbackGrace + (latency / 2).coerceAtMost(1000)
        return System.currentTimeMillis() - ServerObserver.lastLagBackAt < window
    }

    private fun applyDirectionOverride(packet: S12PacketEntityVelocity) {
        if (overrideDirection == "None") {
            return
        }

        val yaw = Math.toRadians(
            if (overrideDirection == "Hard") {
                overrideDirectionYaw.toDouble()
            } else {
                mc.thePlayer.rotationYaw + overrideDirectionYaw + 90f
            }.toDouble()
        )
        val dist = sqrt((packet.motionX * packet.motionX + packet.motionZ * packet.motionZ).toDouble())
        packet.motionX = (kotlin.math.cos(yaw) * dist).toInt()
        packet.motionZ = (kotlin.math.sin(yaw) * dist).toInt()
    }

    private fun handleDelayedPacket(event: PacketEvent, player: net.minecraft.client.entity.EntityPlayerSP) {
        val packet = event.packet

        if (delayedBlinkActive && delayedBlinkOutgoing && event.eventType == EventState.SEND) {
            BlinkUtils.blink(packet, event, sent = true, receive = false)
            return
        }

        if (event.eventType != EventState.RECEIVE) {
            return
        }

        if (packet is S12PacketEntityVelocity && packet.entityID == player.entityId) {
            event.cancelEvent()

            if (delayedBlink) {
                delayedBlinkActive = true
                delayedBlinkTimer.reset()
            } else {
                delayedVelocityTick = player.ticksExisted
            }

            delayedPackets += packet to (System.currentTimeMillis() + delayedDelay)
            return
        }

        if (delayedBlink && delayedBlinkActive && packet.javaClass.simpleName.startsWith("S", ignoreCase = true) && player.ticksExisted > 10) {
            event.cancelEvent()
            delayedPackets += packet to (System.currentTimeMillis() + delayedDelay)
            return
        }

        if (!delayedBlink && delayedDelayTransaction && packet is S32PacketConfirmTransaction && delayedVelocityTick == player.ticksExisted) {
            event.cancelEvent()
            delayedPackets += packet to (System.currentTimeMillis() + delayedDelay)
        }
    }

    private fun flushDelayedPackets(force: Boolean = false) {
        val now = System.currentTimeMillis()

        if (!force && delayedBlink && delayedBlinkActive && !delayedBlinkTimer.hasTimePassed(delayedDelay.toLong())) {
            return
        }

        val shouldUnblink = delayedBlinkActive
        val iterator = delayedPackets.iterator()
        while (iterator.hasNext()) {
            val (packet, releaseAt) = iterator.next()

            if (force || releaseAt <= now) {
                PacketUtils.schedulePacketProcess(packet)
                iterator.remove()
            }
        }

        if (delayedBlinkActive && (force || delayedPackets.isEmpty())) {
            delayedBlinkActive = false

            if (shouldUnblink && BlinkUtils.isBlinking) {
                BlinkUtils.unblink()
            }
        }

        if (force || delayedPackets.isEmpty()) {
            delayedVelocityTick = -1
        }
    }

    private fun refreshModeChoices(force: Boolean = false) {
        val observed = ServerObserver.guessAnticheat()?.takeUnless { it.equals("Unknown", true) }
        val fingerprint = "$antiCheat|${observed.orEmpty()}"
        if (!force && fingerprint == lastProfileFingerprint) return
        lastProfileFingerprint = fingerprint

        val available = AnticheatModeAdvisor.filteredModes("Velocity", antiCheat, observed, VELOCITY_MODES)
        modeValue.updateValues(available)
        if (available.none { it.equals(mode, true) }) {
            val previous = mode
            modeValue.set(available.first())
            if (state && !previous.equals(mode, true)) {
                hud.addNotification(Notification("Velocity compatibility",
                    "$previous unavailable here; switched to $mode.", Type.WARNING, 4500))
            }
        } else if (state) {
            warnAboutMode(mode)
        }
    }

    private fun warnAboutMode(selectedMode: String) {
        val observed = ServerObserver.guessAnticheat()?.takeUnless { it.equals("Unknown", true) }
        if (antiCheat.equals("Auto", true) && observed == null) return
        val advice = AnticheatModeAdvisor.assess("Velocity", selectedMode, antiCheat, observed)
        if (advice.risk == ModeRisk.RECOMMENDED) return

        val warningKey = "${advice.profile}|$selectedMode|${advice.risk}"
        if (warningKey == lastModeWarning) return
        lastModeWarning = warningKey
        val message = when (advice.risk) {
            ModeRisk.EXPERIMENTAL -> "$selectedMode is experimental on ${advice.profile.displayName}; setbacks are possible."
            ModeRisk.LIKELY_DETECTED -> "Probable detection: $selectedMode on ${advice.profile.displayName}. Prefer ${advice.recommendedMode}."
            ModeRisk.RECOMMENDED -> return
        }
        hud.addNotification(Notification("Velocity compatibility", message, Type.WARNING, 4500))
    }

    private fun effectiveSimpleFactor(base: Float): Float {
        if (base == 0f || (!simpleJitter && !simplePingScale)) {
            return base
        }

        var factor = base

        if (simplePingScale && simplePingScaleAmount > 0f) {
            val latency = ServerObserver.ping.coerceAtLeast(0)
            val pingRelax = (latency / 400.0).coerceIn(0.0, 1.0)
            val tps = ServerObserver.tps.takeIf(Double::isFinite) ?: 20.0
            val tpsRelax = ((20.0 - tps) / 10.0).coerceIn(0.0, 1.0)
            val relax = maxOf(pingRelax, tpsRelax) * simplePingScaleAmount
            factor += ((1f - base) * relax).toFloat()
        }

        if (simpleJitter && simpleJitterAmount > 0f) {
            factor += nextFloat(-simpleJitterAmount, simpleJitterAmount)
        }

        return factor.coerceIn(0f, 1f)
    }

    private fun isInsideGrimSetbackCooldown(): Boolean =
        ServerObserver.lastLagBackAt > 0L &&
            System.currentTimeMillis() - ServerObserver.lastLagBackAt < grimSetbackCooldown

    private fun grimAdaptiveFactor(): Double {
        if (isInsideGrimSetbackCooldown()) return 1.0

        val observed = ServerObserver.guessAnticheat()?.takeUnless { it.equals("Unknown", true) }
        val profile = AnticheatModeAdvisor.resolve(antiCheat, observed)
        val familyConfidence = when {
            profile == AnticheatProfile.GRIM -> 0.85
            antiCheat.equals("All", true) -> 0.4
            else -> 0.55
        }
        val tpsConfidence = (ServerObserver.tps.takeIf(Double::isFinite) ?: 18.0).div(20.0).coerceIn(0.35, 1.0)
        val pingConfidence = (1.0 - (ServerObserver.ping.coerceAtLeast(0) / 900.0)).coerceIn(0.35, 1.0)
        val setbackConfidence = (1.0 / (1.0 + ServerObserver.lagBackCount * 0.18)).coerceIn(0.35, 1.0)
        val confidence = familyConfidence * tpsConfidence * pingConfidence * setbackConfidence
        val reduction = 1.0 - (1.0 - grimAdaptiveHorizontal) * confidence
        val variance = sin((mc.thePlayer?.ticksExisted ?: 0) * 1.618 + ServerObserver.ping) *
            grimAdaptiveUncertainty * (1.0 - confidence * 0.5)
        return (reduction + variance).coerceIn(grimAdaptiveHorizontal.toDouble(), 1.0)
    }

    private fun checkGrimC07Block(pos: BlockPos, world: net.minecraft.client.multiplayer.WorldClient): Boolean {
        if (grimC07OnlyBreakAir && !world.isAirBlock(pos)) {
            return false
        }

        if (grimC07SendC03) {
            if (grimC07SendC06) {
                sendPacket(
                    C03PacketPlayer.C06PacketPlayerPosLook(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        mc.thePlayer.rotationYaw,
                        mc.thePlayer.rotationPitch,
                        mc.thePlayer.onGround
                    )
                )
            } else {
                sendPacket(C03PacketPlayer(mc.thePlayer.onGround))
            }
        }

        sendPacket(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, DOWN))

        if (grimC07BreakOnWorld) {
            world.setBlockToAir(pos)
        }

        return true
    }

    val onJump = handler<JumpEvent> { event ->
        val thePlayer = mc.thePlayer

        if (thePlayer == null || thePlayer.isInLiquid || thePlayer.isInWeb)
            return@handler

        when (mode.lowercase()) {
            "aacpush" -> {
                jump = true

                if (!thePlayer.isCollidedVertically)
                    event.cancelEvent()
            }

            "aaczero" ->
                if (thePlayer.hurtTime > 0)
                    event.cancelEvent()
        }
    }

    val onStrafe = handler<StrafeEvent> {
        val player = mc.thePlayer ?: return@handler

        if (mode == "Jump" && hasReceivedVelocity) {
            if (!ServerObserver.hasRecentLagBack && !player.isJumping && nextInt(endExclusive = 100) < chance && shouldJump() && player.isSprinting && player.onGround && player.hurtTime == 9) {
                player.tryJump()
                limitUntilJump = 0
            }
            hasReceivedVelocity = false
            return@handler
        }

        if (mode == "SideStrafe") {
            if (shouldBlockLegacyVelocity(player)) {
                return@handler
            }

            val pos = sideStrafePos ?: return@handler

            if (player.hurtTime <= 0) {
                return@handler
            }

            val rot = RotationUtils.getRotations(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

            if (sideStrafeFace) {
                player.rotationYaw = rot.yaw
                player.rotationYawHead = rot.yaw
                player.renderYawOffset = rot.yaw
            }

            val yaw = rot.yaw
            if (sideStrafeStrafe) {
                val moveSpeed = MovementUtils.speed
                val yawRad = Math.toRadians(yaw.toDouble())
                player.motionX = -kotlin.math.sin(yawRad) * moveSpeed
                player.motionZ = kotlin.math.cos(yawRad) * moveSpeed
            } else {
                var strafe = it.strafe
                var forward = it.forward
                var friction = it.friction
                var f = strafe * strafe + forward * forward

                if (f >= 1.0E-4F) {
                    f = MathHelper.sqrt_float(f)

                    if (f < 1.0F) {
                        f = 1.0F
                    }

                    friction /= f
                    strafe *= friction
                    forward *= friction

                    val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                    val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                    player.motionX += strafe * yawCos - forward * yawSin
                    player.motionZ += forward * yawCos + strafe * yawSin
                }
            }
        }

        when (jumpCooldownMode.lowercase()) {
            "ticks" -> limitUntilJump++
            "receivedhits" -> if (player.hurtTime == 9) limitUntilJump++
        }
    }

    val onBlockBB = handler<BlockBBEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (mode == "GhostBlock") {
            if (hasReceivedVelocity) {
                if (player.hurtTime in hurtTimeRange) {
                    // Check if there is air exactly 1 level above the player's Y position
                    if (event.block is BlockAir && event.y == mc.thePlayer.posY.toInt() + 1) {
                        event.boundingBox = AxisAlignedBB(
                            event.x.toDouble(),
                            event.y.toDouble(),
                            event.z.toDouble(),
                            event.x + 1.0,
                            event.y + 1.0,
                            event.z + 1.0
                        )
                    }
                } else if (player.hurtTime == 0) {
                    hasReceivedVelocity = false
                }
            }
        }
    }

    private fun shouldJump() = when (jumpCooldownMode.lowercase()) {
        "ticks" -> limitUntilJump >= ticksUntilJump
        "receivedhits" -> limitUntilJump >= hitsUntilJump
        else -> false
    }

    private fun handleVelocity(event: PacketEvent) {
        val packet = event.packet

        val horizontal = effectiveSimpleFactor(this.horizontal)
        val vertical = effectiveSimpleFactor(this.vertical)

        if (packet is S12PacketEntityVelocity) {
            // Always cancel event and handle motion from here
            event.cancelEvent()

            if (horizontal == 0f && vertical == 0f)
                return

            // Don't modify player's motionXZ when horizontal value is 0
            if (horizontal != 0f) {
                var motionX = packet.realMotionX
                var motionZ = packet.realMotionZ

                if (limitMaxMotionValue.get()) {
                    val distXZ = sqrt(motionX * motionX + motionZ * motionZ)

                    if (distXZ > maxXZMotion) {
                        val ratioXZ = maxXZMotion / distXZ

                        motionX *= ratioXZ
                        motionZ *= ratioXZ
                    }
                }

                mc.thePlayer.motionX = motionX * horizontal
                mc.thePlayer.motionZ = motionZ * horizontal
            }

            // Don't modify player's motionY when vertical value is 0
            if (vertical != 0f) {
                var motionY = packet.realMotionY

                if (limitMaxMotionValue.get())
                    motionY = motionY.coerceAtMost(maxYMotion + 0.00075)

                mc.thePlayer.motionY = motionY * vertical
            }
        } else if (packet is S27PacketExplosion) {
            // Don't cancel explosions, modify them, they could change blocks in the world
            if (horizontal != 0f && vertical != 0f) {
                packet.field_149152_f = 0f
                packet.field_149153_g = 0f
                packet.field_149159_h = 0f

                return
            }

            // Unlike with S12PacketEntityVelocity explosion packet motions get added to player motion, doesn't replace it
            // Velocity might behave a bit differently, especially LimitMaxMotion
            packet.field_149152_f *= horizontal // motionX
            packet.field_149153_g *= vertical // motionY
            packet.field_149159_h *= horizontal // motionZ

            if (limitMaxMotionValue.get()) {
                val distXZ =
                    sqrt(packet.field_149152_f * packet.field_149152_f + packet.field_149159_h * packet.field_149159_h)
                val distY = packet.field_149153_g
                val maxYMotion = maxYMotion + 0.00075f

                if (distXZ > maxXZMotion) {
                    val ratioXZ = maxXZMotion / distXZ

                    packet.field_149152_f *= ratioXZ
                    packet.field_149159_h *= ratioXZ
                }

                if (distY > maxYMotion) {
                    packet.field_149153_g *= maxYMotion / distY
                }
            }
        }
    }

    private fun getNearestEntityInRange(range: Float = this.range): Entity? {
        val player = mc.thePlayer ?: return null

        return mc.theWorld.loadedEntityList.filter {
            isSelected(it, true) && player.getDistanceToEntityBox(it) <= range
        }.minByOrNull { player.getDistanceToEntityBox(it) }
    }
}
