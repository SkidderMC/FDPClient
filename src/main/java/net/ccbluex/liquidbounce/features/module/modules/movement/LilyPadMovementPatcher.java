package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.BlockBBEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;

//Fix 1.8.x Client using ViaVersion/ViaForge infinite lag on LilyPad 
@ModuleInfo(name = "LilyPadMovementPatcher", category = ModuleCategory.MOVEMENT, array = false)
public final class LilyPadMovementPatcher extends Module {
    @EventTarget
    public void onBB(BlockBBEvent event) {
        if (event.getBlock() instanceof BlockLilyPad && lilyPad.get()) {
            event.setBoundingBox(new AxisAlignedBB(event.getX(), event.getY(), event.getZ(),
                    event.getX() + 1.0, event.getY() + 0.09375, event.getZ() + 1.0));
        }
    }
}