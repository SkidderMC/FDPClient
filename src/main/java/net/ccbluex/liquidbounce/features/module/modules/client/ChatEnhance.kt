package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue

@ModuleInfo(name = "ChatEnhance", category = ModuleCategory.CLIENT, defaultOn = true)
class ChatEnhance : Module() {
    val chatLimitValue = BoolValue("NoChatLimit", true)
    val chatClearValue = BoolValue("NoChatClear", true)
    val chatCombineValue = BoolValue("ChatCombine", true)
    val fontChatValue = BoolValue("FontChat", false)
    val chatRectValue = BoolValue("ChatRect", false)
    val betterChatRectValue = BoolValue("BetterChatRect", false)
    val chatAnimValue = BoolValue("ChatAnimation", false)
}