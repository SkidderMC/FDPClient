/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file

import com.google.gson.JsonObject

abstract class ConfigSection(val sectionName: String) {
    abstract fun load(json: JsonObject)

    abstract fun save(): JsonObject
}