/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements;

import net.ccbluex.liquidbounce.features.value.IntegerValue;
import net.ccbluex.liquidbounce.ui.client.hud.element.Border;
import net.ccbluex.liquidbounce.ui.client.hud.element.Element;
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.AnimationHelper;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.TimerUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;

import java.awt.*;

@ElementInfo(name = "Indicators")
public class Indicators extends Element {

    public static final IntegerValue rRed = new IntegerValue("Red", 0, 0, 255);
    public static final IntegerValue rGreen = new IntegerValue("Green", 0, 0, 255);
    public static final IntegerValue rBlue = new IntegerValue("Blue", 0, 0, 255);


    public final IntegerValue indx = new IntegerValue("noting",120,0,1000);
    public final IntegerValue indy = new IntegerValue("noting2",80,0,1000);

    public static Color mainColor() {
        return new Color(rRed.get(), rGreen.get(), rBlue.get());
    }

    private double armorBarWidth;

    private double hurttimeBarWidth;

    private double bpsBarWidth;

    private double healthBarWidth;

    public final int x2 = indx.get();
    public final int y3 = indy.get();

    final TimerUtil timerHelper = new TimerUtil();

    final ScaledResolution sr = new ScaledResolution(mc);
    final float scaledWidth = sr.getScaledWidth();
    final float scaledHeight = sr.getScaledHeight();

    @Override
    public Border drawElement(float partialTicks) {
        double prevZ = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        double prevX = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double lastDist = Math.sqrt(prevX * prevX + prevZ * prevZ);
        double currSpeed = lastDist * 15.3571428571D / 4;

        final float xX = scaledWidth / 2.0f -x2;
        final float yX = scaledHeight / 2.0f + y3;
        RenderUtils.drawNewRect(xX + 4.5, yX  + 196.5 - 405, xX + 100.5, yX + 246.5 - 408, new Color(11, 11, 11, 255).getRGB());
        RenderUtils.drawNewRect(xX + 5, yX + 198 - 405, xX + 100, yX + 246 - 408, new Color(28, 28, 28, 255).getRGB());
        RenderUtils.drawNewRect(xX + 5, yX  + 198 - 405, xX + 100, yX + 208 - 408, new Color(21, 19, 20, 255).getRGB());
        RenderUtils.drawNewRect(xX + 44, yX + 210 - 406, xX + 95, yX + 213.5 - 406, new Color(41, 41, 41, 255).getRGB());
        RenderUtils.drawNewRect(xX + 44, yX + 219 - 406, xX + 95, yX + 222.5 - 406, new Color(41, 41, 41, 255).getRGB());
        RenderUtils.drawNewRect(xX + 44, yX + 228 - 406, xX + 95, yX + 231.5 - 406, new Color(41, 41, 41, 255).getRGB());
        RenderUtils.drawNewRect(xX + 44, yX + 237 - 406, xX + 95, yX + 240.5 - 406, new Color(41, 41, 41, 255).getRGB());
        RenderUtils.drawNewRect(xX + 5, yX + 197 - 405, xX + 100, yX + 198 - 405, mainColor().getRGB());

        Fonts.fontSFUI35.drawString("Indicators", xX + 37, yX + 202 - 406, -1);

        // armor
        final float armorValue = mc.thePlayer.getTotalArmorValue();
        double armorPercentage = armorValue / 20;
        armorPercentage = MathHelper.clamp_double(armorPercentage, 0.0, 1.0);

        final double armorWidth = 51 * armorPercentage;
        this.armorBarWidth = AnimationHelper.animate(armorWidth, this.armorBarWidth, 0.0229999852180481);
        RenderUtils.drawRect(xX + 44, yX + 210 - 406, xX + 44 + this.armorBarWidth, yX + 213.5 - 406, mainColor().getRGB());

        Fonts.fontSFUI35.drawString("Armor", xX + 8, yX + 211 - 406, -1);

        // HurtTime
        double hurttimePercentage = MathHelper.clamp_double(mc.thePlayer.hurtTime, 0.0, 0.6);
        final double hurttimeWidth = 51.0 * hurttimePercentage;
        this.hurttimeBarWidth = AnimationHelper.animate(hurttimeWidth, this.hurttimeBarWidth, 0.0429999852180481);
        RenderUtils.drawRect(xX + 44, yX + 219 - 406, xX + 44 + this.hurttimeBarWidth, yX + 222.5 - 406, mainColor().getRGB());

        Fonts.fontSFUI35.drawString("HurtTime", xX + 8, yX + 220 - 406, -1);

        // HurtTime
        double bpsPercentage = MathHelper.clamp_double(currSpeed, 0.0, 1.0);
        final double bpsBarWidth = 51.0 * bpsPercentage;
        this.bpsBarWidth = AnimationHelper.animate(bpsBarWidth, this.bpsBarWidth, 0.0329999852180481);

        RenderUtils.drawRect(xX + 44, yX + 228 - 406, xX + 44 + this.bpsBarWidth, yX + 231.5 - 406, mainColor().getRGB());

        Fonts.fontSFUI35.drawString("BPS", xX + 8, yX + 229 - 406, -1);

        // HurtTime
        final float health = mc.thePlayer.getHealth();
        double hpPercentage = health / mc.thePlayer.getMaxHealth();
        hpPercentage = MathHelper.clamp_double(hpPercentage, 0.0, 1.0);
        final double hpWidth = 51.0 * hpPercentage;
        final String healthStr = String.valueOf((int) mc.thePlayer.getHealth() / 2.0f);

        if (timerHelper.hasReached(15L)) {
            this.healthBarWidth = AnimationHelper.animate(hpWidth, this.healthBarWidth, 0.2029999852180481);
            timerHelper.reset();
        }

        RenderUtils.drawRect(xX + 44, yX + 237 - 406, xX + 44 + this.healthBarWidth, yX + 240.5 - 406, mainColor().getRGB());

        Fonts.fontSFUI35.drawString("HP", xX + 8, yX + 238 - 406, -1);
        return new Border(xX + 5, yX + 198 - 405, xX + 100, yX + 246 - 408);
    }
}