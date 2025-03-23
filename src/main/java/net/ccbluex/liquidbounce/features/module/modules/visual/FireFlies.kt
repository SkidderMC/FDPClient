/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.extensions.lerp
import net.ccbluex.liquidbounce.utils.extensions.randomizeDouble
import net.ccbluex.liquidbounce.utils.render.ColorUtils.applyOpacity
import net.ccbluex.liquidbounce.utils.render.ColorUtils.darker
import net.ccbluex.liquidbounce.utils.render.ColorUtils.getAlphaFromColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils.interpolateColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.color
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// made by opZywl
object FireFlies : Module("FireFlies", Category.VISUAL, gameDetecting = false) {

    init {
        state = true
    }

    private val darkImprint by boolean("DarkImprint", false)
    private val lighting by boolean("Lighting", false)
    private val spawnDelay by float("SpawnDelay", 3.0f, 1.0f..10.0f)

    private val partList = ArrayList<FirePart>()
    private val icon = ResourceLocation("${CLIENT_NAME.lowercase()}/firepart.png")

    private val tessellator = Tessellator.getInstance()
    private val buffer = tessellator.worldRenderer

    private val maxPartAliveTime: Long
        get() = 6000L

    private val partColor: Int
        get() = ClientThemesUtils.getColor().rgb

    private fun getRandom(min: Double, max: Double): Float {
        return randomizeDouble(min, max).toFloat()
    }

    private fun generateVecForPart(rangeXZ: Double, rangeY: Double): Vec3 {
        var pos = mc.thePlayer.positionVector.addVector(
            getRandom(-rangeXZ, rangeXZ).toDouble(),
            getRandom(-rangeY / 2.0, rangeY).toDouble(),
            getRandom(-rangeXZ, rangeXZ).toDouble()
        )
        repeat(30) {
            pos = mc.thePlayer.positionVector.addVector(
                getRandom(-rangeXZ, rangeXZ).toDouble(),
                getRandom(-rangeY / 2.0, rangeY).toDouble(),
                getRandom(-rangeXZ, rangeXZ).toDouble()
            )
        }
        return pos
    }

