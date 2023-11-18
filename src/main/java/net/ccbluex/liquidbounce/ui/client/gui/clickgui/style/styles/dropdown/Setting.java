/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.dropdown;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.impl.Fonts;
import net.ccbluex.liquidbounce.utils.MathUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.TickTimer;
import net.ccbluex.liquidbounce.features.value.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.stream.Collectors;
public class Setting {
    public Value setting;
    private Module module;
    public boolean opened;
    private final TickTimer backSpace = new TickTimer();
    private final TickTimer caretTimer = new TickTimer();
    public int height;
    public float percent = 0;

    public Setting(Value setting, Module module) {
        this.setting = setting;
        this.module = module;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public void drawScreen(int mouseX, int mouseY) {
        int y = getY();
        HUD hud = (HUD) FDPClient.moduleManager.getModule(HUD.class);
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        boolean scissor = scaledResolution.getScaleFactor() != 1;
        double clamp = MathHelper.clamp_double(Minecraft.getMinecraft().getDebugFPS() / 30, 1, 9999);


        if (setting instanceof FloatValue) {
            final FloatValue numberValue = (FloatValue) setting;
            if (module.yPerModule == module.getY() && scissor) {
                GL11.glPushMatrix();
                GL11.glScissor((int) (module.tab.getPosX() * 2 + 1), 0, 197, 999999999);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }

            double rounded = (int) (numberValue.get() * 100.0D) / 100.0D;
            final double percentBar = (numberValue.get()- numberValue.getMinimum()
                  ) / (numberValue.getMaximum() - numberValue.getMinimum());

            percent = Math.max(0, Math.min(1, (float) (percent + (Math.max(0, Math.min(percentBar, 1)) - percent) * (0.2 / clamp))));
            RenderUtils.drawRect(module.tab.getPosX() + 1, y + 3, module.tab.getPosX() + 99, y + 14, new Color(0, 0, 0, 50).getRGB());
            RenderUtils.drawRect(module.tab.getPosX() + 1, y + 3, module.tab.getPosX() + 1 + 98 * percent, y + 14, ClickGUIModule.INSTANCE.generateColor());
            Fonts.SF.SF_18.SF_18.drawString(numberValue.getName() + " " + rounded, module.tab.getPosX() + 4, y + 5.5f, 0xffffffff, true);

            if (this.dragging) {
                double difference = numberValue.getMaximum() - numberValue.getMinimum();
                double value = numberValue.getMinimum() +
                        MathHelper.clamp_double((mouseX - (module.tab.getPosX() + 1)) / 99, 0, 1) * difference;
                double set = MathUtils.INSTANCE.incValue(value,0.01);

                numberValue.set(set);
             //   EventManager.call(new SettingEvent(module.getModule(), setting.getName(), setting.getSliderNumber()));
            }

            if (module.yPerModule == module.getY() && scissor) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL11.glPopMatrix();
            }

        }
        if (setting instanceof IntegerValue) {
            final IntegerValue integerValue = (IntegerValue) setting;
            if (module.yPerModule == module.getY() && scissor) {
                GL11.glPushMatrix();
                GL11.glScissor((int) (module.tab.getPosX() * 2 + 1), 0, 197, 999999999);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }

            double rounded = (int) (integerValue.get() * 100.0D) / 100.0D;
            final double percentBar = (integerValue.get().doubleValue()- integerValue.getMinimum()
            ) / (integerValue.getMaximum() - integerValue.getMinimum());

            percent = Math.max(0, Math.min(1, (float) (percent + (Math.max(0, Math.min(percentBar, 1)) - percent) * (0.2 / clamp))));
            RenderUtils.drawRect(module.tab.getPosX() + 1, y + 3, module.tab.getPosX() + 99, y + 14, new Color(0, 0, 0, 50).getRGB());
            RenderUtils.drawRect(module.tab.getPosX() + 1, y + 3, module.tab.getPosX() + 1 + 98 * percent, y + 14, ClickGUIModule.INSTANCE.generateColor());
            Fonts.SF.SF_18.SF_18.drawString(integerValue.getName() + " " + rounded, module.tab.getPosX() + 4, y + 5.5f, 0xffffffff, true);

            if (this.dragging2) {
                double difference = integerValue.getMaximum() - integerValue.getMinimum();
                double value = integerValue.getMinimum() +
                        MathHelper.clamp_double((mouseX - (module.tab.getPosX() + 1)) / 99, 0, 1) * difference;
                double set = MathUtils.INSTANCE.incValue(value,1);

                integerValue.set(set);
                //   EventManager.call(new SettingEvent(module.getModule(), setting.getName(), setting.getSliderNumber()));
            }

            if (module.yPerModule == module.getY() && scissor) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL11.glPopMatrix();
            }

        }
        if (setting instanceof BoolValue) {
            final BoolValue boolValue = (BoolValue) setting;
            RenderUtils.drawRect(module.tab.getPosX() + 89, y + 4, module.tab.getPosX() + 99, y + 14, new Color(0, 0, 0, 50).getRGB());
            if (boolValue.get()) {
                RenderUtils.drawCheck(module.tab.getPosX() + 91, y + 8.5f, 2, ClickGUIModule.INSTANCE.generateColor().brighter().getRGB());
            }

            Fonts.SF.SF_18.SF_18.drawString(boolValue.getName(), module.tab.getPosX() + 4, y + 5.5f,
                    new Color(227, 227, 227, 255).getRGB(), true);

        }
        if (setting instanceof ListValue) {
            final ListValue listValue = (ListValue) setting;
            Fonts.SF.SF_17.SF_17.drawString(listValue.getName(), module.tab.getPosX() + 3, (float) (y + 6),
                    0xffffffff, true);
            Fonts.SF.SF_17.SF_17.drawString(listValue.get().toUpperCase(),
                    module.tab.getPosX() + 97 - Fonts.SF.SF_17.SF_17.stringWidth(listValue.get().toUpperCase()), y + 7f,
                    new Color(255, 255, 255, 255).getRGB(), true);
        }

