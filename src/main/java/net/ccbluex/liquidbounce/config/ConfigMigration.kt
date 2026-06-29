/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/** Versioned migration entry point plus flat-to-nested compatibility for grouped values. */
object ConfigMigration {
    const val CURRENT_VERSION = 1

    fun migrate(root: JsonObject, fromVersion: Int): JsonObject {
        var version = fromVersion.coerceAtLeast(0)
        while (version < CURRENT_VERSION) {
            when (version) {
                0 -> migrateInitialSchema(root)
            }
            version++
        }
        root.addProperty("ConfigVersion", CURRENT_VERSION)
        return root
    }

    /**
     * Returns a normal nested value when available. For a newly grouped configurable, constructs
     * the nested object from matching legacy flat keys so old profiles retain every setting.
     */
    fun findValue(moduleJson: JsonObject, value: Value<*>): JsonElement? {
        direct(moduleJson, value)?.let { return it }
        if (value !is Configurable) return null

        val migrated = JsonObject()
        for (child in value.values) {
            val childElement = findValue(moduleJson, child) ?: continue
            migrated.add(child.name, childElement)
        }
        return migrated.takeIf { it.entrySet().isNotEmpty() }
    }

    private fun direct(json: JsonObject, value: Value<*>): JsonElement? =
        json[value.name] ?: value.aliases.firstNotNullOfOrNull(json::get)

    private fun migrateInitialSchema(root: JsonObject) {
        // Version zero is the existing unversioned schema. Structural value migration is resolved
        // lazily by findValue because module classes own the authoritative value tree.
    }
}
