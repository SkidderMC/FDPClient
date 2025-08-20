package net.ccbluex.liquidbounce.features.module.modules.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioSocketChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import moe.lasoleil.axochat4j.client.buildAxochatClientBootstrap
import moe.lasoleil.axochat4j.codec.gson
import net.ccbluex.liquidbounce.event.SessionUpdateEvent
import net.ccbluex.liquidbounce.event.async.loopSequence
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.login.UserUtils
import net.minecraft.event.ClickEvent
import net.minecraft.network.NetworkManager
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import java.net.URI
import java.net.URISyntaxException
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds

object IRCModule : Module("IRC", Category.CLIENT, subjective = true, gameDetecting = false) {

    fun reloadIfEnabled() {
        if (state) {
            state = false
            state = true
        }
    }

    var jwt by boolean("JWT", false).onChanged {
        reloadIfEnabled()
    }

    @Volatile
    var jwtToken = ""

    val chatClient = Bootstrap()
        .group(NetworkManager.CLIENT_NIO_EVENTLOOP.value)
        .channelFactory(::NioSocketChannel)
        .buildAxochatClientBootstrap()
        .gson()
        .uri(URI("wss://chat.liquidbounce.net:7886/ws"))
        .onConnected { client ->
            chat("§7[§a§lChat§7] §9Connected to chat server!")
            when {
                jwt && !jwtToken.isBlank() -> {
                    client.loginJWT(jwtToken, true)
                    chat("§7[§a§lChat§7] §9Logging in...")
                }
                UserUtils.isValidTokenOffline(mc.session.token) -> {
                    client.requestMojangInfo()
                    chat("§7[§a§lChat§7] §9Logging in...")
                }
            }
        }
        .onDisconnected {
            chat("§7[§a§lChat§7] §cDisconnected from chat server!")
        }
        .onError { _, cause ->
            chat("§7[§a§lChat§7] §c§lError: §7${cause.javaClass.name}: ${cause.message}")
        }
        .onServerMojangInfo { client, sessionHash ->
            mc.sessionService.joinServer(mc.session.profile, mc.session.token, sessionHash)
            jwt = false

            client.loginMojang(mc.session.username, mc.session.profile.id, true)
        }
        .onServerNewJWT { _, token ->
            chat("§7[§a§lChat§7] §9New JWT token received! Reconnecting with it...")
            jwtToken = token
            jwt = true

            reloadIfEnabled()
        }
        .onServerMessage { _, authorInfo, content ->
            val thePlayer = mc.thePlayer

            if (thePlayer == null) {
                ClientUtils.LOGGER.info("[IRC] ${authorInfo.name}: $content")
                return@onServerMessage
            }

            val chatComponent = ChatComponentText("§7[§a§lChat§7] §9${authorInfo.name}: ")
            val messageComponent = content.toChatComponent()
            chatComponent.appendSibling(messageComponent)

            thePlayer.addChatMessage(chatComponent)
        }
        .onServerPrivateMessage { _, authorInfo, content ->
            chat("§7[§a§lChat§7] §c(P)§9 ${authorInfo.name}: §7$content")
        }
        .onServerSuccess { _, reason ->
            when (reason) {
                "Login" -> {
                    chat("§7[§a§lChat§7] §9Logged in!")

                    chat("====================================")
                    chat("§c>> §lIRC")
                    chat("§7Write message: §a.chat <message>")
                    chat("§7Write private message: §a.pchat <user> <message>")
                    chat("====================================")
                }

                "Ban" -> chat("§7[§a§lChat§7] §9Successfully banned user!")
                "Unban" -> chat("§7[§a§lChat§7] §9Successfully unbanned user!")
            }
        }
        .onServerError { _, message ->
            val message = when (message) {
                "NotSupported" -> "This method is not supported!"
                "LoginFailed" -> "Login Failed!"
                "NotLoggedIn" -> "You must be logged in to use the chat! Enable IRC."
                "AlreadyLoggedIn" -> "You are already logged in!"
                "MojangRequestMissing" -> "Mojang request missing!"
                "NotPermitted" -> "You are missing the required permissions!"
                "NotBanned" -> "You are not banned!"
                "Banned" -> "You are banned!"
                "RateLimited" -> "You have been rate limited. Please try again later."
                "PrivateMessageNotAccepted" -> "Private message not accepted!"
                "EmptyMessage" -> "You are trying to send an empty message!"
                "MessageTooLong" -> "Message is too long!"
                "InvalidCharacter" -> "Message contains a non-ASCII character!"
                "InvalidId" -> "The given ID is invalid!"
                "Internal" -> "An internal server error occurred!"
                else -> message
            }

            chat("§7[§a§lChat§7] §cError: §7$message")
        }
        .build()

    override fun onDisable() {
        chatClient.disconnect()
    }

    val onSession = handler<SessionUpdateEvent>(dispatcher = Dispatchers.IO) {
        chatClient.disconnect()
        delay(5.seconds)
        chatClient.connect()
    }

    val onUpdate = loopSequence(dispatcher = Dispatchers.IO) {
        if (!chatClient.connect()) return@loopSequence
        delay(15.seconds)
    }

    /**
     * Forge Hooks
     *
     * @author Forge
     */

    private val urlPattern = Pattern.compile(
        "((?:[a-z0-9]{2,}:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_\\.]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
        Pattern.CASE_INSENSITIVE
    )

    private fun String.toChatComponent(): IChatComponent {
        var component: IChatComponent? = null
        val matcher = urlPattern.matcher(this)
        var lastEnd = 0

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            // Append the previous leftovers.
            val part = this.substring(lastEnd, start)
            if (part.isNotEmpty()) {
                if (component == null) {
                    component = ChatComponentText(part)
                    component.chatStyle.color = EnumChatFormatting.GRAY
                } else
                    component.appendText(part)
            }

            lastEnd = end

            val url = this.substring(start, end)

            try {
                if (URI(url).scheme != null) {
                    // Set the click event and append the link.
                    val link: IChatComponent = ChatComponentText(url)

                    link.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url)
                    link.chatStyle.underlined = true
                    link.chatStyle.color = EnumChatFormatting.GRAY

                    if (component == null)
                        component = link
                    else
                        component.appendSibling(link)
                    continue
                }
            } catch (_: URISyntaxException) {
            }

            if (component == null) {
                component = ChatComponentText(url)
                component.chatStyle.color = EnumChatFormatting.GRAY
            } else
                component.appendText(url)
        }

        // Append the rest of the message.
        val end = this.substring(lastEnd)

        if (component == null) {
            component = ChatComponentText(end)
            component.chatStyle.color = EnumChatFormatting.GRAY
        } else if (end.isNotEmpty())
            component.appendText(this.substring(lastEnd))

        return component
    }

}