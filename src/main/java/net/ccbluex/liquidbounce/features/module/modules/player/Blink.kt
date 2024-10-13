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
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

object Blink : Module("Blink", Category.PLAYER, gameDetecting = false, hideModule = false) {

    private val mode by ListValue("Mode", arrayOf("Sent", "Received", "Both"), "Sent")
    private val outgoingValue by BoolValue("OutGoing", true)
    private val inboundValue by BoolValue("Inbound", false)

    private val pulseValue by BoolValue("Pulse", false)
    private val minPulseDelayValue by IntegerValue("MinPulseDelay", 1000, 100..5000) { pulseValue }
    private val maxPulseDelayValue by IntegerValue("MaxPulseDelay", 1500, 100.. 5000) { pulseValue }

    private val fakePlayerMenu by BoolValue("FakePlayer", true)
    private val pulseTimer = MSTimer()
    private var pulseDelay = 0
    private var fakePlayer: EntityOtherPlayerMP? = null
    private val positions = LinkedList<DoubleArray>()
    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()

    override fun onEnable() {
        if (mc.thePlayer == null) return

        pulseTimer.reset()
        pulseDelay = RandomUtils.nextInt(minPulseDelayValue, maxPulseDelayValue)

        if (fakePlayerMenu) {
            fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile).apply {
                clonePlayer(mc.thePlayer, true)
                copyLocationAndAnglesFrom(mc.thePlayer)
                rotationYawHead = mc.thePlayer.rotationYawHead
                mc.theWorld.addEntityToWorld(-1337, this)
            }
        }

        BlinkUtils.setBlinkState(all = true)
        packets.clear()
        synchronized(positions) {
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight() / 2, mc.thePlayer.posZ))
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ))
        }
    }

    override fun onDisable() {
        synchronized(positions) { positions.clear() }
        if (mc.thePlayer == null) return

        BlinkUtils.setBlinkState(off = true, release = true)
        clearPackets()
        fakePlayer?.let {
            mc.theWorld.removeEntityFromWorld(it.entityId)
            fakePlayer = null
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (mc.thePlayer == null || mc.thePlayer.isDead) return

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
                    if (outgoingValue) BlinkUtils.syncSent()
                }
                "received" -> {
                    if (inboundValue) BlinkUtils.syncReceived()
                }
            }

            if (pulseValue && pulseTimer.hasTimePassed(pulseDelay)) {
                BlinkUtils.unblink()
                if (fakePlayerMenu) {
                    BlinkUtils.addFakePlayer()
                }
                pulseTimer.reset()
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(positions) {
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ))
        }

        if (pulseValue && pulseTimer.hasTimePassed(pulseDelay.toLong())) {
            synchronized(positions) { positions.clear() }
            BlinkUtils.releasePacket()
            clearPackets()
            pulseTimer.reset()
            pulseDelay = RandomUtils.nextInt(minPulseDelayValue, maxPulseDelayValue)
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

    override val tag: String
        get() = (BlinkUtils.packets.size + BlinkUtils.packetsReceived.size).toString()

    private fun clearPackets() {
        synchronized(packets) { packets.clear() }
    }

    fun blinkingSend() = handleEvents() && (mode == "Sent" || mode == "Both")
    fun blinkingReceive() = handleEvents() && (mode == "Received" || mode == "Both")
}
