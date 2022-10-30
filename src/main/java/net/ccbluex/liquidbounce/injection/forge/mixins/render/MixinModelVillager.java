
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.minecraft.client.model.ModelVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ModelVillager.class)
public class MixinModelVillager {

    @ModifyConstant(method = "<init>(FFII)V", constant = @Constant(intValue = 18))
    private int changeTextureHeight(int original) {
        return 20;
    }
}
