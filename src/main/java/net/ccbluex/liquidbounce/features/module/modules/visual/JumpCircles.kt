/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.customRotatedObject2D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.setupDrawCircles
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeInOutElasticx
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeInOutExpo
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeOutBounce
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeOutCirc
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeOutElasticX
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeWave
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.float
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.util.*

object JumpCircles : Module("JumpCircles", Category.VISUAL, hideModule = false) {

    private val maxTime by int("Max Time", 3000, 2000..8000)
    private val radius by float("Radius", 2f, 1f..3f)

    private val texture by choices("Texture", arrayOf("Supernatural", "Aurora", "Leeches", "Circle"), "Leeches")
    private val deepestLight by boolean("Deepest Light", true)

    private val staticLoc = ResourceLocation("${CLIENT_NAME.lowercase()}/zywl/jumpcircles/default")
    private val animatedLoc = ResourceLocation("${CLIENT_NAME.lowercase()}/zywl/jumpcircles/animated")

    private val circleIcon = ResourceLocation("$staticLoc/circle1.png")
    private val supernaturalIcon = ResourceLocation("$staticLoc/circle2.png")

    private var jump = false

    private val tessellator = Tessellator.getInstance()
    val worldRenderer = tessellator.worldRenderer

    private val circles: MutableList<JumpRenderer> = ArrayList()

    private val animatedGroups: MutableList<MutableList<ResourceLocation>> = mutableListOf(mutableListOf(), mutableListOf())

    init {
        if (animatedGroups.any { it.isEmpty() }) {
            initializeResources()
        }
    }

    private fun initializeResources() {
        val groupFrameLengths = intArrayOf(100, 200)
        val groupFrameFormats = arrayOf("jpeg", "png")

        if (animatedGroups.all { it.isEmpty() }) {
            for (groupIndex in groupFrameLengths.indices.reversed()) {
                val groupFrames = groupFrameLengths[groupIndex]
                val format = groupFrameFormats[groupIndex]

                for (frame in 1..groupFrames) {
                    val location = ResourceLocation(
                        "$animatedLoc/animation${groupIndex + 1}/circleframe_$frame.$format"
                    )
                    animatedGroups[groupIndex].add(location)
                }
            }
        }
    }

    private fun jumpTexture(index: Int, progress: Float): ResourceLocation {
        val adjustedProgress = if (texture == "Leeches") progress + 0.6f else progress

        return when (texture) {
            "Aurora", "Leeches" -> {
                val currentGroup = if (texture == "Aurora") animatedGroups[0] else animatedGroups[1]
                val frameOffset = (if (texture == "Leeches") adjustedProgress % 1f
                else (System.currentTimeMillis() + index) % 1500 / 1500f)
                currentGroup[(frameOffset * currentGroup.size).toInt().coerceIn(0, currentGroup.size - 1)]
            }
            "Circle" -> circleIcon
            else -> supernaturalIcon
        }
    }

    private fun addCircleForEntity(entity: Entity) {
        var vec = getVec3dFromEntity(entity).add(Vec3(0.0, 0.005, 0.0))

        val position = BlockPos(vec)
        val blockState = mc.theWorld.getBlockState(position)
        if (blockState.block === Blocks.snow) {
            vec = vec.add(Vec3(0.0, 0.125, 0.0))
        }

        circles.add(JumpRenderer(vec, circles.size))
    }

    fun reset() {
        if (circles.isNotEmpty()) circles.clear()
    }

    fun getColor(alphaPC: Float): Int {
        val baseColor = ClientThemesUtils.getColor().rgb
        val alphaInt = (255f * alphaPC.coerceIn(0f, 1f)).toInt()
        return (alphaInt shl 24) or (baseColor and 0xFFFFFF)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (!mc.thePlayer.onGround) {
            jump = true
        }
        if (mc.thePlayer.onGround && jump) {
            addCircleForEntity(mc.thePlayer)
            jump = false
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circles.isEmpty()) return
        circles.removeIf { it.progress >= 1.0f }
        if (circles.isEmpty()) return

        val deepestLightAnimation = if (deepestLight) 1f else 0f
        val immersiveStrength = when {
            deepestLightAnimation >= 1f / 255f -> {
                when (texture) {
                    "Circle", "Emission" -> 0.1f
                    "Supernatural", "Aurora", "Inusual" -> 0.075f
                    "Leeches" -> 0.2f
                    else -> 0f
                }
            }
            else -> 0f
        }

        setupDrawCircles {
            circles.forEach { circle ->
                doCircle(
                    circle.position,
                    radius.toDouble(),
                    1f - circle.progress,
                    circle.index * 30,
                    deepestLightAnimation,
                    immersiveStrength
                )
            }
        }
    }

