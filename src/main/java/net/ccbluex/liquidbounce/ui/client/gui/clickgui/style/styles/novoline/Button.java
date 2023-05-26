package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.novoline;


import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.Value;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class Button {
    public Module cheat;
    public Window parent;
    public int x;
    public float y;
    public int index;
    public int remander;
    public ArrayList<ValueButton> buttons = new ArrayList<>();
    public boolean expand;

    public Button(Module cheat, int x, int y) {
        this.cheat = cheat;
        this.x = x;
        this.y = y;
        int y2 = y + 15;
        for (Value v : this.cheat.getValues()) {
            buttons.add(new ValueButton(v, this.x + 5, y2));
            y2 += 15;
        }
    }

    int smoothalpha;
    float animationsize;

    public float processFPS(float fps, float defF, float defV) {
        return defV / (fps / defF);
    }

    public long rticks;
    public void render(int mouseX, int mouseY) {
        final GameFontRenderer font = Fonts.font35;
        float y2 = y + 15;
        buttons.clear();
        for (Value v : cheat.getValues()) {
            buttons.add(new ValueButton(v, x + 5, y2));
            y2 += 15;
        }
        if (index != 0) {
            int FPS = Minecraft.getMinecraft().getDebugFPS() == 0 ? 1 : Minecraft.getMinecraft().getDebugFPS();
            Button b2 = parent.buttons.get(index - 1);
            y = b2.y + 15 + animationsize;
            if (b2.expand) {
                parent.buttonanim = true;
                animationsize = AnimationUtil.moveUD(animationsize, 15 * b2.buttons.size(), processFPS(FPS, 1000, 0.013F), processFPS(FPS, 1000, 0.011F));
            } else {
                parent.buttonanim = true;
                animationsize = AnimationUtil.moveUD(animationsize, 0, processFPS(FPS, 1000, 0.013F), processFPS(FPS, 1000, 0.011F));
            }
        }
        if (parent.buttonanim) {
            parent.buttonanim = false;
        }
        int i = 0;
        final float size = buttons.size();
        while (i < size) {
            buttons.get(i).y = y + 17 + 15 * i;
            buttons.get(i).x = x + 5;
            ++i;
        }
        smoothalphas();
        GL11.glPushMatrix();
        //GL11.glEnable(3089);
        //limitation.cut();
        RenderUtils.drawRect(x - 5, y - 5, x + 85 + parent.allX, y + font.FONT_HEIGHT + 5, new Color(40, 40, 40).getRGB());
        RenderUtils.drawRect(x - 5, y - 5 - 1, x + 85 + parent.allX, y + font.FONT_HEIGHT + 3 + 1,
                hudcolorwithalpha());//Button Font List
        rticks++;

        Color Ranbow = ClickGUIModule.INSTANCE.getColorRainbow().get() ? new Color(Color.HSBtoRGB(
                (float) ((double) Minecraft.getMinecraft().thePlayer.ticksExisted / 50.0 + Math.sin((double) 0 / 50.0 * 1.6))
                        % 1.0f,
                0.5f, 1.0f)) : ClickGUIModule.INSTANCE.generateColor();
        ValueButton.valuebackcolor = Ranbow.getRGB();
        if (!expand && size >= 1) {
            Fonts.font35.drawString("+", x + 75 + parent.allX, y - 1, -1);
        } else if (size >= 1) {
            //RenderUtils.drawRect(x - 5, y - 5, x + 85 + parent.allX, y + font.FONT_HEIGHT + 5, new Color(40, 40, 40).getRGB());
            Fonts.font35.drawString("-", x + 75 + parent.allX, y - 1, -1);
        }
        font.drawStringWithShadow(cheat.getName(), x, y - 1, -1);
        if (expand) {
            buttons.forEach(b -> b.render(mouseX, mouseY, parent));
        }
        //GL11.glDisable(3089);
        GL11.glPopMatrix();
    }

    private int hudcolorwithalpha() {
        //if(HUD.Breathinglamp.get()){

        Color Ranbow = new Color(Color.HSBtoRGB(
                (float) ((double) Minecraft.getMinecraft().thePlayer.ticksExisted / 50.0 + Math.sin((double) 50 / 50.0 * 1.6))
                        % 1.0f,
                0.5f, 1.0f));
        return cheat.getState() ? Ranbow.getRGB() : new Color(40,40,40).getRGB();
        //} else {
            //return new Color(HUD.r.getValue().intValue(), HUD.g.getValue().intValue(), HUD.b.getValue().intValue(), smoothalpha).getRGB();
        //}
    }

    private void smoothalphas() {
        int FPS = Minecraft.getMinecraft().getDebugFPS() == 0 ? 1 : Minecraft.getMinecraft().getDebugFPS();
        if (cheat.getState()) {
            smoothalpha = (int) AnimationUtil.moveUD(smoothalpha, 255, processFPS(FPS, 1000, 0.013F), processFPS(FPS, 1000, 0.011F));
        } else {
            smoothalpha = (int) AnimationUtil.moveUD(smoothalpha, 0, processFPS(FPS, 1000, 0.013F), processFPS(FPS, 1000, 0.011F));
        }
    }

    public void key(char typedChar, int keyCode) {
        buttons.forEach(b -> b.key(typedChar, keyCode));
    }

    public void click(int mouseX, int mouseY, int button) {
        if (parent.drag) {
            return;
        }
        if (mouseX > x - 7 && mouseX < x + 85 + parent.allX && mouseY > y - 6 && mouseY < y + Fonts.font35.FONT_HEIGHT) {
            if (button == 0) {
                this.cheat.setState(!this.cheat.getState());
            }
            if (button == 1 && !buttons.isEmpty()) {
                expand = !expand;
            }
        }
        if (expand) {
            buttons.forEach(b -> b.click(mouseX, mouseY, button));
        }
    }

    public void setParent(Window parent) {
        this.parent = parent;
        for (int i = 0; i < this.parent.buttons.size(); ++i) {
            if (this.parent.buttons.get(i) != this) continue;
            index = i;
            remander = this.parent.buttons.size() - i;
            break;
        }
    }
}
