/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import javax.swing.JOptionPane

private const val DOWNLOAD_PAGE = "https://www.java.com/download/manual.jsp"

/**
 * Check if the client is run on a proper JVM.
 */
fun checkJavaVersion() {
    val javaVersion = System.getProperty("java.version")

    val regex = Regex("""(\d+)(?:\.(\d+))?(?:\.(\d+))?_?(\d+)?""")
    val matchResult = regex.matchEntire(javaVersion)

    if (matchResult != null) {
        val (major, minor, patch, update) = matchResult.destructured

        when {
            // <= Java 8
            major == "1" -> when {
                // < Java 8, crash
                minor.toInt() < 8 -> {
                    MiscUtils.showURL(DOWNLOAD_PAGE)
                    error("You should start ${FDPClient.CLIENT_NAME} with Java 8! Get it from $DOWNLOAD_PAGE")
                }
                // < Java 8u100, warn
                update.toInt() < 100 -> {
                    SharedScopes.IO.launch {
                        MiscUtils.showMessageDialog(
                            title = "Warning",
                            message = "You are using an outdated version of Java 8 ($javaVersion).\n"
                                    + "This might cause unexpected bugs.\n"
                                    + "Please update it to 8u101+ or get a new one from $DOWNLOAD_PAGE.",
                            JOptionPane.WARNING_MESSAGE
                        )
                        MiscUtils.showURL(DOWNLOAD_PAGE)
                    }
                }
            }
            // > Java 8
            major.toInt() > 8 -> {
                SharedScopes.IO.launch {
                    MiscUtils.showMessageDialog(
                        title = "Warning",
                        message = "This version of ${FDPClient.CLIENT_NAME} is designed for Java 8 environment.\n"
                                + "Higher versions of Java might cause bug or crash.\n"
                                + "You can get JRE 8 from $DOWNLOAD_PAGE.",
                        JOptionPane.WARNING_MESSAGE
                    )
                    MiscUtils.showURL(DOWNLOAD_PAGE)
                }
            }
        }
    } else {
        // ???
        ClientUtils.LOGGER.error("Failed to parse Java version $javaVersion")
    }
}