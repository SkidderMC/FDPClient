/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.customRotatedObject2D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.setupDrawCircles
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeInOutElasticx
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeInOutExpo
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeOutBounce
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeOutCirc
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeOutElasticX
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeWave
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.choices
import net.ccbluex.liquidbounce.config.float
import net.ccbluex.liquidbounce.config.int
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.client.renderer.GlStateManager.pushMatrix
import net.minecraft.client.renderer.GlStateManager.popMatrix
import net.minecraft.client.renderer.GlStateManager.translate
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*

object JumpCircles : Module("JumpCircles", Category.VISUAL, hideModule = false) {
    private val maxTime by int("Max Time", 3000, 2000..8000)
    private val radius by float("Radius", 2f, 1f..3f)
    private val texture by choices("Texture", arrayOf("Supernatural", "Aurora", "Leeches", "Circle"), "Leeches")
    private val deepestLight by boolean("Deepest Light", true)

    private val staticLoc = ResourceLocation("${CLIENT_NAME.lowercase()}/zywl/jumpcircles/default")
    private val animatedLoc = ResourceLocation("${CLIENT_NAME.lowercase()}/zywl/jumpcircles/animated")

    private val circleIcon = ResourceLocation("$staticLoc/circle1.png")
    private val supernaturalIcon = ResourceLocation("$staticLoc/circle2.png")

    private var hasJumped = false
    private val tessellator = Tessellator.getInstance()
    private val worldRenderer = tessellator.worldRenderer
    private val jumpRenderers = mutableListOf<JumpRenderer>()
    private val animatedGroups = listOf(mutableListOf<ResourceLocation>(), mutableListOf())

    init {
        if (animatedGroups.all { it.isEmpty() }) {
            initializeResources()
        }
    }

    private fun initializeResources() {
        val groupFrameLengths = intArrayOf(100, 200)
        val groupFrameFormats = arrayOf("jpeg", "png")

        for (groupIndex in groupFrameLengths.indices.reversed()) {
            val frames = groupFrameLengths[groupIndex]
            val format = groupFrameFormats[groupIndex]

            repeat(frames) { frame ->
                animatedGroups[groupIndex].add(
                    ResourceLocation("$animatedLoc/animation${groupIndex + 1}/circleframe_${frame + 1}.$format")
                )
            }
        }
    }
    private fun selectJumpTexture(index: Int, progress: Float): ResourceLocation {
        val offset = if (texture == "Leeches") progress + 0.6f else progress

        return when (texture) {
            "Aurora", "Leeches" -> {
                val frames = if (texture == "Aurora") animatedGroups[0] else animatedGroups[1]
                val frameProgress = if (texture == "Leeches") {
                    offset % 1f
                } else {
                    ((System.currentTimeMillis() + index).toFloat() % 1500f) / 1500f
                }
                frames[(frameProgress * frames.size).toInt().coerceIn(0, frames.size - 1)]
            }
            "Circle" -> circleIcon
            else -> supernaturalIcon
        }
    }

    private fun createCircleForEntity(entity: Entity) {
        var entityPos = calculateEntityPosition(entity).addVector(0.0, 0.005, 0.0)
        val position = BlockPos(entityPos)
        if (mc.theWorld.getBlockState(position).block == Blocks.snow) {
            entityPos = entityPos.addVector(0.0, 0.125, 0.0)
        }
        jumpRenderers.add(JumpRenderer(entityPos, jumpRenderers.size, System.currentTimeMillis(), maxTime))
    }

    fun clearCircles() {
        jumpRenderers.clear()
    }

    private fun combineColor(alphaFraction: Float): Int {
        val base = ClientThemesUtils.getColor().rgb
        val alphaValue = (255f * alphaFraction.coerceIn(0f, 1f)).toInt()
        return (alphaValue shl 24) or (base and 0xFFFFFF)
    }

