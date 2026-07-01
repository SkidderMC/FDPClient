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
            listOf("Legit", "Simple", "Jump", "Tick", "Reverse", "SmoothReverse")
        ),
        AnticheatProfile.NCP to Compatibility(
            listOf("Simple", "Legit", "Jump", "Tick", "Reverse"),
            listOf("SmoothReverse", "AttackReduce")
        ),
        AnticheatProfile.AAC to Compatibility(
            listOf("AAC", "AACPush", "AACv4", "AAC4Reduce", "AAC5Reduce"),
            listOf("AACZero", "AAC5.2.0", "AAC5.2.0Combat")
        ),
        AnticheatProfile.GRIM to Compatibility(
            listOf("Legit"),
            listOf("Jump", "Grim")
        ),
        AnticheatProfile.VULCAN to Compatibility(
            listOf("Vulcan", "Legit", "Jump", "Simple"),
            listOf("Tick", "Reverse")
        ),
        AnticheatProfile.WATCHDOG to Compatibility(listOf("Legit", "Jump"), listOf("Simple")),
        AnticheatProfile.VERUS to Compatibility(listOf("Legit", "Jump"), listOf("Tick", "Reverse")),
        AnticheatProfile.MATRIX to Compatibility(listOf("Legit", "Jump"), listOf("MatrixReduce")),
        AnticheatProfile.INTAVE to Compatibility(listOf("Legit", "Jump"), listOf("IntaveReduce", "Intave")),
        AnticheatProfile.SPARTAN to Compatibility(listOf("Legit", "Jump"), listOf("Simple")),
        AnticheatProfile.POLAR to Compatibility(listOf("Legit", "Jump")),
    )

    private val criticals = mapOf(
        AnticheatProfile.GENERIC to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("Packet", "Hop")
        ),
        AnticheatProfile.NCP to Compatibility(
            listOf("NCPPacket", "Jump", "LowJump"), listOf("Packet", "Hop")
        ),
        AnticheatProfile.AAC to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("Packet")
        ),
        AnticheatProfile.GRIM to Compatibility(
            listOf("Jump", "LowJump", "Visual")
        ),
        AnticheatProfile.VULCAN to Compatibility(
            listOf("Jump", "LowJump", "Visual"), listOf("Packet")
        ),
        AnticheatProfile.WATCHDOG to Compatibility(listOf("Jump", "Visual"), listOf("LowJump")),
        AnticheatProfile.VERUS to Compatibility(listOf("Jump", "LowJump", "Visual")),
        AnticheatProfile.MATRIX to Compatibility(listOf("Jump", "LowJump", "Visual")),
        AnticheatProfile.INTAVE to Compatibility(listOf("Jump", "Visual"), listOf("LowJump")),
        AnticheatProfile.SPARTAN to Compatibility(listOf("Jump", "LowJump", "Visual")),
        AnticheatProfile.POLAR to Compatibility(listOf("Jump", "Visual"), listOf("LowJump")),
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
        return ModeAdvice(profile, risk, compatibility.recommended.first())
    }

    private fun compatibility(feature: String, profile: AnticheatProfile): Compatibility =
        when (feature.lowercase(Locale.ROOT)) {
            "velocity" -> velocity.getValue(profile)
            "criticals" -> criticals.getValue(profile)
            else -> Compatibility(listOf("Legit"))
        }

    private fun profileFromName(name: String?): AnticheatProfile? {
        val normalized = name?.lowercase(Locale.ROOT)?.filter(Char::isLetterOrDigit) ?: return null
        return profileMatchers.firstNotNullOfOrNull { matcher -> matcher(normalized) }
    }
}
