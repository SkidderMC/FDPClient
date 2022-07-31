package net.skiddermc.fdpclient.file

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.utils.FileUtils
import java.io.File

class LegalInclude {
    init {
        val legalFile = File(FDPClient.fileManager.legalDir, "LICENSE.txt")

        if (!legalFile.exists()) {
            FileUtils.unpackFile(legalFile, "assets/minecraft/fdpclient/misc/LICENSE.txt")
        }
    }
}