/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.mathalgo;

public class RivensMath {

    private static final int BF_SIN_BITS, BF_SIN_MASK, BF_SIN_COUNT;
    private static final float BF_radFull, BF_radToIndex;
    private static final float BF_degFull, BF_degToIndex;
    private static final float[] BF_sin, BF_cos;

    static {
        BF_SIN_BITS  = 12;
        BF_SIN_MASK  = ~(-1 << BF_SIN_BITS);
        BF_SIN_COUNT = BF_SIN_MASK + 1;

        BF_radFull    = (float) (Math.PI * 2.0);
        BF_degFull    = (float) (360.0);
        BF_radToIndex = BF_SIN_COUNT / BF_radFull;
        BF_degToIndex = BF_SIN_COUNT / BF_degFull;

        BF_sin = new float[BF_SIN_COUNT];
        BF_cos = new float[BF_SIN_COUNT];

        for (int i = 0; i < BF_SIN_COUNT; i++) {
            BF_sin[i] = (float) Math.sin((i + 0.5f) / BF_SIN_COUNT * BF_radFull);
            BF_cos[i] = (float) Math.cos((i + 0.5f) / BF_SIN_COUNT * BF_radFull);
        }

        for (int i = 0; i < 360; i += 90) {
            BF_sin[(int)(i * BF_degToIndex) & BF_SIN_MASK] = (float)Math.sin(i * Math.PI / 180.0);
            BF_cos[(int)(i * BF_degToIndex) & BF_SIN_MASK] = (float)Math.cos(i * Math.PI / 180.0);
        }
    }

    public float sin(float rad) {
        return BF_sin[(int)(rad * BF_radToIndex) & BF_SIN_MASK];
    }

    public float cos(float rad) {
        return BF_cos[(int)(rad * BF_radToIndex) & BF_SIN_MASK];
    }

}
