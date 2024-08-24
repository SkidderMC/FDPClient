/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.utils.extensions.darker
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityOnScreen
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawExhiEnchants
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawExhiRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRectBasedBorder
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glColor4f
import org.lwjgl.opengl.GL11.glPushMatrix
import java.awt.Color

class ExhibitionTH(inst: Targets) : TargetStyle("Exhibition", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {
        val font = Fonts.fontSmall
        val minWidth = 126F.coerceAtLeast(47F + font.getStringWidth(entity.name))

        drawExhiRect(0F, 0F, minWidth, 45F, 1F - targetInstance.getFadeProgress())

        drawRect(2.5F, 2.5F, 42.5F, 42.5F, getColor(Color(59, 59, 59)).rgb)
        drawRect(3F, 3F, 42F, 42F, getColor(Color(19, 19, 19)).rgb)

        glColor4f(1f, 1f, 1f, 1f - targetInstance.getFadeProgress())
        drawEntityOnScreen(22, 40, 16, entity)

        font.drawString(entity.name, 46, 5, getColor(-1).rgb)

        val barLength = 70F * (entity.health / entity.maxHealth).coerceIn(0F, 1F)
        drawRect(
            45F,
            14F,
            45F + 70F,
            18F,
            getColor(ColorUtils.getHealthColor(entity.health, entity.maxHealth).darker(0.3F)).rgb
        )
        drawRect(
            45F,
            14F,
            45F + barLength,
            18F,
            getColor(ColorUtils.getHealthColor(entity.health, entity.maxHealth)).rgb
        )

        for (i in 0..9)
            drawRectBasedBorder(45F + i * 7F, 14F, 45F + (i + 1) * 7F, 18F, 0.5F, getColor(Color.black).rgb)

        Fonts.fontSmall.drawString(
            "HP:${entity.health.toInt()} | Dist:${
                mc.thePlayer.getDistanceToEntityBox(
                    entity
                ).toInt()
            }", 45F, 21F, getColor(-1).rgb
        )

        resetColor()
        glPushMatrix()
        glColor4f(1f, 1f, 1f, 1f - targetInstance.getFadeProgress())
        enableRescaleNormal()
        enableBlend()
        tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()

        val renderItem = mc.renderItem

        var x = 45
        val y = 28

        for (index in 3 downTo 0) {
            val stack = entity.inventory[index] ?: continue

            if (stack.item == null)
                continue

            renderItem.renderItemIntoGUI(stack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
            drawExhiEnchants(stack, x.toFloat(), y.toFloat())

            x += 16
        }

        val mainStack = entity.heldItem
        if (mainStack != null && mainStack.item != null) {
            renderItem.renderItemIntoGUI(mainStack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
            drawExhiEnchants(mainStack, x.toFloat(), y.toFloat())
        }

        RenderHelper.disableStandardItemLighting()
        disableRescaleNormal()
        enableAlpha()
        disableBlend()
        disableLighting()
        disableCull()
        GL11.glPopMatrix()
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(0F, 0F, 126F, 45F)

        val font = Fonts.fontSmall
        val minWidth = 126F.coerceAtLeast(47F + font.getStringWidth(entity.name))

        return Border(0F, 0F, minWidth, 45F)
    }
}