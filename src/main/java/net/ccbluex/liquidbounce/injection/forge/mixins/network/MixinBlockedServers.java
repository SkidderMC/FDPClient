/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.mojang.patchy.BlockedServers;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockedServers.class)
public abstract class MixinBlockedServers {
    /**
     * @author opZywl
     * @reason PATCH
     */
    @Overwrite(remap = false)
    public static boolean isBlockedServer(String server) {
        return MinecraftInstance.mc.isIntegratedServerRunning();
    }
}
