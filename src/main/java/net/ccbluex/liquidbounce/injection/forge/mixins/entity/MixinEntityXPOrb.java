package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityXPOrb.class)
public class MixinEntityXPOrb {
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;getEyeHeight()F"))
    private float lowerHeight(EntityPlayer entityPlayer) {
        return (float) (entityPlayer.getEyeHeight() / 2.0D);
    }
}
