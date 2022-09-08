package net.ccbluex.liquidbounce.file.configs

import net.ccbluex.liquidbounce.file.FileConfig
import java.io.File
import java.nio.charset.Charset
import java.util.*

class ScriptConfig(file: File) : FileConfig(file) {
    val subscripts = mutableListOf<Subscript>()

    override fun loadConfig(config: String) {
        clearSubscripts()
        config.split("\n").forEach { line ->
            if (line.contains(":")) {
                val data = line.split(":").toTypedArray()
                addSubscripts(Base64.getDecoder().decode(data[0]).toString(Charset.defaultCharset()), Base64.getDecoder().decode(data[1]).toString(Charset.defaultCharset()))
            } else {
                Base64.getDecoder().decode(line).toString(Charset.defaultCharset())
            }
        }
    }

    override fun saveConfig(): String {
        val builder = StringBuilder()

        for (subscript in subscripts)
            builder.append(Base64.getEncoder().encode(subscript.url.toByteArray())).append(":")
                .append(Base64.getEncoder().encode(subscript.name.toByteArray())).append("\n")

        return builder.toString()
    }

    @JvmOverloads
    fun addSubscripts(url: String, name: String = url): Boolean {
        if (isSubscript(url)) {
            return false
        }
        subscripts.add(Subscript(url, name))
        return true
    }

    fun removeSubscript(url: String): Boolean {
        if (!isSubscript(url)) return false
        subscripts.removeIf { friend: Subscript -> friend.url == url }
        return true
    }

    fun isSubscript(url: String): Boolean {
        for (subscript in subscripts) if (subscript.url == url) return true
        return false
    }

    fun clearSubscripts() {
        subscripts.clear()
    }

    class Subscript(val url: String, val name: String)
}