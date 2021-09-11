/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.launch.data.legacyui.ClickGUIModule;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.elements.ButtonElement;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.elements.Element;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.elements.ModuleElement;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.Style;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.SlowlyStyle;
import net.ccbluex.liquidbounce.launch.options.LegacyUiLaunchOption;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGui extends GuiScreen {

    public final List<Panel> panels = new ArrayList<>();
    public Style style = new SlowlyStyle();
    private Panel clickedPanel;
    private int mouseX;
    private int mouseY;

    public ClickGui() {
        final int width = 100;
        final int height = 18;

        int yPos = 5;
        for (final ModuleCategory category : ModuleCategory.values()) {
            panels.add(new Panel(category.getDisplayName(), category, 100, yPos, width, height, false) {

                @Override
                public void setupItems() {
                    for (Module module : LiquidBounce.moduleManager.getModules())
                        if (module.getCategory() == category)
                            getElements().add(new ModuleElement(module));
                }
            });

            yPos += 20;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final double scale = LiquidBounce.moduleManager.getModule(ClickGUIModule.class).scaleValue.get();

        mouseX /= scale;
        mouseY /= scale;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        drawDefaultBackground();

        GlStateManager.scale(scale, scale, scale);

        for (final Panel panel : panels) {
            panel.updateFade(RenderUtils.deltaTime);
            panel.drawScreen(mouseX, mouseY, partialTicks);
        }

        for (final Panel panel : panels) {
            for (final Element element : panel.getElements()) {
                if (element instanceof ModuleElement) {
                    final ModuleElement moduleElement = (ModuleElement) element;

                    if (mouseX != 0 && mouseY != 0 && moduleElement.isHovering(mouseX, mouseY) && moduleElement.isVisible() && element.getY() <= panel.getY() + panel.getFade())
                        style.drawDescription(mouseX, mouseY, moduleElement.getModule().getDescription());
                }
            }
        }

        if (Mouse.hasWheel()) {
            int wheel = Mouse.getDWheel();

            for (int i = panels.size() - 1; i >= 0; i--)
                if (panels.get(i).handleScroll(mouseX, mouseY, wheel))
                    break;
        }

        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.scale(1, 1, 1);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        final double scale = LiquidBounce.moduleManager.getModule(ClickGUIModule.class).scaleValue.get();

        mouseX /= scale;
        mouseY /= scale;

        for (final Panel panel : panels) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);

            panel.drag = false;

            if (mouseButton == 0 && panel.isHovering(mouseX, mouseY))
                clickedPanel = panel;
        }

        if (clickedPanel != null) {
            clickedPanel.x2 = clickedPanel.x - mouseX;
            clickedPanel.y2 = clickedPanel.y - mouseY;
            clickedPanel.drag = true;

            panels.remove(clickedPanel);
            panels.add(clickedPanel);
            clickedPanel = null;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        final double scale = LiquidBounce.moduleManager.getModule(ClickGUIModule.class).scaleValue.get();

        mouseX /= scale;
        mouseY /= scale;

        for (Panel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void updateScreen() {
        for (final Panel panel : panels) {
            for (final Element element : panel.getElements()) {
                if (element instanceof ButtonElement) {
                    final ButtonElement buttonElement = (ButtonElement) element;

                    if (buttonElement.isHovering(mouseX, mouseY)) {
                        if (buttonElement.hoverTime < 7)
                            buttonElement.hoverTime++;
                    } else if (buttonElement.hoverTime > 0)
                        buttonElement.hoverTime--;
                }

                if (element instanceof ModuleElement) {
                    if (((ModuleElement) element).getModule().getState()) {
                        if (((ModuleElement) element).slowlyFade < 255)
                            ((ModuleElement) element).slowlyFade += 20;
                    } else if (((ModuleElement) element).slowlyFade > 0)
                        ((ModuleElement) element).slowlyFade -= 20;

                    if (((ModuleElement) element).slowlyFade > 255)
                        ((ModuleElement) element).slowlyFade = 255;

                    if (((ModuleElement) element).slowlyFade < 0)
                        ((ModuleElement) element).slowlyFade = 0;
                }
            }
        }
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        LiquidBounce.fileManager.saveConfig(LegacyUiLaunchOption.getClickGuiConfig());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
