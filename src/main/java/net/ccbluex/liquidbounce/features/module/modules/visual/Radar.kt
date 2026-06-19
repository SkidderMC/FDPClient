/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.newDrawRect
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Radar draws a small circular minimap in a screen corner that plots nearby
 * players as colored dots. Dots are placed relative to the local player and
 * rotated by the player yaw so the top of the radar always points where you
 * are looking.
 *
 * @author opZywl
 */
object Radar : Module("Radar", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val range by int("Range", 40, 10..120)
    private val size by int("Size", 80, 40..160)
    private val background by boolean("Background", true)
    private val dotColor by color("Dot Color", Color(255, 80, 80))

    val onRender2D = handler<Render2DEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        val sr = ScaledResolution(mc)
        val diameter = size
        val radius = diameter / 2f
        val centerX = sr.scaledWidth - radius - 10f
        val centerY = radius + 10f

        if (background) {
            drawFilledCircle(centerX.toInt(), centerY.toInt(), radius, Color.BLACK.withAlpha(120))
        }
        drawCircle(centerX, centerY, radius, 0, 360)

        val yaw = (-player.rotationYaw) * (PI / 180.0)
        val cos = cos(yaw)
        val sin = sin(yaw)
        val scale = radius / range.toDouble()
        val dotRGB = dotColor.rgb

        for (entity in world.playerEntities) {
            if (entity == null || entity === player) continue

            val dx = entity.posX - player.posX
            val dz = entity.posZ - player.posZ

            var x = (dx * cos - dz * sin) * scale
            var y = (dx * sin + dz * cos) * scale

            val distSq = x * x + y * y
            val limit = (radius - 1.5).toDouble()
            if (distSq > limit * limit) {
                val len = Math.sqrt(distSq)
                x = x / len * limit
                y = y / len * limit
            }

            val dotX = centerX + x
            val dotY = centerY + y

            newDrawRect(dotX - 1.5, dotY - 1.5, dotX + 1.5, dotY + 1.5, dotRGB)
        }
    }
}
