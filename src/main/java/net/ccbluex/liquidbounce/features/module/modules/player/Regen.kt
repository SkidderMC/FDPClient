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
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.Potion

@ModuleInfo(name = "Regen", category = ModuleCategory.PLAYER)
class Regen : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "OldSpartan", "NewSpartan", "AAC4NoFire"), "Vanilla")
    private val healthValue = IntegerValue("Health", 18, 0, 20)
    private val delayValue = IntegerValue("Delay", 0, 0, 1000)
    private val foodValue = IntegerValue("Food", 18, 0, 20)
    private val speedValue = IntegerValue("Speed", 100, 1, 100)
    private val noAirValue = BoolValue("NoAir", false)
    private val potionEffectValue = BoolValue("PotionEffect", false)

    private val timer = MSTimer()
    private var resetTimer = false

    override fun onEnable() {
        timer.reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (resetTimer) {
            mc.timer.timerSpeed = 1F
            resetTimer = false
        }

        if ((!noAirValue.get() || mc.thePlayer.onGround) &&
            !mc.thePlayer.capabilities.isCreativeMode &&
            mc.thePlayer.foodStats.foodLevel > foodValue.get() &&
            mc.thePlayer.isEntityAlive &&
            mc.thePlayer.health < healthValue.get()
            ) {
            if (potionEffectValue.get() && !mc.thePlayer.isPotionActive(Potion.regeneration)) {
                return
            }

            if(!(timer.hasTimePassed(delayValue.get().toLong()))) {
                return
            }

            when (modeValue.get().lowercase()) {
                "vanilla" -> {
                    repeat(speedValue.get()) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }
                }

                "aac4nofire" -> {
                    if (mc.thePlayer.isBurning && mc.thePlayer.ticksExisted % 10 == 0) {
                        repeat(35) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                        }
                    }
                }

                "newspartan" -> {
                    if (mc.thePlayer.ticksExisted % 5 == 0) {
                        resetTimer = true
                        mc.timer.timerSpeed = 0.98F
                        repeat(10) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                        }
                    } else {
                        if (MovementUtils.isMoving()) mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }
                }

                "oldspartan" -> {
                    if (MovementUtils.isMoving() || !mc.thePlayer.onGround) {
                        return
                    }

                    repeat(9) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }

                    mc.timer.timerSpeed = 0.45F
                    resetTimer = true
                }
            }
            
            timer.reset()
        }
    }
    override val tag: String
        get() = modeValue.get()
}
