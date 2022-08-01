package net.ccbluex.liquidbounce.injection.access;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.EnumSkyBlock;

public interface IChunk {
    int getLightFor(EnumSkyBlock var1, int var2, int var3, int var4);

    int getLightSubtracted(int var1, int var2, int var3, int var4);

    boolean canSeeSky(int var1, int var2, int var3);

    void setLightFor(EnumSkyBlock var1, int var2, int var3, int var4, int var5);

    IBlockState getBlockState(int var1, int var2, int var3);
}

