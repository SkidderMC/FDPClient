package net.skiddermc.fdpclient.ui.cape

import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage


abstract class DynamicCape(override val name: String) : ICape {

    protected val frames = mutableListOf<BufferedImage>()
    protected val delays = mutableListOf<Int>()
    protected var playTime = 0
    protected val path = "fdp/cape/${name.lowercase().replace(" ","_")}_frame"

    override val cape: ResourceLocation
        get() {
            val frameTime = System.currentTimeMillis() % playTime
            var frameId = 0
            for(i in delays.indices) {
                if(frameTime < delays[i]) {
                    break
                }
                frameId = i
            }
            return ResourceLocation(path + frameId)
        }

    override fun finalize() {
        val mc = Minecraft.getMinecraft()
        for (i in 0 until frames.size) {
            mc.textureManager.deleteTexture(ResourceLocation(path + i))
        }
    }
}