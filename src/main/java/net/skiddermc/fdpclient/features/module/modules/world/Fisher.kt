/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.world

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.utils.MathUtils.inRange
import net.skiddermc.fdpclient.utils.timer.MSTimer
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.IntegerValue
import net.skiddermc.fdpclient.value.ListValue
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S29PacketSoundEffect

@ModuleInfo(name = "Fisher", category = ModuleCategory.WORLD)
object Fisher : Module() {

    private val detectionValue = ListValue("Detection", arrayOf("Motion", "Sound"), "Sound")
    private val recastValue = BoolValue("Recast", true)
    private val recastDelayValue = IntegerValue("RecastDelay", 1, 0, 1000)

    private var stage = Stage.NOTHING
    private val recastTimer = MSTimer()

    override fun onDisable() {
        stage = Stage.NOTHING
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (stage == Stage.RECOVERING) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            stage = if (recastValue.get()) {
                recastTimer.reset()
                Stage.RECASTING
            } else {
                Stage.NOTHING
            }
            return
        } else if (stage == Stage.RECASTING) {
            if (recastTimer.hasTimePassed(recastDelayValue.get().toLong())) {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                stage = Stage.NOTHING
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (detectionValue.get() == "Sound" && packet is S29PacketSoundEffect && mc.thePlayer?.fishEntity != null
            && packet.soundName == "random.splash" && packet.x.inRange(mc.thePlayer.fishEntity.posX, 1.5) && packet.z.inRange(mc.thePlayer.fishEntity.posZ, 1.5)) {
            recoverFishRod()
        } else if (detectionValue.get() == "Motion" && packet is S12PacketEntityVelocity && mc.thePlayer?.fishEntity != null
            && packet.entityID == mc.thePlayer.fishEntity.entityId && packet.motionX == 0 && packet.motionY != 0 && packet.motionZ == 0) {
            recoverFishRod()
        }
    }

    private fun recoverFishRod() {
        if (stage != Stage.NOTHING) {
            return
        }

        stage = Stage.RECOVERING
    }

    private enum class Stage {
        NOTHING,
        RECOVERING,
        RECASTING
    }
}
