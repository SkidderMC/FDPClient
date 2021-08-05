/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class CustomSpeed : SpeedMode("Custom") {
    private var groundTick=0

    override fun onMotion() {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)

        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = if(mc.thePlayer.motionY>0){ speed.customUpTimerValue.get() } else { speed.customDownTimerValue.get() }

            when {
                mc.thePlayer.onGround -> {
                    if(groundTick>=speed.customGroundStay.get()){
                        if(speed.launchSpeedValue.get())
                            MovementUtils.strafe(speed.customLaunchSpeedValue.get())
                        mc.thePlayer.motionY = speed.customYValue.get().toDouble()
                    }else if(speed.groundResetXZValue.get()){
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                    }
                    groundTick++
                }
                else -> {
                    groundTick=0
                    when(speed.customStrafeValue.get().toLowerCase()){
                        "strafe" -> MovementUtils.strafe(speed.customSpeedValue.get())
                        "boost" -> MovementUtils.strafe()
                        "plus" -> MovementUtils.move(speed.customSpeedValue.get()*0.1f)
                        "plusonlyup" -> if(mc.thePlayer.motionY>0){
                            MovementUtils.move(speed.customSpeedValue.get()*0.1f)
                        }else{
                            MovementUtils.strafe()
                        }
                    }
                    mc.thePlayer.motionY += speed.customAddYMotionValue.get() * 0.03
                }
            }
        } else if (speed.resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onEnable() {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)
        if (speed.resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        if (speed.resetYValue.get()) mc.thePlayer.motionY = 0.0
        super.onEnable()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }
}