package net.ccbluex.liquidbounce.utils

import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption


object FileUtils {
    @JvmStatic
    fun unpackFile(file: File, name: String) {
        val fos = FileOutputStream(file)
        IOUtils.copy(FileUtils::class.java.classLoader.getResourceAsStream(name), fos)
        fos.close()
    }

    @JvmStatic
    fun downloadFile(file: File, url: URL) {
        ClientUtils.logWarn("Downloading $url to $file")
        Files.copy(url.openStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}