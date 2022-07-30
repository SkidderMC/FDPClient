/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.file.configs

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.features.module.modules.render.XRay
import net.skiddermc.fdpclient.file.FileConfig
import net.skiddermc.fdpclient.file.FileManager
import net.skiddermc.fdpclient.utils.ClientUtils
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