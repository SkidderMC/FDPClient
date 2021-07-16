package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.C02PacketUseEntity

@ModuleInfo(
        name = "AntiFireBall",
        description = "Auto the fireball entity to let it back.",
        category = ModuleCategory.PLAYER
)
class AntiFireball : Module() {
    private val msTimer = MSTimer()
    private var lastEntity: Entity? = null
    override fun onDisable() {
        clearEntity()
    }

    override fun onEnable() {
        clearEntity()
    }
    private fun clearEntity() {
        this.lastEntity = null
    }

    @EventTarget
    private fun onMotion(event: MotionEvent) {
        for (x in mc.theWorld.loadedEntityList) {
            if (x is EntityFireball && mc.thePlayer.getDistanceToEntity(x) < 5.5 && (msTimer.hasTimePassed(100) || lastEntity != x) && x.ticksExisted != 0 && event.eventState == EventState.POST) {
                lastEntity = x
                RotationUtils.setTargetRotation(RotationUtils.getRotationsEntity(x as EntityLivingBase?))
                mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(x, C02PacketUseEntity.Action.ATTACK))
                mc.thePlayer.swingItem()
                msTimer.reset()
                break
            }
        }
    }
}
