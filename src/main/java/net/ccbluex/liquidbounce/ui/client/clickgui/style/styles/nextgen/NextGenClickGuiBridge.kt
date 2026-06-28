/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen
import net.ccbluex.liquidbounce.utils.input.safeKeyName

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.BlockValue
import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.CurveValue
import net.ccbluex.liquidbounce.config.FileValue
import net.ccbluex.liquidbounce.config.FloatRangeValue
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.config.IntRangeValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.minecraft.client.gui.FontRenderer
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.KeyBindValue
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.config.MultiSelectValue
import net.ccbluex.liquidbounce.config.TextValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.config.Vec3Value
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleBindAction
import net.ccbluex.liquidbounce.features.module.ModuleBindModifier
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.SettingsFiles
import net.ccbluex.liquidbounce.ui.client.gui.GuiUpdate
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.keybind.KeyBindManager
import net.ccbluex.liquidbounce.ui.font.fontmanager.GuiFontManager
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Desktop
import java.awt.Color
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.FutureTask
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

object NextGenClickGuiBridge : MinecraftInstance {

    private val parser = JsonParser()
    private val storageFile = File(FileManager.guiLayoutsDir, "nextgen-clickgui-storage.json")

    fun modules(): JsonArray = JsonArray().apply {
        ModuleManager.forEach { add(moduleJson(it)) }
    }

    fun module(name: String): JsonObject =
        ModuleManager[name]?.let(::moduleJson) ?: JsonObject()

    fun moduleSettings(name: String): JsonObject {
        val module = ModuleManager[name]

        return configurable(
            name = module?.name ?: name,
            values = module?.let {
                listOf(moduleBindSetting(it)) + it.values.mapNotNull(::settingJson)
            }.orEmpty()
        )
    }

    fun applyModuleSettings(name: String, body: String) {
        val module = ModuleManager[name] ?: return
        val root = runCatching { parser.parse(body).asJsonObject }.getOrNull() ?: return
        val settings = root.getAsJsonArray("value") ?: return

        runOnMinecraftThread {
            for (setting in settings) {
                val settingObject = setting.asJsonObject
                val valueName = settingObject.get("name")?.asString ?: continue
                if (valueName == MODULE_BIND_SETTING_NAME) {
                    applyModuleBind(module, settingObject.get("value") ?: continue)
                    continue
                }
                val value = module.getValue(valueName) ?: continue
                applyValue(value, settingObject.get("value") ?: continue)
            }
        }
    }

    fun setModuleEnabled(body: String) {
        val json = runCatching { parser.parse(body).asJsonObject }.getOrNull() ?: return
        val name = json.get("name")?.asString ?: return
        val enabled = json.get("enabled")?.asBoolean ?: return
        val module = ModuleManager[name] ?: return

        runOnMinecraftThread {
            module.state = enabled
        }
    }

    fun localStorage(): JsonObject {
        if (!storageFile.exists()) {
            return JsonObject().apply { add("items", JsonArray()) }
        }

        return runCatching {
            parser.parse(storageFile.reader(Charsets.UTF_8)).asJsonObject
        }.getOrElse {
            JsonObject().apply { add("items", JsonArray()) }
        }
    }

    fun saveLocalStorage(body: String) {
        val json = runCatching { parser.parse(body).asJsonObject }.getOrNull() ?: return
        if (!storageFile.parentFile.exists()) {
            storageFile.parentFile.mkdirs()
        }

        storageFile.writeText(FileManager.PRETTY_GSON.toJson(json), Charsets.UTF_8)
    }

    fun gameWindow(): JsonObject {
        val resolution = ScaledResolution(mc)

        return JsonObject().apply {
            addProperty("width", mc.displayWidth)
            addProperty("height", mc.displayHeight)
            addProperty("scaledWidth", resolution.scaledWidth)
            addProperty("scaledHeight", resolution.scaledHeight)
            addProperty("scaleFactor", resolution.scaleFactor)
            addProperty("guiScale", mc.gameSettings.guiScale)
        }
    }

