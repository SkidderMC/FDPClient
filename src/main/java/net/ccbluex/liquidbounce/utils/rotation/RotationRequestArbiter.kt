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

    data class ActiveRequest(val owner: Any, val priority: Int, val sequence: Long) {
        /** Ticks elapsed since the owner last refreshed (re-acquired) this lease. */
        var idleTicks: Int = 0
    }

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

    /**
     * Safety backstop: auto-releases a lease that has not been refreshed for more than [maxIdleTicks]
     * ticks, so a request whose owner forgot to release it can never block lower-priority modules
     * forever. Runs well after the normal ResetTicks countdown, so it never interferes with active aiming.
     */
    fun tick(maxIdleTicks: Int) {
        val current = activeRequest ?: return

        current.idleTicks++
        if (current.idleTicks > maxIdleTicks) {
            activeRequest = null
        }
    }

    fun clear() {
        activeRequest = null
    }
}
