/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3

/** Finds the first point along [from]..[to] that leaves all supporting collision boxes. */
fun findEdgeCollision(from: Vec3, to: Vec3, allowedDropDown: Float = 0.5F): Vec3? {
    val direction = to.subtract(from)
    if (direction.lengthVector() <= 1.0E-7) return null

    val boxes = collectCollisionBoundingBoxes(from, to, allowedDropDown).toMutableList()
    if (boxes.isEmpty()) return from

    val extendedFrom = from.addVector(-direction.xCoord * 1000.0, -direction.yCoord * 1000.0, -direction.zCoord * 1000.0)
    val extendedTo = to.addVector(direction.xCoord * 1000.0, direction.yCoord * 1000.0, direction.zCoord * 1000.0)
    var current = from

    repeat(boxes.size + 1) {
        val containing = boxes.filter { it.containsInclusive(current) }
        if (containing.isEmpty()) return current
        if (containing.any { it.containsInclusive(to) }) return null

        val next = containing.mapNotNull { it.calculateIntercept(extendedFrom, extendedTo)?.hitVec }
            .filter { it.squareDistanceTo(current) > 1.0E-10 }
            .minByOrNull { it.squareDistanceTo(to) }
            ?: return current

        boxes.removeAll(containing.toSet())
        current = next.addVector(
            direction.xCoord * 1.0E-7,
            direction.yCoord * 1.0E-7,
            direction.zCoord * 1.0E-7,
        )
    }
    return current
}

fun collectCollisionBoundingBoxes(from: Vec3, to: Vec3, allowedDropDown: Float = 0.5F): List<AxisAlignedBB> {
    val player = mc.thePlayer ?: return emptyList()
    val halfWidth = player.width * 0.5
    val minX = minOf(from.xCoord, to.xCoord) - halfWidth - 1.0E-4
    val maxX = maxOf(from.xCoord, to.xCoord) + halfWidth + 1.0E-4
    val minY = minOf(from.yCoord, to.yCoord) - allowedDropDown - 1.0
    val maxY = maxOf(from.yCoord, to.yCoord) + 0.1
    val minZ = minOf(from.zCoord, to.zCoord) - halfWidth - 1.0E-4
    val maxZ = maxOf(from.zCoord, to.zCoord) + halfWidth + 1.0E-4
    val query = AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)

    return player.worldObj.getCollidingBoundingBoxes(player, query).map { box ->
        AxisAlignedBB(
            box.minX - halfWidth,
            box.minY - 1.0,
            box.minZ - halfWidth,
            box.maxX + halfWidth,
            box.maxY + allowedDropDown + 0.05,
            box.maxZ + halfWidth,
        )
    }
}

fun EntityPlayerSP.wouldBeCloseToFallOff(position: Vec3, allowedDropDown: Double = 0.6): Boolean {
    val offsetBox = entityBoundingBox.offset(position.xCoord - posX, position.yCoord - posY, position.zCoord - posZ)
        .contract(0.05, 0.0, 0.05)
        .offset(0.0, -allowedDropDown, 0.0)
    return worldObj.getCollidingBoundingBoxes(this, offsetBox).isEmpty()
}

fun EntityPlayerSP.isCloseToEdge(distance: Double = 0.1): Boolean {
    val horizontalSpeed = kotlin.math.sqrt(motionX * motionX + motionZ * motionZ)
    val dirX: Double
    val dirZ: Double
    if (horizontalSpeed > 0.003) {
        dirX = motionX / horizontalSpeed
        dirZ = motionZ / horizontalSpeed
    } else {
        val yaw = Math.toRadians(DirectionalInput(moveForward, moveStrafing).movementYaw(rotationYaw).toDouble())
        dirX = -kotlin.math.sin(yaw)
        dirZ = kotlin.math.cos(yaw)
    }

    val from = Vec3(posX, entityBoundingBox.minY - 0.1, posZ)
    val to = from.addVector(dirX * distance, 0.0, dirZ * distance)
    if (findEdgeCollision(from, to) != null) return true

    val predicted = Vec3(posX + motionX * 2.0, posY, posZ + motionZ * 2.0)
    return wouldBeCloseToFallOff(Vec3(posX, posY, posZ)) || wouldBeCloseToFallOff(predicted)
}

private fun AxisAlignedBB.containsInclusive(point: Vec3): Boolean =
    point.xCoord >= minX - 1.0E-7 && point.xCoord <= maxX + 1.0E-7 &&
        point.yCoord >= minY - 1.0E-7 && point.yCoord <= maxY + 1.0E-7 &&
        point.zCoord >= minZ - 1.0E-7 && point.zCoord <= maxZ + 1.0E-7
