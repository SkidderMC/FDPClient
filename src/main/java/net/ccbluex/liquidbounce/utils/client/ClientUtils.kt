/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.NetworkManager
import net.minecraft.network.login.client.C01PacketEncryptionResponse
import net.minecraft.network.login.server.S01PacketEncryptionRequest
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Field
import java.security.PublicKey
import javax.crypto.SecretKey

@SideOnly(Side.CLIENT)
object ClientUtils : MinecraftInstance {
    private var fastRenderField: Field? = runCatching {
        GameSettings::class.java.getDeclaredField("ofFastRender")
    }.getOrNull()

    var runTimeTicks = 0

    var profilerName = ""

    val LOGGER: Logger = LogManager.getLogger("FDPCLIENT")

    fun disableFastRender() {
        fastRenderField?.let {
            if (!it.isAccessible)
                it.isAccessible = true

            it.setBoolean(mc.gameSettings, false)
        }
    }

    fun sendEncryption(
        networkManager: NetworkManager,
        secretKey: SecretKey?,
        publicKey: PublicKey?,
        encryptionRequest: S01PacketEncryptionRequest
    ) {
        networkManager.sendPacket(C01PacketEncryptionResponse(secretKey, publicKey, encryptionRequest.verifyToken),
            { networkManager.enableEncryption(secretKey) }
        )
    }

    fun displayAlert(message: String) {
        displayChatMessage("§7[§b§l$CLIENT_NAME§7 §f»]§r $message")
    }

    fun displayChatMessage(message: String) {
        mc.thePlayer?.addChatMessage(ChatComponentText("§7[§b§l$CLIENT_NAME§7] §f§l» §r $message"))
            ?: LOGGER.info("(MCChat) $message")
    }
}

fun chat(message: String) = ClientUtils.displayChatMessage(message)