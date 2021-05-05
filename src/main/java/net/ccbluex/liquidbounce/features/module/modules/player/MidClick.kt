/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.entity.player.EntityPlayer
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.file.configs.FriendsConfig
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import org.lwjgl.input.Mouse

@ModuleInfo(name = "MidClick", description = "Allows you to add a player as a friend by right clicking him.", category = ModuleCategory.PLAYER)
class MidClick : Module() {
    private var wasDown = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.currentScreen != null) return

        if (!wasDown && Mouse.isButtonDown(2)) {
            val entity = mc.objectMouseOver.entityHit

            if (entity is EntityPlayer) {
                val playerName = stripColor(entity.getName())
                val friendsConfig = LiquidBounce.fileManager.friendsConfig

                if (!friendsConfig.isFriend(playerName)) {
                    friendsConfig.addFriend(playerName)
                    LiquidBounce.fileManager.saveConfig(friendsConfig)
                    ClientUtils.displayChatMessage("§a§l$playerName§c was added to your friends.")
                } else {
                    friendsConfig.removeFriend(playerName)
                    LiquidBounce.fileManager.saveConfig(friendsConfig)
                    ClientUtils.displayChatMessage("§a§l$playerName§c was removed from your friends.")
                }
            } else {
                ClientUtils.displayChatMessage("§c§lError: §aYou need to select a player.")
            }
        }

        wasDown = Mouse.isButtonDown(2)
    }
}