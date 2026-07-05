/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import me.liuli.elixir.account.CrackedAccount
import me.liuli.elixir.account.MicrosoftAccount
import me.liuli.elixir.account.MinecraftAccount
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.SessionUpdateEvent
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.gui.GuiMainMenu
import net.ccbluex.liquidbounce.ui.client.gui.multiplayer.GuiServerSelect
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.ServerUtils
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.randomAccount
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.minecraft.client.gui.GuiCreateWorld
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiRenameWorld
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.ServerList
import net.minecraft.util.Session
import net.minecraft.world.storage.SaveFormatComparator
import java.io.File
import java.util.Base64
import java.util.Locale

/** Implements menu-facing REST operations for the web theme using the 1.8.9 client APIs. */
@Suppress("TooManyFunctions") // REST boundary: one small operation per public endpoint keeps routing explicit.
object NextGenMenuBridge : MinecraftInstance {

    private val parser = JsonParser()
    private val initialSession: Session by lazy { mc.session }

    fun openScreen(body: String) {
        val name = body.string("name")?.lowercase(Locale.ROOT) ?: return
        when (name) {
            "title", "multiplayer", "singleplayer", "altmanager", "proxymanager", "clickgui", "inventory", "browser" ->
                NextGenVirtualScreenRouter.openRoute(name)
            "multiplayer_realms" -> NextGenVirtualScreenRouter.openRoute("multiplayer")
            "options" -> onClientThread { displayNative(GuiOptions(GuiMainMenu(), mc.gameSettings)) }
            "create_world" -> onClientThread { displayNative(GuiCreateWorld(GuiSelectWorld(GuiMainMenu()))) }
            else -> Unit
        }
    }

    fun closeScreen() = NextGenVirtualScreenRouter.closeCurrent()

    fun browse(body: String) {
        val target = body.string("target") ?: return
        val url = when (target.uppercase(Locale.ROOT)) {
            "MAINTAINER_GITHUB" -> FDPClient.CLIENT_GITHUB
            "MAINTAINER_DISCORD" -> "https://discord.com/invite/3XRFGeqEYD"
            "CLIENT_WEBSITE" -> "https://fdpinfo.github.io/"
            "MAINTAINER_FORUM" -> "https://github.com/SkidderMC/FDPClient/discussions"
            "MAINTAINER_TWITTER" -> "https://lucas-lima.xyz"
            "MAINTAINER_YOUTUBE" -> "https://www.youtube.com/@opZywl"
            else -> return
        }
        MiscUtils.showURL(url)
    }

    fun exit() = onClientThread { mc.shutdown() }

    fun update(): JsonObject = JsonObject().apply {
        addProperty("development", true)
        addProperty("commit", System.getProperty("fdp.git.commit", "unknown"))
        add("update", JsonNull.INSTANCE)
    }

    fun reconnect() = onClientThread { ServerUtils.connectToLastServer() }

    fun servers(): JsonArray = synchronized(this) {
        val list = ServerList(mc).apply { loadServerList() }
        JsonArray().apply {
            repeat(list.countServers()) { index -> add(serverJson(index, list.getServerData(index))) }
        }
    }

    fun connectServer(body: String) {
        val address = body.string("address")?.trim()?.takeIf(String::isNotEmpty) ?: return
        val data = ServerData(address, address, false)
        ServerUtils.serverData = data
        onClientThread { mc.displayGuiScreen(GuiConnecting(GuiServerSelect(GuiMainMenu()), mc, data)) }
    }

    fun addServer(body: String) = editServerList { list ->
        val name = body.string("name")?.trim().orEmpty()
        val address = body.string("address")?.trim().orEmpty()
        if (name.isEmpty() || address.isEmpty()) return@editServerList
        val data = ServerData(name, address, false)
        setResourceMode(data, body.string("serverResourcePacks"))
        list.addServerData(data)
    }

    fun editServer(body: String) = editServerList { list ->
        val id = body.int("id") ?: return@editServerList
        if (id !in 0 until list.countServers()) return@editServerList
        val data = list.getServerData(id)
        data.serverName = body.string("name")?.trim().orEmpty().ifEmpty { data.serverName }
        data.serverIP = body.string("address")?.trim().orEmpty().ifEmpty { data.serverIP }
        setResourceMode(data, body.string("resourcePackPolicy"))
    }

    fun removeServer(body: String) = editServerList { list ->
        val id = body.int("id") ?: return@editServerList
        if (id in 0 until list.countServers()) list.removeServerData(id)
    }

