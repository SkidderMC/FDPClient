package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.BoolValue
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
    private val registerCommand=TextValue("Register","/register %p %p")
    private val loginCommand=TextValue("Login","/login %p")
    private val passwordValue=TextValue("Password","password")
    private val delayValue=IntegerValue("Delay",1500,100,5000)
    private val title=BoolValue("Title",true)
    private val chat=BoolValue("Chat",true)

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
        val packet=event.packet

        if(title.get()&&packet is S45PacketTitle){
            processMessage(packet.message.unformattedText)
        }

        if(chat.get()&&packet is S02PacketChat){
            processMessage(packet.chatComponent.unformattedText)
        }
    }

    private fun processMessage(msg: String){
        val regCommand=registerCommand.get().split(" ")[0]
        if(regCommand.isNotEmpty()&&msg.contains(regCommand,ignoreCase = true)){
            delayedMessage(registerCommand.get().replace("%p",passwordValue.get()))
        }
        val logCommand=loginCommand.get().split(" ")[0]
        if(logCommand.isNotEmpty()&&msg.contains(logCommand,ignoreCase = true)){
            delayedMessage(loginCommand.get().replace("%p",passwordValue.get()))
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