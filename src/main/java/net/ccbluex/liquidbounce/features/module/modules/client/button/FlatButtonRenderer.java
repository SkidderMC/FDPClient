package net.ccbluex.liquidbounce.features.module.modules.client.button;

import net.ccbluex.liquidbounce.font.CFontRenderer;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FlatButtonRenderer extends GuiButton
{
    private MSTimer time;
    public String displayString;
    public int id;
    public boolean enabled;
    public boolean visible;
    protected boolean hovered;
    private int color;
    private float opacity;
    private CFontRenderer font;
    public boolean round;
    public int rad;

    public FlatButtonRenderer(final int buttonId, final int x, final int y, final int widthIn, final int heightIn, final String buttonText, final int color) {
        super(buttonId, x, y, 10, 12, buttonText);
        this.time = new MSTimer();
        this.width = widthIn;
        this.height = heightIn;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.displayString = buttonText;
        this.color = color;
        this.font = FontLoaders.F18;
    }

    public FlatButtonRenderer(final int buttonId, final int x, final int y, final int scale, final String buttonText, final int color, boolean round) {
        super(buttonId, x, y, 10, 12, buttonText);
        this.time = new MSTimer();
        this.width = scale;
        this.height = scale;
        this.rad = scale;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.displayString = buttonText;
        this.color = color;
        this.round = round;
        this.font = FontLoaders.F18;
    }

    public FlatButtonRenderer(final int buttonId, final int x, final int y, final int scale, final String buttonText, final int color, boolean round, CFontRenderer font) {
        super(buttonId, x, y, 10, 12, buttonText);
        this.time = new MSTimer();
        this.width = scale;
        this.height = scale;
        this.rad = scale;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.displayString = buttonText;
        this.color = color;
        this.round = round;
        this.font = font;
    }

    public FlatButtonRenderer(final int buttonId, final int x, final int y, final int widthIn, final int heightIn, final String buttonText, final int color, final CFontRenderer font) {
        super(buttonId, x, y, 10, 12, buttonText);
        this.time = new MSTimer();
        this.width = widthIn;
        this.height = heightIn;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.displayString = buttonText;
        this.color = color;
        this.font = font;
    }

    @Override
    protected int getHoverState(final boolean mouseOver) {
        int i = 1;
        if (!this.enabled) {
            i = 0;
        }
        else if (mouseOver) {
            i = 2;
        }
        return i;
    }

    @Override
    public void drawButton(final Minecraft mc, final int mouseX, final int mouseY) {
        if (this.visible) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.hovered = (mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);
            final int var5 = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            if (!this.hovered) {
                this.time.reset();
                this.opacity = 0.0f;
            }
            if (this.hovered) {
                this.opacity += 0.5f;
                if (this.opacity > 1.0f) {
                    this.opacity = 1.0f;
                }
            }
            final float radius = this.height / 2.0f;
            if(this.round) {
            	RenderUtils.drawFilledCircle(xPosition, yPosition, this.rad, this.color);
            }else {
            	RenderUtils.drawRoundedCornerRect((int)(this.xPosition - this.opacity * 0.1f), this.yPosition - this.opacity, (int)(this.xPosition + this.width + this.opacity * 0.1f), this.yPosition + radius * 2.0f + this.opacity, 3, this.color);
            }
            GL11.glColor3f(2.55f, 2.55f, 2.55f);
            this.mouseDragged(mc, mouseX, mouseY);
            GL11.glPushMatrix();
            GL11.glPushAttrib(1048575);
            GL11.glScaled(1.0, 1.0, 1.0);
            final boolean var6 = true;
            final float var7 = (float)this.font.getHeight();
            font.drawCenteredString(this.displayString, (float)(this.xPosition + this.width / 2)+2, this.yPosition + (this.height - this.font.getHeight()) / 2.0f + 6f,new Color(255,255,255).getRGB());
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    private Color darkerColor(final Color c, final int step) {
        final int red = c.getRed();
        final int blue = c.getBlue();
        final int green = c.getGreen();
        if (red >= step) {}
        if (blue >= step) {}
        if (green >= step) {}
        return c.darker();
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY) {
    }

    @Override
    public boolean mousePressed(final Minecraft mc, final int mouseX, final int mouseY) {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    @Override
    public boolean isMouseOver() {
        return this.hovered;
    }

    @Override
    public void drawButtonForegroundLayer(final int mouseX, final int mouseY) {
    }

    @Override
    public void playPressSound(final SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0f));
    }

    @Override
    public int getButtonWidth() {
        return this.width;
    }

    @Override
    public void setWidth(final int width) {
        this.width = width;
    }
}
