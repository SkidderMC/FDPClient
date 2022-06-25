/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.features.module.*;
import net.ccbluex.liquidbounce.value.FloatValue;

@ModuleInfo(name = "ItemPhysics", category = ModuleCategory.RENDER)
public class ItemPhysics extends Module  {
    public final FloatValue itemWeight = new FloatValue("Weight", 0.5F, 0F, 1F);

    @Override
    public String getTag() {
        return itemWeight.get().toString();
    }
}