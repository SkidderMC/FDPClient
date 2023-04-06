/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat;

import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.TickEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.features.value.FloatValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import java.io.IOException;

@ModuleInfo(name = "TickBase", category = ModuleCategory.COMBAT)
public final class TickBase extends Module {
    private final FloatValue rangeValue = new FloatValue("Range", 3.0f, 1, 8);
    private int skippedTick, preTick;
    private boolean flag;
    private KillAura killAura = LiquidBounce.moduleManager.getModule(KillAura.class);

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;
    }

    @Override
    public void onEnable() {
        mc.timer.timerSpeed = 1.0f;
    }

    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        if (!MovementUtils.INSTANCE.isMoving() || killAura.getCurrentTarget() == null) {
            mc.timer.timerSpeed = 1.0f;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (flag) return;
        if (killAura.getCurrentTarget() == null) {
            sleep();
        } else {
            if (shouldSkip()) {
                flag = true;
                for (int i = 0; i < preTick; i++) {
                    try {
                        mc.runTick();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                flag = false;
            } else {
                sleep();
            }
        }
    }

    private void sleep() {
        if (skippedTick > 0) {
            try {
                Thread.sleep(2L * skippedTick);
                skippedTick = 0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mc.timer.timerSpeed = 0.054f + skippedTick;
        }
    }

    public boolean shouldSkip() {
        final EntityLivingBase target = killAura.getCurrentTarget();
        if (target == null || skippedTick > 5 || !mc.thePlayer.isSprinting()) return false;
        final double dx = mc.thePlayer.posX - target.posX, dz = mc.thePlayer.posZ - target.posZ;
        if (MathHelper.sqrt_double(dx * dx + dz * dz) > rangeValue.getValue()) {
            preTick = (int) (2 * (MathHelper.sqrt_double(dx * dx + dz * dz) - rangeValue.getValue()));
            skippedTick += preTick;
            return true;
        }
        return false;
    }
}
