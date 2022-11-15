/*
 * ZAVZ Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.patcher.bugfixes;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystem;

import java.util.*;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager {
    @Shadow public abstract boolean isSoundPlaying(ISound sound);

    @Shadow @Final private Map<String, ISound> playingSounds;

    private final List<String> p_pausedSounds = new ArrayList<>();

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Redirect(
        method = "pauseAllSounds",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;pause(Ljava/lang/String;)V", remap = false)
    )
    private void p_onlyPauseSoundIfNecessary(@Coerce SoundSystem soundSystem, String sound) {
        if (isSoundPlaying(playingSounds.get(sound))) {
            soundSystem.pause(sound);
            p_pausedSounds.add(sound);
        }
    }

    @Redirect(
        method = "resumeAllSounds",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false)
    )
    private Iterator<String> p_iterateOverPausedSounds(Set<String> keySet) {
        return p_pausedSounds.iterator();
    }

    @Redirect(
        method = "playSound",
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=Unable to play unknown soundEvent: {}", ordinal = 0)),
        at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Lorg/apache/logging/log4j/Marker;Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 0, remap = false)
    )
    private void p_silenceWarning(Logger instance, Marker marker, String s, Object[] objects) {
        // No-op
    }

    @Inject(method = "resumeAllSounds", at = @At("TAIL"))
    private void p_clearPausedSounds(CallbackInfo ci) {
        p_pausedSounds.clear();
    }
}
