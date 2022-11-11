/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(BlockRedstoneTorch.class)
public class MixinBlockRedstoneTorch {
    @Shadow private static Map<World, List<BlockRedstoneTorch.Toggle>> toggles = new WeakHashMap<>();
}
