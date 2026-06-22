/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.item.EntityMinecart
import net.minecraft.network.play.client.C02PacketUseEntity

/**
 * Breaks boats and minecarts in a single click by firing a burst of attack packets at the
 * vehicle the moment you hit it, instead of needing several manual hits.
 */
object VehicleOneHit : Module("VehicleOneHit", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    private val hits by int("Hits", 20, 2..40)
        .describe("Number of attack packets sent per hit.")

    private var bursting = false

    val onPacket = handler<PacketEvent> { event ->
        if (bursting) {
            return@handler
        }

        val packet = event.packet
        if (packet !is C02PacketUseEntity || packet.action != C02PacketUseEntity.Action.ATTACK) {
            return@handler
        }

        val world = mc.theWorld ?: return@handler
        val target = packet.getEntityFromWorld(world) ?: return@handler

        if (target is EntityBoat || target is EntityMinecart) {
            bursting = true
            repeat(hits - 1) {
                sendPacket(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
            }
            bursting = false
        }
    }
}
