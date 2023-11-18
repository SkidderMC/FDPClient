package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.novoline;


import com.google.common.collect.Lists;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.Translate;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;


public class Window {
    public final ModuleCategory category;
    public final ArrayList<Button> buttons = Lists.newArrayList();
    public boolean drag;
    public boolean extended;
    public int x;
    public int y;
    public float expand;
    public int dragX;
    public int dragY;
    public final int max;
    public int scroll;
    public int scrollTo;
    int allX;
    final Translate translate = new Translate(0F);

    public Window(ModuleCategory category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
        max = 120;
        int y2 = y + 22;
        for (Module c : FDPClient.moduleManager.getModules()) {
            if (c.getCategory() != category)
                continue;
            buttons.add(new Button(c, x + 5, y2));
            y2 += 15;//15
        }
        for (Button b2 : buttons) {
            b2.setParent(this);
        }
    }
    int wheely;
    public float totalY;
    boolean buttonanim;

    public void render(int mouseX, int mouseY) {
        boolean isOnPanel;
        int current = 0;
        int iY = y + 22;
        totalY = 17;
        for (Button b3 : buttons) {
            b3.y = (int) (iY - translate.getY());
            iY += 15;
            totalY += 15;
            if (b3.expand) {
                for (ValueButton ignored : b3.buttons) {
                    current += 15;//15
                    totalY += 15;//15
                }
            }
            current += 15;
        }

        allX = 12;
        int height = 15 + current;
        if (category.name().equals("Misc")) {
            if (height > 250 - 143) {
                height = 250 - 143;
            }
        }
        if (category.name().equals("Player")) {
            if (height > 230 - 93) {
                height = 230 - 93;
            }
        }
        if (category.name().equals("Movement")) {
            if (height > 316 - 44) {
                height = 316 - 44;
            }
        }
        if (category.name().equals("Combat")) {
            if (height > 270 - 58) {
                height = 270 - 58;
            }
        }
        if (!category.name().equals("Player") && !category.name().equals("Misc")
                && !category.name().equals("Combat") && !category.name().equals("Movement")) {
            if (height > 316) {
                height = 316;
            }
        }
        isOnPanel = mouseX > x - 2 && mouseX < x + 92 && mouseY > y - 2 && mouseY < y + expand;
        translate.interpolate(0, wheely, 0.15F);
        if (isOnPanel) {
            runWheel(height);
        }
        if (extended) {
            if (buttonanim) {
                expand = AnimationUtil.moveUD(expand, height, 0.2f, 0.15f);
            } else {
                if (!category.name().equals("World")) {
                    expand = AnimationUtil.moveUD(expand, height, 0.5f, 0.5f);
                } else {
                    expand = AnimationUtil.moveUD(expand, height, 0.2f, 0.15f);
                }
            }
        } else {
            if (buttonanim) {
                expand = AnimationUtil.moveUD(expand, 0, 0.2f, 0.15f);
            } else {
                if (!category.name().equals("World")) {
                    expand = AnimationUtil.moveUD(expand, 0, 0.5f, 0.5f);
                } else {
                    expand = AnimationUtil.moveUD(expand, 0, 0.2f, 0.15f);
                }
            }
        }
        Gui.drawRect(x - 1, y, x + 91 + allX, y + 17, new Color(29, 29, 29).getRGB());
        RenderUtils.drawBorderedRect(x - 0.5, y - 0.5, x + 91 + allX, y + expand, 0.05f, new Color(29, 29, 29).getRGB(), new Color(40, 40, 40).getRGB());


        if (category.name().equals("Misc")) {
            Fonts.font35.drawStringWithShadow("Exploit", x + 4, y + 5, -1);
        }
        if (category.name().equals("World")) {
            Fonts.font35.drawStringWithShadow("Misc", x + 4, y + 5, -1);
        }
        if (category.name().equals("Render")) {
            Fonts.font35.drawStringWithShadow("Visuals", x + 4, y + 5, -1);
        }
        if (!category.name().equals("Render") && !category.name().equals("Misc") && !category.name().equals("World")) {
            Fonts.font35.drawStringWithShadow(category.name(), x + 4, y + 5, -1);
        }
        //Icons start
        final GameFontRenderer novoicons = Fonts.font35;
        if (category.name().equals("Combat")) {
            novoicons.drawString("a", x + 78 + allX, y + 7, -1);
        }
        if (category.name().equals("Misc")) {
            novoicons.drawString("G", x + 78 + allX, y + 5, -1);
        }
        if (category.name().equals("Render")) {
            novoicons.drawString("f", x + 78 + allX, y + 6, -1);
        }
        if (category.name().equals("Player")) {
            novoicons.drawString("b", x + 78 + allX, y + 6, -1);
        }
        if (category.name().equals("Movement")) {
            novoicons.drawString("m", x + 78 + allX, y + 6, -1);
        }
        if (category.name().equals("World")) {
            novoicons.drawString("c", x + 78 + allX, y + 5, -1);
        }
        //GL11.glScissor(x - 1, y - 1, x + 91 + allX,  y + (int)expand);
        //GL11.glEnable(GL11.GL_SCISSOR_TEST);

        //Icons end
        if (expand > 0) {
            buttons.forEach(b2 -> b2.render(mouseX, mouseY));
        }

        //GL11.glDisable(GL11.GL_SCISSOR_TEST);
        if (drag) {
            if (!Mouse.isButtonDown(0)) {
                drag = false;
            }
            x = mouseX - dragX;
            y = mouseY - dragY;
            buttons.get(0).y = y + 22 - scroll;
            for (Button b4 : buttons) {
                b4.x = x + 5;//Button X�����λ��
            }
        }

    }

    protected void runWheel(int height) {
        if (Mouse.hasWheel()) {
            int wheel = Mouse.getDWheel();
            if (totalY - height <= 0) {
                return;
            }
            if (wheel < 0) {
                if (wheely < totalY - height) {
                    wheely += 20 + Mouse.getDWheel();
                    if (wheely < 0) {
                        wheely = 0;
                    }
                }
            } else if (wheel > 0) {
                wheely -= 20 + Mouse.getDWheel();
                if (wheely < 0) {
                    wheely = 0;
                }
            }
        }
    }

    public void key(char typedChar, int keyCode) {
        buttons.forEach(b2 -> b2.key(typedChar, keyCode));
    }

    public void mouseScroll(int mouseX, int mouseY, int amount) {
        if (mouseX > x - 2 && mouseX < x + 92 && mouseY > y - 2 && mouseY < y + 17 + expand) {
            scrollTo = (int) ((float) scrollTo - (amount / 120 * 28));
        }
    }

    public void click(int mouseX, int mouseY, int button) {
        if (mouseX > x - 2 && mouseX < x + 92 && mouseY > y - 2 && mouseY < y + 17) {
            if (button == 1) {
                extended = !extended;
            }
            if (button == 0) {
                drag = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            }
        }
        if (extended) {
            buttons.stream().filter(b2 -> b2.y < y + expand).forEach(b2 -> b2.click(mouseX, mouseY, button));
        }
    }
}