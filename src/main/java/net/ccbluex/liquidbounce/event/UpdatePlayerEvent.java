package net.ccbluex.liquidbounce.event;

public class UpdatePlayerEvent extends MultiTypeEvent {

    public float yaw, pitch;
    public double x, y, z;

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean onGround;

    public UpdatePlayerEvent(float yaw, float pitch, double x, double y, double z, boolean onGround, EventType type){
        super(type);
        this.yaw = yaw;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }

    public void setType(EventType t) {
        this.type = t;
    }

    public boolean pre() {
        return this.type == EventType.PRE;
    }

    public boolean post() {
        return this.type == EventType.POST;
    }
}
