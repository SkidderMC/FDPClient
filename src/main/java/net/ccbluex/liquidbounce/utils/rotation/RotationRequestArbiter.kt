/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

enum class RotationPriority(val level: Int) {
    BACKGROUND(0),
    LOW(100),
    NORMAL(200),
    HIGH(300),
    CRITICAL(400),
}

/**
 * Keeps one active rotation owner and rejects lower-priority requests until it releases or resets.
 * Requests from the current owner always refresh their lease; equal priority preserves legacy last-writer behavior.
 */
class RotationRequestArbiter {

    data class ActiveRequest(val owner: Any, val priority: Int, val sequence: Long)

    var activeRequest: ActiveRequest? = null
        private set

    private var sequence = 0L

    fun canAcquire(owner: Any, priority: Int): Boolean {
        val current = activeRequest

        return current == null || current.owner === owner || priority >= current.priority
    }

    fun tryAcquire(owner: Any, priority: Int): Boolean {
        if (!canAcquire(owner, priority)) return false

        activeRequest = ActiveRequest(owner, priority, ++sequence)
        return true
    }

    fun owns(owner: Any) = activeRequest?.owner === owner

    fun release(owner: Any): Boolean {
        if (!owns(owner)) return false

        activeRequest = null
        return true
    }

    fun clear() {
        activeRequest = null
    }
}
