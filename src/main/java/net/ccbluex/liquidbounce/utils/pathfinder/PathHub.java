package net.ccbluex.liquidbounce.utils.pathfinder;

import java.util.ArrayList;

/**
 * The type Path hub.
 */
public class PathHub {
    private Vec3 loc;
    private ArrayList<Vec3> pathway;
    private double sqDist;
    private double currentCost;
    private double maxCost;

    /**
     * Instantiates a new Path hub.
     *
     * @param loc           the loc
     * @param parentPathHub the parent path hub
     * @param pathway       the pathway
     * @param sqDist        the sq dist
     * @param currentCost   the current cost
     * @param maxCost       the max cost
     */
    public PathHub(final Vec3 loc, final PathHub parentPathHub, final ArrayList<Vec3> pathway,
                   final double sqDist, final double currentCost, final double maxCost) {
        this.loc = loc;
        this.pathway = pathway;
        this.sqDist = sqDist;
        this.currentCost = currentCost;
        this.maxCost = maxCost;
    }

    /**
     * Gets loc.
     *
     * @return the loc
     */
    public Vec3 getLoc() {
        return this.loc;
    }

    /**
     * Gets pathway.
     *
     * @return the pathway
     */
    public ArrayList<Vec3> getPathway() {
        return this.pathway;
    }

    /**
     * Gets sq dist.
     *
     * @return the sq dist
     */
    public double getSqDist() {
        return this.sqDist;
    }

    /**
     * Gets current cost.
     *
     * @return the current cost
     */
    public double getCurrentCost() {
        return this.currentCost;
    }

    /**
     * Sets loc.
     *
     * @param loc the loc
     */
    public void setLoc(final Vec3 loc) {
        this.loc = loc;
    }

    /**
     * Sets parent path hub.
     *
     * @param parentPathHub the parent path hub
     */
    public void setParentPathHub(final PathHub parentPathHub) {
    }

    /**
     * Sets pathway.
     *
     * @param pathway the pathway
     */
    public void setPathway(final ArrayList<Vec3> pathway) {
        this.pathway = pathway;
    }

    /**
     * Sets sq dist.
     *
     * @param sqDist the sq dist
     */
    public void setSqDist(final double sqDist) {
        this.sqDist = sqDist;
    }

    /**
     * Sets current cost.
     *
     * @param currentCost the current cost
     */
    public void setCurrentCost(final double currentCost) {
        this.currentCost = currentCost;
    }

    /**
     * Gets max cost.
     *
     * @return the max cost
     */
    public double getMaxCost() {
        return this.maxCost;
    }

    /**
     * Sets max cost.
     *
     * @param maxCost the max cost
     */
    public void setMaxCost(final double maxCost) {
        this.maxCost = maxCost;
    }
}