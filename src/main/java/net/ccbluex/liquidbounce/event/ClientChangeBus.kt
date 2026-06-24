/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import java.util.concurrent.CopyOnWriteArrayList

/** Lightweight change notifications for integrations that must not run on the game thread. */
sealed class ClientChange {
    data class ModuleState(val moduleName: String, val enabled: Boolean, val hidden: Boolean) : ClientChange()
    data class ValueState(val ownerName: String, val valueName: String) : ClientChange()
    data class Configuration(val name: String) : ClientChange()
    data class Command(val name: String) : ClientChange()
    data class WorldState(val connected: Boolean) : ClientChange()
}

object ClientChangeBus {
    private val listeners = CopyOnWriteArrayList<(ClientChange) -> Unit>()

    fun subscribe(listener: (ClientChange) -> Unit): () -> Unit {
        listeners += listener
        return { listeners -= listener }
    }

    fun publish(change: ClientChange) {
        listeners.forEach { listener -> runCatching { listener(change) } }
    }
}
