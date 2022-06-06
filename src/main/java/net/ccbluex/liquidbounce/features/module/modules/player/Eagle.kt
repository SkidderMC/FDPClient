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
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

@ModuleInfo(name = "Eagle", category = ModuleCategory.PLAYER)
class Eagle : Module() {
    
    private val onlyVoidValue = BoolValue("OnlyPredictVoid", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (onlyVoidValue.get()){
            if (!checkVoid.get()){
                mc.gameSettings.keyBindSneak.pressed =
                        mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air
            }
        } else {
            mc.gameSettings.keyBindSneak.pressed =
                        mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air
        }
    }

    override fun onDisable() {
        if (mc.thePlayer == null) {
            return
        }

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
        }
    }
    private fun checkVoid(): Boolean {
        var i = (-(mc.thePlayer.posY-1.4857625)).toInt()
        var dangerous = true
		while (i <= 0) {
			dangerous = mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(mc.thePlayer.motionX * 1.4, i.toDouble(), mc.thePlayer.motionZ * 1.4)).isEmpty()
			i++
			if (!dangerous) break
		}
        return dangerous
    }
}
