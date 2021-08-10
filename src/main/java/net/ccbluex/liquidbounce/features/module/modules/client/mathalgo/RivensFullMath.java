/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.mathalgo;

public class RivensFullMath {

    private static final float BF_SIN_TO_COS;
    private static final int BF_SIN_BITS, BF_SIN_MASK, BF_SIN_COUNT;
    private static final float BF_radFull, BF_radToIndex;
    private static final float[] BF_sinFull;

    static {
        BF_SIN_TO_COS = (float) (Math.PI * 0.5f);

        BF_SIN_BITS = 12;
        BF_SIN_MASK = ~(-1 << BF_SIN_BITS);
        BF_SIN_COUNT = BF_SIN_MASK + 1;

        BF_radFull = (float) (Math.PI * 2.0);
        BF_radToIndex = BF_SIN_COUNT / BF_radFull;

        BF_sinFull = new float[BF_SIN_COUNT];
        for (int i = 0; i < BF_SIN_COUNT; i++)
            BF_sinFull[i] = (float) Math.sin((i + Math.min(1, i % (BF_SIN_COUNT / 4)) * 0.5) / BF_SIN_COUNT * BF_radFull);
    }

    public float sin(float rad) {
        return BF_sinFull[(int)(rad * BF_radToIndex) & BF_SIN_MASK];
    }

    public float cos(float rad) {
        return sin(rad + BF_SIN_TO_COS);
    }
}
