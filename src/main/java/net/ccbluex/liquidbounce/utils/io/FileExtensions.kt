/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.Reader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private val parser = JsonParser()

/**
 * Write [text] to this file atomically: the content is fully written to a sibling
 * temporary file, flushed to disk, and only then moved over the target. This guarantees
 * that an interruption (crash, blue screen, IO contention while recording) can never leave
 * the destination file half-written and corrupt - the old content is kept until the new
 * content is complete.
 */
fun File.writeTextAtomic(text: String, charset: Charset = Charsets.UTF_8) {
    val parent = parentFile
    if (parent != null && !parent.exists()) parent.mkdirs()

    val tmp = File(parent, "$name.tmp")
    tmp.outputStream().use { out ->
        out.write(text.toByteArray(charset))
        out.flush()
        out.fd.sync()
    }

    try {
        Files.move(tmp.toPath(), toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    } catch (_: Exception) {
        // ATOMIC_MOVE is not supported on every filesystem - fall back to a plain replace.
        try {
            Files.move(tmp.toPath(), toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (_: Exception) {
            if (exists()) delete()
            if (!tmp.renameTo(this)) {
                writeText(text, charset)
                tmp.delete()
            }
        }
    } finally {
        if (tmp.exists()) tmp.delete()
    }
}

fun File.writeJson(content: JsonElement, gson: Gson = PRETTY_GSON) = writeTextAtomic(gson.toJson(content))

fun File.writeJson(content: Any?, gson: Gson = PRETTY_GSON) = writeTextAtomic(gson.toJson(content))

fun File.readJson(): JsonElement = bufferedReader().use { parser.parse(it) }

fun String.parseJson(): JsonElement = parser.parse(this)

fun Reader.readJson(): JsonElement = parser.parse(this)

fun File.sha256(): String = inputStream().use { DigestUtils.sha256Hex(it) }

val File.isEmpty: Boolean get() = length() == 0L