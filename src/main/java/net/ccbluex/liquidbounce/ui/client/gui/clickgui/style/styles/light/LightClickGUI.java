/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.light;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.light.LightSettings.InputBox;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.light.ModuleSettings.Settings;
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.AnimationHelper;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.*;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class LightClickGUI extends GuiScreen implements GuiYesNoCallback {

    // light style by dg636 & opzywl

    private ModuleCategory currentCategory = ModuleCategory.COMBAT;
    private Module currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
    private float startX = 50, startY = 25;
    private int moduleStart = 0;
    private int valueStart = 0;
    private boolean previousMouse = true;
    private boolean mouse;
    private float moveX = 0, moveY = 0;
    private float oldX = 0, oldY = 0;
    private final FontRenderer defaultFont = Fonts.font35;
    private final FontRenderer logoFont = Fonts.font40;

    private final int fontHeight = Math.round(defaultFont.FONT_HEIGHT / 2);
    private boolean rightClickMouse = false;
    private boolean categoryMouse = false;
    private int animationHeight = 0;
    private int categoryAnimation = 0;
    private int moduleAnimation = 0;
    private boolean moduleHover = false;
    private float guiScale = 0;
    private final AnimationHelper alphaAnim = new AnimationHelper();
    private final AnimationHelper valueAnim = new AnimationHelper();
    private int categoryYpos = 35;
    private InputBox searchBox;
    private boolean firstSetAnimation;
    public LightClickGUI() {
        firstSetAnimation = false;
        alphaAnim.resetAlpha();
        valueAnim.resetAlpha();
    }

    @Override
    public void initGui() {
        firstSetAnimation = false;
        alphaAnim.resetAlpha();
        valueAnim.resetAlpha();
        this.searchBox = new InputBox(1, (int)startX, (int)startY + 20, 45, 8);
    }

    @Override
    protected void mouseClicked(int p_mouseClicked_1_, int p_mouseClicked_2_, int p_mouseClicked_3_) throws IOException {
        super.mouseClicked(p_mouseClicked_1_,p_mouseClicked_2_,p_mouseClicked_3_);
        searchBox.mouseClicked(p_mouseClicked_1_,p_mouseClicked_2_,p_mouseClicked_3_);
    }

    @Override
    public void updateScreen() {
        searchBox.updateCursorCounter();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        }
        if(typedChar == 9 && this.searchBox.isFocused()) {
            this.searchBox.setFocused(!this.searchBox.isFocused());
        }

        this.searchBox.textboxKeyTyped(typedChar, keyCode);
//        } else {
//            if(!hovered)
//                return;
//            String keyName = Keyboard.getKeyName(keyCode);
//            if(!keyName.equalsIgnoreCase("LMENU") && !keyName.equalsIgnoreCase("SPACE") && !keyName.equalsIgnoreCase("BACKSLASH") && !keyName.equalsIgnoreCase("LCONTROL") && !keyName.equalsIgnoreCase("CAPITAL") && !keyName.equalsIgnoreCase("APPS") && !keyName.equalsIgnoreCase("LMETA") && !keyName.equalsIgnoreCase("APOSTROPHE") && !keyName.equalsIgnoreCase("PERIOD") && !keyName.equalsIgnoreCase("COMMA") && !keyName.equalsIgnoreCase("SEMICOLON") && !keyName.equalsIgnoreCase("RSHIFT") && !keyName.equalsIgnoreCase("BACKSLAS") && !keyName.equalsIgnoreCase("MINUS") && !keyName.equalsIgnoreCase("GRAVE") && !keyName.equalsIgnoreCase("MINUS") && !keyName.equalsIgnoreCase("EQUALS") && !keyName.equalsIgnoreCase("F12") && !keyName.equalsIgnoreCase("F11") && !keyName.equalsIgnoreCase("F10") && !keyName.equalsIgnoreCase("F9") && !keyName.equalsIgnoreCase("F8") && !keyName.equalsIgnoreCase("7") && !keyName.equalsIgnoreCase("F6") && !keyName.equalsIgnoreCase("F5") && !keyName.equalsIgnoreCase("F4") && !keyName.equalsIgnoreCase("F3") && !keyName.equalsIgnoreCase("F2") && !keyName.equalsIgnoreCase("NUMLOCK") && !keyName.equalsIgnoreCase("F1") && !keyName.equalsIgnoreCase("RETURN") && !keyName.equalsIgnoreCase("SCROLL") && !keyName.equalsIgnoreCase("PRIOR") && !keyName.equalsIgnoreCase("NEXT") && !keyName.equalsIgnoreCase("LSHIFT") && !keyName.equalsIgnoreCase("SHIFT") && !keyName.equalsIgnoreCase("RCONTROL") && !keyName.equalsIgnoreCase("HOME") && !keyName.equalsIgnoreCase("TAB") && !keyName.equalsIgnoreCase("BACK") && !keyName.equalsIgnoreCase("none") && !keyName.equalsIgnoreCase("Alt") && !keyName.equalsIgnoreCase("Insert") && !keyName.equalsIgnoreCase("DELETE") && !keyName.equalsIgnoreCase("END") && !keyName.equalsIgnoreCase("Pause") && !keyName.equalsIgnoreCase("SYSRQ")) {
//                text += Keyboard.getKeyName(keyCode);
//            }
//            if(Keyboard.isKeyDown(Keyboard.KEY_V) && isCtrlKeyDown()) {
//                text += getClipboardString();
//            }
//            if(Keyboard.isKeyDown(Keyboard.KEY_C) && isCtrlKeyDown()) {
//                setText(text);
//            }
//            if(Keyboard.isKeyDown(Keyboard.KEY_BACK)) {
//                if(!searchBox.getText().isEmpty()) {
//                    text = searchBox.getText().substring(0, searchBox.getText().length() - 1);
//                }
//            }
//        }
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // yes hud designer here
        RenderUtils.drawImage(new ResourceLocation( "fdpclient/ui/clickgui/hud.png"), 9, height - 41, 32, 32);

        // set up bool animations?
        if(!firstSetAnimation) {
            for (Module i : LiquidBounce.moduleManager.getModules()) {
                i.getAnimation().animationX = i.getState() ? 5 : -5;
                for(Value<?> j : i.getValues()) {
                    if(j instanceof BoolValue) {
                        BoolValue boolValue = (BoolValue) j;
                        boolValue.getAnimation().animationX = boolValue.get() ? 5 : -5;
                    }
                }
            }
            firstSetAnimation = true;
        }

        // serach box
        searchBox.xPosition = (int) startX;
        searchBox.yPosition = (int) (startY + 15);
        this.searchBox.setMaxStringLength(20);

        // animate alpha
        if(alphaAnim.getAlpha() == 250)
            alphaAnim.alpha = 255;
        else
            alphaAnim.updateAlpha(25);
        //整个界面的alpha
        if(valueAnim.getAlpha() == 240)
            alphaAnim.alpha = 255;
        else
            valueAnim.updateAlpha(30);
        //value界面的alpha

        // animate scale
        if(guiScale < 100)
            guiScale += 10;//启动动画
        GlStateManager.scale(guiScale / 100, guiScale / 100, guiScale / 100);

        // make that variable gurl
        Settings settings = new Settings(valueAnim);//value的settings

        // huddesigner icon sense clik
        if (Mouse.isButtonDown(0) && mouseX >= 5 && mouseX <= 50 && mouseY <= height - 5 && mouseY >= height - 50)
            mc.displayGuiScreen(new GuiHudDesigner()); //进入自定义HUD界面

        // click = move target hud
        if ((isHovered(startX - 5, startY, startX + 400, startY + 25, mouseX, mouseY) || isHovered(startX - 5, startY, startX + 400, startY + 25, (int) oldX, (int) oldY)) && Mouse.isButtonDown(0)) {//移动窗口
            if (moveX == 0 && moveY == 0) {
                moveX = mouseX - startX;
                moveY = mouseY - startY;
            } else {
                startX = mouseX - moveX;
                startY = mouseY - moveY;
            }
            this.previousMouse = true;
        } else if (moveX != 0 || moveY != 0) {
            moveX = 0;
            moveY = 0;
        }
        oldX = mouseX;
        oldY = mouseY;

        // fdp water mark
        Fonts.font35.drawString("Made by SkidderMC", startX + 210, startY + 295, new Color(200,200,200).getRGB());

        // new sexy background
        RenderUtils.drawRoundedRect2((int) startX - 5, (int) startY + 10, (int) startX + 400, (int) startY + 310, 3,
                new Color(230, 230, 230, 255).getRGB());
        RenderUtils.drawRoundedRect2((int) startX - 5, (int) startY, (int) startX + 400, (int) startY + 25, 10,
                new Color(250, 250, 250, 255).getRGB());


        //drawBorderedRect(startX + 130, startY + 7, startX + 190, startY + 15, 0.5F, -1, new Color(100,100,100).getRGB());

        // search box time
        defaultFont.drawString(searchBox.getText().isEmpty() && !searchBox.isFocused() ? "Search..." : searchBox.getText(), (int) (startX + 3), (int) (startY + 15), new Color(80, 80, 80).getRGB());

        // no module???? holder text
        if(currentModule == null) {
            logoFont.drawStringWithShadow("No Modules Selected", startX + 80, startY + 130, new Color(100,100,100).getRGB());
        }
//        if(isHovered(startX + 130, startY + 7, startX + 190, startY + 15, mouseX, mouseY) && Mouse.isButtonDown(0))
//            hovered = true;
//        else if(!isHovered(startX + 130, startY + 7, startX + 190, startY + 15, mouseX, mouseY) && Mouse.isButtonDown(0))
//            hovered = false;
        //   FontManager.logo24.drawCenteredStringWithShadow("L", startX + 5, startY + 8, new Color(49, 86, 255).getRGB(),255);
        //FontManager.logo24.drawCenteredStringWithShadow("ight", startX + 5 + FontManager.logo24.getStringWidth("L") + 8, startY + 8, new Color(255,255,255).getRGB(),255); // 客户端名字



        // mouse scroll now est variable
        int m = Mouse.getDWheel();//鼠标滚轮.

        // smth
        if(searchBox.getText().isEmpty()) {
            if (this.isCategoryHovered(startX + 60, startY + 40, startX + 200, startY + 280, mouseX, mouseY)) {
                if (m < 0 && moduleStart < LiquidBounce.moduleManager.getModuleInCategory(currentCategory).size() - 8) {
                    moduleStart++;
                }
                if (m > 0 && moduleStart > 0) {
                    moduleStart--;
                }
            }
        }
        if (this.isCategoryHovered(startX + 200, startY, startX + 400, startY + 280, mouseX, mouseY)) {
            if (m < 0 && valueStart < currentModule.getValues().size() - 11) {
                valueStart++;
            }
            if (m > 0 && valueStart > 0) {
                valueStart--;
            }
        }

        // draw current categorys category's name
        logoFont.drawString(currentCategory.getDisplayName(), (int) (startX + 60), (int) (startY + 10),
                new Color(100, 100, 100,alphaAnim.getAlpha()).getRGB());
        RenderUtils.circle(startX + 390, startY + 8, 1.5F, new Color(31, 158, 255).getRGB());
        if (isCheckBoxHovered(startX + 388, startY + 6, startX + 391, startY + 9, mouseX, mouseY)
                && Mouse.isButtonDown(0)) {
            mc.displayGuiScreen(new GuiExit());
        }

        moduleHover = false;


        if(!searchBox.getText().isEmpty()) {
            if (this.isCategoryHovered(startX + 60, startY + 40, startX + 200, startY + 280, mouseX, mouseY)) {
                if (m < 0 && moduleStart < LiquidBounce.moduleManager.getModuleInCategory(currentCategory).size() - 8) {
                    moduleStart++;
                }
                if (m > 0 && moduleStart > 0) {
                    moduleStart--;
                }
            }
            if (this.isCategoryHovered(startX + 200, startY + 40, startX + 400, startY + 280, mouseX, mouseY)) {
                if (m < 0 && valueStart < currentModule.getValues().size() - 11) {
                    valueStart++;
                }
                if (m > 0 && valueStart > 0) {
                    valueStart--;
                }
            }

            // draw categories search box
            float mY = startY + 30;

            for(int i = 0; i < LiquidBounce.moduleManager.getModulesByName(searchBox.getText()).size(); i++) {
                Module module = LiquidBounce.moduleManager.getModulesByName(searchBox.getText()).get(i);
                if (mY > startY + 280)
                    break;
                if (i < moduleStart) {
                    continue;
                }
                int moduleColor = new Color(118, 117, 117,alphaAnim.getAlpha()).getRGB();


                if (!Mouse.isButtonDown(0)) {
                    this.previousMouse = false;
                }

                int colorV =  245 - (int) (module.getAnimation().animationX);
                RenderUtils.drawRoundedRect2(startX + 63, mY + 1, startX + 187, mY + 25, 6, new Color(210, 210, 210, alphaAnim.getAlpha()).getRGB());
                RenderUtils.drawRoundedRect2(startX + 64, mY + 2, startX + 186, mY + 24, 5, new Color(230, 230, 230, alphaAnim.getAlpha()).getRGB());
                RenderUtils.drawRoundedRect2(startX + 65, mY + 3, startX + 185, mY + 23, 4, new Color(colorV, colorV, colorV, alphaAnim.getAlpha()).getRGB());

                if (isSettingsButtonHovered(startX + 64, mY + 2, startX + 186, mY + 24, mouseX, mouseY) && categoryAnimation < 15) {
                    defaultFont.drawString(module.getName(), (int) (startX + 70) + Math.round(moduleAnimation / 2), (int) (mY + 6 + 5 - fontHeight), moduleColor);
                    defaultFont.drawString("KeyBind: " + (!Keyboard.getKeyName(module.getKeyBind()).equalsIgnoreCase("NONE") ? Keyboard.getKeyName(module.getKeyBind()) : "None"), (int) (startX + 70) + Math.round(moduleAnimation / 2), (int) (mY + 13 + 5 - fontHeight), new Color(80, 80, 80, alphaAnim.getAlpha()).getRGB() - 14 +  moduleAnimation * 2);

                    moduleAnimation += 1;
                    if (moduleAnimation > 6) {
                        moduleAnimation = 6;
                    }
                    moduleHover = true;

                    if (Mouse.isButtonDown(1) && !rightClickMouse && currentModule != module) {
                        currentModule = module;
                        valueAnim.resetAlpha();
                        valueStart = 0;
                        rightClickMouse = true;
                    }
                    if (!this.previousMouse && Mouse.isButtonDown(0)) {
                        module.setState(!module.getState());
                        previousMouse = true;
                    }
                    if (!this.previousMouse && Mouse.isButtonDown(1)) {
                        previousMouse = true;
                    }
                } else {
                    defaultFont.drawString(module.getName(), (int) (startX + 70), (int) (mY + 6 + 5 - fontHeight), moduleColor);
                    defaultFont.drawString("KeyBind: " + (!Keyboard.getKeyName(module.getKeyBind()).equalsIgnoreCase("NONE") ? Keyboard.getKeyName(module.getKeyBind()) : "None"), (int) (startX + 70), (int) (mY + 13 + 5 - fontHeight), new Color(80, 80, 80, alphaAnim.getAlpha()).getRGB());
                }
                if (rightClickMouse && !Mouse.isButtonDown(1))
                    rightClickMouse = false;

                RenderUtils.drawRoundedRect2(startX + 160, mY + 6, startX + 180, mY + 16, 4, module.getState() && module.getAnimation().getAnimationX() >= 3F ? new Color(29, 143, 237,alphaAnim.getAlpha()).getRGB() : new Color(114, 118, 125,alphaAnim.getAlpha()).getRGB());
                RenderUtils.circle(startX + 170 + module.getAnimation().getAnimationX(), mY + 11, 4, module.getState() ? new Color(255,255,255,alphaAnim.getAlpha()).getRGB() : new Color(164, 168, 175,alphaAnim.getAlpha()).getRGB());
                if (module.getState()) {
                    module.getAnimation().animationX += (5F - module.getAnimation().animationX) / 2.5;
                } else {
                    module.getAnimation().animationX += (-5F - module.getAnimation().animationX) / 2.5;
                }

                mY += 28;
            }
        }
        if (currentModule != null) {
            logoFont.drawString(currentModule.getName(), (int) (startX + 205), (int) (startY + 10),
                    new Color(100, 100, 100,valueAnim.getAlpha()).getRGB());
            float mY = startY + 30;
            if(searchBox.getText().isEmpty()) {
                for (int i = 0; i < LiquidBounce.moduleManager.getModuleInCategory(currentCategory).size(); i++) {
                    Module module = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(i);
                    if (mY > startY + 280)
                        break;
                    if (i < moduleStart)
                        continue;

                    int moduleColor = new Color(118, 117, 117,alphaAnim.getAlpha()).getRGB();


                    if (!Mouse.isButtonDown(0)) {
                        this.previousMouse = false;
                    }

                    int colorV =  245 - (int) (module.getAnimation().animationX);
                    RenderUtils.drawRoundedRect2(startX + 63, mY + 1, startX + 187, mY + 25, 6, new Color(210, 210, 210, alphaAnim.getAlpha()).getRGB());
                    RenderUtils.drawRoundedRect2(startX + 64, mY + 2, startX + 186, mY + 24, 5, new Color(230, 230, 230, alphaAnim.getAlpha()).getRGB());
                    RenderUtils.drawRoundedRect2(startX + 65, mY + 3, startX + 185, mY + 23, 4, new Color(colorV, colorV, colorV, alphaAnim.getAlpha()).getRGB());

                    if (isSettingsButtonHovered(startX + 64, mY + 2, startX + 186, mY + 24, mouseX, mouseY) && categoryAnimation < 15) {
                        defaultFont.drawString(module.getName(), (int) (startX + 70) + Math.round(moduleAnimation / 2), (int) (mY + 6 + 5 - fontHeight), moduleColor);
                        defaultFont.drawString("KeyBind: " + (!Keyboard.getKeyName(module.getKeyBind()).equalsIgnoreCase("NONE") ? Keyboard.getKeyName(module.getKeyBind()) : "None"), (int) (startX + 70) + Math.round(moduleAnimation / 2), (int) (mY + 13 + 5 - fontHeight), new Color(80, 80, 80, alphaAnim.getAlpha()).getRGB() - 14 +  moduleAnimation * 2);

                        moduleAnimation += 1;
                        if (moduleAnimation > 6) {
                            moduleAnimation = 6;
                        }
                        moduleHover = true;

                        if (Mouse.isButtonDown(1) && !rightClickMouse && currentModule != module) {
                            currentModule = module;
                            valueAnim.resetAlpha();
                            valueStart = 0;
                            rightClickMouse = true;
                        }
                        if (!this.previousMouse && Mouse.isButtonDown(0)) {
                            module.setState(!module.getState());
                            previousMouse = true;
                        }
                        if (!this.previousMouse && Mouse.isButtonDown(1)) {
                            previousMouse = true;
                        }
                    } else {
                        defaultFont.drawString(module.getName(), (int) (startX + 70), (int) (mY + 6 + 5 - fontHeight), moduleColor);
                        defaultFont.drawString("KeyBind: " + (!Keyboard.getKeyName(module.getKeyBind()).equalsIgnoreCase("NONE") ? Keyboard.getKeyName(module.getKeyBind()) : "None"), (int) (startX + 70), (int) (mY + 13 + 5 - fontHeight), new Color(80, 80, 80, alphaAnim.getAlpha()).getRGB());
                    }
                    if (rightClickMouse && !Mouse.isButtonDown(1))
                        rightClickMouse = false;

                    RenderUtils.drawRoundedRect2(startX + 160, mY + 6, startX + 180, mY + 16, 4, module.getState() && module.getAnimation().getAnimationX() >= 3F ? new Color(29, 143, 237,alphaAnim.getAlpha()).getRGB() : new Color(114, 118, 125,alphaAnim.getAlpha()).getRGB());
                    RenderUtils.circle(startX + 170 + module.getAnimation().getAnimationX(), mY + 11, 4, module.getState() ? new Color(255,255,255,alphaAnim.getAlpha()).getRGB() : new Color(164, 168, 175,alphaAnim.getAlpha()).getRGB());
                    if (module.getState()) {
                        module.getAnimation().animationX += (5F - module.getAnimation().animationX) / 2.5;
                    } else {
                        module.getAnimation().animationX += (-5F - module.getAnimation().animationX) / 2.5;

                    }

                    mY += 28;
                }
            }
            mY = startY + 30;
            if (currentModule.getValues().isEmpty())
                logoFont.drawString("Current Module Has No Settings", (int) (startX + 220), (int) (startY + 135 - fontHeight),
                        new Color(100, 100, 100,valueAnim.getAlpha()).getRGB());
            for (int i = 0; i < currentModule.getValues().size(); i++) {
                if (mY > startY + 280)
                    break;
                if (i < valueStart) {
                    continue;
                }
                Value<?> value = currentModule.getValues().get(i);
                if (value instanceof FloatValue) {
                    FloatValue floatValue = (FloatValue) value;
                    float x = startX + 300;
                    settings.drawFloatValue(mouseX, mY, startX, previousMouse,this.isButtonHovered(x, mY - 2, x + 100, mY + 7, mouseX, mouseY), floatValue);
                    if (!Mouse.isButtonDown(0)) {
                        this.previousMouse = false;
                    }
                    mY += 20;
                }
                if (value instanceof IntegerValue) {
                    IntegerValue integerValue = (IntegerValue) value;
                    float x = startX + 300;
                    settings.drawIntegerValue(mouseX, mY, startX, previousMouse, this.isButtonHovered(x, mY - 2, x + 100, mY + 7, mouseX, mouseY), integerValue);
                    if (!Mouse.isButtonDown(0)) {
                        this.previousMouse = false;
                    }
                    mY += 20;
                }
                if(value instanceof Value.ColorValue) {
                    Value.ColorValue colorValue = (Value.ColorValue) value;
                    settings.drawColorValue(startX,mY,startX + 300, mouseX,mouseY, colorValue);
                    if (!Mouse.isButtonDown(0)) {
                        this.previousMouse = false;
                    }
                    mY += 20;
                }
                if (value instanceof BoolValue) {
                    BoolValue boolValue = (BoolValue) value;
                    float x = startX + 325;
                    settings.drawBoolValue(mouse,mouseX,mouseY,startX,mY,boolValue);
                    if (this.isCheckBoxHovered(x + 30, mY - 2, x + 50, mY + 8, mouseX, mouseY)) {
                        if (!this.previousMouse && Mouse.isButtonDown(0)) {
                            this.previousMouse = true;
                            this.mouse = true;
                        }
                        if (this.mouse) {
                            boolValue.set(!boolValue.get());
                            this.mouse = false;
                        }
                    }
                    mY += 20;
                }
                if(value instanceof TextValue) {
                    TextValue textValue = (TextValue) value;
                    settings.drawTextValue(startX, mY, textValue);
                    mY += 20;
                }
                if (value instanceof ListValue) {
                    float x = startX + 295;
                    ListValue listValue = (ListValue) value;
                    settings.drawListValue(previousMouse,mouseX,mouseY,mY,startX,listValue);
                    this.previousMouse = Mouse.isButtonDown(0);
                    mY += 25;
                }
            }
        }

        if (!moduleHover) {
            moduleAnimation = 0;
        }

        // set y pos to category
        if (currentCategory == ModuleCategory.COMBAT)
            categoryYpos = 35;
        if (currentCategory == ModuleCategory.MOVEMENT)
            categoryYpos = 70;
        if (currentCategory == ModuleCategory.WORLD)
            categoryYpos = 105;
        if (currentCategory == ModuleCategory.PLAYER)
            categoryYpos = 140;
        if (currentCategory == ModuleCategory.EXPLOIT)
            categoryYpos = 175;
        if (currentCategory == ModuleCategory.MISC)
            categoryYpos = 210;
        if (currentCategory == ModuleCategory.CLIENT)
            categoryYpos = 245;
        if (currentCategory == ModuleCategory.RENDER)
            categoryYpos = 280;

        // detect mouse clicking on categories
        // and set animation if hovered over
        if(categoryMouse && !Mouse.isButtonDown(0))
            categoryMouse = false;
        if (isCategoryHovered(startX - 5, startY + 25, startX + 35 + categoryAnimation, startY + 13 + 45, mouseX, mouseY)) {
            categoryYpos = 35;
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.COMBAT) {
                    currentCategory = ModuleCategory.COMBAT;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX - 5, startY + 3 + 55, startX + 35 + categoryAnimation, startY + 13 + 80, mouseX, mouseY)) {
            categoryYpos = 70;
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.MOVEMENT) {
                    currentCategory = ModuleCategory.MOVEMENT;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX - 5, startY + 3 + 90, startX + 35 + categoryAnimation, startY + 13 + 115, mouseX, mouseY)) {
            categoryYpos = 105;
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.WORLD) {
                    currentCategory = ModuleCategory.WORLD;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX - 5, startY + 3 + 125, startX + 35 + categoryAnimation, startY + 13 + 150, mouseX, mouseY)) {
            categoryYpos = 140;
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.PLAYER) {
                    currentCategory = ModuleCategory.PLAYER;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX - 5, startY + 3 + 160, startX + 35 + categoryAnimation, startY + 13 + 185, mouseX, mouseY)) {
            categoryYpos = 175;
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.EXPLOIT) {
                    currentCategory = ModuleCategory.EXPLOIT;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX - 5, startY + 3 + 195, startX + 35 + categoryAnimation, startY + 13 + 220, mouseX, mouseY)) {
            categoryYpos = 210;
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.MISC) {
                    currentCategory = ModuleCategory.MISC;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX - 5, startY + 3 + 230, startX + 35 + categoryAnimation, startY + 13 + 255, mouseX, mouseY)) {
            categoryYpos = 245;
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.CLIENT) {
                    currentCategory = ModuleCategory.CLIENT;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX - 5, startY + 3 + 265, startX + 35 + categoryAnimation, startY + 13 + 290, mouseX, mouseY)) {
            categoryYpos = 280;
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.RENDER) {
                    currentCategory = ModuleCategory.RENDER;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = LiquidBounce.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }


        if (isCategoryHovered(startX - 5, startY + 25, startX + categoryAnimation + 53, startY + 310, mouseX, mouseY)) {
            categoryAnimation += Math.round((50 - categoryAnimation) / 3.5);
        } else {
            categoryAnimation += Math.round((0 - categoryAnimation) / 3.5);
        }

        // gradient shadwo things
        RenderUtils.drawGradientSideways(startX + categoryAnimation + 48, startY + 25, startX + categoryAnimation + 55, startY + 310,
                new Color(0, 0, 0, 190).getRGB(), new Color(0, 0, 0, 0).getRGB());//255,255,255,30

        RenderUtils.drawGradientSideways(startX + 200, startY + 25, startX + 205, startY + 310,
                new Color(0, 0, 0, 70).getRGB(), new Color(0,0,0,0).getRGB());//239,237,237,30

        // grayish bg of categories
        RenderUtils.drawRoundedRect2((int) startX - 5, (int) startY + 25, (int) startX + 50 + categoryAnimation, (int) startY + 310, 1,
                new Color(255, 255, 255, 250).getRGB());

        // blue box that highlights categories;ll;l
        RenderUtils.drawRoundedRect2((int) startX + 6, (int) startY + animationHeight - 5, (int) startX + 38 + categoryAnimation, (int) startY + animationHeight + 17, 5,
                new Color(29, 143, 237, 250).getRGB());


        if (categoryAnimation > 4) {
            Fonts.font40.drawString("Combat",   startX + 30 + Math.round(categoryAnimation / 5), startY + 35 + 6 - fontHeight,  new Color(30, 30, 30, Math.round(categoryAnimation * 5)).getRGB());
            Fonts.font40.drawString("Move",     startX + 30 + Math.round(categoryAnimation / 5), startY + 70 + 6 - fontHeight,  new Color(30, 30, 30, Math.round(categoryAnimation * 5)).getRGB());
            Fonts.font40.drawString("World",    startX + 30 + Math.round(categoryAnimation / 5), startY + 105 + 6 - fontHeight, new Color(30, 30, 30, Math.round(categoryAnimation * 5)).getRGB());
            Fonts.font40.drawString("Player",   startX + 30 + Math.round(categoryAnimation / 5), startY + 140 + 6 - fontHeight, new Color(30, 30, 30, Math.round(categoryAnimation * 5)).getRGB());
            Fonts.font40.drawString("Exploit",  startX + 30 + Math.round(categoryAnimation / 5), startY + 175 + 6 - fontHeight, new Color(30, 30, 30, Math.round(categoryAnimation * 5)).getRGB());
            Fonts.font40.drawString("Misc",     startX + 30 + Math.round(categoryAnimation / 5), startY + 210 + 6 - fontHeight, new Color(30, 30, 30, Math.round(categoryAnimation * 5)).getRGB());
            Fonts.font40.drawString("Client",   startX + 30 + Math.round(categoryAnimation / 5), startY + 245 + 6 - fontHeight, new Color(30, 30, 30, Math.round(categoryAnimation * 5)).getRGB());
            Fonts.font40.drawString("Render",   startX + 30 + Math.round(categoryAnimation / 5), startY + 280 + 6 - fontHeight, new Color(30, 30, 30, Math.round(categoryAnimation * 5)).getRGB());
        }
        searchBox.drawTextBox();
        // 判断category所处的位置是否被按下或者被略过

        int Hdiff = animationHeight - categoryYpos;

        animationHeight += Math.round((categoryYpos - animationHeight)/2.5);

        if ( Hdiff > -3 && Hdiff < 0 ) {
            animationHeight = categoryYpos;
        }
        if ( Hdiff < 3 && Hdiff > 0 ) {
            animationHeight = categoryYpos;
        }

        RenderUtils.drawImage(new ResourceLocation("fdpclient/ui/clickgui/light/Combat.png"), (int) startX + 17, (int) startY + 35, 12, 12);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/ui/clickgui/light/Movement.png"), (int) startX + 17, (int) startY + 70, 12, 12);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/ui/clickgui/light/World.png"), (int) startX + 17, (int) startY + 105, 12, 12);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/ui/clickgui/light/Player.png"), (int) startX + 17, (int) startY + 140, 12, 12);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/ui/clickgui/light/Exploit.png"), (int) startX + 17, (int) startY + 175, 12, 12);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/ui/clickgui/light/Misc.png"), (int) startX + 17, (int) startY + 210, 12, 12);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/ui/clickgui/light/Client.png"), (int) startX + 17, (int) startY + 245, 12, 12);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/ui/clickgui/light/Render.png"), (int) startX + 17, (int) startY + 280, 12, 12);

    }

    public boolean isStringHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2;
    }

    public boolean isSettingsButtonHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    public boolean isButtonHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2;
    }

    public boolean isCheckBoxHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2;
    }

    public boolean isCategoryHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    public boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

}
