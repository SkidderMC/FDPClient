/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.C0APacketAnimation
import net.ccbluex.liquidbounce.features.module.modules.visual.Tracers
import java.awt.Color
import java.util.*

@ModuleInfo(name = "AntiFireBall", description = "", category = ModuleCategory.COMBAT)
class AntiFireBall : Module() {
    private val timer = MSTimer()

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val rotationValue = BoolValue("Rotation", true)
    private val maxTurnSpeed: FloatValue =
            object : FloatValue("MaxTurnSpeed", 120f, 0f, 180f) {
                override fun onChanged(oldValue: Float, newValue: Float) {
                    val i = minTurnSpeed.get()
                    if (i > newValue) set(i)
                }
            }
    private val minTurnSpeed: FloatValue =
            object : FloatValue("MinTurnSpeed", 80f, 0f, 180f) {
                override fun onChanged(oldValue: Float, newValue: Float) {
                    val i = maxTurnSpeed.get()
                    if (i < newValue) set(i)
                }
            }

    @EventTarget
    private fun onUpdate(event: UpdateEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityFireball && mc.thePlayer.getDistanceToEntity(entity) < 15) {
                FDPClient.moduleManager[Tracers::class.java]!!.drawTraces(entity, Color.white, true)
            }
            if (entity is EntityFireball && mc.thePlayer.getDistanceToEntity(entity) < 3 && timer.hasTimePassed(300)) {
                if (rotationValue.get()) {
                    RotationUtils.setTargetRotation(
                            RotationUtils.limitAngleChange(
                                    RotationUtils.serverRotation!!,
                                    (RotationUtils.getRotationsNonLivingEntity(entity)),

                                    RandomUtils.nextFloat(minTurnSpeed.get(), maxTurnSpeed.get())
                            )
                    )
                }

                when (swingValue.get().lowercase(Locale.getDefault())) {
                    "normal" -> mc.thePlayer.swingItem()
                    "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
                }

                timer.reset()
                break
            }
        }
    }
}
