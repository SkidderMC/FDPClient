/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.manager

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.file.FileManager.deleteFile
import net.ccbluex.liquidbounce.file.FileManager.settingsDir
import net.ccbluex.liquidbounce.file.FileManager.writeFile
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory
import net.ccbluex.liquidbounce.utils.render.Pair
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

/**
 * @author opZywl - yzyGUI Manager
 */
class GUIManager {

    val positions = mutableMapOf<yzyCategory, Pair<Int, Int?>>()
    val extendeds = mutableMapOf<yzyCategory, Boolean>()

    private fun getCategoryFile(category: yzyCategory): File =
        File(settingsDir, "${category.name.lowercase(Locale.getDefault())}.yzygui")

    fun register() {
        yzyCategory.entries.forEach { category ->
            val categoryFile = getCategoryFile(category)
            if (categoryFile.exists()) {
                try {
                    FileReader(categoryFile).use { reader ->
                        val element = JsonParser().parse(reader)
                        if (element.isJsonObject) {
                            val `object` = element.asJsonObject

                            for ((key, value) in `object`.entrySet()) {
                                when (key) {
                                    "x" -> {
                                        val positionX = value.asInt

                                        positions[category] =
                                            Pair(positionX, null)
                                    }

                                    "y" -> {
                                        val positionY = value.asInt

                                        val positions =
                                            positions[category]!!

                                        positions.value = positionY
                                    }

                                    "extended" -> {
                                        val extended = value.asBoolean

                                        extendeds[category] = extended
                                    }
                                }
                            }
                        }
                    }
                } catch (exception: IOException) {
                    this.save(category)
                }
            }
        }
    }

    fun save(category: yzyCategory) {
        val categoryFile = getCategoryFile(category)
        deleteFile(categoryFile)
        try {
            if (categoryFile.createNewFile()) {
                val jsonObject = JsonObject().apply {
                    addProperty("x", positions[category]?.key ?: 0)
                    addProperty("y", positions[category]?.value)
                    addProperty("extended", extendeds[category] ?: false)
                }
                writeFile(categoryFile, PRETTY_GSON.toJson(jsonObject), true)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun save() {
        yzyCategory.entries.forEach { save(it) }
    }

    fun isExtended(category: yzyCategory): Boolean =
        extendeds[category] ?: false

    fun getPositions(category: yzyCategory): Pair<Int, Int?> =
        positions[category] ?: Pair(0, null)
}