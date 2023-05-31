/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.utils.extensions.getEntitiesInRadius
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import java.lang.ref.SoftReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * Entity AABB cacher for backtracing entities
 */
class LocationCache : MinecraftInstance(), Listenable {
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            val theWorld = mc.theWorld ?: return
            val thePlayer = mc.thePlayer ?: return

            playerLocationList.add(Location(Vec3(thePlayer.posX, thePlayer.entityBoundingBox.minY, thePlayer.posZ), RotationUtils.serverRotation))

            val entities = theWorld.getEntitiesInRadius(thePlayer, DISCOVER_RANGE)

            // Manual garbage collect by distance check
            aabbList.keys.filterNot(entities.map(Entity::getEntityId)::contains).forEach(aabbList::remove)

            // Add entity locations to ring buffer
            for (entity in entities) {
                aabbList.getOrPut(entity.entityId) { SoftReference(RingBuffer(CACHE_SIZE)) }.get()?.add(entity.entityBoundingBox)
            }
        }
    }

    override fun handleEvents(): Boolean = true

    companion object {
        private const val CACHE_SIZE = 50
        private const val DISCOVER_RANGE = 64.0

        private val aabbList = HashMap<Int, SoftReference<RingBuffer<AxisAlignedBB>>>(CACHE_SIZE)
        private val playerLocationList = RingBuffer<Location>(CACHE_SIZE)

        fun getPreviousAABB(entityId: Int, ticksBefore: Int, default: AxisAlignedBB): AxisAlignedBB {
            if (aabbList.isEmpty()) return default
            return aabbList[entityId]?.get()?.run { get(size - ticksBefore - 1) } ?: default
        }

        fun getPreviousPlayerLocation(ticksBefore: Int, default: Location): Location = playerLocationList[playerLocationList.size - ticksBefore - 1]
                ?: default
    }
}

/**
 * A simple ring buffer (circular queue) implementation
 * Referenced 'https://gist.github.com/ToxicBakery/05d3d98256aaae50bfbde04ae0c62dbd'
 */
class RingBuffer<T>(val maxCapacity: Int) : Iterable<T> {
    private val array: Array<Any?> = Array(maxCapacity) { null }

    var size = 0
        private set

    private val head
        get() = if (size == maxCapacity) (tail + 1) % size else 0
    private var tail = 0

    fun add(element: T) {
        tail = (tail + 1) % maxCapacity
        array[tail] = element
        if (size < maxCapacity) size++
    }

    operator fun get(index: Int): T? {
        return when {
            size == 0 || index >= size || index < 0 -> null // IndexOOB
            size == maxCapacity -> array[(head + index) % maxCapacity] // Index circulation
            else -> array[index] // Default
        } as? T?
    }

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private val index: AtomicInteger = AtomicInteger(0)

        override fun hasNext(): Boolean = index.get() < size

        override fun next(): T = get(index.getAndIncrement()) ?: throw NoSuchElementException()
    }
}

data class Location(val position: Vec3, val rotation: Rotation)
