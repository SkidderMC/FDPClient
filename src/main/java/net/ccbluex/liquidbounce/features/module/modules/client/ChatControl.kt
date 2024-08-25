/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.BoolValue

object ChatControl : Module("ChatControl", Category.CLIENT, gameDetecting = false, hideModule = false, subjective = true) {

    init {
        state = true
    }

    val chatLimitValue by BoolValue("NoChatLimit", true)
    val chatClearValue by BoolValue("NoChatClear", true)
    val fontChatValue by BoolValue("FontChat", false)
}