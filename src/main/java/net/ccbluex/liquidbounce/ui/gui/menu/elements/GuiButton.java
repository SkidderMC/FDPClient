/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.gui.menu.elements;

import net.ccbluex.liquidbounce.ui.font.cf.FontLoaders;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class GuiButton {

    public String text;
    public int x, y;
    public int width, height;

    public int hoverFade = 0;

    public GuiButton(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;

        this.width = 132;
        this.height = 12;
    }

    public void drawButton(int mouseX, int mouseY) {
        boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if (hovered) {
            if (hoverFade < 40) hoverFade += 10;
        } else {
            if (hoverFade > 0) hoverFade -= 10;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F);
        RenderUtils.drawRound(this.x - 1, this.y - 1, this.width + 2, this.height + 2, 2, new Color(30, 30, 30, 60));
        RenderUtils.drawRound(this.x, this.y, this.width, this.height, 2, new Color(255, 255, 255, 38 + hoverFade));

        RenderUtils.drawRoundOutline(this.x, this.y, this.x + this.width, this.y + this.height, 2, 3, new Color(255, 255, 255, 30).getRGB());

        FontLoaders.Roboto.drawCenteredString(this.text, this.x + (float) this.width / 2 + 0.5F, this.y + (float) (this.height - 4) / 2 + 1.5F, new Color(30, 30, 30, 50).getRGB());
        FontLoaders.Roboto.drawCenteredString(this.text, this.x + (float) this.width / 2, this.y + (float) (this.height - 4) / 2 + 1F, new Color(190, 195, 189).getRGB());
    }

}
