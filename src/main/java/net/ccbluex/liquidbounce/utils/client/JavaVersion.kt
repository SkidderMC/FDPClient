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

/**
 * Check if the client is run on a proper JVM.
 */
val javaVersion by lazy {
    val javaVersion = System.getProperty("java.version")

    val regex = Regex("""(\d+)(?:\.(\d+))?(?:\.(\d+))?_?(\d+)?""")

    try {
        val matchResult = regex.matchEntire(javaVersion)!! // NPE

        val (major, minor, patch, update) = matchResult.destructured

        JavaVersion(javaVersion, major.toInt(), minor.toInt(), patch, update.toInt())
    } catch (e: Exception) {
        // ???
        ClientUtils.LOGGER.error("Failed to parse Java version $javaVersion")
        return@lazy null
    }.also {
        // < Java 8, crash
        if (it.major == 1 && it.minor < 8) {
            MiscUtils.showURL(JavaVersion.DOWNLOAD_PAGE)
            error("You should start ${FDPClient.CLIENT_NAME} with Java 8! Get it from ${JavaVersion.DOWNLOAD_PAGE}")
        }
    }
}