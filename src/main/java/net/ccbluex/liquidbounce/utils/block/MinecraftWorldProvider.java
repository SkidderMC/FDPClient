package net.ccbluex.liquidbounce.utils.block;

import me.liuli.path.Cell;
import me.liuli.path.IWorldProvider;
import net.minecraft.block.*;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class MinecraftWorldProvider implements IWorldProvider {

    private final World world;

    public MinecraftWorldProvider(World world) {
        this.world = world;
    }

    @Override
    public boolean isBlocked(Cell cell) {
        return isSolid(cell.x, cell.y, cell.z);
    }

    public boolean isBlocked(int x, int y, int z) {
        return isSolid(x, y, z) || isSolid(x, y + 1, z) || unableToStand(x, y - 1, z);
    }

    private boolean isSolid(int x, int y, int z) {
        Block block=BlockUtils.getBlock(new BlockPos(x, y, z));
        if(block==null) return false;

        return block.isFullBlock() ||
                (block instanceof BlockSlab) ||
                (block instanceof BlockStairs)||
                (block instanceof BlockCactus)||
                (block instanceof BlockChest)||
                (block instanceof BlockEnderChest)||
                (block instanceof BlockSkull)||
                (block instanceof BlockPane)||
                (block instanceof BlockFence)||
                (block instanceof BlockWall)||
                (block instanceof BlockGlass)||
                (block instanceof BlockPistonBase)||
                (block instanceof BlockPistonExtension)||
                (block instanceof BlockPistonMoving)||
                (block instanceof BlockStainedGlass)||
                (block instanceof BlockTrapDoor);
    }

    private boolean unableToStand(int x, int y, int z) {
        final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        return block instanceof BlockFence || block instanceof BlockWall;
    }
}