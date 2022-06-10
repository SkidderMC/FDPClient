package net.ccbluex.liquidbounce.features.module.modules.client;

import java.awt.Color;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.RenderWings;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


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
