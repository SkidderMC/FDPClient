/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.impl;

import net.ccbluex.liquidbounce.config.BoolValue;
import net.ccbluex.liquidbounce.config.FloatValue;
import net.ccbluex.liquidbounce.config.IntegerValue;
import net.ccbluex.liquidbounce.config.ListValue;
import net.ccbluex.liquidbounce.config.NumberValue;
import net.ccbluex.liquidbounce.config.TextValue;
import net.ccbluex.liquidbounce.config.Value;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.EaseInOutQuad;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Main;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.PasswordField;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.GuiEvents;
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.Fonts;
import net.ccbluex.liquidbounce.utils.extensions.MathExtensionsKt;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class SettingComponents extends Component {

    public static float scale;
    private final Module module;
    public Animation settingHeightScissor;
    private final HashMap<Module, Animation[]> keySettingAnimMap = new HashMap<>();
    private final HashMap<IntegerValue, Float> sliderintMap = new HashMap<>();
    private final HashMap<IntegerValue, Animation[]> sliderintAnimMap = new HashMap<>();
    private final HashMap<FloatValue, Float> sliderfloatMap = new HashMap<>();
    private final HashMap<FloatValue, Animation[]> sliderfloatAnimMap = new HashMap<>();
    private final HashMap<NumberValue, Float> sliderMap = new HashMap<>();
    private final HashMap<NumberValue, Animation[]> sliderAnimMap = new HashMap<>();
    private final HashMap<BoolValue, Animation[]> toggleAnimation = new HashMap<>();
    private final HashMap<ListValue, Animation[]> modeSettingAnimMap = new HashMap<>();
    private final HashMap<ListValue, Boolean> modeSettingClick = new HashMap<>();
    private final HashMap<ListValue, HashMap<String, Animation>> modesHoverAnimation = new HashMap<>();
    public Module binding;
    public Value draggingNumber;
    public float x, y, width, rectHeight, panelLimitY;
    public int alphaAnimation;
    public double settingSize;
    private PasswordField selectedField;
    private TextValue selectedStringSetting;
    private boolean hueFlag;

    public SettingComponents(Module module) {
        this.module = module;
        keySettingAnimMap.put(module, new Animation[]{
                new EaseInOutQuad(250, 1, Direction.BACKWARDS),
                new DecelerateAnimation(225, 1, Direction.BACKWARDS)
        });

        for (Value setting : module.getValues()) {

            if (setting instanceof NumberValue) {
                sliderMap.put((NumberValue) setting, 0f);
                sliderAnimMap.put((NumberValue) setting, new Animation[]{
                        new DecelerateAnimation(250, 1, Direction.BACKWARDS),
                        new DecelerateAnimation(200, 1, Direction.BACKWARDS)
                });
            }
            if (setting instanceof FloatValue) {
                sliderfloatMap.put((FloatValue) setting, 0f);
                sliderfloatAnimMap.put((FloatValue) setting, new Animation[]{
                        new DecelerateAnimation(250, 1, Direction.BACKWARDS),
                        new DecelerateAnimation(200, 1, Direction.BACKWARDS)
                });
            }
            if (setting instanceof IntegerValue) {
                sliderintMap.put((IntegerValue) setting, 0f);
                sliderintAnimMap.put((IntegerValue) setting, new Animation[]{
                        new DecelerateAnimation(250, 1, Direction.BACKWARDS),
                        new DecelerateAnimation(200, 1, Direction.BACKWARDS)
                });
            }
            if (setting instanceof BoolValue) {
                toggleAnimation.put((BoolValue) setting, new Animation[]{
                        new DecelerateAnimation(225, 1, Direction.BACKWARDS),
                        new DecelerateAnimation(200, 1, Direction.BACKWARDS)
                });
            }
            if (setting instanceof ListValue) {
                ListValue modeSetting = (ListValue) setting;
                modeSettingClick.put(modeSetting, false);
                modeSettingAnimMap.put(modeSetting, new Animation[]{
                        new DecelerateAnimation(225, 1, Direction.BACKWARDS),
                        new EaseInOutQuad(250, 1, Direction.BACKWARDS)
                });

                HashMap<String, Animation> modeMap = new HashMap<>();
                for (String mode : modeSetting.getValues()) {
                    modeMap.put(mode, new DecelerateAnimation(225, 1, Direction.BACKWARDS));
                }
                modesHoverAnimation.put(modeSetting, modeMap);
            }
        }
    }

    @Override
    public void initGui() {
        // No additional init code here
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (binding != null) {
            selectedField = null;
            selectedStringSetting = null;
            if (keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
                binding.setKeyBind(Keyboard.KEY_NONE);
            }
            binding.setKeyBind(keyCode);
            binding = null;
            return;
        }

        if (selectedField != null) {
            // ESC key => stop focusing
            if (keyCode == 1) {
                selectedField = null;
                selectedStringSetting = null;
                return;
            }
            selectedField.textboxKeyTyped(typedChar, keyCode);
            selectedStringSetting.set(selectedField.getTextValue(), true);
        }
    }

    public void handle(int mouseX, int mouseY, int button, GuiEvents type) {
        // Setting up the colors
        Color textColor = new Color(255, 255, 255, alphaAnimation);
        Color darkRectColor = new Color(48, 50, 55, alphaAnimation);
        Color darkRectColorDisabled = new Color(52, 52, 52, alphaAnimation);
        Color darkRectHover = DrRenderUtils.brighter(darkRectColor, .8f);

        boolean accent = ClickGUIModule.INSTANCE.getColormode().equalsIgnoreCase("Color");
        int index = 0;
        Color color2 = new Color(ClickGUIModule.generateColor(index).getRGB());
        Color[] colors = new Color[]{color2, color2};

        Color accentedColor = DrRenderUtils.applyOpacity(colors[0], alphaAnimation / 255f);
        Color accentedColor2 = DrRenderUtils.applyOpacity(colors[1], alphaAnimation / 255f);

        double count = 0;

        for (Value setting : module.getValues().stream().filter(Value::shouldRender).collect(Collectors.toList())) {

            float settingY = (float) MathExtensionsKt.roundToHalf(y + (count * rectHeight));

            // ----- FloatValue -----
            if (setting instanceof FloatValue) {
                FloatValue numberSetting = (FloatValue) setting;

                String value = Float.toString((float) MathExtensionsKt.round(numberSetting.getValue(), 0.01));
                float regularFontWidth = (float) Fonts.SF.SF_18.SF_18.stringWidth(numberSetting.getName() + ": ");
                float valueFontWidth = (float) Fonts.SF.SF_18.SF_18.stringWidth(value);

                float titleX = x + width / 2f - (regularFontWidth + valueFontWidth) / 2f;
                float titleY = settingY + Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight)
                        - Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight) / 2f + 1;

                GlStateManager.color(1, 1, 1, 1);
                Fonts.SF.SF_18.SF_18.drawString(numberSetting.getName() + ": ", titleX, titleY, textColor.getRGB());
                Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.drawString(value, titleX + regularFontWidth, titleY, textColor.getRGB());

                Animation hoverAnimation = sliderfloatAnimMap.get(numberSetting)[0];
                Animation selectAnimtion = sliderfloatAnimMap.get(numberSetting)[1];

                float totalSliderWidth = width - 10;
                boolean hoveringSlider = isClickable(settingY + 17)
                        && DrRenderUtils.isHovering(x + 5, settingY + 17, totalSliderWidth, 6, mouseX, mouseY);

                if (type == GuiEvents.RELEASE) {
                    draggingNumber = null;
                }
                hoverAnimation.setDirection(
                        hoveringSlider || draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS
                );
                selectAnimtion.setDirection(
                        draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS
                );

                if (type == GuiEvents.CLICK && hoveringSlider && button == 0) {
                    draggingNumber = numberSetting;
                }

                double currentValue = numberSetting.getValue();
                if (draggingNumber != null && draggingNumber == setting) {
                    float percent = Math.min(1, Math.max(0, (mouseX - (x + 5)) / totalSliderWidth));
                    double newValue = (percent * (numberSetting.getMaximum() - numberSetting.getMinimum()))
                            + numberSetting.getMinimum();
                    numberSetting.set(newValue);
                }

                float sliderMath = (float) ((currentValue - numberSetting.getMinimum())
                        / (numberSetting.getMaximum() - numberSetting.getMinimum()));

                // Animate the slider position
                float oldSlider = sliderfloatMap.get(numberSetting);
                float targetSlider = totalSliderWidth * sliderMath;
                sliderfloatMap.put(
                        numberSetting,
                        (float) DrRenderUtils.animate(targetSlider, oldSlider, .1)
                );

                float sliderY = (settingY + 18);
                RenderUtils.drawCustomShapeWithRadius(
                        x + 5, sliderY, totalSliderWidth, 3, 1.5f,
                        DrRenderUtils.applyOpacity(darkRectHover, (float) (.4f + (.2 * hoverAnimation.getOutput())))
                );
                RenderUtils.drawCustomShapeWithRadius(
                        x + 5, sliderY, Math.max(4, sliderfloatMap.get(numberSetting)), 3, 1.5f,
                        accent ? accentedColor2 : textColor
                );

                DrRenderUtils.setAlphaLimit(0);
                DrRenderUtils.fakeCircleGlow(
                        x + 4 + Math.max(4, sliderfloatMap.get(numberSetting)),
                        sliderY + 1.5f, 6, Color.BLACK, .3f
                );
                DrRenderUtils.drawGoodCircle(
                        x + 4 + Math.max(4, sliderfloatMap.get(numberSetting)),
                        sliderY + 1.5f, 3.75f,
                        accent ? accentedColor2.getRGB() : textColor.getRGB()
                );

                count += .5f;
            }

            // ----- IntegerValue -----
            if (setting instanceof IntegerValue) {
                IntegerValue numberSetting = (IntegerValue) setting;
                String value = Float.toString((float) MathExtensionsKt.roundX(numberSetting.getValue(), 1));

                float regularFontWidth = (float) Fonts.SF.SF_18.SF_18.stringWidth(numberSetting.getName() + ": ");
                float valueFontWidth = (float) Fonts.SF.SF_18.SF_18.stringWidth(value);

                float titleX = x + width / 2f - (regularFontWidth + valueFontWidth) / 2f;
                float titleY = settingY + Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight)
                        - Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight) / 2f + 1;

                GlStateManager.color(1, 1, 1, 1);
                Fonts.SF.SF_18.SF_18.drawString(numberSetting.getName() + ": ", titleX, titleY, textColor.getRGB());
                Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.drawString(value, titleX + regularFontWidth, titleY, textColor.getRGB());

                Animation hoverAnimation = sliderintAnimMap.get(numberSetting)[0];
                Animation selectAnimtion = sliderintAnimMap.get(numberSetting)[1];

                float totalSliderWidth = width - 10;
                boolean hoveringSlider = isClickable(settingY + 17)
                        && DrRenderUtils.isHovering(x + 5, settingY + 17, totalSliderWidth, 6, mouseX, mouseY);

                if (type == GuiEvents.RELEASE) {
                    draggingNumber = null;
                }
                hoverAnimation.setDirection(
                        hoveringSlider || draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS
                );
                selectAnimtion.setDirection(
                        draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS
                );

                if (type == GuiEvents.CLICK && hoveringSlider && button == 0) {
                    draggingNumber = numberSetting;
                }

                double currentValue = numberSetting.getValue();
                if (draggingNumber != null && draggingNumber == setting) {
                    float percent = Math.min(1, Math.max(0, (mouseX - (x + 5)) / totalSliderWidth));
                    double newValue = (percent * (numberSetting.getMaximum() - numberSetting.getMinimum()))
                            + numberSetting.getMinimum();
                    numberSetting.set(newValue);
                }

                float sliderMath = (float) ((currentValue - numberSetting.getMinimum())
                        / (numberSetting.getMaximum() - numberSetting.getMinimum()));

                // Animate the slider position
                float oldSlider = sliderintMap.get(numberSetting);
                float targetSlider = totalSliderWidth * sliderMath;
                sliderintMap.put(
                        numberSetting,
                        (float) DrRenderUtils.animate(targetSlider, oldSlider, .1)
                );

                float sliderY = (settingY + 18);
                RenderUtils.drawCustomShapeWithRadius(
                        x + 5, sliderY, totalSliderWidth, 3, 1.5f,
                        DrRenderUtils.applyOpacity(darkRectHover, (float) (.4f + (.2 * hoverAnimation.getOutput())))
                );
                RenderUtils.drawCustomShapeWithRadius(
                        x + 5, sliderY, Math.max(4, sliderintMap.get(numberSetting)), 3, 1.5f,
                        accent ? accentedColor2 : textColor
                );

                DrRenderUtils.setAlphaLimit(0);
                DrRenderUtils.fakeCircleGlow(
                        x + 4 + Math.max(4, sliderintMap.get(numberSetting)),
                        sliderY + 1.5f, 6, Color.BLACK, .3f
                );
                DrRenderUtils.drawGoodCircle(
                        x + 4 + Math.max(4, sliderintMap.get(numberSetting)),
                        sliderY + 1.5f, 3.75f,
                        accent ? accentedColor2.getRGB() : textColor.getRGB()
                );

                count += .5f;
            }

            // ----- NumberValue -----
            if (setting instanceof NumberValue) {
                NumberValue numberSetting = (NumberValue) setting;
                String value = Float.toString(
                        (float) MathExtensionsKt.round(numberSetting.getValue(), numberSetting.getInc())
                );

                float regularFontWidth = (float) Fonts.SF.SF_18.SF_18.stringWidth(numberSetting.getName() + ": ");
                float valueFontWidth = (float) Fonts.SF.SF_18.SF_18.stringWidth(value);

                float titleX = x + width / 2f - (regularFontWidth + valueFontWidth) / 2f;
                float titleY = settingY + Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight)
                        - Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight) / 2f + 1;

                GlStateManager.color(1, 1, 1, 1);
                Fonts.SF.SF_18.SF_18.drawString(numberSetting.getName() + ": ", titleX, titleY, textColor.getRGB());
                Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.drawString(value, titleX + regularFontWidth, titleY, textColor.getRGB());

                Animation hoverAnimation = sliderAnimMap.get(numberSetting)[0];
                Animation selectAnimtion = sliderAnimMap.get(numberSetting)[1];

                float totalSliderWidth = width - 10;
                boolean hoveringSlider = isClickable(settingY + 17)
                        && DrRenderUtils.isHovering(x + 5, settingY + 17, totalSliderWidth, 6, mouseX, mouseY);

                if (type == GuiEvents.RELEASE) {
                    draggingNumber = null;
                }
                hoverAnimation.setDirection(
                        hoveringSlider || draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS
                );
                selectAnimtion.setDirection(
                        draggingNumber == numberSetting ? Direction.FORWARDS : Direction.BACKWARDS
                );

                if (type == GuiEvents.CLICK && hoveringSlider && button == 0) {
                    draggingNumber = numberSetting;
                }

                double currentValue = numberSetting.getValue();
                if (draggingNumber != null && draggingNumber == setting) {
                    float percent = Math.min(1, Math.max(0, (mouseX - (x + 5)) / totalSliderWidth));
                    double newValue = (percent * (numberSetting.getMaximum() - numberSetting.getMinimum()))
                            + numberSetting.getMinimum();
                    numberSetting.setValue(newValue);
                }

                float sliderMath = (float) ((currentValue - numberSetting.getMinimum())
                        / (numberSetting.getMaximum() - numberSetting.getMinimum()));

                float oldSlider = sliderMap.get(numberSetting);
                float targetSlider = totalSliderWidth * sliderMath;
                sliderMap.put(
                        numberSetting,
                        (float) DrRenderUtils.animate(targetSlider, oldSlider, .1)
                );

                float sliderY = (settingY + 18);
                RenderUtils.drawCustomShapeWithRadius(
                        x + 5, sliderY, totalSliderWidth, 3, 1.5f,
                        DrRenderUtils.applyOpacity(darkRectHover, (float) (.4f + (.2 * hoverAnimation.getOutput())))
                );
                RenderUtils.drawCustomShapeWithRadius(
                        x + 5, sliderY, Math.max(4, sliderMap.get(numberSetting)), 3, 1.5f,
                        accent ? accentedColor2 : textColor
                );

                DrRenderUtils.setAlphaLimit(0);
                DrRenderUtils.fakeCircleGlow(
                        x + 4 + Math.max(4, sliderMap.get(numberSetting)),
                        sliderY + 1.5f, 6, Color.BLACK, .3f
                );
                DrRenderUtils.drawGoodCircle(
                        x + 4 + Math.max(4, sliderMap.get(numberSetting)),
                        sliderY + 1.5f, 3.75f,
                        accent ? accentedColor2.getRGB() : textColor.getRGB()
                );

                count += .5f;
            }

            // ----- BoolValue -----
            if (setting instanceof BoolValue) {
                BoolValue booleanSetting = (BoolValue) setting;
                Animation toggleAnim = this.toggleAnimation.get(booleanSetting)[0];
                Animation hoverAnim = this.toggleAnimation.get(booleanSetting)[1];

                DrRenderUtils.resetColor();
                OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
                GlStateManager.enableBlend();

                Fonts.SF.SF_18.SF_18.drawString(
                        booleanSetting.getName(),
                        (int) MathExtensionsKt.roundToHalf(x + 4),
                        settingY + 5,
                        textColor.getRGB()
                );

                float switchWidth = 16;
                boolean hoveringSwitch = isClickable(settingY + Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight) - 1)
                        && DrRenderUtils.isHovering(
                        x + width - (switchWidth + 6),
                        settingY + Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight) - 1,
                        switchWidth, 8, mouseX, mouseY
                );

                hoverAnim.setDirection(hoveringSwitch ? Direction.FORWARDS : Direction.BACKWARDS);

                if (type == GuiEvents.CLICK && hoveringSwitch && button == 0) {
                    booleanSetting.toggle();
                }

                toggleAnim.setDirection(booleanSetting.get() ? Direction.FORWARDS : Direction.BACKWARDS);
                DrRenderUtils.resetColor();

                Color accentCircle = accent
                        ? DrRenderUtils.applyOpacity(accentedColor, .8f)
                        : DrRenderUtils.darker(textColor, .8f);

                RenderUtils.drawCustomShapeWithRadius(
                        x + width - (switchWidth + 5.5f),
                        settingY + Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight) + 2,
                        switchWidth, 4.5f, 2,
                        DrRenderUtils.interpolateColorC(
                                DrRenderUtils.applyOpacity(darkRectHover, .5f),
                                accentCircle, (float) toggleAnim.getOutput()
                        )
                );

                DrRenderUtils.fakeCircleGlow(
                        (float) ((x + width - (switchWidth + 3))
                                + ((switchWidth - 5) * toggleAnim.getOutput())),
                        settingY + Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight) + 4,
                        6, Color.BLACK, .3f
                );

                DrRenderUtils.resetColor();
                RenderUtils.drawCustomShapeWithRadius(
                        (float) (x + width - (switchWidth + 6) + ((switchWidth - 5) * toggleAnim.getOutput())),
                        settingY + Fonts.SF.SF_18.SF_18.getMiddleOfBox(rectHeight) + 1,
                        6.5f, 6.5f, 3, textColor
                );
            }

            // ----- ListValue -----
            if (setting instanceof ListValue) {
                ListValue modeSetting = (ListValue) setting;
                Animation hoverAnim = modeSettingAnimMap.get(modeSetting)[0];
                Animation openAnim = modeSettingAnimMap.get(modeSetting)[1];

                boolean hoveringModeRect = isClickable(settingY + 5)
                        && DrRenderUtils.isHovering(x + 5, settingY + 5, width - 10, rectHeight + 7, mouseX, mouseY);

                if (type == GuiEvents.CLICK && hoveringModeRect && button == 1) {
                    modeSettingClick.put(modeSetting, !modeSettingClick.get(modeSetting));
                }
                hoverAnim.setDirection(hoveringModeRect ? Direction.FORWARDS : Direction.BACKWARDS);
                openAnim.setDirection(modeSettingClick.get(modeSetting) ? Direction.FORWARDS : Direction.BACKWARDS);

                float math = (modeSetting.getValues().length - 1) * rectHeight;
                RenderUtils.drawCustomShapeWithRadius(
                        x + 5,
                        (float) (settingY + rectHeight + 2 + (12 * openAnim.getOutput())),
                        width - 10,
                        (float) (math * openAnim.getOutput()),
                        3,
                        DrRenderUtils.applyOpacity(darkRectHover, (float) (.35f * openAnim.getOutput()))
                );

                if (!openAnim.isDone() && type == GuiEvents.DRAW) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                    DrRenderUtils.scissor(
                            x + 5,
                            (float) (settingY + 7 + rectHeight + (3 * openAnim.getOutput())),
                            width - 10,
                            (float) (math * openAnim.getOutput())
                    );
                }

                float modeCount = 0;
                for (String mode : modeSetting.getValues()) {
                    if (mode.equalsIgnoreCase(modeSetting.get())) continue;

                    float modeY = (float) (
                            settingY + rectHeight + 11
                                    + ((8 + (modeCount * rectHeight)) * openAnim.getOutput())
                    );
                    DrRenderUtils.resetColor();

                    boolean hoveringMode = isClickable(modeY - 5)
                            && openAnim.getDirection().equals(Direction.FORWARDS)
                            && DrRenderUtils.isHovering(x + 5, modeY - 5, width - 10, rectHeight, mouseX, mouseY);

                    Animation modeHover = modesHoverAnimation.get(modeSetting).get(mode);
                    modeHover.setDirection(hoveringMode ? Direction.FORWARDS : Direction.BACKWARDS);

                    if (modeHover.finished(Direction.FORWARDS) || !modeHover.isDone()) {
                        RenderUtils.drawCustomShapeWithRadius(
                                x + 5, modeY - 5, width - 10, rectHeight, 3,
                                DrRenderUtils.applyOpacity(textColor, (float) (.2f * modeHover.getOutput()))
                        );
                    }

                    if (type == GuiEvents.CLICK && button == 0 && hoveringMode) {
                        modeSettingClick.put(modeSetting, !modeSettingClick.get(modeSetting));
                        modeSetting.set(mode, true);
                    }
                    if (openAnim.isDone() && openAnim.getDirection().equals(Direction.FORWARDS) || !openAnim.isDone()) {
                        Fonts.SF.SF_18.SF_18.drawString(
                                mode,
                                x + 13,
                                modeY,
                                DrRenderUtils.applyOpacity(textColor, (float) openAnim.getOutput()).getRGB()
                        );
                    }
                    modeCount++;
                }

                if (!openAnim.isDone() && type == GuiEvents.DRAW) {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                }
                if (settingHeightScissor.isDone()
                        && openAnim.isDone()
                        && GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                }

                RenderUtils.drawCustomShapeWithRadius(
                        x + 5, settingY + 5, width - 10, rectHeight + 7, 3,
                        DrRenderUtils.applyOpacity(darkRectHover, .45f)
                );

                if (!hoverAnim.isDone() || hoverAnim.finished(Direction.FORWARDS)) {
                    RenderUtils.drawCustomShapeWithRadius(
                            x + 5, settingY + 5, width - 10, rectHeight + 7, 3,
                            DrRenderUtils.applyOpacity(textColor, (float) (.2f * hoverAnim.getOutput()))
                    );
                }

                float selectRectWidth = (float) ((width - 10) * openAnim.getOutput());
                if (openAnim.isDone() && openAnim.getDirection().equals(Direction.FORWARDS)
                        || !openAnim.isDone()) {
                    RenderUtils.drawCustomShapeWithRadius(
                            x + 5 + ((width - 10) / 2f - selectRectWidth / 2f),
                            settingY + rectHeight + 10.5f,
                            Math.max(2, selectRectWidth), 1.5f, .5f,
                            accent ? accentedColor2 : textColor
                    );
                }

                Fonts.SF.SF_14.SF_14.drawString(
                        modeSetting.getName(),
                        x + 13,
                        settingY + 9,
                        textColor.getRGB()
                );

                DrRenderUtils.resetColor();
                Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.drawString(
                        modeSetting.get(),
                        x + 13,
                        (float) (settingY + 17.5),
                        textColor.getRGB()
                );

                DrRenderUtils.resetColor();
                DrRenderUtils.drawClickGuiArrow(
                        x + width - 15,
                        settingY + 17,
                        5,
                        openAnim,
                        textColor.getRGB()
                );

                count += 1 + ((math / rectHeight) * openAnim.getOutput());
            }

            // ----- TextValue -----
            if (setting instanceof TextValue) {
                TextValue stringSetting = (TextValue) setting;

                DrRenderUtils.resetColor();
                Fonts.SF.SF_16.SF_16.drawString(
                        stringSetting.getName(),
                        x + 5,
                        settingY + 2,
                        textColor.getRGB()
                );

                // Create the PasswordField (which might just be a text box in your code)
                PasswordField stringSettingField = new PasswordField(
                        "Type Here...",
                        0,
                        (int) (x + 5),
                        (int) (settingY + 15),
                        (int) (width - 10),
                        10,
                        Fonts.SF.SF_18.SF_18
                );

                // Use renamed methods to avoid ambiguous calls:
                // (Assuming PasswordField was updated to have updateText(...) and updateTextColor(...))
                stringSettingField.updateText(stringSetting.get());
                stringSettingField.setFocused(selectedStringSetting == stringSetting);
                stringSettingField.setBottomBarColor(textColor.getRGB());
                stringSettingField.updateTextColor(textColor.getRGB());
                stringSettingField.setPlaceHolderTextX(x + 30);

                if (type == GuiEvents.CLICK) {
                    stringSettingField.mouseClicked(mouseX, mouseY, button);
                }
                if (stringSettingField.isFocused()) {
                    selectedField = stringSettingField;
                    selectedStringSetting = stringSetting;
                } else if (selectedStringSetting == stringSetting) {
                    selectedStringSetting = null;
                    selectedField = null;
                }

                stringSettingField.drawTextBox();
                // Reflect any changes back to the actual setting
                stringSetting.set(stringSettingField.getTextValue(), true);

                count++;
            }

            // Render the key bind
            String bind = Keyboard.getKeyName(module.getKeyBind());
            boolean hoveringBindRect = isClickable(
                    y + Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.getMiddleOfBox(rectHeight) - 1
            ) && DrRenderUtils.isHovering(
                    x + width - (Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.stringWidth(bind) + 10),
                    y + Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.getMiddleOfBox(rectHeight) - 1,
                    (float) (Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.stringWidth(bind) + 8),
                    Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.getHeight() + 6,
                    mouseX, mouseY
            );

            if (type == GuiEvents.CLICK && hoveringBindRect && button == 0) {
                binding = module;
                return;
            }

            Animation[] animations = keySettingAnimMap.get(module);
            animations[1].setDirection(binding == module ? Direction.FORWARDS : Direction.BACKWARDS);
            animations[0].setDirection(hoveringBindRect ? Direction.FORWARDS : Direction.BACKWARDS);

            // (Any extra code for rendering the bind rectangle is commented out below)
            /*
             int offsetX = 10;
             float bindButtonY = y + 4; // Adjust as needed

             RoundedUtil.drawRound(
                 x + width - (Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.stringWidth(bind) + 12) + offsetX,
                 bindButtonY,
                 (float) (Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.stringWidth(bind) + 8),
                 Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.getHeight() + 6,
                 5,
                 DrRenderUtils.applyOpacity(darkRectHover, (float) (.4 + (.2 * animations[0].getOutput())))
             );

             Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.drawString(
                 bind,
                 x + width - (Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.stringWidth(bind) + 9) + offsetX,
                 bindButtonY + Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.getMiddleOfBox(rectHeight) + 1,
                 DrRenderUtils.interpolateColor(
                     textColor.getRGB(),
                     accentedColor2.getRGB(),
                     (float) animations[1].getOutput()
                 )
             );
            */
            count++;
        }
        settingSize = count;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        handle(mouseX, mouseY, -1, GuiEvents.DRAW);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        handle(mouseX, mouseY, button, GuiEvents.CLICK);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        handle(mouseX, mouseY, state, GuiEvents.RELEASE);
    }

    /**
     * Returns whether we can safely interact with a setting at the given y-position,
     * preventing clicks from “spilling over” the visible region.
     */
    public boolean isClickable(float y) {
        return y > panelLimitY && y < panelLimitY + 17 + Main.allowedClickGuiHeight;
    }
}