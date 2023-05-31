/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.misc;

import net.ccbluex.liquidbounce.utils.timer.TimerUtils;

public abstract class Animation  {
    public TimerUtils timerUtil;
    protected int duration;
    protected double endPoint;
    protected Direction direction;
    
    public Animation(final int ms, final double endPoint) {
        this.timerUtil = new TimerUtils();
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = Direction.FORWARDS;
    }
    
    public Animation(final int ms, final double endPoint, final Direction direction) {
        this.timerUtil = new TimerUtils();
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
    }
    
    public boolean finished(final Direction direction) {
        return this.isDone() && this.direction.equals(direction);
    }
    
    public double getLinearOutput() {
        return 1.0 - this.timerUtil.getTime() / (double)this.duration * this.endPoint;
    }
    
    public double getEndPoint() {
        return this.endPoint;
    }
    
    public void setEndPoint(final double endPoint) {
        this.endPoint = endPoint;
    }
    
    public void reset() {
        this.timerUtil.reset();
    }
    
    public boolean isDone() {
        return this.timerUtil.hasTimeElapsed(this.duration);
    }
    
    public void changeDirection() {
        this.setDirection(this.direction.opposite());
    }
    
    public Direction getDirection() {
        return this.direction;
    }
    
    public void setDirection(final Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            this.timerUtil.setTime(System.currentTimeMillis() - (this.duration - Math.min(this.duration, this.timerUtil.getTime())));
        }
    }
    
    public void setDuration(final int duration) {
        this.duration = duration;
    }
    
    protected boolean correctOutput() {
        return false;
    }
    
    public double getOutput() {
        if (this.direction == Direction.FORWARDS) {
            if (this.isDone()) {
                return this.endPoint;
            }
            return this.getEquation((double)this.timerUtil.getTime()) * this.endPoint;
        }
        else {
            if (this.isDone()) {
                return 0.0;
            }
            if (this.correctOutput()) {
                final double revTime = (double)Math.min(this.duration, Math.max(0L, this.duration - this.timerUtil.getTime()));
                return this.getEquation(revTime) * this.endPoint;
            }
            return (1.0 - this.getEquation((double)this.timerUtil.getTime())) * this.endPoint;
        }
    }
    
    protected abstract double getEquation(final double p0);
}
