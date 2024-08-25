/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCrystal
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBoxESP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFDP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawJello
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawLies
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatformESP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawZavz
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*

object CombatVisuals : Module("CombatVisuals", Category.VISUAL, hideModule = false, subjective = true) {

    init {
        state = true
    }

    // Mark - TargetESP
    private val markValue by ListValue("MarkMode", arrayOf("None", "Zavz", "Jello", "Lies", "FDP", "Sims", "Box", "RoundBox", "Head", "Mark"), "Zavz")
    private val isMarkMode: Boolean
        get() = markValue != "None" && markValue != "Sims" && markValue != "FDP"  && markValue != "Lies" && markValue != "Jello"

    val colorRedValue by IntegerValue("Mark-Red", 0, 0.. 255) { isMarkMode }
    val colorGreenValue by IntegerValue("Mark-Green", 160, 0..255) { isMarkMode }
    val colorBlueValue by IntegerValue("Mark-Blue", 255, 0.. 255) { isMarkMode }

    val alphaValue by IntegerValue("Alpha", 255, 0.. 255) { isMarkMode && markValue == "Zavz" && markValue == "Jello"}

    val colorRedTwoValue by IntegerValue("Mark-Red 2", 0, 0.. 255) { isMarkMode && markValue == "Zavz" }
    val colorGreenTwoValue by IntegerValue("Mark-Green 2", 160, 0..255) { isMarkMode && markValue == "Zavz" }
    val colorBlueTwoValue by IntegerValue("Mark-Blue 2", 255, 0.. 255) { isMarkMode && markValue == "Zavz" }

    private val rainbow by BoolValue("Mark-RainBow", false) { isMarkMode }
    private val hurt by BoolValue("Mark-HurtTime", true) { isMarkMode }
    private val boxOutline by BoolValue("Mark-Outline", true, subjective = true) { isMarkMode && markValue == "RoundBox" }

    // fake sharp
    private val fakeSharp by BoolValue("FakeSharp", true, subjective = true)

    // Sound

    private val particle by ListValue("Particle",
        arrayOf("None", "Blood", "Lighting", "Fire", "Heart", "Water", "Smoke", "Magic", "Crits"), "Blood")

    private val amount by IntegerValue("ParticleAmount", 5, 1..20) { particle != "None" }

    //Sound
    private val sound by ListValue("Sound", arrayOf("None", "Hit", "Explode", "Orb", "Pop", "Splash", "Lightning"), "Pop")

    private val volume by FloatValue("Volume", 1f, 0.1f.. 5f) { sound != "None" }
    private val pitch by FloatValue("Pitch", 1f, 0.1f..5f) { sound != "None" }

