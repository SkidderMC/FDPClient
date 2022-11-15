/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

@ModuleInfo(name = "PerspectiveMod", category = ModuleCategory.RENDER)
public final class PerspectiveMod extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean perspectiveToggled = false;
    public static float cameraYaw = 0F;
    public static float cameraPitch = 0F;
    private static int previousPerspective = 0;

   public void onEnable() {
       perspectiveToggled = !perspectiveToggled;
       cameraYaw = mc.thePlayer.rotationYaw;
       cameraPitch = mc.thePlayer.rotationPitch;
       if (perspectiveToggled) {
           previousPerspective = mc.gameSettings.thirdPersonView;
           mc.gameSettings.thirdPersonView = 1;
       } else {
           mc.gameSettings.thirdPersonView = previousPerspective;
       }
    }

    public static boolean overrideMouse() {
        if (mc.inGameHasFocus && Display.isActive()) {
            if (!perspectiveToggled) {
                return true;
            }

            mc.mouseHelper.mouseXYChange();
            float f1 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f2 = f1 * f1 * f1 * 8.0F;
            float f3 = (float) mc.mouseHelper.deltaX * f2;
            float f4 = (float) mc.mouseHelper.deltaY * f2;

            cameraYaw += f3 * 0.15F;
            cameraPitch += f4 * 0.15F;

            if (cameraPitch > 90) cameraPitch = 90;
            if (cameraPitch < -90) cameraPitch = -90;
        }
        return false;
    }

    public void onDisable() {
        resetPerspective();
    }

    public static void resetPerspective() {
        perspectiveToggled = false;
        mc.gameSettings.thirdPersonView = previousPerspective;
    }

}
