/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.launch.data.legacyui.clickgui.style.styles.light;

import java.awt.*;
import java.io.IOException;

import net.skiddermc.fdpclient.launch.data.legacyui.clickgui.style.styles.light.LightSettings.InputBox;
import net.skiddermc.fdpclient.launch.data.legacyui.clickgui.style.styles.light.ModuleSettings.Setting.Settings;
import net.skiddermc.fdpclient.utils.AnimationHelper;
import net.skiddermc.fdpclient.FDPClient;
import net.skiddermc.fdpclient.features.module.*;
import net.skiddermc.fdpclient.utils.render.RenderUtils;
import net.skiddermc.fdpclient.ui.client.hud.designer.GuiHudDesigner;
import net.skiddermc.fdpclient.ui.font.Fonts;
import net.skiddermc.fdpclient.value.*;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;


public class LightClickGUI extends GuiScreen implements GuiYesNoCallback {
    private ModuleCategory currentCategory = ModuleCategory.COMBAT;
    private Module currentModule = FDPClient.moduleManager.getModuleInCategory(currentCategory).get(0);
    private float startX = 50, startY = 25;
    private int moduleStart = 0;
    private int valueStart = 0;
    private boolean previousMouse = true;
    private boolean mouse;
    private float moveX = 0, moveY = 0;
    private final FontRenderer defaultFont = Fonts.font35;
    private final FontRenderer logoFont = Fonts.font40;
    private boolean rightClickMouse = false;
    private boolean categoryMouse = false;
    private int animationHeight = 0;
    private float guiScale = 0;
    private final AnimationHelper alphaAnim = new AnimationHelper();
    private final AnimationHelper valueAnim = new AnimationHelper();
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
        RenderUtils.drawImage(new ResourceLocation( "fdpclient/clickgui/Hud.png"), 9, height - 41, 32, 32);
        if(!firstSetAnimation) {
            for (Module i : FDPClient.moduleManager.getModules()) {
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
        searchBox.xPosition = (int) startX;
        searchBox.yPosition = (int) (startY + 20);
        this.searchBox.setMaxStringLength(20);
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
        if(guiScale < 100)
            guiScale += 10;//启动动画
        GlStateManager.scale(guiScale / 100, guiScale / 100, guiScale / 100);
        Settings settings = new Settings(valueAnim);//value的settings
        if (Mouse.isButtonDown(0) && mouseX >= 5 && mouseX <= 50 && mouseY <= height - 5 && mouseY >= height - 50)
            mc.displayGuiScreen(new GuiHudDesigner()); //进入自定义HUD界面
        if (isHovered(startX - 5, startY, startX + 400, startY + 25, mouseX, mouseY) && Mouse.isButtonDown(0)) {//移动窗口
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

        RenderUtils.drawRoundedRect2((int) startX - 5, (int) startY, (int) startX + 400, (int) startY + 280,4,
                new Color(239, 237, 237).getRGB());
        //drawBorderedRect(startX + 130, startY + 7, startX + 190, startY + 15, 0.5F, -1, new Color(100,100,100).getRGB());
        defaultFont.drawString(searchBox.getText().isEmpty() && !searchBox.isFocused() ? "Search..." : searchBox.getText(), (int) (startX + 3), (int) (startY + 25), new Color(80, 80, 80).getRGB());
        if(currentModule == null) {
            logoFont.drawStringWithShadow("Empty Modules", startX + 80, startY + 130, new Color(100,100,100).getRGB());
        }
//        if(isHovered(startX + 130, startY + 7, startX + 190, startY + 15, mouseX, mouseY) && Mouse.isButtonDown(0))
//            hovered = true;
//        else if(!isHovered(startX + 130, startY + 7, startX + 190, startY + 15, mouseX, mouseY) && Mouse.isButtonDown(0))
//            hovered = false;
        //   FontManager.logo24.drawCenteredStringWithShadow("L", startX + 5, startY + 8, new Color(49, 86, 255).getRGB(),255);
        //FontManager.logo24.drawCenteredStringWithShadow("ight", startX + 5 + FontManager.logo24.getStringWidth("L") + 8, startY + 8, new Color(255,255,255).getRGB(),255); // 客户端名字
        Fonts.font35.drawString("FDPClient", startX + 5, startY + 5, new Color(80,80,80).getRGB());
        for(int i = 0; i < ModuleCategory.values().length; i++) {
            ModuleCategory[] categories = ModuleCategory.values();
            if (categories[i] == currentCategory) {
                int lastHeight = i * 45;
                RenderUtils.drawSuperCircle(startX - 5, startY + 50 + animationHeight, 5, new Color(100, 100,255).getRGB());
                if (animationHeight < lastHeight) {
                    if (lastHeight - animationHeight < 30) {
                        animationHeight += 3;
                    } else {
                        animationHeight += 6;
                    }
                } else if (animationHeight > lastHeight) {
                    if (animationHeight - lastHeight < 30) {
                        animationHeight -= 3;
                    } else {
                        animationHeight -= 6;
                    }
                }
            }
        }
        int m = Mouse.getDWheel();//鼠标滚轮.
        if(searchBox.getText().isEmpty()) {
            if (this.isCategoryHovered(startX + 60, startY + 40, startX + 200, startY + 280, mouseX, mouseY)) {
                if (m < 0 && moduleStart < FDPClient.moduleManager.getModuleInCategory(currentCategory).size() - 8) {
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
        logoFont.drawString(currentCategory.getDisplayName(), (int) (startX + 60), (int) (startY + 10),
                new Color(100, 100, 100,alphaAnim.getAlpha()).getRGB());
        RenderUtils.circle(startX + 390, startY + 8, 1.5F, new Color(31, 158, 255).getRGB());
        if (isCheckBoxHovered(startX + 388, startY + 6, startX + 391, startY + 9, mouseX, mouseY)
                && Mouse.isButtonDown(0)) {
            mc.displayGuiScreen(new GuiExit());
        }
        if(!searchBox.getText().isEmpty()) {
            if (this.isCategoryHovered(startX + 60, startY + 40, startX + 200, startY + 280, mouseX, mouseY)) {
                if (m < 0 && moduleStart < FDPClient.moduleManager.getModuleInCategory(currentCategory).size() - 8) {
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
            float mY = startY + 30;
            for(int i = 0; i < FDPClient.moduleManager.getModulesByName(searchBox.getText()).size(); i++) {
                Module module = FDPClient.moduleManager.getModulesByName(searchBox.getText()).get(i);
                if (mY > startY + 250)
                    break;
                if (i < moduleStart) {
                    continue;
                }
                int moduleColor = new Color(118, 117, 117,alphaAnim.getAlpha()).getRGB();
                if (isSettingsButtonHovered(startX + 160, mY, startX + 180, mY + 10, mouseX, mouseY)) {
                    if (!this.previousMouse && Mouse.isButtonDown(0)) {
                        module.setState(!module.getState());
                        previousMouse = true;
                    }
                    if (!this.previousMouse && Mouse.isButtonDown(1)) {
                        previousMouse = true;
                    }
                }
                RenderUtils.drawRoundedRect2(startX + 160, mY + 6, startX + 180, mY + 16, 4, module.getState() && module.getAnimation().getAnimationX() >= 3F ? new Color(70, 255, 70,alphaAnim.getAlpha()).getRGB() : new Color(114, 118, 125,alphaAnim.getAlpha()).getRGB());
                RenderUtils.circle(startX + 170 + module.getAnimation().getAnimationX(), mY + 11, 4, module.getState() ? new Color(255,255,255,alphaAnim.getAlpha()).getRGB() : new Color(164, 168, 175,alphaAnim.getAlpha()).getRGB());
                if(module.getAnimation().getAnimationX() > -5F && !module.getState())
                    module.getAnimation().animationX -= 1F;
                else if(module.getAnimation().getAnimationX() < 5F && module.getState())
                    module.getAnimation().animationX += 1F;
                defaultFont.drawString(module.getName(), (int) (startX + 65), (int) (mY + 6), moduleColor);
                defaultFont.drawString("KeyBind: " + (!Keyboard.getKeyName(module.getKeyBind()).equalsIgnoreCase("NONE") ? Keyboard.getKeyName(module.getKeyBind()) : "None"), (int) (startX + 65), (int) (mY + 13), new Color(80, 80, 80, alphaAnim.getAlpha()).getRGB());
                if (!Mouse.isButtonDown(0)) {
                    this.previousMouse = false;
                }
                if (isSettingsButtonHovered(startX + 50, mY - 8, startX + 200, mY + 20, mouseX, mouseY)
                        && Mouse.isButtonDown(1) && !rightClickMouse && currentModule != module) {
                    currentModule = module;
                    valueAnim.resetAlpha();
                    valueStart = 0;
                    rightClickMouse = true;
                }
                if (rightClickMouse && !Mouse.isButtonDown(1))
                    rightClickMouse = false;
                mY += 28;
            }
        }
        if (currentModule != null) {
            logoFont.drawString(currentModule.getName(), (int) (startX + 205), (int) (startY + 10),
                    new Color(100, 100, 100,valueAnim.getAlpha()).getRGB());
            float mY = startY + 30;
            if(searchBox.getText().isEmpty()) {
                for (int i = 0; i < FDPClient.moduleManager.getModuleInCategory(currentCategory).size(); i++) {
                    Module module = FDPClient.moduleManager.getModuleInCategory(currentCategory).get(i);
                    if (mY > startY + 250)
                        break;
                    if (i < moduleStart)
                        continue;
                    int moduleColor = new Color(118, 117, 117,alphaAnim.getAlpha()).getRGB();
                    if (isSettingsButtonHovered(startX + 160, mY, startX + 180, mY + 10, mouseX, mouseY)) {
                        if (!this.previousMouse && Mouse.isButtonDown(0)) {
                            module.setState(!module.getState());
                            previousMouse = true;
                        }
                        if (!this.previousMouse && Mouse.isButtonDown(1)) {
                            previousMouse = true;
                        }
                    }
                    RenderUtils.drawRoundedRect2(startX + 160, mY + 6, startX + 180, mY + 16, 4, module.getState() && module.getAnimation().getAnimationX() >= 3F ? new Color(70, 255, 70,alphaAnim.getAlpha()).getRGB() : new Color(114, 118, 125,alphaAnim.getAlpha()).getRGB());
                    RenderUtils.circle(startX + 170 + module.getAnimation().getAnimationX(), mY + 11, 4, module.getState() ? new Color(255,255,255,alphaAnim.getAlpha()).getRGB() : new Color(164, 168, 175,alphaAnim.getAlpha()).getRGB());
                    if(module.getAnimation().getAnimationX() > -5F && !module.getState())
                        module.getAnimation().animationX -= 1F;
                    else if(module.getAnimation().getAnimationX() < 5F && module.getState())
                        module.getAnimation().animationX += 1F;
                    defaultFont.drawString(module.getName(), (int) (startX + 65), (int) (mY + 6), moduleColor);
                    defaultFont.drawString("KeyBind: " + (!Keyboard.getKeyName(module.getKeyBind()).equalsIgnoreCase("NONE") ? Keyboard.getKeyName(module.getKeyBind()) : "None"), (int) (startX + 65), (int) (mY + 13), /*!module.getState() ? */new Color(80, 80, 80, alphaAnim.getAlpha()).getRGB() /*: new Color(220, 220, 220).getRGB()*/);
                    if (!Mouse.isButtonDown(0)) {
                        this.previousMouse = false;
                    }

                    if (isSettingsButtonHovered(startX + 50, mY - 8, startX + 200, mY + 20, mouseX, mouseY)
                            && Mouse.isButtonDown(1) && !rightClickMouse && currentModule != module) {
                        currentModule = module;
                        valueAnim.resetAlpha();
                        valueStart = 0;
                        rightClickMouse = true;
                    }
                    if (rightClickMouse && !Mouse.isButtonDown(1))
                        rightClickMouse = false;
                    mY += 28;
                }
            }
            mY = startY + 30;
            if (currentModule.getValues().isEmpty())
                logoFont.drawString("Empty Settings", (int) (startX + 250), (int) (startY + 130),
                        new Color(100, 100, 100,valueAnim.getAlpha()).getRGB());
            for (int i = 0; i < currentModule.getValues().size(); i++) {
                if (mY > startY + 260)
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
                    if (this.isStringHovered(x, mY - 5, x + 80, mY + 11, mouseX, mouseY)) {
                        this.previousMouse = Mouse.isButtonDown(0);
                    }
                    mY += 25;
                }
            }
        }
        if(categoryMouse && !Mouse.isButtonDown(0))
            categoryMouse = false;
        if (isCategoryHovered(startX + 10, startY + 35, startX + 40, startY + 65, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.COMBAT) {
                    currentCategory = ModuleCategory.COMBAT;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = FDPClient.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX + 10, startY + 82, startX + 40, startY + 112, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.PLAYER) {
                    currentCategory = ModuleCategory.PLAYER;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = FDPClient.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX + 10, startY + 128, startX + 40, startY + 158, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.MOVEMENT) {
                    currentCategory = ModuleCategory.MOVEMENT;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = FDPClient.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX + 10, startY + 170, startX + 40, startY + 202, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.RENDER) {
                    currentCategory = ModuleCategory.RENDER;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = FDPClient.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        if (isCategoryHovered(startX + 10, startY + 218, startX + 40, startY + 247, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0) && !categoryMouse) {
                if (currentCategory != ModuleCategory.WORLD) {
                    currentCategory = ModuleCategory.WORLD;
                    categoryMouse = true;
                    if(searchBox.getText().isEmpty()) {
                        moduleStart = 0;
                        currentModule = FDPClient.moduleManager.getModuleInCategory(currentCategory).get(0);
                        alphaAnim.resetAlpha();
                        valueAnim.resetAlpha();
                    }
                }
            }
        }
        searchBox.drawTextBox();
        // 判断category所处的位置是否被按下或者被略过
        RenderUtils.drawGradientSideways(startX + 50, startY, startX + 55, startY + 280, new Color(0, 0, 0, 70).getRGB(),
                new Color(0, 0, 0,0).getRGB());//255,255,255,30

        RenderUtils.drawGradientSideways(startX + 200, startY, startX + 205, startY + 280,
                new Color(0, 0, 0, 70).getRGB(), new Color(0,0,0,0).getRGB());//239,237,237,30
        RenderUtils.circle(startX + 25, startY + 50, 15,
                isCategoryHovered(startX + 10, startY + 35, startX + 40, startY + 65, mouseX, mouseY)
                        || currentCategory == ModuleCategory.COMBAT ? new Color(180, 180, 180).getRGB() : new Color(255,255,255).getRGB());
        RenderUtils.circle(startX + 25, startY + 97, 15,
                isCategoryHovered(startX + 10, startY + 82, startX + 40, startY + 112, mouseX, mouseY)
                        || currentCategory == ModuleCategory.PLAYER ? new Color(180,180,180).getRGB() : new Color(255,255,255).getRGB());
        RenderUtils.circle(startX + 25, startY + 143, 15,
                isCategoryHovered(startX + 10, startY + 128, startX + 40, startY + 158, mouseX, mouseY)
                        || currentCategory == ModuleCategory.MOVEMENT ? new Color(180,180,180).getRGB() : new Color(255,255,255).getRGB());
        RenderUtils.circle(startX + 25, startY + 187, 15,
                isCategoryHovered(startX + 10, startY + 170, startX + 40, startY + 202, mouseX, mouseY)
                        || currentCategory == ModuleCategory.RENDER ? new Color(180,180,180).getRGB() : new Color(255,255,255).getRGB()); // 画出category的圆
        RenderUtils.circle(startX + 25, startY + 233, 15,
                isCategoryHovered(startX + 10, startY + 218, startX + 40, startY + 247, mouseX, mouseY)
                        || currentCategory == ModuleCategory.WORLD ? new Color(180,180,180).getRGB() : new Color(255,255,255).getRGB());
//        iconFont.drawString("1", startX + 14, startY + 43,
//                /*isCategoryHovered(startX + 10, startY + 35, startX + 40, startY + 65, mouseX, mouseY)
//                        || currentCategory == ModuleCategory.COMBAT ? -1 : */new Color(107, 107, 107).getRGB());
//        iconFont.drawString("6", startX + 14, startY + 90,
//                /*isCategoryHovered(startX + 10, startY + 82, startX + 40, startY + 112, mouseX, mouseY)
//                        || currentCategory == ModuleCategory.PLAYER ? -1 : */new Color(107, 107, 107).getRGB());
//        iconFont.drawString("5", startX + 14, startY + 136,
//                /*isCategoryHovered(startX + 10, startY + 128, startX + 40, startY + 158, mouseX, mouseY)
//                        || currentCategory == ModuleCategory.MOVEMENT ? -1 : */new Color(107, 107, 107).getRGB());
//        iconFont.drawString("2", startX + 14, startY + 180,
//                /*isCategoryHovered(startX + 10, startY + 170, startX + 40, startY + 202, mouseX, mouseY)
//                        || currentCategory == ModuleCategory.RENDER ? -1 : */new Color(107, 107, 107).getRGB());
//        iconFont.drawString("3", startX + 14, startY + 226,
//                /*isCategoryHovered(startX + 10, startY + 218, startX + 40, startY + 247, mouseX, mouseY)
//                        || currentCategory == ModuleCategory.WORLD ? -1 : */new Color(107, 107, 107).getRGB());
        RenderUtils.drawImage(new ResourceLocation("fdpclient/clickgui/Combat.png"), (int) startX + 17, (int) startY + 43, 16, 16);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/clickgui/Player.png"), (int) startX + 17, (int) startY + 90, 16, 16);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/clickgui/Move.png"), (int) startX + 17, (int) startY + 136, 16, 16);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/clickgui/Render.png"), (int) startX + 17, (int) startY + 180, 16, 16);
        RenderUtils.drawImage(new ResourceLocation("fdpclient/clickgui/World.png"), (int) startX + 17, (int) startY + 226, 16, 16);
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