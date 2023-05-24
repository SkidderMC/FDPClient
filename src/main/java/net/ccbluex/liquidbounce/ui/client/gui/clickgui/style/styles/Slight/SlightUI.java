package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.Slight;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.value.*;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.Colors;
import net.ccbluex.liquidbounce.utils.timer.TimerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SlightUI extends GuiScreen implements GuiYesNoCallback {

    public static ModuleCategory currentModuleType = ModuleCategory.COMBAT;
    public static Module currentModule = (Module) FDPClient.moduleManager.getModuleInCategory(currentModuleType).get(0);
    public static float startX = 100.0F;
    public static float startY = 85.0F;
    public float moduleStart = 0.0F;
    public int valueStart = 0;
    boolean previousmouse = true;
    boolean mouse;
    boolean MIND = false;
    public Opacity opacity = new Opacity(0);
    public int opacityx = 255;
    public float animationopacity = 0.0F;
    public float animationMN = 0.0F;
    public float animationX = 0.0F;
    public float animationY = 0.0F;
    public float moveX = 0.0F;
    public float moveY = 0.0F;
    private Color buttonColor = new Color(0, 0, 0);
    public GameFontRenderer LogoFont;
    boolean bind;
    TimerUtils AnimationTimer;
    private boolean isDraging;
    private boolean clickNotDraging;
    float animationDWheel;
    int finheight;
    float animheight;
    public ArrayList modBooleanValue;
    public ArrayList modModeValue;
    public ArrayList modDoubleValue;
    public ArrayList modIntValue;
    public static Map doubleValueMap = new HashMap();
    public static Map IntValueMap = new HashMap();

    public SlightUI() {
        this.LogoFont = Fonts.fontSFUI35;
        this.bind = false;
        this.AnimationTimer = new TimerUtils();
        this.animheight = 0.0F;
        this.modBooleanValue = new ArrayList();
        this.modModeValue = new ArrayList();
        this.modDoubleValue = new ArrayList();
        this.modIntValue = new ArrayList();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.isHovered(startX - 10.0F, startY - 40.0F, startX + 280.0F, startY + 25.0F, mouseX, mouseY) && Mouse.isButtonDown(0)) {
            if (this.moveX == 0.0F && this.moveY == 0.0F) {
                this.moveX = (float) mouseX - startX;
                this.moveY = (float) mouseY - startY;
            } else {
                startX = (float) mouseX - this.moveX;
                startY = (float) mouseY - this.moveY;
            }

            this.previousmouse = true;
        } else if (this.moveX != 0.0F || this.moveY != 0.0F) {
            this.moveX = 0.0F;
            this.moveY = 0.0F;
        }

        float scale = 1.0F;
        ScaledResolution sr = new ScaledResolution(this.mc);

        if (sr.getScaledHeight() > 420 && sr.getScaledWidth() > 570) {
            scale = 1.0F;
        } else {
            scale = 0.8F;
        }

        RenderUtil.drawImage(new ResourceLocation("fdpclient/ui/clickgui/slight/background.png"), 0, 0, (int) ((float) sr.getScaledWidth() * (1.0F / scale)), (int) ((float) sr.getScaledHeight() * (1.0F / scale)));
        this.opacity.interpolate((float) this.opacityx);
        boolean countMod = false;
        int[] counter = new int[1];
        int rainbowCol = UISlider.rainbow(System.nanoTime() * 3L, (float) counter[0], 1.0F).getRGB();
        Color col = new Color(rainbowCol);
        int Ranbow = (new Color(0, col.getGreen() / 3 + 40, col.getGreen() / 2 + 100)).getRGB();
        int Ranbow1 = (new Color(0, col.getGreen() / 4 + 20, col.getGreen() / 2 + 100)).getRGB();

        RenderUtil.drawDimRect((double) (startX - 40.0F), (double) (startY - 10.0F), (double) (startX + 300.0F), (double) (startY + 260.0F), Colors.getColor(32, 32, 32));
        RenderUtil.drawGradientRect2((double) (startX - 40.0F), (double) (startY - 12.0F), (double) (startX + 300.0F), (double) (startY - 10.0F), Ranbow, (new Color(4555775)).getRGB());
        RenderUtil.drawRect(startX + 65.0F, startY + 25.0F, startX + 165.0F, startY + 30.0F, (new Color(25, 145, 220)).getRGB());

        int m;

        for (m = 0; m < ModuleCategory.values().length; ++m) {
            ModuleCategory[] mY = ModuleCategory.values();

            if (mY[m] == currentModuleType) {
                this.finheight = m * 30;
                RenderUtil.drawGradientRect2((double) (startX - 30.0F), (double) (startY + 30.0F + this.animheight), (double) (startX - 29.0F), (double) (startY + 40.0F + this.animheight), Ranbow, (new Color(4555775)).getRGB());
                this.animheight = (float) RenderUtil.getAnimationState((double) this.animheight, (double) this.finheight, (double) Math.max(100.0F, Math.abs((float) this.finheight - this.animheight) * 10.0F));
                if (this.animheight == (float) this.finheight) {
                    Fonts.fontSFUI35.drawString(mY[m].name(), startX - 25.0F, startY + 30.0F + (float) (m * 30), (new Color(255, 255, 255)).getRGB());
                } else {
                    Fonts.fontSFUI35.drawString(mY[m].name(), startX - 25.0F, startY + 30.0F + (float) (m * 30), (new Color(196, 196, 196)).getRGB());
                }
            } else {
                RenderUtil.drawRect(startX - 25.0F, startY + 50.0F + (float) (m * 30), startX + 60.0F, startY + 75.0F + (float) (m * 30), (new Color(255, 255, 255, 0)).getRGB());
                Fonts.fontSFUI35.drawString(mY[m].name(), startX - 25.0F, startY + 30.0F + (float) (m * 30), (new Color(196, 196, 196)).getRGB());
            }

            try {
                if (this.isCategoryHovered(startX - 40.0F, startY + 20.0F + (float) (m * 30), startX + 60.0F, startY + 45.0F + (float) (m * 40), mouseX, mouseY) && Mouse.isButtonDown(0) && !this.MIND) {
                    currentModuleType = mY[m];
                    currentModule = FDPClient.moduleManager.getModuleInCategory(currentModuleType).size() != 0 ? (Module) FDPClient.moduleManager.getModuleInCategory(currentModuleType).get(0) : null;
                    this.moduleStart = 0.0F;
                }
            } catch (Exception exception) {
                System.err.println(exception);
            }
        }

        m = Mouse.getDWheel();
        if (this.isCategoryHovered(startX + 60.0F, startY, startX + 200.0F, startY + 235.0F, mouseX, mouseY) && !this.MIND) {
            if (m < 0 && this.moduleStart < (float) (FDPClient.moduleManager.getModuleInCategory(currentModuleType).size() - 1)) {
                ++this.moduleStart;
                this.animationDWheel = (float) RenderUtil.getAnimationState((double) this.animationDWheel, 1.0D, 50.0D);
                Minecraft.getMinecraft().thePlayer.playSound("random.click", 0.2F, 2.0F);
            }

            if (m > 0 && this.moduleStart > 0.0F) {
                --this.moduleStart;
                this.moduleStart = (float) RenderUtil.getAnimationState((double) this.moduleStart, -1.0D, 50.0D);
                Minecraft.getMinecraft().thePlayer.playSound("random.click", 0.2F, 2.0F);
            }
        } else {
            this.animationDWheel = 0.0F;
        }

        if (this.isCategoryHovered(startX - 40.0F, startY - 10.0F, startX + 300.0F, startY + 240.0F, mouseX, mouseY) && this.MIND) {
            if (m < 0 && this.valueStart < currentModule.getValues().size() - 1) {
                ++this.valueStart;
            }

            if (m > 0 && this.valueStart > 0) {
                --this.valueStart;
            }
        }

        if (currentModule != null) {
            this.modBooleanValue.clear();
            this.modModeValue.clear();
            this.modDoubleValue.clear();
            this.modIntValue.clear();
            float f = startY + 30.0F;

            int font;

            for (font = 0; font < FDPClient.moduleManager.getModuleInCategory(currentModuleType).size(); ++font) {
                Module value = (Module) FDPClient.moduleManager.getModuleInCategory(currentModuleType).get(font);

                if (f > startY + 220.0F) {
                    break;
                }

                if ((float) font >= this.moduleStart) {
                    RenderUtil.drawRect(startX + 75.0F, f, startX + 185.0F, f + 2.0F, (new Color(246, 246, 246, 100)).getRGB());
                    if (!value.getState()) {
                        RenderUtil.drawDimRect((double) (startX + 50.0F), (double) (f - 5.0F), (double) (startX + 285.0F), (double) (f + 20.0F), (new Color(38, 38, 37)).getRGB());
                        if (SlightUI.currentModule.getValues().size() > 0) {
                            RenderUtil.drawDimRect((double) (startX + 270.0F), (double) (f - 5.0F), (double) (startX + 285.0F), (double) (f + 20.0F), (new Color(44, 44, 45)).getRGB());
                            RenderUtil.circle(startX + 277.0F, f + 2.0F, 0.7F, new Color(95, 95, 95));
                            RenderUtil.circle(startX + 277.0F, f + 7.0F, 0.7F, new Color(95, 95, 95));
                            RenderUtil.circle(startX + 277.0F, f + 12.0F, 0.7F, new Color(95, 95, 95));
                        }
                    } else {
                        RenderUtil.drawDimRect((double) (startX + 50.0F), (double) (f - 5.0F), (double) (startX + 285.0F), (double) (f + 20.0F), (new Color(55, 55, 55)).getRGB());
                    }

                    if (this.isSettingsButtonHovered(startX + 65.0F, f, startX + 285.0F, f + 8.0F + (float) Fonts.fontSFUI35.getStringWidth(""), mouseX, mouseY) && !this.MIND) {
                        this.animationopacity = (float) RenderUtil.getAnimationState((double) this.animationopacity, 0.30000001192092896D, 20.0D);
                        this.animationMN = (float) RenderUtil.getAnimationState((double) this.animationMN, 10.0D, 100.0D);
                        if (!value.getState()) {
                            Fonts.fontSFUI35.drawString(value.getName(), startX + 70.0F + this.animationMN, f + 4.0F, (new Color(240, 240, 240)).getRGB(), false);
                        } else {
                            Fonts.fontSFUI35.drawString(value.getName(), startX + 70.0F + this.animationMN, f + 4.0F, (new Color(255, 255, 255)).getRGB(), false);
                        }
                    } else {
                        this.animationopacity = (float) RenderUtil.getAnimationState((double) this.animationopacity, 0.0D, 20.0D);
                        this.animationMN = (float) RenderUtil.getAnimationState((double) this.animationMN, 0.0D, 100.0D);
                        if (value.getState()) {
                            Fonts.fontSFUI35.drawString(value.getName(), startX + 70.0F + this.animationMN, f + 4.0F, (new Color(200, 200, 200)).getRGB(), false);
                        } else {
                            Fonts.fontSFUI35.drawString(value.getName(), startX + 70.0F + this.animationMN, f + 4.0F, (new Color(190, 190, 190)).getRGB(), false);
                        }
                    }

                    RenderUtil.drawRect(startX + 50.0F, f - 5.0F, startX + 285.0F, f + 20.0F, RenderUtil.reAlpha(Colors.WHITE.c, this.animationopacity));
                    if (value.getState()) {
                        RenderUtil.drawGradientRect2((double) (startX + 50.0F), (double) (f - 5.0F), (double) (startX + 51.0F), (double) (f + 20.0F), Ranbow, (new Color(4555775)).getRGB());
                    }

                    if (this.isSettingsButtonHovered(startX + 65.0F, f, startX + 285.0F, f + 8.0F + (float) Fonts.fontSFUI35.getStringWidth(""), mouseX, mouseY) && !this.MIND) {
                        if (!this.previousmouse && Mouse.isButtonDown(0)) {
                            if (value.getState()) {
                                value.setState(false);
                            } else {
                                value.setState(true);
                            }

                            this.previousmouse = true;
                        }

                        if (!this.previousmouse && Mouse.isButtonDown(1)) {
                            this.previousmouse = true;
                        }
                    }

                    if (!Mouse.isButtonDown(0) && !this.MIND) {
                        this.previousmouse = false;
                    }

                    if (this.isSettingsButtonHovered(startX + 65.0F, f, startX + 285.0F + (float) Fonts.fontSFUI35.getStringWidth(value.getName()), f + 8.0F + (float) Fonts.fontSFUI35.getStringWidth(""), mouseX, mouseY) && Mouse.isButtonDown(1) && !this.MIND) {
                        currentModule = value;
                        Minecraft.getMinecraft().thePlayer.playSound("random.click", 0.5F, 4.0F);
                        this.valueStart = 0;
                        this.MIND = true;
                    }

                    f += 30.0F;
                }
            }

            for (font = 0; font < currentModule.getValues().size(); ++font) {
                Value value = (Value) currentModule.getValues().get(font);

                if (value instanceof BoolValue) {
                    this.modBooleanValue.add((BoolValue) value);
                }

                if (value instanceof ListValue) {
                    this.modModeValue.add((ListValue) value);
                }

                if (value instanceof FloatValue) {
                    this.modDoubleValue.add((FloatValue) value);
                }

                if (value instanceof IntegerValue) {
                    this.modIntValue.add((IntegerValue) value);
                }
            }

            f = startY + 12.0F;
            if (this.MIND) {
                if (this.isCategoryHovered(startX - 40.0F, startY - 10.0F, startX + 300.0F, startY + 240.0F, mouseX, mouseY)) {
                    if (m < 0 && this.valueStart < (currentModule.getValues().size() - 1) * 12) {
                        this.valueStart += 12;
                    }

                    if (m > 0 && this.valueStart - 12 >= 0) {
                        this.valueStart -= 12;
                    } else if (m > 0) {
                        this.valueStart = 0;
                    }
                }

                if (this.animationX == 0.0F) {
                    ;
                }

                this.animationX = (float) RenderUtil.getAnimationState((double) this.animationX, 390.0D, 600.0D);
                this.animationY = (float) RenderUtil.getAnimationState((double) this.animationY, 120.0D, 800.0D);
                GL11.glPushMatrix();
                GL11.glEnable(3089);
                RenderUtil.doGlScissor((int) (startX - 40.0F), 0, (int) this.animationX, RenderUtil.height());
                RenderUtil.drawDimRect((double) (startX - 40.0F), (double) (startY - 10.0F), (double) (startX + 300.0F), (double) (startY + 8.0F), Colors.getColor(44, 44, 45));
                RenderUtil.drawDimRect((double) (startX - 40.0F), (double) (startY + 8.0F), (double) (startX + 300.0F), (double) (startY + 260.0F), Colors.getColor(37, 37, 38));
                RenderUtil.circle(startX + 292.0F, startY - 4.0F, 4.0F, (new Color(-14848033)).brighter());
                if (this.isSettingsButtonHovered(startX + 288.0F, startY - 6.0F, startX + 344.0F, startY + 2.0F, mouseX, mouseY) && Mouse.isButtonDown(0)) {
                    this.MIND = false;
                }

                GameFontRenderer gamefontrenderer = Fonts.fontSFUI35;

                Fonts.fontSFUI35.drawString(currentModule.getName(), startX - 35.0F, startY - 8.0F, -1);
                GL11.glPushMatrix();
                if (this.animationX == 390.0F) {
                    RenderUtil.doGlScissor((int) startX - 40, (int) startY + 8, (int) startX + 300, RenderUtil.height());
                }

                float x;
                Iterator iterator;

                for (iterator = this.modBooleanValue.iterator(); iterator.hasNext(); f += 20.0F) {
                    BoolValue value1 = (BoolValue) iterator.next();

                    if (f - (float) this.valueStart > startY + 220.0F) {
                        break;
                    }

                    Gui.drawRect(1, 1, 1, 1, -1);
                    x = startX + 250.0F;
                    gamefontrenderer.drawString(value1.getName(), startX - 30.0F, f - (float) this.valueStart, (new Color(255, 255, 255)).getRGB());
                    if (((Boolean) value1.getValue()).booleanValue()) {
                        this.buttonColor = (new Color(-14848033)).brighter();
                    } else {
                        this.buttonColor = new Color(80, 80, 80);
                    }

                    RenderUtil.circle(x + 35.0F, f - (float) this.valueStart + 2.0F, 4.0F, this.buttonColor.getRGB());
                    if (this.isCheckBoxHovered(x + 30.0F, f - (float) this.valueStart, x + 38.0F, f - (float) this.valueStart + 9.0F, mouseX, mouseY)) {
                        if (!this.previousmouse && Mouse.isButtonDown(0)) {
                            this.previousmouse = true;
                            this.mouse = true;
                        }

                        if (this.mouse) {
                            value1.setValue(Boolean.valueOf(!((Boolean) value1.getValue()).booleanValue()));
                            this.mouse = false;
                        }
                    }

                    if (!Mouse.isButtonDown(0)) {
                        this.previousmouse = false;
                    }
                }

                UISlider uislider;

                for (iterator = this.modDoubleValue.iterator(); iterator.hasNext(); f += 22.0F) {
                    FloatValue floatvalue = (FloatValue) iterator.next();

                    if (f - (float) this.valueStart > startY + 220.0F) {
                        break;
                    }

                    if (doubleValueMap.containsKey(floatvalue)) {
                        uislider = (UISlider) doubleValueMap.get(floatvalue);
                    } else {
                        uislider = new UISlider(floatvalue);
                        doubleValueMap.put(floatvalue, uislider);
                    }

                    uislider.drawAll(startX + 45.0F, f - (float) this.valueStart, mouseX, mouseY);
                }

                for (iterator = this.modIntValue.iterator(); iterator.hasNext(); f += 22.0F) {
                    IntegerValue integervalue = (IntegerValue) iterator.next();

                    if (f - (float) this.valueStart > startY + 220.0F) {
                        break;
                    }

                    if (IntValueMap.containsKey(integervalue)) {
                        uislider = (UISlider) IntValueMap.get(integervalue);
                    } else {
                        uislider = new UISlider(integervalue);
                        IntValueMap.put(integervalue, uislider);
                    }

                    uislider.drawAlll(startX + 45.0F, f - (float) this.valueStart, mouseX, mouseY);
                }

                for (iterator = this.modModeValue.iterator(); iterator.hasNext(); f += 20.0F) {
                    ListValue listvalue = (ListValue) iterator.next();

                    if (f - (float) this.valueStart > startY + 220.0F) {
                        break;
                    }

                    x = startX + 250.0F;
                    RenderUtil.drawRect(x - 40.0F, f - (float) this.valueStart - 1.0F, x + 40.0F, f - (float) this.valueStart + 12.0F, (new Color(60, 60, 60)).getRGB());
                    Fonts.fontSFUI35.drawString(listvalue.getName(), startX - 30.0F, f - (float) this.valueStart, (new Color(255, 255, 255)).getRGB());
                    Fonts.fontSFUI35.drawCenteredString((String) listvalue.getValue(), x - 2.0F, f - (float) this.valueStart + 2.0F, -1);
                    if (this.isStringHovered(x - 40.0F, f - (float) this.valueStart - 1.0F, x + 40.0F, f - (float) this.valueStart + 12.0F, mouseX, mouseY)) {
                        if (Mouse.isButtonDown(0) && !this.previousmouse) {
                            String current = (String) listvalue.getValue();

                            listvalue.set(listvalue.getValues()[listvalue.getModeListNumber(current) + 1 >= listvalue.getValues().length ? 0 : listvalue.getModeListNumber(current) + 1]);
                            this.previousmouse = true;
                        }

                        if (!Mouse.isButtonDown(0)) {
                            this.previousmouse = false;
                        }
                    }
                }

                GL11.glPopMatrix();
                GL11.glDisable(3089);
                GL11.glPopMatrix();
            } else {
                this.animationX = (float) RenderUtil.getAnimationState((double) this.animationX, 0.0D, 800.0D);
                this.animationY = (float) RenderUtil.getAnimationState((double) this.animationY, 0.0D, 800.0D);
            }
        }

    }

    public boolean isStringHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return (float) mouseX >= f && (float) mouseX <= g && (float) mouseY >= y && (float) mouseY <= y2;
    }

    public boolean isSettingsButtonHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return (float) mouseX >= x && (float) mouseX <= x2 && (float) mouseY >= y && (float) mouseY <= y2;
    }

    public boolean isButtonHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return (float) mouseX >= f && (float) mouseX <= g && (float) mouseY >= y && (float) mouseY <= y2;
    }

    public boolean isCheckBoxHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return (float) mouseX >= f && (float) mouseX <= g && (float) mouseY >= y && (float) mouseY <= y2;
    }

    public boolean isCategoryHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return (float) mouseX >= x && (float) mouseX <= x2 && (float) mouseY >= y && (float) mouseY <= y2;
    }

    public boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return (float) mouseX >= x && (float) mouseX <= x2 && (float) mouseY >= y && (float) mouseY <= y2;
    }

    public void onGuiClosed() {
        this.opacity.setOpacity(0.0F);
    }
}
