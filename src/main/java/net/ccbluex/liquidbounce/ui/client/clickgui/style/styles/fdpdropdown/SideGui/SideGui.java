/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule;
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule;
import net.ccbluex.liquidbounce.handler.api.AutoSettings;
import net.ccbluex.liquidbounce.handler.api.ClientApi;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.TimerUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.Drag;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.Fonts;
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils;
import net.ccbluex.liquidbounce.utils.render.AnimationUtils;
import net.ccbluex.liquidbounce.utils.client.ClientUtils;
import net.ccbluex.liquidbounce.config.SettingsUtils;
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

    private final String[] categories = {"UI", "Configs", "Color"};
    public boolean focused;
    public Animation clickAnimation;
    private Animation hoverAnimation;
    private Animation textAnimation;
    private Animation moveOverGradientAnimation;
    private HashMap<String, Animation[]> categoryAnimation = new HashMap<>();
    private Drag drag;
    private String currentCategory = "UI";
    private float scroll = 0F;
    private float animScroll = 0F;
    private final float[] smooth = {0F, 0F, 0F, 0F};
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

        AWTFontRenderer.Companion.setAssumeNonVolatile(true);

        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            scroll += wheel > 0 ? -30 : 30;
            scroll = Math.max(-200, Math.min(0, scroll));
        }

        animScroll = AnimationUtils.INSTANCE.animate(scroll, animScroll, 0.5F);

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

        if (currentCategory.equals("Color")) {
            String[] themeColors = {
                    "Zywl", "Water", "Magic", "DarkNight", "Sun",
                    "Tree", "Flower", "Loyoi", "Cero", "Soniga",
                    "May", "Mint", "Azure", "Rainbow", "Astolfo",
                    "Pumpkin", "Polarized", "Sundae", "Terminal", "Coral",
                    "Fire", "Aqua", "Peony"
            };

            float colorXStart = drag.getX() + 25;
            float colorYStart = drag.getY() + 60 + animScroll;
            float colorWidth = 80f;
            float colorHeight = 60f;
            int colorsPerRow = 5;
            float colorX = colorXStart;
            float colorY = colorYStart;

            float maxVisibleHeight = drag.getY() + rectHeight - 60;

            for (int i = 0; i < themeColors.length; i++) {
                String colorName = themeColors[i];

                boolean isHovered = DrRenderUtils.isHovering(colorX, colorY, colorWidth, colorHeight, mouseX, mouseY);
                boolean mousePressed = Mouse.isButtonDown(0);

                if (colorY + colorHeight > drag.getY() + 60 && colorY < maxVisibleHeight) {
                    boolean isSelected = ClientThemesUtils.INSTANCE.getClientColorMode().equals(colorName);

                    int startColor = ClientThemesUtils.INSTANCE.getColorFromName(colorName, 0).getRGB();
                    int endColor = ClientThemesUtils.INSTANCE.getColorFromName(colorName, 180).getRGB();

                    RenderUtils.INSTANCE.drawGradientRect(
                            (int) colorX,
                            (int) colorY,
                            (int) (colorX + colorWidth),
                            (int) (colorY + colorHeight),
                            startColor,
                            endColor
                    );

                    if (isSelected) {
                        smooth[0] = AnimationUtils.INSTANCE.animate(colorX, smooth[0], 0.02F * RenderUtils.INSTANCE.getDeltaTime());
                        smooth[1] = AnimationUtils.INSTANCE.animate(colorY, smooth[1], 0.02F * RenderUtils.INSTANCE.getDeltaTime());
                        smooth[2] = AnimationUtils.INSTANCE.animate(colorX + colorWidth, smooth[2], 0.02F * RenderUtils.INSTANCE.getDeltaTime());
                        smooth[3] = AnimationUtils.INSTANCE.animate(colorY + colorHeight, smooth[3], 0.02F * RenderUtils.INSTANCE.getDeltaTime());
                        RenderUtils.INSTANCE.drawRoundedOutline(
                                (int) smooth[0],
                                (int) smooth[1],
                                (int) smooth[2],
                                (int) smooth[3],
                                10,
                                3,
                                new Color(startColor).brighter().getRGB()
                        );
                    }

                    Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawCenteredString(
                            colorName,
                            (int) (colorX + colorWidth / 2),
                            (int) (colorY + colorHeight / 2 - (float) Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.getHeight() / 2),
                            Color.WHITE.getRGB()
                    );
                }

                if (isHovered && mousePressed) {
                    ClientThemesUtils.INSTANCE.setClientColorMode(colorName);
                    FDPClient.INSTANCE.getFileManager().saveConfig(FDPClient.INSTANCE.getFileManager().getColorThemeConfig(), true);
                    ClientUtils.INSTANCE.getLOGGER().info("Saved color theme configuration: " + colorName);
                }

                colorX += colorWidth + 10;
                if ((i + 1) % colorsPerRow == 0) {
                    colorX = colorXStart;
                    colorY += colorHeight + 10;
                }
            }

            float buttonX = colorXStart + (colorWidth + 10) * 5;
            float buttonY = drag.getY() + 60 + animScroll;
            float buttonWidth = 50f;
            float buttonHeight = 15f;
            float buttonSpacing = 5f;
            float fadeSpeedSliderX = drag.getX() + 25;
            float fadeSpeedSliderY = drag.getY() + 20;
            float fadeSpeedSliderWidth = 80f;
            float fadeSpeedSliderHeight = 10f;

            if (buttonY + (buttonHeight + buttonSpacing) * 2 < maxVisibleHeight) {

                DrRenderUtils.drawRect2(fadeSpeedSliderX, fadeSpeedSliderY, fadeSpeedSliderWidth, fadeSpeedSliderHeight, new Color(60, 60, 60).getRGB());

                float sliderValue = ClientThemesUtils.INSTANCE.getThemeFadeSpeed() / 10f * fadeSpeedSliderWidth;
                DrRenderUtils.drawRect2(fadeSpeedSliderX, fadeSpeedSliderY, sliderValue, fadeSpeedSliderHeight, new Color(100, 150, 100).getRGB());

                Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(String.format("Speed: %s", ClientThemesUtils.INSTANCE.getThemeFadeSpeed()), fadeSpeedSliderX + 5, fadeSpeedSliderY - 15, Color.WHITE.getRGB());

                DrRenderUtils.drawRect2(buttonX, buttonY, buttonWidth, buttonHeight,
                        ClientThemesUtils.INSTANCE.getUpdown() ? new Color(0, 150, 0).getRGB() : new Color(150, 0, 0).getRGB());
                Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("Side", buttonX + 2, buttonY + 2, Color.WHITE.getRGB());
            }
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
            int onlineButtonColor = !showLocalConfigs ? new Color(100, 150, 100, alpha).getRGB() :
                    (isOnlineHovered ? new Color(70, 70, 70, alpha).getRGB() : new Color(50, 50, 50, alpha).getRGB());
            DrRenderUtils.drawRect2(onlineButtonX, y + 30, buttonToggleWidth, buttonToggleHeight, onlineButtonColor);
            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("ONLINE", onlineButtonX + 10, y + 35, DrRenderUtils.applyOpacity(-1, alpha / 255f));

            // "LOCAL" Button
            boolean isLocalHovered = DrRenderUtils.isHovering(localButtonX, y + 30, buttonToggleWidth, buttonToggleHeight, mouseX, mouseY);
            int localButtonColor = showLocalConfigs ? new Color(100, 150, 100, alpha).getRGB() :
                    (isLocalHovered ? new Color(70, 70, 70, alpha).getRGB() : new Color(50, 50, 50, alpha).getRGB());
            DrRenderUtils.drawRect2(localButtonX, y + 30, buttonToggleWidth, buttonToggleHeight, localButtonColor);
            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("LOCAL", localButtonX + 10, y + 35, DrRenderUtils.applyOpacity(-1, alpha / 255f));

            // "OPEN FOLDER" Button
            boolean isOpenFolderHovered = DrRenderUtils.isHovering(openFolderButtonX, y + 30, buttonToggleWidth * 2, buttonToggleHeight, mouseX, mouseY);
            int openFolderButtonColor = isOpenFolderHovered ? new Color(70, 70, 70, alpha).getRGB() : new Color(50, 50, 50, alpha).getRGB();
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
                wasMousePressed = true;
            }

            if (!Mouse.isButtonDown(0)) {
                wasMousePressed = false;
            }

            float configX = x + 25;
            float configY = y + 60;
            float buttonWidth = (rectWidth - 50) / 4 - 10;
            float buttonHeight = 20;
            int configsPerRow = 4;
            int configCount = 0;

            if (showLocalConfigs) {
                File[] localConfigs = FDPClient.INSTANCE.getFileManager().getSettingsDir().listFiles((dir, name) -> name.endsWith(".txt"));
                if (localConfigs != null && localConfigs.length > 0) {
                    for (File file : localConfigs) {
                        String configName = file.getName().replace(".txt", "");

                        boolean isHovered = DrRenderUtils.isHovering(configX, configY, buttonWidth, buttonHeight, mouseX, mouseY);
                        int buttonColor = isHovered ? new Color(70, 70, 70, alpha).getRGB() : new Color(50, 50, 50, alpha).getRGB();
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
                if (getAutoSettingsList() != null && getAutoSettingsList().length > 0) {
                    for (AutoSettings setting : getAutoSettingsList()) {
                        boolean isHovered = DrRenderUtils.isHovering(configX, configY, buttonWidth, buttonHeight, mouseX, mouseY);
                        int buttonColor = isHovered ? new Color(70, 70, 70, alpha).getRGB() : new Color(50, 50, 50, alpha).getRGB();
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

        RenderUtils.INSTANCE.drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, new Color(HUDModule.INSTANCE.getGuiColor()));

        AWTFontRenderer.Companion.setAssumeNonVolatile(false);
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

            if (currentCategory.equals("Color")) {
                String[] themeColors = {
                        "Zywl", "Water", "Magic", "DarkNight", "Sun",
                        "Tree", "Flower", "Loyoi", "Cero", "Soniga",
                        "May", "Mint", "Azure", "Rainbow", "Astolfo",
                        "Pumpkin", "Polarized", "Sundae", "Terminal", "Coral",
                        "Fire", "Aqua", "Peony"
                };

                float colorXStart = drag.getX() + 25;
                float colorYStart = drag.getY() + 60 + animScroll;
                float colorWidth = 80f;
                float colorHeight = 60f;
                int colorsPerRow = 5;
                float colorX = colorXStart;
                float colorY = colorYStart;

                for (int i = 0; i < themeColors.length; i++) {
                    String colorName = themeColors[i];
                    if (DrRenderUtils.isHovering(colorX, colorY, colorWidth, colorHeight, mouseX, mouseY)) {
                        ClientThemesUtils.INSTANCE.setClientColorMode(colorName);
                        return;
                    }

                    colorX += colorWidth + 10;
                    if ((i + 1) % colorsPerRow == 0) {
                        colorX = colorXStart;
                        colorY += colorHeight + 10;
                    }
                }

                float buttonX = colorXStart + (colorWidth + 10) * 5;
                float buttonY = drag.getY() + 60 + animScroll;
                float buttonWidth = 80f;
                float buttonHeight = 20f;

                if (DrRenderUtils.isHovering(buttonX, buttonY, buttonWidth, buttonHeight, mouseX, mouseY)) {
                    ClientThemesUtils.INSTANCE.setUpdown(!ClientThemesUtils.INSTANCE.getUpdown());
                }

                float sliderX = drag.getX() + 25;
                float sliderY = drag.getY() + 20;
                float sliderWidth = 80f;
                float sliderHeight = 10f;

                if (DrRenderUtils.isHovering(sliderX, sliderY, sliderWidth, sliderHeight, mouseX, mouseY)) {
                    float newValue = (mouseX - sliderX) / sliderWidth * 10;
                    newValue = Math.max(0, Math.min(10, newValue));
                    ClientThemesUtils.INSTANCE.setThemeFadeSpeed((int) newValue);
                }
            }
        }
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
