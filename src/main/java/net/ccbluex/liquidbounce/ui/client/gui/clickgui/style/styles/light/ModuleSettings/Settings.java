/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.light.ModuleSettings;

import net.ccbluex.liquidbounce.utils.AnimationHelper;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.*;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class Settings extends Setting {
    private final AnimationHelper alphaAnim;
    public Settings(AnimationHelper alphaAnim) {
        this.alphaAnim = alphaAnim;
    }

    @Override
    public void drawListValue(boolean previousMouse, int mouseX, int mouseY, float mY, float startX, ListValue listValue) {
        float x = startX + 310;
        int l   = font.getStringWidth(listValue.get());
        
        font.drawString(listValue.getName(), startX + 210, mY + 1, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());
        
        
        if (listValue.openList) {
            int height = listValue.getValues().length * (font.FONT_HEIGHT + 2);
            
            
            RenderUtils.drawRoundedRect2(x + 61 - l, mY - 3, x + 92, mY + 11, 2, new Color(230, 230, 230, 200).getRGB());
            RenderUtils.drawRoundedRect2(x + 85, mY - 6, x + 201, (int) (mY + height + 3), 2, new Color(230, 230, 230, 200).getRGB());
            
            RenderUtils.drawRoundedRect2(x + 60 - l, mY - 4, x + 80, mY + 10, 2, new Color(250, 250, 250, 250).getRGB());
            RenderUtils.drawRect(x + 63 - l, mY - 4, x + 90, mY + 10, new Color(250, 250, 250, 250).getRGB());
            
            font.drawString(listValue.get(), x + 70 - l, mY + 1, new Color(80, 80, 80, 250).getRGB());
            font.drawString(">", x + 73, mY + 1, new Color(80, 80, 80, 250).getRGB());
            

            RenderUtils.drawRoundedRect2(x + 85, mY - 8, x + 200, (int) (mY + height + 2), 2, new Color(250, 250, 250, 250).getRGB());
            
            for (int i = 0; i < listValue.getValues().length; i++) {
                
                if (this.isHovered(x + 85, mY - 5 + i * (font.FONT_HEIGHT + 2), x + 200, mY - 3 + font.FONT_HEIGHT + i * (font.FONT_HEIGHT + 2), mouseX, mouseY)) {
                    RenderUtils.drawRoundedRect2(x + 88, mY - 6 + i * (font.FONT_HEIGHT + 2), x + 197, mY - 2 + font.FONT_HEIGHT + i * (font.FONT_HEIGHT + 2), 2, new Color(220, 220, 220, 255).getRGB());
                    if (Mouse.isButtonDown(0) && !previousMouse) {
                        listValue.set(listValue.getValues()[i]);
                    }
                }
                
                if (i == listValue.getModeListNumber(listValue.get())) {
                    RenderUtils.drawRoundedRect2(x + 88, mY - 6 + i * (font.FONT_HEIGHT + 2), x + 197, mY - 2 + font.FONT_HEIGHT + i * (font.FONT_HEIGHT + 2), 2, new Color(200, 200, 200, 255).getRGB());
                }
                
                font.drawString(listValue.getValues()[i], x + 91, mY - 2 + i * (font.FONT_HEIGHT + 2), new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());

            }
                                                      
        } else {
            RenderUtils.drawRoundedRect2(x + 61 - l, mY - 3, x + 81, mY + 11, 2, new Color(230, 230, 230, 200).getRGB());
            RenderUtils.drawRoundedRect2(x + 60 - l, mY - 4, x + 80, mY + 10, 2, new Color(250, 250, 250, 250).getRGB());
            font.drawString(listValue.get(), x + 70 - l, mY + 1, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());
            font.drawString("<", x + 73, mY + 1, new Color(80, 80, 80,alphaAnim.getAlpha()).getRGB());
        }

        
        if (this.isHovered(x + 60 - l, mY - 4, x + 85, mY + 11, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0) && !previousMouse) {
                listValue.openList = !listValue.openList;
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
        RenderUtils.drawRoundedRect2(x + 28, mY - 4, x + 52, mY + 10, 5, boolValue.get() ? new Color(66, 134, 245,alphaAnim.getAlpha()).getRGB() : new Color(114, 118, 125,alphaAnim.getAlpha()).getRGB() );
        RenderUtils.drawRoundedRect2(x + 30, mY - 2, x + 50, mY + 8, 4, new Color(250, 250, 250, 255).getRGB());
        RenderUtils.circle(x + 40 + boolValue.getAnimation().getAnimationX(), mY + 3, 4, boolValue.get() ? new Color(66, 134, 245,alphaAnim.getAlpha()).getRGB() : new Color(174, 174, 174,alphaAnim.getAlpha()).getRGB());
        if (boolValue.get()) {
            boolValue.getAnimation().animationX += (5F - boolValue.getAnimation().animationX) / 2.5;
        } else {
            boolValue.getAnimation().animationX += (-5F - boolValue.getAnimation().animationX) / 2.5;
        }
        
        if (this.isHovered(x + 28, mY - 4, x + 52, mY + 10, mouseX, mouseY)) {
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
