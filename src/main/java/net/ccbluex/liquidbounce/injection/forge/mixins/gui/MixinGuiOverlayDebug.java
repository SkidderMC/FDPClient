/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import com.google.common.collect.Lists;
import net.ccbluex.liquidbounce.features.module.modules.client.BrandSpoofer;
import net.ccbluex.liquidbounce.handler.payload.ClientBrandRetriever;
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils;
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

import static net.ccbluex.liquidbounce.utils.client.MinecraftInstance.mc;

@Mixin(GuiOverlayDebug.class)
public abstract class MixinGuiOverlayDebug {

    @Shadow
    protected abstract boolean isReducedDebug();

    private static String resolveSelectedBrand() {
        final BrandSpoofer spoofer = BrandSpoofer.INSTANCE;
        final String sel = spoofer.getPossibleBrands().get();
        final String lcSel = sel.trim().toLowerCase();

        if ("custom".equals(lcSel)) {
            String custom = spoofer.getCustomValue().get();
            return custom.isEmpty() ? "Custom" : custom;
        }
        switch (lcSel) {
            case "vanilla":      return "vanilla";
            case "fml":          return "fml,forge";
            case "optifine":     return "OptiFine";
            case "fabric":       return "fabric";
            case "feather":      return "Feather";
            case "lunarclient":  return "LunarClient";
            case "labymod":      return "LabyMod";
            case "cheatbreaker": return "CheatBreaker";
            case "pvplounge":    return "PvPLounge";
            case "minebuilders": return "Minebuilders";
            case "geyser":       return "Geyser";
            case "log4j":        return "Log4j";
            default:             return sel;
        }
    }

    private static boolean isLunarSelected() {
        return "LunarClient".equalsIgnoreCase(BrandSpoofer.INSTANCE.getPossibleBrands().get());
    }

    @Inject(method = "call", at = @At(value = "TAIL"), cancellable = true)
    public void call(CallbackInfoReturnable<List<String>> cir) {
        final BrandSpoofer brandSpoofer = BrandSpoofer.INSTANCE;
        final boolean lunarSelected = isLunarSelected();

        BlockPos blockpos = new BlockPos(
                mc.getRenderViewEntity().posX,
                mc.getRenderViewEntity().getEntityBoundingBox().minY,
                mc.getRenderViewEntity().posZ
        );

        if (brandSpoofer.handleEvents() && lunarSelected) {
            List<String> list = Lists.newArrayList();
            Entity entity = mc.getRenderViewEntity();
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
                    break;
                default:
                    break;
            }

            list.add("Minecraft 1.8.9 (1.8.9/" + ClientBrandRetriever.getClientModName() + ")");
            list.add(mc.debug);
            list.add(mc.renderGlobal.getDebugInfoRenders());
            list.add(mc.renderGlobal.getDebugInfoEntities());
            list.add("P: " + mc.effectRenderer.getStatistics() + ". T: " + mc.theWorld.getDebugLoadedEntities());
            list.add(mc.theWorld.getProviderName());
            list.add("");
            list.add(String.format("XYZ: %.3f / %.5f / %.3f",
                    mc.getRenderViewEntity().posX,
                    mc.getRenderViewEntity().getEntityBoundingBox().minY,
                    mc.getRenderViewEntity().posZ
            ));
            list.add(String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()));
            list.add(String.format("Chunk: %d %d %d in %d %d %d",
                    blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15,
                    blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4
            ));
            list.add(String.format("Facing: %s (%s) (%.1f / %.1f)",
                    enumfacing, s,
                    MathHelper.wrapAngleTo180_float(entity.rotationYaw),
                    MathHelper.wrapAngleTo180_float(entity.rotationPitch)
            ));

            if (mc.theWorld != null && mc.theWorld.isBlockLoaded(blockpos)) {
                Chunk chunk = mc.theWorld.getChunkFromBlockCoords(blockpos);
                list.add("Biome: " + chunk.getBiome(blockpos, mc.theWorld.getWorldChunkManager()).biomeName);
                list.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " ("
                        + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, "
                        + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
                DifficultyInstance difficultyinstance = mc.theWorld.getDifficultyForLocation(blockpos);

                if (mc.isIntegratedServerRunning() && mc.getIntegratedServer() != null) {
                    EntityPlayerMP entityplayermp = mc.getIntegratedServer()
                            .getConfigurationManager()
                            .getPlayerByUUID(mc.thePlayer.getUniqueID());
                }

                list.add(String.format("Local Difficulty: %.2f (Day %d)",
                        difficultyinstance.getAdditionalDifficulty(),
                        mc.theWorld.getWorldTime() / 24000L
                ));
            }

            if (mc.entityRenderer != null && mc.entityRenderer.isShaderActive()) {
                list.add("Shader: " + mc.entityRenderer.getShaderGroup().getShaderGroupName());
            }

            if (mc.objectMouseOver != null
                    && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos1 = mc.objectMouseOver.getBlockPos();
                list.add(String.format("Looking at: %d %d %d", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
            }

            cir.setReturnValue(list);
        }
    }

    @Inject(method = "call", at = @At("RETURN"), cancellable = true)
    private void modifyLeftBrand(CallbackInfoReturnable<List<String>> cir) {
        final BrandSpoofer spoofer = BrandSpoofer.INSTANCE;
        if (!spoofer.handleEvents()) return;

        List<String> list = cir.getReturnValue();
        if (list == null || list.isEmpty()) return;

        final String spoof = resolveSelectedBrand();
        if (spoof == null || spoof.isEmpty()) return;

        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            if (line != null && line.startsWith("Minecraft ") && line.contains("(") && line.endsWith(")")) {
                int slash = line.lastIndexOf('/');
                int open = line.lastIndexOf('(');
                int close = line.lastIndexOf(')');
                if (slash > open && close > slash) {
                    String newLine = line.substring(0, slash + 1) + spoof + ")";
                    list.set(i, newLine);
                    cir.setReturnValue(list);
                    return;
                } else if (open != -1 && close != -1) {
                    String prefix = line.substring(0, open + 1);
                    String newLine = prefix + "1.8.9/" + spoof + ")";
                    list.set(i, newLine);
                    cir.setReturnValue(list);
                    return;
                }
            }
        }
        cir.setReturnValue(list);
    }

    @ModifyVariable(method = "getDebugInfoRight", at = @At(value = "STORE"), ordinal = 0)
    private List<String> modifyDebugInfo(List<String> originalList) {
        final BrandSpoofer brandSpoofer = BrandSpoofer.INSTANCE;
        final boolean lunarSelected = isLunarSelected();

        if (brandSpoofer.handleEvents() && lunarSelected) {
            originalList.add("");
            originalList.add("[LC Async Resources] Absent: " + RandomUtils.INSTANCE.nextInt(0, 5000) + " textures, 0 bytes");
            originalList.add("[LC Async Resources] Low Quality: 0 textures, 0 B");
            originalList.add("[LC Async Resources] Full Quality: 0 textures, 0 B");
        }
        return originalList;
    }

    @Inject(method = "getDebugInfoRight", at = @At("RETURN"), cancellable = true)
    private void modifyDebugInfoRight(CallbackInfoReturnable<List<String>> ci) {
        final BrandSpoofer brandSpoofer = BrandSpoofer.INSTANCE;
        final boolean lunarSelected = isLunarSelected();

        List<String> list = ci.getReturnValue();

        if (!this.isReducedDebug()) {
            if (brandSpoofer.handleEvents() && lunarSelected) {
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