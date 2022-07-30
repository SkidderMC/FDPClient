
package net.skiddermc.fdpclient.injection.forge.mixins.gui;

import net.skiddermc.fdpclient.FDPClient;
import net.skiddermc.fdpclient.features.module.modules.client.HUD;
import net.skiddermc.fdpclient.features.module.modules.client.button.AbstractButtonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {

   @Shadow
   public int xPosition;

   @Shadow
   public int yPosition;

   @Shadow
   public int width;

   @Shadow
   public int height;

   @Shadow
   public boolean hovered;

   @Shadow
   public boolean enabled;

   @Shadow
   public boolean visible;

   @Final
   @Shadow
   protected static ResourceLocation buttonTextures;

   @Shadow
   public abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

   @Shadow
   protected abstract int getHoverState(boolean p_getHoverState_1_);

   @Shadow
   public int packedFGColour;

   @Shadow
   public String displayString;

   protected final AbstractButtonRenderer buttonRenderer = FDPClient.moduleManager.getModule(HUD.class).getButtonRenderer((GuiButton)(Object)this);

   /**
    * @author liuli
    */
   @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
   public void drawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
      if(this.buttonRenderer != null) {
         if(!visible) {
            return;
         }
         this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
         this.mouseDragged(mc, mouseX, mouseY);
         buttonRenderer.render(mouseX, mouseY, mc);
         buttonRenderer.drawButtonText(mc);
         ci.cancel();
      }
   }
}
