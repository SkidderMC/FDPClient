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
import net.ccbluex.liquidbounce.utils.render.RenderUtils.disableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.enableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetCaps
import net.minecraft.entity.item.EntityTNTPrimed
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.pow

object TNTTimer : Module("TNTTimer", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, spacedName = "TNT Timer") {

    private val scale by float("Scale", 3F, 1F..4F)
    private val font by font("Font", Fonts.fontSemibold40)
    private val fontShadow by boolean("Shadow", true)

    private val color by color("Color", Color.WHITE)

    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200).onChanged { value ->
        maxRenderDistanceSq = value.toDouble().pow(2)
    }

    private val onLook by boolean("OnLook", false)
    private val maxAngleDifference by float("MaxAngleDifference", 5.0f, 5.0f..90f) { onLook }

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2) else value
        }

    private val tntEntities by EntityLookup<EntityTNTPrimed>()
        .filter { it.fuse > 0 }
        .filter { mc.thePlayer.getDistanceSqToEntity(it) <= maxRenderDistanceSq }
        .filter { !onLook || mc.thePlayer.isLookingOnEntity(it, maxAngleDifference.toDouble()) }

    val onRender3D = handler<Render3DEvent> {
        for (entity in tntEntities) {
            renderTNTTimer(entity, entity.fuse / 5)
        }
    }

    private fun renderTNTTimer(tnt: EntityTNTPrimed, timeRemaining: Int) {
        val thePlayer = mc.thePlayer ?: return

        val renderManager = mc.renderManager
        val rotateX = if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f

        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()

        val (x, y, z) = tnt.interpolatedPosition(tnt.lastTickPos) - renderManager.renderPos

        // Translate to TNT position
        glTranslated(x, y + 1.5f, z)

        glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(renderManager.playerViewX * rotateX, 1F, 0F, 0F)

        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val text = "TNT Explodes in: $timeRemaining"

        val fontRenderer = font

        // Scale
        val scale = ((thePlayer.getDistanceToEntity(tnt) / 4F).coerceAtLeast(1F) / 150F) * scale
        glScalef(-scale, -scale, scale)

        // Draw text
        val width = fontRenderer.getStringWidth(text) * 0.5f
        fontRenderer.drawString(
            text, 1F + -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, color.rgb, fontShadow
        )

        resetCaps()
        glPopMatrix()
        glPopAttrib()
    }

}
