/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.world;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Some servers send malformed scoreboard team packets that try to create a team that already
 * exists, or remove a team that does not. Vanilla 1.8.9 throws IllegalArgumentException /
 * NullPointerException for those, which spams the client thread. These guards make the
 * operations idempotent/null-safe instead of throwing.
 */
@Mixin(Scoreboard.class)
public abstract class MixinScoreboard {

    @Shadow
    public abstract ScorePlayerTeam getTeam(String name);

    @Inject(method = "createTeam", at = @At("HEAD"), cancellable = true)
    private void fdpGuardCreateTeam(String name, CallbackInfoReturnable<ScorePlayerTeam> cir) {
        final ScorePlayerTeam existing = getTeam(name);
        if (existing != null) {
            cir.setReturnValue(existing);
        }
    }

    @Inject(method = "removeTeam", at = @At("HEAD"), cancellable = true)
    private void fdpGuardRemoveTeam(ScorePlayerTeam team, CallbackInfo ci) {
        if (team == null) {
            ci.cancel();
        }
    }
}