    fun clientInfo(): JsonObject {
        return JsonObject().apply {
            addProperty("os", osName())
            addProperty("gameVersion", "1.8.9")
            addProperty("clientVersion", FDPClient.clientVersionText)
            addProperty("clientName", FDPClient.CLIENT_NAME)
            addProperty("development", FDPClient.IN_DEV)
            addProperty("fps", Minecraft.getDebugFPS())
            addProperty("gameDir", mc.mcDataDir.absolutePath)
            addProperty("clientDir", FileManager.dir.absolutePath)
            addProperty("inGame", mc.theWorld != null && mc.thePlayer != null)
            addProperty("viaFabricPlus", false)
            addProperty("hasProtocolHack", false)
        }
    }

    fun theme(id: String): JsonObject {
        val accent = ClientThemesUtils.getColor()
        val tint = ClientThemesUtils.getBackgroundColor(0, 255)

        return JsonObject().apply {
            addProperty("name", "FDPClient NextGen")
            addProperty("id", id)
            add("colors", JsonObject().apply {
                addProperty("accent", accent.rgb)
                addProperty("tint", tint.rgb)
            })
            add("settings", JsonObject())
        }
    }

    fun globalSettings(): JsonObject = configurable(
        "Settings",
        listOf(
            configurable(
                "Shortcuts",
                listOf(
                    button("HUD Designer", "hud-designer"),
                    button("Spotify Player", "spotify-player"),
                    button("Spotify Settings", "spotify-settings"),
                    button("Keybind Manager", "keybind-manager"),
                    button("Font Manager", "font-manager"),
                    button("Check Update", "check-update"),
                    button("Save Config", "save-config"),
                    button("Reload Config", "reload-config"),
                    button("Open Configs Folder", "open-configs-folder"),
                    button("Open Themes Folder", "open-themes-folder"),
                    button("GitHub", "github"),
                    button("Discord", "discord"),
                    button("Support", "support")
                )
            ),
            configurable(
                "ClickGUI",
                ClickGUIModule.values.mapNotNull(::settingJson)
            ),
            configurable(
                "Theme",
                listOf(
                    choose("Theme Mode", displayChoice(ClientThemesUtils.ClientColorMode, THEME_MODES), THEME_MODES),
                    choose("Background Mode", displayChoice(ClientThemesUtils.BackgroundMode, BACKGROUND_MODES), BACKGROUND_MODES),
                    intSetting("Theme Fade Speed", ClientThemesUtils.ThemeFadeSpeed, 1, 10),
                    booleanSetting("Reverse Fade", ClientThemesUtils.updown)
                )
            )
        )
    )

    fun applyGlobalSettings(body: String) {
        val root = runCatching { parser.parse(body).asJsonObject }.getOrNull() ?: return
        val groups = root.getAsJsonArray("value") ?: return

        runOnMinecraftThread {
            for (group in groups) {
                val groupObject = group.asJsonObject
                val groupName = groupObject.get("name")?.asString ?: continue
                val values = groupObject.getAsJsonArray("value") ?: continue

                when (groupName.lowercase(Locale.ROOT)) {
                    "clickgui" -> applyValues(ClickGUIModule.values, values)
                    "theme" -> applyThemeSettings(values)
                }
            }

            FileManager.saveConfig(FileManager.valuesConfig)
            FileManager.saveConfig(FileManager.colorThemeConfig)
        }
    }

    fun runAction(body: String) {
        val action = runCatching { parser.parse(body).asJsonObject.get("action").asString }.getOrNull() ?: return

        runOnMinecraftThread {
            when (action) {
                "hud-designer" -> mc.displayGuiScreen(GuiHudDesigner())
                "spotify-player" -> SpotifyModule.openPlayerScreen()
                "spotify-settings" -> SpotifyModule.openConfigScreen()
                "spotify-connect" -> SpotifyModule.connectWebApi()
                "keybind-manager" -> mc.displayGuiScreen(KeyBindManager)
                "font-manager" -> mc.displayGuiScreen(GuiFontManager(mc.currentScreen))
                "check-update" -> mc.displayGuiScreen(GuiUpdate())
                "save-config" -> FileManager.saveAllConfigs()
                "reload-config" -> FileManager.load(FileManager.nowConfig, save = false)
                "redownload-nextgen-assets" -> {
                    NextGenBrowserRuntime.retry(redownloadAssets = true)
                    HUD.addNotification(
                        Notification("NextGen ClickGUI", "Re-downloading in-game browser assets...", Type.INFO)
                    )
                }
                "open-configs-folder" -> openFolder(FileManager.settingsDir)
                "open-themes-folder" -> openFolder(FileManager.themesDir)
                "github" -> MiscUtils.showURL(FDPClient.CLIENT_GITHUB)
                "discord" -> MiscUtils.showURL("https://discord.com/invite/3XRFGeqEYD")
                "support" -> MiscUtils.showURL("https://github.com/opZywl/fdpclient/issues")
            }
        }
    }

