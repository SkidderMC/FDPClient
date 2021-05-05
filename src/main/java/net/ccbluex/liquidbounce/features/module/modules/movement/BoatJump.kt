package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.FloatValue
import net.ccbluex.liquidbounce.features.IntegerValue
import net.minecraft.entity.item.EntityBoat
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "BoatJump", description = "Insane jump xdddd.", category = ModuleCategory.MOVEMENT)
class BoatJump : Module() {
    private val hBoost= FloatValue("HBoost",3.0F,0F,6.0F)
    private val vBoost= FloatValue("VBoost",3.0F,0F,6.0F)
    private val delay= IntegerValue("Delay",200,100,500)
    private val autoHit= BoolValue("AutoHit",true)

    private var jumpState=1
    private val timer=MSTimer()
    private val hitTimer=MSTimer()
    private var lastRide=false

    override fun onEnable() {
        jumpState=1
        lastRide=false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(mc.thePlayer.isRiding&&jumpState==1){
            if(!lastRide){
                timer.reset()
            }
            if(timer.hasTimePassed(delay.get().toLong())){
                jumpState=2
                mc.gameSettings.keyBindSneak.pressed=true
            }
        }else if(jumpState==2&&!mc.thePlayer.isRiding){
            mc.gameSettings.keyBindSneak.pressed=false
            val radiansYaw=mc.thePlayer.rotationYaw * Math.PI / 180

            mc.thePlayer.motionX = hBoost.get() * -sin(radiansYaw)
            mc.thePlayer.motionZ = hBoost.get() * cos(radiansYaw)
            mc.thePlayer.motionY = vBoost.get().toDouble()
            jumpState=1
            timer.reset()
            hitTimer.reset()
        }

        lastRide=mc.thePlayer.isRiding

        if(autoHit.get()&&!mc.thePlayer.isRiding&&hitTimer.hasTimePassed(1500)){
            for(entity in mc.theWorld.loadedEntityList){
                if(entity is EntityBoat&&mc.thePlayer.getDistanceToEntity(entity)<3){
                    mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, Vec3(0.5,0.5,0.5)))
                    mc.netHandler.addToSendQueue(C02PacketUseEntity(entity,C02PacketUseEntity.Action.INTERACT))
                    hitTimer.reset()
                }
            }
        }
    }
}