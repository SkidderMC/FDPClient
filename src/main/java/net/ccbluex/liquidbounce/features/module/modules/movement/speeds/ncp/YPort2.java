/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp;

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;

public class YPort2 extends SpeedMode {

    public YPort2() {
        super("YPort2");
    }

    @Override
    public void onMotion() {
        if(mc.thePlayer.isOnLadder() || mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isInWeb || !MovementUtils.isMoving())
            return;

        if(mc.thePlayer.onGround)
            mc.thePlayer.jump();
        else
            mc.thePlayer.motionY = -1D;

        MovementUtils.strafe();
    }
}
