/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.lasoleil.axochat4j.packet.AxochatPacket
import moe.lasoleil.axochat4j.packet.s2c.S2CErrorPacket
import moe.lasoleil.axochat4j.packet.s2c.S2CMessagePacket
import moe.lasoleil.axochat4j.packet.s2c.S2CNewJWTPacket
import moe.lasoleil.axochat4j.packet.s2c.S2CPrivateMessagePacket
import moe.lasoleil.axochat4j.packet.s2c.S2CSuccessPacket
import net.ccbluex.liquidbounce.handler.irc.Client
import net.ccbluex.liquidbounce.event.SessionUpdateEvent
import net.ccbluex.liquidbounce.event.async.loopSequence
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.login.UserUtils
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import java.net.URI
import java.net.URISyntaxException
import java.util.regex.Pattern

object IRCModule : Module("IRC", Category.CLIENT, subjective = true, gameDetecting = false) {

    var jwt by boolean("JWT", false).onChanged {
        if (state) {
            state = false
            state = true
        }
    }

    var jwtToken = ""

    val client = object : Client() {

        /**
         * Handle connect to web socket
         */
        override fun onConnect() = chat("§7[§a§lChat§7] §9Connecting to chat server...")

        /**
         * Handle connect to web socket
         */
        override fun onConnected() = chat("§7[§a§lChat§7] §9Connected to chat server!")

        /**
         * Handle handshake
         */
        override fun onHandshake(success: Boolean) {}

        /**
         * Handle disconnect
         */
        override fun onDisconnect() = chat("§7[§a§lChat§7] §cDisconnected from chat server!")

        /**
         * Handle logon to web socket with minecraft account
         */
        override fun onLogon() = chat("§7[§a§lChat§7] §9Logging in...")

        /**
         * Handle incoming packets
         */
        override fun onPacket(packet: AxochatPacket) {
            when (packet) {
                is S2CMessagePacket -> {
                    val thePlayer = mc.thePlayer

                    if (thePlayer == null) {
                        LOGGER.info("[IRC] ${packet.authorInfo.name}: ${packet.content}")
                        return
                    }

                    val chatComponent = ChatComponentText("§7[§a§lChat§7] §9${packet.authorInfo.name}: ")
                    val messageComponent = toChatComponent(packet.content)
                    chatComponent.appendSibling(messageComponent)

                    thePlayer.addChatMessage(chatComponent)
                }

                is S2CPrivateMessagePacket -> chat("§7[§a§lChat§7] §c(P)§9 ${packet.authorInfo.name}: §7${packet.content}")
                is S2CErrorPacket -> {
                    val message = when (packet.message) {
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
                        else -> packet.message
                    }

                    chat("§7[§a§lChat§7] §cError: §7$message")
                }

                is S2CSuccessPacket -> {
                    when (packet.reason) {
                        "Login" -> {
                            chat("§7[§a§lChat§7] §9Logged in!")

                            chat("====================================")
                            chat("§c>> §lIRC")
                            chat("§7Write message: §a.chat <message>")
                            chat("§7Write private message: §a.pchat <user> <message>")
                            chat("====================================")

                            loggedIn = true
                        }

                        "Ban" -> chat("§7[§a§lChat§7] §9Successfully banned user!")
                        "Unban" -> chat("§7[§a§lChat§7] §9Successfully unbanned user!")
                    }
                }

                is S2CNewJWTPacket -> {
                    jwtToken = packet.token
                    jwt = true

                    state = false
                    state = true
                }
            }
        }

        /**
         * Handle error
         */
        override fun onError(cause: Throwable) =
            chat("§7[§a§lChat§7] §c§lError: §7${cause.javaClass.name}: ${cause.message}")
    }

    private var loggedIn = false

    override fun onDisable() {
        loggedIn = false
        client.disconnect()
    }

    private val loginMutex = Mutex()

    val onSession = handler<SessionUpdateEvent>(dispatcher = Dispatchers.IO) {
        client.disconnect()
        connect()
    }

    val onUpdate = loopSequence(dispatcher = Dispatchers.IO) {
        if (client.isConnected()) return@loopSequence

        connect()

        delay(5000L)
    }

    private suspend fun connect() {
        if (client.isConnected()) return

        if (jwt && jwtToken.isEmpty()) {
            chat("§7[§a§lChat§7] §cError: §7No token provided!")
            state = false
            return
        }

        loggedIn = false

        try {
            loginMutex.withLock {
                if (client.isConnected())
                    return@withLock

                client.connect()

                when {
                    jwt -> client.loginJWT(jwtToken)
                    UserUtils.isValidTokenOffline(mc.session.token) -> client.loginMojang()
                }
            }
        } catch (cause: Exception) {
            LOGGER.error("IRC error", cause)
            chat("§7[§a§lChat§7] §cError: §7${cause.javaClass.name}: ${cause.message}")
        }
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

    private fun toChatComponent(string: String): IChatComponent {
        var component: IChatComponent? = null
        val matcher = urlPattern.matcher(string)
        var lastEnd = 0

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            // Append the previous leftovers.
            val part = string.substring(lastEnd, start)
            if (part.isNotEmpty()) {
                if (component == null) {
                    component = ChatComponentText(part)
                    component.chatStyle.color = EnumChatFormatting.GRAY
                } else
                    component.appendText(part)
            }

            lastEnd = end

            val url = string.substring(start, end)

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
        val end = string.substring(lastEnd)

        if (component == null) {
            component = ChatComponentText(end)
            component.chatStyle.color = EnumChatFormatting.GRAY
        } else if (end.isNotEmpty())
            component.appendText(string.substring(lastEnd))

        return component
    }

}