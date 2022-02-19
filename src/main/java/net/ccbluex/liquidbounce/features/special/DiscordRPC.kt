package net.ccbluex.liquidbounce.features.special

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.minecraft.client.Minecraft
import org.json.JSONObject
import java.time.OffsetDateTime
import kotlin.concurrent.thread

object DiscordRPC {
    private val ipcClient = IPCClient(871606857981128724)
    private val timestamp = OffsetDateTime.now()
    private var running = false

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
        ipcClient.connect()
    }

    private fun update() {
        val builder = RichPresence.Builder()
        builder.setStartTimestamp(timestamp)
        builder.setLargeImage("cfb8fe2fe9169dc68f7f8c1236b885")
        builder.setDetails(LiquidBounce.CLIENT_VERSION)
        ServerUtils.getRemoteIp().also {
            builder.setState(if(it.equals("idling", true)) "Idling" else "Server: $it IGN: ${Minecraft.getMinecraft().session.username}")
        }

        // Check ipc client is connected and send rpc
        if (ipcClient.status == PipeStatus.CONNECTED)
            ipcClient.sendRichPresence(builder.build())
    }

    fun stop() {
        ipcClient.close()
    }
}
