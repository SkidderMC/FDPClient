/*
 * Decompiled with CFR 0.136.
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.novoline;


import com.google.common.collect.Lists;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class ClickyUI extends GuiScreen {
    public static ArrayList<Window> windows = Lists.newArrayList();
    public int scrollVelocity;
    public static boolean binding;

    public ClickyUI() {
        if (windows.isEmpty()) {
            int x = 25;
            ModuleCategory[] arrmoduleType = ModuleCategory.values();
            int n = arrmoduleType.length;
            int n2 = 0;
            while (n2 < n) {
                ModuleCategory c = arrmoduleType[n2];
                windows.add(new Window(c, 20 , x));
                x += 30;
                ++n2;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int defaultHeight1 = (this.height);
        int defaultWidth1 = (this.width);
        Gui.drawRect(0, 0, Display.getWidth(), Display.getHeight(), new Color(0, 0, 0, 100).getRGB());//Shadow

        GlStateManager.pushMatrix();
        windows.forEach(w2 -> w2.render(mouseX, mouseY));
        GlStateManager.popMatrix();
        if (Mouse.hasWheel()) {
            int wheel = Mouse.getDWheel();
            scrollVelocity = wheel < 0 ? -120 : (wheel > 0 ? 130 : 0);
        }
        windows.forEach(w2 -> w2.mouseScroll(mouseX, mouseY, scrollVelocity));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        windows.forEach(w2 -> w2.click(mouseX, mouseY, mouseButton));
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1 && !binding) {
            mc.displayGuiScreen(null);
            return;
        }
        windows.forEach(w2 -> w2.key(typedChar, keyCode));
    }

}

