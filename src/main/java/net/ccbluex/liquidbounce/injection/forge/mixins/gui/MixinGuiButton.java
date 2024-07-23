/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.client.BrandSpoofer;
import net.ccbluex.liquidbounce.features.module.modules.client.button.AbstractButtonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiButton.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButton extends Gui {

   @Shadow
   public boolean visible;
   @Shadow
   public int xPosition;
   @Shadow
   public int yPosition;
   @Shadow
   public int width;
   @Shadow
   public int height;
   @Shadow
   public boolean enabled;
   @Shadow
   public boolean hovered;

   @Shadow
   protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

   protected final AbstractButtonRenderer fDPClient$buttonRenderer = BrandSpoofer.INSTANCE.getButtonRenderer((GuiButton)(Object)this);

   /**
    * @author CCBlueX
    */
   @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
   public void drawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
      if(this.fDPClient$buttonRenderer != null) {
         if(!visible) {
            return;
         }

          // Render custom button renderer if available
         this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
         this.mouseDragged(mc, mouseX, mouseY);
         fDPClient$buttonRenderer.render(mouseX, mouseY, mc);
         fDPClient$buttonRenderer.drawButtonText(mc);
         ci.cancel();
      }
   }
}