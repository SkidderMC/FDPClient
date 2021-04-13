
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.AnimationUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;


@Mixin(GuiButton.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButton extends Gui {
   public int cs;

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
   protected boolean hovered;

   @Shadow
   public boolean enabled;

   @Shadow
   protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

   @Shadow
   public String displayString;

   @Shadow
   @Final
   protected static ResourceLocation buttonTextures;
   private float cut;
   private float alpha;
   private float anim1;

   protected double animation = 0.10000000149011612;

   protected int getHoverState(boolean mouseOver) {
      int i = 1;
      if (!this.enabled) {
         i = 0;
      } else if (mouseOver) {
         i = 2;
      }
      return i;
   }

   /**
    * @author Me
    * @Version 1
    */
//   @Overwrite
//   public void drawButton(Minecraft mc, int mouseX, int mouseY) {
//      if (this.visible) {
//         FontRenderer fontrenderer = Fonts.font35;
//         GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
//         this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
//         GlStateManager.enableBlend();
//         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//         GlStateManager.blendFunc(770, 771);
//         this.animation = RenderUtils.getAnimationState(this.animation, this.hovered ? 0.2f : 0.1f, 0.5);
//         RenderUtils.drawRect(this.xPosition, this.yPosition + 2, this.xPosition + this.width, (float) (this.yPosition + this.height) + 1.5f, new Color(0x006fb6).brighter().getRGB());
//         RenderUtils.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 0xFF262626);
//         RenderUtils.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, (float) (this.yPosition + this.height) + 1.5f, ClientUtils.reAlpha(Colors.BLACK.c, (float) this.animation * 2.0f));
//         this.mouseDragged(mc, mouseX, mouseY);
//         fontrenderer.drawStringWithShadow(ClientUtils.removeColorCode(this.displayString), this.xPosition + (this.width - fontrenderer.getStringWidth(ClientUtils.removeColorCode(this.displayString))) / 2, this.yPosition + (this.height - 12) / 2 + 3, Colors.WHITE.c);
//      }
//   }

   private void updatefade() {
      if (this.enabled)
         if (this.hovered) {
            this.alpha += 25;
            if (this.alpha >= 210)
               this.alpha = 210;
         } else {
            this.alpha -= 25;
            if (this.alpha <= 120)
               this.alpha = 120;
         }
   }

   @Overwrite
   public void drawButton(Minecraft mc, int mouseX, int mouseY) {
      if (this.visible) {
         GameFontRenderer fr = Fonts.font35;
         GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
         this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
         this.animation = AnimationUtils.getAnimationState(this.animation, (this.hovered ? 0.2f : 0.1f), 5.0);
         this.anim1 = this.hovered ? (float) AnimationUtils.animate(18.0, this.anim1, 0.25) : (float) AnimationUtils.animate(0.0, this.anim1, 0.25);
         if (this.enabled) {
            RenderUtils.drawGradientSideways(this.xPosition, ((float)this.yPosition + 18.0f - this.anim1), (this.xPosition + this.width), (this.yPosition + this.height), new Color(10, 90, 205, 220).getRGB(), new Color(1, 190, 206, 220).getRGB());
         } else {
            RenderUtils.drawGradientSideways(this.xPosition, ((float)this.yPosition + 18.0f - this.anim1), (this.xPosition + this.width), (this.yPosition + this.height), new Color(255, 10, 10, 220).getRGB(), new Color(255, 111, 0, 220).getRGB());
         }
         RenderUtils.drawRect((float)this.xPosition, (float)this.yPosition, (float)(this.xPosition + this.width), ((float)(this.yPosition + this.height) - 1.5f), new Color(0, 0, 0, 125).getRGB());
         RenderUtils.drawRect((float)this.xPosition, (float)this.yPosition, (float)(this.xPosition + this.width), (float)(this.yPosition + this.height), new Color(255, 255, 255, 25).getRGB());
         this.mouseDragged(mc, mouseX, mouseY);
         fr.drawStringWithShadow(this.displayString, (float)this.xPosition + ((float)this.width - (float)fr.getStringWidth(this.displayString) + 2.0f) / 2.0f, (float)(this.yPosition + (this.height - 8) / 2) + 1.0f, -1);
      }
   }
}