    private fun doCircle(
        position: Vec3,
        maxRadius: Double,
        deltaTime: Float,
        index: Int,
        immersiveShift: Float,
        immersiveIntensity: Float
    ) {
        val immersive = immersiveShift >= 1f / 255f
        val waveDelta = easeWave(1f - deltaTime)
        var alphaPC = easeOutCirc(waveDelta.toDouble()).toFloat()
        if (deltaTime < 0.5f) alphaPC *= easeInOutExpo(alphaPC.toDouble()).toFloat()

        val radius = ((if (deltaTime > 0.5f) easeOutElasticX((waveDelta * waveDelta).toDouble())
        else easeOutBounce(waveDelta.toDouble())) * maxRadius).toFloat()
        val rotation = easeInOutElasticx(waveDelta.toDouble()) * 90.0 / (1.0 + waveDelta)
        val resource = jumpTexture(index, deltaTime)

        val color = getColor(alphaPC)
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        val alpha = (color shr 24 and 0xFF) / 255.0f

        mc.textureManager.bindTexture(resource)
        mc.textureManager.getTexture(resource).setBlurMipmap(true, true)
        pushMatrix()
        translate(position.xCoord - radius / 2.0, position.yCoord, position.zCoord - radius / 2.0)
        glRotatef(90.0f, 1.0f, 0.0f, 0.0f)
        customRotatedObject2D(0F, 0F, radius, radius, rotation)
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(0.0, radius.toDouble(), 0.0).tex(0.0, 1.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(radius.toDouble(), radius.toDouble(), 0.0).tex(1.0, 1.0).color(red, green, blue, alpha).endVertex()
        worldRenderer.pos(radius.toDouble(), 0.0, 0.0).tex(1.0, 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        popMatrix()

        if (immersive) {
            pushMatrix()
            translate(position.xCoord, position.yCoord, position.zCoord)
            glRotated(rotation, 0.0, 1.0, 0.0)
            worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
            val polygons = 40f
            val extMaxY = radius / 3.5f
            val extMaxXZ = radius / 7f
            for (i in 1 until polygons.toInt()) {
                val iPC = i / polygons
                val extY = extMaxY * i / polygons - extMaxY / polygons
                val aPC = (alphaPC * immersiveIntensity * immersiveShift).takeIf { it * 255 >= 1 } ?: continue
                val radiusPost = radius + easeOutCirc(easeWave(iPC - 1.5f / polygons).toDouble()).toFloat() * extMaxXZ
                val alphaInt = (aPC * 255).toInt()
                worldRenderer.pos((-radiusPost / 2f).toDouble(), extY.toDouble(), (-radiusPost / 2f).toDouble())
                    .tex(0.0, 0.0).color(red, green, blue, alphaInt / 255.0f).endVertex()
                worldRenderer.pos((-radiusPost / 2f).toDouble(), extY.toDouble(), (radiusPost / 2f).toDouble())
                    .tex(0.0, 1.0).color(red, green, blue, alphaInt / 255.0f).endVertex()
                worldRenderer.pos((radiusPost / 2f).toDouble(), extY.toDouble(), (radiusPost / 2f).toDouble())
                    .tex(1.0, 1.0).color(red, green, blue, alphaInt / 255.0f).endVertex()
                worldRenderer.pos((radiusPost / 2f).toDouble(), extY.toDouble(), (-radiusPost / 2f).toDouble())
                    .tex(1.0, 0.0).color(red, green, blue, alphaInt / 255.0f).endVertex()
            }
            tessellator.draw()
            popMatrix()
        }
    }

    private fun getVec3dFromEntity(entity: Entity): Vec3 {
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

    @EventTarget
    fun onWorld(event: WorldEvent) {
        reset()
    }

    override fun onEnable() {
        reset()
        super.onEnable()
    }

    override fun onDisable() {
        reset()
        super.onDisable()
    }

    class JumpRenderer(val position: Vec3, val index: Int) {
        private val startTime = System.currentTimeMillis()
        val progress: Float
            get() = ((System.currentTimeMillis() - startTime) / maxTime.toFloat())
    }
}