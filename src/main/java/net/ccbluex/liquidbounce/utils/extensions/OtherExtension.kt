package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3


fun Vec3.multiply(value: Double): Vec3 {
    return Vec3(this.xCoord * value, this.yCoord * value, this.zCoord * value)
}

fun AxisAlignedBB.getLookingTargetRange(thePlayer: EntityPlayerSP, rotation: Rotation? = null, range: Double=6.0): Double {
    val eyes = thePlayer.getPositionEyes(1F)
    val movingObj = this.calculateIntercept(eyes, (rotation ?: RotationUtils.targetRotation)!!.toDirection().multiply(range).add(eyes)) ?: return Double.MAX_VALUE
    return movingObj.hitVec.distanceTo(eyes)
}