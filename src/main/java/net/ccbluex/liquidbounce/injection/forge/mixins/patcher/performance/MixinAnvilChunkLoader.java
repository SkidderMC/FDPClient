/*
 * ZAVZ Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.patcher.performance;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.DataInputStream;
import java.io.IOException;

@Mixin(AnvilChunkLoader.class)
public class MixinAnvilChunkLoader {
    // Local Capture misses this variable, so we have to resort to a Redirect.
    @Redirect(
        method = "loadChunk__Async",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompressedStreamTools;read(Ljava/io/DataInputStream;)Lnet/minecraft/nbt/NBTTagCompound;")
    )
    private NBTTagCompound closeStream(DataInputStream stream) throws IOException {
        NBTTagCompound result = CompressedStreamTools.read(stream);
        stream.close();
        return result;
    }
}
