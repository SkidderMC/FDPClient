/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.SpoofItemUtils
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.MovingObjectPosition


@ModuleInfo(name = "AutoItem", category = ModuleCategory.PLAYER)
object AutoItem : Module() {
    private val autoTool = BoolValue("AutoTool", true)
    private val noCombat = BoolValue("NoCombat", true).displayable { autoTool.get() }
    private val autoWeapon = BoolValue("AutoWeapon", false)
    private val onlySwordValue = BoolValue("OnlySword", false).displayable { autoWeapon.get() }
    private val silent = BoolValue("Spoof", false)
    private var prevItem = 0
    private var mining = false
    private var bestSlot = 0
    private var attackEnemy = false
    private var prevItemWeapon = 0
    private var spoofTick = 0

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (autoTool.get()) {
            if (!mc.gameSettings.keyBindUseItem.isKeyDown && mc.gameSettings.keyBindAttack.isKeyDown && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (noCombat.get() && FDPClient.combatManager.inCombat) return
                var bestSpeed = 0
                bestSlot = -1
                if (!mining) {
                    prevItem = mc.thePlayer.inventory.currentItem
                    if (silent.get())
                         SpoofItemUtils.startSpoof(prevItem)
                }

                val block = mc.theWorld.getBlockState(mc.objectMouseOver.blockPos).block

                for (i in 0..8) {
                    val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
                    val speed = item.getStrVsBlock(block)

                    if (speed > bestSpeed) {
                        bestSpeed = speed.toInt()
                        bestSlot = i
                    }

                    if (bestSlot != -1) {
                        mc.thePlayer.inventory.currentItem = bestSlot
                    }
                }
                mining = true
            } else {
                if (mining) {
                    SpoofItemUtils.stopSpoof()
                    mining = false
                } else {
                    prevItem = mc.thePlayer.inventory.currentItem
                }
            }
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        attackEnemy = true
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (autoWeapon.get()) {
            if (event.packet is C02PacketUseEntity && event.packet.action == C02PacketUseEntity.Action.ATTACK &&
                attackEnemy
            ) {
                attackEnemy = false

                // Find best weapon in hotbar (#Kotlin Style)
                val (slot, _) = (0..8)
                    .map { Pair(it, mc.thePlayer.inventory.getStackInSlot(it)) }
                    .filter { it.second != null && (it.second.item is ItemSword || (it.second.item is ItemTool && !onlySwordValue.get())) }
                    .maxByOrNull {
                        (it.second.attributeModifiers["generic.attackDamage"].first()?.amount
                            ?: 0.0) + 1.25 * ItemUtils.getEnchantment(it.second, Enchantment.sharpness)
                    } ?: return

                if (slot == mc.thePlayer.inventory.currentItem) { // If in hand no need to swap
                    return
                }

                // Switch to best weapon
                if (!SpoofItemUtils.spoofing) {
                    prevItemWeapon = mc.thePlayer.inventory.currentItem
                    if (silent.get())
                        SpoofItemUtils.startSpoof(prevItemWeapon)
                }
                spoofTick = 15
                mc.thePlayer.inventory.currentItem = slot
                mc.playerController.updateController()


                // Resend attack packet
                mc.netHandler.addToSendQueue(event.packet)
                event.cancelEvent()
            }
        }
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (autoWeapon.get()) {
            if (spoofTick > 0) {
                if (spoofTick == 1) {
                    mc.thePlayer.inventory.currentItem
                    SpoofItemUtils.stopSpoof()
                }
                spoofTick--
            }
        }
    }
}
