package net.ccbluex.liquidbounce.file

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

class LegalInclude {
    init {
        val legalFile = File(LiquidBounce.fileManager.legalDir, "LICENSE.txt")

        if (!legalFile.exists()) {
            FileUtils.unpackFile(legalFile, "assets/minecraft/fdpclient/misc/LICENSE.txt")
        }
    }
}