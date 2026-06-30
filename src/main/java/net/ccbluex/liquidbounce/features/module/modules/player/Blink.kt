/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.visual.Breadcrumbs
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.network.play.client.C02PacketUseEntity
import org.lwjgl.opengl.GL11.*

object Blink : Module("Blink", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Sent", "Received", "Both"), "Sent")
        .describe("Which packets to hold back while blinking.")

    private val pulse by boolean("Pulse", false)
        .describe("Periodically release held packets in bursts.")
    private val pulseDelay by int("PulseDelay", 1000, 500..5000) { pulse }
        .describe("Delay between pulse releases in milliseconds.")

    private val fakePlayerMenu by boolean("FakePlayer", true)
        .describe("Spawn a fake player at your last position.")

    private val action by choices("Action", arrayOf("Release", "Reset"), "Release")
        .describe("What to do with held packets when disabling.")

    private val ambush by boolean("Ambush", false)
        .describe("Release packets when you attack an entity.")

    private val autoDisable by boolean("AutoDisable", false)
        .describe("Automatically disable after a number of ticks.")
    private val resetAfter by int("ResetAfter", 100, 1..1000) { autoDisable }
        .describe("Ticks before auto-disabling blink.")

    private val pulseTimer = MSTimer()

    private var tickCounter = 0

    init {
        group("General", "Mode", "FakePlayer", "Action")
        group("Pulse", "Pulse", "PulseDelay")
        group("Ambush", "Ambush")
        group("AutoDisable", "AutoDisable", "ResetAfter")
    }

    override fun onEnable() {
        pulseTimer.reset()
        tickCounter = 0

        if (fakePlayerMenu)
            BlinkUtils.addFakePlayer()
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        when (action.lowercase()) {
            "reset" -> BlinkUtils.cancel()
            else -> BlinkUtils.unblink()
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (mc.thePlayer == null || mc.thePlayer.isDead)
            return@handler

        if (ambush && packet is C02PacketUseEntity && packet.action == C02PacketUseEntity.Action.ATTACK) {
            BlinkUtils.unblink()
            if (fakePlayerMenu)
                BlinkUtils.addFakePlayer()
            tickCounter = 0
            return@handler
        }

        when (mode.lowercase()) {
            "sent" -> {
                BlinkUtils.blink(packet, event, sent = true, receive = false)
            }

            "received" -> {
                BlinkUtils.blink(packet, event, sent = false, receive = true)
            }

            "both" -> {
                BlinkUtils.blink(packet, event)
            }
        }
    }

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState == EventState.POST) {
            val thePlayer = mc.thePlayer ?: return@handler

            if (thePlayer.isDead || mc.thePlayer.ticksExisted <= 10) {
                BlinkUtils.unblink()
            }

            when (mode.lowercase()) {
                "sent" -> {
                    BlinkUtils.syncSent()
                }

                "received" -> {
                    BlinkUtils.syncReceived()
                }
            }

            if (pulse && pulseTimer.hasTimePassed(pulseDelay)) {
                BlinkUtils.unblink()
                if (fakePlayerMenu) {
                    BlinkUtils.addFakePlayer()
                }
                pulseTimer.reset()
            }

            if (autoDisable) {
                tickCounter++
                if (tickCounter >= resetAfter) {
                    tickCounter = 0
                    state = false
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        val color = Breadcrumbs.colors.color()

        synchronized(BlinkUtils.positions) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            glBegin(GL_LINE_STRIP)
            glColor(color)

            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            for (pos in BlinkUtils.positions)
                glVertex3d(pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ)

            glColor4d(1.0, 1.0, 1.0, 1.0)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    override val tag
        get() = (BlinkUtils.packets.size + BlinkUtils.packetsReceived.size).toString()

    fun blinkingSend() = handleEvents() && (mode == "Sent" || mode == "Both")
    fun blinkingReceive() = handleEvents() && (mode == "Received" || mode == "Both")
}