package net.ccbluex.liquidbounce.file.configs

import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.utils.FDP4nt1Sk1dUtils
import java.io.File

class ScriptConfig(file: File) : FileConfig(file) {
    val subscripts = mutableListOf<Subscript>()

    override fun loadConfig(config: String) {
        clearSubscripts()
        config.split("\n").forEach { line ->
            if (line.contains(":")) {
                val data = line.split(":").toTypedArray()
                addSubscripts(FDP4nt1Sk1dUtils.decrypt(data[0]), FDP4nt1Sk1dUtils.decrypt(data[1]))
            } else {
                FDP4nt1Sk1dUtils.decrypt(line)
            }
        }
    }

    override fun saveConfig(): String {
        val builder = StringBuilder()

        for (subscript in subscripts)
            builder.append(FDP4nt1Sk1dUtils.encrypt(subscript.url)).append(":")
                .append(FDP4nt1Sk1dUtils.encrypt(subscript.name)).append("\n")

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