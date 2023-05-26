package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Settings;

import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Downward;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.ModuleRender;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.OtcClickGUi;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.BoolValue;

import java.awt.*;

public class BoolSetting extends Downward<BoolValue>  {
    private float modulex;
    private float moduley;
    private float booly;
    
    public BoolSetting(final BoolValue s, final float x, final float y, final int width, final int height, final ModuleRender moduleRender) {
        super(s, x, y, width, height, moduleRender);
    }
    
    @Override
    public void draw(final int mouseX, final int mouseY) {
        int guiColor = ClickGUIModule.INSTANCE.generateColor().getRGB();
        this.modulex = OtcClickGUi.getMainx();
        this.moduley = OtcClickGUi.getMainy();
        this.booly = this.pos.y + this.getScrollY();
        RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.pos.x + 4.0f, this.moduley + 17.0f + this.booly + 8.0f, 7.0f, 7.0f, 1.0f, ((boolean)((BoolValue)this.setting).get()) ? new Color(86, 94, 115).getRGB() : new Color(50, 54, 65).getRGB(), 1.0f, ((boolean)((BoolValue)this.setting).get()) ? new Color(86, 94, 115).getRGB() : new Color(85, 90, 96).getRGB());
        if (this.isHovered(mouseX, mouseY)) {
            RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.pos.x + 4.0f, this.moduley + 17.0f + this.booly + 8.0f, 7.0f, 7.0f, 1.0f, new Color(0, 0, 0, 0).getRGB(), 1.0f, guiColor);
        }
        Fonts.fontTahoma.drawString(((BoolValue)this.setting).getName(), this.modulex + 5.0f + this.pos.x + 4.0f + 10.0f, this.moduley + 17.0f + this.booly + 8.0f + 3.0f, new Color(200, 200, 200).getRGB());
    }
    
    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.isHovered(mouseX, mouseY)) {
            ((BoolValue)this.setting).toggle();
        }
    }
    
    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
    }
    
    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.modulex + 5.0f + this.pos.x + 4.0f && mouseX <= this.modulex + 5.0f + this.pos.x + 4.0f + 135.0f - 128.0f && mouseY >= this.moduley + 17.0f + this.booly + 8.0f && mouseY <= this.moduley + 17.0f + this.booly + 8.0f + 7.0f;
    }
}
