package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.roundToInt

@ElementInfo(name = "Targets")
class Targets : Element(-46.0, -40.0, 1F, Side(Side.Horizontal.MIDDLE, Side.Vertical.MIDDLE)) {
    private val modeValue = ListValue("Mode", arrayOf("Novoline", "Astolfo", "Liquid", "Flux", "Rise", "Zamorozka", "Arris", "Tenacity"), "Rise")
    private val animSpeedValue = IntegerValue("AnimSpeed", 10, 5, 20)
    private val hpAnimTypeValue = EaseUtils.getEnumEasingList("HpAnimType")
    private val hpAnimOrderValue = EaseUtils.getEnumEasingOrderList("HpAnimOrder")
    private val switchModeValue = ListValue("SwitchMode", arrayOf("Slide", "Zoom", "None"), "Slide")
    private val switchAnimTypeValue = EaseUtils.getEnumEasingList("SwitchAnimType")
    private val switchAnimOrderValue = EaseUtils.getEnumEasingOrderList("SwitchAnimOrder")
    private val switchAnimSpeedValue = IntegerValue("SwitchAnimSpeed", 20, 5, 40)
    private val arrisRoundedValue = BoolValue("ArrisRounded", true)
    private val riseCountValue = IntegerValue("Rise-Count", 5, 1, 20)
    private val riseSizeValue = FloatValue("Rise-Size", 1f, 0.5f, 3f)
    private val riseAlphaValue = FloatValue("Rise-Alpha", 0.7f, 0.1f, 1f)
    private val riseDistanceValue = FloatValue("Rise-Distance", 1f, 0.5f, 2f)
    private val riseMoveTimeValue = IntegerValue("Rise-MoveTime", 20, 5, 40)
    private val riseFadeTimeValue = IntegerValue("Rise-FadeTime", 20, 5, 40)
    private val fontValue = FontValue("Font", Fonts.font40)

    private var prevTarget: EntityLivingBase? = null
    private var displayPercent = 0f
    private var lastUpdate = System.currentTimeMillis()
    private val decimalFormat = DecimalFormat("0.0")

