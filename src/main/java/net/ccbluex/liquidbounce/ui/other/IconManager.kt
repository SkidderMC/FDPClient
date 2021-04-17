package net.ccbluex.liquidbounce.ui.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class IconManager : MinecraftInstance() {
    private val icons=HashMap<String,ResourceLocation>()

    init {
        loadIcon("check-circle")
        loadIcon("close-circle")
        loadIcon("information")
        loadIcon("shadow")
    }

    private fun loadIcon(name: String){
        icons[name] = genResource(ImageIO.read(IconManager::class.java.classLoader.getResourceAsStream("icon/$name.png")))
    }

    fun getIcon(name: String): ResourceLocation? {
        return icons[name]
    }

    fun genResource(image: BufferedImage?): ResourceLocation {
        //create and load
        val resourceLocation = ResourceLocation(
            LiquidBounce.CLIENT_NAME.toLowerCase()+"/"
                    + RandomUtils.randomString(10)
        )
        mc.textureManager.loadTexture(resourceLocation, DynamicTexture(image))
        return resourceLocation
    }
}