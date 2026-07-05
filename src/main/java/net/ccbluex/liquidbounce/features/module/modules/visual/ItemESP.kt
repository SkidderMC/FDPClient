/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderColor.glColor
import net.ccbluex.liquidbounce.utils.render.renderWorldText
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.minecraft.entity.item.EntityItem
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.pow
import net.ccbluex.liquidbounce.utils.render.RenderPrimitives

object ItemESP : Module("ItemESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {
    private val mode by choices("Mode", arrayOf("Box", "OtherBox", "Glow"), "Box")
        .describe("How dropped items are highlighted.")

    private val itemText by boolean("ItemText", false)
        .describe("Show item names above dropped items.")

    private val glowSettings = GlowRenderSettings(isSupported = { mode == "Glow" }).also { addValues(it.values) }

    private val color by color("Color", Color.GREEN)
        .describe("Color used to highlight items.")

    private val maxDistance by float("MaxDistance", 256F, 1F..512F)
        .describe("Maximum distance to render items.")

    private val showTracers by boolean("Tracers", false)
        .describe("Draw lines pointing to dropped items.")
    private val tracerThickness by float("TracerThickness", 2F, 1F..5F) { showTracers }
        .describe("Line thickness of item tracers.")

    private val mergeIntersecting by boolean("MergeIntersecting", false) { mode != "Glow" }
        .describe("Skip boxes overlapping one already drawn.")

    private val renderFilters = RenderFilterSettings(50, 1..200).also { addValues(it.values) }

    private val scale by float("Scale", 3F, 1F..5F) { itemText }
        .describe("Size of the item name text.")
    private val itemCounts by boolean("ItemCounts", true) { itemText }
        .describe("Show stack size next to item names.")
    private val font by font("Font", Fonts.fontSemibold40) { itemText }
        .describe("Font used for item name text.")
    private val fontShadow by boolean("Shadow", true) { itemText }
        .describe("Draw a shadow behind item text.")
    private val yOffset by float("YOffset", 0F, -2F..2F) { itemText }
        .describe("Vertical offset of the item text.")
    private val backgroundAlpha by int("BackgroundAlpha", 0, 0..255) { itemText }
        .describe("Opacity of the text background.")

    private val renderGroup = Configurable("Render")
    private val glowGroup = Configurable("Glow")
    private val filtersGroup = Configurable("Filters")
    private val tracersGroup = Configurable("Tracers")
    private val textGroup = Configurable("Text")

    init {
        moveValues(renderGroup, "Mode", "Color", "MaxDistance", "MergeIntersecting")
        moveValues(glowGroup,
            "Glow-Renderscale", "Glow-Radius", "Glow-Fade", "Glow-Target-Alpha")
        moveValues(filtersGroup,
            "MaxRenderDistance", "OnLook", "MaxAngleDifference", "ThruBlocks")
        moveValues(tracersGroup, "Tracers", "TracerThickness")
        moveValues(textGroup,
            "ItemText", "Scale", "ItemCounts", "Font", "Shadow", "YOffset", "BackgroundAlpha")

        addValues(listOf(
            renderGroup, glowGroup, filtersGroup, tracersGroup, textGroup,
        ))
    }
    private val maxDistanceSq
        get() = maxDistance.toDouble().pow(2.0)

    private val itemEntities by EntityLookup<EntityItem>()
        .filter { renderFilters.withinDistance(mc.thePlayer.getDistanceSqToEntity(it)) }
        .filter { mc.thePlayer.getDistanceSqToEntity(it) <= maxDistanceSq }
        .filter { !renderFilters.onLook || mc.thePlayer.isLookingOn(it, renderFilters.maxAngleDifference.toDouble()) }
        .filter { renderFilters.thruBlocks || isEntityHeightVisible(it) }

    val onRender3D = handler<Render3DEvent> {
        if (mc.theWorld == null || mc.thePlayer == null)
            return@handler

        val drawnBoxes = if (mergeIntersecting) mutableListOf<AxisAlignedBB>() else null

        for (entityItem in itemEntities) {
            val isUseful =
                InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful && InventoryCleaner.isStackUseful(
                    entityItem.entityItem,
                    mc.thePlayer.openContainer.inventory,
                    mapOf(entityItem.entityItem to entityItem)
                )

            val renderColor = if (isUseful) Color.green else color

            if (itemText) {
                renderEntityText(entityItem, renderColor)
            }

            if (mode == "Glow")
                continue

            // Skip boxes that overlap one already drawn this frame to avoid stacked labels
            if (drawnBoxes != null) {
                val box = entityItem.hitBox
                if (drawnBoxes.any { it.intersectsWith(box) })
                    continue

                drawnBoxes += box
            }

            // Only render green boxes on useful items, if enabled, render boxes of the configured color on useless items as well
            drawEntityBox(entityItem, renderColor, mode == "Box")
        }

        if (showTracers)
            renderTracers()
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

        if (backgroundAlpha > 0)
            renderTextBackground(entity, fontRenderer.getStringWidth(text) * 0.5f, fontRenderer.FONT_HEIGHT * 0.5f)

        renderWorldText(entity, text, fontRenderer, color.rgb, fontShadow, scale, yOffset.toDouble())
    }

    // Replicates the world-text transform so the background quad aligns with renderWorldText output
    private fun renderTextBackground(entity: EntityItem, halfWidth: Float, halfHeight: Float) {
        val player = mc.thePlayer ?: return
        val renderManager = mc.renderManager
        val rotateX = if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f

        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()

        val pos = entity.interpolatedPosition(entity.lastTickPos) - renderManager.renderPos

        glTranslated(pos.xCoord, pos.yCoord + yOffset.toDouble(), pos.zCoord)
        glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(renderManager.playerViewX * rotateX, 1F, 0F, 0F)

        RenderUtils.disableGlCap(GL_LIGHTING, GL_DEPTH_TEST, GL_TEXTURE_2D)
        RenderUtils.enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val scaledDistance = ((player.getDistanceToEntity(entity) / 4F).coerceAtLeast(1F) / 150F) * scale
        glScalef(-scaledDistance, -scaledDistance, scaledDistance)

        val padding = 1f
        RenderPrimitives.drawRect(
            -halfWidth - padding,
            -padding,
            halfWidth + padding,
            halfHeight * 2 + padding,
            Color(0, 0, 0, backgroundAlpha).rgb
        )

        RenderUtils.resetCaps()
        glPopMatrix()
        glPopAttrib()
    }

    private fun renderTracers() {
        val player = mc.thePlayer ?: return

        val originalViewBobbing = mc.gameSettings.viewBobbing
        mc.gameSettings.viewBobbing = false
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0)

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(tracerThickness)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        glBegin(GL_LINES)

        val yaw = (player.prevRotationYaw..player.rotationYaw).lerpWith(mc.timer.renderPartialTicks)
        val pitch = (player.prevRotationPitch..player.rotationPitch).lerpWith(mc.timer.renderPartialTicks)
        val eyeVector = Vec3(0.0, 0.0, 1.0).rotatePitch(-pitch.toRadians()).rotateYaw(-yaw.toRadians())

        for (entityItem in itemEntities) {
            val isUseful =
                InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful && InventoryCleaner.isStackUseful(
                    entityItem.entityItem,
                    mc.thePlayer.openContainer.inventory,
                    mapOf(entityItem.entityItem to entityItem)
                )

            val (x, y, z) = entityItem.interpolatedPosition(entityItem.lastTickPos) - mc.renderManager.renderPos

            glColor(if (isUseful) Color.green else color)
            glVertex3d(eyeVector.xCoord, player.getEyeHeight() + eyeVector.yCoord, eyeVector.zCoord)
            glVertex3d(x, y, z)
        }

        glEnd()

        mc.gameSettings.viewBobbing = originalViewBobbing

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glColor4f(1f, 1f, 1f, 1f)
    }

    override fun handleEvents() =
        super.handleEvents() || (InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful)
}
