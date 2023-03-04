package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue

@ModuleInfo(name = "ChatEnhance", category = ModuleCategory.MISC, defaultOn = true)
class ChatEnhance : Module() {
    val chatLimitValue = BoolValue("NoChatLimit", false)
    val chatClearValue = BoolValue("NoChatClear", true)
    val chatCombineValue = BoolValue("ChatCombine", true)
}