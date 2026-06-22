/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11.*

object CombineMobs : Module("CombineMobs", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val combineArmorStands by boolean("CombineArmorStands", false)
        .describe("Also combine armor stands into groups.")
    private val babyGrouping by boolean("SeparateBabies", true)
        .describe("Group baby mobs separately from adults.")
    private val showCount by boolean("ShowCount", true)
        .describe("Show the group size above the mob.")
    private val minGroup by int("MinGroup", 2, 2..16)
        .describe("Minimum mobs needed to form a group.")
    private val countScale by float("CountScale", 0.025f, 0.01f..0.05f)
        .describe("Size of the count text above the mob.")

    private data class CombineKey(val type: Class<*>, val x: Int, val y: Int, val z: Int, val baby: Boolean)

    private val representatives = HashMap<Int, Int>()

    val onMotion = handler<MotionEvent> {
        representatives.clear()

        val world = mc.theWorld ?: return@handler
        val self = mc.thePlayer

        val groups = HashMap<CombineKey, MutableList<EntityLivingBase>>()

        for (en in world.loadedEntityList) {
            val entity = en ?: continue
            if (entity === self) continue
            if (!canCombine(entity)) continue

            val living = entity as EntityLivingBase
            val key = keyFor(living)
            groups.getOrPut(key) { ArrayList() }.add(living)
        }

        for ((_, list) in groups) {
            if (list.size < minGroup) {
                for (entity in list) {
                    if (entity.renderDistanceWeight <= 0.0) entity.renderDistanceWeight = 1.0
                }
                continue
            }

            val representative = list[0]
            representative.renderDistanceWeight = 1.0
            representatives[representative.entityId] = list.size

            for (i in 1 until list.size) {
                list[i].renderDistanceWeight = 0.0
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (!showCount) return@handler

        val world = mc.theWorld ?: return@handler
        val renderManager = mc.renderManager
        val fontRenderer = mc.fontRendererObj

        for (en in world.loadedEntityList) {
            val entity = en ?: continue
            val count = representatives[entity.entityId] ?: continue
            if (entity !is EntityLivingBase) continue

            val partial = mc.timer.renderPartialTicks
            val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - renderManager.renderPosX
            val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - renderManager.renderPosY + entity.height + 0.5
            val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - renderManager.renderPosZ

            val text = "x$count"
            val width = fontRenderer.getStringWidth(text) / 2

            glPushMatrix()
            glTranslatef(x.toFloat(), y.toFloat(), z.toFloat())
            glNormal3f(0.0f, 1.0f, 0.0f)
            glRotatef(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
            glRotatef(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
            glScalef(-countScale, -countScale, countScale)

            RenderUtils.setGLCap(GL_LIGHTING, false)
            RenderUtils.setGLCap(GL_DEPTH_TEST, false)
            RenderUtils.setGLCap(GL_BLEND, true)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            fontRenderer.drawString(text, (-width).toFloat(), 0f, 0xFFFFFF, true)

            RenderUtils.revertAllCaps()
            glColor4f(1f, 1f, 1f, 1f)
            glPopMatrix()
        }
    }

    private fun canCombine(entity: Entity): Boolean {
        if (entity is EntityArmorStand) return combineArmorStands
        return entity is EntityLivingBase
    }

    private fun keyFor(living: EntityLivingBase): CombineKey {
        val pos = BlockPos(living)
        val baby = babyGrouping && living.isChild
        return CombineKey(living.javaClass, pos.x, pos.y, pos.z, baby)
    }

    override fun onDisable() {
        representatives.clear()
        val world = mc.theWorld ?: return
        for (en in world.loadedEntityList) {
            val entity = en ?: continue
            if (entity !== mc.thePlayer && entity.renderDistanceWeight <= 0.0) {
                entity.renderDistanceWeight = 1.0
            }
        }
    }
}
