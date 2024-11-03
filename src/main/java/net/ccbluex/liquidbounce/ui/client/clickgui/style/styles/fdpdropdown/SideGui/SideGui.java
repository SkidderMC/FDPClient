/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule;
import net.ccbluex.liquidbounce.handler.api.AutoSettings;
import net.ccbluex.liquidbounce.handler.api.ClientApi;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.TimerUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.Drag;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils;
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.Fonts;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.SettingsUtils;
import net.ccbluex.liquidbounce.utils.extensions.MathExtensionsKt;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

import static net.ccbluex.liquidbounce.handler.api.ClientSettingsKt.getAutoSettingsList;

public class SideGui extends GuiPanel {

    private final String[] categories = {"UI", "Configs", "Design"};
    public boolean focused;
    public Animation clickAnimation;
    private Animation hoverAnimation;
    private Animation textAnimation;
    private Animation moveOverGradientAnimation;
    private HashMap<String, Animation[]> categoryAnimation = new HashMap<>();
    private Drag drag;
    private String currentCategory = "UI";

    private TimerUtil timerUtil;
    private boolean showLocalConfigs = false;
    private boolean wasMousePressed = false;

    @Override
    public void initGui() {
        focused = false;
        timerUtil = new TimerUtil();
        rectWidth = 550;
        rectHeight = 350;
        ScaledResolution sr = new ScaledResolution(mc);
        drag = new Drag(sr.getScaledWidth() - 30, sr.getScaledHeight() / 2f - rectHeight / 2f);
        textAnimation = new DecelerateAnimation(500, 1);
        textAnimation.setDirection(Direction.BACKWARDS);
        clickAnimation = new DecelerateAnimation(325, 1);
        clickAnimation.setDirection(Direction.BACKWARDS);
        categoryAnimation = new HashMap<>();
        for (String category : categories) {
            categoryAnimation.put(category, new Animation[]{new DecelerateAnimation(250, 1), new DecelerateAnimation(250, 1)});
        }

        moveOverGradientAnimation = new DecelerateAnimation(250, 1);
        moveOverGradientAnimation.setDirection(Direction.BACKWARDS);

        hoverAnimation = new DecelerateAnimation(250, 1);
        hoverAnimation.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks, int alpha) {

        clickAnimation.setDirection(focused ? Direction.FORWARDS : Direction.BACKWARDS);
        boolean hovering = DrRenderUtils.isHovering(drag.getX(), drag.getY(), rectWidth, rectHeight, mouseX, mouseY);
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
        ScaledResolution sr = new ScaledResolution(mc);

        boolean setDirection = !focused && (!timerUtil.hasTimeElapsed(6000) || (!hoverAnimation.isDone() || hoverAnimation.isDone() && hoverAnimation.getDirection().equals(Direction.FORWARDS)));
        textAnimation.setDirection(setDirection ? Direction.FORWARDS : Direction.BACKWARDS);

        if (textAnimation.isDone()) {
            if (textAnimation.getDirection().equals(Direction.FORWARDS)) {
                textAnimation.isDone();
            }
        }

        if (!clickAnimation.isDone()) {
            drag.setX(MathExtensionsKt.interpolateFloat(sr.getScaledWidth() - 30, focused ? sr.getScaledWidth() / 2f - rectWidth / 2f : drag.getX(), (float) clickAnimation.getOutput()));
            drag.setY(MathExtensionsKt.interpolateFloat(sr.getScaledHeight() / 2f - rectHeight / 2f, drag.getY(), (float) clickAnimation.getOutput()));
        }

        boolean gradient = drag.getX() + rectWidth > sr.getScaledWidth() && focused && (clickAnimation.isDone() && clickAnimation.getDirection().equals(Direction.FORWARDS));
        moveOverGradientAnimation.setDirection(gradient ? Direction.FORWARDS : Direction.BACKWARDS);


        float rectAlpha = (float) Math.min((float) ((185 + (30 * hoverAnimation.getOutput()) + (70 * clickAnimation.getOutput()))) - (70 * moveOverGradientAnimation.getOutput()), 255);
        rectAlpha *= alpha / 255f;

        Color mainRectColor = new Color(30, 30, 30, (int) rectAlpha);

        if (focused) {
            drag.onDraw(mouseX, mouseY);
        }

        float x = drag.getX(), y = drag.getY();
        RenderUtils.drawCustomShapeWithRadius(x, y, rectWidth, rectHeight, 9, mainRectColor);
        if (!focused) return;
        int textColor = DrRenderUtils.applyOpacity(-1, alpha / 255f);
        int seperation = 0;
        for (String category : categories) {
            float xVal = x + rectWidth / 2f - 50 + seperation;
            float yVal = y + 15;

            boolean hovered = DrRenderUtils.isHovering(xVal - 30, yVal - 5, 60, Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.getHeight() + 10, mouseX, mouseY);
            Animation hoverAnimation = categoryAnimation.get(category)[0];
            Animation enableAnimation = categoryAnimation.get(category)[1];

            hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
            enableAnimation.setDirection(currentCategory.equals(category) ? Direction.FORWARDS : Direction.BACKWARDS);

            int index = 0;
            Color color22 = new Color(ClickGUIModule.generateColor(index).getRGB());
            Color categoryColor = new Color(45, 45, 45, alpha);
            Color hoverColor = DrRenderUtils.interpolateColorC(categoryColor, DrRenderUtils.brighter(categoryColor, .8f), (float) hoverAnimation.getOutput());
            Color finalColor = DrRenderUtils.interpolateColorC(hoverColor, DrRenderUtils.applyOpacity(color22, alpha / 255f), (float) enableAnimation.getOutput());

            RenderUtils.drawCustomShapeWithRadius(xVal - 30, yVal - 5, 60, Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.getHeight() + 10, 6, finalColor);

            DrRenderUtils.resetColor();
            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawCenteredString(category, xVal, y + 15, textColor);
            seperation += 100;
        }

        DrRenderUtils.drawRect2(x + 20, y + 50, rectWidth - 40, 1, new Color(45, 45, 45, alpha).getRGB());

        if (currentCategory.equals("Design")) {

            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("Not Finished - SOOOOOOOOON", x + rectWidth / 2, y + rectHeight / 2, DrRenderUtils.applyOpacity(-1, alpha / 255f));

          /*  // Text
            text = C.INSTANCE.getTextValue().get();
            RenderUtils.drawRoundedRect(25F, 350.0f, 40F, 365.0f, 5F, textColor);
            RenderUtils.drawRoundedOutline(25F, 350.0f, 40F, 365.0f, 7F, 1F, Color.WHITE.getRGB());
            FontLoaders.C12.drawStringWithShadow("Text White Color", 43.0, 351.5, Color.WHITE.getRGB());
            FontLoaders.C12.drawStringWithShadow("Fade Side : " + ClientTheme.INSTANCE.getUpdown().get(), 25.0, 376.5, Color.WHITE.getRGB());
            FontLoaders.C12.drawStringWithShadow("FadeSpeed : " + ClientTheme.INSTANCE.getFadespeed().get(), 25.0, 401.5, Color.WHITE.getRGB());
            GlStateManager.resetColor();

            int wheel = Mouse.getDWheel();
            if (wheel != 0) {
                if (wheel > 0) {
                    scroll += 15f;
                } else {
                    scroll -= 15f;
                }
            }
            if (scroll < -100F) {
                scroll = -100F;
            }
            if (scroll > 0F) {
                scroll = 0F;
            }

            RenderUtils.drawRoundedOutline(
                    x + 22f,
                    y + 163.0f + scroll,
                    x + 126.4f,
                    y + 237.0f + scroll,
                    23.5F,
                    4F,
                    new Color(255, 255, 255).getRGB()
            );
            RenderUtils.drawRoundedGradientRectCorner(
                    x + 24F,
                    y + 164.5F + scroll,
                    x + 124.5F,
                    y + 235F + scroll,
                    20F,
                    ClientTheme.INSTANCE.getColorFromName("Tree", 0).getRGB(),
                    ClientTheme.INSTANCE.getColorFromName("Tree", 90).getRGB(),
                    ClientTheme.INSTANCE.getColorFromName("Tree", 180).getRGB(),
                    ClientTheme.INSTANCE.getColorFromName("Tree", 270).getRGB()
            );
            FontLoaders.F18.drawStringWithShadow("Tree", x + 60.0, y + 240.0 + scroll, ClientTheme.INSTANCE.getColorFromName("Tree", 1).getRGB());*/

        }

        if (currentCategory.equals("UI")) {

            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("Not Finished - SOOOOOOOOON", x + rectWidth / 2, y + rectHeight / 2, DrRenderUtils.applyOpacity(-1, alpha / 255f));
        }

        if (currentCategory.equals("Configs")) {
            // Button dimensions
            float buttonToggleWidth = 70; // Width of toggle buttons
            float buttonToggleHeight = 20; // Height of toggle buttons
            float buttonSpacing = 10; // Spacing between buttons

            // Button positions (aligned to the left side)
            float onlineButtonX = x + 25; // "ONLINE" button position
            float localButtonX = onlineButtonX + buttonToggleWidth + buttonSpacing; // "LOCAL" button position
            float openFolderButtonX = localButtonX + buttonToggleWidth + buttonSpacing; // "OPEN FOLDER" button position

            // "ONLINE" Button
            boolean isOnlineHovered = DrRenderUtils.isHovering(onlineButtonX, y + 30, buttonToggleWidth, buttonToggleHeight, mouseX, mouseY);
            int onlineButtonColor = !showLocalConfigs ? new Color(100, 150, 100, (int) alpha).getRGB() :
                    (isOnlineHovered ? new Color(70, 70, 70, (int) alpha).getRGB() : new Color(50, 50, 50, (int) alpha).getRGB());
            DrRenderUtils.drawRect2(onlineButtonX, y + 30, buttonToggleWidth, buttonToggleHeight, onlineButtonColor);
            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("ONLINE", onlineButtonX + 10, y + 35, DrRenderUtils.applyOpacity(-1, alpha / 255f));

            // "LOCAL" Button
            boolean isLocalHovered = DrRenderUtils.isHovering(localButtonX, y + 30, buttonToggleWidth, buttonToggleHeight, mouseX, mouseY);
            int localButtonColor = showLocalConfigs ? new Color(100, 150, 100, (int) alpha).getRGB() :
                    (isLocalHovered ? new Color(70, 70, 70, (int) alpha).getRGB() : new Color(50, 50, 50, (int) alpha).getRGB());
            DrRenderUtils.drawRect2(localButtonX, y + 30, buttonToggleWidth, buttonToggleHeight, localButtonColor);
            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("LOCAL", localButtonX + 10, y + 35, DrRenderUtils.applyOpacity(-1, alpha / 255f));

            // "OPEN FOLDER" Button
            boolean isOpenFolderHovered = DrRenderUtils.isHovering(openFolderButtonX, y + 30, buttonToggleWidth * 2, buttonToggleHeight, mouseX, mouseY);
            int openFolderButtonColor = isOpenFolderHovered ? new Color(70, 70, 70, (int) alpha).getRGB() : new Color(50, 50, 50, (int) alpha).getRGB();
            DrRenderUtils.drawRect2(openFolderButtonX, y + 30, buttonToggleWidth * 2, buttonToggleHeight, openFolderButtonColor);
            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("OPEN FOLDER", openFolderButtonX + 10, y + 35, DrRenderUtils.applyOpacity(-1, alpha / 255f));

            // Check if one of the buttons was clicked (only once)
            if (!wasMousePressed && Mouse.isButtonDown(0)) {
                if (isOnlineHovered) {
                    showLocalConfigs = false;
                } else if (isLocalHovered) {
                    showLocalConfigs = true;
                } else if (isOpenFolderHovered) {
                    try {
                        Desktop.getDesktop().open(FDPClient.INSTANCE.getFileManager().getSettingsDir());
                        ClientUtils.INSTANCE.displayChatMessage("Opening configuration folder...");
                    } catch (IOException e) {
                        ClientUtils.INSTANCE.displayChatMessage("Error opening folder: " + e.getMessage());
                    }
                }
                wasMousePressed = true; // Mark that the mouse button was pressed
            }

            // Reset the variable when the mouse button is released
            if (!Mouse.isButtonDown(0)) {
                wasMousePressed = false;
            }

            // Initial position for drawing configurations
            float configX = x + 25;
            float configY = y + 60;
            float buttonWidth = (rectWidth - 50) / 4 - 10;
            float buttonHeight = 20;
            int configsPerRow = 4;
            int configCount = 0;

            if (showLocalConfigs) {
                // Display local configurations
                File[] localConfigs = FDPClient.INSTANCE.getFileManager().getSettingsDir().listFiles((dir, name) -> name.endsWith(".txt"));
                if (localConfigs != null && localConfigs.length > 0) {
                    for (File file : localConfigs) {
                        String configName = file.getName().replace(".txt", "");

                        boolean isHovered = DrRenderUtils.isHovering(configX, configY, buttonWidth, buttonHeight, mouseX, mouseY);
                        int buttonColor = isHovered ? new Color(70, 70, 70, (int) alpha).getRGB() : new Color(50, 50, 50, (int) alpha).getRGB();
                        DrRenderUtils.drawRect2(configX, configY, buttonWidth, buttonHeight, buttonColor);
                        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(configName, configX + 5, configY + 5, DrRenderUtils.applyOpacity(-1, alpha / 255f));

                        if (isHovered && Mouse.isButtonDown(0)) {
                            try {
                                ClientUtils.INSTANCE.displayChatMessage("Loading local configuration: " + configName + "...");
                                String localConfigContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                                SettingsUtils.INSTANCE.applyScript(localConfigContent);
                                ClientUtils.INSTANCE.displayChatMessage("Local configuration " + configName + " loaded successfully!");
                            } catch (IOException e) {
                                ClientUtils.INSTANCE.displayChatMessage("Error loading local configuration: " + e.getMessage());
                            }
                        }

                        configX += buttonWidth + 10;
                        configCount++;
                        if (configCount % configsPerRow == 0) {
                            configX = x + 25;
                            configY += buttonHeight + 5;
                        }
                    }
                } else {
                    Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("No local configurations available.", configX, configY, DrRenderUtils.applyOpacity(-1, alpha / 255f));
                }
            } else {
                // Display online configurations from the API
                if (getAutoSettingsList() != null && getAutoSettingsList().length > 0) {
                    for (AutoSettings setting : getAutoSettingsList()) {
                        boolean isHovered = DrRenderUtils.isHovering(configX, configY, buttonWidth, buttonHeight, mouseX, mouseY);
                        int buttonColor = isHovered ? new Color(70, 70, 70, (int) alpha).getRGB() : new Color(50, 50, 50, (int) alpha).getRGB();
                        DrRenderUtils.drawRect2(configX, configY, buttonWidth, buttonHeight, buttonColor);
                        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(setting.getName(), configX + 5, configY + 5, DrRenderUtils.applyOpacity(-1, alpha / 255f));

                        if (isHovered && Mouse.isButtonDown(0)) {
                            try {
                                ClientUtils.INSTANCE.displayChatMessage("Loading configuration: " + setting.getName() + "...");
                                String configScript = ClientApi.INSTANCE.requestSettingsScript(setting.getSettingId(), "legacy");
                                SettingsUtils.INSTANCE.applyScript(configScript);
                                ClientUtils.INSTANCE.displayChatMessage("Configuration " + setting.getName() + " loaded successfully!");
                            } catch (Exception e) {
                                ClientUtils.INSTANCE.displayChatMessage("Error loading configuration: " + e.getMessage());
                            }
                        }

                        configX += buttonWidth + 10;
                        configCount++;
                        if (configCount % configsPerRow == 0) {
                            configX = x + 25;
                            configY += buttonHeight + 5;
                        }
                    }
                } else {
                    Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("No online configurations available.", configX, configY, DrRenderUtils.applyOpacity(-1, alpha / 255f));
                }
            }
        }

        DrRenderUtils.setAlphaLimit(0);
        DrRenderUtils.drawGradientRect2(x + 20, y + 51, rectWidth - 40, 8, new Color(0, 0, 0, (int) (60 * (alpha / 255f))).getRGB(), new Color(0, 0, 0, 0).getRGB());

        DrRenderUtils.setAlphaLimit(0);
        int index = 0;
        DrRenderUtils.drawGradientRectSideways2(sr.getScaledWidth() - 40, 0, 40, sr.getScaledHeight(),
                DrRenderUtils.applyOpacity(ClickGUIModule.generateColor(index).getRGB(), 0),
                DrRenderUtils.applyOpacity(ClickGUIModule.generateColor(index).getRGB(), (float) (.4 * moveOverGradientAnimation.getOutput())));

        DrRenderUtils.setAlphaLimit(1);

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovering = DrRenderUtils.isHovering(drag.getX(), drag.getY(), rectWidth, rectHeight, mouseX, mouseY);
        if (hovering && button == 0 && !focused) {
            focused = true;
            return;
        }

        if (focused) {
            boolean canDrag = DrRenderUtils.isHovering(drag.getX(), drag.getY(), rectWidth, 50, mouseX, mouseY)
                    || DrRenderUtils.isHovering(drag.getX(), drag.getY(), 20, rectHeight, mouseX, mouseY);
            drag.onClick(mouseX, mouseY, button, canDrag);

            float x = drag.getX(), y = drag.getY();
            int seperation = 0;
            for (String category : categories) {
                float xVal = x + rectWidth / 2f - 50 + seperation;
                float yVal = y + 15;

                boolean hovered = DrRenderUtils.isHovering(xVal - 30, yVal - 5, 60, Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.getHeight() + 10, mouseX, mouseY);

                if (hovered) {
                    currentCategory = category;
                    return;
                }
                seperation += 100;
            }
        }

        /*
        if (currentCategory.equals("Design")) {
            if (mouseWithinBounds(mouseX, mouseY, 25, 70 + animScroll, 122, 140 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Cherry");
            }

            if (mouseWithinBounds(mouseX, mouseY, 150, 70 + animScroll, 247, 140 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Water");
            }

            if (mouseWithinBounds(mouseX, mouseY, 275, 70 + animScroll, 372, 140 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Magic");
            }

            if (mouseWithinBounds(mouseX, mouseY, 400, 70 + animScroll, 497, 140 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("DarkNight");
            }

            if (mouseWithinBounds(mouseX, mouseY, 525, 70 + animScroll, 622, 140 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Sun");
            }

            // Line 2

            if (mouseWithinBounds(mouseX, mouseY, 25, 165 + animScroll, 122, 235 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Tree");
            }

            if (mouseWithinBounds(mouseX, mouseY, 150, 165 + animScroll, 247, 235 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Flower");
            }

            if (mouseWithinBounds(mouseX, mouseY, 275, 165 + animScroll, 372, 235 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Loyoi");
            }

            if (mouseWithinBounds(mouseX, mouseY, 400, 165 + animScroll, 497, 235 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Cero");
            }

            if (mouseWithinBounds(mouseX, mouseY, 525, 165 + animScroll, 622, 235 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Soniga");
            }

            // Line 3

            if (mouseWithinBounds(mouseX, mouseY, 25, 260 + animScroll, 122, 330 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("May");
            }
            if (mouseWithinBounds(mouseX, mouseY, 150, 260 + animScroll, 247, 330 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Mint");
            }
            if (mouseWithinBounds(mouseX, mouseY, 275, 260 + animScroll, 372, 330 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Azure");
            }
            if (mouseWithinBounds(mouseX, mouseY, 400, 260 + animScroll, 497, 330 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Rainbow");
            }
            if (mouseWithinBounds(mouseX, mouseY, 525, 260 + animScroll, 622, 330 + animScroll)) {
                ClientTheme.INSTANCE.getClientColorMode().set("Astolfo");
            }

            // Line 4
            if (animScroll < -75) {
                if (mouseWithinBounds(mouseX, mouseY, 25, 355 + animScroll, 122, 425 + animScroll)) {
                    ClientTheme.INSTANCE.getClientColorMode().set("Pumpkin");
                }
                if (mouseWithinBounds(mouseX, mouseY, 150, 355 + animScroll, 247, 425 + animScroll)) {
                    ClientTheme.INSTANCE.getClientColorMode().set("Polarized");
                }
                if (mouseWithinBounds(mouseX, mouseY, 275, 355 + animScroll, 372, 425 + animScroll)) {
                    ClientTheme.INSTANCE.getClientColorMode().set("Sundae");
                }
                if (mouseWithinBounds(mouseX, mouseY, 400, 355 + animScroll, 497, 425 + animScroll)) {
                    ClientTheme.INSTANCE.getClientColorMode().set("Terminal");
                }
                if (mouseWithinBounds(mouseX, mouseY, 525, 355 + animScroll, 622, 425 + animScroll)) {
                    ClientTheme.INSTANCE.getClientColorMode().set("Coral");
                }
            }
            // Line 5
            if (animScroll < -115) {
                if (mouseWithinBounds(mouseX, mouseY, 25, 450 + animScroll, 122, 520 + animScroll)) {
                    ClientTheme.INSTANCE.getClientColorMode().set("Fire");
                }
                if (mouseWithinBounds(mouseX, mouseY, 150, 450 + animScroll, 247, 520 + animScroll)) {
                    ClientTheme.INSTANCE.getClientColorMode().set("Aqua");
                }
                if (mouseWithinBounds(mouseX, mouseY, 275, 450 + animScroll, 372, 520 + animScroll)) {
                    ClientTheme.INSTANCE.getClientColorMode().set("Peony");
                }

            }

            if (mouseWithinBounds(mouseX, mouseY, 25, 350, 40, 365)) {
                ClientTheme.INSTANCE.getTextValue().set(!ClientTheme.INSTANCE.getTextValue().get());
            }
            if (mouseWithinBounds(mouseX, mouseY, 90, 375, 140, 390)) {
                ClientTheme.INSTANCE.getUpdown().set(!ClientTheme.INSTANCE.getUpdown().get());
            }
            if (mouseWithinBounds(mouseX, mouseY, 160, 380, 180, 400)) {
                if (ClientTheme.INSTANCE.getFadespeed().get() != 20)
                    ClientTheme.INSTANCE.getFadespeed().set(ClientTheme.INSTANCE.getFadespeed().get() + 1);
            }
            if (mouseWithinBounds(mouseX, mouseY, 160, 410, 180, 430)) {
                if (ClientTheme.INSTANCE.getFadespeed().get() != 0)
                    ClientTheme.INSTANCE.getFadespeed().set(ClientTheme.INSTANCE.getFadespeed().get() - 1);
            }
        }
        */
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (focused) {
            drag.onRelease(button);
            ScaledResolution sr = new ScaledResolution(mc);
            if (drag.getX() + rectWidth > sr.getScaledWidth() && clickAnimation.isDone()) {
                focused = false;
            }
        }
    }
}
