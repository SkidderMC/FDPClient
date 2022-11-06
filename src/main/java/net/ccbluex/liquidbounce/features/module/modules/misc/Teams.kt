/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor

@ModuleInfo(name = "Teams", category = ModuleCategory.MISC)
class Teams : Module() {

    private val scoreboardValue = BoolValue("ScoreboardTeam", true)
    private val colorValue = BoolValue("Color", true)
    private val gommeSWValue = BoolValue("GommeSW", false)
    private val armorValue = BoolValue("ArmorColor", false)

    /**
     * Check if [entity] is in your own team using scoreboard, name color or team prefix
     */
    fun isInYourTeam(entity: EntityLivingBase): Boolean {
        mc.thePlayer ?: return false

        if (scoreboardValue.get() && mc.thePlayer.team != null && entity.team != null &&
                mc.thePlayer.team.isSameTeam(entity.team)) {
            return true
        }
        if (gommeSWValue.get() && mc.thePlayer.displayName != null && entity.displayName != null) {
            val targetName = entity.displayName.formattedText.replace("§r", "")
            val clientName = mc.thePlayer.displayName.formattedText.replace("§r", "")
            if (targetName.startsWith("T") && clientName.startsWith("T")) {
                if (targetName[1].isDigit() && clientName[1].isDigit()) {
                    return targetName[1] == clientName[1]
                }
            }
        }
        if (armorValue.get()) {
            val entityPlayer = entity as EntityPlayer
            if (mc.thePlayer.inventory.armorInventory[3] != null && entityPlayer.inventory.armorInventory[3] != null) {
                val myHead = mc.thePlayer.inventory.armorInventory[3]
                val myItemArmor = myHead.item as ItemArmor

                val entityHead = entityPlayer.inventory.armorInventory[3]
                var entityItemArmor = myHead.item as ItemArmor

                if (myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead)) {
                    return true
                }
            }
        }
        if (colorValue.get() && mc.thePlayer.displayName != null && entity.displayName != null) {
            val targetName = entity.displayName.formattedText.replace("§r", "")
            val clientName = mc.thePlayer.displayName.formattedText.replace("§r", "")
            return targetName.startsWith("§${clientName[1]}")
        }

        return false
    }
}
