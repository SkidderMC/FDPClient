/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.attackDamage
import net.ccbluex.liquidbounce.utils.inventory.totalDurability
import net.ccbluex.liquidbounce.utils.item.EnchantmentValueEstimator
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK

object AutoWeapon : Module("AutoWeapon", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT, subjective = true) {

    private val enchantmentEstimator = EnchantmentValueEstimator(
        EnchantmentValueEstimator.WeightedEnchantment(Enchantment.fireAspect, 0.35F),
        EnchantmentValueEstimator.WeightedEnchantment(Enchantment.knockback, 0.15F),
        EnchantmentValueEstimator.WeightedEnchantment(Enchantment.unbreaking, 0.05F),
    )

    private val onlySword by boolean("OnlySword", false)
        .describe("Only switch to swords, ignore tools.")

    private val spoof by boolean("SpoofItem", false)
        .describe("Silently spoof the weapon instead of switching.")
    private val spoofTicks by int("SpoofTicks", 10, 1..20) { spoof }
        .describe("Ticks to keep the spoofed weapon selected.")
    private val switchBack by boolean("SwitchBack", false) { !spoof }
        .describe("Switch back to the previous slot after combat ends.")
    private val switchBackTicks by int("SwitchBackTicks", 20, 1..300, "ticks") { switchBack && !spoof }
        .describe("Ticks without attacking before switching back.")

    private var attackEnemy = false
    private var previousSlot = -1
    private var ticksSinceAttack = 0

    val onAttack = handler<AttackEvent> {
        attackEnemy = true
        ticksSinceAttack = 0
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        if (!switchBack || spoof || previousSlot < 0) return@handler

        ticksSinceAttack++
        if (ticksSinceAttack < switchBackTicks) return@handler

        if (previousSlot in 0..8 && player.inventory.getStackInSlot(previousSlot) != null) {
            player.inventory.currentItem = previousSlot
        }
        previousSlot = -1
        ticksSinceAttack = 0
    }

    override fun onDisable() {
        if (switchBack && !spoof && previousSlot in 0..8) {
            mc.thePlayer?.inventory?.let { it.currentItem = previousSlot }
        }
        previousSlot = -1
        ticksSinceAttack = 0
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (event.packet is C02PacketUseEntity && event.packet.action == ATTACK && attackEnemy) {
            attackEnemy = false

            // Find the best weapon in hotbar (#Kotlin Style)
            val (slot, _) = (0..8)
                .map { it to mc.thePlayer.inventory.getStackInSlot(it) }
                .filter {
                    it.second != null && ((onlySword && it.second.item is ItemSword)
                            || (!onlySword && (it.second.item is ItemSword || it.second.item is ItemTool)))
                }
                .maxWithOrNull(
                    compareBy<Pair<Int, net.minecraft.item.ItemStack>> { it.second.attackDamage }
                        .thenBy { enchantmentEstimator.estimateValue(it.second) }
                        .thenBy { it.second.totalDurability }
                        .thenBy { it.first == player.inventory.currentItem }
                ) ?: return@handler

            if (slot == mc.thePlayer.inventory.currentItem) // If in hand no need to swap
                return@handler

            // Switch to best weapon
            SilentHotbar.selectSlotSilently(this, slot, spoofTicks, true, !spoof, spoof)

            if (!spoof) {
                if (switchBack && previousSlot < 0) previousSlot = player.inventory.currentItem
                player.inventory.currentItem = slot
                SilentHotbar.resetSlot(this)
            }

            // Resend attack packet
            sendPacket(event.packet)
            event.cancelEvent()
        }
    }
}
