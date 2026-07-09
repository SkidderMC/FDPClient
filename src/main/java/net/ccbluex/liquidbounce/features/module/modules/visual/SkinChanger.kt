/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager.dir
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.get
import net.ccbluex.liquidbounce.utils.io.readJson
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.minecraft.client.renderer.IImageBuffer
import net.minecraft.client.renderer.ImageBufferDownload
import net.minecraft.client.renderer.ThreadDownloadImageData
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
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

    @Volatile
    private var pendingKey = ""

    val skinLocation: ResourceLocation?
        get() = if (handleEvents()) resolved else null

    private fun invalidate() {
        lastKey = ""
        pendingKey = ""
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

        val key = "online:$name"
        if (lastKey == key) {
            return
        }

        lastKey = key
        pendingKey = key

        SharedScopes.IO.launch {
            runCatching {
                val uuid = fetchUuid(name) ?: return@launch
                val skinUrl = fetchSkinUrl(uuid) ?: return@launch

                mc.addScheduledTask {
                    if (pendingKey != key) return@addScheduledTask
                    applyDownloadedSkin(key, uuid, skinUrl)
                }
            }.onFailure {
                LOGGER.error("Failed to resolve skin for $name", it)
            }
        }
    }

    private fun applyDownloadedSkin(key: String, uuid: String, skinUrl: String) {
        val location = ResourceLocation("fdp/skin-changer/$uuid")
        val texture = ThreadDownloadImageData(
            File(skinDir, "$uuid.png"),
            skinUrl,
            null,
            object : IImageBuffer {
                override fun parseUserSkin(image: BufferedImage?): BufferedImage? =
                    runCatching { ImageBufferDownload().parseUserSkin(image) }.getOrNull() ?: image

                override fun skinAvailable() {
                    mc.addScheduledTask { if (pendingKey == key) resolved = location }
                }
            }
        )
        mc.textureManager.loadTexture(location, texture)
    }

    private fun fetchUuid(name: String): String? =
        HttpClient.get("https://api.mojang.com/users/profiles/minecraft/$name").use { response ->
            if (!response.isSuccessful) return null
            response.body.charStream().readJson().asJsonObject.get("id")?.asString?.takeIf(String::isNotBlank)
        }

    private fun fetchSkinUrl(uuid: String): String? =
        HttpClient.get("https://sessionserver.mojang.com/session/minecraft/profile/$uuid").use { response ->
            if (!response.isSuccessful) return null
            val properties = response.body.charStream().readJson().asJsonObject
                .getAsJsonArray("properties") ?: return null
            val encoded = properties.firstOrNull {
                it.asJsonObject.get("name")?.asString == "textures"
            }?.asJsonObject?.get("value")?.asString ?: return null
            val decoded = String(java.util.Base64.getDecoder().decode(encoded), Charsets.UTF_8)
            decoded.reader().readJson().asJsonObject
                .getAsJsonObject("textures")
                ?.getAsJsonObject("SKIN")
                ?.get("url")?.asString?.takeIf(String::isNotBlank)
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

            val processed = runCatching { ImageBufferDownload().parseUserSkin(image) }.getOrNull() ?: image
            mc.textureManager.loadTexture(fileSkinLocation, DynamicTexture(processed))
            resolved = fileSkinLocation
        }.onFailure {
            LOGGER.error("Failed to load custom skin", it)
            resolved = null
        }
    }
}
