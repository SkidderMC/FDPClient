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
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.ccbluex.liquidbounce.utils.timer.MSTimer

@ModuleInfo(name = "Eagle", category = ModuleCategory.PLAYER)
class Eagle : Module() {
    
    private val motionPredict = FloatValue("PredictionAmount", 0.2f, 0.1f, 1.1f)
    private val holdTime = IntegerValue("HoldSneak", 90, 0, 600)
    
    private val holdTimer = MSTimer()
    
    private var sneakValue = false
    
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX + mc.thePlayer.motionX.toDouble() * motionPredict.toDouble(), mc.thePlayer.posY - 1.0, mc.thePlayer.posZ + mc.thePlayer.motionZ.toDouble() * motionPredict.toDouble())).block == Blocks.air) {
            sneakValue = true
            holdTimer.reset()
        } else if (holdTimer.hasTimePassed(holdTime.get().toLong()) {
            sneakValue = false
        }

        mc.gameSettings.keyBindSneak.pressed = sneakValue
    }
    
    override fun onDisable() {
        sneakValue = false
        holdTimer.reset()
    }

    override fun onDisable() {
        if (mc.thePlayer == null) {
            return
        }

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
        }
    }
}
