/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

public final class PathUtils extends MinecraftInstance {
    public static List<Vec3> findBlinkPath(final double tpX, final double tpY, final double tpZ){
        return findBlinkPath(tpX, tpY, tpZ,5);
    }

    public static List<Vec3> findBlinkPath(final double tpX, final double tpY, final double tpZ,final double dist){
        return findBlinkPath(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,tpX,tpY,tpZ,dist);
    }

    public static List<Vec3> findBlinkPath(double curX, double curY, double curZ,final double tpX, final double tpY, final double tpZ, final double dashDistance) {
        Vec3 topFrom=new Vec3(curX,curY,curZ);
        Vec3 to=new Vec3(tpX,tpY,tpZ);

        if (!canPassThrow(new BlockPos(topFrom))) {
            topFrom = topFrom.addVector(0, 1, 0);
        }
        AStarCustomPathFinder pathfinder = new AStarCustomPathFinder(topFrom, to);
        pathfinder.compute();

        int i = 0;
        Vec3 lastLoc = null;
        Vec3 lastDashLoc = null;
        ArrayList<Vec3> path = new ArrayList<>();
        ArrayList<Vec3> pathFinderPath = pathfinder.getPath();
        for (Vec3 pathElm : pathFinderPath) {
            if (i == 0 || i == pathFinderPath.size() - 1) {
                if (lastLoc != null) {
                    path.add(lastLoc.addVector(0.5, 0, 0.5));
                }
                path.add(pathElm.addVector(0.5, 0, 0.5));
                lastDashLoc = pathElm;
            } else {
                boolean canContinue = true;
                if (pathElm.squareDistanceTo(lastDashLoc) > dashDistance * dashDistance) {
                    canContinue = false;
                } else {
                    double smallX = Math.min(lastDashLoc.xCoord, pathElm.xCoord);
                    double smallY = Math.min(lastDashLoc.yCoord, pathElm.yCoord);
                    double smallZ = Math.min(lastDashLoc.zCoord, pathElm.zCoord);
                    double bigX = Math.max(lastDashLoc.xCoord, pathElm.xCoord);
                    double bigY = Math.max(lastDashLoc.yCoord, pathElm.yCoord);
                    double bigZ = Math.max(lastDashLoc.zCoord, pathElm.zCoord);
                    cordsLoop:
                    for (int x = (int) smallX; x <= bigX; x++) {
                        for (int y = (int) smallY; y <= bigY; y++) {
                            for (int z = (int) smallZ; z <= bigZ; z++) {
                                if (!AStarCustomPathFinder.checkPositionValidity(x, y, z, false)) {
                                    canContinue = false;
                                    break cordsLoop;
                                }
                            }
                        }
                    }
                }
                if (!canContinue) {
                    path.add(lastLoc.addVector(0.5, 0, 0.5));
                    lastDashLoc = lastLoc;
                }
            }
            lastLoc = pathElm;
            i++;
        }

        return path;
    }

    private static boolean canPassThrow(BlockPos pos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(new net.minecraft.util.BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock();
        return block.getMaterial() == Material.air || block.getMaterial() == Material.plants || block.getMaterial() == Material.vine || block == Blocks.ladder || block == Blocks.water || block == Blocks.flowing_water || block == Blocks.wall_sign || block == Blocks.standing_sign;
    }

    public static List<Vector3d> findPath(final double tpX, final double tpY, final double tpZ, final double offset) {
        final List<Vector3d> positions = new ArrayList<>();
        final double steps = Math.ceil(getDistance(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tpX, tpY, tpZ) / offset);

        final double dX = tpX - mc.thePlayer.posX;
        final double dY = tpY - mc.thePlayer.posY;
        final double dZ = tpZ - mc.thePlayer.posZ;

        for(double d = 1D; d <= steps; ++d) {
            positions.add(new Vector3d(mc.thePlayer.posX + (dX * d) / steps, mc.thePlayer.posY + (dY * d) / steps, mc.thePlayer.posZ + (dZ * d) / steps));
        }

        return positions;
    }

    private static double getDistance(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double xDiff = x1 - x2;
        final double yDiff = y1 - y2;
        final double zDiff = z1 - z2;
        return MathHelper.sqrt_double(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }
}
