package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Settings;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.*;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Settings.*;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Utils.Position;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.TextValue;
import net.ccbluex.liquidbounce.features.value.Value;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class TextSetting extends Downward<TextValue>  {
    public TextValue textValue;
    private float modulex;
    private float moduley;
    private float texty;
    
    public TextSetting(final TextValue s, final float x, final float y, final int width, final int height, final ModuleRender moduleRender) {
        super(s, x, y, width, height, moduleRender);
        this.textValue = s;
    }
    
    @Override
    public void draw(final int mouseX, final int mouseY) {
        int guiColor = ClickGUIModule.generateColor().getRGB();
        this.modulex = OtcClickGUi.getMainx();
        this.moduley = OtcClickGUi.getMainy();
        this.texty = this.pos.y + this.getScrollY();
        RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.pos.x + 55.0f, this.moduley + 17.0f + this.texty + 8.0f, 75.0f, 11.0f, 1.0f, new Color(59, 63, 72).getRGB(), 1.0f, this.textValue.getTextHovered() ? guiColor : new Color(85, 90, 96).getRGB());
        if (this.isHovered(mouseX, mouseY) && !this.textValue.getTextHovered()) {
            RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.pos.x + 55.0f, this.moduley + 17.0f + this.texty + 8.0f, 75.0f, 11.0f, 1.0f, new Color(0, 0, 0, 0).getRGB(), 1.0f, guiColor);
        }
        Fonts.fontTahoma.drawString(this.textValue.getName(), this.modulex + 5.0f + this.pos.x + 4.0f, this.moduley + 17.0f + this.texty + 13.0f, new Color(200, 200, 200).getRGB());
        if (Fonts.fontTahoma.getStringWidth(this.textValue.getTextHovered() ? (this.textValue.get() + "_") : ((Value<String>)this.textValue).get()) > 70) {
            Fonts.fontTahoma.drawString(Fonts.fontTahoma.trimStringToWidth(this.textValue.getTextHovered() ? (this.textValue.get() + "_") : ((Value<String>)this.textValue).get(), 78, true), this.modulex + 5.0f + this.pos.x + 57.0f, this.moduley + 17.0f + this.texty + 13.0f, new Color(200, 200, 200).getRGB());
        }
        else if (this.textValue.get().isEmpty() && !this.textValue.getTextHovered()) {
            Fonts.fontTahoma.drawString("Type Here...", this.modulex + 5.0f + this.pos.x + 57.0f, this.moduley + 17.0f + this.texty + 13.0f, new Color(200, 200, 200).getRGB());
        }
        else {
            Fonts.fontTahoma.drawString(this.textValue.getTextHovered() ? (this.textValue.get() + "_") : ((Value<String>)this.textValue).get(), this.modulex + 5.0f + this.pos.x + 57.0f, this.moduley + 17.0f + this.texty + 13.0f, new Color(200, 200, 200).getRGB());
        }
    }
    
    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.modulex + 5.0f + this.pos.x + 55.0f && mouseX <= this.modulex + 5.0f + this.pos.x + 55.0f + 75.0f && mouseY >= this.moduley + 17.0f + this.texty + 8.0f && mouseY <= this.moduley + 17.0f + this.texty + 8.0f + 11.0f;
    }
    
    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.isHovered(mouseX, mouseY)) {
            this.textValue.setTextHovered(!this.textValue.getTextHovered());
        }
        else if (this.textValue.getTextHovered()) {
            this.textValue.setTextHovered(false);
        }
    }
    
    @Override
    public void keyTyped(final char typedChar, final int keyCode) {
        if (this.textValue.getTextHovered()) {
            if (keyCode == 1) {
                this.textValue.setTextHovered(false);
            }
            else if (keyCode != 14 && keyCode != 157 && keyCode != 29 && keyCode != 54 && keyCode != 42 && keyCode != 15 && keyCode != 58 && keyCode != 211 && keyCode != 199 && keyCode != 210 && keyCode != 200 && keyCode != 208 && keyCode != 205 && keyCode != 203 && keyCode != 56 && keyCode != 184 && keyCode != 197 && keyCode != 70 && keyCode != 207 && keyCode != 201 && keyCode != 209 && keyCode != 221 && keyCode != 59 && keyCode != 60 && keyCode != 61 && keyCode != 62 && keyCode != 63 && keyCode != 64 && keyCode != 65 && keyCode != 66 && keyCode != 67 && keyCode != 68 && keyCode != 87 && keyCode != 88) {
                this.textValue.append(typedChar);
            }
            if (((TextValue)this.setting).getTextHovered() && Keyboard.isKeyDown(14) && this.textValue.get().length() >= 1) {
                this.textValue.set(this.textValue.get().substring(0, this.textValue.get().length() - 1));
            }
        }
    }
    
    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
    }
}
