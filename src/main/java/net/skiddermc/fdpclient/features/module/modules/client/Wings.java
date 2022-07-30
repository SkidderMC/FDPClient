/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.client;

import net.skiddermc.fdpclient.event.EventTarget;
import net.skiddermc.fdpclient.event.Render3DEvent;
import net.skiddermc.fdpclient.features.module.Module;
import net.skiddermc.fdpclient.features.module.ModuleCategory;
import net.skiddermc.fdpclient.features.module.ModuleInfo;
import net.skiddermc.fdpclient.utils.RenderWings;
import net.skiddermc.fdpclient.value.BoolValue;

@ModuleInfo(name = "Wings", category = ModuleCategory.CLIENT, array = false)
public class Wings extends Module {
    
    private final BoolValue onlyThirdPerson = new BoolValue("OnlyThirdPerson",true);

    @EventTarget
    public void onRenderPlayer(Render3DEvent event) {
        if (onlyThirdPerson.get() && mc.gameSettings.thirdPersonView == 0) {
            return;
        }
        
        RenderWings renderWings = new RenderWings();
        renderWings.renderWings(event.getPartialTicks());
    }

}
