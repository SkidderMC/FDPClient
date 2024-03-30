/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module;

import lombok.Getter;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.normal.Main;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.objects.Drag;
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.render.Scroll;

public enum ModuleCategory {

    COMBAT("%module.category.combat%", "a", "Combat", 15, 15, false, true),
    PLAYER("%module.category.player%", "d", "Player", 15, 180, false, true),
    MOVEMENT("%module.category.movement%", "b", "Movement", 330, 15, false, true),
    VISUAL("%module.category.visual%", "c", "Visual", 225, 15, false, true),
    CLIENT("%module.category.client%", "e", "Client", 120, 15, false, true),
    OTHER("%module.category.other%", "g", "Other", 15, 330, false, true),
    EXPLOIT("%module.category.exploit%", "f", "Exploit", 120, 180, false, true);

    public final String namee;
    public final String icon;
    public final String displayName;
    public final String configName;
    public boolean expanded;

    public int posXs;
    public int posYs;
    public boolean clickeds;
    public boolean showModsV;

    @Getter
    private final Scroll scroll = new Scroll();

    @Getter
    private final Drag drag;
    public int posY = 20;

    ModuleCategory(String a1, String a, String config, int posX, int posY, boolean clicked, boolean showMods) {
        namee = a1;
        icon = a;
        displayName = a1;
        posX = 40 + (Main.categoryCount * 120);
        configName = config;
        drag = new Drag(posX, posY);
        expanded = true;
        posXs = posX;
        posYs = posY;
        clickeds = clicked;
        showModsV = showMods;
        Main.categoryCount++;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
