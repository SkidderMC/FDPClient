package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.entity.player.EntityPlayer
import net.ccbluex.liquidbounce.event.WorldEvent

@ModuleInfo(name = "MurderDetector", category = ModuleCategory.MISC)
object MurderDetector : Module() {

    private var murderer: EntityPlayer? = null

    fun onPreMotion() {
        if (mc.thePlayer.ticksExisted % 2 == 0 || this.murderer != null) {
            return
        }

        for (player in mc.theWorld.playerEntities) {
            if (player.heldItem != null) {
                if (player.heldItem.displayName.contains("Knife", ignoreCase = true)) {
                    ClientUtils.displayChatMessage(player.getName() + "is The Murderer.")
                    murderer = player
                }
            }
        }
    }

    fun onWorld(event: WorldEvent){
        murderer = null;
    }   
}
