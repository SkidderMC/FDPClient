/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.DOUBLE_PI
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorPrimary
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorSecondary
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.start
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.animations.AnimationUtil.easeInOutQuadX
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.render.ColorUtils.glFloatColor
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import kotlin.math.*

/**
 * Entity-specific visual rendering utilities
 *
 * @author Zywl
 */
object RenderEntity : MinecraftInstance {

    /**
     * Draws a crystal effect around the specified entity.
     *
     * @param entity The entity to render the effect on
     * @param color The color of the crystal effect
     * @param e The render event containing partial ticks
     */
    @JvmStatic
    fun drawCrystal(entity: EntityLivingBase, color: Int, e: Render3DEvent) {
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * e.partialTicks - mc.renderManager.renderPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * e.partialTicks - mc.renderManager.renderPosY
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * e.partialTicks - mc.renderManager.renderPosZ
        val radius = 0.15f
        val side = 4

        glPushMatrix()
        glTranslated(x, y + 2, z)
        glRotatef(-entity.width, 0.0f, 1.0f, 0.0f)

        RenderUtils.glColor(color)
        RenderUtils.enableSmoothLine(1.5f)

        val c = Cylinder()
        glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
        c.drawStyle = 100012
        RenderUtils.glColor(if ((entity.hurtTime <= 0)) Color(80, 255, 80, 200) else Color(255, 0, 0, 200))
        c.draw(0.0f, radius, 0.3f, side, 1)
        c.drawStyle = 100012

        glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0.0f, 0.3f, side, 1)

        glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
        c.drawStyle = 100011

        glTranslated(0.0, 0.0, -0.3)
        RenderUtils.glColor(color)
        c.draw(0.0f, radius, 0.3f, side, 1)
        c.drawStyle = 100011

        glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0.0f, 0.3f, side, 1)

        RenderUtils.disableSmoothLine()
        glPopMatrix()
    }

    /**
     * Draws a visual effect around the specified entity in 3D space.
     *
     * @param entity The entity to render the effect on
     * @param event The render event containing the partial tick time
     * @param dual Whether to draw dual spiraling effects
     */
    @JvmStatic
    fun drawZavz(entity: EntityLivingBase, event: Render3DEvent, dual: Boolean) {
        val speed = 0.1f

        val ticks = event.partialTicks
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)

        RenderUtils.startSmooth()

        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glLineWidth(2.0f)
        glBegin(GL_LINE_STRIP)

        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * ticks - mc.renderManager.renderPosX
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * ticks - mc.renderManager.renderPosZ
        var y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * ticks - mc.renderManager.renderPosY

        val radius = 0.65
        val precision = 360

        var startPos = start % 360
        start += speed

        for (i in 0..precision) {
            val posX = x + radius * cos(startPos + i * DOUBLE_PI / (precision / 2.0))
            val posZ = z + radius * sin(startPos + i * DOUBLE_PI / (precision / 2.0))

            val t = abs(System.currentTimeMillis() / 10.0) / 100.0 + y
            val grad = ColorUtils
                .getGradientOffset(colorPrimary, colorSecondary, t)
                .withAlpha(255)

            RenderUtils.glColor(grad.red, grad.green, grad.blue, grad.alpha)

            glVertex3d(posX, y, posZ)
            y += entity.height / precision
            RenderUtils.glColor(0, 0, 0, 0)
        }

        glEnd()
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)

        RenderUtils.endSmooth()

        glEnable(GL_TEXTURE_2D)
        glPopMatrix()

        if (dual) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)

            RenderUtils.startSmooth()

            glDisable(GL_DEPTH_TEST)
            glDepthMask(false)
            glLineWidth(2.0f)
            glBegin(GL_LINE_STRIP)

            startPos = start % 360
            start += speed

            y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * ticks - mc.renderManager.renderPosY + entity.height

            for (i in 0..precision) {
                val posX = x + radius * cos(-(startPos + i * DOUBLE_PI / (precision / 2.0)))
                val posZ = z + radius * sin(-(startPos + i * DOUBLE_PI / (precision / 2.0)))

                val t2 = abs(System.currentTimeMillis() / 10.0) / 100.0 + y
                val grad2 = ColorUtils
                    .getGradientOffset(colorPrimary, colorSecondary, t2)
                    .withAlpha(255)

                RenderUtils.glColor(grad2.red, grad2.green, grad2.blue, grad2.alpha)

                glVertex3d(posX, y, posZ)
                y -= entity.height / precision
                RenderUtils.glColor(0, 0, 0, 0)
            }

            glEnd()
            glDepthMask(true)
            glEnable(GL_DEPTH_TEST)

            RenderUtils.endSmooth()

            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    /**
     * Draws a jello-like effect around the given entity.
     *
     * @param entity The entity to draw the jello effect around
     */
    @JvmStatic
    fun drawJello(entity: EntityLivingBase) {
        val drawTime = (System.currentTimeMillis() % 2000).toInt()
        val drawMode = drawTime > 1000
        var drawPercent = drawTime / 1000.0

        drawPercent = if (drawMode) drawPercent - 1 else 1 - drawPercent
        drawPercent = easeInOutQuadX(drawPercent)

        val bb = entity.entityBoundingBox
        val radius = bb.maxX - bb.minX
        val height = bb.maxY - bb.minY
        val posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks
        var posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks
        posY += if (drawMode) -0.5 else 0.5
        val posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks

        val points = mutableListOf<Vec3>()
        for (i in 0..360 step 7) {
            points.add(Vec3(
                posX - sin(i * Math.PI / 180F) * radius,
                posY + height * drawPercent,
                posZ + cos(i * Math.PI / 180F) * radius
            ))
        }
        points.add(points[0])

        mc.entityRenderer.disableLightmap()
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        glBegin(GL_LINE_STRIP)

        val baseMove = if (drawPercent > 0.5) 1 - drawPercent else drawPercent
        val min = (height / 60) * 20 * (1 - baseMove) * (if (drawMode) -1 else 1)

        for (i in 0..20) {
            var moveFace = (height / 60F) * i * baseMove
            if (drawMode) moveFace = -moveFace

            val firstPoint = points[0]
            glVertex3d(
                firstPoint.xCoord - mc.renderManager.viewerPosX,
                firstPoint.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                firstPoint.zCoord - mc.renderManager.viewerPosZ
            )

            glColor4f(1F, 1F, 1F, 0.7F * (i / 20F))
            for (vec3 in points) {
                glVertex3d(
                    vec3.xCoord - mc.renderManager.viewerPosX,
                    vec3.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                    vec3.zCoord - mc.renderManager.viewerPosZ
                )
            }
            glColor4f(0F, 0F, 0F, 0F)
        }

        glEnd()
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Draws an FDP-style ring effect around the entity.
     *
     * @param entity The entity to render the effect on
     * @param event The render event containing partial ticks
     */
    @JvmStatic
    fun drawFDP(entity: EntityLivingBase, event: Render3DEvent) {
        val themeTextColor = getColor(1).rgb

        val drawTime = (System.currentTimeMillis() % 1500).toInt()
        val drawMode = drawTime > 750
        var drawPercent = drawTime / 750.0

        if (!drawMode) {
            drawPercent = 1 - drawPercent
        } else {
            drawPercent -= 1
        }
        drawPercent = easeInOutQuadX(drawPercent)

        mc.entityRenderer.disableLightmap()
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)

        val bb = entity.entityBoundingBox
        val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * 0.5f
        val height = bb.maxY - bb.minY
        val x =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
        val y =
            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
        val z =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
        mc.entityRenderer.disableLightmap()
        glLineWidth((radius * 8f).toFloat())
        glBegin(GL_LINE_STRIP)
        for (i in 0..360 step 10) {
            RenderUtils.glColor(themeTextColor)
            glVertex3d(x - sin(i * Math.PI / 180F) * radius, y, z + cos(i * Math.PI / 180F) * radius)
        }
        glEnd()

        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Draws a Lies-style shaded ring effect around the entity.
     *
     * @param entity The entity to render the effect on
     * @param event The render event containing partial ticks
     */
    @JvmStatic
    fun drawLies(entity: EntityLivingBase, event: Render3DEvent) {
        val themeTextColor = getColor(1)

        val everyTime = 3000
        val drawTime = (System.currentTimeMillis() % everyTime).toInt()
        val drawMode = drawTime > (everyTime / 2)
        var drawPercent = drawTime / (everyTime / 2.0)

        if (!drawMode) {
            drawPercent = 1 - drawPercent
        } else {
            drawPercent -= 1
        }
        drawPercent = easeInOutQuadX(drawPercent)

        mc.entityRenderer.disableLightmap()
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)
        glShadeModel(GL_SMOOTH)
        mc.entityRenderer.disableLightmap()

        val bb = entity.entityBoundingBox
        val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * 0.5f
        val height = bb.maxY - bb.minY
        val x =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
        val y =
            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
        val z =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
        val eased = (height / 3) * (if (drawPercent > 0.5) {
            1 - drawPercent
        } else {
            drawPercent
        }) * (if (drawMode) {
            -1
        } else {
            1
        })

        for (i in 5..360 step 5) {
            val x1 = x - sin(i * Math.PI / 180F) * radius
            val z1 = z + cos(i * Math.PI / 180F) * radius
            val x2 = x - sin((i - 5) * Math.PI / 180F) * radius
            val z2 = z + cos((i - 5) * Math.PI / 180F) * radius
            glBegin(GL_QUADS)
            glFloatColor(themeTextColor, 0f)
            glVertex3d(x1, y + eased, z1)
            glVertex3d(x2, y + eased, z2)
            glFloatColor(themeTextColor, 150f)
            glVertex3d(x2, y, z2)
            glVertex3d(x1, y, z1)
            glEnd()
        }

        glEnable(GL_CULL_FACE)
        glShadeModel(GL_FLAT)
        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }
}
