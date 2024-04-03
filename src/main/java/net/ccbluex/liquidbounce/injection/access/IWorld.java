/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.access;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;

public interface IWorld {

    boolean fDPClient$isAreaLoaded(int var1, int var2, int var3, int var4, boolean var5);

    boolean fDPClient$isBlockLoaded(int var1, int var2, int var3);

    boolean fDPClient$isBlockLoaded(int var1, int var2, int var3, boolean var4);

    boolean fDPClient$isValid(int var1, int var2, int var3);

    boolean fDPClient$canSeeSky(int var1, int var2, int var3);

    int fDPClient$getCombinedLight(int var1, int var2, int var3, int var4);

    int fDPClient$getRawLight(int var1, int var2, int var3, EnumSkyBlock var4);

    int fDPClient$getLight(int var1, int var2, int var3, boolean var4);

    int fDPClient$getLightFor(EnumSkyBlock var1, int var2, int var3, int var4);

    int fDPClient$getLightFromNeighbors(int var1, int var2, int var3);

    int fDPClient$getLightFromNeighborsFor(EnumSkyBlock var1, int var2, int var3, int var4);

    void fDPClient$setLightFor(EnumSkyBlock var1, int var2, int var3, int var4, int var5);

    boolean fDPClient$checkLight(int var1, int var2, int var3);

    boolean fDPClient$checkLightFor(EnumSkyBlock var1, int var2, int var3, int var4);

    float fDPClient$getLightBrightness(int var1, int var2, int var3);

    IBlockState fDPClient$getBlockState(int n, int n2, int n3);

    boolean fDPClient$setBlockState(int var1, int var2, int var3, IBlockState var4, int var5);

    void fDPClient$markBlockForUpdate(int var1, int var2, int var3);

    void fDPClient$markAndNotifyBlock(int var1, int var2, int var3, Chunk var4, IBlockState var5, IBlockState var6, int var7);

    void fDPClient$notifyLightSet(int var1, int var2, int var3);

    Chunk fDPClient$getChunkFromBlockCoords(int var1, int var2, int var3);
}