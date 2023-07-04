/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import java.util.*
import kotlin.math.abs

@ModuleInfo(name = "DamageParticle", category = ModuleCategory.RENDER)
object DamageParticle : Module() {

    private val aliveTicksValue = IntegerValue("AliveTicks", 20, 10, 50)
    private val sizeValue = IntegerValue("Size", 3, 1, 7)
    private val colourValue = ListValue("ColourType", arrayOf("Damage", "Custom", "Rainbow"), "Custom")
    private val colorRedValue = IntegerValue("Red", 68, 0, 255).displayable { colourValue.get() == "Custom" }
    private val colorGreenValue = IntegerValue("Green", 117, 0, 255).displayable { colourValue.get() == "Custom" }
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255).displayable { colourValue.get() == "Custom" }
    private val colorAlphaValue = IntegerValue("Alpha", 100, 0, 255)
    private val shadowValue = ListValue("Mode", arrayOf("LB", "Default", "Autumn", "Outline", "None"), "Outline").displayable { colourValue.get() != "Damage" }
    private val healthData = mutableMapOf<Int, Float>()
    private val particles = mutableListOf<SingleParticle>()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(particles) {
            for(entity in mc.theWorld.loadedEntityList) {
                if(entity is EntityLivingBase && EntityUtils.isSelected(entity,true)) {
                    val lastHealth = healthData.getOrDefault(entity.entityId,entity.maxHealth)
                    healthData[entity.entityId] = entity.health
                    if(lastHealth == entity.health) continue
                    val colourPrefix = if (colourValue.get() == "Damage") (if(lastHealth>entity.health){"§c"}else{"§a"}) else ""
                    val prefix = if (colourValue.get() != "Rainbow") (if(lastHealth>entity.health){"❤"}else{"❤"}) else (if(lastHealth>entity.health){"-"}else{"+"})
                    particles.add(SingleParticle(colourPrefix + prefix + BigDecimal(abs(lastHealth - entity.health).toDouble()).setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
                        ,entity.posX - 0.5 + Random(System.currentTimeMillis()).nextInt(5).toDouble() * 0.1
                        ,entity.entityBoundingBox.minY + (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) / 2.0
                        ,entity.posZ - 0.5 + Random(System.currentTimeMillis() + 1L).nextInt(5).toDouble() * 0.1)
                    )
                }
            }

            val needRemove = ArrayList<SingleParticle> ()
            for (particle in particles) {
                particle.ticks++
                if (particle.ticks>aliveTicksValue.get()) {
                    needRemove.add(particle)
                }
            }
            for (particle in needRemove) {
                particles.remove(particle)
            }
        }
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        synchronized(particles) {
            val renderManager = mc.renderManager
            val size = sizeValue.get() * 0.01

            for (particle in particles) {
                val n: Double = particle.posX - renderManager.renderPosX
                val n2: Double = particle.posY - renderManager.renderPosY
                val n3: Double = particle.posZ - renderManager.renderPosZ
                GlStateManager.pushMatrix()
                GlStateManager.enablePolygonOffset()
                GlStateManager.doPolygonOffset(1.0f, -1500000.0f)
                GlStateManager.translate(n.toFloat(), n2.toFloat(), n3.toFloat())
                GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                val textY = if (mc.gameSettings.thirdPersonView == 2) { -1.0f } else { 1.0f }

                GlStateManager.rotate(renderManager.playerViewX, textY, 0.0f, 0.0f)
                GlStateManager.scale(-size, -size, size)
                GL11.glDepthMask(false)
                val x = -(mc.fontRendererObj.getStringWidth(particle.str) / 2)
                val y = -(mc.fontRendererObj.FONT_HEIGHT - 1)
                if(colourValue.get() != "Damage"){
                    when (shadowValue.get()) {
                        "LB" -> {mc.fontRendererObj.drawString(particle.str, (x + 1), (y + 1), Color(0, 0, 0, 150).rgb)}
                        "Autumn" -> {mc.fontRendererObj.drawString(particle.str, (x + 1), (y + 1), Color(20, 20, 20, 150).rgb)}
                        "Default" -> {mc.fontRendererObj.drawString(particle.str, (x + 0.5f), (y + 0.5f), Color(0, 0, 0, 130).rgb, false)}
                        "Outline" -> {
                            mc.fontRendererObj.drawString(particle.str, x + 0.5F, y + 0.5F, Color(0, 0, 0, 130).rgb, false)
                            mc.fontRendererObj.drawString(particle.str, x - 0.5F, y - 0.5F, Color(0, 0, 0, 130).rgb, false)
                            mc.fontRendererObj.drawString(particle.str, x + 0.5F, y - 0.5F, Color(0, 0, 0, 130).rgb, false)
                            mc.fontRendererObj.drawString(particle.str, x - 0.5F, y + 0.5F, Color(0, 0, 0, 130).rgb, false)
                        }
                    }
                }
                mc.fontRendererObj.drawString(particle.str, (-(mc.fontRendererObj.getStringWidth(particle.str) / 2)).toFloat(), (-(mc.fontRendererObj.FONT_HEIGHT - 1)).toFloat(), (if (colourValue.get() == "Rainbow") ColorUtils.rainbowWithAlpha(colorAlphaValue.get()) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())).rgb, false)
                GL11.glColor4f(187.0f, 255.0f, 255.0f, 1.0f)
                GL11.glDepthMask(true)
                GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
                GlStateManager.disablePolygonOffset()
                GlStateManager.resetColor()
                GlStateManager.popMatrix()
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        particles.clear()
        healthData.clear()
    }
}

class SingleParticle(val str: String, val posX: Double, val posY: Double, val posZ: Double) {
    var ticks = 0
}