    /** Current Spotify playback for the web GUI to poll. Works for both the Local and Web API sources. */
    fun spotifyNowPlaying(): JsonObject = JsonObject().apply {
        val state = SpotifyModule.currentState
        addProperty("connection", SpotifyModule.connectionState.name)
        addProperty("local", SpotifyModule.usingLocalSource)
        addProperty("isPlaying", state?.isPlaying ?: false)
        addProperty("progressMs", state?.progressMs ?: 0)
        addProperty("volumePercent", state?.volumePercent ?: -1)
        addProperty("shuffle", state?.shuffleEnabled ?: false)
        addProperty("repeat", SpotifyModule.repeatMode.name)
        addProperty("updatedAt", state?.updatedAt ?: 0L)
        val track = state?.track
        if (track == null) {
            add("track", JsonNull.INSTANCE)
        } else {
            add("track", JsonObject().apply {
                addProperty("id", track.id)
                addProperty("title", track.title)
                addProperty("artists", track.artists)
                addProperty("album", track.album)
                addProperty("coverUrl", track.coverUrl ?: "")
                addProperty("durationMs", track.durationMs)
            })
        }
    }

    /** Playback control from the web GUI: {"action":"playPause"|"next"|"previous"|"seek"|"shuffle"|"repeat"}. */
    fun spotifyControl(body: String) {
        val obj = runCatching { parser.parse(body).asJsonObject }.getOrNull() ?: return
        val action = runCatching { obj.get("action").asString }.getOrNull() ?: return
        when (action.lowercase(Locale.ROOT)) {
            "playpause", "toggle" -> SpotifyModule.togglePlayback()
            "next" -> SpotifyModule.next()
            "previous", "prev" -> SpotifyModule.previous()
            "seek" -> obj.get("positionMs")?.takeIf { it.isJsonPrimitive }?.asInt?.let { SpotifyModule.seekTo(it) }
            "shuffle" -> SpotifyModule.toggleShuffle()
            "repeat" -> SpotifyModule.cycleRepeat()
        }
    }

    fun spotifyPlaylists(): JsonObject = JsonObject().apply {
        addProperty("connection", SpotifyModule.connectionState.name)
        addProperty("local", SpotifyModule.usingLocalSource)
        val playlists = runBlocking { withTimeoutOrNull(12000L) { SpotifyModule.browsePlaylists() } } ?: emptyList()
        add("playlists", JsonArray().apply {
            add(JsonObject().apply {
                addProperty("id", "liked")
                addProperty("name", "Liked Songs")
                addProperty("trackCount", -1)
                addProperty("imageUrl", "")
                addProperty("uri", "")
                addProperty("likedSongs", true)
            })
            playlists.forEach { p ->
                add(JsonObject().apply {
                    addProperty("id", p.id)
                    addProperty("name", p.name)
                    addProperty("owner", p.owner ?: "")
                    addProperty("trackCount", p.trackCount)
                    addProperty("imageUrl", p.imageUrl ?: "")
                    addProperty("uri", p.uri ?: "")
                    addProperty("likedSongs", p.isLikedSongs)
                })
            }
        })
    }

