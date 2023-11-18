package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap;

import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Utils.OtcScroll;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OtcClickGUi extends GuiScreen  {
    private static float mainx;
    private float x;
    private static float mainy;
    private float hight;
    private int x2;
    private int y2;
    private boolean dragging;
    private final List<CategoryScreen> tabs;
    
    public int sHeight() {
        return super.height * 2;
    }
    
    public OtcClickGUi() {
        this.mainx = 320.0f;
        this.x = 0.0f;
        this.mainy = 130.0f;
        this.hight = 120.0f;
        this.tabs = new ArrayList<CategoryScreen>();
        for (final ModuleCategory category : ModuleCategory.values()) {
            this.tabs.add(new CategoryScreen(category, this.x));
            this.x += Fonts.fontTahoma.getStringWidth(this.newcatename(category)) + 10;
        }
    }
    
    public String newcatename(final ModuleCategory moduleCategory) {
        if (moduleCategory.getDisplayName().equals("Combat")) {
            return "combat";
        }
        if (moduleCategory.getDisplayName().equals("Player")) {
            return "player";
        }
        if (moduleCategory.getDisplayName().equals("Movement")) {
            return "move";
        }
        if (moduleCategory.getDisplayName().equals("Render")) {
            return "visuals";
        }
        if (moduleCategory.getDisplayName().equals("World")) {
            return "world";
        }
        return "";
    }
    
    public void initGui() {
        super.initGui();
    }
    
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        int guiColor = ClickGUIModule.INSTANCE.generateColor().getRGB();
        try {
            if (this.dragging) {
                this.mainx = (float)(this.x2 + mouseX);
                this.mainy = (float)(this.y2 + mouseY);
            }
            final ScaledResolution scaledResolution = new ScaledResolution(this.mc);
            RenderUtils.drawRect(0.0f, 0.0f, (float)scaledResolution.getScaledWidth(), (float)scaledResolution.getScaledHeight(), new Color(0, 0, 0, 120).getRGB());
            RoundedUtil.drawRound(this.mainx, this.mainy, 290.0f, this.hight + 180.0f, 3.0f, new Color(44, 47, 56));
            RoundedUtil.drawRound(this.mainx, this.mainy - 50.0f, 290.0f, this.hight - 80.0f, 3.0f, new Color(44, 47, 56));
            RoundedUtil.drawGradientHorizontal(this.mainx, this.mainy - 50.0f, 290.0f, this.hight - 116.0f, 3.0f, new Color(guiColor), new Color(guiColor));
            Fonts.fontTahoma.drawString("onetap.su", this.mainx + 11.0f, this.mainy - 31.0f, new Color(255, 255, 255).getRGB());
            RoundedUtil.drawRound(this.mainx + 64.0f, this.mainy - 35.0f, 0.5f, this.hight - 105.0f, 1.0f, new Color(255, 255, 255, 150));
            final CategoryScreen selectedTab = this.getSelectedTab();
            if (selectedTab == null) {
                this.mc.fontRendererObj.drawString("-------------", (int)this.mainx + 109, (int)this.mainy + 40, new Color(255, 255, 255).getRGB());
                this.mc.fontRendererObj.drawString(" Select one of", (int)this.mainx + 109, (int)this.mainy + 50, new Color(255, 255, 255).getRGB());
                this.mc.fontRendererObj.drawString("-------------", (int)this.mainx + 109, (int)this.mainy + 60, new Color(255, 255, 255).getRGB());
                this.mc.fontRendererObj.drawString("Enjoy OneTap", (int)this.mainx + 107, (int)this.mainy + 75, new Color(255, 255, 255).getRGB());
            }
            this.tabs.forEach(s -> s.drawScreen(mouseX, mouseY));
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (final CategoryScreen categoryScreen : this.tabs) {
                if (categoryScreen.isHovered(mouseX, mouseY)) {
                    for (final CategoryScreen other : this.tabs) {
                        other.setSelected(false);
                    }
                    categoryScreen.setSelected(true);
                }
            }
        }
        if (this.isHovered(mouseX, mouseY)) {
            this.x2 = (int)(this.mainx - mouseX);
            this.y2 = (int)(this.mainy - mouseY);
            this.dragging = true;
        }
        final CategoryScreen selectedTab = this.getSelectedTab();
        if (selectedTab != null) {
            selectedTab.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public CategoryScreen getSelectedTab() {
        return this.tabs.stream().filter(CategoryScreen::isSelected).findAny().orElse(null);
    }

    private boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.mainx && mouseX <= this.mainx + 45.0f + 105.0f + 270.0f && mouseY >= this.mainy - 50.0f - 7.0f && mouseY <= this.mainy - 50.0f + 20.0f;
    }

    public static OtcScroll scroll() {
        final int mouse = Mouse.getDWheel();
        if (mouse > 0) {
            return OtcScroll.UP;
        }
        if (mouse < 0) {
            return OtcScroll.DOWN;
        }
        return null;
    }

    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        if (state == 0) {
            this.dragging = false;
        }
        this.tabs.forEach(e -> e.mouseReleased(mouseX, mouseY, state));
        super.mouseReleased(mouseX, mouseY, state);
    }

    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        this.tabs.forEach(e -> e.keyTyped(typedChar, keyCode));
        super.keyTyped(typedChar, keyCode);
    }

    public static float getMainx() {
        return mainx;
    }

    public static float getMainy() {
        return mainy;
    }
    
    public float getX2() {
        return (float)this.x2;
    }
    
    public float getY2() {
        return (float)this.y2;
    }
    
    public int getHeight() {
        return (int)this.hight;
    }
}
