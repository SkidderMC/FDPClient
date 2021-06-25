/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.stats.StatList
import net.ccbluex.liquidbounce.utils.MovementUtils

@ModuleInfo(name = "Criticals", description = "Automatically deals critical hits.", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    val modeValue = ListValue("Mode", arrayOf("Packet", "NoGround", "Visual", "RedeSkyLowHop", "Hop", "TPHop", "FakeCollide", "TPCollide", "Jump", "LowJump"), "packet")
    val noGroundValue = ListValue("NoGroundOffset", arrayOf("Normal", "RedeskySmart"), "Normal")
    val packetValue = ListValue("PacketOffset", arrayOf("Normal", "NCP", "AAC", "Phase"), "Normal")
    val delayValue = IntegerValue("Delay", 0, 0, 500)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val lookValue = BoolValue("UseC06Packet", false)
    private val debugValue = BoolValue("DebugMessage", false)
    private val NofallValue = BoolValue("NoGroundNofall",true)

    val msTimer = MSTimer()

    private var target = 0
    private var MotionX = mc.thePlayer.motionX
    private var MotionZ = mc.thePlayer.motionZ
    
    override fun onEnable() {
        if (modeValue.get().equals("NoGround", ignoreCase = true) && mc.thePlayer.onGround)
            mc.thePlayer.jump()
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity
            target = entity.entityId

            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                    mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                    LiquidBounce.moduleManager[Fly::class.java]!!.state || !msTimer.hasTimePassed(delayValue.get().toLong()))
                return

            val x = mc.thePlayer.posX
            val y = mc.thePlayer.posY
            val z = mc.thePlayer.posZ
            val yaw = mc.thePlayer.rotationYaw
            val pitch = mc.thePlayer.rotationPitch
            if(MovementUtils.isMoving()) {
                MotionX = mc.thePlayer.motionX
                MotionZ = mc.thePlayer.motionZ
            }else{
                MotionX = 0.00
                MotionZ = 0.00
            }
            when (modeValue.get().toLowerCase()) {
                "packet" -> {
                    when (packetValue.get().toLowerCase()) {
                        "normal" -> {
                            if(lookValue.get()){
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0625, z, yaw, pitch, true))
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, false))
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 1.1E-5, z, yaw, pitch, false))
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, false))
                            }else{
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0625, z, true))
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1.1E-5, z, false))
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                            }
                            mc.thePlayer.onCriticalHit(entity)
                        }
                        
                        "ncp" -> {
                            if(lookValue.get()){
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.11, z, yaw, pitch, false))
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.1100013579, z, yaw, pitch, false))
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0000013579, z, yaw, pitch, false))
                            }else{
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.11, z, false))
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.1100013579, z, false))
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0000013579, z, false))
                            }
                            mc.thePlayer.onCriticalHit(entity)
                        }

                        "aac" -> {
                            if(lookValue.get()){
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.05250000001304, z, yaw, pitch, true))
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.00150000001304, z, yaw, pitch, false))
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.01400000001304, z, yaw, pitch, false))
                                mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.00150000001304, z, yaw, pitch, false))
                            }else{
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.05250000001304,z, true))
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01400000001304, z, false))
                                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                            }
                            mc.thePlayer.onCriticalHit(entity)
                        }
                    }
                }

                "hop" -> {
                    mc.thePlayer.motionY = 0.1
                    mc.thePlayer.fallDistance = 0.1f
                    mc.thePlayer.onGround = false
                }
                
                "fakecollide" -> {
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    mc.thePlayer.motionX *= 0.94
                    mc.thePlayer.motionZ *= 0.94
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x+(MotionX/3), y + 0.20, z+(MotionZ/3), yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x+(MotionX/1.5), y + 0.121600000013, z+(MotionZ/1.5), yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x+(MotionX/3), y + 0.20, z+(MotionZ/3), false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x+(MotionX/1.5), y + 0.121600000013, z+(MotionZ/1.5), false))
                    }
                    //mc.thePlayer.setPosition(x, y + 0.12160000001304075, z)
                    mc.thePlayer.onCriticalHit(entity)
                }
                
                "tpcollide" -> {
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.setPosition(x, y + 0.2, z)
                    mc.thePlayer.onGround = false
                }

                "tphop" -> {
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.02, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.01, z, yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.02, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01, z, false))
                    }
                    mc.thePlayer.setPosition(x, y + 0.01, z)
                }
                
                "visual" -> mc.thePlayer.onCriticalHit(entity)
                "jump" -> mc.thePlayer.motionY = 0.42
                "lowjump" -> mc.thePlayer.motionY = 0.3425
                "redeskylowhop" -> mc.thePlayer.motionY = 0.35
            }
            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer){
            when (modeValue.get().toLowerCase()) {
                "noground" -> packet.onGround = false
                "redeskysmartground" -> {
                    if(rsNofallValue.get()&&mc.thePlayer.fallDistance>0){
                        packet.onGround=true
                        return
                    }

                    if(mc.thePlayer.onGround && LiquidBounce.combatManager.inCombat && (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook)){
                        packet.onGround=false
                    }
                }
            }
        }
        if(packet is S0BPacketAnimation &&debugValue.get()){
            if(packet.animationType==4&&packet.entityID==target){
                chat("CRIT")
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
