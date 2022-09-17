/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.modernui;

import net.ccbluex.liquidbounce.launch.data.modernui.clickgui.fonts.impl.Fonts;
import net.ccbluex.liquidbounce.launch.data.modernui.clickgui.fonts.logo.info;
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.utils.MainMenuButton;
import net.ccbluex.liquidbounce.utils.render.ParticleUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class GuiMainMenu extends GuiScreen {
    public ArrayList butt = new ArrayList();
    private float currentX;
    private float currentY;
    private ScaledResolution res;

    public void initGui() {
        this.butt.clear();
        this.butt.add(new MainMenuButton(this, 0, "G", "SinglePlayer", () -> {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }));
        this.butt.add(new MainMenuButton(this, 1, "H", "MultiPlayer", () -> {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }));
        this.butt.add(new MainMenuButton(this, 2, "I", "AltManager", () -> {
            this.mc.displayGuiScreen(new GuiAltManager(this));
        }));
        this.butt.add(new MainMenuButton(this, 3, "J", "Mods", () -> {
            this.mc.displayGuiScreen(new GuiModList(this));
        }, 0.5F));
        this.butt.add(new MainMenuButton(this, 4, "K", "Options", () -> {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }));
        this.butt.add(new MainMenuButton(this, 5, "L", "Languages", () -> {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }));
        this.butt.add(new MainMenuButton(this, 6, "M", "Quit", () -> {
            this.mc.shutdown();
        }));
        this.res = new ScaledResolution(this.mc);
        super.initGui();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawGradientRect(0, 0, this.width, this.height, 16777215, 16777215);
        int h = this.height;
        int w = this.width;
        float xDiff = ((float)(mouseX - h / 2) - this.currentX) / (float)this.res.getScaleFactor();
        float yDiff = ((float)(mouseY - w / 2) - this.currentY) / (float)this.res.getScaleFactor();
        this.currentX += xDiff * 0.3F;
        this.currentY += yDiff * 0.3F;
        GlStateManager.translate(this.currentX / 30.0F, this.currentY / 15.0F, 0.0F);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/background.png"), -30, -30, this.res.getScaledWidth() + 60, this.res.getScaledHeight() + 60);

        GlStateManager.translate(-this.currentX / 30.0F, -this.currentY / 15.0F, 0.0F);
        ParticleUtils.drawParticles(mouseX, mouseY);

        RenderUtils.drawRect((float)this.width / 2.0F - 50.0F * ((float)this.butt.size() / 2.0F), (float)this.height / 2.0F - 50.0F, (float)this.width / 2.0F + 50.0F * ((float)this.butt.size() / 2.0F), (float)this.height / 2.0F + 50.0F, 2097152000);
        RenderUtils.drawRect((float)this.width / 2.0F - 50.0F * ((float)this.butt.size() / 2.0F), (float)this.height / 2.0F + 20.0F, (float)this.width / 2.0F + 50.0F * ((float)this.butt.size() / 2.0F), (float)this.height / 2.0F + 50.0F, 1040187392);
        float startX = (float)this.width / 2.0F - 50.0F * ((float)this.butt.size() / 2.0F);

        for(Iterator var9 = this.butt.iterator(); var9.hasNext(); startX += 50.0F) {
            MainMenuButton button = (MainMenuButton)var9.next();
            button.draw(startX, (float)this.height / 2.0F + 20.0F, mouseX, mouseY);
        }

        Fonts.SF.SF_35.SF_35.drawString("FDPCLIENT", (float)this.width / 2.0F - 50.0F * ((float)this.butt.size() / 2.0F) + 80.0F, (float)this.height / 2.0F - 30.0F, -1);
       Fonts.SF.SF_35.SF_35.drawString(info.version, (float)this.width / 2.0F - 50.0F * ((float)this.butt.size() / 2.0F) + 80.0F, (float)this.height / 2.0F - 10.0F, -1);
        String s = "v4.7.0";
        Fonts.SF.SF_35.SF_35.drawString(s, (float)this.width / 2.0F + 50.0F * ((float)this.butt.size() / 2.0F) - (float) Fonts.SF.SF_35.SF_35.stringWidth(s) - 10.0F, (float)this.height / 2.0F + 5.0F, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        Iterator var4 = this.butt.iterator();

        while(var4.hasNext()) {
            MainMenuButton button = (MainMenuButton)var4.next();
            button.mouseClick(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void updateScreen() {
        this.res = new ScaledResolution(this.mc);
        super.updateScreen();
    }
}
