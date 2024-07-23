/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.ui.client.gui.GuiClientFixes;
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = GuiMultiplayer.class, priority = 1001)
public abstract class MixinGuiMultiplayer extends MixinGuiScreen {

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        // Detect ViaForge button
        GuiButton button = buttonList.stream().filter(b -> b.displayString.equals("ViaForge")).findFirst().orElse(null);

        int increase = 0;
        int yPosition = 8;

        if (button != null) {
            increase += 105;
            yPosition = Math.min(button.yPosition, 10);
        }

        buttonList.add(new GuiButton(997, 5 + increase, yPosition, 45, 20, "Fixes"));
        buttonList.add(new GuiButton(999, width - 104, yPosition, 98, 20, "Alt Manager"));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) throws IOException {
        switch (button.id) {
            case 997:
                mc.displayGuiScreen(new GuiClientFixes((GuiScreen) (Object) this));
                break;
            case 999:
                mc.displayGuiScreen(new GuiAltManager((GuiScreen) (Object) this));
                break;
        }
    }
}