    // variables
    private val targetList = HashMap<EntityLivingBase, Long>()
    private val combat = CombatManager
    var random = Random()
    const val DOUBLE_PI = Math.PI * 2
    var start = 0.0

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        targetList.clear()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color: Color = if (rainbow) ColorUtils.rainbow() else Color(
            colorRedValue,
            colorGreenValue,
            colorBlueValue,
            alphaValue
        )
        val renderManager = mc.renderManager
        val entityLivingBase = combat.target ?: return
        (entityLivingBase.lastTickPosX + (entityLivingBase.posX - entityLivingBase.lastTickPosX) * mc.timer.renderPartialTicks
                - renderManager.renderPosX)
        (entityLivingBase.lastTickPosY + (entityLivingBase.posY - entityLivingBase.lastTickPosY) * mc.timer.renderPartialTicks
                - renderManager.renderPosY)
        (entityLivingBase.lastTickPosZ + (entityLivingBase.posZ - entityLivingBase.lastTickPosZ) * mc.timer.renderPartialTicks
                - renderManager.renderPosZ)
        when (markValue.lowercase()) {
            "box" -> drawEntityBoxESP(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(255, 50, 50, 75) else color
            )

            "roundbox" -> drawEntityBox(
                entityLivingBase,
                if (hurt && entityLivingBase.hurtTime > 3)
                    Color(37, 126, 255, 70)
                else
                    Color(255, 0, 0, 70),
                boxOutline
            )

            "head" -> drawPlatformESP(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(255, 50, 50, 75) else color
            )

            "mark" -> drawPlatform(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(37, 126, 255, 70) else color
            )

            "sims" -> drawCrystal(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime <= 0)) Color(80, 255, 80, 200).rgb else Color(255, 0, 0, 200).rgb,
                event
            )

            "zavz" -> drawZavz(
                entityLivingBase,
                event,
                dual = true, // or false based on your requirement
            )

            "jello" -> drawJello(
                entityLivingBase,
            )

            "fdp" -> drawFDP(
                entityLivingBase,
                event
            )

            "lies" -> drawLies(
                entityLivingBase,
                event
            )
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity as? EntityLivingBase ?: return

        repeat(amount) {
            doEffect(target)
        }

        doSound()
        attackEntity(target)
    }

    @EventTarget
    private fun attackEntity(entity: EntityLivingBase) {
        val thePlayer = mc.thePlayer

        // Extra critical effects
        repeat(3) {
            // Critical Effect
            if (thePlayer.fallDistance > 0F && !thePlayer.onGround && !thePlayer.isOnLadder && !thePlayer.isInWater && !thePlayer.isPotionActive(
                    Potion.blindness
                ) && thePlayer.ridingEntity == null || Criticals.handleEvents() && Criticals.msTimer.hasTimePassed(
                    Criticals.delay
                ) && !thePlayer.isInWater && !thePlayer.isInLava && !thePlayer.isInWeb) {
                thePlayer.onCriticalHit(entity)
            }

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(thePlayer.heldItem,
                    entity.creatureAttribute
                ) > 0f || fakeSharp
            ) {
                thePlayer.onEnchantmentCritical(entity)
            }
        }
    }

    private fun doSound() {
        val player = mc.thePlayer

        when (sound) {
            "Hit" -> player.playSound("random.bowhit", volume, pitch)
            "Orb" -> player.playSound("random.orb", volume, pitch)
            "Pop" -> player.playSound("random.pop", volume, pitch)
            "Splash" -> player.playSound("random.splash", volume, pitch)
            "Lightning" -> player.playSound("ambient.weather.thunder", volume, pitch)
            "Explode" -> player.playSound("random.explode", volume, pitch)
        }
    }

    private fun doEffect(target: EntityLivingBase) {
        when (particle) {
            "Blood" -> spawnBloodParticle(EnumParticleTypes.BLOCK_CRACK, target)
            "Crits" -> spawnEffectParticle(EnumParticleTypes.CRIT, target)
            "Magic" -> spawnEffectParticle(EnumParticleTypes.CRIT_MAGIC, target)
            "Lighting" -> spawnLightning(target)
            "Smoke" -> spawnEffectParticle(EnumParticleTypes.SMOKE_NORMAL, target)
            "Water" -> spawnEffectParticle(EnumParticleTypes.WATER_DROP, target)
            "Heart" -> spawnEffectParticle(EnumParticleTypes.HEART, target)
            "Fire" -> spawnEffectParticle(EnumParticleTypes.LAVA, target)
        }
    }

    private fun spawnBloodParticle(particleType: EnumParticleTypes, target: EntityLivingBase) {
        mc.theWorld.spawnParticle(particleType,
            target.posX, target.posY + target.height - 0.75, target.posZ,
            0.0, 0.0, 0.0,
            Block.getStateId(Blocks.redstone_block.defaultState)
        )
    }

    private fun spawnEffectParticle(particleType: EnumParticleTypes, target: EntityLivingBase) {
        mc.effectRenderer.spawnEffectParticle(particleType.particleID,
            target.posX, target.posY, target.posZ,
            target.posX, target.posY, target.posZ
        )
    }

    private fun spawnLightning(target: EntityLivingBase) {
        mc.netHandler.handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity(
            EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ)
        ))
    }
}