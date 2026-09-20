/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.util.Locale

/** Adds shared modern controls and the applicable native FDP controls without duplicating values. */
object NextGenHudVisualDefaults {

    fun enrich(definition: JsonObject) {
        val appender = Appender(definition)
        common(appender)
        componentDefaults[definition.get("name")?.asString]?.invoke(appender)
    }

    private fun common(v: Appender) = with(v) {
        float("Opacity", 1.0, 0.05, 1.0)
        float("ElementScale", 1.0, 0.25, 4.0)
        int("Padding", 0, 0, 24, "px")
        int("Radius", 5, 0, 24, "px")
        color("AccentColor", 0xff00bfffL)
        color("BackgroundColor", 0xb0101520L)
        color("TextColor", 0xffffffffL)
        bool("Shadow", true)
        bool("Blur", false)
    }

    private val componentDefaults: Map<String, Appender.() -> Unit> = mapOf(
        "ArmorItems" to { choice("Alignment", "Horizontal", "Horizontal", "Vertical"); bool("Attributes", true); bool("Enchant", true); bool("MinimalMode", false); int("AlertRepairReminderThreshold", 25, 0, 100); int("AlertDurabilityThreshold", 10, 0, 100) },
        "BlockCounter" to { bool("ScaffoldOnly", false); color("TextColor", 0xffffffffL); text("Font", "Inter"); bool("ShadowText", true) },
        "Effects" to { choice("Mode", "FDP", "FDP", "Classic", "Compact"); text("Font", "Inter"); bool("Icon", true); bool("Name", true); bool("DurationBar", true); choice("TitleAlign", "Left", "Left", "Center", "Right") },
        "Hotbar" to { bool("SmoothHotbarSlot", true); choice("HotbarColor", "Theme", "Custom", "Rainbow", "Gradient", "Theme"); color("HighlightColor", 0xff00bfffL); int("SlotSize", 45, 28, 64); bool("ShowStatus", true); bool("ShowItemName", true) },
        "Image" to { color("Color", 0xffffffffL); bool("ImageShadow", false); float("ShadowXDistance", 0.0, -20.0, 20.0); float("ShadowYDistance", 0.0, -20.0, 20.0); color("ShadowColor", 0x80000000L) },
        "Inventory" to { inventory("Inventory") },
        "CraftingInventory" to { inventory("CraftingInventory") },
        "EnderChestInventory" to { inventory("EnderChestInventory") },
        "InventoryStatistics" to { inventory("InventoryStatistics") },
        "KeyBinds" to { text("Title", "Hot Keys"); bool("ShowTitle", true); bool("Icon", true); color("KeyColor", 0xff00bfffL); int("MinWidth", 150, 80, 320) },
        "Keystrokes" to { bool("RenderBorder", true); float("BorderWidth", 1.0, 0.0, 5.0); float("ShrinkPercentage", 0.88, 0.5, 1.0); float("ShrinkSpeed", 0.15, 0.01, 1.0); bool("ShowMouseButtons", true); bool("ShowCPS", true); bool("TextShadow", true); text("Font", "Inter"); int("KeySize", 50, 24, 80) },
        "Notifications" to { choice("Background", "Blur", "None", "Color", "Blur"); color("CustomColor1", 0xff00bfffL); color("CustomColor2", 0xff8a2be2L); int("FadeDistance", 50, 0, 100); int("Duration", 3000, 500, 10000); choice("Position", "Right", "Left", "Right") },
        "Scoreboard" to { bool("Rect", true); color("RectangleColor", 0xb0101520L); bool("DrawRectOnTitle", true); color("TitleRectColor", 0xff00bfffL); bool("ServerIP", false); bool("Number", true); text("Font", "Inter") },
        "TabGui" to { bool("Border", true); float("BorderStrength", 1.0, 0.0, 5.0); color("BorderColor", 0xff00bfffL); bool("DisplayIcons", true); bool("Arrows", true); text("Font", "Inter"); bool("TextShadow", true); bool("UpperCase", false); int("Width", 100, 60, 220); int("TabHeight", 22, 14, 40) },
        "TargetHud" to { bool("MultiTarget", false); int("MaxTargets", 3, 1, 8); bool("OnlyPlayer", true); bool("ShowWhenChat", true); choice("Style", "Modern", "Modern", "Compact", "Classic"); choice("Animation", "Smooth", "Slide", "Smooth", "Fade"); float("AnimationSpeed", 0.2, 0.01, 1.0); float("HealthSpeed", 0.2, 0.01, 1.0); bool("ShowArmor", true); bool("ShowAvatar", true) },
        "Text" to { bool("ScaffoldOnly", false); bool("ShowBlock", false); choice("TextMode", "Custom", "Custom", "Theme", "Rainbow", "Gradient"); color("BackgroundColor", 0x00000000L); bool("BackgroundBorder", false); color("BackgroundBorderColor", 0xff00bfffL) },
    )

    private fun Appender.inventory(title: String) {
        text("Title", title)
        bool("ShowTitle", false)
        color("TitleColor", 0xffffffffL)
        bool("Border", false)
        color("BorderColor", 0xff00bfffL)
        int("SlotGap", 4, 0, 12)
        int("SlotSize", 32, 18, 54)
    }

    private class Appender(definition: JsonObject) {
        private val values = definition.getAsJsonArray("values") ?: JsonArray().also { definition.add("values", it) }
        private val existing = values.mapNotNull { it.takeIf(JsonElement::isJsonObject)?.asJsonObject?.get("name")?.asString }
            .map(::protocolName).toHashSet()

        fun bool(name: String, value: Boolean) = add(scalar("BOOLEAN", name, value))
        fun color(name: String, value: Long) = add(scalar("COLOR", name, value))
        fun text(name: String, value: String) = add(scalar("TEXT", name, value))

        fun choice(name: String, value: String, vararg choices: String) = add(scalar("CHOOSE", name, value).apply {
            add("choices", JsonArray().apply { choices.forEach { add(JsonPrimitive(it)) } })
        })

        fun int(name: String, value: Int, min: Int, max: Int, suffix: String = "") =
            add(number("INT", name, value, min to max, suffix))

        fun float(name: String, value: Double, min: Double, max: Double) =
            add(number("FLOAT", name, value, min to max, ""))

        private fun add(setting: JsonObject) {
            val name = setting.get("name")?.asString ?: return
            if (existing.add(protocolName(name))) values.add(setting)
        }

        private fun scalar(type: String, name: String, value: Any) = JsonObject().apply {
            addProperty("type", type)
            addProperty("name", name)
            when (value) {
                is Boolean -> addProperty("value", value)
                is Number -> addProperty("value", value)
                else -> addProperty("value", value.toString())
            }
        }

        private fun number(type: String, name: String, value: Number, range: Pair<Number, Number>, suffix: String) =
            scalar(type, name, value).apply {
                add("range", JsonObject().apply {
                    addProperty("min", range.first)
                    addProperty("max", range.second)
                })
                addProperty("suffix", suffix)
            }
    }

    private fun protocolName(name: String): String =
        name.filter(Char::isLetterOrDigit).replaceFirstChar { it.lowercase(Locale.ROOT) }
}
