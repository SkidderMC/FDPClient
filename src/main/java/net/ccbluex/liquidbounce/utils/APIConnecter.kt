/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object APIConnecter {

    var canConnect = false
    var isLatest = false
    var discord = ""
    var discordApp = ""
    var appClientID = ""
    var appClientSecret = ""
    var donate = ""
    var changelogs = ""
    var bugs = ""

    private var pictures = mutableListOf<Triple<String, String, ResourceLocation>>()
    private var donorCapeLocations = mutableListOf<Pair<String, ResourceLocation>>()

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
            return arrayOf()
        }
    })
    private val sslContext = SSLContext.getInstance("TLS")

    private fun tlsAuthConnectionFixes() {
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
    }

    fun callImage(image: String, location: String): ResourceLocation {
        for ((i, l, s) in pictures) {
            if (i == image && l == location)
                return s
        }
        return ResourceLocation("fdpclient/temp.png")
    }

    fun loadPictures() {
        try {
            if (pictures.isNotEmpty())
                pictures.clear()
            var gotNames: String
            tlsAuthConnectionFixes()
            val nameClient = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .build()
            val nameBuilder = Request.Builder().url(URLComponent.PICTURES + "locations.txt")
            val nameRequest: Request = nameBuilder.build()
            nameClient.newCall(nameRequest).execute().use { response ->
                gotNames = response.body!!.string()
            }
            val details = gotNames.split("---")
            for (i in details) {
                var gotImage: BufferedImage
                val fileName = i.split(":")[0]
                val picType = i.split(":")[1]
                tlsAuthConnectionFixes()
                gotImage = ImageIO.read(URL(URLComponent.PICTURES + picType + "/" + fileName + ".png"))
                pictures.add(
                    Triple(
                        fileName,
                        picType,
                        MinecraftInstance.mc.textureManager.getDynamicTextureLocation(
                            FDPClient.clientTitle,
                            DynamicTexture(gotImage)
                        )
                    )
                )
                LOGGER.info("Load Picture $fileName, $picType")
            }
            canConnect = true
            LOGGER.info("Loaded Pictures")
        } catch (e: Exception) {
            canConnect = false
            LOGGER.info("Failed to load Pictures")
        }
    }

    fun loadCape(player: EntityPlayer): ResourceLocation? {
        for ((i, l) in donorCapeLocations) {
            if (i == player.uniqueID.toString())
                return l
        }
        return null
    }

    fun loadDonors() {
        try {
            if (donorCapeLocations.isNotEmpty())
                donorCapeLocations.clear()
            var gotNames: String
            tlsAuthConnectionFixes()
            val nameClient = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .build()
            val nameBuilder = Request.Builder().url(URLComponent.DONORS + "users.txt")
            val nameRequest: Request = nameBuilder.build()
            nameClient.newCall(nameRequest).execute().use { response ->
                gotNames = response.body!!.string()
            }
            val details = gotNames.split("///")
            for (i in details) {
                var gotCapes: BufferedImage
                val uuid = i.split(":")[0]
                val cape = i.split(":")[1]
                tlsAuthConnectionFixes()
                gotCapes = ImageIO.read(URL(URLComponent.DONORS + cape))
                donorCapeLocations.add(
                    Pair(
                        uuid,
                        MinecraftInstance.mc.textureManager.getDynamicTextureLocation(
                            FDPClient.CLIENT_NAME,
                            DynamicTexture(gotCapes)
                        )
                    )
                )
            }
            canConnect = true
            LOGGER.info("Loaded Donor Capes")
        } catch (e: Exception) {
            canConnect = false
            LOGGER.info("Failed to load Donor Capes")
        }
    }

    fun checkStatus() {
        try {
            var gotData: String
            tlsAuthConnectionFixes()
            val client = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .build()
            val builder = Request.Builder().url(URLComponent.STATUS)
            val request: Request = builder.build()
            client.newCall(request).execute().use { response ->
                gotData = response.body!!.string()
            }
            val details = gotData.split("///")
            isLatest = details[5] == FDPClient.clientVersionText
            discord = details[4]
            donate = details[3]
            discordApp = details[2]
            appClientSecret = details[1]
            appClientID = details[0]
            canConnect = true
            LOGGER.info("Loaded API")
        } catch (e: Exception) {
            canConnect = false
            LOGGER.info("Failed to load API")
        }
    }

    fun checkChangelogs() {
        try {
            var gotData: String
            tlsAuthConnectionFixes()
            val client = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .build()
            val builder = Request.Builder().url(URLComponent.CHANGELOGS)
            val request: Request = builder.build()
            client.newCall(request).execute().use { response ->
                gotData = response.body!!.string()
            }
            changelogs = gotData
            LOGGER.info("Loaded Changelogs")
        } catch (e: Exception) {
            LOGGER.info("Failed to load Changelogs")
        }
    }

    fun checkBugs() {
        try {
            var gotData: String
            tlsAuthConnectionFixes()
            val client = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .build()
            val builder = Request.Builder().url(URLComponent.BUGS)
            val request: Request = builder.build()
            client.newCall(request).execute().use { response ->
                gotData = response.body!!.string()
            }
            bugs = gotData
            LOGGER.info("Loaded Bugs")
        } catch (e: Exception) {
            LOGGER.info("Failed to load Bugs")
        }
    }
}