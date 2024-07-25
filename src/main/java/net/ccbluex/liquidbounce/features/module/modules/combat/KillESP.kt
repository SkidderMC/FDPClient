/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C02PacketUseEntity
import java.awt.Color
import java.util.*

object KillESP : Module("KillESP", Category.COMBAT) {

    private val modeValue by ListValue("Mode", arrayOf("Box", "Head", "Mark"), "Crystal")
    private val colorRedValue by IntegerValue("R", 0, 0.. 255)
    private val colorGreenValue by IntegerValue("G", 160, 0..255)
    private val colorBlueValue by IntegerValue("B", 255, 0.. 255)
    private val alphaValue by IntegerValue("Alpha", 255, 0.. 255)
    private val killLightningBoltValue by BoolValue("LightningBolt", true)
    private val rainbow by BoolValue("RainBow", false)
    private val hurt by BoolValue("HurtTime", true)

    private val targetList = HashMap<EntityLivingBase, Long>()
    private val combat = CombatManager
    var random: Random = Random()

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (killLightningBoltValue) {
            for ((key, value) in targetList) {
                if (key.isDead || key.health == 0f) {
                    if (value > System.currentTimeMillis()) {
                        val ent = EntityLightningBolt(
                            mc.theWorld,
                            key.posX, key.posY, key.posZ
                        )
                        mc.theWorld.addEntityToWorld(-1, ent)
                        mc.thePlayer.playSound("random.explode", 0.5f, 0.5f + random.nextFloat() * 0.2f)
                    }
                    targetList.remove(key)
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet: Packet<*> = event.packet
        if (killLightningBoltValue) {
            if (packet is C02PacketUseEntity) {
                if (packet.action == C02PacketUseEntity.Action.ATTACK) {
                    val entity = packet.getEntityFromWorld(mc.theWorld)
                    if (entity is EntityLivingBase) {
                        targetList[entity] = System.currentTimeMillis() + 3000L
                    }
                }
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        targetList.clear()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val color: Color = if (rainbow) ColorUtils.rainbow() else Color(
            colorRedValue,
            colorGreenValue,
            colorBlueValue,
            alphaValue
        )
        val renderManager: RenderManager = mc.renderManager
        val entityLivingBase: EntityLivingBase = combat.target ?: return
        val pX: Double =
            (entityLivingBase.lastTickPosX + (entityLivingBase.posX - entityLivingBase.lastTickPosX) * mc.timer.renderPartialTicks
                    - renderManager.renderPosX)
        val pY: Double =
            (entityLivingBase.lastTickPosY + (entityLivingBase.posY - entityLivingBase.lastTickPosY) * mc.timer.renderPartialTicks
                    - renderManager.renderPosY)
        val pZ: Double =
            (entityLivingBase.lastTickPosZ + (entityLivingBase.posZ - entityLivingBase.lastTickPosZ) * mc.timer.renderPartialTicks
                    - renderManager.renderPosZ)
        when (modeValue.lowercase()) {
            "box" -> RenderUtils.drawEntityBoxESP(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(255, 50, 50, 75) else color
            )

            "head" -> RenderUtils.drawPlatformESP(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(255, 50, 50, 75) else color
            )

            "mark" -> RenderUtils.drawPlatform(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(255, 50, 50, 75) else color
            )
        }
    }
}