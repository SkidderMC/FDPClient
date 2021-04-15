package net.ccbluex.liquidbounce.ui.client.keybind;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;

public class KeyBindMgr extends GuiScreen {
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

//        RenderUtils.drawText("KeyBind Manager", Fonts.fontBold40, (int)(width*0.3), (int)(height*0.3), 3);
//
//        RenderUtils.drawRect(width*0.3F,height*0.3F+(Fonts.fontBold40.getHeight()*1.5F)
//                ,width*0.7F,height*0.7F, Color.WHITE.getRGB());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
