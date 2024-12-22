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
 * A utility object for managing client theme colors,
 * supporting multiple color modes, animations, etc.,
 * now without custom delegated properties.
 */
object ClientThemesUtils {
    /**
     * The selected color mode (e.g., "Zywl", "Water", "Magic", etc.).
     * Previously delegated with `by choices(...)`.
     * Now a normal var with a default value "Soniga".
     */
    var ClientColorMode: String = "Soniga"
        set(value) {
            field = value.lowercase()
        }

    /**
     * Speed controlling fade or animation in certain color transitions.
     * Previously `by int(...) range = 1..10`.
     * Now a normal var with default=7.
     */
    var ThemeFadeSpeed: Int = 7
        set(value) {
            // If you want to clamp it to 1..10, do so here:
            field = value.coerceIn(1, 10)
        }

    /**
     * Up/down boolean toggling fade direction or type.
     * Previously `by boolean(...)`.
     * Now a normal var (default = false).
     */
    var updown: Boolean = false

    // ------------------------------------------------------------------------
    //                           PUBLIC COLOR METHODS
    // ------------------------------------------------------------------------

    /**
     * Returns a color depending on the [ClientColorMode], used for
     * "start" or "end" of a gradient (or similar).
     *
     * @param type  "START" or anything else => get second color
     * @param alpha transparency [0..255]
     */
    fun setColor(type: String, alpha: Int): Color {
        val mode = ClientColorMode.lowercase()
        return when (mode) {
            "zywl" ->
                if (type == "START") Color(215, 171, 168, alpha)
                else Color(206, 58, 98, alpha)

            "water" ->
                if (type == "START") Color(108, 170, 207, alpha)
                else Color(35, 69, 148, alpha)

            "magic" ->
                if (type == "START") Color(255, 180, 255, alpha)
                else Color(192, 67, 255, alpha)

            "darknight" ->
                if (type == "START") Color(203, 200, 204, alpha)
                else Color(93, 95, 95, alpha)

            "sun" ->
                if (type == "START") Color(252, 205, 44, alpha)
                else Color(255, 143, 0, alpha)

            "flower" ->
                if (type == "START") Color(182, 140, 195, alpha)
                else Color(184, 85, 199, alpha)

            "tree" ->
                if (type == "START") Color(76, 255, 102, alpha)
                else Color(18, 155, 38, alpha)

            "loyoi" ->
                if (type == "START") Color(255, 131, 124, alpha)
                else Color(255, 131, 0, alpha)

            "soniga" ->
                if (type == "START") Color(100, 255, 255, alpha)
                else Color(255, 100, 255, alpha)

            "may" ->
                if (type == "START") Color(255, 255, 255, alpha)
                else Color(255, 80, 255, alpha)

            "mint" ->
                if (type == "START") Color(85, 255, 255, alpha)
                else Color(85, 255, 140, alpha)

            "cero" ->
                if (type == "START") Color(170, 255, 170, alpha)
                else Color(170, 0, 170, alpha)

            "azure" ->
                if (type == "START") Color(0, 180, 255, alpha)
                else Color(0, 90, 255, alpha)

            "pumpkin" ->
                if (type == "START") Color(241, 166, 98, alpha)
                else Color(255, 216, 169, alpha)

            "polarized" ->
                if (type == "START") Color(173, 239, 209, alpha)
                else Color(0, 32, 64, alpha)

            "sundae" ->
                if (type == "START") Color(206, 74, 126, alpha)
                else Color(28, 28, 27, alpha)

            "terminal" ->
                if (type == "START") Color(15, 155, 15, alpha)
                else Color(25, 30, 25, alpha)

            "coral" ->
                if (type == "START") Color(244, 168, 150, alpha)
                else Color(52, 133, 151, alpha)

            "fire" ->
                if (type == "START") Color(255, 45, 30, alpha)
                else Color(255, 123, 15, alpha)

            "aqua" ->
                if (type == "START") Color(80, 255, 255, alpha)
                else Color(80, 190, 255, alpha)

            "peony" ->
                if (type == "START") Color(255, 120, 255, alpha)
                else Color(255, 190, 255, alpha)

            "astolfo" ->
                if (type == "START")
                    ColorUtils.skyRainbow(0, 0.6f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
                else
                    ColorUtils.skyRainbow(90, 0.6f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)

            "rainbow" ->
                if (type == "START")
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

        // Mapping mode -> Pair of Colors
        val colorMap = mapOf(
            "zywl"       to Pair(Color(206, 58, 98),   Color(215, 171, 168)),
            "water"      to Pair(Color(35, 69, 148),   Color(108, 170, 207)),
            "magic"      to Pair(Color(255, 180, 255), Color(181, 139, 194)),
            "tree"       to Pair(Color(18, 155, 38),   Color(76, 255, 102)),
            "darknight"  to Pair(Color(93, 95, 95),    Color(203, 200, 204)),
            "sun"        to Pair(Color(255, 143, 0),   Color(252, 205, 44)),
            "flower"     to Pair(Color(184, 85, 199),  Color(182, 140, 195)),
            "loyoi"      to Pair(Color(255, 131, 0),   Color(255, 131, 124)),
            "soniga"     to Pair(Color(255, 100, 255), Color(100, 255, 255)),
            "may"        to Pair(Color(255, 80, 255),  Color(255, 255, 255)),
            "mint"       to Pair(Color(85, 255, 140),  Color(85, 255, 255)),
            "cero"       to Pair(Color(170, 0, 170),   Color(170, 255, 170)),
            "azure"      to Pair(Color(0, 90, 255),    Color(0, 180, 255)),
            "pumpkin"    to Pair(Color(255, 216, 169), Color(241, 166, 98)),
            "polarized"  to Pair(Color(0, 32, 64),     Color(173, 239, 209)),
            "sundae"     to Pair(Color(28, 28, 27),    Color(206, 74, 126)),
            "terminal"   to Pair(Color(25, 30, 25),    Color(15, 155, 15)),
            "coral"      to Pair(Color(52, 133, 151),  Color(244, 168, 150)),
            "fire"       to Pair(Color(255, 45, 30),   Color(255, 123, 15)),
            "aqua"       to Pair(Color(80,255,255),    Color(80,190,255)),
            "peony"      to Pair(Color(255,120,255),   Color(255,190,255)),
            "astolfo"    to Pair(
                ColorUtils.skyRainbow(0, 0.6F, 1F, 20000F / ThemeFadeSpeed),
                ColorUtils.skyRainbow(90, 0.6F, 1F, 20000F / ThemeFadeSpeed)
            ),
            "rainbow"    to Pair(
                ColorUtils.skyRainbow(0, 1F, 1F, 20000F / ThemeFadeSpeed),
                ColorUtils.skyRainbow(90, 1F, 1F, 20000F / ThemeFadeSpeed)
            )
        )

        val colorPair = colorMap[mode] ?: return Color(-1)
        return mixColors(colorPair.first, colorPair.second, fadeVal, index)
    }

    /**
     * Retrieve a color from [name] rather than the current mode,
     * typically used for e.g. theme previews or external calls.
     */
    fun getColorFromName(name: String, index: Int): Color {
        val fadeSpeed = (ThemeFadeSpeed / 5.0) * if (updown) 1 else -1

        val colorMap = mapOf(
            "zywl"       to { f: Double -> mixColors(Color(206, 58, 98), Color(215, 171, 168), f, index) },
            "water"      to { f: Double -> mixColors(Color(35, 69, 148), Color(108, 170, 207), f, index) },
            "magic"      to { f: Double -> mixColors(Color(255, 180, 255), Color(181, 139, 194), f, index) },
            "tree"       to { f: Double -> mixColors(Color(18, 155, 38), Color(76, 255, 102), f, index) },
            "darknight"  to { f: Double -> mixColors(Color(93, 95, 95), Color(203, 200, 204), f, index) },
            "sun"        to { f: Double -> mixColors(Color(255, 143, 0), Color(252, 205, 44), f, index) },
            "flower"     to { f: Double -> mixColors(Color(184, 85, 199), Color(182, 140, 195), f, index) },
            "loyoi"      to { f: Double -> mixColors(Color(255, 131, 0), Color(255, 131, 124), f, index) },
            "soniga"     to { f: Double -> mixColors(Color(255, 100, 255), Color(100, 255, 255), f, index) },
            "may"        to { f: Double -> mixColors(Color(255, 80, 255), Color(255, 255, 255), f, index) },
            "mint"       to { f: Double -> mixColors(Color(85, 255, 140), Color(85, 255, 255), f, index) },
            "cero"       to { f: Double -> mixColors(Color(170, 0, 170), Color(170, 255, 170), f, index) },
            "azure"      to { f: Double -> mixColors(Color(0, 90, 255), Color(0, 180, 255), f, index) },
            "rainbow"    to { _: Double ->
                ColorUtils.skyRainbow(0, 1F, 1F, (20000F / fadeSpeed).toFloat())
            },
            "astolfo"    to { _: Double ->
                ColorUtils.skyRainbow(0, 0.6F, 1F, (20000F / fadeSpeed).toFloat())
            },
            "pumpkin"    to { f: Double -> mixColors(Color(255, 216, 169), Color(241, 166, 98), f, index) },
            "polarized"  to { f: Double -> mixColors(Color(0, 32, 64), Color(173, 239, 209), f, index) },
            "sundae"     to { f: Double -> mixColors(Color(28, 28, 27), Color(206, 74, 126), f, index) },
            "terminal"   to { f: Double -> mixColors(Color(25, 30, 25), Color(15, 155, 15), f, index) },
            "coral"      to { f: Double -> mixColors(Color(52, 133, 151), Color(244, 168, 150), f, index) },
            "fire"       to { f: Double -> mixColors(Color(255, 45, 30), Color(255, 123, 15), f, index) },
            "aqua"       to { f: Double -> mixColors(Color(80,255,255), Color(80,190,255), f, index) },
            "peony"      to { f: Double -> mixColors(Color(255,120,255), Color(255,190,255), f, index) },
        )

        val key = name.lowercase()
        val producer = colorMap[key] ?: return Color(-1)
        return producer(fadeSpeed)
    }

    /**
     * Like [getColor], but also sets the [alpha].
     */
    fun getColorWithAlpha(index: Int, alpha: Int): Color {
        val fadeSpeed = (ThemeFadeSpeed / 5.0) * if (updown) 1 else -1
        val mode = ClientColorMode.lowercase()

        return when (mode) {
            "zywl" -> mixColors(Color(206, 58, 98), Color(215, 171, 168), fadeSpeed, index).setAlpha(alpha)
            "water" -> mixColors(Color(35, 69, 148), Color(108, 170, 207), fadeSpeed, index).setAlpha(alpha)
            "magic" -> mixColors(Color(255, 180, 255), Color(181, 139, 194), fadeSpeed, index).setAlpha(alpha)
            "tree" -> mixColors(Color(18, 155, 38), Color(76, 255, 102), fadeSpeed, index).setAlpha(alpha)
            "darknight" -> mixColors(Color(93, 95, 95), Color(203, 200, 204), fadeSpeed, index).setAlpha(alpha)
            "sun" -> mixColors(Color(255, 143, 0), Color(252, 205, 44), fadeSpeed, index).setAlpha(alpha)
            "flower" -> mixColors(Color(184, 85, 199), Color(182, 140, 195), fadeSpeed, index).setAlpha(alpha)
            "loyoi" -> mixColors(Color(255, 131, 0), Color(255, 131, 124), fadeSpeed, index).setAlpha(alpha)
            "soniga" -> mixColors(Color(255, 100, 255), Color(100, 255, 255), fadeSpeed, index).setAlpha(alpha)
            "may" -> mixColors(Color(255, 80, 255), Color(255, 255, 255), fadeSpeed, index).setAlpha(alpha)
            "mint" -> mixColors(Color(85, 255, 180), Color(85, 255, 255), fadeSpeed, index).setAlpha(alpha)
            "cero" -> mixColors(Color(170, 0, 170), Color(170, 255, 170), fadeSpeed, index).setAlpha(alpha)
            "azure" -> mixColors(Color(0, 90, 255), Color(0, 180, 255), fadeSpeed, index).setAlpha(alpha)
            "rainbow" -> ColorUtils.skyRainbow(0, 1F, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
            "astolfo" -> ColorUtils.skyRainbow(0, 0.6f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha)
            "pumpkin" -> mixColors(Color(255, 216, 169), Color(241, 166, 98), fadeSpeed, index).setAlpha(alpha)
            "polarized" -> mixColors(Color(0, 32, 64), Color(173, 239, 209), fadeSpeed, index).setAlpha(alpha)
            "sundae" -> mixColors(Color(28, 28, 27), Color(206, 74, 126), fadeSpeed, index).setAlpha(alpha)
            "terminal" -> mixColors(Color(25, 30, 25), Color(15, 155, 15), fadeSpeed, index).setAlpha(alpha)
            "coral" -> mixColors(Color(52, 133, 151), Color(244, 168, 150), fadeSpeed, index).setAlpha(alpha)
            "fire" -> mixColors(Color(255,45,30), Color(255,123,15), fadeSpeed, index).setAlpha(alpha)
            "aqua" -> mixColors(Color(80,255,255), Color(80,190,255), fadeSpeed, index).setAlpha(alpha)
            "peony" -> mixColors(Color(255,120,255), Color(255,190,255), fadeSpeed, index).setAlpha(alpha)
            else -> Color(-1)
        }
    }
}