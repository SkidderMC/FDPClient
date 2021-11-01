/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.minecraft.util.IChatComponent
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.Display

object ClientUtils : MinecraftInstance() {
    private val logger = LogManager.getLogger("FDPClient")
    val osType: EnumOSType
    var inDevMode = System.getProperty("dev-mode") != null

    init {
        val os = System.getProperty("os.name").lowercase()
        osType = if (os.contains("win")) {
            EnumOSType.WINDOWS
        } else if (os.contains("mac")) {
            EnumOSType.MACOS
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            EnumOSType.LINUX
        } else {
            EnumOSType.UNKNOWN
        }
    }

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
        Display.setTitle(LiquidBounce.CLIENT_NAME + " " + LiquidBounce.CLIENT_VERSION + " | Mc " + LiquidBounce.MINECRAFT_VERSION)
    }

    fun displayAlert(message: String) {
        displayChatMessage("§8[" + LiquidBounce.COLORED_NAME + "§8] §f" + message)
    }

    fun displayChatMessage(message: String) {
        if (mc.thePlayer == null) {
            logger.info("(MCChat)$message")
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