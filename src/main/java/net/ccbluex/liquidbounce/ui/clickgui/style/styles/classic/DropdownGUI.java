/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.clickgui.style.styles.classic;

import lombok.Getter;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
public class DropdownGUI extends GuiScreen {

    @Getter
    private final List<Tab> tabs = new CopyOnWriteArrayList<>();
    private int dragX;
    private int dragY;
    private final ResourceLocation hudIcon = new ResourceLocation("fdpclient/gui/design/hud.png");

    @Override
    public void initGui() {
        float x = 75;

        if (tabs.isEmpty()) {
            for (ModuleCategory value : ModuleCategory.values()) {
                tabs.add(new Tab(value, x, 10));
                x += 110;
            }
            // tabs.add(new ConfigTab(x, 10));
        }

        if (!(mc.currentScreen instanceof GuiChest) && mc.currentScreen != this) {
            for (Tab tab : tabs) {
                for (Module module : tab.modules) {
                    module.fraction = 0;

                    for (Setting setting : module.settings) {
                        setting.setPercent(0);
                    }
                }
            }
        }

        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Mouse.isButtonDown(0) && mouseX >= 5 && mouseX <= 50 && mouseY <= height - 5 && mouseY >= height - 50) {
            mc.displayGuiScreen(new GuiHudDesigner());
        }
        RenderUtils.drawImage(hudIcon, 9, height - 41, 32, 32);

        GL11.glPushMatrix();
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int x = ScaleUtils.getScaledMouseCoordinates(mc, mouseX, mouseY)[0];
        int y = ScaleUtils.getScaledMouseCoordinates(mc, mouseX, mouseY)[1];
        ScaleUtils.scale(mc);

        for (Tab tab : tabs) {
            tab.drawScreen(x, y);
            if (tab.dragging) {
                tab.setPosX(dragX + x);
                tab.setPosY(dragY + y);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glPopMatrix();
    }

    public void updatemouse() {
        int scrollWheel = Mouse.getDWheel();
        for (Tab tab : tabs) {
            tab.setPosY(tab.getPosY() + (scrollWheel < 0 ? -15 : (scrollWheel > 0 ? 15 : 0)));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int x = ScaleUtils.getScaledMouseCoordinates(mc, mouseX, mouseY)[0];
        int y = ScaleUtils.getScaledMouseCoordinates(mc, mouseX, mouseY)[1];

        for (Tab tab : tabs) {
            if (tab.isHovered(x, y) && mouseButton == 0) {
                if (!anyDragging()) {
                    tab.dragging = true;
                    dragX = (int) (tab.getPosX() - x);
                    dragY = (int) (tab.getPosY() - y);
                }
            }
            try {
                tab.mouseClicked(x, y, mouseButton);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean anyDragging() {
        return tabs.stream().anyMatch(tab -> tab.dragging);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE && !areAnyHovered()) {
            mc.displayGuiScreen(null);
            if (mc.currentScreen == null) {
                mc.setIngameFocus();
            }
        } else {
            tabs.forEach(tab -> tab.keyTyped(typedChar, keyCode));
        }

    }

    private boolean areAnyHovered() {
        return false;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            tabs.forEach(tab -> tab.dragging = false);
        }

        tabs.forEach(tab -> tab.mouseReleased(mouseX, mouseY, state));
        super.mouseReleased(mouseX, mouseY, state);
    }

}
