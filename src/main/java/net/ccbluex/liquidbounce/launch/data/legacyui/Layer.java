package net.ccbluex.liquidbounce.launch.data.legacyui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class Layer {
    public static int draw(int FullWidth,int FullHeight,Float width,Float height,String title,String con,int opacity,int mouseX,int mouseY,boolean click){
        Gui.drawRect(0,0,FullWidth,FullHeight, new Color(0,0,0,120).getRGB());
        RenderUtils.drawRoundedCornerRect((FullWidth-width)/2,(FullHeight-height)/2,(FullWidth+width)/2,(FullHeight+height)/2,3f,LiquidBounce.INSTANCE.getDarkmode() ? new Color(245,245,245,255).getRGB() : new Color(20,20,20,255).getRGB());
        FontLoaders.F30.DisplayFonts(title,(FullWidth-width)/2+10,(FullHeight-height)/2+10,!LiquidBounce.INSTANCE.getDarkmode() ? new Color(230,230,230).getRGB() : new Color(0,0,0).getRGB(),FontLoaders.F30);
        String[] lines=con.split("#");
        int i=0;
        for(String line:lines) {
            i++;
            FontLoaders.F18.DisplayFonts(line, (FullWidth - width) / 2 + 10, (FullHeight - height) / 2 + 5 + (i*10) + FontLoaders.F30.getHeight(),!LiquidBounce.INSTANCE.getDarkmode() ? new Color(220, 220, 220).getRGB() : new Color(50, 50, 50).getRGB(), FontLoaders.F18);
        }
        boolean hover=((FullWidth+width)/2-60)<mouseX && ((FullWidth+width)/2-10)> mouseX && ((FullHeight+height)/2-30)<mouseY && ((FullHeight+height)/2-10)>mouseY;
        if(hover) RenderUtils.drawRoundedCornerRect((FullWidth+width)/2-60,(FullHeight+height)/2-30,(FullWidth+width)/2-10,(FullHeight+height)/2-10,3f,new Color(0,0,0,90).getRGB());
        else{
            RenderUtils.drawRoundedCornerRect((FullWidth+width)/2-60,(FullHeight+height)/2-30,(FullWidth+width)/2-10,(FullHeight+height)/2-10,3f,new Color(0,0,0,30).getRGB());
        }
        FontLoaders.F18.DisplayFonts("Yes",(FullWidth+width)/2-35-(FontLoaders.F18.DisplayFontWidths(FontLoaders.F18,"Yes")/2),(FullHeight+height)/2-24,LiquidBounce.INSTANCE.getDarkmode() ? new Color(33,150,243).getRGB() : new Color(33,150,243,235).getRGB(),FontLoaders.F18);
        if(click && hover){
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("random.click"), 0.8F));
            return 2;
        }
        return 1;
    }
}
