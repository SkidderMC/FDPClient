package net.ccbluex.liquidbounce.injection.forge.mixins.resources;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.DefaultResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(DefaultResourcePack.class)
public class MixinDefaultResourcePack {

    @Shadow
    public static final Set<String> defaultResourceDomains = ImmutableSet
            .of("minecraft", "realms");

}
