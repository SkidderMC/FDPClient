/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.visual.ProtectionZones
import net.minecraft.util.BlockPos

object ProtectionZonesCommand : Command("protectionzones", "zones", "pzones") {

    override fun execute(args: Array<String>) {
        if (args.size < 2) {
            chatSyntax(arrayOf("add <x1> <y1> <z1> <x2> <y2> <z2>", "demo", "clear", "list"))
            return
        }

        when (args[1].lowercase()) {
            "add" -> {
                if (args.size < 8) {
                    chatSyntax("protectionzones add <x1> <y1> <z1> <x2> <y2> <z2>")
                    return
                }

                try {
                    val from = BlockPos(args[2].toInt(), args[3].toInt(), args[4].toInt())
                    val to = BlockPos(args[5].toInt(), args[6].toInt(), args[7].toInt())

                    ProtectionZones.addZone(from, to)
                    chat("§7Added zone §8$from §7- §8$to§7.")
                    playEdit()
                } catch (exception: NumberFormatException) {
                    chatSyntaxError()
                }
            }

            "demo" -> {
                ProtectionZones.addDemoZone()
                chat("§7Added a demo zone around you.")
                playEdit()
            }

            "clear" -> {
                ProtectionZones.clearZones()
                chat("§7Cleared all zones.")
                playEdit()
            }

            "list" -> {
                if (ProtectionZones.zones.isEmpty()) {
                    chat("§7No zones configured.")
                    return
                }

                chat("§7Zones (§8${ProtectionZones.zones.size}§7):")
                ProtectionZones.zones.forEachIndexed { index, zone ->
                    chat("§8> §7#$index §8${zone.from} §7- §8${zone.to}")
                }
            }

            else -> chatSyntaxError()
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("add", "demo", "clear", "list").filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}
