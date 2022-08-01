/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient
import net.minecraft.util.IChatComponent
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.Display

object
ClientUtils : MinecraftInstance() {
    val logger = LogManager.getLogger("FDPClient")


    fun logInfo(msg: String) {
        logger.info(msg)
    }

    fun logWarn(msg: String) {
        logger.warn(msg)
    }

    fun logError(msg: String) {
        logger.error(msg)
    }

    fun logError(msg: String, t: Throwable) {
        logger.error(msg, t)
    }

    fun logDebug(msg: String) {
        logger.debug(msg)
    }

    fun setTitle() {
        Display.setTitle("${FDPClient.CLIENT_NAME} ${FDPClient.CLIENT_VERSION} (${FDPClient.CLIENT_BRANCH}) | ${FDPClient.CLIENT_WEBSITE}")
    }
    fun setTitle(stats:String) {
        Display.setTitle("${FDPClient.CLIENT_NAME} ${FDPClient.CLIENT_VERSION} (${FDPClient.CLIENT_BRANCH}) | ${FDPClient.CLIENT_WEBSITE} - " + stats)
    }

    fun displayAlert(message: String) {
        displayChatMessage(FDPClient.COLORED_NAME + message)
    }

    fun displayChatMessage(message: String) {
        if (mc.thePlayer == null) {
            logger.info("(MCChat) $message")
            return
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("text", message)
        mc.thePlayer.addChatMessage(IChatComponent.Serializer.jsonToComponent(jsonObject.toString()))
    }

    enum class EnumOSType(val friendlyName: String) {
        WINDOWS("win"), LINUX("linux"), MACOS("mac"), UNKNOWN("unk");
    }
}
