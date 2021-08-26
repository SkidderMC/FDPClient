/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "FastUse", category = ModuleCategory.PLAYER)
class FastUse : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Instant", "Timer", "CustomDelay", "DelayedInstant","MinemoraTest"), "DelayedInstant")
    private val timerValue = FloatValue("Timer", 1.22F, 0.1F, 2.0F).displayable { modeValue.get().equals("Timer",true) }
    private val durationValue = IntegerValue("InstantDelay", 14, 0, 35).displayable { modeValue.get().equals("DelayedInstant",true) }
    private val delayValue = IntegerValue("CustomDelay", 0, 0, 300).displayable { modeValue.get().equals("CustomDelay",true) }

    private val msTimer = MSTimer()
    private var usedTimer = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }

        if (!mc.thePlayer.isUsingItem)
            return

        val usingItem = mc.thePlayer.itemInUse.item

        if (usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion) {
            when (modeValue.get().toLowerCase()) {
                "delayedinstant" -> if (mc.thePlayer.itemInUseDuration > durationValue.get()) {
                    repeat(36-mc.thePlayer.itemInUseDuration) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }

                    mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }

                "instant" -> {
                    repeat(35) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }

                    mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }

                "timer" -> {
                    mc.timer.timerSpeed = timerValue.get()
                    usedTimer = true
                }

                "MinemoraTest" -> {
                    mc.timer.timerSpeed = 0.5F
                    usedTimer = true
                    if(mc.thePlayer.ticksExisted % 2 == 0){
                        repeat(2) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                        }
                    }
                }

                "customdelay" -> {
                    if (!msTimer.hasTimePassed(delayValue.get().toLong()))
                        return

                    mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    msTimer.reset()
                }
            }
        }
    }

    override fun onDisable() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
    }

    override val tag: String
        get() = modeValue.get()
}
