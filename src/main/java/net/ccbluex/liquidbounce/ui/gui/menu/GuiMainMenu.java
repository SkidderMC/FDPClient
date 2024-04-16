/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.gui.menu;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager;
import net.ccbluex.liquidbounce.ui.gui.menu.elements.*;
import net.ccbluex.liquidbounce.ui.gui.menu.elements.GuiButton;
import net.ccbluex.liquidbounce.ui.font.cf.FontLoaders;
import net.ccbluex.liquidbounce.ui.gui.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.utils.GitUtils;
import net.ccbluex.liquidbounce.utils.misc.MiscUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.awt.*;

public class GuiMainMenu extends GuiScreen implements  GuiYesNoCallback {
    private ResourceLocation logo;
    private GuiButton btnSinglePlayer;
    private GuiButton btnMultiplayer;
    private GuiButton btnCommitInfo;
    private ImageButton btnClientOptions;
    private ImageButton btnCosmetics;
    private ImageButton btnMinecraftOptions;
    private ImageButton btnLanguage;
    private ImageButton btnForgeModList;
    private GuiButton btnWebsite;
    private GuiButton btnDiscord;
    private QuitButton btnQuit;

    private static int panoramaTimer;
    private ImageButton btnAddAccount;
    GuiCapeManager guiCapeManager = GuiCapeManager.INSTANCE;

    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{
            new ResourceLocation("fdpclient/gui/panorama/panorama_0.png"),
            new ResourceLocation("fdpclient/gui/panorama/panorama_1.png"),
            new ResourceLocation("fdpclient/gui/panorama/panorama_2.png"),
            new ResourceLocation("fdpclient/gui/panorama/panorama_3.png"),
            new ResourceLocation("fdpclient/gui/panorama/panorama_4.png"),
            new ResourceLocation("fdpclient/gui/panorama/panorama_5.png")
    };

    @Override
    public void initGui() {
        this.logo = new ResourceLocation("fdpclient/gui/design/logo.png");

        this.btnSinglePlayer = new GuiButton("S I N G L E P L A Y E R", this.width / 2 - 66, this.height / 2);
        this.btnMultiplayer = new GuiButton("M U L T I P L A Y E R", this.width / 2 - 66, this.height / 2 + 16);
        this.btnCommitInfo = new GuiButton("G I T C O M M I T", this.width / 2 - 66, this.height / 2 + 36);

        int yPos = this.height - 20;

        this.btnClientOptions = new ImageButton("SETTINGS", new ResourceLocation("fdpclient/gui/design/logo.png"), this.width / 2 - 30, yPos);
        this.btnCosmetics = new ImageButton("COSMETICS", new ResourceLocation("fdpclient/gui/icons/cosmetics.png"), this.width / 2 - 15, yPos);
        this.btnMinecraftOptions = new ImageButton("MINECRAFT SETTINGS", new ResourceLocation("fdpclient/gui/icons/cog.png"), this.width / 2, yPos);
        this.btnLanguage = new ImageButton("LANGUAGE", new ResourceLocation("fdpclient/gui/icons/globe.png"), this.width / 2 + 15, yPos);
        this.btnForgeModList = new ImageButton("FORGE MODS", new ResourceLocation("fdpclient/gui/icons/forge.png"), this.width / 2 + 30, yPos);

        this.btnAddAccount = new ImageButton("LOGIN", new ResourceLocation("fdpclient/gui/icons/add-account.png"), 10, 10);
        this.btnDiscord = new ImageButton("DISCORD", new ResourceLocation("fdpclient/gui/icons/discord.png"), 20, 5);
        this.btnWebsite = new ImageButton("WEBSITE", new ResourceLocation("fdpclient/gui/icons/lighting.png"), 25, 5);

        this.btnQuit = new QuitButton(this.width - 17, 7);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (this.btnSinglePlayer.hoverFade > 0) {
            mc.displayGuiScreen(new GuiSelectWorld(this));
        }
        if (this.btnMultiplayer.hoverFade > 0) {
            mc.displayGuiScreen(new GuiMultiplayer(this));
        }
        if (this.btnQuit.hoverFade > 0) {
            mc.shutdown();
        }
        if (this.btnMinecraftOptions.hoverFade > 0) {
            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        }
        if (this.btnLanguage.hoverFade > 0) {
            mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }
        if (this.btnClientOptions.hoverFade > 0) {
            mc.displayGuiScreen(new GuiInfo(this));
        }
        if (this.btnForgeModList.hoverFade > 0) {
            mc.displayGuiScreen(new GuiModList(mc.currentScreen));
        }
        if (this.btnCommitInfo.hoverFade > 0) {
            mc.displayGuiScreen(new GuiCommitInfo());
        }

        if (this.btnCosmetics.hoverFade > 0) {
            mc.displayGuiScreen(guiCapeManager);
        }
        if (this.btnAddAccount.hoverFade > 0) {
            mc.displayGuiScreen(new GuiAltManager(this));
        }

        if (this.btnDiscord.hoverFade > 0) {
            MiscUtils.INSTANCE.showURL(FDPClient.CLIENT_DISCORD);
        }

        if (this.btnWebsite.hoverFade > 0) {
            MiscUtils.INSTANCE.showURL(FDPClient.CLIENT_WEBSITE);
        }

    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableAlpha();
        this.renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(logo);
        Gui.drawModalRectWithCustomSizedTexture(this.width / 2 - 25, this.height / 2 - 68, 0, 0, 49, 49, 49, 49);
        FontLoaders.R15.drawCenteredString("by SkidderMC with love ", (float) this.width / 2 - 0.25F, (double) this.height / 2 - 18, new Color(30, 30, 30, 70).getRGB());
        FontLoaders.R15.drawCenteredString("by SkidderMC with love ", (float) this.width / 2, (float) this.height / 2 - 19, -1);
        this.btnSinglePlayer.drawButton(mouseX, mouseY);
        this.btnMultiplayer.drawButton(mouseX, mouseY);
        this.btnCommitInfo.drawButton(mouseX, mouseY);
        this.btnClientOptions.drawButton(mouseX, mouseY);
        this.btnCosmetics.drawButton(mouseX, mouseY);
        this.btnMinecraftOptions.drawButton(mouseX, mouseY);
        this.btnLanguage.drawButton(mouseX, mouseY);
        this.btnForgeModList.drawButton(mouseX, mouseY);
        this.btnAddAccount.drawButton(mouseX, mouseY);
        this.btnQuit.drawButton(mouseX, mouseY);
        String s = FDPClient.CLIENT_VERSION;
        FontLoaders.R15.drawString(FDPClient.CLIENT_NAME + "(" + GitUtils.gitBranch + "/" + GitUtils.gitInfo.getProperty("git.commit.id.abbrev") + ") | Minecraft 1.8.9", 7, this.height - 11, new Color(255, 255, 255, 100).getRGB());
        FontLoaders.R15.drawString(s, this.width - FontLoaders.R15.getStringWidth(s) - 6, this.height - 11, new Color(255, 255, 255, 100).getRGB());
    }

