/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.movement.InventoryCleaner
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.EntityUtils.isLookingOnEntities
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.item.EntityItem
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.pow

object ItemESP : Module("ItemESP", Category.VISUAL, hideModule = false) {
    private val mode by ListValue("Mode", arrayOf("Box", "OtherBox", "Glow"), "Box")

    private val glowRenderScale by FloatValue("Glow-Renderscale", 1f, 0.5f..2f) { mode == "Glow" }
    private val glowRadius by IntegerValue("Glow-Radius", 4, 1..5) { mode == "Glow" }
    private val glowFade by IntegerValue("Glow-Fade", 10, 0..30) { mode == "Glow" }
    private val glowTargetAlpha by FloatValue("Glow-Target-Alpha", 0f, 0f..1f) { mode == "Glow" }

    private val colorRainbow by BoolValue("Rainbow", true)
    private val colorRed by IntegerValue("R", 0, 0..255) { !colorRainbow }
    private val colorGreen by IntegerValue("G", 255, 0..255) { !colorRainbow }
    private val colorBlue by IntegerValue("B", 0, 0..255) { !colorRainbow }

    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 50, 1..100) {
        override fun onInit(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
    }

    private var maxRenderDistanceSq = 0.0

    private val onLook by BoolValue("OnLook", false)
    private val maxAngleDifference by FloatValue("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by BoolValue("ThruBlocks", true)

    val color
        get() = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)

    // TODO: Removed highlighting of EntityArrow to not complicate things even further

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (mc.theWorld == null || mc.thePlayer == null || mode == "Glow")
            return

        runCatching {
            mc.theWorld.loadedEntityList.asSequence()
                .filterIsInstance<EntityItem>()
                .filter { mc.thePlayer.getDistanceSqToEntity(it) <= maxRenderDistanceSq }
                .filter { !onLook || isLookingOnEntities(it, maxAngleDifference.toDouble()) }
                .filter { thruBlocks || RotationUtils.isVisible(Vec3(it.posX, it.posY, it.posZ)) }
                .forEach { entityItem ->
                    val isUseful = InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful && InventoryCleaner.isStackUseful(
                        entityItem.entityItem,
                        mc.thePlayer.openContainer.inventory,
                        mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>().associateBy { it.entityItem }
                    )

                    // Only render green boxes on useful items, if ItemESP is enabled, render boxes of ItemESP.color on useless items as well
                    drawEntityBox(entityItem, if (isUseful) Color.green else color, mode == "Box")
                }
        }.onFailure {
            LOGGER.error("An error occurred while rendering ItemESP!", it)
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.theWorld == null || mc.thePlayer == null || mode != "Glow")
            return

        runCatching {
            mc.theWorld.loadedEntityList.asSequence()
                .filterIsInstance<EntityItem>()
                .filter { mc.thePlayer.getDistanceSqToEntity(it) <= maxRenderDistanceSq }
                .filter { !onLook || isLookingOnEntities(it, maxAngleDifference.toDouble()) }
                .filter { thruBlocks || RotationUtils.isVisible(Vec3(it.posX, it.posY, it.posZ)) }
                .forEach { entityItem ->
                    val isUseful = InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful && InventoryCleaner.isStackUseful(
                        entityItem.entityItem,
                        mc.thePlayer.openContainer.inventory,
                        mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>().associateBy { it.entityItem }
                    )

                    GlowShader.startDraw(event.partialTicks, glowRenderScale)

                    mc.renderManager.renderEntityStatic(entityItem, event.partialTicks, true)

                    // Only render green boxes on useful items, if ItemESP is enabled, render boxes of ItemESP.color on useless items as well
                    GlowShader.stopDraw(if (isUseful) Color.green else color, glowRadius, glowFade, glowTargetAlpha)
                }
        }.onFailure {
            LOGGER.error("An error occurred while rendering ItemESP!", it)
        }
    }

    override fun handleEvents() = super.handleEvents() || (InventoryCleaner.handleEvents() && InventoryCleaner.highlightUseful)
}