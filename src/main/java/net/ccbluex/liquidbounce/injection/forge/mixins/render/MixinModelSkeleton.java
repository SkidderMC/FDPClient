package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelSkeleton;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelSkeleton.class)
public class MixinModelSkeleton extends ModelBiped {

    @Override
    public void postRenderArm(float scale) {
        this.bipedRightArm.rotationPointX++;
        this.bipedRightArm.postRender(scale);
        this.bipedRightArm.rotationPointX--;
    }

}
