/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.ui.client.gui.mainmenu.ModernGuiMainMenu;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.impl.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;

import java.awt.*;
public class MainMenuButton {
    public String icon;
    public String text;
    public Executor action;
    public float x;
    public float y;
    public float yAnimation = 0.0F;

    public MainMenuButton(String icon, String text, Executor action) {
        this.icon = icon;
        this.text = text;
        this.action = action;
    }

    public MainMenuButton(String icon, String text, Executor action, float yOffset) {
        this.icon = icon;
        this.text = text;
        this.action = action;
    }

    public void draw(float x, float y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;
        RenderUtils.drawRoundedCornerRect(x-30F,y-30F,x+30f,y+30f,15f,new Color(0,0,0,40).getRGB());
        this.yAnimation = RenderUtils.smoothAnimation(this.yAnimation, RenderUtils.isHovering(mouseX, mouseY, this.x-30f, this.y-30f, this.x + 30.0F, this.y + 30.0F) ? 4.0F : 0.0F, 20.0F, 0.3F);
        Fonts.MAINMENU.MAINMENU30.MAINMENU30.drawString(this.icon, x - (float) Fonts.MAINMENU.MAINMENU30.MAINMENU30.stringWidth(this.icon) / 2.0F, y-6f+(this.yAnimation*-1f), Color.WHITE.getRGB(),false);
        if(this.yAnimation>=0.11) {
            Fonts.SF.SF_16.SF_16.drawString(this.text, x - (float) Fonts.SF.SF_16.SF_16.stringWidth(this.text) / 2.0F, y + 12f +(this.yAnimation*-1f), new Color(255,255,255, ((((this.yAnimation/4.0f)) * 254.0f * 1f)<=255.0f) ? (int)(((this.yAnimation/4.0f)) * 254.0f + 1f) : 25).getRGB());
        }//RenderUtils.drawGradientRect(x, y + 40.0F - this.yAnimation * 3.0F, x + 50.0F, y + 40.0F, 3453695, 2016719615);
        RenderUtils.drawRoundedCornerRect(x-30F,y-30F,x+30f,y+30f,15f,new Color(255,255,255,50).getRGB());
    }

    public void mouseClick(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtils.isHovering(mouseX, mouseY, this.x-30f, this.y-30f, this.x + 30.0F, this.y + 30.0F) && this.action != null && mouseButton == 0) {
            this.action.execute();
        }

    }

   public interface Executor {
        void execute();
    }
}
