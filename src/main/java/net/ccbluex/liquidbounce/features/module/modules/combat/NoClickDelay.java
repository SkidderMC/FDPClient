/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat;

import net.ccbluex.liquidbounce.event.Listener;
import net.ccbluex.liquidbounce.event.PreMotionEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;

@ModuleInfo(name = "NoClickDelay", category = ModuleCategory.COMBAT)
public class NoClickDelay extends Module {

    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.leftClickCounter = 0;
        }
    };
}


