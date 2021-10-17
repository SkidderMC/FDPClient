package net.ccbluex.liquidbounce.features.module.modules.client.mathalgo;

import net.ccbluex.liquidbounce.utils.misc.MiscUtils;

public class NewMCMath {

    private static final float[] SIN_TABLE = MiscUtils.INSTANCE.make(new float[65536], (e) ->
    {
        for (int i = 0; i < e.length; ++i)
        {
            e[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0);
        }
    });

    public float sin(float rad) {
        return SIN_TABLE[(int)(rad * 10430.378F) & 65535];
    }

    public float cos(float rad) {
        return SIN_TABLE[(int)(rad * 10430.378F + 16384.0F) & 65535];
    }
}