    fun orderServers(body: String) = editServerList { list ->
        val order = body.array("order")?.mapNotNull { runCatching { it.asInt }.getOrNull() } ?: return@editServerList
        val original = (0 until list.countServers()).map(list::getServerData)
        if (order.size != original.size || order.toSet() != original.indices.toSet()) return@editServerList
        val current = original.toMutableList()
        order.map(original::get).forEachIndexed { target, wanted ->
            val source = current.indexOf(wanted)
            if (source != target) {
                list.swapServers(target, source)
                val displaced = current[target]
                current[target] = current[source]
                current[source] = displaced
            }
        }
    }

    fun protocols(): JsonArray = JsonArray().apply { add(protocol()) }
    fun protocol(): JsonObject = JsonObject().apply {
        addProperty("name", "1.8.9")
        addProperty("version", 47)
    }

    fun accounts(): JsonArray = JsonArray().apply {
        FileManager.accountsConfig.accounts.forEachIndexed { index, account ->
            val uuid = runCatching { account.session.uuid }.getOrDefault("")
            add(JsonObject().apply {
                addProperty("avatar", if (uuid.isBlank()) "" else "https://crafatar.com/avatars/$uuid?overlay")
                addProperty("favorite", false)
                addProperty("id", index)
                addProperty("type", accountType(account))
                addProperty("username", account.name)
                addProperty("uuid", uuid)
            })
        }
    }

    fun addCrackedAccount(body: String) {
        val username = body.string("username")?.trim()?.takeIf { it.matches("[A-Za-z0-9_]{1,16}".toRegex()) } ?: return
        FileManager.accountsConfig.addCrackedAccount(username)
        FileManager.saveConfig(FileManager.accountsConfig)
        UiEventSocket.publish("accountManagerAddition", JsonObject().apply {
            addProperty("username", username)
            add("error", JsonNull.INSTANCE)
        })
    }

    fun removeAccount(body: String) {
        val id = body.int("id") ?: return
        val removed = FileManager.accountsConfig.accounts.getOrNull(id) ?: return
        FileManager.accountsConfig.removeAccount(removed)
        FileManager.saveConfig(FileManager.accountsConfig)
        UiEventSocket.publish("accountManagerRemoval", JsonObject().apply { addProperty("username", removed.name) })
    }

    fun orderAccounts(body: String) {
        val accounts = FileManager.accountsConfig.accounts
        val order = body.array("order")?.mapNotNull { runCatching { it.asInt }.getOrNull() } ?: return
        if (order.size != accounts.size || order.toSet() != accounts.indices.toSet()) return
        val original = accounts.toList()
        accounts.clear()
        order.mapTo(accounts) { original[it] }
        FileManager.saveConfig(FileManager.accountsConfig)
    }

    fun loginAccount(body: String) {
        val account = FileManager.accountsConfig.accounts.getOrNull(body.int("id") ?: return) ?: return
        login(account)
    }

    fun directCrackedLogin(body: String) {
        val username = body.string("username")?.trim()?.takeIf { it.matches("[A-Za-z0-9_]{1,16}".toRegex()) } ?: return
        onClientThread {
            mc.session = Session(username, "", "", "legacy")
            EventManager.call(SessionUpdateEvent)
            publishLogin(username)
        }
    }

    fun restoreSession() = onClientThread {
        mc.session = initialSession
        EventManager.call(SessionUpdateEvent)
        publishLogin(initialSession.username)
    }

    fun randomName(): JsonObject = JsonObject().apply { addProperty("name", randomAccount().name) }

    fun worlds(): JsonArray = JsonArray().apply {
        val saves = runCatching { mc.saveLoader.saveList }.getOrDefault(emptyList<SaveFormatComparator>())
        saves.forEachIndexed { index, save ->
            val worldInfo = runCatching { mc.saveLoader.getWorldInfo(save.fileName) }.getOrNull()
            val icon = File(mc.mcDataDir, "saves/${save.fileName}/icon.png").takeIf(File::isFile)
                ?.let { Base64.getEncoder().encodeToString(it.readBytes()) }
            add(JsonObject().apply {
                addProperty("id", index)
                addProperty("name", save.fileName)
                addProperty("displayName", save.displayName)
                addProperty("lastPlayed", save.lastTimePlayed)
                addProperty("gameMode", save.enumGameType.name.lowercase(Locale.ROOT))
                addProperty("difficulty", worldInfo?.difficulty?.difficultyResourceKey ?: "normal")
                if (icon == null) add("icon", JsonNull.INSTANCE) else addProperty("icon", icon)
                addProperty("hardcore", save.isHardcoreModeEnabled)
                addProperty("commandsAllowed", save.cheatsEnabled)
                addProperty("version", "1.8.9")
            })
        }
    }

