/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.AAC
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.AAC3311
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.AAC3315
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac.LAAC
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other.*
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other.Blink
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB.fromBounds
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.max

object NoFall : Module("NoFall", Category.PLAYER, hideModule = false) {
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

        // Other Server
        Spartan,
        CubeCraft,
    )

    private val modes = noFallModes.map { it.modeName }.toTypedArray()

    val mode by choices("Mode", modes, "MLG")

    val minFallDistance by float("MinMLGHeight", 5f, 2f..50f, subjective = true) { mode == "MLG" }
    private val retrieveDelayValue: IntegerValue = object : IntegerValue("RetrieveDelayTicks", 5, 1..10, subjective = true) {
        override fun isSupported() = mode == "MLG"
        override fun onChange(oldValue: Int, newValue: Int): Int {
            maxRetrievalWaitingTimeValue.set(max(maxRetrievalWaitingTime, newValue))

            return newValue
        }
    }

    val retrieveDelay by retrieveDelayValue

    val maxRetrievalWaitingTimeValue = object : IntegerValue("MaxRetrievalWaitingTime", 10, 1..20) {
        override fun isSupported() = mode == "MLG"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(retrieveDelay)
    }

    val maxRetrievalWaitingTime by maxRetrievalWaitingTimeValue

    val autoMLG by choices("AutoMLG", arrayOf("Off", "Pick", "Spoof"), "Spoof") { mode == "MLG" }
    val swing by boolean("Swing", true) { mode == "MLG" }

    val options = RotationSettings(this) { mode == "MLG" }.apply {
        rotationsValue.excludeWithState(true)
        resetTicksValue.setSupport { mode == "MLG" && keepRotation }
    }

    // Using too many times of simulatePlayer could result timer flag. Hence, why this is disabled by default.
    val checkFallDist by boolean("CheckFallDistance", false, subjective = true) { mode == "Blink" }

    val minFallDist: FloatValue = object : FloatValue("MinFallDistance", 2.5f, 0f..10f, subjective = true) {
        override fun isSupported() = mode == "Blink" && checkFallDist
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxFallDist.get())
    }
    val maxFallDist: FloatValue = object : FloatValue("MaxFallDistance", 20f, 0f..100f, subjective = true) {
        override fun isSupported() = mode == "Blink" && checkFallDist
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minFallDist.get())
    }

    val autoOff by boolean("AutoOff", true) { mode == "Blink" }
    val simulateDebug by boolean("SimulationDebug", false, subjective = true) { mode == "Blink" }
    val fakePlayer by boolean("FakePlayer", true, subjective = true) { mode == "Blink" }

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