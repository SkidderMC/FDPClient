/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.aac.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.blocksmc.BlocksMC
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.blocksmc.BlocksMC2
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.hypixel.BoostHypixel
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.hypixel.FreeHypixel
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.hypixel.Hypixel
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.ncp.NCP
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.ncp.OldNCP
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.spartan.BugSpartan
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.spartan.Spartan
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.spartan.Spartan2
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla.DefaultVanilla
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla.SmoothVanilla
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla.Vanilla
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.verus.Verus
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.verus.VerusGlide
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vulcan.Vulcan
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vulcan.VulcanGhost
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vulcan.VulcanOld
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.RotationSettings
import net.ccbluex.liquidbounce.utils.extensions.stop
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.lwjgl.input.Keyboard
import java.awt.Color

object Flight : Module("Fly", Category.MOVEMENT, Keyboard.KEY_F, hideModule = false) {
    private val flyModes = arrayOf(
        Vanilla, SmoothVanilla, DefaultVanilla,

        // NCP
        NCP, OldNCP,

        // AAC
        AAC1910, AAC305, AAC316, AAC3312, AAC3312Glide, AAC3313,

        // CubeCraft
        CubeCraft,

        // Hypixel
        Hypixel, BoostHypixel, FreeHypixel,

        // Other server specific flys
        NeruxVace, Minesucht, BlocksMC, BlocksMC2,

        // Spartan
        Spartan, Spartan2, BugSpartan,

        // Vulcan
        Vulcan, VulcanOld, VulcanGhost,

        // Verus
        Verus, VerusGlide,

        // Other anti-cheats
        MineSecure, HawkEye, HAC, WatchCat,

        // Other
        Jetpack, KeepAlive, Collide, Jump, Flag, Fireball
    )

    /**
     * Old/Deprecated Modes
     */
    private val deprecatedMode = arrayOf(
        Spartan, Spartan2, BugSpartan,

        MineSecure, HawkEye, HAC, WatchCat, NeruxVace, Minesucht,

        BlocksMC, BlocksMC2,

        Hypixel, BoostHypixel, FreeHypixel,

        NCP, OldNCP,

        AAC1910, AAC305, AAC316, AAC3312, AAC3312Glide, AAC3313,

        CubeCraft
    )

    private val showDeprecatedValue: BoolValue = object : BoolValue("DeprecatedMode", true) {
        override fun onUpdate(value: Boolean) {
            modeValue.changeValue(modesList.first { it !in deprecatedMode }.modeName)
            modeValue.updateValues(modesList.filter { value || it !in deprecatedMode }.map { it.modeName }
                .toTypedArray())
        }
    }

    private val showDeprecated by showDeprecatedValue

    private var modesList = flyModes

    val modeValue = choices("Mode", modesList.map { it.modeName }.toTypedArray(), "Vanilla")
    val mode by modeValue

    val vanillaSpeed by float("VanillaSpeed", 2f, 0f..10f, subjective = true) {
        mode in arrayOf("Vanilla",
            "KeepAlive",
            "MineSecure",
            "BugSpartan"
        )
    }
    private val vanillaKickBypass by boolean("VanillaKickBypass",
        false,
        subjective = true
    ) { mode in arrayOf("Vanilla", "SmoothVanilla") }
    val ncpMotion by float("NCPMotion", 0f, 0f..1f) { mode == "NCP" }

    val smoothValue by boolean("Smooth", false) { mode == "DefaultVanilla" }
    val speedValue by float("Speed", 2f, 0f.. 5f) { mode == "DefaultVanilla" }
    val vspeedValue by float("Vertical", 2f, 0f..5f) { mode == "DefaultVanilla" }
    val kickBypassValue by boolean("KickBypass", false) { mode == "DefaultVanilla" }
    val kickBypassModeValue by choices("KickBypassMode", arrayOf("Motion", "Packet"), "Packet") {  kickBypassValue }
    val kickBypassMotionSpeedValue by float("KickBypass-MotionSpeed", 0.0626F, 0.05F..0.1F) { kickBypassModeValue == "Motion" && kickBypassValue }
    val keepAliveValue by boolean("KeepAlive", false) { mode == "DefaultVanilla" }
    val noClipValue by boolean("NoClip", false) { mode == "DefaultVanilla" }
    val spoofValue by boolean("SpoofGround", false) { mode == "DefaultVanilla" }

