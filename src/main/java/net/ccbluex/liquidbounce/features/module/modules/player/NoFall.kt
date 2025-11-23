/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.AAC
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.AAC3311
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.AAC3315
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.LAAC
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.grim.Grim2371
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other.*
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other.Blink
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.rotation.AlwaysRotationSettings
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

        // AAC
        AAC,
        LAAC,
        AAC3311,
        AAC3315,

        // Hypixel (Watchdog)
        Hypixel,
        HypixelTimer,

        // Vulcan
        VulcanFast288,

        // Grim
        Grim2371,

        // Other Server
        Spartan,
        CubeCraft,
    )

    private val modes = noFallModes.map { it.modeName }.toTypedArray()

    val mode by choices("Mode", modes, "MLG")

    val minFallDistance by float("MinMLGHeight", 5f, 2f..50f) { mode == "MLG" }.subjective()

    val retrieveDelay: Int by int("RetrieveDelayTicks", 5, 1..10) {
        mode == "MLG"
    }.onChanged {
        maxRetrievalWaitingTimeValue.set(max(maxRetrievalWaitingTime, it))
    }.subjective()

    private val maxRetrievalWaitingTimeValue = int("MaxRetrievalWaitingTime", 10, 1..20) {
        mode == "MLG"
    }.onChange { _, new ->
        new.coerceAtLeast(retrieveDelay)
    }

    val maxRetrievalWaitingTime by maxRetrievalWaitingTimeValue

    val autoMLG by choices("AutoMLG", arrayOf("Off", "Pick", "Spoof"), "Spoof") { mode == "MLG" }
    val swing by boolean("Swing", true) { mode == "MLG" }

    val options = AlwaysRotationSettings(this) { mode == "MLG" }

    // Using too many times of simulatePlayer could result timer flag. Hence, why this is disabled by default.
    val checkFallDist by boolean("CheckFallDistance", false) { mode == "Blink" }.subjective()
    val fallDist by floatRange("FallDistance", 2.5f..20f, 0f..100f) {
        mode == "Blink" && checkFallDist
    }.subjective()

    val autoOff by boolean("AutoOff", true) { mode == "Blink" }
    val simulateDebug by boolean("SimulationDebug", false) { mode == "Blink" }.subjective()
    val fakePlayer by boolean("FakePlayer", true) { mode == "Blink" }.subjective()

    var currentMlgBlock: BlockPos? = null
    var retrievingPos: Vec3? = null

    override fun onEnable() {
        modeModule.onEnable()
        retrievingPos = null
    }

    override fun onDisable() {
        if (mode == "MLG") {
            currentMlgBlock = null
            retrievingPos = null
        }

        modeModule.onDisable()
    }

    val onTick = handler<GameTickEvent> {
        modeModule.onTick()
    }

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer

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
        get() = noFallModes.find { it.modeName == mode }!!
}