// 
// Decompiled by Procyon v0.5.36
// 

package net.ccbluex.liquidbounce.utils.misc;

public enum Direction
{
    FORWARDS, 
    BACKWARDS;
    
    public Direction opposite() {
        if (this == Direction.FORWARDS) {
            return Direction.BACKWARDS;
        }
        return Direction.FORWARDS;
    }
}
