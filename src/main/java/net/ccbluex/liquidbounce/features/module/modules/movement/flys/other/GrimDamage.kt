package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType

class GrimDamage : FlyMode("GrimDamage") {
    override fun onEnable() {
        if(mc.isSingleplayer) {
            FDPClient.hud.addNotification(Notification("GrimDamage", "Damage GrimAC Fly (TNT ONLY Is A Test!)", NotifyType.ERROR))
            fly.state = false
        }
    }
      
        mc.thePlayer.setPositionAndRotation(mc.thePlayer.posX+1000, mc.thePlayer.posY, mc.thePlayer.posZ+1000, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }
}
