/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import net.ccbluex.liquidbounce.utils.client.ClientUtils

class EventHook<T : Event>(
    val owner: Listenable,
    val always: Boolean = false,
    val priority: Byte = 0,
    private val action: (T) -> Unit
) {
    val isActive: Boolean
        get() = this.owner.handleEvents() || this.always

    internal fun processEvent(event: T) {
        if (!this.isActive)
            return

        try {
            action(event)
        } catch (e: Exception) {
            ClientUtils.LOGGER.error(
                "Exception during processing event, owner=${owner.javaClass.simpleName}, event=$event",
                e
            )
        }
    }

    override fun toString(): String {
        return "EventHook{owner=$owner, always=$always, priority=$priority, action=$action}"
    }
}