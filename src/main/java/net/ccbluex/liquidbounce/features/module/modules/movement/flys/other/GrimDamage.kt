package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode

class GrimDamage : FlyMode("GrimDamage") {
    override fun onUpdate(event: UpdateEvent) {
            FDPClient.hud.addNotification(Notification("GrimDamage", "Damage GrimAC Fly (TNT ONLY Is A Test!)", NotifyType.ERROR))
            fly.state = false
        }
    }
      
        mc.thePlayer.setPositionAndRotation(mc.thePlayer.posX+1000, mc.thePlayer.posY, mc.thePlayer.posZ+1000, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }
}
