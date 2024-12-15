/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCrystal
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBoxESP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFDP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawJello
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawLies
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatformESP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawZavz
import net.minecraft.block.Block
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes
import java.awt.Color
import java.util.*

object CombatVisuals : Module("CombatVisuals", Category.VISUAL, hideModule = false, subjective = true) {

    init {
        state = true
    }

    // Mark - TargetESP
    private val markValue by choices("MarkMode", arrayOf("None", "Zavz", "Circle", "Jello", "Lies", "FDP", "Sims", "Box", "RoundBox", "Head", "Mark"), "Zavz")
    private val isMarkMode: Boolean
        get() = markValue != "None" && markValue != "Sims" && markValue != "FDP"  && markValue != "Lies" && markValue != "Jello"

    val colorRedValue by int("Mark-Red", 0, 0.. 255) { isMarkMode }
    val colorGreenValue by int("Mark-Green", 160, 0..255) { isMarkMode }
    val colorBlueValue by int("Mark-Blue", 255, 0.. 255) { isMarkMode }

    private val circleRainbow by boolean("CircleRainbow", false, subjective = true) { markValue == "Circle" }
    private val colors = ColorSettingsInteger(this, "Circle", alphaApply = { markValue == "Circle" })
    { markValue == "Circle" && !circleRainbow }.with(132, 102, 255, 100)
    private val fillInnerCircle by boolean("FillInnerCircle", false, subjective = true) { markValue == "Circle" }
    private val withHeight by boolean("WithHeight", true, subjective = true) { markValue == "Circle" }
    private val animateHeight by boolean("AnimateHeight", false, subjective = true) { withHeight }
    private val heightRange by floatRange("HeightRange", 0.0f..0.4f, -2f..2f, subjective = true) { withHeight }
    private val extraWidth by float("ExtraWidth", 0F, 0F..2F, subjective = true) { markValue == "Circle" }
    private val animateCircleY by boolean("AnimateCircleY", true, subjective = true) { fillInnerCircle || withHeight }
    private val circleYRange by floatRange("CircleYRange", 0F..0.5F, 0F..2F, subjective = true) { animateCircleY }
    private val duration by float("Duration", 1.5F, 0.5F..3F, suffix = "Seconds", subjective = true)
    { animateCircleY || animateHeight }

    private val alphaValue by int("Alpha", 255, 0.. 255) { isMarkMode && markValue == "Zavz" && markValue == "Jello"}

    val colorRedTwoValue by int("Mark-Red 2", 0, 0.. 255) { isMarkMode && markValue == "Zavz" }
    val colorGreenTwoValue by int("Mark-Green 2", 160, 0..255) { isMarkMode && markValue == "Zavz" }
    val colorBlueTwoValue by int("Mark-Blue 2", 255, 0.. 255) { isMarkMode && markValue == "Zavz" }

    private val rainbow by boolean("Mark-RainBow", false) { isMarkMode }
    private val hurt by boolean("Mark-HurtTime", true) { isMarkMode }
    private val boxOutline by boolean("Mark-Outline", true, subjective = true) { isMarkMode && markValue == "RoundBox" }

    // fake sharp
    private val fakeSharp by boolean("FakeSharp", true, subjective = true)

    // Sound

    private val particle by choices("Particle",
        arrayOf("None", "Blood", "Lighting", "Fire", "Heart", "Water", "Smoke", "Magic", "Crits"), "Blood")

    private val amount by int("ParticleAmount", 5, 1..20) { particle != "None" }

    //Sound
    private val sound by choices("Sound", arrayOf("None", "Hit", "Explode", "Orb", "Pop", "Splash", "Lightning"), "Pop")

    private val volume by float("Volume", 1f, 0.1f.. 5f) { sound != "None" }
    private val pitch by float("Pitch", 1f, 0.1f..5f) { sound != "None" }

    // variables
    private val targetList = HashMap<EntityLivingBase, Long>()
    private val combat = CombatManager
    var random = Random()
    const val DOUBLE_PI = Math.PI * 2
    var start = 0.0


    val onWorld = handler<WorldEvent> {
        targetList.clear()
    }

    val onRender3D = handler<Render3DEvent> { event ->
        val color: Color = if (rainbow) rainbow() else Color(
            colorRedValue,
            colorGreenValue,
            colorBlueValue,
            alphaValue
        )
        val renderManager = mc.renderManager
        val entityLivingBase = combat.target ?: return@handler
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

            "circle" -> drawCircle(
                entityLivingBase,
                duration * 1000F,
                heightRange.takeIf { animateHeight } ?: heightRange.endInclusive..heightRange.endInclusive,
                extraWidth,
                fillInnerCircle,
                withHeight,
                circleYRange.takeIf { animateCircleY },
                if (circleRainbow) rainbow().withAlpha(colors.color().alpha) else colors.color()
            )
        }
    }


    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity as? EntityLivingBase ?: return

        repeat(amount) {
            doEffect(target)
        }

        doSound()
        attackEntity(target)
    }


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