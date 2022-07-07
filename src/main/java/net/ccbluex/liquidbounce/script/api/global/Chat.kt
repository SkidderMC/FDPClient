/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.script.api.global

import net.ccbluex.liquidbounce.utils.ClientUtils

/**
 * Object used by the script API to provide an easier way of calling chat-related methods.
 */
object Chat {

    /**
     * Prints a message to the chat (client-side)
     * @param message Message to be printed
     */
    @Suppress("unused")
    @JvmStatic
    fun print(message: String) {
        ClientUtils.displayChatMessage(message)
    }

    @Suppress("unused")
    @JvmStatic
    fun alert(message: String) {
        ClientUtils.displayAlert(message)
    }
}