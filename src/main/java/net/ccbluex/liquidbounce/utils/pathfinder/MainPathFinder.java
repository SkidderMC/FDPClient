package net.ccbluex.liquidbounce.utils.pathfinder;

import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;

/**
 * The type Main path finder.
 */
public class MainPathFinder {
    private final ArrayList<Vec3> path = new ArrayList<>();

    /**
     * Instantiates a new Main path finder.
     *
     * @param startVec3 the start vec 3
     * @param endVec3   the end vec 3
     */
    public MainPathFinder(final Vec3 startVec3, final Vec3 endVec3) {
        Vec3 startVec31 = startVec3.addVector(0.0, 0.0, 0.0).floor();
        Vec3 endVec31 = endVec3.addVector(0.0, 0.0, 0.0).floor();
    }

    /**
     * Gets path.
     *
     * @return the path
     */
    public ArrayList<Vec3> getPath() {
        return this.path;
    }

    /**
     * Is valid boolean.
     *
     * @param x           the x
     * @param y           the y
     * @param z           the z
     * @param checkGround the check ground
     * @return the boolean
     */
    public static boolean isValid(final int x, final int y, final int z, final boolean checkGround) {
        final BlockPos block1 = new BlockPos(x, y, z);
        final BlockPos block2 = new BlockPos(x, y + 1, z);
        final BlockPos block3 = new BlockPos(x, y - 1, z);
        return !isNotPassable(block1) && !isNotPassable(block2)
                && (isNotPassable(block3) || !checkGround)
                && canWalkOn(block3);
    }

    private static boolean isNotPassable(final BlockPos block) {
        final Block b = MinecraftInstance.mc.theWorld.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ())).getBlock();

        return b.isFullBlock()
                || b instanceof BlockSlab
                || b instanceof BlockStairs
                || b instanceof BlockCactus
                || b instanceof BlockChest
                || b instanceof BlockEnderChest
                || b instanceof BlockSkull
                || b instanceof BlockPane
                || b instanceof BlockFence
                || b instanceof BlockWall
                || b instanceof BlockGlass
                || b instanceof BlockPistonBase
                || b instanceof BlockPistonExtension
                || b instanceof BlockPistonMoving
                || b instanceof BlockStainedGlass
                || b instanceof BlockTrapDoor
                || b instanceof BlockEndPortalFrame
                || b instanceof BlockEndPortal
                || b instanceof BlockBed
                || b instanceof BlockWeb
                || b instanceof BlockBarrier
                || b instanceof BlockLadder
                || b instanceof BlockCarpet;
    }

    private static boolean canWalkOn(final BlockPos block) {
        return !(MinecraftInstance.mc.theWorld.getBlockState(new BlockPos(block.getX(), block.getY(),
                block.getZ())).getBlock() instanceof BlockFence)
                && !(MinecraftInstance.mc.theWorld.getBlockState(new BlockPos(block.getX(), block.getY(),
                block.getZ())).getBlock() instanceof BlockWall);
    }

    /**
     * Can pass through boolean.
     *
     * @param pos the pos
     * @return the boolean
     */
    public static boolean canPassThrough(final BlockPos pos) {
        final Block block = MinecraftInstance.mc.theWorld
                .getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock();
        return block.getMaterial() == Material.air || block.getMaterial() == Material.plants
                || block.getMaterial() == Material.vine || block == Blocks.ladder || block == Blocks.water
                || block == Blocks.flowing_water || block == Blocks.wall_sign || block == Blocks.standing_sign;
    }

    /**
     * Compute path array list.
     *
     * @param topFrom the top from
     * @param to      the to
     * @return the array list
     */
    public static ArrayList<Vec3> computePath(Vec3 topFrom, final Vec3 to) {
        if (!canPassThrough(new BlockPos(topFrom.mc()))) {
            topFrom = topFrom.addVector(0.0, 1.0, 0.0);
        }

        final PathFinder pathfinder = new PathFinder(topFrom, to);
        pathfinder.compute();
        int i = 0;
        Vec3 lastLoc = null;
        Vec3 lastDashLoc = null;
        final ArrayList<Vec3> path = new ArrayList<>();
        final ArrayList<Vec3> pathFinderPath = pathfinder.getPath();

        for (final Vec3 pathElm : pathFinderPath) {
            if (i == 0 || i == pathFinderPath.size() - 1) {
                if (lastLoc != null) {
                    path.add(lastLoc.addVector(0.5, 0.0, 0.5));
                }
                path.add(pathElm.addVector(0.5, 0.0, 0.5));
                lastDashLoc = pathElm;
            } else {
                boolean canContinue = true;
                if (pathElm.squareDistanceTo(lastDashLoc) > 5 * 5) {
                    canContinue = false;
                } else {
                    final double smallX = Math.min(lastDashLoc.getX(), pathElm.getX());
                    final double smallY = Math.min(lastDashLoc.getY(), pathElm.getY());
                    final double smallZ = Math.min(lastDashLoc.getZ(), pathElm.getZ());
                    final double bigX = Math.max(lastDashLoc.getX(), pathElm.getX());
                    final double bigY = Math.max(lastDashLoc.getY(), pathElm.getY());
                    final double bigZ = Math.max(lastDashLoc.getZ(), pathElm.getZ());
                    int x = (int) smallX;
                    block1:
                    while (x <= bigX) {
                        int y2 = (int) smallY;
                        while (y2 <= bigY) {
                            int z = (int) smallZ;
                            while (z <= bigZ) {
                                if (!isValid(x, y2, z, false)) {
                                    canContinue = false;
                                    break block1;
                                }
                                ++z;
                            }
                            ++y2;
                        }
                        ++x;
                    }
                }

                if (!canContinue) {
                    path.add(lastLoc.addVector(0.5, 0.0, 0.5));
                    lastDashLoc = lastLoc;
                }
            }
            lastLoc = pathElm;
            ++i;
        }
        return path;
    }
}