    private var hpEaseAnimation: Animation? = null
    private var easingHP = 0f
        get() {
            if (hpEaseAnimation != null) {
                field = hpEaseAnimation!!.value.toFloat()
                if (hpEaseAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    hpEaseAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (hpEaseAnimation == null || (hpEaseAnimation != null && hpEaseAnimation!!.to != value.toDouble())) {
                hpEaseAnimation = Animation(EaseUtils.EnumEasingType.valueOf(hpAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(hpAnimOrderValue.get()), field.toDouble(), value.toDouble(), animSpeedValue.get() * 100L).start()
            }
        }

    private fun getHealth(entity: EntityLivingBase?): Float {
        return entity?.health ?: 0f
    }

    override fun drawElement(partialTicks: Float): Border? {
        var target = LiquidBounce.combatManager.target
        val time = System.currentTimeMillis()
        val pct = (time - lastUpdate) / (switchAnimSpeedValue.get() * 50f)
        lastUpdate = System.currentTimeMillis()

        if (mc.currentScreen is GuiHudDesigner) {
            target = mc.thePlayer
        } else if (target != null) {
            prevTarget = target
        }
        prevTarget ?: return getTBorder()

        if (target != null) {
            if (displayPercent < 1) {
                displayPercent += pct
            }
            if (displayPercent > 1) {
                displayPercent = 1f
            }
        } else {
            if (displayPercent > 0) {
                displayPercent -= pct
            }
            if (displayPercent < 0) {
                displayPercent = 0f
                prevTarget = null
                return getTBorder()
            }
        }

        easingHP = getHealth(target)

        val easedPersent = EaseUtils.apply(EaseUtils.EnumEasingType.valueOf(switchAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(switchAnimOrderValue.get()), displayPercent.toDouble()).toFloat()
        when (switchModeValue.get().lowercase()) {
            "zoom" -> {
                val border = getTBorder() ?: return null
                GL11.glScalef(easedPersent, easedPersent, easedPersent)
                GL11.glTranslatef(((border.x2 * 0.5f * (1 - easedPersent)) / easedPersent), ((border.y2 * 0.5f * (1 - easedPersent)) / easedPersent), 0f)
            }
            "slide" -> {
                val percent = EaseUtils.easeInQuint(1.0 - easedPersent)
                val xAxis = ScaledResolution(mc).scaledWidth - renderX
                GL11.glTranslated(xAxis * percent, 0.0, 0.0)
            }
        }

        when (modeValue.get().lowercase()) {
            "novoline" -> drawNovo(prevTarget!!)
            "astolfo" -> drawAstolfo(prevTarget!!)
            "liquid" -> drawLiquid(prevTarget!!)
            "flux" -> drawFlux(prevTarget!!)
            "rise" -> drawRise(prevTarget!!)
            "zamorozka" -> drawZamorozka(prevTarget!!)
            "arris" -> drawArris(prevTarget!!)
            "tenacity" -> drawTenacity(prevTarget!!)
        }

        return getTBorder()
    }

    private fun drawAstolfo(target: EntityLivingBase) {
        val font = fontValue.get()
        val color = ColorUtils.skyRainbow(1, 1F, 0.9F, 5.0)
        val hpPct = easingHP / target.maxHealth

        RenderUtils.drawRect(0F, 0F, 140F, 60F, Color(0, 0, 0, 110).rgb)

        // health rect
        RenderUtils.drawRect(3F, 55F, 137F, 58F, ColorUtils.reAlpha(color, 100).rgb)
        RenderUtils.drawRect(3F, 55F, 3 + (hpPct * 134F), 58F, color.rgb)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderUtils.drawEntityOnScreen(18, 46, 20, target)

        font.drawStringWithShadow(target.name, 37F, 6F, -1)
        GL11.glPushMatrix()
        GL11.glScalef(2F, 2F, 2F)
        font.drawString("${getHealth(target).roundToInt()} ❤", 19, 9, color.rgb)
        GL11.glPopMatrix()
    }

    private fun drawNovo(target: EntityLivingBase) {
        val font = fontValue.get()
        val color = ColorUtils.healthColor(getHealth(target), target.maxHealth)
        val darkColor = ColorUtils.darker(color, 0.6F)
        val hpPos = 33F + ((getHealth(target) / target.maxHealth * 10000).roundToInt() / 100)

        RenderUtils.drawRect(0F, 0F, 140F, 40F, Color(40, 40, 40).rgb)
        font.drawString(target.name, 33, 5, Color.WHITE.rgb)
        RenderUtils.drawEntityOnScreen(20, 35, 15, target)
        RenderUtils.drawRect(hpPos, 18F, 33F + ((easingHP / target.maxHealth * 10000).roundToInt() / 100), 25F, darkColor)
        RenderUtils.drawRect(33F, 18F, hpPos, 25F, color)
        font.drawString("❤", 33, 30, Color.RED.rgb)
        font.drawString(decimalFormat.format(getHealth(target)), 43, 30, Color.WHITE.rgb)
    }

    private fun drawLiquid(target: EntityLivingBase) {
        val width = (38 + target.name.let(Fonts.font40::getStringWidth))
            .coerceAtLeast(118)
            .toFloat()
        // Draw rect box
        RenderUtils.drawBorderedRect(0F, 0F, width, 36F, 3F, Color.BLACK.rgb, Color.BLACK.rgb)

        // Damage animation
        if (easingHP > getHealth(target)) {
            RenderUtils.drawRect(0F, 34F, (easingHP / target.maxHealth) * width,
                36F, Color(252, 185, 65).rgb)
        }

        // Health bar
        RenderUtils.drawRect(0F, 34F, (getHealth(target) / target.maxHealth) * width,
            36F, Color(252, 96, 66).rgb)

        // Heal animation
        if (easingHP < getHealth(target)) {
            RenderUtils.drawRect((easingHP / target.maxHealth) * width, 34F,
                (getHealth(target) / target.maxHealth) * width, 36F, Color(44, 201, 144).rgb)
        }

        target.name.let { Fonts.font40.drawString(it, 36, 3, 0xffffff) }
        Fonts.font35.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}", 36, 15, 0xffffff)

        // Draw info
        RenderUtils.drawHead(target.skin, 2, 2, 30, 30)
        val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
        if (playerInfo != null) {
            Fonts.font35.drawString("Ping: ${playerInfo.responseTime.coerceAtLeast(0)}",
                36, 24, 0xffffff)
        }
    }

    private fun drawZamorozka(target: EntityLivingBase) {
        val font = fontValue.get()

        // Frame
        RenderUtils.drawCircleRect(0f, 0f, 150f, 55f, 5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawRect(7f, 7f, 35f, 40f, Color(0, 0, 0, 70).rgb)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderUtils.drawEntityOnScreen(21, 38, 15, target)

        // Healthbar
        val barLength = 143 - 7f
        RenderUtils.drawCircleRect(7f, 45f, 143f, 50f, 2.5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawCircleRect(7f, 45f, 7 + ((easingHP / target.maxHealth) * barLength), 50f, 2.5f, ColorUtils.rainbowWithAlpha(90).rgb)
        RenderUtils.drawCircleRect(7f, 45f, 7 + ((target.health / target.maxHealth) * barLength), 50f, 2.5f, ColorUtils.rainbow().rgb)

        // Info
        RenderUtils.drawCircleRect(43f, 15f - font.FONT_HEIGHT, 143f, 17f, (font.FONT_HEIGHT + 1) * 0.45f, Color(0, 0, 0, 70).rgb)
        font.drawCenteredString("${target.name} ${if (target.ping != -1) { "§f${target.ping}ms" } else { "" }}", 93f, 16f - font.FONT_HEIGHT, ColorUtils.rainbow().rgb, false)
        font.drawString("Health: ${decimalFormat.format(easingHP)} §7/ ${decimalFormat.format(target.maxHealth)}", 43, 11 + font.FONT_HEIGHT, Color.WHITE.rgb)
        font.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}", 43, 11 + font.FONT_HEIGHT * 2, Color.WHITE.rgb)
    }

    private val riseParticleList = mutableListOf<RiseParticle>()

    private fun drawRise(target: EntityLivingBase) {
        val font = fontValue.get()

        RenderUtils.drawCircleRect(0f, 0f, 150f, 50f, 5f, Color(0, 0, 0, 130).rgb)

        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.2f * hurtPercent * 2)
        } else {
            0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
        }
        val size = 30

        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        RenderUtils.quickDrawHead(target.skin, 0, 0, size, size)
        GL11.glPopMatrix()

        font.drawString("Name ${target.name}", 40, 11, Color.WHITE.rgb)
        font.drawString("Distance ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))} Hurt ${target.hurtTime}", 40, 11 + font.FONT_HEIGHT, Color.WHITE.rgb)

        // 渐变血量条
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        val stopPos = (5 + ((135 - font.getStringWidth(decimalFormat.format(target.maxHealth))) * (easingHP / target.maxHealth))).toInt()
        for (i in 5..stopPos step 5) {
            val x1 = (i + 5).coerceAtMost(stopPos).toDouble()
            RenderUtils.quickDrawGradientSideways(i.toDouble(), 39.0, x1, 45.0,
                ColorUtils.hslRainbow(i, indexOffset = 10).rgb, ColorUtils.hslRainbow(x1.toInt(), indexOffset = 10).rgb)
        }
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        GL11.glColor4f(1f, 1f, 1f, 1f)

        font.drawString(decimalFormat.format(easingHP), stopPos + 5, 43 - font.FONT_HEIGHT / 2, Color.WHITE.rgb)

        if(target.hurtTime >= 9) {
            for(i in 0 until riseCountValue.get()) {
                riseParticleList.add(RiseParticle())
            }
        }

        val curTime = System.currentTimeMillis()
        riseParticleList.map { it }.forEach { rp ->
            if ((curTime - rp.time) > ((riseMoveTimeValue.get() + riseFadeTimeValue.get()) * 50)) {
                riseParticleList.remove(rp)
            }
            val movePercent = if((curTime - rp.time) < riseMoveTimeValue.get() * 50) {
                (curTime - rp.time) / (riseMoveTimeValue.get() * 50f)
            } else {
                1f
            }
            val x = (movePercent * rp.x * 0.5f * riseDistanceValue.get()) + 20
            val y = (movePercent * rp.y * 0.5f * riseDistanceValue.get()) + 20
            val alpha = if((curTime - rp.time) > riseMoveTimeValue.get() * 50) {
                1f - ((curTime - rp.time - riseMoveTimeValue.get() * 50) / (riseFadeTimeValue.get() * 50f)).coerceAtMost(1f)
            } else {
                1f
            } * riseAlphaValue.get()
            RenderUtils.drawCircle(x, y, riseSizeValue.get() * 2, Color(rp.colorR, rp.colorG, rp.colorB, (alpha * 255).toInt()).rgb)
        }
    }

    class RiseParticle {
        val colorR = RandomUtils.nextInt(0, 255)
        val colorG = RandomUtils.nextInt(0, 255)
        val colorB = RandomUtils.nextInt(0, 255)
        val alpha = RandomUtils.nextInt(150, 255)
        val time = System.currentTimeMillis()
        val x = RandomUtils.nextInt(-50, 50)
        val y = RandomUtils.nextInt(-50, 50)
    }

    private fun drawFlux(target: EntityLivingBase) {
        val width = (38 + target.name.let(Fonts.font40::getStringWidth))
            .coerceAtLeast(70)
            .toFloat()

        // draw background
        RenderUtils.drawRect(0F, 0F, width, 34F, Color(40, 40, 40).rgb)
        RenderUtils.drawRect(2F, 22F, width - 2F, 24F, Color.BLACK.rgb)
        RenderUtils.drawRect(2F, 28F, width - 2F, 30F, Color.BLACK.rgb)

        // draw bars
        RenderUtils.drawRect(2F, 22F, 2 + (easingHP / target.maxHealth) * (width - 4), 24F, Color(231, 182, 0).rgb)
        RenderUtils.drawRect(2F, 22F, 2 + (getHealth(target) / target.maxHealth) * (width - 4), 24F, Color(0, 224, 84).rgb)
        RenderUtils.drawRect(2F, 28F, 2 + (target.totalArmorValue / 20F) * (width - 4), 30F, Color(77, 128, 255).rgb)

        // draw text
        Fonts.font40.drawString(target.name, 22, 3, Color.WHITE.rgb)
        GL11.glPushMatrix()
        GL11.glScaled(0.7, 0.7, 0.7)
        Fonts.font35.drawString("Health: ${decimalFormat.format(getHealth(target))}", 22 / 0.7F, (4 + Fonts.font40.height) / 0.7F, Color.WHITE.rgb)
        GL11.glPopMatrix()

        // Draw head
        RenderUtils.drawHead(target.skin, 2, 2, 16, 16)
    }

    private fun drawArris(target: EntityLivingBase) {
        val font = fontValue.get()

        val hp = decimalFormat.format(easingHP)
        val additionalWidth = font.getStringWidth("${target.name}  ${hp} hp").coerceAtLeast(75)
        if(arrisRoundedValue.get()){
            RenderUtils.drawCircleRect(0f, 0f, 45f + additionalWidth, 40f, 7f, Color(0, 0, 0, 110).rgb)
        } else {
            RenderUtils.drawRect(0f, 0f, 45f + additionalWidth, 1f, ColorUtils.rainbow())
            RenderUtils.drawRect(0f, 1f, 45f + additionalWidth, 40f, Color(0, 0, 0, 110).rgb)
        }

        RenderUtils.quickDrawHead(target.skin, 5, 5, 30, 30)

        // info text
        font.drawString(target.name, 40, 5, Color.WHITE.rgb)
        "$hp hp".also {
            font.drawString(it, 40 + additionalWidth - font.getStringWidth(it), 5, Color.LIGHT_GRAY.rgb)
        }

        // hp bar
        val yPos = 5 + font.FONT_HEIGHT + 3f
        RenderUtils.drawRect(40f, yPos, 40 + (easingHP / target.maxHealth) * additionalWidth, yPos + 4, Color.GREEN.rgb)
        RenderUtils.drawRect(40f, yPos + 9, 40 + (target.totalArmorValue / 20F) * additionalWidth, yPos + 13, Color(77, 128, 255).rgb)
    }

    private fun drawTenacity(target: EntityLivingBase) {
        val font = fontValue.get()

        val additionalWidth = font.getStringWidth(target.name).coerceAtLeast(75)
        RenderUtils.drawCircleRect(0f, 0f, 45f + additionalWidth, 40f, 7f, Color(0, 0, 0, 110).rgb)

        // circle player avatar
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)

        // info text
        font.drawCenteredString(target.name, 40 + (additionalWidth / 2f), 5f, Color.WHITE.rgb, false)
        "${decimalFormat.format((easingHP / target.maxHealth) * 100)}%".also {
            font.drawString(it, (40f + (easingHP / target.maxHealth) * additionalWidth - font.getStringWidth(it)).coerceAtLeast(40f), 28f - font.FONT_HEIGHT, Color.WHITE.rgb, false)
        }

        // hp bar
        RenderUtils.drawCircleRect(40f, 28f, 40f + additionalWidth, 33f, 2.5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawCircleRect(40f, 28f, 40f + (easingHP / target.maxHealth) * additionalWidth, 33f, 2.5f, ColorUtils.rainbow().rgb)
    }

    private fun getTBorder(): Border? {
        return when (modeValue.get().lowercase()) {
            "novoline" -> Border(0F, 0F, 140F, 40F)
            "astolfo" -> Border(0F, 0F, 140F, 60F)
            "liquid" -> Border(0F, 0F, (38 + mc.thePlayer.name.let(Fonts.font40::getStringWidth)).coerceAtLeast(118).toFloat(), 36F)
            "flux" -> Border(0F, 0F, (38 + mc.thePlayer.name.let(Fonts.font40::getStringWidth))
                .coerceAtLeast(70)
                .toFloat(), 34F)
            "rise" -> Border(0F, 0F, 150F, 50F)
            "zamorozka" -> Border(0F, 0F, 150F, 55F)
            "arris" -> Border(0F, 0F, 120F, 40F)
            "tenacity" -> Border(0F, 0F, 120F, 40F)
            else -> null
        }
    }
}