    fun spotifyPlaylistTracks(id: String): JsonObject = JsonObject().apply {
        addProperty("id", id)
        val page = runBlocking { withTimeoutOrNull(15000L) { SpotifyModule.browsePlaylistTracks(id) } }
        val tracks = page?.tracks ?: emptyList()
        val liked = runBlocking { withTimeoutOrNull(8000L) { SpotifyModule.likedStatuses(tracks.map { it.id }) } } ?: emptyMap()
        addProperty("total", page?.total ?: 0)
        add("tracks", JsonArray().apply {
            tracks.forEach { t ->
                add(JsonObject().apply {
                    addProperty("id", t.id)
                    addProperty("uri", t.uri ?: "")
                    addProperty("title", t.title)
                    addProperty("artists", t.artists)
                    addProperty("album", t.album)
                    addProperty("durationMs", t.durationMs)
                    addProperty("coverUrl", t.coverUrl ?: "")
                    addProperty("addedAt", t.addedAt ?: 0L)
                    addProperty("liked", liked[t.id] ?: false)
                })
            }
        })
    }

    fun spotifyPlay(body: String) {
        val obj = runCatching { parser.parse(body).asJsonObject }.getOrNull() ?: return
        val contextUri = obj.get("contextUri")?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
        val trackUri = obj.get("trackUri")?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
        SpotifyModule.playContext(contextUri, trackUri)
    }

    fun spotifyLike(body: String) {
        val obj = runCatching { parser.parse(body).asJsonObject }.getOrNull() ?: return
        val trackId = obj.get("trackId")?.takeIf { it.isJsonPrimitive }?.asString ?: return
        val save = obj.get("save")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: true
        SpotifyModule.setLiked(trackId, save)
    }

    fun virtualScreen(): JsonObject = JsonObject().apply {
        addProperty("name", "clickgui")
    }

    fun printableKey(key: String): JsonObject = JsonObject().apply {
        addProperty("translationKey", key)
        addProperty("localized", printableKeyName(key))
    }

    private fun moduleJson(module: Module): JsonObject = JsonObject().apply {
        addProperty("name", module.name)
        addProperty("category", categoryName(module.category))
        add("keyBind", inputBind(module))
        addProperty("enabled", module.state)
        addProperty("description", runCatching { module.description }.getOrDefault(""))
        addProperty("hidden", module.isHidden)
        add("aliases", JsonArray().apply {
            module.aliases.forEach { add(JsonPrimitive(it)) }
        })
        module.tag?.let { addProperty("tag", it) } ?: add("tag", JsonNull.INSTANCE)
    }

    private fun categoryName(category: Category): String = when (category) {
        Category.VISUAL -> "Render"
        Category.OTHER -> "Misc"
        else -> category.displayName
    }

    private fun configurable(name: String, values: List<JsonObject>): JsonObject = settingBase("CONFIGURABLE", name).apply {
        add("value", JsonArray().apply { values.forEach(::add) })
    }

    private fun button(label: String, action: String): JsonObject = settingBase("BUTTON", label).apply {
        addProperty("value", label)
        addProperty("action", action)
    }

    private fun moduleBindSetting(module: Module): JsonObject =
        settingBase("BIND", MODULE_BIND_SETTING_NAME).apply {
            add("value", inputBind(module))
            add("defaultValue", inputBind(Keyboard.KEY_NONE, ModuleBindAction.TOGGLE, emptySet()))
            addProperty("description", "Configure the key and action used to toggle or hold ${module.name}.")
        }

    private fun choose(name: String, value: String, choices: Array<String>): JsonObject = settingBase("CHOOSE", name).apply {
        addProperty("value", value)
        add("choices", JsonArray().apply {
            choices.forEach { add(JsonPrimitive(it)) }
        })
    }

    private fun intSetting(name: String, value: Int, min: Int, max: Int): JsonObject = settingBase("INT", name).apply {
        addProperty("value", value)
        add("range", range(min, max))
        addProperty("suffix", "")
    }

    private fun booleanSetting(name: String, value: Boolean): JsonObject = settingBase("BOOLEAN", name).apply {
        addProperty("value", value)
    }