    val onUpdate = handler<UpdateEvent> {
        if (!mc.thePlayer.onGround) {
            hasJumped = true
        }
        if (mc.thePlayer.onGround && hasJumped) {
            createCircleForEntity(mc.thePlayer)
            hasJumped = false
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (jumpRenderers.isEmpty()) return@handler
        jumpRenderers.removeAll { it.progress >= 1f }
        if (jumpRenderers.isEmpty()) return@handler

        val lightFactor = if (deepestLight) 1f else 0f
        val effectStrength = when {
            lightFactor >= 1f / 255f -> when (texture) {
                "Circle", "Emission" -> 0.1f
                "Supernatural", "Aurora", "Inusual" -> 0.075f
                "Leeches" -> 0.2f
                else -> 0f
            }
            else -> 0f
        }

        setupDrawCircles {
            jumpRenderers.forEach {
                renderCircle(it.position, radius.toDouble(), 1f - it.progress, it.index * 30, lightFactor, effectStrength)
            }
        }
    }
    private fun renderCircle(pos: Vec3, maxRadius: Double, timeFraction: Float, circleIndex: Int, shift: Float, intensity: Float) {
        val waveValue = 1f - timeFraction
        val wave = easeWave(waveValue)
        var alphaFraction = easeOutCirc(wave.toDouble()).toFloat()
        if (timeFraction < 0.5f) alphaFraction *= easeInOutExpo(alphaFraction.toDouble()).toFloat()
        val mainFactor = if (timeFraction > 0.5f) {
            easeOutElasticX((wave * wave).toDouble())
        } else {
            easeOutBounce(wave.toDouble())
        }
        val circleRadius = (mainFactor * maxRadius).toFloat()
        val rotation = (easeInOutElasticx(wave.toDouble()) * 90.0 / (1.0 + wave.toDouble()))
        val textureResource = selectJumpTexture(circleIndex, timeFraction)
        val color = combineColor(alphaFraction)

        val red = ((color shr 16) and 0xFF) / 255f
        val green = ((color shr 8) and 0xFF) / 255f
        val blue = (color and 0xFF) / 255f
        val alpha = ((color shr 24) and 0xFF) / 255f

        mc.textureManager.bindTexture(textureResource)
        mc.textureManager.getTexture(textureResource).setBlurMipmap(true, true)

        pushMatrix()
        translate(pos.xCoord - circleRadius / 2.0, pos.yCoord, pos.zCoord - circleRadius / 2.0)
        glRotatef(90f, 1f, 0f, 0f)
        customRotatedObject2D(0f, 0f, circleRadius, circleRadius, rotation)
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(0.0, circleRadius.toDouble(), 0.0).tex(0.0, 1.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(circleRadius.toDouble(), circleRadius.toDouble(), 0.0).tex(1.0, 1.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(circleRadius.toDouble(), 0.0, 0.0).tex(1.0, 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        popMatrix()

        if (shift >= 1f / 255f) {
            pushMatrix()
            translate(pos.xCoord, pos.yCoord, pos.zCoord)
            glRotated(rotation, 0.0, 1.0, 0.0)
            worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

            val polygons = 40
            val maxY = circleRadius / 3.5f
            val maxXZ = circleRadius / 7f

            for (i in 1 until polygons) {
                val fraction = i / polygons.toFloat()
                val fractionValue = fraction - (1.5f / polygons)
                val wave2 = easeWave(fractionValue)
                val circVal = easeOutCirc(wave2.toDouble()).toFloat()
                val alphaCheck = (alphaFraction * intensity * shift).takeIf { it * 255 >= 1 } ?: continue
                val alphaInt = (alphaCheck * 255).toInt()
                val variedRadius = circleRadius + circVal * maxXZ

                worldRenderer.pos((-variedRadius / 2f).toDouble(), (maxY * i / polygons - maxY / polygons).toDouble(),
                    (-variedRadius / 2f).toDouble()
                )
                    .tex(0.0, 0.0).color(red, green, blue, alphaInt / 255f).endVertex()
                worldRenderer.pos((-variedRadius / 2f).toDouble(),
                    (maxY * i / polygons - maxY / polygons).toDouble(), (variedRadius / 2f).toDouble()
                )
                    .tex(0.0, 1.0).color(red, green, blue, alphaInt / 255f).endVertex()
                worldRenderer.pos(
                    (variedRadius / 2f).toDouble(), (maxY * i / polygons - maxY / polygons).toDouble(),
                    (variedRadius / 2f).toDouble()
                )
                    .tex(1.0, 1.0).color(red, green, blue, alphaInt / 255f).endVertex()
                worldRenderer.pos((variedRadius / 2f).toDouble(), (maxY * i / polygons - maxY / polygons).toDouble(),
                    (-variedRadius / 2f).toDouble()
                )
                    .tex(1.0, 0.0).color(red, green, blue, alphaInt / 255f).endVertex()
            }
            tessellator.draw()
            popMatrix()
        }
    }

    private fun calculateEntityPosition(entity: Entity): Vec3 {
        val partialTicks = mc.timer.renderPartialTicks
        val dx = entity.posX - entity.lastTickPosX
        val dy = entity.posY - entity.lastTickPosY
        val dz = entity.posZ - entity.lastTickPosZ
        return Vec3(
            entity.lastTickPosX + dx * partialTicks + dx * 2.0,
            entity.lastTickPosY + dy * partialTicks,
            entity.lastTickPosZ + dz * partialTicks + dz * 2.0
        )
    }

    val onWorld = handler<WorldEvent> {
        clearCircles()
    }

    override fun onEnable() {
        clearCircles()
        super.onEnable()
    }

    override fun onDisable() {
        clearCircles()
        super.onDisable()
    }

    class JumpRenderer(
        val position: Vec3,
        val index: Int,
        private val startTime: Long,
        private val maxTimeValue: Int
    ) {
        val progress: Float
            get() = (System.currentTimeMillis() - startTime).toFloat() / maxTimeValue
    }
}