package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos

@ModuleInfo(name = "AntiVoid", category = ModuleCategory.PLAYER)
class AntiVoid : Module() {
    private val moveValue=ListValue("Move", arrayOf("Blink","TPBack","FlyFlag","GroundSpoof"),"Blink")
    private val fallModeValue=ListValue("FallCheckMove", arrayOf("GroundDist","PredictFall","FallDist"),"FallDist")
    private val maxFallDistValue=FloatValue("MaxFallDistance",10F,5F,20F)
    private val resetMotion=BoolValue("ResetMotion",false)
    private val startFallDistValue=FloatValue("BlinkStartFallDistance",2F,0F,5F)
    private val autoScaffold=BoolValue("BlinkAutoScaffold",true)

    private val packetCache=ArrayList<C03PacketPlayer>()
    private var blink=false
    private var canBlink=false
    private var canSpoof=false

    private var posX=0.0
    private var posY=0.0
    private var posZ=0.0
    private var motionX=0.0
    private var motionY=0.0
    private var motionZ=0.0

    private fun willVoid(distance: Float):Boolean {
        if(mc.thePlayer.onGround)
            return false

        return when(fallModeValue.get().toLowerCase()){
            "grounddist" -> {
                val collide = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, 0.0, 0.0, 0.0, 0F, 0F, 0F).findCollision(60)
                return collide==null||(mc.thePlayer.posY-collide.y)>distance
            }

            "predictfall" -> {
                val collide = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ, mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward).findCollision(60)
                return collide==null||(mc.thePlayer.posY-collide.y)>distance
            }

            "falldist" -> {
                mc.thePlayer.fallDistance>distance && mc.thePlayer.motionY<0
            }

            else -> false
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        when(moveValue.get().toLowerCase()){
            "groundspoof" -> {
                canSpoof = willVoid(maxFallDistValue.get())
            }

            "flyflag" -> {
                if(willVoid(maxFallDistValue.get())){
                    mc.thePlayer.motionY += 0.1
                    mc.thePlayer.fallDistance = 0F
                }
            }

            "tpback" -> {
                if (mc.thePlayer.onGround && BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) !is BlockAir) {
                    posX = mc.thePlayer.prevPosX
                    posY = mc.thePlayer.prevPosY
                    posZ = mc.thePlayer.prevPosZ
                }

                if(willVoid(maxFallDistValue.get())){
                    mc.thePlayer.setPositionAndUpdate(posX, posY, posZ)
                    mc.thePlayer.fallDistance = 0F
                    mc.thePlayer.motionY = 0.0
                    if(resetMotion.get()){
                        mc.thePlayer.motionX=0.0
                        mc.thePlayer.motionY=0.0
                        mc.thePlayer.motionZ=0.0
                    }
                }
            }

            "blink" -> {
                if(!blink){
                    if(canBlink && willVoid(startFallDistValue.get())){
                        posX=mc.thePlayer.posX
                        posY=mc.thePlayer.posY
                        posZ=mc.thePlayer.posZ
                        motionX=mc.thePlayer.motionX
                        motionY=mc.thePlayer.motionY
                        motionZ=mc.thePlayer.motionZ

                        packetCache.clear()
                        blink=true
                    }

                    if(mc.thePlayer.onGround){
                        canBlink=true
                    }
                }else{
                    if(mc.thePlayer.fallDistance>maxFallDistValue.get()){
                        mc.thePlayer.setPositionAndUpdate(posX,posY,posZ)
                        if(resetMotion.get()){
                            mc.thePlayer.motionX=0.0
                            mc.thePlayer.motionY=0.0
                            mc.thePlayer.motionZ=0.0
                        }else{
                            mc.thePlayer.motionX=motionX
                            mc.thePlayer.motionY=motionY
                            mc.thePlayer.motionZ=motionZ
                        }

                        if(autoScaffold.get()){
                            LiquidBounce.moduleManager.getModule(Scaffold::class.java)?.state=true
                        }

                        packetCache.clear()
                        blink=false
                        canBlink=false
                    }else if(mc.thePlayer.onGround){
                        blink=false

                        for(packet in packetCache){
                            mc.netHandler.addToSendQueue(packet)
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet=event.packet

        when(moveValue.get().toLowerCase()){
            "blink" -> {
                if(blink && (packet is C03PacketPlayer)){
                    packetCache.add(packet)
                    event.cancelEvent()
                }
            }

            "groundspoof" -> {
                if(canSpoof&&(packet is C03PacketPlayer)){
                    packet.onGround=true
                }
            }
        }
    }
}