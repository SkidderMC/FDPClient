/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "ChatControl", category = ModuleCategory.CLIENT, defaultOn = true)
object ChatControl : Module() {

    val chatLimitValue = BoolValue("NoChatLimit", true)
    val chatClearValue = BoolValue("NoChatClear", true)
    val chatCombineValue = BoolValue("ChatCombine", true)
    val fontChatValue = BoolValue("FontChat", false)
    private val fontEpsilonValue = FloatValue("FontVectorEpsilon", 0.5f, 0f, 1.5f)
    val chatRectValue = BoolValue("ChatBackGround", false)
    val betterChatRectValue = BoolValue("BetterChatRect", false)
    val chatAnimValue = BoolValue("ChatAnimation", false)

    private var lastFontEpsilon = 0f
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        FDPClient.hud.update()
        if (mc.currentScreen == null && lastFontEpsilon != fontEpsilonValue.get()) {
            lastFontEpsilon = fontEpsilonValue.get()
            alert("You need to reload FDPClient to apply changes!")
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        lastFontEpsilon = fontEpsilonValue.get()
    }

}