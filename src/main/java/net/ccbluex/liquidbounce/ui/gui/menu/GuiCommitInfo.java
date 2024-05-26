/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.gui.menu;

import me.zywl.fdpclient.FDPClient;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.GitUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class GuiCommitInfo extends GuiScreen {
    public static final ResourceLocation gitImage = new ResourceLocation("fdpclient/gui/icons/git.png");

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(0, 70, 30 + Fonts.minecraftFont.FONT_HEIGHT * 7 + 20, "Back"));
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        RenderUtils.drawImage(gitImage, 30, 30, 30, 30);
        String buildUser = GitUtils.gitInfo.getProperty("git.build.user.name");
        String version = GitUtils.gitInfo.getProperty("git.build.version");
        String commitId = GitUtils.gitInfo.getProperty("git.commit.id");
        String commitIdAbbrev = GitUtils.gitInfo.getProperty("git.commit.id.abbrev");
        String commitMessage = GitUtils.gitInfo.getProperty("git.commit.message.short");
        String branch = GitUtils.gitInfo.getProperty("git.branch");
        String repo = GitUtils.gitInfo.getProperty("git.remote.origin.url");
        this.drawString(Fonts.minecraftFont, "Git Info", 70 ,30, new Color(255, 255, 255).getRGB());
        this.drawString(Fonts.minecraftFont, FDPClient.CLIENT_NAME + " built by " + buildUser, 70, 30 + Fonts.minecraftFont.FONT_HEIGHT, new Color(255, 255, 255).getRGB());
        this.drawString(Fonts.minecraftFont, "Version: " + version, 70, 30 + Fonts.minecraftFont.FONT_HEIGHT * 2 + 5, new Color(255, 255, 255).getRGB());
        this.drawString(Fonts.minecraftFont, "CommitId: " + commitId + " (" + commitIdAbbrev + ")", 70, 30 + Fonts.minecraftFont.FONT_HEIGHT * 3 + 5, new Color(255, 255, 255).getRGB());
        this.drawString(Fonts.minecraftFont, "CommitMessage: " + commitMessage, 70, 30 + Fonts.minecraftFont.FONT_HEIGHT * 4 + 5, new Color(255, 255, 255).getRGB());
        this.drawString(Fonts.minecraftFont, "Branch: " + branch, 70, 30 + Fonts.minecraftFont.FONT_HEIGHT * 5 + 5, new Color(255, 255, 255).getRGB());
        this.drawString(Fonts.minecraftFont, "Remote origin: " + repo, 70, 30 + Fonts.minecraftFont.FONT_HEIGHT * 6 + 5, new Color(255, 255, 255).getRGB());
        this.drawString(Fonts.minecraftFont, "Developers: " + String.join(" ", FDPClient.CLIENT_DEV), 70, 30 + Fonts.minecraftFont.FONT_HEIGHT * 7 + 5, new Color(255, 255, 255).getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(null);
        }
    }
}
