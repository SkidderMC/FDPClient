
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

    @Mixin(KeyBinding.class)
    public interface IMixinKeyBinding {
        @Accessor void setPressed(boolean pressed);
    }

