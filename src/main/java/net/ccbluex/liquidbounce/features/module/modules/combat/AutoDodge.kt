/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.entity.projectile.EntityArrow
import kotlin.math.sqrt

/**
 * Nudges you sideways out of the path of incoming arrows. Each move tick it finds the
 * closest in-flight arrow that is actually closing in on you within range, then adds a
 * perpendicular push away from its trajectory so the shot misses.
 */
object AutoDodge : Module("AutoDodge", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    private val range by float("Range", 5f, 1f..16f)
    private val strength by float("Strength", 0.4f, 0.05f..1f)

    val onMove = handler<MoveEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        val threat = world.loadedEntityList
            .filterIsInstance<EntityArrow>()
            .filter { arrow ->
                (arrow.motionX != 0.0 || arrow.motionY != 0.0 || arrow.motionZ != 0.0) &&
                    player.getDistanceToEntity(arrow) <= range &&
                    isApproaching(arrow)
            }
            .minByOrNull { player.getDistanceToEntity(it) } ?: return@handler

        val dx = threat.motionX
        val dz = threat.motionZ
        val len = sqrt(dx * dx + dz * dz)
        if (len < 1e-4) return@handler

        var perpX = -dz / len
        var perpZ = dx / len

        // Push toward whichever side leaves us further from the arrow's line of travel.
        val toPlayerX = player.posX - threat.posX
        val toPlayerZ = player.posZ - threat.posZ
        if (perpX * toPlayerX + perpZ * toPlayerZ < 0.0) {
            perpX = -perpX
            perpZ = -perpZ
        }

        event.x += perpX * strength
        event.z += perpZ * strength
    }

    private fun isApproaching(arrow: EntityArrow): Boolean {
        val player = mc.thePlayer ?: return false
        val curX = arrow.posX - player.posX
        val curY = arrow.posY - player.posY
        val curZ = arrow.posZ - player.posZ
        val nextX = arrow.posX + arrow.motionX - player.posX
        val nextY = arrow.posY + arrow.motionY - player.posY
        val nextZ = arrow.posZ + arrow.motionZ - player.posZ
        val cur = curX * curX + curY * curY + curZ * curZ
        val next = nextX * nextX + nextY * nextY + nextZ * nextZ
        return next < cur
    }
}
