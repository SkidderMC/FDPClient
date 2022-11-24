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
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.math.MathUtils
import net.ccbluex.liquidbounce.utils.timer.Timer
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSword
import kotlin.random.Random

@ModuleInfo(name = "AutoClicker", category = ModuleCategory.COMBAT)
class AutoClicker : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Normal", "Gaussian"), "Normal")

    // Normal
    private val normalMaxCPSValue: IntegerValue = object : IntegerValue("Normal-MaxCPS", 8, 1, 40) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minCPS = normalMinCPSValue.get()
            if (minCPS > newValue) {
                set(minCPS)
            }
        }
    }
    private val normalMinCPSValue: IntegerValue = object : IntegerValue("Normal-MinCPS", 5, 1, 40) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxCPS = normalMaxCPSValue.get()
            if (maxCPS < newValue) {
                set(maxCPS)
            }
        }
    }
    private val normalLegitJitterValue = BoolValue("Normal-LegitJitterCPS", false).displayable { modeValue.equals("Normal") }

    private val normalRightValue = BoolValue("Normal-Right", true).displayable { modeValue.equals("Normal") }
    private val normalRightBlockOnlyValue = BoolValue("Normal-RightBlockOnly", false).displayable { modeValue.equals("Normal") }
    private val normalLeftValue = BoolValue("Normal-Left", true).displayable { modeValue.equals("Normal") }
    private val normalLeftSwordOnlyValue = BoolValue("Normal-LeftSwordOnly", false).displayable { modeValue.equals("Normal") }
    private val normalJitterValue = BoolValue("Normal-Jitter", false).displayable { modeValue.equals("Normal") }

    private var normalRightDelay = TimeUtils.randomClickDelay(normalMinCPSValue.get(), normalMaxCPSValue.get())
    private var normalRightLastSwing = 0L
    private var normalLeftDelay = TimeUtils.randomClickDelay(normalMinCPSValue.get(), normalMaxCPSValue.get())
    private var normalLeftLastSwing = 0L

    // Gaussian
    private val gaussianCpsValue = IntegerValue("Gaussian-CPS", 5, 1, 40).displayable { modeValue.equals("Gaussian") }
    private val gaussianSigmaValue = FloatValue("Gaussian-Sigma", 0.5F, 0.1F, 5F).displayable { modeValue.equals("Gaussian") }

    private val gaussianRightValue = BoolValue("Gaussian-Right", true).displayable { modeValue.equals("Gaussian") }
    private val gaussianRightBlockOnlyValue = BoolValue("Gaussian-RightBlockOnly", false).displayable { modeValue.equals("Gaussian") }
    private val gaussianLeftValue = BoolValue("Gaussian-Left", true).displayable { modeValue.equals("Gaussian") }
    private val gaussianLeftSwordOnlyValue = BoolValue("Gaussian-LeftSwordOnly", false).displayable { modeValue.equals("Gaussian") }
    private val gaussianJitterValue = BoolValue("Gaussian-Jitter", false).displayable { modeValue.equals("Gaussian") }

    private val gaussianTimer = Timer()
    private var gaussianClickDelay = 0F



    @EventTarget
    fun onRender(event: Render3DEvent) {
        // Left click
        when (modeValue.get().lowercase()) {
            "normal" -> {
                if (mc.gameSettings.keyBindAttack.isKeyDown && normalLeftValue.get() &&
                    System.currentTimeMillis() - normalLeftLastSwing >= normalLeftDelay && (!normalLeftSwordOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemSword) && mc.playerController.curBlockDamageMP == 0F) {
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Minecraft Click Handling

                    normalLeftLastSwing = System.currentTimeMillis()
                    if (normalLegitJitterValue.get()) {
                        normalLeftDelay = if (Random.nextInt(1, 14) <= 3) {
                            if (Random.nextInt(1,3) == 2) {
                                (Random.nextInt(98,102)).toLong()
                            } else {
                                (Random.nextInt(114,117)).toLong()
                            }
                        } else {
                            if (Random.nextInt(1,4) == 1) {
                                (Random.nextInt(64,68)).toLong()
                            } else {
                                (Random.nextInt(84,85)).toLong()
                            }
                        }
                    } else {
                        normalLeftDelay = TimeUtils.randomClickDelay(normalMinCPSValue.get(), normalMaxCPSValue.get())
                    }
                }

                // Right click
                if (mc.gameSettings.keyBindUseItem.isKeyDown && !mc.thePlayer.isUsingItem && normalRightValue.get() &&
                    System.currentTimeMillis() - normalRightLastSwing >= normalRightDelay &&
                    (!normalRightBlockOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemBlock) && normalRightValue.get()) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode) // Minecraft Click Handling

                    normalRightLastSwing = System.currentTimeMillis()
                    if (normalLegitJitterValue.get()) {
                        normalRightDelay = if (Random.nextInt(1, 14) <= 3) {
                            if (Random.nextInt(1,3) == 2) {
                                (Random.nextInt(98,102)).toLong()
                            } else {
                                (Random.nextInt(114,117)).toLong()
                            }
                        } else {
                            if (Random.nextInt(1,4) == 1) {
                                (Random.nextInt(64,68)).toLong()
                            } else {
                                (Random.nextInt(84,85)).toLong()
                            }
                        }
                    } else {
                        normalRightDelay = TimeUtils.randomClickDelay(normalMinCPSValue.get(), normalMaxCPSValue.get())
                    }
                }
            }

            "gaussian" -> {
                // Left click
                if (mc.gameSettings.keyBindAttack.isKeyDown &&
                    gaussianLeftValue.get() &&
                    (!gaussianLeftSwordOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemSword) &&
                    mc.playerController.curBlockDamageMP == 0F &&
                    gaussianTimer.check(gaussianClickDelay)
                ) {
                    gaussianUpdateDelay()
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Minecraft Click Handling
                }

                // Right click
                if (mc.gameSettings.keyBindUseItem.isKeyDown &&
                    !mc.thePlayer.isUsingItem &&
                    gaussianRightValue.get() &&
                    (!gaussianRightBlockOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemBlock) &&
                    gaussianTimer.check(gaussianClickDelay)
                ) {
                    gaussianUpdateDelay()
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode) // Minecraft Click Handling
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (modeValue.get().lowercase()) {
            "normal" -> {
                if (normalJitterValue.get() && (normalLeftValue.get() && mc.gameSettings.keyBindAttack.isKeyDown || normalRightValue.get() && mc.gameSettings.keyBindUseItem.isKeyDown && !mc.thePlayer.isUsingItem)) {
                    if (Random.nextBoolean()) mc.thePlayer.rotationYaw += if (Random.nextBoolean()) -RandomUtils.nextFloat(0F, 1F) else RandomUtils.nextFloat(0F, 1F)

                    if (Random.nextBoolean()) {
                        mc.thePlayer.rotationPitch += if (Random.nextBoolean()) -RandomUtils.nextFloat(0F, 1F) else RandomUtils.nextFloat(0F, 1F)

                        // Make sure pitch does not go in to blatent values
                        if (mc.thePlayer.rotationPitch > 90)
                            mc.thePlayer.rotationPitch = 90F
                        else if (mc.thePlayer.rotationPitch < -90)
                            mc.thePlayer.rotationPitch = -90F
                    }
                }
            }

            "gaussian" -> {
                if (gaussianJitterValue.get() && (gaussianLeftValue.get() && mc.gameSettings.keyBindAttack.isKeyDown || gaussianRightValue.get() && mc.gameSettings.keyBindUseItem.isKeyDown && !mc.thePlayer.isUsingItem)) {
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
        }
    }

    override fun onEnable() {
        if(modeValue.get() == "Gaussian") {
            gaussianUpdateDelay()
        }
    }

    private fun gaussianUpdateDelay(): Float {
        gaussianTimer.reset()
        gaussianClickDelay = 1000F / (MathUtils.calculateGaussianDistribution(gaussianCpsValue.get().toFloat(), gaussianSigmaValue.get()).toFloat()
            .coerceAtLeast(1F)) // 1000ms = 1s
        return gaussianClickDelay
    }
}