    private fun settingJson(value: Value<*>): JsonObject? {
        if (!value.shouldRender()) {
            return null
        }
        if (value.owner === ClickGUIModule && value.name == "Re-download Assets") {
            return button(value.name, "redownload-nextgen-assets").apply {
                addProperty(
                    "description",
                    value.description ?: "Re-download the in-game browser assets and retry the NextGen ClickGUI."
                )
            }
        }

        val json = when (value) {
            is BoolValue -> settingBase("BOOLEAN", value.name).apply {
                addProperty("value", value.get())
            }

            is IntValue -> settingBase("INT", value.name).apply {
                addProperty("value", value.get())
                add("range", range(value.minimum, value.maximum))
                addProperty("suffix", value.suffix ?: "")
            }

            is BlockValue -> settingBase("INT", value.name).apply {
                addProperty("value", value.get())
                add("range", range(value.minimum, value.maximum))
                addProperty("suffix", value.suffix ?: "")
            }

            is IntRangeValue -> settingBase("INT_RANGE", value.name).apply {
                add("value", range(value.get().first, value.get().last))
                add("range", range(value.minimum, value.maximum))
                addProperty("suffix", value.suffix ?: "")
            }

            is FloatValue -> settingBase("FLOAT", value.name).apply {
                addProperty("value", value.get())
                add("range", range(value.minimum, value.maximum))
                addProperty("suffix", value.suffix ?: "")
            }

            is FloatRangeValue -> settingBase("FLOAT_RANGE", value.name).apply {
                add("value", range(value.get().start, value.get().endInclusive))
                add("range", range(value.minimum, value.maximum))
                addProperty("suffix", value.suffix ?: "")
            }

            is ListValue -> settingBase("CHOOSE", value.name).apply {
                addProperty("value", value.get())
                add("choices", JsonArray().apply {
                    value.values.forEach { add(JsonPrimitive(it)) }
                })
            }

            is MultiSelectValue -> settingBase("MULTI_CHOOSE", value.name).apply {
                add("value", JsonArray().apply {
                    value.get().forEach { add(JsonPrimitive(it)) }
                })
                add("choices", JsonArray().apply {
                    value.choices.forEach { add(JsonPrimitive(it)) }
                })
                addProperty("canBeNone", true)
                addProperty("isOrderSensitive", false)
            }

            is ColorValue -> settingBase("COLOR", value.name).apply {
                addProperty("value", value.get().rgb)
            }

            is TextValue -> settingBase("TEXT", value.name).apply {
                addProperty("value", value.get())
            }

            is FileValue -> settingBase("FILE", value.name).apply {
                addProperty("value", value.get())
                addProperty("shortName", value.shortName)
                addProperty("dialogMode", when (value.dialogMode.name) {
                    "SELECT_FOLDER" -> "OPEN_FOLDER"
                    else -> value.dialogMode.name
                })
                add("supportedExtensions", JsonArray().apply {
                    value.extensions.forEach { add(JsonPrimitive(it)) }
                })
            }

            is FontValue -> settingBase("CHOOSE", value.name).apply {
                addProperty("value", fontLabel(value.get()))
                add("choices", JsonArray().apply {
                    Fonts.fonts.forEach { add(JsonPrimitive(fontLabel(it))) }
                })
            }

            is KeyBindValue -> settingBase("KEY", value.name).apply {
                addProperty("value", toMinecraftKey(value.get()))
            }

            is Vec3Value -> settingBase("VECTOR3_D", value.name).apply {
                val vec = value.get()
                add("value", JsonObject().apply {
                    addProperty("x", vec[0])
                    addProperty("y", vec[1])
                    addProperty("z", vec[2])
                })
                addProperty("useLocateButton", false)
            }

            is CurveValue -> settingBase("CURVE", value.name).apply {
                val points = value.get()
                add("value", JsonArray().apply {
                    val last = points.size - 1
                    points.forEachIndexed { index, y ->
                        add(JsonObject().apply {
                            addProperty("x", if (last <= 0) 0.0 else index.toDouble() / last)
                            addProperty("y", y)
                        })
                    }
                })
                add("xAxis", curveAxis("X"))
                add("yAxis", curveAxis("Y"))
                addProperty("tension", 0.0)
            }

            // Nested group (ValueGroup / ToggleableValueGroup / ModeValueGroup are all Configurable):
            // emit a CONFIGURABLE whose children are serialized recursively, so sub-group values
            // actually show up instead of collapsing into a single TEXT field. The group's master
            // toggle ("Enabled") or mode ("Mode") is itself a child value, so it comes along for free,
            // and gated-off children already drop out via shouldRender() above.
            is Configurable -> configurable(value.name, value.values.mapNotNull(::settingJson))

            else -> settingBase("TEXT", value.name).apply {
                addProperty("value", value.toText())
            }
        }

        json.addProperty(
            "description",
            value.description ?: "Configure ${value.name.replace(NAME_BOUNDARY, " ").lowercase(Locale.ROOT)}."
        )
        return json
    }

