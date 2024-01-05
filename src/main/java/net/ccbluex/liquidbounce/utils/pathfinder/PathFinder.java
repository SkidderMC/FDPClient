package net.ccbluex.liquidbounce.utils.pathfinder;


import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.block.*;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

/**
 * The type Path finder.
 */
public class PathFinder {
    private final Vec3 startVec3Path;
    private final Vec3 endVec3Path;
    private ArrayList<Vec3> path = new ArrayList<>();
    private final ArrayList<PathHub> pathHubs = new ArrayList<>();
    private final ArrayList<PathHub> workingPathHubList = new ArrayList<>();
    private static final Vec3[] directions = new Vec3[]{new Vec3(1.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 0.0),
            new Vec3(0.0, 0.0, 1.0), new Vec3(0.0, 0.0, -1.0)};

    /**
     * Instantiates a new Path finder.
     *
     * @param startVec3Path the start vec 3 path
     * @param endVec3Path   the end vec 3 path
     */
    public PathFinder(final Vec3 startVec3Path, final Vec3 endVec3Path) {
        this.startVec3Path = startVec3Path.addVector(0.0, 0.0, 0.0).floor();
        this.endVec3Path = endVec3Path.addVector(0.0, 0.0, 0.0).floor();
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
     * Compute.
     */
    public void compute() {
        this.compute(1000, 4);
    }

    /**
     * Compute.
     *
     * @param loops the loops
     * @param depth the depth
     */
    public void compute(final int loops, final int depth) {
        this.path.clear();
        this.workingPathHubList.clear();

        final ArrayList<Vec3> initPath = new ArrayList<>();
        initPath.add(this.startVec3Path);

        this.workingPathHubList
                .add(new PathHub(this.startVec3Path, null, initPath, this.startVec3Path.squareDistanceTo(this.endVec3Path), 0.0, 0.0));

        block0:
        for (int i = 0; i < loops; ++i) {
            this.workingPathHubList.sort(new CompareHub());
            int j = 0;

            if (this.workingPathHubList.size() == 0) {
                break;
            }

            for (final PathHub pathHub : new ArrayList<>(this.workingPathHubList)) {
                final Vec3 loc2;

                if (++j > depth) {
                    continue block0;
                }

                this.workingPathHubList.remove(pathHub);
                this.pathHubs.add(pathHub);

                for (final Vec3 direction : directions) {
                    final Vec3 loc = pathHub.getLoc().add(direction).floor();
                    if (isValid(loc, false) && this.putHub(pathHub, loc, 0.0)) {
                        break block0;
                    }
                }

                final Vec3 loc1 = pathHub.getLoc().addVector(0.0, 1.0, 0.0).floor();
                if (isValid(loc1, false) && this.putHub(pathHub, loc1, 0.0)
                        || isValid(loc2 = pathHub.getLoc().addVector(0.0, -1.0, 0.0).floor(), false)
                        && this.putHub(pathHub, loc2, 0.0)) {
                    break block0;
                }
            }
        }

        this.pathHubs.sort(new CompareHub());
        this.path = this.pathHubs.get(0).getPathway();
    }

    /**
     * Is valid boolean.
     *
     * @param loc         the loc
     * @param checkGround the check ground
     * @return the boolean
     */
    public static boolean isValid(final Vec3 loc, final boolean checkGround) {
        return isValid((int) loc.getX(), (int) loc.getY(), (int) loc.getZ(),
                checkGround);
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
     * Does hub exist at path hub.
     *
     * @param loc the loc
     * @return the path hub
     */
    public PathHub doesHubExistAt(final Vec3 loc) {
        for (final PathHub pathHub : this.pathHubs) {
            if (pathHub.getLoc().getX() != loc.getX() || pathHub.getLoc().getY() != loc.getY()
                    || pathHub.getLoc().getZ() != loc.getZ()) {
                continue;
            }
            return pathHub;
        }

        for (final PathHub pathHub : this.workingPathHubList) {
            if (pathHub.getLoc().getX() != loc.getX() || pathHub.getLoc().getY() != loc.getY()
                    || pathHub.getLoc().getZ() != loc.getZ()) {
                continue;
            }
            return pathHub;
        }
        return null;
    }

    /**
     * Put hub boolean.
     *
     * @param parent the parent
     * @param loc    the loc
     * @param cost   the cost
     * @return the boolean
     */
    public boolean putHub(final PathHub parent, final Vec3 loc, final double cost) {
        final PathHub existingPathHub = this.doesHubExistAt(loc);
        double totalCost = cost;

        if (parent != null) {
            totalCost += parent.getMaxCost();
        }

        if (existingPathHub == null) {
            if (loc.getX() == this.endVec3Path.getX() && loc.getY() == this.endVec3Path.getY()
                    && loc.getZ() == this.endVec3Path.getZ()
                    || loc.squareDistanceTo(this.endVec3Path) <= 1) {
                this.path.clear();
                this.path = Objects.requireNonNull(parent).getPathway();
                this.path.add(loc);
                return true;
            }

            final ArrayList<Vec3> path = new ArrayList<>(Objects.requireNonNull(parent).getPathway());
            path.add(loc);
            this.workingPathHubList.add(new PathHub(loc, parent, path, loc.squareDistanceTo(this.endVec3Path), cost, totalCost));
        } else if (existingPathHub.getCurrentCost() > cost) {
            final ArrayList<Vec3> path = new ArrayList<>(Objects.requireNonNull(parent).getPathway());
            path.add(loc);
            existingPathHub.setLoc(loc);
            existingPathHub.setParentPathHub(parent);
            existingPathHub.setPathway(path);
            existingPathHub.setSqDist(loc.squareDistanceTo(this.endVec3Path));
            existingPathHub.setCurrentCost(cost);
            existingPathHub.setMaxCost(totalCost);
        }
        return false;
    }

    /**
     * The type Compare hub.
     */
    public static class CompareHub implements Comparator<PathHub> {
        @Override
        public int compare(final PathHub o1, final PathHub o2) {
            return (int) (o1.getSqDist() + o1.getMaxCost()
                    - (o2.getSqDist() + o2.getMaxCost()));
        }
    }
}