/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.access;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.EnumSkyBlock;

public interface IChunk {
    int fDPClient$getLightFor(EnumSkyBlock var1, int var2, int var3, int var4);

    int fDPClient$getLightSubtracted(int var1, int var2, int var3, int var4);

    boolean fDPClient$canSeeSky(int var1, int var2, int var3);

    void fDPClient$setLightFor(EnumSkyBlock var1, int var2, int var3, int var4, int var5);

    IBlockState fDPClient$getBlockState(int var1, int var2, int var3);
}