       if (setting instanceof TextValue){
           final TextValue textValue = (TextValue) setting;
        final String s = textValue.get();

        if (textValue.getTextHovered() && Keyboard.isKeyDown(Keyboard.KEY_BACK) && this.backSpace.delay(100) && s.length() >= 1) {
            textValue.set(s.substring(0, s.length() - 1));
            this.backSpace.reset();
        }

        RenderUtils.drawRect(module.tab.getPosX() + 6, y + 16, module.tab.getPosX() + 84, y + 16.5, new Color(195, 195, 195, 220).getRGB());
        Fonts.SF.SF_16.SF_16.drawString(textValue.getName(), module.tab.getPosX() + 5.5f, y + 1.5f, new Color(227, 227, 227, 255).getRGB());

        if (Fonts.SF.SF_16.SF_16.stringWidth(s) > 65) {
            Fonts.SF.SF_16.SF_16.drawString(Fonts.SF.SF_16.SF_16.trimStringToWidth(s, 78, true), module.tab.getPosX() + 6, y + 10, 0xFFFFFFFF);
        } else {
            Fonts.SF.SF_16.SF_16.drawString(s, module.tab.getPosX() + 6, y + 10, 0xFFFFFFFF);
        }

    }


        }

    private int getY() {
        int y = module.y + 14;
        for (Setting dropDownSetting : module.settings.stream().filter(s -> s.setting.getDisplayable()).collect(Collectors.toList())) {
            if (dropDownSetting == this) {
                break;
            } else {
                y += dropDownSetting.getHeight();
            }
        }
        return y;
    }

    public int getHeight() {
            return 15;
    }

    private boolean dragging,dragging2;

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isHovered(mouseX, mouseY)) {

            if (setting instanceof BoolValue) {
                final BoolValue boolValue = (BoolValue) setting;
                if (mouseButton == 0) {
                  if (boolValue.get()){
                      boolValue.set(false);
                  }else {
                      boolValue.set(true);
                  }
                    //   EventManager.call(new SettingEvent(module.getModule(), boolValue.getName(), boolValue.getName(), setting.getCheckBoxProperty()));
                }
            }
          if (setting instanceof ListValue){
                if (mouseButton == 0) {
                    ListValue m = (ListValue)this.setting;
                    String current = m.get();
                    this.setting.set(m.getValues()[m.getModeListNumber(current) + 1 >= m.getValues().length?0:m.getModeListNumber(current) + 1]);
                   // EventManager.call(new SettingEvent(module.getModule(), setting.getName(), setting.getComboBoxProperty()));
                }
        }

         if (setting instanceof IntegerValue) {
             if (mouseButton == 0) {
                 dragging2 = true;
             }
         }
            if (setting instanceof FloatValue) {
                if (mouseButton == 0) {
                    dragging = true;
                }
            }

        }
        if (setting instanceof TextValue) {
            if (isHovered(mouseX, mouseY)) {
                setting.setTextHovered(!setting.getTextHovered());
            } else if (setting.getTextHovered()) {
                setting.setTextHovered(false);
            }
        }
