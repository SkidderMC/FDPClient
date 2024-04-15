/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.getLookingTargetRange
import net.ccbluex.liquidbounce.utils.extensions.getNearestPointBB
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.ThreadQuickExitException
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.*
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.util.*

@ModuleInfo(name = "FakeLag", category = ModuleCategory.COMBAT)
object FakeLag : Module() {
    /**
     * BackTrack
     */
    val modeValue = ListValue("LagMode", arrayOf("PacketDelay", "Automatic", "Manual"), "Automatic")
    private val esp = BoolValue("Render-Server-Pos", true)
    private val timeValue = IntegerValue("Time", 200, 0, 2000).displayable { modeValue.equals("Automatic") }
    private val onlyPlayer = BoolValue("OnlyPlayer", true).displayable { modeValue.equals("Automatic") }
    private val sizeValue = IntegerValue("Delay-Size", 100, 0, 1000).displayable { modeValue.equals("PacketDelay") }

    private var needFreeze = false
    private val storagePackets = ArrayList<Packet<INetHandlerPlayClient>>()
    private val storageEntities = ArrayList<Entity>()

    private var timer = MSTimer()
    private var attacked: Entity? = null

    private val packets: LinkedList<PacketEvent> = LinkedList()
    private val entityPosMap: LinkedHashMap<Int, PosData> = LinkedHashMap()
    private var hasPlace = false
    override fun onDisable() {
        releasePackets()
        hasPlace = false
        clear()
    }

