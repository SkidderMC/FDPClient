package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Settings;

import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.*;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.misc.Direction;
import net.ccbluex.liquidbounce.utils.misc.EaseInOutQuad;
import net.ccbluex.liquidbounce.utils.misc.Animation;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.ListValue;
import net.ccbluex.liquidbounce.features.value.Value;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ListSetting extends Downward  {
    private ListValue listValue;
    private final Animation arrowAnimation;
    private float modulex;
    private float moduley;
    private float listy;
    
    public ListSetting(final ListValue s, final float x, final float y, final int width, final int height, final ModuleRender moduleRender) {
        super(s, x, y, width, height, moduleRender);
        this.arrowAnimation = new EaseInOutQuad(250, 1.0, Direction.BACKWARDS);
        this.listValue = s;
    }
    
    @Override
    public void draw(final int mouseX, final int mouseY) {
        int guiColor = ClickGUIModule.INSTANCE.generateColor().getRGB();
        this.modulex = OtcClickGUi.getMainx();
        this.moduley = OtcClickGUi.getMainy();
        this.listy = this.pos.y + this.getScrollY();
        Fonts.fontTahoma.drawString(this.listValue.getName(), this.modulex + 5.0f + this.pos.x + 4.0f, this.moduley + 17.0f + this.listy + 13.0f, new Color(200, 200, 200).getRGB());
        RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.pos.x + 80.0f, this.moduley + 17.0f + this.listy + 8.0f, 50.0f, 11.0f, 1.0f, new Color(59, 63, 72).getRGB(), 1.0f, new Color(85, 90, 96).getRGB());
        if (this.isHovered(mouseX, mouseY)) {
            RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.pos.x + 80.0f, this.moduley + 17.0f + this.listy + 8.0f, 50.0f, 11.0f, 1.0f, new Color(0, 0, 0, 0).getRGB(), 1.0f, guiColor);
        }
        Fonts.fontTahoma.drawString(this.listValue.get() + "", this.modulex + 5.0f + this.pos.x + 82.0f, this.moduley + 17.0f + this.listy + 13.0f, new Color(200, 200, 200).getRGB());
        this.arrowAnimation.setDirection(this.listValue.openList ? Direction.FORWARDS : Direction.BACKWARDS);
        RenderUtils.drawClickGuiArrow(this.modulex + 5.0f + this.pos.x + 123.5f, this.moduley + 17.0f + this.listy + 13.0f, 4.0f, this.arrowAnimation, new Color(222, 224, 236).getRGB());
        if (this.listValue.openList) {
            GL11.glTranslatef(0.0f, 0.0f, 2.0f);
            RenderUtils.drawBorderedRect(this.modulex + 5.0f + this.pos.x + 80.0f, this.moduley + 17.0f + this.listy + 8.0f + 13.0f, this.modulex + 5.0f + this.pos.x + 80.0f + 50.0f, this.moduley + 17.0f + this.listy + 8.0f + 13.0f + this.listValue.getModes().size() * 11.0f, 1.0f, new Color(85, 90, 96).getRGB(), new Color(59, 63, 72).getRGB());
            for (final String option : this.listValue.getModes()) {
                Fonts.fontTahoma.drawString(option, this.modulex + 5.0f + this.pos.x + 82.0f, this.moduley + 17.0f + this.listy + 1.0f + 13.0f + 12.0f + this.listValue.getModeListNumber(option) * 11, option.equals(((Value<?>)this.listValue).get()) ? guiColor : new Color(200, 200, 200).getRGB());
            }
            GL11.glTranslatef(0.0f, 0.0f, -2.0f);
        }
    }
    
    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (mouseButton == 1 && this.isHovered(mouseX, mouseY)) {
            this.listValue.openList = !this.listValue.openList;
        }
        if (mouseButton == 0 && this.listValue.openList && mouseX >= this.modulex + 5.0f + this.pos.x + 80.0f && mouseX <= this.modulex + 5.0f + this.pos.x + 80.0f + 50.0f) {
            for (int i = 0; i < this.listValue.getModes().size(); ++i) {
                final int v = (int)(this.moduley + 17.0f + this.listy + 8.0f + 13.0f + i * 11);
                if (mouseY >= v && mouseY <= v + 11) {
                    this.listValue.set(this.listValue.getModeGet(i));
                    this.listValue.openList = false;
                }
            }
        }
    }
    
    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.modulex + 5.0f + this.pos.x + 80.0f && mouseX <= this.modulex + 5.0f + this.pos.x + 80.0f + 50.0f && mouseY >= this.moduley + 17.0f + this.listy + 8.0f && mouseY <= this.moduley + 17.0f + this.listy + 8.0f + 11.0f;
    }
    
    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
    }
}
