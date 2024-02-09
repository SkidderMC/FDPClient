/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

class RavenB4TH(inst: Targets) : TargetStyle("RavenB4", inst, true) {
    private val rm = BoolValue("Modern", true).displayable { targetInstance.styleValue.get().equals("ravenb4", true) }
    override fun drawTarget(entity: EntityLivingBase) {
        if (!rm.get()) {
            val font = mc.fontRendererObj
            val healthString = decimalFormat2.format(entity.health)
            val length = font.getStringWidth(entity.displayName.formattedText)
            GlStateManager.pushMatrix()
            RenderUtils.drawRect(0F, 0F, 60F + length, 28F, Color(0, 0, 0, fadeAlpha(100)).rgb)
            GlStateManager.enableBlend()
            font.drawStringWithShadow(
                "Target: " + entity.displayName.formattedText + if (entity.health < mc.thePlayer.health) " §AW§F" else " §CL§F",
                4F,
                3F,
                Color(255, 255, 255, fadeAlpha(255)).rgb
            )
            font.drawStringWithShadow("Health: ", 4.3f, 16F, Color(255, 255, 255, fadeAlpha(255)).rgb)
            font.drawStringWithShadow(
                healthString,
                42f,
                16F,
                ColorUtils.reAlpha(BlendUtils.getHealthColor(entity.health, entity.maxHealth), fadeAlpha(255)).rgb
            )
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            RenderUtils.drawRect(0F, 28f, 1F, 0F, Color(0, 0, 0, fadeAlpha(255)))
            RenderUtils.drawRect(
                0F,
                28f,
                1F,
                (entity.maxHealth - entity.health),
                ColorUtils.reAlpha(BlendUtils.getHealthColor(entity.health, entity.maxHealth), fadeAlpha(255)).rgb
            )
            GlStateManager.popMatrix()
        } else {
            val font = Fonts.minecraftFont
            val hp = decimalFormat2.format(entity.health)
            val hplength = font.getStringWidth(decimalFormat2.format(entity.health))
            val length = font.getStringWidth(entity.displayName.formattedText)
            GlStateManager.pushMatrix()
            updateAnim(entity.health)
            RenderUtils.drawRoundedGradientOutlineCorner(
                0F,
                0F,
                length + hplength + 23F,
                35F,
                2F, 8F,
                ClientTheme.setColor("START", fadeAlpha(255)).rgb,
                ClientTheme.setColor("END", fadeAlpha(255)).rgb
            )
            RenderUtils.drawRoundedRect(0F, 0F, length + hplength + 23F, 35F, 4F, Color(0, 0, 0, fadeAlpha(100)).rgb)
            GlStateManager.enableBlend()
            font.drawStringWithShadow(
                entity.displayName.formattedText,
                6F,
                8F,
                Color(255, 255, 255, fadeAlpha(255)).rgb
            )
            font.drawStringWithShadow(
                if (entity.health > mc.thePlayer.health) "L" else "W",
                length + hplength + 11.6F,
                8F,
                if (entity.health > mc.thePlayer.health) Color(255, 0, 0, fadeAlpha(255)).rgb else Color(
                    0,
                    255,
                    0,
                    fadeAlpha(255)
                ).rgb
            )
            font.drawStringWithShadow(
                hp,
                length + 8F,
                8F,
                ColorUtils.reAlpha(BlendUtils.getHealthColor(entity.health, entity.maxHealth), fadeAlpha(255)).rgb
            )
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            RenderUtils.drawRoundedRect(
                5.0F,
                29.55F,
                length + hplength + 18F,
                25F,
                2F,
                Color(0, 0, 0, fadeAlpha(110)).rgb,
            )
            RenderUtils.drawRoundedGradientRectCorner(
                5F,
                25F,
                8F + (entity.health / 20) * (length + hplength + 10F),
                29.5F,
                4F,
                ClientTheme.setColor("START", fadeAlpha(255)).rgb,
                ClientTheme.setColor("END", fadeAlpha(255)).rgb
            )
            GlStateManager.popMatrix()
        }
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(0F, 0F, if (rm.get())40F + mc.fontRendererObj.getStringWidth(entity!!.displayName.formattedText) else 60F + mc.fontRendererObj.getStringWidth(entity!!.displayName.formattedText), if (rm.get()) 35F else 28F)
    }
}