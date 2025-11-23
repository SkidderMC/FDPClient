/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.renderer.GlStateManager.popMatrix
import net.minecraft.client.renderer.GlStateManager.pushMatrix
import net.minecraft.entity.item.EntityTNTPrimed
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.pow

object TNTTrails : Module("TNTTrails", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, spacedName = "TNT Trails") {

    private val renderMode by choices("Mode", arrayOf("Line", "Area", "Particles"), "Line")
    private val activeColor by color("ActiveColor", Color.WHITE)
    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200)
    private var maxRenderDistanceSq = maxRenderDistance.toDouble().pow(2)
        set(value) { field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2) else value }

    private val activeTrails = mutableMapOf<EntityTNTPrimed, MutableList<MutableList<Triple<Double, Double, Double>>>>()
    private val completedTrails = mutableListOf<MutableList<MutableList<Triple<Double, Double, Double>>>>()

    val onRender3D = handler<Render3DEvent> {
        when (renderMode) {
            "Particles" -> {
                glPushAttrib(GL_ENABLE_BIT)
                pushMatrix()
                glPointSize(4.0f)
                glColor3f(activeColor.red / 255f, activeColor.green / 255f, activeColor.blue / 255f)
                activeTrails.values.forEach { scribbleList ->
                    scribbleList.forEach { scribble ->
                        glBegin(GL_POINTS)
                        scribble.forEach { point ->
                            glVertex3d(
                                point.first - mc.renderManager.viewerPosX,
                                point.second - mc.renderManager.viewerPosY,
                                point.third - mc.renderManager.viewerPosZ
                            )
                        }
                        glEnd()
                    }
                }
                glColor3f(0f, 0f, 0f)
                completedTrails.forEach { scribbleList ->
                    scribbleList.forEach { scribble ->
                        glBegin(GL_POINTS)
                        scribble.forEach { point ->
                            glVertex3d(
                                point.first - mc.renderManager.viewerPosX,
                                point.second - mc.renderManager.viewerPosY,
                                point.third - mc.renderManager.viewerPosZ
                            )
                        }
                        glEnd()
                    }
                }
                popMatrix()
                glPopAttrib()
            }
            else -> {
                glPushAttrib(GL_ENABLE_BIT)
                pushMatrix()
                if (renderMode == "Line") glLineWidth(2.0f)
                glColor3f(activeColor.red / 255f, activeColor.green / 255f, activeColor.blue / 255f)
                activeTrails.values.forEach { scribbleList ->
                    scribbleList.forEach { scribble ->
                        glBegin(GL_LINE_STRIP)
                        scribble.forEach { point ->
                            glVertex3d(
                                point.first - mc.renderManager.viewerPosX,
                                point.second - mc.renderManager.viewerPosY,
                                point.third - mc.renderManager.viewerPosZ
                            )
                        }
                        glEnd()
                    }
                }
                glColor3f(0f, 0f, 0f)
                completedTrails.forEach { scribbleList ->
                    scribbleList.forEach { scribble ->
                        glBegin(GL_LINE_STRIP)
                        scribble.forEach { point ->
                            glVertex3d(
                                point.first - mc.renderManager.viewerPosX,
                                point.second - mc.renderManager.viewerPosY,
                                point.third - mc.renderManager.viewerPosZ
                            )
                        }
                        glEnd()
                    }
                }
                popMatrix()
                glPopAttrib()
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        maxRenderDistanceSq = maxRenderDistance.toDouble().pow(2)
        val loadedTNTs = mc.theWorld.loadedEntityList.filterIsInstance<EntityTNTPrimed>()
            val toRemove = mutableListOf<EntityTNTPrimed>()
            activeTrails.forEach { (tnt, scribbleList) ->
                if (!loadedTNTs.contains(tnt)) {
                    completedTrails.add(scribbleList)
                    toRemove.add(tnt)
                }
            }
            toRemove.forEach { activeTrails.remove(it) }
            loadedTNTs.forEach { tnt ->
                if (!activeTrails.containsKey(tnt)) {
                    when (renderMode) {
                        "Line" -> {
                            activeTrails[tnt] = mutableListOf(mutableListOf(Triple(tnt.posX, tnt.posY, tnt.posZ)))
                        }
                        "Area", "Particles" -> {
                            val scribbleList = mutableListOf<MutableList<Triple<Double, Double, Double>>>()
                            repeat(20) {
                                val scribble = mutableListOf<Triple<Double, Double, Double>>()
                                repeat(5) {
                                    val offsetX = (Math.random() - 0.5) * 8.0
                                    val offsetY = (Math.random() - 0.5) * 8.0
                                    val offsetZ = (Math.random() - 0.5) * 8.0
                                    scribble.add(Triple(tnt.posX + offsetX, tnt.posY + offsetY, tnt.posZ + offsetZ))
                                }
                                scribbleList.add(scribble)
                            }
                            activeTrails[tnt] = scribbleList
                        }

                    }
                } else {
                    when (renderMode) {
                        "Line" -> {
                            activeTrails[tnt]?.forEach { scribble ->
                                val offsetX = (Math.random() - 0.5) * 0.5
                                val offsetY = (Math.random() - 0.5) * 0.5
                                val offsetZ = (Math.random() - 0.5) * 0.5
                                scribble.add(Triple(tnt.posX + offsetX, tnt.posY + offsetY, tnt.posZ + offsetZ))
                            }
                        }
                        "Area", "Particles" -> {
                            activeTrails[tnt]?.forEach { scribble ->
                                repeat(5){
                                    val offsetX = (Math.random() - 0.5) * 8.0
                                    val offsetY = (Math.random() - 0.5) * 8.0
                                    val offsetZ = (Math.random() - 0.5) * 8.0
                                    scribble.add(Triple(tnt.posX + offsetX, tnt.posY + offsetY, tnt.posZ + offsetZ))
                                }

                            }
                        }
                    }
                }
            }
        }
}