/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import me.liuli.path.Cell;
import me.liuli.path.IWorldProvider;
import me.liuli.path.Pathfinder;
import net.ccbluex.liquidbounce.utils.block.MinecraftWorldProvider;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class PathUtils extends MinecraftInstance {
    public static List<Vec3> findBlinkPath(final double tpX, final double tpY, final double tpZ){
        return findBlinkPath(tpX, tpY, tpZ,5);
    }

    public static List<Vec3> findBlinkPath(final double tpX, final double tpY, final double tpZ,final double dist){
        return findBlinkPath(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tpX, tpY, tpZ, dist);
    }

    public static List<Vec3> findBlinkPath(double curX, double curY, double curZ, final double tpX, final double tpY, final double tpZ, final double dashDistance) {
        final MinecraftWorldProvider worldProvider = new MinecraftWorldProvider(mc.theWorld);
        final Pathfinder pathfinder = new Pathfinder(new Cell((int)curX, (int)curY, (int)curZ), new Cell((int)tpX, (int)tpY, (int)tpZ),
                Pathfinder.COMMON_NEIGHBORS, worldProvider);

        return simplifyPath(pathfinder.findPath(3000), dashDistance, worldProvider);
    }

    public static ArrayList<Vec3> simplifyPath(final ArrayList<Cell> path, final double dashDistance, final MinecraftWorldProvider worldProvider) {
        final ArrayList<Vec3> finalPath = new ArrayList<>();

        Cell cell = path.get(0);
        Vec3 vec3;
        Vec3 lastLoc = new Vec3(cell.x + 0.5, cell.y, cell.z + 0.5);
        Vec3 lastDashLoc = lastLoc;
        for (int i = 1; i < path.size() - 1; i++) {
            cell = path.get(i);
            vec3 = new Vec3(cell.x + 0.5, cell.y, cell.z + 0.5);
            boolean canContinue = true;
            if (vec3.squareDistanceTo(lastDashLoc) > dashDistance * dashDistance) {
                canContinue = false;
            } else {
                double smallX = Math.min(lastDashLoc.xCoord, vec3.xCoord);
                double smallY = Math.min(lastDashLoc.yCoord, vec3.yCoord);
                double smallZ = Math.min(lastDashLoc.zCoord, vec3.zCoord);
                double bigX = Math.max(lastDashLoc.xCoord, vec3.xCoord);
                double bigY = Math.max(lastDashLoc.yCoord, vec3.yCoord);
                double bigZ = Math.max(lastDashLoc.zCoord, vec3.zCoord);
                cordsLoop:
                for (int x = (int) smallX; x <= bigX; x++) {
                    for (int y = (int) smallY; y <= bigY; y++) {
                        for (int z = (int) smallZ; z <= bigZ; z++) {
                            if (worldProvider.isBlocked(x, y, z)) {
                                canContinue = false;
                                break cordsLoop;
                            }
                        }
                    }
                }
            }
            if (!canContinue) {
                finalPath.add(lastLoc);
                lastDashLoc = lastLoc;
            }
            lastLoc = vec3;
        }

        return finalPath;
    }
}
