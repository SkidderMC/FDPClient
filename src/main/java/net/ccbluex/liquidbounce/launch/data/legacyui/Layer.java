package net.ccbluex.liquidbounce.launch.data.legacyui;

import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class Layer {
    public static int draw(int FullWidth,int FullHeight,Float width,Float height,String title,String con,int opacity,int mouseX,int mouseY,boolean click){
        Gui.drawRect(0,0,FullWidth,FullHeight,new Color(0,0,0,120).getRGB());
        RenderUtils.drawRoundedCornerRect((FullWidth-width)/2,(FullHeight-height)/2,(FullWidth+width)/2,(FullHeight+height)/2,3f,new Color(245,245,245,255).getRGB());
        FontLoaders.F30.DisplayFonts(title,(FullWidth-width)/2+10,(FullHeight-height)/2+10,new Color(0,0,0).getRGB(),FontLoaders.F30);
        String[] lines=con.split("#");
        int i=0;
        for(String line:lines) {
            i++;
            FontLoaders.F18.DisplayFonts(line, (FullWidth - width) / 2 + 10, (FullHeight - height) / 2 + 5 + (i*10) + FontLoaders.F30.getHeight(), new Color(50, 50, 50).getRGB(), FontLoaders.F18);
        }
        boolean hover=((FullWidth+width)/2-60)<mouseX && ((FullWidth+width)/2-10)> mouseX && ((FullHeight+height)/2-30)<mouseY && ((FullHeight+height)/2-10)>mouseY;
        if(hover) RenderUtils.drawRoundedCornerRect((FullWidth+width)/2-60,(FullHeight+height)/2-30,(FullWidth+width)/2-10,(FullHeight+height)/2-10,3f,new Color(200,200,200,255).getRGB());
        else{
            RenderUtils.drawRoundedCornerRect((FullWidth+width)/2-60,(FullHeight+height)/2-30,(FullWidth+width)/2-10,(FullHeight+height)/2-10,3f,new Color(240,240,240,255).getRGB());
        }
        FontLoaders.F18.DisplayFonts("Yes",(FullWidth+width)/2-35-(FontLoaders.F18.DisplayFontWidths(FontLoaders.F18,"Yes")/2),(FullHeight+height)/2-24,new Color(33,150,243).getRGB(),FontLoaders.F18);
        if(click && hover){
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("random.click"), 0.8F));
            return 2;
        }
        return 1;
    }
}
