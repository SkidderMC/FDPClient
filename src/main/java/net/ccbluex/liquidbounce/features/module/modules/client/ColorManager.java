/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.clickgui.ClickGUIModule;
import net.ccbluex.liquidbounce.utils.render.BlendUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Locale;

@ModuleInfo(name = "ColorManager", category = ModuleCategory.CLIENT)
public class ColorManager extends Module {
    @EventTarget
    public void onRender2D(Render2DEvent render2DEvent) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
    }

    // Color values
    public static IntegerValue red = new IntegerValue("Red-1", 255, 0, 255);
    public static IntegerValue green = new IntegerValue("Green-1", 0, 0, 255);
    public static IntegerValue blue = new IntegerValue("Blue-1", 84, 0, 255);
    public static IntegerValue red2 = new IntegerValue("Red-2", 0, 0, 255);
    public static IntegerValue green2 = new IntegerValue("Green-2", 19, 0, 255);
    public static IntegerValue blue2 = new IntegerValue("Blue-2", 0, 0, 255);

    public static final FloatValue rainbowStartValue = new FloatValue("RainbowStart", 0.41f, 0f, 1f);
    public static final FloatValue rainbowStopValue = new FloatValue("RainbowStop", 0.58f, 0f, 1f);

    public static final FloatValue rainbowSaturationValue = new FloatValue("RainbowSaturation", 0.7f, 0f, 1f);
    public static final FloatValue rainbowBrightnessValue = new FloatValue("RainbowBrightness", 1f, 0f, 1f);
    public static final IntegerValue rainbowSpeedValue = new IntegerValue("RainbowSpeed", 1500, 500, 7000);

    private float tempY = 65.0f;
    private float tempHeight = 65.0f;

    public static IntegerValue r = new IntegerValue("Red-1", 255, 0, 255);
    public static IntegerValue g = new IntegerValue("Green-1", 0, 0, 255);
    public static IntegerValue b = new IntegerValue("Blue-1", 84, 0, 255);
    public static IntegerValue r2 = new IntegerValue("Red-2", 0, 0, 255);
    public static IntegerValue g2 = new IntegerValue("Green-2", 19, 0, 255);
    public static IntegerValue b2 = new IntegerValue("Blue-2", 0, 0, 255);
    public static IntegerValue a = new IntegerValue("Alpha-1", 0, 0, 255);
    public static IntegerValue a2 = new IntegerValue("Alpha-2", 0, 0, 255);
    public static FloatValue ra = new FloatValue("Radius", 4.5f, 0.1f, 8.0f);
    public static IntegerValue c = new IntegerValue("ColorSpeed", 100, 10, 1000);
    public static BoolValue hueInterpolation = new BoolValue("Interpolate", false);
    public static BoolValue movingcolors = new BoolValue("MovingColors", false);

    public static ListValue rainbowMode = new ListValue("ColorMode", new String[]{"Rainbow", "Light Rainbow", "Static", "Double Color", "Default"}, "Light Rainbow");

    // ColorElement instances
    public ColorElement[] colorElements = new ColorElement[10];

    private static float[] lastFraction = new float[]{};
    public static Color[] lastColors = new Color[]{};

    public final IntegerValue blendAmount = new IntegerValue("Mixer-Amount", 2, 2, 10) {
        @Override
        protected void onChanged(final Integer oldValue, final Integer newValue) {
            regenerateColors(oldValue != newValue);
        }
    };

    public final ColorElement col1RedValue = new ColorElement(1, ColorElement.Material.RED);
    public final ColorElement col1GreenValue = new ColorElement(1, ColorElement.Material.GREEN);
    public final ColorElement col1BlueValue = new ColorElement(1, ColorElement.Material.BLUE);

    public final ColorElement col2RedValue = new ColorElement(2, ColorElement.Material.RED);
    public final ColorElement col2GreenValue = new ColorElement(2, ColorElement.Material.GREEN);
    public final ColorElement col2BlueValue = new ColorElement(2, ColorElement.Material.BLUE);

    public final ColorElement col3RedValue = new ColorElement(3, ColorElement.Material.RED, blendAmount);
    public final ColorElement col3GreenValue = new ColorElement(3, ColorElement.Material.GREEN, blendAmount);
    public final ColorElement col3BlueValue = new ColorElement(3, ColorElement.Material.BLUE, blendAmount);

    public final ColorElement col4RedValue = new ColorElement(4, ColorElement.Material.RED, blendAmount);
    public final ColorElement col4GreenValue = new ColorElement(4, ColorElement.Material.GREEN, blendAmount);
    public final ColorElement col4BlueValue = new ColorElement(4, ColorElement.Material.BLUE, blendAmount);

    public final ColorElement col5RedValue = new ColorElement(5, ColorElement.Material.RED, blendAmount);
    public final ColorElement col5GreenValue = new ColorElement(5, ColorElement.Material.GREEN, blendAmount);
    public final ColorElement col5BlueValue = new ColorElement(5, ColorElement.Material.BLUE, blendAmount);

    public final ColorElement col6RedValue = new ColorElement(6, ColorElement.Material.RED, blendAmount);
    public final ColorElement col6GreenValue = new ColorElement(6, ColorElement.Material.GREEN, blendAmount);
    public final ColorElement col6BlueValue = new ColorElement(6, ColorElement.Material.BLUE, blendAmount);

    public final ColorElement col7RedValue = new ColorElement(7, ColorElement.Material.RED, blendAmount);
    public final ColorElement col7GreenValue = new ColorElement(7, ColorElement.Material.GREEN, blendAmount);
    public final ColorElement col7BlueValue = new ColorElement(7, ColorElement.Material.BLUE, blendAmount);

    public final ColorElement col8RedValue = new ColorElement(8, ColorElement.Material.RED, blendAmount);
    public final ColorElement col8GreenValue = new ColorElement(8, ColorElement.Material.GREEN, blendAmount);
    public final ColorElement col8BlueValue = new ColorElement(8, ColorElement.Material.BLUE, blendAmount);

    public final ColorElement col9RedValue = new ColorElement(9, ColorElement.Material.RED, blendAmount);
    public final ColorElement col9GreenValue = new ColorElement(9, ColorElement.Material.GREEN, blendAmount);
    public final ColorElement col9BlueValue = new ColorElement(9, ColorElement.Material.BLUE, blendAmount);

    public final ColorElement col10RedValue = new ColorElement(10, ColorElement.Material.RED, blendAmount);
    public final ColorElement col10GreenValue = new ColorElement(10, ColorElement.Material.GREEN, blendAmount);
    public final ColorElement col10BlueValue = new ColorElement(10, ColorElement.Material.BLUE, blendAmount);

    public static Color getMixedColor(int index, int seconds) {
        final ColorManager colMixer = FDPClient.moduleManager.getModule(ColorManager.class);
        if (colMixer == null) return Color.white;

        if (lastColors.length <= 0 || lastFraction.length <= 0) regenerateColors(true); // just to make sure it won't go white

        return BlendUtils.blendColors(lastFraction, lastColors, (System.currentTimeMillis() + index) % (seconds * 1000) / (float) (seconds * 1000));
    }

    public static void regenerateColors(boolean forceValue) {
        final ColorManager colMixer = FDPClient.moduleManager.getModule(ColorManager.class);

        if (colMixer == null) return;

        // color generation
        if (forceValue || lastColors.length <= 0 || lastColors.length != (colMixer.blendAmount.get() * 2) - 1) {
            Color[] generator = new Color[(colMixer.blendAmount.get() * 2) - 1];

            // reflection is cool
            for (int i = 1; i <= colMixer.blendAmount.get(); i++) {
                Color result = Color.white;
                try {
                    Field red = ColorManager.class.getField("col"+i+"RedValue");
                    Field green = ColorManager.class.getField("col"+i+"GreenValue");
                    Field blue = ColorManager.class.getField("col"+i+"BlueValue");

                    int r = ((ColorElement)red.get(colMixer)).get();
                    int g = ((ColorElement)green.get(colMixer)).get();
                    int b = ((ColorElement)blue.get(colMixer)).get();

                    result = new Color(Math.max(0, Math.min(r, 255)), Math.max(0, Math.min(g, 255)), Math.max(0, Math.min(b, 255)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                generator[i - 1] = result;
            }

            int h = colMixer.blendAmount.get();
            for (int z = colMixer.blendAmount.get() - 2; z >= 0; z--) {
                generator[h] = generator[z];
                h++;
            }

            lastColors = generator;
        }

        // cache thingy
        if (forceValue || lastFraction.length <= 0 || lastFraction.length != (colMixer.blendAmount.get() * 2) - 1) {
            // color frac regenerate if necessary
            float[] colorFraction = new float[(colMixer.blendAmount.get() * 2) - 1];

            for (int i = 0; i <= (colMixer.blendAmount.get() * 2) - 2; i++)
            {
                colorFraction[i] = (float)i / (float)((colMixer.blendAmount.get() * 2) - 2);
            }

            lastFraction = colorFraction;
        }
    }

    public Color getColor1() {
        final int red = this.red.get(),
                green = this.green.get(),
                blue = this.blue.get();
        return new Color(red, green, blue);
    }

    public Color getColor2() {
        final int red = this.red2.get(),
                green = this.green2.get(),
                blue = this.blue2.get();
        return new Color(red, green, blue);
    }

    public static Color[] getClientColors() {
        Color firstColor;
        Color secondColor;

        switch (rainbowMode.get().toLowerCase(Locale.getDefault())) {
            case "light rainbow":
                firstColor = ColorUtils.rainbowc(15, 1, .6f, 1F, 1F);
                secondColor = ColorUtils.rainbowc(15, 40, .6f, 1F, 1F);
                break;
            case "rainbow":
                firstColor = ColorUtils.rainbowc(15, 1, 1F, 1F, 1F);
                secondColor = ColorUtils.rainbowc(15, 40, 1F, 1F, 1F);
                break;
            case "double color":
                firstColor = ColorUtils.interpolateColorsBackAndForth(15, 0, Color.PINK, Color.BLUE, hueInterpolation.get());
                secondColor = ColorUtils.interpolateColorsBackAndForth(15, 90, Color.PINK, Color.BLUE, hueInterpolation.get());
                break;
            case "static":
                firstColor = new Color(ClickGUIModule.INSTANCE.getColorRedValue().get(), ClickGUIModule.INSTANCE.getColorGreenValue().get(), ClickGUIModule.INSTANCE.getColorBlueValue().get());
                secondColor = firstColor;
                break;
            default:
                firstColor = new Color(-1);
                secondColor = new Color(-1);
                break;
        }

        return new Color[]{firstColor, secondColor};
    }

}