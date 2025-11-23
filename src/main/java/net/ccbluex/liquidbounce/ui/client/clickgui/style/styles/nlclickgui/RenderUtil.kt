package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.tessellate.Tessellation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.tessellate.Tessellation.Companion.createExpanding
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.awt.Color
import java.util.function.Consumer
import kotlin.math.*

object RenderUtil {
    val tessellator: Tessellation
    var mc: Minecraft = Minecraft.getMinecraft()
    private val csBuffer: MutableList<Int?>
    private val ENABLE_CLIENT_STATE: Consumer<Int?>
    private val DISABLE_CLIENT_STATE: Consumer<Int?>
    var deltaTime: Int = 0

    internal var zLevel: Float = 0f

    var delta: Float = 0f

    fun isHovered(x: Float, y: Float, x2: Float, y2: Float, mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2
    }


    fun startRender() {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glDisable(GL11.GL_CULL_FACE)
    }


    fun stopRender() {
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        color(Color.white)
    }

    fun color(color: Color) {
        GL11.glColor4d(
            color.getRed() / 255.0,
            color.getGreen() / 255.0,
            color.getBlue() / 255.0,
            color.getAlpha() / 255.0
        )
    }

    private fun drawCircle(xPos: Double, yPos: Double, radius: Double) {
        val theta = (2 * Math.PI / 360.0)
        val tangetial_factor = tan(theta)
        val radial_factor = MathHelper.cos(theta.toFloat()).toDouble()
        var x = radius
        var y = 0.0
        for (i in 0..359) {
            GL11.glVertex2d(x + xPos, y + yPos)

            val tx = -y
            val ty = x

            x += tx * tangetial_factor
            y += ty * tangetial_factor

            x *= radial_factor
            y *= radial_factor
        }
    }

    fun drawCircle(xPos: Double, yPos: Double, radius: Double, color: Color) {
        startRender()
        color(color)
        GL11.glBegin(GL11.GL_POLYGON)
        run {
            RenderUtil.drawCircle(xPos, yPos, radius)
        }
        GL11.glEnd()

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(2f)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        run {
            RenderUtil.drawCircle(xPos, yPos, radius)
        }
        GL11.glEnd()
        stopRender()
    }

    fun drawFastRoundedRect(x0: Float, y0: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        val f2 = (color shr 24 and 0xFF) / 255.0f
        val f3 = (color shr 16 and 0xFF) / 255.0f
        val f4 = (color shr 8 and 0xFF) / 255.0f
        val f5 = (color and 0xFF) / 255.0f
        GL11.glDisable(2884)
        GL11.glDisable(3553)
        GL11.glEnable(3042)

        GL11.glBlendFunc(770, 771)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        GL11.glColor4f(f3, f4, f5, f2)
        GL11.glBegin(5)
        GL11.glVertex2f(x0 + radius, y0)
        GL11.glVertex2f(x0 + radius, y1)
        GL11.glVertex2f(x1 - radius, y0)
        GL11.glVertex2f(x1 - radius, y1)
        GL11.glEnd()
        GL11.glBegin(5)
        GL11.glVertex2f(x0, y0 + radius)
        GL11.glVertex2f(x0 + radius, y0 + radius)
        GL11.glVertex2f(x0, y1 - radius)
        GL11.glVertex2f(x0 + radius, y1 - radius)
        GL11.glEnd()
        GL11.glBegin(5)
        GL11.glVertex2f(x1, y0 + radius)
        GL11.glVertex2f(x1 - radius, y0 + radius)
        GL11.glVertex2f(x1, y1 - radius)
        GL11.glVertex2f(x1 - radius, y1 - radius)
        GL11.glEnd()
        GL11.glBegin(6)
        var f6 = x1 - radius
        var f7 = y0 + radius
        GL11.glVertex2f(f6, f7)
        var j: Int
        j = 0
        while (j <= 18) {
            val f8 = j * 5.0f
            GL11.glVertex2f(
                f6 + radius * MathHelper.cos(Math.toRadians(f8.toDouble()).toFloat()), f7 - radius * MathHelper.sin(
                    Math.toRadians(f8.toDouble()).toFloat()
                )
            )
            ++j
        }
        GL11.glEnd()
        GL11.glBegin(6)
        f6 = x0 + radius
        f7 = y0 + radius
        GL11.glVertex2f(f6, f7)
        j = 0
        while (j <= 18) {
            val f9 = j * 5.0f
            GL11.glVertex2f(
                f6 - radius * MathHelper.cos(Math.toRadians(f9.toDouble()).toFloat()),
                f7 - radius * MathHelper.sin(Math.toRadians(f9.toDouble()).toFloat())
            )
            ++j
        }
        GL11.glEnd()
        GL11.glBegin(6)
        f6 = x0 + radius
        f7 = y1 - radius
        GL11.glVertex2f(f6, f7)
        j = 0
        while (j <= 18) {
            val f10 = j * 5.0f
            GL11.glVertex2f(
                f6 - radius * MathHelper.cos(Math.toRadians(f10.toDouble()).toFloat()), f7 + radius * MathHelper.sin(
                    Math.toRadians(f10.toDouble()).toFloat()
                )
            )
            ++j
        }
        GL11.glEnd()
        GL11.glBegin(6)
        f6 = x1 - radius
        f7 = y1 - radius
        GL11.glVertex2f(f6, f7)
        j = 0
        while (j <= 18) {
            val f11 = j * 5.0f
            GL11.glVertex2f(
                f6 + radius * MathHelper.cos(Math.toRadians(f11.toDouble()).toFloat()), f7 + radius * MathHelper.sin(
                    Math.toRadians(f11.toDouble()).toFloat()
                )
            )
            ++j
        }
        GL11.glEnd()
        GL11.glEnable(3553)
        GL11.glEnable(2884)
        GL11.glDisable(3042)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun startGlScissor(x: Int, y: Int, width: Int, height: Int) {
        val scaleFactor = ScaledResolution(mc).getScaleFactor()
        GL11.glPushMatrix()
        GL11.glEnable(3089)
        GL11.glScissor(
            x * scaleFactor,
            mc.displayHeight - (y + height) * scaleFactor,
            width * scaleFactor,
            (height + 14) * scaleFactor
        )
    }

    fun stopGlScissor() {
        GL11.glDisable(3089)
        GL11.glPopMatrix()
    }

    fun convertRGB(rgb: Int): FloatArray {
        val a = (rgb shr 24 and 0xFF) / 255.0f
        val r = (rgb shr 16 and 0xFF) / 255.0f
        val g = (rgb shr 8 and 0xFF) / 255.0f
        val b = (rgb and 0xFF) / 255.0f
        return floatArrayOf(r, g, b, a)
    }

    fun toColorRGB(rgb: Int, alpha: Float): Color {
        val rgba = convertRGB(rgb)
        return Color(rgba[0], rgba[1], rgba[2], alpha / 255f)
    }

    fun color(color: Color, alpha: Float) {
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha / 255f)
    }

