package net.ccbluex.liquidbounce.ui.cape

import com.sksamuel.scrimage.nio.AnimatedGifReader
import com.sksamuel.scrimage.nio.ImageSource
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.InputStream

class GifCape(name: String, imageIS: InputStream) : DynamicCape(name) {

    init {
        val gif = AnimatedGifReader.read(ImageSource.of(imageIS))

        var delay = 0
        for(i in 0 until gif.frameCount) {
            frames.add(gif.frames[i].awt())

            delay += gif.getDelay(i).toMillis().toInt()
            delays.add(delay)
        }

        playTime = delay

        val mc = Minecraft.getMinecraft()
        frames.forEachIndexed { index, image ->
            mc.textureManager.loadTexture(ResourceLocation(path + index), DynamicTexture(image))
        }
    }
}