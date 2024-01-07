/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.resources;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.DefaultResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

/**
 * The type Mixin default resource pack.
 */
@Mixin(DefaultResourcePack.class)
public class MixinDefaultResourcePack {

    /**
     * The constant defaultResourceDomains.
     */
    @Final
    @Shadow
    public static final Set<String> defaultResourceDomains = ImmutableSet
            .of("minecraft", "realms");

}
