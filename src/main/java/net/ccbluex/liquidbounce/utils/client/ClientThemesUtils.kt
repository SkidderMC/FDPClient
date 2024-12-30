/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
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
     * Now a normal var with a default value "Soniga".
     */
    var ClientColorMode: String = "FDP"
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

    /**
     * O modo de background selecionado. Pode ser "dark", "synced", "none", etc.
     * Ou diretamente "#RRGGBB" ou "#AARRGGBB".
     */
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

            "rainbow"   -> ColorUtils.skyRainbow(0, 1F, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
            "astolfo"   -> ColorUtils.skyRainbow(0, 0.6f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
            else        -> Color(-1)
        }
    }

    /**
     * Returns a color for the given [name], ignoring the global [ClientColorMode].
     * If [name] starts with "#", we parse it as a hex color. Otherwise, we use
     * the same color mapping used in [getColor].
     *
     * @param name e.g. "water", "rainbow", or "#FF00FF"
     * @param index typically used for color animations or color mixing
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