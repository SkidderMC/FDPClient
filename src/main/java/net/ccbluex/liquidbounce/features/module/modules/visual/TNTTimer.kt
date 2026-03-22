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
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.renderWorldText
import net.minecraft.entity.item.EntityTNTPrimed
import java.awt.Color

object TNTTimer : Module("TNTTimer", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, spacedName = "TNT Timer") {

    private val scale by float("Scale", 3F, 1F..4F)
    private val font by font("Font", Fonts.fontSemibold40)
    private val fontShadow by boolean("Shadow", true)

    private val color by color("Color", Color.WHITE)

    private val renderFilters = RenderFilterSettings(50, 1..200, defaultMaxAngleDifference = 5f, includeThruBlocks = false)
        .also { addValues(it.values) }

    private val tntEntities by EntityLookup<EntityTNTPrimed>()
        .filter { it.fuse > 0 }
        .filter { renderFilters.withinDistance(mc.thePlayer.getDistanceSqToEntity(it)) }
        .filter { !renderFilters.onLook || mc.thePlayer.isLookingOnEntity(it, renderFilters.maxAngleDifference.toDouble()) }

    val onRender3D = handler<Render3DEvent> {
        for (entity in tntEntities) {
            renderTNTTimer(entity, entity.fuse / 5)
        }
    }

    private fun renderTNTTimer(tnt: EntityTNTPrimed, timeRemaining: Int) {
        val text = "TNT Explodes in: $timeRemaining"
        renderWorldText(tnt, text, font, color.rgb, fontShadow, scale, yOffset = 1.5)
    }

}
