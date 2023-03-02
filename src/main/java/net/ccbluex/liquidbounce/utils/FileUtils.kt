package net.ccbluex.liquidbounce.utils

import org.apache.commons.io.IOUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream


object FileUtils {
    fun readInputStream(inputStream: InputStream?): String? {
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) stringBuilder.append(line).append('\n')
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    fun unpackFile(file: File, name: String) {
        val fos = FileOutputStream(file)
        IOUtils.copy(FileUtils::class.java.classLoader.getResourceAsStream(name), fos)
        fos.close()
    }
    fun writeFile(str:String,path: String){
        val file=File(path)
        file.writeText(str, Charsets.UTF_8)
    }
    fun extractZip(zipStream: InputStream, folder: File) {
        if (!folder.exists()) {
            folder.mkdir()
        }

        ZipInputStream(zipStream).use { zipInputStream ->
            var zipEntry = zipInputStream.nextEntry

            while (zipEntry != null) {
                if (zipEntry.isDirectory) {
                    zipEntry = zipInputStream.nextEntry
                    continue
                }

                val newFile = File(folder, zipEntry.name)
                File(newFile.parent).mkdirs()

                FileOutputStream(newFile).use {
                    zipInputStream.copyTo(it)
                }
                zipEntry = zipInputStream.nextEntry
            }

            zipInputStream.closeEntry()
        }
    }

    fun extractZip(zipFile: File, folder: File) = extractZip(FileInputStream(zipFile), folder)

    fun copyDir(fromDir: File, toDir: File) {
        if (!fromDir.exists()) {
            throw IllegalArgumentException("From dir not exists")
        }

        if (!fromDir.isDirectory || (toDir.exists() && !toDir.isDirectory)) {
            throw IllegalArgumentException("Arguments MUST be a directory")
        }

        if (!toDir.exists()) {
            toDir.mkdirs()
        }

        fromDir.listFiles().forEach {
            val toFile = File(toDir, it.name)
            if (it.isDirectory) {
                copyDir(it, toFile)
            } else {
                Files.copy(it.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}
