/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.random.Random

object DamageParticle : Module("DamageParticle", Category.VISUAL) {

    private val aliveTicks by int("AliveTicks", 50, 10..50)
    private val size by int("Size", 3, 1..7)
    private val colorMode by choices("ColourType", arrayOf("Damage", "Custom", "Client"), "Damage")
    private val customColor by color("Color", Color.WHITE) { colorMode == "Custom" }
    private val shadowMode by choices("Shadow", arrayOf("Normal", "Default", "Vanilla", "Outline", "None"), "Outline") { colorMode != "Damage" }

    private val healthData = mutableMapOf<Int, Float>()
    private val particles = mutableListOf<SingleParticle>()

    val onUpdate = handler<UpdateEvent> {
        synchronized(particles) {
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityLivingBase && EntityUtils.isSelected(entity, true)) {
                    val lastHealth = healthData.getOrDefault(entity.entityId, entity.maxHealth)
                    healthData[entity.entityId] = entity.health
                    if (lastHealth == entity.health) continue

                    val colorPrefix = when (colorMode) {
                        "Damage" -> if (lastHealth > entity.health) "§c" else "§a"
                        else -> ""
                    }

                    val prefix = when (colorMode) {
                        "Client" -> if (lastHealth > entity.health) "-" else "+"
                        else -> "❤"
                    }

                    val damageAmount = BigDecimal(abs((lastHealth - entity.health).toDouble())).setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()

                    particles.add(
                        SingleParticle(
                            colorPrefix + prefix + damageAmount,
                            entity.posX - 0.5 + Random.nextInt(5).toDouble() * 0.1,
                            entity.entityBoundingBox.minY + (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) / 2.0,
                            entity.posZ - 0.5 + Random(1).nextInt(5).toDouble() * 0.1
                        )
                    )
                }
            }

            particles.removeAll { particle ->
                particle.ticks++
                particle.ticks > aliveTicks
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        synchronized(particles) {
            val renderManager = mc.renderManager
            val particleSize = size * 0.01

            for (particle in particles) {
                val x = particle.posX - renderManager.renderPosX
                val y = particle.posY - renderManager.renderPosY
                val z = particle.posZ - renderManager.renderPosZ

                glPushMatrix()
                disablePolygonOffset()
                doPolygonOffset(1.0f, -1500000.0f)
                translate(x.toFloat(), y.toFloat(), z.toFloat())
                rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)

                val textY = if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f
                rotate(renderManager.playerViewX, textY, 0.0f, 0.0f)
                scale(-particleSize, -particleSize, particleSize)
                glDepthMask(false)

                val textWidth = mc.fontRendererObj.getStringWidth(particle.str)
                val textHeight = mc.fontRendererObj.FONT_HEIGHT - 1

                val textColor = when (colorMode) {
                    "Client" -> getColor(1)
                    "Custom" -> customColor
                    else -> Color.WHITE
                }.rgb

                if (colorMode != "Damage") {
                    val shadowColor = Color(0, 0, 0, 130).rgb
                    val shadowOffset = 0.5f
                    when (shadowMode) {
                        "Normal" -> mc.fontRendererObj.drawString(particle.str, (-(textWidth / 2) + 1), (-(textHeight) + 1), Color(0, 0, 0, 150).rgb)
                        "Vanilla" -> mc.fontRendererObj.drawString(particle.str, (-(textWidth / 2) + 1), (-(textHeight) + 1), Color(20, 20, 20, 150).rgb)
                        "Default" -> mc.fontRendererObj.drawString(particle.str, (-(textWidth / 2) + shadowOffset), (-(textHeight) + shadowOffset), Color(0, 0, 0, 130).rgb, false)
                        "Outline" -> {
                            mc.fontRendererObj.drawString(particle.str, (-(textWidth / 2) + shadowOffset), (-(textHeight) + shadowOffset), shadowColor, false)
                            mc.fontRendererObj.drawString(particle.str, (-(textWidth / 2) - shadowOffset), (-(textHeight) - shadowOffset), shadowColor, false)
                            mc.fontRendererObj.drawString(particle.str, (-(textWidth / 2) + shadowOffset), (-(textHeight) - shadowOffset), shadowColor, false)
                            mc.fontRendererObj.drawString(particle.str, (-(textWidth / 2) - shadowOffset), (-(textHeight) + shadowOffset), shadowColor, false)
                        }
                    }
                }
                mc.fontRendererObj.drawString(particle.str, (-(textWidth / 2)).toFloat(), (-(textHeight)).toFloat(), textColor, false)

                glColor4f(187.0f, 255.0f, 255.0f, 1.0f)
                glDepthMask(true)
                doPolygonOffset(1.0f, 1500000.0f)
                disablePolygonOffset()
                resetColor()
                glPopMatrix()
            }
        }
    }

    val onWorld = handler<WorldEvent> {
        particles.clear()
        healthData.clear()
    }

    class SingleParticle(val str: String, val posX: Double, val posY: Double, val posZ: Double) {
        var ticks = 0
    }
}