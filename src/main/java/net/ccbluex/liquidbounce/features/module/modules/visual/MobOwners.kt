/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.getFullName
import net.ccbluex.liquidbounce.utils.extensions.interpolatedPosition
import net.ccbluex.liquidbounce.utils.extensions.lastTickPos
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityTameable
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import java.util.UUID

object MobOwners : Module("MobOwners", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {

    private val tameables by boolean("Tameables", true)
    private val horses by boolean("Horses", true)
    private val projectiles by boolean("Projectiles", false)
    private val stripColors by boolean("StripColors", false)
    private val height by float("Height", 0.6F, 0F..2F)

    val onRender3D = handler<Render3DEvent> {
        val world = mc.theWorld ?: return@handler
        mc.thePlayer ?: return@handler

        for (entity in world.loadedEntityList) {
            val owner = resolveOwner(entity) ?: continue
            val text = if (stripColors) ColorUtils.stripColor(owner) else owner

            val pos = entity.interpolatedPosition(entity.lastTickPos)
            RenderUtils.renderNameTag(
                text,
                pos.xCoord,
                pos.yCoord + entity.height + height,
                pos.zCoord
            )
        }
    }

    private fun resolveOwner(entity: Entity): String? = when {
        entity is EntityTameable && tameables -> nameFromOwnerId(entity.ownerId)
        entity is EntityHorse && horses -> nameFromOwnerId(entity.ownerId)
        entity is EntityArrow && projectiles -> entity.shootingEntity?.displayName?.formattedText
        entity is EntityThrowable && projectiles -> entity.thrower?.displayName?.formattedText
        else -> null
    }

    private fun nameFromOwnerId(ownerId: String?): String? {
        if (ownerId.isNullOrEmpty()) {
            return null
        }

        val uuid = runCatching { UUID.fromString(ownerId) }.getOrNull() ?: return null

        mc.netHandler?.getPlayerInfo(uuid)?.let { return it.getFullName() }
        mc.theWorld?.getPlayerEntityByUUID(uuid)?.let { return it.displayName.formattedText }

        return ownerId.substring(0, ownerId.indexOf('-').takeIf { it > 0 } ?: ownerId.length)
    }
}
