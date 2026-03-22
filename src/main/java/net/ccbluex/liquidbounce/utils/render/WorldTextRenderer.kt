package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.interpolatedPosition
import net.ccbluex.liquidbounce.utils.extensions.lastTickPos
import net.ccbluex.liquidbounce.utils.extensions.minus
import net.ccbluex.liquidbounce.utils.extensions.renderPos
import net.minecraft.client.gui.FontRenderer
import net.minecraft.entity.Entity
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.GL_ENABLE_BIT
import org.lwjgl.opengl.GL11.GL_LIGHTING
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glPopAttrib
import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushAttrib
import org.lwjgl.opengl.GL11.glPushMatrix
import org.lwjgl.opengl.GL11.glRotatef
import org.lwjgl.opengl.GL11.glScalef
import org.lwjgl.opengl.GL11.glTranslated

fun renderWorldText(
    entity: Entity,
    text: String,
    fontRenderer: FontRenderer,
    color: Int,
    shadow: Boolean,
    scaleMultiplier: Float,
    yOffset: Double = 0.0,
) {
    val player = MinecraftInstance.mc.thePlayer ?: return
    val renderManager = MinecraftInstance.mc.renderManager
    val rotateX = if (MinecraftInstance.mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f

    glPushAttrib(GL_ENABLE_BIT)
    glPushMatrix()

    val interpolatedPosition = entity.interpolatedPosition(entity.lastTickPos) - renderManager.renderPos

    glTranslated(interpolatedPosition.xCoord, interpolatedPosition.yCoord + yOffset, interpolatedPosition.zCoord)
    glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
    glRotatef(renderManager.playerViewX * rotateX, 1F, 0F, 0F)

    RenderUtils.disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)
    RenderUtils.enableGlCap(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    val scaledDistance = ((player.getDistanceToEntity(entity) / 4F).coerceAtLeast(1F) / 150F) * scaleMultiplier
    glScalef(-scaledDistance, -scaledDistance, scaledDistance)

    val width = fontRenderer.getStringWidth(text) * 0.5f
    fontRenderer.drawString(
        text,
        1F - width,
        if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F,
        color,
        shadow,
    )

    RenderUtils.resetCaps()
    glPopMatrix()
    glPopAttrib()
}
