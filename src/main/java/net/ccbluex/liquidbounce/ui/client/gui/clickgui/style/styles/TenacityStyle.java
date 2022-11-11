/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles;

import net.ccbluex.liquidbounce.ui.client.gui.clickgui.Panel;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.elements.ButtonElement;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.elements.ModuleElement;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.Style;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager;
import net.ccbluex.liquidbounce.utils.block.BlockUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.*;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class TenacityStyle extends Style {

    private boolean mouseDown;
    private boolean rightMouseDown;

    public static float drawSlider(final float value, final float min, final float max, final int x, final int y, final int width, final int mouseX, final int mouseY, final Color color) {
        final float displayValue = Math.max(min, Math.min(value, max));

        final float sliderValue = (float) x + (float) width * (displayValue - min) / (max - min);

        RenderUtils.drawRect(x, y, x + width, y + 2, Integer.MAX_VALUE);
        RenderUtils.drawRect(x, y, sliderValue, y + 2, color);
        RenderUtils.drawFilledCircle((int) sliderValue, y + 1, 3, color);

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 3 && Mouse.isButtonDown(0)) {
            double i = MathHelper.clamp_double(((double) mouseX - (double) x) / ((double) width - 3), 0, 1);

            BigDecimal bigDecimal = new BigDecimal(Double.toString((min + (max - min) * i)));
            bigDecimal = bigDecimal.setScale(2, 4);
            return bigDecimal.floatValue();
        }

        return value;
    }

    @Override
    public void drawPanel(int mouseX, int mouseY, Panel panel) {
        if (panel.getFade() > 0) {
        RenderUtils.drawGradientRoundedRect( panel.getX(), panel.getY() - 3, panel.getX() + panel.getWidth(), panel.getY() + 26 + panel.getFade(), 7, new Color(241,135,251).getRGB(), new Color(67,156,251).getRGB()); 
        RenderUtils.customRounded((float) panel.getX() + 2, (float) panel.getY() + 17 + panel.getFade(), (float) panel.getX() + panel.getWidth() - 2, (float) panel.getY() + 19 + panel.getFade() + 5, 0f, 0f, 7, 7, new Color(33, 33, 33).getRGB());
        } else {
        RenderUtils.drawGradientRoundedRect( panel.getX(), panel.getY() - 3, panel.getX() + panel.getWidth(), panel.getY() + 18, 7, new Color(241,135,251).getRGB(), new Color(67,156,251).getRGB()); 
        }
        GlStateManager.resetColor();

        float textWidth = Fonts.fontTenacityBold35.getStringWidth("§f" + StringUtils.stripControlCodes(LanguageManager.INSTANCE.get(panel.getName().replaceAll("%",""))));
        Fonts.fontTenacityBold35.drawString(LanguageManager.INSTANCE.get(panel.getName().replaceAll("%","")), (int) (panel.getX() - (textWidth - 100.0F) / 2F), panel.getY() + 7 - 3, Color.WHITE.getRGB());
    }

    @Override
    public void drawDescription(int mouseX, int mouseY, String text) {
        int textWidth = Fonts.font35.getStringWidth(LanguageManager.INSTANCE.get(text.replaceAll("%","")));

        RenderUtils.drawBorderedRect(mouseX + 9, mouseY, mouseX + textWidth + 14, mouseY + Fonts.font35.FONT_HEIGHT + 3, 3F, new Color(33, 33, 33).getRGB(), new Color(33,33,33).getRGB());
        GlStateManager.resetColor();
        Fonts.font35.drawString(LanguageManager.INSTANCE.get(text.replaceAll("%","")), mouseX + 12, mouseY + (Fonts.font35.FONT_HEIGHT / 2), Color.WHITE.getRGB());
    }

    @Override
    public void drawButtonElement(int mouseX, int mouseY, ButtonElement buttonElement) {
        Gui.drawRect(buttonElement.getX() - 1, buttonElement.getY() - 1, buttonElement.getX() + buttonElement.getWidth() + 1, buttonElement.getY() + buttonElement.getHeight() + 1, hoverColor(buttonElement.getColor() != Integer.MAX_VALUE ? new Color(14, 159, 255) : new Color(217,217,217), buttonElement.hoverTime).getRGB());

        GlStateManager.resetColor();

        Fonts.font35.drawString(LanguageManager.INSTANCE.get(buttonElement.getDisplayName().replaceAll("%","")), buttonElement.getX() + 5, buttonElement.getY() + 5, Color.WHITE.getRGB());
    }

    @Override
    public void drawModuleElement(int mouseX, int mouseY, ModuleElement moduleElement) {
        if(moduleElement.getModule().getState() == true){
            Gui.drawRect(moduleElement.getX() + 2, moduleElement.getY() - 1, moduleElement.getX() + moduleElement.getWidth() -2, moduleElement.getY() + moduleElement.getHeight(), hoverColor(new Color(0,0,0,30), moduleElement.hoverTime).getRGB());
        } else {
            Gui.drawRect(moduleElement.getX() +2, moduleElement.getY() - 2, moduleElement.getX() + moduleElement.getWidth() - 2, moduleElement.getY() + moduleElement.getHeight() + 1, hoverColor(new Color(33, 33, 33), moduleElement.hoverTime).getRGB());
        }
        GlStateManager.resetColor();

        float modtextWidth = Fonts.fontTenacity35.getStringWidth(moduleElement.getDisplayName().replaceAll("%",""));
        Fonts.fontTenacity35.drawString(LanguageManager.INSTANCE.get(moduleElement.getDisplayName().replaceAll("%","")), moduleElement.getX() - ((modtextWidth - 100.0F) / 2F), moduleElement.getY() + 5, Color.WHITE.getRGB());

        // Draw settings
        final List<Value<?>> moduleValues = moduleElement.getModule().getValues();

        if (!moduleValues.isEmpty()) {

            if (moduleElement.isShowSettings()) {
                if (moduleElement.getSettingsWidth() > 0F && moduleElement.slowlySettingsYPos > moduleElement.getY() + 6)
                    RenderUtils.drawBorderedRect(moduleElement.getX() + moduleElement.getWidth() + 4, moduleElement.getY() + 6, moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth(), moduleElement.slowlySettingsYPos + 2, 3F, new Color(40,40,40).getRGB(), new Color(40,40,40).getRGB());

                moduleElement.slowlySettingsYPos = moduleElement.getY() + 6;
                for (final Value value : moduleValues) {
                    if(!value.getDisplayable())
                        continue;

                    if (value instanceof BoolValue) {
                        final String text = value.getName();
                        final float textWidth = Fonts.font35.getStringWidth(text);

                        if (moduleElement.getSettingsWidth() < textWidth + 8)
                            moduleElement.setSettingsWidth(textWidth + 8);

                        if (mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= moduleElement.slowlySettingsYPos && mouseY <= moduleElement.slowlySettingsYPos + 12 && Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                            final BoolValue boolValue = (BoolValue) value;

                            boolValue.set(!boolValue.get());
                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        }

                        Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, moduleElement.slowlySettingsYPos + 2, ((BoolValue) value).get() ? Color.WHITE.getRGB() : new Color(160, 160, 160).getRGB());
                        moduleElement.slowlySettingsYPos += 11;
                    } else if (value instanceof ListValue) {
                        final ListValue listValue = (ListValue) value;

                        final String text = value.getName();
                        final float textWidth = Fonts.font35.getStringWidth(text);

                        if (moduleElement.getSettingsWidth() < textWidth + 16)
                            moduleElement.setSettingsWidth(textWidth + 16);
                        Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, moduleElement.slowlySettingsYPos + 2, Color.WHITE.getRGB());
                        Fonts.font35.drawString(listValue.openList ? "-" : "+", (int) (moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() - (listValue.openList ? 5 : 6)), moduleElement.slowlySettingsYPos + 2, Color.WHITE.getRGB());

                        if (mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= moduleElement.slowlySettingsYPos && mouseY <= moduleElement.slowlySettingsYPos + Fonts.font35.FONT_HEIGHT && Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                            listValue.openList = !listValue.openList;
                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        }

                        moduleElement.slowlySettingsYPos += Fonts.font35.FONT_HEIGHT + 1;

                        for (final String valueOfList : listValue.getValues()) {
                            final float textWidth2 = Fonts.font35.getStringWidth("- " + valueOfList);

                            if (moduleElement.getSettingsWidth() < textWidth2 + 12)
                                moduleElement.setSettingsWidth(textWidth2 + 12);

                            if (listValue.openList) {
                                if (mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= moduleElement.slowlySettingsYPos + 2 && mouseY <= moduleElement.slowlySettingsYPos + 14 && Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                                    listValue.set(valueOfList);
                                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                                }

                                GlStateManager.resetColor();
                                Fonts.font35.drawString("- " + valueOfList, moduleElement.getX() + moduleElement.getWidth() + 6, moduleElement.slowlySettingsYPos + 2, listValue.get() != null && listValue.get().equalsIgnoreCase(valueOfList) ? Color.WHITE.getRGB() : new Color(160, 160, 160).getRGB());
                                moduleElement.slowlySettingsYPos += Fonts.font35.FONT_HEIGHT + 1;
                            }
                        }

                        if (!listValue.openList) {
                            moduleElement.slowlySettingsYPos += 1;
                        }
                    } else if (value instanceof FloatValue) {
                        final FloatValue floatValue = (FloatValue) value;
                        final String text = value.getName() + ": " + round(floatValue.get());
                        final float textWidth = Fonts.font35.getStringWidth(text);

                        if (moduleElement.getSettingsWidth() < textWidth + 8)
                            moduleElement.setSettingsWidth(textWidth + 8);

                        final float valueOfSlide = drawSlider(floatValue.get(), floatValue.getMinimum(), floatValue.getMaximum(), moduleElement.getX() + moduleElement.getWidth() + 8, moduleElement.slowlySettingsYPos + 14, (int) moduleElement.getSettingsWidth() - 12, mouseX, mouseY, new Color(7, 152, 252));

                        if (valueOfSlide != floatValue.get())
                            floatValue.set(valueOfSlide);

                        Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, moduleElement.slowlySettingsYPos + 3, Color.WHITE.getRGB());
                        moduleElement.slowlySettingsYPos += 19;
                    } else if (value instanceof IntegerValue) {
                        final IntegerValue integerValue = (IntegerValue) value;
                        final String text = value.getName() + ": " + (value instanceof BlockValue ? BlockUtils.getBlockName(integerValue.get()) + " (" + integerValue.get() + ")" : integerValue.get());
                        final float textWidth = Fonts.font35.getStringWidth(text);

                        if (moduleElement.getSettingsWidth() < textWidth + 8)
                            moduleElement.setSettingsWidth(textWidth + 8);

                        final float valueOfSlide = drawSlider(integerValue.get(), integerValue.getMinimum(), integerValue.getMaximum(), moduleElement.getX() + moduleElement.getWidth() + 8, moduleElement.slowlySettingsYPos + 14, (int) moduleElement.getSettingsWidth() - 12, mouseX, mouseY, new Color(7, 152, 252));

                        if (valueOfSlide != integerValue.get())
                            integerValue.set((int) valueOfSlide);

                        Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, moduleElement.slowlySettingsYPos + 3, Color.WHITE.getRGB());
                        moduleElement.slowlySettingsYPos += 19;
                    } else if (value instanceof FontValue) {
                        final FontValue fontValue = (FontValue) value;
                        final FontRenderer fontRenderer = fontValue.get();

                        String displayString = "Font: Unknown";

                        if (fontRenderer instanceof GameFontRenderer) {
                            final GameFontRenderer liquidFontRenderer = (GameFontRenderer) fontRenderer;

                            displayString = "Font: " + liquidFontRenderer.getDefaultFont().getFont().getName() + " - " + liquidFontRenderer.getDefaultFont().getFont().getSize();
                        } else if (fontRenderer == Fonts.minecraftFont)
                            displayString = "Font: Minecraft";
                        else {
                            final Object[] objects = Fonts.getFontDetails(fontRenderer);

                            if (objects != null) {
                                displayString = objects[0] + ((int) objects[1] != -1 ? " - " + objects[1] : "");
                            }
                        }

                        Fonts.font35.drawString(displayString, moduleElement.getX() + moduleElement.getWidth() + 6, moduleElement.slowlySettingsYPos + 2, Color.WHITE.getRGB());
                        int stringWidth = Fonts.font35.getStringWidth(displayString);

                        if (moduleElement.getSettingsWidth() < stringWidth + 8)
                            moduleElement.setSettingsWidth(stringWidth + 8);

                        if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && mouseX >= moduleElement.getX() + moduleElement.getWidth() + 4 && mouseX <= moduleElement.getX() + moduleElement.getWidth() + moduleElement.getSettingsWidth() && mouseY >= moduleElement.slowlySettingsYPos && mouseY <= moduleElement.slowlySettingsYPos + 12) {
                            final List<FontRenderer> fonts = Fonts.getFonts();

                            if (Mouse.isButtonDown(0)) {
                                for (int i = 0; i < fonts.size(); i++) {
                                    final FontRenderer font = fonts.get(i);

                                    if (font == fontRenderer) {
                                        i++;

                                        if (i >= fonts.size())
                                            i = 0;

                                        fontValue.set(fonts.get(i));
                                        break;
                                    }
                                }
                            } else {
                                for (int i = fonts.size() - 1; i >= 0; i--) {
                                    final FontRenderer font = fonts.get(i);

                                    if (font == fontRenderer) {
                                        i--;

                                        if (i >= fonts.size())
                                            i = 0;

                                        if (i < 0)
                                            i = fonts.size() - 1;

                                        fontValue.set(fonts.get(i));
                                        break;
                                    }
                                }
                            }
                        }

                        moduleElement.slowlySettingsYPos += 11;
                    } else {
                        final String text = value.getName() + ": " + value.get();
                        final float textWidth = Fonts.font35.getStringWidth(text);

                        if (moduleElement.getSettingsWidth() < textWidth + 8)
                            moduleElement.setSettingsWidth(textWidth + 8);

                        GlStateManager.resetColor();
                        Fonts.font35.drawString(text, moduleElement.getX() + moduleElement.getWidth() + 6, moduleElement.slowlySettingsYPos + 4, Color.WHITE.getRGB());
                        moduleElement.slowlySettingsYPos += 12;
                    }
                }

                moduleElement.updatePressed();
                mouseDown = Mouse.isButtonDown(0);
                rightMouseDown = Mouse.isButtonDown(1);
            }
        }
    }

    private BigDecimal round(final float v) {
        BigDecimal bigDecimal = new BigDecimal(Float.toString(v));
        bigDecimal = bigDecimal.setScale(2, 4);
        return bigDecimal;
    }

    private Color hoverColor(final Color color, final int hover) {
        final int r = color.getRed() - (hover * 2);
        final int g = color.getGreen() - (hover * 2);
        final int b = color.getBlue() - (hover * 2);

        return new Color(Math.max(r, 0), Math.max(g, 0), Math.max(b, 0), color.getAlpha());
    }
}
