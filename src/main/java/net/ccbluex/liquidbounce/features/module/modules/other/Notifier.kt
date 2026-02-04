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
import net.minecraft.item.*
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S38PacketPlayerListItem.Action
import net.minecraft.potion.Potion
import kotlin.math.roundToInt
import java.util.concurrent.ConcurrentHashMap

object Notifier : Module("Notifier", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

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

    private val ironPickaxe by boolean("Iron-Pickaxe", true) { itemChecker }
    private val diamondPickaxe by boolean("Diamond-Pickaxe", true) { itemChecker }
    private val goldPickaxe by boolean("Gold-Pickaxe", true) { itemChecker }

    private val fireBallSword by boolean("FireBall", true) { itemChecker }
    private val enderPearl by boolean("EnderPearl", true) { itemChecker }
    private val tnt by boolean("TNT", true) { itemChecker }
    private val obsidian by boolean("Obsidian", true) { itemChecker }
    private val invisibilityPotion by boolean("InvisibilityPotion", true) { itemChecker }

    private val diamondArmor by boolean("DiamondArmor", true) { bedWarsHelp }
    private val chainArmor by boolean("ChainArmor", true) { bedWarsHelp }
    private val ironArmor by boolean("IronArmor", true) { bedWarsHelp }

    private val enchantedArmor by boolean("EnchantedArmor", true) { bedWarsHelp }
    private val enchantedSword by boolean("EnchantedSword", true) { itemChecker }

    private val invisibleCheck by boolean("InvisibleCheck", true) { bedWarsHelp }
    private val potionInvis by boolean("PotionInvis", true) { bedWarsHelp }

    private val warnDelay by int("WarnDelay", 5000, 1000..50000) {
        onPlayerDeath || onHeldExplosive || onPlayerTool || playerCombat || drinkAlert
    }

    private val recentlyWarned = ConcurrentHashMap<String, Long>()
    private val playerCombat by boolean("PlayerCombat", false)
    private val drinkAlert by boolean("DrinkAlert", false)
    private val alertTimer = MSTimer()
    private val drinkers = arrayListOf<EntityLivingBase>()

    private var wasPlayerInvisible = false
    private val invisiblePlayers = mutableSetOf<String>()

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
        ItemInfo("Iron Pickaxe", Items.iron_pickaxe, { ironPickaxe }),
        ItemInfo("Diamond Pickaxe", Items.diamond_pickaxe, { diamondPickaxe }),
        ItemInfo("Gold Pickaxe", Items.golden_pickaxe, { goldPickaxe }),
        ItemInfo("FireBall", Items.fire_charge, { fireBallSword }),
        ItemInfo("Ender Pearl", Items.ender_pearl, { enderPearl }),
        ItemInfo("TNT Block", ItemBlock.getItemById(46), { tnt }),
        ItemInfo("Obsidian Block", ItemBlock.getItemById(49), { obsidian }),
        ItemInfo("Invisibility Potion", Potion.invisibility, { invisibilityPotion }),
        ItemInfo("Diamond Armor", "diamond_armor", { diamondArmor }),
        ItemInfo("Chain Armor", "chain_armor", { chainArmor }),
        ItemInfo("Iron Armor", "iron_armor", { ironArmor }),
        ItemInfo("Enchanted Armor", "enchanted_armor", { enchantedArmor }),
        ItemInfo("Enchanted Sword", "enchanted_sword", { enchantedSword })
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
                when (itemInfo.item) {
                    is String -> {
                        when (itemInfo.item) {
                            "diamond_armor" -> {
                                if (isWearingDiamondArmor(entity) && !itemInfo.playerList.contains(entity.name)) {
                                    displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§bDiamond Armor")
                                    itemInfo.playerList.add(entity.name)
                                    player.playSound("note.pling", 1.0f, 1.0f)
                                }
                            }
                            "chain_armor" -> {
                                if (isWearingChainArmor(entity) && !itemInfo.playerList.contains(entity.name)) {
                                    displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§bChain Armor")
                                    itemInfo.playerList.add(entity.name)
                                    player.playSound("note.pling", 1.0f, 1.0f)
                                }
                            }
                            "iron_armor" -> {
                                if (isWearingIronArmor(entity) && !itemInfo.playerList.contains(entity.name)) {
                                    displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§bIron Armor")
                                    itemInfo.playerList.add(entity.name)
                                    player.playSound("note.pling", 1.0f, 1.0f)
                                }
                            }
                            "enchanted_armor" -> {
                                if (isWearingEnchantedArmor(entity) && !itemInfo.playerList.contains(entity.name)) {
                                    displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§bReinforced Armor")
                                    itemInfo.playerList.add(entity.name)
                                    player.playSound("note.pling", 1.0f, 1.0f)
                                }
                            }
                            "enchanted_sword" -> {
                                if (isHoldingEnchantedSword(entity) && !itemInfo.playerList.contains(entity.name)) {
                                    displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§bSharpened Sword")
                                    itemInfo.playerList.add(entity.name)
                                    player.playSound("note.pling", 1.0f, 1.0f)
                                }
                            }
                        }
                    }
                    is ItemBlock -> {
                        val heldItem = entity.heldItem?.item
                        if (itemInfo.enabled() && heldItem == itemInfo.item && !itemInfo.playerList.contains(entity.name)) {
                            displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§b${itemInfo.name}")
                            itemInfo.playerList.add(entity.name)
                            player.playSound("note.pling", 1.0f, 1.0f)
                        }
                    }
                    else -> {
                        val heldItem = entity.heldItem?.item
                        if (itemInfo.enabled() && heldItem == itemInfo.item && !itemInfo.playerList.contains(entity.name)) {
                            displayChatMessage("§F[§dBWH§F] ${entity.displayName.formattedText} has §l§b${itemInfo.name}")
                            itemInfo.playerList.add(entity.name)
                            player.playSound("note.pling", 1.0f, 1.0f)
                        }
                    }
                }
            }
            if (entity.isDead) {
                trackedItems.forEach { it.playerList.remove(entity.name) }
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
                onHeldExplosive && heldItem != null &&
                        (heldItem is ItemFireball || (heldItem is ItemBlock && heldItem.block is BlockTNT)) -> {
                    chat("§7${entity.name} is holding a §eFireball §a(${entityDistance}m)")
                    recentlyWarned[entity.uniqueID.toString()] = currentTime
                }
                onPlayerTool && heldItem is ItemTool -> {
                    chat("§7${entity.name} is holding a §b${entity.heldItem?.displayName} §a(${entityDistance}m)")
                    recentlyWarned[entity.uniqueID.toString()] = currentTime
                }
            }
        }

        for (entity in world.playerEntities) {
            if (entity.gameProfile.id == player.uniqueID) continue
            if (invisibleCheck) {
                val hasInvis = entity.isPotionActive(Potion.invisibility)
                if (hasInvis && entity.name !in invisiblePlayers) {
                    chat("§7${entity.name} is now §eInvisible")
                    invisiblePlayers.add(entity.name)
                } else if (!hasInvis && entity.name in invisiblePlayers) {
                    chat("§7${entity.name} is now §eVisible")
                    invisiblePlayers.remove(entity.name)
                }
            }
        }

        if (potionInvis) {
            val playerInvis = player.isPotionActive(Potion.invisibility)
            if (playerInvis) {
                wasPlayerInvisible = true
                if (player.ticksExisted % 200 == 0) {
                    val duration = player.getActivePotionEffect(Potion.invisibility)?.duration?.div(20) ?: 0
                    chat("Your Invisibility §cexpires in §r${duration} second(s)")
                }
            } else if (wasPlayerInvisible) {
                chat("Invisibility §cExpired")
                wasPlayerInvisible = false
            }
        }
    }

    val onAttackEntity = handler<AttackEvent> { event ->
        if (!playerCombat) return@handler
        val attacker = mc.thePlayer ?: return@handler
        val attacked = event.targetEntity as? EntityPlayer ?: return@handler
        if (isBot(attacker) || isBot(attacked)) return@handler
        if (attacker.gameProfile.id != attacked.gameProfile.id) {
            chat("§7${attacked.name} was §cattacked by ${attacker.name}")
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler
        if (player.ticksExisted < 50) return@handler

        when (val packet = event.packet) {
            is S38PacketPlayerListItem -> {
                if (onPlayerJoin && packet.action == Action.ADD_PLAYER) {
                    for (playerData in packet.entries) {
                        val profile = playerData.profile ?: continue
                        if (profile.id == player.uniqueID || profile.id in AntiBot.botList) continue
                        chat("§7${profile.name} §ajoined the game.")
                    }
                }
                if (onPlayerLeft && packet.action == Action.REMOVE_PLAYER) {
                    for (playerData in packet.entries) {
                        val profile = world.getPlayerEntityByUUID(playerData?.profile?.id)?.gameProfile ?: continue
                        if (profile.id == player.uniqueID || profile.id in AntiBot.botList) continue
                        chat("§7${profile.name} §cleft the game.")
                    }
                }
            }
        }
    }

    private fun isWearingDiamondArmor(player: EntityPlayer): Boolean {
        val armorInventory = player.inventory?.armorInventory ?: return false
        for (itemStack in armorInventory) {
            if (itemStack != null && (itemStack.item == Items.diamond_chestplate || itemStack.item == Items.diamond_leggings)) {
                return true
            }
        }
        return false
    }

    private fun isWearingChainArmor(player: EntityPlayer): Boolean {
        val armorInventory = player.inventory?.armorInventory ?: return false
        for (itemStack in armorInventory) {
            if (itemStack != null && itemStack.item is ItemArmor) {
                val material = (itemStack.item as ItemArmor).armorMaterial
                if (material == ItemArmor.ArmorMaterial.CHAIN) {
                    return true
                }
            }
        }
        return false
    }

    private fun isWearingIronArmor(player: EntityPlayer): Boolean {
        val armorInventory = player.inventory?.armorInventory ?: return false
        for (itemStack in armorInventory) {
            if (itemStack != null && itemStack.item is ItemArmor) {
                val material = (itemStack.item as ItemArmor).armorMaterial
                if (material == ItemArmor.ArmorMaterial.IRON) {
                    return true
                }
            }
        }
        return false
    }

    private fun isWearingEnchantedArmor(player: EntityPlayer): Boolean {
        val chestplate = player.inventory.armorInventory.getOrNull(2)
        if (chestplate != null && chestplate.item is ItemArmor) {
            return chestplate.hasTagCompound() &&
                    chestplate.enchantmentTagList != null &&
                    chestplate.enchantmentTagList.tagCount() > 0
        }
        return false
    }

    private fun isHoldingEnchantedSword(player: EntityPlayer): Boolean {
        val heldItem = player.heldItem
        if (heldItem != null && heldItem.item is ItemSword) {
            return heldItem.hasTagCompound() &&
                    heldItem.enchantmentTagList != null &&
                    heldItem.enchantmentTagList.tagCount() > 0
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