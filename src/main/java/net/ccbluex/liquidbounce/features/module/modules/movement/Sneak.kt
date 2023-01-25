/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.forge.mixins.client.IMixinKeyBinding
import net.minecraft.network.play.client.C0BPacketEntityAction

@ModuleInfo(name = "Sneak", category = ModuleCategory.MOVEMENT)
class Sneak : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "Packet"), "Vanilla")
    private val onlySneakValue = BoolValue("OnlySneak", false)
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(onlySneakValue.get() && !mc.gameSettings.keyBindSneak.pressed) return

        when(modeValue.get().lowercase()) {
            "vanilla" -> {
                mc.gameSettings.keyBindSneak.pressed = true
            }

            "packet" -> {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
            }
        }
    }
}