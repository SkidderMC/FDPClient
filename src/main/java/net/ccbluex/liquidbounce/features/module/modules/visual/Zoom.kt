/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual;

import net.ccbluex.liquidbounce.features.module.EnumTriggerType;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Zoom", category = ModuleCategory.VISUAL, keyBind = Keyboard.KEY_C, triggerType = EnumTriggerType.PRESS)
public class Zoom extends Module {
    private final BoolValue slowerSens = new BoolValue("Slower sensitivity", true);
    private final BoolValue smoothCam = new BoolValue("Smooth camera", true);

    private boolean active;
    private float oldFov;
    private float oldSens;

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent e) {
        if (this.getState()) {
            if (mc.thePlayer != null) {
                    if (!active) {
                        oldSens = mc.gameSettings.mouseSensitivity;
                        oldFov = mc.gameSettings.fovSetting;
                        active = true;

                        if (slowerSens.get()) {
                            mc.gameSettings.mouseSensitivity /= 4F;
                        }

                        if (smoothCam.get()) {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSmoothCamera.getKeyCode(), true);
                        }

                        mc.gameSettings.fovSetting = 25;
                    }
                } else {
                    if (active) {
                        active = false;

                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSmoothCamera.getKeyCode(), false);
                        mc.gameSettings.mouseSensitivity = oldSens;
                        mc.gameSettings.fovSetting = oldFov;
                    }
                }
            }
        }
}