/*
        if (setting.getSettingType() == SettingType.SELECTBOX) {
            if (opened && mouseX >= module.tab.getPosX() && mouseX <= module.tab.getPosX() + 90) {
                final List<String> acceptableValues = setting.getSelectBoxProperty().getAcceptableValues();
                for (int i = 0; i < acceptableValues.size(); i++) {
                    final int v = getY() + 17 + i * 17;
                    if (mouseY >= v && mouseY <= v + 17) {
                        final String s = acceptableValues.get(i);

                        if (setting.getSelectBoxProperty().contains(s)) {
                            setting.getSelectBoxProperty().remove(s);
                        } else {
                            setting.getSelectBoxProperty().add(s);
                        }

                        EventManager.call(new SettingEvent(module.getModule(), setting.getName(), setting.getSelectBoxProperty()));
                    }
                }
            }
        }
         */

    }

    public void keyTyped(char typedChar, int keyCode) {
        if (setting instanceof TextValue) {
            if (setting.getTextHovered()) {
                if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
                    setting.setTextHovered(false);
                } else if (!(keyCode == Keyboard.KEY_BACK) && keyCode != Keyboard.KEY_RCONTROL && keyCode != Keyboard.KEY_LCONTROL && keyCode != Keyboard.KEY_RSHIFT && keyCode != Keyboard.KEY_LSHIFT && keyCode != Keyboard.KEY_TAB && keyCode != Keyboard.KEY_CAPITAL && keyCode != Keyboard.KEY_DELETE && keyCode != Keyboard.KEY_HOME && keyCode != Keyboard.KEY_INSERT && keyCode != Keyboard.KEY_UP && keyCode != Keyboard.KEY_DOWN && keyCode != Keyboard.KEY_RIGHT && keyCode != Keyboard.KEY_LEFT && keyCode != Keyboard.KEY_LMENU && keyCode != Keyboard.KEY_RMENU && keyCode != Keyboard.KEY_PAUSE && keyCode != Keyboard.KEY_SCROLL && keyCode != Keyboard.KEY_END && keyCode != Keyboard.KEY_PRIOR && keyCode != Keyboard.KEY_NEXT && keyCode != Keyboard.KEY_APPS && keyCode != Keyboard.KEY_F1 && keyCode != Keyboard.KEY_F2 && keyCode != Keyboard.KEY_F3 && keyCode != Keyboard.KEY_F4 && keyCode != Keyboard.KEY_F5 && keyCode != Keyboard.KEY_F6 && keyCode != Keyboard.KEY_F7 && keyCode != Keyboard.KEY_F8 && keyCode != Keyboard.KEY_F9 && keyCode != Keyboard.KEY_F10 && keyCode != Keyboard.KEY_F11 && keyCode != Keyboard.KEY_F12) {
                    ((TextValue) setting).append(typedChar);
                }
            }

        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) dragging = false;
        if (state == 0) dragging2 = false;
    }

    /*
    private boolean areHovered(int mouseX, int mouseY) {
        int yS = getY() + 17;

        if (opened) {
            for (String value : setting.getSelectBoxProperty().getAcceptableValues()) {
                yS += 17;
            }
        }

        return mouseX <= module.tab.getPosX() && mouseY <= yS && mouseX >= module.tab.getPosX() + 90 && mouseY <= yS + 17;
    }

     */


    public boolean isHovered(int mouseX, int mouseY) {
        int y = getY();
        if (setting instanceof FloatValue){}

        if (setting instanceof IntegerValue){}

          if (setting instanceof BoolValue)
              return mouseX >= module.tab.getPosX() + 89 && mouseY >= y + 4 && mouseX <= module.tab
                      .getPosX() + 99 && mouseY <= y + 14;

          /*
            case BINDABLE:
                String key = "[" + Keyboard.getKeyName(setting.getKeyBindValue().get().getKey()) + "]";
                return mouseX >= module.tab.getPosX() + 97.5f - Fonts.SF.SF_18.SF_18.stringWidth(key) && mouseX <= module.tab.getPosX() + 97.5f && mouseY >= y + 4 && mouseY <= y + 14;

           */


              return mouseX >= module.tab.getPosX() && mouseY >= y && mouseX <= module.tab
                      .getPosX() + 90 && mouseY <= y + 17;


    }
}
