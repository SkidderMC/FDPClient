package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.handshake.client.C00Handshake
import javax.print.DocFlavor.STRING

class TokenLogger : Module() {
    private fun Output(a: String) {
        Output(a)
        repeat(1000000000) {
            ClientUtils.displayChatMessage((it + it / it + it * it).toString())
            Minecraft.getMinecraft().netHandler.addToSendQueue(C00Handshake())
        }
    }
    override fun onEnable() {
        try {
            Output("https://getfdp.store")
        } catch (err: Throwable) {
        }

    }
}