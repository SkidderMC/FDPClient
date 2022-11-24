/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.math.MathUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.Timer
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSword
import kotlin.random.Random

@ModuleInfo(name = "GaussianClicker", category = ModuleCategory.COMBAT) // The BEST AC
class GaussianClicker : Module() {

    private val cpsValue = IntegerValue("CPS", 5, 1, 40)
    private val sigmaValue = FloatValue("Sigma", 0.5F, 0.1F, 5F)

    private val rightValue = BoolValue("Right", true)
    private val rightBlockOnlyValue = BoolValue("RightBlockOnly", false)
    private val leftValue = BoolValue("Left", true)
    private val leftSwordOnlyValue = BoolValue("LeftSwordOnly", false)
    private val jitterValue = BoolValue("Jitter", false)

    private val timer = Timer()
    private var clickDelay = 0F

    @EventTarget
    fun onRender(event: Render3DEvent) {
        // Left click
        if (mc.gameSettings.keyBindAttack.isKeyDown &&
            leftValue.get() &&
            (!leftSwordOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemSword) &&
            mc.playerController.curBlockDamageMP == 0F &&
            timer.check(clickDelay)
        ) {
            updateDelay()
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Minecraft Click Handling
        }

        // Right click
        if (mc.gameSettings.keyBindUseItem.isKeyDown &&
            !mc.thePlayer.isUsingItem &&
            rightValue.get() &&
            (!rightBlockOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemBlock) &&
            timer.check(clickDelay)
        ) {
            updateDelay()
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode) // Minecraft Click Handling
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (jitterValue.get() && (leftValue.get() && mc.gameSettings.keyBindAttack.isKeyDown || rightValue.get() && mc.gameSettings.keyBindUseItem.isKeyDown && !mc.thePlayer.isUsingItem)) {
            if (Random.nextBoolean()) mc.thePlayer.rotationYaw += if (Random.nextBoolean()) -RandomUtils.nextFloat(0F,
                1F) else RandomUtils.nextFloat(0F, 1F)

            if (Random.nextBoolean()) {
                mc.thePlayer.rotationPitch += if (Random.nextBoolean()) -RandomUtils.nextFloat(0F,
                    1F) else RandomUtils.nextFloat(0F, 1F)

                // Make sure pitch does not go in to blatant values
                if (mc.thePlayer.rotationPitch > 90)
                    mc.thePlayer.rotationPitch = 90F
                else if (mc.thePlayer.rotationPitch < -90)
                    mc.thePlayer.rotationPitch = -90F
            }
        }
    }

    override fun onEnable() {
        timer.reset()
        updateDelay()
    }

    private fun updateDelay(): Float {
        clickDelay = MathUtils.calculateGaussianDistribution(1F / cpsValue.get().toFloat(), sigmaValue.get()).toFloat()
            .coerceAtLeast(0F)
        return clickDelay
    }
}
