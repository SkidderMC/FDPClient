package net.ccbluex.liquidbounce.launch.data.legacyui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.font.CFontRenderer;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.script.Script;
import net.ccbluex.liquidbounce.utils.render.BlurUtils;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.SmoothRenderUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class GuiScriptLoadMenu extends GuiScreen {
    public MSTimer timer1 = new MSTimer();
    public MSTimer timer2 = new MSTimer();
    public int x=20;
    public int y=20;
    public int dragX;
    public boolean drag;
    public int dragY;

    public int scroll;
    public int scrollTo;
    public int scrollVelocity;
    @Override
    public void initGui() {
        guiOpenTime = System.currentTimeMillis();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseX > x - 2 && mouseX < x + 450 && mouseY > y - 2 && mouseY < y + 20) {
            if (mouseButton == 0) {
                drag = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            }
        }
        if (mouseX > x + 450 && mouseX < x + 490 && mouseY > y + 280 && mouseY < y + 295) {
            LiquidBounce.scriptManager.disableScripts();
            LiquidBounce.scriptManager.unloadScripts();
            LiquidBounce.scriptManager.loadScripts();
            LiquidBounce.scriptManager.enableScripts();
        }
        if (mouseX > x +405 && mouseX < x + 445 && mouseY > y + 280 && mouseY < y + 295) {
            LiquidBounce.scriptManager.disableScripts();
            LiquidBounce.scriptManager.unloadScripts();
        }
        int i=0;
        try {
            for (Script script : LiquidBounce.scriptManager.getScripts()) {
                if (x + 450 < mouseX && y + 41 - scroll + (i * 30) < mouseY && mouseX < x + 490 && mouseY < y - scroll + 59 + (i * 30)) {
                    if (script.getState()) {
                        for (Module registeredModule : script.getRegisteredModules()) {
                            registeredModule.setState(false);
                        }
                        script.onDisable();
                    }
                    else {
                        script.onEnable();
                        script.regAnyThing();
                    }
                }
                if (x + 425 < mouseX && y + 41 - scroll + (i * 30) < mouseY && mouseX < x + 445 && mouseY < y - scroll + 59 + (i * 30) && script.getState()) {
                    for (Module registeredModule : script.getRegisteredModules()) {
                        registeredModule.setState(!registeredModule.getState());
                    }

                }
                i++;
            }
        }catch (Exception E){
            E.printStackTrace();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar,keyCode);
    }
    private long guiOpenTime = -1;

    private boolean translated = false;
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        double pct = Math.max(500d - (System.currentTimeMillis() - guiOpenTime), 0) / (500d);
        if (pct != 0) {
            pct = EaseUtils.INSTANCE.apply(EaseUtils.EnumEasingType.EXPO,
                    EaseUtils.EnumEasingOrder.FAST_AT_START_AND_END, pct);
            ScaledResolution scaledResolution=new ScaledResolution(Minecraft.getMinecraft());
            double scale = 1 - pct;
            GL11.glScaled(scale, scale, scale);
            GL11.glTranslated(((0 + (scaledResolution.getScaledWidth() * 0.5 * pct)) / scale) - 0,
                    ((0 + (scaledResolution.getScaledHeight() * 0.5d * pct)) / scale) - 0,
                    0);
            translated = true;
        }
        if(pct!=0){
            timer2.reset();
        }
        if (drag) {
            if (!Mouse.isButtonDown(0)) {
                drag = false;
            }
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        if (Mouse.hasWheel()) {
            scrollVelocity = Mouse.getDWheel();
            if(scrollVelocity!=0) timer1.reset();
        }
        mouseScroll(mouseX, mouseY, scrollVelocity);
        if(scroll<=-5 && !(mouseX > x - 2 && mouseX < x + 450 && mouseY > y - 2 && mouseY < y + 20)){
            scroll=-5;
            if(timer1.hasTimePassed(100) && scroll<=-5){
                scroll=0;
            }
        }
        if(scroll>=(LiquidBounce.scriptManager.getScripts().size()*30) - 230 && !(mouseX > x - 2 && mouseX < x + 450 && mouseY > y - 2 && mouseY < y + 20)){
            scroll=(LiquidBounce.scriptManager.getScripts().size()*30) - 230;
            if(timer1.hasTimePassed(100) && scroll>=(LiquidBounce.scriptManager.getScripts().size()*30)-230){
                scroll=(LiquidBounce.scriptManager.getScripts().size()*30) - 235;
            }
        }

        if(pct==0 && timer2.timePassed()!=0) BlurUtils.INSTANCE.draw(x,y,120,300,(float) timer2.timePassed()<=200f ? 30f*(timer2.timePassed()/200f) : 30f);
        SmoothRenderUtils.drawRect(x,y,x+120,y+300,new Color(31, 31, 31, (int) (180*(1-pct))).getRGB());
        SmoothRenderUtils.drawRoundRect(x+8,y+50,x+112,y+70,3f,new Color(0,186,231, 255).getRGB());
        SmoothRenderUtils.drawRect(x,y,x+120,y+41,new Color(0, 0, 0, 60).getRGB());
        SmoothRenderUtils.drawRoundRect(x+120,y,x+500,y+300,7f,new Color(24, 24, 24, 255).getRGB());
        SmoothRenderUtils.drawRect(x+120,y,x+140,y+300,new Color(24, 24, 24, 255).getRGB());
        SmoothRenderUtils.drawRoundRect(x+120,y,x+500,y+20,5f,new Color(0,186,231, 255).getRGB());
        SmoothRenderUtils.drawRect(x+120,y+15,x+500,y+20f,new Color(0,186,231, 255).getRGB());
        SmoothRenderUtils.drawRect(x+120,y,x+140,y+20f,new Color(0,186,231, 255).getRGB());
        CFontRenderer.DisplayFonts(FontLoaders.C16,"Local Scripts",x+126,y+6,new Color(255,255,255).getRGB());
        CFontRenderer.DisplayFonts(FontLoaders.F40,"FDPClient",x+10,y+10,new Color(255,255,255,245).getRGB());
        CFontRenderer.DisplayFonts(FontLoaders.C18,"Local Scripts",x+12,y+56,new Color(255,255,255,255).getRGB());
        CFontRenderer.DisplayFonts(FontLoaders.C18,"Online Scripts",x+12,y+80,new Color(255,255,255,255).getRGB());
        int i=0;
        GL11.glPushMatrix();
        GL11.glEnable(3089);
        RenderUtils.makeScissorBox(x+120,y+20,x+500,y+275);
        for(Script script:LiquidBounce.scriptManager.getScripts()){
            CFontRenderer.DisplayFonts(FontLoaders.C18,script.scriptName,x+134,y-scroll + 40 + (i * 30),script.getState() ? new Color(255,255,255).getRGB() : new Color(180,180,180).getRGB());
            StringBuilder authors= new StringBuilder();
            int length=0;
            for(String author:script.scriptAuthors){
                length++;
                if(length!=script.scriptAuthors.length) authors.append(author).append("/"); else{
                    authors.append(author);}
            }
            CFontRenderer.DisplayFonts(FontLoaders.C16,"§3Authors: §f"+authors+" §3Version: §f"+script.scriptVersion,x+134,y-scroll + 52 + (i * 30),new Color(255,255,255).getRGB());
            if(i+1!=LiquidBounce.scriptManager.getScripts().size()) SmoothRenderUtils.drawRect(x+130,y-scroll + 64.8 + (i * 30),x+490,y-scroll + 65 + (i * 30),new Color(211, 211, 211, 95).getRGB());
            if(i!=LiquidBounce.scriptManager.getScripts().size()) SmoothRenderUtils.drawRoundRect(x+450,y-scroll + 41 + (i * 30),x+490,y-scroll + 59 + (i * 30),3f,new Color(0,186,231, 255).getRGB());
            if(i!=LiquidBounce.scriptManager.getScripts().size()) CFontRenderer.DisplayFonts(FontLoaders.C16,script.getState() ? "Loaded" : "Unload",x+455 + (script.getState() ? -1 : 0),y-scroll + 46 + (i * 30),!script.getState() ? new Color(220,220,220,200).getRGB() : new Color(255,255,255,255).getRGB());
            if(i!=LiquidBounce.scriptManager.getScripts().size()) for (Module registeredModule : script.getRegisteredModules()) {
                SmoothRenderUtils.drawRoundRect(x+425,y-scroll + 41 + (i * 30),x+445,y-scroll + 59 + (i * 30),3f,new Color(0,186,231, 255).getRGB());
                CFontRenderer.DisplayFonts(FontLoaders.C16,registeredModule.getState() ? "On" : "Off",x+426 + (registeredModule.getState() ? 1 : 0),y-scroll + 46 + (i * 30),!registeredModule.getState() ? new Color(220,220,220,200).getRGB() : new Color(255,255,255,255).getRGB());
            }
            i++;
        }
        GL11.glDisable(3089);
        GL11.glPopMatrix();
        RenderUtils.drawGradientSidewaysV(x+120,y+20,x+500,y+30,new Color(24,24,24).getRGB(),new Color(24,24,24,0).getRGB());
        RenderUtils.drawGradientSidewaysV(x+120,y+265,x+500,y+275,new Color(24,24,24,0).getRGB(),new Color(24,24,24).getRGB());
        RenderUtils.drawRect(x+120,y+274f,x+500f,y + 274.4f,new Color(176, 176, 176, 89).getRGB());
        SmoothRenderUtils.drawRoundRect(x+450,y+280,x+490,y + 295,3f,new Color(0,186,231, 255).getRGB());
        CFontRenderer.DisplayFonts(FontLoaders.C16,"Reload",x+455,y+284,new Color(255,255,255,255).getRGB());
        SmoothRenderUtils.drawRoundRect(x+405,y+280,x+445,y + 295,3f,new Color(0,186,231, 255).getRGB());
        CFontRenderer.DisplayFonts(FontLoaders.C16,"Unload",x+410,y+284,new Color(255,255,255,255).getRGB());
        if (translated) {
            translated = false;
        }
    }
    public void mouseScroll(int mouseX, int mouseY, int amount) {
        if (mouseX > x - 120 && mouseX < x + 450 && mouseY > y + 30 && mouseY < y + 280) {
            scrollTo = (int) ((float) scrollTo - (amount / 120 * 28));
            scroll = scroll+(amount / 120 * -5);
        }


    }
}
