package net.skiddermc.fdpclient.utils.math;

import net.skiddermc.fdpclient.utils.MinecraftInstance;
import net.minecraft.block.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AStarCustomPathFinder extends MinecraftInstance {
    private Vec4 startVec4;
    private Vec4 endVec4;
    private ArrayList<Vec4> path = new ArrayList<>();
    private ArrayList<Hub> hubs = new ArrayList<>();
    private ArrayList<Hub> hubsToWork = new ArrayList<>();
    private double minDistanceSquared = 9;
    private boolean nearest = true;

    private static Vec4[] flatCardinalDirections = {
            new Vec4(1, 0, 0),
            new Vec4(-1, 0, 0),
            new Vec4(0, 0, 1),
            new Vec4(0, 0, -1)
    };

    public AStarCustomPathFinder(Vec4 startVec4, Vec4 endVec4) {
        this.startVec4 = startVec4.addVector(0, 0, 0).floor();
        this.endVec4 = endVec4.addVector(0, 0, 0).floor();
    }

    public ArrayList<Vec4> getPath() {
        return path;
    }

    public void compute() {
        compute(1000, 4);
    }

    public void compute(int loops, int depth) {
        path.clear();
        hubsToWork.clear();
        ArrayList<Vec4> initPath = new ArrayList<Vec4>();
        initPath.add(startVec4);
        hubsToWork.add(new Hub(startVec4, null, initPath, startVec4.squareDistanceTo(endVec4), 0, 0));
        search:
        for (int i = 0; i < loops; i++) {
            Collections.sort(hubsToWork, new CompareHub());
            int j = 0;
            if (hubsToWork.size() == 0) {
                break;
            }
            for (Hub hub : new ArrayList<Hub>(hubsToWork)) {
                j++;
                if (j > depth) {
                    break;
                } else {
                    hubsToWork.remove(hub);
                    hubs.add(hub);

                    for (Vec4 direction : flatCardinalDirections) {
                        Vec4 loc = hub.getLoc().add(direction).floor();
                        if (checkPositionValidity(loc, false)) {
                            if (addHub(hub, loc, 0)) {
                                break search;
                            }
                        }
                    }

                    Vec4 loc1 = hub.getLoc().addVector(0, 1, 0).floor();
                    if (checkPositionValidity(loc1, false)) {
                        if (addHub(hub, loc1, 0)) {
                            break search;
                        }
                    }

                    Vec4 loc2 = hub.getLoc().addVector(0, -1, 0).floor();
                    if (checkPositionValidity(loc2, false)) {
                        if (addHub(hub, loc2, 0)) {
                            break search;
                        }
                    }
                }
            }
        }
        if (nearest) {
            Collections.sort(hubs, new CompareHub());
            path = hubs.get(0).getPath();
        }
    }

    public static boolean checkPositionValidity(Vec3 vec32) {
        BlockPos pos = new BlockPos(vec32);
        if (AStarCustomPathFinder.isBlockSolid(pos) || AStarCustomPathFinder.isBlockSolid(pos.add(0, 1, 0))) {
            return false;
        }
        return AStarCustomPathFinder.isSafeToWalkOn(pos.add(0, -1, 0));
    }

    public static boolean checkPositionValidity(Vec4 loc, boolean checkGround) {
        return checkPositionValidity((int) loc.getX(), (int) loc.getY(), (int) loc.getZ(), checkGround);
    }

    public static boolean checkPositionValidity(int x, int y, int z, boolean checkGround) {
        BlockPos block1 = new BlockPos(x, y, z);
        BlockPos block2 = new BlockPos(x, y + 1, z);
        BlockPos block3 = new BlockPos(x, y - 1, z);
        return !isBlockSolid(block1) && !isBlockSolid(block2) && (isBlockSolid(block3) || !checkGround) && isSafeToWalkOn(block3);
    }

    private static boolean isBlockSolid(BlockPos block) {
        return mc.theWorld.getBlockState(block).getBlock().isFullBlock() ||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockSlab) ||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockStairs)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockCactus)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockChest)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockEnderChest)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockSkull)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockPane)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockFence)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockWall)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockGlass)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockPistonBase)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockPistonExtension)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockPistonMoving)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockStainedGlass)||
                (mc.theWorld.getBlockState(block).getBlock() instanceof BlockTrapDoor);
    }

    private static boolean isSafeToWalkOn(BlockPos block) {
        return !(mc.theWorld.getBlockState(block).getBlock() instanceof BlockFence) &&
                !(mc.theWorld.getBlockState(block).getBlock() instanceof BlockWall);
    }

    public Hub isHubExisting(Vec4 loc) {
        for (Hub hub : hubs) {
            if (hub.getLoc().getX() == loc.getX() && hub.getLoc().getY() == loc.getY() && hub.getLoc().getZ() == loc.getZ()) {
                return hub;
            }
        }
        for (Hub hub : hubsToWork) {
            if (hub.getLoc().getX() == loc.getX() && hub.getLoc().getY() == loc.getY() && hub.getLoc().getZ() == loc.getZ()) {
                return hub;
            }
        }
        return null;
    }

    public boolean addHub(Hub parent, Vec4 loc, double cost) {
        Hub existingHub = isHubExisting(loc);
        double totalCost = cost;
        if (parent != null) {
            totalCost += parent.getTotalCost();
        }
        if (existingHub == null) {
            if ((loc.getX() == endVec4.getX() && loc.getY() == endVec4.getY() && loc.getZ() == endVec4.getZ()) || (minDistanceSquared != 0 && loc.squareDistanceTo(endVec4) <= minDistanceSquared)) {
                path.clear();
                path = parent.getPath();
                path.add(loc);
                return true;
            } else {
                ArrayList<Vec4> path = new ArrayList<Vec4>(parent.getPath());
                path.add(loc);
                hubsToWork.add(new Hub(loc, parent, path, loc.squareDistanceTo(endVec4), cost, totalCost));
            }
        } else if (existingHub.getCost() > cost) {
            ArrayList<Vec4> path = new ArrayList<Vec4>(parent.getPath());
            path.add(loc);
            existingHub.setLoc(loc);
            existingHub.setParent(parent);
            existingHub.setPath(path);
            existingHub.setSquareDistanceToFromTarget(loc.squareDistanceTo(endVec4));
            existingHub.setCost(cost);
            existingHub.setTotalCost(totalCost);
        }
        return false;
    }

    private class Hub {
        private Vec4 loc = null;
        private Hub parent = null;
        private ArrayList<Vec4> path;
        private double squareDistanceToFromTarget;
        private double cost;
        private double totalCost;

        public Hub(Vec4 loc, Hub parent, ArrayList<Vec4> path, double squareDistanceToFromTarget, double cost, double totalCost) {
            this.loc = loc;
            this.parent = parent;
            this.path = path;
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
            this.cost = cost;
            this.totalCost = totalCost;
        }

        public Vec4 getLoc() {
            return loc;
        }

        public Hub getParent() {
            return parent;
        }

        public ArrayList<Vec4> getPath() {
            return path;
        }

        public double getSquareDistanceToFromTarget() {
            return squareDistanceToFromTarget;
        }

        public double getCost() {
            return cost;
        }

        public void setLoc(Vec4 loc) {
            this.loc = loc;
        }

        public void setParent(Hub parent) {
            this.parent = parent;
        }

        public void setPath(ArrayList<Vec4> path) {
            this.path = path;
        }

        public void setSquareDistanceToFromTarget(double squareDistanceToFromTarget) {
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public double getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(double totalCost) {
            this.totalCost = totalCost;
        }
    }

    public class CompareHub implements Comparator<Hub> {
        @Override
        public int compare(Hub o1, Hub o2) {
            return (int) (
                    (o1.getSquareDistanceToFromTarget() + o1.getTotalCost()) - (o2.getSquareDistanceToFromTarget() + o2.getTotalCost())
            );
        }
    }
}
