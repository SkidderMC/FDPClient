package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.newVer;

import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.gui.newVer.IconManager;
import net.ccbluex.liquidbounce.ui.client.gui.newVer.element.CategoryElement;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.newVer.element.SearchElement;
import net.ccbluex.liquidbounce.ui.client.gui.newVer.element.module.ModuleElement;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.MouseUtils;
import net.ccbluex.liquidbounce.utils.AnimationUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.Stencil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class NewUi extends GuiScreen {

    private static NewUi instance;
    public static NewUi getInstance() {
        return instance == null ? instance = new NewUi() : instance;
    }

    public static void resetInstance() {
        instance = new NewUi();
    }

    private NewUi() {
        for (ModuleCategory c : ModuleCategory.values())
            categoryElements.add(new CategoryElement(c));
        categoryElements.get(0).setFocused(true);
    }

    public final List<CategoryElement> categoryElements = new ArrayList<>();

    private float startYAnim = height / 2F;
    private float endYAnim = height / 2F;

    private SearchElement searchElement;

    private float fading = 0F;

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        for (CategoryElement ce : categoryElements) {
            for (ModuleElement me : ce.getModuleElements()) {
                if (me.listeningKeybind())
                    me.resetState();
            }
        }
        searchElement = new SearchElement(40F, 115F, 180F, 20F);
        super.initGui();
    }

    public void onGuiClosed() {
        for (CategoryElement ce : categoryElements) {
            if (ce.getFocused())
                ce.handleMouseRelease(-1, -1, 0, 0, 0, 0, 0);
        }
        Keyboard.enableRepeatEvents(false);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // will draw reduced ver once it gets under 1140x780.
        drawFullSized(mouseX, mouseY, partialTicks, ClickGUIModule.INSTANCE.generateColor());
    }

    private void drawFullSized(int mouseX, int mouseY, float partialTicks, Color accentColor) {
        RenderUtils.originalRoundedRect(30F, 30F, this.width - 30F, this.height - 30F, 8F, 0xFF101010);
        // something to make it look more like windoze
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, this.width - 54F, 30F, this.width - 30F, 50F))
            fading += 0.2F * RenderUtils.deltaTime * 0.045F;
        else
            fading -= 0.2F * RenderUtils.deltaTime * 0.045F;
        fading = MathHelper.clamp_float(fading, 0F, 1F);
        RenderUtils.customRounded(this.width - 54F, 30F, this.width - 30F, 50F, 0F, 8F, 0F, 8F, new Color(1F, 0F, 0F, fading).getRGB());
        GlStateManager.disableAlpha();
        RenderUtils.drawImage(IconManager.removeIcon, this.width - 47, 35, 10, 10);
        GlStateManager.enableAlpha();
        Stencil.write(true);
        RenderUtils.drawFilledCircle(65F, 80F, 25F, new Color(45, 45, 45));
        Stencil.erase(true);
        if (mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()) != null) {
            final ResourceLocation skin = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getLocationSkin();
            glPushMatrix();
            glTranslatef(40F, 55F, 0F);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glDepthMask(false);
            OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            glColor4f(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(skin);
            Gui.drawScaledCustomSizeModalRect(0, 0, 8F, 8F, 8, 8, 50, 50,
                    64F, 64F);
            glDepthMask(true);
            glDisable(GL_BLEND);
            glEnable(GL_DEPTH_TEST);
            glPopMatrix();
        }
        Stencil.dispose();

        if (Fonts.fontLarge.getStringWidth(mc.thePlayer.getGameProfile().getName()) > 70)
            Fonts.fontLarge.drawString(Fonts.fontLarge.trimStringToWidth(mc.thePlayer.getGameProfile().getName(), 50) + "...", 100, 78 - Fonts.fontLarge.FONT_HEIGHT + 15, -1);
        else
            Fonts.fontLarge.drawString(mc.thePlayer.getGameProfile().getName(), 100, 78 - Fonts.fontLarge.FONT_HEIGHT + 15, -1);

        if (searchElement.drawBox(mouseX, mouseY, accentColor)) {
            searchElement.drawPanel(mouseX, mouseY, 230, 50, width - 260, height - 80, Mouse.getDWheel(), categoryElements, accentColor);
            return;
        }

        final float elementHeight = 24;
        float startY = 140F;
        for (CategoryElement ce : categoryElements) {
            ce.drawLabel(mouseX, mouseY, 30F, startY, 200F, elementHeight);
            if (ce.getFocused()) {
                startYAnim = ClickGUIModule.INSTANCE.getFastRenderValue().get() ? startY + 6F : AnimationUtils.animate(startY + 6F, startYAnim, (startYAnim - (startY + 5F) > 0 ? 0.65F : 0.55F) * RenderUtils.deltaTime * 0.025F);
                endYAnim = ClickGUIModule.INSTANCE.getFastRenderValue().get() ? startY + elementHeight - 6F : AnimationUtils.animate(startY + elementHeight - 6F, endYAnim, (endYAnim - (startY + elementHeight - 5F) < 0 ? 0.65F : 0.55F) * RenderUtils.deltaTime * 0.025F);

                ce.drawPanel(mouseX, mouseY, 230, 50, width - 260, height - 80, Mouse.getDWheel(), accentColor);
            }
            startY += elementHeight;
        }
        RenderUtils.originalRoundedRect(32F, startYAnim, 34F, endYAnim, 1F, accentColor.getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, this.width - 54F, 30F, this.width - 30F, 50F)) {
            mc.displayGuiScreen(null);
            return;
        }
        final float elementHeight = 24;
        float startY = 140F;
        searchElement.handleMouseClick(mouseX, mouseY, mouseButton, 230, 50, width - 260, height - 80, categoryElements);
        if (!searchElement.isTyping()) for (CategoryElement ce : categoryElements) {
            if (ce.getFocused())
                ce.handleMouseClick(mouseX, mouseY, mouseButton, 230, 50, width - 260, height - 80);
            if (MouseUtils.mouseWithinBounds(mouseX, mouseY, 30F, startY, 230F, startY + elementHeight) && !searchElement.isTyping()) {
                categoryElements.forEach(e -> e.setFocused(false));
                ce.setFocused(true);
                return;
            }
            startY += elementHeight;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (CategoryElement ce : categoryElements) {
            if (ce.getFocused()) {
                if (ce.handleKeyTyped(typedChar, keyCode))
                    return;
            }
        }
        if (searchElement.handleTyping(typedChar, keyCode, 230, 50, width - 260, height - 80, categoryElements))
            return;
        super.keyTyped(typedChar, keyCode);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        searchElement.handleMouseRelease(mouseX, mouseY, state, 230, 50, width - 260, height - 80, categoryElements);
        if (!searchElement.isTyping())
            for (CategoryElement ce : categoryElements) {
                if (ce.getFocused())
                ce.handleMouseRelease(mouseX, mouseY, state, 230, 50, width - 260, height - 80);
            }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
