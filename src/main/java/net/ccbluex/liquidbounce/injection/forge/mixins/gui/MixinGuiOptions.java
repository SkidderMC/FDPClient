package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiOptions.class)
public class MixinGuiOptions extends GuiScreen {
    @Override
    public void onGuiClosed() {
        mc.gameSettings.saveOptions();
    }
}
