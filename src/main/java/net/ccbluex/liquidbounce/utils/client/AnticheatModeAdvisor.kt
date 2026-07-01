/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import java.util.Locale

enum class AnticheatProfile(val displayName: String) {
    NCP("NCP"), AAC("AAC"), GRIM("Grim"), VULCAN("Vulcan"), WATCHDOG("Watchdog"),
    VERUS("Verus"), MATRIX("Matrix"), INTAVE("Intave"), SPARTAN("Spartan"), POLAR("Polar"),
    GENERIC("Generic")
}

enum class ModeRisk {
    RECOMMENDED, EXPERIMENTAL, LIKELY_DETECTED
}

data class ModeAdvice(
    val profile: AnticheatProfile,
    val risk: ModeRisk,
    val recommendedMode: String,
)

/** Curated compatibility data. It deliberately does not label legacy packet tricks as bypasses. */
object AnticheatModeAdvisor {

    private val profileMatchers = listOf<(String) -> AnticheatProfile?>(
        { value -> AnticheatProfile.NCP.takeIf { value == "ncp" || "nocheatplus" in value } },
        { value -> AnticheatProfile.AAC.takeIf { value.startsWith("aac") } },
        { value -> AnticheatProfile.GRIM.takeIf { "grim" in value } },
        { value -> AnticheatProfile.VULCAN.takeIf { "vulcan" in value } },
        { value -> AnticheatProfile.WATCHDOG.takeIf { "watchdog" in value || "hypixel" in value } },
        { value -> AnticheatProfile.VERUS.takeIf { "verus" in value } },
        { value -> AnticheatProfile.MATRIX.takeIf { "matrix" in value } },
        { value -> AnticheatProfile.INTAVE.takeIf { "intave" in value } },
        { value -> AnticheatProfile.SPARTAN.takeIf { "spartan" in value } },
        { value -> AnticheatProfile.POLAR.takeIf { "polar" in value } },
    )

    private data class Compatibility(
        val recommended: List<String>,
        val experimental: List<String> = emptyList(),
    )

    private val velocity = mapOf(
        AnticheatProfile.GENERIC to Compatibility(
            listOf("Legit", "Simple", "Jump", "Tick", "Reverse", "SmoothReverse"),
            listOf("Delay", "Delayed", "Cancel", "Spoof")
        ),
        AnticheatProfile.NCP to Compatibility(
            listOf("Simple", "Legit", "Jump", "Tick", "Reverse"),
            listOf("SmoothReverse", "AttackReduce", "S32Packet", "Delay")
        ),
        AnticheatProfile.AAC to Compatibility(
            listOf("AAC", "AACPush", "AACv4", "AAC4Reduce", "AAC5Reduce"),
            listOf("AACZero", "AAC5.2.0", "AAC5.2.0Combat", "Legit")
        ),
        AnticheatProfile.GRIM to Compatibility(
            listOf("Grim", "GrimC03", "Legit"),
            listOf("Grim1.17", "GrimC07", "GrimVertical", "GrimDamage", "Minemen", "Sentinel", "Jump")
        ),
        AnticheatProfile.VULCAN to Compatibility(
            listOf("Vulcan", "Legit", "Jump", "Simple"),
            listOf("Tick", "Reverse", "BlocksMC", "GhostBlock")
        ),
        AnticheatProfile.WATCHDOG to Compatibility(
            listOf("Hypixel", "HypixelAir", "Legit"),
            listOf("HypixelBoost", "Jump")
        ),
        AnticheatProfile.VERUS to Compatibility(
            listOf("Legit", "Jump"),
            listOf("Tick", "Reverse", "Spoof")
        ),
        AnticheatProfile.MATRIX to Compatibility(
            listOf("MatrixReduce", "MatrixSimple", "Legit"),
            listOf("MatrixReverse", "Jump")
        ),
        AnticheatProfile.INTAVE to Compatibility(
            listOf("IntaveReduce", "Intave", "Legit"),
            listOf("Jump")
        ),
        AnticheatProfile.SPARTAN to Compatibility(
            listOf("Legit", "Jump", "Simple"),
            listOf("Tick")
        ),
        AnticheatProfile.POLAR to Compatibility(
            listOf("Polar", "Legit", "Jump"),
            listOf("SideStrafe")
        ),
    )

