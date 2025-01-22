/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.api

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import java.text.SimpleDateFormat

// Define a loadingLock object to synchronize access to the settings loading code
private val loadingLock = Mutex()

// Define a mutable list of AutoSetting objects to store the loaded settings
var autoSettingsList: Array<AutoSettings>? = null

// Define a function to load settings from a remote GitHub repository
fun loadSettings(useCached: Boolean, timeout: Long? = null, callback: (Array<AutoSettings>) -> Unit = {}) {
    // Launch a new job to perform the loading operation
    val job = SharedScopes.IO.launch {
        // Synchronize access to the loading code to prevent concurrent loading of settings
        loadingLock.withLock {
            // If cached settings are requested and have been loaded previously, return them immediately
            if (useCached && autoSettingsList != null) {
                callback(autoSettingsList!!)
                return@launch
            }

            try {
                // Fetch the settings list from the API
                val autoSettings = ClientApi.getSettingsList().map {
                    runCatching {
                        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(it.date)
                        val statusDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(it.statusDate)

                        val humanReadableDateFormat = SimpleDateFormat()

                        it.date = humanReadableDateFormat.format(date)
                        it.statusDate = humanReadableDateFormat.format(statusDate)
                    }.onFailure {
                        LOGGER.error("Failed to parse date.", it)
                    }

                    it
                }.toTypedArray()

                // Invoke the callback with the parsed AutoSetting objects and store them in the cache for future use
                callback(autoSettings)
                autoSettingsList = autoSettings
            } catch (e: Exception) {
                LOGGER.error("Failed to fetch auto settings list.", e)

                // If an error occurs, display an error message to the user
                chat("Failed to fetch auto settings list.")
            }
        }
    }

    // If a timeout is provided, block the current thread until the loading thread completes or the timeout is reached
    if (timeout != null) {
        runBlocking {
            withTimeoutOrNull(timeout) {
                job.join()
            }
        }
    }
}