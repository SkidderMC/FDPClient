package net.ccbluex.liquidbounce.utils.entity.impl;

import net.ccbluex.liquidbounce.utils.entity.ICheck;
import net.minecraft.entity.Entity;

public final class WallCheck implements ICheck {
    @Override
    public boolean validate(Entity entity) {
        return mc.thePlayer.canEntityBeSeen(entity);
    }
}

