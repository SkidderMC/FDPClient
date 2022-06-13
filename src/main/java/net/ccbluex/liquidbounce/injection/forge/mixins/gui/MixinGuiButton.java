
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.module.modules.client.button.AbstractButtonRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

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

   private double animation = 0.0;
   private long lastUpdate=System.currentTimeMillis();

   protected final AbstractButtonRenderer buttonRenderer = LiquidBounce.moduleManager.getModule(HUD.class).getButtonRenderer((GuiButton)(Object)this);

   /**
    * @author liuli
    */
   @Overwrite
   public void drawButton(Minecraft mc, int mouseX, int mouseY) {
      if(!visible)
         return;

      this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
      this.mouseDragged(mc, mouseX, mouseY);
      long time=System.currentTimeMillis();
      double pct=(time-lastUpdate)/500D;

      if (this.hovered) {
         if(animation<1){
            animation+=pct;
         }
         if(animation>1){
            animation=1;
         }
      } else {
         if(animation>0){
            animation-=pct;
         }
         if(animation<0){
            animation=0;
         }
      }

      double percent = EaseUtils.INSTANCE.easeOutQuad(animation);
      RenderUtils.drawRect(this.xPosition,this.yPosition,this.xPosition + width,this.yPosition + height, new Color(31,31,31,150).getRGB());
      double half=this.width / 2.0;
      double center=this.xPosition + half;
      if(enabled)
         RenderUtils.drawRect(center - percent*(half), this.yPosition + this.height - 1, center + percent*(half), this.yPosition + this.height, Color.WHITE.getRGB());
      Fonts.font35.drawCenteredString(this.displayString,this.xPosition + this.width / 2F,this.yPosition+this.height/2F-Fonts.font40.getHeight()/2F+1,visible?Color.WHITE.getRGB():Color.GRAY.getRGB(),false);
      lastUpdate=time;
   }
}