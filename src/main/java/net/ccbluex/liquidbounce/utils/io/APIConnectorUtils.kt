/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

object APIConnectorUtils {

    @Volatile
    var canConnect: Boolean = false
        private set
    @Volatile
    var isLatest: Boolean = false
        private set
    @Volatile
    var discord: String = ""
        private set
    @Volatile
    var discordApp: String = ""
        private set
    @Volatile
    var donate: String = ""
        private set
    @Volatile
    var changelogs: String = ""
        private set
    @Volatile
    var bugs: String = ""
        private set
    private var appClientID: String = ""
    private var appClientSecret: String = ""
    private val picturesCache = mutableMapOf<Pair<String, String>, ResourceLocation>()
    private val cacheMutex = Mutex()

    /**
     * Data class representing an image with its metadata.
     */
    data class Picture(val fileName: String, val picType: String, val resourceLocation: ResourceLocation)

    /**
     * Asynchronously loads images and stores them in the cache.
     */
    suspend fun loadPicturesAsync() = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            picturesCache.clear()
            LOGGER.info("Image cache cleared.")
        }
        try {
            val locationsUrl = "${URLRegistryUtils.PICTURES}locations.txt"
            val details = HttpClient.get(locationsUrl).use { response ->
                if (response.code != 200) throw IOException("Failed to fetch locations: HTTP ${response.code}")
                response.body.string().split("---")
            }
            coroutineScope {
                details.forEach { detail ->
                    launch {
                        runCatching {
                            val (fileName, picType) = detail.split(":")
                            val imageUrl = "${URLRegistryUtils.PICTURES}$picType/$fileName.png"
                            HttpClient.get(imageUrl).use { response ->
                                if (response.code != 200) throw IOException("Failed to download image: HTTP ${response.code}")
                                val imageBytes = response.body.byteStream()
                                val bufferedImage: BufferedImage = ImageIO.read(imageBytes)
                                    ?: throw IOException("Failed to decode image: $imageUrl")
                                withContext(Dispatchers.Main) {
                                    try {
                                        val dynamicTexture = DynamicTexture(bufferedImage)
                                        val resourceLocation = MinecraftInstance.mc.textureManager.getDynamicTextureLocation(
                                            FDPClient.clientTitle,
                                            dynamicTexture
                                        )
                                        cacheMutex.withLock {
                                            picturesCache[Pair(fileName, picType)] = resourceLocation
                                        }
                                        LOGGER.info("Image loaded successfully: $fileName, Type: $picType")
                                    } catch (e: Exception) {
                                        LOGGER.error("Failed to create texture for image: $fileName, Type: $picType", e)
                                    }
                                }
                            }
                        }.onFailure { exception ->
                            LOGGER.error("Failed to load image for detail: $detail", exception)
                        }
                    }
                }
            }

            canConnect = true
            LOGGER.info("All image load tasks scheduled successfully.")
        } catch (e: Exception) {
            canConnect = false
            LOGGER.error("Failed to load images from server.", e)
        }
    }

    /**
     * Retrieves the [ResourceLocation] for a specific image and location.
     *
     * @param image The name of the image.
     * @param location The category/location of the image.
     * @return The corresponding [ResourceLocation], or a default one if not found.
     */
    fun callImage(image: String, location: String): ResourceLocation {
        return picturesCache[Pair(image, location)] ?: ResourceLocation("fdpclient/temp.png")
    }

    /**
     * Asynchronously checks the API status and updates relevant properties.
     */
    suspend fun checkStatusAsync() = withContext(Dispatchers.IO) {
        try {
            val details = HttpClient.get(URLRegistryUtils.STATUS).use { response ->
                if (response.code != 200) throw IOException("Failed to fetch status: HTTP ${response.code}")
                response.body.string().split("///")
            }
            require(details.size >= 6) { "Incomplete status data received." }
            appClientID = details[0]
            appClientSecret = details[1]
            discordApp = details[2]
            discord = details[4]
            isLatest = details[5] == FDPClient.clientVersionText
            canConnect = true
            LOGGER.info("API status checked successfully. Is Latest: $isLatest")
        } catch (e: Exception) {
            canConnect = false
            LOGGER.error("Failed to verify API status.", e)
        }
    }

    /**
     * Asynchronously fetches the latest changelogs from the server.
     */
    suspend fun checkChangelogsAsync() = withContext(Dispatchers.IO) {
        try {
            val changelogsResponse = HttpClient.get(URLRegistryUtils.CHANGELOGS).use { response ->
                if (response.code != 200) throw IOException("Failed to fetch changelogs: HTTP ${response.code}")
                response.body.string()
            }
            changelogs = changelogsResponse
            LOGGER.info("Changelogs loaded successfully.")
        } catch (e: Exception) {
            LOGGER.error("Failed to load changelogs.", e)
        }
    }

    /**
     * Asynchronously fetches the latest bugs from the server.
     */
    suspend fun checkBugsAsync() = withContext(Dispatchers.IO) {
        try {
            val bugsResponse = HttpClient.get(URLRegistryUtils.BUGS).use { response ->
                if (response.code != 200) throw IOException("Failed to fetch bugs: HTTP ${response.code}")
                response.body.string()
            }
            bugs = bugsResponse
            LOGGER.info("Bugs loaded successfully.")
        } catch (e: Exception) {
            LOGGER.error("Failed to load bugs.", e)
        }
    }

    /**
     * Executes all API checks asynchronously.
     */
    suspend fun performAllChecksAsync() = coroutineScope {
        launch { checkStatusAsync() }
        launch { checkChangelogsAsync() }
        launch { checkBugsAsync() }
        launch { loadPicturesAsync() }
    }
}