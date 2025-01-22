/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S09PacketHeldItemChange
import net.minecraft.potion.Potion.regeneration
import net.minecraft.util.MathHelper
import java.util.*

object Gapple : Module("Gapple", Category.PLAYER) {

    private val modeValue by choices("Mode", arrayOf("Auto", "LegitAuto", "Legit", "Head"), "Auto")
    private val percent by float("HealthPercent", 75.0f, 1.0f..100.0f)
    private val min by int("MinDelay", 75, 1.. 5000)
    private val max by int("MaxDelay", 125, 1.. 5000)
    private val regenSec by float("MinRegenSec", 4.6f, 0.0f.. 10.0f)
    private val groundCheck by boolean("OnlyOnGround", false)
    private val waitRegen by boolean("WaitRegen", true)
    private val invCheck by boolean("InvCheck", false)
    private val absorpCheck by boolean("NoAbsorption", true)
    private val fastEatValue by boolean("FastEat", false) { modeValue == ("LegitAuto") || modeValue == ("Legit") }
    private val eatDelayValue by int("FastEatDelay", 14, 0.. 35) { fastEatValue }
    val timer = MSTimer()
    private var eating = -1
    var delay = 0
    private var isDisable = false
    private var tryHeal = false
    private var prevSlot = -1
    private var switchBack = false
    override fun onEnable() {
        eating = -1
	prevSlot = -1
	switchBack = false
        timer.reset()
        isDisable = false
        tryHeal = false
        delay = MathHelper.getRandomIntegerInRange(Random(), min, max)
    }


       val onWorld = handler<WorldEvent> {
        isDisable = true
        tryHeal = false
    }


    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        if (eating != -1 && packet is C03PacketPlayer) {
            eating++
        } else if (packet is S09PacketHeldItemChange || packet is C09PacketHeldItemChange) {
            eating = -1
        }
    }


    val onUpdate = handler<UpdateEvent> {
        if (tryHeal) {
            when (modeValue.lowercase()) {
                "auto" -> {
                    val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
                    if (gappleInHotbar != -1) {
                        if (gappleInHotbar != null) {
                            sendPacket(C09PacketHeldItemChange(gappleInHotbar - 36))
                        }
                        sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        repeat(35) {
                            sendPacket(C03PacketPlayer(mc.thePlayer.onGround))
                        }
                        sendPacket(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        Chat.print("Gapple eaten")
                        tryHeal = false
                        timer.reset()
                        delay = MathHelper.getRandomIntegerInRange(Random(), min, max)
                    }else {
                        tryHeal = false
                    }
                }
                "legitauto" -> {
                    if (eating == -1) {
                        val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
                        if(gappleInHotbar == -1) {
                            tryHeal = false
                            return@handler
                        }
                        if (gappleInHotbar != null) {
                            sendPacket(C09PacketHeldItemChange(gappleInHotbar - 36))
                        }
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        eating = 0
                    } else if (eating > 35 || (fastEatValue && eating > eatDelayValue)) {
	      		repeat(35 - eating) {
                    sendPacket(C03PacketPlayer(mc.thePlayer.onGround))
                        }
                        sendPacket(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        timer.reset()
                        tryHeal = false
                        delay = MathHelper.getRandomIntegerInRange(Random(), min, max)
                    }
                }
                "legit" -> {
                    if (eating == -1) {
                        val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
                        if(gappleInHotbar == -1) {
                            tryHeal = false
                            return@handler
                        }
                        if (prevSlot == -1)
                            prevSlot = mc.thePlayer.inventory.currentItem

                        if (gappleInHotbar != null) {
                            mc.thePlayer.inventory.currentItem = gappleInHotbar - 36
                        }
                        sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        eating = 0
                    } else if (eating > 35 || (fastEatValue && eating > eatDelayValue)) {
			repeat(35 - eating) {
                sendPacket(C03PacketPlayer(mc.thePlayer.onGround))
                        }
                        timer.reset()
                        tryHeal = false
                        delay = MathHelper.getRandomIntegerInRange(Random(), min, max)
                    }
                }
                "head" -> {
                    val headInHotbar = InventoryUtils.findItem(36, 45, Items.skull)
                    if (headInHotbar != -1) {
                        if (headInHotbar != null) {
                            sendPacket(C09PacketHeldItemChange(headInHotbar - 36))
                        }
                        sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        sendPacket(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        timer.reset()
                        tryHeal = false
                        delay = MathHelper.getRandomIntegerInRange(Random(), min, max)
                    }else {
                        tryHeal = false
                    }
                }
            }
        }
        if (mc.thePlayer.ticksExisted <= 10 && isDisable) {
            isDisable = false
        }
        val absorp = MathHelper.ceiling_double_int(mc.thePlayer.absorptionAmount.toDouble())


        if (!tryHeal && prevSlot != -1) {
            if (!switchBack) {
                switchBack = true
                return@handler
            }
            mc.thePlayer.inventory.currentItem = prevSlot
	    eating = -1
            prevSlot = -1
            switchBack = false
        }

        if ((groundCheck && !mc.thePlayer.onGround) || (invCheck && mc.currentScreen is GuiContainer) || (absorp > 0 && absorpCheck))
            return@handler
        if (waitRegen && mc.thePlayer.isPotionActive(regeneration) && mc.thePlayer.getActivePotionEffect(regeneration).duration > regenSec * 20.0f)
            return@handler
        if (!isDisable && (mc.thePlayer.health <= (percent / 100.0f) * mc.thePlayer.maxHealth) && timer.hasTimePassed(delay.toLong())) {
            if (tryHeal)
                return@handler
            tryHeal = true
        }
    }

    override val tag: String
        get() = modeValue
} 
