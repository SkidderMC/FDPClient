/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category;

import net.ccbluex.liquidbounce.features.module.Category;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author opZywl - Category
 */
public enum yzyCategory {
    COMBAT(Category.COMBAT, "Combat", new Color(-0x19b2c6)),
    PLAYER(Category.PLAYER, "Player", new Color(-0x71ba52)),
    MOVEMENT(Category.MOVEMENT, "Movement", new Color(-0xd13291)),
    VISUAL(Category.VISUAL, "Visual", new Color(-0xc9fe32)),
    CLIENT(Category.CLIENT, "Client", new Color(0xCBFF02)),
    OTHER(Category.OTHER, "Other", new Color(0xFFC200)),
    EXPLOIT(Category.EXPLOIT, "Exploit", new Color(-0xcc6727));

    private final Category parent;
    private final String name;
    private final Color color;

    yzyCategory(final Category parent, final String name, final Color color) {
        this.parent = parent;
        this.name = name;
        this.color = color;
    }

    public Category getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public ResourceLocation getIcon() {
        return new ResourceLocation("fdpclient/clickgui/zywl/icons/" + name.toLowerCase(Locale.getDefault()) + ".png");
    }

    public static yzyCategory of(final Category category) {
        return Arrays.stream(values())
                .filter(astolfoCategory -> astolfoCategory.getParent() == category)
                .findFirst().orElse(null);
    }

}
