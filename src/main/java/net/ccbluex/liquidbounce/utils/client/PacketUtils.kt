/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import kotlinx.coroutines.Dispatchers
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.FakeLag
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.extensions.currPos
import net.ccbluex.liquidbounce.utils.kotlin.removeEach
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.*
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayDeque
import kotlin.concurrent.withLock
import kotlin.concurrent.write
import kotlin.math.roundToInt

object PacketUtils : MinecraftInstance, Listenable {

    private val queuedPackets = ArrayDeque<Packet<*>>()
    private val queueLock = ReentrantLock()

    fun schedulePacketProcess(elements: Collection<Packet<*>>): Boolean = queueLock.withLock {
        queuedPackets.addAll(elements)
    }

    fun schedulePacketProcess(element: Packet<*>): Boolean = queueLock.withLock {
        queuedPackets.add(element)
    }

    fun isQueueEmpty(): Boolean = queueLock.withLock {
        queuedPackets.isEmpty()
    }

    val onTick = handler<GameTickEvent>(priority = 2) {
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

    val onPacket = handler<PacketEvent>(dispatcher = Dispatchers.Main, priority = 2) { event ->
        val world = mc.theWorld ?: return@handler

        when (val packet = event.packet) {
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

    val onGameLoop = handler<GameLoopEvent>(priority = -5) {
        if (EventManager.call(DelayedPacketProcessEvent()).isCancelled) {
            return@handler
        }

        queueLock.withLock {
            queuedPackets.removeEach { packet ->
                handlePacket(packet)
                val packetEvent = PacketEvent(packet, EventState.RECEIVE)
                EventManager.call(packetEvent, FakeLag)
                EventManager.call(packetEvent, Velocity)

                true
            }
        }
    }

    val onWorld = handler<WorldEvent>(priority = -1) { event ->
        if (event.worldClient == null) {
            queueLock.withLock {
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
            netManager.readWriteLock.write {
                netManager.outboundPacketsQueue += NetworkManager.InboundHandlerTuplePacketListener(packet, null)
            }
        }
    }

    @JvmStatic
    fun sendPackets(vararg packets: Packet<*>, triggerEvents: Boolean = true) =
        packets.forEach { sendPacket(it, triggerEvents) }

    @JvmStatic
    fun handlePackets(vararg packets: Packet<*>) =
        packets.forEach { handlePacket(it) }

    @JvmStatic
    private fun handlePacket(packet: Packet<*>?) {
        runCatching { (packet as Packet<INetHandlerPlayClient>).processPacket(mc.netHandler) }.onSuccess {
            PPSCounter.registerType(PPSCounter.PacketType.RECEIVED)
        }
    }
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
    val delta = RenderUtils.deltaTimeNormalized(3)

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

val BlockBBEvent.pos
    get() = BlockPos(x, y, z)

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