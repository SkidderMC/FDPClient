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
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.grim.GrimFly
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.polar.PolarFly
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.sentinel.SentinelFly
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
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

        // Grim
        GrimFly,

        // Polar
        PolarFly,

        // Sentinel
        SentinelFly,

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
        .describe("Anticheat-specific fly method to use.")
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
        .describe("Horizontal fly speed for the BlockDrop mode.")
    val blockDropVerticalSpeed by float("BlockDrop-VerticalSpeed", 1f, 0.1f..5f) { mode == "BlockDrop" }
        .describe("Vertical fly speed for the BlockDrop mode.")
    val clipX by float("ClipX", 2f, -5f..5f) { mode == "Clip" }
        .describe("X offset to clip the player to.")
    val clipY by float("ClipY", 2f, -5f..5f) { mode == "Clip" }
        .describe("Y offset to clip the player to.")
    val clipZ by float("ClipZ", 2f, -5f..5f) { mode == "Clip" }
        .describe("Z offset to clip the player to.")
    val clipDelay by int("ClipDelay", 500, 0..3000) { mode == "Clip" }
        .describe("Delay in ms between clip steps.")
    val clipMotionX by float("ClipMotionX", 0f, -1f..1f) { mode == "Clip" }
        .describe("X motion applied after clipping.")
    val clipMotionY by float("ClipMotionY", 0f, -1f..1f) { mode == "Clip" }
        .describe("Y motion applied after clipping.")
    val clipMotionZ by float("ClipMotionZ", 0f, -1f..1f) { mode == "Clip" }
        .describe("Z motion applied after clipping.")
    val clipSpoofGround by boolean("ClipSpoofGround", false) { mode == "Clip" }
        .describe("Spoof being on the ground while clipping.")
    val clipGroundWhenClip by boolean("ClipGroundWhenClip", true) { mode == "Clip" }
        .describe("Spoof ground only during the clip step.")
    val clipTimer by float("ClipTimer", 0.7f, 0.02f..2.5f) { mode == "Clip" }
        .describe("Game timer speed used while clipping.")
    val fakeGroundNoJump by boolean("FakeGround-NoJump", false) { mode == "FakeGround" }
        .describe("Disable jumping in the FakeGround mode.")
    val fakeGroundJumpUpY by boolean("FakeGround-JumpUpY", false) { mode == "FakeGround" }
        .describe("Jump upward in Y in the FakeGround mode.")
    val funCraftTimer by float("FunCraft-Timer", 1f, 0.1f..10f) { mode == "FunCraft" }
        .describe("Game timer speed for the FunCraft mode.")
    private val vanillaKickBypass by boolean(
        "VanillaKickBypass",
        false
    ) { mode in arrayOf("Vanilla", "SmoothVanilla") }.subjective()
    val ncpMotion by float("NCPMotion", 0f, 0f..1f) { mode == "NCP" }
        .describe("Extra motion added in the NCP mode.")

    val smoothValue by boolean("Smooth", false) { mode == "DefaultVanilla" }
        .describe("Smooth out the vanilla fly movement.")
    val speedValue by float("Speed", 2f, 0f.. 5f) { mode == "DefaultVanilla" }
        .describe("Horizontal fly speed for default vanilla mode.")
    val vspeedValue by float("Vertical", 2f, 0f..5f) { mode == "DefaultVanilla" }
        .describe("Vertical fly speed for default vanilla mode.")
    val kickBypassValue by boolean("KickBypass", false) { mode == "DefaultVanilla" }
        .describe("Avoid the flying kick in default vanilla mode.")
    val kickBypassModeValue by choices("KickBypassMode", arrayOf("Motion", "Packet"), "Packet") {  kickBypassValue }
        .describe("Method used to bypass the flying kick.")
    val kickBypassMotionSpeedValue by float("KickBypass-MotionSpeed", 0.0626F, 0.05F..0.1F) { kickBypassModeValue == "Motion" && kickBypassValue }
        .describe("Downward motion speed used by the kick bypass.")
    val noClipValue by boolean("NoClip", false) { mode == "DefaultVanilla" }
        .describe("Pass through blocks in default vanilla mode.")
    val spoofValue by boolean("SpoofGround", false) { mode == "DefaultVanilla" }
        .describe("Spoof being on the ground in default vanilla mode.")

    // AAC
    val aacSpeed by float("AAC1.9.10-Speed", 0.3f, 0f..1f) { mode == "AAC1.9.10" }
        .describe("Fly speed for the AAC 1.9.10 mode.")
    val aacFast by boolean("AAC3.0.5-Fast", true) { mode == "AAC3.0.5" }
        .describe("Use the faster variant of the AAC 3.0.5 mode.")
    val aacMotion by float("AAC3.3.12-Motion", 10f, 0.1f..10f) { mode == "AAC3.3.12" }
        .describe("Motion strength for the AAC 3.3.12 mode.")
    val aacMotion2 by float("AAC3.3.13-Motion", 10f, 0.1f..10f) { mode == "AAC3.3.13" }
        .describe("Motion strength for the AAC 3.3.13 mode.")

    // Hypixel
    val hypixelBoost by boolean("Hypixel-Boost", true) { mode == "Hypixel" }
        .describe("Enable the initial boost in the Hypixel mode.")
    val hypixelBoostDelay by int("Hypixel-BoostDelay", 1200, 50..2000) { mode == "Hypixel" && hypixelBoost }
        .describe("Delay in ms between Hypixel boosts.")
    val hypixelBoostTimer by float("Hypixel-BoostTimer", 1f, 0.1f..5f) { mode == "Hypixel" && hypixelBoost }
        .describe("Game timer speed during the Hypixel boost.")

    // Other
    val neruxVaceTicks by int("NeruxVace-Ticks", 6, 2..20) { mode == "NeruxVace" }
        .describe("Ticks to fly per cycle in the NeruxVace mode.")

    // Verus
    val damage by boolean("Damage", false) { mode == "Verus" }
        .describe("Take damage to trigger the Verus boost.")
    val timerSlow by boolean("TimerSlow", true) { mode == "Verus" }
        .describe("Slow the game timer during the Verus fly.")
    val boostTicksValue by int("BoostTicks", 20, 1..30) { mode == "Verus" }
        .describe("Number of ticks the Verus boost lasts.")
    val boostMotion by float("BoostMotion", 6.5f, 1f..9.85f) { mode == "Verus" }
        .describe("Horizontal boost motion for the Verus mode.")
    val yBoost by float("YBoost", 0.42f, 0f..10f) { mode == "Verus" }
        .describe("Upward boost motion for the Verus mode.")
    val verusDamageSpeed by float("VerusDamageSpeed", 1.5f, 0f..10f) { mode == "VerusDamage" }
        .describe("Fly speed for the Verus damage mode.")
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
        .describe("Keep the BlocksMC fly more stable in the air.")
    val timerSlowed by boolean("TimerSlowed", true) { mode == "BlocksMC" || mode == "BlocksMC2" }
        .describe("Slow the game timer during the BlocksMC fly.")
    val boostSpeed by float("BoostSpeed", 6f, 1f..15f) { mode == "BlocksMC" || mode == "BlocksMC2" }
        .describe("Boost speed for the BlocksMC fly.")
    val extraBoost by float("ExtraSpeed", 1f, 0.0F..2f) { mode == "BlocksMC" || mode == "BlocksMC2" }
        .describe("Extra speed added to the BlocksMC boost.")
    val stopOnLanding by boolean("StopOnLanding", true) { mode == "BlocksMC" || mode == "BlocksMC2" }
        .describe("Stop motion when landing in the BlocksMC fly.")
    val stopOnNoMove by boolean("StopOnNoMove", false) { mode == "BlocksMC" || mode == "BlocksMC2" }
        .describe("Stop motion when not pressing a move key.")
    val debugFly by boolean("Debug", false) { mode == "BlocksMC" || mode == "BlocksMC2" }
        .describe("Show debug info for the BlocksMC fly.")

    // Fireball
    val pitchMode by choices("PitchMode", arrayOf("Custom", "Smart"), "Custom") { mode == "Fireball" }
        .describe("How to choose the pitch when throwing fireballs.")
    val rotationPitch by float("Pitch", 90f, 0f..90f) { pitchMode != "Smart" && mode == "Fireball" }
        .describe("Pitch angle used to aim the fireball.")
    val invertYaw by boolean("InvertYaw", true) { pitchMode != "Smart" && mode == "Fireball" }
        .describe("Aim the fireball away from the look direction.")

    val autoFireball by choices(
        "AutoFireball",
        arrayOf("Off", "Pick", "Spoof", "Switch"),
        "Spoof"
    ) { mode == "Fireball" }
    val swing by boolean("Swing", true) { mode == "Fireball" }
        .describe("Swing the arm when throwing a fireball.")
    val fireballTry by int("MaxFireballTry", 1, 0..2) { mode == "Fireball" }
        .describe("Max retries when throwing a fireball fails.")
    val fireBallThrowMode by choices("FireballThrow", arrayOf("Normal", "Edge"), "Normal") { mode == "Fireball" }
        .describe("Where to aim the fireball when throwing it.")
    val edgeThreshold by float(
        "EdgeThreshold",
        1.05f,
        1f..2f
    ) { fireBallThrowMode == "Edge" && mode == "Fireball" }

    val options = RotationSettings(this) { mode == "Fireball" }
        .withRequestPriority(RotationPriority.HIGH)

    val autoJump by boolean("AutoJump", true) { mode == "Fireball" }
        .describe("Jump automatically before throwing a fireball.")

    // Visuals
    private val mark by boolean("Mark", true).subjective()
        .describe("Draw a platform marker at the start height.")

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
