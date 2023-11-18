/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.player.EntityPlayer

@ModuleInfo(name = "KeepRange", category = ModuleCategory.COMBAT)
object KeepRange : Module() {

    private val mode = ListValue("Mode", arrayOf("ReleaseKey", "CancelMove"), "ReleaseKey")
    private val minDistance = FloatValue("MinDistance", 2.3F, 0F, 4F)
    private val maxDistance = FloatValue("MaxDistance", 4.0F, 3F, 7F)
    private val onlyForward = BoolValue("OnlyForward", true)
    private val onlyCombo = BoolValue("OnlyCombat", true)

    private val keepTick = IntegerValue("KeepTick", 10, 0, 40)
    private val restTick = IntegerValue("RestTick", 4, 0, 40)
    
    private var comboing = false
    private val ticks = TickTimer()
    var target: EntityPlayer? = null
    private val binds = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindLeft
    )

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (mc.thePlayer.hurtTime < 1) comboing = true
        target = if (event.targetEntity is EntityPlayer) event.targetEntity else return
    }
    @EventTarget fun onStrafe(event: StrafeEvent) {
        if (onlyCombo.get() && !comboing) return
        if (mode.equals("CancelMove")) {
            target?.let {
                if (mc.thePlayer.getDistanceToEntityBox(it) <= minDistance.get() && !ticks.hasTimePassed(keepTick.get())) {
                    if (!onlyForward.get() || event.forward > 0F) {
                        event.cancelEvent()
                    }
                }
            }
        }
    }
    @EventTarget fun onUpdate(event: UpdateEvent) {
        if (target == null) return
        if (onlyCombo.get() && !comboing) return
        if (ticks.hasTimePassed(keepTick.get() + restTick.get())) ticks.reset()
        ticks.update()
        val distance = mc.thePlayer.getDistanceToEntityBox(target!!)
        if (target!!.isDead || distance >= maxDistance.get()) {
            target = null
            for (bind in binds) bind.pressed = GameSettings.isKeyDown(bind)
            return
        }
        if (mode.equals("ReleaseKey")) {
            if (distance <= minDistance.get() && !ticks.hasTimePassed(keepTick.get())) {
                if (onlyForward.get()) mc.gameSettings.keyBindForward.pressed = false
                else for (bind in binds) bind.pressed = false
            } else {
                for (bind in binds) bind.pressed = GameSettings.isKeyDown(bind)
            }
        }
    }
}
