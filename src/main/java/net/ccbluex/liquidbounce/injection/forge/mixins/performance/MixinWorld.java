package net.ccbluex.liquidbounce.injection.forge.mixins.performance;

import com.google.common.collect.ImmutableSetMultimap;
import net.ccbluex.liquidbounce.features.module.modules.client.Performance;
import net.ccbluex.liquidbounce.injection.access.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.ForgeChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Mixin(value={World.class})
public abstract class MixinWorld implements IWorld {
    @Shadow
    @Final
    public WorldProvider provider;
    @Shadow
    private int skylightSubtracted;
    @Shadow
    @Final
    public boolean isRemote;
    @Shadow
    protected WorldInfo worldInfo;
    @Shadow
    @Final
    public Profiler theProfiler;
    @Shadow
    protected List<IWorldAccess> worldAccesses;
    @Shadow
    int[] lightUpdateBlockList;

    @Shadow
    protected abstract boolean isChunkLoaded(int var1, int var2, boolean var3);

    @Shadow
    public abstract Chunk getChunkFromChunkCoords(int var1, int var2);

    @Shadow
    protected abstract boolean isAreaLoaded(int var1, int var2, int var3, int var4, int var5, int var6, boolean var7);

    @Shadow
    protected Set<ChunkCoordIntPair> activeChunkSet;
    @Shadow
    @Final
    public List<EntityPlayer> playerEntities;
    @Shadow
    private int ambientTickCountdown;
    @Shadow
    @Final
    public Random rand;
    @Shadow
    public abstract ImmutableSetMultimap<ChunkCoordIntPair, ForgeChunkManager.Ticket> getPersistentChunks();
    @Shadow
    protected abstract int getRenderDistanceChunks();

    @Inject(method={"setActivePlayerChunksAndCheckLight"}, at={@At(value="HEAD")}, cancellable=true)
    private void setActivePlayerChunksAndCheckLight(CallbackInfo callbackInfo) {
        if (Performance.fastBlockLightningValue.get()) {
            int n;
            int n2;
            int n3;
            callbackInfo.cancel();
            this.activeChunkSet.clear();
            this.theProfiler.startSection("buildList");
            this.activeChunkSet.addAll(this.getPersistentChunks().keySet());
            for (EntityPlayer entityPlayer : this.playerEntities) {
                n3 = MathHelper.floor_double(entityPlayer.posX / 16.0);
                n2 = MathHelper.floor_double(entityPlayer.posZ / 16.0);
                n = this.getRenderDistanceChunks();
                for (int i = -n; i <= n; ++i) {
                    for (int j = -n; j <= n; ++j) {
                        this.activeChunkSet.add(new ChunkCoordIntPair(i + n3, j + n2));
                    }
                }
            }
            this.theProfiler.endSection();
            if (this.ambientTickCountdown > 0) {
                --this.ambientTickCountdown;
            }
            this.theProfiler.startSection("playerCheckLight");
            if (!this.playerEntities.isEmpty()) {
                EntityPlayer entityPlayer;
                int n4 = this.rand.nextInt(this.playerEntities.size());
                entityPlayer = this.playerEntities.get(n4);
                n3 = MathHelper.floor_double(entityPlayer.posX) + this.rand.nextInt(11) - 5;
                n2 = MathHelper.floor_double(entityPlayer.posY) + this.rand.nextInt(11) - 5;
                n = MathHelper.floor_double(entityPlayer.posZ) + this.rand.nextInt(11) - 5;
                this.checkLight(n3, n2, n);
            }
            this.theProfiler.endSection();
        }
    }

    @Override
    public boolean isAreaLoaded(int n, int n2, int n3, int n4, boolean bl) {
        return this.isAreaLoaded(n - n4, n2 - n4, n3 - n4, n + n4, n2 + n4, n3 + n4, bl);
    }

    @Override
    public boolean isBlockLoaded(int n, int n2, int n3) {
        return this.isBlockLoaded(n, n2, n3, true);
    }

    @Override
    public boolean isBlockLoaded(int n, int n2, int n3, boolean bl) {
        return this.isValid(n, n2, n3) && this.isChunkLoaded(n >> 4, n3 >> 4, bl);
    }

    @Override
    public boolean isValid(int n, int n2, int n3) {
        return n >= -30000000 && n3 >= -30000000 && n < 30000000 && n3 < 30000000 && n2 >= 0 && n2 < 256;
    }

