/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.api

import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER

fun reloadMessageOfTheDay() {
    try {
        messageOfTheDay = ClientApi.getMessageOfTheDay()
    } catch (e: Exception) {
        LOGGER.error("Unable to receive message of the day", e)
    }
}

var messageOfTheDay: MessageOfTheDay? = null
    private set