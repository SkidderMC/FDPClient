/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.visual.Breadcrumbs
import net.ccbluex.liquidbounce.utils.BlinkUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.handlePacket
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.status.client.C00PacketServerQuery
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

object Blink : Module("Blink", Category.PLAYER, gameDetecting = false, hideModule = false) {

	private val mode by ListValue("Mode", arrayOf("Sent", "Received", "Both"), "Sent")

    private val outgoingValue = BoolValue("OutGoing", true)

    private val pulse by BoolValue("Pulse", false)
		private var pulseDelay by IntegerValue("PulseDelay", 1000, 500..5000) { pulse }

    private val outgoingMin by IntegerValue("MinOutGoing", 1000, 100.. 5000) { pulse }
    private val outgoingMax by IntegerValue("MaxOutGoing", 1500, 100.. 5000) { pulse }

    private val fakePlayerMenu by BoolValue("FakePlayer", true)

    private val pulseTimer = MSTimer()
    private var fakePlayer: EntityOtherPlayerMP? = null
    private val positions = LinkedList<DoubleArray>()

    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()


    override fun onEnable() {
        if (mc.thePlayer == null) return
        if (outgoingValue.get()) {
            BlinkUtils.setBlinkState(all = true)
            if (!pulse) {
                fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
                fakePlayer!!.clonePlayer(mc.thePlayer, true)
                fakePlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
                fakePlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
                mc.theWorld.addEntityToWorld(-1337, fakePlayer)
            } else {
                pulseDelay = RandomUtils.nextInt(outgoingMin, outgoingMax)
            }
        }
        packets.clear()
        synchronized(positions) {
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight() / 2, mc.thePlayer.posZ))
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ))
        }

        pulseTimer.reset()

        if (fakePlayerMenu)
            BlinkUtils.addFakePlayer()
    }

    private fun clearPackets() {
        while (!packets.isEmpty()) {
            handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
        }
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        synchronized(positions) { positions.clear() }
        if (mc.thePlayer == null) return
        BlinkUtils.setBlinkState(off = true, release = true)
        clearPackets()
        if (fakePlayer != null) {
            mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
            fakePlayer = null
        }

        BlinkUtils.unblink()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (mc.thePlayer == null || mc.thePlayer.isDead)
            return

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

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            val thePlayer = mc.thePlayer ?: return

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
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color =
            if (Breadcrumbs.colorRainbow) rainbow()
            else Color(Breadcrumbs.colorRed, Breadcrumbs.colorGreen, Breadcrumbs.colorBlue)

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
