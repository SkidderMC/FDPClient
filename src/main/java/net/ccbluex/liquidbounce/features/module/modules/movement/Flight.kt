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
import net.ccbluex.liquidbounce.features.module.modules.modeNames
import net.ccbluex.liquidbounce.features.module.modules.selectedMode
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.aac.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.blocksmc.BlocksMC
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.blocksmc.BlocksMC2
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.hypixel.BoostHypixel
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.hypixel.FreeHypixel
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.hypixel.Hypixel
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.ncp.BlockDrop
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.ncp.NCP
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.ncp.OldNCP
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.spartan.BugSpartan
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.spartan.Spartan
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.spartan.Spartan2
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla.Creative
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla.DefaultVanilla
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla.SmoothVanilla
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla.Vanilla
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.verus.Verus
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.verus.VerusDamage
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.verus.VerusGlide
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vulcan.Vulcan
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vulcan.VulcanGhost
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vulcan.VulcanOld
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.extensions.stop
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.lwjgl.input.Keyboard
import java.awt.Color

object Flight : Module("Flight", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, Keyboard.KEY_F) {
    private val flyModes = arrayOf(
        Vanilla, SmoothVanilla, DefaultVanilla, Creative,

        // NCP
        NCP, OldNCP, BlockDrop,

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
        Verus, VerusDamage, VerusGlide,

        // Other anti-cheats
        MineSecure, HawkEye, HAC, WatchCat,

        // Other
        Jetpack, KeepAlive, Collide, Jump, Flag, Fireball, Clip, FakeGround, FunCraft, TeleportRewinside
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
        BlockDrop,

        AAC1910, AAC305, AAC316, AAC3312, AAC3312Glide, AAC3313,

        CubeCraft,

        Creative, Clip, FakeGround, FunCraft, TeleportRewinside, VerusDamage
    )

    private val showDeprecated by boolean("DeprecatedMode", true).onChanged { value ->
        modeValue.changeValue(modesList.first { it !in deprecatedMode }.modeName)
        modeValue.updateValues(modesList.filter { value || it !in deprecatedMode }.modeNames())
    }

    private var modesList = flyModes

    val modeValue = choices("Mode", modesList.modeNames(), "Vanilla")
    val mode by modeValue

    val vanillaSpeed by float("VanillaSpeed", 2f, 0f..10f) {
        mode in arrayOf(
            "Vanilla",
            "KeepAlive",
            "MineSecure",
            "BugSpartan"
        )
    }.subjective()
    val vanillaVerticalSpeed by float("VanillaVerticalSpeed", 2f, 0f..10f) {
        mode in arrayOf(
            "Vanilla",
            "KeepAlive"
        )
    }.subjective()
    val blockDropHorizontalSpeed by float("BlockDrop-HorizontalSpeed", 1f, 0.1f..5f) { mode == "BlockDrop" }
    val blockDropVerticalSpeed by float("BlockDrop-VerticalSpeed", 1f, 0.1f..5f) { mode == "BlockDrop" }
    val clipX by float("ClipX", 2f, -5f..5f) { mode == "Clip" }
    val clipY by float("ClipY", 2f, -5f..5f) { mode == "Clip" }
    val clipZ by float("ClipZ", 2f, -5f..5f) { mode == "Clip" }
    val clipDelay by int("ClipDelay", 500, 0..3000) { mode == "Clip" }
    val clipMotionX by float("ClipMotionX", 0f, -1f..1f) { mode == "Clip" }
    val clipMotionY by float("ClipMotionY", 0f, -1f..1f) { mode == "Clip" }
    val clipMotionZ by float("ClipMotionZ", 0f, -1f..1f) { mode == "Clip" }
    val clipSpoofGround by boolean("ClipSpoofGround", false) { mode == "Clip" }
    val clipGroundWhenClip by boolean("ClipGroundWhenClip", true) { mode == "Clip" }
    val clipTimer by float("ClipTimer", 0.7f, 0.02f..2.5f) { mode == "Clip" }
    val fakeGroundNoJump by boolean("FakeGround-NoJump", false) { mode == "FakeGround" }
    val fakeGroundJumpUpY by boolean("FakeGround-JumpUpY", false) { mode == "FakeGround" }
    val funCraftTimer by float("FunCraft-Timer", 1f, 0.1f..10f) { mode == "FunCraft" }
    private val vanillaKickBypass by boolean(
        "VanillaKickBypass",
        false
    ) { mode in arrayOf("Vanilla", "SmoothVanilla") }.subjective()
    val ncpMotion by float("NCPMotion", 0f, 0f..1f) { mode == "NCP" }

