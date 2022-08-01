package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(value=EnumFacing.class)
public class MixinEnumFacing {
    @Shadow
    @Final
    private int opposite;

    @Overwrite
    public EnumFacing getOpposite() {
        return StaticStorage.facings()[this.opposite];
    }

    @Overwrite
    public static EnumFacing getFront(int n) {
        return StaticStorage.facings()[n % StaticStorage.facings().length];
    }

    @Overwrite
    public static EnumFacing random(Random random) {
        return StaticStorage.facings()[random.nextInt(StaticStorage.facings().length)];
    }

    @Overwrite
    public static EnumFacing getFacingFromVector(float f, float f2, float f3) {
        EnumFacing enumFacing = EnumFacing.NORTH;
        float f4 = Float.MIN_VALUE;
        for (EnumFacing enumFacing2 : StaticStorage.facings()) {
            float f5 = f * (float)enumFacing2.getDirectionVec().getX() + f2 * (float)enumFacing2.getDirectionVec().getY() + f3 * (float)enumFacing2.getDirectionVec().getZ();
            if (!(f5 > f4)) continue;
            f4 = f5;
            enumFacing = enumFacing2;
        }
        return enumFacing;
    }

    @Overwrite
    public static EnumFacing getFacingFromAxis(EnumFacing.AxisDirection axisDirection, EnumFacing.Axis axis) {
        for (EnumFacing enumFacing : StaticStorage.facings()) {
            if (enumFacing.getAxisDirection() != axisDirection || enumFacing.getAxis() != axis) continue;
            return enumFacing;
        }
        throw new IllegalArgumentException("No such direction: " + axisDirection + " " + axis);
    }
}
