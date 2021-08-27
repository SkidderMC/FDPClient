/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
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
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.stats.StatList

@ModuleInfo(name = "Criticals", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    val modeValue = ListValue("Mode", arrayOf("Packet", "NCPPacket", "Hypixel", "Hypixel2","AACPacket", "AAC4.3.11OldHYT", "NoGround", "Visual", "TPHop", "FakeCollide", "Mineplex", "More", "TestMinemora", "Motion", "Hover"), "packet")
    val motionValue = ListValue("MotionMode", arrayOf("RedeSkyLowHop", "Hop", "Jump", "LowJump"), "Jump")
    val hoverValue = ListValue("HoverMode", arrayOf("AAC4", "AAC4Other", "OldRedesky", "Normal1", "Normal2", "Minis", "Minis2", "TPCollide", "2b2t"), "AAC4")
    val hoverNoFall = BoolValue("HoverNoFall",true)
    val hoverCombat = BoolValue("HoverOnlyCombat",true)
    val delayValue = IntegerValue("Delay", 0, 0, 500)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val lookValue = BoolValue("UseC06Packet", false)
    private val debugValue = BoolValue("DebugMessage", false)
    private val rsNofallValue = BoolValue("RedeNofall",true)

    val msTimer = MSTimer()

    private var target = 0
    var jState = 0
    var aacLastState = false
    override fun onEnable() {
        if (modeValue.get().equals("NoGround", ignoreCase = true))
            mc.thePlayer.jump()
        jState = 0
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity
            target = entity.entityId

            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                LiquidBounce.moduleManager[Fly::class.java].state || !msTimer.hasTimePassed(delayValue.get().toLong()))
                return

            val x = mc.thePlayer.posX
            val y = mc.thePlayer.posY
            val z = mc.thePlayer.posZ
            val yaw = mc.thePlayer.rotationYaw
            val pitch = mc.thePlayer.rotationPitch
            val motionX: Double
            val motionZ: Double
            if(MovementUtils.isMoving()) {
                motionX = mc.thePlayer.motionX
                motionZ = mc.thePlayer.motionZ
            }else{
                motionX = 0.00
                motionZ = 0.00
            }
            when (modeValue.get().toLowerCase()) {
                "packet" -> {
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
                }

                "ncppacket" -> {
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.11, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.1100013579, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0000013579, z, yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.11, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.1100013579, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0000013579, z, false))
                    }
                }

                "hypixel" -> {
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.04132332, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.023243243674, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.01, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0011, z, yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.04132332, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.023243243674, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0011, z, false))
                    }
                }
                
                "aac4.3.11oldhyt" -> {
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.042487, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0104649713461000007, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0014749900000101, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0000007451816400000, z, yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.042487, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0104649713461000007, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0014749900000101, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0000007451816400000, z, false))
                    }
                }

                "hypixel2" -> {
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.05250000001304, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.00150000001304, z, yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.05250000001304, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                    }
                }

                "mineplex" -> {
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0000000000000045, z, yaw, pitch, true))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0000000000000045, z, true))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                    }
                }

                "more" -> {
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.00000000001, z, yaw, pitch, true))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00000000001, z, true))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                    }
                }

                // Minemora criticals to try
                "testminemora" -> {
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0114514, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0010999999940395355, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.00150000001304, z, yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y + 0.0012016413, z, yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0114514, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0010999999940395355, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.0012016413, z, false))
                    }
                }

                "aacpacket" -> {
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
                }

                "fakecollide" -> {
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    if(lookValue.get()){
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x+(motionX/3), y + 0.20000004768372, z+(motionZ/3), yaw, pitch, false))
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x+(motionX/1.5), y + 0.12160004615784, z+(motionZ/1.5), yaw, pitch, false))
                    }else{
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x+(motionX/3), y + 0.20, z+(motionZ/3), false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x+(motionX/1.5), y + 0.121600000013, z+(motionZ/1.5), false))
                    }
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
                "motion" -> {
                    when (motionValue.get().toLowerCase()) {
                        "jump" -> mc.thePlayer.motionY = 0.42
                        "lowjump" -> mc.thePlayer.motionY = 0.3425
                        "redeskylowhop" -> mc.thePlayer.motionY = 0.35
                        "hop" -> {
                            mc.thePlayer.motionY = 0.1
                            mc.thePlayer.fallDistance = 0.1f
                            mc.thePlayer.onGround = false
                        }
                    }
                }
            }
            mc.thePlayer.onCriticalHit(entity)
            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer){
            when (modeValue.get().toLowerCase()) {
                "noground" -> packet.onGround = false
                "hover" -> {
                    if(hoverCombat.get() && !LiquidBounce.combatManager.inCombat) return
                    if(packet is C05PacketPlayerLook) {
                        mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, packet.yaw, packet.pitch, packet.onGround))  
                        event.cancelEvent()
                        return
                    }else if(!(packet is C04PacketPlayerPosition) && !(packet is C06PacketPlayerPosLook)) {
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, packet.onGround))
                        event.cancelEvent()
                        return
                    }
                    when (hoverValue.get().toLowerCase()) {
                        "2b2t" -> {
                            if(mc.thePlayer.onGround){
                                packet.onGround=false
                                jState++
                                when(jState) {
                                    2 -> packet.y += 0.02
                                    3 -> packet.y += 0.01
                                    4 -> {
                                        if(hoverNoFall.get()) packet.onGround=true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            }else jState = 0
                        }
                        "minis2" -> {
                            if(mc.thePlayer.onGround && !aacLastState) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            if(mc.thePlayer.onGround){
                                packet.onGround = false
                                jState++
                                if(jState % 2 == 0) {
                                    packet.y += 0.015625
                                }else if(jState>100) {
                                    if(hoverNoFall.get()) packet.onGround = true
                                    jState = 1
                                }
                            }else jState = 0
                        }
                        "tpcollide" -> {
                            if(mc.thePlayer.onGround){
                                packet.onGround=false
                                jState++
                                when(jState) {
                                    2 -> packet.y += 0.20000004768372
                                    3 -> packet.y += 0.12160004615784
                                    4 -> {
                                        if(hoverNoFall.get()) packet.onGround=true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            }else jState = 0
                        }
                        "minis" -> {
                            if(mc.thePlayer.onGround && !aacLastState) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            if(mc.thePlayer.onGround){
                                packet.onGround = false
                                jState++
                                if(jState % 2 == 0) {
                                    packet.y += 0.0625
                                }else if(jState>50) {
                                    if(hoverNoFall.get()) packet.onGround = true
                                    jState = 1
                                }
                            }else jState = 0
                        }
                        "normal1" -> {
                            if(mc.thePlayer.onGround){
                                if(!(hoverNoFall.get() && jState == 0)) packet.onGround=false
                                jState++
                                when(jState) {
                                    2 -> packet.y += 0.001335979112147
                                    3 -> packet.y += 0.0000000131132
                                    4 -> packet.y += 0.0000000194788
                                    5 -> packet.y += 0.00000000001304
                                    6 -> {
                                        if(hoverNoFall.get()) packet.onGround=true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            }else jState = 0
                        }
                        "aac4other" -> {
                            if(mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.00101
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 0.001
                            if(mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "aac4" -> {
                            if(mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.000000000000136
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 0.000000000000036
                            if(mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "normal2" -> {
                            if(mc.thePlayer.onGround){
                                if(!(hoverNoFall.get() && jState == 0)) packet.onGround=false
                                jState++
                                when(jState) {
                                    2 -> packet.y += 0.00000000000667547
                                    3 -> packet.y += 0.00000000000045413
                                    4 -> packet.y += 0.000000000000036
                                    5 -> {
                                        if(hoverNoFall.get()) packet.onGround=true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            }else jState = 0
                        }
                        "oldredesky" -> {
                            if(hoverNoFall.get()&&mc.thePlayer.fallDistance>0){
                                packet.onGround=true
                                return
                            }

                            if(mc.thePlayer.onGround){
                                packet.onGround=false
                            }
                        }
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
