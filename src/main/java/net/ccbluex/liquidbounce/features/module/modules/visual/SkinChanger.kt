/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager.dir
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.File
import javax.imageio.ImageIO

object SkinChanger : Module("SkinChanger", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Online", "File"), "Online").onChanged {
        invalidate()
    }
        .describe("Load the skin from an online account or a local file.")

    private val username by text("Username", "Notch") { mode == "Online" }.onChanged {
        invalidate()
    }
        .describe("Account name whose skin is fetched online.")

    private val fileName by fileValue("FileName", "skin.png", extensions = listOf("png")) { mode == "File" }.onChanged {
        invalidate()
    }

    private val skinDir = File(dir, "skins").apply { mkdirs() }

    private val fileSkinLocation = ResourceLocation("fdp/skin-changer-from-file")

    private var resolved: ResourceLocation? = null
    private var lastKey = ""

    val skinLocation: ResourceLocation?
        get() = if (handleEvents()) resolved else null

    private fun invalidate() {
        lastKey = ""
        resolved = null
    }

    override fun onEnable() {
        invalidate()
    }

    override fun onDisable() {
        invalidate()
    }

    val onUpdate = handler<UpdateEvent> {
        when (mode) {
            "Online" -> resolveOnline()
            "File" -> resolveFile()
        }
    }

    private fun resolveOnline() {
        val name = username.trim()
        if (name.isEmpty()) {
            resolved = null
            return
        }

        if (lastKey == "online:$name" && resolved != null) {
            return
        }

        lastKey = "online:$name"

        val playerInfoMap = mc.netHandler?.playerInfoMap ?: run {
            resolved = null
            return
        }

        val info = synchronized(playerInfoMap) {
            ArrayList(playerInfoMap)
        }.firstOrNull {
            it?.gameProfile?.name.equals(name, ignoreCase = true)
        }

        resolved = info?.locationSkin
    }

    private fun resolveFile() {
        val target = fileName.trim()
        if (target.isEmpty()) {
            resolved = null
            return
        }

        if (lastKey == "file:$target" && resolved != null) {
            return
        }

        lastKey = "file:$target"

        runCatching {
            val configured = File(target)
            val file = if (configured.isAbsolute || configured.isFile) configured else File(skinDir, target)
            if (!file.isFile) {
                if (handleEvents()) {
                    chat("Skin file not found: ${file.absolutePath}")
                }
                resolved = null
                return
            }

            val image = ImageIO.read(file) ?: run {
                resolved = null
                return
            }

            mc.textureManager.loadTexture(fileSkinLocation, DynamicTexture(image))
            resolved = fileSkinLocation
        }.onFailure {
            LOGGER.error("Failed to load custom skin", it)
            resolved = null
        }
    }
}
