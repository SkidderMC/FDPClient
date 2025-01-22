/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;

@Mixin(GuiTextField.class)
public class MixinGuiTextField {

    @Shadow
    public int xPosition;

    @Shadow
    public int yPosition;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Redirect(method = "drawTextBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiTextField;getEnableBackgroundDrawing()Z"))
    private boolean injectClientDraw(GuiTextField instance) {
        if (instance.getEnableBackgroundDrawing()) {
            float radius = 1F;
            float width = 2.5F;

            if (instance.isFocused()) {
                // Some cool breathing effects
                width = 1f + (4f - 1f) * AnimationUtil.INSTANCE.breathe(1500F);
            }

            RenderUtils.INSTANCE.drawRoundedBorder(this.xPosition, this.yPosition + height, this.xPosition + this.width, this.yPosition + height, width - 0.5F, Color.BLUE.getRGB(), radius - 1F);
            RenderUtils.INSTANCE.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + height - 0.5F, new Color(0, 0, 0, 100).getRGB(), radius - 1F, RenderUtils.RoundedCorners.ALL);
            RenderUtils.INSTANCE.drawRoundedBorderedWithoutBottom(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, Color.BLACK.getRGB(), width, radius);
        }

        return false;
    }
}