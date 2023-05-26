package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Settings.*;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Utils.Position;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import net.ccbluex.liquidbounce.features.value.*;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleRender  {
    public Position pos;
    public float y;
    public float x;
    private float offset;
    private final Module parentModule;
    public boolean selected;
    public boolean binds;
    public int height;
    public int scrollY;
    public List<Downward> downwards;
    private float modulex;
    private float moduley;
    
    public ModuleRender(final Module module, final float modX, final float modY, final float w, final float h) {
        this.offset = 0.0f;
        this.height = 0;
        this.scrollY = 0;
        this.downwards = new ArrayList<Downward>();
        this.parentModule = module;
        int cHeight = 20;
        for (final Value setting : module.getValues()) {
            if (setting instanceof IntegerValue) {
                this.downwards.add(new NumberSetting((IntegerValue)setting, modX, modY + cHeight, 0, 0, this));
                cHeight += 16;
            }
            if (setting instanceof FloatValue) {
                this.downwards.add(new FloatSetting((FloatValue)setting, modX, modY + cHeight, 0, 0, this));
                cHeight += 16;
            }
            if (setting instanceof BoolValue) {
                this.downwards.add(new BoolSetting((BoolValue)setting, modX, modY + cHeight - 6.0f, 0, 0, this));
                cHeight += 16;
            }
            if (setting instanceof ListValue) {
                this.downwards.add(new ListSetting((ListValue)setting, modX, modY + cHeight, -6, 0, this));
                cHeight += 22;
            }
            if (setting instanceof TextValue) {
                this.downwards.add(new TextSetting((TextValue)setting, modX, modY + cHeight, -6, 0, this));
                cHeight += 22;
            }
            if (setting instanceof ColorValue) {
                this.downwards.add(new ColorSetting((ColorValue)setting, modX, modY + cHeight, -6, 0, this));
                cHeight += 22;
            }
        }
        this.height = cHeight;
        this.pos = new Position(modX, modY, w, (float)cHeight);
    }
    
    public void drawScreen(final int mouseX, final int mouseY) {
        int guiColor = ClickGUIModule.INSTANCE.generateColor().getRGB();
        try {
            this.modulex = OtcClickGUi.getMainx();
            this.moduley = OtcClickGUi.getMainy();
            this.x = this.pos.x;
            this.y = this.pos.y + this.getScrollY();
            RoundedUtil.drawRound(this.modulex + 5.0f + this.x, this.moduley + 17.0f + this.y, 135.0f, this.pos.height, 3.0f, new Color(50, 54, 65));
            RoundedUtil.drawGradientHorizontal(this.modulex + 5.0f + this.x, this.moduley + 18.0f + this.y, 135.0f, 1.5f, 1.0f, new Color(guiColor), new Color(guiColor));
            Fonts.fontTahoma.drawString(this.parentModule.getName(), this.modulex + 5.0f + this.x, this.moduley + 17.0f + this.y - 8.0f, new Color(255, 255, 255).getRGB());
            RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.x + 4.0f, this.moduley + 17.0f + this.y + 8.0f, 7.0f, 7.0f, 1.0f, this.parentModule.getState() ? new Color(86, 94, 115).getRGB() : new Color(50, 54, 65).getRGB(), 1.0f, this.parentModule.getState() ? new Color(86, 94, 115).getRGB() : new Color(85, 90, 96).getRGB());
            if (this.isHovered(mouseX, mouseY)) {
                RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.x + 4.0f, this.moduley + 17.0f + this.y + 8.0f, 7.0f, 7.0f, 1.0f, new Color(0, 0, 0, 0).getRGB(), 1.0f, new Color(guiColor).getRGB());
            }
            Fonts.fontTahoma.drawString("Enable", this.modulex + 5.0f + this.x + 4.0f + 10.0f, this.moduley + 17.0f + this.y + 8.0f + 3.0f, new Color(200, 200, 200).getRGB());
            for (final Downward downward : this.downwards) {
                downward.draw(mouseX, mouseY);
            }
            RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.x + 115.0f - Fonts.fontTahoma.getStringWidth(this.binds ? "..." : Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase()) + 13.0f, this.moduley + 17.0f + this.y + 8.0f + 0.5f, (float)(Fonts.fontTahoma.getStringWidth(this.binds ? "..." : Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase()) + 4), 7.0f, 1.0f, new Color(28, 32, 40).getRGB(), 1.0f, new Color(86, 94, 115).getRGB());
            Fonts.fontTahoma.drawString(this.binds ? "..." : Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase(), this.modulex + 5.0f + this.x + 117.0f - Fonts.fontTahoma.getStringWidth(this.binds ? "..." : Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase()) + 13.0f, this.moduley + 17.0f + this.y + 11.0f, -1);
            if (this.isKeyBindHovered(mouseX, mouseY)) {
                RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.x + 115.0f - Fonts.fontTahoma.getStringWidth(this.binds ? "..." : Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase()) + 13.0f, this.moduley + 17.0f + this.y + 8.0f + 0.5f, (float)(Fonts.fontTahoma.getStringWidth(this.binds ? "..." : Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase()) + 4), 7.0f, 1.0f, new Color(0, 0, 0, 0).getRGB(), 1.0f, guiColor);
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    
    public boolean isSelected() {
        return this.selected;
    }
    
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }
    
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.isKeyBindHovered(mouseX, mouseY) && mouseButton == 0) {
            this.binds = true;
        }
        if (!this.isKeyBindHovered(mouseX, mouseY) && this.binds && mouseButton == 0) {
            this.binds = false;
        }
        if (this.isHovered(mouseX, mouseY) && mouseButton == 0) {
            this.parentModule.toggle();
        }
        if (this.binds && mouseButton == 1) {
            this.parentModule.setKeyBind(0);
            this.binds = false;
        }
        this.downwards.forEach(e -> e.mouseClicked(mouseX, mouseY, mouseButton));
    }
    
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
        this.downwards.forEach(e -> e.mouseReleased(mouseX, mouseY, state));
    }
    
    public void keyTyped(final char typedChar, int keyCode) {
        final int finalKeyCode = keyCode;
        this.downwards.forEach(e -> e.keyTyped(typedChar, finalKeyCode));
        if (this.binds) {
            if (keyCode == 211) {
                keyCode = 0;
            }
            this.parentModule.setKeyBind(keyCode);
            this.binds = false;
        }
    }
    
    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.modulex + 5.0f + this.x + 4.0f && mouseX <= this.modulex + 5.0f + this.x + 4.0f + 15.0f && mouseY >= this.moduley + 17.0f + this.y + 8.0f && mouseY <= this.moduley + 17.0f + this.y + 8.0f + 6.0f;
    }
    
    public boolean isKeyBindHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.modulex + 5.0f + this.x + 115.0f - Fonts.fontTahoma.getStringWidth(Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase()) + 13.0f && mouseX <= this.modulex + 5.0f + this.x + 115.0f - Fonts.fontTahoma.getStringWidth(Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase()) + 13.0f + Fonts.fontTahoma.getStringWidth(Keyboard.getKeyName(this.parentModule.getKeyBind()).toLowerCase()) + 3.0f && mouseY >= this.moduley + 17.0f + this.y + 8.0f + 0.5f && mouseY <= this.moduley + 17.0f + this.y + 8.0f + 0.5f + 7.0f;
    }
    
    public Module getparent() {
        return this.parentModule;
    }
    
    public float getY() {
        return this.pos.y + this.getScrollY();
    }
    
    public float getMaxValueY() {
        return this.downwards.get(this.downwards.size() - 1).getY();
    }
    
    public void setY(final float y) {
        this.pos.y = y;
    }
    
    public int getScrollY() {
        return this.scrollY;
    }
    
    public void setScrollY(final int scrollY) {
        this.scrollY = scrollY;
    }
    
    public float getOffset() {
        return this.offset;
    }
    
    public void setOffset(final float offset) {
        this.offset = offset;
    }
}
