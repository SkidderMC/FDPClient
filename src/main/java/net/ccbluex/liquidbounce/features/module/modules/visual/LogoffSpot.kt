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
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawAxisAlignedBB
import net.ccbluex.liquidbounce.utils.render.RenderUtils.renderNameTag
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import java.awt.Color

object LogoffSpot : Module("LogoffSpot", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false, spacedName = "Logoff Spot") {

    private val maxTrackDistance by float("MaxTrackDistance", 64F, 8F..256F)
    private val clearDistance by float("ClearDistance", 4F, 1F..16F)
    private val sendInChat by boolean("SendInChat", false)
    private val color by color("Color", Color(255, 170, 0))

    private data class Spot(val name: String, val x: Double, val y: Double, val z: Double)

    private val lastSeen = HashMap<String, Vec3>()
    private val spots = HashMap<String, Spot>()

    override fun onEnable() {
        lastSeen.clear()
        spots.clear()
    }

    override fun onDisable() {
        lastSeen.clear()
        spots.clear()
    }

    val onWorld = handler<WorldEvent> {
        lastSeen.clear()
        spots.clear()
    }

    val onUpdate = handler<UpdateEvent> {
        val world = mc.theWorld ?: return@handler
        val self = mc.thePlayer ?: return@handler

        val current = HashMap<String, Vec3>()
        for (entity in world.playerEntities) {
            if (entity === self) continue
            if (self.getDistanceToEntity(entity) > maxTrackDistance) continue
            current[entity.name] = entity.positionVector
        }

        // Players that disappeared since last tick become a logoff spot
        for ((name, pos) in lastSeen) {
            if (!current.containsKey(name) && !spots.containsKey(name)) {
                spots[name] = Spot(name, pos.xCoord, pos.yCoord, pos.zCoord)
                if (sendInChat)
                    ClientUtils.displayChatMessage(
                        "$name disappeared at X:${pos.xCoord.toInt()} Y:${pos.yCoord.toInt()} Z:${pos.zCoord.toInt()}"
                    )
            }
        }

        // Player reappeared -> drop their spot
        for (name in current.keys) {
            if (sendInChat && spots.containsKey(name))
                ClientUtils.displayChatMessage("$name reappeared")
            spots.remove(name)
        }

        // Reached the spot -> drop it
        spots.entries.removeIf { (_, spot) ->
            self.getDistanceSq(spot.x, spot.y, spot.z) <= clearDistance * clearDistance
        }

        lastSeen.clear()
        lastSeen.putAll(current)
    }

    val onRender3D = handler<Render3DEvent> {
        val self = mc.thePlayer ?: return@handler
        val renderManager = mc.renderManager
        val boxColor = color

        for (spot in spots.values) {
            val x = spot.x
            val y = spot.y
            val z = spot.z

            drawAxisAlignedBB(
                AxisAlignedBB.fromBounds(
                    x - 0.4 - renderManager.renderPosX, y - renderManager.renderPosY, z - 0.4 - renderManager.renderPosZ,
                    x + 0.4 - renderManager.renderPosX, y + 1.8 - renderManager.renderPosY, z + 0.4 - renderManager.renderPosZ
                ), boxColor
            )

            val distance = self.getDistance(x, y, z).toInt()
            renderNameTag("${spot.name} [${distance}m]", x, y + 2.2, z)
        }
    }
}
