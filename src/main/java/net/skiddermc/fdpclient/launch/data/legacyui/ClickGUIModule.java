/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.launch.data.legacyui;

import net.skiddermc.fdpclient.event.EventTarget;
import net.skiddermc.fdpclient.event.PacketEvent;
import net.skiddermc.fdpclient.features.module.Module;
import net.skiddermc.fdpclient.features.module.ModuleCategory;
import net.skiddermc.fdpclient.features.module.ModuleInfo;
import net.skiddermc.fdpclient.launch.data.legacyui.clickgui.ClickGui;
import net.skiddermc.fdpclient.launch.data.legacyui.clickgui.style.styles.*;
import net.skiddermc.fdpclient.launch.data.legacyui.clickgui.style.styles.light.LightClickGUI;
import net.skiddermc.fdpclient.launch.data.legacyui.clickgui.style.styles.novoline.ClickyUI;
import net.skiddermc.fdpclient.launch.options.LegacyUiLaunchOption;
import net.skiddermc.fdpclient.utils.render.ColorUtils;
import net.skiddermc.fdpclient.value.BoolValue;
import net.skiddermc.fdpclient.value.FloatValue;
import net.skiddermc.fdpclient.value.IntegerValue;
import net.skiddermc.fdpclient.value.ListValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import org.lwjgl.input.Keyboard;

import java.awt.*;

@ModuleInfo(name = "ClickGUI", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RSHIFT, canEnable = false)
public class ClickGUIModule extends Module {
    private final ListValue styleValue = new ListValue("Style", new String[]{"Novoline", "LiquidBounce", "Null", "Slowly", "Black", "White", "Astolfo"}, "Astolfo") {
        @Override
        protected void onChanged(final String oldValue, final String newValue) {
            updateStyle();
        }
    };

    private final ListValue modeValue = new ListValue("Mode", new String[]{"LiquidBounce", "Light"}, "LiquidBounce");

    public final FloatValue scaleValue = new FloatValue("Scale", 1F, 0.7F, 2F);
    public final IntegerValue maxElementsValue = new IntegerValue("MaxElements", 15, 1, 20);
    public final ListValue backgroundValue = new ListValue("Background", new String[] {"Default", "Gradient", "None"}, "None");

    public final ListValue animationValue = new ListValue("Animation", new String[] {"Bread", "Slide", "LiquidBounce", "Zoom", "Ziul", "None"}, "Ziul");
    public static final BoolValue colorRainbow = new BoolValue("Rainbow", false);
    public static final IntegerValue colorRedValue = (IntegerValue) new IntegerValue("R", 0, 0, 255).displayable(() -> !colorRainbow.get());
    public static final IntegerValue colorGreenValue = (IntegerValue) new IntegerValue("G", 160, 0, 255).displayable(() -> !colorRainbow.get());
    public static final IntegerValue colorBlueValue = (IntegerValue) new IntegerValue("B", 255, 0, 255).displayable(() -> !colorRainbow.get());

    public static Color generateColor() {
        return colorRainbow.get() ? ColorUtils.INSTANCE.rainbow() : new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
    }

    @Override
    public void onEnable() {
        if (styleValue.get().contains("Novoline")) {
            mc.displayGuiScreen(new ClickyUI());
            this.setState(false);
        } else if (modeValue.get().contains("Light")) {
            mc.displayGuiScreen(new LightClickGUI());
            this.setState(false);
        } else {
            updateStyle();
            mc.displayGuiScreen(LegacyUiLaunchOption.clickGui);
        }

    }

    private void updateStyle() {
        switch (styleValue.get().toLowerCase()) {
            case "liquidbounce":
                LegacyUiLaunchOption.clickGui.style = new LiquidBounceStyle();
                break;
            case "null":
                LegacyUiLaunchOption.clickGui.style = new NullStyle();
                break;
            case "slowly":
                LegacyUiLaunchOption.clickGui.style = new SlowlyStyle();
                break;
            case "black":
                LegacyUiLaunchOption.clickGui.style = new BlackStyle();
                break;
            case "white":
                LegacyUiLaunchOption.clickGui.style = new WhiteStyle();
                break;
            case "astolfo":
                LegacyUiLaunchOption.clickGui.style = new AstolfoStyle();
                break;
        }
    }

    @EventTarget(ignoreCondition = true)
    public void onPacket(final PacketEvent event) {
        final Packet packet = event.getPacket();

        if (packet instanceof S2EPacketCloseWindow && mc.currentScreen instanceof ClickGui) {
            event.cancelEvent();
        }
    }
}
