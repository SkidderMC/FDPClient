package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.animation.AnimationModelBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={AnimationModelBase.class})
public class MixinAnimationModelBase {
    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] render() {
        return StaticStorage.enumFacings;
    }
}