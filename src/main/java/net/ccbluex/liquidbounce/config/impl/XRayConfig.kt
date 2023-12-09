/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config.impl

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.modules.visual.XRay
import net.ccbluex.liquidbounce.config.FileConfig
import net.ccbluex.liquidbounce.config.FileManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.block.Block
import java.io.File

class XRayConfig(file: File) : FileConfig(file) {

    override fun loadConfig(config: String) {
        val xRay = FDPClient.moduleManager[XRay::class.java]!!
        val jsonArray = JsonParser().parse(config).asJsonArray
        xRay.xrayBlocks.clear()

        for (jsonElement in jsonArray) {
            try {
                val block = Block.getBlockFromName(jsonElement.asString)
                if (xRay.xrayBlocks.contains(block)) {
                    ClientUtils.logError("[FileManager] Skipped xray block '" + block.registryName + "' because the block is already added.")
                    continue
                }
                xRay.xrayBlocks.add(block)
            } catch (throwable: Throwable) {
                ClientUtils.logError("[FileManager] Failed to add block to xray.", throwable)
            }
        }
    }

    override fun saveConfig(): String {
        val xRay = FDPClient.moduleManager[XRay::class.java]!!
        val jsonArray = JsonArray()

        for (block in xRay.xrayBlocks)
            jsonArray.add(FileManager.PRETTY_GSON.toJsonTree(Block.getIdFromBlock(block)))

        return FileManager.PRETTY_GSON.toJson(jsonArray)
    }
}