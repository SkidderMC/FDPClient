package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.element.Channel
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent
import org.kitteh.irc.client.library.util.StsUtil

@ModuleInfo(name = "IRC", category = ModuleCategory.CLIENT, defaultOn = true)
object IRC : Module() {
    private var client: Client? = null
    private var channel: Channel? = null
    private var nick=mc.session.username.replace("_","")

    override fun onEnable() {
        client=Client.builder().nick(nick).server()
            .host("irc.undernet.org")
            .port(6669,Client.Builder.Server.SecurityType.INSECURE).then()
            .management().stsStorageManager(StsUtil.getDefaultStorageManager()).then()
            .buildAndConnect()

        client!!.eventManager.registerEventListener(object : Any(){
            @Handler
            fun onMessage(event: ChannelMessageEvent) {
                displayChat(event.actor.nick, event.message)
            }
            @Handler
            fun onJoin(event: ChannelJoinEvent) {
                channel=event.channel
            }
        })
        client!!.addChannel("#FDPCLIENT")
    }

    override fun onDisable() {
        client ?: return
        client!!.shutdown()
        client=null
        channel=null
    }

    private fun displayChat(nick: String, msg: String){
        ClientUtils.displayChatMessage("§b§lIRC §8> §7[§f$nick§7] §f$msg")
    }

    fun changeNick(nick_: String){
        client ?: return
        val nick=nick_.replace("_","")
        if(nick!=this.nick){
            client!!.nick=nick
            this.nick=nick
        }
    }

    fun sendMessage(msg: String){
        channel ?: return
        channel!!.sendMessage(msg)
        displayChat(nick, msg)
    }

    fun isUser(name: String):Boolean{
        channel ?: return false
        channel!!.nicknames.forEach {
            if(it.replace("_","").equals(name.replace("_",""),true))
                return true
        }
        return false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        changeNick(mc.getSession().username)
    }

    override val tag: String?
        get() = if(channel==null){ "Connecting" }else { null }
}