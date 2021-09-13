package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.item.EntityBoat
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0CPacketInput
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "BoatJump", category = ModuleCategory.MOVEMENT)
class BoatJump : Module() {
    private val mode=ListValue("Mode", arrayOf("Boost","Launch"),"Boost")
    private val hBoost=FloatValue("HBoost",3F,0F,6F)
    private val vBoost=FloatValue("VBoost",3F,0F,6F)
    private val launchRadius=FloatValue("LaunchRadius",4F,3F,10F).displayable { mode.equals("Launch") }
    private val delay=IntegerValue("Delay",200,100,500)
    private val autoHit=BoolValue("AutoHit",true)

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
                mc.netHandler.addToSendQueue(C0CPacketInput(mc.thePlayer.moveStrafing,mc.thePlayer.moveForward,false,true))
            }
        }else if(jumpState==2&&!mc.thePlayer.isRiding){
            val radiansYaw=mc.thePlayer.rotationYaw * Math.PI / 180

            when(mode.get().toLowerCase()){
                "boost" -> {
                    mc.thePlayer.motionX = hBoost.get() * -sin(radiansYaw)
                    mc.thePlayer.motionZ = hBoost.get() * cos(radiansYaw)
                    mc.thePlayer.motionY = vBoost.get().toDouble()
                    jumpState=1
                }

                "launch" -> {
                    mc.thePlayer.motionX += (hBoost.get()*0.1) * -sin(radiansYaw)
                    mc.thePlayer.motionZ += (hBoost.get()*0.1) * cos(radiansYaw)
                    mc.thePlayer.motionY += vBoost.get()*0.1

                    var hasBoat=false
                    for(entity in mc.theWorld.loadedEntityList){
                        if(entity is EntityBoat&&mc.thePlayer.getDistanceToEntity(entity)<launchRadius.get()){
                            hasBoat=true
                            break
                        }
                    }
                    if(!hasBoat)
                        jumpState=1
                }
            }

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