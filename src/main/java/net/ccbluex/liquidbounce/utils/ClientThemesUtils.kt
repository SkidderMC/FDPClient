/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils
import net.ccbluex.liquidbounce.utils.extensions.setAlpha
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.mixColors
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import java.awt.Color

object ClientThemesUtils {

    var ClientColorMode by ListValue(
        "ColorMode",
        arrayOf(
            "Zywl",
            "Water",
            "Magic",
            "DarkNight",
            "Sun",
            "Tree",
            "Flower",
            "Loyoi",
            "Soniga",
            "May",
            "Mint",
            "Cero",
            "Azure",
            "Rainbow",
            "Astolfo",
            "Pumpkin",
            "Polarized",
            "Sundae",
            "Terminal",
            "Coral",
            "Fire",
            "Aqua",
            "Peony"),
        "Soniga"
    ) { false }

    var textValue by BoolValue("TextStaticColor", false)

    var ThemeFadeSpeed by IntegerValue("Fade-speed", 7, 1..10)
    var updown by BoolValue("Fade-Type", false)

    fun setColor(type: String, alpha: Int): Color {
        val mode = ClientColorMode.lowercase()
        val color = when (mode) {
            "zywl" -> if (type == "START") Color(215, 171, 168, alpha) else Color(206, 58, 98, alpha)
            "water" -> if (type == "START") Color(108, 170, 207, alpha) else Color(35, 69, 148, alpha)
            "magic" -> if (type == "START") Color(255, 180, 255, alpha) else Color(192, 67, 255, alpha)
            "darknight" -> if (type == "START") Color(203, 200, 204, alpha) else Color(93, 95, 95, alpha)
            "sun" -> if (type == "START") Color(252, 205, 44, alpha) else Color(255, 143, 0, alpha)
            "flower" -> if (type == "START") Color(182, 140, 195, alpha) else Color(184, 85, 199, alpha)
            "tree" -> if (type == "START") Color(76, 255, 102, alpha) else Color(18, 155, 38, alpha)
            "loyoi" -> if (type == "START") Color(255, 131, 124, alpha) else Color(255, 131, 0, alpha)
            "soniga" -> if (type == "START") Color(100, 255, 255, alpha) else Color(255, 100, 255, alpha)
            "may" -> if (type == "START") Color(255, 255, 255, alpha) else Color(255, 80, 255, alpha)
            "mint" -> if (type == "START") Color(85, 255, 255, alpha) else Color(85, 255, 140, alpha)
            "cero" -> if (type == "START") Color(170, 255, 170, alpha) else Color(170, 0, 170, alpha)
            "azure" -> if (type == "START") Color(0, 180, 255, alpha) else Color(0, 90, 255, alpha)
            "pumpkin" -> if (type == "START") Color(241, 166, 98, alpha) else Color(255, 216, 169, alpha)
            "polarized" -> if (type == "START") Color(173, 239, 209, alpha) else Color(0, 32, 64, alpha)
            "sundae" -> if (type == "START") Color(206, 74, 126, alpha) else Color(28, 28, 27, alpha)
            "terminal" -> if (type == "START") Color(15, 155, 15, alpha) else Color(25, 30, 25, alpha)
            "coral" -> if (type == "START") Color(244, 168, 150, alpha) else Color(52, 133, 151, alpha)
            "fire" -> if (type == "START") Color(255, 45, 30, alpha) else Color(255, 123, 15, alpha)
            "aqua" -> if (type == "START") Color(80, 255, 255, alpha) else Color(80, 190, 255, alpha)
            "peony" -> if (type == "START") Color(255, 120, 255, alpha) else Color(255, 190, 255, alpha)
            "astolfo" -> if (type == "START") ColorUtils.skyRainbow(0, 0.6f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha) else ColorUtils.skyRainbow(90, 0.6f, 1F,20000F / ThemeFadeSpeed).setAlpha(alpha)
            "rainbow" -> if (type == "START") ColorUtils.skyRainbow(0, 1f, 1f, 20000F / ThemeFadeSpeed).setAlpha(alpha) else ColorUtils.skyRainbow(90, 1f, 1F,20000F / ThemeFadeSpeed).setAlpha(alpha)
            else -> Color(-1)
        }
        return color
    }
    fun getColor(index: Int = 0): Color {
        val mode = ClientColorMode.lowercase()
        val colorMap = mapOf(
            "zywl" to Pair(Color(206, 58, 98), Color(215, 171, 168)),
            "water" to Pair(Color(35, 69, 148), Color(108, 170, 207)),
            "magic" to Pair(Color(255, 180, 255), Color(181, 139, 194)),
            "tree" to Pair(Color(18, 155, 38), Color(76, 255, 102)),
            "darknight" to Pair(Color(93, 95, 95), Color(203, 200, 204)),
            "sun" to Pair(Color(255, 143, 0), Color(252, 205, 44)),
            "flower" to Pair(Color(184, 85, 199), Color(182, 140, 195)),
            "loyoi" to Pair(Color(255, 131, 0), Color(255, 131, 124)),
            "soniga" to Pair(Color(255, 100, 255), Color(100, 255, 255)),
            "may" to Pair(Color(255, 80, 255), Color(255, 255, 255)),
            "mint" to Pair(Color(85, 255, 140), Color(85, 255, 255)),
            "cero" to Pair(Color(170, 0, 170), Color(170, 255, 170)),
            "azure" to Pair(Color(0, 90, 255), Color(0, 180, 255)),
            "pumpkin" to Pair(Color(255, 216, 169), Color(241, 166, 98)),
            "polarized" to Pair(Color(0, 32, 64), Color(173, 239, 209)),
            "sundae" to Pair(Color(28, 28, 27), Color(206, 74, 126)),
            "terminal" to Pair(Color(25, 30, 25), Color(15, 155, 15)),
            "coral" to Pair(Color(52, 133, 151), Color(244, 168, 150)),
            "fire" to Pair(Color(255,45,30), Color(255,123,15)),
            "aqua" to Pair(Color(80,255,255), Color(80,190,255)),
            "peony" to Pair(Color(255,120,255), Color(255,190,255)),
            "astolfo" to Pair(ColorUtils.skyRainbow(0, 0.6F, 1F, 20000F / ThemeFadeSpeed), ColorUtils.skyRainbow(90, 0.6F, 1F, 20000F / ThemeFadeSpeed)),
            "rainbow" to Pair(ColorUtils.skyRainbow(0, 1F, 1F, 20000F / ThemeFadeSpeed), ColorUtils.skyRainbow(90, 1F, 1F, 20000F / ThemeFadeSpeed))
        )

        val colorPair = colorMap[mode]
        return if (colorPair != null) {
            mixColors(
                colorPair.first,
                colorPair.second,
                ThemeFadeSpeed / 5.0 * if (updown) 1 else -1,
                index
            )
        } else {
            Color(-1)
        }
    }

