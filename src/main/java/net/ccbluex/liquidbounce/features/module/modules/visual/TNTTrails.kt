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
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.extensions.component3
import net.minecraft.client.renderer.GlStateManager.popMatrix
import net.minecraft.client.renderer.GlStateManager.pushMatrix
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

object TNTTrails : Module("TNTTrails", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, spacedName = "TNT Trails") {

    private val renderMode by choices("Mode", arrayOf("Line", "Area", "Particles"), "Line")
    private val activeColor by color("ActiveColor", Color.WHITE)
    private val completedColor by color("CompletedColor", Color(0, 0, 0))
    private val lineThickness by float("LineThickness", 2.0f, 1.0f..5.0f) { renderMode == "Line" }
    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200)
    private var maxRenderDistanceSq = maxRenderDistance.toDouble().pow(2)
        set(value) { field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2) else value }

    // Memory leak fix: Limit maximum TNT trails to prevent unbounded growth
    private const val MAX_TNT_TRAILS = 50
    private const val MAX_COMPLETED_TRAILS = 20

    private val activeTrails = mutableMapOf<EntityTNTPrimed, List<MutableList<Vec3>>>()
    private val completedTrails = ArrayDeque<List<List<Vec3>>>()

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
                        scribble.forEach { (x, y, z) ->
                            glVertex3d(
                                x - mc.renderManager.viewerPosX,
                                y - mc.renderManager.viewerPosY,
                                z - mc.renderManager.viewerPosZ
                            )
                        }
                        glEnd()
                    }
                }
                glColor3f(completedColor.red / 255f, completedColor.green / 255f, completedColor.blue / 255f)
                completedTrails.forEach { scribbleList ->
                    scribbleList.forEach { scribble ->
                        glBegin(GL_POINTS)
                        scribble.forEach { (x, y, z) ->
                            glVertex3d(
                                x - mc.renderManager.viewerPosX,
                                y - mc.renderManager.viewerPosY,
                                z - mc.renderManager.viewerPosZ
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
                if (renderMode == "Line") glLineWidth(lineThickness)
                glColor3f(activeColor.red / 255f, activeColor.green / 255f, activeColor.blue / 255f)
                activeTrails.values.forEach { scribbleList ->
                    scribbleList.forEach { scribble ->
                        glBegin(GL_LINE_STRIP)
                        scribble.forEach { (x, y, z) ->
                            glVertex3d(
                                x - mc.renderManager.viewerPosX,
                                y - mc.renderManager.viewerPosY,
                                z - mc.renderManager.viewerPosZ
                            )
                        }
                        glEnd()
                    }
                }
                glColor3f(completedColor.red / 255f, completedColor.green / 255f, completedColor.blue / 255f)
                completedTrails.forEach { scribbleList ->
                    scribbleList.forEach { scribble ->
                        glBegin(GL_LINE_STRIP)
                        scribble.forEach { (x, y, z) ->
                            glVertex3d(
                                x - mc.renderManager.viewerPosX,
                                y - mc.renderManager.viewerPosY,
                                z - mc.renderManager.viewerPosZ
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
        maxRenderDistanceSq = (maxRenderDistance * maxRenderDistance).toDouble()
        val random = ThreadLocalRandom.current()
        val loadedTNTs = mc.theWorld.loadedEntityList.filterIsInstance<EntityTNTPrimed>()
        val toRemove = HashSet<EntityTNTPrimed>()
        activeTrails.forEach { (tnt, scribbleList) ->
            if (!loadedTNTs.contains(tnt)) {
                // Memory leak fix: Limit completed trails
                if (completedTrails.size >= MAX_COMPLETED_TRAILS) {
                    completedTrails.removeFirst()
                }
                completedTrails.add(scribbleList)
                toRemove.add(tnt)
            }
        }
        activeTrails.keys.removeAll(toRemove)

            // Memory leak fix: Limit active trails
            if (activeTrails.size >= MAX_TNT_TRAILS) {
                // Remove oldest trail
                activeTrails.remove(activeTrails.keys.first())
            }

            loadedTNTs.forEach { tnt ->
                if (!activeTrails.containsKey(tnt)) {
                    when (renderMode) {
                        "Line" -> {
                            activeTrails[tnt] = mutableListOf(mutableListOf(tnt.positionVector))
                        }
                        "Area", "Particles" -> {
                            val scribbleList = mutableListOf<MutableList<Vec3>>()
                            repeat(20) {
                                val scribble = mutableListOf<Vec3>()
                                repeat(5) {
                                    val offsetX = (random.nextDouble() - 0.5) * 8.0
                                    val offsetY = (random.nextDouble() - 0.5) * 8.0
                                    val offsetZ = (random.nextDouble() - 0.5) * 8.0
                                    scribble.add(Vec3(tnt.posX + offsetX, tnt.posY + offsetY, tnt.posZ + offsetZ))
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
                                val offsetX = (random.nextDouble() - 0.5) * 0.5
                                val offsetY = (random.nextDouble() - 0.5) * 0.5
                                val offsetZ = (random.nextDouble() - 0.5) * 0.5
                                scribble.add(Vec3(tnt.posX + offsetX, tnt.posY + offsetY, tnt.posZ + offsetZ))
                            }
                        }
                        "Area", "Particles" -> {
                            activeTrails[tnt]?.forEach { scribble ->
                                repeat(5) {
                                    val offsetX = (random.nextDouble() - 0.5) * 8.0
                                    val offsetY = (random.nextDouble() - 0.5) * 8.0
                                    val offsetZ = (random.nextDouble() - 0.5) * 8.0
                                    scribble.add(Vec3(tnt.posX + offsetX, tnt.posY + offsetY, tnt.posZ + offsetZ))
                                }

                            }
                        }
                    }
                }
            }
        }
}
