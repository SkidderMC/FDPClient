/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.other.UnlimitedValues
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.kotlin.StringUtils
import net.minecraft.block.Block
import net.minecraft.item.Item
import org.lwjgl.input.Keyboard

/**
 * Module command
 *
 * @author opZywl
 */
class ModuleCommand(val module: Module, val values: Collection<Value<*>> = module.values) :
    Command(module.name.lowercase()) {

    init {
        if (values.isEmpty())
            throw IllegalArgumentException("Values are empty!")
    }

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val leaves = leafValues(values)
        val valueNames = leaves.joinToString(separator = "/") { it.name.lowercase() }

        val moduleName = module.name.lowercase()

        if (args.size < 2) {
            chatSyntax(if (leaves.size == 1) "$moduleName $valueNames <value>" else "$moduleName <$valueNames>")
            return
        }

        when (val value = module[args[1]]) {
            null -> chatSyntax("$moduleName <$valueNames>")

            is BoolValue -> {
                if (args.size != 2) {
                    chatSyntax("$moduleName ${args[1].lowercase()}")
                    return
                }

                val newValue = !value.get()
                value.set(newValue)

                chat("§7${module.getName()} §8${args[1]}§7 was toggled ${if (newValue) "§8on§7" else "§8off§7" + "."}")
                playEdit()
            }

            is Configurable -> {
                chat("§7${module.getName()} §8${value.name}§7:")
                listValues(value.values, 1)
                return
            }

            else -> {
                if (if (value is TextValue) args.size < 3 else args.size != 3) {
                    when (value) {
                        is IntValue, is LongValue, is FloatValue, is DoubleValue, is TextValue, is FileValue -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <value>")
                        }

                        is ListValue -> {
                            chatSyntax(
                                "$moduleName ${args[1].lowercase()} <${
                                    value.values.joinToString(separator = "/").lowercase()
                                }>"
                            )
                        }

                        is MultiSelectValue -> {
                            chatSyntax(
                                "$moduleName ${args[1].lowercase()} <${
                                    value.choices.joinToString(separator = "/").lowercase()
                                }>"
                            )
                        }

                        is IntRangeValue, is LongRangeValue, is FloatRangeValue, is DoubleRangeValue -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <min>-<max>")
                        }

                        is BlockValue -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <block>")
                        }

                        is ColorValue -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <red> <green> <blue> [alpha]")
                        }

                        is KeyBindValue -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <key>")
                        }

                        is Vec2Value -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <x> <y>")
                        }

                        is Vec3Value -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <x> <y> <z>")
                        }

                        is MutableListValue -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <value..>")
                        }

                        else -> {
                            chatSyntax("$moduleName ${args[1].lowercase()} <value>")
                        }
                    }

                    return
                }

                try {
                    val pair: Pair<Boolean, String> = when (value) {
                        is IntRangeValue -> {
                            val rangeParts = args[2].split("-").takeIf { it.size == 2 }
                            if (rangeParts != null) {
                                val start = rangeParts[0].toIntOrNull()
                                val end = rangeParts[1].toIntOrNull()

                                if (start != null && end != null) {
                                    val newRange = start..end

                                    require(start <= end) {
                                        chat("§7Min ($start) cannot be greater than $end!")
                                        return
                                    }

                                    val limitsRemoved = UnlimitedValues.handleEvents() && UnlimitedValues.removeLimits

                                    if (limitsRemoved || (newRange.first in value.range && newRange.last in value.range)) {
                                        if (value.set(newRange)) {
                                            chat("§7${module.getName()} §8${args[1]}§7 was set to §8${newRange.first} - ${newRange.last}§7.")
                                            playEdit()
                                        } else chatInvalid("$newRange", value)
                                    } else {
                                        chat("§7${module.getName()} §8${args[1]}§7 range is out of bounds (${value.minimum} - ${value.maximum}).")
                                    }
                                } else {
                                    chat("§7Invalid range format for ${args[1]}. Please use <start>-<end> with integer values.")
                                }
                            } else {
                                chat("§7Invalid range format for ${args[1]}. Please use <start>-<end> with integer values.")
                            }
                            return
                        }

                        is FloatRangeValue -> {
                            val rangeParts = args[2].split("-").takeIf { it.size == 2 }
                            if (rangeParts != null) {
                                val start = rangeParts[0].toFloatOrNull()
                                val end = rangeParts[1].toFloatOrNull()

                                if (start != null && end != null) {
                                    val newRange = start..end

                                    require(start <= end) {
                                        chat("§7Min ($start) cannot be greater than $end!")
                                        return
                                    }

                                    val limitsRemoved = UnlimitedValues.handleEvents() && UnlimitedValues.removeLimits

                                    if (limitsRemoved || (newRange.start in value.range && newRange.endInclusive in value.range)) {
                                        if (value.set(newRange)) {
                                            chat("§7${module.getName()} §8${args[1]}§7 was set to §8${newRange.start} - ${newRange.endInclusive}§7.")
                                            playEdit()
                                        } else chatInvalid("$newRange", value)
                                    } else {
                                        chat("§7${module.getName()} §8${args[1]}§7 range is out of bounds (${value.minimum} - ${value.maximum}).")
                                    }
                                } else {
                                    chat("§7Invalid range format for ${args[1]}. Please use <start>-<end> with float values.")
                                }
                            } else {
                                chat("§7Invalid range format for ${args[1]}. Please use <start>-<end> with float values.")
                            }
                            return
                        }

                        is BlockValue -> {
                            val id = try {
                                args[2].toInt()
                            } catch (exception: NumberFormatException) {
                                val tmpId = Block.getBlockFromName(args[2])?.let { Block.getIdFromBlock(it) }

                                if (tmpId == null || tmpId <= 0) {
                                    chat("§7Block §8${args[2]}§7 does not exist!")
                                    return
                                }

                                tmpId
                            }

                            if (!value.set(id)) {
                                chatInvalid(id.toString(), value)
                                return
                            }

                            chat("§7${module.getName()} §8${args[1].lowercase()}§7 was set to §8${getBlockName(id)}§7.")
                            playEdit()

                            return
                        }

                        is LongRangeValue -> {
                            val rangeParts = args[2].split("-").takeIf { it.size == 2 }
                            val start = rangeParts?.getOrNull(0)?.toLongOrNull()
                            val end = rangeParts?.getOrNull(1)?.toLongOrNull()

                            if (start == null || end == null) {
                                chat("§7Invalid range format for ${args[1]}. Please use <start>-<end> with integer values.")
                                return
                            }

                            require(start <= end) {
                                chat("§7Min ($start) cannot be greater than $end!")
                                return
                            }

                            val newRange = start..end
                            val limitsRemoved = UnlimitedValues.handleEvents() && UnlimitedValues.removeLimits

                            if (limitsRemoved || (newRange.first in value.range && newRange.last in value.range)) {
                                if (value.set(newRange)) {
                                    chat("§7${module.getName()} §8${args[1]}§7 was set to §8${newRange.first} - ${newRange.last}§7.")
                                    playEdit()
                                } else chatInvalid("$newRange", value)
                            } else {
                                chat("§7${module.getName()} §8${args[1]}§7 range is out of bounds (${value.minimum} - ${value.maximum}).")
                            }
                            return
                        }

                        is DoubleRangeValue -> {
                            val rangeParts = args[2].split("-").takeIf { it.size == 2 }
                            val start = rangeParts?.getOrNull(0)?.toDoubleOrNull()
                            val end = rangeParts?.getOrNull(1)?.toDoubleOrNull()

                            if (start == null || end == null) {
                                chat("§7Invalid range format for ${args[1]}. Please use <start>-<end> with float values.")
                                return
                            }

                            require(start <= end) {
                                chat("§7Min ($start) cannot be greater than $end!")
                                return
                            }

                            val newRange = start..end
                            val limitsRemoved = UnlimitedValues.handleEvents() && UnlimitedValues.removeLimits

                            if (limitsRemoved || (newRange.start in value.range && newRange.endInclusive in value.range)) {
                                if (value.set(newRange)) {
                                    chat("§7${module.getName()} §8${args[1]}§7 was set to §8${newRange.start} - ${newRange.endInclusive}§7.")
                                    playEdit()
                                } else chatInvalid("$newRange", value)
                            } else {
                                chat("§7${module.getName()} §8${args[1]}§7 range is out of bounds (${value.minimum} - ${value.maximum}).")
                            }
                            return
                        }

                        is MultiSelectValue -> {
                            val choice = value.choices.find { it.equals(args[2], true) }
                            if (choice == null) {
                                chatSyntax(
                                    "$moduleName ${args[1].lowercase()} <${
                                        value.choices.joinToString(separator = "/").lowercase()
                                    }>"
                                )
                                return
                            }

                            value.toggle(choice)
                            chat("§7${module.getName()} §8${args[1]}§7 was set to §8${value.get().joinToString(", ")}§7.")
                            playEdit()
                            return
                        }

                        is KeyBindValue -> {
                            val newKey = if (args[2].equals("None", true)) {
                                Keyboard.KEY_NONE
                            } else {
                                Keyboard.getKeyIndex(args[2].uppercase()).takeIf { it != Keyboard.KEY_NONE }
                            }

                            if (newKey == null) {
                                chatInvalid(args[2], value)
                                return
                            }

                            value.set(newKey)
                            chat("§7${module.getName()} §8${args[1]}§7 was set to §8${value.keyName}§7.")
                            playEdit()
                            return
                        }

                        is IntValue -> value.set(args[2].toInt()) to args[2]
                        is LongValue -> value.set(args[2].toLong()) to args[2]
                        is FloatValue -> value.set(args[2].toFloat()) to args[2]
                        is DoubleValue -> value.set(args[2].toDouble()) to args[2]
                        is FileValue -> {
                            val string = StringUtils.toCompleteString(args, 2)
                            value.set(string) to string
                        }
                        is ListValue -> {
                            if (args[2] !in value) {
                                chatSyntax(
                                    "$moduleName ${args[1].lowercase()} <${
                                        value.values.joinToString(separator = "/").lowercase()
                                    }>"
                                )
                                return
                            }

                            value.set(args[2]) to args[2]
                        }

                        is TextValue -> {
                            val string = StringUtils.toCompleteString(args, 2)
                            value.set(string) to string
                        }

                        is ColorValue -> {
                            val components = args.drop(2).map { it.toIntOrNull() ?: return chatInvalid(args[2], value) }
                            if (components.size < 3) {
                                chatSyntax("$moduleName ${args[1].lowercase()} <red> <green> <blue> [alpha]")
                                return
                            }

                            val (r, g, b) = components
                            val a = components.getOrElse(3) { 255 }
                            val color = java.awt.Color(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255), a.coerceIn(0, 255))

                            value.set(color)
                            chat("§7${module.getName()} §8${args[1]}§7 was set to §8$r, $g, $b, $a§7.")
                            playEdit()
                            return
                        }

                        is Vec2Value -> {
                            val nums = args.drop(2).mapNotNull { it.toDoubleOrNull() }
                            if (nums.size < 2) {
                                chatSyntax("$moduleName ${args[1].lowercase()} <x> <y>")
                                return
                            }

                            value.set(doubleArrayOf(nums[0], nums[1]))
                            chat("§7${module.getName()} §8${args[1]}§7 was set to §8${nums[0]}, ${nums[1]}§7.")
                            playEdit()
                            return
                        }

                        is Vec3Value -> {
                            val nums = args.drop(2).mapNotNull { it.toDoubleOrNull() }
                            if (nums.size < 3) {
                                chatSyntax("$moduleName ${args[1].lowercase()} <x> <y> <z>")
                                return
                            }

                            value.set(doubleArrayOf(nums[0], nums[1], nums[2]))
                            chat("§7${module.getName()} §8${args[1]}§7 was set to §8${nums[0]}, ${nums[1]}, ${nums[2]}§7.")
                            playEdit()
                            return
                        }

                        is MutableListValue -> {
                            val entries = StringUtils.toCompleteString(args, 2)
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }

                            value.set(entries)
                            chat("§7${module.getName()} §8${args[1]}§7 was set to §8${entries.joinToString(", ").ifEmpty { "empty" }}§7.")
                            playEdit()
                            return
                        }

                        else -> return
                    }

                    // If value wasn't changed successfully, write that previous argument isn't valid
                    if (!pair.first) {
                        chatInvalid(pair.second, value)
                        return
                    }

                    chat("§7${module.getName()} §8${args[1]}§7 was set to §8${value.get()}§7.")
                    playEdit()
                } catch (e: NumberFormatException) {
                    chatInvalid(args[2], value, "cannot be converted to a number for")
                }
            }
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> leafValues(values).mapNotNull {
                it.name.takeIf { name -> name.startsWith(args[0], true) }?.lowercase()
            }

            2 -> {
                when (val value = module[args[0]]) {
                    is BlockValue -> {
                        return Item.itemRegistry.keys.mapNotNull {
                            it.resourcePath.lowercase().takeIf { it.startsWith(args[1], true) }
                        }

                    }

                    is ListValue -> value.values.filter { it.startsWith(args[1], true) }

                    is MultiSelectValue -> value.choices.filter { it.startsWith(args[1], true) }

                    else -> emptyList()
                }
            }

            else -> emptyList()
        }
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

    private fun listValues(values: Collection<Value<*>>, indent: Int) {
        val pad = "  ".repeat(indent)

        for (value in values) {
            if (value is FontValue || !value.shouldRender()) continue

            if (value is Configurable) {
                chat("§8$pad${value.name}§7:")
                listValues(value.values, indent + 1)
                continue
            }

            chat("§8$pad${value.name}§7: §8${describeCurrent(value)}§7")
        }
    }

    private fun describeCurrent(value: Value<*>): String = when (value) {
        is ListValue -> "${value.get()} (${value.values.joinToString("/")})"
        is MultiSelectValue -> value.get().joinToString(", ").ifEmpty { "none" }
        is KeyBindValue -> value.keyName
        is IntRangeValue -> "${value.get().first} - ${value.get().last}"
        is LongRangeValue -> "${value.get().first} - ${value.get().last}"
        is FloatRangeValue -> "${value.get().start} - ${value.get().endInclusive}"
        is DoubleRangeValue -> "${value.get().start} - ${value.get().endInclusive}"
        is BlockValue -> getBlockName(value.get())
        is Vec2Value -> value.get().joinToString(", ")
        is Vec3Value -> value.get().joinToString(", ")
        is CurveValue -> value.get().joinToString(", ")
        is MutableListValue -> value.get().joinToString(", ").ifEmpty { "empty" }
        else -> value.get().toString()
    }

    private fun chatInvalid(arg: String, value: Value<*>, reason: String? = null) {
        val finalReason = reason ?: if (value.get().toString().equals(arg, true)) "is already the value of"
        else "isn't a valid value for"

        chat("§8$arg§7 $finalReason §8${value.name}§7!")
    }

}