    /** Axis descriptor for a CURVE setting; both axes span 0..1 for our normalized curve points. */
    private fun curveAxis(label: String): JsonObject = JsonObject().apply {
        addProperty("label", label)
        add("range", range(0.0, 1.0))
    }

    private fun settingBase(type: String, name: String): JsonObject = JsonObject().apply {
        addProperty("valueType", type)
        addProperty("name", name)
        add("description", JsonNull.INSTANCE)
        add("key", JsonNull.INSTANCE)
    }

    private fun range(from: Number, to: Number): JsonObject = JsonObject().apply {
        addProperty("from", from)
        addProperty("to", to)
    }

    /** Label for a font, matching FontValue's own naming, used as the CHOOSE option key. */
    private fun fontLabel(font: FontRenderer): String = when {
        font is GameFontRenderer -> "${font.defaultFont.font.name} - ${font.defaultFont.font.size}"
        font === Fonts.minecraftFont -> "Minecraft"
        else -> Fonts.getFontDetails(font)?.let {
            "${it.name}${if (it.size != -1) " - ${it.size}" else ""}"
        } ?: "Unknown"
    }

    private fun inputBind(module: Module): JsonObject =
        inputBind(module.keyBind, module.bindAction, module.bindModifiers)

    private fun inputBind(
        key: Int,
        action: ModuleBindAction = ModuleBindAction.TOGGLE,
        modifiers: Set<ModuleBindModifier> = emptySet(),
    ): JsonObject = JsonObject().apply {
        addProperty("boundKey", toMinecraftKey(key))
        addProperty("action", action.displayName)
        add("modifiers", JsonArray().apply {
            modifiers.sortedBy { it.ordinal }.forEach { add(JsonPrimitive(it.displayName)) }
        })
    }

