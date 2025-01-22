/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

class PlaceInfo(val blockPos: BlockPos, val enumFacing: EnumFacing, var vec3: Vec3 = blockPos.center) {

    companion object {

        /**
         * Allows you to find a specific place info for your [blockPos]
         */
        fun get(pos: BlockPos) = EnumFacing.entries.find {
            it != EnumFacing.UP && pos.offset(it).canBeClicked()
        }?.let { side -> PlaceInfo(pos.offset(side), side.opposite) }
    }
}