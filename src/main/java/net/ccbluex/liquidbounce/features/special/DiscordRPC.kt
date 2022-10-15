package net.ccbluex.liquidbounce.features.special

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.client.DiscordRPCModule
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.mc
import org.json.JSONObject
import java.time.OffsetDateTime
import kotlin.concurrent.thread

object DiscordRPC {
    private val ipcClient = IPCClient(1021236965108109333)
    private val timestamp = OffsetDateTime.now()
    private var running = false
    private var fdpwebsite = "fdpinfo.github.io - "


    fun run() {
        ipcClient.setListener(object : IPCListener {
            override fun onReady(client: IPCClient?) {
                running = true
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

            override fun onClose(client: IPCClient?, json: JSONObject?) {
                running = false
            }
        })
        try {
            ipcClient.connect()
        } catch (e: Exception) {
            println("discord rpc failed to start")
        } catch (e: RuntimeException) {
            println("discord rpc failed to start")
        }
    }

    private fun update() {
        val builder = RichPresence.Builder()
        val discordRPCModule = LiquidBounce.moduleManager[DiscordRPCModule::class.java]!!
        builder.setStartTimestamp(timestamp)
        builder.setLargeImage(if (discordRPCModule.animated.get()){"https://skiddermc.github.io/repo/skiddermc/FDPclient/dcrpc/fdp.gif"} else {"https://skiddermc.github.io/repo/skiddermc/FDPclient/dcrpc/fdp.png"})
        builder.setDetails(fdpwebsite + LiquidBounce.CLIENT_VERSION)
        ServerUtils.getRemoteIp().also {
            builder.setState(if(it.equals("idling", true)) "Idling" else "" + if(discordRPCModule.drpcValue.get() == "ShowServer"){"Server: $it"} else if(discordRPCModule.drpcValue.get() == "ShowName"){ "Username: ${if(mc.thePlayer != null) mc.thePlayer.name else mc.session.username}" } else if(discordRPCModule.drpcValue.get().equals("ShowHealth")){ "health: " + mc.thePlayer.health } else { " enjoying the breeze <3" })
        }

        // Check ipc client is connected and send rpc
        if (ipcClient.status == PipeStatus.CONNECTED) ipcClient.sendRichPresence(builder.build())
    }

    fun stop() {
        ipcClient.close()
    }
}
