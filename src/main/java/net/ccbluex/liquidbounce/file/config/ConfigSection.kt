package net.ccbluex.liquidbounce.file.config

import com.google.gson.JsonObject

abstract class ConfigSection(val sectionName: String) {
    abstract fun load(json: JsonObject)

    abstract fun save(): JsonObject
}