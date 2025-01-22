/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.utils.client.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.block.BlockTNT
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemFireball
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemTool
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S38PacketPlayerListItem.Action.*
import net.minecraft.potion.Potion
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

object Notifier : Module("Notifier", Category.OTHER) {

    private val onPlayerJoin by boolean("Join", true)
    private val onPlayerLeft by boolean("Left", true)
    private val onPlayerDeath by boolean("Death", true)
    private val onHeldExplosive by boolean("HeldExplosive", true)
    private val onPlayerTool by boolean("HeldTools", false)

    private val bedWarsHelp by boolean("BedWarsHelp", true)

    private val itemChecker by boolean("Item-Checker", true) { bedWarsHelp }
    private val stoneSword by boolean("Stone-Sword", false) { itemChecker }
    private val ironSword by boolean("Iron-Sword", true) { itemChecker }
    private val diamondSword by boolean("Diamond-Sword", true) { itemChecker }
    private val fireBallSword by boolean("FireBall", true) { itemChecker }
    private val enderPearl by boolean("EnderPearl", true) { itemChecker }
    private val tnt by boolean("TNT", true) { itemChecker }
    private val obsidian by boolean("Obsidian", true) { itemChecker }
    private val invisibilityPotion by boolean("InvisibilityPotion", true) { itemChecker }
    private val diamondArmor by boolean("DiamondArmor", true) { bedWarsHelp }

    private val warnDelay by int("WarnDelay", 5000, 1000..50000)
    { onPlayerDeath || onHeldExplosive || onPlayerTool || playerCombat || drinkAlert}

    private val recentlyWarned = ConcurrentHashMap<String, Long>()
    private val playerCombat by boolean("PlayerCombat", false)

    private val drinkAlert by boolean("DrinkAlert", false)
    private val alertTimer = MSTimer()
    private val drinkers = arrayListOf<EntityLivingBase>()

    private data class ItemInfo(
        val name: String,
        val item: Any,
        val enabled: () -> Boolean,
        val playerList: MutableList<String> = mutableListOf()
    )

    private val trackedItems = listOf(
        ItemInfo("Stone Sword", Items.stone_sword, { stoneSword }),
        ItemInfo("Iron Sword", Items.iron_sword, { ironSword }),
        ItemInfo("Diamond Sword", Items.diamond_sword, { diamondSword }),
        ItemInfo("FireBall", Items.fire_charge, { fireBallSword }),
        ItemInfo("Ender Pearl", Items.ender_pearl, { enderPearl }),
        ItemInfo("TNT Block", ItemBlock.getItemById(46), { tnt }),
        ItemInfo("Obsidian Block", ItemBlock.getItemById(49), { obsidian }),
        ItemInfo("Invisibility Potion", Potion.invisibility, { invisibilityPotion }),
        ItemInfo("Diamond Armor", "diamond_armor", { diamondArmor })
    )

