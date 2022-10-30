package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.client.renderer.tileentity.TileEntityBannerRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(TileEntityBannerRenderer.class)
public class MixinTileEntityBannerRenderer {
    @Shadow
    @Final
    private static Map<String, TileEntityBannerRenderer.TimedBannerTexture> DESIGNS;

    @Shadow
    @Final
    private static ResourceLocation BANNERTEXTURES;

    /**
     * @author asbyth
     * @reason Resolve banners in chests not displaying once cache is full
     */
    @Overwrite
    private ResourceLocation func_178463_a(TileEntityBanner banner) {
        String texture = banner.getPatternResourceLocation();

        if (texture.isEmpty()) {
            return null;
        } else {
            TileEntityBannerRenderer.TimedBannerTexture timedTexture = DESIGNS.get(texture);
            if (timedTexture == null) {
                if (DESIGNS.size() >= 256 && !this.freeCacheSlot()) {
                    return BANNERTEXTURES;
                }

                List<TileEntityBanner.EnumBannerPattern> patternList = banner.getPatternList();
                List<EnumDyeColor> colorList = banner.getColorList();
                List<String> patternPath = Lists.newArrayList();

                for (TileEntityBanner.EnumBannerPattern pattern : patternList) {
                    patternPath.add("textures/entity/banner/" + pattern.getPatternName() + ".png");
                }

                timedTexture = new TileEntityBannerRenderer.TimedBannerTexture();
                timedTexture.bannerTexture = new ResourceLocation(texture);
                Minecraft.getMinecraft().getTextureManager().loadTexture(timedTexture.bannerTexture, new LayeredColorMaskTexture(BANNERTEXTURES, patternPath, colorList));
                DESIGNS.put(texture, timedTexture);
            }

            timedTexture.systemTime = System.currentTimeMillis();
            return timedTexture.bannerTexture;
        }
    }

    @Unique
    private boolean freeCacheSlot() {
        long start = System.currentTimeMillis();
        Iterator<String> iterator = DESIGNS.keySet().iterator();

        while (iterator.hasNext()) {
            String next = iterator.next();
            TileEntityBannerRenderer.TimedBannerTexture timedTexture = DESIGNS.get(next);

            if ((start - timedTexture.systemTime) > 5000L) {
                Minecraft.getMinecraft().getTextureManager().deleteTexture(timedTexture.bannerTexture);
                iterator.remove();
                return true;
            }
        }

        return DESIGNS.size() < 256;
    }

    @Redirect(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityBanner;DDDFI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTotalWorldTime()J"))
    private long resolveOverflow(World world) {
        return world.getTotalWorldTime() % 100L;
    }
}
