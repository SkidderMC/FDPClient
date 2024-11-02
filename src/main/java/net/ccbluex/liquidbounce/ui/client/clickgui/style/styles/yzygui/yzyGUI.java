/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.manager.GUIManager;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils.Pair;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author opZywl
 */
public final class yzyGUI extends GuiScreen {

    private final List<Panel> panels = new ArrayList<>();
    private final ClickGUIModule clickGui;

    final GUIManager guiManager = FDPClient.INSTANCE.getGuiManager();
    private int yShift = 0;

    public double slide, progress = 0;

    public long lastMS = System.currentTimeMillis();

    public yzyGUI(final ClickGUIModule clickGui) {
        this.clickGui = clickGui;

        final GUIManager guiManager = FDPClient.INSTANCE.getGuiManager();
        int panelX = 5;

        for (final yzyCategory category : yzyCategory.values()) {
            final Pair<Integer, Integer> positions = guiManager.getPositions(category);
            Panel panel;

            if (!guiManager.getPositions().containsKey(category)) {
                panel = new Panel(this, category, panelX, 5);

                panelX += panel.getWidth() + 5;
            } else {
                panel = new Panel(this, category, positions.getKey(), positions.getValue());

                panel.setExtended(guiManager.getExtendeds().get(category));
            }

            guiManager.getPositions().put(category, new Pair<>(panel.getX(), panel.getY()));
            guiManager.getExtendeds().put(category, panel.isExtended());

            panels.add(panel);

            panel.setExtended(guiManager.isExtended(category));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        slide = progress = 0;
        lastMS = System.currentTimeMillis();

    }


    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        mc.gameSettings.guiScale = clickGui.getLastScale();

        panels.forEach(Panel::onGuiClosed);

        mc.entityRenderer.loadEntityShader(null);
    }

    private void handleScroll(final int wheel) {
        if (wheel == 0)
            return;

        for(final Panel panel : panels)
            panel.setY(panel.getY() + wheel);
    }

    @Override
    public void drawScreen(final int mouseX, int mouseY, final float partialTicks) {
        if (Mouse.hasWheel()) {
            int wheel = Mouse.getDWheel();
            boolean handledScroll = false;

            for (int i = panels.size() - 1; i >= 0; i--)
                if (panels.get(i).handleScroll(mouseX, mouseY, wheel)) {
                    handledScroll = true;
                    break;
                }
            if (!handledScroll)
                handleScroll(wheel);

        }
        int finalMouseY = mouseY;
        panels.forEach(panel -> panel.drawScreen(mouseX, finalMouseY, partialTicks));
    }

    @Override
    protected void mouseClicked(final int mouseX, int mouseY, final int mouseButton) throws IOException {
        mouseY += yShift;
        int finalMouseY = mouseY;
        panels.forEach(panel -> panel.mouseClicked(mouseX, finalMouseY, mouseButton));
    }

    @Override
    protected void mouseReleased(final int mouseX, int mouseY, final int state) {
        mouseY += yShift;
        int finalMouseY = mouseY;
        panels.forEach(panel -> panel.mouseReleased(mouseX, finalMouseY, state));
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        panels.forEach(panel -> panel.keyTyped(typedChar, keyCode));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public List<Panel> getPanels() {
        return panels;
    }

    public ClickGUIModule getClickGui() {
        return clickGui;
    }

}
