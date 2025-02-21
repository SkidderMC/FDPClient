/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.client.BrandSpoofer;
import net.ccbluex.liquidbounce.features.module.modules.client.button.AbstractButtonRenderer;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.resetColor;

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

   @Shadow
   public int id;

   @Unique
   private long startTime = -1L;
   @Unique
   private boolean lastHover = false;
   @Unique
   private float progress = xPosition;

   protected final AbstractButtonRenderer fDPClient$buttonRenderer = BrandSpoofer.INSTANCE.getButtonRenderer((GuiButton)(Object)this);

   /**
    * @author CCBlueX
    */
   @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
   public void drawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
      if (!visible) {
         return;
      }

      hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

      float supposedWidth = width;

      if ((Object) this instanceof GuiOptionSlider) {
         supposedWidth *= ((GuiOptionSlider) (Object) this).sliderValue;
         hovered = true;
      }

      if ((Object) this instanceof GuiScreenOptionsSounds.Button) {
         supposedWidth *= ((GuiScreenOptionsSounds.Button) (Object) this).field_146156_o;
         hovered = true;
      }

      if (hovered != lastHover) {
         if (System.currentTimeMillis() - startTime > 200L) {
            startTime = System.currentTimeMillis();
         }
         lastHover = hovered;
      }

      long elapsed = System.currentTimeMillis() - startTime;

      float startingPos = enabled && hovered ? xPosition : progress;
      float endingPos = enabled && hovered ? xPosition + supposedWidth : xPosition;

      progress = (int) (startingPos + (endingPos - startingPos) * MathHelper.clamp_float(elapsed / 200f, 0f, 1f));

      float radius = 2.5F;

      RenderUtils.INSTANCE.withClipping(() -> {
         RenderUtils.INSTANCE.drawRoundedRect(xPosition, yPosition, xPosition + width, yPosition + height, enabled ? new Color(0F, 0F, 0F, 120 / 255f).getRGB() : new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB(), radius, RenderUtils.RoundedCorners.ALL);
         return null;
      }, () -> {
         if (enabled && progress != xPosition) {
            // Draw blue overlay
            RenderUtils.INSTANCE.drawGradientRect(xPosition, yPosition, progress, yPosition + height, Color.CYAN.darker().getRGB(), Color.BLUE.darker().getRGB(), 0F);
         }
         return null;
      });

      mc.getTextureManager().bindTexture(buttonTextures);
      mouseDragged(mc, mouseX, mouseY);

      if (fDPClient$buttonRenderer == null) {
         AWTFontRenderer.Companion.setAssumeNonVolatile(true);
         FontRenderer fontRenderer = Fonts.fontSemibold35;
         fontRenderer.drawStringWithShadow(displayString,
                 (float) (xPosition + width / 2 - fontRenderer.getStringWidth(displayString) / 2),
                 yPosition + (height - 5) / 2F, 14737632);
         AWTFontRenderer.Companion.setAssumeNonVolatile(false);
      } else {
         fDPClient$buttonRenderer.render(mouseX, mouseY, mc);
         fDPClient$buttonRenderer.drawButtonText(mc);
      }

      resetColor();

      ci.cancel();
   }
}