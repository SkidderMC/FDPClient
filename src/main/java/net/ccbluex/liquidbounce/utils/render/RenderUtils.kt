/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import com.jhlabs.image.GaussianFilter
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.DOUBLE_PI
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorPrimary
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorSecondary
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.start
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.block.toVec
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.client.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.io.flipSafely
import net.ccbluex.liquidbounce.utils.render.ColorUtils.getMainColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils.glFloatColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils.setColour
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeInOutQuadX
import net.ccbluex.liquidbounce.utils.render.shader.UIEffectRenderer.drawTexturedRect
import net.minecraft.client.Minecraft.getMinecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.*
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.glBlendFuncSeparate
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import javax.vecmath.Point3d
import kotlin.math.*


object RenderUtils : MinecraftInstance {
    // ARGB 0xff006fff
    const val CLIENT_COLOR = -16748545
    // ARGB 0x7f006fff
    const val CLIENT_COLOR_HALF_ALPHA = 2130735103

    private val glowCircle = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/targetesp/glow_circle.png")

    private val glCapMap = mutableMapOf<Int, Boolean>()
    private val shadowCache: HashMap<Int, Int> = HashMap()
    private val DISPLAY_LISTS_2D = IntArray(4) {
        glGenLists(1)
    }
    var deltaTime = 0

    private val frustrum = Frustum()

