/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.UpdateEvent
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.FloatValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemArmor
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C16PacketClientStatus

@ModuleInfo(name = "ArmorFilter", category = ModuleCategory.EXPLOIT)
class ArmorFilter : Module() {

    private val percentage = FloatValue("Percentage", 25f, 0f, 100f)
    private val delay = FloatValue("Delay", 100f, 1f, 3000f)
    private val safe = BoolValue("OnKillAura", true)

    private val timer = TimerUtils()


    @EventTarget
    fun handle(event: UpdateEvent?) {
        if (timer.delay(delay.value)) {
            timer.reset()

            val killAura = checkNotNull(
                FDPClient.moduleManager.getModule(
                    KillAura::class.java
                )
            )
            val state = killAura.state && safe.get()

            for (slot in 9..44) {
                val item = mc.thePlayer.inventoryContainer.getSlot(slot).stack

                if (item != null && item.item is ItemArmor) {
                    val durability = (item.maxDamage - item.itemDamage).toFloat()
                    val percentage = durability / item.maxDamage

                    if (percentage < this.percentage.get() / 100.0f) {
                        if (state) {
                            killAura.onToggle(true)
                        }

                        val open = mc.currentScreen !is GuiInventory

                        if (open) {
                            mc.thePlayer.sendQueue.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
                        }

                        mc.playerController.windowClick(
                            mc.thePlayer.inventoryContainer.windowId,
                            slot, 1, 4,
                            mc.thePlayer
                        )

                        if (open) {
                            mc.thePlayer.sendQueue.addToSendQueue(C0DPacketCloseWindow())
                        }

                        if (state) {
                            killAura.onToggle(true)
                        }

                        return
                    }
                }
            }
        }
    }
}