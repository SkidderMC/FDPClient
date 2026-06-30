/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object ConfigSystem {

    private val loadingScopes = AtomicInteger(1)
    private val initialLoading = AtomicBoolean(true)

    @JvmStatic
    val isLoadingConfig: Boolean
        get() = loadingScopes.get() > 0

    /**
     * Suppresses persistence side effects and live UI events for exactly the lifetime of [block].
     * Scopes are re-entrant and exception-safe, so a failed or nested load cannot leave a global flag stuck.
     */
    fun <T> withConfigLoading(block: () -> T): T {
        loadingScopes.incrementAndGet()
        return try {
            block()
        } finally {
            loadingScopes.decrementAndGet()
        }
    }

    /** Releases the bootstrap scope created before modules and values are initialized. */
    fun completeInitialLoading() {
        if (initialLoading.compareAndSet(true, false)) {
            loadingScopes.decrementAndGet()
        }
    }
}
