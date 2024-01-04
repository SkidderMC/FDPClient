/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.features.module.modules.client.ColorManager;
import net.ccbluex.liquidbounce.value.IntegerValue;

public class ColorElement extends IntegerValue {

    public ColorElement(int counter, Material m, IntegerValue basis) {
        super("Color" + counter + "-" + m.getColorName(), 255, 0, 255, () -> (basis.get() >= counter));
    }

    public ColorElement(int counter, Material m) {
        super("Color" + counter + "-" + m.getColorName(), 255, 0, 255);
    }

    @Override
    protected void onChanged(final Integer oldValue, final Integer newValue) {
        ColorManager.regenerateColors(true);
    }

    enum Material {
        RED("Red"),
        GREEN("Green"),
        BLUE("Blue");

        private final String colName;
        Material(String name) {
            this.colName = name;
        }

        public String getColorName() {
            return this.colName;
        }
    }
    
}