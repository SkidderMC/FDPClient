/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.ghost

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.utils.CPSCounterUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.MathUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSword
import kotlin.random.Random
import org.lwjgl.input.Mouse

@ModuleInfo(name = "AutoClicker", category = ModuleCategory.GHOST)
class AutoClicker : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Normal", "Gaussian", "LegitJitter", "LegitButterfly"), "Normal")
    private val legitJitterValue = ListValue("LegitJitterMode", arrayOf("Jitter1", "Jitter2", "Jitter3", "SimpleJitter"), "Jitter1").displayable {modeValue.equals("LegitJitter")}
    private val legitButterflyValue = ListValue("LegitButterflyMode", arrayOf("Butterfly1", "Butterfly2"), "Butterfly1").displayable {modeValue.equals("LegitButterfly")}
    private val extraRandomization = FloatValue("ExtraRandomization", 0.4f, 0f, 1f)
    private val exhaustion = IntegerValue("Exhaust", 30, 0, 100)

    private val doubleClickValue = BoolValue("DoubleClick", true)
    private val doubleClickCPSValue = IntegerValue("DCCPSActivation", 5, 0, 10).displayable { doubleClickValue.get() }
    private val doubleClickChanceValue = FloatValue("DoubleClickChance", 0.8f, 0f, 1f).displayable { doubleClickValue.get() }


    // Normal
    private val normalMaxCPSValue: IntegerValue = object : IntegerValue("Normal-MaxCPS", 8, 1, 40) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minCPS = normalMinCPSValue.get()
            if (minCPS > newValue) {
                set(minCPS)
            }
        }
    }
    private val normalMinCPSValue: IntegerValue = object : IntegerValue("Normal-MinCPS", 5, 1, 40)  {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxCPS = normalMaxCPSValue.get()
            if (maxCPS < newValue) {
                set(maxCPS)
            }
        }
    }
    private val rightValue = BoolValue("RightClick", true)
    private val rightBlockOnlyValue = BoolValue("RightBlockOnly", false).displayable { rightValue.get() }
    
    private val leftValue = BoolValue("LeftClick", true)
    
    private val triggerBotValue = BoolValue("TiggerBot", false).displayable { leftValue.get() }
    private val triggerBotRangeValue = FloatValue("TriggerBotRange", 4.5f, 0f, 6f).displayable { triggerBotValue.get() && triggerBotValue.displayable }
    
    private val leftSwordOnlyValue = BoolValue("LeftSwordOnly", false).displayable { leftValue.get() }
    
    private val breakStopValue = BoolValue("BreakingStop", true).displayable { leftValue.get() }
    
    private val blockValue = BoolValue("AutoBlock", false). displayable { leftValue.get() }
    private val blockOnClick = BoolValue("AutoBlockOnRightClick", true). displayable { leftValue.get() && blockValue.get() }
    
    private val blockMode = ListValue("AutoblockMode", arrayOf("Percent", "Click", "Ticks", "Miliseconds"), "Percent"). displayable { leftValue.get() && blockValue.get() }
    
    private val blockPercentStartValue = FloatValue("PercentStart", 0.2f, 0.05f, 1f).displayable { blockMode.displayable && blockMode.equals("Percent") }
    private val blockPercentEndValue = FloatValue("PercentEnd", 0.8f, 0.05f, 1f).displayable { blockMode.displayable && blockMode.equals("Percent") }
    private val blockTicksValue = IntegerValue("BlockTicks", 2, 1, 10).displayable { blockMode.displayable && blockMode.equals("Ticks") }
    private val blockMsValue = IntegerValue("BlockMiliseconds", 80, 1, 1000).displayable { blockMode.displayable && blockMode.equals("Miliseconds") }

    
    private val jitterValue = BoolValue("Jitter", false)
    

    // Gaussian
    private val gaussianCpsValue = IntegerValue("Gaussian-CPS", 5, 1, 40).displayable { modeValue.equals("Gaussian") }
    private val gaussianSigmaValue = FloatValue("Gaussian-Sigma", 0.5F, 0.1F, 5F).displayable { modeValue.equals("Gaussian") }


    private var gaussianClickDelay = 0F

    private var rightDelay = 50L
    private var rightLastSwing = 0L
    private var leftDelay = 50L
    private var leftLastSwing = 0L

    private var delayNum = 0
    private var cDelay = 0

    private var doBlock = false
    private var clickBlocked = false
    private var blockTicks = 0

    private var extraRandomDelay = 0L
    private val extraRandomTimer = MSTimer()
    private var extraRandomAmount = 0L

    private var exhaust = 1f



    @EventTarget
    fun onRender(event: Render3DEvent) {
        val leftAttack = mc.gameSettings.keyBindAttack.isKeyDown || (triggerBotValue.get() && isHovered())
        if (leftAttack && leftValue.get() &&
            System.currentTimeMillis() - leftLastSwing >= leftDelay && (!leftSwordOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemSword) && (!breakStopValue.get() || mc.playerController.curBlockDamageMP == 0F)) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Minecraft Click Handling
            clickBlocked = false
            blockTicks = 0

            if (doubleClickValue.get()) {
                var cps = CPSCounterUtils.getCPS(CPSCounterUtils.MouseButton.LEFT)
                if (cps >= doubleClickCPSValue.get() && doubleClickChanceValue.get() > Random.nextFloat()) {
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)
                }
            }
            leftLastSwing = System.currentTimeMillis()
            leftDelay = updateClicks().toLong()
        }
           
           
        if (mc.gameSettings.keyBindUseItem.isKeyDown && !mc.thePlayer.isUsingItem && rightValue.get() && System.currentTimeMillis() - rightLastSwing >= rightDelay && (!rightBlockOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemBlock) && rightValue.get()) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)
            rightLastSwing = System.currentTimeMillis()
            rightDelay = updateClicks().toLong()
        }
        
        if (blockValue.get() && mc.thePlayer.heldItem?.item is ItemSword && leftValue.get() &&  mc.gameSettings.keyBindAttack.isKeyDown && (!breakStopValue.get() || mc.playerController.curBlockDamageMP == 0F)) {
            mc.gameSettings.keyBindUseItem.pressed = false

            doBlock = when(blockMode.get().lowercase()) {
                "percent" -> (System.currentTimeMillis() - leftLastSwing >= leftDelay * blockPercentStartValue.get().toDouble() && System.currentTimeMillis() - leftLastSwing <= leftDelay * blockPercentEndValue.get().toDouble()) 
                "ticks" -> (blockTicks <= blockTicksValue.get())
                "miliseconds" -> ((System.currentTimeMillis() - leftLastSwing) <= blockMsValue.get().toDouble())
                else -> false
            }

            if ( !blockOnClick.get() || Mouse.isButtonDown(1)) {
                if (blockMode.equals("Click")) {
                    if ( !clickBlocked && System.currentTimeMillis() - leftLastSwing > 1 ) {
                        clickBlocked = true
                        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)
                    }
                } else {
                    mc.gameSettings.keyBindUseItem.pressed = doBlock
                }
            }
        }
            
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        exhaust *= 0.995f
        if (extraRandomTimer.hasTimePassed(extraRandomDelay)) {
            extraRandomDelay = RandomUtils.nextInt(700,2000).toLong()
            extraRandomAmount = (RandomUtils.nextFloat(-0.3f, 0.7f) * extraRandomization.get() * 40f).toLong()
        }

        blockTicks ++
        if (jitterValue.get() && (leftValue.get() && mc.gameSettings.keyBindAttack.isKeyDown || rightValue.get() && mc.gameSettings.keyBindUseItem.isKeyDown && !mc.thePlayer.isUsingItem)) {
            if (Random.nextBoolean()) mc.thePlayer.rotationYaw += if (Random.nextBoolean()) -RandomUtils.nextFloat(0F, 1F) else RandomUtils.nextFloat(0F, 1F)

            if (Random.nextBoolean()) {
                mc.thePlayer.rotationPitch += if (Random.nextBoolean()) -RandomUtils.nextFloat(0F, 1F) else RandomUtils.nextFloat(0F, 1F)

                // Make sure pitch does not go in to blatant values
                if (mc.thePlayer.rotationPitch > 90)
                    mc.thePlayer.rotationPitch = 90F
                else if (mc.thePlayer.rotationPitch < -90)
                    mc.thePlayer.rotationPitch = -90F
            }
         }
    }

    override fun onEnable() {
        if(modeValue.equals("Gaussian")) {
            gaussianUpdateDelay()
        }
    }

    private fun gaussianUpdateDelay(): Float {
        gaussianClickDelay = 1000F / (MathUtils.calculateGaussianDistribution(gaussianCpsValue.get().toFloat(), gaussianSigmaValue.get()).toFloat()
            .coerceAtLeast(1F)) // 1000ms = 1s
        return gaussianClickDelay
    }

    private fun isHovered(): Boolean {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity != null && entity != mc.thePlayer && EntityUtils.isSelected(entity, false)) {
                if (RotationUtils.isFaced(entity, triggerBotRangeValue.get().toDouble())) return true
            }
        }
        return false
    }
    
    private fun updateClicks(): Int {
        
        when (modeValue.get().lowercase()) {
            "normal" -> {
                cDelay = TimeUtils.randomClickDelay(normalMinCPSValue.get(), normalMaxCPSValue.get()).toInt()
            }

            "gaussian" -> {
                gaussianUpdateDelay()
                cDelay = gaussianClickDelay.toInt()
            }

            "legitjitter" -> {
                when (legitJitterValue.get().lowercase()) {
                    "jitter1" -> {
                        if (1 == Random.nextInt(1, 5))
                            delayNum = 0

                        if (delayNum == 0) {
                            if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(98, 110)
                            } else if (Random.nextInt(1, 2) == 1) {
                                cDelay = Random.nextInt(125, 138)
                            } else {
                                cDelay = Random.nextInt(148, 153)
                            }

                            delayNum = 1
                        } else {
                            if (Random.nextInt(1, 4) !== 1) {
                                if (Random.nextBoolean()) {
                                    cDelay = Random.nextInt(65, 69)
                                } else {
                                    if (Random.nextInt(1, 5) == 1) {
                                        cDelay = Random.nextInt(81, 87)
                                    } else {
                                        cDelay = Random.nextInt(97, 101)
                                    }
                                }
                            }
                        }
                    }

                    "jitter2" -> {
                        if (Random.nextInt(1, 14) <= 3) {
                            if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(98, 102)
                            } else {
                                cDelay = Random.nextInt(114, 117)
                            }
                        } else {
                            if (Random.nextInt(1, 4) == 1) {
                                cDelay = Random.nextInt(64, 69)
                            } else {
                                cDelay = Random.nextInt(83, 85)
                            }

                        }
                    }

                    "jitter3" -> {
                        if (Random.nextInt(1, 5) == 1 && delayNum == 0) {
                            delayNum = 1
                            if (Random.nextInt(1, 4) == 1) {
                                cDelay = Random.nextInt(114, 118)
                            } else {
                                cDelay = Random.nextInt(98, 104)
                            }
                        } else {
                            if (delayNum == 1) {
                                delayNum = 0
                                cDelay = Random.nextInt(65, 70)
                            } else {
                                cDelay = Random.nextInt(84, 88)
                            }
                        }
                    }

                    "simplejitter" -> {
                        if (Random.nextInt(1, 5) == 1) {
                            if (Random.nextBoolean()) {
                                cDelay = Random.nextInt(105, 110)
                            } else {
                                cDelay = Random.nextInt(120, 128)
                            }
                        } else {
                            if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(76, 79)
                            } else {
                                if (Random.nextInt(1, 3) == 1) {
                                    cDelay = 78
                                } else {
                                    cDelay = 77
                                }
                            }
                        }
                    }
                }
            }

            "legitbutterfly" -> {
                when (legitButterflyValue.get().lowercase()) {
                    "butterfly1" -> {
                        if (Random.nextInt(1, 7) == 1) {
                            cDelay = Random.nextInt(80, 104)
                        } else {
                            if (Random.nextInt(1, 7) <= 2) {
                                cDelay = 117
                            } else {
                                cDelay = Random.nextInt(114, 119)
                            }
                        }
                    }

                    "butterfly2" -> {
                        if (Random.nextInt(1, 10) == 1) {
                            cDelay = Random.nextInt(225, 250)
                        } else {
                            if (Random.nextInt(1, 6) == 1) {
                                cDelay = Random.nextInt(89, 94)
                            } else if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(95, 103)
                            } else if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(115, 123)
                            } else {
                                if (Random.nextBoolean()) {
                                    cDelay = Random.nextInt(131, 136)
                                } else {
                                    cDelay = Random.nextInt(165, 174)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (exhaust > 1) {
            exhaust = 1f
        }
        exhaust += 0.17f/(100.5f - exhaustion.get().toFloat())
        return (cDelay + extraRandomAmount.toInt()) * (1 + (exhaust * exhaustion.get() / 4).toInt())
    }

    override val tag: String
        get() = when (modeValue.get().lowercase()) {
            "normal" -> normalMinCPSValue.get().toString() + " - " + normalMaxCPSValue.get().toString()
            "legitjitter" -> legitJitterValue.get()
            "legitbutterfly" -> legitButterflyValue.get()
            "gaussian" -> gaussianCpsValue.get().toString()
            else -> modeValue.get()
        }
}
