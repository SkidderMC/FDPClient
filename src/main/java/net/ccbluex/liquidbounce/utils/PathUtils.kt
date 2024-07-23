/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import me.liuli.path.Cell
import me.liuli.path.Pathfinder
import net.ccbluex.liquidbounce.utils.block.MinecraftWorldProvider
import net.minecraft.util.Vec3
import javax.vecmath.Vector3d
import kotlin.math.*

object PathUtils : MinecraftInstance() {
    fun findBlinkPath(tpX: Double, tpY: Double, tpZ: Double): List<Vector3d> {
        val positions = mutableListOf<Vector3d>()

        var curX = mc.thePlayer.posX
        var curY = mc.thePlayer.posY
        var curZ = mc.thePlayer.posZ
        var distance = abs(curX - tpX) + abs(curY - tpY) + abs(curZ - tpZ)

        var count = 0
        while (distance > 0) {
            distance = abs(curX - tpX) + abs(curY - tpY) + abs(curZ - tpZ)

            val diffX = curX - tpX
            val diffY = curY - tpY
            val diffZ = curZ - tpZ
            val offset = if (count and 1 == 0) 0.4 else 0.1

            val minX = diffX.coerceIn(-offset, offset)
            curX -= minX

            val minY = diffY.coerceIn(-0.25, 0.25)
            curY -= minY

            val minZ = diffZ.coerceIn(-offset, offset)
            curZ -= minZ

            positions += Vector3d(curX, curY, curZ)
            count++
        }

        return positions
    }


    fun findBlinkPath(tpX: Double, tpY: Double, tpZ: Double, dist: Double): List<Vec3> {
        return findBlinkPath(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tpX, tpY, tpZ, dist)
    }

    fun findBlinkPath(
        curX: Double,
        curY: Double,
        curZ: Double,
        tpX: Double,
        tpY: Double,
        tpZ: Double,
        dashDistance: Double
    ): List<Vec3> {
        val worldProvider = MinecraftWorldProvider(mc.theWorld)
        val pathfinder = Pathfinder(
            Cell(curX.toInt(), curY.toInt(), curZ.toInt()), Cell(tpX.toInt(), tpY.toInt(), tpZ.toInt()),
            Pathfinder.COMMON_NEIGHBORS, worldProvider
        )

        return simplifyPath(pathfinder.findPath(3000), dashDistance, worldProvider)
    }


    fun findPath(tpX: Double, tpY: Double, tpZ: Double, offset: Double): List<Vector3d> {
        val positions = mutableListOf<Vector3d>()
        val steps = ceil(getDistance(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tpX, tpY, tpZ) / offset)

        val dX = tpX - mc.thePlayer.posX
        val dY = tpY - mc.thePlayer.posY
        val dZ = tpZ - mc.thePlayer.posZ

        var d = 1.0
        while (d <= steps) {
            positions += Vector3d(mc.thePlayer.posX + dX * d / steps, mc.thePlayer.posY + dY * d / steps, mc.thePlayer.posZ + dZ * d / steps)
            ++d
        }

        return positions
    }

    private fun getDistance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
        val xDiff = x1 - x2
        val yDiff = y1 - y2
        val zDiff = z1 - z2

        return sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff)
    }

    fun simplifyPath(
        path: ArrayList<Cell>,
        dashDistance: Double,
        worldProvider: MinecraftWorldProvider
    ): ArrayList<Vec3> {
        val finalPath = ArrayList<Vec3>()

        var cell = path[0]
        var vec3: Vec3
        var lastLoc = Vec3(cell.x + 0.5, cell.y.toDouble(), cell.z + 0.5)
        var lastDashLoc = lastLoc
        for (i in 1 until path.size - 1) {
            cell = path[i]
            vec3 = Vec3(cell.x + 0.5, cell.y.toDouble(), cell.z + 0.5)
            var canContinue = true
            if (vec3.squareDistanceTo(lastDashLoc) > dashDistance * dashDistance) {
                canContinue = false
            } else {
                val smallX = min(lastDashLoc.xCoord, vec3.xCoord)
                val smallY = min(lastDashLoc.yCoord, vec3.yCoord)
                val smallZ = min(lastDashLoc.zCoord, vec3.zCoord)
                val bigX = max(lastDashLoc.xCoord, vec3.xCoord)
                val bigY = max(lastDashLoc.yCoord, vec3.yCoord)
                val bigZ = max(lastDashLoc.zCoord, vec3.zCoord)
                var x = smallX.toInt()
                cordsLoop@ while (x <= bigX) {
                    var y = smallY.toInt()
                    while (y <= bigY) {
                        var z = smallZ.toInt()
                        while (z <= bigZ) {
                            if (worldProvider.isBlocked(x, y, z)) {
                                canContinue = false
                                break@cordsLoop
                            }
                            z++
                        }
                        y++
                    }
                    x++
                }
            }
            if (!canContinue) {
                finalPath.add(lastLoc)
                lastDashLoc = lastLoc
            }
            lastLoc = vec3
        }

        return finalPath
    }
}
