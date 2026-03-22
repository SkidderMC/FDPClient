/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.renderWorldText
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.minecraft.entity.item.EntityItem
import java.awt.Color

object ItemESP : Module("ItemESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {
    private val mode by choices("Mode", arrayOf("Box", "OtherBox", "Glow"), "Box")

    private val itemText by boolean("ItemText", false)

    private val glowSettings = GlowRenderSettings(isSupported = { mode == "Glow" }).also { addValues(it.values) }

    private val color by color("Color", Color.GREEN)

    private val renderFilters = RenderFilterSettings(50, 1..200).also { addValues(it.values) }

    private val scale by float("Scale", 3F, 1F..5F) { itemText }
    private val itemCounts by boolean("ItemCounts", true) { itemText }
    private val font by font("Font", Fonts.fontSemibold40) { itemText }
    private val fontShadow by boolean("Shadow", true) { itemText }

    private val itemEntities by EntityLookup<EntityItem>()
        .filter { renderFilters.withinDistance(mc.thePlayer.getDistanceSqToEntity(it)) }
        .filter { !renderFilters.onLook || mc.thePlayer.isLookingOnEntity(it, renderFilters.maxAngleDifference.toDouble()) }
        .filter { renderFilters.thruBlocks || isEntityHeightVisible(it) }

    val onRender3D = handler<Render3DEvent> {
        if (mc.theWorld == null || mc.thePlayer == null)
            return@handler

        for (entityItem in itemEntities) {
            val isUseful =
                InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful && InventoryCleaner.isStackUseful(
                    entityItem.entityItem,
                    mc.thePlayer.openContainer.inventory,
                    mapOf(entityItem.entityItem to entityItem)
                )

            if (itemText) {
                renderEntityText(entityItem, if (isUseful) Color.green else color)
            }

            if (mode == "Glow")
                continue

            // Only render green boxes on useful items, if ItemESP is enabled, render boxes of ItemESP.color on useless items as well
            drawEntityBox(entityItem, if (isUseful) Color.green else color, mode == "Box")
        }
    }

    val onRender2D = handler<Render2DEvent> { event ->
        if (mode != "Glow")
            return@handler

        for (entityItem in itemEntities) {
            val isUseful =
                InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful && InventoryCleaner.isStackUseful(
                    entityItem.entityItem,
                    mc.thePlayer.openContainer.inventory,
                    mapOf(entityItem.entityItem to entityItem)
                )

            renderGlow(event.partialTicks, if (isUseful) Color.green else color, glowSettings) {
                mc.renderManager.renderEntityStatic(entityItem, event.partialTicks, true)
            }
        }
    }

    private fun renderEntityText(entity: EntityItem, color: Color) {
        val fontRenderer = font
        val itemStack = entity.entityItem
        val text = itemStack.displayName + if (itemCounts) " (${itemStack.stackSize})" else ""

        renderWorldText(entity, text, fontRenderer, color.rgb, fontShadow, scale)
    }

    override fun handleEvents() =
        super.handleEvents() || (InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful)
}
