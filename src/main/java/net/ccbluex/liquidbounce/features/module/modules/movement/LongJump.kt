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
        VulcanLongJumpMode
    )

    val mode by choices("Mode", longJumpModes.modeNames(), "NCP")
    val ncpBoost by float("NCPBoost", 4.25f, 1f..10f) { mode == "NCP" }
    val autoJumpValue by boolean("AutoJump", true)
    val autoDisableValue by boolean("AutoDisable", true)
    val timerValue by float("GlobalTimer", 1f, 0.1f..2f)
    val onlyAirValue by boolean("TimerOnlyAir", true)
    val legacyWarningValue by boolean("LegacyWarn", true)

    val boostSpeed by float("Boost-Speed", 0.48f, 0f..3f) { mode == "Boost" }
    val boostJumpBoost by float("Boost-JumpBoost", 1.5f, 1f..3f) { mode == "Boost" }
    val boostStrafeBoost by float("Boost-StrafeBoost", 1.5f, 1f..3f) { mode == "Boost" }

    val ncpLatestBoost by float("NCPLatest-Boost", 10f, 1f..10f) { mode == "NCPLatest" }
    val ncpLatestBlink by boolean("NCPLatest-Blink", false) { mode == "NCPLatest" }
    val ncpLatestOldMMC by boolean("NCPLatest-OldMMC", false) { mode == "NCPLatest" }
    val ncpLatestWarn by boolean("NCPLatest-Warn", true) { mode == "NCPLatest" }

    val oldNcpDamageMode by choices("OldNCPDamage-Mode", arrayOf("Normal", "OldHypixel"), "Normal") {
        mode == "OldNCPDamage"
    }
    val oldNcpDamageBoostSpeed by float("OldNCPDamage-BoostSpeed", 1.2f, 1f..2f) {
        mode == "OldNCPDamage" && oldNcpDamageMode == "OldHypixel"
    }
    val oldNcpDamageBoost by float("OldNCPDamage-Boost", 4.25f, 1f..10f) {
        mode == "OldNCPDamage" && oldNcpDamageMode == "Normal"
    }
    val oldNcpDamageInstant by boolean("OldNCPDamage-DamageInstant", false) { mode == "OldNCPDamage" }

    val oldMatrixHurtBoostSpeed by float("OldMatrixHurt-BoostSpeed", 0.416f, 0.1f..1f) { mode == "OldMatrixHurt" }
    val oldMatrixHurtTicks by int("OldMatrixHurt-Ticks", 10, 5..20) { mode == "OldMatrixHurt" }

    val vulcanRepeatTimes by int("Vulcan-RepeatTimes", 2, 1..6) { mode == "Vulcan" }
    val vulcanDistance by float("Vulcan-Distance", 7f, 2f..8f) { mode == "Vulcan" }
    val vulcanOnlyDamage by boolean("Vulcan-OnlyDamage", true) { mode == "Vulcan" }
    val vulcanSelfDamage by boolean("Vulcan-SelfDamage", true) { mode == "Vulcan" }

    var jumped = false
    var canBoost = false
    var teleported = false
    var airTick = 0
    var noTimerModify = false

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if ((!onlyAirValue || !player.onGround) && !noTimerModify) {
            mc.timer.timerSpeed = timerValue
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
