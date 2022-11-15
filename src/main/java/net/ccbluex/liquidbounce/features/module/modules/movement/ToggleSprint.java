/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.injection.forge.mixins.client.IMixinKeyBinding;
import net.minecraft.client.Minecraft;

@ModuleInfo(name = "ToggleSprint", category = ModuleCategory.MOVEMENT)
public final class ToggleSprint extends Module {
    boolean enabled;
    public void onEnable() {
        ((IMixinKeyBinding) Minecraft.getMinecraft().gameSettings.keyBindSprint).setPressed(true);
        enabled = true;
    }

    @EventTarget
    public void UpdateEvent(final UpdateEvent event) { if(enabled){ ((IMixinKeyBinding) Minecraft.getMinecraft().gameSettings.keyBindSprint).setPressed(true); } else{((IMixinKeyBinding) Minecraft.getMinecraft().gameSettings.keyBindSprint).setPressed(false);} }

    public void onDisable() {
        ((IMixinKeyBinding) Minecraft.getMinecraft().gameSettings.keyBindSprint).setPressed(false);
        enabled = false;
    }
}
