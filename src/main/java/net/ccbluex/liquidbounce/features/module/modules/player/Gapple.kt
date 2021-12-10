package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.init.Items
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S09PacketHeldItemChange

@ModuleInfo(name = "Gapple", category = ModuleCategory.PLAYER)
class Gapple : Module() {
    val modeValue = ListValue("Mode", arrayOf("Auto", "LegitAuto", "Head"), "Auto")
    // Auto Mode
    private val healthValue = FloatValue("Health", 10F, 1F, 20F)
    private val delayValue = IntegerValue("Delay", 150, 0, 1000)
    private val noAbsorption = BoolValue("NoAbsorption", true)
    private val timer = MSTimer()

    private var eating = -1

    override fun onEnable() {
        eating = -1
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.health > healthValue.get() || !timer.hasTimePassed(delayValue.get().toLong()) || noAbsorption.get() && mc.thePlayer.absorptionAmount> 0) {
            return
        }

        when (modeValue.get().lowercase()) {
            "auto" -> {
                val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
                if (gappleInHotbar != -1) {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(gappleInHotbar - 36))
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                    repeat(35) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    alert("Gapple eaten")
                    timer.reset()
                }
            }
            "legitauto" -> {
                if (eating == -1) {
                    val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
                    if(gappleInHotbar == -1) return
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(gappleInHotbar - 36))
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                    eating = 0
                } else if (eating > 35) {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    timer.reset()
                }
            }
            "head" -> {
                val headInHotbar = InventoryUtils.findItem(36, 45, Items.skull)
                if (headInHotbar != -1) {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(headInHotbar - 36))
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    timer.reset()
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (eating != -1 && packet is C03PacketPlayer) {
            eating++
        } else if (packet is S09PacketHeldItemChange || packet is C09PacketHeldItemChange) {
            eating = -1
        }
    }

    override val tag: String
        get() = modeValue.get()
}