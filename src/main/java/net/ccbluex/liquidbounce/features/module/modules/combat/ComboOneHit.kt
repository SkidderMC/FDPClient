/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation

@ModuleInfo(name = "ComboOneHit", category = ModuleCategory.COMBAT)
class ComboOneHit : Module() {

    private val amountValue = IntegerValue("Packets", 200, 0, 500)
    private val swingItemValue = BoolValue("SwingPacket", false)
    private val onlyAuraValue = BoolValue("OnlyAura", false)

    @EventTarget
    fun onAttack(event: AttackEvent) {
        event.targetEntity ?: return
        if (onlyAuraValue.get() && !LiquidBounce.moduleManager[KillAura::class.java]!!.state && !LiquidBounce.moduleManager[InfiniteAura::class.java]!!.state) return

        repeat (amountValue.get()) {
            mc.netHandler.addToSendQueue(C0APacketAnimation())
            mc.netHandler.addToSendQueue(C02PacketUseEntity(event.targetEntity, C02PacketUseEntity.Action.ATTACK))
        }
    }

}
