/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import com.google.common.collect.Lists;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.special.spoof.ClientSpoofHandler;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.handler.spoof.ClientBrandRetriever;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.misc.RandomUtils;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiOverlayDebug.class)
public abstract class MixinGuiOverlayDebug {

    @Shadow
    protected abstract boolean isReducedDebug();

    @Inject(method = "call", at = @At(value = "TAIL"), cancellable = true)
    public void call(CallbackInfoReturnable<List<String>> cir) {
        BlockPos blockpos = new BlockPos(MinecraftInstance.mc.getRenderViewEntity().posX, MinecraftInstance.mc.getRenderViewEntity().getEntityBoundingBox().minY, MinecraftInstance.mc.getRenderViewEntity().posZ);

        if (ClientSpoofHandler.handleClientBrand().equalsIgnoreCase(ClientSpoofHandler.LUNAR_DISPLAY_NAME)) {
            List<String> list = Lists.newArrayList();
            Entity entity = MinecraftInstance.mc.getRenderViewEntity();
            EnumFacing enumfacing = entity.getHorizontalFacing();
            String s = "Invalid";

            switch (enumfacing) {
                case NORTH:
                    s = "Towards negative Z";
                    break;
                case SOUTH:
                    s = "Towards positive Z";
                    break;
                case WEST:
                    s = "Towards negative X";
                    break;
                case EAST:
                    s = "Towards positive X";
            }
            list.add("Minecraft 1.8.9 (1.8.9/" + ClientBrandRetriever.getClientModName() + ")");
            list.add(MinecraftInstance.mc.debug);
            list.add(MinecraftInstance.mc.renderGlobal.getDebugInfoRenders());
            list.add(MinecraftInstance.mc.renderGlobal.getDebugInfoEntities());
            list.add("P: " + MinecraftInstance.mc.effectRenderer.getStatistics() + ". T: " + MinecraftInstance.mc.theWorld.getDebugLoadedEntities());
            list.add(MinecraftInstance.mc.theWorld.getProviderName());
            list.add("");
            list.add(String.format("XYZ: %.3f / %.5f / %.3f", MinecraftInstance.mc.getRenderViewEntity().posX, MinecraftInstance.mc.getRenderViewEntity().getEntityBoundingBox().minY, MinecraftInstance.mc.getRenderViewEntity().posZ));
            list.add(String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()));
            list.add(String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15, blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4));
            list.add(String.format("Facing: %s (%s) (%.1f / %.1f)", enumfacing, s, MathHelper.wrapAngleTo180_float(entity.rotationYaw), MathHelper.wrapAngleTo180_float(entity.rotationPitch)));

            if (MinecraftInstance.mc.theWorld != null && MinecraftInstance.mc.theWorld.isBlockLoaded(blockpos)) {
                Chunk chunk = MinecraftInstance.mc.theWorld.getChunkFromBlockCoords(blockpos);
                list.add("Biome: " + chunk.getBiome(blockpos, MinecraftInstance.mc.theWorld.getWorldChunkManager()).biomeName);
                list.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
                DifficultyInstance difficultyinstance = MinecraftInstance.mc.theWorld.getDifficultyForLocation(blockpos);

                if (MinecraftInstance.mc.isIntegratedServerRunning() && MinecraftInstance.mc.getIntegratedServer() != null) {
                    EntityPlayerMP entityplayermp = MinecraftInstance.mc.getIntegratedServer().getConfigurationManager().getPlayerByUUID(MinecraftInstance.mc.thePlayer.getUniqueID());

                }

                list.add(String.format("Local Difficulty: %.2f (Day %d)", difficultyinstance.getAdditionalDifficulty(), MinecraftInstance.mc.theWorld.getWorldTime() / 24000L));
            }

            if (MinecraftInstance.mc.entityRenderer != null && MinecraftInstance.mc.entityRenderer.isShaderActive()) {
                list.add("Shader: " + MinecraftInstance.mc.entityRenderer.getShaderGroup().getShaderGroupName());
            }

            if (MinecraftInstance.mc.objectMouseOver != null && MinecraftInstance.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && MinecraftInstance.mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos1 = MinecraftInstance.mc.objectMouseOver.getBlockPos();
                list.add(String.format("Looking at: %d %d %d", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
            }

            cir.setReturnValue(list);
        }

    }

    @ModifyVariable(method = "getDebugInfoRight", at = @At(value = "STORE"), ordinal = 0)
    private List<String> modifyDebugInfo(List<String> originalList) {

        if (ClientSpoofHandler.handleClientBrand().equalsIgnoreCase(ClientSpoofHandler.LUNAR_DISPLAY_NAME)) {
            originalList.add("");
            originalList.add("[LC Async Resources] Absent: " + RandomUtils.nextInt(0, 5000) + " textures, 0 bytes");
            originalList.add("[LC Async Resources] Low Quality: 0 textures, 0 B");
            originalList.add("[LC Async Resources] Full Quality: 0 textures, 0 B");
        } else {

            final ProtocolVersion version = ProtocolBase.getManager().getTargetVersion();

            originalList.add("");

            if (!MinecraftInstance.mc.isIntegratedServerRunning())
                originalList.add("Protocol: " + version.getName());
            else
                originalList.add("Protocol: 1.8.x");

            originalList.add("");

            originalList.add(FDPClient.CLIENT_NAME + " Client " + FDPClient.CLIENT_VERSION);

        }
        return originalList;
    }

    @Inject(method = "getDebugInfoRight", at = @At("RETURN"), cancellable = true)
    private void modifyDebugInfoRight(CallbackInfoReturnable<List<String>> ci) {
        List<String> list = ci.getReturnValue();
        if (!this.isReducedDebug()) {
            if (ClientSpoofHandler.handleClientBrand().equalsIgnoreCase(ClientSpoofHandler.LUNAR_DISPLAY_NAME)) {
                list.removeAll(FMLCommonHandler.instance().getBrandings(false));
                for (int i = 0; i < list.size(); i++) {
                    String line = list.get(i);
                    if (line.startsWith("Java:")) {
                        list.set(i, "Java: 17.0.3 64bit");
                        break;
                    }
                }
            }
            ci.setReturnValue(list);
        }
    }
}
