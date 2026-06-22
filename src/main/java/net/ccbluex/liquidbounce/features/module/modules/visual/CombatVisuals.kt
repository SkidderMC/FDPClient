/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCrystal
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBoxESP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFDP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImageMark
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawJello
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawLies
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatformESP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawZavz
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPoints
import net.minecraft.block.Block
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.util.*

object CombatVisuals : Module("CombatVisuals", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, subjective = true) {

    init {
        state = true
    }

    // Mark - TargetESP
    private val markValue by choices(
        "MarkMode",
        arrayOf("None", "Points", "Image", "Zavz", "Circle", "Jello", "Lies", "FDP", "Sims", "Box", "RoundBox", "Head", "Mark"),
        "Points"
    )
    private val isMarkMode: Boolean
        get() = markValue != "None" && markValue != "Sims" && markValue != "FDP"  && markValue != "Lies" && markValue != "Jello"

    val colorPrimary by color("Color Primary", Color(0, 90, 255)) { isMarkMode }
        .describe("Primary color of the target marker.")
    val colorSecondary by color("Color Secondary",Color(0, 90, 255)) { isMarkMode && markValue == "Zavz" }
        .describe("Secondary color for the Zavz marker.")

    // Circle options
    private val circleStartColor by color("CircleStartColor", Color.BLUE) { markValue == "Circle" }.subjective()
        .describe("Start color of the circle gradient.")
    private val circleEndColor by color("CircleEndColor", Color.CYAN.withAlpha(0)) { markValue == "Circle" }.subjective()
        .describe("End color of the circle gradient.")

    private val pointsSpeed by float("PointsSpeed", 2.0f, 0.5f..5.0f) { markValue == "Points" }.subjective()
        .describe("Rotation speed of the points marker.")
    private val pointsRadius by float("PointsRadius", 0.60f, 0.20f..1.20f) { markValue == "Points" }.subjective()
        .describe("Radius of the points marker.")
    private val pointsScale by float("PointsScale", 0.25f, 0.05f..0.60f) { markValue == "Points" }.subjective()
        .describe("Size of each point in the marker.")
    private val pointsLayers by int("PointsLayers", 3, 1..5) { markValue == "Points" }.subjective()
        .describe("Number of stacked point layers.")
    private val pointsAdditive by boolean("PointsAdditive", true) { markValue == "Points" }.subjective()
        .describe("Use additive blending for the points.")

    private val imageMode by choices(
        "ImageMode",
        arrayOf("Rectangle","QuadStapple","TriangleStapple","TriangleStipple","GlowCircle"),
        "Rectangle"
    ) { markValue == "Image" }.subjective()
    private val imageScale by float("ImageScale", 0.6f, 0.1f..2.0f) { markValue == "Image" }.subjective()
        .describe("Size of the image marker.")
    private val imageXOffset by float("ImageXOffset", 0.0f, -1.5f..1.5f) { markValue == "Image" }.subjective()
        .describe("Horizontal offset of the image marker.")
    private val imageYOffset by float("ImageYOffset", 0.0f, -0.5f..1.5f) { markValue == "Image" }.subjective()
        .describe("Vertical offset of the image marker.")
    private val imageAdditive by boolean("ImageAdditive", true) { markValue == "Image" }.subjective()
        .describe("Use additive blending for the image.")
    private val imageSpin by boolean("ImageSpin", false) { markValue == "Image" }.subjective()
        .describe("Spin the image marker over time.")
    private val imageSpinSpeed by float("ImageSpinSpeed", 1.0f, 0.1f..5.0f) { markValue == "Image" && imageSpin }.subjective()
        .describe("Spin speed of the image marker.")
    private val imageBillboard by boolean("ImageBillboard", true) { markValue == "Image" }.subjective()
        .describe("Keep the image facing the camera.")
    private val imageColor1 by color("ImageColor1", Color(255,255,255,255)) { markValue == "Image" }.subjective()
        .describe("First color tint of the image marker.")
    private val imageColor2 by color("ImageColor2", Color(255,255,255,255)) { markValue == "Image" }.subjective()
        .describe("Second color tint of the image marker.")
    private val imageColor3 by color("ImageColor3", Color(255,255,255,255)) { markValue == "Image" }.subjective()
        .describe("Third color tint of the image marker.")
    private val imageColor4 by color("ImageColor4", Color(255,255,255,255)) { markValue == "Image" }.subjective()
        .describe("Fourth color tint of the image marker.")

