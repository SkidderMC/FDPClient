/*
 * ZAVZ Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.ColorUtils.reAlpha
import net.ccbluex.liquidbounce.utils.render.ColorUtils.slowlyRainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "KillESP", category = ModuleCategory.RENDER)
class KillESP : Module() {

    val modeValue = ListValue("Mode", arrayOf("Default", "Box", "Jello", "Tracers", "Zavz"), "Zavz")
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "Slowly", "Fade", "Mixer", "Health"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)
    private val jelloAlphaValue = FloatValue("JelloEndAlphaPercent", 0.4f, 0f, 1f)
    private val jelloWidthValue = FloatValue("JelloCircleWidth", 3f, 0.01f, 5f)
    private val jelloGradientHeightValue = FloatValue("JelloGradientHeight", 3f, 1f, 8f)
    private val jelloFadeSpeedValue = FloatValue("JelloFadeSpeed", 0.1f, 0.01f, 0.5f)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    var moveMarkValue = FloatValue("MoveMarkY", 0.6f, 0f, 2f)
    private val thicknessValue = FloatValue("Thickness", 1f, 0.1f, 5f)
    private val colorTeam = BoolValue("Team", false)
    private var entity: EntityLivingBase? = null
    private var direction = 1.0
    override var yPos: Float = 0.0F
    private var progress = 0.0
    private var al = 0f
    private var bb: AxisAlignedBB? = null
    private var start = 0.0
    private var aura: KillAura? = null
    private var lastMS = System.currentTimeMillis()
    private var lastDeltaMS = 0L
    override fun onEnable() {
        start = 0.0
    }

    override fun onInitialize() {
        aura = FDPClient.moduleManager.getModule(KillAura::class.java)
    }

    @EventTarget
    fun onTick(event: TickEvent?) {
        when {
            modeValue.get().equals("jello", ignoreCase = true) && !aura!!.targetModeValue.get().equals("multi", ignoreCase = true) -> {
                al = AnimationUtils.changer(
                    al,
                    if (aura!!.currentTarget != null) jelloFadeSpeedValue.get() else -jelloFadeSpeedValue.get(),
                    0f,
                    colorAlphaValue.get() / 255.0f
                )
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (modeValue.get().equals("jello", ignoreCase = true) && !aura!!.targetModeValue.get()
                .equals("multi", ignoreCase = true)
        ) {
            val lastY = yPos
            if (al > 0f) {
                if (System.currentTimeMillis() - lastMS >= 1000L) {
                    direction = -direction
                    lastMS = System.currentTimeMillis()
                }
                val weird =
                    if (direction > 0) System.currentTimeMillis() - lastMS else 1000L - (System.currentTimeMillis() - lastMS)
                progress = weird.toDouble() / 1000.0
                lastDeltaMS = System.currentTimeMillis() - lastMS
            } else { // keep the progress
                lastMS = System.currentTimeMillis() - lastDeltaMS
            }
            if (aura!!.currentTarget != null) {
                entity = aura!!.currentTarget
                bb = entity!!.entityBoundingBox
            }
            if (bb == null || entity == null) return
            val radius = bb!!.maxX - bb!!.minX
            val height = bb!!.maxY - bb!!.minY
            val posX = entity!!.lastTickPosX + (entity!!.posX - entity!!.lastTickPosX) * mc.timer.renderPartialTicks
            val posY = entity!!.lastTickPosY + (entity!!.posY - entity!!.lastTickPosY) * mc.timer.renderPartialTicks
            val posZ = entity!!.lastTickPosZ + (entity!!.posZ - entity!!.lastTickPosZ) * mc.timer.renderPartialTicks
            yPos = (easeInOutQuart(progress) * height).toFloat()
            val deltaY =
                (if (direction > 0) yPos - lastY else lastY - yPos) * -direction * jelloGradientHeightValue.get()
            if (al <= 0 && entity != null) {
                entity = null
                return
            }
            val colour = getColor(entity)
            val r = colour.red / 255.0f
            val g = colour.green / 255.0f
            val b = colour.blue / 255.0f
            pre3D()
            //post circles
            GL11.glTranslated(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ)
            GL11.glBegin(GL11.GL_QUAD_STRIP)
            for (i in 0..360) {
                val calc = i * Math.PI / 180
                val posX2 = posX - Math.sin(calc) * radius
                val posZ2 = posZ + Math.cos(calc) * radius
                GL11.glColor4f(r, g, b, 0f)
                GL11.glVertex3d(posX2, posY + yPos + deltaY, posZ2)
                GL11.glColor4f(r, g, b, al * jelloAlphaValue.get())
                GL11.glVertex3d(posX2, posY + yPos, posZ2)
            }
            GL11.glEnd()
            drawCircle(posX, posY + yPos, posZ, jelloWidthValue.get(), radius, r, g, b, al)
            post3D()
        } else if (modeValue.get().equals("default", ignoreCase = true)) {
            if (!aura!!.targetModeValue.get()
                    .equals("multi", ignoreCase = true) && aura!!.currentTarget != null
            ) RenderUtils.drawPlatforms(
                aura!!.currentTarget,
                if (aura!!.currentTarget!!.hurtTime > 0) reAlpha(
                    getColor(aura!!.currentTarget),
                    colorAlphaValue.get()
                ) else Color(235, 40, 40, colorAlphaValue.get())
            )
        } else if (modeValue.get().equals("tracers", ignoreCase = true)) {
            if (!aura!!.targetModeValue.get().equals("multi", ignoreCase = true) && aura!!.currentTarget != null) {
                val tracers = FDPClient.moduleManager.getModule(Tracers::class.java) ?: return
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glLineWidth(thicknessValue.get())
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glDepthMask(false)
                GL11.glBegin(GL11.GL_LINES)
                var dist = (mc.thePlayer.getDistanceToEntity(aura!!.currentTarget) * 2).toInt()
                if (dist > 255) dist = 255
                tracers.drawTraces(aura!!.currentTarget!!, getColor(aura!!.currentTarget), false)
                GL11.glEnd()
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDepthMask(true)
                GL11.glDisable(GL11.GL_BLEND)
                GlStateManager.resetColor()
            }
        } else {
            if (!aura!!.targetModeValue.get()
                    .equals("multi", ignoreCase = true) && aura!!.currentTarget != null
            ) RenderUtils.drawEntityBox(
                aura!!.currentTarget,
                if (aura!!.currentTarget!!.hurtTime > 3) reAlpha(
                    getColor(aura!!.currentTarget),
                    colorAlphaValue.get()
                ) else Color(255, 0, 0, colorAlphaValue.get()),
                false
            )
        }
    }

    fun getColor(ent: Entity?): Color {
        if (ent is EntityLivingBase) {
            val entityLivingBase = ent
            if (colorModeValue.get().equals("Health", ignoreCase = true)) return BlendUtils.getHealthColor(
                entityLivingBase.health,
                entityLivingBase.maxHealth
            )
            if (colorTeam.get()) {
                val chars = entityLivingBase.displayName.formattedText.toCharArray()
                var color = Int.MAX_VALUE
                for (i in chars.indices) {
                    if (chars[i] != '§' || i + 1 >= chars.size) continue
                    val index = getColorIndex(chars[i + 1])
                    if (index < 0 || index > 15) continue
                    color = ColorUtils.hexColors[index]
                    break
                }
                return Color(color)
            }
        }
        return when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "Slowly" -> slowlyRainbow(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
            else -> fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
        }
    }

    private fun drawCircle(
        x: Double,
        y: Double,
        z: Double,
        width: Float,
        radius: Double,
        red: Float,
        green: Float,
        blue: Float,
        alp: Float
    ) {
        GL11.glLineWidth(width)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        GL11.glColor4f(red, green, blue, alp)
        var i = 0
        while (i <= 360) {
            val posX = x - Math.sin(i * Math.PI / 180) * radius
            val posZ = z + Math.cos(i * Math.PI / 180) * radius
            GL11.glVertex3d(posX, y, posZ)
            i += 1
        }
        GL11.glEnd()
    }

    private fun easeInOutQuart(x: Double): Double {
        return if (x < 0.5) 8 * x * x * x * x else 1 - Math.pow(-2 * x + 2, 4.0) / 2
    }

    override val tag: String
        get() = modeValue.get()

    companion object {
        private var category = ModuleCategory.RENDER
        private const val DOUBLE_PI = Math.PI * 2
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
            GL11.glDisable(2884)
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
    }
}