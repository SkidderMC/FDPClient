/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.launch.data.modernui.GuiMainMenu;
import net.ccbluex.liquidbounce.launch.data.modernui.clickgui.fonts.impl.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;

import java.awt.*;

public class MainMenuButton {
    public GuiMainMenu parent;
    public String icon;
    public String text;
    public Executor action;
    public int buttonID;
    public float x;
    public float y;
    public float textOffset;
    public float yAnimation = 0.0F;

    public MainMenuButton(GuiMainMenu parent, int id, String icon, String text, Executor action) {
        this.parent = parent;
        this.buttonID = id;
        this.icon = icon;
        this.text = text;
        this.action = action;
        this.textOffset = 0.0F;
    }

    public MainMenuButton(GuiMainMenu parent, int id, String icon, String text, Executor action, float yOffset) {
        this.parent = parent;
        this.buttonID = id;
        this.icon = icon;
        this.text = text;
        this.action = action;
        this.textOffset = yOffset;
    }

    public void draw(float x, float y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;
        Fonts.MAINMENU.MAINMENU30.MAINMENU30.drawString(this.icon, x + 25.0F - (float) Fonts.MAINMENU.MAINMENU30.MAINMENU30.stringWidth(this.icon) / 2.0F - 2.0F, y + 9.0F, Color.WHITE.getRGB(),false);
        this.yAnimation = RenderUtils.smoothAnimation(this.yAnimation, RenderUtils.isHovering(mouseX, mouseY, x, y, x + 50.0F, y + 30.0F) ? 2.0F : 0.0F, 50.0F, 0.3F);
        RenderUtils.drawGradientRect(x, y + 30.0F - this.yAnimation * 3.0F, x + 50.0F, y + 30.0F, 3453695, 2016719615);
        RenderUtils.drawRect(x, y + 30.0F - this.yAnimation, x + 50.0F, y + 30.0F, -13323521);
    }

    public void mouseClick(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtils.isHovering(mouseX, mouseY, this.x, this.y, this.x + 50.0F, this.y + 30.0F) && this.action != null && mouseButton == 0) {
            this.action.execute();
        }

    }

   public interface Executor {
        void execute();
    }
}
