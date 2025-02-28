/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private fun ZipInputStream.entrySequence() = generateSequence { nextEntry }

fun File.extractZipTo(outputFolder: File, fileExtracted: (File) -> Unit = {}) {
    require(this.isFile) { "You can only extract from a file." }
    require(outputFolder.isDirectory) { "You can only extract zip to a directory." }

    outputFolder.apply {
        if (!exists()) mkdirs()
    }

    ZipInputStream(inputStream().buffered()).use { zis ->
        zis.entrySequence().forEach { entry ->
            val newFile = File(outputFolder, entry.name)

            if (!newFile.canonicalPath.startsWith(outputFolder.canonicalPath)) {
                throw SecurityException("Illegal Zip Entry：${entry.name}")
            }

            if (entry.isDirectory) {
                newFile.mkdirs()
            } else {
                newFile.parentFile?.mkdirs()
                newFile.outputStream().use { zis.copyTo(it) }
                fileExtracted(newFile)
            }
        }
    }
}

fun Collection<File>.zipFilesTo(outputZipFile: File) {
    ZipOutputStream(outputZipFile.outputStream().buffered()).use { zipOut ->
        this@zipFilesTo.forEach { file ->
            if (file.exists()) {
                val zipEntry = ZipEntry(file.name)
                zipOut.putNextEntry(zipEntry)

                file.inputStream().use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }
}