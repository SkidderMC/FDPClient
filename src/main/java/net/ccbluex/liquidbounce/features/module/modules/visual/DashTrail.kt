/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.EntityMovementEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.SmoothStepAnimation
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.extensions.isLookingOnEntity
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.*

object DashTrail : Module("DashTrail", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    init {
        state = true
    }

    private val renderSelf by boolean("RenderSelf", true)
    private val renderPlayers by boolean("Render Players", true)
    private val showDashSegments by boolean("Dash Segments", false)
    private val showDashDots by boolean("Dash Dots", true)
    private val animationTime by int("Anim Time", 20, 100..500)
    private val animationDuration by int("Time", 400, 100..2000)

    private val colorModeOption by choices("Color", arrayOf("Custom", "Theme"), "Custom")
    private val outerColorOption = color("OuterColor", Color(0, 111, 255, 255)) { colorModeOption == "Custom" }

    private val renderOnLook by boolean("OnLook", false)
    private val maxAngleDifference by float("MaxAngleDifference", 90f, 5.0f..90f) { renderOnLook }
    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200)
    private var maxRenderDistanceSq = maxRenderDistance.toDouble().pow(2)

    private const val MIN_ENTITY_SPEED = 0.04
    private const val SPEED_DIVISOR = 0.045
    private const val MIN_DASH_COUNT = 1
    private const val MAX_DASH_COUNT = 16
    private const val POSITION_OFFSET_BASE = 0.0875f
    private const val POSITION_OFFSET_RANGE = 0.175f
    private const val Y_OFFSET_MULTIPLIER = 0.7f

    private val DASH_CUBIC_BLOOM_TEXTURE = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/dashtrail/dashbloomsample.png")
    private val dashCubicTextures: MutableList<TextureResource> = ArrayList()
    private val dashCubicAnimatedTextures: MutableList<MutableList<TextureResource>> = ArrayList()
    private val randomGenerator = Random()
    private val dashCubics: MutableList<DashCubic> = ArrayList()
    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer

    init {
        loadDashCubicTextures()
        loadDashCubicAnimatedTextures()
        randomGenerator.setSeed(1234567891L)
    }

    private fun loadDashCubicTextures() {
        val totalDashTextures = 21
        for (i in 0 until totalDashTextures) {
            dashCubicTextures.add(
                TextureResource(
                    ResourceLocation("${CLIENT_NAME.lowercase()}/texture/dashtrail/dashcubics/dashcubic${i + 1}.png")
                )
            )
        }
    }

    private fun loadDashCubicAnimatedTextures() {
        val animatedDashGroupCounts = intArrayOf(11, 23, 32, 16, 32)
        var groupIndex = 0
        for (dashFragmentCount in animatedDashGroupCounts) {
            groupIndex++
            val animatedTexturesList: MutableList<TextureResource> = ArrayList()
            for (fragIndex in 0 until dashFragmentCount) {
                animatedTexturesList.add(
                    TextureResource(
                        ResourceLocation("${CLIENT_NAME.lowercase()}/texture/dashtrail/dashcubics/group_dashs/group$groupIndex/dashcubic${fragIndex + 1}.png")
                    )
                )
            }
            if (animatedTexturesList.isNotEmpty()) {
                dashCubicAnimatedTextures.add(animatedTexturesList)
            }
        }
    }

    private fun animateColor(baseColor: Color, progress: Float): Color {
        val newAlpha = (baseColor.alpha * (1 - progress)).toInt().coerceIn(0, 255)
        return baseColor.withAlpha(newAlpha)
    }

    private fun getDashCubicColor(dashCubic: DashCubic, alpha: Int): Int {
        val finalAlpha = (dashCubic.animation.output * alpha).toInt().coerceIn(0, 255)
        return when (colorModeOption) {
            "Theme" -> ClientThemesUtils.getColorWithAlpha(0, finalAlpha).rgb
            else -> animateColor(outerColorOption.selectedColor(), ((1f - dashCubic.animation.output).toFloat())).rgb
        }
    }

    private fun getTextureResolution(resource: ResourceLocation): IntArray {
        try {
            val res = mc.resourceManager.getResource(resource)
            res.inputStream.use { stream ->
                val image: BufferedImage = ImageIO.read(stream)
                return intArrayOf(image.width, image.height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return intArrayOf(0, 0)
    }

    private fun getRandomDashCubicTextureIndex(): Int = randomGenerator.nextInt(dashCubicTextures.size)
    private fun getRandomAnimatedTextureGroupIndex(): Int = randomGenerator.nextInt(dashCubicAnimatedTextures.size)
    private fun getDashCubicTextureByIndex(index: Int): TextureResource = dashCubicTextures[index]
    private fun getDashCubicAnimatedTextureGroupByIndex(index: Int): List<TextureResource> =
        dashCubicAnimatedTextures[index]

    private fun shouldUseAnimatedTexture(): Boolean = randomGenerator.nextInt(100) > 40

    private fun getDashRenderOptions(): BooleanArray = booleanArrayOf(showDashSegments, showDashDots)

    private fun withDashRenderState(renderAction: () -> Unit, useTexture2D: Boolean, bloom: Boolean) {
        GL11.glPushMatrix()
        GlStateManager.tryBlendFuncSeparate(770, if (bloom) 32772 else 771, 1, 0)
        GL11.glEnable(3042)
        GL11.glLineWidth(1.0f)
        if (!useTexture2D) {
            GL11.glDisable(3553)
        } else {
            GL11.glEnable(3553)
        }
        GlStateManager.disableLight(0)
        GlStateManager.disableLight(1)
        GlStateManager.disableColorMaterial()
        mc.entityRenderer.disableLightmap()
        GL11.glDisable(2896)
        GL11.glShadeModel(7425)
        GL11.glDisable(3008)
        GL11.glDisable(2884)
        GL11.glDepthMask(false)
        GL11.glTexParameteri(3553, 10241, 9729)
        renderAction()
        GL11.glDepthMask(true)
        GL11.glEnable(2884)
        GL11.glEnable(3008)
        GL11.glLineWidth(1.0f)
        GL11.glShadeModel(7424)
        GL11.glEnable(3553)
        GlStateManager.resetColor()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GL11.glPopMatrix()
    }

    private fun getFilteredDashCubics(): List<DashCubic> = dashCubics
    private fun getAnimationDurationTime(): Int = animationDuration

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer
        if (renderSelf && player != null) {
            val dx = player.posX - player.prevPosX
            val dy = player.posY - player.prevPosY
            val dz = player.posZ - player.prevPosZ
            val entitySpeed = sqrt(dx * dx + dy * dy + dz * dz)
            val dashCount = if (entitySpeed < MIN_ENTITY_SPEED) {
                MIN_DASH_COUNT
            } else {
                MathHelper.clamp_float((entitySpeed / SPEED_DIVISOR).toInt().toFloat(), MIN_DASH_COUNT.toFloat(), MAX_DASH_COUNT.toFloat()).toInt()
            }
            val renderOptions = getDashRenderOptions()
            for (i in 0 until dashCount) {
                dashCubics.add(
                    DashCubic(
                        DashBase(
                            player,
                            0.04f,
                            DashTexture(true),
                            i.toFloat() / dashCount,
                            getAnimationDurationTime()
                        ),
                        renderOptions[0] || renderOptions[1]
                    )
                )
            }
        }

        dashCubics.removeIf { it.animation.finished(Direction.BACKWARDS) }
        dashCubics.forEach { it.processMotion(null) }
    }

    val onEntityMove = handler<EntityMovementEvent> { event ->
        if (event.movedEntity !is EntityLivingBase) return@handler

        if (!renderPlayers && event.movedEntity != mc.thePlayer) return@handler

        val distanceSq = mc.thePlayer.getDistanceSqToEntity(event.movedEntity)
        if (distanceSq > maxRenderDistanceSq) return@handler

        if (renderOnLook && !mc.thePlayer.isLookingOnEntity(event.movedEntity, maxAngleDifference.toDouble())) return@handler

        if (event.movedEntity == mc.thePlayer && !renderSelf) return@handler

        val targetEntity = event.movedEntity
        val previousPos = Vec3(targetEntity.prevPosX, targetEntity.prevPosY, targetEntity.prevPosZ)
        val currentPos = targetEntity.positionVector
        val dx = currentPos.xCoord - previousPos.xCoord
        val dy = currentPos.yCoord - previousPos.yCoord
        val dz = currentPos.zCoord - previousPos.zCoord
        val entitySpeed = sqrt(dx * dx + dy * dy + dz * dz)
        val entitySpeedXZ = sqrt(dx * dx + dz * dz)

        if (targetEntity != mc.thePlayer && entitySpeedXZ < MIN_ENTITY_SPEED) return@handler

        val animated = true
        val renderOptions = getDashRenderOptions()
        val dashCount = MathHelper.clamp_float((entitySpeed / SPEED_DIVISOR).toInt().toFloat(), MIN_DASH_COUNT.toFloat(), MAX_DASH_COUNT.toFloat()).toInt()

        for (i in 0 until dashCount) {
            dashCubics.add(
                DashCubic(
                    DashBase(
                        targetEntity,
                        0.04f,
                        DashTexture(animated),
                        i.toFloat() / dashCount,
                        getAnimationDurationTime()
                    ),
                    renderOptions[0] || renderOptions[1]
                )
            )
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        val partialTicks = event.partialTicks
        val frustum = Frustum().apply {
            setPosition(mc.renderViewEntity.posX, mc.renderViewEntity.posY, mc.renderViewEntity.posZ)
        }
        val renderOptions = getDashRenderOptions()

        val filteredCubics = getFilteredDashCubics().filter { dashCubic ->
            val entity = dashCubic.base.entity

            if(!renderPlayers && entity != mc.thePlayer) return@filter false

            val distanceSq = mc.thePlayer.getDistanceSqToEntity(entity)
            if (distanceSq > maxRenderDistanceSq) return@filter false
            if (renderOnLook && !mc.thePlayer.isLookingOnEntity(entity, maxAngleDifference.toDouble())) return@filter false

            val x = dashCubic.getRenderPosX(partialTicks)
            val y = dashCubic.getRenderPosY(partialTicks)
            val z = dashCubic.getRenderPosZ(partialTicks)
            val bbox = AxisAlignedBB(x, y, z, x, y, z).expand(
                0.2 * dashCubic.animation.output,
                0.2 * dashCubic.animation.output,
                0.2 * dashCubic.animation.output
            )
            frustum.isBoundingBoxInFrustum(bbox)
        }

        if (renderOptions[0] || renderOptions[1]) {
            GL11.glTranslated(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ)
            if (renderOptions[1]) {
                withDashRenderState({
                    GL11.glEnable(2832)
                    GL11.glPointSize(2.0f)
                    GL11.glBegin(GL11.GL_POINTS)
                    filteredCubics.forEach { dashCubic ->
                        val renderDashPos = doubleArrayOf(
                            dashCubic.getRenderPosX(partialTicks),
                            dashCubic.getRenderPosY(partialTicks),
                            dashCubic.getRenderPosZ(partialTicks)
                        )
                        dashCubic.dashSparks.forEach { spark ->
                            val renderSparkPos = doubleArrayOf(
                                spark.getRenderPosX(partialTicks),
                                spark.getRenderPosY(partialTicks),
                                spark.getRenderPosZ(partialTicks)
                            )
                            val color = ColorUtils.interpolateColor(
                                getDashCubicColor(dashCubic, 255),
                                -1,
                                dashCubic.animation.output.toFloat()
                            )
                            RenderUtils.color(color)
                            GL11.glVertex3d(
                                renderSparkPos[0] + renderDashPos[0],
                                renderSparkPos[1] + renderDashPos[1],
                                renderSparkPos[2] + renderDashPos[2]
                            )
                            GL11.glVertex3d(
                                -renderSparkPos[0] + renderDashPos[0],
                                -renderSparkPos[1] + renderDashPos[1],
                                -renderSparkPos[2] + renderDashPos[2]
                            )
                        }
                    }
                    GL11.glEnd()
                }, useTexture2D = false, bloom = false)
            }
            if (renderOptions[0]) {
                withDashRenderState({
                    filteredCubics.forEach { dashCubic ->
                        val renderDashPos = doubleArrayOf(
                            dashCubic.getRenderPosX(partialTicks),
                            dashCubic.getRenderPosY(partialTicks),
                            dashCubic.getRenderPosZ(partialTicks)
                        )
                        GL11.glBegin(GL11.GL_QUADS)
                        dashCubic.dashSparks.forEach { spark ->
                            val renderSparkPos = doubleArrayOf(
                                spark.getRenderPosX(partialTicks),
                                spark.getRenderPosY(partialTicks),
                                spark.getRenderPosZ(partialTicks)
                            )
                            val color = ColorUtils.interpolateColor(
                                getDashCubicColor(dashCubic, 255),
                                -1,
                                (1 - dashCubic.animation.output).toFloat()
                            )
                            RenderUtils.color(color)
                            GL11.glVertex3d(
                                renderSparkPos[0] + renderDashPos[0],
                                renderSparkPos[1] + renderDashPos[1],
                                renderSparkPos[2] + renderDashPos[2]
                            )
                            GL11.glVertex3d(
                                -renderSparkPos[0] + renderDashPos[0],
                                -renderSparkPos[1] + renderDashPos[1],
                                -renderSparkPos[2] + renderDashPos[2]
                            )
                        }
                        GL11.glEnd()
                    }
                }, useTexture2D = false, bloom = true)
            }
            GL11.glTranslated(mc.renderManager.viewerPosX, mc.renderManager.viewerPosY, mc.renderManager.viewerPosZ)
        }

        if (filteredCubics.isNotEmpty()) {
            withDashRenderState({
                GL11.glTranslated(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ)
                filteredCubics.forEach { dashCubic ->
                    dashCubic.drawDash(partialTicks, isBloomRenderer = false)
                }
                bindResource(DASH_CUBIC_BLOOM_TEXTURE)
                filteredCubics.forEach { dashCubic ->
                    dashCubic.drawDash(partialTicks, isBloomRenderer = true)
                }
            }, useTexture2D = true, bloom = true)
        }
    }

    private fun bindResource(resource: ResourceLocation) {
        mc.textureManager.bindTexture(resource)
    }

    private fun drawBoundTexture(
        x: Float,
        y: Float,
        x2: Float,
        y2: Float,
        c1: Int,
        c2: Int,
        c3: Int,
        c4: Int
    ) {
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0)
            .tex(0.0, 0.0)
            .color((c1 shr 16) and 0xFF, (c1 shr 8) and 0xFF, c1 and 0xFF, (c1 shr 24) and 0xFF)
            .endVertex()
        worldRenderer.pos(x.toDouble(), y2.toDouble(), 0.0)
            .tex(0.0, 1.0)
            .color((c2 shr 16) and 0xFF, (c2 shr 8) and 0xFF, c2 and 0xFF, (c2 shr 24) and 0xFF)
            .endVertex()
        worldRenderer.pos(x2.toDouble(), y2.toDouble(), 0.0)
            .tex(1.0, 1.0)
            .color((c3 shr 16) and 0xFF, (c3 shr 8) and 0xFF, c3 and 0xFF, (c3 shr 24) and 0xFF)
            .endVertex()
        worldRenderer.pos(x2.toDouble(), y.toDouble(), 0.0)
            .tex(1.0, 0.0)
            .color((c4 shr 16) and 0xFF, (c4 shr 8) and 0xFF, c4 and 0xFF, (c4 shr 24) and 0xFF)
            .endVertex()
        tessellator.draw()
    }

    private fun drawBoundTexture(x: Float, y: Float, x2: Float, y2: Float, c: Int) {
        drawBoundTexture(x, y, x2, y2, c, c, c, c)
    }

    private fun with3DDashPosition(renderPos: DoubleArray, renderPart: () -> Unit, rotationValues: FloatArray) {
        GL11.glPushMatrix()
        GL11.glTranslated(renderPos[0], renderPos[1], renderPos[2])
        GL11.glRotated(-rotationValues[0].toDouble(), 0.0, 1.0, 0.0)
        GL11.glRotated(rotationValues[1].toDouble(), if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0, 0.0, 0.0)
        GL11.glScaled(-0.1, -0.1, 0.1)
        renderPart()
        GL11.glPopMatrix()
    }

    private fun addDashSpark(segment: DashCubic) {
        segment.dashSparks.add(DashSpark())
    }

    private fun removeFinishedDashSparks(segment: DashCubic) {
        if (segment.dashSparks.isNotEmpty()) {
            if (segment.addExtras) {
                segment.dashSparks.removeIf { segment.animation.finished(Direction.BACKWARDS) }
            } else {
                segment.dashSparks.clear()
            }
        }
    }

    private class TextureResource(val resource: ResourceLocation) {
        val resolution: IntArray = getTextureResolution(resource)
    }

    private class DashCubic(val base: DashBase, val addExtras: Boolean) {
        var animation: SmoothStepAnimation = SmoothStepAnimation(animationTime, 1.0, Direction.FORWARDS)
        private val rotationAngles = floatArrayOf(0.0f, 0.0f)
        val dashSparks: MutableList<DashSpark> = ArrayList()

        init {
            if (sqrt(base.motionX * base.motionX + base.motionZ * base.motionZ) < 5.0E-4) {
                rotationAngles[0] = (360.0 * Math.random()).toFloat()
                rotationAngles[1] = mc.renderManager.playerViewX
            } else {
                val motionYaw = base.getMotionYaw()
                rotationAngles[0] = motionYaw - 45.0f - 15.0f - (base.entity.prevRotationYaw - base.entity.rotationYaw) * 3.0f
                val currentRotYaw = RotationUtils.currentRotation?.yaw ?: base.entity.rotationYaw
                val yawDiff = MathHelper.wrapAngleTo180_float((motionYaw + 26.3f) - currentRotYaw)
                rotationAngles[1] = if (yawDiff < 10.0f || yawDiff > 160.0f) -90.0f else mc.renderManager.playerViewX
            }
        }

        fun getRenderPosX(partialTicks: Float): Double = base.posX
        fun getRenderPosY(partialTicks: Float): Double = base.posY
        fun getRenderPosZ(partialTicks: Float): Double = base.posZ

        fun processMotion(nextSegment: DashCubic?) {
            base.prevPosX = base.posX
            base.prevPosY = base.posY
            base.prevPosZ = base.posZ

            if (addExtras) {
                if (randomGenerator.nextInt(12) > 5) {
                    repeat(if (getDashRenderOptions()[0]) 1 else 3) { addDashSpark(this) }
                }
                dashSparks.forEach { it.processMotion() }
            }
            removeFinishedDashSparks(this)
            if (animation.timerUtil.hasTimeElapsed(getAnimationDurationTime().toLong())) {
                animation.setDirection(Direction.BACKWARDS)
            }
        }

        fun drawDash(partialTicks: Float, isBloomRenderer: Boolean) {
            val textureResource = base.dashTexture.getResourceWithSizes()
            val scale = 0.02f * animation.output
            val extX = textureResource.resolution[0] * scale
            val extY = textureResource.resolution[1] * scale
            val renderPos = doubleArrayOf(
                getRenderPosX(partialTicks),
                getRenderPosY(partialTicks),
                getRenderPosZ(partialTicks)
            )

            if (isBloomRenderer) {
                with3DDashPosition(renderPos, {
                    val extXY = sqrt((extX * extX + extY * extY)).toFloat()
                    drawBoundTexture(-extXY * 2.0f, -extXY * 2.0f, extXY * 2.0f, extXY * 2.0f, getDashCubicColor(this@DashCubic, 64))
                }, floatArrayOf(mc.renderManager.playerViewY, mc.renderManager.playerViewX))
            } else {
                with3DDashPosition(renderPos, {
                    bindResource(textureResource.resource)
                    drawBoundTexture(
                        ((-extX / 2.0f).toFloat()),
                        ((-extY / 2.0f).toFloat()),
                        ((extX / 2.0f).toFloat()),
                        ((extY / 2.0f).toFloat()),
                        ColorUtils.darker(getDashCubicColor(this@DashCubic, 64), 1.0f)
                    )
                }, rotationAngles)
            }
        }
    }

    private class DashBase(
        val entity: EntityLivingBase,
        speedFactor: Float,
        val dashTexture: DashTexture,
        offsetTickPercentage: Float,
        rmTime: Int
    ) {
        var motionX: Double = calculateMotionX()
        var motionY: Double = calculateMotionY()
        var motionZ: Double = calculateMotionZ()
        var posX: Double = entity.lastTickPosX - motionX * offsetTickPercentage + ( -POSITION_OFFSET_BASE + POSITION_OFFSET_RANGE * Math.random() )
        var posY: Double = entity.lastTickPosY - motionY * offsetTickPercentage + (entity.height / 3.0 + entity.height / 4.0 * Math.random() * Y_OFFSET_MULTIPLIER)
        var posZ: Double = entity.lastTickPosZ - motionZ * offsetTickPercentage + ( -POSITION_OFFSET_BASE + POSITION_OFFSET_RANGE * Math.random() )
        var prevPosX: Double = posX
        var prevPosY: Double = posY
        var prevPosZ: Double = posZ

        private fun calculateMotionX(): Double = -(entity.prevPosX - entity.posX)
        private fun calculateMotionY(): Double = -(entity.prevPosY - entity.posY)
        private fun calculateMotionZ(): Double = -(entity.prevPosZ - entity.posZ)

        init {
            motionX *= speedFactor
            motionY *= speedFactor
            motionZ *= speedFactor
        }

        fun getMotionYaw(): Float {
            var motionYaw = Math.toDegrees(atan2(motionZ, motionX) - Math.toRadians(90.0)).toFloat()
            if (motionYaw < 0) motionYaw += 360f
            return motionYaw
        }
    }

    private class DashTexture(animated: Boolean) {
        val textures: MutableList<TextureResource>
        val isAnimated: Boolean = animated && shouldUseAnimatedTexture()
        var spawnTime: Long = 0
        var animationInterval: Long = 0

        init {
            if (this.isAnimated) {
                spawnTime = System.currentTimeMillis()
                textures = getDashCubicAnimatedTextureGroupByIndex(getRandomAnimatedTextureGroupIndex()).toMutableList()
                animationInterval = getAnimationDurationTime().toLong()
            } else {
                textures = ArrayList()
                textures.add(getDashCubicTextureByIndex(getRandomDashCubicTextureIndex()))
            }
        }

        fun getResourceWithSizes(): TextureResource {
            if (isAnimated) {
                val fragCount = textures.size.toFloat()
                if (fragCount > 0f) {
                    val timeDiff = (System.currentTimeMillis() - spawnTime) % animationInterval
                    val index = MathHelper.clamp_float((timeDiff.toFloat() / animationInterval.toFloat()) * fragCount, 0f, fragCount)
                    textures.getOrNull(index.toInt())?.let { return it }
                }
            }
            return textures[0]
        }
    }

    private class DashSpark {
        var posX: Double = 0.0
        var posY: Double = 0.0
        var posZ: Double = 0.0
        var prevPosX: Double = 0.0
        var prevPosY: Double = 0.0
        var prevPosZ: Double = 0.0
        var speed: Double = Math.random() / 50.0
        var radianYaw: Double = Math.random() * 360.0
        var radianPitch: Double = -90.0 + Math.random() * 180.0

        fun processMotion() {
            val radYaw = Math.toRadians(radianYaw)
            prevPosX = posX
            prevPosY = posY
            prevPosZ = posZ
            posX += sin(radYaw) * speed
            posY += cos(Math.toRadians(radianPitch - 90.0)) * speed
            posZ += cos(radYaw) * speed
        }

        fun getRenderPosX(partialTicks: Float): Double =
            prevPosX + (posX - prevPosX) * partialTicks.toDouble()

        fun getRenderPosY(partialTicks: Float): Double =
            prevPosY + (posY - prevPosY) * partialTicks.toDouble()

        fun getRenderPosZ(partialTicks: Float): Double =
            prevPosZ + (posZ - prevPosZ) * partialTicks.toDouble()
    }
}
