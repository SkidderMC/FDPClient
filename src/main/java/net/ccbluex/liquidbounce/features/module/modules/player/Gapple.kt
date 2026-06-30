/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.config.Configurable
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
import net.minecraft.item.Item
import net.minecraft.potion.Potion.regeneration
import net.minecraft.util.MathHelper
import java.util.*

object Gapple : Module("Gapple", Category.PLAYER, Category.SubCategory.PLAYER_COUNTER) {

    private val modeValue by choices("Mode", arrayOf("Auto", "LegitAuto", "Legit", "Head"), "Auto")
        .describe("How to eat the golden apple.")
    private val triggerMode by choices("TriggerMode", arrayOf("Percent", "Health"), "Percent")
        .describe("Trigger by health percent or absolute health.")
    private val percent by float("HealthPercent", 75.0f, 1.0f..100.0f) { triggerMode == "Percent" }
        .describe("Health percent below which to eat.")
    private val healthValue by float("Health", 10.0f, 1.0f..20.0f) { triggerMode == "Health" }
        .describe("Health value below which to eat.")
    private val min by int("MinDelay", 75, 1.. 5000)
        .describe("Minimum delay between eats in milliseconds.")
    private val max by int("MaxDelay", 125, 1.. 5000)
        .describe("Maximum delay between eats in milliseconds.")
    private val regenSec by float("MinRegenSec", 4.6f, 0.0f.. 10.0f)
        .describe("Skip eating if regen lasts at least this long.")
    private val groundCheck by boolean("OnlyOnGround", false)
        .describe("Only eat while standing on the ground.")
    private val waitRegen by boolean("WaitRegen", true)
        .describe("Wait for regeneration to run out before eating.")
    private val invCheck by boolean("InvCheck", false)
        .describe("Do not eat while an inventory is open.")
    private val absorpCheck by boolean("NoAbsorption", true)
        .describe("Do not eat while absorption hearts are active.")
    private val legitCompletion by choices("LegitCompletion", arrayOf("Current", "Legacy"), "Current") {
        modeValue == "LegitAuto" || modeValue == "Legit"
    }
        .describe("Eating-completion logic to use in legit modes.")
    private val fastEatValue by boolean("FastEat", false) {
        legitCompletion == "Current" && (modeValue == "LegitAuto" || modeValue == "Legit")
    }
        .describe("Speed up finishing the eat in legit modes.")
    private val eatDelayValue by int("FastEatDelay", 14, 0.. 35) { legitCompletion == "Current" && fastEatValue }
        .describe("Ticks to wait before finishing fast eat.")
    val timer = MSTimer()
    private var eating = -1
    var delay = 0
    private var isDisable = false
    private var tryHeal = false
    private var prevSlot = -1
    private var switchBack = false

    private val modeGroup = Configurable("Mode")
    private val triggerGroup = Configurable("Trigger")
    private val delaysGroup = Configurable("Delays")
    private val checksGroup = Configurable("Checks")

    init {
        moveValues(modeGroup, "Mode", "LegitCompletion", "FastEat", "FastEatDelay")
        moveValues(triggerGroup, "TriggerMode", "HealthPercent", "Health")
        moveValues(delaysGroup, "MinDelay", "MaxDelay", "MinRegenSec", "WaitRegen")
        moveValues(checksGroup, "OnlyOnGround", "InvCheck", "NoAbsorption")

        addValues(listOf(modeGroup, triggerGroup, delaysGroup, checksGroup))
    }

    private fun moveValues(group: Configurable, vararg names: String) {
        for (name in names) {
            values.firstOrNull { it.matchesKey(name) }?.let(group::addValue)
        }
    }

    override fun onEnable() {
        eating = -1
	prevSlot = -1
	switchBack = false
        timer.reset()
        isDisable = false
        tryHeal = false
        delay = MathHelper.getRandomIntegerInRange(Random(), min, max)
    }

    private fun findHotbarContainerSlot(item: Item): Int? = InventoryUtils.findItem(36, 44, item)?.plus(36)
    private fun shouldFinishLegitEating(): Boolean =
        eating > 35 || (legitCompletion == "Current" && fastEatValue && eating > eatDelayValue)

    private fun sendRemainingEatPackets() {
        if (legitCompletion != "Current") return

        repeat((35 - eating).coerceAtLeast(0)) {
            sendPacket(C03PacketPlayer(mc.thePlayer.onGround))
        }
    }

    private fun shouldStartHealing(): Boolean = when (triggerMode) {
        "Health" -> mc.thePlayer.health <= healthValue
        else -> mc.thePlayer.health <= (percent / 100.0f) * mc.thePlayer.maxHealth
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
                    val gappleInHotbar = findHotbarContainerSlot(Items.golden_apple)
                    if (gappleInHotbar != null) {
                        sendPacket(C09PacketHeldItemChange(gappleInHotbar - 36))
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
                        val gappleInHotbar = findHotbarContainerSlot(Items.golden_apple) ?: run {
                            tryHeal = false
                            return@handler
                        }
                        sendPacket(C09PacketHeldItemChange(gappleInHotbar - 36))
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        eating = 0
                    } else if (shouldFinishLegitEating()) {
                        sendRemainingEatPackets()
                        sendPacket(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        timer.reset()
                        tryHeal = false
                        delay = MathHelper.getRandomIntegerInRange(Random(), min, max)
                    }
                }
                "legit" -> {
                    if (eating == -1) {
                        val gappleInHotbar = findHotbarContainerSlot(Items.golden_apple) ?: run {
                            tryHeal = false
                            return@handler
                        }
                        if (prevSlot == -1)
                            prevSlot = mc.thePlayer.inventory.currentItem

                        mc.thePlayer.inventory.currentItem = gappleInHotbar - 36
                        sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        eating = 0
                    } else if (shouldFinishLegitEating()) {
                        sendRemainingEatPackets()
                        timer.reset()
                        tryHeal = false
                        delay = MathHelper.getRandomIntegerInRange(Random(), min, max)
                    }
                }
                "head" -> {
                    val headInHotbar = findHotbarContainerSlot(Items.skull)
                    if (headInHotbar != null) {
                        sendPacket(C09PacketHeldItemChange(headInHotbar - 36))
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
        if (!isDisable && shouldStartHealing() && timer.hasTimePassed(delay.toLong())) {
            if (tryHeal)
                return@handler
            tryHeal = true
        }
    }

    override val tag: String
        get() = modeValue
} 
