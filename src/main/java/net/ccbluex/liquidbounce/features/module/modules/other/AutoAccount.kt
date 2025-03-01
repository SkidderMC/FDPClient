/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import me.liuli.elixir.account.CrackedAccount
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.event.EventManager.call
import net.ccbluex.liquidbounce.event.async.launchSequence
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager.accountsConfig
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.ServerUtils
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.randomAccount
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.util.ChatComponentText
import net.minecraft.util.Session

object AutoAccount :
    Module("AutoAccount", Category.CLIENT, subjective = true, gameDetecting = false) {

    private val register by boolean("AutoRegister", true)
    private val login by boolean("AutoLogin", true)

    private const val DEFAULT_PASSWORD = "zywl1337#"

    // Gamster requires 8 chars+
    private val passwordValue = text("Password", DEFAULT_PASSWORD) {
        register || login
    }.onChange { old, new ->
        when {
            new.any { it.isWhitespace() } -> {
                chat("§7[§a§lAutoAccount§7] §cPassword cannot contain a space!")
                old
            }

            new.lowercase() == "reset" -> {
                chat("§7[§a§lAutoAccount§7] §3Password reset to its default value.")
                DEFAULT_PASSWORD
            }

            new.length < 4 -> {
                chat("§7[§a§lAutoAccount§7] §cPassword must be longer than 4 characters!")
                old
            }

            else -> new
        }
    }.subjective()

    private val password by passwordValue

    // Needed for Gamster
    private val sendDelay by intRange("SendDelay", 150..300, 0..500) { passwordValue.isSupported() }

    private val autoSession by boolean("AutoSession", false)
    private val startupValue = boolean("RandomAccountOnStart", false) { autoSession }
    private val relogInvalidValue = boolean("RelogWhenPasswordInvalid", true) { autoSession }
    private val relogKickedValue = boolean("RelogWhenKicked", false) { autoSession }

    private val reconnectDelayValue = int("ReconnectDelay", 1000, 0..2500)
    { relogInvalidValue.isActive() || relogKickedValue.isActive() }
    private val reconnectDelay by reconnectDelayValue

    private val accountModeValue = choices("AccountMode", arrayOf("RandomName", "RandomAlt"), "RandomName") {
        reconnectDelayValue.isSupported() || startupValue.isActive()
    }.onChange { old, new ->
        if (new == "RandomAlt" && accountsConfig.accounts.filterIsInstance<CrackedAccount>().size <= 1) {
            chat("§7[§a§lAutoAccount§7] §cAdd more cracked accounts in AltManager to use RandomAlt option!")
            old
        } else {
            new
        }
    }
    private val accountMode by accountModeValue

    private val saveValue = boolean("SaveToAlts", false) {
        accountModeValue.isSupported() && accountMode != "RandomAlt"
    }

    private var status = Status.WAITING

    private fun relog(info: String = "") {
        // Disconnect from server
        if (mc.currentServerData != null && mc.theWorld != null)
            mc.netHandler.networkManager.closeChannel(
                ChatComponentText("$info\n\nReconnecting with a random account in ${reconnectDelay}ms")
            )

        // Log in to account with a random name, optionally save it
        changeAccount()

        launchSequence(Dispatchers.Main) {
            delay(sendDelay.random().toLong())
            // connectToLastServer needs thread with OpenGL context
            ServerUtils.connectToLastServer()
        }
    }

    private fun respond(msg: String) = when {
        register && "/reg" in msg -> {
            addNotification(Notification("Trying to register.", "Trying to Register", Type.INFO))
            launchSequence(Dispatchers.IO) {
                delay(sendDelay.random().toLong())
                mc.thePlayer.sendChatMessage("/register $password $password")
            }
            true
        }

        login && "/log" in msg -> {
            addNotification(Notification("Trying to log in.", "Trying to log in.", Type.INFO))
            launchSequence(Dispatchers.IO) {
                delay(sendDelay.random().toLong())
                mc.thePlayer.sendChatMessage("/login $password")
            }
            true
        }

        else -> false
    }

    val onPacket = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            is S02PacketChat, is S45PacketTitle -> {
                // Don't respond to register / login prompts when failed once
                if (!passwordValue.isSupported() || status == Status.STOPPED) return@handler

                val msg = when (packet) {
                    is S02PacketChat -> packet.chatComponent?.unformattedText?.lowercase()
                    is S45PacketTitle -> packet.message?.unformattedText?.lowercase()
                    else -> return@handler
                } ?: return@handler

                if (status == Status.WAITING) {
                    // Try to register / log in, return if invalid message
                    if (!respond(msg))
                        return@handler

                    event.cancelEvent()
                    status = Status.SENT_COMMAND
                } else {
                    // Check response from server
                    when {
                        // Logged in
                        "success" in msg || "logged" in msg || "registered" in msg -> {
                            success()
                            event.cancelEvent()
                        }
                        // Login failed, possibly relog
                        "incorrect" in msg || "wrong" in msg || "spatne" in msg -> fail()
                        "unknown" in msg || "command" in msg || "allow" in msg || "already" in msg -> {
                            // Tried executing /login or /register from lobby, stop trying
                            status = Status.STOPPED
                            event.cancelEvent()
                        }
                    }
                }
            }

            is S40PacketDisconnect -> {
                if (relogKickedValue.isActive() && status != Status.SENT_COMMAND) {
                    val reason = packet.reason.unformattedText
                    if ("ban" in reason) return@handler

                    relog(packet.reason.unformattedText)
                }
            }
        }

    }

    val onWorld = handler<WorldEvent> { event ->
        if (!passwordValue.isSupported()) return@handler

        // Reset status if player wasn't in a world before
        if (mc.theWorld == null) {
            status = Status.WAITING
            return@handler
        }

        if (status == Status.SENT_COMMAND) {
            // Server redirected the player to a lobby, success
            if (event.worldClient != null && mc.theWorld != event.worldClient) success()
            // Login failed, possibly relog
            else fail()
        }
    }

    val onStartup = handler<StartupEvent> {
        // Log in to account with a random name after startup, optionally save it
        if (startupValue.isActive()) changeAccount()
    }

    // Login succeeded
    private fun success() {
        if (status == Status.SENT_COMMAND) {
            addNotification(Notification("Logged in as ${mc.session.username}", "Logged", Type.SUCCESS))

            // Stop waiting for response
            status = Status.STOPPED
        }
    }

    // Login failed
    private fun fail() {
        if (status == Status.SENT_COMMAND) {
            addNotification(Notification("Failed to log in as ${mc.session.username}", "ERROR", Type.ERROR))

            // Stop waiting for response
            status = Status.STOPPED

            // Trigger relog task
            if (relogInvalidValue.isActive()) relog()
        }
    }

    private fun changeAccount() {
        if (accountMode == "RandomAlt") {
            val account = accountsConfig.accounts.filter { it is CrackedAccount && it.name != mc.session.username }
                .randomOrNull() ?: return
            mc.session = Session(
                account.session.username, account.session.uuid,
                account.session.token, account.session.type
            )
            call(SessionUpdateEvent)
            return
        }

        // Log in to account with a random name
        val account = randomAccount()

        // Save as a new account if SaveToAlts is enabled
        if (saveValue.isActive() && !accountsConfig.accountExists(account)) {
            accountsConfig.addAccount(account)
            accountsConfig.saveConfig()

            addNotification(Notification("Saved alt ${account.name}", "Sucess", Type.SUCCESS))
        }
    }

    private enum class Status {
        WAITING, SENT_COMMAND, STOPPED
    }
}