    private val glowCircle = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/targetesp/glow_circle.png")
    private val rectangle = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/targetesp/rectangle.png")
    private val quadstapple = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/targetesp/quadstapple.png")
    private val trianglestapple = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/targetesp/trianglestapple.png")
    private val trianglestipple  = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/targetesp/trianglestipple.png")

    private val fillInnerCircle by boolean("FillInnerCircle", false) { markValue == "Circle" }.subjective()
        .describe("Fill the inside of the circle marker.")
    private val withHeight by boolean("WithHeight", true) { markValue == "Circle" }.subjective()
        .describe("Extrude the circle into a cylinder.")
    private val animateHeight by boolean("AnimateHeight", false) { withHeight }.subjective()
        .describe("Animate the cylinder height.")
    private val heightRange by floatRange("HeightRange", 0.0f..0.4f, -2f..2f) { withHeight }.subjective()
        .describe("Min and max height of the cylinder.")
    private val extraWidth by float("ExtraWidth", 0F, 0F..2F) { markValue == "Circle" }.subjective()
        .describe("Extra width added to the circle.")
    private val animateCircleY by boolean("AnimateCircleY", true) { fillInnerCircle || withHeight }.subjective()
        .describe("Animate the circle vertical position.")
    private val circleYRange by floatRange("CircleYRange", 0F..0.5F, 0F..2F) { animateCircleY }.subjective()
        .describe("Min and max vertical range of the circle.")
    private val duration by float(
        "Duration",
        1.5F,
        0.5F..3F,
        suffix = "Seconds"
    ) { animateCircleY || animateHeight }.subjective()

    private val filterEntityType by choices(
        "FilterEntityType",
        arrayOf("All", "Players", "Mobs", "Animals"),
        "All"
    )

    private val rainbow by boolean("Mark-RainBow", false) { isMarkMode }
        .describe("Animate the marker with rainbow colors.")
    private val hurt by boolean("Mark-HurtTime", true) { isMarkMode }
        .describe("Flash the marker red when the target is hurt.")
    private val boxOutline by boolean("Outline", true) { markValue == "Box" }.subjective()
        .describe("Draw an outline around the box marker.")

    // fake sharp
    private val fakeSharp by boolean("FakeSharp", false).subjective()
        .describe("Play a fake sharpness hit effect.")

    // Sound

    private val particle by choices("Particle",
        arrayOf("None", "Blood", "Lighting", "Fire", "Heart", "Water", "Smoke", "Magic", "Crits"), "Blood")

    private val amount by int("ParticleAmount", 5, 1..20) { particle != "None" }
        .describe("Number of particles spawned per hit.")

    //Sound
    private val sound by choices("Sound", arrayOf("None", "Hit", "Explode", "Orb", "Pop", "Splash", "Lightning"), "Pop")
        .describe("Sound played when you hit a target.")

    private val volume by float("Volume", 1f, 0.1f.. 5f) { sound != "None" }
        .describe("Volume of the hit sound.")
    private val pitch by float("Pitch", 1f, 0.1f..5f) { sound != "None" }
        .describe("Pitch of the hit sound.")

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
        val color: Color = if (rainbow) rainbow() else colorPrimary.withAlpha(255)
        val renderManager = mc.renderManager
        val entityLivingBase = combat.target ?: return@handler
        if (!matchesEntityFilter(entityLivingBase)) return@handler
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
                circleStartColor.rgb,
                circleEndColor.rgb
            )

            "points" -> drawPoints(
                entityLivingBase,
                color,
                pointsSpeed,
                pointsRadius,
                pointsScale,
                pointsLayers,
                pointsAdditive,
                hurt
            )

            "image" -> {
                val tex = when (imageMode) {
                    "Rectangle" -> rectangle
                    "QuadStapple" -> quadstapple
                    "TriangleStapple" -> trianglestapple
                    "TriangleStipple" -> trianglestipple
                    else -> glowCircle
                }
                drawImageMark(
                    entityLivingBase,
                    tex,
                    imageColor1,
                    imageColor2,
                    imageColor3,
                    imageColor4,
                    imageScale,
                    imageXOffset,
                    imageYOffset,
                    imageAdditive,
                    imageSpin,
                    imageSpinSpeed,
                    imageBillboard,
                    hurt
                )
            }
        }
    }


    val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity as? EntityLivingBase ?: return@handler

        repeat(amount) {
            doEffect(target)
        }

        doSound()
        attackEntity(target)
    }

    private fun matchesEntityFilter(entity: EntityLivingBase): Boolean {
        return when (filterEntityType) {
            "Players" -> entity is EntityPlayer
            "Mobs" -> entity.isMob()
            "Animals" -> entity.isAnimal()
            else -> true
        }
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