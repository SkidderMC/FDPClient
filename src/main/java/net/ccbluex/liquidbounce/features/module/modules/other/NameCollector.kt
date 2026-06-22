/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import java.io.File

object NameCollector : Module("NameCollector", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val saveToFile by boolean("SaveToFile", true)
        .describe("Write collected player names to a text file on disable.")

    private val collected = linkedSetOf<String>()
    private var lastCollect = 0L

    val onUpdate = handler<UpdateEvent> {
        val netHandler = mc.netHandler ?: return@handler

        val now = System.currentTimeMillis()
        if (now - lastCollect < 500L) {
            return@handler
        }
        lastCollect = now

        for (info in netHandler.playerInfoMap.filterNotNull()) {
            val name = info.gameProfile?.name ?: continue
            collected.add(name)
        }
    }

    override fun onDisable() {
        if (saveToFile && collected.isNotEmpty()) {
            runCatching {
                val dir = File(mc.mcDataDir, "FDPCLIENT")
                if (!dir.exists()) dir.mkdirs()
                File(dir, "collected_names.txt").writeText(collected.sorted().joinToString("\n"))
            }.onFailure { ClientUtils.LOGGER.error("[NameCollector] Failed to save names", it) }
        }
        ClientUtils.LOGGER.info("[NameCollector] Collected ${collected.size} unique names.")
        collected.clear()
    }
}
