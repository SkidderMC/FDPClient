package net.ccbluex.liquidbounce.injection.access;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;

public interface IWorld {

    boolean isAreaLoaded(int var1, int var2, int var3, int var4, boolean var5);

    boolean isBlockLoaded(int var1, int var2, int var3);

    boolean isBlockLoaded(int var1, int var2, int var3, boolean var4);

    boolean isValid(int var1, int var2, int var3);

    boolean canSeeSky(int var1, int var2, int var3);

    int getCombinedLight(int var1, int var2, int var3, int var4);

    int getRawLight(int var1, int var2, int var3, EnumSkyBlock var4);

    int getLight(int var1, int var2, int var3, boolean var4);

    int getLightFor(EnumSkyBlock var1, int var2, int var3, int var4);

    int getLightFromNeighbors(int var1, int var2, int var3);

    int getLightFromNeighborsFor(EnumSkyBlock var1, int var2, int var3, int var4);

    void setLightFor(EnumSkyBlock var1, int var2, int var3, int var4, int var5);

    boolean checkLight(int var1, int var2, int var3);

    boolean checkLightFor(EnumSkyBlock var1, int var2, int var3, int var4);

    float getLightBrightness(int var1, int var2, int var3);

    IBlockState getBlockState(int n, int n2, int n3);

    boolean setBlockState(int var1, int var2, int var3, IBlockState var4, int var5);

    void markBlockForUpdate(int var1, int var2, int var3);

    void markAndNotifyBlock(int var1, int var2, int var3, Chunk var4, IBlockState var5, IBlockState var6, int var7);

    void notifyLightSet(int var1, int var2, int var3);

    Chunk getChunkFromBlockCoords(int var1, int var2, int var3);
}