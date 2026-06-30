/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.modeNames
import net.ccbluex.liquidbounce.features.module.modules.selectedMode
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.aac.AACv1
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.aac.AACv2
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.aac.AACv3
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.matrix.MatrixFlag
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.matrix.OldMatrixHurt
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.ncp.NCPLatest
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.ncp.NCP
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.ncp.OldNCPDamage
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.Boost
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.Buzz
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.Hycraft
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.Redesky
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.VerusDamage
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other.VerusDamage.damaged
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.vulcan.VulcanLongJumpMode
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.grim.Grim
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.hypixel.Hypixel
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.spartan.Spartan
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump

object LongJump : Module("LongJump", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    private val longJumpModes = arrayOf(
        // NCP
        NCP, NCPLatest, OldNCPDamage,

        // AAC
        AACv1, AACv2, AACv3,

        // Matrix
        MatrixFlag, OldMatrixHurt,

        // Other
        Boost, Redesky, Hycraft, Buzz, VerusDamage,

        // Vulcan
        VulcanLongJumpMode,

        // Grim
        Grim,

        // Hypixel
        Hypixel,

        // Spartan
        Spartan
    )

    val mode by choices("Mode", longJumpModes.modeNames(), "NCP")
        .describe("Anticheat bypass method used for the long jump.")
    val ncpBoost by float("NCPBoost", 4.25f, 1f..10f) { mode == "NCP" }
        .describe("Forward boost strength for the NCP mode.")
    val autoJumpValue by boolean("AutoJump", true)
        .describe("Automatically jump when moving on the ground.")
    val autoJumpNotWhileUsingItemValue by boolean("AutoJumpNotWhileUsingItem", false) { autoJumpValue }
        .describe("Do not auto jump while using an item.")
    val autoDisableValue by boolean("AutoDisable", true)
        .describe("Disable the module after landing.")
    val timerValue by float("GlobalTimer", 1f, 0.1f..2f)
        .describe("Game timer speed applied during the jump.")
    val onlyAirValue by boolean("TimerOnlyAir", true)
        .describe("Only change the timer while in the air.")
    val resetTimerOnGroundValue by boolean("ResetTimerOnGround", false)
        .describe("Reset the timer to normal when on the ground.")
    val legacyWarningValue by boolean("LegacyWarn", true)
        .describe("Warn when using a bypass for an outdated anticheat.")

    val boostSpeed by float("Boost-Speed", 0.48f, 0f..3f) { mode == "Boost" }
        .describe("Forward speed for the Boost mode.")
    val boostJumpBoost by float("Boost-JumpBoost", 1.5f, 1f..3f) { mode == "Boost" }
        .describe("Vertical boost factor for the Boost mode.")
    val boostStrafeBoost by float("Boost-StrafeBoost", 1.5f, 1f..3f) { mode == "Boost" }
        .describe("Strafe boost factor for the Boost mode.")

    val ncpLatestBoost by float("NCPLatest-Boost", 10f, 1f..10f) { mode == "NCPLatest" }
        .describe("Forward boost strength for the NCPLatest mode.")
    val ncpLatestBlink by boolean("NCPLatest-Blink", false) { mode == "NCPLatest" }
        .describe("Use blinking during the NCPLatest jump.")
    val ncpLatestOldMMC by boolean("NCPLatest-OldMMC", false) { mode == "NCPLatest" }
        .describe("Use the old MMC variant of the NCPLatest mode.")
    val ncpLatestWarn by boolean("NCPLatest-Warn", true) { mode == "NCPLatest" }
        .describe("Warn that the NCPLatest bypass may be outdated.")

    val oldNcpDamageMode by choices("OldNCPDamage-Mode", arrayOf("Normal", "OldHypixel"), "Normal") {
        mode == "OldNCPDamage"
    }
        .describe("Boost variant used by the OldNCPDamage mode.")
    val oldNcpDamageBoostSpeed by float("OldNCPDamage-BoostSpeed", 1.2f, 1f..2f) {
        mode == "OldNCPDamage" && oldNcpDamageMode == "OldHypixel"
    }
        .describe("Speed multiplier for the OldHypixel variant.")
    val oldNcpDamageBoost by float("OldNCPDamage-Boost", 4.25f, 1f..10f) {
        mode == "OldNCPDamage" && oldNcpDamageMode == "Normal"
    }
        .describe("Forward boost distance for the Normal variant.")
    val oldNcpDamageInstant by boolean("OldNCPDamage-DamageInstant", false) { mode == "OldNCPDamage" }
        .describe("Trigger the boost instantly on damage.")

    val oldMatrixHurtBoostSpeed by float("OldMatrixHurt-BoostSpeed", 0.416f, 0.1f..1f) { mode == "OldMatrixHurt" }
        .describe("Boost speed for the OldMatrixHurt mode.")
    val oldMatrixHurtTicks by int("OldMatrixHurt-Ticks", 10, 5..20) { mode == "OldMatrixHurt" }
        .describe("Number of ticks to boost in OldMatrixHurt mode.")

    val vulcanRepeatTimes by int("Vulcan-RepeatTimes", 2, 1..6) { mode == "Vulcan" }
        .describe("How many times to repeat the Vulcan boost.")
    val vulcanDistance by float("Vulcan-Distance", 7f, 2f..8f) { mode == "Vulcan" }
        .describe("Target jump distance for the Vulcan mode.")
    val vulcanOnlyDamage by boolean("Vulcan-OnlyDamage", true) { mode == "Vulcan" }
        .describe("Only boost when taking damage in Vulcan mode.")
    val vulcanSelfDamage by boolean("Vulcan-SelfDamage", true) { mode == "Vulcan" }
        .describe("Deal self damage to trigger the Vulcan boost.")

    private val generalGroup = Configurable("General")
    private val timerGroup = Configurable("Timer")
    private val boostGroup = Configurable("Boost")
    private val ncpLatestGroup = Configurable("NCPLatest")
    private val oldNcpDamageGroup = Configurable("OldNCPDamage")
    private val oldMatrixHurtGroup = Configurable("OldMatrixHurt")
    private val vulcanGroup = Configurable("Vulcan")

    init {
        moveValues(generalGroup,
            "Mode", "NCPBoost", "AutoJump", "AutoJumpNotWhileUsingItem", "AutoDisable", "LegacyWarn")

        moveValues(timerGroup, "GlobalTimer", "TimerOnlyAir", "ResetTimerOnGround")

        moveValues(boostGroup, "Boost-Speed", "Boost-JumpBoost", "Boost-StrafeBoost")

        moveValues(ncpLatestGroup,
            "NCPLatest-Boost", "NCPLatest-Blink", "NCPLatest-OldMMC", "NCPLatest-Warn")

        moveValues(oldNcpDamageGroup,
            "OldNCPDamage-Mode", "OldNCPDamage-BoostSpeed", "OldNCPDamage-Boost", "OldNCPDamage-DamageInstant")

        moveValues(oldMatrixHurtGroup, "OldMatrixHurt-BoostSpeed", "OldMatrixHurt-Ticks")

        moveValues(vulcanGroup,
            "Vulcan-RepeatTimes", "Vulcan-Distance", "Vulcan-OnlyDamage", "Vulcan-SelfDamage")

        addValues(listOf(
            generalGroup, timerGroup, boostGroup, ncpLatestGroup,
            oldNcpDamageGroup, oldMatrixHurtGroup, vulcanGroup
        ))
    }

    private fun moveValues(group: Configurable, vararg names: String) {
        for (name in names) {
            values.filter { it.matchesKey(name) }.forEach(group::addValue)
        }
    }

    var jumped = false
    var canBoost = false
    var teleported = false
    var airTick = 0
    var noTimerModify = false

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if ((!onlyAirValue || !player.onGround) && !noTimerModify) {
            mc.timer.timerSpeed = timerValue
        } else if (resetTimerOnGroundValue && player.onGround) {
            mc.timer.timerSpeed = 1f
        }

        if (!player.onGround) {
            airTick++
        } else {
            if (airTick > 1 && autoDisableValue) {
                modeModule.onAttemptDisable()
            } else if (!autoDisableValue) {
                airTick = 0
            }
        }

        if (jumped && (player.onGround || player.capabilities.isFlying)) {
            jumped = false

            if (mode == "NCP") {
                player.motionX = 0.0
                player.motionZ = 0.0
            }
        }

        modeModule.onUpdate()

        if (autoJumpValue && player.onGround && player.isMoving && airTick < 2) {
            if (autoJumpNotWhileUsingItemValue && player.isUsingItem) {
                return@handler
            }

            if (mode == "VerusDamage" && !damaged) {
                return@handler
            }

            jumped = true
            modeModule.onAttemptJump()
        }
    }

    val onMove = handler<MoveEvent> { event ->
        modeModule.onMove(event)
    }

    val onMotion = handler<MotionEvent> { event ->
        modeModule.onMotion(event)
    }

    val onPacket = handler<PacketEvent> { event ->
        modeModule.onPacket(event)
    }

    val onBlockBB = handler<BlockBBEvent> { event ->
        modeModule.onBlockBB(event)
    }

    val onStep = handler<StepEvent> { event ->
        modeModule.onStep(event)
    }

    override fun onEnable() {
        airTick = 0
        noTimerModify = false
        jumped = false
        canBoost = false
        teleported = false
        modeModule.onEnable()
    }

    override fun onDisable() {
        noTimerModify = false
        mc.thePlayer?.run {
            capabilities.isFlying = false
            capabilities.flySpeed = 0.05f
            noClip = false
            jumpMovementFactor = 0.02f
        }
        mc.timer.timerSpeed = 1f
        modeModule.onDisable()
    }

    val onJump = handler<JumpEvent>(always = true) { event ->
        jumped = true
        canBoost = true
        teleported = false

        if (handleEvents()) {
            modeModule.onJump(event)
        }
    }

    override val tag
        get() = mode

    fun sendLegacyWarning(message: String = "This bypass is for an outdated anti cheat version!") {
        if (!legacyWarningValue) {
            return
        }

        chat(message)
    }

    private val modeModule
        get() = longJumpModes.selectedMode(mode)
}
