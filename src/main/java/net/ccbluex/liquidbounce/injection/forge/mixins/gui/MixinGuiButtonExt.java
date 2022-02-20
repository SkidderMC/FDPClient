package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiButtonExt.class)
public abstract class MixinGuiButtonExt extends MixinGuiButton {

    /**
     * @author CCBlueX
     */
    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    public void drawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        if(this.buttonRenderer != null) {
            if(!visible) {
                return;
            }
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            buttonRenderer.render(mouseX, mouseY, mc);

            mc.getTextureManager().bindTexture(buttonTextures);
            mouseDragged(mc, mouseX, mouseY);
            GlStateManager.resetColor();

            buttonRenderer.drawButtonText(mc);
            ci.cancel();
        }
    }
}