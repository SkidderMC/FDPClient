/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.config.SettingsUtils
import net.ccbluex.liquidbounce.event.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.api.PresetCatalogService
import net.ccbluex.liquidbounce.utils.client.chat
import kotlin.concurrent.thread

object AutoConfig : Module("AutoConfig", Category.CLIENT, Category.SubCategory.CONFIGS, gameDetecting = false) {

    private val autoLoadValue = boolean("AutoLoadOnJoin", true)
        .describe("Load a config automatically when joining a server.")
    private val notifyValue = boolean("Notify", true)
        .describe("Show chat messages about config loading.")
    private val catalogUrlValue = text("CatalogURL", "")
        .describe("HTTPS URL of a versioned preset catalog whose scripts carry mandatory SHA-256 digests.")
    private val catalogSha256Value = text("CatalogSHA256", "") { catalogUrlValue.get().isNotBlank() }
        .describe("Pinned SHA-256 for the catalog itself; update it only after reviewing a new catalog release.")
    private val allowLegacyUnsignedValue = boolean("AllowLegacyUnsigned", false)
        .describe("Allow the old unverified per-domain URL format. Unsafe and disabled by default.")
    private val baseUrlValue = text("LegacyBaseURL", "https://raw.githubusercontent.com/") { allowLegacyUnsignedValue.get() }
        .describe("Legacy base URL used only when unsigned loading is explicitly enabled.")
    private val suffixValue = text("LegacyURLSuffix", ".txt") { allowLegacyUnsignedValue.get() }
        .describe("Suffix appended to the config URL.")
    private val blacklistValue = text("Blacklist", "poke.sexy,loyisa.cn,anticheat-test.com")
        .describe("Comma-separated domains to never load configs for.")

    private fun currentDomain(): String? {
        val serverData = mc.currentServerData ?: return null
        if (mc.isSingleplayer) return null
        val ip = serverData.serverIP ?: return null
        val withoutPort = ip.substringBefore(':').trim().lowercase()
        if (withoutPort.isEmpty()) return null

        val parts = withoutPort.split('.')
        return if (parts.size >= 2) {
            parts.takeLast(2).joinToString(".")
        } else {
            withoutPort
        }
    }

    private fun isBlacklisted(domain: String): Boolean {
        return blacklistValue.get()
            .split(',')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .any { domain.endsWith(it) }
    }

    fun loadForCurrentServer() {
        val domain = currentDomain()

        if (domain == null) {
            if (notifyValue.get()) {
                chat("§7[§3§lAutoConfig§7] §cYou are not connected to a server.")
            }
            return
        }

        if (isBlacklisted(domain)) {
            if (notifyValue.get()) {
                chat("§7[§3§lAutoConfig§7] §7This server is blacklisted.")
            }
            return
        }

        thread(name = "AutoConfig-Loader") {
            runCatching {
                val catalogUrl = catalogUrlValue.get().trim()
                when {
                    catalogUrl.isNotEmpty() -> {
                        val catalog = PresetCatalogService.fetchCatalog(catalogUrl, catalogSha256Value.get().trim())
                        val preset = PresetCatalogService.findForServer(catalog, domain)
                            ?: error("No verified preset for $domain")
                        PresetCatalogService.downloadVerified(catalogUrl, preset)
                    }
                    allowLegacyUnsignedValue.get() ->
                        SettingsUtils.loadFromUrl("${baseUrlValue.get()}$domain${suffixValue.get()}")
                    else -> error("No verified preset catalog configured")
                }
            }.onSuccess { script ->
                mc.addScheduledTask {
                    SettingsUtils.applyScript(script)
                    if (notifyValue.get()) {
                        chat("§7[§3§lAutoConfig§7] §7Loaded config for §a§l$domain§7.")
                    }
                }
            }.onFailure {
                if (notifyValue.get()) {
                    chat("§7[§3§lAutoConfig§7] §7No config found for §a§l$domain§7.")
                }
            }
        }
    }

    override fun onEnable() {
        loadForCurrentServer()
    }

    val onWorldChange = handler<WorldChangeEvent> { event ->
        if (!autoLoadValue.get()) {
            return@handler
        }

        if (event.worldClient == null) {
            return@handler
        }

        loadForCurrentServer()
    }
}
