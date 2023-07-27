/*
 * ZAVZ Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.currentTarget
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.api.FontManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.ColorUtils.reAlpha
import net.ccbluex.liquidbounce.utils.render.ColorUtils.slowlyRainbow
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "KillESP", category = ModuleCategory.RENDER)
class KillESP : Module() {

    private val showTargetValue = BoolValue("ShowTarget", true)
    val markValue = ListValue("Mark", arrayOf("Liquid", "FDP", "Block", "OtherBlock", "Jello", "Sims", "Lies", "None"), "Jello")

    val modeValue = ListValue("Mode", arrayOf("Default", "Box", "Jello", "Lies", "Sims", "Tracers", "Zavz"), "Sims")
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "Slowly", "Fade", "Health"), "Custom")
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
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
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

    private val markTimer = MSTimer()
    private var markEntity: EntityLivingBase? = null

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
    fun onRender2D(event: Render2DEvent) {
        if (showTargetValue.get()) {
            val sr2 = ScaledResolution(mc)
            if (currentTarget != null) {
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                Fonts.font32.drawStringWithShadow(currentTarget!!.name, (sr2.scaledWidth / 2 - Fonts.font32.getStringWidth(currentTarget!!.name) / 2).toFloat(), (sr2.scaledHeight / 2 - 40).toFloat(), 16777215)
                mc.textureManager.bindTexture(ResourceLocation("textures/gui/icons.png"))
                var i2 = 0
                var i3 = 0
                while (i2 < currentTarget!!.maxHealth / 2) {
                    mc.ingameGUI.drawTexturedModalRect(((sr2.scaledWidth / 2) - currentTarget!!.maxHealth / 2.0f * 10.0f / 2.0f + (i2 * 10)).toInt(), (sr2.scaledHeight / 2 - 20), 16, 0, 9, 9);
                    ++i2
                }
                i2 = 0
                while (i2 < currentTarget!!.health / 2.0){
                    mc.ingameGUI.drawTexturedModalRect(((sr2.scaledWidth / 2) - currentTarget!!.maxHealth / 2.0f * 10.0f / 2.0f + (i2 * 10)).toInt(), (sr2.scaledHeight / 2 - 20), 52, 0, 9, 9);
                    ++i2
                }
                while (i3 < 20 / 2.0f) {
                    mc.ingameGUI.drawTexturedModalRect(((sr2.scaledWidth / 2) - currentTarget!!.maxHealth / 2.0f * 10.0f / 2.0f + (i3 * 10)).toInt(), (sr2.scaledHeight / 2 - 30), 16, 9, 9, 9);
                    ++i3;
                }
                i3 = 0;
                while (i3 < currentTarget!!.totalArmorValue / 2.0f) {
                    mc.ingameGUI.drawTexturedModalRect(((sr2.scaledWidth / 2) - currentTarget!!.maxHealth / 2.0f * 10.0f / 2.0f + (i3 * 10)).toInt(), (sr2.scaledHeight / 2 - 30), 34, 9, 9, 9);
                    ++i3;
                }
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        discoveredTargets.forEach {
        if (modeValue.get().equals("jello", ignoreCase = true) && !aura!!.targetModeValue.get()
                .equals("multi", ignoreCase = true)) {
            val drawTime = (System.currentTimeMillis() % 2000).toInt()
            val drawMode=drawTime>1000
            var drawPercent=drawTime/1000.0
            if(!drawMode){
                drawPercent=1-drawPercent
            }else{
                drawPercent-=1
            }
            drawPercent= EaseUtils.easeInOutQuad(drawPercent)
            val points = mutableListOf<Vec3>()
            val bb=it.hitBox
            val radius=bb.maxX-bb.minX
            val height=bb.maxY-bb.minY
            val posX = it.lastTickPosX + (it.posX - it.lastTickPosX) * mc.timer.renderPartialTicks
            var posY = it.lastTickPosY + (it.posY - it.lastTickPosY) * mc.timer.renderPartialTicks
            if(drawMode){
                posY-=0.5
            }else{
                posY+=0.5
            }
            val posZ = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * mc.timer.renderPartialTicks
            for(i in 0..360 step 7){
                points.add(Vec3(posX - sin(i * Math.PI / 180F) * radius,posY+height*drawPercent,posZ + cos(i * Math.PI / 180F) * radius))
            }
            points.add(points[0])
            //draw
            mc.entityRenderer.disableLightmap()
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            val baseMove=(if(drawPercent>0.5){1-drawPercent}else{drawPercent})*2
            val min=(height/60)*20*(1-baseMove)*(if(drawMode){-1}else{1})
            for(i in 0..20) {
                var moveFace=(height/60F)*i*baseMove
                if(drawMode){
                    moveFace=-moveFace
                }
                val firstPoint=points[0]
                GL11.glVertex3d(
                    firstPoint.xCoord - mc.renderManager.viewerPosX, firstPoint.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                    firstPoint.zCoord - mc.renderManager.viewerPosZ
                )
                GL11.glColor4f(1F, 1F, 1F, 0.7F*(i/20F))
                for (vec3 in points) {
                    GL11.glVertex3d(
                        vec3.xCoord - mc.renderManager.viewerPosX, vec3.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                        vec3.zCoord - mc.renderManager.viewerPosZ
                    )
                }
                GL11.glColor4f(0F,0F,0F,0F)
            }
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        } else if (modeValue.get().equals("lies", ignoreCase = true)) {
            val everyTime = 3000
            val drawTime = (System.currentTimeMillis() % everyTime).toInt()
            val drawMode = drawTime > (everyTime / 2)
            var drawPercent = drawTime / (everyTime / 2.0)
            // true when goes up
            if (!drawMode) {
                drawPercent = 1 - drawPercent
            } else {
                drawPercent -= 1
            }
            drawPercent = EaseUtils.easeInOutQuad(drawPercent)
            mc.entityRenderer.disableLightmap()
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_CULL_FACE)
            GL11.glShadeModel(7425)
            mc.entityRenderer.disableLightmap()

            val bb = it.hitBox
            val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * 0.5f
            val height = bb.maxY - bb.minY
            val x =
                it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
            val y =
                (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
            val z =
                it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
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
                val color = Color.getHSBColor(
                    if (i < 180) {
                        HUD.rainbowStartValue.get() + (HUD.rainbowStopValue.get() - HUD.rainbowStartValue.get()) * (i / 180f)
                    } else {
                        HUD.rainbowStartValue.get() + (HUD.rainbowStopValue.get() - HUD.rainbowStartValue.get()) * (-(i - 360) / 180f)
                    }, 0.7f, 1.0f
                )
                val x1 = x - sin(i * Math.PI / 180F) * radius
                val z1 = z + cos(i * Math.PI / 180F) * radius
                val x2 = x - sin((i - 5) * Math.PI / 180F) * radius
                val z2 = z + cos((i - 5) * Math.PI / 180F) * radius
                GL11.glBegin(GL11.GL_QUADS)
                RenderUtils.glColor(color, 0f)
                GL11.glVertex3d(x1, y + eased, z1)
                GL11.glVertex3d(x2, y + eased, z2)
                RenderUtils.glColor(color, 150f)
                GL11.glVertex3d(x2, y, z2)
                GL11.glVertex3d(x1, y, z1)
                GL11.glEnd()
            }

            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glShadeModel(7424)
            GL11.glColor4f(1f, 1f, 1f, 1f)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }  else if (modeValue.get().equals("sims", ignoreCase = true)) {
            if (!aura!!.targetModeValue.get().equals("multi", ignoreCase = true) && aura!!.currentTarget != null
            )   renderESP()
                drawESP(
                currentTarget!!,
                if (it.hurtTime <= 0) Color(80, 255, 80, 200).rgb else Color(255, 0, 0, 200).rgb,
                event
            )
        } else if (modeValue.get().equals("default", ignoreCase = true)) {
            if (!aura!!.targetModeValue.get()
                    .equals("multi", ignoreCase = true) && aura!!.currentTarget != null) RenderUtils.drawPlatforms(
                aura!!.currentTarget,
                if (aura!!.currentTarget!!.hurtTime > 0) reAlpha(
                    getColor(aura!!.currentTarget),
                    colorAlphaValue.get()
                ) else Color(235, 40, 40, colorAlphaValue.get())
            )
        } else if (modeValue.get().equals("default", ignoreCase = true)) {
            if (!aura!!.targetModeValue.get()
                    .equals("multi", ignoreCase = true) && aura!!.currentTarget != null) RenderUtils.drawPlatforms(
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

    private fun renderESP() {
        if (markEntity !=null){
            if(markTimer.hasTimePassed(500) || markEntity!!.isDead){
                markEntity =null
                return
            }
            //can mark
            val drawTime = (System.currentTimeMillis() % 2000).toInt()
            val drawMode=drawTime>1000
            var drawPercent=drawTime/1000F
            //true when goes up
            if(!drawMode){
                drawPercent=1-drawPercent
            }else{
                drawPercent-=1
            }
            val points = mutableListOf<Vec3>()
            val bb= markEntity!!.entityBoundingBox
            val radius=bb.maxX-bb.minX
            val height=bb.maxY-bb.minY
            val posX = markEntity!!.lastTickPosX + (markEntity!!.posX - markEntity!!.lastTickPosX) * mc.timer.renderPartialTicks
            var posY = markEntity!!.lastTickPosY + (markEntity!!.posY - markEntity!!.lastTickPosY) * mc.timer.renderPartialTicks
            if(drawMode){
                posY-=0.5
            }else{
                posY+=0.5
            }
            val posZ = markEntity!!.lastTickPosZ + (markEntity!!.posZ - markEntity!!.lastTickPosZ) * mc.timer.renderPartialTicks
            for(i in 0..360 step 7){
                points.add(Vec3(posX - sin(i * Math.PI / 180F) * radius,posY+height*drawPercent,posZ + cos(i * Math.PI / 180F) * radius))
            }
            points.add(points[0])
            //draw
            mc.entityRenderer.disableLightmap()
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            for(i in 0..20) {
                var moveFace=(height/60F)*i
                if(drawMode){
                    moveFace=-moveFace
                }
                val firstPoint=points[0]
                GL11.glVertex3d(
                    firstPoint.xCoord - mc.renderManager.viewerPosX, firstPoint.yCoord - moveFace - mc.renderManager.viewerPosY,
                    firstPoint.zCoord - mc.renderManager.viewerPosZ
                )
                GL11.glColor4f(1F, 1F, 1F, 0.7F*(i/20F))
                for (vec3 in points) {
                    GL11.glVertex3d(
                        vec3.xCoord - mc.renderManager.viewerPosX, vec3.yCoord - moveFace - mc.renderManager.viewerPosY,
                        vec3.zCoord - mc.renderManager.viewerPosZ
                    )
                }
                GL11.glColor4f(0F,0F,0F,0F)
            }
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }

    private fun drawESP(entity: EntityLivingBase, color: Int, e: Render3DEvent) {
        val x: Double =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * e.partialTicks.toDouble() - mc.renderManager.renderPosX
        val y: Double =
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * e.partialTicks.toDouble() - mc.renderManager.renderPosY
        val z: Double =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * e.partialTicks.toDouble() - mc.renderManager.renderPosZ
        val radius = 0.15f
        val side = 4
        GL11.glPushMatrix()
        GL11.glTranslated(x, y + 2, z)
        GL11.glRotatef(-entity.width, 0.0f, 1.0f, 0.0f)
        RenderUtils.glColor(color)
        RenderUtils.enableSmoothLine(1.5F)
        val c = Cylinder()
        GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
        c.drawStyle = 100012
        RenderUtils.glColor(if (entity.hurtTime <= 0) Color(80,255,80,200) else Color(255, 0, 0, 200))
        c.draw(0F, radius, 0.3f, side, 1)
        c.drawStyle = 100012
        GL11.glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0f, 0.3f, side, 1)
        GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
        c.drawStyle = 100011
        GL11.glTranslated(0.0, 0.0, -0.3)
        RenderUtils.glColor(color)
        c.draw(0F, radius, 0.3f, side, 1)
        c.drawStyle = 100011
        GL11.glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0F, 0.3f, side, 1)
        RenderUtils.disableSmoothLine()
        GL11.glPopMatrix()
    }
}