    // AAC
    val aacSpeed by float("AAC1.9.10-Speed", 0.3f, 0f..1f) { mode == "AAC1.9.10" }
    val aacFast by boolean("AAC3.0.5-Fast", true) { mode == "AAC3.0.5" }
    val aacMotion by float("AAC3.3.12-Motion", 10f, 0.1f..10f) { mode == "AAC3.3.12" }
    val aacMotion2 by float("AAC3.3.13-Motion", 10f, 0.1f..10f) { mode == "AAC3.3.13" }

    // Hypixel
    val hypixelBoost by boolean("Hypixel-Boost", true) { mode == "Hypixel" }
    val hypixelBoostDelay by int("Hypixel-BoostDelay", 1200, 50..2000) { mode == "Hypixel" && hypixelBoost }
    val hypixelBoostTimer by float("Hypixel-BoostTimer", 1f, 0.1f..5f) { mode == "Hypixel" && hypixelBoost }

    // Other
    val neruxVaceTicks by int("NeruxVace-Ticks", 6, 2..20) { mode == "NeruxVace" }

    // Verus
    val damage by boolean("Damage", false) { mode == "Verus" }
    val timerSlow by boolean("TimerSlow", true) { mode == "Verus" }
    val boostTicksValue by int("BoostTicks", 20, 1..30) { mode == "Verus" }
    val boostMotion by float("BoostMotion", 6.5f, 1f..9.85f) { mode == "Verus" }
    val yBoost by float("YBoost", 0.42f, 0f..10f) { mode == "Verus" }

    // BlocksMC
    val stable by boolean("Stable", false) { mode == "BlocksMC" || mode == "BlocksMC2" }
    val timerSlowed by boolean("TimerSlowed", true) { mode == "BlocksMC" || mode == "BlocksMC2" }
    val boostSpeed by float("BoostSpeed", 6f, 1f..15f) { mode == "BlocksMC" || mode == "BlocksMC2" }
    val extraBoost by float("ExtraSpeed", 1f, 0.0F..2f) { mode == "BlocksMC" || mode == "BlocksMC2" }
    val stopOnLanding by boolean("StopOnLanding", true) { mode == "BlocksMC" || mode == "BlocksMC2" }
    val stopOnNoMove by boolean("StopOnNoMove", false) { mode == "BlocksMC" || mode == "BlocksMC2" }
    val debugFly by boolean("Debug", false) { mode == "BlocksMC" || mode == "BlocksMC2" }

    // Fireball
    val pitchMode by choices("PitchMode", arrayOf("Custom", "Smart"), "Custom") { mode == "Fireball" }
    val rotationPitch by float("Pitch", 90f, 0f..90f) { pitchMode != "Smart" && mode == "Fireball" }
    val invertYaw by boolean("InvertYaw", true) { pitchMode != "Smart" && mode == "Fireball" }

    val autoFireball by choices("AutoFireball",
        arrayOf("Off", "Pick", "Spoof", "Switch"),
        "Spoof"
    ) { mode == "Fireball" }
    val swing by boolean("Swing", true) { mode == "Fireball" }
    val fireballTry by int("MaxFireballTry", 1, 0..2) { mode == "Fireball" }
    val fireBallThrowMode by choices("FireballThrow", arrayOf("Normal", "Edge"), "Normal") { mode == "Fireball" }
    val edgeThreshold by float("EdgeThreshold",
        1.05f,
        1f..2f
    ) { fireBallThrowMode == "Edge" && mode == "Fireball" }

