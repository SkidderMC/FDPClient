/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.BlinkUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import kotlin.random.Random

@ModuleInfo(name = "DynamicLag", category = ModuleCategory.COMBAT)
object DynamicLag : Module() {        BlinkUtils.setBlinkState(all = true)

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

    override fun onUpdate(event: UpdateEvent) {
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
               closestEntity = mc.thePlayer.getDistanceToEntityBox(it)
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
              delayLength = 1000L + Random.nextInt(0, 302).toLong()
            }
            else -> {
              durationLength = 1L
              delayLength = 1L
              //   place holder code, must fix later
            }
          }
        }
    }
}
