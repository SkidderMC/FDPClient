/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.block.Block
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "HitEffect", category = ModuleCategory.RENDER)
object HitEffect : Module() {

    private val timingValue = ListValue("Timing", arrayOf("Attack", "Kill"), "Attack")
    private val modeValue = ListValue("Mode", arrayOf("Lighting", "Blood", "Fire", "Critical", "MagicCritical"), "Lighting")
    private val timesValue = IntegerValue("Times", 1, 1, 10)
    private val lightingSoundValue = BoolValue("LightingSound", true).displayable { modeValue.equals("Lighting") }

    private val blockState = Block.getStateId(Blocks.redstone_block.defaultState)

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if(timingValue.equals("Attack") && EntityUtils.isSelected(event.targetEntity, true)) {
            displayEffectFor(event.targetEntity as EntityLivingBase)
        }
    }

    @EventTarget
    fun onKilled(event: EntityKilledEvent) {
        if(timingValue.equals("Kill")) { // the CombatManager has checked if the entity is selected, we don't need to check it again
            displayEffectFor(event.targetEntity)
        }
    }

    private fun displayEffectFor(entity: EntityLivingBase) {
        repeat(timesValue.get()) {
            when(modeValue.get().lowercase()) {
                "lighting" -> {
                    mc.netHandler.handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity(EntityLightningBolt(mc.theWorld, entity.posX, entity.posY, entity.posZ)))
                    if(lightingSoundValue.get()) {
                        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.explode"), 1.0f))
                        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("ambient.weather.thunder"), 1.0f))
                    }
                }
                "blood" -> {
                    repeat(10) {
                        mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.particleID, entity.posX, entity.posY + entity.height / 2, entity.posZ,
                            entity.motionX + RandomUtils.nextFloat(-0.5f, 0.5f), entity.motionY + RandomUtils.nextFloat(-0.5f, 0.5f), entity.motionZ + RandomUtils.nextFloat(-0.5f, 0.5f), blockState)
                    }
                }
                "fire" ->
                    mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.LAVA)
                "critical" -> mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT)
                "magiccritical" -> mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT_MAGIC)
            }
        }
    }
}