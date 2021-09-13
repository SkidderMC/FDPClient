/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(name = "FakeDead", category = ModuleCategory.RENDER)
class FakeDead : Module() {
    private val deathtime= IntegerValue("FakeDeathTime",4,0,14)
    private val hurt = BoolValue("FakeGetHurt",false)

    override fun onEnable() {
        if(mc.thePlayer == null) return
        mc.thePlayer.deathTime = deathtime.get()
        if (hurt.get()) mc.thePlayer.hurtTime = 2
    }

    override fun onDisable() {
        if(mc.thePlayer == null) return
        // mc.thePlayer.isDead = false
        mc.thePlayer.deathTime = 0
        if (!hurt.get()) return
        mc.thePlayer.hurtTime = 0
    }

    @EventTarget
    fun onUpdate(event : UpdateEvent) {
        if(mc.thePlayer == null) return
        if(!state) return
        mc.thePlayer.deathTime = deathtime.get()
        if (hurt.get()) mc.thePlayer.hurtTime = 2
    }
}