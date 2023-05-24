package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class Hover : CriticalMode("Hover") {
    private val hoverValue = ListValue(
        "HoverMode",
        arrayOf("AAC4", "AAC4Other",
            "OldRedesky",
            "Normal1", "Normal2", "Minis", "Minis2", "TPCollide",
            "2b2t"),
        "AAC4")

    private val hoverNoFall = BoolValue("Hover-NoFall", true)
    private val hoverCombat = BoolValue("Hover-OnlyCombat", true)
    private var jState = 0
    private var aacLastState = false
    override fun onEnable() {
        jState = 0
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S08PacketPlayerPosLook) {
            if (critical.s08FlagValue.get()) {
                jState = 0
            }
        }
        if(packet is C03PacketPlayer) {
            if (hoverCombat.get() && !FDPClient.combatManager.inCombat) return
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
                        } else if (jState > 100) {
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
                        } else if (jState > 50) {
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
                    if (hoverNoFall.get() && mc.thePlayer.fallDistance > 0) {
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