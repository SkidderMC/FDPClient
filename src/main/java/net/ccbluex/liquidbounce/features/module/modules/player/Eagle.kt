/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.loopHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks.air
import net.minecraft.util.BlockPos

object Eagle : Module("Eagle", Category.PLAYER) {

    private val maxSneakTime by intRange("MaxSneakTime", 1..5, 0..20)
    private val onlyWhenLookingDown by boolean("OnlyWhenLookingDown", false)
    private val lookDownThreshold by float("LookDownThreshold", 45f, 0f..90f) { onlyWhenLookingDown }

    private val sneakTimer = TickTimer()

    val onUpdate = loopHandler {
        val thePlayer = mc.thePlayer ?: return@loopHandler

        if (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) return@loopHandler

        if (thePlayer.onGround && BlockPos(thePlayer).down().block == air) {
            val shouldSneak = !onlyWhenLookingDown || thePlayer.rotationPitch >= lookDownThreshold

            mc.gameSettings.keyBindSneak.pressed = shouldSneak && !GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
        } else {
            if (sneakTimer.hasTimePassed(maxSneakTime.random())) {
                mc.gameSettings.keyBindSneak.pressed = false
                sneakTimer.reset()
            } else sneakTimer.update()
        }
    }

    override fun onDisable() {
        sneakTimer.reset()

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
            mc.gameSettings.keyBindSneak.pressed = false
    }
}