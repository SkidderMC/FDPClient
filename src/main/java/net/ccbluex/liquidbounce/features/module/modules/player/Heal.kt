/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.event.PacketEvent
import net.minecraft.network.play.server.S02PacketChat
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.MathHelper
import net.minecraft.init.Items
import net.minecraft.potion.Potion
import org.apache.commons.lang3.tuple.Pair
import java.util.*

@ModuleInfo(name = "Heal", category = ModuleCategory.PLAYER)
class Heal : Module() {
    private val percent = FloatValue("HealthPercent", 75.0f, 1.0f, 100.0f)
    private val min = IntegerValue("MinDelay", 75, 1, 5000)
    private val max = IntegerValue("MaxDelay", 125, 1, 5000)
    private val regenSec = FloatValue("RegenSec", 4.6f, 0.0f, 10.0f)
    private val groundCheck = BoolValue("GroundCheck", false)
    private val voidCheck = BoolValue("VoidCheck", true)
    private val waitRegen = BoolValue("WaitRegen", true)
    private val invCheck = BoolValue("InvCheck", false)
    private val absorpCheck = BoolValue("AbsorpCheck", true)
    val timer = MSTimer()
    var delay = 0
    var isDisable = false
    override fun onEnable() {
        super.onEnable()
        timer.reset()
        isDisable = false
        delay = MathHelper.getRandomIntegerInRange(Random(), min.get(), max.get())
    }

    @EventTarget
    fun onPacket(e: PacketEvent) {
        if (e.packet is S02PacketChat && e.packet.chatComponent.formattedText.contains("§r§7 won the game! §r§e\u272a§r")) {
            ClientUtils.displayChatMessage("§f[§cSLHeal§f] §6Temp Disable Heal")
            isDisable = true
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer.ticksExisted <= 5 && isDisable) {
            isDisable = false
            ClientUtils.displayChatMessage("§f[§cSLHeal§f] §6Enable Heal due to World Changed or Player Respawned")
        }
        val absorp = MathHelper.ceiling_double_int(mc.thePlayer.absorptionAmount.toDouble())
        if (groundCheck.get() && !mc.thePlayer.onGround || voidCheck.get() && !MovementUtils.isBlockUnder() || invCheck.get() && mc.currentScreen is GuiContainer || absorp != 0 && absorpCheck.get())
            return
        if (waitRegen.get() && mc.thePlayer.isPotionActive(Potion.regeneration) && mc.thePlayer.getActivePotionEffect(Potion.regeneration).duration > regenSec.get() * 20.0f)
            return
        val pair = gAppleSlot
        if (!isDisable && pair != null && (mc.thePlayer.health <= percent.get() / 100.0f * mc.thePlayer.maxHealth || !mc.thePlayer.isPotionActive(Potion.absorption) || absorp == 0 && mc.thePlayer.health == 20.0f && mc.thePlayer.isPotionActive(Potion.absorption)) && timer.hasTimePassed(delay.toLong())) {
            ClientUtils.displayChatMessage("§f[§cSLHeal§f] §6Healed")
            val lastSlot = mc.thePlayer.inventory.currentItem
            val slot = pair.left as Int
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(slot))
            val stack = pair.right as ItemStack
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(stack))
            for (i in 0..31) {
                mc.netHandler.addToSendQueue(C03PacketPlayer())
            }
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(lastSlot))
            mc.thePlayer.inventory.currentItem = lastSlot
            mc.playerController.updateController()
            delay = MathHelper.getRandomIntegerInRange(Random(), min.get(), max.get())
            timer.reset()
        }
    }

    private val gAppleSlot: Pair<Int, ItemStack>?
        private get() {
            for (i in 0..8) {
                val stack = mc.thePlayer.inventory.getStackInSlot(i)
                if (stack != null && stack.item === Items.golden_apple) {
                    return Pair.of(i, stack)
                }
            }
            return null
        }
    override val tag: String?
        get() = if (mc.thePlayer == null || mc.thePlayer.health == Float.NaN) null else String.format(
            "%.2f HP",
            percent.get() / 100.0f * mc.thePlayer.maxHealth
        )
}