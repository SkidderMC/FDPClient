/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.features.module.modules.player.HackerDetector
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.ping
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.*
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo(name = "NameTags", category = ModuleCategory.RENDER)
class NameTags : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Simple", "Liquid", "Jello"), "Simple")
    private val healthValue = BoolValue("Health", true)
    private val pingValue = BoolValue("Ping", true)
    private val distanceValue = BoolValue("Distance", false)
    private val armorValue = BoolValue("Armor", true)
    private val clearNamesValue = BoolValue("ClearNames", true)
    private val fontValue = FontValue("Font", Fonts.font40)
    private val borderValue = BoolValue("Border", true)
    private val hackerValue = BoolValue("Hacker", true)
    private val jelloColorValue = BoolValue("JelloHPColor", true).displayable { modeValue.equals("Jello") }
    private val jelloAlphaValue = IntegerValue("JelloAlpha", 170, 0, 255).displayable { modeValue.equals("Jello") }
    private val scaleValue = FloatValue("Scale", 1F, 1F, 4F)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, false)) {
                renderNameTag(entity as EntityLivingBase,
                    if (hackerValue.get() && LiquidBounce.moduleManager[HackerDetector::class.java]!!.isHacker(entity)) { "§c" } else { "" } + if (!modeValue.equals("Liquid") && AntiBot.isBot(entity)) { "§e" } else { "" } +
                            if (clearNamesValue.get()) { entity.name } else { entity.getDisplayName().unformattedText })
            }
        }
    }

    private fun getPlayerName(entity: EntityLivingBase): String {
        val name = entity.displayName.formattedText
        var pre = ""
        val teams = LiquidBounce.moduleManager[Teams::class.java]!!
        if (LiquidBounce.fileManager.friendsConfig.isFriend(entity.name)) {
            pre = "$pre§b[Friend] "
        }
        if (teams.isInYourTeam(entity)) {
            pre = "$pre§a[TEAM] "
        }
        if (AntiBot.isBot(entity)) {
            pre = "$pre§e[BOT] "
        }
        if (!AntiBot.isBot(entity) && !teams.isInYourTeam(entity)) {
            pre = if (LiquidBounce.fileManager.friendsConfig.isFriend(entity.name)) {
                "§b[Friend] §c"
            } else {
                "§c"
            }
        }
        return name + pre
    }

    private fun renderNameTag(entity: EntityLivingBase, tag: String) {
        // Set fontrenderer local
        val fontRenderer = fontValue.get()

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

        if (distance < 1F) {
            distance = 1F
        }

        val scale = (distance / 150F) * scaleValue.get()

        // Disable lightning and depth test
        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        // Enable blend
        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Draw nametag
        when (modeValue.get().lowercase()) {
            "simple" -> {
                val healthPercent = (entity.health / entity.maxHealth).coerceAtMost(1F)
                val width = fontRenderer.getStringWidth(tag).coerceAtLeast(30) / 2
                val maxWidth = width * 2 + 12F

                glScalef(-scale * 2, -scale * 2, scale * 2)
                drawRect(-width - 6F, -fontRenderer.FONT_HEIGHT * 1.7F, width + 6F, -2F, Color(0, 0, 0, jelloAlphaValue.get()))
                drawRect(-width - 6F, -2F, -width - 6F + (maxWidth * healthPercent), 0F, ColorUtils.healthColor(entity.health, entity.maxHealth, jelloAlphaValue.get()))
                drawRect(-width - 6F + (maxWidth * healthPercent), -2F, width + 6F, 0F, Color(0, 0, 0, jelloAlphaValue.get()))
                fontRenderer.drawString(tag, (-fontRenderer.getStringWidth(tag) * 0.5F).toInt(), (-fontRenderer.FONT_HEIGHT * 1.4F).toInt(), Color.WHITE.rgb)
            }

            "liquid" -> {
                // Modify tag
                val bot = AntiBot.isBot(entity)
                val nameColor = if (bot) "§3" else if (entity.isInvisible) "§6" else if (entity.isSneaking) "§4" else "§7"
                val ping = entity.ping

                val distanceText = if (distanceValue.get()) "§7 [§a${mc.thePlayer.getDistanceToEntity(entity).roundToInt()}§7]" else ""
                val pingText = if (pingValue.get() && entity is EntityPlayer) (if (ping > 200) "§c" else if (ping > 100) "§e" else "§a") + ping + "ms §7" else ""
                val healthText = if (healthValue.get()) "§7 [§f" + entity.health.toInt() + "§c❤§7]" else ""
                val botText = if (bot) " §7[§6§lBot§7]" else ""

                val text = "$distanceText$pingText$nameColor$tag$healthText$botText"

                glScalef(-scale, -scale, scale)
                val width = fontRenderer.getStringWidth(text) / 2
                if (borderValue.get()) {
                    drawBorderedRect(-width - 2F, -2F, width + 4F, fontRenderer.FONT_HEIGHT + 2F, 2F, Color(255, 255, 255, 90).rgb, Integer.MIN_VALUE)
                } else {
                    drawRect(-width - 2F, -2F, width + 4F, fontRenderer.FONT_HEIGHT + 2F, Integer.MIN_VALUE)
                }

                fontRenderer.drawString(text, 1F + -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, 0xFFFFFF, true)

                if (armorValue.get() && entity is EntityPlayer) {
                    for (index in 0..4) {
                        if (entity.getEquipmentInSlot(index) == null) {
                            continue
                        }

                        mc.renderItem.zLevel = -147F
                        mc.renderItem.renderItemAndEffectIntoGUI(entity.getEquipmentInSlot(index), -50 + index * 20, -22)
                    }

                    enableAlpha()
                    disableBlend()
                    enableTexture2D()
                }
            }

            "jello" -> {
                // colors
                var hpBarColor = Color(255, 255, 255, jelloAlphaValue.get())
                val name = entity.displayName.unformattedText
                if (jelloColorValue.get() && name.startsWith("§")) {
                    hpBarColor = ColorUtils.colorCode(name.substring(1, 2), jelloAlphaValue.get())
                }
                val bgColor = Color(50, 50, 50, jelloAlphaValue.get())
                val width = fontRenderer.getStringWidth(tag) / 2
                val maxWidth = (width + 4F) - (-width - 4F)
                var healthPercent = entity.health / entity.maxHealth

                // render bg
                glScalef(-scale * 2, -scale * 2, scale * 2)
                drawRect(-width - 4F, -fontRenderer.FONT_HEIGHT * 3F, width + 4F, -3F, bgColor)

                // render hp bar
                if (healthPercent> 1) {
                    healthPercent = 1F
                }

                drawRect(-width - 4F, -3F, (-width - 4F) + (maxWidth * healthPercent), 1F, hpBarColor)
                drawRect((-width - 4F) + (maxWidth * healthPercent), -3F, width + 4F, 1F, bgColor)

                // string
                fontRenderer.drawString(tag, -width, -fontRenderer.FONT_HEIGHT * 2 - 4, Color.WHITE.rgb)
                glScalef(0.5F, 0.5F, 0.5F)
                fontRenderer.drawString("Health: " + entity.health.toInt(), -width * 2, -fontRenderer.FONT_HEIGHT * 2, Color.WHITE.rgb)
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
}
