/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonUtils {

    public static final Gson PRETTY_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
}
