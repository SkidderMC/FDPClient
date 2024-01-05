package net.ccbluex.liquidbounce.utils.pathfinder;

/**
 * The type Vec 3.
 */
public class Vec3 {
    private final double x;
    private final double y;
    private final double z;

    /**
     * Instantiates a new Vec 3.
     *
     * @param x  the x
     * @param y2 the y 2
     * @param z  the z
     */
    public Vec3(final double x, final double y2, final double z) {
        this.x = x;
        this.y = y2;
        this.z = z;
    }

    /**
     * Gets x.
     *
     * @return the x
     */
    public double getX() {
        return this.x;
    }

    /**
     * Gets y.
     *
     * @return the y
     */
    public double getY() {
        return this.y;
    }

    /**
     * Gets z.
     *
     * @return the z
     */
    public double getZ() {
        return this.z;
    }

    /**
     * Add vector vec 3.
     *
     * @param x  the x
     * @param y2 the y 2
     * @param z  the z
     * @return the vec 3
     */
    public Vec3 addVector(final double x, final double y2, final double z) {
        return new Vec3(this.x + x, this.y + y2, this.z + z);
    }

    /**
     * Floor vec 3.
     *
     * @return the vec 3
     */
    public Vec3 floor() {
        return new Vec3(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
    }

    /**
     * Square distance to double.
     *
     * @param v the v
     * @return the double
     */
    public double squareDistanceTo(final Vec3 v) {
        return Math.pow(v.x - this.x, 2.0) + Math.pow(v.y - this.y, 2.0) + Math.pow(v.z - this.z, 2.0);
    }

    /**
     * Add vec 3.
     *
     * @param v the v
     * @return the vec 3
     */
    public Vec3 add(final Vec3 v) {
        return this.addVector(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Mc net . minecraft . util . vec 3.
     *
     * @return the net . minecraft . util . vec 3
     */
    public net.minecraft.util.Vec3 mc() {
        return new net.minecraft.util.Vec3(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return "[" + this.x + ";" + this.y + ";" + this.z + "]";
    }
}