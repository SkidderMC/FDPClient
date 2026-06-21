/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.INTERACT
import net.minecraft.network.play.client.C02PacketUseEntity.Action.INTERACT_AT

/**
 * Module NoEntityInteract
 *
 * Prevents right-clicking from interacting with entities (villagers, item frames,
 * armor stands, etc.) by cancelling the use-entity interaction packet for the
 * selected entity categories. Useful to avoid accidentally opening villager trades
 * or rotating item frames while fighting or moving.
 */
object NoEntityInteract : Module("NoEntityInteract", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val villagers by boolean("Villagers", true)
    private val itemFrames by boolean("ItemFrames", true)
    private val armorStands by boolean("ArmorStands", true)
    private val others by boolean("Others", false)
    private val onlyWhenSneaking by boolean("OnlyWhenSneaking", false)

    private val handlePacket = handler<PacketEvent> { event ->
        val world = mc.theWorld ?: return@handler
        val player = mc.thePlayer ?: return@handler
        val packet = event.packet

        // Only react to interaction (right-click) actions, never to ATTACK.
        if (packet !is C02PacketUseEntity || (packet.action != INTERACT && packet.action != INTERACT_AT)) {
            return@handler
        }

        if (onlyWhenSneaking && !player.isSneaking) {
            return@handler
        }

        val entity = packet.getEntityFromWorld(world) ?: return@handler

        if (shouldCancel(entity)) {
            event.cancelEvent()
        }
    }

    private fun shouldCancel(entity: Entity): Boolean = when {
        villagers && entity is EntityVillager -> true
        itemFrames && entity is EntityItemFrame -> true
        armorStands && entity is EntityArmorStand -> true
        others && entity !is EntityVillager && entity !is EntityItemFrame && entity !is EntityArmorStand -> true
        else -> false
    }
}
