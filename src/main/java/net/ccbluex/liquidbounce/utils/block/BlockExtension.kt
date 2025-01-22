/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isBlockBBValid
import net.ccbluex.liquidbounce.utils.extensions.ceilInt
import net.ccbluex.liquidbounce.utils.extensions.floorInt
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i

val BlockPos.state: IBlockState?
    get() = mc.theWorld?.getBlockState(this)

val BlockPos.block: Block?
    get() = this.state?.block

val BlockPos.material: Material?
    get() = this.block?.material

val BlockPos.isReplaceable: Boolean
    get() = this.material?.isReplaceable ?: false

val BlockPos.center: Vec3
    get() = Vec3(x + 0.5, y + 0.5, z + 0.5)

fun BlockPos.toVec() = Vec3(this)

fun BlockPos.canBeClicked(): Boolean {
    val world = mc.theWorld ?: return false
    val state = this.state ?: return false
    val block = state.block ?: return false

    return when {
        this !in world.worldBorder -> false
        !block.canCollideCheck(state, false) -> false
        block.material.isReplaceable -> false
        block.hasTileEntity(state) -> false
        !isBlockBBValid(this, state, supportSlabs = true, supportPartialBlocks = true) -> false
        world.loadedEntityList.any { it is EntityFallingBlock && it.position == this } -> false
        block is BlockContainer || block is BlockWorkbench -> false
        else -> true
    }
}

val Block.id: Int
    get() = Block.getIdFromBlock(this)
val Int.blockById: Block
    get() = Block.getBlockById(this)

val String.blockByName: Block?
    get() = Block.getBlockFromName(this)

fun BlockPos.MutableBlockPos.set(vec3i: Vec3i, xOffset: Int = 0, yOffset: Int = 0, zOffset: Int = 0): BlockPos.MutableBlockPos =
    set(vec3i.x + xOffset, vec3i.y + yOffset, vec3i.z + zOffset)
fun BlockPos.getAllInBoxMutable(radius: Int): Iterable<BlockPos> {
    return BlockPos.getAllInBoxMutable(add(-radius, -radius, -radius), add(radius, radius, radius))
}
fun BlockPos.getAllInBox(radius: Int): Iterable<BlockPos> {
    return BlockPos.getAllInBox(add(-radius, -radius, -radius), add(radius, radius, radius))
}
fun Vec3.getAllInBoxMutable(radius: Double): Iterable<BlockPos> {
    val from = BlockPos(
        (xCoord - radius).floorInt(),
        (yCoord - radius).floorInt(),
        (zCoord - radius).floorInt()
    )
    val to = BlockPos(
        (xCoord + radius).ceilInt(),
        (yCoord + radius).ceilInt(),
        (zCoord + radius).ceilInt()
    )
    return BlockPos.getAllInBoxMutable(from, to)
}
fun Vec3.getAllInBox(radius: Double): Iterable<BlockPos> {
    val from = BlockPos(
        (xCoord - radius).floorInt(),
        (yCoord - radius).floorInt(),
        (zCoord - radius).floorInt()
    )
    val to = BlockPos(
        (xCoord + radius).ceilInt(),
        (yCoord + radius).ceilInt(),
        (zCoord + radius).ceilInt()
    )
    return BlockPos.getAllInBox(from, to)
}