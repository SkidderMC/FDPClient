package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.mojang.patchy.BlockedServers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockedServers.class)
public abstract class MixinBlockedServers {
    /**
     * @author koitoyuu
     * @reason fucked server limit.
     */
    @Overwrite
    public static boolean isBlockedServer(String server) {
        return false;
    }
}
