/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawAxisAlignedBB
import net.ccbluex.liquidbounce.utils.render.RenderUtils.renderNameTag
import net.minecraft.entity.item.EntityEnderEye
import net.minecraft.util.AxisAlignedBB
import java.awt.Color
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

object StrongholdFinder : Module("StrongholdFinder", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false, spacedName = "Stronghold Finder") {

    private val color by color("Color", Color(0, 200, 255))
    private val minAngleDifference by float("MinAngleDifference", 2F, 0.5F..20F)

    private data class EyeThrow(val x: Double, val z: Double, val dx: Double, val dz: Double)

    private val knownEyes = HashSet<Int>()
    private var prevThrow: EyeThrow? = null
    private var lastThrow: EyeThrow? = null
    private var resultX: Double? = null
    private var resultZ: Double? = null

    override fun onEnable() = reset()
    override fun onDisable() = reset()

    private fun reset() {
        knownEyes.clear()
        prevThrow = null
        lastThrow = null
        resultX = null
        resultZ = null
    }

    val onWorld = handler<WorldEvent> { reset() }

    val onUpdate = handler<UpdateEvent> {
        val world = mc.theWorld ?: return@handler

        for (entity in world.loadedEntityList.toList()) {
            if (entity !is EntityEnderEye) continue
            if (!knownEyes.add(entity.entityId)) continue

            val dx = entity.motionX
            val dz = entity.motionZ
            if (sqrt(dx * dx + dz * dz) < 1e-4) continue

            prevThrow = lastThrow
            lastThrow = EyeThrow(entity.posX, entity.posZ, dx, dz)
            triangulate()
        }
    }

    private fun triangulate() {
        val a = prevThrow ?: return
        val b = lastThrow ?: return

        // Minimal angle difference between the two throw directions, normalized to [0, 180]
        val rawDiff = Math.toDegrees(atan2(a.dz, a.dx) - atan2(b.dz, b.dx))
        val angleDiff = abs(((rawDiff + 540.0) % 360.0) - 180.0)
        if (angleDiff < minAngleDifference) {
            chat("§e[StrongholdFinder] §7Throw directions too similar (${"%.1f".format(angleDiff)}°). Move further to the side and throw another eye.")
            return
        }

        val denom = a.dx * b.dz - a.dz * b.dx
        if (abs(denom) < 1e-6) return

        val t = ((b.x - a.x) * b.dz - (b.z - a.z) * b.dx) / denom
        val x = a.x + t * a.dx
        val z = a.z + t * a.dz

        resultX = x
        resultZ = z

        val self = mc.thePlayer
        val distance = if (self != null) sqrt((x - self.posX) * (x - self.posX) + (z - self.posZ) * (z - self.posZ)).toInt() else 0
        chat("§b[StrongholdFinder] §aStronghold at X §f${x.toInt()} §aZ §f${z.toInt()} §7(~${distance}m)")
    }

    val onRender3D = handler<Render3DEvent> {
        val x = resultX ?: return@handler
        val z = resultZ ?: return@handler
        val self = mc.thePlayer ?: return@handler
        val renderManager = mc.renderManager

        val baseY = self.posY - 2.0
        val topY = self.posY + 60.0

        drawAxisAlignedBB(
            AxisAlignedBB.fromBounds(
                x - 0.5 - renderManager.renderPosX, baseY - renderManager.renderPosY, z - 0.5 - renderManager.renderPosZ,
                x + 0.5 - renderManager.renderPosX, topY - renderManager.renderPosY, z + 0.5 - renderManager.renderPosZ
            ), color
        )

        val distance = sqrt((x - self.posX) * (x - self.posX) + (z - self.posZ) * (z - self.posZ)).toInt()
        renderNameTag("Stronghold [${distance}m]", x, self.posY + 2.0, z)
    }
}
