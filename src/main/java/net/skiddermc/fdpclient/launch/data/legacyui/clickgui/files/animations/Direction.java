/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.launch.data.legacyui.clickgui.files.animations;

public enum Direction {
    FORWARDS,
    BACKWARDS;

    public Direction opposite() {
        if (this == Direction.FORWARDS) {
            return Direction.BACKWARDS;
        } else return Direction.FORWARDS;
    }

}
