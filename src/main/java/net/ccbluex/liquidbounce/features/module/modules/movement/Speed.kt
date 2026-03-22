/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.modeNames
import net.ccbluex.liquidbounce.features.module.modules.selectedMode
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac.AACHop3313
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac.AACHop350
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac.AACHop4
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac.AACHop5
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.hypixel.HypixelHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.hypixel.HypixelLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.intave.IntaveHop14
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.intave.IntaveTimer14
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix.MatrixHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix.MatrixSlowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix.MatrixSpeeds
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix.OldMatrixHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spartan.SpartanYPort
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spectre.SpectreBHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spectre.SpectreLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spectre.SpectreOnGround
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.verus.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.vulcan.VulcanGround288
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.vulcan.VulcanHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.vulcan.VulcanLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.vulcan.VulcanSpeeds
import net.ccbluex.liquidbounce.utils.extensions.isMoving

object Speed : Module("Speed", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    private val speedModes = arrayOf(

        // NCP
        NCPBHop,
        NCPFHop,
        SNCPBHop,
        NCPHop,
        NCPYPort,
        UNCPHop,
        UNCPHopNew,

        // AAC
        AACHop3313,
        AACHop350,
        AACHop4,
        AACHop5,

        // Spartan
        SpartanYPort,

        // Spectre
        SpectreLowHop,
        SpectreBHop,
        SpectreOnGround,

        // Verus
        VerusHop,
        VerusFHop,
        VerusLowHop,
        VerusLowHopNew,
        LatestVerusHop,
        VerusSpeeds,

        // Vulcan
        VulcanHop,
        VulcanLowHop,
        VulcanGround288,
        VulcanSpeeds,

        // Matrix
        OldMatrixHop,
        MatrixHop,
        MatrixSlowHop,
        MatrixSpeeds,

        // Intave
        IntaveHop14,
        IntaveTimer14,

        // Server specific
        TeleportCubeCraft,
        HypixelHop,
        HypixelLowHop,
        BlocksMCHop,

        // Other
        BlocksMCSpeed,
        Boost,
        Frame,
        MiJump,
        OnGround,
        SlowHop,
        Legit,
        CustomSpeed
    )

    /**
     * Old/Deprecated Modes
     */
    private val deprecatedMode = arrayOf(
        TeleportCubeCraft,

        OldMatrixHop,

        VerusLowHop,

        SpectreLowHop, SpectreBHop, SpectreOnGround,

        AACHop3313, AACHop350, AACHop4,

        NCPBHop, NCPFHop, SNCPBHop, NCPHop, NCPYPort,

        MiJump, Frame
    )

    private val showDeprecated by boolean("DeprecatedMode", true).onChanged { value ->
        mode.changeValue(modesList.first { it !in deprecatedMode }.modeName)
        mode.updateValues(modesList.filter { value || it !in deprecatedMode }.modeNames())
    }

    private var modesList = speedModes

    val mode = choices("Mode", modesList.modeNames(), "NCPBHop")

    // Custom Speed
    val customBehavior by choices("CustomBehavior", arrayOf("Current", "Legacy"), "Current") { mode.get() == "Custom" }
    val customY by float("CustomY", 0.42f, 0f..4f) { mode.get() == "Custom" }
    val customGroundStrafe by float("CustomGroundStrafe", 1.6f, 0f..2f) { mode.get() == "Custom" }
    val customAirStrafe by float("CustomAirStrafe", 0f, 0f..2f) { mode.get() == "Custom" }
    val customGroundTimer by float("CustomGroundTimer", 1f, 0.1f..2f) { mode.get() == "Custom" }
    val customAirTimerTick by int("CustomAirTimerTick", 5, 1..20) { mode.get() == "Custom" }
    val customAirTimer by float("CustomAirTimer", 1f, 0.1f..2f) { mode.get() == "Custom" }
    val legacyCustomSpeed by float("CustomSpeed", 1.6f, 0f..2f) { mode.get() == "Custom" && customBehavior == "Legacy" }
    val legacyCustomDoLaunchSpeed by boolean("CustomDoLaunchSpeed", true) { mode.get() == "Custom" && customBehavior == "Legacy" }
    val legacyCustomLaunchSpeed by float("CustomLaunchSpeed", 1.6f, 0.2f..2f) {
        mode.get() == "Custom" && customBehavior == "Legacy" && legacyCustomDoLaunchSpeed
    }
    val legacyCustomLaunchMoveBeforeJump by boolean("CustomLaunchMoveBeforeJump", false) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomDoMinimumSpeed by boolean("CustomDoMinimumSpeed", true) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomMinimumSpeed by float("CustomMinimumSpeed", 0.25f, 0.1f..2f) {
        mode.get() == "Custom" && customBehavior == "Legacy" && legacyCustomDoMinimumSpeed
    }
    val legacyCustomAddYMotion by float("CustomAddYMotion", 0f, 0f..2f) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomDoModifyJumpY by boolean("CustomDoModifyJumpY", true) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomUpTimer by float("CustomUpTimer", 1f, 0.1f..2f) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomJumpTimer by float("CustomJumpTimer", 1.25f, 0.1f..2f) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomDownTimer by float("CustomDownTimer", 1f, 0.1f..2f) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomUpAirSpeed by float("CustomUpAirSpeed", 2.03f, 0.5f..3.5f) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomDownAirSpeed by float("CustomDownAirSpeed", 2.01f, 0.5f..3.5f) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomStrafe by choices(
        "CustomStrafe",
        arrayOf("Strafe", "Boost", "AirSpeed", "Plus", "PlusOnlyUp", "PlusOnlyDown", "Non-Strafe"),
        "Boost"
    ) { mode.get() == "Custom" && customBehavior == "Legacy" }
    val legacyCustomPlusMode by choices("PlusBoostMode", arrayOf("Add", "Multiply"), "Add") {
        mode.get() == "Custom" && customBehavior == "Legacy" &&
            legacyCustomStrafe in arrayOf("Plus", "PlusOnlyUp", "PlusOnlyDown")
    }
    val legacyCustomPlusMultiplyAmount by float("PlusMultiplyAmount", 1.1f, 1f..2f) {
        mode.get() == "Custom" && customBehavior == "Legacy" &&
            legacyCustomPlusMode == "Multiply" &&
            legacyCustomStrafe in arrayOf("Plus", "PlusOnlyUp", "PlusOnlyDown")
    }
    val legacyCustomGroundStay by int("CustomGroundStay", 0, 0..10) { mode.get() == "Custom" && customBehavior == "Legacy" }
    val legacyCustomGroundResetXZ by boolean("CustomGroundResetXZ", false) { mode.get() == "Custom" && customBehavior == "Legacy" }
    val legacyCustomDoJump by boolean("CustomDoJump", true) { mode.get() == "Custom" && customBehavior == "Legacy" }
    val legacyCustomPressSpaceKeyOnGround by boolean("CustomPressSpaceKeyOnGround", true) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomPressSpaceKeyInAir by boolean("CustomPressSpaceKeyInAir", false) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }
    val legacyCustomUsePreMotion by boolean("CustomUsePreMotion", true) {
        mode.get() == "Custom" && customBehavior == "Legacy"
    }

    // Extra options
    val resetXZ by boolean("ResetXZ", false) { mode.get() == "Custom" }
    val resetY by boolean("ResetY", false) { mode.get() == "Custom" }
    val notOnConsuming by boolean("NotOnConsuming", false) { mode.get() == "Custom" }
    val notOnFalling by boolean("NotOnFalling", false) { mode.get() == "Custom" }
    val notOnVoid by boolean("NotOnVoid", true) { mode.get() == "Custom" }

    // TeleportCubecraft Speed
    val cubecraftPortLength by float("CubeCraft-PortLength", 1f, 0.1f..2f)
    { mode.get() == "TeleportCubeCraft" }

    // IntaveHop14 Speed
    val boost by boolean("Boost", true) { mode.get() == "IntaveHop14" }
    val initialBoostMultiplier by float("InitialBoostMultiplier", 1f, 0.01f..10f)
    { boost && mode.get() == "IntaveHop14" }
    val intaveLowHop by boolean("LowHop", true) { mode.get() == "IntaveHop14" }
    val strafeStrength by float("StrafeStrength", 0.29f, 0.1f..0.29f)
    { mode.get() == "IntaveHop14" }
    val groundTimer by float("GroundTimer", 0.5f, 0.1f..5f) { mode.get() == "IntaveHop14" }
    val airTimer by float("AirTimer", 1.09f, 0.1f..5f) { mode.get() == "IntaveHop14" }

    // Matrix
    val matrixSpeed by choices("Matrix-Mode", arrayOf("MatrixHop2", "Matrix6.6.1", "Matrix6.9.2"), "MatrixHop2") { mode.get() == "MatrixSpeeds" }
    val matrixGroundStrafe by boolean("GroundStrafe-Hop2", false) { mode.get() == "MatrixSpeeds" }
    val matrixVeloBoostValue by boolean("VelocBoost-6.6.1", true) { mode.get() == "MatrixSpeeds" }
    val matrixTimerBoostValue by boolean("TimerBoost-6.6.1", false) { mode.get() == "MatrixSpeeds" }
    val matrixUsePreMotion by boolean("UsePreMotion6.6.1", false) { mode.get() == "MatrixSpeeds" }

    // VerusSpeed
    val verusSpeed by choices("Verus-Mode", arrayOf("OldHop", "Float", "Ground", "YPort", "YPort2"), "OldHop")  { mode.get() == "VerusSpeeds" }
    val verusYPortspeedValue by float("YPort-Speed", 0.61f, 0.1f.. 1f)  { mode.get() == "VerusSpeeds" }
    val verusYPort2speedValue by float("YPort2-Speed", 0.61f, 0.1f.. 1f)  { mode.get() == "VerusSpeeds" }
    val latestVerusHopCustomSpeed by boolean("LatestVerusHop-CustomSpeed", false) { mode.get() == "LatestVerusHop" }
    val latestVerusHopJumpMovementFactorWithPotion by float("LatestVerusHop-JumpMovementFactorWithPotion", 0.02f, 0.01f..0.04f) {
        mode.get() == "LatestVerusHop" && latestVerusHopCustomSpeed
    }
    val latestVerusHopJumpMovementFactorWithoutPotion by float("LatestVerusHop-JumpMovementFactorWithoutPotion", 0.02f, 0.01f..0.04f) {
        mode.get() == "LatestVerusHop" && latestVerusHopCustomSpeed
    }
    val latestVerusHopFrictionWithPotion by float("LatestVerusHop-FrictionWithPotion", 0.48f, 0.1f..2f) {
        mode.get() == "LatestVerusHop" && latestVerusHopCustomSpeed
    }
    val latestVerusHopFrictionWithoutPotion by float("LatestVerusHop-FrictionWithoutPotion", 0.48f, 0.1f..2f) {
        mode.get() == "LatestVerusHop" && latestVerusHopCustomSpeed
    }
    val latestVerusHopSpeedWithPotion by float("LatestVerusHop-SpeedWithPotion", 2.8f, 1f..4f) {
        mode.get() == "LatestVerusHop" && latestVerusHopCustomSpeed
    }
    val latestVerusHopSpeedWithoutPotion by float("LatestVerusHop-SpeedWithoutPotion", 2.0f, 1f..4f) {
        mode.get() == "LatestVerusHop" && latestVerusHopCustomSpeed
    }
    val latestVerusHopDamageBoost by boolean("LatestVerusHop-DamageBoost", false) { mode.get() == "LatestVerusHop" }
    val latestVerusHopBoostSpeed by float("LatestVerusHop-BoostSpeed", 1f, 0.1f..9f) {
        mode.get() == "LatestVerusHop" && latestVerusHopDamageBoost
    }

    // Vulcan legacy speed pack
    val vulcanMode by choices("Vulcan-Mode", arrayOf("LowHop", "Hop", "OldGround", "YPort", "YPort2", "LowHop2"), "LowHop") {
        mode.get() == "Vulcan"
    }
    val vulcanBoostDelay by int("Boost-Delay", 8, 2..15) { mode.get() == "Vulcan" && vulcanMode == "OldGround" }
    val vulcanGroundBoost by boolean("Ground-Boost", true) { mode.get() == "Vulcan" && vulcanMode == "OldGround" }

    // UNCPHopNew Speed
    private val pullDown by boolean("PullDown", true) { mode.get() == "UNCPHopNew" }
    val onTick by int("OnTick", 5, 5..9) { pullDown && mode.get() == "UNCPHopNew" }
    val onHurt by boolean("OnHurt", true) { pullDown && mode.get() == "UNCPHopNew" }
    val shouldBoost by boolean("ShouldBoost", true) { mode.get() == "UNCPHopNew" }
    val timerBoost by boolean("TimerBoost", true) { mode.get() == "UNCPHopNew" }
    val damageBoost by boolean("DamageBoost", true) { mode.get() == "UNCPHopNew" }
    val lowHop by boolean("LowHop", true) { mode.get() == "UNCPHopNew" }
    val airStrafe by boolean("AirStrafe", true) { mode.get() == "UNCPHopNew" }

    // MatrixHop Speed
    val matrixLowHop by boolean("LowHop", true)
    { mode.get() == "MatrixHop" || mode.get() == "MatrixSlowHop" }
    val extraGroundBoost by float("ExtraGroundBoost", 0.2f, 0f..0.5f)
    { mode.get() == "MatrixHop" || mode.get() == "MatrixSlowHop" }

    // HypixelLowHop Speed
    val glide by boolean("Glide", true) { mode.get() == "HypixelLowHop" }

    // BlocksMCHop Speed
    val fullStrafe by boolean("FullStrafe", true) { mode.get() == "BlocksMCHop" }
    val bmcLowHop by boolean("LowHop", true) { mode.get() == "BlocksMCHop" }
    val bmcDamageBoost by boolean("DamageBoost", true) { mode.get() == "BlocksMCHop" }
    val damageLowHop by boolean("DamageLowHop", false) { mode.get() == "BlocksMCHop" }
    val safeY by boolean("SafeY", true) { mode.get() == "BlocksMCHop" }

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (thePlayer.isSneaking)
            return@handler

        if (thePlayer.isMoving && !sprintManually)
            thePlayer.isSprinting = true

        modeModule.onUpdate()
    }

    val onMotion = handler<MotionEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        if (thePlayer.isSneaking || event.eventState != EventState.PRE)
            return@handler

        if (thePlayer.isMoving && !sprintManually)
            thePlayer.isSprinting = true

        modeModule.onMotion()
        modeModule.onPreMotion()
    }

    val onMove = handler<MoveEvent> { event ->
        if (mc.thePlayer?.isSneaking == true)
            return@handler

        modeModule.onMove(event)
    }

    val tickHandler = handler<GameTickEvent> {
        if (mc.thePlayer?.isSneaking == true)
            return@handler

        modeModule.onTick()
    }

    val onStrafe = handler<StrafeEvent> {
        if (mc.thePlayer?.isSneaking == true)
            return@handler

        modeModule.onStrafe()
    }

    val onJump = handler<JumpEvent> { event ->
        if (mc.thePlayer?.isSneaking == true)
            return@handler

        modeModule.onJump(event)
    }

    val onPacket = handler<PacketEvent> { event ->
        if (mc.thePlayer?.isSneaking == true)
            return@handler

        modeModule.onPacket(event)
    }

    override fun onEnable() {
        if (mc.thePlayer == null)
            return

        mc.timer.timerSpeed = 1f

        modeModule.onEnable()
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f

        modeModule.onDisable()
    }

    override val tag
        get() = mode.get()

    private val modeModule
        get() = speedModes.selectedMode(mode.get())

    private val sprintManually
        // Maybe there are more but for now there's the Legit mode.get().
        get() = modeModule in arrayOf(Legit)
}
