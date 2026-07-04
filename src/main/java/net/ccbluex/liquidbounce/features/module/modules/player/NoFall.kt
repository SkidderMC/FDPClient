/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.modeNames
import net.ccbluex.liquidbounce.features.module.modules.selectedMode
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.*
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.grim.Grim2371
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.matrix.*
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.normal.*
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other.*
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other.Blink
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.packet.Packet1
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.packet.Packet2
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.verus.Verus
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.vulcan.LatestVulcan
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.vulcan.OldVulcan
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.polar.Polar
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.sentinel.Sentinel
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.intave.Intave
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.rotation.AlwaysRotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB.fromBounds
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.max

object NoFall : Module("NoFall", Category.PLAYER, Category.SubCategory.PLAYER_COUNTER) {
    private val noFallModes = arrayOf(

        // Main
        SpoofGround,
        NoGround,
        Packet,
        Cancel,
        MLG,
        Blink,

        // Vanilla / Normal
        Vanilla,
        AlwaysSpoof,
        Damage,
        MotionFlag,
        Phase,

        // Packet
        Packet1,
        Packet2,

        // AAC
        AAC,
        LAAC,
        AAC3311,
        AAC3315,
        AAC44XFlag,
        AAC5014,
        AAC504,
        AACv4,

        // Hypixel (Watchdog)
        Hypixel,
        HypixelBlink,
        HypixelFlag,
        HypixelTimer,

        // Vulcan
        VulcanFast288,
        OldVulcan,
        LatestVulcan,

        // Matrix
        Matrix62x,
        Matrix62xPacket,
        MatrixCollide,
        Matrix663,
        MatrixNew,

        // Grim
        Grim2371,

        // Polar
        Polar,

        // Sentinel
        Sentinel,

        // Intave
        Intave,

        // Other Server
        Verus,
        Spartan,
        CubeCraft,
    )

    val mode by choices("Mode", noFallModes.modeNames(), "MLG")
        .describe("No-fall bypass method to use.")

    val minFallDistance by float("MinMLGHeight", 5f, 2f..50f) { mode == "MLG" }.subjective()
        .describe("Minimum fall height before MLG triggers.")

    val retrieveDelay: Int by int("RetrieveDelayTicks", 5, 1..10) {
        mode == "MLG"
    }.onChanged {
        maxRetrievalWaitingTimeValue.set(max(maxRetrievalWaitingTime, it))
    }.subjective()
        .describe("Ticks to wait before retrieving the placed block.")

    private val maxRetrievalWaitingTimeValue = int("MaxRetrievalWaitingTime", 10, 1..20) {
        mode == "MLG"
    }.onChange { _, new ->
        new.coerceAtLeast(retrieveDelay)
    }
        .describe("Maximum ticks to wait while retrieving the block.")

    val maxRetrievalWaitingTime by maxRetrievalWaitingTimeValue

    val autoMLG by choices("AutoMLG", arrayOf("Off", "Pick", "Spoof"), "Spoof") { mode == "MLG" }
        .describe("How to place a block to save yourself.")
    val swing by boolean("Swing", true) { mode == "MLG" }
        .describe("Swing the arm when placing the MLG block.")

    val options = AlwaysRotationSettings(this) { mode == "MLG" }
        .withRequestPriority(RotationPriority.CRITICAL)
    val matrixSafe by boolean("SafeNoFall", true) { mode == "Matrix6.6.3" }
        .describe("Use the safer Matrix no-fall variant.")
    val motionFlagSpeed by float("MotionFlag-MotionSpeed", -0.01f, -5f..5f) { mode == "MotionFlag" }
        .describe("Vertical motion sent in MotionFlag mode.")
    val phaseOffset by int("PhaseOffset", 1, 0..5) { mode == "Phase" }
        .describe("Offset used in Phase no-fall mode.")
    val hypixelBlinkIndicator by boolean("Indicator", true) { mode == "HypixelBlink" }
        .describe("Show an indicator in HypixelBlink mode.")

    // Using too many times of simulatePlayer could result timer flag. Hence, why this is disabled by default.
    val checkFallDist by boolean("CheckFallDistance", false) { mode == "Blink" }.subjective()
        .describe("Only blink within a fall-distance range.")
    val fallDist by floatRange("FallDistance", 2.5f..20f, 0f..100f) {
        mode == "Blink" && checkFallDist
    }.subjective()
        .describe("Fall-distance range within which to blink.")

