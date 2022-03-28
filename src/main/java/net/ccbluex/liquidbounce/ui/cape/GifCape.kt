package net.ccbluex.liquidbounce.ui.cape

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode

class GifCape(name: String, imageIS: InputStream) : DynamicCape(name) {

    init {
        val reader = ImageIO.getImageReadersBySuffix("gif").next()
        reader.setInput(ImageIO.createImageInputStream(imageIS), false)

        var delay = 0
        var baseImage: BufferedImage? = null
        for(i in 0 until reader.getNumImages(true)) {
            val frame = reader.read(i)
            frames.add(if(baseImage != null) {
                val image = BufferedImage(baseImage.width, baseImage.height, BufferedImage.TYPE_INT_ARGB)
                image.graphics.drawImage(baseImage, 0, 0, null)
                image.graphics.drawImage(frame, 0, 0, null)
                image
            } else {
                baseImage = frame
                frame
            })

            delay += getDelayMills(reader.getImageMetadata(i))
            delays.add(delay)
        }

        playTime = delay

        val mc = Minecraft.getMinecraft()
        frames.forEachIndexed { index, image ->
            mc.textureManager.loadTexture(ResourceLocation(path + index), DynamicTexture(image))
        }
    }

    private fun getDelayMills(imageMetaData: IIOMetadata): Int {
        val metaFormatName = imageMetaData.nativeMetadataFormatName
        val root = imageMetaData.getAsTree(metaFormatName) as IIOMetadataNode
        val graphicsControlExtensionNode = getNode(root, "GraphicControlExtension")
        return graphicsControlExtensionNode.getAttribute("delayTime").toInt() * 10
    }

    private fun getNode(rootNode: IIOMetadataNode, nodeName: String): IIOMetadataNode {
        val nNodes = rootNode.length
        for (i in 0 until nNodes) {
            if (rootNode.item(i).nodeName.compareTo(nodeName, ignoreCase = true) == 0) {
                return rootNode.item(i) as IIOMetadataNode
            }
        }
        val node = IIOMetadataNode(nodeName)
        rootNode.appendChild(node)
        return node
    }
}