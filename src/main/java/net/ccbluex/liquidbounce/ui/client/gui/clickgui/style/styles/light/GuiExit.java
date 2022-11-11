/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.light;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiExit extends GuiScreen {
    boolean mouseClicked = false;
    @Override
    public void onGuiClosed() {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(mouseX >= width / 2 - 75 && mouseX <= width / 2 - 10 && mouseY >= height / 2 + 20 && mouseY <= height / 2 + 35) {
            if(Mouse.isButtonDown(0) && !mouseClicked)
                mc.displayGuiScreen(new LightClickGUI());
        } else mouseClicked = Mouse.isButtonDown(0) && !(mouseX >= width / 2 && mouseX <= width / 2 + 65 && mouseY >= height / 2 + 20 && mouseY <= height / 2 + 35);
        if(mouseX >= width / 2 && mouseX <= width / 2 + 65 && mouseY >= height / 2 + 20 && mouseY <= height / 2 + 35) {
            if(Mouse.isButtonDown(0) && !mouseClicked)
                mc.displayGuiScreen(null);
        } else mouseClicked = Mouse.isButtonDown(0) && !(mouseX >= width / 2 - 75 && mouseX <= width / 2 - 10 && mouseY >= height / 2 + 20 && mouseY <= height / 2 + 35);
        //RenderUtils.drawImage(new ResourceLocation("fdpclient/clickgui/rect.png"), width / 2 - 80,height / 2 - 30,150,70);
        int fontHeight = Math.round(Fonts.font40.FONT_HEIGHT / 2);
        RenderUtils.drawRect(width / 2 - 80, height / 2 - 30, width / 2 + 70, height / 2 + 40,new Color(40,40,40,125).getRGB());
        RenderUtils.drawRect(width / 2 - 75, height / 2 + 20, width / 2 - 10, height / 2 + 35, new Color(19, 138, 225).getRGB());
        RenderUtils.drawRect(width / 2, height / 2 + 20, width / 2 + 65, height / 2 + 35, new Color(241, 54, 35).getRGB());
        Fonts.font40.drawString("Exit Gui", width / 2 - 25,height / 2 - 20, -1);
        Fonts.font40.drawString("Back",width / 2 - 50, height / 2 + 28 - fontHeight, -1);
        Fonts.font40.drawString("Exit",width / 2 + 25, height / 2 + 28 - fontHeight, -1);
    }

}
