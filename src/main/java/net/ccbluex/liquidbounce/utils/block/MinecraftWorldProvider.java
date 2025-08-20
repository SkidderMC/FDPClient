/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block;

import me.liuli.path.Cell;
import me.liuli.path.IWorldProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class MinecraftWorldProvider implements IWorldProvider {

    private static final ThreadLocal<BlockPos.MutableBlockPos> CACHE = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    private final World world;

    public MinecraftWorldProvider(World world) {
        this.world = world;
    }

    @Override
    public boolean isBlocked(Cell cell) {
        return isBlocked(cell.x, cell.y, cell.z);
    }

    public boolean isBlocked(int x, int y, int z) {
        return isSolid(x, y, z) || isSolid(x, y + 1, z) || unableToStand(x, y - 1, z);
    }

    private boolean isSolid(int x, int y, int z) {
        Block block = world.getBlockState(CACHE.get().set(x, y, z)).getBlock();
        if(block == null) return true;

        return block.getMaterial().isSolid();
    }

    private boolean unableToStand(int x, int y, int z) {
        final Block block = world.getBlockState(CACHE.get().set(x, y, z)).getBlock();
        return block instanceof BlockFence || block instanceof BlockWall;
    }
}