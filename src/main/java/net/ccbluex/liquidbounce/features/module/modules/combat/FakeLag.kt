/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import com.google.common.collect.Queues
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack.runWithModifiedRotation
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.pos
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.kotlin.removeEach
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import kotlin.math.min

object FakeLag : Module("FakeLag", Category.COMBAT, Category.SubCategory.COMBAT_RAGE, gameDetecting = false) {

    private val delay by int("Delay", 550, 0..1000)
    private val recoilTime by int("RecoilTime", 750, 0..2000)

    private val allowedDistToEnemy by floatRange("MinAllowedDistToEnemy", 1.5f..3.5f, 0f..6f)

    private val blinkOnAction by boolean("BlinkOnAction", true)

    private val pauseOnNoMove by boolean("PauseOnNoMove", true)
    private val pauseOnChest by boolean("PauseOnChest", false)

    private val line by boolean("Line", true).subjective()
    private val lineColor by color("LineColor", Color.GREEN) { line }.subjective()

    private val renderModel by boolean("RenderModel", true).subjective()

    private val packetQueue = Queues.newArrayDeque<QueueData>()
    private val positions = Queues.newArrayDeque<PositionData>()
    private val resetTimer = MSTimer()
    private var wasNearEnemy = false
    private var ignoreWholeTick = false

    private var renderData = ModelRenderData(Vec3_ZERO, Rotation.ZERO)

    override fun onDisable() {
        if (mc.thePlayer == null) return

        blink()
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val packet = event.packet

        if (!handleEvents() || player.isDead || event.isCancelled || allowedDistToEnemy.endInclusive > 0.0 && wasNearEnemy || ignoreWholeTick) {
            return@handler
        }

        if (pauseOnNoMove && !player.isMoving) {
            blink()
            return@handler
        }

        // Flush on damaged received
        if (player.health < player.maxHealth) {
            if (player.hurtTime != 0) {
                blink()
                return@handler
            }
        }

        // Flush on scaffold/tower usage
        if (Scaffold.handleEvents() && Scaffold.placeRotation != null) {
            blink()
            return@handler
        }

        // Flush on attack/interact
        if (blinkOnAction && packet is C02PacketUseEntity) {
            blink()
            return@handler
        }

        if (pauseOnChest && mc.currentScreen is GuiContainer) {
            blink()
            return@handler
        }

        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> return@handler

            // Flush on window clicked (Inventory)
            is C0EPacketClickWindow, is C0DPacketCloseWindow -> {
                blink()
                return@handler
            }

            // Flush on doing action/getting action
            is S08PacketPlayerPosLook, is C08PacketPlayerBlockPlacement, is C07PacketPlayerDigging, is C12PacketUpdateSign, is C19PacketResourcePackStatus -> {
                blink()
                return@handler
            }

            // Flush on knockback
            is S12PacketEntityVelocity -> {
                if (player.entityId == packet.entityID) {
                    blink()
                    return@handler
                }
            }

            is S27PacketExplosion -> {
                if (packet.field_149153_g != 0f || packet.field_149152_f != 0f || packet.field_149159_h != 0f) {
                    blink()
                    return@handler
                }
            }
        }

        if (!resetTimer.hasTimePassed(recoilTime)) return@handler

        if (mc.isSingleplayer || mc.currentServerData == null) {
            blink()
            return@handler
        }

        if (event.eventType == EventState.SEND) {
            event.cancelEvent()

            if (packet is C03PacketPlayer && packet.isMoving) {
                synchronized(positions) {
                    positions += PositionData(
                        packet.pos,
                        System.currentTimeMillis(),
                        player.renderYawOffset,
                        RotationUtils.serverRotation
                    )
                }
            }

            synchronized(packetQueue) {
                packetQueue += QueueData(packet, System.currentTimeMillis())
            }
        }
    }

    val onWorld = handler<WorldEvent> { event ->
        // Clear packets on disconnect only
        if (event.worldClient == null) blink(false)
    }

    private fun getTruePositionEyes(player: EntityPlayer): Vec3 {
        val mixinPlayer = player as? IMixinEntity
        return Vec3(mixinPlayer!!.trueX, mixinPlayer.trueY + player.getEyeHeight().toDouble(), mixinPlayer.trueZ)
    }

    val onGameLoop = handler<GameLoopEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (allowedDistToEnemy.endInclusive > 0) {
            val playerPos = player.currPos
            val serverPos = positions.firstOrNull()?.pos ?: playerPos

            val playerBox = player.hitBox.offset(serverPos - playerPos)

            wasNearEnemy = false

            world.playerEntities.forEach { otherPlayer ->
                if (otherPlayer == player) return@forEach

                val entityMixin = otherPlayer as? IMixinEntity

                if (entityMixin != null) {
                    val eyes = getTruePositionEyes(otherPlayer)

                    if (eyes.distanceTo(getNearestPointBB(eyes, playerBox)) in allowedDistToEnemy) {
                        blink()
                        wasNearEnemy = true
                        return@handler
                    }
                }
            }
        }

        if (Blink.blinkingSend() || player.isDead || player.isUsingItem) {
            blink()
            return@handler
        }

        if (!resetTimer.hasTimePassed(recoilTime)) return@handler

        handlePackets()
        ignoreWholeTick = false
    }

    val onRender3D = handler<Render3DEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (Blink.blinkingSend() || positions.isEmpty()) {
            renderData.reset(player)
            return@handler
        }

        renderData.update(positions)

        if (line) {
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

            for ((pos) in positions) glVertex3d(
                pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ
            )

            glColor4d(1.0, 1.0, 1.0, 1.0)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }

        // A pretty basic model render process. Position and rotation interpolation is applied to look visually appealing to the user.
        // This can be smarter by adding sneak checks, more timed hand swing/body movement, etc.
        if (mc.gameSettings.thirdPersonView == 0 || !renderModel) return@handler

        val manager = mc.renderManager

        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)

        glColor(Color.BLACK)

        val (old, new) = positions.first() to positions.elementAt(min(1, positions.size - 1))

        val pos = renderData.pos - manager.renderPos

        runWithModifiedRotation(player, renderData.rotation, old.body to new.body) {
            manager.doRenderEntity(
                player, pos.xCoord, pos.yCoord, pos.zCoord, it.yaw, event.partialTicks, true
            )
        }

        glPopAttrib()
        glPopMatrix()
    }

    override val tag
        get() = packetQueue.size.toString()

    private fun blink(handlePackets: Boolean = true) {
        mc.addScheduledTask {
            if (handlePackets) {
                resetTimer.reset()
            }

            handlePackets(true)
            ignoreWholeTick = true
        }
    }

    private fun handlePackets(clear: Boolean = false) {
        synchronized(packetQueue) {
            packetQueue.removeEach { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - delay || clear) {
                    sendPacket(packet, false)
                    true
                } else false
            }
        }

        synchronized(positions) {
            positions.removeEach { (_, timestamp) -> timestamp <= System.currentTimeMillis() - delay || clear }
        }
    }

}

data class ModelRenderData(var pos: Vec3, var rotation: Rotation) {
    fun reset(player: EntityPlayerSP) {
        pos = player.currPos
        rotation = RotationUtils.serverRotation
    }

    fun update(positions: ArrayDeque<PositionData>) {
        val data = positions.first()

        pos = pos.lerpWith(data.pos, RenderUtils.deltaTimeNormalized(3))
        rotation = rotation.lerpWith(data.rotation, RenderUtils.deltaTimeNormalized(1))
    }
}

data class PositionData(val pos: Vec3, val time: Long, val body: Float, val rotation: Rotation)