    fun fakeLagPacket(event: PacketEvent) {
        if (modeValue.equals("Automatic") || modeValue.equals("Manual")) {
            mc.thePlayer ?: return
            val packet = event.packet
            val theWorld = mc.theWorld!!
            if (packet.javaClass.name.contains("net.minecraft.network.play.server.", true)) {
                if (packet is S14PacketEntity) {
                    val entity = packet.getEntity(theWorld) ?: return
                    if (entity !is EntityLivingBase) return
                    if (onlyPlayer.get() && entity !is EntityPlayer) return
                    entity.serverPosX += packet.func_149062_c().toInt()
                    entity.serverPosY += packet.func_149061_d().toInt()
                    entity.serverPosZ += packet.func_149064_e().toInt()
                    val x = entity.serverPosX.toDouble() / 32.0
                    val y = entity.serverPosY.toDouble() / 32.0
                    val z = entity.serverPosZ.toDouble() / 32.0
                    if (EntityUtils.isSelected(entity, true)) {
                        val afterBB = AxisAlignedBB(x - 0.4F, y - 0.1F, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)
                        val afterRange: Double
                        val eyes = mc.thePlayer!!.getPositionEyes(1F)
                        afterRange = getNearestPointBB(eyes, afterBB).distanceTo(eyes)
                        val beforeRange: Double = mc.thePlayer!!.getDistanceToEntityBox(entity)


                        if (beforeRange <= 6) {
                            if (afterRange in 0F..6F && (afterRange > beforeRange + 0.02)) {
                                if (!needFreeze) {
                                    timer.reset()
                                    needFreeze = true
                                }
                                if (!storageEntities.contains(entity)) storageEntities.add(entity)
                                event.cancelEvent()
                                return
                            }
                        } else {
                            if (!modeValue.equals("Manual")) {
                                if (afterRange < beforeRange) {
                                    if (needFreeze) releasePackets()
                                }
                            }
                        }
                    }
                    if (needFreeze) {
                        if (!storageEntities.contains(entity)) storageEntities.add(entity)
                        event.cancelEvent()
                        return
                    }
                    if (!event.isCancelled && !needFreeze) {
                        FDPClient.eventManager.callEvent(EntityMovementEvent(entity))
                        val f =
                            if (packet.func_149060_h()) (packet.func_149066_f() * 360).toFloat() / 256.0f else entity.rotationYaw
                        val f1 =
                            if (packet.func_149060_h()) (packet.func_149063_g() * 360).toFloat() / 256.0f else entity.rotationPitch
                        entity.setPositionAndRotation2(x, y, z, f, f1, 3, false)
                        entity.onGround = packet.onGround
                    }
                    event.cancelEvent()
                    //                storageEntities.add(entity)
                } else {
                    if (needFreeze && !event.isCancelled) {
                        if (packet is S19PacketEntityStatus) {
                            if (packet.opCode == 2.toByte()) return
                        }
                        storagePackets.add(packet as Packet<INetHandlerPlayClient>)
                        event.cancelEvent()
                    }
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (modeValue.equals("PacketDelay")) {
            if (entityPosMap.isEmpty()) return
            for (value in entityPosMap.values) {
                value.update()
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (modeValue.equals("PacketDelay")) {
            if (event.isCancelled) return
            val packet: Packet<*> = event.packet
            if (packet is S03PacketTimeUpdate) return
            if (packet is S01PacketJoinGame || packet is S07PacketRespawn) {
                clear()
                return
            }
            if (packet.javaClass.name.startsWith("net.minecraft.network.play.server.")) {
                packets.add(event)
                event.cancelEvent()
                when (packet) {
                    is S0CPacketSpawnPlayer -> {
                        val spawn = packet
                        if (entityPosMap.containsKey(spawn.entityID)) return
                        val pd = PosData()
                        pd.prevX = (spawn.x.also { pd.x = it }) / 32.0
                        pd.prevY = (spawn.y.also { pd.y = it }) / 32.0
                        pd.prevZ = (spawn.z.also { pd.z = it }) / 32.0
                        entityPosMap[spawn.entityID] = pd
                    }

                    is S14PacketEntity -> {
                        val pd: PosData
                        if (!entityPosMap.containsKey(packet.getEntity(mc.theWorld).entityId)) {
                            val entity = packet.getEntity(mc.theWorld)
                            if (entity == null || entity is EntityArmorStand) return
                            pd = PosData()
                            pd.x = entity.serverPosX
                            pd.y = entity.serverPosY
                            pd.z = entity.serverPosZ
                            val adder = entity.collisionBorderSize
                            pd.width = entity.width / 2 + adder
                            pd.height = entity.height + adder
                            entityPosMap[packet.getEntity(mc.theWorld).entityId] = pd
                        } else pd = entityPosMap[packet.getEntity(mc.theWorld).entityId]!!
                        pd.motionX(packet.func_149062_c())
                        pd.motionY(packet.func_149061_d())
                        pd.motionZ(packet.func_149064_e())
                    }

                    is S18PacketEntityTeleport -> {
                        val pd: PosData
                        if (!entityPosMap.containsKey(packet.entityId)) {
                            pd = PosData()
                            val entity: Entity
                            if ((mc.theWorld.getEntityByID(packet.entityId).also { entity = it }) != null) {
                                val adder = entity.collisionBorderSize
                                pd.width = entity.width / 2 + adder
                                pd.height = entity.height + adder
                            }
                            entityPosMap[packet.entityId] = pd
                        } else pd = entityPosMap[packet.entityId]!!
                        pd.x = packet.x
                        pd.y = packet.y
                        pd.z = packet.z
                    }
                }
            } else if (packet is C08PacketPlayerBlockPlacement) {
                val place: C08PacketPlayerBlockPlacement = packet as C08PacketPlayerBlockPlacement
                if (place.placedBlockDirection == 255) return
                hasPlace = true
            }
        }
    }

    private fun clear(size: Int) {
        if (modeValue.equals("PacketDelay")) {
            if (packets.isEmpty()) return
            val handler: INetHandlerPlayClient = mc.netHandler
            while (packets.size > size) {
                val event = packets.pollFirst() ?: continue
                try {
                    val packet = event.packet as Packet<INetHandlerPlayClient>
                    packet.processPacket(handler)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) return
        if (modeValue.equals("PacketDelay")) {
            if (packets.isEmpty()) return;
            clear(this.sizeValue.get());
        } else {
            if (needFreeze) {
                if (!modeValue.equals("Manual")) {
                    if (timer.hasTimePassed(timeValue.get().toLong())) {
                        releasePackets()
                        return
                    }
                }
                if (storageEntities.isNotEmpty()) {
                    var release = false // for-each
                    for (entity in storageEntities) {
                        val x = entity.serverPosX.toDouble() / 32.0
                        val y = entity.serverPosY.toDouble() / 32.0
                        val z = entity.serverPosZ.toDouble() / 32.0
                        val entityBB = AxisAlignedBB(x - 0.4F, y - 0.1F, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)
                        var range = entityBB.getLookingTargetRange(mc.thePlayer!!)
                        if (range == Double.MAX_VALUE) {
                            val eyes = mc.thePlayer!!.getPositionEyes(1F)
                            range = getNearestPointBB(eyes, entityBB).distanceTo(eyes) + 0.075
                        }
                        if (range <= 0.5) {
                            release = true
                            break
                        }
                        val entity1 = attacked
                        if (entity1 != entity) continue
                        if (!modeValue.equals("Manual")) {
                            if (timer.hasTimePassed(timeValue.get().toLong())) {
                                if (range >= 6) {
                                    release = true
                                    break
                                }
                            }
                        }
                    }
                    if (!modeValue.equals("Manual")) {
                        if (release) releasePackets()
                    }
                }
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (modeValue.equals("PacketDelay")) {
            clear()
        } else {
            attacked = null
            storageEntities.clear()
            if (event.worldClient == null) storagePackets.clear()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!esp.get()) return
        // pre draw
        GL11.glPushMatrix()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)

        // drawing
        if (modeValue.equals("PacketDelay")) {
            for (data in entityPosMap.values) {
                if (FDPClient.combatManager.target != null) {
                    val px: Double = data.posX - mc.renderManager.renderPosX
                    val py: Double = data.posY - mc.renderManager.renderPosY
                    val pz: Double = data.posZ - mc.renderManager.renderPosZ
                    val bb = AxisAlignedBB(
                        px - data.width,
                        py,
                        pz - data.width,
                        px + data.width,
                        py + data.height,
                        pz + data.width
                    )
                    if (FDPClient.combatManager.target!!.hurtTime > 0) RenderUtils.glColor(
                        255,
                        32,
                        32,
                        35
                    ) else RenderUtils.glColor(32, 255, 32, 35)
                    RenderUtils.drawFilledBox(bb)
                }
            }
        } else {
            val renderManager = mc.renderManager
            for (entity in storageEntities) {
                val x = entity.serverPosX.toDouble() / 32.0 - renderManager.renderPosX
                val y = entity.serverPosY.toDouble() / 32.0 - renderManager.renderPosY
                val z = entity.serverPosZ.toDouble() / 32.0 - renderManager.renderPosZ
                if (entity is EntityPlayer) {
                    if (entity.hurtTime > 0) {
                        RenderUtils.glColor(255, 32, 32, 35)
                    } else RenderUtils.glColor(32, 255, 32, 35)
                    RenderUtils.drawFilledBox(AxisAlignedBB(x - 0.4F, y, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F))
                }
            }
        }

        // post draw
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glPopMatrix()
    }

    private fun releasePackets() {
        if (modeValue.equals("PacketDelay")) return
        attacked = null
        val netHandler: INetHandlerPlayClient = mc.netHandler
        if (storagePackets.isEmpty()) return
        while (storagePackets.isNotEmpty()) {
            storagePackets.removeAt(0).let {
                try {
                    val packetEvent = PacketEvent(it, PacketEvent.Type.SEND)
                    if (!PacketUtils.packets.contains(it)) FDPClient.eventManager.callEvent(packetEvent)
                    if (!packetEvent.isCancelled) it.processPacket(netHandler)
                } catch (_: ThreadQuickExitException) {
                }
            }
        }
        while (storageEntities.isNotEmpty()) {
            storageEntities.removeAt(0).let { entity ->
                if (!entity.isDead) {
                    val x = entity.serverPosX.toDouble() / 32.0
                    val y = entity.serverPosY.toDouble() / 32.0
                    val z = entity.serverPosZ.toDouble() / 32.0
                    entity.setPosition(x, y, z)
                }
            }
        }
        needFreeze = false
    }

    private fun clear() {
        if (modeValue.equals("PacketDelay")) {
            clear(0)
            entityPosMap.clear()
        }
    }

    override val tag: String?
        get() = modeValue.get()
}
private class PosData {
    var height: Float = 1.9f
    var width: Float = 0.4f
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0
    var prevX: Double = 0.0
    var prevY: Double = 0.0
    var prevZ: Double = 0.0
    private var increment = 0

    val posX: Double
        get() = x / 32.0

    val posY: Double
        get() = y / 32.0

    val posZ: Double
        get() = z / 32.0

    fun motionX(x: Byte) {
        prevX = posX
        this.x += x
        increment = 3
    }

    fun motionY(y: Byte) {
        prevY = posY
        this.y += y
        increment = 3
    }

    fun motionZ(z: Byte) {
        prevZ = posZ
        this.z += z
        increment = 3
    }

    fun update() {
        if (increment > 0) {
            prevX += ((x / 32.0) - prevX) / increment
            prevY += ((y / 32.0) - prevY) / increment
            prevZ += ((z / 32.0) - prevZ) / increment
            --increment
        }
    }
}