    fun openWorld(body: String) {
        val name = body.string("name") ?: return
        val displayName = runCatching { mc.saveLoader.saveList.firstOrNull { it.fileName == name }?.displayName }.getOrNull() ?: name
        onClientThread { mc.launchIntegratedServer(name, displayName, null) }
    }

    fun editWorld(body: String) {
        val name = body.string("name") ?: return
        onClientThread { displayNative(GuiRenameWorld(GuiSelectWorld(GuiMainMenu()), name)) }
    }

    fun removeWorld(body: String) {
        val name = body.string("name") ?: return
        runCatching {
            mc.saveLoader.flushCache()
            mc.saveLoader.deleteWorldDirectory(name)
        }
    }

    fun emptyProxies() = JsonArray()

    fun openNativeAltManager() = onClientThread { displayNative(GuiAltManager(GuiMainMenu())) }

    fun rejectProxyOperation() {
        UiEventSocket.publish("proxyCheckResult", JsonObject().apply {
            add("proxy", JsonNull.INSTANCE)
            addProperty("error", "Proxy transport is unavailable in the 1.8.9 runtime; no fake connection was applied.")
        })
    }

    private fun login(account: MinecraftAccount) {
        SharedScopes.IO.launch {
            runCatching {
                account.update()
                Session(account.session.username, account.session.uuid, account.session.token, "microsoft")
            }.onSuccess { session ->
                onClientThread {
                    mc.session = session
                    EventManager.call(SessionUpdateEvent)
                    publishLogin(session.username)
                }
            }.onFailure { throwable ->
                UiEventSocket.publish("accountManagerLogin", JsonObject().apply {
                    add("username", JsonNull.INSTANCE)
                    addProperty("error", throwable.message ?: "Account login failed")
                })
            }
        }
    }

    private fun publishLogin(username: String) {
        UiEventSocket.publish("accountManagerLogin", JsonObject().apply {
            addProperty("username", username)
            add("error", JsonNull.INSTANCE)
        })
    }

    private fun serverJson(index: Int, data: ServerData) = JsonObject().apply {
        addProperty("id", index)
        addProperty("address", data.serverIP)
        addProperty("icon", data.base64EncodedIconData ?: "")
        addProperty("label", data.serverMOTD ?: "")
        add("players", JsonObject().apply {
            addProperty("max", 0)
            addProperty("online", 0)
        })
        addProperty("name", data.serverName)
        addProperty("online", data.pingToServer >= 0)
        addProperty("playerCountLabel", data.populationInfo ?: "")
        addProperty("protocolVersion", data.version)
        addProperty("version", data.gameVersion ?: "1.8.9")
        addProperty("ping", data.pingToServer)
        addProperty("resourcePackPolicy", data.resourceMode.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() })
    }

    private fun setResourceMode(data: ServerData, value: String?) {
        val mode = ServerData.ServerResourceMode.values().firstOrNull { it.name.equals(value, true) } ?: return
        data.resourceMode = mode
    }

    private inline fun editServerList(block: (ServerList) -> Unit) = synchronized(this) {
        val list = ServerList(mc).apply { loadServerList() }
        block(list)
        list.saveServerList()
    }

    private fun accountType(account: MinecraftAccount) = when (account) {
        is CrackedAccount -> "Cracked"
        is MicrosoftAccount -> "Microsoft"
        else -> account.javaClass.simpleName.removeSuffix("Account")
    }

    private fun displayNative(screen: net.minecraft.client.gui.GuiScreen) =
        NextGenVirtualScreenRouter.displayNative(screen)

    private fun onClientThread(action: () -> Unit) {
        mc.addScheduledTask(action)
    }

    private fun String.json(): JsonObject? = runCatching { parser.parse(this).asJsonObject }.getOrNull()
    private fun String.string(name: String) = json()?.get(name)?.takeIf { it.isJsonPrimitive }?.asString
    private fun String.int(name: String) = json()?.get(name)?.takeIf { it.isJsonPrimitive }?.asInt
    private fun String.array(name: String) = json()?.getAsJsonArray(name)
}