    private fun setupGLDrawsFireParts(partsRender: Runnable) {
        val glX: Double = mc.renderManager.viewerPosX
        val glY: Double = mc.renderManager.viewerPosY
        val glZ: Double = mc.renderManager.viewerPosZ
        glPushMatrix()
        tryBlendFuncSeparate(770, 1, 1, 0)
        mc.entityRenderer.disableLightmap()
        glEnable(GL_BLEND)
        glLineWidth(1.0f)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glShadeModel(GL_SMOOTH)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_CULL_FACE)
        glDepthMask(false)
        glTranslated(-glX, -glY, -glZ)
        partsRender.run()
        glTranslated(glX, glY, glZ)
        glDepthMask(true)
        glEnable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glLineWidth(1.0f)
        glShadeModel(GL_FLAT)
        glEnable(GL_TEXTURE_2D)
        resetColor()
        tryBlendFuncSeparate(770, 771, 1, 0)
        glPopMatrix()
    }

    private fun bindResource(toBind: ResourceLocation) {
        mc.textureManager.bindTexture(toBind)
    }

    private fun drawBindedTexture(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        val alpha = (color shr 24 and 0xFF) / 255.0f

        buffer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(x.toDouble(), y2.toDouble(), 0.0).tex(0.0, 1.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(x2.toDouble(), y2.toDouble(), 0.0).tex(1.0, 1.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(x2.toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
    }

    private fun drawPart(part: FirePart, partialTicks: Float, renderedParts: MutableSet<FirePart>) {
        if (renderedParts.contains(part)) return
        renderedParts.add(part)

        val color = partColor
        if (darkImprint) {
            tryBlendFuncSeparate(770, 771, 1, 0)
            drawSparkPartsList(color, part, partialTicks)
            drawTrailPartsList(color, part)
            tryBlendFuncSeparate(770, 1, 1, 0)
        } else {
            drawSparkPartsList(color, part, partialTicks)
            drawTrailPartsList(color, part)
        }

        val pos = part.getRenderPosVec(partialTicks)
        glPushMatrix()
        glTranslated(pos.xCoord, pos.yCoord, pos.zCoord)
        glNormal3d(1.0, 1.0, 1.0)
        glRotated((-mc.renderManager.playerViewY).toDouble(), 0.0, 1.0, 0.0)
        glRotated(
            mc.renderManager.playerViewX.toDouble(),
            if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0,
            0.0,
            0.0
        )
        glScaled(-0.1, -0.1, 0.1)

        val scale = 7.0f

        drawBindedTexture(-scale / 2.0f, -scale / 2.0f, scale / 2.0f, scale / 2.0f, color)

        if (lighting) {
            val lightingScale = scale * 3.0f
            drawBindedTexture(
                -lightingScale / 2.0f,
                -lightingScale / 2.0f,
                lightingScale / 2.0f,
                lightingScale / 2.0f,
                applyOpacity(
                    darker(color, 0.4f),
                    getAlphaFromColor(color).toFloat() / 5.0f
                )
            )
        }

        glPopMatrix()
    }


    val onUpdate = handler<UpdateEvent> {
        if (mc.thePlayer != null && mc.thePlayer.ticksExisted == 1) {
            partList.forEach { it.setToRemove() }
        }

        val currentTime = System.currentTimeMillis()
        partList.forEach { it.updatePart() }
        partList.removeIf { it.toRemove || (currentTime - it.startTime) >= maxPartAliveTime }

        if (mc.thePlayer.ticksExisted % (spawnDelay.toInt() + 1) == 0) {
            partList.add(
                FirePart(
                    generateVecForPart(10.0, 4.0),
                    maxPartAliveTime.toFloat(),
                    ::getRandom
                )
            )
            partList.add(
                FirePart(
                    generateVecForPart(6.0, 5.0),
                    maxPartAliveTime.toFloat(),
                    ::getRandom
                )
            )
        }
    }


    val onRender3D = handler<Render3DEvent> { event ->
        if (partList.isNotEmpty()) {
            setupGLDrawsFireParts {
                bindResource(icon)
                val renderedParts = mutableSetOf<FirePart>()
                partList.forEach { part ->
                    drawPart(part, event.partialTicks, renderedParts)
                }
            }
        }
    }

    private fun drawSparkPartsList(color: Int, firePart: FirePart, partialTicks: Float) {
        if (firePart.sparkParts.size < 2) return

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_POINT_SMOOTH)

        val smoothDistance = mc.thePlayer?.let {
            val interpolatedX = it.lastTickPosX + (it.posX - it.lastTickPosX) * partialTicks
            val interpolatedY = it.lastTickPosY + (it.posY - it.lastTickPosY) * partialTicks
            val interpolatedZ = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * partialTicks

            val deltaX = interpolatedX - firePart.posVec.xCoord
            val deltaY = interpolatedY - (firePart.posVec.yCoord + 1.6f)
            val deltaZ = interpolatedZ - firePart.posVec.zCoord

            sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toFloat()
        } ?: 0f

        glPointSize(
            1.5f + 6.0f * MathHelper.clamp_float(
                1.0f - (smoothDistance - 3.0f) / 10.0f,
                0.0f,
                1.0f
            )
        )

        glBegin(GL_POINTS)
        for (spark in firePart.sparkParts) {
            val c = applyOpacity(
                interpolateColor(-1, color, spark.timePC().toFloat()),
                getAlphaFromColor(color) * (1.0f - spark.timePC().toFloat())
            )
            color(c)
            glVertex3d(
                spark.getRenderPosX(partialTicks),
                spark.getRenderPosY(partialTicks),
                spark.getRenderPosZ(partialTicks)
            )
        }
        glEnd()
        resetColor()
        glEnable(GL_ALPHA_TEST)
        glEnable(GL_TEXTURE_2D)
    }

    private fun drawTrailPartsList(color: Int, firePart: FirePart) {
        if (firePart.trailParts.size < 2) return

        glDisable(GL_TEXTURE_2D)

        val smoothDistance = mc.thePlayer?.let {
            val interpolatedX = it.lastTickPosX + (it.posX - it.lastTickPosX) * mc.timer.renderPartialTicks
            val interpolatedY = it.lastTickPosY + (it.posY - it.lastTickPosY) * mc.timer.renderPartialTicks
            val interpolatedZ = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * mc.timer.renderPartialTicks

            val deltaX = interpolatedX - firePart.posVec.xCoord
            val deltaY = interpolatedY - (firePart.posVec.yCoord + 1.6f)
            val deltaZ = interpolatedZ - firePart.posVec.zCoord

            sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toFloat()
        } ?: 0f

        glLineWidth(
            1.0E-5f + 8.0f * MathHelper.clamp_float(
                1.0f - (smoothDistance - 3.0f) / 20.0f,
                0.0f,
                1.0f
            )
        )

        glEnable(GL_BLEND)
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        glBegin(GL_LINE_STRIP)
        firePart.trailParts.forEachIndexed { index, trail ->
            val sizePC = (index.toFloat() / firePart.trailParts.size).let {
                if (it > 0.5) 1.0f - it else it
            } * 2.0f
            val c = applyOpacity(color, getAlphaFromColor(color) * sizePC)
            color(c)
            glVertex3d(trail.x, trail.y, trail.z)
        }
        glEnd()

        resetColor()
        glEnable(GL_ALPHA_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glLineWidth(1.0f)
        glEnable(GL_TEXTURE_2D)
    }

    private class FirePart(
        var posVec: Vec3,
        maxAlive: Float,
        private val getRandom: (Double, Double) -> Float
    ) {
        var trailParts: MutableList<TrailPart> = mutableListOf()
        var sparkParts: MutableList<SparkPart> = mutableListOf()
        var prevPos: Vec3
        var anim: Float = 0.0f
        var animTo: Float = 1.0f
        var animSpeed: Float = 0.02f
        var msChangeSideRate: Int = calculateMsChangeSideRate()
        var moveYawSet: Float = getRandom(0.0, 360.0)
        var speed: Float = getRandom(0.1, 0.25)
        var yMotion: Float = getRandom(-0.075, 0.1)
        var moveYaw: Float = moveYawSet
        var maxAlive: Float
        var startTime: Long = System.currentTimeMillis()
        var rateTimer: Long = System.currentTimeMillis()
        var toRemove: Boolean = false

        init {
            prevPos = posVec
            this.maxAlive = maxAlive
        }

        val timePC: Float
            get() = MathHelper.clamp_float(
                (System.currentTimeMillis() - startTime).toFloat() / maxAlive,
                0.0f,
                1.0f
            )

        fun setAlphaPCTo(to: Float) {
            animTo = to
        }

        fun getAlphaPC(): Float {
            return anim
        }

        fun getRenderPosVec(pTicks: Float): Vec3 {
            return posVec.addVector(
                -(prevPos.xCoord - posVec.xCoord) * pTicks.toDouble(),
                -(prevPos.yCoord - posVec.yCoord) * pTicks.toDouble(),
                -(prevPos.zCoord - posVec.zCoord) * pTicks.toDouble()
            )
        }

        // by opZywl - FireFlies 

        fun updatePart() {
            anim += (animTo - anim) * animSpeed
            anim = MathHelper.clamp_float(anim, 0.0f, 1.0f)

            if (System.currentTimeMillis() - this.rateTimer >= msChangeSideRate.toLong()) {
                this.msChangeSideRate = calculateMsChangeSideRate()
                this.rateTimer = System.currentTimeMillis()
                this.moveYawSet = getRandom(0.0, 360.0)
            }

            this.moveYaw = lerp(this.moveYaw, this.moveYawSet, 0.065f)

            val motionX = -(sin(Math.toRadians(moveYaw.toDouble())).toFloat()) * (1.005f.let { this.speed /= it; this.speed })
            val motionZ = cos(Math.toRadians(moveYaw.toDouble())).toFloat() * speed

            this.prevPos = this.posVec
            val scaleBox = 0.1f
            val delente = if (mc.theWorld.getCollisionBoxes(
                    AxisAlignedBB(
                        posVec.xCoord - (scaleBox / 2.0f),
                        posVec.yCoord,
                        posVec.zCoord - (scaleBox / 2.0f),
                        posVec.xCoord + (scaleBox / 2.0f),
                        posVec.yCoord + scaleBox,
                        posVec.zCoord + (scaleBox / 2.0f)
                    )
                ).isNotEmpty()
            ) 0.3f else 1.0f

            this.posVec = posVec.addVector(
                (motionX / delente).toDouble(),
                ((1.02f.let { this.yMotion /= it; this.yMotion }) / delente).toDouble(),
                (motionZ / delente).toDouble()
            )

            if (this.timePC >= 1.0f) {
                this.setAlphaPCTo(0.0f)
                if (this.getAlphaPC() < 0.003921569f) {
                    this.setToRemove()
                }
            }

            trailParts.add(TrailPart(this, 400))
            if (trailParts.isNotEmpty()) {
                trailParts.removeIf { it.toRemove() }
            }
            for (i in 0..1) {
                sparkParts.add(SparkPart(this, 300))
            }
            sparkParts.forEach { it.motionSparkProcess() }
            if (sparkParts.isNotEmpty()) {
                sparkParts.removeIf { it.toRemove() }
            }
        }

        fun setToRemove() {
            toRemove = true
        }

        private fun calculateMsChangeSideRate(): Int {
            return getRandom(300.5, 900.5).toInt()
        }
    }

    private class SparkPart(part: FirePart, var maxTime: Int) {
        var posX: Double = part.posVec.xCoord
        var posY: Double = part.posVec.yCoord
        var posZ: Double = part.posVec.zCoord
        var prevPosX: Double = posX
        var prevPosY: Double = posY
        var prevPosZ: Double = posZ
        var speed: Double = Math.random() / 30.0
        var radianYaw: Double = Math.random() * 360.0
        var radianPitch: Double = -90.0 + Math.random() * 180.0
        var startTime: Long = System.currentTimeMillis()

        fun timePC(): Double {
            return MathHelper.clamp_float(
                (System.currentTimeMillis() - startTime).toFloat() / maxTime,
                0.0f,
                1.0f
            ).toDouble()
        }

        fun toRemove(): Boolean {
            return timePC() == 1.0
        }

        fun motionSparkProcess() {
            val radYaw = Math.toRadians(radianYaw)
            prevPosX = posX
            prevPosY = posY
            prevPosZ = posZ
            posX += sin(radYaw) * speed
            posY += cos(Math.toRadians(radianPitch - 90.0)) * speed
            posZ += cos(radYaw) * speed
        }

        fun getRenderPosX(partialTicks: Float): Double {
            return prevPosX + (posX - prevPosX) * partialTicks
        }

        fun getRenderPosY(partialTicks: Float): Double {
            return prevPosY + (posY - prevPosY) * partialTicks
        }

        fun getRenderPosZ(partialTicks: Float): Double {
            return prevPosZ + (posZ - prevPosZ) * partialTicks
        }
    }

    private class TrailPart(part: FirePart, var maxTime: Int) {
        var x: Double = part.posVec.xCoord
        var y: Double = part.posVec.yCoord
        var z: Double = part.posVec.zCoord
        var startTime: Long = System.currentTimeMillis()

        val timePC: Float
            get() = MathHelper.clamp_float(
                (System.currentTimeMillis() - startTime).toFloat() / maxTime,
                0.0f,
                1.0f
            )

        fun toRemove(): Boolean {
            return timePC == 1.0f
        }
    }
}