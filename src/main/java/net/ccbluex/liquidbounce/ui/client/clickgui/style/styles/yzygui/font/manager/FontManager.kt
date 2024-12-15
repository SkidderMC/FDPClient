/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.manager;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author opZywl - FontManager
 */
public final class FontManager{

    private final Map<String, FontRenderer> registry = new HashMap<>();

    public ResourceLocation getFontResource(final String name) {
        return new ResourceLocation("fdpclient/clickgui/zywl/fonts/" + name + ".ttf");
    }

    private void register(final String name, final ResourceLocation location, final int size) {
        final FontRenderer fontRenderer = new FontRenderer(location, size);

        registry.put(name, fontRenderer);
    }

    public void register() {
        this.register(
                "lato-bold-15",
                this.getFontResource("lato-bold"),
                15
        );
        this.register(
                "lato-bold-17",
                this.getFontResource("lato-bold"),
                17
        );
        this.register(
                "lato-bold-13",
                this.getFontResource("lato-bold"),
                13
        );

        this.register(
                "lato-bold-13",
                this.getFontResource("lato-bold"),
                13
        );
        this.register(
                "lato-bold-15",
                this.getFontResource("lato-bold"),
                15
        );
        this.register(
                "lato-bold-17",
                this.getFontResource("lato-bold"),
                17
        );
        this.register(
                "lato-bold-30",
                this.getFontResource("lato-bold"),
                30
        );
        this.register(
                "lato-bold-64",
                this.getFontResource("lato-bold"),
                64
        );
    }

    public FontRenderer get(final String name) {
        return registry.get(name);
    }

}
