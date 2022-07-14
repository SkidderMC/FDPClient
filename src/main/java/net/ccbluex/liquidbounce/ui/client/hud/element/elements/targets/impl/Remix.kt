/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color

class Remix(inst: Targets): TargetStyle("Remix", inst, false) {

    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)

        // background
        RenderUtils.newDrawRect(0F, 0F, 146F, 49F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(1F, 1F, 145F, 48F, getColor(Color(35, 35, 35)).rgb)

        // health bar
        RenderUtils.newDrawRect(4F, 40F, 142F, 45F, getColor(Color.red.darker().darker()).rgb)
        RenderUtils.newDrawRect(4F, 40F, 4F + (easingHealth / entity.maxHealth).coerceIn(0F, 1F) * 138F, 45F, targetInstance.barColor.rgb)

        // head
        RenderUtils.newDrawRect(4F, 4F, 38F, 38F, getColor(Color(150, 150, 150)).rgb)
        RenderUtils.newDrawRect(5F, 5F, 37F, 37F, getColor(Color(0, 0, 0)).rgb)

        // armor bar
        RenderUtils.newDrawRect(40F, 36F, 141.5F, 38F, getColor(Color.blue.darker()).rgb)
        RenderUtils.newDrawRect(40F, 36F, 40F + (entity.getTotalArmorValue().toFloat() / 20F).coerceIn(0F, 1F) * 101.5F, 38F, getColor(Color.blue).rgb)

        // armor item background
        RenderUtils.newDrawRect(40F, 16F, 58F, 34F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(41F, 17F, 57F, 33F, getColor(Color(95, 95, 95)).rgb)

        RenderUtils.newDrawRect(60F, 16F, 78F, 34F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(61F, 17F, 77F, 33F, getColor(Color(95, 95, 95)).rgb)

        RenderUtils.newDrawRect(80F, 16F, 98F, 34F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(81F, 17F, 97F, 33F, getColor(Color(95, 95, 95)).rgb)

        RenderUtils.newDrawRect(100F, 16F, 118F, 34F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(101F, 17F, 117F, 33F, getColor(Color(95, 95, 95)).rgb)

        // name
        Fonts.minecraftFont.drawStringWithShadow(entity.name, 41F, 5F, getColor(-1).rgb)

        // ping
        if (mc.netHandler.getPlayerInfo(entity.uniqueID) != null) {
            // actual head
            drawHead(mc.netHandler.getPlayerInfo(entity.uniqueID).locationSkin, 5, 5, 32, 32, 1F - targetInstance.getFadeProgress())

            val responseTime = mc.netHandler.getPlayerInfo(entity.uniqueID).responseTime.toInt()
            val stringTime = "${responseTime.coerceAtLeast(0)}ms"

            var j = 0

            if (responseTime < 0)
                j = 5
            else if (responseTime < 150)
                j = 0
            else if (responseTime < 300)
                j = 1
            else if (responseTime < 600)
                j = 2
            else if (responseTime < 1000)
                j = 3
            else
                j = 4

            mc.textureManager.bindTexture(Gui.icons)
            RenderUtils.drawTexturedModalRect(132, 18, 0, 176 + j * 8, 10, 8, 100.0F)

            GL11.glPushMatrix()
            GL11.glTranslatef(142F - Fonts.minecraftFont.getStringWidth(stringTime) / 2F, 28F, 0F)
            GL11.glScalef(0.5F, 0.5F, 0.5F)
            Fonts.minecraftFont.drawStringWithShadow(stringTime, 0F, 0F, getColor(-1).rgb)
            GL11.glPopMatrix()
        }

        // armor items
        GL11.glPushMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f - targetInstance.getFadeProgress())
        RenderHelper.enableGUIStandardItemLighting()

        val renderItem = mc.renderItem

        var x = 41
        var y = 17

        for (index in 3 downTo 0) {
            val stack = entity.inventory.armorInventory[index] ?: continue

            if (stack.getItem() == null)
                continue

            renderItem.renderItemAndEffectIntoGUI(stack, x, y)
            x += 20
        }

        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 146F, 49F)
    }

}