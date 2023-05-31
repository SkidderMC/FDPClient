/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.novoline.AnimationUtil;

public final class Translate {
    private boolean AckerRunCRACKED_f;
    private float AckerRunCRACKED_a;
    private float AckerRunCRACKED_ALLATORIxDEMO;

    /*
     * WARNING - void declaration
     */
    public Translate(float f2) {
        float IIiiiiiiIiIii = 0;
        Translate IIiiiiiiIiIii2 = this;
        IIiiiiiiIiIii2.AckerRunCRACKED_a = IIiiiiiiIiIii;
        IIiiiiiiIiIii2.AckerRunCRACKED_ALLATORIxDEMO = f2;
    }

    public float getY() {
        Translate IIiiiiiiIiIii = this;
        return IIiiiiiiIiIii.AckerRunCRACKED_ALLATORIxDEMO;
    }

    public void interpolate(float IIiiiiiiIiIii, float IIiiiiiiIiIii2, double IIiiiiiiIiIii3) {
        Translate IIiiiiiiIiIii4 = this;
        if (IIiiiiiiIiIii4.AckerRunCRACKED_f) {
            IIiiiiiiIiIii4.AckerRunCRACKED_a = (float)AnimationUtil.animate(IIiiiiiiIiIii, IIiiiiiiIiIii4.AckerRunCRACKED_a, IIiiiiiiIiIii3);
            IIiiiiiiIiIii4.AckerRunCRACKED_ALLATORIxDEMO = (float)AnimationUtil.animate(IIiiiiiiIiIii2, IIiiiiiiIiIii4.AckerRunCRACKED_ALLATORIxDEMO, IIiiiiiiIiIii3);
            return;
        }
        IIiiiiiiIiIii4.AckerRunCRACKED_a = IIiiiiiiIiIii;
        IIiiiiiiIiIii4.AckerRunCRACKED_ALLATORIxDEMO = IIiiiiiiIiIii2;
        IIiiiiiiIiIii4.AckerRunCRACKED_f = true;
    }

}
