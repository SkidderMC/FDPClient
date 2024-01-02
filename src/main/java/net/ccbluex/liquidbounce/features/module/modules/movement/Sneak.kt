/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C0BPacketEntityAction

@ModuleInfo(name = "Sneak", category = ModuleCategory.MOVEMENT)
object Sneak : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "Vanilla2", "Packet", "NCP"), "Vanilla")
    private val onlySneakValue = BoolValue("OnlySneak", false)

    override fun onEnable() {
        if (modeValue.equals("Packet")) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
        }
    }

    override fun onDisable() {
        if (modeValue.equals("Packet")) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }
    } 
    
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if(onlySneakValue.get() && !GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) return

        when(event.eventState) {
            EventState.PRE -> {
                when(modeValue.get().lowercase()) {
                    "vanilla" -> {
                        mc.gameSettings.keyBindSneak.pressed = true
                    }

                    "vanilla2" -> {
                        mc.thePlayer.movementInput.sneak = mc.thePlayer.sendQueue.doneLoadingTerrain
                    }

                    "ncp" -> {
                        mc.thePlayer.movementInput.sneak = mc.thePlayer.sendQueue.doneLoadingTerrain
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
                    }

                    else -> null
                }
            }

            EventState.POST -> {
                when(modeValue.get().lowercase()) {
                    "vanilla2" -> {
                        mc.thePlayer.movementInput.sneak = mc.thePlayer.sendQueue.doneLoadingTerrain
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
                    }

                    "ncp" -> {
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
                    }

                    else -> null
                }
            }
        }
    }
}
