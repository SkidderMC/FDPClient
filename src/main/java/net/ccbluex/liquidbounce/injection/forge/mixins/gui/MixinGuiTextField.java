package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {

    @Shadow
    public abstract boolean getVisible();
    @Shadow
    public abstract boolean getEnableBackgroundDrawing();
    @Shadow
    public int id;
    @Shadow
    public FontRenderer fontRendererInstance;
    @Shadow
    public int xPosition;
    @Shadow
    public int yPosition;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    private String text = "";
    @Shadow
    private int maxStringLength = 32;
    @Shadow
    private int cursorCounter;
    @Shadow
    private boolean enableBackgroundDrawing = true;
    @Shadow
    private boolean canLoseFocus = true;
    @Shadow
    private boolean isFocused;
    @Shadow
    private boolean isEnabled = true;
    @Shadow
    private int lineScrollOffset;
    @Shadow
    private int cursorPosition;
    @Shadow
    private int selectionEnd;
    private int enabledColor = 14737632;
    @Shadow
    private int disabledColor = 7368816;
    @Shadow
    public abstract int getWidth();
    @Shadow
    public abstract int getMaxStringLength();
    @Shadow
    public abstract void drawCursorVertical(int p_drawCursorVertical_1_, int p_drawCursorVertical_2_, int p_drawCursorVertical_3_, int p_drawCursorVertical_4_);
    /**
     * @author XiGua
     */
    @Inject(method = "drawTextBox", at = @At("HEAD"), cancellable = true)
    public void drawTextBox(CallbackInfo ci) {
        if (this.getVisible())
        {
            if (this.getEnableBackgroundDrawing())
            {
                RenderUtils.drawRoundedCornerRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1,2, new Color(255,255,255,50).getRGB());
                RenderUtils.drawRoundedCornerRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height,2, new Color(0,0,0,200).getRGB());
            }

            int i = this.isEnabled ? this.enabledColor : this.disabledColor;
            int j = this.cursorPosition - this.lineScrollOffset;
            int k = this.selectionEnd - this.lineScrollOffset;
            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && flag;
            int l = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            int i1 = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
            int j1 = l;

            if (k > s.length())
            {
                k = s.length();
            }

            if (s.length() > 0)
            {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = this.fontRendererInstance.drawStringWithShadow(s1, (float)l+5, (float)i1+2, i);
            }

            boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int k1 = j1;

            if (!flag)
            {
                k1 = j > 0 ? l + this.width : l;
            }
            else if (flag2)
            {
                k1 = j1 - 1;
                --j1;
            }

            if (s.length() > 0 && flag && j < s.length())
            {
                j1 = this.fontRendererInstance.drawStringWithShadow(s.substring(j), (float)j1+5, (float)i1+2, i);
            }

            if (flag1)
            {
                if (flag2)
                {
                    RenderUtils.drawRoundedCornerRect(k1, i1 - 1, k1 + 1, i1 + 1 + this.fontRendererInstance.FONT_HEIGHT,3, -3092272);
                }
                else
                {
                    this.fontRendererInstance.drawStringWithShadow("_", (float)k1+2, (float)i1+2, i);
                }
            }

            if (k != j)
            {
                int l1 = l + this.fontRendererInstance.getStringWidth(s.substring(0, k));
                this.drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + this.fontRendererInstance.FONT_HEIGHT);
            }
        }
        ci.cancel();
    }
}