    private fun applyModuleBind(module: Module, element: JsonElement) {
        val bind = runCatching { element.asJsonObject }.getOrNull() ?: return
        module.keyBind = fromMinecraftKey(bind.get("boundKey")?.asString ?: UNKNOWN_KEY)
        module.bindAction = ModuleBindAction.fromDisplayName(bind.get("action")?.asString)
        module.bindModifiers = bind.getAsJsonArray("modifiers")?.mapNotNull {
            ModuleBindModifier.fromDisplayName(runCatching { it.asString }.getOrNull())
        }?.toSet().orEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    private fun applyValue(value: Value<*>, element: JsonElement) {
        runCatching {
            when (value) {
                is BoolValue -> value.set(element.asBoolean)
                is IntValue -> value.set(element.asInt)
                is BlockValue -> value.set(element.asInt)
                is IntRangeValue -> value.set(element.asJsonObject.get("from").asInt..element.asJsonObject.get("to").asInt)
                is FloatValue -> value.set(element.asFloat)
                is FloatRangeValue -> value.set(element.asJsonObject.get("from").asFloat..element.asJsonObject.get("to").asFloat)
                is ListValue -> value.set(element.asString)
                is MultiSelectValue -> value.set(element.asJsonArray.mapNotNull { it.asString }.toSet())
                is ColorValue -> value.set(Color(element.asInt, true))
                is TextValue -> value.set(element.asString)
                is FileValue -> value.set(element.asString)
                is KeyBindValue -> value.set(fromMinecraftKey(element.asString))
                is Vec3Value -> {
                    val vector = element.asJsonObject
                    value.set(doubleArrayOf(
                        vector.get("x").asDouble,
                        vector.get("y").asDouble,
                        vector.get("z").asDouble
                    ))
                }
                is FontValue -> Fonts.fonts.firstOrNull { fontLabel(it) == element.asString }?.let { value.set(it) }
                is CurveValue -> {
                    val points = element.asJsonArray.mapNotNull { entry ->
                        val point = entry.asJsonObject
                        val x = point.get("x")?.asDouble ?: return@mapNotNull null
                        val y = point.get("y")?.asDouble ?: return@mapNotNull null
                        x to y
                    }.sortedBy { it.first }
                    if (points.size >= 2) {
                        value.set(DoubleArray(points.size) { points[it].second.coerceIn(0.0, 1.0) })
                    }
                }
                is Configurable -> applyValues(value.values, element.asJsonArray)
                else -> Unit
            }
        }
    }

    private fun applyValues(values: List<Value<*>>, settings: JsonArray) {
        for (setting in settings) {
            val settingObject = setting.asJsonObject
            val valueName = settingObject.get("name")?.asString ?: continue
            val value = values.firstOrNull { it.name == valueName } ?: continue
            applyValue(value, settingObject.get("value") ?: continue)
        }
    }

    private fun applyThemeSettings(settings: JsonArray) {
        for (setting in settings) {
            val settingObject = setting.asJsonObject
            val name = settingObject.get("name")?.asString ?: continue
            val value = settingObject.get("value") ?: continue

            when (name) {
                "Theme Mode" -> ClientThemesUtils.ClientColorMode = value.asString
                "Background Mode" -> ClientThemesUtils.BackgroundMode = value.asString
                "Theme Fade Speed" -> ClientThemesUtils.ThemeFadeSpeed = value.asInt
                "Reverse Fade" -> ClientThemesUtils.updown = value.asBoolean
            }
        }
    }

    private fun openFolder(folder: File) {
        runCatching {
            if (!folder.exists()) {
                folder.mkdirs()
            }
            Desktop.getDesktop().open(folder)
        }.onFailure {
            LOGGER.error("[NextGen] Failed to open folder ${folder.absolutePath}", it)
        }
    }

    fun openFileDialog(body: String): JsonObject {
        val request = runCatching { parser.parse(body).asJsonObject }.getOrNull()
            ?: return JsonObject().apply { add("file", JsonNull.INSTANCE) }
        val mode = request.get("mode")?.asString ?: "OPEN_FILE"
        val extensions = request.getAsJsonArray("supportedExtensions")
            ?.mapNotNull { if (it.isJsonPrimitive) it.asString else null }
            .orEmpty()

        val task = FutureTask<File?> {
            val chooser = JFileChooser().apply {
                fileSelectionMode = if (mode == "OPEN_FOLDER") JFileChooser.DIRECTORIES_ONLY else JFileChooser.FILES_ONLY
                if (extensions.isNotEmpty() && mode != "OPEN_FOLDER") {
                    fileFilter = FileNameExtensionFilter(extensions.joinToString(", "), *extensions.toTypedArray())
                }
            }
            val result = if (mode == "SAVE_FILE") chooser.showSaveDialog(null) else chooser.showOpenDialog(null)
            chooser.selectedFile.takeIf { result == JFileChooser.APPROVE_OPTION }
        }
        runCatching {
            if (SwingUtilities.isEventDispatchThread()) task.run() else SwingUtilities.invokeAndWait(task)
        }.onFailure { LOGGER.error("Failed to open file chooser", it) }

        return JsonObject().apply {
            val selected = if (task.isDone) runCatching { task.get() }.getOrNull() else null
            if (selected == null) add("file", JsonNull.INSTANCE) else addProperty("file", selected.absolutePath)
        }
    }

    fun browsePath(body: String) {
        val path = runCatching { parser.parse(body).asJsonObject.get("path").asString }.getOrNull() ?: return
        val file = File(path)
        if (!file.exists()) return
        runCatching { Desktop.getDesktop().open(file) }
            .onFailure { LOGGER.error("Failed to open ${file.absolutePath}", it) }
    }

    private fun displayChoice(value: String, choices: Array<String>): String =
        choices.firstOrNull { it.equals(value, ignoreCase = true) } ?: choices.first()

    private fun runOnMinecraftThread(action: () -> Unit) {
        if (mc.isCallingFromMinecraftThread) {
            action()
            return
        }

        mc.addScheduledTask { action() }.get(2, TimeUnit.SECONDS)
    }

    private fun osName(): String {
        val os = System.getProperty("os.name", "unknown").lowercase(Locale.ROOT)
        return when {
            "win" in os -> "windows"
            "mac" in os -> "mac"
            "linux" in os -> "linux"
            "sunos" in os || "solaris" in os -> "solaris"
            else -> "unknown"
        }
    }

    private fun toMinecraftKey(key: Int): String {
        if (key == Keyboard.KEY_NONE) {
            return UNKNOWN_KEY
        }

        val keyName = (safeKeyName(key) ?: "None")?.lowercase(Locale.ROOT) ?: return UNKNOWN_KEY

        return KEY_TO_MINECRAFT[keyName] ?: when {
            keyName.length == 1 && keyName[0].isLetterOrDigit() -> "key.keyboard.$keyName"
            keyName.matches(Regex("f\\d{1,2}")) -> "key.keyboard.$keyName"
            else -> "key.keyboard.${keyName.replace('_', '.')}"
        }
    }

    private fun fromMinecraftKey(key: String): Int {
        if (key == UNKNOWN_KEY) {
            return Keyboard.KEY_NONE
        }

        val mapped = MINECRAFT_TO_KEY[key]
        if (mapped != null) {
            return Keyboard.getKeyIndex(mapped)
        }

        val raw = key.removePrefix("key.keyboard.").replace('.', '_').uppercase(Locale.ROOT)
        return Keyboard.getKeyIndex(raw).takeIf { it != Keyboard.KEY_NONE } ?: Keyboard.KEY_NONE
    }

    private fun printableKeyName(key: String): String {
        if (key == UNKNOWN_KEY) {
            return "None"
        }

        return key
            .removePrefix("key.keyboard.")
            .removePrefix("key.mouse.")
            .split('.', '_')
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase(Locale.ROOT) } }
    }

    private const val UNKNOWN_KEY = "key.keyboard.unknown"
    private const val MODULE_BIND_SETTING_NAME = "Bind"
    private val NAME_BOUNDARY = Regex("(?<=[a-z0-9])(?=[A-Z])")

    private val KEY_TO_MINECRAFT = mapOf(
        "escape" to "key.keyboard.escape",
        "return" to "key.keyboard.enter",
        "space" to "key.keyboard.space",
        "back" to "key.keyboard.backspace",
        "tab" to "key.keyboard.tab",
        "lshift" to "key.keyboard.left.shift",
        "rshift" to "key.keyboard.right.shift",
        "lcontrol" to "key.keyboard.left.control",
        "rcontrol" to "key.keyboard.right.control",
        "lmenu" to "key.keyboard.left.alt",
        "rmenu" to "key.keyboard.right.alt",
        "left" to "key.keyboard.left",
        "right" to "key.keyboard.right",
        "up" to "key.keyboard.up",
        "down" to "key.keyboard.down",
        "delete" to "key.keyboard.delete",
        "insert" to "key.keyboard.insert",
        "home" to "key.keyboard.home",
        "end" to "key.keyboard.end",
        "prior" to "key.keyboard.page.up",
        "next" to "key.keyboard.page.down",
        "capital" to "key.keyboard.caps.lock",
        "numlock" to "key.keyboard.num.lock"
    )

    private val MINECRAFT_TO_KEY = KEY_TO_MINECRAFT.entries.associate { it.value to it.key.uppercase(Locale.ROOT) }

    private val THEME_MODES = arrayOf(
        "Zywl", "Water", "Magic", "DarkNight", "Sun", "Flower", "Tree", "Loyoi", "FDP", "May",
        "Mint", "Cero", "Azure", "Pumpkin", "Polarized", "Sundae", "Terminal", "Coral", "Fire",
        "Aqua", "Peony", "Vergren", "EveningSunshine", "LightOrange", "Reef", "Amin", "Magics",
        "MangoPulp", "MoonPurple", "Aqualicious", "Stripe", "Shifter", "Quepal", "Orca",
        "SublimeVivid", "MoonAsteroid", "SummerDog", "PinkFlavour", "SinCityRed", "Timber",
        "PinotNoir", "DirtyFog", "Piglet", "LittleLeaf", "Nelson", "TurquoiseFlow", "Purplin",
        "Martini", "SoundCloud", "Inbox", "Amethyst", "Blush", "MochaRose", "Astolfo", "Rainbow"
    )

    private val BACKGROUND_MODES = arrayOf("Synced", "Dark", "Custom", "NeverLose", "None")
}
