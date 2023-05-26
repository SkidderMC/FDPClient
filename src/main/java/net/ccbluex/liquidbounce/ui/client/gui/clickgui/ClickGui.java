/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.elements.ButtonElement;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.elements.Element;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.elements.ModuleElement;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.Style;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.SlowlyStyle;
import net.ccbluex.liquidbounce.ui.client.gui.options.modernuiLaunchOption;
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClickGui extends GuiScreen {

    public final List<Panel> panels = new ArrayList<>();
    public Style style = new SlowlyStyle();
    private Panel clickedPanel;
    private int mouseX;
    private int mouseY;

    private double slide, progress = 0;

    public ClickGui() {
        final int width = 100;
        final int height = 18;

        int yPos = 5;
        for (final ModuleCategory category : ModuleCategory.values()) {
            panels.add(new Panel(category.getDisplayName(), category, 100, yPos, width, height, false) {

                @Override
                public void setupItems() {
                    for (Module module : FDPClient.moduleManager.getModules())
                        if (module.getCategory() == category)
                            getElements().add(new ModuleElement(module));
                }
            });

            yPos += 20;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float trueCguiScale;
        if (FDPClient.moduleManager.getModule(ClickGUIModule.class).INSTANCE.getStyleValue().get().equals("Jello")) {
            trueCguiScale = 1;
        } else if(FDPClient.moduleManager.getModule(ClickGUIModule.class).INSTANCE.getStyleValue().get().equals("Glow")) {
            trueCguiScale = 1;
        } else {
            trueCguiScale = FDPClient.moduleManager.getModule(ClickGUIModule.class).INSTANCE.getScaleValue().get();
        }
        final double scale = trueCguiScale;
        if (progress < 1) progress += 0.1 * (1 - partialTicks);
        else progress = 1;

        switch (Objects.requireNonNull(FDPClient.moduleManager.getModule(ClickGUIModule.class)).INSTANCE.getAnimationValue().get().toLowerCase()) {
            case "liquidbounce":
            case "ziul":
                slide = EaseUtils.easeOutBack(progress);
                break;
            case "slide":
            case "zoom":
            case "bread":
                slide = EaseUtils.easeOutQuart(progress);
                break;
            case "none":
                slide = 1;
                break;
        }

        if (Mouse.isButtonDown(0) && mouseX >= 5 && mouseX <= 50 && mouseY <= height - 5 && mouseY >= height - 50)
            mc.displayGuiScreen(new GuiHudDesigner());

        AWTFontRenderer.Companion.setAssumeNonVolatile(true);

        mouseX /= scale;
        mouseY /= scale;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        switch (Objects.requireNonNull(FDPClient.moduleManager.getModule(ClickGUIModule.class)).INSTANCE.getBackgroundValue().get()) {
            case "Default":
                drawDefaultBackground();
                break;
            case "Gradient":
                drawGradientRect(0, 0, width, height, ColorUtils.reAlpha(ClickGUIModule.INSTANCE.generateColor(), 40).getRGB(), ClickGUIModule.INSTANCE.generateColor().getRGB());
                break;
            default:
                break;
        }

        drawDefaultBackground();
        int defaultHeight1 = (this.height);
        int defaultWidth1 = (this.width);

        switch (Objects.requireNonNull(FDPClient.moduleManager.getModule(ClickGUIModule.class)).INSTANCE.getAnimationValue().get().toLowerCase()) {
            case "bread":
                GlStateManager.translate(0, (1.0 - slide) * height * 2.0, 0);
                GlStateManager.scale(scale, scale + (1.0 - slide) * 2.0, scale);
                break;
            case "slide":
            case "liquidbounce":
                GlStateManager.translate(0, (1.0 - slide) * height * 2.0, 0);
                GlStateManager.scale(scale, scale, scale);
                break;
            case "zoom":
                GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), (1.0 - slide) * (width / 2.0));
                GlStateManager.scale(scale * slide, scale * slide, scale * slide);
                break;
            case "ziul":
                GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), 0);
                GlStateManager.scale(scale * slide, scale * slide, scale * slide);
                break;
            case "none":
                GlStateManager.scale(scale, scale, scale);
                break;
        }

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
            boolean handledScroll = false;
            
            for (int i = panels.size() - 1; i >= 0; i--)
                if (panels.get(i).handleScroll(mouseX, mouseY, wheel)) {
			        handledScroll = true;
			        break;
		        }
            
            if (!handledScroll)
		        handleScroll(wheel);
        }

        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();

        switch (Objects.requireNonNull(FDPClient.moduleManager.getModule(ClickGUIModule.class)).getAnimationValue().get().toLowerCase()) {
            case "bread":
            case "slide":
            case "liquidbounce":
                GlStateManager.translate(0, (1.0 - slide) * height * -2.0, 0);
                break;
            case "zoom":
                GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), -1 * (1.0 - slide) * (width / 2.0));
                break;
            case "ziul":
                GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), 0);
                break;
        }
        GlStateManager.scale(1, 1, 1);

        AWTFontRenderer.Companion.setAssumeNonVolatile(false);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void handleScroll(final int wheel) {
        if (wheel == 0)
            return;
		
        for(final Panel panel : panels)
	        panel.setY(panel.getY() + wheel);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        float trueCguiScale;
        if (FDPClient.moduleManager.getModule(ClickGUIModule.class).getStyleValue().get().equals("Jello")) {
            trueCguiScale = 1;
        } else if(FDPClient.moduleManager.getModule(ClickGUIModule.class).getStyleValue().get().equals("Glow")) {
            trueCguiScale = 1;
        } else {
            trueCguiScale = FDPClient.moduleManager.getModule(ClickGUIModule.class).getScaleValue().get();
        }
        final double scale = trueCguiScale;

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
        float trueCguiScale;
        if (FDPClient.moduleManager.getModule(ClickGUIModule.class).getStyleValue().get().equals("Jello")) {
            trueCguiScale = 1;
        } else if(FDPClient.moduleManager.getModule(ClickGUIModule.class).getStyleValue().get().equals("Glow")) {
            trueCguiScale = 1;
        } else {
            trueCguiScale = FDPClient.moduleManager.getModule(ClickGUIModule.class).getScaleValue().get();
        }
        final double scale = trueCguiScale;

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
        FDPClient.fileManager.saveConfig(modernuiLaunchOption.getClickGuiConfig());
        slide = 0;
	progress = 0;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
