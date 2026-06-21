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
import net.ccbluex.liquidbounce.utils.client.chat
import kotlin.concurrent.thread

object AutoConfig : Module("AutoConfig", Category.CLIENT, Category.SubCategory.CONFIGS, gameDetecting = false) {

    private val autoLoadValue = boolean("AutoLoadOnJoin", true)
    private val notifyValue = boolean("Notify", true)
    private val baseUrlValue = text("BaseURL", "https://raw.githubusercontent.com/")
    private val suffixValue = text("URLSuffix", ".txt")
    private val blacklistValue = text("Blacklist", "poke.sexy,loyisa.cn,anticheat-test.com")

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

        val url = "${baseUrlValue.get()}$domain${suffixValue.get()}"

        thread(name = "AutoConfig-Loader") {
            runCatching {
                SettingsUtils.applyScript(SettingsUtils.loadFromUrl(url))
            }.onSuccess {
                if (notifyValue.get()) {
                    chat("§7[§3§lAutoConfig§7] §7Loaded config for §a§l$domain§7.")
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
