package net.ccbluex.liquidbounce.injection.forge.mixins.packets;

import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(S19PacketEntityHeadLook.class)
public class MixinS19PacketEntityHeadLook {

    @Inject(method = "getEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void addNullCheck(World worldIn, CallbackInfoReturnable<Entity> cir) {
        if (worldIn == null) {
            cir.setReturnValue(null);
        }
    }
}
