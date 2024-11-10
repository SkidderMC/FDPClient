/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.boolean

object ChatControl : Module("ChatControl", Category.CLIENT, gameDetecting = false, hideModule = false, subjective = true) {

    init {
        state = true
    }

    val chatLimitValue by boolean("NoChatLimit", true)
    val chatClearValue by boolean("NoChatClear", true)
    val fontChatValue by boolean("FontChat", false)
}