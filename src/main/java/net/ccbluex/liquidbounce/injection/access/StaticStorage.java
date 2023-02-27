package net.ccbluex.liquidbounce.injection.access;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;

/**
 * values() will cause performance issues, so we store them in a static array.
 * We use ASM to replace values() with our own array. [net.ccbluex.liquidbounce.injection.transformers.OptimizeTransformer]
 * https://stackoverflow.com/questions/2446135/is-there-a-performance-hit-when-using-enum-values-vs-string-arrays
 *
 * in my tests, this is 10 times faster than using values()
 * I access them 1145141919 times and save EnumFacing.name into a local variable in my test
 * EnumFacings.values() cost 122 ms
 * StaticStorage.facings() cost 15 ms
 *
 * @author liulihaocai
 */
public class StaticStorage {

    private static final EnumFacing[] facings = EnumFacing.values();
    private static final EnumChatFormatting[] chatFormatting = EnumChatFormatting.values();
    private static final EnumParticleTypes[] particleTypes = EnumParticleTypes.values();
    private static final EnumWorldBlockLayer[] worldBlockLayers = EnumWorldBlockLayer.values();

    public static ScaledResolution scaledResolution;

    public static EnumFacing[] facings() {
        return facings;
    }

    public static EnumChatFormatting[] chatFormatting() {
        return chatFormatting;
    }

    public static EnumParticleTypes[] particleTypes() {
        return particleTypes;
    }

    public static EnumWorldBlockLayer[] worldBlockLayers() {
        return worldBlockLayers;
    }
}
