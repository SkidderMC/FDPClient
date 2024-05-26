/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.SideGui;

import net.ccbluex.liquidbounce.ui.clickgui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.animations.Animation;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.animations.Direction;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.normal.TimerUtil;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.objects.Drag;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils;
import net.ccbluex.liquidbounce.ui.font.cf.FontLoaders;
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.Fonts;
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme;
import net.ccbluex.liquidbounce.utils.MathUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.HashMap;

import static net.ccbluex.liquidbounce.utils.MouseUtils.mouseWithinBounds;

public class SideGui extends GuiPanel {

    //  private final ConfigPanel configPanel = new ConfigPanel();
    //  private final ScriptPanel scriptPanel = new ScriptPanel();
    private final String[] categories = {"Scripts", "Configs", "Design"};
    public boolean focused;
    public Animation clickAnimation;
    private Animation hoverAnimation;
    private Animation textAnimation;
    private Animation moveOverGradientAnimation;
    private HashMap<String, Animation[]> categoryAnimation = new HashMap<>();
    private Drag drag;
    private String currentCategory = "Configs";

    private TimerUtil timerUtil;
    private float scroll = 0F;
    private boolean text = false;
    private float animScroll = 0F;


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
        //  configPanel.initGui();
        //   scriptPanel.initGui();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        switch (currentCategory) {
            case "Configs":
                //     configPanel.keyTyped(typedChar, keyCode);
                break;
            case "Scripts":
                //scriptPanel.keyTyped(typedChar, keyCode);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks, int alpha) {

        // if (configPanel.reInit) {
        //    configPanel.initGui();
        //     configPanel.reInit = false;
        //  }
        //if (scriptPanel.reInit) {
        //  scriptPanel.initGui();
        // }

        clickAnimation.setDirection(focused ? Direction.FORWARDS : Direction.BACKWARDS);
        boolean hovering = DrRenderUtils.isHovering(drag.getX(), drag.getY(), rectWidth, rectHeight, mouseX, mouseY);
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
        ScaledResolution sr = new ScaledResolution(mc);

        boolean setDirection = !focused && (!timerUtil.hasTimeElapsed(6000) || (!hoverAnimation.isDone() || hoverAnimation.isDone() && hoverAnimation.getDirection().equals(Direction.FORWARDS)));
        textAnimation.setDirection(setDirection ? Direction.FORWARDS : Direction.BACKWARDS);


        if(!textAnimation.isDone() || textAnimation.getDirection().equals(Direction.FORWARDS) && textAnimation.isDone()) {
            //    FontUtil.iconFont26.drawString(FontUtil.PLAY, drag.getX() -
            //              ((FontUtil.iconFont26.getStringWidth(FontUtil.PLAY) + 10) * textAnimation.getOutput()),
            //     drag.getY() + FontUtil.iconFont26.getMiddleOfBox(rectHeight),
            //   ColorUtil.applyOpacity(-1, (float) textAnimation.getOutput() * 0.5F));
        }


        if (!clickAnimation.isDone()) {
            drag.setX(MathUtils.interpolateFloat(sr.getScaledWidth() - 30, focused ? sr.getScaledWidth() / 2f - rectWidth / 2f : drag.getX(), (float) clickAnimation.getOutput()));
            drag.setY(MathUtils.interpolateFloat(sr.getScaledHeight() / 2f - rectHeight / 2f, drag.getY(), (float) clickAnimation.getOutput()));
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
        RoundedUtil.drawRound(x, y, rectWidth, rectHeight, 9, mainRectColor);
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

            RoundedUtil.drawRound(xVal - 30, yVal - 5, 60, Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.getHeight() + 10, 6, finalColor);

            DrRenderUtils.resetColor();
            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawCenteredString(category, xVal, y + 15, textColor);
            seperation += 100;
        }

        DrRenderUtils.drawRect2(x + 20, y + 50, rectWidth - 40, 1, new Color(45, 45, 45, alpha).getRGB());

        if (currentCategory.equals("Design")) {

            // Text
            text = ClientTheme.INSTANCE.getTextValue().get();
            RenderUtils.drawRoundedRect(25F, 350.0f, 40F, 365.0f, 5F, textColor);
            RenderUtils.drawRoundedOutline(25F, 350.0f, 40F, 365.0f, 7F, 1F, Color.WHITE.getRGB());
            FontLoaders.C12.drawStringWithShadow("Text White Color", 43.0, 351.5, Color.WHITE.getRGB());
            FontLoaders.C12.drawStringWithShadow("Fade Side : " + ClientTheme.INSTANCE.getUpdown().get(), 25.0, 376.5, Color.WHITE.getRGB());
            FontLoaders.C12.drawStringWithShadow("FadeSpeed : " + ClientTheme.INSTANCE.getFadespeed().get(), 25.0, 401.5, Color.WHITE.getRGB());
            GlStateManager.resetColor();

            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString("Not Finished - SOOOOOOOOON", x + rectWidth / 2, y + rectHeight / 2, DrRenderUtils.applyOpacity(-1, alpha / 255f));

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
            FontLoaders.F18.drawStringWithShadow("Tree", x + 60.0, y + 240.0 + scroll, ClientTheme.INSTANCE.getColorFromName("Tree", 1).getRGB());

        }

        if (currentCategory.equals("Scripts")) {
           /* scriptPanel.x = x;
            scriptPanel.rawY = y;
            scriptPanel.rectWidth = rectWidth;
            scriptPanel.rectHeight = rectHeight;
            scriptPanel.drawScreen(mouseX, mouseY, partialTicks, (int) rectAlpha);*/
        } else {
            //  configPanel.x = x;
            //     configPanel.rawY = y;
            //  configPanel.rectWidth = rectWidth;
            // configPanel.rectHeight = rectHeight;
            //configPanel.drawScreen(mouseX, mouseY, partialTicks, (int) rectAlpha);
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

            if (currentCategory.equals("Configs")) {

            } else {

            }
        }

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
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (focused) {
            drag.onRelease(button);
            ScaledResolution sr = new ScaledResolution(mc);
            if (drag.getX() + rectWidth > sr.getScaledWidth() && clickAnimation.isDone()) {
                focused = false;
            }
            if (currentCategory.equals("Configs")) {

            }
        }
    }
}
