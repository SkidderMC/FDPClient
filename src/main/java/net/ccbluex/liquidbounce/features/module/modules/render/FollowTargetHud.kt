/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.roundToInt

@ModuleInfo(name = "FollowTargetHud", category = ModuleCategory.RENDER)
class FollowTargetHud : Module() {

    private val zoomIn = BoolValue("ZoomIn", true)
    private val zoomTicks = IntegerValue("ZoomInTicks", 4, 2, 15).displayable {zoomIn.get()}
    private val modeValue = ListValue("Mode", arrayOf("Juul", "Jello", "Material", "Material2", "Arris", "FDP"), "Juul")
    private val fontValue = FontValue("Font", Fonts.font40)
    private val materialShadow = BoolValue("MaterialShadow", false).displayable {modeValue.equals("Material") || modeValue.equals("Material2")}
    private val fdpVertical = BoolValue("FDPVertical", false).displayable {modeValue.equals("FDP")}
    private val fdpText = BoolValue("FDPDrawText", true).displayable {modeValue.equals("FDP") && !fdpVertical.get()}
    private val fdpRed = BoolValue("FDPRed", false).displayable {modeValue.equals("FDP")}
    private val smoothMove = BoolValue("SmoothHudMove", true)
    private val smoothValue = FloatValue("SmoothHudMoveValue", 5.2f, 1f, 8f).displayable { smoothMove.get() }
    private val smoothRot = BoolValue("SmoothHudRotations", true)
    private val rotSmoothValue = FloatValue("SmothHudRotationValue", 2.1f, 1f, 6f). displayable {smoothRot.get() }
    private val jelloColorValue = BoolValue("JelloHPColor", true).displayable { modeValue.equals("Jello") }
    private val jelloAlphaValue = IntegerValue("JelloAlpha", 170, 0, 255).displayable { modeValue.equals("Jello") }
    private val scaleValue = FloatValue("Scale", 1F, 1F, 4F)
    private val staticScale = BoolValue("StaticScale", false)
    private val translateY = FloatValue("TanslateY", 0.55F,-2F,2F)
    private val translateX = FloatValue("TranslateX", 0F, -2F, 2F)
    private var xChange = translateX.get() * 20

    private var targetTicks = 0
    private var entityKeep = "yes"
    
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0
    
    private var lastYaw = 0.0f
    private var lastPitch = 0.0f

