package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.IntegerValue
import net.ccbluex.liquidbounce.features.TextValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle
import java.util.*

@ModuleInfo(name = "AutoLogin", description = "Automatic login into server.", category = ModuleCategory.MISC)
class AutoLogin : Module() {
    private val passwordValue=TextValue("Password","password")
    private val delayValue=IntegerValue("Delay",1500,100,5000)

    private var logined=false

    override fun onEnable() {
        logined=false
    }

    @EventTarget
    fun onWorld(event: WorldEvent){
        logined=false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if(logined) return
        val password=passwordValue.get()
        val packet=event.packet

        if(packet is S45PacketTitle && packet.type==S45PacketTitle.Type.SUBTITLE){
            val msg=packet.message.unformattedText
            // redesky
            if (msg.contains("/register", ignoreCase = true)) {
                delayedMessage("/register $password $password")
            } else if (msg.contains("/login", ignoreCase = true)) {
                delayedMessage("/login $password")
            }
        }

        if(packet is S02PacketChat){
            val msg=packet.chatComponent.unformattedText
            // mush and some other server
            if (msg.contains("/register", ignoreCase = true)) {
                delayedMessage("/register $password $password")
            } else if (msg.contains("/login", ignoreCase = true)) {
                delayedMessage("/login $password")
            }
            // mcyc
            if (msg.contains(".reg", ignoreCase = true)) {
                delayedMessage(".reg $password $password")
            } else if (msg.contains(".l", ignoreCase = true)) {
                delayedMessage(".l $password")
            }
        }
    }

    private fun delayedMessage(message: String){
        logined=true
        Timer().schedule(object : TimerTask() {
            override fun run() {
                mc.thePlayer.sendChatMessage(message)
            }
        },delayValue.get().toLong())
    }
}