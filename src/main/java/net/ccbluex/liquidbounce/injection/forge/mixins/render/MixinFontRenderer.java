/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.TextEvent;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {
    @ModifyVariable(method = "renderString", at = @At("HEAD"), ordinal = 0)
    private String renderString(String string) {
        if (string == null || LiquidBounce.eventManager == null)
            return string;

        final TextEvent textEvent = new TextEvent(string);
        LiquidBounce.eventManager.callEvent(textEvent);
        return textEvent.getText();
    }

    @ModifyVariable(method = "getStringWidth", at = @At("HEAD"), ordinal = 0)
    private String getStringWidth(String string) {
        if (string == null || LiquidBounce.eventManager == null)
            return string;

        final TextEvent textEvent = new TextEvent(string);
        LiquidBounce.eventManager.callEvent(textEvent);
        return textEvent.getText();
    }

}
