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
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import java.awt.Color
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object VoidESP : Module("VoidESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false, spacedName = "Void ESP") {

    private val players by boolean("Players", true)
        .describe("Highlight players standing over the void.")
    private val otherEntities by boolean("OtherEntities", false)
        .describe("Highlight non-player entities over the void.")
    private val selfWarning by boolean("SelfWarning", true)
        .describe("Notify you when you are over the void.")
    private val yThreshold by int("YThreshold", 0, 0..64)
        .describe("Y level below which a column counts as void.")
    private val rangeFacing by float("RangeFacing", 0f, 0f..16f)
        .describe("Forward range to scan ahead for void.")
    private val rangeSide by float("RangeSide", 0f, 0f..16f)
        .describe("Sideways range to scan for void.")
    private val color by color("Color", Color(255, 60, 60))
        .describe("Box color for entities over the void.")

    private var wasOverVoid = false

    private fun isColumnVoid(x: Double, z: Double, y: Double): Boolean {
        val world = mc.theWorld ?: return false
        val bx = floor(x).toInt()
        val bz = floor(z).toInt()
        var checkY = y.toInt()
        if (checkY < yThreshold) return true
        while (checkY >= yThreshold) {
            if (!world.isAirBlock(BlockPos(bx, checkY, bz))) return false
            checkY--
        }
        return true
    }

    private fun isOverVoid(entity: Entity): Boolean {
        val x = entity.posX
        val y = entity.posY
        val z = entity.posZ

        if (isColumnVoid(x, z, y)) return true

        val facing = rangeFacing
        val side = rangeSide
        if (facing <= 0f && side <= 0f) return false

        val yaw = Math.toRadians(entity.rotationYaw.toDouble())
        val fx = -sin(yaw)
        val fz = cos(yaw)
        val sx = cos(yaw)
        val sz = sin(yaw)

        var f = 0
        while (f <= facing.toInt()) {
            var s = -side.toInt()
            while (s <= side.toInt()) {
                if (f != 0 || s != 0) {
                    val px = x + fx * f + sx * s
                    val pz = z + fz * f + sz * s
                    if (isColumnVoid(px, pz, y)) return true
                }
                s++
            }
            f++
        }
        return false
    }

    val onRender3D = handler<Render3DEvent> {
        val world = mc.theWorld ?: return@handler
        val self = mc.thePlayer ?: return@handler
        val boxColor = color

        for (entity in world.loadedEntityList.toList()) {
            if (entity === self) continue

            val isPlayer = entity is EntityPlayer
            if (isPlayer && !players) continue
            if (!isPlayer && !otherEntities) continue

            if (isOverVoid(entity))
                drawEntityBox(entity, boxColor, false)
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (!selfWarning) {
            wasOverVoid = false
            return@handler
        }

        val self = mc.thePlayer ?: return@handler
        val overVoid = isOverVoid(self)

        if (overVoid && !wasOverVoid)
            addNotification(Notification("VoidESP", "You are standing over the void!", Type.ERROR, 2000))

        wasOverVoid = overVoid
    }
}