    @JvmStatic
    private fun setupFBO(fbo: Framebuffer) {
        // Deletes old render buffer extensions such as depth
        // Args: Render Buffer ID
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)
        // Generates a new render buffer ID for the depth and stencil extension
        val stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT()
        // Binds new render buffer by ID
        // Args: Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID)
        // Adds the depth and stencil extension
        // Args: Target (GL_RENDERBUFFER_EXT), Extension (GL_DEPTH_STENCIL_EXT),
        // Width, Height
        EXTFramebufferObject.glRenderbufferStorageEXT(
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
            mc.displayWidth,
            mc.displayHeight
        )
        // Adds the stencil attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_STENCIL_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencil_depth_buffer_ID
        )
        // Adds the depth attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_DEPTH_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencil_depth_buffer_ID
        )
    }

    @JvmStatic
    fun checkSetupFBO() {
        // Gets the FBO of Minecraft
        val fbo = mc.framebuffer

        // Check if FBO isn't null
        if (fbo != null) {
            // Checks if screen has been resized or new FBO has been created
            if (fbo.depthBuffer > -1) {
                // Sets up the FBO with depth and stencil extensions (24/8 bit)
                setupFBO(fbo)
                // Reset the ID to prevent multiple FBO's
                fbo.depthBuffer = -1
            }
        }
    }

    /**
     * Useful for clipping any top-layered rectangle that falls outside a bottom-layered rectangle.
     */
    inline fun withClipping(main: () -> Unit, toClip: () -> Unit) {
        disableFastRender()
        checkSetupFBO()
        glPushMatrix()

        disableAlpha()

        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 1)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glStencilMask(1)
        glClear(GL_STENCIL_BUFFER_BIT)

        main()

        glStencilFunc(GL_EQUAL, 1, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glStencilMask(0)

        toClip()

        glStencilMask(0xFF)
        glDisable(GL_STENCIL_TEST)

        enableAlpha()

        glPopMatrix()
    }

    inline fun withOutline(main: () -> Unit, toOutline: () -> Unit) {
        disableFastRender()
        checkSetupFBO()
        glPushMatrix()

        disableAlpha()

        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)

        glStencilFunc(GL_ALWAYS, 1, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        glStencilMask(1)

        main()

        glStencilFunc(GL_EQUAL, 0, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glStencilMask(0)

        toOutline()

        glStencilMask(0xFF)
        glDisable(GL_STENCIL_TEST)

        enableAlpha()

        glPopMatrix()
    }

    fun deltaTimeNormalized(ticks: Int = 1) = (deltaTime safeDivD ticks * 50.0).coerceAtMost(1.0)

    private const val CIRCLE_STEPS = 40
    private val circlePoints = Array(CIRCLE_STEPS + 1) {
        val theta = 2 * PI * it / CIRCLE_STEPS
        Point3d(-sin(theta), 0.0, cos(theta))
    }

    fun drawHueCircle(position: Vec3, radius: Float, innerColor: Color, outerColor: Color) {
        val manager = mc.renderManager

        val renderX = manager.viewerPosX
        val renderY = manager.viewerPosY
        val renderZ = manager.viewerPosZ

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0f)
        mc.entityRenderer.disableLightmap()

        glBegin(GL_TRIANGLE_FAN)
        circlePoints.forEachIndexed { index, pos ->
            val innerX = pos.x * radius
            val innerZ = pos.z * radius

            val innerHue = ColorUtils.shiftHue(innerColor, (index / CIRCLE_STEPS))
            glColor4f(innerHue.red / 255f, innerHue.green / 255f, innerHue.blue / 255f, innerColor.alpha / 255f)
            glVertex3d(
                position.xCoord - renderX + innerX, position.yCoord - renderY, position.zCoord - renderZ + innerZ
            )
        }
        glEnd()

        glBegin(GL_LINE_LOOP)
        circlePoints.forEachIndexed { index, pos ->
            val outerX = pos.x * radius
            val outerZ = pos.z * radius

            val outerHue = ColorUtils.shiftHue(outerColor, (index / CIRCLE_STEPS))
            glColor4f(outerHue.red / 255f, outerHue.green / 255f, outerHue.alpha / 255f, outerColor.alpha / 255f)
            glVertex3d(
                position.xCoord - renderX + outerX, position.yCoord - renderY, position.zCoord - renderZ + outerZ
            )
        }
        glEnd()

        glEnable(GL_CULL_FACE)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
        glPopAttrib()
    }

    init {
        glNewList(DISPLAY_LISTS_2D[0], GL_COMPILE)
        quickDrawRect(-7f, 2f, -4f, 3f)
        quickDrawRect(4f, 2f, 7f, 3f)
        quickDrawRect(-7f, 0.5f, -6f, 3f)
        quickDrawRect(6f, 0.5f, 7f, 3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[1], GL_COMPILE)
        quickDrawRect(-7f, 3f, -4f, 3.3f)
        quickDrawRect(4f, 3f, 7f, 3.3f)
        quickDrawRect(-7.3f, 0.5f, -7f, 3.3f)
        quickDrawRect(7f, 0.5f, 7.3f, 3.3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[2], GL_COMPILE)
        quickDrawRect(4f, -20f, 7f, -19f)
        quickDrawRect(-7f, -20f, -4f, -19f)
        quickDrawRect(6f, -20f, 7f, -17.5f)
        quickDrawRect(-7f, -20f, -6f, -17.5f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[3], GL_COMPILE)
        quickDrawRect(7f, -20f, 7.3f, -17.5f)
        quickDrawRect(-7.3f, -20f, -7f, -17.5f)
        quickDrawRect(4f, -20.3f, 7.3f, -20f)
        quickDrawRect(-7.3f, -20.3f, -4f, -20f)
        glEndList()
    }

    @JvmStatic
    fun BlockPos.drawBlockDamageText(
        currentDamage: Float,
        font: FontRenderer,
        fontShadow: Boolean,
        color: Int,
        scale: Float,
    ) {
        require(currentDamage in 0f..1f)
        val renderManager = mc.renderManager
        val progress = (currentDamage * 100).coerceIn(0f, 100f).toInt()
        val progressText = "$progress%"
        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()
        val (x, y, z) = this.center - renderManager.renderPos
        // Translate to block position
        glTranslated(x, y, z)
        glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(renderManager.playerViewX, 1F, 0F, 0F)
        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)
        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        // Scale
        val renderScale = (mc.thePlayer.getDistanceSq(this) / 8F).coerceAtLeast(1.5) / 150F * scale
        glScaled(-renderScale, -renderScale, renderScale)
        // Draw text
        val width = font.getStringWidth(progressText) * 0.5f
        font.drawString(
            progressText, -width, if (font == Fonts.minecraftFont) 1F else 1.5F, color, fontShadow
        )
        resetCaps()
        glPopMatrix()
        glPopAttrib()
    }

    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager

        val (x, y, z) = blockPos.toVec() - renderManager.renderPos

        var axisAlignedBB = AxisAlignedBB.fromBounds(x, y, z, x + 1.0, y + 1.0, z + 1.0)

        blockPos.block?.let { block ->
            val player = mc.thePlayer

            val pos = -player.interpolatedPosition(player.lastTickPos)

            val f = 0.002F.toDouble()

            block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)

            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(f, f, f).offset(pos)
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) = drawWithTessellatorWorldRenderer {
        begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        // Lower Rectangle
        pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
    }

    fun drawCircle(
        entity: EntityLivingBase,
        speed: Float,
        height: ClosedFloatingPointRange<Float>,
        size: Float,
        filled: Boolean,
        withHeight: Boolean,
        circleY: ClosedFloatingPointRange<Float>? = null,
        startColor: Int,
        endColor: Int
    ) {
        val manager = mc.renderManager
        val positions = mutableListOf<DoubleArray>()

        val renderX = manager.viewerPosX
        val renderY = manager.viewerPosY
        val renderZ = manager.viewerPosZ

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glDepthMask(false)
        glAlphaFunc(GL_GREATER, 0.0f)
        mc.entityRenderer.disableLightmap()

        shadeModel(GL_SMOOTH)

        val a1 = (startColor shr 24 and 255) / 255f
        val r1 = (startColor shr 16 and 255) / 255f
        val g1 = (startColor shr 8 and 255) / 255f
        val b1 = (startColor and 255) / 255f
        val a2 = (endColor shr 24 and 255) / 255f
        val r2 = (endColor shr 16 and 255) / 255f
        val g2 = (endColor shr 8 and 255) / 255f
        val b2 = (endColor and 255) / 255f

        val breathingT = AnimationUtil.breathe(speed)
        val entityHeight = (entity.hitBox.maxY - entity.hitBox.minY).toFloat()

        val width = (mc.renderManager.getEntityRenderObject<Entity>(entity)?.shadowSize ?: 0.5F) + size
        val animatedHeight = (0F..entityHeight).lerpWith((height.endInclusive..height.start).lerpWith(breathingT))
        val animatedCircleY = (0F..entityHeight).lerpWith(circleY?.lerpWith(breathingT) ?: 0F)

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.worldRenderer

        if (filled) {
            buffer.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR)
        }

        entity.interpolatedPosition(entity.prevPos).let { pos ->
            circlePoints.forEachIndexed { index, it ->
                val p = pos + Vec3(it.x * width, it.y + animatedCircleY, it.z * width)
                positions += doubleArrayOf(p.xCoord, p.yCoord, p.zCoord)

                if (filled) {
                    buffer.pos(p.xCoord - renderX, p.yCoord - renderY, p.zCoord - renderZ).color(r1, g1, b1, a1)
                        .endVertex()
                }
            }
        }

        if (filled) {
            tessellator.draw()
        }

        if (withHeight) {
            buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)

            positions.forEachIndexed { index, pos ->
                val endPos = positions.getOrNull(index + 1) ?: return@forEachIndexed

                buffer.pos(pos[0] - renderX, pos[1] - renderY, pos[2] - renderZ).color(r1, g1, b1, a1).endVertex()
                buffer.pos(endPos[0] - renderX, endPos[1] - renderY, endPos[2] - renderZ).color(r1, g1, b1, a1)
                    .endVertex()
                buffer.pos(endPos[0] - renderX, endPos[1] - renderY + animatedHeight, endPos[2] - renderZ)
                    .color(r2, g2, b2, a2).endVertex()
                buffer.pos(pos[0] - renderX, pos[1] - renderY + animatedHeight, pos[2] - renderZ).color(r2, g2, b2, a2)
                    .endVertex()
            }
            tessellator.draw()
        }

        shadeModel(GL_FLAT)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_BLEND)
        glEnable(GL_CULL_FACE)
        glDepthMask(true)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
        glPopAttrib()
    }

    fun drawPoints(
        target: EntityLivingBase,
        baseColor: Color,
        speed: Float,
        pointsRadius: Float,
        pointsScale: Float,
        pointsLayers: Int,
        pointsAdditive: Boolean,
        hurt: Boolean
    ) {
        val rm = mc.renderManager
        val partial = mc.timer.renderPartialTicks

        val x = target.lastTickPosX + (target.posX - target.lastTickPosX) * partial - rm.renderPosX
        val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * partial - rm.renderPosY + target.height / 1.6f
        val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partial - rm.renderPosZ

        val altColor = Color(baseColor.red, baseColor.green, baseColor.blue, (baseColor.alpha * 0.75f).toInt())

        val now = System.currentTimeMillis()
        val s = (1500.0 / speed.coerceAtLeast(0.0001f))
        val u = (now % 1_000_000L).toDouble() / s
        val t = u + sin(u) / 10.0

        pushMatrix()
        translate(x, y, z)

        glRotatef(-rm.playerViewY, 0f, 1f, 0f)
        glRotatef(rm.playerViewX, 1f, 0f, 0f)

        disableCull()
        enableBlend()
        if (pointsAdditive) {
            tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ZERO)
        } else {
            blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }
        disableAlpha()

        mc.textureManager.bindTexture(glowCircle)
        enableTexture2D()

        val tess = Tessellator.getInstance()
        val vb = tess.worldRenderer

        var layerYOffset = 0.0
        var flip = false
        val layers = pointsLayers.coerceAtLeast(1)

        repeat(layers) {
            val angle = (t * 360.0 * if (flip) -1.0 else 1.0)
            val end = angle + 90.0 * if (flip) -1.0 else 1.0
            val step = if (flip) -2.0 else 2.0

            var i = angle
            while (if (flip) i >= end else i <= end) {
                val prog = abs((i - angle) / 90.0).coerceIn(0.0, 1.0)

                val rad = Math.toRadians(i)
                val rf = pointsRadius
                val pointY = layerYOffset + sin(rad * 1.2) * 0.10

                val sizeBase = (if (!flip) 0.25f else 0.15f) *
                        (max(if (flip) 0.25f else 0.15f, if (flip) prog.toFloat() else (1f + (0.4f - prog.toFloat())) / 2f) + 0.45f)

                val size = (sizeBase * (2f + ((1f - 0.5f) * 2f)) * pointsScale).toDouble()
                val half = size / 2.0

                val c = if (prog < 0.5) baseColor else altColor
                val r = c.red / 255f
                val g = c.green / 255f
                val b = c.blue / 255f
                val a = (c.alpha / 255f) * (if (hurt && target.hurtTime > 3) 1.0f else 0.9f)

                pushMatrix()
                translate((cos(rad).toFloat() * rf).toDouble(), pointY, (sin(rad).toFloat() * rf).toDouble())

                vb.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
                vb.pos(-half, -half, 0.0).tex(0.0, 0.0).color((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt(), (a * 255).toInt()).endVertex()
                vb.pos(-half,  half, 0.0).tex(0.0, 1.0).color((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt(), (a * 255).toInt()).endVertex()
                vb.pos( half,  half, 0.0).tex(1.0, 1.0).color((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt(), (a * 255).toInt()).endVertex()
                vb.pos( half, -half, 0.0).tex(1.0, 0.0).color((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt(), (a * 255).toInt()).endVertex()
                tess.draw()
                popMatrix()

                i += step
            }

            flip = !flip
            layerYOffset += 0.45
        }

        enableAlpha()
        disableBlend()
        enableCull()
        popMatrix()
    }

    fun drawImageMark(
        target: EntityLivingBase,
        texture: ResourceLocation,
        color1: Color,
        color2: Color,
        color3: Color,
        color4: Color,
        scale: Float,
        xOffset: Float,
        yOffset: Float,
        additive: Boolean,
        spin: Boolean,
        spinSpeed: Float,
        billboard: Boolean,
        hurt: Boolean
    ) {
        val rm = mc.renderManager
        val partial = mc.timer.renderPartialTicks
        val x = target.lastTickPosX + (target.posX - target.lastTickPosX) * partial - rm.renderPosX + xOffset
        val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * partial - rm.renderPosY + target.height / 1.6f + yOffset
        val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partial - rm.renderPosZ
        val aMul = if (hurt && target.hurtTime > 3) 1f else 0.9f
        val r1 = color1.red; val g1 = color1.green; val b1 = color1.blue; val a1 = (color1.alpha * aMul).toInt().coerceIn(0,255)
        val r2 = color2.red; val g2 = color2.green; val b2 = color2.blue; val a2 = (color2.alpha * aMul).toInt().coerceIn(0,255)
        val r3 = color3.red; val g3 = color3.green; val b3 = color3.blue; val a3 = (color3.alpha * aMul).toInt().coerceIn(0,255)
        val r4 = color4.red; val g4 = color4.green; val b4 = color4.blue; val a4 = (color4.alpha * aMul).toInt().coerceIn(0,255)
        pushMatrix()
        translate(x, y, z)
        if (billboard) {
            glRotatef(-rm.playerViewY, 0f, 1f, 0f)
            glRotatef(rm.playerViewX, 1f, 0f, 0f)
        }
        if (spin) {
            val now = System.currentTimeMillis()
            val ang = ((now % 10000L).toFloat() / 10000f) * 360f * spinSpeed
            glRotatef(ang, 0f, 0f, 1f)
        }
        disableCull()
        enableBlend()
        if (additive) {
            tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ZERO)
        } else {
            blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }
        disableAlpha()
        mc.textureManager.bindTexture(texture)
        enableTexture2D()
        val tess = Tessellator.getInstance()
        val vb = tess.worldRenderer
        val half = (scale / 2f).toDouble()
        vb.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
        vb.pos(-half, -half, 0.0).tex(0.0, 0.0).color(r1, g1, b1, a1).endVertex()
        vb.pos(-half,  half, 0.0).tex(0.0, 1.0).color(r2, g2, b2, a2).endVertex()
        vb.pos( half,  half, 0.0).tex(1.0, 1.0).color(r3, g3, b3, a3).endVertex()
        vb.pos( half, -half, 0.0).tex(1.0, 0.0).color(r4, g4, b4, a4).endVertex()
        tess.draw()
        enableAlpha()
        disableBlend()
        enableCull()
        popMatrix()
    }

    /**
     * Draws a dome around the specified [pos]
     *
     * Only [GL_LINES], [GL_TRIANGLES] and [GL_QUADS] are allowed.
     */
    fun drawDome(pos: Vec3, hRadius: Double, vRadius: Double, lineWidth: Float? = null, color: Color, renderMode: Int) {
        require(renderMode in arrayOf(GL_LINES, GL_TRIANGLES, GL_QUADS))

        val manager = mc.renderManager ?: return

        val renderX = manager.viewerPosX
        val renderY = manager.viewerPosY
        val renderZ = manager.viewerPosZ
        val (posX, posY, posZ) = pos

        val vStep = Math.PI / (CIRCLE_STEPS / 2)
        val hStep = 2 * Math.PI / CIRCLE_STEPS

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        lineWidth?.let { glLineWidth(it) }
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0f)
        glTranslated(-renderX, -renderY, -renderZ)
        glColor(color)

        drawWithTessellatorWorldRenderer {
            begin(renderMode, DefaultVertexFormats.POSITION)

            val min = if (renderMode != GL_TRIANGLES) 0 to 0 else -1 to 1

            for (i in min.first until CIRCLE_STEPS / 2) {
                val vAngle1 = i * vStep
                val vAngle2 = (i + 1) * vStep

                for (j in min.second until CIRCLE_STEPS) {
                    val hAngle1 = j * hStep
                    val hAngle2 = (j + 1) * hStep

                    val p1 = calculateDomeVertex(posX, posY, posZ, vAngle1, hAngle1, hRadius, vRadius)
                    val p2 = calculateDomeVertex(posX, posY, posZ, vAngle2, hAngle1, hRadius, vRadius)
                    val p3 = calculateDomeVertex(posX, posY, posZ, vAngle2, hAngle2, hRadius, vRadius)
                    val p4 = calculateDomeVertex(posX, posY, posZ, vAngle1, hAngle2, hRadius, vRadius)

                    when (renderMode) {
                        GL_QUADS -> {
                            pos(p1[0], p1[1], p1[2]).endVertex()
                            pos(p2[0], p2[1], p2[2]).endVertex()
                            pos(p3[0], p3[1], p3[2]).endVertex()
                            pos(p4[0], p4[1], p4[2]).endVertex()
                        }

                        GL_TRIANGLES, GL_LINES -> {
                            pos(p1[0], p1[1], p1[2]).endVertex()
                            pos(p2[0], p2[1], p2[2]).endVertex()

                            pos(p2[0], p2[1], p2[2]).endVertex()
                            pos(p3[0], p3[1], p3[2]).endVertex()

                            pos(p3[0], p3[1], p3[2]).endVertex()
                            pos(p4[0], p4[1], p4[2]).endVertex()

                            pos(p4[0], p4[1], p4[2]).endVertex()
                            pos(p1[0], p1[1], p1[2]).endVertex()
                        }
                    }
                }
            }
        }

        glEnable(GL_CULL_FACE)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)

        glPopMatrix()
        glPopAttrib()
    }
    private fun calculateDomeVertex(
        entityX: Double,
        entityY: Double,
        entityZ: Double,
        theta: Double,
        phi: Double,
        horizontalRadius: Double,
        verticalRadius: Double
    ): DoubleArray {
        return doubleArrayOf(
            entityX + horizontalRadius * sin(theta) * cos(phi),
            entityY + verticalRadius * cos(theta),
            entityZ + horizontalRadius * sin(theta) * sin(phi)
        )
    }

    fun drawConesForEntities(f: () -> Unit) {
        pushAttrib()
        pushMatrix()

        disableTexture2D()
        disableCull()

        enableBlend()
        glEnable(GL_DEPTH_TEST)
        depthMask(false)
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        f()

        resetColor()

        enableTexture2D()
        depthMask(true)
        enableCull()

        disableBlend()
        glDisable(GL_DEPTH_TEST)

        popMatrix()
        popAttrib()
    }

    fun drawCone(width: Float, height: Float, useTexture: Boolean = false) {
        if (useTexture) {
            mc.textureManager.bindTexture(ResourceLocation("fdpclient/texture/hat.png"))
            enableTexture2D()
            depthMask(true)
        }

        drawWithTessellatorWorldRenderer {
            begin(GL_TRIANGLE_FAN, if (useTexture) DefaultVertexFormats.POSITION_TEX else DefaultVertexFormats.POSITION)

            if (useTexture) {
                pos(0.0, height.toDouble(), 0.0).tex(0.5, 0.5).endVertex()
            } else {
                pos(0.0, height.toDouble(), 0.0).endVertex()
            }

            for (point in circlePoints) {
                if (useTexture) {
                    val u = 0.5 + 0.5 * point.x
                    val v = 0.5 + 0.5 * point.z
                    pos(point.x * width, 0.0, point.z * width).tex(u, v).endVertex()
                } else {
                    pos(point.x * width, 0.0, point.z * width).endVertex()
                }
            }
        }
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)

        val (x, y, z) = entity.interpolatedPosition(entity.lastTickPos) - mc.renderManager.renderPos
        val entityBox = entity.hitBox

        val axisAlignedBB = AxisAlignedBB.fromBounds(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawPosBox(x: Double, y: Double, z: Double, width: Float, height: Float, color: Color, outline: Boolean) {
        val (adjustedX, adjustedY, adjustedZ) = Vec3(x, y, z) - mc.renderManager.renderPos

        val axisAlignedBB = AxisAlignedBB.fromBounds(
            adjustedX - width / 2,
            adjustedY,
            adjustedZ - width / 2,
            adjustedX + width / 2,
            adjustedY + height,
            adjustedZ + width / 2
        )

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)

        glDepthMask(false)

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)

        resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawBacktrackBox(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color.red, color.green, color.blue, 90)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawAxisAlignedBB(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawPlatform(y: Double, color: Color, size: Double) {
        val renderY = y - mc.renderManager.renderPosY
        drawAxisAlignedBB(AxisAlignedBB.fromBounds(size, renderY + 0.02, size, -size, renderY, -size), color)
    }

    fun drawPlatformESP(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer

        val axisAlignedBB = entity.entityBoundingBox.offset(-entity.posX, -entity.posY, -entity.posZ).offset(
            (entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * (timer.renderPartialTicks.toDouble()))) - renderManager.renderPosX,
            (entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * (timer.renderPartialTicks.toDouble()))) - renderManager.renderPosY,
            (entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * (timer.renderPartialTicks.toDouble()))) - renderManager.renderPosZ
        )
       drawAxisAlignedBB(
            AxisAlignedBB(
                axisAlignedBB.minX,
                axisAlignedBB.maxY - 0.5,
                axisAlignedBB.minZ,
                axisAlignedBB.maxX,
                axisAlignedBB.maxY + 0.2,
                axisAlignedBB.maxZ
            ), color
        )
    }

    fun drawPlatform(entity: Entity, color: Color) {
        val deltaPos = entity.interpolatedPosition(entity.lastTickPos) - mc.renderManager.renderPos
        val axisAlignedBB = entity.entityBoundingBox.offset(-entity.currPos + deltaPos)

        drawAxisAlignedBB(
            AxisAlignedBB.fromBounds(
                axisAlignedBB.minX,
                axisAlignedBB.maxY + 0.2,
                axisAlignedBB.minZ,
                axisAlignedBB.maxX,
                axisAlignedBB.maxY + 0.26,
                axisAlignedBB.maxZ
            ), color
        )
    }

    fun enableSmoothLine(width: Float) {
        glDisable(3008)
        glEnable(3042)
        glBlendFunc(770, 771)
        glDisable(3553)
        glDisable(2929)
        glDepthMask(false)
        glEnable(2884)
        glEnable(2848)
        glHint(3154, 4354)
        glHint(3155, 4354)
        glLineWidth(width)
    }

    /**
     * Enables smooth line and polygon rendering.
     * Adjusts the OpenGL settings to achieve smooth rendering effects.
     */
    fun startSmooth() {
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_POLYGON_SMOOTH)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)
        glHint(GL_POINT_SMOOTH_HINT, GL_NICEST)
    }

    /**
     * Disables smooth line and polygon rendering.
     * Restores the OpenGL settings after smooth rendering effects were applied.
     */
    fun endSmooth() {
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_POLYGON_SMOOTH)
        glEnable(GL_BLEND)
    }

    /**
     * Disables the smooth in line effect.
     * Restores the OpenGL states to their defaults for regular rendering.
     */
    fun disableSmoothLine() {
        glEnable(3553)
        glEnable(2929)
        glDisable(3042)
        glEnable(3008)
        glDepthMask(true)
        glCullFace(1029)
        glDisable(2848)
        glHint(3154, 4352)
        glHint(3155, 4352)
    }


    /**
     * Draws an ESP (Extra Sensory Perception) effect around the given entity.
     *
     * @param entity The entity to draw the ESP effect around.
     * @param color The color of the ESP effect.
     * @param e The Render3DEvent containing partial ticks for interpolation.
     */
    fun drawCrystal(entity: EntityLivingBase, color: Int, e: Render3DEvent) {
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * e.partialTicks - mc.renderManager.renderPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * e.partialTicks - mc.renderManager.renderPosY
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * e.partialTicks - mc.renderManager.renderPosZ
        val radius = 0.15f
        val side = 4

        glPushMatrix()
        glTranslated(x, y + 2, z)
        glRotatef(-entity.width, 0.0f, 1.0f, 0.0f)

        glColor(color)
        enableSmoothLine(1.5f)

        val c = Cylinder()
        glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
        c.drawStyle = 100012
        glColor(if ((entity.hurtTime <= 0)) Color(80, 255, 80, 200) else Color(255, 0, 0, 200))
        c.draw(0.0f, radius, 0.3f, side, 1)
        c.drawStyle = 100012

        glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0.0f, 0.3f, side, 1)

        glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
        c.drawStyle = 100011

        glTranslated(0.0, 0.0, -0.3)
        glColor(color)
        c.draw(0.0f, radius, 0.3f, side, 1)
        c.drawStyle = 100011

        glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0.0f, 0.3f, side, 1)

        disableSmoothLine()
        glPopMatrix()
    }

    /**
     * Draws a visual effect around the specified entity in 3D space.
     *
     * @param event The render event containing the partial tick time for smooth rendering.
     */
    fun drawZavz(entity: EntityLivingBase, event: Render3DEvent, dual: Boolean) {
        val speed = 0.1f

        val ticks = event.partialTicks
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)

        startSmooth()

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

            glColor(grad.red, grad.green, grad.blue, grad.alpha)

            glVertex3d(posX, y, posZ)
            y += entity.height / precision
            glColor(0, 0, 0, 0)
        }

        glEnd()
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)

        endSmooth()

        glEnable(GL_TEXTURE_2D)
        glPopMatrix()

        if (dual) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)

            startSmooth()

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

                glColor(grad2.red, grad2.green, grad2.blue, grad2.alpha)

                glVertex3d(posX, y, posZ)
                y -= entity.height / precision
                glColor(0, 0, 0, 0)
            }

            glEnd()
            glDepthMask(true)
            glEnable(GL_DEPTH_TEST)

            endSmooth()

            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    /**
     * Draws a jello-like effect around the given entity.
     *
     * @param entity The entity to draw the jello effect around.
     * @param partialTicks The partial ticks for smooth rendering.
     */
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
            glColor4f(0F,0F,0F,0F)
        }

        glEnd()
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    fun drawFDP(entity: EntityLivingBase, event: Render3DEvent) {

        val themeTextColor = getColor(1).rgb

        val drawTime = (System.currentTimeMillis() % 1500).toInt()
        val drawMode = drawTime > 750
        var drawPercent = drawTime / 750.0
        // true when goes up
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
            glColor(themeTextColor)
            glVertex3d(x - sin(i * Math.PI / 180F) * radius, y, z + cos(i * Math.PI / 180F) * radius)
        }
        glEnd()

        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

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
        glShadeModel(7425)
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
        glShadeModel(7424)
        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }


    /**
     * Draws a rectangle.
     *
     * @param left The left coordinate.
     * @param top The top coordinate.
     * @param right The right coordinate.
     * @param bottom The bottom coordinate.
     * @param color The color of the rectangle.
     */
    fun drawFilledRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
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

        val alpha = (color shr 24 and 255) / 255.0f
        val red = (color shr 16 and 255) / 255.0f
        val green = (color shr 8 and 255) / 255.0f
        val blue = (color and 255) / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(red, green, blue, alpha)
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldRenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldRenderer.pos(right.toDouble(), top.toDouble(), 0.0).endVertex()
        worldRenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    fun drawFilledBox(axisAlignedBB: AxisAlignedBB) = drawWithTessellatorWorldRenderer {
        begin(7, DefaultVertexFormats.POSITION)
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) = drawRect(x, y, x2, y2, color.rgb)

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, borderColor: Int, rectColor: Int) {
        drawRect(x, y, x2, y2, rectColor)
        drawBorder(x, y, x2, y2, width, borderColor)
    }

    fun drawBorderedRect(x: Int, y: Int, x2: Int, y2: Int, width: Number, borderColor: Int, rectColor: Int) {
        drawRect(x, y, x2, y2, rectColor)
        drawBorder(x, y, x2, y2, width, borderColor)
    }


    fun drawRoundedBorderRect(
        x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int, radius: Float
    ) {
        drawRoundedRect(x, y, x2, y2, color1, radius)
        drawRoundedBorder(x, y, x2, y2, width, color2, radius)
    }

    fun drawRectBasedBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int) {
        drawRect(x - width / 2f, y - width / 2f, x2 + width / 2f, y + width / 2f, color1)
        drawRect(x - width / 2f, y + width / 2f, x + width / 2f, y2 + width / 2f, color1)
        drawRect(x2 - width / 2f, y + width / 2f, x2 + width / 2f, y2 + width / 2f, color1)
        drawRect(x + width / 2f, y2 - width / 2f, x2 - width / 2f, y2 + width / 2f, color1)
    }

    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glLineWidth(width)

        drawWithTessellatorWorldRenderer {
            begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION)
            pos(x2.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y2.toDouble(), 0.0).endVertex()
            pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        }

        glColor(Color.WHITE)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
    }

    fun drawBorder(x: Int, y: Int, x2: Int, y2: Int, width: Number, color: Int) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glLineWidth(width.toFloat())

        drawWithTessellatorWorldRenderer {
            begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION)
            pos(x2.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y2.toDouble(), 0.0).endVertex()
            pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        }

        glColor(Color.WHITE)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun drawRoundedBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int, radius: Float) {
        renderRoundedBorder(x, y, x2, y2, color, width, radius)
    }

    /** rounded rect outline
     * @author Shoroa
     * @param x : X pos
     * @param y : Y pos
     * @param x1 : X2 pos
     * @param y1 : Y2 pos
     * @param radius : round of edges;
     * @param lineWidth : width of outline line;
     * @param colour : color;
     */
    fun drawRoundedOutline(x: Float, y: Float, x1: Float, y1: Float, radius: Float, lineWidth: Float, colour: Int) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(colour)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(lineWidth)
        glBegin(GL_LINE_LOOP)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius, y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glLineWidth(1f)
        color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun renderRoundedBorder(
        x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float, radius: Float, bottom: Boolean = true
    ) {
        val (alpha, red, green, blue) = ColorUtils.unpackARGBFloatValue(color)
        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(width)

        glColor4f(red, green, blue, alpha)

        val radiusD = min(radius.toDouble(), min(newX2 - newX1, newY2 - newY1) / 2.0)

        val corners = arrayOf(
            doubleArrayOf(newX2 - radiusD, newY2 - radiusD, 0.0),
            doubleArrayOf(newX2 - radiusD, newY1 + radiusD, 90.0),
            doubleArrayOf(newX1 + radiusD, newY1 + radiusD, 180.0),
            doubleArrayOf(newX1 + radiusD, newY2 - radiusD, 270.0)
        )

        drawWithTessellatorWorldRenderer {
            begin(if (bottom) GL_LINE_LOOP else GL_LINE_STRIP, DefaultVertexFormats.POSITION)

            for ((cx, cy, startAngle) in corners) {
                for (i in 0..90 step 10) {
                    val angle = Math.toRadians(startAngle + i)
                    val x = cx + radiusD * sin(angle)
                    val y = cy + radiusD * cos(angle)
                    pos(x, y, 0.0).endVertex()
                }
            }
        }

        glColor(Color.WHITE)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun drawRoundedBorderedWithoutBottom(
        x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float, radius: Float
    ) = renderRoundedBorder(x1, y1, x2, y2, color, width, radius, false)

    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float) {
        drawWithTessellatorWorldRenderer {
            begin(GL_QUADS, DefaultVertexFormats.POSITION)
            pos(x2.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y2.toDouble(), 0.0).endVertex()
            pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        }
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        drawWithTessellatorWorldRenderer {
            begin(GL_QUADS, DefaultVertexFormats.POSITION)
            pos(x2.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y2.toDouble(), 0.0).endVertex()
            pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        }
        glColor(Color.WHITE)

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun drawRect(x: Int, y: Int, x2: Int, y2: Int, color: Int) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)

        drawWithTessellatorWorldRenderer {
            begin(GL_QUADS, DefaultVertexFormats.POSITION)
            pos(x2.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y2.toDouble(), 0.0).endVertex()
            pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        }

        glColor(Color.WHITE)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float, alpha: Float) {
        drawRect(x - 3.5f, y - 3.5f, x2 + 3.5f, y2 + 3.5f, Color(0f, 0f, 0f, alpha).rgb)
        drawRect(x - 3f, y - 3f, x2 + 3f, y2 + 3f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x - 2.5f, y - 2.5f, x2 + 2.5f, y2 + 2.5f, Color(26f / 255f, 26f / 255f, 26f / 255f, alpha).rgb)
        drawRect(x - 0.5f, y - 0.5f, x2 + 0.5f, y2 + 0.5f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x, y, x2, y2, Color(18f / 255f, 18 / 255f, 18f / 255f, alpha).rgb)
    }

    /**
     * Like [.drawRect], but without setup
     */
    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glPushMatrix()
        glColor(color)

        drawWithTessellatorWorldRenderer {
            begin(GL_QUADS, DefaultVertexFormats.POSITION)
            pos(x2.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y2.toDouble(), 0.0).endVertex()
            pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        }
        glColor(Color.WHITE)
        glPopMatrix()
    }

    /**
     * Draws a rectangle with borders.
     *
     * @param x The x coordinate of the top-left corner.
     * @param y The y coordinate of the top-left corner.
     * @param x2 The x coordinate of the bottom-right corner.
     * @param y2 The y coordinate of the bottom-right corner.
     * @param borderWidth The width of the border.
     * @param borderColor The color of the border.
     * @param fillColor The color of the fill.
     */
    fun drawRectWithBorder(x: Float, y: Float, x2: Float, y2: Float, borderWidth: Float, borderColor: Int, fillColor: Int) {
        drawFilledRect(x, y, x2, y2, fillColor)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        glColor(borderColor)
        glLineWidth(borderWidth)
        glBegin(GL_LINES)
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun quickDrawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        quickDrawRect(x, y, x2, y2, color2)
        glColor(color1)
        glLineWidth(width)

        drawWithTessellatorWorldRenderer {
            begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION)
            pos(x2.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
            pos(x.toDouble(), y2.toDouble(), 0.0).endVertex()
            pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        }
    }

    fun drawLoadingCircle(x: Float, y: Float) {
        for (i in 0..3) {
            val rot = (System.nanoTime() / 5000000 * i % 360).toInt()
            drawCircle(x, y, (i * 10).toFloat(), rot - 180, rot)
        }
    }

    fun drawRoundWordRect(left: Float, top: Float, width: Float, height: Float, color: Int) {
        val f3 = (color shr 24 and 0xFF) / 255.0f
        val f4 = (color shr 16 and 0xFF) / 255.0f
        val f5 = (color shr 8 and 0xFF) / 255.0f
        val f6 = (color and 0xFF) / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f4, f5, f6, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), (top + height).toDouble(), 0.0).endVertex()
        worldrenderer.pos((left + width).toDouble(), (top + height).toDouble(), 0.0).endVertex()
        worldrenderer.pos((left + width).toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, edgeRadius: Float, color: Int, borderWidth: Float, borderColor: Int) {
        var edgeRadius = edgeRadius
        var color = color
        var borderColor = borderColor
        if (color == 16777215) {
            color = -65794
        }
        if (borderColor == 16777215) {
            borderColor = -65794
        }
        if (edgeRadius < 0.0f) {
            edgeRadius = 0.0f
        }
        if (edgeRadius > width / 2.0f) {
            edgeRadius = width / 2.0f
        }
        if (edgeRadius > height / 2.0f) {
            edgeRadius = height / 2.0f
        }
        drawRoundWordRect(x + edgeRadius, y + edgeRadius,
            width - edgeRadius * 2.0f,
            height - edgeRadius * 2.0f,
            color
        )
        drawRoundWordRect(x + edgeRadius, y, width - edgeRadius * 2.0f, edgeRadius, color)
        drawRoundWordRect(x + edgeRadius, y + height - edgeRadius, width - edgeRadius * 2.0f, edgeRadius, color)
        drawRoundWordRect(x, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0f, color)
        drawRoundWordRect(x + width - edgeRadius, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0f, color)
        enableRender2D()
        color(color)
        glBegin(6)
        var centerX = x + edgeRadius
        var centerY = y + edgeRadius
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 180) / (vertices * 4)
                glVertex2d(
                    centerX + sin(angleRadians) * edgeRadius,
                    centerY + cos(angleRadians) * edgeRadius
                )
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + width - edgeRadius
        centerY = y + edgeRadius
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 90) / (vertices * 4)
                glVertex2d(
                    centerX + sin(angleRadians) * edgeRadius,
                    centerY + cos(angleRadians) * edgeRadius
                )
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + edgeRadius
        centerY = y + height - edgeRadius
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 270) / (vertices * 4)
                glVertex2d(
                    centerX + sin(angleRadians) * edgeRadius,
                    centerY + cos(angleRadians) * edgeRadius
                )
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + width - edgeRadius
        centerY = y + height - edgeRadius
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * i / (vertices * 4)
                glVertex2d(
                    centerX + sin(angleRadians) * edgeRadius,
                    centerY + cos(angleRadians) * edgeRadius
                )
                ++i
            }
        }
        glEnd()
        color(borderColor)
        glLineWidth(borderWidth)
        glBegin(3)
        centerX = x + edgeRadius
        centerY = y + edgeRadius
        val vertices: Int
        var i: Int
        vertices = (min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt().also {
            i = it
        })
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 180) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
            --i
        }
        glVertex2d((x + edgeRadius).toDouble(), y.toDouble())
        glVertex2d((x + width - edgeRadius).toDouble(), y.toDouble())
        centerX = x + width - edgeRadius
        centerY = y + edgeRadius
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 90) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
            --i
        }
        glVertex2d((x + width).toDouble(), (y + edgeRadius).toDouble())
        glVertex2d((x + width).toDouble(), (y + height - edgeRadius).toDouble())
        centerX = x + width - edgeRadius
        centerY = y + height - edgeRadius
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * i / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
            --i
        }
        glVertex2d((x + width - edgeRadius).toDouble(), (y + height).toDouble())
        glVertex2d((x + edgeRadius).toDouble(), (y + height).toDouble())
        centerX = x + edgeRadius
        centerY = y + height - edgeRadius
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 270) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
            --i
        }
        glVertex2d(x.toDouble(), (y + height - edgeRadius).toDouble())
        glVertex2d(x.toDouble(), (y + edgeRadius).toDouble())
        glEnd()
        disableRender2D()
    }

    fun drawRoundedRect(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Int,
        radius: Float,
        cornersToRound: RoundedCorners = RoundedCorners.ALL
    ) {
        val (alpha, red, green, blue) = ColorUtils.unpackARGBFloatValue(color)

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius, cornersToRound)
    }

    fun drawRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float, color: Int) {
        drawRoundedRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true)
    }

    fun drawRoundedRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd

        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()

        if (popPush) glPushMatrix()

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }

        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)

        if (popPush) glPopMatrix()
    }

    fun drawRoundedBindRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z = 0f
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()

        if (popPush) glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (popPush) glPopMatrix()
    }

    fun drawRoundedBindRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int
    ) {
        drawRoundedBindRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true)
    }

    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()


        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }

    /**
     * Draw rounded rect.
     *
     * @param paramXStart the param x start
     * @param paramYStart the param y start
     * @param paramXEnd   the param x end
     * @param paramYEnd   the param y end
     * @param radius      the radius
     * @param color       the color
     */
    fun drawShadowRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int
    ) {
        drawShadowRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true)
    }

    /**
     * Draw rounded rect.
     *
     * @param paramXStart the param x start
     * @param paramYStart the param y start
     * @param paramXEnd   the param x end
     * @param paramYEnd   the param y end
     * @param radius      the radius
     * @param color       the color
     * @param popPush     the pop push
     */
    fun drawShadowRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()

        if (popPush) glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (popPush) glPopMatrix()
    }
    fun drawRoundedRect2(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Color,
        radius: Float,
        cornersToRound: RoundedCorners = RoundedCorners.ALL
    ) {
        val alpha = color.alpha / 255.0f
        val red = color.red / 255.0f
        val green = color.green / 255.0f
        val blue = color.blue / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius, cornersToRound)
    }

    fun customRounded(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        rTL: Float,
        rTR: Float,
        rBR: Float,
        rBL: Float,
        color: Int
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val xTL = (paramXStart + rTL).toDouble()
        val yTL = (paramYStart + rTL).toDouble()

        val xTR = (paramXEnd - rTR).toDouble()
        val yTR = (paramYStart + rTR).toDouble()

        val xBR = (paramXEnd - rBR).toDouble()
        val yBR = (paramYEnd - rBR).toDouble()

        val xBL = (paramXStart + rBL).toDouble()
        val yBL = (paramYEnd - rBL).toDouble()

        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(xBR + sin(i * degree) * rBR, yBR + cos(i * degree) * rBR)
                i += 0.25
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(xTR + sin(i * degree) * rTR, yTR + cos(i * degree) * rTR)
                i += 0.25
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(xTL + sin(i * degree) * rTL, yTL + cos(i * degree) * rTL)
                i += 0.25
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(xBL + sin(i * degree) * rBL, yBL + cos(i * degree) * rBL)
            i += 0.25
        }
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun drawRoundedRectInt(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        color: Int,
        radius: Float,
        cornersToRound: RoundedCorners = RoundedCorners.ALL
    ) {
        val (alpha, red, green, blue) = ColorUtils.unpackARGBFloatValue(color)

        val (newX1, newY1, newX2, newY2) = orderPoints(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius, cornersToRound)
    }

    enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    enum class RoundedCorners(val corners: Set<Corner>, val displayName: String) {
        NONE(emptySet(), "None"), TOP_LEFT_ONLY(
            setOf(Corner.TOP_LEFT),
            "Top-Left-Only"
        ),
        TOP_RIGHT_ONLY(setOf(Corner.TOP_RIGHT), "Top-Right-Only"), BOTTOM_LEFT_ONLY(
            setOf(Corner.BOTTOM_LEFT),
            "Bottom-Left-Only"
        ),
        BOTTOM_RIGHT_ONLY(setOf(Corner.BOTTOM_RIGHT), "Bottom-Right-Only"), TOP_ONLY(
            setOf(
                Corner.TOP_LEFT,
                Corner.TOP_RIGHT
            ), "Top-Only"
        ),
        BOTTOM_ONLY(setOf(Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT), "Bottom-Only"), LEFT_ONLY(
            setOf(
                Corner.TOP_LEFT,
                Corner.BOTTOM_LEFT
            ), "Left-Only"
        ),
        RIGHT_ONLY(setOf(Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT), "Right-Only"), ALL(
            setOf(
                Corner.TOP_LEFT,
                Corner.TOP_RIGHT,
                Corner.BOTTOM_LEFT,
                Corner.BOTTOM_RIGHT
            ), "All"
        )
    }

    private fun drawRoundedRectangle(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        radius: Float,
        cornersToRound: RoundedCorners = RoundedCorners.ALL
    ) {
        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        glColor4f(red, green, blue, alpha)

        val radiusD = min(radius.toDouble(), min(newX2 - newX1, newY2 - newY1) / 2.0)

        val corners = arrayOf(
            Corner.BOTTOM_RIGHT to doubleArrayOf(
                newX2 - radiusD, newY2 - radiusD, 0.0, newX2.toDouble(), newY2.toDouble()
            ), Corner.TOP_RIGHT to doubleArrayOf(
                newX2 - radiusD, newY1 + radiusD, 90.0, newX2.toDouble(), newY1.toDouble()
            ), Corner.TOP_LEFT to doubleArrayOf(
                newX1 + radiusD, newY1 + radiusD, 180.0, newX1.toDouble(), newY1.toDouble()
            ), Corner.BOTTOM_LEFT to doubleArrayOf(
                newX1 + radiusD, newY2 - radiusD, 270.0, newX1.toDouble(), newY2.toDouble()
            )
        )

        drawWithTessellatorWorldRenderer {
            begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION)

            for ((corner, directionData) in corners) {
                val (cx, cy, startAngle, ox, oy) = directionData

                if (corner in cornersToRound.corners) {
                    for (i in 0..90 step 10) {
                        val angle = Math.toRadians(startAngle + i)
                        val x = cx + radiusD * sin(angle)
                        val y = cy + radiusD * cos(angle)
                        pos(x, y, 0.0).endVertex()
                    }
                } else {
                    pos(ox, oy, 0.0).endVertex()
                }
            }
        }

        glColor(Color.WHITE)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    private fun orderPoints(x1: Float, y1: Float, x2: Float, y2: Float): FloatArray {
        val newX1 = min(x1, x2)
        val newY1 = min(y1, y2)
        val newX2 = max(x1, x2)
        val newY2 = max(y1, y2)
        return floatArrayOf(newX1, newY1, newX2, newY2)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor(Color.WHITE)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(2f)
        glBegin(GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            val rad = i.toRadians()
            glVertex2f(
                x + cos(rad) * (radius * 1.001f), y + sin(rad) * (radius * 1.001f)
            )
            i -= 360 / 90f
        }
        glEnd()
        glDisable(GL_LINE_SMOOTH)
        enableTexture2D()
        disableBlend()
    }

    fun drawFilledCircle(xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float
        glPushAttrib(GL_ENABLE_BIT)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)
        for (i in 0 until sections) {
            x = (radius * sin(i * dAngle)).toFloat()
            y = (radius * cos(i * dAngle)).toFloat()
            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            glVertex2f(xx + x, yy + y)
        }
        resetColor()
        glEnd()
        glPopAttrib()
    }

    fun drawHead(
        skin: ResourceLocation?,
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float,
        color: Color
    ) {
        glPushMatrix()
        val texture: ResourceLocation = skin ?: mc.thePlayer.locationSkin

        glColor(color)
        mc.textureManager.bindTexture(texture)
        drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight)
        glColor(Color.WHITE)
        glPopMatrix()
    }

    fun drawImage(
        image: ResourceLocation?,
        x: Number,
        y: Number,
        width: Int,
        height: Int,
        color: Color = Color.WHITE,
        radius: Float = 0f
    ) {
        glPushMatrix()
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor(color)

        mc.textureManager.bindTexture(image)

        if (radius > 0) {
            val x1 = x.toFloat()
            val y1 = y.toFloat()
            val x2 = x1 + width
            val y2 = y1 + height
            val radiusD = min(radius.toDouble(), min(width, height) / 2.0)

            drawWithTessellatorWorldRenderer {
                begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX)

                val corners = arrayOf(
                    doubleArrayOf(x2 - radiusD, y2 - radiusD, 0.0),
                    doubleArrayOf(x2 - radiusD, y1 + radiusD, 90.0),
                    doubleArrayOf(x1 + radiusD, y1 + radiusD, 180.0),
                    doubleArrayOf(x1 + radiusD, y2 - radiusD, 270.0),
                )

                for (corner in corners) {
                    val (cx, cy, startAngle) = corner
                    for (i in 0..90 step 10) {
                        val angle = Math.toRadians(startAngle + i)
                        val px = cx + radiusD * sin(angle)
                        val py = cy + radiusD * cos(angle)
                        val texX = (px - x1) / width
                        val texY = (py - y1) / height
                        pos(px, py, 0.0).tex(texX, texY).endVertex()
                    }
                }
            }
        } else {
            drawModalRectWithCustomSizedTexture(
                x.toFloat(), y.toFloat(), 0f, 0f, width.toFloat(), height.toFloat(), width.toFloat(), height.toFloat()
            )
        }
        glColor(Color.WHITE)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glPopMatrix()
    }

    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    fun drawModalRectWithCustomSizedTexture(
        x: Float, y: Float, u: Float, v: Float, width: Float, height: Float, textureWidth: Float, textureHeight: Float
    ) = drawWithTessellatorWorldRenderer {
        val f = 1f / textureWidth
        val f1 = 1f / textureHeight
        begin(7, DefaultVertexFormats.POSITION_TEX)
        pos(x.toDouble(), (y + height).toDouble(), 0.0).tex((u * f).toDouble(), ((v + height) * f1).toDouble())
            .endVertex()
        pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(
            ((u + width) * f).toDouble(), ((v + height) * f1).toDouble()
        ).endVertex()
        pos((x + width).toDouble(), y.toDouble(), 0.0).tex(((u + width) * f).toDouble(), (v * f1).toDouble())
            .endVertex()
        pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
    }

    data class ColorValueCache(val lastHue: Float, val cachedTextureID: Int)

    private val colorValueCache: MutableMap<ColorValue, MutableMap<Int, ColorValueCache>> = mutableMapOf()

    fun ColorValue.updateTextureCache(
        id: Int,
        hue: Float,
        width: Int,
        height: Int,
        generateImage: (BufferedImage, Graphics2D) -> Unit,
        drawAt: (Int) -> Unit
    ) {
        val cached = colorValueCache[this]?.get(id)
        val lastHue = cached?.lastHue

        if (lastHue == null || lastHue != hue) {
            val image = createRGBImageDrawing(width, height) { img, graphics -> generateImage(img, graphics) }
            val texture = convertImageToTexture(image)
            colorValueCache.getOrPut(this, ::mutableMapOf)[id] = ColorValueCache(hue, texture)
        }

        colorValueCache[this]?.get(id)?.cachedTextureID?.let(drawAt)
    }

    private fun createRGBImageDrawing(width: Int, height: Int, f: (BufferedImage, Graphics2D) -> Unit): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        f(image, g)

        g.dispose()
        return image
    }

    private fun convertImageToTexture(image: BufferedImage): Int {
        val width = image.width
        val height = image.height

        val pixels = IntArray(width * height)

        image.getRGB(0, 0, width, height, pixels, 0, width)

        val buffer = ByteBuffer.allocateDirect(width * height * 4)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            buffer.put(((pixel shr 16) and 0xFF).toByte())
            buffer.put(((pixel shr 8) and 0xFF).toByte())
            buffer.put(((pixel shr 0) and 0xFF).toByte())
            buffer.put(((pixel shr 24) and 0xFF).toByte())
        }

        buffer.flipSafely()

        val textureID = glGenTextures()

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glBindTexture(GL_TEXTURE_2D, textureID)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

        glPopMatrix()
        glPopAttrib()

        return textureID
    }

    fun drawTexture(textureID: Int, x: Int, y: Int, width: Int, height: Int) {
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glEnable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glDisable(GL_CULL_FACE)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0f)

        glBindTexture(GL_TEXTURE_2D, textureID)

        glTranslatef(x.toFloat(), y.toFloat(), 0.0f)

        glBegin(GL_QUADS)
        glTexCoord2f(0.0f, 0.0f); glVertex2f(0.0f, 0.0f) // Bottom-left corner
        glTexCoord2f(1.0f, 0.0f); glVertex2f(width.toFloat(), 0.0f) // Bottom-right corner
        glTexCoord2f(1.0f, 1.0f); glVertex2f(width.toFloat(), height.toFloat()) // Top-right corner
        glTexCoord2f(0.0f, 1.0f); glVertex2f(0.0f, height.toFloat()) // Top-left corner
        glEnd()

        glDisable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_CULL_FACE)

        glPopMatrix()
        glPopAttrib()
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height.
     */
    fun drawTexturedModalRect(
        x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int, zLevel: Float
    ) = drawWithTessellatorWorldRenderer {
        val f = 0.00390625f
        val f1 = 0.00390625f
        begin(7, DefaultVertexFormats.POSITION_TEX)
        pos(x.toDouble(), (y + height).toDouble(), zLevel.toDouble()).tex(
            (textureX.toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble()
        ).endVertex()
        pos(
            (x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble()
        ).tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble())
            .endVertex()
        pos((x + width).toDouble(), y.toDouble(), zLevel.toDouble()).tex(
            ((textureX + width).toFloat() * f).toDouble(), (textureY.toFloat() * f1).toDouble()
        ).endVertex()
        pos(x.toDouble(), y.toDouble(), zLevel.toDouble()).tex(
            (textureX.toFloat() * f).toDouble(), (textureY.toFloat() * f1).toDouble()
        ).endVertex()
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) =
        glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f)

    /**
     * Gl color.
     *
     * @param hex the hex
     */
    @JvmStatic
    fun glHexColor(hex: Int) {
        val alpha = (hex shr 24 and 0xFF) / 255f
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f

        color(red, green, blue, alpha)
    }

    fun glColor(color: Color) = glColor(color.red, color.green, color.blue, color.alpha)

    fun glStateManagerColor(color: Color) =
        color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)


    fun glColor(hex: Int) =
        glColor(hex shr 16 and 0xFF, hex shr 8 and 0xFF, hex and 0xFF, hex shr 24 and 0xFF)

    fun draw2D(entity: EntityLivingBase, posX: Double, posY: Double, posZ: Double, color: Int, backgroundColor: Int) {
        glPushMatrix()
        glTranslated(posX, posY, posZ)
        glRotated(-mc.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
        glScaled(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        glTranslated(0.0, 21 + -(entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 12, 0.0)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun draw2D(blockPos: BlockPos, color: Int, backgroundColor: Int) {
        val renderManager = mc.renderManager
        val (x, y, z) = blockPos.center.offset(EnumFacing.DOWN, 0.5) - renderManager.renderPos
        glPushMatrix()
        glTranslated(x, y, z)
        glRotated(-renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
        glScaled(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        glTranslated(0.0, 9.0, 0.0)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun renderNameTag(string: String, x: Double, y: Double, z: Double) {
        val renderManager = mc.renderManager
        val (x1, y1, z1) = Vec3(x, y, z) - renderManager.renderPos

        glPushMatrix()
        glTranslated(x1, y1, z1)
        glNormal3f(0f, 1f, 0f)
        glRotatef(-renderManager.playerViewY, 0f, 1f, 0f)
        glRotatef(renderManager.playerViewX, 1f, 0f, 0f)
        glScalef(-0.05f, -0.05f, 0.05f)
        setGlCap(GL_LIGHTING, false)
        setGlCap(GL_DEPTH_TEST, false)
        setGlCap(GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        val width = Fonts.fontSemibold35.getStringWidth(string) / 2
        drawRect(-width - 1, -1, width + 1, Fonts.fontSemibold35.FONT_HEIGHT, Int.MIN_VALUE)
        Fonts.fontSemibold35.drawString(string, -width.toFloat(), 1.5f, Color.WHITE.rgb, true)
        resetCaps()
        resetColor()
        glPopMatrix()
    }

    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2d(x, y)
        glVertex2d(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    fun makeScissorBox(x: Float, y: Float, x2: Float, y2: Float) {
        val scaledResolution = ScaledResolution(mc)
        val factor = scaledResolution.scaleFactor
        glScissor(
            (x * factor).toInt(),
            ((scaledResolution.scaledHeight - y2) * factor).toInt(),
            ((x2 - x) * factor).toInt(),
            ((y2 - y) * factor).toInt()
        )
    }

    /**
     * GL CAP MANAGER
     *
     *
     * TODO: Remove gl cap manager and replace by something better
     */

    fun resetCaps() = glCapMap.forEach { (cap, state) -> setGlState(cap, state) }

    fun enableGlCap(cap: Int) = setGlCap(cap, true)

    fun enableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, true)
    }

    fun disableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, false)
    }

    fun setGlCap(cap: Int, state: Boolean) {
        glCapMap[cap] = glGetBoolean(cap)
        setGlState(cap, state)
    }

    fun setGlState(cap: Int, state: Boolean) = if (state) glEnable(cap) else glDisable(cap)

    fun drawScaledCustomSizeModalRect(
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) = drawWithTessellatorWorldRenderer {
        val f = 1f / tileWidth
        val f1 = 1f / tileHeight
        begin(7, DefaultVertexFormats.POSITION_TEX)
        pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(
            (u * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()
        ).endVertex()
        pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(
            ((u + uWidth.toFloat()) * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()
        ).endVertex()
        pos((x + width).toDouble(), y.toDouble(), 0.0).tex(((u + uWidth.toFloat()) * f).toDouble(), (v * f1).toDouble())
            .endVertex()
        pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
    }

    /**
     * Draws a bloom effect with a specified blur radius and color.
     *
     * This method creates a blurred rectangle that simulates a glow effect
     * around the specified area.
     *
     * @param x          The x-coordinate of the rectangle's top-left corner.
     * @param y          The y-coordinate of the rectangle's top-left corner.
     * @param width      The width of the rectangle.
     * @param height     The height of the rectangle.
     * @param blurRadius The radius of the blur applied to the edges.
     * @param color      The [java.awt.Color] used for the bloom effect.
     */
    fun drawBloom(x: Int, y: Int, width: Int, height: Int, blurRadius: Int, color: Color) {
        var x = x
        var y = y
        var width = width
        var height = height
        Gui.drawRect(0, 0, 0, 0, 0)
        pushAttrib()
        pushMatrix()
        alphaFunc(516, 0.01f)
        height = max(0.0, height.toDouble()).toInt()
        width = max(0.0, width.toDouble()).toInt()
        width += blurRadius * 2
        height += blurRadius * 2
        x -= blurRadius
        y -= blurRadius
        val _X = x - 0.25f
        val _Y = y + 0.25f
        val identifier = width * height + width + color.hashCode() * blurRadius + blurRadius
        glEnable(3553)
        glDisable(2884)
        glEnable(3008)
        glEnable(3042)
        if (shadowCache.containsKey(identifier)) {
            val texId: Int = shadowCache.get(identifier)!!
            bindTexture(texId)
        } else {
            val original = BufferedImage(width, height, 2)
            val g = original.graphics
            g.color = color
            g.fillRect(blurRadius, blurRadius, width - blurRadius * 2, height - blurRadius * 2)
            g.dispose()
            val op = GaussianFilter(blurRadius.toFloat())
            val blurred = op.filter(original, null)
            val texId = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), blurred, true, false)
            shadowCache[identifier] = texId
        }
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glBegin(7)
        glTexCoord2f(0.0f, 0.0f)
        glVertex2d(_X.toDouble(), _Y.toDouble())
        glTexCoord2f(0.0f, 1.0f)
        glVertex2d(_X.toDouble(), (_Y + height).toDouble())
        glTexCoord2f(1.0f, 1.0f)
        glVertex2d((_X + width).toDouble(), (_Y + height).toDouble())
        glTexCoord2f(1.0f, 0.0f)
        glVertex2d((_X + width).toDouble(), _Y.toDouble())
        glEnd()
        glDisable(3553)
        glEnable(2884)
        glDisable(3008)
        glDisable(3042)
        popAttrib()
        popMatrix()
    }

    /**
     * Fast rounded rect.
     *
     * @param paramXStart the param x start
     * @param paramYStart the param y start
     * @param paramXEnd   the param x end
     * @param paramYEnd   the param y end
     * @param radius      the radius
     */
    fun fastRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()

        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()
        glDisable(GL_LINE_SMOOTH)
    }

    /**
     * Draws a rounded rectangle with equal rounded corners on all sides.
     *
     * @param x         The x-coordinate of the top-left corner.
     * @param y         The y-coordinate of the top-left corner.
     * @param width     The width of the rounded rectangle.
     * @param height    The height of the rounded rectangle.
     * @param radius    The radius of the rounded corners.
     * @param color     The color of the rounded rectangle.
     */
    @JvmStatic
    fun drawCustomShapeWithRadius(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        drawCustomShapeWithRadiusRetangle(x, y, width, height, radius, color,
            leftTop = true,
            leftBottom = true,
            rightBottom = true,
            rightTop = true
        )
    }

    fun color(color: Int, alpha: Float) {
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        color(r, g, b, alpha)
    }

    fun color(color: Int) {
        color(color, (color shr 24 and 0xFF) / 255.0f)
    }

    /**
     * Draws a rounded rectangle with customizability for rounded corners.
     *
     * @param x             The x-coordinate of the top-left corner.
     * @param y             The y-coordinate of the top-left corner.
     * @param width         The width of the rounded rectangle.
     * @param height        The height of the rounded rectangle.
     * @param radius        The radius of the rounded corners.
     * @param leftTop       If true, the top-left corner will be rounded.
     * @param leftBottom    If true, the bottom-left corner will be rounded.
     * @param rightBottom   If true, the bottom-right corner will be rounded.
     * @param rightTop      If true, the top-right corner will be rounded.
     */
    fun drawCustomShapeWithRadiusRetangle(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        c: Color,
        leftTop: Boolean,
        leftBottom: Boolean,
        rightBottom: Boolean,
        rightTop: Boolean
    ) {
        var x = x
        var y = y
        var width = width
        var height = height
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        enableBlend()
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        width *= 2.0.toFloat()
        height *= 2.0.toFloat()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_LINE_SMOOTH)
        ColorUtils.clearColor()
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_POLYGON)
        ColorUtils.setColor(c.rgb)
        var i: Int

        if (leftTop) {
            i = 0
            while (i <= 90) {
                glVertex2d(
                    x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                    y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
                )
                i += 3
            }
        } else glVertex2d(x.toDouble(), y.toDouble())

        if (leftBottom) {
            i = 90
            while (i <= 180) {
                glVertex2d(
                    x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                    y + height - radius + cos(i * Math.PI / 180.0) * radius * -1.0
                )
                i += 3
            }
        } else glVertex2d(x.toDouble(), (y + height).toDouble())

        if (rightBottom) {
            i = 0
            while (i <= 90) {
                glVertex2d(
                    x + width - radius + sin(i * Math.PI / 180.0) * radius,
                    y + height - radius + cos(i * Math.PI / 180.0) * radius
                )
                i += 3
            }
        } else glVertex2d((x + width).toDouble(), (y + height).toDouble())

        if (rightTop) {
            i = 90
            while (i <= 180) {
                glVertex2d(
                    x + width - radius + sin(i * Math.PI / 180.0) * radius,
                    y + radius + cos(i * Math.PI / 180.0) * radius
                )
                i += 3
            }
        } else glVertex2d((x + width).toDouble(), y.toDouble())

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_LINE_SMOOTH)
        disableBlend()
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        ColorUtils.clearColor()
    }

    /**
     * Draws a rounded outline with specified parameters.
     *
     * @param x      The x-coordinate of the top-left corner.
     * @param y      The y-coordinate of the top-left corner.
     * @param x2     The x-coordinate of the bottom-right corner.
     * @param y2     The y-coordinate of the bottom-right corner.
     * @param radius The radius of the rounded corners.
     * @param width  The width of the outline.
     * @param color  The color of the outline in RGBA format.
     */
    @JvmStatic
    fun drawRoundOutline(x: Int, y: Int, x2: Int, y2: Int, radius: Float, width: Float, color: Int) {
        val f1 = (color shr 24 and 0xFF) / 255.0f
        val f2 = (color shr 16 and 0xFF) / 255.0f
        val f3 = (color shr 8 and 0xFF) / 255.0f
        val f4 = (color and 0xFF) / 255.0f
        glColor4f(f2, f3, f4, f1)
        drawRoundedShapeOutline(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), radius, width)
    }

    /**
     * Draws an outlined shape with rounded corners using specified parameters.
     *
     * @param x      The starting x-coordinate of the outline.
     * @param y      The starting y-coordinate of the outline.
     * @param x2     The ending x-coordinate of the outline.
     * @param y2     The ending y-coordinate of the outline.
     * @param radius The radius of the rounded corners.
     * @param width  The width of the outline.
     */
    fun drawRoundedShapeOutline(x: Float, y: Float, x2: Float, y2: Float, radius: Float, width: Float) {
        val segments = 18 // Number of segments to approximate the rounded corners
        90 / segments

        // Disable unnecessary features and enable needed ones
        disableTexture2D()
        enableBlend()
        disableCull()
        enableColorMaterial()
        blendFunc(770, 771)
        tryBlendFuncSeparate(770, 771, 1, 0)

        // Set line width if it's not the default value
        if (width != 1.0f) {
            glLineWidth(width)
        }

        // Draw the straight edges of the outline
        glBegin(GL_LINES)

        // Bottom edge
        glVertex2f(x + radius, y)
        glVertex2f(x2 - radius, y)

        // Right edge
        glVertex2f(x2, y + radius)
        glVertex2f(x2, y2 - radius)

        // Top edge
        glVertex2f(x2 - radius, y2)
        glVertex2f(x + radius, y2)

        // Left edge
        glVertex2f(x, y2 - radius)
        glVertex2f(x, y + radius)

        glEnd()

        // Draw the rounded corners
        drawRoundedCorner(x + radius, y + radius, radius, 270f, 360f, segments)
        drawRoundedCorner(x2 - radius, y + radius, radius, 0f, 90f, segments)
        drawRoundedCorner(x2 - radius, y2 - radius, radius, 90f, 180f, segments)
        drawRoundedCorner(x + radius, y2 - radius, radius, 180f, 270f, segments)

        // Reset line width if changed
        if (width != 1.0f) {
            glLineWidth(1.0f)
        }

        // Restore OpenGL state
        enableCull()
        disableBlend()
        disableColorMaterial()
        enableTexture2D()
    }

    /**
     * Draws a rounded corner with the given parameters.
     *
     * @param cx      Center x-coordinate of the corner
     * @param cy      Center y-coordinate of the corner
     * @param radius  Radius of the corner
     * @param start   Starting angle (degrees) of the arc
     * @param end     Ending angle (degrees) of the arc
     * @param segments Number of segments to approximate the arc
     */
    private fun drawRoundedCorner(cx: Float, cy: Float, radius: Float, start: Float, end: Float, segments: Int) {
        val angleStep = (end - start) / segments
        glBegin(GL_LINE_STRIP)

        for (i in 0..segments) {
            val angle = Math.toRadians((start + i * angleStep).toDouble()).toFloat()
            val x = cx + radius * cos(angle.toDouble()).toFloat()
            val y = cy + radius * sin(angle.toDouble()).toFloat()
            glVertex2f(x, y)
        }

        glEnd()
    }

    /**
     * Draws a rectangle with rounded corners at the specified coordinates and dimensions.
     *
     * @param x      The x-coordinate of the top-left corner of the rectangle.
     * @param y      The y-coordinate of the top-left corner of the rectangle.
     * @param x1     The x-coordinate of the bottom-right corner of the rectangle.
     * @param y1     The y-coordinate of the bottom-right corner of the rectangle.
     * @param radius The radius of the rounded corners.
     * @param color  The color of the rectangle in integer representation (e.g., 0xFF0000 for red).
     */
    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        glEnable(GL_BLEND) // Enable blending for smooth alpha transitions
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) // Set blending function for transparency
        glDisable(GL_TEXTURE_2D) // Disable textures for pure color rendering
        val hasCull = glIsEnabled(GL_CULL_FACE) // Check if face culling is enabled
        glDisable(GL_CULL_FACE) // Disable face culling for full visibility of rounded corners

        glColor(color) // Set the color of the rectangle

        // Draw the rounded corner rectangle
        drawRoundedCornerRectWithOpenGL(x, y, x1, y1, radius)

        glEnable(GL_TEXTURE_2D) // Re-enable textures for subsequent rendering
        glDisable(GL_BLEND) // Disable blending to restore default rendering
        setGlState(GL_CULL_FACE, hasCull) // Restore the state of face culling
    }

    /**
     * Draws a rectangle with rounded corners using OpenGL.
     *
     * @param x      The x-coordinate of the top-left corner of the rectangle.
     * @param y      The y-coordinate of the top-left corner of the rectangle.
     * @param x1     The x-coordinate of the bottom-right corner of the rectangle.
     * @param y1     The y-coordinate of the bottom-right corner of the rectangle.
     * @param radius The radius of the rounded corners.
     */
    fun drawRoundedCornerRectWithOpenGL(x: Float, y: Float, x1: Float, y1: Float, radius: Float) {
        glBegin(GL_POLYGON) // Begin drawing a filled polygon (to create rounded corners)

        // Calculate the actual radius to use (limited by rectangle dimensions)
        val xRadius = min((x1 - x) * 0.5, radius.toDouble()).toFloat()
        val yRadius = min((y1 - y) * 0.5, radius.toDouble()).toFloat()

        // Draw each rounded corner using quickPolygonCircle method
        quickPolygonCircle(x + xRadius, y + yRadius, xRadius, yRadius, 180, 270) // Top-left corner
        quickPolygonCircle(x1 - xRadius, y + yRadius, xRadius, yRadius, 90, 180) // Top-right corner
        quickPolygonCircle(x1 - xRadius, y1 - yRadius, xRadius, yRadius, 0, 90) // Bottom-right corner
        quickPolygonCircle(x + xRadius, y1 - yRadius, xRadius, yRadius, 270, 360) // Bottom-left corner

        glEnd() // End drawing the polygon
    }

    /**
     * Draws a portion of a circle using OpenGL, approximating it with a polygon.
     *
     * @param x       The x-coordinate of the center of the circle.
     * @param y       The y-coordinate of the center of the circle.
     * @param xRadius The radius of the circle along the x-axis.
     * @param yRadius The radius of the circle along the y-axis.
     * @param start   The starting angle of the arc in degrees (0 degrees is to the right, increasing counter-clockwise).
     * @param end     The ending angle of the arc in degrees.
     */
    private fun quickPolygonCircle(x: Float, y: Float, xRadius: Float, yRadius: Float, start: Int, end: Int) {
        var i = end
        while (i >= start) {
            glVertex2d(x + sin(Math.toRadians(i.toDouble())) * xRadius, y + cos(Math.toRadians(i.toDouble())) * yRadius)
            i -= 4
        }
    }

    /**
     * Draws a shadow effect around a rectangular area using textured rectangles.
     *
     * @param x      The x-coordinate of the top-left corner of the rectangular area.
     * @param y      The y-coordinate of the top-left corner of the rectangular area.
     * @param width  The width of the rectangular area.
     * @param height The height of the rectangular area.
     */
    fun drawShadow(x: Float, y: Float, width: Float, height: Float) {
        drawTexturedRect(x - 9, y - 9, 9F, 9F, "paneltopleft")
        drawTexturedRect(x - 9, y + height, 9F, 9F, "panelbottomleft")
        drawTexturedRect(x + width, y + height, 9F, 9F, "panelbottomright")
        drawTexturedRect(x + width, y - 9, 9F, 9F, "paneltopright")
        drawTexturedRect(x - 9, y, 9F, height, "panelleft")
        drawTexturedRect(x + width, y, 9F, height, "panelright")
        drawTexturedRect(x, y - 9, width, 9F, "paneltop")
        drawTexturedRect(x, y + height, width, 9F, "panelbottom")
    }

    /**
     * Draw filled circle.
     *
     * @param xx     the xx
     * @param yy     the yy
     * @param radius the radius
     * @param color  the color
     */
    fun drawFilledForCircle(xx: Float, yy: Float, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float

        glPushAttrib(GL_ENABLE_BIT)

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)

        for (i in 0 until sections) {
            x = (radius * sin((i * dAngle))).toFloat()
            y = (radius * cos((i * dAngle))).toFloat()

            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            glVertex2f(xx + x, yy + y)
        }

        color(0f, 0f, 0f)

        glEnd()

        glPopAttrib()
    }

    /**
     * Draw gradient sideways.
     *
     * @param left   the left
     * @param top    the top
     * @param right  the right
     * @param bottom the bottom
     * @param col1   the col 1
     * @param col2   the col 2
     */
    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 0xFF) / 255.0f
        val f2 = (col1 shr 16 and 0xFF) / 255.0f
        val f3 = (col1 shr 8 and 0xFF) / 255.0f
        val f4 = (col1 and 0xFF) / 255.0f
        val f5 = (col2 shr 24 and 0xFF) / 255.0f
        val f6 = (col2 shr 16 and 0xFF) / 255.0f
        val f7 = (col2 shr 8 and 0xFF) / 255.0f
        val f8 = (col2 and 0xFF) / 255.0f
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glEnable(2848)
        glShadeModel(7425)
        glPushMatrix()
        glBegin(7)
        glColor4f(f2, f3, f4, f)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        glColor4f(f6, f7, f8, f5)
        glVertex2d(right, bottom)
        glVertex2d(right, top)
        glEnd()
        glPopMatrix()
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        glShadeModel(7424)
    }


    fun drawGradientSI(left: Float, top: Float, right: Float, bottom: Float, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 255) / 255.0f
        val f1 = (startColor shr 16 and 255) / 255.0f
        val f2 = (startColor shr 8 and 255) / 255.0f
        val f3 = (startColor and 255) / 255.0f
        val f4 = (endColor shr 24 and 255) / 255.0f
        val f5 = (endColor shr 16 and 255) / 255.0f
        val f6 = (endColor shr 8 and 255) / 255.0f
        val f7 = (endColor and 255) / 255.0f
        disableTexture2D()
        enableBlend()
        disableAlpha()
        tryBlendFuncSeparate(770, 771, 1, 0)
        shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).color(f5, f6, f7, f4).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).color(f5, f6, f7, f4).endVertex()
        Tessellator.getInstance().draw()
        shadeModel(7424)
        disableBlend()
        enableAlpha()
        enableTexture2D()
    }
    /**
     * Calculates a color at a given offset between two colors using linear interpolation (gradient).
     *
     * @param color1 The starting color of the gradient.
     * @param color2 The ending color of the gradient.
     * @param offset The offset value between 0.0 and 1.0, where 0.0 represents color1 and 1.0 represents color2.
     * Values outside this range are wrapped around (e.g., offset 1.5 becomes 0.5).
     * @return The interpolated color at the given offset.
     */
    fun getGradientOffset(color1: Color, color2: Color, offset: Double): Color {
        var offset = offset
        val redPart: Int

        // Wrap offset if it's greater than 1.0
        if (offset > 1.0) {
            val fractionalPart = offset % 1.0
            val integerPart = offset.toInt()
            offset = if (integerPart % 2 == 0) fractionalPart else 1.0 - fractionalPart
        }

        // Calculate inverse percentage
        val inverse_percent = 1.0 - offset

        // Interpolate RGB components
        redPart = (color1.red.toDouble() * inverse_percent + color2.red.toDouble() * offset).toInt()
        val greenPart = (color1.green.toDouble() * inverse_percent + color2.green.toDouble() * offset).toInt()
        val bluePart = (color1.blue.toDouble() * inverse_percent + color2.blue.toDouble() * offset).toInt()

        // Return the interpolated color
        return Color(redPart, greenPart, bluePart)
    }

    @JvmStatic
    fun renderOne(lineWidth: Float) {
        checkSetupFBO()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(lineWidth)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(0xF)
        glStencilFunc(GL_NEVER, 1, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    @JvmStatic
    fun renderTwo() {
        glStencilFunc(GL_NEVER, 0, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    }

    @JvmStatic
    fun renderThree() {
        glStencilFunc(GL_EQUAL, 1, 0xF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    @JvmStatic
    fun renderFour(color: Color) {
        setColor(color)
        glDepthMask(false)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_POLYGON_OFFSET_LINE)
        glPolygonOffset(1f, -2000000f)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f)
    }

    @JvmStatic
    fun renderFive() {
        glPolygonOffset(1f, 2000000f)
        glDisable(GL_POLYGON_OFFSET_LINE)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glEnable(GL_BLEND)
        glEnable(GL_LIGHTING)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_ALPHA_TEST)
        glPopAttrib()
    }

    @JvmStatic
    fun setColor(color: Color) {
        glColor4d(
            (color.red / 255f).toDouble(),
            (color.green / 255f).toDouble(),
            (color.blue / 255f).toDouble(),
            (color.alpha / 255f).toDouble()
        )
    }

    /**
     * Draw gradient rect.
     *
     * @param left       the left
     * @param top        the top
     * @param right      the right
     * @param bottom     the bottom
     * @param startColor the start color
     * @param endColor   the end color
     */
    fun drawGradientRect(
        left: Number, top: Number, right: Number, bottom: Number, startColor: Int, endColor: Int, zLevel: Float
    ) {
        val a1 = (startColor shr 24 and 255) / 255f
        val r1 = (startColor shr 16 and 255) / 255f
        val g1 = (startColor shr 8 and 255) / 255f
        val b1 = (startColor and 255) / 255f
        val a2 = (endColor shr 24 and 255) / 255f
        val r2 = (endColor shr 16 and 255) / 255f
        val g2 = (endColor shr 8 and 255) / 255f
        val b2 = (endColor and 255) / 255f

        pushMatrix()
        disableTexture2D()
        enableBlend()
        disableAlpha()
        tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        shadeModel(GL_SMOOTH)

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.worldRenderer

        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        buffer.pos(right.toDouble(), top.toDouble(), zLevel.toDouble()).color(r2, g2, b2, a2).endVertex()
        buffer.pos(left.toDouble(), top.toDouble(), zLevel.toDouble()).color(r1, g1, b1, a1).endVertex()
        buffer.pos(left.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(r1, g1, b1, a1).endVertex()
        buffer.pos(right.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(r2, g2, b2, a2).endVertex()
        tessellator.draw()

        shadeModel(GL_FLAT)
        disableBlend()
        enableAlpha()
        enableTexture2D()
        popMatrix()
    }

    //TAHOMA
    private fun drawExhiOutlined(text: String, x: Float, y: Float, borderColor: Int, mainColor: Int): Float {
        Fonts.fontSmall.drawString(text, x, y - 0.35.toFloat(), borderColor)
        Fonts.fontSmall.drawString(text, x, y + 0.35.toFloat(), borderColor)
        Fonts.fontSmall.drawString(text, x - 0.35.toFloat(), y, borderColor)
        Fonts.fontSmall.drawString(text, x + 0.35.toFloat(), y, borderColor)
        Fonts.fontSmall.drawString(text, x, y, mainColor)
        return x + Fonts.fontSmall.getStringWidth(text) - 2f
    }

    private fun getBorderColor(level: Int): Int {
        if (level == 2) return 0x7055FF55
        if (level == 3) return 0x7000AAAA
        if (level == 4) return 0x70AA0000
        if (level >= 5) return 0x70FFAA00
        return 0x70FFFFFF
    }

    fun yzyTexture(
        x: Double,
        y: Double,
        u: Float,
        v: Float,
        width: Double,
        height: Double,
        textureWidth: Float,
        textureHeight: Float,
        color: Color
    ) {
        val valueWidth = 1.0f / textureWidth
        val valueHeight = 1.0f / textureHeight
        val tessellator = Tessellator.getInstance()
        val renderer = tessellator.worldRenderer

        color(
            color.red / 255.0f,
            color.green / 255.0f,
            color.blue / 255.0f,
            color.alpha / 255.0f
        )

        renderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        renderer.pos(x, y + height, 0.0).tex((u * valueWidth).toDouble(),
            ((v + height.toFloat()) * valueHeight).toDouble()
        ).endVertex()
        renderer.pos(x + width, y + height, 0.0).tex(((u + width.toFloat()) * valueWidth).toDouble(),
            ((v + height.toFloat()) * valueHeight).toDouble()
        ).endVertex()
        renderer.pos(x + width, y, 0.0).tex(((u + width.toFloat()) * valueWidth).toDouble(),
            (v * valueHeight).toDouble()
        ).endVertex()
        renderer.pos(x, y, 0.0).tex((u * valueWidth).toDouble(), (v * valueHeight).toDouble()).endVertex()

        tessellator.draw()

        color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun yzyRectangle(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color
    ) {
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)

        val renderer = Tessellator.getInstance().worldRenderer

        color(
            color.red / 255.0f,
            color.green / 255.0f,
            color.blue / 255.0f,
            color.alpha / 255.0f
        )

        renderer.begin(7, DefaultVertexFormats.POSITION)
        renderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).endVertex()
        renderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).endVertex()
        renderer.pos((x + width).toDouble(), y.toDouble(), 0.0).endVertex()
        renderer.pos(x.toDouble(), y.toDouble(), 0.0).endVertex()

        Tessellator.getInstance().draw()

        enableTexture2D()
        disableBlend()
        bindTexture(0)
        color(1f, 1f, 1f, 1f)
    }


    fun drawExhiEnchants(stack: ItemStack, x: Float, y: Float) {
        var y = y
        RenderHelper.disableStandardItemLighting()
        disableDepth()
        disableBlend()
        resetColor()
        val darkBorder = -0x1000000
        if (stack.item is ItemArmor) {
            val prot = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            val thorn = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)
            if (prot > 0) {
                drawExhiOutlined(
                    prot.toString() + "",
                    drawExhiOutlined("P", x, y, darkBorder, -1),
                    y,
                    getBorderColor(prot),
                    getMainColor(prot)
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, darkBorder, -1),
                    y,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
                y += 4f
            }
            if (thorn > 0) {
                drawExhiOutlined(
                    thorn.toString() + "",
                    drawExhiOutlined("T", x, y, darkBorder, -1),
                    y,
                    getBorderColor(thorn),
                    getMainColor(thorn)
                )
                y += 4f
            }
        }
        if (stack.item is ItemBow) {
            val power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack)
            val punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack)
            val flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (power > 0) {
                drawExhiOutlined(
                    power.toString() + "",
                    drawExhiOutlined("Pow", x, y, darkBorder, -1),
                    y,
                    getBorderColor(power),
                    getMainColor(power)
                )
                y += 4f
            }
            if (punch > 0) {
                drawExhiOutlined(
                    punch.toString() + "",
                    drawExhiOutlined("Pun", x, y, darkBorder, -1),
                    y,
                    getBorderColor(punch),
                    getMainColor(punch)
                )
                y += 4f
            }
            if (flame > 0) {
                drawExhiOutlined(
                    flame.toString() + "",
                    drawExhiOutlined("F", x, y, darkBorder, -1),
                    y,
                    getBorderColor(flame),
                    getMainColor(flame)
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, darkBorder, -1),
                    y,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
                y += 4f
            }
        }
        if (stack.item is ItemSword) {
            val sharp = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
            val kb = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack)
            val fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (sharp > 0) {
                drawExhiOutlined(
                    sharp.toString() + "",
                    drawExhiOutlined("S", x, y, darkBorder, -1),
                    y,
                    getBorderColor(sharp),
                    getMainColor(sharp)
                )
                y += 4f
            }
            if (kb > 0) {
                drawExhiOutlined(
                    kb.toString() + "",
                    drawExhiOutlined("K", x, y, darkBorder, -1),
                    y,
                    getBorderColor(kb),
                    getMainColor(kb)
                )
                y += 4f
            }
            if (fire > 0) {
                drawExhiOutlined(
                    fire.toString() + "",
                    drawExhiOutlined("F", x, y, darkBorder, -1),
                    y,
                    getBorderColor(fire),
                    getMainColor(fire)
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, darkBorder, -1),
                    y,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
            }
        }
        enableDepth()
        RenderHelper.enableGUIStandardItemLighting()
    }

    fun drawGradientRoundedRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Int,
        startColor: Int,
        endColor: Int
    ) {
        Stencil.write(false)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        fastRoundedRect(left, top, right, bottom, radius.toFloat())
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        Stencil.erase(true)
        drawGradientRect(
            left.toInt(),
            top.toInt(),
            right.toInt(),
            bottom.toInt(),
            startColor,
            endColor,
            0f
        )
        Stencil.dispose()
    }

    fun drawEntityBoxESP(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        pushMatrix()
        glBlendFunc(770, 771)
        enableGlCap(3042)
        disableGlCap(3553, 2929)
        glDepthMask(false)
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)
        val entityBox = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )
        glTranslated(x, y, z)
        glRotated(-entity.rotationYawHead.toDouble(), 0.0, 1.0, 0.0)
        glTranslated(-x, -y, -z)
        glLineWidth(3.0f)
        enableGlCap(2848)
        glColor(0, 0, 0, 255)
        RenderGlobal.drawSelectionBoundingBox(axisAlignedBB)
        glLineWidth(1.0f)
        enableGlCap(2848)
        glColor(color.red, color.green, color.blue, 255)
        RenderGlobal.drawSelectionBoundingBox(axisAlignedBB)
        resetColor()
        glDepthMask(true)
        resetCaps()
        popMatrix()
    }

    fun drawHead(skin: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, color: Int) {
        mc.textureManager.bindTexture(skin)
        drawScaledCustomSizeModalRect(
            x, y, 8f, 8f, 8, 8, width, height,
            64f, 64f
        )
        drawScaledCustomSizeModalRect(
            x, y, 40f, 8f, 8, 8, width, height,
            64f, 64f
        )
    }

    fun quickDrawHead(skin: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        mc.textureManager.bindTexture(skin)
        drawScaledCustomSizeModalRect(
            x, y, 8f, 8f, 8, 8, width, height,
            64f, 64f
        )
        drawScaledCustomSizeModalRect(
            x, y, 40f, 8f, 8, 8, width, height,
            64f, 64f
        )
    }

    fun drawEntityOnScreen(posX: Double, posY: Double, scale: Float, entity: EntityLivingBase?) {
        pushMatrix()
        enableColorMaterial()

        translate(posX, posY, 50.0)
        scale((-scale), scale, scale)
        rotate(180f, 0f, 0f, 1f)
        rotate(135f, 0f, 1f, 0f)
        RenderHelper.enableStandardItemLighting()
        rotate(-135f, 0f, 1f, 0f)
        translate(0.0, 0.0, 0.0)

        val rendermanager = mc.renderManager
        rendermanager.setPlayerViewY(180f)
        rendermanager.isRenderShadow = false
        rendermanager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0f, 1f)
        rendermanager.isRenderShadow = true

        popMatrix()
        RenderHelper.disableStandardItemLighting()
        disableRescaleNormal()
        setActiveTexture(OpenGlHelper.lightmapTexUnit)
        disableTexture2D()
        setActiveTexture(OpenGlHelper.defaultTexUnit)
    }

    fun drawEntityOnScreen(posX: Int, posY: Int, scale: Int, entity: EntityLivingBase?) {
        drawEntityOnScreen(posX.toDouble(), posY.toDouble(), scale.toFloat(), entity)
    }

    fun interpolateColors(color1: Int, color2: Int, progress: Float): Int {
        val alpha = ((1.0 - progress) * (color1 ushr 24) + progress * (color2 ushr 24)).toInt()
        val red = ((1.0 - progress) * ((color1 shr 16) and 0xFF) + progress * ((color2 shr 16) and 0xFF)).toInt()
        val green = ((1.0 - progress) * ((color1 shr 8) and 0xFF) + progress * ((color2 shr 8) and 0xFF)).toInt()
        val blue = ((1.0 - progress) * (color1 and 0xFF) + progress * (color2 and 0xFF)).toInt()

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    fun drawRectFloat(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
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
        val worldrenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    // Enable Render 2D (GL11)
    fun enableRender2D() {
        glEnable(3042)
        glDisable(2884)
        glDisable(3553)
        glEnable(2848)
        glBlendFunc(770, 771)
        glLineWidth(1.0f)
    }

    // Disable Render 2D (GL11)
    fun disableRender2D() {
        glDisable(3042)
        glEnable(2884)
        glEnable(3553)
        glDisable(2848)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        shadeModel(7424)
        disableBlend()
        enableTexture2D()
    }

    fun setGLCap(cap: Int, flag: Boolean) {
        glCapMap[cap] = glGetBoolean(cap)
        if (flag) {
            glEnable(cap)
        } else {
            glDisable(cap)
        }
    }

    private fun revertGLCap(cap: Int) {
        val origCap: Boolean = glCapMap[cap] == true
        if (origCap) {
            glEnable(cap)
        } else {
            glDisable(cap)
        }
    }

    fun revertAllCaps() {
        val localIterator: Iterator<*> = glCapMap.keys.iterator()
        while (localIterator.hasNext()) {
            val cap = localIterator.next() as Int
            revertGLCap(cap)
        }
    }

    fun renderOutlines(x: Double, y: Double, z: Double, width: Float, height: Float, c: Color, outlinewidth: Float) {
        var y = y
        val halfwidth = width / 2.0f
        val halfheight = height / 2.0f
        pushMatrix()
        depthMask(false)
        disableTexture2D()
        disableLighting()
        disableCull()
        disableBlend()
        disableDepth()
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR)
        y++
        glLineWidth(outlinewidth)
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        tessellator.draw()
        enableDepth()
        depthMask(true)
        enableTexture2D()
        enableLighting()
        enableCull()
        enableBlend()
        popMatrix()
    }

    fun renderBox(x: Double, y: Double, z: Double, width: Float, height: Float, c: Color) {
        var y = y
        val halfwidth = width / 2.0f
        val halfheight = height / 2.0f
        pushMatrix()
        depthMask(false)
        disableTexture2D()
        disableLighting()
        disableCull()
        disableBlend()
        disableDepth()
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        enableBlend()
        tryBlendFuncSeparate(770,
            771,
            1,
            0)
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        y++
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        tessellator.draw()
        enableDepth()
        depthMask(true)
        enableTexture2D()
        enableLighting()
        enableCull()
        enableBlend()
        resetColor()
        popMatrix()
    }

    fun startDrawing() {
        glEnable(3042);
        glEnable(3042);
        glBlendFunc(770, 771);
        glEnable(2848);
        glDisable(3553);
        glDisable(2929);
        getMinecraft().entityRenderer.setupCameraTransform(getMinecraft().timer.renderPartialTicks, 0);
    }

    fun stopDrawing() {
        glDisable(3042)
        glEnable(3553)
        glDisable(2848)
        glDisable(3042)
        glEnable(2929)
    }

    fun customRotatedObject2D(x: Float, y: Float, width: Float, height: Float, rotation: Double) {
        val centerX = x + width / 2
        val centerY = y + height / 2
        translate(centerX.toDouble(), centerY.toDouble(), 0.0)
        rotate(rotation.toFloat(), 0f, 0f, 1f)
        translate(-centerX.toDouble(), -centerY.toDouble(), 0.0)
    }

    fun setupOrientationMatrix(x: Double, y: Double, z: Double) {
        translate(x - mc.renderManager.viewerPosX, y - mc.renderManager.viewerPosY, z - mc.renderManager.viewerPosZ)
    }

    fun setupDrawCircles(render: Runnable) {
        val lightingEnabled = glIsEnabled(GL_LIGHTING)
        pushMatrix()
        enableBlend()
        enableAlpha()
        alphaFunc(GL_GREATER, 0f)
        depthMask(false)
        disableCull()
        if (lightingEnabled) disableLighting()
        shadeModel(GL_SMOOTH)

        blendFunc(770, 1)
        setupOrientationMatrix(0.0, 0.0, 0.0)
        render.run()
        blendFunc(770, 771)
        color(1f, 1f, 1f)
        shadeModel(GL_FLAT)
        if (lightingEnabled) enableLighting()
        enableCull()
        depthMask(true)
        alphaFunc(GL_GREATER, .1f)
        enableAlpha()
        popMatrix()
    }

    @JvmStatic
    fun renderGLUtil(mode: Int, render: Runnable) {
        glBegin(mode)
        render.run()
        glEnd()
    }

    @JvmStatic
    fun setup2DRenderingGLUtil(f: Runnable) {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        f.run()
        glEnable(GL_TEXTURE_2D)
        disableBlend()
    }

    fun interpolate(current: Double, old: Double, scale: Double): Double =
        old + (current - old) * scale

    fun isInViewFrustum(entity: Entity): Boolean =
        isInViewFrustum(entity.entityBoundingBox) || entity.ignoreFrustumCheck

    private fun isInViewFrustum(bb: AxisAlignedBB): Boolean {
        val current = mc.renderViewEntity
        frustrum.setPosition(current.posX, current.posY, current.posZ)
        return frustrum.isBoundingBoxInFrustum(bb)
    }

    fun newDrawRect(
        leftInput: Float,
        topInput: Float,
        rightInput: Float,
        bottomInput: Float,
        color: Int
    ) {
        var left = leftInput
        var right = rightInput
        var top = topInput
        var bottom = bottomInput

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

        val f3 = ((color ushr 24) and 0xFF) / 255.0f
        val f  = ((color ushr 16) and 0xFF) / 255.0f
        val f1 = ((color ushr 8)  and 0xFF) / 255.0f
        val f2 = ((color)        and 0xFF) / 255.0f

        val tessellator    = Tessellator.getInstance()
        val worldRenderer  = tessellator.worldRenderer

        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f, f1, f2, f3)

        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(left.toDouble(),  bottom.toDouble(), 0.0).endVertex()
        worldRenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldRenderer.pos(right.toDouble(), top.toDouble(),    0.0).endVertex()
        worldRenderer.pos(left.toDouble(),  top.toDouble(),    0.0).endVertex()
        tessellator.draw()

        enableTexture2D()
        disableBlend()
    }

    fun newDrawRect(
        leftInput: Double,
        topInput: Double,
        rightInput: Double,
        bottomInput: Double,
        color: Int
    ) {
        var left = leftInput
        var right = rightInput
        var top = topInput
        var bottom = bottomInput

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

        val f3 = ((color ushr 24) and 0xFF) / 255.0f
        val f  = ((color ushr 16) and 0xFF) / 255.0f
        val f1 = ((color ushr 8)  and 0xFF) / 255.0f
        val f2 = ((color)        and 0xFF) / 255.0f

        val tessellator    = Tessellator.getInstance()
        val worldRenderer  = tessellator.worldRenderer

        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f, f1, f2, f3)

        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(left,  bottom, 0.0).endVertex()
        worldRenderer.pos(right, bottom, 0.0).endVertex()
        worldRenderer.pos(right, top,    0.0).endVertex()
        worldRenderer.pos(left,  top,    0.0).endVertex()
        tessellator.draw()

        enableTexture2D()
        disableBlend()
    }
}