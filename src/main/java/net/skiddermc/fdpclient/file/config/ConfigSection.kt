package net.skiddermc.fdpclient.file.config

import com.google.gson.JsonObject

abstract class ConfigSection(val sectionName: String) {
    abstract fun load(json: JsonObject)

    abstract fun save(): JsonObject
}