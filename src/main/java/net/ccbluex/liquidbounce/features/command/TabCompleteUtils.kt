/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.minecraft.block.Block
import net.minecraft.item.Item
import org.lwjgl.input.Keyboard

/**
 * Shared, crash-safe tab-completion helpers for FDP commands.
 *
 * Every helper:
 *  - never throws (world/player/registry access is null-guarded),
 *  - filters case-insensitively by the partial argument the user has typed,
 *  - returns a [List] suitable for returning straight out of [Command.tabComplete].
 *
 * Usage from a command:
 *   override fun tabComplete(args: Array<String>) = when (args.size) {
 *       1 -> TabCompleteUtils.modules(args[0])
 *       else -> emptyList()
 *   }
 */
object TabCompleteUtils {

    /**
     * Filter [options] keeping only those that start with [prefix] (case-insensitive).
     * Empty [prefix] returns everything. Always returns a fresh list.
     */
    fun match(prefix: String, options: Iterable<String>): List<String> =
        options.filter { it.startsWith(prefix, ignoreCase = true) }

    /**
     * Filter [options] keeping only those that start with [prefix] (case-insensitive).
     */
    fun match(prefix: String, vararg options: String): List<String> =
        match(prefix, options.asIterable())

    /**
     * All module names (unspaced, registry casing) matching [prefix].
     */
    fun modules(prefix: String): List<String> =
        match(prefix, moduleManager.map { it.name })

    /**
     * Leaf value names of the module identified by [moduleName] matching [prefix].
     * Returns empty if the module is unknown. Values are flattened out of their
     * [Configurable] groups so nested settings are offered directly; font values and
     * non-renderable values (unsupported, excluded or hidden) are filtered out so
     * suggestions only ever offer values the value command can actually set, matching
     * ModuleCommand behaviour. Names are emitted lowercase to mirror the value
     * command's own completion output.
     */
    fun moduleValues(moduleName: String, prefix: String): List<String> {
        val module = moduleManager[moduleName] ?: return emptyList()
        return match(prefix, leafValues(module.values).map { it.name.lowercase() })
    }

    private fun leafValues(values: Collection<Value<*>>): List<Value<*>> {
        val leaves = mutableListOf<Value<*>>()

        for (value in values) {
            if (value is FontValue || !value.shouldRender()) continue

            if (value is Configurable) {
                leaves += leafValues(value.values)
                continue
            }

            leaves += value
        }

        return leaves
    }

    /**
     * Online player names in the current world matching [prefix].
     * Safe to call outside a world (returns empty). Optionally excludes the local player.
     */
    fun players(prefix: String, includeSelf: Boolean = true): List<String> {
        val world = mc.theWorld ?: return emptyList()
        val self = mc.thePlayer
        return world.playerEntities
            .asSequence()
            .filter { includeSelf || it !== self }
            .mapNotNull { it.name }
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .distinct()
            .toList()
    }

    /**
     * Tab list player names (works even when entities are out of render range,
     * e.g. for friend/clan management). Safe outside a server.
     */
    fun tabListPlayers(prefix: String): List<String> {
        val handler = mc.netHandler ?: return emptyList()
        return handler.playerInfoMap
            .asSequence()
            .mapNotNull { it.gameProfile?.name }
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .distinct()
            .toList()
    }

    /**
     * Block registry names (resourcePath, lowercase) matching [prefix],
     * optionally filtered by a [predicate] on the resolved [Block].
     */
    fun blocks(prefix: String, predicate: (Block) -> Boolean = { true }): List<String> =
        Block.blockRegistry.keys
            .asSequence()
            .map { it.resourcePath.lowercase() }
            .filter { name ->
                val block = Block.getBlockFromName(name)
                block != null && Block.getIdFromBlock(block) > 0 && predicate(block)
            }
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .distinct()
            .toList()

    /**
     * Item registry names (resourcePath, lowercase) matching [prefix].
     */
    fun items(prefix: String): List<String> =
        Item.itemRegistry.keys
            .asSequence()
            .map { it.resourcePath.lowercase() }
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .distinct()
            .toList()

    /**
     * LWJGL key names (e.g. "F", "RSHIFT", "NUMPAD0") matching [prefix].
     * Useful for bind/macro key arguments. Always includes "none" to unbind.
     */
    fun keys(prefix: String): List<String> {
        val names = sequence {
            yield("NONE")
            for (key in 1 until Keyboard.KEYBOARD_SIZE) {
                val name = Keyboard.getKeyName(key)
                if (!name.isNullOrEmpty()) yield(name)
            }
        }
        return names
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .distinct()
            .toList()
    }
}
