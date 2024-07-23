/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PathUtils
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.PlayerCapabilities
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C13PacketPlayerAbilities
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.Vec3
import kotlin.concurrent.thread
import org.lwjgl.opengl.GL11
import kotlin.math.sqrt

object InfiniteAura : Module(name = "InfiniteAura", category = Category.COMBAT, spacedName = "Infinite Aura", hideModule = false) {
    private val packetValue by ListValue("PacketMode", arrayOf("PacketPosition", "PacketPosLook"), "PacketPosition")
    private val packetBack by BoolValue("DoTeleportBackPacket", false)
    private val modeValue by ListValue("Mode", arrayOf("Aura", "Click"), "Aura")
    private val targetsValue by IntegerValue("Targets", 3, 1..10) { modeValue == "Aura" }
    private val cpsValue by IntegerValue("CPS", 1, 1..10)
    private val distValue by IntegerValue("Distance", 30, 20..100)
    private val moveDistanceValue by FloatValue("MoveDistance", 5f, 2f..15f)
    private val noRegenValue by BoolValue("NoRegen", true)
    private val noLagBackValue by BoolValue("NoLagback", true)
    private val swingValue by BoolValue("Swing", true) { modeValue == "Aura" }
    private val pathRenderValue by BoolValue("PathRender", true)

    var lastTarget: EntityLivingBase? = null
    val timer = MSTimer()
    var points = mutableListOf<Vec3>()
    var thread: Thread? = null

    private fun getDelay(): Int {
        return 1000 / cpsValue
    }

    override fun onEnable() {
        timer.reset()
        points.clear()
    }

    override fun onDisable() {
        timer.reset()
        points.clear()
        thread?.interrupt()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!timer.hasTimePassed(getDelay().toLong())) return
        if (thread?.isAlive == true) return
        when (modeValue.lowercase()) {
            "aura" -> {
                thread = thread(name = "InfiniteAura") {
                    // do it async because a* pathfinding need some time
                    doTpAura()
                }
                points.clear()
                timer.reset()
            }

            "click" -> {
                if (mc.gameSettings.keyBindAttack.isKeyDown) {
                    thread = thread(name = "InfiniteAura") {
                        // do it async because a* pathfinding need some time
                        val entity = RaycastUtils.raycastEntity(distValue.toDouble()) { entity -> entity != null && EntityUtils.isSelected(entity, true) } ?: return@thread
                        if (mc.thePlayer.getDistanceToEntity(entity) < 3) {
                            return@thread
                        }

                        hit(entity as EntityLivingBase, true)
                    }
                    timer.reset()
                }
                points.clear()
            }
        }
    }

    private fun doTpAura() {
        val targets = mc.theWorld.loadedEntityList.filter { it is EntityLivingBase &&
                EntityUtils.isSelected(it, true) &&
                mc.thePlayer.getDistanceToEntity(it) < distValue }.toMutableList()
        if (targets.isEmpty()) return
        targets.sortBy { mc.thePlayer.getDistanceToEntity(it) }

        var count = 0
        for (entity in targets) {
            if (hit(entity as EntityLivingBase)) {
                count++
            }
            if (count > targetsValue) break
        }
    }

    private fun hit(entity: EntityLivingBase, force: Boolean = false): Boolean {
        val path = PathUtils.findBlinkPath(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, entity.posX, entity.posY, entity.posZ, moveDistanceValue.toDouble())
        if (path.isEmpty()) return false
        val lastDistance = path.last().let { entity.getDistance(it.xCoord, it.yCoord, it.zCoord) }
        if (!force && lastDistance > 10) return false // pathfinding has failed

        path.forEach {
            if (packetValue == "PacketPosition") {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(it.xCoord, it.yCoord, it.zCoord, true))
            } else {
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C06PacketPlayerPosLook(
                        it.xCoord,
                        it.yCoord,
                        it.zCoord,
                        mc.thePlayer.rotationYaw,
                        mc.thePlayer.rotationPitch,
                        true
                    )
                )
            }
            points.add(it)
        }

        if (lastDistance > 3 && packetBack) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(entity.posX, entity.posY, entity.posZ, true))
        }

        if (swingValue) {
            mc.thePlayer.swingItem()
        } else {
            mc.netHandler.addToSendQueue(C0APacketAnimation())
        }
        mc.playerController.attackEntity(mc.thePlayer, entity)

        for (i in path.size - 1 downTo 0) {
            val vec = path[i]
            if (packetValue == "PacketPosition") {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vec.xCoord, vec.yCoord, vec.zCoord, true))
            } else {
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C06PacketPlayerPosLook(
                        vec.xCoord,
                        vec.yCoord,
                        vec.zCoord,
                        mc.thePlayer.rotationYaw,
                        mc.thePlayer.rotationPitch,
                        true
                    )
                )
            }
        }
        if (packetBack) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        }
        return true
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S08PacketPlayerPosLook) {
            timer.reset()
        }
        val isMovePacket = (event.packet is C04PacketPlayerPosition || event.packet is C03PacketPlayer.C06PacketPlayerPosLook)
        if (noRegenValue && event.packet is C03PacketPlayer && !isMovePacket) {
            event.cancelEvent()
        }
        if (noLagBackValue && event.packet is S08PacketPlayerPosLook) {
            val capabilities = PlayerCapabilities()
            capabilities.allowFlying = true
            mc.netHandler.addToSendQueue(C13PacketPlayerAbilities(capabilities)) // Packet C13

            val x = event.packet.x - mc.thePlayer.posX
            val y = event.packet.y - mc.thePlayer.posY
            val z = event.packet.z - mc.thePlayer.posZ
            val diff = sqrt(x * x + y * y + z * z)
            event.cancelEvent() // cancel
            sendPacket(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    event.packet.x,
                    event.packet.y,
                    event.packet.z,
                    event.packet.yaw,
                    event.packet.pitch,
                    true
                )
            )
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        synchronized(points) {
            if (points.isEmpty() || !pathRenderValue) return
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            GL11.glPushMatrix()
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glShadeModel(GL11.GL_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDepthMask(false)

            rainbow()

            for (vec in points) {
                val x = vec.xCoord - renderPosX
                val y = vec.yCoord - renderPosY
                val z = vec.zCoord - renderPosZ
                val width = 0.3
                val height = mc.thePlayer.eyeHeight.toDouble()
                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2)
                GL11.glLineWidth(2f)
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y + height, z - width)
                GL11.glVertex3d(x + width, y + height, z - width)
                GL11.glVertex3d(x + width, y, z - width)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y, z + width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x + width, y, z + width)
                GL11.glVertex3d(x + width, y + height, z + width)
                GL11.glVertex3d(x - width, y + height, z + width)
                GL11.glVertex3d(x - width, y, z + width)
                GL11.glVertex3d(x + width, y, z + width)
                GL11.glVertex3d(x + width, y, z - width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x + width, y + height, z + width)
                GL11.glVertex3d(x + width, y + height, z - width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x - width, y + height, z + width)
                GL11.glVertex3d(x - width, y + height, z - width)
                GL11.glEnd()
            }

            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPopMatrix()
            GL11.glColor4f(1f, 1f, 1f, 1f)
        }
    }
}