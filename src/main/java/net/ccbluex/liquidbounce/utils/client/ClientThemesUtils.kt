/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.utils.extensions.setAlpha
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.mixColors
import java.awt.Color

/**
 *  by @opZywl
 * A utility object for managing client theme colors and background,
 * supporting multiple color modes, animations, etc.
 */
object ClientThemesUtils {

    /**
     * The selected color mode (e.g., "Zywl", "Water", "Magic", etc.).
     * Now a normal var with a default value "FDP".
     */
    var ClientColorMode: String = "MoonPurple"
        set(value) {
            field = value.lowercase()
        }

    /**
     * Speed controlling fade or animation in certain color transitions.
     */
    var ThemeFadeSpeed: Int = 7
        set(value) {
            field = value.coerceIn(1, 10)
        }

    /**
     * Up/down boolean toggling fade direction or type.
     * Now a normal var (default = false).
     */
    var updown: Boolean = false

    var BackgroundMode: String = "Synced"
        set(value) {
            field = value.lowercase()
        }

    /**
     * The selected background mode (e.g., "Dark", "Synced", "Custom", "NeverLose", "None").
     * Just like ClientColorMode, but for backgrounds.
     */
    var customBgColor: Color = Color(32, 32, 64)
    var neverLoseBgColor: Color = Color(60, 60, 60)

    /**
     * A custom background color if the user chooses "Custom" mode.
     * You might want to store or expose this in other ways, e.g. a ColorValue.
     */
    private fun parseHexColor(hexString: String): Color {
        val raw = hexString.replace("#", "")
        return try {
            val colorLong = raw.toLong(16)
            when (raw.length) {
                6 -> Color(colorLong.toInt() and 0xFFFFFF)
                8 -> Color((colorLong and 0xFFFFFFFF).toInt(), true)
                else -> Color(-1)
            }
        } catch (e: NumberFormatException) {
            Color(-1)
        }
    }

    /**
     * Returns the background color based on [BackgroundMode].
     *
     * @param index Usually used if "Synced" mode has an animation offset
     * @param alpha The transparency channel [0..255]
     */
    fun getBackgroundColor(index: Int = 0, alpha: Int = 255): Color {
        val m = BackgroundMode.lowercase()

        if (m.startsWith("#")) {
            return parseHexColor(m).setAlpha(alpha)
        }

        return when (m) {
            "dark" -> Color(21, 21, 21, alpha)
            "synced" -> getColorWithAlpha(index, alpha).darker().darker()
            "custom" -> customBgColor.setAlpha(alpha)
            "neverlose" -> neverLoseBgColor.setAlpha(alpha)
            "none" -> Color(0, 0, 0, 0)
            else -> Color(21, 21, 21, alpha)
        }
    }

