package net.ccbluex.liquidbounce.ui.cape

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage

class Cape(val name: String, val image: BufferedImage) {
    val resource = ResourceLocation("fdp/cape/${name.lowercase().replace(" ","_")}")

    init {
        val mc = Minecraft.getMinecraft()
        // this MUST be run on minecraft main thread
        mc.addScheduledTask {
            mc.textureManager.loadTexture(resource, DynamicTexture(image))
        }
    }
}