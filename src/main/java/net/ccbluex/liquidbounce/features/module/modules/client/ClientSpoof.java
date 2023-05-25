/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.value.ListValue;
// Made By Zywl

public final class ClientSpoof extends Module {
    private static ModuleCategory category = ModuleCategory.CLIENT;
    public ClientSpoof() {
        super("ClientSpoof", category = ModuleCategory.CLIENT);
    }


    public final ListValue modeValue = new ListValue("Mode", new String[]{
            "Vanilla",
            "Forge",
            "Lunar",
            "LabyMod",
            "CheatBreaker",
            "PvPLounge"
    }, "Vanilla");


    // Made By Zywl
}