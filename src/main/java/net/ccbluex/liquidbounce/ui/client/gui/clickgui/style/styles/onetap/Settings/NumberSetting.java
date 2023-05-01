package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Settings;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.*;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.math.MathUtil;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import net.ccbluex.liquidbounce.features.value.IntegerValue;
import net.ccbluex.liquidbounce.features.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class NumberSetting extends Downward<IntegerValue>
{
    private float modulex;
    private float moduley;
    private float numbery;
    public float percent;
    private boolean iloveyou;
    
    public NumberSetting(final IntegerValue s, final float x, final float y, final int width, final int height, final ModuleRender moduleRender) {
        super(s, x, y, width, height, moduleRender);
        this.percent = 0.0f;
    }
    
    @Override
    public void draw(final int mouseX, final int mouseY) {
        int guiColor = ClickGUIModule.generateColor().getRGB();
        this.modulex = OtcClickGUi.getMainx();
        this.moduley = OtcClickGUi.getMainy();
        this.numbery = this.pos.y + this.getScrollY();
        Minecraft.getMinecraft();
        final double clamp = MathHelper.clamp_double((double)(Minecraft.getDebugFPS() / 30), 1.0, 9999.0);
        final double percentBar = (((IntegerValue)this.setting).get() - (double)((IntegerValue)this.setting).getMinimum()) / (((IntegerValue)this.setting).getMaximum() - ((IntegerValue)this.setting).getMinimum());
        this.percent = Math.max(0.0f, Math.min(1.0f, (float)(this.percent + (Math.max(0.0, Math.min(percentBar, 1.0)) - this.percent) * (0.2 / clamp))));
        RoundedUtil.drawRound(this.modulex + 5.0f + this.pos.x + 55.0f, this.moduley + 17.0f + this.numbery + 8.0f, 75.0f, 2.5f, 1.0f, new Color(34, 38, 48));
        RoundedUtil.drawRound(this.modulex + 5.0f + this.pos.x + 55.0f, this.moduley + 17.0f + this.numbery + 8.0f, 75.0f * this.percent, 2.5f, 1.0f, new Color(guiColor));
        Fonts.fontTahoma.drawString(((IntegerValue)this.setting).getName(), this.modulex + 5.0f + this.pos.x + 4.0f, this.moduley + 17.0f + this.numbery + 8.0f, new Color(200, 200, 200).getRGB());
        if (this.iloveyou) {
            final float percentt = Math.min(1.0f, Math.max(0.0f, (mouseX - (this.modulex + 5.0f + this.pos.x + 55.0f)) / 99.0f * 1.3f));
            final double newValue = percentt * (((IntegerValue)this.setting).getMaximum() - ((IntegerValue)this.setting).getMinimum()) + ((IntegerValue)this.setting).getMinimum();
            final double set = MathUtil.incValue(newValue, 1.0);
            ((IntegerValue)this.setting).set(set);
        }
        final ClickGUIModule cg = (ClickGUIModule)LiquidBounce.moduleManager.getModule(ClickGUIModule.class);
        if (this.iloveyou || this.isHovered(mouseX, mouseY) || cg.disp.get()) {
            RoundedUtil.drawRound(this.modulex + 5.0f + this.pos.x + 55.0f + 61.0f * this.percent, this.moduley + 17.0f + this.numbery + 8.0f + 6.0f, (float)(Fonts.fontTahoma.getStringWidth(((Value<?>)this.setting).get() + "") + 2), 6.0f, 1.0f, new Color(32, 34, 39));
            Fonts.fontTahoma.drawString(((Value<?>)this.setting).get() + "", this.modulex + 5.0f + this.pos.x + 55.0f + 62.0f * this.percent, this.moduley + 17.0f + this.numbery + 8.0f + 8.0f, new Color(250, 250, 250).getRGB());
        }
        if (this.isHovered(mouseX, mouseY)) {
            RenderUtils.drawRoundedRect(this.modulex + 5.0f + this.pos.x + 55.0f, this.moduley + 17.0f + this.numbery + 8.0f, 75.0f, 2.5f, 1.0f, new Color(0, 0, 0, 0).getRGB(), 1.0f, guiColor);
        }
    }
    
    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.isHovered(mouseX, mouseY) && mouseButton == 0) {
            this.iloveyou = true;
        }
    }
    
    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
        if (state == 0) {
            this.iloveyou = false;
        }
    }
    
    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.modulex + 5.0f + this.pos.x + 55.0f && mouseX <= this.modulex + 5.0f + this.pos.x + 55.0f + 75.0f && mouseY >= this.moduley + 17.0f + this.numbery + 8.0f && mouseY <= this.moduley + 17.0f + this.numbery + 8.0f + 2.5f;
    }
}