    companion object {
        val HEALTH_FORMAT = DecimalFormat("#.#")
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if(mc.thePlayer == null)
            return
        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, false)) {
                renderNameTag(entity as EntityLivingBase, entity.name)
            }
        }
    }

    private fun getPlayerName(entity: EntityLivingBase): String {
        val name = entity.displayName.formattedText
        var pre = ""
        val teams = FDPClient.moduleManager[Teams::class.java]!!
        if (FDPClient.fileManager.friendsConfig.isFriend(entity.name)) {
            pre = "$pre§b[Friend] "
        }
        if (teams.isInYourTeam(entity)) {
            pre = "$pre§a[TEAM] "
        }
        if (AntiBot.isBot(entity)) {
            pre = "$pre§e[BOT] "
        }
        if (!AntiBot.isBot(entity) && !teams.isInYourTeam(entity)) {
            pre = if (FDPClient.fileManager.friendsConfig.isFriend(entity.name)) {
                "§b[Friend] §c"
            } else {
                "§c"
            }
        }
        return name + pre
    }

    private fun renderNameTag(entity: EntityLivingBase, tag: String) {
        xChange = translateX.get() * 20

        if (entity != FDPClient.combatManager.target && entity.getName() != entityKeep) {
            return
        } else if ( entity == FDPClient.combatManager.target) {
            entityKeep = entity.getName()
            targetTicks++
            if (targetTicks >= zoomTicks.get() + 2) {
                targetTicks = zoomTicks.get() + 1
            }
        } else if (FDPClient.combatManager.target == null) {
            targetTicks--
            if (targetTicks <= -1) {
                targetTicks = 0
                entityKeep = "dg636 top"
            }
        }

        if (targetTicks == 0) {
            return
        }
        
        // Set fontrenderer local
        val fontRenderer = fontValue.get()
        val font = fontValue.get()

        // Push
        glPushMatrix()

        // Translate to player position
        val renderManager = mc.renderManager
        val timer = mc.timer
        
        if (smoothMove.get()) {
            lastX += ((entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX).toDouble() - lastX) / smoothValue.get().toDouble()          
            lastY += ((entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY + entity.eyeHeight.toDouble() + translateY.get().toDouble()).toDouble() - lastY) / smoothValue.get().toDouble()
            lastZ += ((entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ).toDouble() - lastZ) / smoothValue.get().toDouble()
            
            glTranslated( lastX, lastY, lastZ )
        } else {
            glTranslated( // Translate to player position with render pos and interpolate it
                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY + entity.eyeHeight.toDouble() + translateY.get().toDouble(),
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
            )
        }

        // Rotate view to player
        if (smoothRot.get()) {
            lastYaw += (-mc.renderManager.playerViewY - lastYaw) / rotSmoothValue.get()
            lastPitch += (mc.renderManager.playerViewX - lastPitch) / rotSmoothValue.get()
            
            glRotatef(lastYaw, 0F, 1F, 0F)
            glRotatef(lastPitch, 1F, 0F, 0F)
        } else {
            glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
            glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)
        }

        // Scale
        var distance = mc.thePlayer.getDistanceToEntity(entity) / 4F

        if (distance < 1F)
            distance = 1F
            
        if (staticScale.get())
            distance = 1F
     
        var scale = (distance / 150F) * scaleValue.get()
        if (zoomIn.get()) {
            scale *= (targetTicks.coerceAtMost(zoomTicks.get()) / zoomTicks.get()).toFloat()
        }

        // Disable lightning and depth test
        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        // Enable blend
        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        
        val name = entity.displayName.unformattedText
        var healthPercent = entity.health / entity.maxHealth
        // render hp bar
        if (healthPercent> 1) {
            healthPercent = 1F
        }

        // Draw nametag
        when (modeValue.get().lowercase()) {

          
            "juul" -> {

                // render bg
                glScalef(-scale * 2, -scale * 2, scale * 2)
                drawRoundedCornerRect(-120f + xChange, -16f, -50f + xChange, 10f, 5f, Color(64, 64, 64, 255).rgb)
                drawRoundedCornerRect(-110f + xChange, 0f,   -20f + xChange, 35f, 5f, Color(96, 96, 96, 255).rgb)

                // draw strings
                fontRenderer.drawString("Attacking", -105 + xChange.toInt(), -13, Color.WHITE.rgb)
                fontRenderer.drawString(tag, -106 + xChange.toInt() , 10, Color.WHITE.rgb)
               
                
                val distanceString = "⤢" + ( ( ( mc.thePlayer.getDistanceToEntity(entity) * 10f ).toInt() ).toFloat() * 0.1f ).toString() 
                fontRenderer.drawString(distanceString, -25 - fontRenderer.getStringWidth(distanceString).toInt() + xChange.toInt(), 10, Color.WHITE.rgb)
                
                // draw health bars
                drawRoundedCornerRect(-104f + xChange, 22f, -50f + xChange, 30f, 1f, Color(64, 64, 64, 255).rgb) 
                drawRoundedCornerRect(-104f + xChange, 22f, -104f + (healthPercent * 54) + xChange, 30f, 1f, Color.WHITE.rgb)
                
            }
            
            "material" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                
                // render bg
                if (materialShadow.get()) {
                    drawShadow(-40f + xChange, 0f, 40f + xChange, 29f)
                    drawRect(-40f + xChange, 0f, 40f + xChange, 29f, Color(72, 72, 72, 250).rgb)
                } else {
                    drawRoundedCornerRect(-40f + xChange, 0f, 40f + xChange, 29f, 5f, Color(72, 72, 72, 250).rgb)
                }
                
                // draw health bars
                drawRoundedCornerRect(-35f + xChange, 7f, -35f + (healthPercent * 70) + xChange, 12f, 2f, Color(10, 250, 10, 255).rgb)
                drawRoundedCornerRect(-35f + xChange, 17f, -35f + ((entity.totalArmorValue / 20F) * 70) + xChange, 22f, 2f, Color(10, 10, 250, 255).rgb)
                    
            }
            
            "material2" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                
                // render bg
                if (materialShadow.get()) {
                    drawShadow(-40f + xChange, 0f, 40f + xChange, 15f)
                    drawShadow(-40f + xChange, 0f, 20f + xChange, 35f)

                    drawRect(-40f + xChange, 0f, 40f + xChange, 15f, Color(72, 72, 72, 250).rgb)
                    drawRect(-40f + xChange, 20f, 40f + xChange, 35f, Color(72, 72, 72, 250).rgb)
                } else {
                    drawRoundedCornerRect(-40f + xChange, 0f, 40f + xChange, 15f, 5f, Color(72, 72, 72, 250).rgb)
                    drawRoundedCornerRect(-40f + xChange, 20f, 40f + xChange, 35f, 5f, Color(72, 72, 72, 250).rgb)
                }
                
                // draw health bars
                drawRoundedCornerRect(-35f + xChange, 5f, -35f + (healthPercent * 70) + xChange, 10f, 2f, Color(10, 250, 10, 255).rgb)
                drawRoundedCornerRect(-35f + xChange, 25f, -35f + ((entity.totalArmorValue / 20F) * 70) + xChange, 30f, 2f, Color(10, 10, 250, 255).rgb)
                    
            }
            
            "arris" -> {
                
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val hp = healthPercent
                val additionalWidth = font.getStringWidth("${entity.name}  $hp hp").coerceAtLeast(75)
                drawRoundedCornerRect(xChange, 0f, 45f + additionalWidth + xChange, 40f, 7f, Color(0, 0, 0, 110).rgb)

                // info text
                font.drawString(entity.name, 40 + xChange.toInt(), 5, Color.WHITE.rgb)
                "${HEALTH_FORMAT.format(entity.health)} hp".also {
                    font.drawString(it, 40 + additionalWidth - font.getStringWidth(it) + xChange.toInt(), 5, Color.LIGHT_GRAY.rgb)
                }

                // hp bar
                val yPos = 5 + font.FONT_HEIGHT + 3f
                drawRect(40f + xChange, yPos,     40 + xChange + (healthPercent) * additionalWidth, yPos + 4, Color.GREEN.rgb)
                drawRect(40f + xChange, yPos + 9, 40 + xChange + (entity.totalArmorValue / 20F) * additionalWidth, yPos + 13, Color(77, 128, 255).rgb)   
            }
            
            "fdp" -> {
                
                val font = fontValue.get()
                glScalef(-scale * 2, -scale * 2, scale * 2)
                
                if (!fdpVertical.get()) {
                    var addedLen = (60 + font.getStringWidth(entity.name) * 1.60f).toFloat()
                    if (!fdpText.get()) addedLen = 110f
                    
                    if (fdpRed.get()) {
                        RenderUtils.drawRect(0f + xChange, 0f, addedLen + xChange, 47f, Color(212, 63, 63, 90).rgb)
                        RenderUtils.drawRoundedCornerRect(0f + xChange, 0f, healthPercent * addedLen + xChange, 47f, 3f, Color(245, 52, 27, 90).rgb)
                    } else {
                        RenderUtils.drawRect(0f + xChange, 0f, addedLen + xChange, 47f, Color(0, 0, 0, 120).rgb)
                        RenderUtils.drawRoundedCornerRect(0f + xChange, 0f, healthPercent * addedLen + xChange, 47f, 3f, Color(0, 0, 0, 90).rgb)
                    }

                    drawShadow(0f, 0f, addedLen + xChange, 47f)
        
                    if (fdpText.get()) {
        
                        fontRenderer.drawString(entity.name, 45 + xChange.toInt(), 8, Color.WHITE.rgb)
                        fontRenderer.drawString("Health ${entity.health.roundToInt()}", 45 + xChange.toInt(), 11 + (font.FONT_HEIGHT).toInt(), Color.WHITE.rgb)
                    }
                } else {
                    if (fdpRed.get()) {
                        RenderUtils.drawRect(0f + xChange, 0f, 47f + xChange, 120f + xChange, Color(212, 63, 63, 90).rgb)
                        RenderUtils.drawRoundedCornerRect(healthPercent*120f + xChange, 0f, 47f + xChange, 0f, 3f, Color(245, 52, 27, 90).rgb)
                    } else {
                        RenderUtils.drawRect(0f + xChange, 0f, 47f + xChange, 120f, Color(0, 0, 0, 120).rgb)
                        RenderUtils.drawRoundedCornerRect(0f + xChange, 0f, 47f + xChange, healthPercent * 120f, 3f, Color(0, 0, 0, 90).rgb)
                    }
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
                val width = fontRenderer.getStringWidth(tag)
                val maxWidth = (width + 4F) - (-width - 4F)
                var healthPercent = entity.health / entity.maxHealth

                // render bg
                glScalef(-scale * 2, -scale * 2, scale * 2)
                drawRect(xChange, -fontRenderer.FONT_HEIGHT * 3F, width + 8F + xChange, -3F, bgColor)

                // render hp bar
                if (healthPercent> 1) {
                    healthPercent = 1F
                }

                drawRect(xChange,                            -3F, maxWidth * healthPercent + xChange, 1F, hpBarColor)
                drawRect(maxWidth * healthPercent + xChange, -3F, width + 8F + xChange,               1F, bgColor)

                // string
                fontRenderer.drawString(tag, 4 + xChange.toInt(), -fontRenderer.FONT_HEIGHT * 2 - 4, Color.WHITE.rgb)
                glScalef(0.5F, 0.5F, 0.5F)
                fontRenderer.drawString("Health: " + entity.health.toInt(), 4 + xChange.toInt(), -fontRenderer.FONT_HEIGHT * 2, Color.WHITE.rgb)
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
