package net.ccbluex.liquidbounce.injection.forge.mixins.performance;

import net.ccbluex.liquidbounce.injection.access.IChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderDebug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={Chunk.class})
public abstract class MixinChunk implements IChunk {
    @Shadow
    @Final
    private World worldObj;
    @Shadow
    @Final
    private ExtendedBlockStorage[] storageArrays;
    @Shadow
    @Final
    private int[] heightMap;
    @Shadow
    private boolean isModified;

    @Shadow
    public abstract void generateSkylightMap();

    @Override
    public int getLightFor(EnumSkyBlock enumSkyBlock, int n, int n2, int n3) {
        int n4 = n & 0xF;
        int n5 = n3 & 0xF;
        ExtendedBlockStorage extendedBlockStorage = this.storageArrays[n2 >> 4];
        return extendedBlockStorage == null ? (this.canSeeSky(n, n2, n3) ? enumSkyBlock.defaultLightValue : 0) : (enumSkyBlock == EnumSkyBlock.SKY ? (this.worldObj.provider.getHasNoSky() ? 0 : extendedBlockStorage.getExtSkylightValue(n4, n2 & 0xF, n5)) : (enumSkyBlock == EnumSkyBlock.BLOCK ? extendedBlockStorage.getExtBlocklightValue(n4, n2 & 0xF, n5) : enumSkyBlock.defaultLightValue));
    }

    @Override
    public int getLightSubtracted(int n, int n2, int n3, int n4) {
        int n5 = n & 0xF;
        int n6 = n3 & 0xF;
        ExtendedBlockStorage extendedBlockStorage = this.storageArrays[n2 >> 4];
        if (extendedBlockStorage == null) {
            return !this.worldObj.provider.getHasNoSky() && n4 < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - n4 : 0;
        }
        int n7 = this.worldObj.provider.getHasNoSky() ? 0 : extendedBlockStorage.getExtSkylightValue(n5, n2 & 0xF, n6);
        int n8 = extendedBlockStorage.getExtBlocklightValue(n5, n2 & 0xF, n6);
        if (n8 > (n7 -= n4)) {
            n7 = n8;
        }
        return n7;
    }

    @Override
    public boolean canSeeSky(int n, int n2, int n3) {
        int n4 = n3 & 0xF;
        int n5 = n & 0xF;
        return n2 >= this.heightMap[n4 << 4 | n5];
    }

    @Override
    public void setLightFor(EnumSkyBlock enumSkyBlock, int n, int n2, int n3, int n4) {
        int n5 = n & 0xF;
        int n6 = n3 & 0xF;
        ExtendedBlockStorage extendedBlockStorage = this.storageArrays[n2 >> 4];
        if (extendedBlockStorage == null) {
            ExtendedBlockStorage extendedBlockStorage2 = new ExtendedBlockStorage(n2 >> 4 << 4, !this.worldObj.provider.getHasNoSky());
            this.storageArrays[n2 >> 4] = extendedBlockStorage2;
            extendedBlockStorage = extendedBlockStorage2;
            this.generateSkylightMap();
        }
        this.isModified = true;
        if (enumSkyBlock == EnumSkyBlock.SKY) {
            if (!this.worldObj.provider.getHasNoSky()) {
                extendedBlockStorage.setExtSkylightValue(n5, n2 & 0xF, n6, n4);
            }
        } else if (enumSkyBlock == EnumSkyBlock.BLOCK) {
            extendedBlockStorage.setExtBlocklightValue(n5, n2 & 0xF, n6, n4);
        }
    }

    @Override
    public IBlockState getBlockState(int n, int n2, int n3) {
        if (this.worldObj.getWorldType() == WorldType.DEBUG_WORLD) {
            IBlockState iBlockState = null;
            if (n2 == 60) {
                iBlockState = Blocks.barrier.getDefaultState();
            }
            if (n2 == 70) {
                iBlockState = ChunkProviderDebug.func_177461_b(n, n3);
            }
            return iBlockState == null ? Blocks.air.getDefaultState() : iBlockState;
        }
        try {
            ExtendedBlockStorage extendedBlockStorage;
            if (n2 >= 0 && n2 >> 4 < this.storageArrays.length && (extendedBlockStorage = this.storageArrays[n2 >> 4]) != null) {
                int n4 = n & 0xF;
                int n5 = n2 & 0xF;
                int n6 = n3 & 0xF;
                return extendedBlockStorage.get(n4, n5, n6);
            }
            return Blocks.air.getDefaultState();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Getting block state");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Block being got");
            crashReportCategory.addCrashSectionCallable("Location", () -> CrashReportCategory.getCoordinateInfo(new BlockPos(n, n2, n3)));
            throw new ReportedException(crashReport);
        }
    }
}
