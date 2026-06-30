/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.config.Configurable
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
        .describe("Notify when a player joins the game.")
    private val onPlayerLeft by boolean("Left", true)
        .describe("Notify when a player leaves the game.")
    private val onPlayerDeath by boolean("Death", true)
        .describe("Notify when a nearby player dies.")
    private val onHeldExplosive by boolean("HeldExplosive", true)
        .describe("Notify when a player holds a fireball or TNT.")
    private val onPlayerTool by boolean("HeldTools", false)
        .describe("Notify when a player holds a tool.")

    private val joinMessages by boolean("Join Messages", false)
        .describe("Show a compact plus marker on player join.")
    private val leaveMessages by boolean("Leave Messages", false)
        .describe("Show a compact minus marker on player leave.")
    private val heldItemMessages by boolean("Held Item Messages", false)
        .describe("Print your currently held item when you switch.")
    private val itemConsumptionMessages by boolean("Item Consumption Messages", false)
        .describe("Print a message when you finish consuming an item.")
    private val gameModeMessages by boolean("Game Mode Messages", false)
        .describe("Print a message when your game mode changes.")

    private val bedWarsHelp by boolean("BedWarsHelp", true)
        .describe("Enable BedWars item and armor detection alerts.")

    private val itemChecker by boolean("Item-Checker", true) { bedWarsHelp }
        .describe("Alert when players hold tracked weapons or items.")

    private val stoneSword by boolean("Stone-Sword", false) { itemChecker }
        .describe("Alert when a player holds a stone sword.")
    private val ironSword by boolean("Iron-Sword", true) { itemChecker }
        .describe("Alert when a player holds an iron sword.")
    private val diamondSword by boolean("Diamond-Sword", true) { itemChecker }
        .describe("Alert when a player holds a diamond sword.")

    private val ironPickaxe by boolean("Iron-Pickaxe", true) { itemChecker }
        .describe("Alert when a player holds an iron pickaxe.")
    private val diamondPickaxe by boolean("Diamond-Pickaxe", true) { itemChecker }
        .describe("Alert when a player holds a diamond pickaxe.")
    private val goldPickaxe by boolean("Gold-Pickaxe", true) { itemChecker }
        .describe("Alert when a player holds a gold pickaxe.")

    private val fireBallSword by boolean("FireBall", true) { itemChecker }
        .describe("Alert when a player holds a fireball.")
    private val enderPearl by boolean("EnderPearl", true) { itemChecker }
        .describe("Alert when a player holds an ender pearl.")
    private val tnt by boolean("TNT", true) { itemChecker }
        .describe("Alert when a player holds TNT.")
    private val obsidian by boolean("Obsidian", true) { itemChecker }
        .describe("Alert when a player holds obsidian.")
    private val invisibilityPotion by boolean("InvisibilityPotion", true) { itemChecker }
        .describe("Alert when a player holds an invisibility potion.")

    private val diamondArmor by boolean("DiamondArmor", true) { bedWarsHelp }
        .describe("Alert when a player wears diamond armor.")
    private val chainArmor by boolean("ChainArmor", true) { bedWarsHelp }
        .describe("Alert when a player wears chain armor.")
    private val ironArmor by boolean("IronArmor", true) { bedWarsHelp }
        .describe("Alert when a player wears iron armor.")

    private val enchantedArmor by boolean("EnchantedArmor", true) { bedWarsHelp }
        .describe("Alert when a player wears enchanted armor.")
    private val enchantedSword by boolean("EnchantedSword", true) { itemChecker }
        .describe("Alert when a player holds an enchanted sword.")

    private val invisibleCheck by boolean("InvisibleCheck", true) { bedWarsHelp }
        .describe("Alert when a player turns invisible or visible.")
    private val potionInvis by boolean("PotionInvis", true) { bedWarsHelp }
        .describe("Alert about your own invisibility potion timer.")

    private val warnDelay by int("WarnDelay", 5000, 1000..50000) {
        onPlayerDeath || onHeldExplosive || onPlayerTool || playerCombat || drinkAlert
    }
        .describe("Minimum delay between repeated warnings in milliseconds.")

    private val recentlyWarned = ConcurrentHashMap<String, Long>()
    private val playerCombat by boolean("PlayerCombat", false)
        .describe("Notify when one player attacks another.")
    private val drinkAlert by boolean("DrinkAlert", false)
        .describe("Notify when a nearby player drinks a potion.")

    private val combatGroup = Configurable("Combat")
    private val connectionGroup = Configurable("Connection")
    private val clientGroup = Configurable("Client")
    private val bedWarsGroup = Configurable("BedWars")
    private val trackedItemsGroup = Configurable("TrackedItems")
    private val armorAndInvisGroup = Configurable("ArmorAndInvisibility")

    init {
        moveValues(combatGroup,
            "Death", "HeldExplosive", "HeldTools", "PlayerCombat", "DrinkAlert", "WarnDelay")

        moveValues(connectionGroup,
            "Join", "Left", "Join Messages", "Leave Messages")

        moveValues(clientGroup,
            "Held Item Messages", "Item Consumption Messages", "Game Mode Messages")

        moveValues(bedWarsGroup,
            "BedWarsHelp", "Item-Checker")

        moveValues(trackedItemsGroup,
            "Stone-Sword", "Iron-Sword", "Diamond-Sword", "Iron-Pickaxe", "Diamond-Pickaxe",
            "Gold-Pickaxe", "FireBall", "EnderPearl", "TNT", "Obsidian", "InvisibilityPotion",
            "EnchantedSword")

        moveValues(armorAndInvisGroup,
            "DiamondArmor", "ChainArmor", "IronArmor", "EnchantedArmor", "InvisibleCheck",
            "PotionInvis")

        addValues(listOf(
            combatGroup, connectionGroup, clientGroup, bedWarsGroup, trackedItemsGroup,
            armorAndInvisGroup,
        ))
    }

    private fun moveValues(group: Configurable, vararg names: String) {
        for (name in names) {
            values.firstOrNull { it.matchesKey(name) }?.let(group::addValue)
        }
    }

    private val alertTimer = MSTimer()
    private val drinkers = arrayListOf<EntityLivingBase>()

    private var wasPlayerInvisible = false
    private val invisiblePlayers = mutableSetOf<String>()

    private var lastHeldSlot = -1
    private var wasConsuming = false
    private var lastConsumedName: String? = null
    private var lastGameType: String? = null

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

        recentlyWarned.entries.removeIf { (_, timestamp) -> currentTime - timestamp > warnDelay * 2 }

        if (heldItemMessages) {
            val slot = player.inventory.currentItem
            if (lastHeldSlot == -1) {
                lastHeldSlot = slot
            } else if (slot != lastHeldSlot) {
                lastHeldSlot = slot
                val held = player.heldItem
                val itemName = held?.displayName ?: "empty hand"
                chat("§7Now holding §b$itemName")
            }
        } else {
            lastHeldSlot = player.inventory.currentItem
        }

        if (itemConsumptionMessages) {
            val consuming = player.isUsingItem &&
                    (player.heldItem?.item is ItemFood || player.heldItem?.item is ItemPotion || player.heldItem?.item is ItemBucketMilk)
            if (consuming) {
                wasConsuming = true
                lastConsumedName = player.heldItem?.displayName
            } else if (wasConsuming) {
                wasConsuming = false
                val itemName = lastConsumedName ?: "item"
                chat("§7Finished consuming §b$itemName")
                lastConsumedName = null
            }
        } else {
            wasConsuming = false
            lastConsumedName = null
        }

        if (gameModeMessages) {
            val gameType = mc.playerController?.currentGameType?.name
            if (lastGameType == null) {
                lastGameType = gameType
            } else if (gameType != null && gameType != lastGameType) {
                lastGameType = gameType
                chat("§7Game mode changed to §b${gameType.lowercase()}")
            }
        } else {
            lastGameType = mc.playerController?.currentGameType?.name
        }

        val onlinePlayers = world.playerEntities.mapTo(mutableSetOf()) { it.name }
        invisiblePlayers.retainAll(onlinePlayers)

        trackedItems.forEach { it.playerList.retainAll(onlinePlayers) }

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
                if ((onPlayerJoin || joinMessages) && packet.action == Action.ADD_PLAYER) {
                    for (playerData in packet.entries) {
                        val profile = playerData.profile ?: continue
                        if (profile.id == player.uniqueID || profile.id in AntiBot.botList) continue
                        if (onPlayerJoin) chat("§7${profile.name} §ajoined the game.")
                        if (joinMessages) chat("§7[§a+§7] §a${profile.name}")
                    }
                }
                if ((onPlayerLeft || leaveMessages) && packet.action == Action.REMOVE_PLAYER) {
                    for (playerData in packet.entries) {
                        val profile = world.getPlayerEntityByUUID(playerData?.profile?.id)?.gameProfile ?: continue
                        if (profile.id == player.uniqueID || profile.id in AntiBot.botList) continue
                        if (onPlayerLeft) chat("§7${profile.name} §cleft the game.")
                        if (leaveMessages) chat("§7[§c-§7] §c${profile.name}")
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
        lastHeldSlot = -1
        wasConsuming = false
        lastConsumedName = null
        lastGameType = null
    }
}