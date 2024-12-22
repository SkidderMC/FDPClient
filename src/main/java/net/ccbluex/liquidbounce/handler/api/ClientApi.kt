/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.api

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.io.HttpUtils.applyBypassHttps
import net.ccbluex.liquidbounce.utils.io.decodeJson
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

private const val HARD_CODED_BRANCH = "legacy"

private const val API_V1_ENDPOINT = "https://api.liquidbounce.net/api/v1"

/**
 * User agent
 * <version> (<commit>, <branch>, <build-type>, <platform>)
 */
private val ENDPOINT_AGENT =
    "${FDPClient.CLIENT_NAME}/${FDPClient.clientVersionText} (${FDPClient.clientCommit}, ${FDPClient.clientBranch}, ${if (FDPClient.IN_DEV) "dev" else "release"}, ${System.getProperty("os.name")})"

/**
 * Session token
 *
 * This is used to identify the client in one session
 */
private val SESSION_TOKEN = RandomUtils.randomString(16)

private val client = OkHttpClient.Builder()
    .connectTimeout(3, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .applyBypassHttps()
    .addInterceptor { chain ->
        val original = chain.request()
        val request: Request = original.newBuilder()
            .header("User-Agent", ENDPOINT_AGENT)
            .header("X-Session-Token", SESSION_TOKEN)
            .build()

        chain.proceed(request)
    }.build()

/**
 * ClientApi
 */
object ClientApi {

    fun getNewestBuild(branch: String = HARD_CODED_BRANCH, release: Boolean = false): Build {
        val url = "$API_V1_ENDPOINT/version/newest/$branch${if (release) "/release" else "" }"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Request failed: ${response.code}")
            return response.body!!.charStream().decodeJson()
        }
    }

    fun getMessageOfTheDay(branch: String = HARD_CODED_BRANCH): MessageOfTheDay {
        val url = "$API_V1_ENDPOINT/client/$branch/motd"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Request failed: ${response.code}")
            return response.body!!.charStream().decodeJson()
        }
    }

    fun getSettingsList(branch: String = HARD_CODED_BRANCH): List<AutoSettings> {
        val url = "$API_V1_ENDPOINT/client/$branch/settings"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Request failed: ${response.code}")
            return response.body!!.charStream().decodeJson()
        }
    }

    fun getSettingsScript(branch: String = HARD_CODED_BRANCH, settingId: String): String {
        val url = "$API_V1_ENDPOINT/client/$branch/settings/$settingId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Request failed: ${response.code}")
            return response.body!!.string()
        }
    }

    // TODO: backend not implemented yet
    fun reportSettings(branch: String = HARD_CODED_BRANCH, settingId: String): ReportResponse {
        val url = "$API_V1_ENDPOINT/client/$branch/settings/report/$settingId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Request failed: ${response.code}")
            return response.body!!.charStream().decodeJson()
        }
    }

    // TODO: backend not implemented yet
    fun uploadSettings(
        branch: String = HARD_CODED_BRANCH,
        name: RequestBody,
        contributors: RequestBody,
        settingsFile: MultipartBody.Part
    ): UploadResponse {
        val url = "$API_V1_ENDPOINT/client/$branch/settings/upload"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name", null, name)
            .addFormDataPart("contributors", null, contributors)
            .addPart(settingsFile)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Request failed: ${response.code}")
            return response.body!!.charStream().decodeJson()
        }
    }
}