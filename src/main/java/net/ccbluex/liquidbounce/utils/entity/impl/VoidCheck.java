package net.ccbluex.liquidbounce.utils.entity.impl;

import net.ccbluex.liquidbounce.utils.entity.ICheck;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

public final class VoidCheck implements ICheck {
    @Override
    public boolean validate(Entity entity) {
        return this.isBlockUnder(entity);
    }

    private boolean isBlockUnder(Entity entity) {
        int offset = 0;
        while (offset < entity.posY + entity.getEyeHeight()) {
            AxisAlignedBB boundingBox = entity.getEntityBoundingBox().offset(0.0, -offset, 0.0);
            if (!mc.theWorld.getCollidingBoundingBoxes(entity, boundingBox).isEmpty()) {
                return true;
            }
            offset += 2;
        }
        return false;
    }
}

