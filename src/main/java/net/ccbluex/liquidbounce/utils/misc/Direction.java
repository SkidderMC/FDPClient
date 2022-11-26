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
