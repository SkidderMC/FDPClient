/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockVine
import net.minecraft.util.BlockPos

@ModuleInfo(name = "FastLadder", category = ModuleCategory.MOVEMENT)
class AirLadder : Module() {
    private val yMotionValue = FloatValue("YMotion", 0.15f, 0.1f, 0.2f)
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val block = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ))
        if ((block is BlockLadder && mc.thePlayer.isCollidedHorizontally) || (block is BlockVine || BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) is BlockVine)) {
            mc.thePlayer.motionY = yMotionValue.get().toDouble()
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