    /**
     * Returns a color depending on the [ClientColorMode], used for
     * "start" or "end" of a gradient (or similar).
     *
     * @param type "START" or anything else => get second color
     * @param alpha transparency [0..255]
     */
    fun setColor(type: String, alpha: Int): Color {
        val mode = ClientColorMode.lowercase()
        if (mode.startsWith("#")) {
            return parseHexColor(mode).setAlpha(alpha)
        }
        return when (mode) {
            "zywl" ->
                if (type == "start") Color(215, 171, 168, alpha) else Color(206, 58, 98, alpha)
            "water" ->
                if (type == "start") Color(108, 170, 207, alpha) else Color(35, 69, 148, alpha)
            "magic" ->
                if (type == "start") Color(255, 180, 255, alpha) else Color(192, 67, 255, alpha)
            "darknight" ->
                if (type == "start") Color(203, 200, 204, alpha) else Color(93, 95, 95, alpha)
            "sun" ->
                if (type == "start") Color(252, 205, 44, alpha) else Color(255, 143, 0, alpha)
            "flower" ->
                if (type == "start") Color(182, 140, 195, alpha) else Color(184, 85, 199, alpha)
            "tree" ->
                if (type == "start") Color(76, 255, 102, alpha) else Color(18, 155, 38, alpha)
            "loyoi" ->
                if (type == "start") Color(255, 131, 124, alpha) else Color(255, 131, 0, alpha)
            "fdp" ->
                if (type == "start") Color(100, 255, 255, alpha) else Color(255, 100, 255, alpha)
            "may" ->
                if (type == "start") Color(255, 255, 255, alpha) else Color(255, 80, 255, alpha)
            "mint" ->
                if (type == "start") Color(85, 255, 255, alpha) else Color(85, 255, 140, alpha)
            "cero" ->
                if (type == "start") Color(170, 255, 170, alpha) else Color(170, 0, 170, alpha)
            "azure" ->
                if (type == "start") Color(0, 180, 255, alpha) else Color(0, 90, 255, alpha)
            "pumpkin" ->
                if (type == "start") Color(241, 166, 98, alpha) else Color(255, 216, 169, alpha)
            "polarized" ->
                if (type == "start") Color(173, 239, 209, alpha) else Color(0, 32, 64, alpha)
            "sundae" ->
                if (type == "start") Color(206, 74, 126, alpha) else Color(28, 28, 27, alpha)
            "terminal" ->
                if (type == "start") Color(15, 155, 15, alpha) else Color(25, 30, 25, alpha)
            "coral" ->
                if (type == "start") Color(244, 168, 150, alpha) else Color(52, 133, 151, alpha)
            "fire" ->
                if (type == "start") Color(255, 45, 30, alpha) else Color(255, 123, 15, alpha)
            "aqua" ->
                if (type == "start") Color(80, 255, 255, alpha) else Color(80, 190, 255, alpha)
            "peony" ->
                if (type == "start") Color(255, 120, 255, alpha) else Color(255, 190, 255, alpha)
            "vergren" ->
                if (type == "start") Color(170, 255, 169, alpha) else Color(17, 255, 189, alpha)
            "eveningsunshine" ->
                if (type == "start") Color(185, 43, 39, alpha) else Color(21, 101, 192, alpha)
            "lightorange" ->
                if (type == "start") Color(255, 183, 94, alpha) else Color(237, 143, 3, alpha)
            "reef" ->
                if (type == "start") Color(0, 210, 255, alpha) else Color(58, 123, 213, alpha)
            "amin" ->
                if (type == "start") Color(142, 45, 226, alpha) else Color(74, 0, 224, alpha)
            "magics" ->
                if (type == "start") Color(89, 193, 115, alpha) else Color(93, 38, 193, alpha)
            "mangopulp" ->
                if (type == "start") Color(240, 152, 25, alpha) else Color(237, 222, 93, alpha)
            "moonpurple" ->
                if (type == "start") Color(78, 84, 200, alpha) else Color(143, 148, 251, alpha)
            "aqualicious" ->
                if (type == "start") Color(80, 201, 195, alpha) else Color(150, 222, 218, alpha)
            "stripe" ->
                if (type == "start") Color(31, 162, 255, alpha) else Color(166, 255, 203, alpha)
            "shifter" ->
                if (type == "start") Color(188, 78, 156, alpha) else Color(248, 7, 89, alpha)
            "quepal" ->
                if (type == "start") Color(17, 153, 142, alpha) else Color(56, 239, 125, alpha)
            "orca" ->
                if (type == "start") Color(68, 160, 141, alpha) else Color(9, 54, 55, alpha)
            "sublimevivid" ->
                if (type == "start") Color(252, 70, 107, alpha) else Color(63, 94, 251, alpha)
            "moonasteroid" ->
                if (type == "start") Color(15, 32, 39, alpha) else Color(44, 83, 100, alpha)
            "summerdog" ->
                if (type == "start") Color(168, 255, 120, alpha) else Color(120, 255, 214, alpha)
            "pinkflavour" ->
                if (type == "start") Color(128, 0, 128, alpha) else Color(255, 192, 203, alpha)
            "sincityred" ->
                if (type == "start") Color(237, 33, 58, alpha) else Color(147, 41, 30, alpha)
            "timber" ->
                if (type == "start") Color(252, 0, 255, alpha) else Color(0, 219, 222, alpha)
            "pinotnoir" ->
                if (type == "start") Color(75, 108, 183, alpha) else Color(24, 40, 72, alpha)
            "dirtyfog" ->
                if (type == "start") Color(185, 147, 214, alpha) else Color(140, 166, 219, alpha)
            "piglet" ->
                if (type == "start") Color(238, 156, 167, alpha) else Color(255, 221, 225, alpha)
            "littleleaf" ->
                if (type == "start") Color(118, 184, 82, alpha) else Color(141, 194, 111, alpha)
            "nelson" ->
                if (type == "start") Color(242, 112, 156, alpha) else Color(255, 148, 114, alpha)
            "turquoiseflow" ->
                if (type == "start") Color(19, 106, 138, alpha) else Color(38, 120, 113, alpha)
            "purplin" ->
                if (type == "start") Color(106, 48, 147, alpha) else Color(160, 68, 255, alpha)
            "martini" ->
                if (type == "start") Color(253, 252, 71, alpha) else Color(36, 254, 65, alpha)
            "soundcloud" ->
                if (type == "start") Color(254, 140, 0, alpha) else Color(248, 54, 0, alpha)
            "inbox" ->
                if (type == "start") Color(69, 127, 202, alpha) else Color(86, 145, 200, alpha)
            "amethyst" ->
                if (type == "start") Color(157, 80, 187, alpha) else Color(110, 72, 170, alpha)
            "blush" ->
                if (type == "start") Color(178, 69, 146, alpha) else Color(241, 95, 121, alpha)
            "mocharose" ->
                if (type == "start") Color(245, 194, 231, alpha) else Color(243, 139, 168, alpha)
            "astolfo" ->
                if (type == "start")
                    ColorUtils.skyRainbow(0, 0.6f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
                else
                    ColorUtils.skyRainbow(90, 0.6f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)

            "rainbow" ->
                if (type == "start")
                    ColorUtils.skyRainbow(0, 1f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
                else
                    ColorUtils.skyRainbow(90, 1f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)

            else -> Color(-1)
        }
    }

    /**
     * Retrieve a color for the current mode, optionally using [index] for animation offsets.
     */
    fun getColor(index: Int = 0): Color {
        val mode = ClientColorMode.lowercase()
        val fadeVal = ThemeFadeSpeed / 5.0 * if (updown) 1 else -1
        if (mode.startsWith("#")) {
            return parseHexColor(mode)
        }
        val colorMap = mapOf(
            "zywl"      to Pair(Color(206, 58, 98),   Color(215, 171, 168)),
            "water"     to Pair(Color(35, 69, 148),   Color(108, 170, 207)),
            "magic"     to Pair(Color(255, 180, 255), Color(181, 139, 194)),
            "tree"      to Pair(Color(18, 155, 38),   Color(76, 255, 102)),
            "darknight" to Pair(Color(93, 95, 95),    Color(203, 200, 204)),
            "sun"       to Pair(Color(255, 143, 0),   Color(252, 205, 44)),
            "flower"    to Pair(Color(184, 85, 199),  Color(182, 140, 195)),
            "loyoi"     to Pair(Color(255, 131, 0),   Color(255, 131, 124)),
            "fdp"    to Pair(Color(255, 100, 255), Color(100, 255, 255)),
            "may"       to Pair(Color(255, 80, 255),  Color(255, 255, 255)),
            "mint"      to Pair(Color(85, 255, 140),  Color(85, 255, 255)),
            "cero"      to Pair(Color(170, 0, 170),   Color(170, 255, 170)),
            "azure"     to Pair(Color(0, 90, 255),    Color(0, 180, 255)),
            "pumpkin"   to Pair(Color(255, 216, 169), Color(241, 166, 98)),
            "polarized" to Pair(Color(0, 32, 64),     Color(173, 239, 209)),
            "sundae"    to Pair(Color(28, 28, 27),    Color(206, 74, 126)),
            "terminal"  to Pair(Color(25, 30, 25),    Color(15, 155, 15)),
            "coral"     to Pair(Color(52, 133, 151),  Color(244, 168, 150)),
            "fire"      to Pair(Color(255, 45, 30),   Color(255, 123, 15)),
            "aqua"      to Pair(Color(80,255,255),    Color(80,190,255)),
            "peony"     to Pair(Color(255,120,255),   Color(255,190,255)),
            "vergren"   to Pair(Color(170, 255, 169), Color(17, 255, 189)),
            "eveningsunshine" to Pair(Color(185, 43, 39), Color(21, 101, 192)),
            "lightorange"     to Pair(Color(255, 183, 94), Color(237, 143, 3)),
            "reef"             to Pair(Color(0, 210, 255), Color(58, 123, 213)),
            "amin"             to Pair(Color(142, 45, 226), Color(74, 0, 224)),
            "magics"            to Pair(Color(89, 193, 115), Color(93, 38, 193)),
            "mangopulp"       to Pair(Color(240, 152, 25), Color(237, 222, 93)),
            "moonpurple"      to Pair(Color(78, 84, 200), Color(143, 148, 251)),
            "aqualicious"      to Pair(Color(80, 201, 195), Color(150, 222, 218)),
            "stripe"           to Pair(Color(31, 162, 255), Color(166, 255, 203)),
            "shifter"          to Pair(Color(188, 78, 156), Color(248, 7, 89)),
            "quepal"           to Pair(Color(17, 153, 142), Color(56, 239, 125)),
            "orca"             to Pair(Color(68, 160, 141), Color(9, 54, 55)),
            "sublimevivid"    to Pair(Color(252, 70, 107), Color(63, 94, 251)),
            "moonasteroid"    to Pair(Color(15, 32, 39), Color(44, 83, 100)),
            "summerdog"       to Pair(Color(168, 255, 120), Color(120, 255, 214)),
            "pinkflavour"     to Pair(Color(128, 0, 128), Color(255, 192, 203)),
            "sincityred"     to Pair(Color(237, 33, 58), Color(147, 41, 30)),
            "timber"           to Pair(Color(252, 0, 255), Color(0, 219, 222)),
            "pinotnoir"       to Pair(Color(75, 108, 183), Color(24, 40, 72)),
            "dirtyfog"        to Pair(Color(185, 147, 214), Color(140, 166, 219)),
            "piglet"           to Pair(Color(238, 156, 167), Color(255, 221, 225)),
            "littleleaf"      to Pair(Color(118, 184, 82), Color(141, 194, 111)),
            "nelson"           to Pair(Color(242, 112, 156), Color(255, 148, 114)),
            "turquoiseflow"   to Pair(Color(19, 106, 138), Color(38, 120, 113)),
            "purplin"          to Pair(Color(106, 48, 147), Color(160, 68, 255)),
            "martini"          to Pair(Color(253, 252, 71), Color(36, 254, 65)),
            "soundcloud"       to Pair(Color(254, 140, 0), Color(248, 54, 0)),
            "inbox"            to Pair(Color(69, 127, 202), Color(86, 145, 200)),
            "amethyst"         to Pair(Color(157, 80, 187), Color(110, 72, 170)),
            "blush"            to Pair(Color(178, 69, 146), Color(241, 95, 121)),
            "mocharose"       to Pair(Color(245, 194, 231), Color(243, 139, 168)),
        "astolfo"   to Pair(
                ColorUtils.skyRainbow(0, 0.6F, 1F, 20000F / ThemeFadeSpeed),
                ColorUtils.skyRainbow(90, 0.6F, 1F, 20000F / ThemeFadeSpeed)
            ),
            "rainbow"   to Pair(
                ColorUtils.skyRainbow(0, 1F, 1F, 20000F / ThemeFadeSpeed),
                ColorUtils.skyRainbow(90, 1F, 1F, 20000F / ThemeFadeSpeed)
            )
        )

        val colorPair = colorMap[mode] ?: return Color(-1)
        return mixColors(colorPair.first, colorPair.second, fadeVal, index)
    }

    fun getColorWithAlpha(index: Int, alpha: Int): Color {
        val mode = ClientColorMode.lowercase()
        val fadeSpeed = (ThemeFadeSpeed / 5.0) * if (updown) 1 else -1

        if (mode.startsWith("#")) {
            return parseHexColor(mode).setAlpha(alpha)
        }
        return when (mode) {
            "zywl"      -> mixColors(Color(206, 58, 98),   Color(215, 171, 168), fadeSpeed, index).setAlpha(alpha)
            "water"     -> mixColors(Color(35, 69, 148),   Color(108, 170, 207), fadeSpeed, index).setAlpha(alpha)
            "magic"     -> mixColors(Color(255, 180, 255), Color(181, 139, 194), fadeSpeed, index).setAlpha(alpha)
            "tree"      -> mixColors(Color(18, 155, 38),   Color(76, 255, 102), fadeSpeed, index).setAlpha(alpha)
            "darknight" -> mixColors(Color(93, 95, 95),    Color(203, 200, 204), fadeSpeed, index).setAlpha(alpha)
            "sun"       -> mixColors(Color(255, 143, 0),   Color(252, 205, 44), fadeSpeed, index).setAlpha(alpha)
            "flower"    -> mixColors(Color(184, 85, 199),  Color(182, 140, 195), fadeSpeed, index).setAlpha(alpha)
            "loyoi"     -> mixColors(Color(255, 131, 0),   Color(255, 131, 124), fadeSpeed, index).setAlpha(alpha)
            "fdp"    -> mixColors(Color(255, 100, 255), Color(100, 255, 255), fadeSpeed, index).setAlpha(alpha)
            "may"       -> mixColors(Color(255, 80, 255),  Color(255, 255, 255), fadeSpeed, index).setAlpha(alpha)
            "mint"      -> mixColors(Color(85, 255, 180),  Color(85, 255, 255), fadeSpeed, index).setAlpha(alpha)
            "cero"      -> mixColors(Color(170, 0, 170),   Color(170, 255, 170), fadeSpeed, index).setAlpha(alpha)
            "azure"     -> mixColors(Color(0, 90, 255),    Color(0, 180, 255), fadeSpeed, index).setAlpha(alpha)
            "pumpkin"   -> mixColors(Color(255, 216, 169), Color(241, 166, 98), fadeSpeed, index).setAlpha(alpha)
            "polarized" -> mixColors(Color(0, 32, 64),     Color(173, 239, 209), fadeSpeed, index).setAlpha(alpha)
            "sundae"    -> mixColors(Color(28, 28, 27),    Color(206, 74, 126), fadeSpeed, index).setAlpha(alpha)
            "terminal"  -> mixColors(Color(25, 30, 25),    Color(15, 155, 15), fadeSpeed, index).setAlpha(alpha)
            "coral"     -> mixColors(Color(52, 133, 151),  Color(244, 168, 150), fadeSpeed, index).setAlpha(alpha)
            "fire"      -> mixColors(Color(255,45,30),     Color(255,123,15), fadeSpeed, index).setAlpha(alpha)
            "aqua"      -> mixColors(Color(80,255,255),    Color(80,190,255), fadeSpeed, index).setAlpha(alpha)
            "peony"     -> mixColors(Color(255,120,255),   Color(255,190,255), fadeSpeed, index).setAlpha(alpha)
            "vergren" -> mixColors(Color(170, 255, 169), Color(17, 255, 189), fadeSpeed, index).setAlpha(alpha)
            "eveningsunshine" -> mixColors(Color(185, 43, 39), Color(21, 101, 192), fadeSpeed, index).setAlpha(alpha)
            "lightorange" -> mixColors(Color(255, 183, 94), Color(237, 143, 3), fadeSpeed, index).setAlpha(alpha)
            "reef" -> mixColors(Color(0, 210, 255), Color(58, 123, 213), fadeSpeed, index).setAlpha(alpha)
            "amin" -> mixColors(Color(142, 45, 226), Color(74, 0, 224), fadeSpeed, index).setAlpha(alpha)
            "magics" -> mixColors(Color(89, 193, 115), Color(93, 38, 193), fadeSpeed, index).setAlpha(alpha)
            "mangopulp" -> mixColors(Color(240, 152, 25), Color(237, 222, 93), fadeSpeed, index).setAlpha(alpha)
            "moonpurple" -> mixColors(Color(78, 84, 200), Color(143, 148, 251), fadeSpeed, index).setAlpha(alpha)
            "aqualicious" -> mixColors(Color(80, 201, 195), Color(150, 222, 218), fadeSpeed, index).setAlpha(alpha)
            "stripe" -> mixColors(Color(31, 162, 255), Color(166, 255, 203), fadeSpeed, index).setAlpha(alpha)
            "shifter" -> mixColors(Color(188, 78, 156), Color(248, 7, 89), fadeSpeed, index).setAlpha(alpha)
            "quepal" -> mixColors(Color(17, 153, 142), Color(56, 239, 125), fadeSpeed, index).setAlpha(alpha)
            "orca" -> mixColors(Color(68, 160, 141), Color(9, 54, 55), fadeSpeed, index).setAlpha(alpha)
            "sublimevivid" -> mixColors(Color(252, 70, 107), Color(63, 94, 251), fadeSpeed, index).setAlpha(alpha)
            "moonasteroid" -> mixColors(Color(15, 32, 39), Color(44, 83, 100), fadeSpeed, index).setAlpha(alpha)
            "summerdog" -> mixColors(Color(168, 255, 120), Color(120, 255, 214), fadeSpeed, index).setAlpha(alpha)
            "pinkflavour" -> mixColors(Color(128, 0, 128), Color(255, 192, 203), fadeSpeed, index).setAlpha(alpha)
            "sincityred" -> mixColors(Color(237, 33, 58), Color(147, 41, 30), fadeSpeed, index).setAlpha(alpha)
            "timber" -> mixColors(Color(252, 0, 255), Color(0, 219, 222), fadeSpeed, index).setAlpha(alpha)
            "pinotnoir" -> mixColors(Color(75, 108, 183), Color(24, 40, 72), fadeSpeed, index).setAlpha(alpha)
            "dirtyfog" -> mixColors(Color(185, 147, 214), Color(140, 166, 219), fadeSpeed, index).setAlpha(alpha)
            "piglet" -> mixColors(Color(238, 156, 167), Color(255, 221, 225), fadeSpeed, index).setAlpha(alpha)
            "littleleaf" -> mixColors(Color(118, 184, 82), Color(141, 194, 111), fadeSpeed, index).setAlpha(alpha)
            "nelson" -> mixColors(Color(242, 112, 156), Color(255, 148, 114), fadeSpeed, index).setAlpha(alpha)
            "turquoiseflow" -> mixColors(Color(19, 106, 138), Color(38, 120, 113), fadeSpeed, index).setAlpha(alpha)
            "purplin" -> mixColors(Color(106, 48, 147), Color(160, 68, 255), fadeSpeed, index).setAlpha(alpha)
            "martini" -> mixColors(Color(253, 252, 71), Color(36, 254, 65), fadeSpeed, index).setAlpha(alpha)
            "soundcloud" -> mixColors(Color(254, 140, 0), Color(248, 54, 0), fadeSpeed, index).setAlpha(alpha)
            "inbox" -> mixColors(Color(69, 127, 202), Color(86, 145, 200), fadeSpeed, index).setAlpha(alpha)
            "amethyst" -> mixColors(Color(157, 80, 187), Color(110, 72, 170), fadeSpeed, index).setAlpha(alpha)
            "blush" -> mixColors(Color(178, 69, 146), Color(241, 95, 121), fadeSpeed, index).setAlpha(alpha)
            "mocharose" -> mixColors(Color(245, 194, 231), Color(243, 139, 168), fadeSpeed, index).setAlpha(alpha)

            "rainbow"   -> ColorUtils.skyRainbow(0, 1F, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
            "astolfo"   -> ColorUtils.skyRainbow(0, 0.6f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
            else        -> Color(-1)
        }
    }

    /**
     * Retrieve a color from [name] rather than the current mode,
     * typically used for e.g. theme previews or external calls.
     */
    fun getColorFromName(name: String, index: Int): Color {
        if (name.startsWith("#")) {
            return parseHexColor(name)
        }

        val fadeVal = ThemeFadeSpeed / 5.0 * if (updown) 1 else -1
        val lower = name.lowercase()

        val colorMap = mapOf(
            "zywl"      to Pair(Color(206, 58, 98),   Color(215, 171, 168)),
            "water"     to Pair(Color(35, 69, 148),   Color(108, 170, 207)),
            "magic"     to Pair(Color(255, 180, 255), Color(181, 139, 194)),
            "tree"      to Pair(Color(18, 155, 38),   Color(76, 255, 102)),
            "darknight" to Pair(Color(93, 95, 95),    Color(203, 200, 204)),
            "sun"       to Pair(Color(255, 143, 0),   Color(252, 205, 44)),
            "flower"    to Pair(Color(184, 85, 199),  Color(182, 140, 195)),
            "loyoi"     to Pair(Color(255, 131, 0),   Color(255, 131, 124)),
            "fdp"    to Pair(Color(255, 100, 255), Color(100, 255, 255)),
            "may"       to Pair(Color(255, 80, 255),  Color(255, 255, 255)),
            "mint"      to Pair(Color(85, 255, 140),  Color(85, 255, 255)),
            "cero"      to Pair(Color(170, 0, 170),   Color(170, 255, 170)),
            "azure"     to Pair(Color(0, 90, 255),    Color(0, 180, 255)),
            "pumpkin"   to Pair(Color(255, 216, 169), Color(241, 166, 98)),
            "polarized" to Pair(Color(0, 32, 64),     Color(173, 239, 209)),
            "sundae"    to Pair(Color(28, 28, 27),    Color(206, 74, 126)),
            "terminal"  to Pair(Color(25, 30, 25),    Color(15, 155, 15)),
            "coral"     to Pair(Color(52, 133, 151),  Color(244, 168, 150)),
            "fire"      to Pair(Color(255, 45, 30),   Color(255, 123, 15)),
            "aqua"      to Pair(Color(80,255,255),    Color(80,190,255)),
            "peony"     to Pair(Color(255,120,255),   Color(255,190,255)),
            "vergren"   to Pair(Color(170, 255, 169), Color(17, 255, 189)),
            "eveningsunshine" to Pair(Color(185, 43, 39), Color(21, 101, 192)),
            "lightorange"     to Pair(Color(255, 183, 94), Color(237, 143, 3)),
            "reef"             to Pair(Color(0, 210, 255), Color(58, 123, 213)),
            "amin"             to Pair(Color(142, 45, 226), Color(74, 0, 224)),
            "magics"            to Pair(Color(89, 193, 115), Color(93, 38, 193)),
            "mangopulp"       to Pair(Color(240, 152, 25), Color(237, 222, 93)),
            "moonpurple"      to Pair(Color(78, 84, 200), Color(143, 148, 251)),
            "aqualicious"      to Pair(Color(80, 201, 195), Color(150, 222, 218)),
            "stripe"           to Pair(Color(31, 162, 255), Color(166, 255, 203)),
            "shifter"          to Pair(Color(188, 78, 156), Color(248, 7, 89)),
            "quepal"           to Pair(Color(17, 153, 142), Color(56, 239, 125)),
            "orca"             to Pair(Color(68, 160, 141), Color(9, 54, 55)),
            "sublimevivid"    to Pair(Color(252, 70, 107), Color(63, 94, 251)),
            "moonasteroid"    to Pair(Color(15, 32, 39), Color(44, 83, 100)),
            "summerdog"       to Pair(Color(168, 255, 120), Color(120, 255, 214)),
            "pinkflavour"     to Pair(Color(128, 0, 128), Color(255, 192, 203)),
            "sincityred"     to Pair(Color(237, 33, 58), Color(147, 41, 30)),
            "timber"           to Pair(Color(252, 0, 255), Color(0, 219, 222)),
            "pinotnoir"       to Pair(Color(75, 108, 183), Color(24, 40, 72)),
            "dirtyfog"        to Pair(Color(185, 147, 214), Color(140, 166, 219)),
            "piglet"           to Pair(Color(238, 156, 167), Color(255, 221, 225)),
            "littleleaf"      to Pair(Color(118, 184, 82), Color(141, 194, 111)),
            "nelson"           to Pair(Color(242, 112, 156), Color(255, 148, 114)),
            "turquoiseflow"   to Pair(Color(19, 106, 138), Color(38, 120, 113)),
            "purplin"          to Pair(Color(106, 48, 147), Color(160, 68, 255)),
            "martini"          to Pair(Color(253, 252, 71), Color(36, 254, 65)),
            "soundcloud"       to Pair(Color(254, 140, 0), Color(248, 54, 0)),
            "inbox"            to Pair(Color(69, 127, 202), Color(86, 145, 200)),
            "amethyst"         to Pair(Color(157, 80, 187), Color(110, 72, 170)),
            "blush"            to Pair(Color(178, 69, 146), Color(241, 95, 121)),
            "mocharose"       to Pair(Color(245, 194, 231), Color(243, 139, 168)),
            "astolfo"   to Pair(
                ColorUtils.skyRainbow(0, 0.6F, 1F, 20000F / ThemeFadeSpeed),
                ColorUtils.skyRainbow(90, 0.6F, 1F, 20000F / ThemeFadeSpeed)
            ),
            "rainbow"   to Pair(
                ColorUtils.skyRainbow(0, 1F, 1F, 20000F / ThemeFadeSpeed),
                ColorUtils.skyRainbow(90, 1F, 1F, 20000F / ThemeFadeSpeed)
            )
        )

        val pair = colorMap[lower] ?: return Color(-1)

        return mixColors(pair.first, pair.second, fadeVal, index)
    }
}