    private val criticals = mapOf(
        AnticheatProfile.GENERIC to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("Packet", "Hop", "MiniJump")
        ),
        AnticheatProfile.NCP to Compatibility(
            listOf("NCPPacket", "Jump", "LowJump"), listOf("Packet", "Hop", "TPHop")
        ),
        AnticheatProfile.AAC to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("Packet", "MiniJump")
        ),
        AnticheatProfile.GRIM to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("NoGround")
        ),
        AnticheatProfile.VULCAN to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("Packet", "BlocksMC", "BlocksMC2")
        ),
        AnticheatProfile.WATCHDOG to Compatibility(
            listOf("Jump", "Visual"), listOf("LowJump", "MiniJump")
        ),
        AnticheatProfile.VERUS to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("CustomMotion")
        ),
        AnticheatProfile.MATRIX to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("NoGround")
        ),
        AnticheatProfile.INTAVE to Compatibility(listOf("Jump", "Visual"), listOf("LowJump")),
        AnticheatProfile.SPARTAN to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("MiniJump")
        ),
        AnticheatProfile.POLAR to Compatibility(listOf("Jump", "Visual"), listOf("LowJump")),
    )

    private val autoblock = mapOf(
        AnticheatProfile.GENERIC to Compatibility(listOf("Packet"), listOf("Fake")),
        AnticheatProfile.NCP to Compatibility(listOf("Packet"), listOf("Fake")),
        AnticheatProfile.AAC to Compatibility(listOf("Fake"), listOf("Packet")),
        AnticheatProfile.GRIM to Compatibility(listOf("Fake"), listOf("Packet")),
        AnticheatProfile.VULCAN to Compatibility(listOf("Fake"), listOf("Packet")),
        AnticheatProfile.WATCHDOG to Compatibility(listOf("Fake"), listOf("Packet")),
        AnticheatProfile.VERUS to Compatibility(listOf("Fake"), listOf("Packet")),
        AnticheatProfile.MATRIX to Compatibility(listOf("Fake"), listOf("Packet")),
        AnticheatProfile.INTAVE to Compatibility(listOf("Fake")),
        AnticheatProfile.SPARTAN to Compatibility(listOf("Packet"), listOf("Fake")),
        AnticheatProfile.POLAR to Compatibility(listOf("Fake"), listOf("Packet")),
    )

    private val autoclicker = mapOf(
        AnticheatProfile.GENERIC to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.NCP to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.AAC to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.GRIM to Compatibility(listOf("Legacy"), listOf("Modern")),
        AnticheatProfile.VULCAN to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.WATCHDOG to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.VERUS to Compatibility(listOf("Legacy"), listOf("Modern")),
        AnticheatProfile.MATRIX to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.INTAVE to Compatibility(listOf("Legacy"), listOf("Modern")),
        AnticheatProfile.SPARTAN to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.POLAR to Compatibility(listOf("Legacy"), listOf("Modern")),
    )

    private val backtrack = mapOf(
        AnticheatProfile.GENERIC to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.NCP to Compatibility(listOf("Legacy"), listOf("Modern")),
        AnticheatProfile.AAC to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.GRIM to Compatibility(emptyList(), listOf("Modern", "Legacy")),
        AnticheatProfile.VULCAN to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.WATCHDOG to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.VERUS to Compatibility(emptyList(), listOf("Modern", "Legacy")),
        AnticheatProfile.MATRIX to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.INTAVE to Compatibility(emptyList(), listOf("Legacy", "Modern")),
        AnticheatProfile.SPARTAN to Compatibility(listOf("Modern"), listOf("Legacy")),
        AnticheatProfile.POLAR to Compatibility(emptyList(), listOf("Modern", "Legacy")),
    )

    fun resolve(selection: String, observed: String?): AnticheatProfile {
        if (!selection.equals("Auto", true) && !selection.equals("All", true)) {
            return profileFromName(selection) ?: AnticheatProfile.GENERIC
        }
        return profileFromName(observed) ?: AnticheatProfile.GENERIC
    }

    fun filteredModes(feature: String, selection: String, observed: String?, allModes: Array<String>): Array<String> {
        if (selection.equals("All", true)) return allModes.copyOf()
        if (selection.equals("Auto", true) && profileFromName(observed) == null) return allModes.copyOf()

        val compatibility = compatibility(feature, resolve(selection, observed))
        val allowed = compatibility.recommended + compatibility.experimental
        return allowed.mapNotNull { candidate -> allModes.firstOrNull { it.equals(candidate, true) } }.toTypedArray()
            .takeIf(Array<String>::isNotEmpty) ?: allModes.copyOf()
    }

    fun assess(feature: String, mode: String, selection: String, observed: String?): ModeAdvice {
        val profile = resolve(selection, observed)
        val compatibility = compatibility(feature, profile)
        val risk = when {
            compatibility.recommended.any { it.equals(mode, true) } -> ModeRisk.RECOMMENDED
            compatibility.experimental.any { it.equals(mode, true) } -> ModeRisk.EXPERIMENTAL
            else -> ModeRisk.LIKELY_DETECTED
        }
        val fallback = compatibility.recommended.firstOrNull()
            ?: compatibility.experimental.firstOrNull()
            ?: mode
        return ModeAdvice(profile, risk, fallback)
    }

    /** Best-known safe mode for a feature under the resolved profile, or null when unmapped. */
    fun bestMode(feature: String, selection: String, observed: String?): String? {
        val compatibility = compatibility(feature, resolve(selection, observed))
        return compatibility.recommended.firstOrNull()
    }

    /** Every RECOMMENDED mode for the resolved profile, most-trusted first. */
    fun recommendedModes(feature: String, selection: String, observed: String?): List<String> =
        compatibility(feature, resolve(selection, observed)).recommended

    /** True when a mode is graded RECOMMENDED for the resolved profile. */
    fun isRecommendedMode(feature: String, mode: String, selection: String, observed: String?): Boolean =
        assess(feature, mode, selection, observed).risk == ModeRisk.RECOMMENDED

    /** True when a mode is graded LIKELY_DETECTED for the resolved profile. */
    fun isRiskyMode(feature: String, mode: String, selection: String, observed: String?): Boolean =
        assess(feature, mode, selection, observed).risk == ModeRisk.LIKELY_DETECTED

    private fun compatibility(feature: String, profile: AnticheatProfile): Compatibility =
        when (feature.lowercase(Locale.ROOT)) {
            "velocity" -> velocity.getValue(profile)
            "criticals" -> criticals.getValue(profile)
            "autoblock" -> autoblock.getValue(profile)
            "autoclicker" -> autoclicker.getValue(profile)
            "backtrack" -> backtrack.getValue(profile)
            else -> Compatibility(listOf("Legit"))
        }

    private fun profileFromName(name: String?): AnticheatProfile? {
        val normalized = name?.lowercase(Locale.ROOT)?.filter(Char::isLetterOrDigit) ?: return null
        return profileMatchers.firstNotNullOfOrNull { matcher -> matcher(normalized) }
    }
}
