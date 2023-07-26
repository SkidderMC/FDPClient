package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.entity.player.EntityPlayer

@ModuleInfo(name="MurderDetector", category = ModuleCategory.MISC)
class MurderDetector() : Module(){
    
    private EntityPlayer murderer;

    override fun onPreMotion() {
    
        if (mc.thePlayer.ticksExisted % 2 == 0 || murderer != null) {
                return;
            }

        for (player in mc.theWorld.playerEntities){
            if (player.getHeldItem() != null){
                if (player.getHeldItem().getDisplayName().contains("Knife")) {
                    ClientUtils.displayChatMessage(player.getCommandSenderName() + " is The Murderer.");
                    murderer = player;
                }
            }
        }

    }
}

