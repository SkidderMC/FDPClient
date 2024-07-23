/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.entity.item.EntityTNTPrimed
import org.lwjgl.opengl.GL11

object TNTTrails : Module("TNTTrails", Category.VISUAL, spacedName = "TNT Trails", hideModule = false) {
    private val tntPositions = mutableMapOf<EntityTNTPrimed, MutableList<Triple<Double, Double, Double>>>()

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        tntPositions.keys.toList().forEach { tnt ->
            val positions = tntPositions[tnt] ?: return@forEach

            GL11.glPushMatrix()
            GL11.glLineWidth(2.0f)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glColor3f(1.0f, 0.0f, 0.0f)

            for (pos in positions) {
                GL11.glVertex3d(pos.first - mc.renderManager.viewerPosX, pos.second - mc.renderManager.viewerPosY, pos.third - mc.renderManager.viewerPosZ)
            }

            GL11.glEnd()
            GL11.glPopMatrix()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.theWorld.loadedEntityList.filterIsInstance<EntityTNTPrimed>().forEach { tnt ->
            val positions = tntPositions.getOrPut(tnt) { mutableListOf() }
            positions.add(Triple(tnt.posX, tnt.posY, tnt.posZ))
        }
    }
}