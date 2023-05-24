/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.mainmenu;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.utils.MainMenuButton;
//import net.ccbluex.liquidbounce.utils.render.BlurUtils;
import net.ccbluex.liquidbounce.utils.render.ParticleUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
public class ModernGuiMainMenu extends GuiScreen {
    public final ArrayList butt = new ArrayList();
    private float currentX;
    private float currentY;
    private ScaledResolution res;

    public void initGui() {
        this.butt.clear();
        this.butt.add(new MainMenuButton("G", "SinglePlayer", () -> this.mc.displayGuiScreen(new GuiSelectWorld(this))));
        this.butt.add(new MainMenuButton("H", "MultiPlayer", () -> this.mc.displayGuiScreen(new GuiMultiplayer(this))));
        this.butt.add(new MainMenuButton("I", "AltManager", () -> this.mc.displayGuiScreen(new GuiAltManager(this))));
        this.butt.add(new MainMenuButton("J", "Mods", () -> this.mc.displayGuiScreen(new GuiModList(this)), 0.5F));
        this.butt.add(new MainMenuButton("K", "Options", () -> this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings))));
        this.butt.add(new MainMenuButton("L", "Languages", () -> this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()))));
        this.butt.add(new MainMenuButton("M", "Quit", () -> this.mc.shutdown()));
        this.res = new ScaledResolution(this.mc);
        super.initGui();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        try {
            this.drawGradientRect(0, 0, this.width, this.height, 16777215, 16777215);
            int h = this.height;
            int w = this.width;
            float xDiff = ((float) (mouseX - h / 2) - this.currentX) / (float) this.res.getScaleFactor();
            float yDiff = ((float) (mouseY - w / 2) - this.currentY) / (float) this.res.getScaleFactor();
            this.currentX += xDiff * 0.3F;
            this.currentY += yDiff * 0.3F;
            GlStateManager.translate(this.currentX / 30.0F, this.currentY / 15.0F, 0.0F);
            RenderUtils.drawImage(new ResourceLocation("fdpclient/background.png"), -30, -30, this.res.getScaledWidth() + 60, this.res.getScaledHeight() + 60);
            GlStateManager.translate(-this.currentX / 30.0F, -this.currentY / 15.0F, 0.0F);
            RenderUtils.drawRoundedCornerRect((float) this.width / 2.0F - 80.0F * ((float) this.butt.size() / 2.0F) - 3f, (float) this.height / 2.0F - 100.0F - 3f, (float) this.width / 2.0F + 80.0F * ((float) this.butt.size() / 2.0F) + 3f, (float) this.height / 2.0F + 103.0F, 10, new Color(0, 0, 0, 80).getRGB());
            FontLoaders.F18.drawCenteredString("Made by SkidderMC with love.",(float)this.width / 2.0F,(float)this.height / 2.0F + 70.0F,new Color(255,255,255,255).getRGB());
            //BlurUtils.INSTANCE.draw(0, 0, mc.displayWidth, mc.displayHeight, 30f);
            FontLoaders.F40.drawCenteredString("FDPCLIENT",(float)this.width / 2.0F,(float)this.height / 2.0F - 70.0F,new Color(255,255,255).getRGB());
            //BlurUtils.INSTANCE.draw(0, 0, mc.displayWidth, mc.displayHeight, 10f);
            ParticleUtils.drawParticles(mouseX, mouseY);
            RenderUtils.drawRoundedCornerRect((float) this.width / 2.0F - 80.0F * ((float) this.butt.size() / 2.0F), (float) this.height / 2.0F - 100.0F, (float) this.width / 2.0F + 80.0F * ((float) this.butt.size() / 2.0F), (float) this.height / 2.0F + 100.0F, 10, new Color(0, 0, 0, 100).getRGB());
            //RenderUtils.drawRect((float)this.width / 2.0F - 50.0F * ((float)this.butt.size() / 2.0F), (float)this.height / 2.0F + 20.0F, (float)this.width / 2.0F + 50.0F * ((float)this.butt.size() / 2.0F), (float)this.height / 2.0F + 50.0F, 1040187392);
            float startX = (float) this.width / 2.0F - 64.5F * ((float) this.butt.size() / 2.0F);

            for (Iterator var9 = this.butt.iterator(); var9.hasNext(); startX += 75.0F) {
                MainMenuButton button = (MainMenuButton) var9.next();
                button.draw(startX, (float) this.height / 2.0F + 20.0F, mouseX, mouseY);
            }
            FontLoaders.F40.drawCenteredString("FDPCLIENT",(float)this.width / 2.0F,(float)this.height / 2.0F - 70.0F,new Color(255,255,255).getRGB());
            FontLoaders.F18.drawCenteredString(FDPClient.CLIENT_VERSION,(float)this.width / 2.0F,(float)this.height / 2.0F - 30.0F,new Color(255,255,255).getRGB());
            RenderUtils.drawRect((float)this.width / 2.0F - 30f,(float)this.height / 2.0F - 40.0F,(float)this.width / 2.0F + 30f,(float)this.height / 2.0F - 39.5F,new Color(255,255,255,100).getRGB());
            FontLoaders.F18.drawCenteredString("Made by SkidderMC with love.",(float)this.width / 2.0F,(float)this.height / 2.0F + 70.0F,new Color(255,255,255,100).getRGB());

        }catch (Exception e){
            e.printStackTrace();
        }
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
