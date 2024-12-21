/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import java.io.File
import java.util.zip.ZipInputStream

private fun ZipInputStream.entrySequence() = generateSequence { nextEntry }

fun File.extractZipTo(outputFolder: File, fileExtracted: (File) -> Unit = {}) {
    require(this.isFile) { "You can only extract from a file." }
    require(outputFolder.isDirectory) { "You can only extract zip to a directory." }


    outputFolder.apply {
        if (!exists()) mkdirs()
    }

    ZipInputStream(inputStream()).use { zis ->
        zis.entrySequence().forEach { entry ->

            val newFile = File(outputFolder, entry.name)

            if (!newFile.canonicalPath.startsWith(outputFolder.canonicalPath)) {
                throw SecurityException("Illegal Zip Entry：${entry.name}")
            }
            if (entry.isDirectory) {
                newFile.mkdirs()
            } else {
                newFile.parentFile?.mkdirs()
                zis.copyTo(newFile.outputStream())
                fileExtracted(newFile)
            }
        }
    }
}