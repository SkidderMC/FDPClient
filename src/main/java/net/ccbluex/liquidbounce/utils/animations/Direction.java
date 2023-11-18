package net.ccbluex.liquidbounce.utils.animations;
public enum Direction {
    FORWARDS,
    BACKWARDS;

    public Direction opposite() {
        if (this == Direction.FORWARDS) {
            return Direction.BACKWARDS;
        } else return Direction.FORWARDS;
    }


    public boolean forwards() {
        return this == Direction.FORWARDS;
    }

    public boolean backwards() {
        return this == Direction.BACKWARDS;
    }

}