    val onRender2D = handler<Render2DEvent> {
        if (!bedWarsHelp) return@handler

        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (player.ticksExisted < 5) {
            trackedItems.forEach { it.playerList.clear() }
        }

        for (entity in world.playerEntities) {
            trackedItems.forEach { itemInfo ->
                val item = entity.heldItem?.item
                if (itemInfo.enabled() && !itemInfo.playerList.contains(entity.name)) {
                    when (itemInfo.item) {
                        is String -> {
                            if (itemInfo.item == "diamond_armor" && isWearingDiamondArmor(entity)) {
                                displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§bDiamond Armor")
                                itemInfo.playerList.add(entity.name)
                                player.playSound("note.pling", 1.0f, 1.0f)
                            }
                        }
                        is ItemBlock -> {
                            if (item == itemInfo.item && entity.heldItem?.item == itemInfo.item ) {
                                displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§b${itemInfo.name}")
                                itemInfo.playerList.add(entity.name)
                                player.playSound("note.pling", 1.0f, 1.0f)
                            }
                        }
                        else -> {
                            if(item == itemInfo.item) {
                                displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§b${itemInfo.name}")
                                itemInfo.playerList.add(entity.name)
                                player.playSound("note.pling", 1.0f, 1.0f)
                            }
                        }
                    }
                }
                if (entity.isDead) {
                    itemInfo.playerList.remove(entity.name)
                }
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        val currentTime = System.currentTimeMillis()
        if (drinkAlert) {
            for (entity in world.playerEntities) {
                if (entity !in drinkers && entity != player && entity.isUsingItem && entity.heldItem?.item is ItemPotion) {
                    chat("§e${entity.name}§r is drinking!")
                    drinkers.add(entity)
                    alertTimer.reset()
                }
            }
            if (alertTimer.hasTimePassed(3000L) && drinkers.isNotEmpty()) {
                clearDrinkers()
            }
        }

        for (entity in world.playerEntities) {
            if (entity.gameProfile.id == player.uniqueID || isBot(entity)) continue
            val entityDistance = player.getDistanceToEntity(entity).roundToInt()
            val lastNotified = recentlyWarned[entity.uniqueID.toString()] ?: 0L

            if (currentTime - lastNotified < warnDelay) continue

            val heldItem = entity.heldItem?.item

            when {
                onPlayerDeath && (entity.isDead || !entity.isEntityAlive) -> {
                    chat("§7${entity.name} has §cdied §a(${entityDistance}m)")
                    recentlyWarned[entity.uniqueID.toString()] = currentTime
                }

                onHeldExplosive && heldItem != null && (heldItem is ItemFireball || heldItem is ItemBlock && heldItem.block is BlockTNT) -> {
                    chat("§7${entity.name} is holding a §eFireball §a(${entityDistance}m)")
                    recentlyWarned[entity.uniqueID.toString()] = currentTime
                }

                onPlayerTool && heldItem is ItemTool -> {
                    chat("§7${entity.name} is holding a §b${entity.heldItem?.displayName} §a(${entityDistance}m)")
                    recentlyWarned[entity.uniqueID.toString()] = currentTime
                }
            }
        }
    }

    val onAttackEntity = handler<AttackEvent> { event ->
        if (!playerCombat) return@handler
        val player = mc.thePlayer ?: return@handler
        val attackedEntity = event.targetEntity
        if (attackedEntity is EntityPlayer && attackedEntity.gameProfile.id != player.uniqueID && !isBot(attackedEntity)) {
            chat("§7${attackedEntity.name} was §cattacked by ${player.name}")
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (player.ticksExisted < 50) return@handler

        when (val packet = event.packet) {
            is S38PacketPlayerListItem -> {
                if (onPlayerJoin && packet.action == ADD_PLAYER) {
                    for (playerData in packet.entries) {
                        val players = playerData.profile ?: continue
                        if (players.id == player.uniqueID || players.id in AntiBot.botList) continue

                        chat("§7${players.name} §ajoined the game.")
                    }
                }
                if (onPlayerLeft && packet.action == REMOVE_PLAYER) {
                    for (playerData in packet.entries) {
                        val players = world.getPlayerEntityByUUID(playerData?.profile?.id)?.gameProfile ?: continue
                        if (players.id == player.uniqueID || players.id in AntiBot.botList) continue

                        chat("§7${players.name} §cleft the game.")
                    }
                }
            }
        }
    }

    private fun isWearingDiamondArmor(player: EntityPlayer): Boolean {
        val armorInventory = player.inventory?.armorInventory ?: return false

        for (itemStack in armorInventory) {
            if (itemStack != null && (itemStack.item == Items.diamond_leggings || itemStack.item == Items.diamond_chestplate)) {
                return true
            }
        }

        return false
    }

    private fun clearDrinkers() {
        drinkers.clear()
        alertTimer.reset()
    }


    val onWorld = handler<WorldEvent> {
        recentlyWarned.clear()
        trackedItems.forEach { it.playerList.clear() }
        clearDrinkers()
    }
}