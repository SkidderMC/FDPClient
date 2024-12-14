/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.interpolatedPosition
import net.ccbluex.liquidbounce.utils.extensions.prevPos
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawDome
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.choices
import net.ccbluex.liquidbounce.config.float
import net.minecraft.entity.item.EntityTNTPrimed
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object TNTESP : Module("TNTESP", Category.VISUAL, spacedName = "TNT ESP", hideModule = false) {

    private val dangerZoneDome by boolean("DangerZoneDome", false)
    private val mode by choices("Mode", arrayOf("Lines", "Triangles", "Filled"), "Lines") { dangerZoneDome }
    private val lineWidth by float("LineWidth", 1F, 0.5F..5F) { mode == "Lines" }
    private val rainbow by boolean("Rainbow", false) { dangerZoneDome }
    private val colors = ColorSettingsInteger(this, "Dome", alphaApply = { dangerZoneDome })
    { !rainbow && dangerZoneDome }

    private val renderModes = mapOf("Lines" to GL_LINES, "Triangles" to GL_TRIANGLES, "Filled" to GL_QUADS)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val renderMode = renderModes[mode] ?: return
        val color = if (rainbow) ColorUtils.rainbow().withAlpha(colors.color().alpha) else colors.color()

        val width = lineWidth.takeIf { mode == "Lines" }

        mc.theWorld.loadedEntityList.forEach {
            if (it !is EntityTNTPrimed) return@forEach

            if (dangerZoneDome) {
                drawDome(it.interpolatedPosition(it.prevPos), 8.0, 8.0, width, color, renderMode)
            }

            drawEntityBox(it, Color.RED, false)
        }
    }
}