    val options = RotationSettings(this) { mode == "Fireball" }.apply {
        resetTicksValue.setSupport { { it && keepRotation } }
    }

    val autoJump by boolean("AutoJump", true) { mode == "Fireball" }

    // Visuals
    private val mark by boolean("Mark", true, subjective = true)

    var wasFired = false
    var firePosition: BlockPos? = null

    var jumpY = 0.0

    var startY = 0.0
        private set

    private val groundTimer = MSTimer()
    private var wasFlying = false

    override fun onEnable() {
        val thePlayer = mc.thePlayer ?: return

        startY = thePlayer.posY
        jumpY = thePlayer.posY
        wasFlying = mc.thePlayer.capabilities.isFlying

        modeModule.onEnable()
    }

    override fun onDisable() {
        val thePlayer = mc.thePlayer ?: return

        if (!mode.startsWith("AAC") && mode != "Hypixel" && mode != "VerusGlide"
            && mode != "SmoothVanilla" && mode != "Vanilla" && mode != "Rewinside"
            && mode != "Fireball" && mode != "Collide" && mode != "Jump") {

            if (mode == "CubeCraft") thePlayer.stopXZ()
            else thePlayer.stop()
        }

        wasFired = false
        firePosition = null
        thePlayer.capabilities.isFlying = wasFlying
        mc.timer.timerSpeed = 1f
        thePlayer.speedInAir = 0.02f

        modeModule.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        modeModule.onUpdate()
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        if (mode == "Fireball" && wasFired) {
            WaitTickUtils.schedule(2) {
                Flight.state = false
            }
        }

        modeModule.onTick()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!mark || mode == "Vanilla" || mode == "SmoothVanilla" || mode == "DefaultVanilla")
            return

        val y = startY + 2.0 + (if (mode == "BoostHypixel") 0.42 else 0.0)
        drawPlatform(
            y,
            if (mc.thePlayer.entityBoundingBox.maxY < y) Color(0, 255, 0, 90) else Color(255, 0, 0, 90),
            1.0
        )

        modeModule.onRender3D(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return

        modeModule.onPacket(event)
    }

    @EventTarget
    fun onBB(event: BlockBBEvent) {
        mc.thePlayer ?: return

        modeModule.onBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        modeModule.onJump(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        modeModule.onStep(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        modeModule.onMotion(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        modeModule.onMove(event)
    }

    fun handleVanillaKickBypass() {
        if (!vanillaKickBypass || !groundTimer.hasTimePassed(1000)) return
        val ground = calculateGround() + 0.5
        run {
            var posY = mc.thePlayer.posY
            while (posY > ground) {
                sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true))
                if (posY - 8.0 < ground) break // Prevent next step
                posY -= 8.0
            }
        }
        sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, ground, mc.thePlayer.posZ, true))
        var posY = ground
        while (posY < mc.thePlayer.posY) {
            sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true))
            if (posY + 8.0 > mc.thePlayer.posY) break // Prevent next step
            posY += 8.0
        }
        sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        groundTimer.reset()
    }

    // TODO: Make better and faster calculation lol
    private fun calculateGround(): Double {
        val playerBoundingBox = mc.thePlayer.entityBoundingBox
        var blockHeight = 0.05
        var ground = mc.thePlayer.posY
        while (ground > 0.0) {
            val customBox = AxisAlignedBB.fromBounds(
                playerBoundingBox.maxX,
                ground + blockHeight,
                playerBoundingBox.maxZ,
                playerBoundingBox.minX,
                ground,
                playerBoundingBox.minZ
            )
            if (mc.theWorld.checkBlockCollision(customBox)) {
                if (blockHeight <= 0.05) return ground + blockHeight
                ground += blockHeight
                blockHeight = 0.05
            }
            ground -= blockHeight
        }
        return 0.0
    }

    override val tag
        get() = mode

    private val modeModule
        get() = flyModes.find { it.modeName == mode }!!
}