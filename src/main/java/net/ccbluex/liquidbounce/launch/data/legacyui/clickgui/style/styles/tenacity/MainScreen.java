package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.launch.data.legacyui.ClickGUIModule;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.impl.Fonts;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.impl.ModuleRect;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.animations.Animation;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.animations.Direction;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.normal.Main;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.normal.Screen;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.render.DrRenderUtils;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.render.StencilUtil;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.utils.math.MathUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.render.DrRenderUtils.isHovering;

public class MainScreen implements Screen {

    private final ModuleCategory category;
    private final float rectWidth = 110;
    private final float categoryRectHeight = 18;
    public Animation animation;
    public HashMap<ModuleRect, Animation> moduleAnimMap = new HashMap<>();
    public Animation openingAnimation;
    private List<ModuleRect> moduleRects;

    public MainScreen(ModuleCategory category) {
        this.category = category;
    }

    @Override
    public void initGui() {
        if (moduleRects == null) {
            moduleRects = new ArrayList<>();
            for (Module module : Main.getModulesInCategory(category,LiquidBounce.moduleManager).stream().sorted(Comparator.comparing(Module::getName)).collect(Collectors.toList())) {
                ModuleRect moduleRect = new ModuleRect(module);
                moduleRects.add(moduleRect);
                moduleAnimMap.put(moduleRect, new DecelerateAnimation(250, 1));
            }
        }

        if (moduleRects != null) {
            moduleRects.forEach(ModuleRect::initGui);
        }

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (moduleRects != null) {
            moduleRects.forEach(moduleRect -> moduleRect.keyTyped(typedChar, keyCode));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float animClamp = (float) Math.max(0, Math.min(255, 255 * animation.getOutput()));
        int alphaAnimation = (int) animClamp;
        int categoryRectColor = new Color(29, 29, 29, alphaAnimation).getRGB();
        int textColor = new Color(255, 255, 255, alphaAnimation).getRGB();

        category.getDrag().onDraw(mouseX, mouseY);
        float x = category.getDrag().getX(), y = category.getDrag().getY();
        DrRenderUtils.drawRect2(x, y, rectWidth, categoryRectHeight, categoryRectColor);
        DrRenderUtils.setAlphaLimit(0);
        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(category.name(), x + 5, y + Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.getMiddleOfBox(categoryRectHeight), textColor);

        // String icon = category.icon;
        //绘制图标
        String l = "";
        if (category.name().equalsIgnoreCase("Combat")) {
            l = "D";
        } else if (category.name().equalsIgnoreCase("Movement")) {
            l = "A";
        } else if (category.name().equalsIgnoreCase("Player")) {
            l = "B";
        } else if (category.name().equalsIgnoreCase("Render")) {
            l = "C";
        } else if (category.name().equalsIgnoreCase("Exploit")) {
            l = "G";
        } else if (category.name().equalsIgnoreCase("Misc")) {
            l = "F";
        }


        DrRenderUtils.setAlphaLimit(0);
        DrRenderUtils.resetColor();
        Fonts.ICONFONT.ICONFONT_20.ICONFONT_20.drawString(l, x + rectWidth - (Fonts.ICONFONT.ICONFONT_20.ICONFONT_20.stringWidth(l) + 5),
                y + Fonts.ICONFONT.ICONFONT_20.ICONFONT_20.getMiddleOfBox(categoryRectHeight), textColor);

        if (category.name().equalsIgnoreCase("World")){
            Fonts.CheckFont.CheckFont_20.CheckFont_20.drawString("b", x + rectWidth - (Fonts.CheckFont.CheckFont_20.CheckFont_20.stringWidth("b") + 5),
                    y + Fonts.ICONFONT.ICONFONT_20.ICONFONT_20.getMiddleOfBox(categoryRectHeight), textColor);
        }

        //     ClickGuiMod clickGUIMod = (ClickGuiMod) Tenacity.INSTANCE.getModuleCollection().get(ClickGuiMod.class);

        if (ClickGUIModule.scrollMode.get().equals("Value")) {
            Main.allowedClickGuiHeight =  ClickGUIModule.clickHeight.get().floatValue();
        } else {
            ScaledResolution sr = new ScaledResolution(mc);
            Main.allowedClickGuiHeight = 2 * sr.getScaledHeight() / 3f;
        }

        float allowedHeight = Main.allowedClickGuiHeight;


        boolean hoveringMods = isHovering(x, y + categoryRectHeight, rectWidth, allowedHeight, mouseX, mouseY);


        float scaleAnim = Math.max(1, (float) openingAnimation.getOutput() + .7f);
        float width = rectWidth;

        StencilUtil.initStencilToWrite();
        DrRenderUtils.drawRect2(x - 100, y + categoryRectHeight, rectWidth + 150, allowedHeight, -1);
        StencilUtil.readStencilBuffer(1);

        double scroll = category.getScroll().getScroll();
        double count = 0;
        for (ModuleRect moduleRect : moduleRects) {
            Animation animation = moduleAnimMap.get(moduleRect);
            animation.setDirection(moduleRect.module.getExpanded() ? Direction.FORWARDS : Direction.BACKWARDS);

            moduleRect.settingAnimation = animation;
            moduleRect.alphaAnimation = alphaAnimation;
            moduleRect.x = x;
            moduleRect.height = 17;
            moduleRect.panelLimitY = y;
            moduleRect.openingAnimation = openingAnimation;
            moduleRect.y = (float) (y + categoryRectHeight + (count * 17) + MathUtils.roundToHalf(scroll));
            moduleRect.width = rectWidth;
            moduleRect.drawScreen(mouseX, mouseY);

            // count ups by one but then accounts for setting animation opening
            count += 1 + (moduleRect.getSettingSize());
        }

        if (hoveringMods) {
            category.getScroll().onScroll(30);
            float hiddenHeight = (float) ((count * 17) - allowedHeight);
            //category.getScroll().setMaxScroll(Math.max(0, hiddenHeight));
        }

        StencilUtil.uninitStencilBuffer();

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean canDrag = isHovering(category.getDrag().getX(), category.getDrag().getY(), rectWidth, categoryRectHeight, mouseX, mouseY);
        category.getDrag().onClick(mouseX, mouseY, button, canDrag);
        moduleRects.forEach(moduleRect -> moduleRect.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        category.getDrag().onRelease(state);
        moduleRects.forEach(moduleRect -> moduleRect.mouseReleased(mouseX, mouseY, state));
    }
}