    public void updateScreen() {
        ++panoramaTimer;
        super.updateScreen();
    }

    private void drawPanorama(int p_73970_1_, int p_73970_2_, float p_73970_3_) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int i = 8;

        for (int j = 0; j < i * i; ++j) {
            GlStateManager.pushMatrix();
            float f = ((float) (j % i) / (float) i - 0.5F) / 64.0F;
            float f1 = ((float) (j / i) / (float) i - 0.5F) / 64.0F;
            float f2 = 0.0F;
            GlStateManager.translate(f, f1, f2);
            GlStateManager.rotate(MathHelper.sin(((float) panoramaTimer + p_73970_3_) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-((float) panoramaTimer + p_73970_3_) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int k = 0; k < 6; ++k) {
                GlStateManager.pushMatrix();

                if (k == 1) {
                    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (k == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                if (k == 3) {
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (k == 4) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (k == 5) {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                this.mc.getTextureManager().bindTexture(titlePanoramaPaths[k]);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int l = 255 / (j + 1);
                float f3 = 0.0F;
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private void rotateAndBlurSkybox(float p_73968_1_) {
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();
        int i = 3;

        for (int j = 0; j < i; ++j) {
            float f = 1.0F / (float) (j + 1);
            int k = this.width;
            int l = this.height;
            float f1 = (float) (j - i / 2) / 256.0F;
            worldrenderer.pos(k, l, this.zLevel).tex(0.0F + f1, 1.0).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(k, 0.0, this.zLevel).tex(1.0F + f1, 1.0).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0, 0.0, this.zLevel).tex(1.0F + f1, 0.0).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0, l, this.zLevel).tex(0.0F + f1, 0.0).color(1.0F, 1.0F, 1.0F, f).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    public void renderSkybox(int p_73971_1_, int p_73971_2_, float p_73971_3_) {
        this.mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        this.drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        float f = this.width > this.height ? 120.0F / (float) this.width : 120.0F / (float) this.height;
        float f1 = (float) this.height * f / 256.0F;
        float f2 = (float) this.width * f / 256.0F;
        int i = this.width;
        int j = this.height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0, j, this.zLevel).tex(0.5F - f1, 0.5F + f2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(i, j, this.zLevel).tex(0.5F - f1, 0.5F - f2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(i, 0.0, this.zLevel).tex(0.5F + f1, 0.5F - f2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(0.0, 0.0, this.zLevel).tex(0.5F + f1, 0.5F + f2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
    }
}
