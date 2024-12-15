/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.loopHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.block
import net.minecraft.init.Blocks.air

object AutoBreak : Module("AutoBreak", Category.PLAYER, subjective = true, gameDetecting = false) {

    val onUpdate = loopHandler {
        if (mc.objectMouseOver == null || mc.objectMouseOver.blockPos == null || mc.theWorld == null)
            return@loopHandler

        mc.gameSettings.keyBindAttack.pressed = mc.objectMouseOver.blockPos.block != air
    }

    override fun onDisable() {
        if (!mc.gameSettings.keyBindAttack.pressed)
            mc.gameSettings.keyBindAttack.pressed = false
    }
}