    fun getColorFromName(name: String, index: Int): Color {
        val colorMap = mapOf<String, (Double) -> Color>(
            "zywl" to { fadeSpeed -> mixColors(Color(206, 58, 98), Color(215, 171, 168), fadeSpeed, index) },
            "water" to { fadeSpeed -> mixColors(Color(35, 69, 148), Color(108, 170, 207), fadeSpeed, index) },
            "magic" to { fadeSpeed -> mixColors(Color(255, 180, 255), Color(181, 139, 194), fadeSpeed, index) },
            "tree" to { fadeSpeed -> mixColors(Color(18, 155, 38), Color(76, 255, 102), fadeSpeed, index) },
            "darknight" to { fadeSpeed -> mixColors(Color(93, 95, 95), Color(203, 200, 204), fadeSpeed, index) },
            "sun" to { fadeSpeed -> mixColors(Color(255, 143, 0), Color(252, 205, 44), fadeSpeed, index) },
            "flower" to { fadeSpeed -> mixColors(Color(184, 85, 199), Color(182, 140, 195), fadeSpeed, index) },
            "loyoi" to { fadeSpeed -> mixColors(Color(255, 131, 0), Color(255, 131, 124), fadeSpeed, index) },
            "soniga" to { fadeSpeed -> mixColors(Color(255, 100, 255), Color(100, 255, 255), fadeSpeed, index) },
            "may" to { fadeSpeed -> mixColors(Color(255, 80, 255), Color(255, 255, 255), fadeSpeed, index) },
            "mint" to { fadeSpeed -> mixColors(Color(85, 255, 140), Color(85, 255, 255), fadeSpeed, index) },
            "cero" to { fadeSpeed -> mixColors(Color(170, 0, 170), Color(170, 255, 170), fadeSpeed, index) },
            "azure" to { fadeSpeed -> mixColors(Color(0, 90, 255), Color(0, 180, 255), fadeSpeed, index) },
            "rainbow" to { fadeSpeed -> ColorUtils.skyRainbow(0, 1F, 1F, (20000F / fadeSpeed).toFloat()) },
            "astolfo" to { fadeSpeed -> ColorUtils.skyRainbow(0, 0.6F, 1F, (20000F / fadeSpeed).toFloat()) },
            "pumpkin" to { fadeSpeed -> mixColors(Color(255, 216, 169), Color(241, 166, 98), fadeSpeed, index) },
            "polarized" to { fadeSpeed -> mixColors(Color(0, 32, 64), Color(173, 239, 209), fadeSpeed, index) },
            "sundae" to { fadeSpeed -> mixColors(Color(28, 28, 27), Color(206, 74, 126), fadeSpeed, index) },
            "terminal" to { fadeSpeed -> mixColors(Color(25, 30, 25), Color(15, 155, 15), fadeSpeed, index) },
            "coral" to { fadeSpeed -> mixColors(Color(52, 133, 151), Color(244, 168, 150), fadeSpeed, index) },
            "fire" to { fadeSpeed -> mixColors(Color(255,45,30), Color(255,123,15), fadeSpeed, index) },
            "aqua" to { fadeSpeed -> mixColors(Color(80,255,255), Color(80,190,255), fadeSpeed, index) },
            "peony" to { fadeSpeed -> mixColors(Color(255,120,255), Color(255,190,255), fadeSpeed, index) },
        )

        val fadeSpeed = ThemeFadeSpeed / 5.0 * if (updown) 1 else -1
        return colorMap[name.lowercase()]?.invoke(fadeSpeed) ?: Color(-1)
    }
    fun getColorWithAlpha(index: Int, alpha: Int): Color {
        val fadeSpeed = ThemeFadeSpeed / 5.0 * if (updown) 1 else -1

        return when (ClientColorMode.lowercase()) {
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