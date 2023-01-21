/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.ccbluex.liquidbounce.utils.timer.MSTimer

@ModuleInfo(name = "Eagle", category = ModuleCategory.PLAYER)
class Eagle : Module() {
    
    private val motionPredictValue = FloatValue("MotionPredictAmount", 0.2f, 0.0f, 2.0f)
    private val limitTimeValue = BoolValue("SneakTimeLimit", false)
    private val holdTime = IntegerValue("MaxSneakTime", 120, 0, 900)
    
    private val holdTimer = MSTimer()
    
    private var sneakValue = false
    
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX + mc.thePlayer.motionX.toDouble() * motionPredictValue.get().toDouble(), mc.thePlayer.posY - 1.0, mc.thePlayer.posZ + mc.thePlayer.motionZ.toDouble() * motionPredictValue.get().toDouble())).block == Blocks.air) {
            sneakValue = true
            holdTimer.reset()
        } else if (holdTimer.hasTimePassed(holdTime.get().toLong()) && limitTimeValue.get()) {
            sneakValue = false
        } else if (!limitTimeValue.get()) {
            sneakValue = false
        }

        mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || sneakValue)
    }
    
    override fun onEnable() {
        sneakValue = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
        holdTimer.reset()
    }

    override fun onDisable() {
        mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
    }
}
