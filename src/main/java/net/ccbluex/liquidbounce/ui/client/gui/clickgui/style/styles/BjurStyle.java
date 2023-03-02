/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles;

import java.awt.Color;
import java.io.IOException;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.value.*;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.Colors;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class BjurStyle extends GuiScreen {
    boolean previousmouse = true;

    public float moveX = 0.0F;

    public float moveY = 0.0F;

    boolean bind = false;

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        sr = new ScaledResolution(this.mc);
        if (alpha < 255)
            alpha += 5;
        if (this.hue > 255.0F)
            this.hue = 0.0F;
        float hue = this.hue;
        float h2 = this.hue + 85.0F;
        float h3 = this.hue + 170.0F;
        if (hue > 255.0F)
            hue = 0.0F;
        if (h2 > 255.0F)
            h2 -= 255.0F;
        if (h3 > 255.0F)
            h3 -= 255.0F;
        Color color33 = Color.getHSBColor(hue / 255.0F, 0.9F, 1.0F);
        Color color34 = Color.getHSBColor(h2 / 255.0F, 0.9F, 1.0F);
        Color color35 = Color.getHSBColor(h3 / 255.0F, 0.9F, 1.0F);
        int color36 = color33.getRGB();
        int color37 = color34.getRGB();
        int color38 = color35.getRGB();
        int color39 = (new Color(ClickGUIModule.colorRedValue.get(), ClickGUIModule.colorGreenValue.get(), ClickGUIModule.colorBlueValue.get(), alpha)).getRGB();
        this.hue += 0.1F;
        RenderUtils.rectangleBordered(startX, startY, (startX + 450.0F), (startY + 350.0F), 0.01D, Colors.getColor(90, alpha), Colors.getColor(0, alpha));
        RenderUtils.rectangleBordered((startX + 1.0F), (startY + 1.0F), (startX + 450.0F - 1.0F), (startY + 350.0F - 1.0F), 1.0D, Colors.getColor(90, alpha), Colors.getColor(61, alpha));
        RenderUtils.rectangleBordered(startX + 2.5D, startY + 2.5D, (startX + 450.0F) - 2.5D, (startY + 350.0F) - 2.5D, 0.01D, Colors.getColor(61, alpha), Colors.getColor(0, alpha));
        RenderUtils.rectangleBordered((startX + 3.0F), (startY + 3.0F), (startX + 450.0F - 3.0F), (startY + 350.0F - 3.0F), 0.01D, Colors.getColor(27, alpha), Colors.getColor(61, alpha));
        if (alpha >= 55) {
            RenderUtils.drawGradientSideways((startX + 3.0F), (startY + 3.0F), (startX + 225.0F), startY + 3.6D, color36, color37);
            RenderUtils.drawGradientSideways((startX + 225.0F), (startY + 3.0F), (startX + 450.0F - 3.0F), startY + 3.6D, color37, color38);
        }
        RenderUtils.drawRect(startX + 98.0F, startY + 48.0F, startX + 432.0F, startY + 318.0F, (new Color(30, 30, 30, alpha)).getRGB());
        RenderUtils.drawRect(startX + 100.0F, startY + 50.0F, startX + 430.0F, startY + 315.0F, (new Color(35, 35, 35, alpha)).getRGB());
        RenderUtils.drawRect(startX + 200.0F, startY + 50.0F, startX + 430.0F, startY + 315.0F, (new Color(37, 37, 37, alpha)).getRGB());
        RenderUtils.drawRect(startX + 202.0F, startY + 50.0F, startX + 430.0F, startY + 315.0F, (new Color(40, 40, 40, alpha)).getRGB());
        Fonts.font40.drawCenteredString("LA MIERDA", startX + 50.0F, startY + 12.0F, (new Color(255, 255, 255, alpha)).getRGB());
        Fonts.font35.drawCenteredString("ClickGUI", startX + 50.0F, startY + 32.0F, color39);
        Fonts.font35.drawString("", startX + 15.0F, startY + 330.0F, (new Color(180, 180, 180, alpha)).getRGB());
        int dWheel = Mouse.getDWheel();
        if (isCategoryHovered(startX + 100.0F, startY + 40.0F, startX + 200.0F, startY + 315.0F, mouseX, mouseY)) {
            if (dWheel < 0 && moduleStart < LiquidBounce.moduleManager.getModuleInCategory(currentModuleType).size() - 1)
                moduleStart++;
            if (dWheel > 0 && moduleStart > 0)
                moduleStart--;
        }
        if (isCategoryHovered(startX + 200.0F, startY + 50.0F, startX + 430.0F, startY + 315.0F, mouseX, mouseY)) {
            if (dWheel < 0 && valueStart < currentModule.getValues().size() - 1)
                valueStart++;
            if (dWheel > 0 && valueStart > 0)
                valueStart--;
        }
        float mY = startY + 12.0F;
        for (int i = 0; i < LiquidBounce.moduleManager.getModuleInCategory(currentModuleType).size(); i++) {
            Module module = LiquidBounce.moduleManager.getModuleInCategory(currentModuleType).get(i);
            if (mY > startY + 250.0F)
                break;
            if (i >= moduleStart) {
                if (!module.getState()) {
                    RenderUtils.drawRect(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, isSettingsButtonHovered(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, mouseX, mouseY) ? (new Color(60, 60, 60, alpha)).getRGB() : (new Color(35, 35, 35, alpha)).getRGB());
                    RenderUtils.drawFilledCircle((startX + (isSettingsButtonHovered(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, mouseX, mouseY) ? 112 : 110)), (mY + 58.0F), 3.0D, (new Color(70, 70, 70, alpha)).getRGB(), 5);
                    Fonts.font35.drawString(module.getName(), startX + (isSettingsButtonHovered(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, mouseX, mouseY) ? 122 : 120), mY + 55.0F, (new Color(175, 175, 175, alpha)).getRGB());
                } else {
                    RenderUtils.drawRect(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, isSettingsButtonHovered(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, mouseX, mouseY) ? (new Color(60, 60, 60, alpha)).getRGB() : (new Color(35, 35, 35, alpha)).getRGB());
                    RenderUtils.drawFilledCircle((startX + (isSettingsButtonHovered(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, mouseX, mouseY) ? 112 : 110)), (mY + 58.0F), 3.0D, (new Color(100, 255, 100, alpha)).getRGB(), 5);
                    Fonts.font35.drawString(module.getName(), startX + (isSettingsButtonHovered(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, mouseX, mouseY) ? 122 : 120), mY + 55.0F, (new Color(255, 255, 255, alpha)).getRGB());
                }
                if (isSettingsButtonHovered(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, mouseX, mouseY)) {
                    if (!this.previousmouse && Mouse.isButtonDown(0)) {
                        if (module.getState()) {
                            module.setState(false);
                        } else {
                            module.setState(true);
                        }
                        this.previousmouse = true;
                    }
                    if (!this.previousmouse && Mouse.isButtonDown(1))
                        this.previousmouse = true;
                }
                if (!Mouse.isButtonDown(0))
                    this.previousmouse = false;
                if (isSettingsButtonHovered(startX + 100.0F, mY + 45.0F, startX + 200.0F, mY + 70.0F, mouseX, mouseY) && Mouse.isButtonDown(1)) {
                    for (int j = 0; j < currentModule.getValues().size(); j++) {
                        //  if (value instanceof BoolValue)
                       //     ((BoolValue)value).setAnim(55.0F);
                    }
                    currentModule = module;
                    valueStart = 0;
                }
                mY += 25.0F;
            }
        }
        mY = startY + 12.0F;
        GameFontRenderer font = Fonts.font35;
        for (int k = 0; k < currentModule.getValues().size() && mY <= startY + 250.0F; k++) {
            if (k >= valueStart) {
                Value value2 = currentModule.getValues().get(k);
                if (value2 instanceof TextValue) {
                    TextValue textValue = (TextValue)value2;
                    Fonts.font40.drawString(textValue.getName() + ": " + textValue.get(), startX + 220.0F, mY + 50.0F, (new Color(175, 175, 175, alpha)).getRGB());
                    mY += 20.0F;
                }
                if (value2 instanceof BlockValue) {
                    BlockValue blockValue = (BlockValue)value2;
                    Fonts.font40.drawString(blockValue.getName() + ": " + blockValue.get(), startX + 220.0F, mY + 50.0F, (new Color(175, 175, 175, alpha)).getRGB());
                    mY += 20.0F;
                }
                if (value2 instanceof FontValue) {
                    FontValue fontValue = (FontValue)value2;
                    Fonts.font40.drawString(fontValue.getName() + ": " + fontValue.get(), startX + 220.0F, mY + 50.0F, (new Color(175, 175, 175, alpha)).getRGB());
                    mY += 20.0F;
                }
                if (value2 instanceof IntegerValue) {
                    IntegerValue floatValue = (IntegerValue)value2;
                    float x = startX + 320.0F;
                    double render = (68.0F * (floatValue.getValue() - floatValue.getMinimum()) / (floatValue.getMaximum() - floatValue.getMinimum()));
                    RenderUtils.drawRect(x + 2.0F, mY + 52.0F, (float)(x + 75.0D), mY + 53.0F, (isButtonHovered(x, mY + 45.0F, x + 100.0F, mY + 57.0F, mouseX, mouseY) && Mouse.isButtonDown(0)) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(30, 30, 30, alpha)).getRGB());
                    RenderUtils.drawRect(x + 2.0F, mY + 52.0F, (float)(x + render + 6.5D), mY + 53.0F, (new Color(35, 35, 255, alpha)).getRGB());
                    RenderUtils.drawFilledCircle(((float)(x + render + 2.0D) + 3.0F), mY + 52.25D, 1.5D, (new Color(35, 35, 255, alpha)).getRGB(), 5);
                    Fonts.font40.drawString(floatValue.getName(), startX + 220.0F, mY + 50.0F, (new Color(175, 175, 175, alpha)).getRGB());
                    Fonts.font40.drawString(floatValue.getValue().toString(), startX + 320.0F - font.getStringWidth(value2.getValue().toString()), mY + 50.0F, (new Color(255, 255, 255, alpha)).getRGB());
                    if (!Mouse.isButtonDown(0))
                        this.previousmouse = false;
                    if (isButtonHovered(x, mY + 45.0F, x + 100.0F, mY + 57.0F, mouseX, mouseY) && Mouse.isButtonDown(0)) {
                        if (!this.previousmouse && Mouse.isButtonDown(0)) {
                            render = floatValue.getMinimum();
                            double max = floatValue.getMaximum();
                            double valAbs = mouseX - x + 1.0D;
                            double perc = valAbs / 68.0D;
                            perc = Math.min(Math.max(0.0D, perc), 1.0D);
                            double valRel = (max - render) * perc;
                            float val = (float)(render + valRel);
                            val = (float)(Math.round(val * 1.0D) / 1.0D);
                        }
                        if (!Mouse.isButtonDown(0))
                            this.previousmouse = false;
                    }
                    mY += 20.0F;
                }
                if (value2 instanceof FloatValue) {
                    FloatValue floatValue2 = (FloatValue)value2;
                    float x = startX + 320.0F;
                    double render = (68.0F * (floatValue2.getValue() - floatValue2.getMinimum()) / (floatValue2.getMaximum() - floatValue2.getMinimum()));
                    RenderUtils.drawRect(x + 2.0F, mY + 52.0F, (float)(x + 75.0D), mY + 53.0F, (isButtonHovered(x, mY + 45.0F, x + 100.0F, mY + 57.0F, mouseX, mouseY) && Mouse.isButtonDown(0)) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(30, 30, 30, alpha)).getRGB());
                    RenderUtils.drawRect(x + 2.0F, mY + 52.0F, (float)(x + render + 6.5D), mY + 53.0F, (new Color(35, 35, 255, alpha)).getRGB());
                    RenderUtils.drawFilledCircle(((float)(x + render + 2.0D) + 3.0F), mY + 52.25D, 1.5D, (new Color(35, 35, 255, alpha)).getRGB(), 5);
                    Fonts.font40.drawString(floatValue2.getName(), startX + 220.0F, mY + 50.0F, (new Color(175, 175, 175, alpha)).getRGB());
                    Fonts.font40.drawString(floatValue2.getValue().toString(), startX + 320.0F - font.getStringWidth(value2.getValue().toString()), mY + 50.0F, (new Color(255, 255, 255, alpha)).getRGB());
                    if (!Mouse.isButtonDown(0))
                        this.previousmouse = false;
                    if (isButtonHovered(x, mY + 45.0F, x + 100.0F, mY + 57.0F, mouseX, mouseY) && Mouse.isButtonDown(0)) {
                        if (!this.previousmouse && Mouse.isButtonDown(0)) {
                            render = floatValue2.getMinimum();
                            double max = floatValue2.getMaximum();
                            double valAbs = mouseX - x + 1.0D;
                            double perc = valAbs / 68.0D;
                            perc = Math.min(Math.max(0.0D, perc), 1.0D);
                            double valRel = (max - render) * perc;
                            float val = (float)(render + valRel);
                            val = (float)(Math.round(val * 100.00000223517424D) / 100.00000223517424D);
                            floatValue2.setValue(val);
                        }
                        if (!Mouse.isButtonDown(0))
                            this.previousmouse = false;
                    }
                    mY += 20.0F;
                }
                if (value2 instanceof BoolValue) {
                    BoolValue boolValue = (BoolValue)value2;
                    float x = startX + 320.0F;
                    Fonts.font40.drawString(boolValue.getName(), startX + 220.0F, mY + 50.0F, (new Color(175, 175, 175, alpha)).getRGB());
                    if (boolValue.getValue()) {
                        RenderUtils.drawRect(x + 50.0F, mY + 50.0F, x + 65.0F, mY + 59.0F, isCheckBoxHovered(x + 50.0F - 5.0F, mY + 50.0F, x + 65.0F + 6.0F, mY + 59.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(20, 20, 20, alpha)).getRGB());
                        RenderUtils.drawFilledCircle((x + 50.0F), mY + 54.5D, 4.5D, isCheckBoxHovered(x + 50.0F - 5.0F, mY + 50.0F, x + 65.0F + 6.0F, mY + 59.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(20, 20, 20, alpha)).getRGB(), 10);
                        RenderUtils.drawFilledCircle((x + 65.0F), mY + 54.5D, 4.5D, isCheckBoxHovered(x + 50.0F - 5.0F, mY + 50.0F, x + 65.0F + 6.0F, mY + 59.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(20, 20, 20, alpha)).getRGB(), 10);
                        RenderUtils.drawFilledCircle((x + 65.0F), mY + 54.5D, 5.0D, (new Color(35, 35, 255, alpha)).getRGB(), 10);
                    } else {
                        RenderUtils.drawRect(x + 50.0F, mY + 50.0F, x + 65.0F, mY + 59.0F, isCheckBoxHovered(x + 50.0F - 5.0F, mY + 50.0F, x + 65.0F + 6.0F, mY + 59.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(20, 20, 20, alpha)).getRGB());
                        RenderUtils.drawFilledCircle((x + 50.0F), mY + 54.5D, 4.5D, isCheckBoxHovered(x + 50.0F - 5.0F, mY + 50.0F, x + 65.0F + 6.0F, mY + 59.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(20, 20, 20, alpha)).getRGB(), 10);
                        RenderUtils.drawFilledCircle((x + 65.0F), mY + 54.5D, 4.5D, isCheckBoxHovered(x + 50.0F - 5.0F, mY + 50.0F, x + 65.0F + 6.0F, mY + 59.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(20, 20, 20, alpha)).getRGB(), 10);
                        RenderUtils.drawFilledCircle((x + 50.0F), mY + 54.5D, 5.0D, (new Color(56, 56, 56, alpha)).getRGB(), 10);
                    }
                    if (isCheckBoxHovered(x + 50.0F - 5.0F, mY + 50.0F, x + 65.0F + 6.0F, mY + 59.0F, mouseX, mouseY)) {
                        if (!this.previousmouse && Mouse.isButtonDown(0)) {
                            this.previousmouse = true;
                            this.mouse = true;
                        }
                        if (this.mouse) {
                            boolValue.setValue(!(Boolean) boolValue.getValue());
                            this.mouse = false;
                        }
                    }
                    if (!Mouse.isButtonDown(0))
                        this.previousmouse = false;
                    mY += 20.0F;
                }
                if (value2 instanceof ListValue) {
                    ListValue listValue = (ListValue)value2;
                    float x = startX + 320.0F;
                    Fonts.font40.drawString(listValue.getName(), startX + 220.0F, mY + 52.0F, (new Color(175, 175, 175, alpha)).getRGB());
                    RenderUtils.drawRect(x + 5.0F, mY + 45.0F, x + 75.0F, mY + 65.0F, isStringHovered(x, mY + 45.0F, x + 75.0F, mY + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB());
                    RenderUtils.drawRect(x + 2.0F, mY + 48.0F, x + 78.0F, mY + 62.0F, isStringHovered(x, mY + 45.0F, x + 75.0F, mY + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB());
                    RenderUtils.drawFilledCircle((x + 5.0F), (mY + 48.0F), 3.0D, isStringHovered(x, mY + 45.0F, x + 75.0F, mY + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB(), 5);
                    RenderUtils.drawFilledCircle((x + 5.0F), (mY + 62.0F), 3.0D, isStringHovered(x, mY + 45.0F, x + 75.0F, mY + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB(), 5);
                    RenderUtils.drawFilledCircle((x + 75.0F), (mY + 48.0F), 3.0D, isStringHovered(x, mY + 45.0F, x + 75.0F, mY + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB(), 5);
                    RenderUtils.drawFilledCircle((x + 75.0F), (mY + 62.0F), 3.0D, isStringHovered(x, mY + 45.0F, x + 75.0F, mY + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB(), 5);
                    Fonts.font40.drawString(listValue.get(), x + 40.0F - (font.getStringWidth(listValue.get()) / 2F), mY + 53.0F, (new Color(255, 255, 255, alpha)).getRGB());
                    if (isStringHovered(x, mY + 45.0F, x + 75.0F, mY + 65.0F, mouseX, mouseY) && Mouse.isButtonDown(0) && !this.previousmouse) {
                        if ((listValue.getValues()).length <= listValue.getModeListNumber(listValue.get()) + 1) {
                            listValue.set(listValue.getValues()[0]);
                        } else {
                            listValue.set(listValue.getValues()[listValue.getModeListNumber(listValue.get()) + 1]);
                        }
                        this.previousmouse = true;
                    }
                    mY += 25.0F;
                }
            }
        }
        float x2 = startX + 320.0F;
        float yyy = startY + 240.0F;
        Fonts.font40.drawString("Bind", startX + 220.0F, yyy + 50.0F, (new Color(170, 170, 170, alpha)).getRGB());
        RenderUtils.drawRect(x2 + 5.0F, yyy + 45.0F, x2 + 75.0F, yyy + 65.0F, isHovered(x2 + 2.0F, yyy + 45.0F, x2 + 78.0F, yyy + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB());
        RenderUtils.drawRect(x2 + 2.0F, yyy + 48.0F, x2 + 78.0F, yyy + 62.0F, isHovered(x2 + 2.0F, yyy + 45.0F, x2 + 78.0F, yyy + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB());
        RenderUtils.drawFilledCircle((x2 + 5.0F), (yyy + 48.0F), 3.0D, isHovered(x2 + 2.0F, yyy + 45.0F, x2 + 78.0F, yyy + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB(), 5);
        RenderUtils.drawFilledCircle((x2 + 5.0F), (yyy + 62.0F), 3.0D, isHovered(x2 + 2.0F, yyy + 45.0F, x2 + 78.0F, yyy + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB(), 5);
        RenderUtils.drawFilledCircle((x2 + 75.0F), (yyy + 48.0F), 3.0D, isHovered(x2 + 2.0F, yyy + 45.0F, x2 + 78.0F, yyy + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB(), 5);
        RenderUtils.drawFilledCircle((x2 + 75.0F), (yyy + 62.0F), 3.0D, isHovered(x2 + 2.0F, yyy + 45.0F, x2 + 78.0F, yyy + 65.0F, mouseX, mouseY) ? (new Color(80, 80, 80, alpha)).getRGB() : (new Color(56, 56, 56, alpha)).getRGB(), 5);
        Fonts.font40.drawString(Keyboard.getKeyName(currentModule.getKeyBind()), x2 + 40.0F - (font.getStringWidth(Keyboard.getKeyName(currentModule.getKeyBind())) / 2F), yyy + 53.0F, (new Color(255, 255, 255, alpha)).getRGB());
        if ((isHovered(startX, startY, startX + 450.0F, startY + 50.0F, mouseX, mouseY) || isHovered(startX, startY + 315.0F, startX + 450.0F, startY + 350.0F, mouseX, mouseY) || isHovered(startX + 430.0F, startY, startX + 450.0F, startY + 350.0F, mouseX, mouseY)) && Mouse.isButtonDown(0)) {
            if (this.moveX == 0.0F && this.moveY == 0.0F) {
                this.moveX = mouseX - startX;
                this.moveY = mouseY - startY;
            } else {
                startX = mouseX - this.moveX;
                startY = mouseY - this.moveY;
            }
            this.previousmouse = true;
        } else if (this.moveX != 0.0F || this.moveY != 0.0F) {
            this.moveX = 0.0F;
            this.moveY = 0.0F;
        }
        if (isHovered((sr.getScaledWidth() / 2F - 40), 0.0F, (sr.getScaledWidth() / 2F + 40), 20.0F, mouseX, mouseY) && Mouse.isButtonDown(0)) {
            startX = (sr.getScaledWidth() / 2F - 225);
            startY = (sr.getScaledHeight() / 2F - 175);
            alpha = 0;
        }
        RenderUtils.drawRect((sr.getScaledWidth() / 2F - 39), 0.0F, (sr.getScaledWidth() / 2F + 39), 19.0F, (new Color(0, 0, 0, alpha / 2)).getRGB());
        RenderUtils.drawRect((sr.getScaledWidth() / 2F - 40), 0.0F, (sr.getScaledWidth() / 2F + 40), 20.0F, (new Color(0, 0, 0, alpha / 2)).getRGB());
        float k2 = startY + 10.0F;
        float xx2 = startX + 5.0F;
        for (int i2 = 0; i2 < (ModuleCategory.values()).length; i2++) {
            ModuleCategory[] iterator = ModuleCategory.values();
            if (iterator[i2] == currentModuleType)
                RenderUtils.drawRect(xx2 + 8.0F, k2 + 12.0F + 60.0F + (i2 * 45), xx2 + 30.0F, k2 + 13.0F + 60.0F + (i2 * 45), color39);
            Fonts.font40.drawString(iterator[i2].toString(), xx2 + (isCategoryHovered(xx2 + 8.0F, k2 - 10.0F + 60.0F + (i2 * 45), xx2 + 80.0F, k2 + 20.0F + 60.0F + (i2 * 45), mouseX, mouseY) ? 27 : 25), k2 + 56.0F + (45 * i2), (new Color(255, 255, 255, alpha)).getRGB());
            try {
                if (isCategoryHovered(xx2 + 8.0F, k2 - 10.0F + 60.0F + (i2 * 45), xx2 + 80.0F, k2 + 20.0F + 60.0F + (i2 * 45), mouseX, mouseY) && Mouse.isButtonDown(0)) {
                    currentModuleType = iterator[i2];
                    currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentModuleType).get(0);
                    moduleStart = 0;
                    valueStart = 0;
                    for (int x3 = 0; x3 < currentModule.getValues().size(); x3++) {
                        // if (value3 instanceof BoolValue)
                        //    ((BoolValue)value3).setAnim(55.0F);
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public void initGui() {
        for (int i = 0; i < currentModule.getValues().size(); i++) {
            // if (value instanceof BoolValue)
           //     ((BoolValue)value).setAnim(55.0F);
        }
        super.initGui();
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (this.bind) {
            currentModule.setKeyBind(keyCode);
            if (keyCode == 1)
                currentModule.setKeyBind(0);
            this.bind = false;
        } else if (keyCode == 1) {
            this.mc.displayGuiScreen((GuiScreen)null);
            ((ClickGUIModule)LiquidBounce.moduleManager.getModule(ClickGUIModule.class)).setState(false);
            if (this.mc.currentScreen == null)
                this.mc.setIngameFocus();
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        float mY = startY + 30.0F;
        for (int i = 0; i < currentModule.getValues().size() && mY <= startY + 350.0F; i++) {
            if (i >= valueStart) {
                Value value = currentModule.getValues().get(i);
                if (value instanceof FloatValue)
                    mY += 20.0F;
                if (value instanceof BoolValue)
                    mY += 20.0F;
                if (value instanceof ListValue)
                    mY += 25.0F;
            }
        }
        float x2 = startX + 320.0F;
        float yyy = startY + 240.0F;
        if (isHovered(x2 + 2.0F, yyy + 45.0F, x2 + 78.0F, yyy + 65.0F, mouseX, mouseY))
            this.bind = true;
        super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isStringHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return (mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2);
    }

    public boolean isSettingsButtonHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2);
    }

    public boolean isButtonHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return (mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2);
    }

    public boolean isCheckBoxHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return (mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2);
    }

    public boolean isCategoryHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2);
    }

    public boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2);
    }

    public void onGuiClosed() {
        alpha = 0;
    }

    public static ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

    public static ModuleCategory currentModuleType = ModuleCategory.COMBAT;

    public static Module currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentModuleType).get(0);

    public static float startX = (sr.getScaledWidth() / 2F - 225);

    public static float startY = (sr.getScaledHeight() / 2F - 175);

    public static int moduleStart = 0;

    public static int valueStart = 0;

    boolean mouse;

    float hue;

    public static int alpha;
}
