package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.util.EnumParticleTypes

class JetpackFly : FlyMode("Jetpack") {
    private val particleValue = BoolValue("${valuePrefix}Particle", true)
    override fun onUpdate(event: UpdateEvent) {
        if (mc.gameSettings.keyBindJump.isKeyDown) {
            if(particleValue.get()) mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.FLAME.particleID, mc.thePlayer.posX, mc.thePlayer.posY + 0.2, mc.thePlayer.posZ, -mc.thePlayer.motionX, -0.5, -mc.thePlayer.motionZ)

            mc.thePlayer.motionY += 0.15
            mc.thePlayer.motionX *= 1.1
            mc.thePlayer.motionZ *= 1.1
        }
    }
}