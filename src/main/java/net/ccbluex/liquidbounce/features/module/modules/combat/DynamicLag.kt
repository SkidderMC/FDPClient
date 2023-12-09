/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.BlinkUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import kotlin.random.Random

@ModuleInfo(name = "DynamicLag", category = ModuleCategory.COMBAT)
object DynamicLag : Module() {       

    private val lagDelay = MSTimer()
    private val lagDuration = MSTimer()

    private var delayLength = 0L
    private var durationLength = 0L

    private var currentState = 0
    private var closestEntity = 0f

    private var resetTimers = false
    

    override fun onEnable() {
        if(!FDPClient.moduleManager[Blink::class.java]!!.state) {
            BlinkUtils.clearPacket()
        }
        lagDuration.reset()
        lagDelay.reset()
        resetTimers = true
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        BlinkUtils.setBlinkState(off = true, release = true)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(FDPClient.moduleManager[Blink::class.java]!!.state) {
            lagDelay.reset()
            lagDuration.reset()
            return
        }
        if (mc.thePlayer.isDead) {
            BlinkUtils.setBlinkState(off = true, release = true)
            return
        }
        closestEntity = 1000f
      
        for (entity in mc.theWorld.loadedEntityList) {
          val it = entity as EntityLivingBase
           if (mc.thePlayer.getDistanceToEntityBox(it) < closestEntity) {
               closestEntity = mc.thePlayer.getDistanceToEntityBox(it).toFloat()
           }
        }

        val prevState = currentState
        if (closestEntity > 30f) {
            currentState = 1
        } else if (closestEntity > 6f) {
            currentState = 2
        } else if (closestEntity > 4f) {
            currentState = 3
        } else if (closestEntity > 2.8f) {
            currentState = 4
        } else {
            currentState = 5
        }

        if (prevState != currentState) {
           lagDelay.reset()
           lagDuration.reset()
           resetTimers = true
        }

        if (lagDelay.hasTimePassed(durationLength + delayLength)) {
            lagDelay.reset()
            lagDuration.reset()
            resetTimers = true
            BlinkUtils.setBlinkState(all = true)
        }

        if (lagDuration.hasTimePassed(durationLength)) {
            BlinkUtils.setBlinkState(off = true, release = true)
        }

        if (resetTimers) {
          when (currentState) {
            1 -> {
                durationLength = 300L + Random.nextInt(0, 153).toLong()
                delayLength = 1500L + Random.nextInt(0, 500).toLong()
            }
            2 -> {
                durationLength = 320L + Random.nextInt(0, 83).toLong()
                delayLength = 1000L + Random.nextInt(0, 120).toLong()
            }
            3 -> {
                durationLength = 300L + Random.nextInt(0, 60).toLong()
                delayLength = 250L + Random.nextInt(0, 45).toLong()
            }
            4 -> {
                durationLength = 750L + Random.nextInt(0, 120).toLong()
                delayLength = 120L + Random.nextInt(0, 40).toLong()
            }
            5 -> {
                durationLength = 200L + Random.nextInt(0, 100).toLong()
                delayLength = 100L + Random.nextInt(0, 30).toLong()
            }
            else -> {
              durationLength = 1L
              delayLength = 1L
              // shouldnt happen
            }
          }
        }
    }
}
