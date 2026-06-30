/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import java.util.Locale

enum class AnticheatProfile(val displayName: String) {
    NCP("NCP"), AAC("AAC"), GRIM("Grim"), VULCAN("Vulcan"), GENERIC("Generic")
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
            listOf("Grim", "Legit", "Jump"),
            listOf("GrimVertical", "GrimC07", "GrimC03")
        ),
        AnticheatProfile.VULCAN to Compatibility(
            listOf("Vulcan", "Legit", "Jump", "Simple"),
            listOf("Tick", "Reverse")
        ),
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
        return when {
            normalized == "ncp" || "nocheatplus" in normalized || "watchdog" in normalized -> AnticheatProfile.NCP
            normalized.startsWith("aac") -> AnticheatProfile.AAC
            "grim" in normalized -> AnticheatProfile.GRIM
            "vulcan" in normalized || "oldvulcan" in normalized -> AnticheatProfile.VULCAN
            else -> null
        }
    }
}
