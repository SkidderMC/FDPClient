/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.UpdateEvent
import me.zywl.fdpclient.value.impl.BoolValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor

@ModuleInfo(name = "AutoRole", category = ModuleCategory.EXPLOIT)
class AutoRole : Module() {
    private val formattingValue = BoolValue("Formatting", true)

    companion object {
        private val STAFF_PREFIXES = arrayOf(
            "[Moderador] ",
            "[MODERADOR] ",
            "[Mod] ",
            "[MOD] ",
            "[Administrador] ",
            "[ADMINISTRADOR] ",
            "[Admin] ",
            "[ADMIN] ",
            "[Coordenador] ",
            "[COORDENADOR] ",
            "[Coord] ",
            "[COORD] ",
            "[Gerente] ",
            "[GERENTE] ",
            "[CEO] ",
            "[Dono] ",
            "[DONO] ",
            "[DIRETOR] ",
            "[DEV] ",
            "[Dev] ",
            "Diretor ",
            "DIRETOR ",
            "Dev ",
            "DEV ",
            "Mod ",
            "MOD ",
            "[Master] ",
            "[MASTER] ",
            "[CEO] ",
            "[TRIAL] "
        )
    }

    private fun isStaff(prefix: String): Boolean {
        return STAFF_PREFIXES.toList().contains(stripColor(prefix))
    }

    @EventTarget
    fun handle(event: UpdateEvent?) {
        val friendManager = FDPClient.fileManager.friendsConfig

        val formatCodes = arrayOf("§k", "§l", "§m", "§n", "§o")
        var currentFormatIndex = 0

        for (team in mc.theWorld.scoreboard.teams) {
            if (this.isStaff(team.colorPrefix)) {
                for (member in team.membershipCollection) {
                    if (!friendManager.isFriend(member)) {
                        friendManager.addFriend(member)

                        var colorPrefix = team.colorPrefix
                        if (formattingValue.get() && currentFormatIndex < formatCodes.size) {
                            colorPrefix = formatCodes[currentFormatIndex] + colorPrefix
                            currentFormatIndex++
                            if (currentFormatIndex >= formatCodes.size) {
                                currentFormatIndex = 0
                            }
                        }

                        chat("§7[§d!§7]§7 ADDED: $colorPrefix$member")
                    }
                }
            }
        }
    }
}