    @Override
    public boolean canSeeSky(int n, int n2, int n3) {
        return ((IChunk)this.getChunkFromBlockCoords(n, n2, n3)).canSeeSky(n, n2, n3);
    }

    @Override
    public int getCombinedLight(int n, int n2, int n3, int n4) {
        int n5 = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, n, n2, n3);
        int n6 = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, n, n2, n3);
        if (n6 < n4) {
            n6 = n4;
        }
        return n5 << 20 | n6 << 4;
    }

    @Override
    public int getRawLight(int n, int n2, int n3, EnumSkyBlock enumSkyBlock) {
        if (enumSkyBlock == EnumSkyBlock.SKY && this.canSeeSky(n, n2, n3)) {
            return 15;
        }
        IBlock IBlock2 = (IBlock)this.getBlockState(n, n2, n3).getBlock();
        int n4 = IBlock2.getLightValue((World)(Object)this, n, n2, n3);
        int n5 = enumSkyBlock == EnumSkyBlock.SKY ? 0 : n4;
        int n6 = IBlock2.getLightOpacity((World)(Object)this, n, n2, n3);
        if (n6 >= 15 && n4 > 0) {
            n6 = 1;
        }
        if (n6 < 1) {
            n6 = 1;
        }
        if (n6 >= 15) {
            return 0;
        }
        if (n5 >= 14) {
            return n5;
        }
        for (EnumFacing enumFacing : StaticStorage.facings()) {
            int n7 = this.getLightFor(enumSkyBlock, n + enumFacing.getFrontOffsetX(), n2 + enumFacing.getFrontOffsetY(), n3 + enumFacing.getFrontOffsetZ()) - n6;
            if (n7 > n5) {
                n5 = n7;
            }
            if (n5 < 14) continue;
            return n5;
        }
        return n5;
    }

    @Override
    public float getLightBrightness(int n, int n2, int n3) {
        return this.provider.getLightBrightnessTable()[this.getLightFromNeighbors(n, n2, n3)];
    }

    @Override
    public int getLight(int n, int n2, int n3, boolean bl) {
        if (n >= -30000000 && n3 >= -30000000 && n < 30000000 && n3 < 30000000) {
            if (bl && this.getBlockState(n, n2, n3).getBlock().getUseNeighborBrightness()) {
                int n4 = this.getLight(n, n2 + 1, n3, false);
                int n5 = this.getLight(n + 1, n2, n3, false);
                int n6 = this.getLight(n - 1, n2, n3, false);
                int n7 = this.getLight(n, n2, n3 + 1, false);
                int n8 = this.getLight(n, n2, n3 - 1, false);
                if (n5 > n4) {
                    n4 = n5;
                }
                if (n6 > n4) {
                    n4 = n6;
                }
                if (n7 > n4) {
                    n4 = n7;
                }
                if (n8 > n4) {
                    n4 = n8;
                }
                return n4;
            }
            if (n2 < 0) {
                return 0;
            }
            if (n2 >= 256) {
                n2 = 255;
            }
            IChunk IChunk2 = (IChunk)this.getChunkFromBlockCoords(n, n2, n3);
            return IChunk2.getLightSubtracted(n, n2, n3, this.skylightSubtracted);
        }
        return 15;
    }

    @Override
    public int getLightFor(EnumSkyBlock enumSkyBlock, int n, int n2, int n3) {
        if (n2 < 0) {
            n2 = 0;
        }
        if (!this.isValid(n, n2, n3)) {
            return enumSkyBlock.defaultLightValue;
        }
        if (!this.isBlockLoaded(n, n2, n3)) {
            return enumSkyBlock.defaultLightValue;
        }
        IChunk IChunk2 = (IChunk)this.getChunkFromBlockCoords(n, n2, n3);
        return IChunk2.getLightFor(enumSkyBlock, n, n2, n3);
    }

    @Override
    public int getLightFromNeighbors(int n, int n2, int n3) {
        return this.getLight(n, n2, n3, true);
    }

    @Override
    public int getLightFromNeighborsFor(EnumSkyBlock enumSkyBlock, int n, int n2, int n3) {
        if (this.provider.getHasNoSky() && enumSkyBlock == EnumSkyBlock.SKY) {
            return 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        if (!this.isValid(n, n2, n3)) {
            return enumSkyBlock.defaultLightValue;
        }
        if (!this.isBlockLoaded(n, n2, n3)) {
            return enumSkyBlock.defaultLightValue;
        }
        if (this.getBlockState(n, n2, n3).getBlock().getUseNeighborBrightness()) {
            int n4 = this.getLightFor(enumSkyBlock, n, n2 + 1, n3);
            int n5 = this.getLightFor(enumSkyBlock, n + 1, n2, n3);
            int n6 = this.getLightFor(enumSkyBlock, n - 1, n2, n3);
            int n7 = this.getLightFor(enumSkyBlock, n, n2, n3 + 1);
            int n8 = this.getLightFor(enumSkyBlock, n, n2, n3 - 1);
            if (n5 > n4) {
                n4 = n5;
            }
            if (n6 > n4) {
                n4 = n6;
            }
            if (n7 > n4) {
                n4 = n7;
            }
            if (n8 > n4) {
                n4 = n8;
            }
            return n4;
        }
        IChunk IChunk2 = (IChunk)this.getChunkFromBlockCoords(n, n2, n3);
        return IChunk2.getLightFor(enumSkyBlock, n, n2, n3);
    }

    @Override
    public void setLightFor(EnumSkyBlock enumSkyBlock, int n, int n2, int n3, int n4) {
        if (this.isValid(n, n2, n3) && this.isBlockLoaded(n, n2, n3)) {
            IChunk IChunk2 = (IChunk)this.getChunkFromBlockCoords(n, n2, n3);
            IChunk2.setLightFor(enumSkyBlock, n, n2, n3, n4);
            this.notifyLightSet(n, n2, n3);
        }
    }

    @Override
    public boolean checkLight(int n, int n2, int n3) {
        boolean bl = false;
        if (!this.provider.getHasNoSky()) {
            bl = this.checkLightFor(EnumSkyBlock.SKY, n, n2, n3);
        }
        return bl | this.checkLightFor(EnumSkyBlock.BLOCK, n, n2, n3);
    }

    @Override
    public boolean checkLightFor(EnumSkyBlock enumSkyBlock, int n, int n2, int n3) {
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10;
        int n11;
        int n12;
        if (!this.isAreaLoaded(n, n2, n3, 17, false)) {
            return false;
        }
        int n13 = 0;
        int n14 = 0;
        this.theProfiler.startSection("getBrightness");
        int n15 = this.getLightFor(enumSkyBlock, n, n2, n3);
        int n16 = this.getRawLight(n, n2, n3, enumSkyBlock);
        if (n16 > n15) {
            this.lightUpdateBlockList[n14++] = 133152;
        } else if (n16 < n15) {
            this.lightUpdateBlockList[n14++] = 0x20820 | n15 << 18;
            while (n13 < n14) {
                n12 = this.lightUpdateBlockList[n13++];
                n11 = (n12 & 0x3F) - 32 + n;
                n10 = (n12 >> 6 & 0x3F) - 32 + n2;
                n9 = (n12 >> 12 & 0x3F) - 32 + n3;
                n8 = n12 >> 18 & 0xF;
                n7 = this.getLightFor(enumSkyBlock, n11, n10, n9);
                if (n7 != n8) continue;
                this.setLightFor(enumSkyBlock, n11, n10, n9, 0);
                if (n8 <= 0 || MathHelper.abs_int(n11 - n) + MathHelper.abs_int(n10 - n2) + MathHelper.abs_int(n9 - n3) >= 17) continue;
                for (EnumFacing enumFacing : StaticStorage.facings()) {
                    int n17 = n11 + enumFacing.getFrontOffsetX();
                    int n18 = n10 + enumFacing.getFrontOffsetY();
                    int n19 = n9 + enumFacing.getFrontOffsetZ();
                    int n20 = Math.max(1, this.getBlockState(n17, n18, n19).getBlock().getLightOpacity());
                    n7 = this.getLightFor(enumSkyBlock, n17, n18, n19);
                    if (n7 != n8 - n20 || n14 >= this.lightUpdateBlockList.length) continue;
                    this.lightUpdateBlockList[n14++] = n17 - n + 32 | n18 - n2 + 32 << 6 | n19 - n3 + 32 << 12 | n8 - n20 << 18;
                }
            }
            n13 = 0;
        }
        this.theProfiler.endSection();
        this.theProfiler.startSection("checkedPosition < toCheckCount");
        while (n13 < n14) {
            boolean bl;
            n12 = this.lightUpdateBlockList[n13++];
            n11 = (n12 & 0x3F) - 32 + n;
            n10 = (n12 >> 6 & 0x3F) - 32 + n2;
            n9 = (n12 >> 12 & 0x3F) - 32 + n3;
            n8 = this.getLightFor(enumSkyBlock, n11, n10, n9);
            n7 = this.getRawLight(n11, n10, n9, enumSkyBlock);
            if (n7 == n8) continue;
            this.setLightFor(enumSkyBlock, n11, n10, n9, n7);
            if (n7 <= n8) continue;
            n6 = Math.abs(n11 - n);
            n5 = Math.abs(n10 - n2);
            n4 = Math.abs(n9 - n3);
            boolean bl2 = bl = n14 < this.lightUpdateBlockList.length - 6;
            if (n6 + n5 + n4 >= 17 || !bl) continue;
            if (this.getLightFor(enumSkyBlock, n11 - 1, n10, n9) < n7) {
                this.lightUpdateBlockList[n14++] = n11 - 1 - n + 32 + (n10 - n2 + 32 << 6) + (n9 - n3 + 32 << 12);
            }
            if (this.getLightFor(enumSkyBlock, n11 + 1, n10, n9) < n7) {
                this.lightUpdateBlockList[n14++] = n11 + 1 - n + 32 + (n10 - n2 + 32 << 6) + (n9 - n3 + 32 << 12);
            }
            if (this.getLightFor(enumSkyBlock, n11, n10 - 1, n9) < n7) {
                this.lightUpdateBlockList[n14++] = n11 - n + 32 + (n10 - 1 - n2 + 32 << 6) + (n9 - n3 + 32 << 12);
            }
            if (this.getLightFor(enumSkyBlock, n11, n10 + 1, n9) < n7) {
                this.lightUpdateBlockList[n14++] = n11 - n + 32 + (n10 + 1 - n2 + 32 << 6) + (n9 - n3 + 32 << 12);
            }
            if (this.getLightFor(enumSkyBlock, n11, n10, n9 - 1) < n7) {
                this.lightUpdateBlockList[n14++] = n11 - n + 32 + (n10 - n2 + 32 << 6) + (n9 - 1 - n3 + 32 << 12);
            }
            if (this.getLightFor(enumSkyBlock, n11, n10, n9 + 1) >= n7) continue;
            this.lightUpdateBlockList[n14++] = n11 - n + 32 + (n10 - n2 + 32 << 6) + (n9 + 1 - n3 + 32 << 12);
        }
        this.theProfiler.endSection();
        return true;
    }

    @Override
    public IBlockState getBlockState(int n, int n2, int n3) {
        if (!this.isValid(n, n2, n3)) {
            return Blocks.air.getDefaultState();
        }
        IChunk IChunk2 = (IChunk)this.getChunkFromBlockCoords(n, n2, n3);
        return IChunk2.getBlockState(n, n2, n3);
    }

    @Override
    public void markBlockForUpdate(int n, int n2, int n3) {
        for (IWorldAccess iWorldAccess : this.worldAccesses) {
            ((IMixinWorldAccess)iWorldAccess).markBlockForUpdate(n, n2, n3);
        }
    }

    @Override
    public void markAndNotifyBlock(int n, int n2, int n3, Chunk chunk, IBlockState iBlockState, IBlockState iBlockState2, int n4) {
        if (!((n4 & 2) == 0 || this.isRemote && (n4 & 4) != 0 || chunk != null && !chunk.isPopulated())) {
            this.markBlockForUpdate(n, n2, n3);
        }
        if (!this.isRemote && (n4 & 1) != 0) {
            iBlockState2.getBlock().hasComparatorInputOverride();
        }// empty if block
    }

    @Override
    public void notifyLightSet(int n, int n2, int n3) {
        for (IWorldAccess iWorldAccess : this.worldAccesses) {
            ((IMixinWorldAccess)iWorldAccess).notifyLightSet(n, n2, n3);
        }
    }

    @Override
    public Chunk getChunkFromBlockCoords(int n, int n2, int n3) {
        return this.getChunkFromChunkCoords(n >> 4, n3 >> 4);
    }
}