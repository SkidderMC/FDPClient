/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C09PacketHeldItemChange

object AutoMobHeal : Module("AutoMobHeal", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val golems by boolean("IronGolems", true)
        .describe("Heal hurt iron golems with iron ingots.")
    private val tamed by boolean("TamedAnimals", true)
        .describe("Heal hurt tamed wolves.")
    private val horses by boolean("Horses", true)
        .describe("Heal hurt tamed horses.")

    private val allowGolden by boolean("AllowGoldenFood", false) { horses }
        .describe("Allow golden food when healing horses.")

    private val range by float("Range", 4.0f, 1.0f..6.0f)
        .describe("Maximum distance to heal a mob.")
    private val healthThreshold by float("HealthThreshold", 75.0f, 1.0f..100.0f, "%")
        .describe("Heal mobs below this health percentage.")
    private val delay by int("Delay", 200, 0..2000, "ms")
        .describe("Delay between heal actions in milliseconds.")

    private val golemFood = arrayOf<Item>(Items.iron_ingot)

    private val tamedFood = arrayOf<Item>(
        Items.cooked_beef,
        Items.cooked_chicken
    )

    private val horseFood = arrayOf<Item>(
        Items.sugar,
        Items.wheat,
        Items.apple,
        Items.carrot,
        Items.bread
    )

    private val horseGoldenFood = arrayOf<Item>(
        Items.golden_carrot,
        Items.golden_apple
    )

    private val timer = MSTimer()

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (mc.currentScreen != null || player.isUsingItem || !timer.hasTimePassed(delay.toLong()))
            return@handler

        val minRatio = (healthThreshold / 100.0f).coerceIn(0.0f, 1.0f)
        val maxRange = range.toDouble()

        var bestTarget: EntityLivingBase? = null
        var bestSlot = -1
        var bestDistance = Double.MAX_VALUE

        for (entity in world.loadedEntityList) {
            if (entity !is EntityLivingBase || !entity.isEntityAlive)
                continue

            if (!isHealable(entity) || entity.health > entity.maxHealth * minRatio)
                continue

            val distance = player.getDistanceToEntityBox(entity)
            if (distance > maxRange || distance >= bestDistance)
                continue

            if (!player.canEntityBeSeen(entity))
                continue

            val slot = findFoodSlot(entity) ?: continue

            bestTarget = entity
            bestSlot = slot
            bestDistance = distance
        }

        val target = bestTarget ?: return@handler

        val previousSlot = player.inventory.currentItem

        sendPacket(C09PacketHeldItemChange(bestSlot))
        player.inventory.currentItem = bestSlot

        sendPacket(C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT))
        player.swingItem()

        if (previousSlot != bestSlot) {
            sendPacket(C09PacketHeldItemChange(previousSlot))
            player.inventory.currentItem = previousSlot
        }

        timer.reset()
    }

    private fun isHealable(entity: EntityLivingBase): Boolean = when (entity) {
        is EntityIronGolem -> golems
        is EntityHorse -> horses && entity.isTame && !entity.isChild
        is EntityWolf -> tamed && entity.isTamed
        else -> false
    }

    private fun findFoodSlot(entity: EntityLivingBase): Int? {
        val items = when (entity) {
            is EntityIronGolem -> golemFood
            is EntityHorse -> if (allowGolden) horseFood + horseGoldenFood else horseFood
            else -> tamedFood
        }

        return findHotbarSlot(items)
    }

    private fun findHotbarSlot(items: Array<Item>): Int? {
        val player = mc.thePlayer ?: return null

        for (i in 0..8) {
            val stack = player.inventory.getStackInSlot(i) ?: continue
            if (items.any { it === stack.item })
                return i
        }

        return null
    }
}
