/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.io.flipSafely
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.Display
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import javax.imageio.ImageIO

@SideOnly(Side.CLIENT)
object IconUtils {
    @JvmStatic
    fun initLwjglIcon(): Boolean {
        val icons = getFavicon()?.filterNotNull()?.toTypedArray()
        if (!icons.isNullOrEmpty()) {
            Display.setIcon(icons)
            return true
        }
        return false
    }

    fun getFavicon() =
        IconUtils::class.java.runCatching {
            arrayOf(
                readImageToBuffer(getResourceAsStream("/assets/minecraft/${CLIENT_NAME.lowercase()}/32.png")),
                readImageToBuffer(getResourceAsStream("/assets/minecraft/${CLIENT_NAME.lowercase()}/32.png")),
                readImageToBuffer(getResourceAsStream("/assets/minecraft/${CLIENT_NAME.lowercase()}/64.png"))
            )
        }.onFailure {
            ClientUtils.LOGGER.warn("Failed to load icons", it)
        }.getOrNull()

    @Throws(IOException::class)
    private fun readImageToBuffer(imageStream: InputStream?): ByteBuffer? {
        val bufferedImage = ImageIO.read(imageStream ?: return null)
        val rgb = bufferedImage.getRGB(0, 0, bufferedImage.width, bufferedImage.height, null, 0, bufferedImage.width)
        val byteBuffer = ByteBuffer.allocate(4 * rgb.size)
        for (i in rgb)
            byteBuffer.putInt(i shl 8 or (i shr 24 and 255))
        byteBuffer.flipSafely()
        return byteBuffer
    }
}