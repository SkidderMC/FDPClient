/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import com.google.common.collect.Queues
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity
import net.ccbluex.liquidbounce.features.module.modules.combat.FakeLag
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.*
import net.minecraft.util.Vec3
import kotlin.math.roundToInt

object PacketUtils : MinecraftInstance(), Listenable {

    val queuedPackets = Queues.newArrayDeque<Packet<*>>()

    @EventTarget(priority = 2)
    fun onTick(event: GameTickEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase) {
                (entity as? IMixinEntity)?.apply {
                    if (!truePos) {
                        updateSpawnPosition(entity.currPos)
                    }
                }
            }
        }
    }

    @EventTarget(priority = 2)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val world = mc.theWorld ?: return

        when (packet) {
            is S0CPacketSpawnPlayer -> (world.getEntityByID(packet.entityID) as? IMixinEntity)?.apply {
                updateSpawnPosition(Vec3(packet.realX, packet.realY, packet.realZ))
            }


            is S0FPacketSpawnMob -> (world.getEntityByID(packet.entityID) as? IMixinEntity)?.apply {
                updateSpawnPosition(Vec3(packet.realX, packet.realY, packet.realZ))
            }

            is S14PacketEntity -> {
                val entity = packet.getEntity(world)
                val mixinEntity = entity as? IMixinEntity

                mixinEntity?.apply {
                    if (!truePos) {
                        updateSpawnPosition(entity.currPos)
                    }

                    trueX += packet.realMotionX
                    trueY += packet.realMotionY
                    trueZ += packet.realMotionZ
                }
            }

            is S18PacketEntityTeleport -> (world.getEntityByID(packet.entityId) as? IMixinEntity)?.apply {
                updateSpawnPosition(Vec3(packet.realX, packet.realY, packet.realZ), true)
            }
        }
    }

    @EventTarget(priority = -5)
    fun onGameLoop(event: GameLoopEvent) {
        synchronized(queuedPackets) {
            queuedPackets.removeAll {
                handlePacket(it)
                val packetEvent = PacketEvent(it, EventState.RECEIVE)
                FakeLag.onPacket(packetEvent)
                Velocity.onPacket(packetEvent)

                true
            }
        }
    }

    @EventTarget(priority = -1)
    fun onDisconnect(event: WorldEvent) {
        if (event.worldClient == null) {
            synchronized(queuedPackets) {
                queuedPackets.clear()
            }
        }
    }

    @JvmStatic
    fun sendPacket(packet: Packet<*>, triggerEvent: Boolean = true) {
        if (triggerEvent) {
            mc.netHandler?.addToSendQueue(packet)
            return
        }

        val netManager = mc.netHandler?.networkManager ?: return

        PPSCounter.registerType(PPSCounter.PacketType.SEND)
        if (netManager.isChannelOpen) {
            netManager.flushOutboundQueue()
            netManager.dispatchPacket(packet, null)
        } else {
            netManager.readWriteLock.writeLock().lock()
            try {
                netManager.outboundPacketsQueue += NetworkManager.InboundHandlerTuplePacketListener(packet, null)
            } finally {
                netManager.readWriteLock.writeLock().unlock()
            }
        }
    }

    @JvmStatic
    fun sendPackets(vararg packets: Packet<*>, triggerEvents: Boolean = true) =
        packets.forEach { sendPacket(it, triggerEvents) }

    fun handlePackets(vararg packets: Packet<*>) =
        packets.forEach { handlePacket(it) }

    fun handlePacket(packet: Packet<*>?) {
        runCatching { (packet as Packet<INetHandlerPlayClient>).processPacket(mc.netHandler) }.onSuccess {
            PPSCounter.registerType(PPSCounter.PacketType.RECEIVED)
        }
    }

    val Packet<*>.type
        get() = when (this.javaClass.simpleName[0]) {
            'C' -> PacketType.CLIENT
            'S' -> PacketType.SERVER
            else -> PacketType.UNKNOWN
        }

    enum class PacketType { CLIENT, SERVER, UNKNOWN }
}

fun IMixinEntity.updateSpawnPosition(target: Vec3, ignoreInterpolation: Boolean = false) {
    trueX = target.xCoord
    trueY = target.yCoord
    trueZ = target.zCoord
    if (!ignoreInterpolation) {
        lerpX = trueX
        lerpY = trueY
        lerpZ = trueZ
    }
    truePos = true
}

fun interpolatePosition(entity: IMixinEntity) = entity.run {
    val delta = RenderUtils.deltaTimeNormalized(150)
    lerpX += (trueX - lerpX) * delta
    lerpY += (trueY - lerpY) * delta
    lerpZ += (trueZ - lerpZ) * delta
}

var S12PacketEntityVelocity.realMotionX
    get() = motionX / 8000.0
    set(value) {
        motionX = (value * 8000.0).roundToInt()
    }
var S12PacketEntityVelocity.realMotionY
    get() = motionY / 8000.0
    set(value) {
        motionX = (value * 8000.0).roundToInt()
    }
var S12PacketEntityVelocity.realMotionZ
    get() = motionZ / 8000.0
    set(value) {
        motionX = (value * 8000.0).roundToInt()
    }

val S14PacketEntity.realMotionX
    get() = func_149062_c() / 32.0
val S14PacketEntity.realMotionY
    get() = func_149061_d() / 32.0
val S14PacketEntity.realMotionZ
    get() = func_149064_e() / 32.0

var S0EPacketSpawnObject.realX
    get() = x / 32.0
    set(value) {
        x = (value * 32.0).roundToInt()
    }
var S0EPacketSpawnObject.realY
    get() = y / 32.0
    set(value) {
        y = (value * 32.0).roundToInt()
    }
var S0EPacketSpawnObject.realZ
    get() = z / 32.0
    set(value) {
        z = (value * 32.0).roundToInt()
    }

val S0CPacketSpawnPlayer.realX
    get() = x / 32.0
val S0CPacketSpawnPlayer.realY
    get() = y / 32.0
val S0CPacketSpawnPlayer.realZ
    get() = z / 32.0

val S0FPacketSpawnMob.realX
    get() = x / 32.0
val S0FPacketSpawnMob.realY
    get() = y / 32.0
val S0FPacketSpawnMob.realZ
    get() = z / 32.0

val S18PacketEntityTeleport.realX
    get() = x / 32.0
val S18PacketEntityTeleport.realY
    get() = y / 32.0
val S18PacketEntityTeleport.realZ
    get() = z / 32.0

var C03PacketPlayer.rotation
    get() = Rotation(yaw, pitch)
    set(value) {
        yaw = value.yaw
        pitch = value.pitch
    }
var C03PacketPlayer.pos
    get() = Vec3(x, y, z)
    set(value) {
        x = value.xCoord
        y = value.yCoord
        z = value.zCoord
    }
fun schedulePacketProcess(packet: Packet<*>) {
    synchronized(PacketUtils.queuedPackets) {
        PacketUtils.queuedPackets.add(packet)
    }
}
fun schedulePacketProcess(packets: Collection<Packet<*>>) {
    synchronized(PacketUtils.queuedPackets) {
        PacketUtils.queuedPackets.addAll(packets)
    }
}