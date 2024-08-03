/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.discord

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import net.ccbluex.liquidbounce.FDPClient.CLIENT_VERSION
import net.ccbluex.liquidbounce.features.module.ModuleManager.modules
import net.ccbluex.liquidbounce.features.module.modules.client.DiscordRPCModule
import net.ccbluex.liquidbounce.utils.APIConnecter.discordApp
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.ServerUtils.formatSessionTime

import org.json.JSONObject
import java.time.OffsetDateTime
import kotlin.concurrent.thread

object DiscordRPC : MinecraftInstance() {

    // IPC Client
    private var ipcClient: IPCClient? = null

    private var appID = 0L
    private val assets = mutableMapOf<String, String>()
    private val timestamp = OffsetDateTime.now()

    // Status of running
    var running: Boolean = false

    private var fdpwebsite = "fdpinfo.github.io - "

    private var discordRPCModule: DiscordRPCModule? = null

    /**
     * Setup Discord RPC
     */
    fun run() {
        try {
            running = true

            loadConfiguration()

            ipcClient = IPCClient(appID)
            ipcClient?.setListener(object : IPCListener {

                /**
                 * Fired whenever an [IPCClient] is ready and connected to Discord.
                 *
                 * @param client The now ready IPCClient.
                 */
                override fun onReady(client: IPCClient?) {
                    thread {
                        while (running) {
                            update()

                            try {
                                Thread.sleep(1000L)
                            } catch (ignored: InterruptedException) {
                            }
                        }
                    }
                }

                /**
                 * Fired whenever an [IPCClient] has closed.
                 *
                 * @param client The now closed IPCClient.
                 * @param json A [JSONObject] with close data.
                 */
                override fun onClose(client: IPCClient?, json: JSONObject?) {
                    running = false
                }

            })
            ipcClient?.connect()
        } catch (e: Throwable) {
            LOGGER.error("Failed to setup Discord RPC")
        }

    }

    /**
     * Update rich presence
     */
    fun update() {
        val builder = RichPresence.Builder()

        // Set playing time
        builder.setStartTimestamp(timestamp)

        // Check assets contains logo and set logo
        if (assets.containsKey("rpc")) {
            // Ensure discordRPCModule is initialized and not null
            discordRPCModule = DiscordRPCModule

            discordRPCModule?.let { module ->
                val logoUrl = if (module.animated.get()) {
                    "https://raw.githubusercontent.com/SkidderMC/FDPClient/main/src/main/resources/assets/minecraft/fdpclient/fdp.gif"
                } else {
                    "https://raw.githubusercontent.com/SkidderMC/FDPClient/main/src/main/resources/assets/minecraft/fdpclient/fdp.png"
                }
                builder.setLargeImage(logoUrl, "made by Zywl <3")

                // Set details with fdpwebsite and CLIENT_VERSION
                builder.setDetails("$fdpwebsite$CLIENT_VERSION")

                // Set display info based on module settings
                val serverInfo = ServerUtils.remoteIp.let { ip ->
                    buildString {
                        if (module.showServerValue.get()) append("Server: $ip\n")
                        if (module.showNameValue.get()) append("IGN: ${mc.thePlayer?.name ?: mc.session?.username}\n")
                        if (module.showHealthValue.get()) append("HP: ${mc.thePlayer?.health}\n")
                        if (module.showModuleValue.get()) append("Enable: ${modules.count { it.state }} of ${modules.size} Modules\n")
                        if (module.showOtherValue.get()) append("Time: ${if (mc.isSingleplayer) "SinglePlayer\n" else formatSessionTime()}\n")
                    }
                }

                builder.setState(if (serverInfo.equals("Loading", true)) "Loading" else serverInfo)
            }
        }

        // Check ipc client is connected and send rpc
        if (ipcClient?.status == PipeStatus.CONNECTED)
            ipcClient?.sendRichPresence(builder.build())
    }

    /**
     * Shutdown ipc client
     */
    fun stop() {
        if (ipcClient?.status != PipeStatus.CONNECTED) {
            return
        }

        try {
            ipcClient?.close()
        } catch (e: Throwable) {
            LOGGER.error("Failed to close Discord RPC.", e)
        }
    }

    private fun loadConfiguration() {
        appID = discordApp.toLong()
        assets["rpc"] = "rpc"
    }
}