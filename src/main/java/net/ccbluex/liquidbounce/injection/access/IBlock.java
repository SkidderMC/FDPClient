package net.ccbluex.liquidbounce.injection.access;

import net.minecraft.world.IBlockAccess;

public interface IBlock {
    int getLightValue(IBlockAccess var1, int var2, int var3, int var4);

    int getLightOpacity(IBlockAccess var1, int var2, int var3, int var4);
}

