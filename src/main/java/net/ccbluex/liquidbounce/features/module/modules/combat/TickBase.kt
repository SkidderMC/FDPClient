/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import kotlinx.coroutines.Dispatchers
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.event.async.waitTicks
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object TickBase : Module("TickBase", Category.COMBAT) {

    private val mode by choices("Mode", arrayOf("Past", "Future"), "Past")
    private val onlyOnKillAura by boolean("OnlyOnKillAura", true)

    private val change by int("Changes", 100, 0..100)

    private val balanceMaxValue by int("BalanceMaxValue", 100, 1..1000)
    private val balanceRecoveryIncrement by float("BalanceRecoveryIncrement", 0.1f, 0.01f..10f)
    private val maxTicksAtATime by int("MaxTicksAtATime", 20, 1..100)

    private val rangeToAttack by floatRange("RangeToAttack", 3f..5f, 0f..10f)

    private val forceGround by boolean("ForceGround", false)
    private val pauseAfterTick by int("PauseAfterTick", 0, 0..100)
    private val pauseOnFlag by boolean("PauseOnFlag", true)

    private val line by boolean("Line", true).subjective()
    private val lineColor by color("LineColor", Color.GREEN) { line }.subjective()

    private var ticksToSkip = 0
    private var tickBalance = 0f
    private var reachedTheLimit = false
    private val tickBuffer = mutableListOf<TickData>()
    var duringTickModification = false

    override val tag
        get() = mode

    override fun onToggle(state: Boolean) {
        duringTickModification = false
    }

    val onPreTick = handler<PlayerTickEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (player.ridingEntity != null || Blink.handleEvents()) {
            return@handler
        }

        if (event.state == EventState.PRE && ticksToSkip-- > 0) {
            event.cancelEvent()
        }
    }

    private var modificationFlag = false
    val onGameLoop = handler<GameLoopEvent> {
        if (modificationFlag) {
            modificationFlag = false
            duringTickModification = false
        }
    }

    val onGameTick = handler<GameTickEvent>(dispatcher = Dispatchers.Main, priority = 1) {
        val player = mc.thePlayer ?: return@handler

        if (player.ridingEntity != null || Blink.handleEvents()) {
            return@handler
        }

        if (!duringTickModification && tickBuffer.isNotEmpty()) {
            val nearbyEnemy = getNearestEntityInRange() ?: return@handler
            val currentDistance = player.positionVector.distanceTo(nearbyEnemy.positionVector)

            val possibleTicks = tickBuffer.mapIndexedNotNull { index, tick ->
                val tickDistance = tick.position.distanceTo(nearbyEnemy.positionVector)

                (index to tick).takeIf {
                    tickDistance < currentDistance && tickDistance in rangeToAttack && !tick.isCollidedHorizontally && (!forceGround || tick.onGround)
                }
            }

            val criticalTick =
                possibleTicks.filter { (_, tick) -> tick.fallDistance > 0.0f }.minByOrNull { (index, _) -> index }

            val (bestTick, _) = criticalTick ?: possibleTicks.minByOrNull { (index, _) -> index } ?: return@handler

            if (bestTick == 0) return@handler

            if (RandomUtils.nextInt(endExclusive = 100) > change ||
                onlyOnKillAura && (!state || KillAura.target == null)
            ) {
                ticksToSkip = 0
                return@handler
            }

            duringTickModification = true

            val skipTicks = (bestTick + pauseAfterTick).coerceAtMost(maxTicksAtATime + pauseAfterTick)

            fun tick() {
                repeat(skipTicks) {
                    player.onUpdate()
                    tickBalance -= 1
                }
            }

            if (mode == "Past") {
                ticksToSkip = skipTicks
                waitTicks(skipTicks)
                tick()
                modificationFlag = true
            } else {
                tick()
                ticksToSkip = skipTicks
                waitTicks(skipTicks)
                modificationFlag = true
            }
        }
    }

    val onMove = handler<MoveEvent> {
        val player = mc.thePlayer ?: return@handler

        if (player.ridingEntity != null || Blink.handleEvents()) {
            return@handler
        }

        tickBuffer.clear()

        val simulatedPlayer = SimulatedPlayer.fromClientPlayer(RotationUtils.modifiedInput)

        simulatedPlayer.rotationYaw = RotationUtils.currentRotation?.yaw ?: player.rotationYaw

        if (tickBalance <= 0) {
            reachedTheLimit = true
        }
        if (tickBalance > balanceMaxValue / 2) {
            reachedTheLimit = false
        }
        if (tickBalance <= balanceMaxValue) {
            tickBalance += balanceRecoveryIncrement
        }

        if (reachedTheLimit) return@handler

        repeat(minOf(tickBalance.toInt(), maxTicksAtATime * if (mode == "Past") 2 else 1)) {
            simulatedPlayer.tick()
            tickBuffer += TickData(
                simulatedPlayer.pos,
                simulatedPlayer.fallDistance,
                simulatedPlayer.motionX,
                simulatedPlayer.motionY,
                simulatedPlayer.motionZ,
                simulatedPlayer.onGround,
                simulatedPlayer.isCollidedHorizontally
            )
        }
    }

    val onDelayedPacketProcess = handler<DelayedPacketProcessEvent> {
        if (duringTickModification) {
            it.cancelEvent()
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (!line) return@handler

        synchronized(tickBuffer) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            glBegin(GL_LINE_STRIP)
            glColor(lineColor)

            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            for (tick in tickBuffer) {
                glVertex3d(
                    tick.position.xCoord - renderPosX,
                    tick.position.yCoord - renderPosY,
                    tick.position.zCoord - renderPosZ
                )
            }

            glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        if (event.packet is S08PacketPlayerPosLook && pauseOnFlag) {
            tickBalance = 0f
        }
    }

    private data class TickData(
        val position: Vec3,
        val fallDistance: Float,
        val motionX: Double,
        val motionY: Double,
        val motionZ: Double,
        val onGround: Boolean,
        val isCollidedHorizontally: Boolean,
    )

    private fun getNearestEntityInRange(): EntityLivingBase? {
        val player = mc.thePlayer ?: return null
        val entities = mc.theWorld.loadedEntityList ?: return null

        return entities.asSequence().filterIsInstance<EntityLivingBase>()
            .filter { EntityUtils.isSelected(it, true) }.minByOrNull { player.getDistanceToEntity(it) }
    }
}