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
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.stats.StatList

@ModuleInfo(name = "Criticals", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    val modeValue = ListValue("Mode", arrayOf("Packet", "NCPPacket", "MiPacket", "Hypixel", "Hypixel2", "VulcanSemi", "MatrixSemi",  "AACPacket", "AAC4.3.11OldHYT", "AAC5.0.4", "NoGround", "MiniPhase", "NanoPacket", "non-calculable", "invalid", "TPHop", "FakeCollide", "Mineplex", "More", "TestMinemora", "VerusSmart","Motion", "Hover"), "packet")
    private val motionValue = ListValue("MotionMode", arrayOf("RedeSkyLowHop", "Hop", "Jump", "LowJump", "MinemoraTest"), "Jump")
    private val hoverValue = ListValue("HoverMode", arrayOf("AAC4", "AAC4Other", "OldRedesky", "Normal1", "Normal2", "Minis", "Minis2", "TPCollide", "2b2t"), "AAC4")
    private val hoverNoFall = BoolValue("HoverNoFall", true).displayable { modeValue.equals("Hover") }
    private val hoverCombat = BoolValue("HoverOnlyCombat", true).displayable { modeValue.equals("Hover") }
    private val delayValue = IntegerValue("Delay", 0, 0, 500)
    private val s08FlagValue = BoolValue("FlagPause", true)
    private val s08DelayValue = IntegerValue("FlagPauseTime", 100, 100, 5000).displayable { s08FlagValue.get() }
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val lookValue = BoolValue("UseC06Packet", false)
    private val debugValue = BoolValue("DebugMessage", false)
    // private val rsNofallValue = BoolValue("RedeNofall",true)

    val msTimer = MSTimer()
    
    val flagTimer = MSTimer()

    private var target = 0
    var jState = 0
    var aacLastState = false
    var attacks = 0

    override fun onEnable() {
        if (modeValue.equals("NoGround")) {
            mc.thePlayer.jump()
        }
        jState = 0
        attacks = 0
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity
            target = entity.entityId

            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                !msTimer.hasTimePassed(delayValue.get().toLong())) {
                return
            }
            
            if(s08FlagValue.get() && !flagTimer.hasTimePassed(s08DelayValue.get().toLong()))
                return

            fun sendCriticalPacket(xOffset: Double = 0.0, yOffset: Double = 0.0, zOffset: Double = 0.0, ground: Boolean) {
                val x = mc.thePlayer.posX + xOffset
                val y = mc.thePlayer.posY + yOffset
                val z = mc.thePlayer.posZ + zOffset
                if (lookValue.get()) {
                    mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, ground))
                } else {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, ground))
                }
            }

            when (modeValue.get().lowercase()) {
                "packet" -> {
                    sendCriticalPacket(yOffset = 0.0625, ground = true)
                    sendCriticalPacket(ground = false)
                    sendCriticalPacket(yOffset = 1.1E-5, ground = false)
                    sendCriticalPacket(ground = false)
                }

                "ncppacket" -> {
                    sendCriticalPacket(yOffset = 0.11, ground = false)
                    sendCriticalPacket(yOffset = 0.1100013579, ground = false)
                    sendCriticalPacket(yOffset = 0.0000013579, ground = false)
                }
                
                "mipacket" -> {
                    sendCriticalPacket(yOffset = 0.0625, ground = false)
                    sendCriticalPacket(ground = false)
                }
                
                "aac5.0.4" -> { //aac5.0.4 moment but with bad cfg(cuz it will flag for timer)
                    sendCriticalPacket(yOffset = 0.00133545, ground = false)
                    sendCriticalPacket(yOffset = -0.000000433, ground = false)
                }

                "hypixel" -> {
                    sendCriticalPacket(yOffset = 0.04132332, ground = false)
                    sendCriticalPacket(yOffset = 0.023243243674, ground = false)
                    sendCriticalPacket(yOffset = 0.01, ground = false)
                    sendCriticalPacket(yOffset = 0.0011, ground = false)
                }

                "aac4.3.11oldhyt" -> {
                    sendCriticalPacket(yOffset = 0.042487, ground = false)
                    sendCriticalPacket(yOffset = 0.0104649713461000007, ground = false)
                    sendCriticalPacket(yOffset = 0.0014749900000101, ground = false)
                    sendCriticalPacket(yOffset = 0.0000007451816400000, ground = false)
                }
                
                "vulcansemi" -> {
                    attacks++
                    if(attacks > 6) {
                        sendCriticalPacket(yOffset = 0.2, ground = false)
                        sendCriticalPacket(yOffset = 0.1216, ground = false)
                        attacks = 0
                    }
                }
                
                "matrixsemi" -> {
                    attacks++
                    if(attacks > 3) {
                    sendCriticalPacket(yOffset = 0.110314, ground = false)
                    sendCriticalPacket(yOffset = 0.0200081, ground = false)
                    sendCriticalPacket(yOffset = 0.00000001300009, ground = false)
                    sendCriticalPacket(yOffset = 0.000000000022, ground = false)
                    sendCriticalPacket(ground = true)
                    attacks = 0
                    }
                }
                
                "verus" -> {
                    attacks ++
                    if (attacks > 4) {
                        attacks = 0
                        
                        sendCriticalPacket(yOffset = 0.001, ground = true)
                        sendCriticalPacket(ground = false)
                    }
                }
                
                "hypixel2" -> {
                    sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                }

                "mineplex" -> {
                    sendCriticalPacket(yOffset = 0.0000000000000045, ground = false)
                    sendCriticalPacket(ground = false)
                }

                "more" -> {
                    sendCriticalPacket(yOffset = 0.00000000001, ground = false)
                    sendCriticalPacket(ground = false)
                }

                // Minemora criticals without test
                "testminemora" -> {
                    sendCriticalPacket(yOffset = 0.0114514, ground = false)
                    sendCriticalPacket(yOffset = 0.0010999999940395355, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.0012016413, ground = false)
                }

                "aacpacket" -> {
                    sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.01400000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                }

                "fakecollide" -> {
                    val motionX: Double
                    val motionZ: Double
                    if (MovementUtils.isMoving()) {
                        motionX = mc.thePlayer.motionX
                        motionZ = mc.thePlayer.motionZ
                    } else {
                        motionX = 0.00
                        motionZ = 0.00
                    }
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    sendCriticalPacket(xOffset = motionX / 3, yOffset = 0.20000004768372, zOffset = motionZ / 3, ground = false)
                    sendCriticalPacket(xOffset = motionX / 1.5, yOffset = 0.12160004615784, zOffset = motionZ / 1.5, ground = false)
                }
                
                "miniphase" -> {
                    sendCriticalPacket(yOffset = -0.0125, ground = false)
                    sendCriticalPacket(yOffset =  0.01275, ground = false)
                    sendCriticalPacket(yOffset = -0.00025, ground = false)
                }

                "nanopacket" -> {
                    sendCriticalPacket(yOffset =  0.00973333333333, ground = false)
                    sendCriticalPacket(yOffset =  0.001, ground = false)
                    sendCriticalPacket(yOffset = -0.01200000000007, ground = false)
                    sendCriticalPacket(yOffset = -0.0005, ground = false)

                }

                "non-calculable" -> {
                    sendCriticalPacket(yOffset =  1E-5, ground = false)
                    sendCriticalPacket(yOffset =  1E-7, ground = false)
                    sendCriticalPacket(yOffset = -1E-6, ground = false)
                    sendCriticalPacket(yOffset = -1E-4, ground = false)

                }

                "invalid" -> {
                    sendCriticalPacket(yOffset =  1E+27, ground = false)
                    sendCriticalPacket(yOffset = -1E+68, ground = false)
                    sendCriticalPacket(yOffset =  1E+41, ground = false)
                }

                "tphop" -> {
                    sendCriticalPacket(yOffset = 0.02, ground = false)
                    sendCriticalPacket(yOffset = 0.01, ground = false)
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ)
                }

                "motion" -> {
                    when (motionValue.get().lowercase()) {
                        "jump" -> mc.thePlayer.motionY = 0.42
                        "lowjump" -> mc.thePlayer.motionY = 0.3425
                        "redeskylowhop" -> mc.thePlayer.motionY = 0.35
                        "hop" -> {
                            mc.thePlayer.motionY = 0.1
                            mc.thePlayer.fallDistance = 0.1f
                            mc.thePlayer.onGround = false
                        }
                        "minemoratest" -> {
                            mc.timer.timerSpeed = 0.82f
                            mc.thePlayer.motionY = 0.124514
                        }
                    }
                }
            }
            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        
        if (packet is S08PacketPlayerPosLook) {
            flagTimer.reset()
            if (s08FlagValue.get()) {
                jState = 0
            }
        }
        
        if(s08FlagValue.get() && !flagTimer.hasTimePassed(s08DelayValue.get().toLong()))
            return

        if (packet is C03PacketPlayer) {
            when (modeValue.get().lowercase()) {
                "noground" -> packet.onGround = false
                "motion" -> {
                    when (motionValue.get().lowercase()) {
                        "minemoratest" -> if (!LiquidBounce.combatManager.inCombat) mc.timer.timerSpeed = 1.00f
                    }
                }
                "hover" -> {
                    if (hoverCombat.get() && !LiquidBounce.combatManager.inCombat) return
                    packet.isMoving = true
                    when (hoverValue.get().lowercase()) {
                        "2b2t" -> {
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.02
                                    3 -> packet.y += 0.01
                                    4 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "minis2" -> {
                            if (mc.thePlayer.onGround && !aacLastState) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                if (jState % 2 == 0) {
                                    packet.y += 0.015625
                                } else if (jState> 100) {
                                    if (hoverNoFall.get()) packet.onGround = true
                                    jState = 1
                                }
                            } else jState = 0
                        }
                        "tpcollide" -> {
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.20000004768372
                                    3 -> packet.y += 0.12160004615784
                                    4 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "minis" -> {
                            if (mc.thePlayer.onGround && !aacLastState) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                if (jState % 2 == 0) {
                                    packet.y += 0.0625
                                } else if (jState> 50) {
                                    if (hoverNoFall.get()) packet.onGround = true
                                    jState = 1
                                }
                            } else jState = 0
                        }
                        "normal1" -> {
                            if (mc.thePlayer.onGround) {
                                if (!(hoverNoFall.get() && jState == 0)) packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.001335979112147
                                    3 -> packet.y += 0.0000000131132
                                    4 -> packet.y += 0.0000000194788
                                    5 -> packet.y += 0.00000000001304
                                    6 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "aac4other" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.00101
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 0.001
                            if (mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "aac4" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.000000000000136
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 0.000000000000036
                            if (mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "normal2" -> {
                            if (mc.thePlayer.onGround) {
                                if (!(hoverNoFall.get() && jState == 0)) packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.00000000000667547
                                    3 -> packet.y += 0.00000000000045413
                                    4 -> packet.y += 0.000000000000036
                                    5 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "oldredesky" -> {
                            if (hoverNoFall.get() && mc.thePlayer.fallDistance> 0) {
                                packet.onGround = true
                                return
                            }

                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                            }
                        }
                    }
                }
            }
        }
        if (packet is S0BPacketAnimation && debugValue.get()) {
            if (packet.animationType == 4 && packet.entityID == target) {
                alert("CRIT")
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