    fun project2D(x: Double, y: Double, z: Double): DoubleArray? {
        val objectPosition = BufferUtils.createFloatBuffer(3)
        val modelView = BufferUtils.createFloatBuffer(16)
        val projection = BufferUtils.createFloatBuffer(16)
        val viewport = BufferUtils.createIntBuffer(16)

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView)
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection)
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport)
        val sc = ScaledResolution(mc)
        if (GLU.gluProject(
                x.toFloat(),
                y.toFloat(),
                z.toFloat(),
                modelView,
                projection,
                viewport,
                objectPosition
            )
        ) return doubleArrayOf(
            (objectPosition.get(0) / sc.getScaleFactor()).toDouble(),
            (objectPosition.get(1) / sc.getScaleFactor()).toDouble(),
            objectPosition.get(2).toDouble()
        )
        return null
    }

    fun getAnimationStateSmooth(target: Double, current: Double, speed: Double): Double {
        var current = current
        var speed = speed
        val larger = target > current
        if (speed < 0.0) {
            speed = 0.0
        } else if (speed > 1.0) {
            speed = 1.0
        }

        if (target == current) {
            return target
        } else {
            val dif = max(target, current) - min(target, current)
            var factor = dif * speed
            if (factor < 0.1) {
                factor = 0.1
            }

            if (larger) {
                if (current + factor > target) {
                    current = target
                } else {
                    current += factor
                }
            } else if (current - factor < target) {
                current = target
            } else {
                current -= factor
            }

            return current
        }
    }

    fun doGlScissor(x: Int, y: Int, width: Int, height: Int) {
        val mc = Minecraft.getMinecraft()
        var scaleFactor = 1
        var k = mc.gameSettings.guiScale
        if (k == 0) {
            k = 1000
        }

        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor
        }

        GL11.glScissor(
            x * scaleFactor,
            mc.displayHeight - (y + height) * scaleFactor,
            width * scaleFactor,
            height * scaleFactor
        )
    }

    fun getAnimationState(animation: Float, finalState: Float, speed: Float): Float {
        var animation = animation
        val add = delta * speed
        if (animation < finalState) {
            if (animation + add < finalState) {
                animation += add
            } else {
                animation = finalState
            }
        } else if (animation - add > finalState) {
            animation -= add
        } else {
            animation = finalState
        }

        return animation
    }


    fun enableGL2D() {
        GL11.glDisable(2929)
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glDepthMask(true)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glHint(3155, 4354)
    }

    fun disableGL2D() {
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glEnable(2929)
        GL11.glDisable(2848)
        GL11.glHint(3154, 4352)
        GL11.glHint(3155, 4352)
    }

    fun hasDepthAttachment(framebuffer: Framebuffer?): Boolean {
        framebuffer ?: return false

        return framebuffer.useDepth || framebuffer.depthBuffer > -1
    }

    private fun draw(
        renderer: WorldRenderer,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        red: Int,
        green: Int,
        blue: Int,
        alpha: Int
    ) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        renderer.pos(x.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + width).toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        Tessellator.getInstance().draw()
    }

    fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        val hadDepthAttachment = hasDepthAttachment(framebuffer)
        val needsRebuild = framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth ||
                framebuffer.framebufferHeight != mc.displayHeight || hadDepthAttachment

        if (needsRebuild) {
            framebuffer?.deleteFramebuffer()

            // Depth buffers are not required for the GUI passes and enabling them leads to dark
            // artifacts around the window while it is dragged. Keep the framebuffer colour-only
            // to avoid the unintended shadow. Record the rebuild for the debug overlay so issues
            // are easier to diagnose on-device.
            return Framebuffer(mc.displayWidth, mc.displayHeight, false)
        }
        return framebuffer
    }

    fun pre3D() {
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDepthMask(false)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
    }

    fun post3D() {
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 255).toFloat() / 255.0f
        val f1 = (col1 shr 16 and 255).toFloat() / 255.0f
        val f2 = (col1 shr 8 and 255).toFloat() / 255.0f
        val f3 = (col1 and 255).toFloat() / 255.0f
        val f4 = (col2 shr 24 and 255).toFloat() / 255.0f
        val f5 = (col2 shr 16 and 255).toFloat() / 255.0f
        val f6 = (col2 shr 8 and 255).toFloat() / 255.0f
        val f7 = (col2 and 255).toFloat() / 255.0f
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        GL11.glPushMatrix()
        GL11.glBegin(7)
        GL11.glColor4f(f1, f2, f3, f)
        GL11.glVertex2d(left, bottom)
        GL11.glVertex2d(right, bottom)
        GL11.glColor4f(f5, f6, f7, f4)
        GL11.glVertex2d(right, top)
        GL11.glVertex2d(left, top)
        GL11.glEnd()
        GL11.glPopMatrix()
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        Gui.drawRect(0, 0, 0, 0, 0)
    }

    fun drawScaledCustomSizeModalRect(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) {
        drawBoundTexture(x, y, u, v, uWidth.toFloat(), vHeight.toFloat(), width, height, tileWidth, tileHeight)
    }

    fun drawTracers(e: Entity?, color: Int, lw: Float) {
        if (e == null) {
            return
        }
        val x =
            e.lastTickPosX + (e.posX - e.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX
        val y =
            e.getEyeHeight() + e.lastTickPosY + (e.posY - e.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY
        val z =
            e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ
        val a = (color shr 24 and 0xFF) / 255.0f
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        GL11.glPushMatrix()
        GL11.glEnable(3042)
        GL11.glEnable(2848)
        GL11.glDisable(2929)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(3042)
        GL11.glLineWidth(lw)
        GL11.glColor4f(r, g, b, a)
        GL11.glBegin(2)
        GL11.glVertex3d(0.0, 0.0 + mc.thePlayer.getEyeHeight(), 0.0)
        GL11.glVertex3d(x, y, z)
        GL11.glEnd()
        GL11.glDisable(3042)
        GL11.glEnable(3553)
        GL11.glEnable(2929)
        GL11.glDisable(2848)
        GL11.glDisable(3042)
        GL11.glPopMatrix()
    }

    fun drawESP(e: Entity?, color: Int, damage: Boolean, type: Int) {
        var color = color
        if (e == null) {
            return
        }
        val x =
            e.lastTickPosX + (e.posX - e.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX
        val y =
            e.lastTickPosY + (e.posY - e.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY
        val z =
            e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ
        if (e is EntityPlayer && damage && e.hurtTime != 0) {
            color = Color.RED.getRGB()
        }
        val a = (color shr 24 and 0xFF) / 255.0f
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        if (type == 1) {
            GlStateManager.pushMatrix()
            GL11.glBlendFunc(770, 771)
            GL11.glEnable(3042)
            GL11.glDisable(3553)
            GL11.glDisable(2929)
            GL11.glDepthMask(false)
            GL11.glLineWidth(3.0f)
            GL11.glColor4f(r, g, b, a)
            RenderGlobal.drawSelectionBoundingBox(
                AxisAlignedBB(
                    e.getEntityBoundingBox().minX - 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX),
                    e.getEntityBoundingBox().minY - e.posY + (e.posY - mc.getRenderManager().viewerPosY),
                    e.getEntityBoundingBox().minZ - 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ),
                    e.getEntityBoundingBox().maxX + 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX),
                    e.getEntityBoundingBox().maxY + 0.1 - e.posY + (e.posY - mc.getRenderManager().viewerPosY),
                    e.getEntityBoundingBox().maxZ + 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ)
                )
            )
            drawAABB(
                AxisAlignedBB(
                    e.getEntityBoundingBox().minX - 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX),
                    e.getEntityBoundingBox().minY - e.posY + (e.posY - mc.getRenderManager().viewerPosY),
                    e.getEntityBoundingBox().minZ - 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ),
                    e.getEntityBoundingBox().maxX + 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX),
                    e.getEntityBoundingBox().maxY + 0.1 - e.posY + (e.posY - mc.getRenderManager().viewerPosY),
                    e.getEntityBoundingBox().maxZ + 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ)
                ), r, g, b
            )
            GL11.glEnable(3553)
            GL11.glEnable(2929)
            GL11.glDepthMask(true)
            GL11.glDisable(3042)
            GlStateManager.popMatrix()
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        } else if (type == 2 || type == 3) {
            val mode = type == 2
            GL11.glBlendFunc(770, 771)
            GL11.glEnable(3042)
            GL11.glLineWidth(3.0f)
            GL11.glDisable(3553)
            GL11.glDisable(2929)
            GL11.glDepthMask(false)
            GL11.glColor4d(r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble())
            if (mode) {
                RenderGlobal.drawSelectionBoundingBox(
                    AxisAlignedBB(
                        e.getEntityBoundingBox().minX - 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX),
                        e.getEntityBoundingBox().minY - e.posY + (e.posY - mc.getRenderManager().viewerPosY),
                        e.getEntityBoundingBox().minZ - 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ),
                        e.getEntityBoundingBox().maxX + 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX),
                        e.getEntityBoundingBox().maxY + 0.1 - e.posY + (e.posY - mc.getRenderManager().viewerPosY),
                        e.getEntityBoundingBox().maxZ + 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ)
                    )
                )
            } else {
                drawAABB(
                    AxisAlignedBB(
                        e.getEntityBoundingBox().minX - 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX),
                        e.getEntityBoundingBox().minY - e.posY + (e.posY - mc.getRenderManager().viewerPosY),
                        e.getEntityBoundingBox().minZ - 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ),
                        e.getEntityBoundingBox().maxX + 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX),
                        e.getEntityBoundingBox().maxY + 0.1 - e.posY + (e.posY - mc.getRenderManager().viewerPosY),
                        e.getEntityBoundingBox().maxZ + 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ)
                    ), r, g, b
                )
            }
            GL11.glEnable(3553)
            GL11.glEnable(2929)
            GL11.glDepthMask(true)
            GL11.glDisable(3042)
        } else if (type == 4) {
            GL11.glPushMatrix()
            GL11.glTranslated(x, y - 0.2, z)
            GL11.glScalef(0.03f, 0.03f, 0.03f)
            GL11.glRotated(-mc.getRenderManager().playerViewY.toDouble(), 0.0, 1.0, 0.0)
            GlStateManager.disableDepth()
            Gui.drawRect(-20, -1, -26, 75, Color.black.getRGB())
            Gui.drawRect(-21, 0, -25, 74, color)
            Gui.drawRect(20, -1, 26, 75, Color.black.getRGB())
            Gui.drawRect(21, 0, 25, 74, color)
            Gui.drawRect(-20, -1, 21, 5, Color.black.getRGB())
            Gui.drawRect(-21, 0, 24, 4, color)
            Gui.drawRect(-20, 70, 21, 75, Color.black.getRGB())
            Gui.drawRect(-21, 71, 25, 74, color)
            GlStateManager.enableDepth()
            GL11.glPopMatrix()
        }
    }

    fun drawAABB(aabb: AxisAlignedBB, r: Float, g: Float, b: Float) {
        val a = 0.25f
        val ts = Tessellator.getInstance()
        val vb = ts.getWorldRenderer()
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR)
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR)
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR)
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR)
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR)
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR)
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        ts.draw()
    }

    fun drawCornerBox(x: Double, y: Double, x2: Double, y2: Double, lw: Double, color: Color) {
        val width = abs(x2 - x)
        val height = abs(y2 - y)
        val halfWidth = width / 4
        val halfHeight = height / 4
        start2D()
        GL11.glPushMatrix()
        GL11.glLineWidth(lw.toFloat())
        setColor(color)

        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glVertex2d(x + halfWidth, y)
        GL11.glVertex2d(x, y)
        GL11.glVertex2d(x, y + halfHeight)
        GL11.glEnd()


        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glVertex2d(x, y + height - halfHeight)
        GL11.glVertex2d(x, y + height)
        GL11.glVertex2d(x + halfWidth, y + height)
        GL11.glEnd()

        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glVertex2d(x + width - halfWidth, y + height)
        GL11.glVertex2d(x + width, y + height)
        GL11.glVertex2d(x + width, y + height - halfHeight)
        GL11.glEnd()

        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glVertex2d(x + width, y + halfHeight)
        GL11.glVertex2d(x + width, y)
        GL11.glVertex2d(x + width - halfWidth, y)
        GL11.glEnd()

        GL11.glPopMatrix()
        stop2D()
    }

    fun drawSolidBlockESP(pos: BlockPos, color: Int) {
        val xPos = pos.getX() - mc.getRenderManager().renderPosX
        val yPos = pos.getY() - mc.getRenderManager().renderPosY
        val zPos = pos.getZ() - mc.getRenderManager().renderPosZ
        val height =
            mc.theWorld.getBlockState(pos).getBlock().getBlockBoundsMaxY() - mc.theWorld.getBlockState(pos).getBlock()
                .getBlockBoundsMinY()
        val f = (color shr 16 and 0xFF).toFloat() / 255.0f
        val f2 = (color shr 8 and 0xFF).toFloat() / 255.0f
        val f3 = (color and 0xFF).toFloat() / 255.0f
        val f4 = (color shr 24 and 0xFF).toFloat() / 255.0f
        GL11.glPushMatrix()
        GL11.glEnable(3042)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(2929)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glDisable(3553)
        GL11.glDisable(2929)
        GL11.glDepthMask(false)
        GL11.glLineWidth(1.0f)
        GL11.glColor4f(f, f2, f3, f4)
        drawOutlinedBoundingBox(AxisAlignedBB(xPos, yPos, zPos, xPos + 1.0, yPos + height, zPos + 1.0))
        GL11.glColor3f(1.0f, 1.0f, 1.0f)
        GL11.glEnable(3553)
        GL11.glEnable(2929)
        GL11.glDepthMask(true)
        GL11.glDisable(3042)
        GL11.glDisable(3042)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(2929)
        GlStateManager.disableBlend()
        GL11.glPopMatrix()
    }

    fun drawLine(blockPos: BlockPos, color: Int) {
        val mc = Minecraft.getMinecraft()
        val renderPosXDelta = blockPos.getX() - mc.getRenderManager().renderPosX + 0.5
        val renderPosYDelta = blockPos.getY() - mc.getRenderManager().renderPosY + 0.5
        val renderPosZDelta = blockPos.getZ() - mc.getRenderManager().renderPosZ + 0.5
        GL11.glPushMatrix()
        GL11.glEnable(3042)
        GL11.glEnable(2848)
        GL11.glDisable(2929)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glLineWidth(1.0f)
        val f = (color shr 16 and 0xFF).toFloat() / 255.0f
        val f2 = (color shr 8 and 0xFF).toFloat() / 255.0f
        val f3 = (color and 0xFF).toFloat() / 255.0f
        val f4 = (color shr 24 and 0xFF).toFloat() / 255.0f
        GL11.glColor4f(f, f2, f3, f4)
        GL11.glLoadIdentity()
        val previousState = mc.gameSettings.viewBobbing
        mc.gameSettings.viewBobbing = false
        (mc.entityRenderer).orientCamera(mc.timer.renderPartialTicks)
        GL11.glBegin(3)
        GL11.glVertex3d(0.0, mc.thePlayer.getEyeHeight().toDouble(), 0.0)
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta)
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta)
        GL11.glEnd()
        mc.gameSettings.viewBobbing = previousState
        GL11.glEnable(3553)
        GL11.glEnable(2929)
        GL11.glDisable(2848)
        GL11.glDisable(3042)
        GL11.glPopMatrix()
    }

    fun drawOutlinedBoundingBox(axisAlignedBB: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.getWorldRenderer()
        worldRenderer.begin(3, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(3, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(1, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }

    fun drawTexturedModalRect(
        x: Double, y: Double, textureX: Double, textureY: Double, width: Double,
        height: Double
    ) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x + 0, y + height, zLevel.toDouble())
            .tex(((textureX + 0).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble()).endVertex()
        worldrenderer.pos(x + width, y + height, zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble())
            .endVertex()
        worldrenderer.pos(x + width, y + 0, zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + 0).toFloat() * f1).toDouble()).endVertex()
        worldrenderer.pos(x + 0, y + 0, zLevel.toDouble())
            .tex(((textureX + 0).toFloat() * f).toDouble(), ((textureY + 0).toFloat() * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    private fun drawBoundTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        uWidth: Float,
        vHeight: Float,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) {
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + vHeight) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + uWidth) * f).toDouble(), ((v + vHeight) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + uWidth) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    fun reAlpha(color: Int, alpha: Float): Int {
        try {
            val c = Color(color)
            val r = (1f / 255) * c.getRed()
            val g = (1f / 255) * c.getGreen()
            val b = (1f / 255) * c.getBlue()
            return Color(r, g, b, alpha).getRGB()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return color
    }

    init {
        tessellator = createExpanding(4, 1.0f, 2.0f)
        csBuffer = ArrayList<Int?>()
        ENABLE_CLIENT_STATE = Consumer { cap: Int? -> GL11.glEnableClientState(cap!!) }
        DISABLE_CLIENT_STATE = Consumer { cap: Int? -> GL11.glEnableClientState(cap!!) }
    }

    fun drawArrow(x: Double, y: Double, lineWidth: Int, color: Int, length: Double) {
        start2D()
        GL11.glPushMatrix()
        GL11.glLineWidth(lineWidth.toFloat())
        setColor(Color(color))
        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glVertex2d(x, y)
        GL11.glVertex2d(x + 3, y + length)
        GL11.glVertex2d(x + 3 * 2, y)
        GL11.glEnd()
        GL11.glPopMatrix()
        stop2D()
    }

    fun setAlphaLimit(limit: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL11.GL_GREATER, (limit * .01).toFloat())
    }

    fun fakeCircleGlow(posX: Float, posY: Float, radius: Float, color: Color, maxAlpha: Float) {
        setAlphaLimit(0f)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        setup2DRendering(Runnable {
            render(GL11.GL_TRIANGLE_FAN, Runnable {
                color(color.getRGB(), maxAlpha)
                GL11.glVertex2d(posX.toDouble(), posY.toDouble())
                color(color.getRGB(), 0f)
                for (i in 0..100) {
                    val angle = (i * .06283) + 3.1415
                    val x2 = sin(angle) * radius
                    val y2 = cos(angle) * radius
                    GL11.glVertex2d(posX + x2, posY + y2)
                }
            })
        })
        GL11.glShadeModel(GL11.GL_FLAT)
        setAlphaLimit(1f)
    }

    fun brighter(color: Color, FACTOR: Float): Color {
        var r = color.getRed()
        var g = color.getGreen()
        var b = color.getBlue()
        val alpha = color.getAlpha()


        val i = (1.0 / (1.0 - FACTOR)).toInt()
        if (r == 0 && g == 0 && b == 0) {
            return Color(i, i, i, alpha)
        }
        if (r > 0 && r < i) r = i
        if (g > 0 && g < i) g = i
        if (b > 0 && b < i) b = i

        return Color(
            min((r / FACTOR).toInt(), 255),
            min((g / FACTOR).toInt(), 255),
            min((b / FACTOR).toInt(), 255),
            alpha
        )
    }

    fun applyOpacity(color: Color, opacity: Float): Color {
        var opacity = opacity
        opacity = min(1f, max(0f, opacity))
        return Color(color.getRed(), color.getGreen(), color.getBlue(), (color.getAlpha() * opacity).toInt())
    }

    fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
        var amount = amount
        amount = min(1f, max(0f, amount))
        return Color(
            DrRenderUtils.interpolateInt(color1.getRed(), color2.getRed(), amount.toDouble()),
            DrRenderUtils.interpolateInt(color1.getGreen(), color2.getGreen(), amount.toDouble()),
            DrRenderUtils.interpolateInt(color1.getBlue(), color2.getBlue(), amount.toDouble()),
            DrRenderUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount.toDouble())
        )
    }

    fun animate(endPoint: Double, current: Double, speed: Double): Double {
        var speed = speed
        val shouldContinueAnimation = endPoint > current
        if (speed < 0.0) {
            speed = 0.0
        } else if (speed > 1.0) {
            speed = 1.0
        }

        val dif = max(endPoint, current) - min(endPoint, current)
        val factor = dif * speed
        return current + (if (shouldContinueAnimation) factor else -factor)
    }

    fun drawRect2(x: Double, y: Double, width: Double, height: Double, color: Int) {
        resetColor()
        setup2DRendering(Runnable {
            render(GL11.GL_QUADS, Runnable {
                color(color)
                GL11.glVertex2d(x, y)
                GL11.glVertex2d(x, y + height)
                GL11.glVertex2d(x + width, y + height)
                GL11.glVertex2d(x + width, y)
            })
        })
    }


    fun drawArc(n: Float, n2: Float, n3: Double, n4: Int, n5: Int, n6: Double, n7: Int) {
        var n = n
        var n2 = n2
        var n3 = n3
        n3 *= 2.0
        n *= 2.0f
        n2 *= 2.0f
        val n8 = (n4 shr 24 and 0xFF) / 255.0f
        val n9 = (n4 shr 16 and 0xFF) / 255.0f
        val n10 = (n4 shr 8 and 0xFF) / 255.0f
        val n11 = (n4 and 0xFF) / 255.0f
        GL11.glDisable(2929)
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glDepthMask(true)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glHint(3155, 4354)
        GL11.glScalef(0.5f, 0.5f, 0.5f)
        GL11.glLineWidth(n7.toFloat())
        GL11.glEnable(2848)
        GL11.glColor4f(n9, n10, n11, n8)
        GL11.glBegin(3)
        var n12 = n5
        while (n12 <= n6) {
            GL11.glVertex2d(
                n + sin(n12 * 3.141592653589793 / 180.0) * n3,
                n2 + cos(n12 * 3.141592653589793 / 180.0) * n3
            )
            ++n12
        }
        GL11.glEnd()
        GL11.glDisable(2848)
        GL11.glScalef(2.0f, 2.0f, 2.0f)
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glEnable(2929)
        GL11.glDisable(2848)
        GL11.glHint(3154, 4352)
        GL11.glHint(3155, 4352)
    }

    fun arc(
        x: Float, y: Float, start: Float, end: Float, radius: Float,
        color: Int
    ) {
        arcEllipse(x, y, start, end, radius, radius, color)
    }

    fun arcEllipse(
        x: Float, y: Float, start: Float, end: Float, w: Float, h: Float,
        color: Int
    ) {
        var start = start
        var end = end
        GlStateManager.color(0.0f, 0.0f, 0.0f)
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f)
        var temp = 0.0f
        if (start > end) {
            temp = end
            end = start
            start = temp
        }
        val var11 = (color shr 24 and 0xFF) / 255.0f
        val var12 = (color shr 16 and 0xFF) / 255.0f
        val var13 = (color shr 8 and 0xFF) / 255.0f
        val var14 = (color and 0xFF) / 255.0f
        val var15 = Tessellator.getInstance()
        val var16 = var15.getWorldRenderer()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(var12, var13, var14, var11)
        if (var11 > 0.5f) {
            GL11.glEnable(2848)
            GL11.glLineWidth(2.0f)
            GL11.glBegin(3)
            var i = end
            while (i >= start) {
                val ldx = cos(i * 3.141592653589793 / 180.0).toFloat() * w * 1.001f
                val ldy = sin(i * 3.141592653589793 / 180.0).toFloat() * h * 1.001f
                GL11.glVertex2f(x + ldx, y + ldy)
                i -= 4.0f
            }
            GL11.glEnd()
            GL11.glDisable(2848)
        }
        GL11.glBegin(6)
        var i = end
        while (i >= start) {
            val ldx = cos(i * 3.141592653589793 / 180.0).toFloat() * w
            val ldy = sin(i * 3.141592653589793 / 180.0).toFloat() * h
            GL11.glVertex2f(x + ldx, y + ldy)
            i -= 4.0f
        }
        GL11.glEnd()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawCircleWithTexture(
        cX: Float,
        cY: Float,
        start: Int,
        end: Int,
        radius: Float,
        res: ResourceLocation?,
        color: Int
    ) {
        var radian: Double
        var x: Double
        var y: Double
        var tx: Double
        var ty: Double
        var xsin: Double
        var ycos: Double
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        mc.getTextureManager().bindTexture(res)
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        color(color)
        GL11.glBegin(GL11.GL_POLYGON)
        for (i in start..<end) {
            radian = i * (Math.PI / 180.0f)
            xsin = sin(radian)
            ycos = cos(radian)

            x = xsin * radius
            y = ycos * radius

            tx = xsin * 0.5 + 0.5
            ty = ycos * 0.5 + 0.5

            GL11.glTexCoord2d(cX + tx, cY + ty)
            GL11.glVertex2d(cX + x, cY + y)
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()
    }

    fun drawClickGuiArrow(x: Float, y: Float, size: Float, animation: Animation, color: Int) {
        GL11.glTranslatef(x, y, 0f)
        setup2DRendering(Runnable {
            render(GL11.GL_TRIANGLE_STRIP, Runnable {
                color(color)
                val interpolation = interpolate(0.0, size / 2.0, animation.getOutput())
                if (animation.getOutput() >= .48) {
                    GL11.glVertex2d((size / 2f).toDouble(), interpolate(size / 2.0, 0.0, animation.getOutput()))
                }
                GL11.glVertex2d(0.0, interpolation)

                if (animation.getOutput() < .48) {
                    GL11.glVertex2d((size / 2f).toDouble(), interpolate(size / 2.0, 0.0, animation.getOutput()))
                }
                GL11.glVertex2d(size.toDouble(), interpolation)
            })
        })
        GL11.glTranslatef(-x, -y, 0f)
    }

    fun render(mode: Int, render: Runnable) {
        GL11.glBegin(mode)
        render.run()
        GL11.glEnd()
    }

    fun setup2DRendering(f: Runnable) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        f.run()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GlStateManager.disableBlend()
    }

    fun interpolate(oldValue: Double, newValue: Double, interpolationValue: Double): Double {
        return (oldValue + (newValue - oldValue) * interpolationValue)
    }

    fun interpolatee(current: Double, old: Double, scale: Double): Double {
        return old + (current - old) * scale
    }

    fun getColor(color: Int): Int {
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color and 0xFF
        val a = 255
        return (r and 0xFF) shl 16 or ((g and 0xFF) shl 8) or (b and 0xFF) or ((a and 0xFF) shl 24)
    }

    fun darker(color: Int, factor: Float): Int {
        val r = ((color shr 16 and 255).toFloat() * factor).toInt()
        val g = ((color shr 8 and 255).toFloat() * factor).toInt()
        val b = ((color and 255).toFloat() * factor).toInt()
        val a = color shr 24 and 255
        return (r and 255) shl 16 or ((g and 255) shl 8) or (b and 255) or ((a and 255) shl 24)
    }

    fun darker(color: Color, FACTOR: Float): Color {
        return Color(
            max((color.getRed() * FACTOR).toInt(), 0),
            max((color.getGreen() * FACTOR).toInt(), 0),
            max((color.getBlue() * FACTOR).toInt(), 0),
            color.getAlpha()
        )
    }

    fun drawGradientRect(left: Float, top: Float, right: Float, bottom: Float, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 255).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 255).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 255).toFloat() / 255.0f
        val f3 = (startColor and 255).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 255).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 255).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 255).toFloat() / 255.0f
        val f7 = (endColor and 255).toFloat() / 255.0f
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).color(f5, f6, f7, f4).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).color(f5, f6, f7, f4).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawGradientRect(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        sideways: Boolean,
        startColor: Int,
        endColor: Int
    ) {
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glBegin(GL11.GL_QUADS)
        color(startColor)
        if (sideways) {
            GL11.glVertex2d(left, top)
            GL11.glVertex2d(left, bottom)
            color(endColor)
            GL11.glVertex2d(right, bottom)
            GL11.glVertex2d(right, top)
        } else {
            GL11.glVertex2d(left, top)
            color(endColor)
            GL11.glVertex2d(left, bottom)
            GL11.glVertex2d(right, bottom)
            color(startColor)
            GL11.glVertex2d(right, top)
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    fun drawCheckeredBackground(x: Float, y: Float, x2: Float, y2: Float) {
        var y = y
        drawRect(x, y, x2, y2, getColor(16777215))
        var offset = false
        while (y < y2) {
            var x1 = x + (if ((!offset).also { offset = it }) 1 else 0).toFloat()
            while (x1 < x2) {
                if (x1 <= x2 - 1.0f) {
                    drawRect(x1, y, x1 + 1.0f, y + 1.0f, getColor(8421504))
                }
                x1 += 2.0f
            }
            ++y
        }
    }

    fun drawStack(font: FontRenderer, renderOverlay: Boolean, stack: ItemStack, x: Float, y: Float) {
        GL11.glPushMatrix()

        val mc = Minecraft.getMinecraft()

        if (mc.theWorld != null) {
            RenderHelper.enableGUIStandardItemLighting()
        }

        GlStateManager.pushMatrix()
        GlStateManager.disableAlpha()
        GlStateManager.clear(256)
        GlStateManager.enableBlend()

        mc.getRenderItem().zLevel = -150.0f
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x.toInt(), y.toInt())

        if (renderOverlay) {
            renderItemOverlayIntoGUI(font, stack, x.toInt(), y.toInt(), stack.stackSize.toString())
        }

        mc.getRenderItem().zLevel = 0.0f

        GlStateManager.enableBlend()
        val z = 0.5f

        GlStateManager.scale(z, z, z)
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.scale(1f, 2.0f, 2.0f)
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()

        GL11.glPopMatrix()
    }

    fun renderItemOverlayIntoGUI(fr: FontRenderer, stack: ItemStack?, xPosition: Int, yPosition: Int, text: String?) {
        if (stack != null) {
            if (stack.stackSize != 1 || text != null) {
                var s = if (text == null) stack.stackSize.toString() else text
                if (text == null && stack.stackSize < 1) {
                    s = EnumChatFormatting.RED.toString() + stack.stackSize.toString()
                }

                GlStateManager.disableLighting()
                GlStateManager.disableDepth()
                GlStateManager.disableBlend()
                fr.drawString(
                    s,
                    (xPosition + 19 - 2 - fr.stringWidth(s)).toFloat(),
                    (yPosition + 6 + 3).toFloat(),
                    16777215
                )
                GlStateManager.enableLighting()
                GlStateManager.enableDepth()
            }

            if (stack.isItemDamaged()) {
                val durability = stack.getItemDamage().toDouble() / stack.getMaxDamage().toDouble()
                val j = Math.round(13.0 - durability * 13.0).toInt()
                val i = Math.round(255.0 - durability * 255.0).toInt()
                GlStateManager.disableLighting()
                GlStateManager.disableDepth()
                GlStateManager.disableTexture2D()
                GlStateManager.disableAlpha()
                GlStateManager.disableBlend()
                val tessellator = Tessellator.getInstance()
                val worldrenderer = tessellator.getWorldRenderer()
                draw(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255)
                draw(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255)
                draw(worldrenderer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255)
                GlStateManager.enableAlpha()
                GlStateManager.enableTexture2D()
                GlStateManager.enableLighting()
                GlStateManager.enableDepth()
            }
        }
    }

    fun drawCheck(x: Double, y: Double, lineWidth: Int, color: Int) {
        start2D()
        GL11.glPushMatrix()
        GL11.glLineWidth(lineWidth.toFloat())
        setColor(Color(color))
        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glVertex2d(x, y)
        GL11.glVertex2d(x + 2, y + 3)
        GL11.glVertex2d(x + 6, y - 2)
        GL11.glEnd()
        GL11.glPopMatrix()
        stop2D()
    }

    fun setGLColor(color: Int) {
        setGLColor(Color(color))
    }

    fun setColor(color: Color) {
        val alpha = (color.getRGB() shr 24 and 0xFF) / 255.0f
        val red = (color.getRGB() shr 16 and 0xFF) / 255.0f
        val green = (color.getRGB() shr 8 and 0xFF) / 255.0f
        val blue = (color.getRGB() and 0xFF) / 255.0f
        GL11.glColor4f(red, green, blue, alpha)
    }

    fun setColor(colorHex: Int) {
        val alpha = (colorHex shr 24 and 255).toFloat() / 255.0f
        val red = (colorHex shr 16 and 255).toFloat() / 255.0f
        val green = (colorHex shr 8 and 255).toFloat() / 255.0f
        val blue = (colorHex and 255).toFloat() / 255.0f
        GL11.glColor4f(red, green, blue, alpha)
    }

    fun setGLColor(color: Color) {
        val r = color.getRed() / 255f
        val g = color.getGreen() / 255f
        val b = color.getBlue() / 255f
        val a = color.getAlpha() / 255f
        GL11.glColor4f(r, g, b, a)
    }

    fun start2D() {
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
    }

    fun stop2D() {
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    fun scissor(x: Double, y: Double, width: Double, height: Double) {
        var scaleFactor: Int
        scaleFactor = ScaledResolution(Minecraft.getMinecraft()).getScaleFactor()
        while (scaleFactor < 2 && Minecraft.getMinecraft().displayWidth / (scaleFactor + 1) >= 320 && Minecraft.getMinecraft().displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor
        }
        GL11.glScissor(
            (x * scaleFactor).toInt(),
            (Minecraft.getMinecraft().displayHeight - (y + height) * scaleFactor).toInt(),
            (width * scaleFactor).toInt(),
            (height * scaleFactor).toInt()
        )
    }

    fun width(): Int {
        return ScaledResolution(Minecraft.getMinecraft()).getScaledWidth()
    }

    fun isHovering(x: Float, y: Float, width: Float, height: Float, mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
    }

    fun height(): Int {
        return ScaledResolution(Minecraft.getMinecraft()).getScaledHeight()
    }

    fun scale(x: Float, y: Float, scale: Float, data: Runnable) {
        GL11.glPushMatrix()
        GL11.glTranslatef(x, y, 0f)
        GL11.glScalef(scale, scale, 1f)
        GL11.glTranslatef(-x, -y, 0f)
        data.run()
        GL11.glPopMatrix()
    }

    fun drawRoundedRect(x: Float, y: Float, x2: Float, y2: Float, round: Float, color: Int) {
        var x = x
        var y = y
        var x2 = x2
        var y2 = y2
        x = (x.toDouble() + (round / 2.0f).toDouble() + 0.5).toFloat()
        y = (y.toDouble() + (round / 2.0f).toDouble() + 0.5).toFloat()
        x2 = (x2.toDouble() - ((round / 2.0f).toDouble() + 0.5)).toFloat()
        y2 = (y2.toDouble() - ((round / 2.0f).toDouble() + 0.5)).toFloat()
        drawRect(x, y, x2, y2, color)
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color)
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color)
        circle(x + round / 2.0f, y + round / 2.0f, round, color)
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color)
        drawRect(x - round / 2.0f - 0.5f, y + round / 2.0f, x2, y2 - round / 2.0f, color)
        drawRect(x, y + round / 2.0f, x2 + round / 2.0f + 0.5f, y2 - round / 2.0f, color)
        drawRect(x + round / 2.0f, y - round / 2.0f - 0.5f, x2 - round / 2.0f, y2 - round / 2.0f, color)
        drawRect(x + round / 2.0f, y, x2 - round / 2.0f, y2 + round / 2.0f + 0.5f, color)
    }

    fun circle(x: Float, y: Float, radius: Float, fill: Int) {
        GL11.glEnable(3042)
        arc(x, y, 0.0f, 360.0f, radius, fill)
        GL11.glDisable(3042)
    }


    fun getHexRGB(hex: Int): Int {
        return -0x1000000 or hex
    }

    fun drawRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        edgeRadius: Float,
        color: Int,
        borderWidth: Float,
        borderColor: Int
    ) {
        var edgeRadius = edgeRadius
        var color = color
        var borderColor = borderColor
        if (color == 16777215) color = -65794
        if (borderColor == 16777215) borderColor = -65794

        if (edgeRadius < 0.0f) {
            edgeRadius = 0.0f
        }

        if (edgeRadius > width / 2.0f) {
            edgeRadius = width / 2.0f
        }

        if (edgeRadius > height / 2.0f) {
            edgeRadius = height / 2.0f
        }

        drawRDRect(x + edgeRadius, y + edgeRadius, width - edgeRadius * 2.0f, height - edgeRadius * 2.0f, color)
        drawRDRect(x + edgeRadius, y, width - edgeRadius * 2.0f, edgeRadius, color)
        drawRDRect(x + edgeRadius, y + height - edgeRadius, width - edgeRadius * 2.0f, edgeRadius, color)
        drawRDRect(x, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0f, color)
        drawRDRect(x + width - edgeRadius, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0f, color)
        enableRender2D()
        color(color)
        GL11.glBegin(6)
        var centerX = x + edgeRadius
        var centerY = y + edgeRadius
        GL11.glVertex2d(centerX.toDouble(), centerY.toDouble())
        var vertices = min(max(edgeRadius, 10.0f), 90.0f).toInt()

        var i: Int
        var angleRadians: Double
        i = 0
        while (i < vertices + 1) {
            angleRadians = 6.283185307179586 * (i + 180).toDouble() / (vertices * 4).toDouble()
            GL11.glVertex2d(
                centerX.toDouble() + sin(angleRadians) * edgeRadius.toDouble(),
                centerY.toDouble() + cos(angleRadians) * edgeRadius.toDouble()
            )
            ++i
        }

        GL11.glEnd()
        GL11.glBegin(6)
        centerX = x + width - edgeRadius
        centerY = y + edgeRadius
        GL11.glVertex2d(centerX.toDouble(), centerY.toDouble())
        vertices = min(max(edgeRadius, 10.0f), 90.0f).toInt()

        i = 0
        while (i < vertices + 1) {
            angleRadians = 6.283185307179586 * (i + 90).toDouble() / (vertices * 4).toDouble()
            GL11.glVertex2d(
                centerX.toDouble() + sin(angleRadians) * edgeRadius.toDouble(),
                centerY.toDouble() + cos(angleRadians) * edgeRadius.toDouble()
            )
            ++i
        }

        GL11.glEnd()
        GL11.glBegin(6)
        centerX = x + edgeRadius
        centerY = y + height - edgeRadius
        GL11.glVertex2d(centerX.toDouble(), centerY.toDouble())
        vertices = min(max(edgeRadius, 10.0f), 90.0f).toInt()

        i = 0
        while (i < vertices + 1) {
            angleRadians = 6.283185307179586 * (i + 270).toDouble() / (vertices * 4).toDouble()
            GL11.glVertex2d(
                centerX.toDouble() + sin(angleRadians) * edgeRadius.toDouble(),
                centerY.toDouble() + cos(angleRadians) * edgeRadius.toDouble()
            )
            ++i
        }

        GL11.glEnd()
        GL11.glBegin(6)

        centerX = x + width - edgeRadius
        centerY = y + height - edgeRadius
        GL11.glVertex2d(centerX.toDouble(), centerY.toDouble())
        vertices = min(max(edgeRadius, 10.0f), 90.0f).toInt()

        i = 0
        while (i < vertices + 1) {
            angleRadians = 6.283185307179586 * i.toDouble() / (vertices * 4).toDouble()
            GL11.glVertex2d(
                centerX.toDouble() + sin(angleRadians) * edgeRadius.toDouble(),
                centerY.toDouble() + cos(angleRadians) * edgeRadius.toDouble()
            )
            ++i
        }

        GL11.glEnd()
        color(borderColor)
        GL11.glLineWidth(borderWidth)
        GL11.glBegin(3)
        centerX = x + edgeRadius
        centerY = y + edgeRadius
        vertices = min(max(edgeRadius, 10.0f), 90.0f).toInt()

        i = vertices
        while (i >= 0) {
            angleRadians = 6.283185307179586 * (i + 180).toDouble() / (vertices * 4).toDouble()
            GL11.glVertex2d(
                centerX.toDouble() + sin(angleRadians) * edgeRadius.toDouble(),
                centerY.toDouble() + cos(angleRadians) * edgeRadius.toDouble()
            )
            --i
        }

        GL11.glVertex2d((x + edgeRadius).toDouble(), y.toDouble())
        GL11.glVertex2d((x + width - edgeRadius).toDouble(), y.toDouble())
        centerX = x + width - edgeRadius
        centerY = y + edgeRadius

        i = vertices
        while (i >= 0) {
            angleRadians = 6.283185307179586 * (i + 90).toDouble() / (vertices * 4).toDouble()
            GL11.glVertex2d(
                centerX.toDouble() + sin(angleRadians) * edgeRadius.toDouble(),
                centerY.toDouble() + cos(angleRadians) * edgeRadius.toDouble()
            )
            --i
        }

        GL11.glVertex2d((x + width).toDouble(), (y + edgeRadius).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + height - edgeRadius).toDouble())
        centerX = x + width - edgeRadius
        centerY = y + height - edgeRadius

        i = vertices
        while (i >= 0) {
            angleRadians = 6.283185307179586 * i.toDouble() / (vertices * 4).toDouble()
            GL11.glVertex2d(
                centerX.toDouble() + sin(angleRadians) * edgeRadius.toDouble(),
                centerY.toDouble() + cos(angleRadians) * edgeRadius.toDouble()
            )
            --i
        }

        GL11.glVertex2d((x + width - edgeRadius).toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + edgeRadius).toDouble(), (y + height).toDouble())

        centerX = x + edgeRadius
        centerY = y + height - edgeRadius

        i = vertices
        while (i >= 0) {
            angleRadians = 6.283185307179586 * (i + 270).toDouble() / (vertices * 4).toDouble()
            GL11.glVertex2d(
                centerX.toDouble() + sin(angleRadians) * edgeRadius.toDouble(),
                centerY.toDouble() + cos(angleRadians) * edgeRadius.toDouble()
            )
            --i
        }

        GL11.glVertex2d(x.toDouble(), (y + height - edgeRadius).toDouble())
        GL11.glVertex2d(x.toDouble(), (y + edgeRadius).toDouble())
        GL11.glEnd()
        disableRender2D()
    }

    fun disableRender2D() {
        GL11.glDisable(3042)
        GL11.glEnable(2884)
        GL11.glEnable(3553)
        GL11.glDisable(2848)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

    fun drawRDRect(left: Float, top: Float, width: Float, height: Float, color: Int) {
        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), (top + height).toDouble(), 0.0).endVertex()
        worldrenderer.pos((left + width).toDouble(), (top + height).toDouble(), 0.0).endVertex()
        worldrenderer.pos((left + width).toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().getTextureManager().bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
    }

    fun drawImage(image: ResourceLocation?, x: Float, y: Float, width: Float, height: Float) {
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().getTextureManager().bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(
            x.toInt(),
            y.toInt(),
            0f,
            0f,
            width.toInt(),
            height.toInt(),
            width,
            height
        )
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
    }

    fun drawImage(image: ResourceLocation?, x: Float, y: Float, width: Float, height: Float, alpha: Float) {
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha)
        Minecraft.getMinecraft().getTextureManager().bindTexture(image)
        drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height)
        GL11.glDepthMask(true)
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        GlStateManager.resetColor()
    }


    fun enableRender2D() {
        GL11.glEnable(3042)
        GL11.glDisable(2884)
        GL11.glDisable(3553)
        GL11.glEnable(2848)
        GL11.glBlendFunc(770, 771)
        GL11.glLineWidth(1.0f)
    }

    fun drawCustomImage(x: Int, y: Int, width: Int, height: Int, image: ResourceLocation?) {
        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        GL11.glDisable(2929)
        GL11.glEnable(3042)
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().getTextureManager().bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width.toFloat(), height.toFloat())
        GL11.glDepthMask(true)
        GL11.glDisable(3042)
        GL11.glEnable(2929)
    }

    fun drawImage(image: ResourceLocation?, x: Float, y: Float, width: Float, height: Float, color: Int) {
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        val f = (color shr 24 and 255).toFloat() / 255.0f
        val f1 = (color shr 16 and 255).toFloat() / 255.0f
        val f2 = (color shr 8 and 255).toFloat() / 255.0f
        val f3 = (color and 255).toFloat() / 255.0f
        GL11.glColor4f(f1, f2, f3, f)
        Minecraft.getMinecraft().getTextureManager().bindTexture(image)
        drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height)
        GL11.glDepthMask(true)
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        GlStateManager.resetColor()
    }

    fun drawModalRectWithCustomSizedTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        width: Float,
        height: Float,
        textureWidth: Float,
        textureHeight: Float
    ) {
        val f = 1.0f / textureWidth
        val f1 = 1.0f / textureHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }


    fun drawRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }

        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) {
        drawRect(x, y, x2, y2, color.getRGB())
    }

    fun drawRect(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }

        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left, bottom, 0.0).endVertex()
        worldrenderer.pos(right, bottom, 0.0).endVertex()
        worldrenderer.pos(right, top, 0.0).endVertex()
        worldrenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, l1: Float, col1: Int, col2: Int) {
        drawRect(x, y, x2, y2, col2)
        val f = (col1 shr 24 and 0xFF) / 255.0f
        val f2 = (col1 shr 16 and 0xFF) / 255.0f
        val f3 = (col1 shr 8 and 0xFF) / 255.0f
        val f4 = (col1 and 0xFF) / 255.0f
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glPushMatrix()
        GL11.glColor4f(f2, f3, f4, f)
        GL11.glLineWidth(l1)
        GL11.glBegin(1)
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x2.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y2.toDouble())
        GL11.glEnd()
        GL11.glPopMatrix()
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
    }

    fun pre() {
        GL11.glDisable(2929)
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
    }

    fun post() {
        GL11.glDisable(3042)
        GL11.glEnable(3553)
        GL11.glEnable(2929)
        GL11.glColor3d(1.0, 1.0, 1.0)
    }

    fun startDrawing() {
        GL11.glEnable(3042)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glDisable(3553)
        GL11.glDisable(2929)
    }

    fun stopDrawing() {
        GL11.glDisable(3042)
        GL11.glEnable(3553)
        GL11.glDisable(2848)
        GL11.glDisable(3042)
        GL11.glEnable(2929)
    }

    fun blend(color1: Color, color2: Color, ratio: Double): Color {
        val r = ratio.toFloat()
        val ir = 1.0f - r
        val rgb1 = FloatArray(3)
        val rgb2 = FloatArray(3)
        color1.getColorComponents(rgb1)
        color2.getColorComponents(rgb2)
        val color3 = Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir)
        return color3
    }

    fun resetColor() {
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    @JvmOverloads
    fun color(color: Int, alpha: Float = (color shr 24 and 255).toFloat() / 255.0f) {
        val r = (color shr 16 and 255).toFloat() / 255.0f
        val g = (color shr 8 and 255).toFloat() / 255.0f
        val b = (color and 255).toFloat() / 255.0f
        GlStateManager.color(r, g, b, alpha)
    }

    fun setupRender(start: Boolean) {
        if (start) {
            GlStateManager.enableBlend()
            GL11.glEnable(2848)
            GlStateManager.disableDepth()
            GlStateManager.disableTexture2D()
            GlStateManager.blendFunc(770, 771)
            GL11.glHint(3154, 4354)
        } else {
            GlStateManager.disableBlend()
            GlStateManager.enableTexture2D()
            GL11.glDisable(2848)
            GlStateManager.enableDepth()
        }
        GlStateManager.depthMask(!start)
    }


    fun bindTexture(texture: Int) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture)
    }
}