    val autoOff by boolean("AutoOff", true) { mode == "Blink" }
        .describe("Disable Blink no-fall after landing.")
    val simulateDebug by boolean("SimulationDebug", false) { mode == "Blink" }.subjective()
        .describe("Render fall simulation debug visuals.")
    val fakePlayer by boolean("FakePlayer", true) { mode == "Blink" }.subjective()
        .describe("Spawn a fake player while blinking.")

    private val mlgGroup = Configurable("MLG")
    private val blinkGroup = Configurable("Blink")
    private val variantsGroup = Configurable("Variants")

    init {
        options.nestInto(mlgGroup)
        moveValues(mlgGroup,
            "Mode", "MinMLGHeight", "RetrieveDelayTicks", "MaxRetrievalWaitingTime", "AutoMLG", "Swing")
        moveValues(blinkGroup,
            "CheckFallDistance", "FallDistance", "AutoOff", "SimulationDebug", "FakePlayer")
        moveValues(variantsGroup,
            "SafeNoFall", "MotionFlag-MotionSpeed", "PhaseOffset", "Indicator")

        addValues(listOf(mlgGroup, blinkGroup, variantsGroup))
    }
    var currentMlgBlock: BlockPos? = null
    var retrievingPos: Vec3? = null
    var wasTimer = false

    override fun onEnable() {
        modeModule.onEnable()
        retrievingPos = null
    }

    override fun onDisable() {
        if (mode == "MLG") {
            currentMlgBlock = null
            retrievingPos = null
        }

        if (wasTimer) {
            mc.timer.timerSpeed = 1f
            wasTimer = false
        }

        modeModule.onDisable()
    }

    val onTick = handler<GameTickEvent> {
        modeModule.onTick()
    }

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer

        if (wasTimer) {
            mc.timer.timerSpeed = 1f
            wasTimer = false
        }

        if (collideBlock(thePlayer.entityBoundingBox) { it is BlockLiquid } || collideBlock(
                fromBounds(
                    thePlayer.entityBoundingBox.maxX,
                    thePlayer.entityBoundingBox.maxY,
                    thePlayer.entityBoundingBox.maxZ,
                    thePlayer.entityBoundingBox.minX,
                    thePlayer.entityBoundingBox.minY - 0.01,
                    thePlayer.entityBoundingBox.minZ
                )
            ) { it is BlockLiquid }
        ) return@handler

        modeModule.onUpdate()
    }

    val onRender3D = handler<Render3DEvent> {
        modeModule.onRender3D(it)
    }

    val onRender2D = handler<Render2DEvent> {
        modeModule.onRender2D(it)
    }

    val onPacket = handler<PacketEvent> {
        mc.thePlayer ?: return@handler

        modeModule.onPacket(it)
    }

    val onBB = handler<BlockBBEvent> {
        mc.thePlayer ?: return@handler

        modeModule.onBB(it)
    }

    // Ignore condition used in LAAC mode
    val onJump = handler<JumpEvent>(always = true) {
        modeModule.onJump(it)
    }

    val onStep = handler<StepEvent> {
        modeModule.onStep(it)
    }

    val onMotion = handler<MotionEvent> {
        modeModule.onMotion(it)
    }

    val onMove = handler<MoveEvent> {
        val thePlayer = mc.thePlayer

        if (collideBlock(thePlayer.entityBoundingBox) { it is BlockLiquid }
            || collideBlock(
                fromBounds(
                    thePlayer.entityBoundingBox.maxX,
                    thePlayer.entityBoundingBox.maxY,
                    thePlayer.entityBoundingBox.maxZ,
                    thePlayer.entityBoundingBox.minX,
                    thePlayer.entityBoundingBox.minY - 0.01,
                    thePlayer.entityBoundingBox.minZ
                )
            ) { it is BlockLiquid }
        ) return@handler

        modeModule.onMove(it)
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        modeModule.onRotationUpdate()
    }

    override val tag
        get() = mode

    private val modeModule
        get() = noFallModes.selectedMode(mode)
}
