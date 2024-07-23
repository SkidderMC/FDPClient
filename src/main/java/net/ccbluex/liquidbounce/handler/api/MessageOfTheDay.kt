/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.api

import net.ccbluex.liquidbounce.handler.api.ClientApi.requestMessageOfTheDayEndpoint
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER

val messageOfTheDay by lazy {
    try {
        requestMessageOfTheDayEndpoint()
    } catch (e: Exception) {
        LOGGER.error("Unable to receive message of the day", e)
        return@lazy null
    }
}
