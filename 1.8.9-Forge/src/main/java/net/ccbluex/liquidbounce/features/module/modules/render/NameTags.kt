/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.*
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.floor
import kotlin.math.roundToInt

@ModuleInfo(name = "NameTags", description = "Changes the scale of the nametags so you can always read them.", category = ModuleCategory.RENDER)
class NameTags : Module() {
    private val healthValue = BoolValue("Health", true)
    private val pingValue = BoolValue("Ping", true)
    private val distanceValue = BoolValue("Distance", false)
    private val armorValue = BoolValue("Armor", true)
    private val clearNamesValue = BoolValue("ClearNames", false)
    private val fontValue = FontValue("Font", Fonts.font40)
    private val borderValue = BoolValue("Border", true)
    private val jelloValue = BoolValue("Jello", true)
    private val jelloColorValue = BoolValue("JelloHPColor", true)
    private val jelloAlphaValue = IntegerValue("JelloAlpha", 170, 0, 255)
    private val scaleValue = FloatValue("Scale", 1F, 1F, 4F)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for(entity in mc.theWorld.loadedEntityList) {
            if(EntityUtils.isSelected(entity, false)) {
                renderNameTag(
                    entity as EntityLivingBase,
                    if (clearNamesValue.get())
                        entity.name
                    else
                        entity.getDisplayName().unformattedText
                )
            }
        }
    }

    private fun renderNameTag(entity: EntityLivingBase, tag: String) {
        // Set fontrenderer local
        val fontRenderer = fontValue.get()

        // Modify tag
        val bot = AntiBot.isBot(entity)
        val nameColor = if (bot) "§3" else if (entity.isInvisible) "§6" else if (entity.isSneaking) "§4" else "§7"
        val ping = if (entity is EntityPlayer) EntityUtils.getPing(entity) else 0

        val distanceText = if (distanceValue.get()) "§7${mc.thePlayer.getDistanceToEntity(entity).roundToInt()}m " else ""
        val pingText = if (pingValue.get() && entity is EntityPlayer) (if (ping > 200) "§c" else if (ping > 100) "§e" else "§a") + ping + "ms §7" else ""
        val healthText = if (healthValue.get()) "§7§c " + entity.health.toInt() + " HP" else ""
        val botText = if (bot) " §c§lBot" else ""

        val text = "$distanceText$pingText$nameColor$tag$healthText$botText"

        // Push
        glPushMatrix()

        // Translate to player position
        val renderManager = mc.renderManager
        val timer = mc.timer

        glTranslated( // Translate to player position with render pos and interpolate it
                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY + entity.eyeHeight.toDouble() + 0.55,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
        )

        // Rotate view to player
        glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)

        // Scale
        var distance = mc.thePlayer.getDistanceToEntity(entity) / 4F

        if (distance < 1F)
            distance = 1F

        val scale = distance / 100F * scaleValue.get()

        // Disable lightning and depth test
        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        // Enable blend
        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)


        AWTFontRenderer.assumeNonVolatile = true

        // Draw nametag
        if(jelloValue.get()){
            //colors
            var hpBarColor=jelloColor(255,255,255)
            val name=entity.displayName.unformattedText
            if(jelloColorValue.get() && name.startsWith("§")){
                when(name.substring(1,2).toLowerCase()){
                    "0" -> {
                        hpBarColor=jelloColor(0,0,0)
                    }
                    "1" -> {
                        hpBarColor=jelloColor(0,0,170)
                    }
                    "2" -> {
                        hpBarColor=jelloColor(0,170,0)
                    }
                    "3" -> {
                        hpBarColor=jelloColor(0,170,170)
                    }
                    "4" -> {
                        hpBarColor=jelloColor(170,0,0)
                    }
                    "5" -> {
                        hpBarColor=jelloColor(170,0,170)
                    }
                    "6" -> {
                        hpBarColor=jelloColor(255,170,0)
                    }
                    "7" -> {
                        hpBarColor=jelloColor(170,170,170)
                    }
                    "8" -> {
                        hpBarColor=jelloColor(85,85,85)
                    }
                    "9" -> {
                        hpBarColor=jelloColor(85,85,255)
                    }
                    "a" -> {
                        hpBarColor=jelloColor(85,255,85)
                    }
                    "b" -> {
                        hpBarColor=jelloColor(85,255,255)
                    }
                    "c" -> {
                        hpBarColor=jelloColor(255,85,85)
                    }
                    "d" -> {
                        hpBarColor=jelloColor(255,85,255)
                    }
                    "e" -> {
                        hpBarColor=jelloColor(255,255,85)
                    }
                    "f" -> {
                        hpBarColor=jelloColor(255,255,255)
                    }
                }
            }
            val bgColor=jelloColor(170,170,170)
            val width = fontRenderer.getStringWidth(tag) / 2
            val maxWidth=(width + 4F)-(-width - 4F)
            var healthPercent=entity.health/entity.maxHealth

            //render bg
            glScalef(-scale*2, -scale*2, scale*2)
            drawRect(-width - 4F, -fontRenderer.FONT_HEIGHT*3F, width + 4F, -3F, bgColor)

            //render hp bar
            if(healthPercent>1){
                healthPercent=1F
            }

            drawRect(-width - 4F, -3F, (-width - 4F)+(maxWidth*healthPercent), 1F, hpBarColor)
            drawRect((-width - 4F)+(maxWidth*healthPercent), -3F, width + 4F, 1F, bgColor)

            //string
            fontRenderer.drawString(tag,-width,-fontRenderer.FONT_HEIGHT*2-4,Color.WHITE.rgb)
            glScalef(0.5F,0.5F,0.5F)
            fontRenderer.drawString("Health: "+entity.health.toInt(),-width*2, -fontRenderer.FONT_HEIGHT*2,Color.WHITE.rgb)
        }else {
            glScalef(-scale, -scale, scale)
            val width = fontRenderer.getStringWidth(text) / 2
            if (borderValue.get())
                drawBorderedRect(
                    -width - 2F, -2F, width + 4F, fontRenderer.FONT_HEIGHT + 2F, 2F,
                    Color(255, 255, 255, 90).rgb, Integer.MIN_VALUE
                )
            else
                drawRect(
                    -width - 2F, -2F, width + 4F, fontRenderer.FONT_HEIGHT + 2F,
                    Integer.MIN_VALUE
                )
            fontRenderer.drawString(
                text, 1F + -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F,
                0xFFFFFF, true
            )

            AWTFontRenderer.assumeNonVolatile = false

            if (armorValue.get() && entity is EntityPlayer) {
                for (index in 0..4) {
                    if (entity.getEquipmentInSlot(index) == null)
                        continue

                    mc.renderItem.zLevel = -147F
                    mc.renderItem.renderItemAndEffectIntoGUI(entity.getEquipmentInSlot(index), -50 + index * 20, -22)
                }

                enableAlpha()
                disableBlend()
                enableTexture2D()
            }
        }
        // Reset caps
        resetCaps()

        // Reset color
        resetColor()
        glColor4f(1F, 1F, 1F, 1F)

        // Pop
        glPopMatrix()
    }

    private fun jelloColor(r:Int,g:Int,b:Int):Color{
        return Color(r,g,b,jelloAlphaValue.get())
    }
}