    val smoothValue by boolean("Smooth", false) { mode == "DefaultVanilla" }
    val speedValue by float("Speed", 2f, 0f.. 5f) { mode == "DefaultVanilla" }
    val vspeedValue by float("Vertical", 2f, 0f..5f) { mode == "DefaultVanilla" }
    val kickBypassValue by boolean("KickBypass", false) { mode == "DefaultVanilla" }
    val kickBypassModeValue by choices("KickBypassMode", arrayOf("Motion", "Packet"), "Packet") {  kickBypassValue }
    val kickBypassMotionSpeedValue by float("KickBypass-MotionSpeed", 0.0626F, 0.05F..0.1F) { kickBypassModeValue == "Motion" && kickBypassValue }
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
    val verusDamageSpeed by float("VerusDamageSpeed", 1.5f, 0f..10f) { mode == "VerusDamage" }
    val verusDamageBoostMode by choices(
        "VerusDamageBoostMode",
        arrayOf("Boost1", "Boost2", "Boost3"),
        "Boost1"
    ) { mode == "VerusDamage" }
    val verusDamageReDamage by boolean("VerusDamageBoost3-ReDamage", true) {
        mode == "VerusDamage" && verusDamageBoostMode == "Boost3"
    }

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

    val autoFireball by choices(
        "AutoFireball",
        arrayOf("Off", "Pick", "Spoof", "Switch"),
        "Spoof"
    ) { mode == "Fireball" }
    val swing by boolean("Swing", true) { mode == "Fireball" }
    val fireballTry by int("MaxFireballTry", 1, 0..2) { mode == "Fireball" }
    val fireBallThrowMode by choices("FireballThrow", arrayOf("Normal", "Edge"), "Normal") { mode == "Fireball" }
    val edgeThreshold by float(
        "EdgeThreshold",
        1.05f,
        1f..2f
    ) { fireBallThrowMode == "Edge" && mode == "Fireball" }

    val options = RotationSettings(this) { mode == "Fireball" }

    val autoJump by boolean("AutoJump", true) { mode == "Fireball" }

    // Visuals
    private val mark by boolean("Mark", true).subjective()

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
            && mode != "Fireball" && mode != "Collide" && mode != "Jump"
        ) {

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

    val onUpdate = handler<UpdateEvent> {
        modeModule.onUpdate()
    }

    val onTick = handler<GameTickEvent> {
        modeModule.onTick()
    }

    val onTick1 = loopSequence {
        if (mode == "Fireball" && wasFired) {
            waitTicks(2)
            state = false
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        if (!mark || mode == "Vanilla" || mode == "SmoothVanilla")
            return@handler

        val y = startY + 2.0 + (if (mode == "BoostHypixel") 0.42 else 0.0)
        drawPlatform(
            y,
            if (mc.thePlayer.entityBoundingBox.maxY < y) Color(0, 255, 0, 90) else Color(255, 0, 0, 90),
            1.0
        )

        modeModule.onRender3D(event)
    }

    val onPacket = handler<PacketEvent> { event ->
        mc.thePlayer ?: return@handler

        modeModule.onPacket(event)
    }

    val onBB = handler<BlockBBEvent> { event ->
        mc.thePlayer ?: return@handler

        modeModule.onBB(event)
    }

    val onJump = handler<JumpEvent> { event ->
        modeModule.onJump(event)
    }

    val onStep = handler<StepEvent> { event ->
        modeModule.onStep(event)
    }

    val onMotion = handler<MotionEvent> { event ->
        modeModule.onMotion(event)
    }

    val onMove = handler<MoveEvent> { event ->
        modeModule.onMove(event)
    }

    fun handleVanillaKickBypass() {
        if (!vanillaKickBypass || !groundTimer.hasTimePassed(1000)) return
        val ground = calculateGround() + 0.5

        var posY = mc.thePlayer.posY
        while (posY > ground) {
            sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true))
            if (posY - 8.0 < ground) break // Prevent next step
            posY -= 8.0
        }

        sendPacket(C04PacketPlayerPosition(mc.thePlayer.posX, ground, mc.thePlayer.posZ, true))
        posY = ground
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
        get() = flyModes.selectedMode(mode)
}
