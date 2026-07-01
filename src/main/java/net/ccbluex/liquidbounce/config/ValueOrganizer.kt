/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

/**
 * Applies a deterministic, domain-based layout to legacy modules that still expose a long flat
 * setting list. Explicit module-authored groups always win; this is only the compatibility layer
 * for modules which have not declared any [Configurable] child themselves.
 *
 * Keeping this policy central avoids hundreds of copy-pasted `moveValues` blocks and also means
 * newly added legacy-style modules receive a usable ClickGUI layout automatically. Flat profiles
 * remain loadable through [ConfigMigration.findValue].
 */
object ValueOrganizer {

    private val pinnedNames = setOf("onlyingame", "hide", "reset", "bind")

    fun organize(configurable: Configurable, minimumValues: Int = 3): List<Configurable> {
        if (configurable.values.any { it is Configurable }) return emptyList()

        val candidates = configurable.values.filterNot { it.normalizedName() in pinnedNames || it.excluded }
        if (candidates.size < minimumValues) return emptyList()

        val indexed = candidates.mapIndexed(::IndexedValue)
        val buckets = indexed.groupBy { classify(it.value.name) }
        val groups = mutableListOf<GroupPlan>()
        val general = mutableListOf<IndexedValue>()

        for ((domain, entries) in buckets) {
            if (domain == Domain.GENERAL || entries.size < 2) {
                general += entries
            } else {
                groups += GroupPlan(domain.label, entries.map { it.value }, entries.minOf { it.index })
            }
        }

        if (general.size >= 2 || groups.isEmpty()) {
            groups += GroupPlan("General", general.map { it.value }, general.minOfOrNull { it.index } ?: 0)
        }

        return groups.sortedBy(GroupPlan::firstIndex).map { plan ->
            Configurable(plan.name).also { group ->
                plan.values.forEach(group::addValue)
                configurable.addValue(group)
            }
        }
    }

    private fun classify(name: String): Domain {
        val key = name.lowercase()
        return when {
            key.containsAny("color", "alpha", "rainbow", "font", "shadow", "outline", "glow",
                "render", "visual", "texture", "width", "height", "size", "scale") -> Domain.VISUALS
            key.containsAny("delay", "duration", "cooldown", "timeout", "tick", "speed", "cps",
                "interval", "fade", "time") -> Domain.TIMING
            key.containsAny("range", "distance", "radius", "reach", "fov", "angle") -> Domain.RANGE
            key.containsAny("motion", "jump", "strafe", "sprint", "sneak", "ground", "air",
                "velocity", "horizontal", "vertical", "yaw", "pitch") -> Domain.MOVEMENT
            key.startsWith("only") || key.startsWith("ignore") || key.startsWith("check") ||
                key.startsWith("require") || key.containsAny("condition", "whitelist", "blacklist",
                    "target", "enemy", "friend", "team") -> Domain.CONDITIONS
            key.containsAny("mode", "type", "style", "method", "algorithm") -> Domain.BEHAVIOR
            else -> Domain.GENERAL
        }
    }

    private fun String.containsAny(vararg fragments: String) = fragments.any(::contains)

    private fun Value<*>.normalizedName() = name.filter(Char::isLetterOrDigit).lowercase()

    private data class IndexedValue(val index: Int, val value: Value<*>)

    private data class GroupPlan(val name: String, val values: List<Value<*>>, val firstIndex: Int)

    private enum class Domain(val label: String) {
        GENERAL("General"),
        BEHAVIOR("Behavior"),
        CONDITIONS("Conditions"),
        RANGE("Range"),
        TIMING("Timing"),
        MOVEMENT("Movement"),
        VISUALS("Visuals"),
    }
}
