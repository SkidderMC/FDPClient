/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.modernui.clickgui.style.styles.light.ModuleSettings.Setting;

import net.ccbluex.liquidbounce.utils.AnimationHelper;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.*;
import org.lwjgl.input.Mouse;
import java.awt.*;

public class Settings extends Setting {
    private final AnimationHelper alphaAnim;
    public Settings(AnimationHelper alphaAnim) {
        this.alphaAnim = alphaAnim;
    }

    @Override
    public void drawListValue(boolean previousMouse, int mouseX, int mouseY, float mY, float startX, ListValue listValue) {
        float x = startX + 295;
        font.drawString(listValue.getName(), startX + 210, mY + 1, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());
        RenderUtils.drawRect(x, mY - 5, x + 80, mY - 4, new Color(0, 100, 255,alphaAnim.getAlpha()).getRGB());
        RenderUtils.drawRect(x, mY + 10, x + 80, mY + 11, new Color(0, 100, 255,alphaAnim.getAlpha()).getRGB());
        RenderUtils.drawRect(x + 80, mY - 5, x + 81, mY + 11, new Color(0, 100, 255,alphaAnim.getAlpha()).getRGB());
        RenderUtils.drawRect(x, mY - 5, x + 1, mY + 10, new Color(0, 100, 255,alphaAnim.getAlpha()).getRGB());
        font.drawString(listValue.get(),
                x + 10,
                mY + 4, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());
        if (this.isHovered(x, mY - 5, x + 80, mY + 11, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0) && !previousMouse) {
                String current = listValue.get();
                int next = listValue.getModeListNumber(current) + 1 >= listValue.getValues().length ? 0
                        : listValue.getModeListNumber(current) + 1;
                listValue.set(listValue.getValues()[next]);
            }
        }
    }

    @Override
    public void drawTextValue(float startX, float mY,TextValue textValue) {
        font.drawString(textValue.getName() + ": " + textValue.get(), startX + 210, mY, new Color(80,80,80).getRGB());
    }

    @Override
    public String toString() {
        return "Light Client Settings";
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void drawFloatValue(int mouseX,float mY,float startX,boolean previousMouse,boolean buttonDown,FloatValue floatValue) {
        float x = startX + 300;
        double render =  (68.0F
                * (floatValue.get() - floatValue.getMinimum())
                / (floatValue.getMaximum()
                - floatValue.getMinimum())
                + 1);
        RenderUtils.drawRect( x - 6, mY + 2, (float) ((double) x + 75), mY + 3,
                (new Color(200, 200, 200,alphaAnim.getAlpha())).getRGB());
        RenderUtils.drawRect( x - 6, mY + 2, (float) ((double) x + render + 6.5D), mY + 3,
                (new Color(61, 141, 255,alphaAnim.getAlpha())).getRGB());
        RenderUtils.circle((float) ((double) x + render + 4D), mY + 2.5F, 2, new Color(61, 141, 255,alphaAnim.getAlpha()));
        font.drawString(String.valueOf(floatValue.get()),
                (float) ((double) x + render - 5), mY - 7, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());
        font.drawString(floatValue.getName(), startX + 210, mY, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());
        if (buttonDown && Mouse.isButtonDown(0)) {
            if (!previousMouse && Mouse.isButtonDown(0)) {
                render = floatValue.getMinimum();
                double max = floatValue.getMaximum();
                double inc = 0.01;
                double valAbs = (double) mouseX - ((double) x + 1.0D);
                double perc = valAbs / 68.0D;
                perc = Math.min(Math.max(0.0D, perc), 1.0D);
                double valRel = (max - render) * perc;
                double val = render + valRel;
                val =  Math.round(val * (1.0D / inc)) / (1.0D / inc);
                floatValue.set((float) val);
            }
        }
    }

    @Override
    public void drawIntegerValue(int mouseX, float mY, float startX, boolean previousMouse, boolean buttonDown, IntegerValue integerValue) {
        float x = startX + 300;
        double render =  (68.0F
                * (integerValue.get() - integerValue.getMinimum())
                / (integerValue.getMaximum()
                - integerValue.getMinimum())
                + 1);
        RenderUtils.drawRect( x - 6, mY + 2, (float) ((double) x + 75), mY + 3,
                (new Color(200, 200, 200)).getRGB());
        RenderUtils.drawRect( x - 6, mY + 2, (float) ((double) x + render + 6.5D), mY + 3,
                (new Color(61, 141, 255)).getRGB());
        RenderUtils.circle((float) ((double) x + render + 4D), mY + 2.5F, 2, new Color(61, 141, 255,alphaAnim.getAlpha()));
        font.drawString(String.valueOf(integerValue.get()),
                (float) ((double) x + render - 5), mY - 7, new Color(80, 80, 80).getRGB());
        font.drawString(integerValue.getName(), startX + 210, mY, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());

        if (buttonDown && Mouse.isButtonDown(0)) {
            if (!previousMouse && Mouse.isButtonDown(0)) {
                render = integerValue.getMinimum();
                double max = integerValue.getMaximum();
                double inc = 1.0;
                double valAbs = (double) mouseX - ((double) x + 1.0D);
                double perc = valAbs / 68.0D;
                perc = Math.min(Math.max(0.0D, perc), 1.0D);
                double valRel = (max - render) * perc;
                double val = render + valRel;
                val = Math.round(val * (1.0D / inc)) / (1.0D / inc);
                integerValue.set((int) val);
            }
        }
    }
    @Override
    public void drawBoolValue(boolean mouse,int mouseX,int mouseY,float startX,float mY,BoolValue boolValue) {
        float x = startX + 325;
        font.drawString(boolValue.getName(), startX + 210, mY, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());
        RenderUtils.drawRoundedRect2(x + 30, mY - 2, x + 50, mY + 8, 4, boolValue.get() ? new Color(66, 134, 245,alphaAnim.getAlpha()).getRGB() : new Color(114, 118, 125,alphaAnim.getAlpha()).getRGB());
        RenderUtils.circle(x + 40 + boolValue.getAnimation().getAnimationX(), mY + 3, 4, boolValue.get() ? new Color(255,255,255,alphaAnim.getAlpha()).getRGB() : new Color(164, 168, 175,alphaAnim.getAlpha()).getRGB());
        if(boolValue.getAnimation().getAnimationX() > -5F && !boolValue.get())
            boolValue.getAnimation().animationX -= 1F;
        else if(boolValue.getAnimation().getAnimationX() < 5F && boolValue.get())
            boolValue.getAnimation().animationX += 1F;
        if (this.isHovered(x + 30, mY + 2, x + 50, mY + 12, mouseX, mouseY)) {
            if (mouse)
                boolValue.set(!boolValue.get());
        }
    }

    @Override
    public void drawColorValue(float startX, float mY,float x, int mouseX, int mouseY, Value.ColorValue colorValue) {
        font.drawString(colorValue.getName(), startX + 210, mY - 2, new Color(80, 80, 80).getRGB());
        float y = mY - 8;
        int ticks;
        int shits;
        final double aDouble = (((mouseX - x) / 50.0 + Math.sin(1.6)) % 1.0F);
        for(ticks = 0; ticks <50; ticks +=1) {
            Color rainbowColor = new Color(Color.HSBtoRGB((float) (ticks / 50.0 + Math.sin(1.6)) % 1.0f, 1.0f, 1.0f));
            if(mouseX>x&&mouseX<x + 50&&mouseY>y&&mouseY<y+13 && Mouse.isButtonDown(0)){
                colorValue.set(new Color(Color.HSBtoRGB((float) aDouble, 1.0F, 1.0f)).getRGB());
                RenderUtils.drawRect(mouseX-1,mouseY-1,mouseX+1,mouseY+1,new Color(100,100,100,100).getRGB());
            }
            if(mouseX>x&&mouseX<x + 50&&mouseY>y&&mouseY<y+13){
                RenderUtils.drawRect(mouseX-1,mouseY-1,mouseX+1,mouseY+1,new Color(100,100,100,100).getRGB());
            }
            RenderUtils.drawRect(x + ticks, y, x + ticks + 1, y + 13, rainbowColor.getRGB());
        }
        for(shits = 0; shits < 50; shits++) {
            Color rainbowColor = new Color(Color.HSBtoRGB((float) (shits / 50.0 + Math.sin(1.6)) % 1.0f, 0.5F, 1.0f));
            if(mouseX>x&&mouseX<x + 100 && mouseY>y&&mouseY<y+13 && Mouse.isButtonDown(0)){
                colorValue.set(new Color(Color.HSBtoRGB((float) aDouble, 0.5F, 1.0f)).getRGB());
                RenderUtils.drawRect(mouseX-1,mouseY-1,mouseX+1,mouseY+1,new Color(100,100,100,100).getRGB());
            }
            if(mouseX>x&&mouseX<x + 100&&mouseY>y&&mouseY<y+13){
                RenderUtils.drawRect(mouseX-1,mouseY-1,mouseX+1,mouseY+1,new Color(100,100,100,100).getRGB());
            }
            RenderUtils.drawRect(x + shits + 50, y, x + shits + 51, y + 13, rainbowColor.getRGB());
        }
        RenderUtils.drawRect(x, y+16, x + 50, y + 20,colorValue.get());
    }
}
