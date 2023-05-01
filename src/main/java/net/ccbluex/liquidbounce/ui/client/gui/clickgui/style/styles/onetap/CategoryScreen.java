package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Utils.Position;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.math.MathUtil;
import net.ccbluex.liquidbounce.utils.misc.Direction;
import net.ccbluex.liquidbounce.utils.misc.SmoothStepAnimation;
import net.ccbluex.liquidbounce.utils.misc.Animation;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CategoryScreen
{
    private float maxScroll;
    private float minScroll;
    private float rawScroll;
    private float scroll;
    public Position pos;
    private ModuleCategory category;
    private float x;
    private float categoryX;
    private float categoryY;
    private boolean selected;
    private final List<ModuleRender> moduleList;
    private Animation scrollAnimation;
    
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
        if (moduleCategory.getDisplayName().equals("Misc")) {
            return "misc";
        }
        if (moduleCategory.getDisplayName().equals("Exploit")) {
            return "exploit";
        }
        return "";
    }
    
    public CategoryScreen(final ModuleCategory category, final float x) {
        this.maxScroll = Float.MAX_VALUE;
        this.minScroll = 0.0f;
        this.category = ModuleCategory.COMBAT;
        this.moduleList = new CopyOnWriteArrayList<ModuleRender>();
        this.scrollAnimation = new SmoothStepAnimation(0, 0.0, Direction.BACKWARDS);
        this.category = category;
        this.x = x;
        this.pos = new Position(0.0f, 0.0f, 0.0f, 0.0f);
        int count = 0;
        int leftAdd = 0;
        int rightAdd = 0;
        for (final Module module : LiquidBounce.moduleManager.getModuleInCategory(this.category)) {
            final float posWidth = 0.0f;
            final float posX = this.pos.x + ((count % 2 == 0) ? 0 : 145);
            final float posY = this.pos.y + ((count % 2 == 0) ? leftAdd : rightAdd);
            final Position pos = new Position(posX, posY, posWidth, 30.0f);
            final ModuleRender otlM = new ModuleRender(module, pos.x, pos.y, pos.width, pos.height);
            pos.height = (float)otlM.height;
            if (count % 2 == 0) {
                leftAdd += (int)(pos.height + 20.0f);
            }
            else {
                rightAdd += (int)(pos.height + 20.0f);
            }
            this.moduleList.add(otlM);
            ++count;
        }
    }
    
    public void drawScreen(final int mouseX, final int mouseY) {
        try {
            this.categoryX = OtcClickGUi.getMainx();
            this.categoryY = OtcClickGUi.getMainy();
            if (this.selected) {
                final double scrolll = this.getScroll();
                for (final ModuleRender module2 : this.moduleList) {
                    module2.scrollY = (int) MathUtil.roundToHalf(scrolll);
                }
                this.onScroll(30);
                this.maxScroll = Math.max(0.0f, this.moduleList.get(this.moduleList.size() - 1).getY() + this.moduleList.get(this.moduleList.size() - 1).height * 2 + 2500.0f);
            }
            Fonts.fontTahoma.drawString(this.newcatename(this.category), this.x + this.categoryX + 77.0f, this.categoryY - 29.0f, -1);
            if (this.selected) {
                RoundedUtil.drawRound(this.x + this.categoryX + 76.0f, this.categoryY - 29.0f - 3.0f, (float)(Fonts.fontTahoma.getStringWidth(this.newcatename(this.category)) + 2), 9.0f, 1.0f, new Color(255, 255, 255, 60));
                GL11.glPushMatrix();
                RenderUtils.scissor(0.0, OtcClickGUi.getMainy(), 1920.0, 300.0);
                GL11.glEnable(3089);
                this.moduleList.stream().sorted((o1, o2) -> Boolean.compare(o1.isSelected(), o2.isSelected())).forEach(module -> module.drawScreen(mouseX, mouseY));
                GL11.glDisable(3089);
                GL11.glPopMatrix();
            }
            if (this.isHovered(mouseX, mouseY)) {
                RoundedUtil.drawRound(this.x + this.categoryX + 76.0f, this.categoryY - 29.0f - 3.0f, (float)(Fonts.fontTahoma.getStringWidth(this.newcatename(this.category)) + 2), 9.0f, 1.0f, new Color(255, 255, 255, 60));
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    public void onScroll(final int ms) {
        this.scroll = (float)(this.rawScroll - this.scrollAnimation.getOutput());
        this.rawScroll += Mouse.getDWheel() / 4.0f;
        this.rawScroll = Math.max(Math.min(this.minScroll, this.rawScroll), -this.maxScroll);
        this.scrollAnimation = new SmoothStepAnimation(ms, this.rawScroll - this.scroll, Direction.BACKWARDS);
    }
    
    public float getScroll() {
        return this.scroll = (float)(this.rawScroll - this.scrollAnimation.getOutput());
    }
    
    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.x + this.categoryX + 76.0f && mouseX <= this.x + this.categoryX + 76.0f + Fonts.fontTahoma.getStringWidth(this.newcatename(this.category)) + 2.0f && mouseY >= this.categoryY - 29.0f - 3.0f && mouseY <= this.categoryY - 29.0f - 3.0f + 9.0f;
    }
    
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        this.moduleList.forEach(s -> s.mouseClicked(mouseX, mouseY, mouseButton));
    }
    
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
        this.moduleList.forEach(e -> e.mouseReleased(mouseX, mouseY, state));
    }
    
    public void keyTyped(final char typedChar, final int keyCode) {
        this.moduleList.forEach(e -> e.keyTyped(typedChar, keyCode));
    }
    
    public void setSelected(final boolean s) {
        this.selected = s;
    }
    
    public boolean isSelected() {
        return this.selected;
    }
}
