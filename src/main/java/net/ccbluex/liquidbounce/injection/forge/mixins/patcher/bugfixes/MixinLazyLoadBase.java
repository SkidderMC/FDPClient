/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.patcher.bugfixes;

import net.minecraft.util.LazyLoadBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LazyLoadBase.class)
public abstract class MixinLazyLoadBase<T> {
    @Shadow private boolean isLoaded;

    @Shadow private T value;

    @Shadow protected abstract T load();

    /**
     * @author LlamaLad7
     * @reason Fix race condition
     */
    @Overwrite
    public T getValue() {
        //noinspection DoubleCheckedLocking
        if (!this.isLoaded) {
            synchronized (this) {
                if (!this.isLoaded) {
                    this.value = this.load();
                    this.isLoaded = true;
                }
            }
        }

        return this.value;
    }
}
