/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.impl;

import lombok.Getter;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.EaseInOutQuad;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Main;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils;
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.Fonts;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Component;
import java.awt.*;

public class ModuleRect extends Component {

    public final Module module;
    private final SettingComponents settingComponents;
    private final Animation animation = new EaseInOutQuad(300, 1, Direction.BACKWARDS);
    private final Animation arrowAnimation = new EaseInOutQuad(250, 1, Direction.BACKWARDS);
    private final Animation hoverAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);
    public Animation settingAnimation;
    public Animation openingAnimation;
    public float x, y, width, height, panelLimitY;
    public int alphaAnimation;
    int clickX, clickY;
    @Getter
    private double settingSize;

    public ModuleRect(Module module) {
        this.module = module;
        settingComponents = new SettingComponents(module);
    }

    public void initGui() {
        animation.setDirection(module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (module.getExpanded()) {
            settingComponents.keyTyped(typedChar, keyCode);
        }
    }

    public void drawScreen(int mouseX, int mouseY) {
        Color rectColor = new Color(43, 45, 50, alphaAnimation);
        Color textColor = new Color(255, 255, 255, alphaAnimation);
        int index = 0;
        Color debcolor = new Color(ClickGUIModule.generateColor(index).getRGB());

        Color clickModColor = DrRenderUtils.applyOpacity(debcolor, alphaAnimation / 255f);

        float alpha = alphaAnimation / 255f;

        boolean hoveringModule = DrRenderUtils.isHovering(x, y, width, height, mouseX, mouseY);
        hoverAnimation.setDirection(hoveringModule ? Direction.FORWARDS : Direction.BACKWARDS);

        DrRenderUtils.drawRect2(x, y, width, height, DrRenderUtils.interpolateColor(rectColor.getRGB(), DrRenderUtils.brighter(rectColor, .8f).getRGB(), (float) hoverAnimation.getOutput()));

        DrRenderUtils.drawRect2(x, y, width, height, DrRenderUtils.applyOpacity(clickModColor, (float) animation.getOutput()).getRGB());

        Fonts.SF.SF_20.SF_20.drawString(module.getName(), x + 5, y + Fonts.SF.SF_20.SF_20.getMiddleOfBox(height), textColor.getRGB());

        if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && module.getKeyBind() != 0) {
            String keyName = Keyboard.getKeyName(module.getKeyBind());
            Fonts.SF.SF_20.SF_20.drawString(keyName, x + width - Fonts.SF.SF_20.SF_20.stringWidth(keyName) - 5, y + Fonts.SF.SF_20.SF_20.getMiddleOfBox(height), textColor.getRGB());
        } else {
            float arrowSize = 6;
            arrowAnimation.setDirection(module.getExpanded() ? Direction.FORWARDS : Direction.BACKWARDS);
            DrRenderUtils.setAlphaLimit(0);
            DrRenderUtils.resetColor();
            DrRenderUtils.drawClickGuiArrow(x + width - (arrowSize + 5), y + height / 2f - 2, arrowSize, arrowAnimation, textColor.getRGB());
        }

        Color settingRectColor = new Color(32, 32, 32, alphaAnimation);


        double settingHeight = (settingComponents.settingSize) * settingAnimation.getOutput();
        if (module.getExpanded() || !settingAnimation.isDone()) {
            DrRenderUtils.drawRect2(x, y + height, width, settingHeight * height, settingRectColor.getRGB());

            if (ClickGUIModule.INSTANCE.getBackback()) {

                DrRenderUtils.resetColor();

                float accentAlpha = (float) (.85 * animation.getOutput()) * alpha;

                DrRenderUtils.drawRect2(x, y + height, width, (float) (settingHeight * height), DrRenderUtils.applyOpacity(clickModColor, accentAlpha).getRGB());
            }


            settingComponents.x = x;
            settingComponents.y = y + height;
            settingComponents.width = width;
            settingComponents.rectHeight = height;
            settingComponents.panelLimitY = panelLimitY;
            settingComponents.alphaAnimation = alphaAnimation;
            settingComponents.settingHeightScissor = settingAnimation;
            if (!settingAnimation.isDone()) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                DrRenderUtils.scissor(x, y + height, width, settingHeight * height);

                settingComponents.drawScreen(mouseX, mouseY);
                DrRenderUtils.drawGradientRect2(x, y + height, width, 6, new Color(0, 0, 0, 60).getRGB(), new Color(0, 0, 0, 0).getRGB());
                DrRenderUtils.drawGradientRect2(x, y + 11 + (settingHeight * height), width, 6, new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, 60).getRGB());
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            } else {
                settingComponents.drawScreen(mouseX, mouseY);
                DrRenderUtils.drawGradientRect2(x, y + height, width, 6, new Color(0, 0, 0, 60).getRGB(), new Color(0, 0, 0, 0).getRGB());
                DrRenderUtils.drawGradientRect2(x, y + 11 + (settingHeight * height), width, 6, new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, 60).getRGB());
            }

        }
        settingSize = settingHeight;

    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hoveringModule = isClickable(y, panelLimitY) && DrRenderUtils.isHovering(x, y, width, height, mouseX, mouseY);
        if (hoveringModule) {
            switch (button) {
                case 0:
                    clickX = mouseX;
                    clickY = mouseY;
                    animation.setDirection(!module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
                    module.toggle();
                    break;
                case 1:
                    module.setExpanded(!module.getExpanded());
                    break;
            }
        }
        if (module.getExpanded()) {
            settingComponents.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (module.getExpanded()) {
            settingComponents.mouseReleased(mouseX, mouseY, state);
        }
    }

    public boolean isClickable(float y, float panelLimitY) {
        return y > panelLimitY && y < panelLimitY + Main.allowedClickGuiHeight + 17;
    }
}