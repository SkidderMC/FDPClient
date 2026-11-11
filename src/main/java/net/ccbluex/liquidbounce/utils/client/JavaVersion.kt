/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.io.MiscUtils

data class JavaVersion(
    val raw: String, val major: Int, val minor: Int, val patch: String, val update: Int
) {
    companion object {
        const val DOWNLOAD_PAGE = "https://www.java.com/download/manual.jsp"
    }
}

private val JVM_VERSION_PATTERN = Regex("""(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:[_.](\d+))?(?:\.\d+)*(?:[-+].*)?""")

fun parseJavaVersion(raw: String): JavaVersion? {
    val match = JVM_VERSION_PATTERN.matchEntire(raw) ?: return null
    val parts = match.groupValues
    return JavaVersion(
        raw = raw,
        major = parts[1].toIntOrNull() ?: return null,
        minor = parts[2].toIntOrNull() ?: 0,
        patch = parts[3].ifEmpty { "0" },
        update = parts[4].toIntOrNull() ?: 0,
    )
}

/**
 * Check if the client is run on a proper JVM.
 */
val javaVersion by lazy {
    val javaVersion = System.getProperty("java.version")

    val parsed = parseJavaVersion(javaVersion)
    if (parsed == null) {
        ClientUtils.LOGGER.error("Failed to parse Java version $javaVersion")
        return@lazy null
    }
    parsed.also {
        // < Java 8, crash
        if (it.major == 1 && it.minor < 8) {
            MiscUtils.showURL(JavaVersion.DOWNLOAD_PAGE)
            error("You should start ${FDPClient.CLIENT_NAME} with Java 8! Get it from ${JavaVersion.DOWNLOAD_PAGE}")
        }
    }
}
