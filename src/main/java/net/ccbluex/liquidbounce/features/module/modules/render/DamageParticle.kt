/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
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
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import java.util.*
import kotlin.math.abs

@ModuleInfo(name = "DamageParticle", category = ModuleCategory.RENDER)
class DamageParticle : Module() {
    private val healthData = HashMap<Int, Float> ()
    private val particles = ArrayList<SingleParticle> ()

    private val aliveTicks = IntegerValue("AliveTicks", 20, 10, 50)
    private val sizeValue = IntegerValue("Size", 3, 1, 7)
    private val colorRedValue = IntegerValue("Red", 68, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 117, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 100, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(particles ) {
            for(entity in mc.theWorld.loadedEntityList) {
                if(entity is EntityLivingBase && EntityUtils.isSelected(entity,true)) {
                    val lastHealth = healthData.getOrDefault(entity.entityId,entity.maxHealth)
                    healthData[entity.entityId] = entity.health
                    if(lastHealth == entity.health) continue

                    val prefix = if (!colorRainbow.get()) (if(lastHealth>entity.health){"§c❤"}else{"§a§l❤"}) else (if(lastHealth>entity.health){"-"}else{"+"})
                    particles.add(SingleParticle(prefix+BigDecimal(abs(lastHealth-entity.health).toDouble()).setScale(1,BigDecimal.ROUND_HALF_UP).toDouble()
                        ,entity.posX - 0.5 + Random(System.currentTimeMillis()).nextInt(5).toDouble() * 0.1
                        ,entity.entityBoundingBox.minY + (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) / 2.0
                        ,entity.posZ - 0.5 + Random(System.currentTimeMillis() + 1L).nextInt(5).toDouble() * 0.1)
                    )
                }
            }

            val needRemove = ArrayList<SingleParticle> ()
            for (particle in particles) {
                particle.ticks++
                if (particle.ticks>aliveTicks.get()) {
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
                mc.fontRendererObj.drawStringWithShadow(
                    particle.str,
                    (-(mc.fontRendererObj.getStringWidth(particle.str) / 2)).toFloat(),
                    (-(mc.fontRendererObj.FONT_HEIGHT - 1)).toFloat(),
                    (if (colorRainbow.get()) ColorUtils.rainbowWithAlpha(colorAlphaValue.get()) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())).rgb
                )
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
