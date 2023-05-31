/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.utils.Particle
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.utils.ShapeType
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.interpolateColorC
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt


@ElementInfo(name = "Targets")
open class Targets : Element(-46.0, -40.0, 1F, Side(Side.Horizontal.MIDDLE, Side.Vertical.MIDDLE)) {

    // Style settings
    private val styleDisplay = BoolValue("Style Options:", true)
    val modeValue = ListValue("Mode", arrayOf("FDP", "Bar", "Chill", "ChillLite", "Vape", "Stitch", "Rice", "Slowly", "Remix", "Astolfo", "Astolfo2", "Liquid", "Flux", "Rise", "Exhibition", "ExhibitionOld", "Zamorozka", "Arris", "Tenacity", "Tenacity5", "WaterMelon", "SparklingWater"), "Slowly").displayable { styleDisplay.get() }
    private val modeRise = ListValue("RiseMode", arrayOf("Original", "New1", "New2", "Rise6"), "Rise6").displayable { modeValue.equals("Rise") }.displayable { styleDisplay.get() }
    private val chillFontSpeed = FloatValue("Chill-FontSpeed", 0.5F, 0.01F, 1F).displayable { modeValue.get().equals("chill", true) }.displayable { styleDisplay.get() }


    private var Health: Float = 0F
    private var EndingTarget: Entity? = null

    private val fontValue = FontValue("Font", Fonts.font40)

    val shadowValue = BoolValue("Shadow", false)
    val shadowStrength = FloatValue("Shadow-Strength", 1F, 0.01F, 40F).displayable { shadowValue.get() }
    val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Custom", "Bar"), "Background").displayable { shadowValue.get() }

    val shadowColorRedValue = IntegerValue("Shadow-Red", 0, 0, 255).displayable { shadowValue.get() && shadowColorMode.get().equals("custom", true) }
    val shadowColorGreenValue = IntegerValue("Shadow-Green", 111, 0, 255).displayable { shadowValue.get() && shadowColorMode.get().equals("custom", true) }
    val shadowColorBlueValue = IntegerValue("Shadow-Blue", 255, 0, 255).displayable { shadowValue.get() && shadowColorMode.get().equals("custom", true) }


    private val animSpeedValue = IntegerValue("AnimSpeed", 10, 5, 20)
    private val hpAnimTypeValue = EaseUtils.getEnumEasingList("HpAnimType")
    private val hpAnimOrderValue = EaseUtils.getEnumEasingOrderList("HpAnimOrder")

    private val switchModeValue = ListValue("SwitchMode", arrayOf("Slide", "Zoom", "None"), "Slide")
    private val switchAnimTypeValue = EaseUtils.getEnumEasingList("SwitchAnimType")
    private val switchAnimOrderValue = EaseUtils.getEnumEasingOrderList("SwitchAnimOrder")
    private val switchAnimSpeedValue = IntegerValue("SwitchAnimSpeed", 20, 5, 40)

    val noAnimValue = BoolValue("No-Animation", false)
    val globalAnimSpeed = FloatValue("Global-AnimSpeed", 3F, 1F, 9F).displayable { noAnimValue.equals("No-Animation") }

    private val arrisRoundedValue = BoolValue("ArrisRounded", true).displayable { modeValue.equals("Arris") }

    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "Slowly", "Fade", "Health"), "Health")
    private val redValue = IntegerValue("Red", 252, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val greenValue = IntegerValue("Green", 96, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val blueValue = IntegerValue("Blue", 66, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val saturationValue = FloatValue("Saturation", 1F, 0F, 1F)
    private val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F)

    private val bgRedValue = IntegerValue("Background-Red", 0, 0, 255)
    private val bgGreenValue = IntegerValue("Background-Green", 0, 0, 255)
    private val bgBlueValue = IntegerValue("Background-Blue", 0, 0, 255)
    private val bgAlphaValue = IntegerValue("Background-Alpha", 160, 0, 255)

    private val rainbowSpeed = IntegerValue("RainbowSpeed", 1, 1, 10)
    private val fadeValue = BoolValue("FadeAnim", false)
    private val fadeSpeed = FloatValue("Fade-Speed", 1F, 0F, 5F)
    private val waveSecondValue = IntegerValue("Seconds", 2, 1, 10)


    val riseHurtTime = BoolValue("RiseHurt", true).displayable { modeValue.equals("Rise") }
    val riseAlpha = IntegerValue("RiseAlpha", 130, 0, 255).displayable { modeValue.equals("Rise") }
    val riseCountValue = IntegerValue("Rise-Count", 5, 1, 20).displayable { modeValue.equals("Rise") }
    val riseSizeValue = FloatValue("Rise-Size", 1f, 0.5f, 3f).displayable { modeValue.equals("Rise") }
    val riseAlphaValue = FloatValue("Rise-Alpha", 0.7f, 0.1f, 1f).displayable { modeValue.equals("Rise") }
    val riseDistanceValue = FloatValue("Rise-Distance", 1f, 0.5f, 2f).displayable { modeValue.equals("Rise") }
    val riseMoveTimeValue = IntegerValue("Rise-MoveTime", 20, 5, 40).displayable { modeValue.equals("Rise") }
    val riseFadeTimeValue = IntegerValue("Rise-FadeTime", 20, 5, 40).displayable { modeValue.equals("Rise") }

    val gradientLoopValue = IntegerValue("GradientLoop", 4, 1, 40).displayable { modeValue.get().equals("Rice", true) }
    val gradientDistanceValue = IntegerValue("GradientDistance", 50, 1, 200).displayable { modeValue.get().equals("Rice", true) }
    val gradientRoundedBarValue = BoolValue("GradientRoundedBar", true).displayable { modeValue.get().equals("Rice", true) }

    val riceParticle = BoolValue("Rice-Particle", true).displayable { modeValue.get().equals("Rice", true) }
    val riceParticleSpin = BoolValue("Rice-ParticleSpin", true).displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }
    val generateAmountValue = IntegerValue("GenerateAmount", 10, 1, 40).displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }
    val riceParticleCircle = ListValue("Circle-Particles", arrayOf("Outline", "Solid", "None"), "Solid").displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }
    val riceParticleRect = ListValue("Rect-Particles", arrayOf("Outline", "Solid", "None"), "Outline").displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }
    val riceParticleTriangle = ListValue("Triangle-Particles", arrayOf("Outline", "Solid", "None"), "Outline").displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }

    val riceParticleSpeed = FloatValue("Rice-ParticleSpeed", 0.05F, 0.01F, 0.2F).displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }
    val riceParticleFade = BoolValue("Rice-ParticleFade", true).displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }
    val riceParticleFadingSpeed = FloatValue("ParticleFadingSpeed", 0.05F, 0.01F, 0.2F).displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }

    val particleRange = FloatValue("Rice-ParticleRange", 50f, 0f, 50f).displayable { modeValue.get().equals("Rice", true) && riceParticle.get() }
    val minParticleSize: FloatValue = object : FloatValue("MinParticleSize", 0.5f, 0f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxParticleSize.get()
            if (v < newValue) set(v)
        }
    }
    val maxParticleSize: FloatValue = object : FloatValue("MaxParticleSize", 2.5f, 0f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minParticleSize.get()
            if (v > newValue) set(v)
        }
    }


    var animProgress = 0F

    var easingHealth = 0F
    var barColor = Color(-1)
    var bgColor = Color(-1)

    private var prevTarget: EntityLivingBase? = null
    private var displayPercent = 0f
    private var lastUpdate = System.currentTimeMillis()

    private val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))
    private val decimalFormat2 = DecimalFormat("##0.0", DecimalFormatSymbols(Locale.ENGLISH))
    private val decimalFormat3 = DecimalFormat("0.#", DecimalFormatSymbols(Locale.ENGLISH))
    private val decimalFormat4 = DecimalFormat("0.0#", DecimalFormatSymbols(Locale.ENGLISH))
    private val ndecimalFormat = DecimalFormat("#", DecimalFormatSymbols(Locale.ENGLISH))

    val shadowOpaque: Color
        get() = ColorUtils.reAlpha(when (shadowColorMode.get().lowercase(Locale.getDefault())) {
            "background" -> bgColor
            "custom" -> Color(shadowColorRedValue.get(), shadowColorGreenValue.get(), shadowColorBlueValue.get())
            else -> barColor
        }, 1F - animProgress)

    val particleList = mutableListOf<Particle>()
    private var gotDamaged = false

    private var progress: Float = 0F

    private var hpEaseAnimation: Animation? = null
    private var pastHP = 0f
    private var easingHP = 0f
    private var ease = 0f
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

    private val numberRenderer = CharRenderer(false)

    private var calcScaleX = 0F
    private var calcScaleY = 0F
    private var calcTranslateX = 0F
    private var calcTranslateY = 0F

    fun updateData(_a: Float, _b: Float, _c: Float, _d: Float) {
        calcTranslateX = _a
        calcTranslateY = _b
        calcScaleX = _c
        calcScaleY = _d
    }

    private fun getHealth(entity: EntityLivingBase?): Float {
        return entity?.health ?: 0f
    }

    override fun drawElement(partialTicks: Float): Border? {
        var target = FDPClient.combatManager.target
        val time = System.currentTimeMillis()
        val pct = (time - lastUpdate) / (switchAnimSpeedValue.get() * 50f)
        lastUpdate = System.currentTimeMillis()

        if (mc.currentScreen is GuiHudDesigner) {
            target = mc.thePlayer
        }
        if (target != null) {
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



        if (hpEaseAnimation != null) {
            easingHP = hpEaseAnimation!!.value.toFloat()
            if (hpEaseAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                hpEaseAnimation = null
            }
        }

        if (hpEaseAnimation == null || (hpEaseAnimation!!.to != getHealth(target).toDouble())) {
            hpEaseAnimation = Animation(EaseUtils.EnumEasingType.valueOf(hpAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(hpAnimOrderValue.get()), pastHP.toDouble(), getHealth(target).toDouble(), animSpeedValue.get() * 100L).start()
        }

        pastHP = getHealth(target)

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

        val preBarColor = when (colorModeValue.get()) {
            "Rainbow" -> Color(ColorUtils.hslRainbow(100 * rainbowSpeed.get()).rgb)
            "Custom" -> Color(redValue.get(), greenValue.get(), blueValue.get())
            "Sky" -> ColorUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble())
            "Fade" -> ColorUtils.fade(Color(redValue.get(), greenValue.get(), blueValue.get()), 0, 100)
            "Health" -> if (target != null) BlendUtils.getHealthColor(target.health, target.maxHealth) else Color.green
            else -> ColorUtils.slowlyRainbow(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())!!
        }

        progress += 0.0025F * RenderUtils.deltaTime * if (target != null) -1F else 1F

        val preBgColor = Color(bgRedValue.get(), bgGreenValue.get(), bgBlueValue.get(), bgAlphaValue.get())

        if (fadeValue.get())
            animProgress += (0.0075F * fadeSpeed.get() * RenderUtils.deltaTime)
        else animProgress = 0F

        animProgress = animProgress.coerceIn(0F, 1F)

        barColor = ColorUtils.reAlpha(preBarColor, preBarColor.alpha / 255F * (1F - animProgress))
        bgColor = ColorUtils.reAlpha(preBgColor, preBgColor.alpha / 255F * (1F - animProgress))


        val calcScaleX = animProgress * (4F / 2F)
        val calcScaleY = animProgress * (4F / 2F)
        val calcTranslateX =  2F * calcScaleX
        val calcTranslateY = 2F * calcScaleY


        if (fadeValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
            GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
        }

        if (fadeValue.get())
            GL11.glPopMatrix()

        GlStateManager.resetColor()

        fun handleDamage(ent: EntityPlayer) {
            if (target != null && ent == target)
                (modeValue.get())
        }

        fun getFadeProgress() = animProgress


        when (modeValue.get().lowercase()) {
            "fdp" -> drawFDP(prevTarget!!)
            "astolfo" -> drawAstolfo(prevTarget!!)
            "astolfo2" -> drawAstolfo2(prevTarget!! as EntityPlayer)
            "liquid" -> drawLiquid(prevTarget!!)
            "flux" -> drawFlux(prevTarget!!)
            "rise" -> {
                when (modeRise.get().lowercase()) {
                    "original" -> drawRise(prevTarget!!)
                    "new1" -> drawRiseNew(prevTarget!!)
                    "new2" -> drawRiseNewNew(prevTarget!!)
                    "rise6" -> drawRiseLatest(prevTarget!!)
                }
            }

            "zamorozka" -> drawZamorozka(prevTarget!!)
            "arris" -> drawArris(prevTarget!!)
            "tenacity" -> drawTenacity(prevTarget!!)
            "tenacity5" -> drawTenacity5(prevTarget!!)
            "chill" -> drawChill(prevTarget!! as EntityPlayer)
            "chilllite" -> drawChillLite(prevTarget!! as EntityPlayer)
            "stitch" -> drawStitch(prevTarget!!)
            "remix" -> drawRemix(prevTarget!! as EntityPlayer)
            "rice" -> drawRice(prevTarget!!)
            "vape" -> drawVape(prevTarget!!)
            "slowly" -> drawSlowly(prevTarget!!)
            "watermelon" -> drawWaterMelon(prevTarget!!)
            "sparklingwater" -> drawSparklingWater(prevTarget!!)
            "exhibition" -> drawExhibition(prevTarget!! as EntityPlayer)
            "exhibitionold" -> drawExhibitionOld(prevTarget!! as EntityPlayer)
            "bar" -> drawBar(prevTarget!!)
        }

        return getTBorder()

    }

    private fun drawVape(target: EntityLivingBase) {

        RenderUtils.drawEntityOnScreen(16, 55, 25, target)

        Fonts.fontTenacityBold35.drawString(target.name, 36.5f, 12.6f / 2f - Fonts.fontTenacityBold35.height / 2f, -1)

        val targetHealth = target.health
        val targetMaxHealth = target.maxHealth
        val targetAbsorptionAmount = target.absorptionAmount
        val targetHealthDWithAbs = targetHealth / (targetMaxHealth + targetAbsorptionAmount).coerceAtLeast(1.0f)
        val targetHealthD = targetHealth / targetMaxHealth.coerceAtLeast(1.0f)
        val color: Color? = interpolateColorC(Color.RED, Color(5, 134, 105), targetHealthD)

        RoundedUtil.drawRound(37f, 12.6f, 68f, 2.9f, 1f, Color(43, 42, 43))
        RoundedUtil.drawRound(37f, 12.6f, 68f * targetHealthDWithAbs, 2.9f, 1f, color)
        if (targetAbsorptionAmount > 0) {
            val absLength = 49f * (targetAbsorptionAmount / (targetMaxHealth + targetAbsorptionAmount))
            RoundedUtil.drawRound(37f + 68f * targetHealthDWithAbs,
                12.6f,
                absLength,
                2.9f,
                1f,
                Color(0xFFAA00))
        }

        val hp = (targetHealth + targetAbsorptionAmount).toString() + "  HP"
        Fonts.fontTenacityBold35.drawString(hp,
            105f - Fonts.fontTenacityBold35.getStringWidth(hp),
            (12.6f - Fonts.fontTenacityBold35.height) / 2f,
            -1)
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
        if (shadowValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                RenderUtils.drawRect(0F, 0F, 140F, 60F, Color(0, 0, 0).rgb)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                RenderUtils.drawRect(0F, 0F, 140F, 60F, Color(0, 0, 0).rgb)
                GL11.glPopMatrix()
            })
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
    }
    private fun drawAstolfo2(entity: EntityPlayer) {
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

        RenderUtils.drawRect(0F, 0F, 160F, 60F, bgColor.rgb)

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)


        Fonts.minecraftFont.drawString(entity.name, 32F, 5F, -1, true)
        GL11.glPushMatrix()
        GL11.glTranslatef(32F, 20F, 32F)
        GL11.glScalef(2F, 2F, 2F)
        Fonts.minecraftFont.drawString("${decimalFormat3.format(entity.health)} ❤", 0, 0, barColor.rgb);
        GL11.glPopMatrix()

        RenderUtils.drawRect(32F, 48F, 32F + 122F, 55F, barColor.darker().rgb)
        RenderUtils.drawRect(32F, 48F, 32F + (easingHealth / entity.maxHealth).toFloat() * 122F, 55F, barColor.rgb)

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
        if (shadowValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                val width = (38 + Fonts.font40.getStringWidth(target.name)).coerceAtLeast(118).toFloat()
                RenderUtils.newDrawRect(0F, 0F, width, 36F, shadowOpaque.rgb)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                val width = (38 + Fonts.font40.getStringWidth(target.name)).coerceAtLeast(118).toFloat()
                RenderUtils.newDrawRect(0F, 0F, width, 36F, shadowOpaque.rgb)
                GL11.glPopMatrix()
            })
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
    }

    private fun drawZamorozka(target: EntityLivingBase) {
        val font = fontValue.get()

        // Frame
        RenderUtils.drawRoundedCornerRect(0f, 0f, 150f, 55f, 5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawRect(7f, 7f, 35f, 40f, Color(0, 0, 0, 70).rgb)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderUtils.drawEntityOnScreen(21, 38, 15, target)

        // Healthbar
        val barLength = 143 - 7f
        RenderUtils.drawRoundedCornerRect(7f, 45f, 143f, 50f, 2.5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawRoundedCornerRect(7f, 45f, 7 + ((easingHP / target.maxHealth) * barLength), 50f, 2.5f, ColorUtils.rainbowWithAlpha(90).rgb)
        RenderUtils.drawRoundedCornerRect(7f, 45f, 7 + ((target.health / target.maxHealth) * barLength), 50f, 2.5f, ColorUtils.rainbow().rgb)

        // Info
        RenderUtils.drawRoundedCornerRect(43f, 15f - font.FONT_HEIGHT, 143f, 17f, (font.FONT_HEIGHT + 1) * 0.45f, Color(0, 0, 0, 70).rgb)
        font.drawCenteredString("${target.name} ${if (target.ping != -1) { "§f${target.ping}ms" } else { "" }}", 93f, 16f - font.FONT_HEIGHT, ColorUtils.rainbow().rgb, false)
        font.drawString("Health: ${decimalFormat.format(easingHP)} §7/ ${decimalFormat.format(target.maxHealth)}", 43, 11 + font.FONT_HEIGHT, Color.WHITE.rgb)
        font.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}", 43, 11 + font.FONT_HEIGHT * 2, Color.WHITE.rgb)
    }

    private val riseParticleList = mutableListOf<RiseParticle>()

    private fun drawRise(target: EntityLivingBase) {
        val font = fontValue.get()

        RenderUtils.drawRoundedCornerRect(0f, 0f, 150f, 50f, 5f, Color(0, 0, 0, riseAlpha.get()).rgb)

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
        if (riseHurtTime.get()) {
            font.drawString("Distance ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))} Hurt ${target.hurtTime}", 40, 11 + font.FONT_HEIGHT, Color.WHITE.rgb)
        } else {
            font.drawString("Distance ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}", 40, 11 + font.FONT_HEIGHT, Color.WHITE.rgb)
        }

        // 渐变血量条
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        val stopPos = (5 + ((135 - font.getStringWidth(decimalFormat.format(target.maxHealth))) * (easingHP / target.maxHealth))).toInt()
        for (i in 5..stopPos step 5) {
            val x1 = (i + 5).coerceAtMost(stopPos).toDouble()
            RenderUtils.quickDrawGradientSidewaysH(i.toDouble(), 39.0, x1, 45.0,
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
            RenderUtils.drawCircle(x, y, riseSizeValue.get() * 2, Color(rp.color.red, rp.color.green, rp.color.blue, (alpha * 255).toInt()).rgb)
        }
    }

    private fun drawRiseNew(target: EntityLivingBase) {
        val font = fontValue.get()

        RenderUtils.drawRoundedCornerRect(0f, 0f, 150f, 50f, 5f, Color(0, 0, 0, riseAlpha.get()).rgb)

        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.2f * hurtPercent * 2)
        } else {
            0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
        }
        val size = 38

        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 7f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        RenderUtils.quickDrawHead(target.skin, 0, 0, size, size)
        GL11.glPopMatrix()

        font.drawString("${target.name}", 48, 8, Color.WHITE.rgb)

        // 渐变血量条
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        val stopPos = 48 + ( (easingHP/ target.maxHealth) * 97f).toInt()
        for (i in 48..stopPos step 5) {
            val x1 = (i + 5).coerceAtMost(stopPos).toDouble()
            RenderUtils.quickDrawGradientSidewaysH(i.toDouble(), (13 + font.FONT_HEIGHT).toDouble(), x1, 45.0,
                ColorUtils.hslRainbow(i, indexOffset = 10).rgb, ColorUtils.hslRainbow(x1.toInt(), indexOffset = 10).rgb)
        }
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        GL11.glColor4f(1f, 1f, 1f, 1f)

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
            RenderUtils.drawCircle(x, y, riseSizeValue.get() * 2, Color(rp.color.red, rp.color.green, rp.color.blue, (alpha * 255).toInt()).rgb)
        }
    }

    private fun drawRiseNewNew(target: EntityLivingBase) {
        val font = fontValue.get()

        val additionalWidth = font.getStringWidth(target.name).coerceAtLeast(60)*1.65f + font.getStringWidth("00")
        RenderUtils.drawRoundedCornerRect(0f, 0f, 45f + additionalWidth, 45f, 7f, Color(0, 0, 0, riseAlpha.get()).rgb)

        // circle player avatar
        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.2f * hurtPercent * 2)
        } else {
            0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
        }
        val size = 30

        //draw head
        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)
        GL11.glPopMatrix()

        // draw name
        GL11.glPushMatrix()
        GL11.glScalef(1.5f, 1.5f, 1.5f)
        font.drawString("${target.name}", 32, 8, Color.WHITE.rgb)
        GL11.glPopMatrix()

        // draw health
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        val stopPos = (48 + ((additionalWidth - 5 - font.getStringWidth(decimalFormat.format(target.maxHealth))) * (easingHP / target.maxHealth))).toInt()
        for (i in 48..stopPos step 5) {
            val x1 = (i + 5).coerceAtMost(stopPos).toDouble()
            RenderUtils.quickDrawGradientSidewaysH(i.toDouble(), 30.0, x1, 38.0,
                ColorUtils.hslRainbow(i, indexOffset = 10).rgb, ColorUtils.hslRainbow(x1.toInt(), indexOffset = 10).rgb)
        }
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        font.drawString(decimalFormat.format(easingHP), stopPos + 5, 36 - font.FONT_HEIGHT / 2, Color.WHITE.rgb)


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
            RenderUtils.drawCircle(x, y, riseSizeValue.get() * 2, Color(rp.color.red, rp.color.green, rp.color.blue, (alpha * 255).toInt()).rgb)
        }
    }

    private fun drawRiseLatest(target: EntityLivingBase) {
        val font = fontValue.get()

        val additionalWidth = ((font.getStringWidth(target.name) * 1.1).toInt().coerceAtLeast(70) + font.getStringWidth("Name: ") * 1.1 + 7.0).roundToInt()
        val healthBarWidth = additionalWidth - (font.getStringWidth("20") * 1.15).roundToInt() - 16
        RenderUtils.drawRoundedCornerRect(0f, 0f, 50f + additionalWidth, 50f, 7f, Color(0, 0, 0, 130).rgb)
        //RenderUtils.drawShadow(2f, 2f, 48f + additionalWidth, 48f)
        if (shadowValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                RenderUtils.drawRoundedCornerRect(0f, 0f, 50f + additionalWidth, 50f, 7f, Color(0, 0, 0, 130).rgb)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                RenderUtils.drawRoundedCornerRect(0f, 0f, 50f + additionalWidth, 50f, 7f, Color(0, 0, 0, 130).rgb)
                GL11.glPopMatrix()
            })
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        // circle player avatar
        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.2f * hurtPercent * 2)
        } else {
            0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
        }
        val size = 45

        //draw head
        GL11.glPushMatrix()
        GL11.glTranslatef(7f, 7f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        Stencil.write(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtils.fastRoundedRect(4F, 4F, 34F, 34F, 8F)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        Stencil.erase(true)
        drawHead(target.skin, 4, 4, 30, 30, 1F - getFadeProgress()) //playerInfo.locationSkin
        Stencil.dispose()
        GL11.glPopMatrix()

        // draw name
        GL11.glPushMatrix()
        GL11.glScalef(1.1f, 1.1f, 1.1f)
        font.drawString("Name: ${target.name}", 45, 14, Color(115, 208, 255, 255).rgb)
        font.drawString("Name:", 45, 14, Color.WHITE.rgb)
        GL11.glPopMatrix()

        // draw health
        RenderUtils.drawRoundedCornerRect(50f, 31f, 50f + healthBarWidth , 39f, 3f, Color(20, 20, 20, 255).rgb)
        RenderUtils.drawRoundedCornerRect(50f, 31f, 50f + (healthBarWidth * (easingHP / target.maxHealth)) , 39f, 4f, Color(122, 214, 255, 255).rgb)
        RenderUtils.drawRoundedCornerRect(52f, 31f, 48f + (healthBarWidth * (easingHP / target.maxHealth)) , 34f, 2f, Color(255, 255, 255, 30).rgb)
        RenderUtils.drawRoundedCornerRect(52f, 36f, 48f + (healthBarWidth * (easingHP / target.maxHealth)) , 39f, 2f, Color(0, 0, 0, 30).rgb)
        GL11.glPushMatrix()
        GL11.glScalef(1.15f, 1.15f, 1.15f)
        font.drawString(getHealth(target).roundToInt().toString(), ((38 + additionalWidth.toInt() - font.getStringWidth((getHealth(target) * 1.15).roundToInt().toString()).toInt()) / 1.15).roundToInt()   , 31 - (font.FONT_HEIGHT/2).toInt(), Color(115, 208, 255, 255).rgb)
        GL11.glPopMatrix()


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
            RenderUtils.drawCircle(x, y, riseSizeValue.get() * 2, Color(rp.color.red, rp.color.green, rp.color.blue, (alpha * 255).toInt()).rgb)
        }
    }

    class RiseParticle {
        val color = ColorUtils.rainbow(RandomUtils.nextInt(0, 30))
        val alpha = RandomUtils.nextInt(150, 255)
        val time = System.currentTimeMillis()
        val x = RandomUtils.nextInt(-50, 50)
        val y = RandomUtils.nextInt(-50, 50)
    }

    private fun drawBar(target: EntityLivingBase) {
        Health = easingHP

        val width = (38 + Fonts.font40.getStringWidth(target.name))
            .coerceAtLeast(119)
            .toFloat()

        RenderUtils.drawBorderedRect(3F, 37F, 115F, 42F, 4.2F, Color(16, 16, 16, 255).rgb, Color(10, 10, 10, 100).rgb)
        RenderUtils.drawBorderedRect(3F, 37F, 115F, 42F, 1.2F, Color(255, 255, 255, 180).rgb, Color(255, 180, 255, 0).rgb)
        if (Health > getHealth(target))
            RenderUtils.drawRect(3F, 37F, (Health / target.maxHealth) * width - 4F,
                42F, Color(250, 0, 0, 120).rgb)

        RenderUtils.drawRect(3.2F, 37F, (getHealth(target) / target.maxHealth) * width - 4F,
            42F, Color(220, 0, 0, 220).rgb)
        if (Health < target.health)
            RenderUtils.drawRect((Health / target.maxHealth) * width, 37F,
                (getHealth(target) / target.maxHealth) * width, 42F, Color(44, 201, 144).rgb)
        RenderUtils.drawBorderedRect(3F, 37F, 115F, 42F, 1.2F, Color(255, 255, 255, 180).rgb, Color(255, 180, 255, 0).rgb)



        mc.fontRendererObj.drawStringWithShadow(target.name.toString(), 36F, 22F, 0xFFFFFF)
        if (shadowValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                mc.fontRendererObj.drawStringWithShadow(target.name.toString(), 36F, 22F, 0xFFFFFF)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                mc.fontRendererObj.drawStringWithShadow(target.name.toString(), 36F, 22F, 0xFFFFFF)
                GL11.glPopMatrix()
            })
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
    }

    private fun drawFDP(target: EntityLivingBase) {
        val font = fontValue.get()
        val addedLen = (60 + font.getStringWidth(target.name) * 1.60f).toFloat()

        RenderUtils.drawRect(0f, 0f, addedLen, 47f, Color(0, 0, 0, 120).rgb)
        RenderUtils.drawRoundedCornerRect(0f, 0f, (easingHP / target.maxHealth) * addedLen, 47f, 3f, Color(0, 0, 0, 90).rgb)

        RenderUtils.drawShadow(0f, 0f, addedLen, 47f)

        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35

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

        GL11.glPushMatrix()
        GL11.glScalef(1.5f, 1.5f, 1.5f)
        font.drawString(target.name, 39, 8, Color.WHITE.rgb)
        GL11.glPopMatrix()
        font.drawString("Health ${getHealth(target).roundToInt()}", 56, 12 + (font.FONT_HEIGHT * 1.5).toInt(), Color.WHITE.rgb)

    }

    private fun drawExhibition(entity: EntityPlayer) {
        val font = Fonts.fontTahoma
        val minWidth = 126F.coerceAtLeast(47F + font.getStringWidth(entity.name))

        RenderUtils.drawExhiRect(0F, 0F, minWidth, 45F, 1F - getFadeProgress())

        RenderUtils.drawRect(2.5F, 2.5F, 42.5F, 42.5F, getColor(Color(59, 59, 59)).rgb)
        RenderUtils.drawRect(3F, 3F, 42F, 42F, getColor(Color(19, 19, 19)).rgb)

        GL11.glColor4f(1f, 1f, 1f, 1f - getFadeProgress())
        RenderUtils.drawEntityOnScreen(22, 40, 16, entity)

        font.drawString(entity.name, 46, 5, getColor(-1).rgb)

        val barLength = 70F * (entity.health / entity.maxHealth).coerceIn(0F, 1F)
        RenderUtils.drawRect(45F, 14F, 45F + 70F, 18F, getColor(BlendUtils.getHealthColor(entity.health, entity.maxHealth).darker(0.3F)).rgb)
        RenderUtils.drawRect(45F, 14F, 45F + barLength, 18F, getColor(BlendUtils.getHealthColor(entity.health, entity.maxHealth)).rgb)

        for (i in 0..9)
            RenderUtils.drawRectBasedBorder(45F + i * 7F, 14F, 45F + (i + 1) * 7F, 18F, 0.5F, getColor(Color.black).rgb)

        Fonts.fontTahomaSmall.drawString("HP:${entity.health.toInt()} | Dist:${mc.thePlayer.getDistanceToEntityBox(entity).toInt()}", 45F, 21F, getColor(-1).rgb)

        GlStateManager.resetColor()
        GL11.glPushMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f - getFadeProgress())
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()

        val renderItem = mc.renderItem

        var x = 45
        var y = 28

        for (index in 3 downTo 0) {
            val stack = entity.inventory.armorInventory[index] ?: continue

            if (stack.item == null)
                continue

            renderItem.renderItemIntoGUI(stack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
            RenderUtils.drawExhiEnchants(stack, x.toFloat(), y.toFloat())

            x += 16
        }

        val mainStack = entity.heldItem
        if (mainStack != null && mainStack.item != null) {
            renderItem.renderItemIntoGUI(mainStack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
            RenderUtils.drawExhiEnchants(mainStack, x.toFloat(), y.toFloat())
        }

        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()
    }

    private fun drawExhibitionOld(entity: EntityPlayer) {
        val font = Fonts.minecraftFont
        val minWidth = 126F.coerceAtLeast(47F + font.getStringWidth(entity.name))

        RenderUtils.drawRect(5F, 2F, minWidth - 5f, 41F, Color(0, 0, 0, 170).rgb)
        // RenderUtils.drawRect(3F, 3F, 42F, 42F, getColor(Color(19, 19, 19)).rgb)

        GL11.glColor4f(1f, 1f, 1f, 1f - getFadeProgress())
        RenderUtils.drawEntityOnScreen(22, 40, 16, entity)

        Fonts.minecraftFont.drawStringWithShadow(entity.name, 46f, 5f, getColor(-1).rgb)

        val barLength = 70F * (entity.health / entity.maxHealth).coerceIn(0F, 1F)
        RenderUtils.drawRect(45F, 14F, 45F + 70F, 18F, getColor(BlendUtils.getHealthColor(entity.health, entity.maxHealth).darker(0.3F)).rgb)
        RenderUtils.drawRect(45F, 14F, 45F + barLength, 18F, getColor(BlendUtils.getHealthColor(entity.health, entity.maxHealth)).rgb)

        for (i in 0..9)
            RenderUtils.drawRectBasedBorder(45F + i * 7F, 14F, 45F + (i + 1) * 7F, 18F, 0.5F, Color(20, 20, 20, 200).rgb)

        Fonts.fontTahomaSmall.drawString("HP:${entity.health.toInt()} | Dist:${mc.thePlayer.getDistanceToEntityBox(entity).toInt()}", 45F, 21F, getColor(-1).rgb)

        GlStateManager.resetColor()
        GL11.glPushMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f - getFadeProgress())
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()

        val renderItem = mc.renderItem

        var x = 40
        var y = 25

        for (index in 3 downTo 0) {
            val stack = entity.inventory.armorInventory[index] ?: continue

            if (stack.item == null)
                continue

            renderItem.renderItemIntoGUI(stack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
            RenderUtils.drawExhiEnchants(stack, x.toFloat(), y.toFloat())

            x += 16
        }

        val mainStack = entity.heldItem
        if (mainStack != null && mainStack.item != null) {
            renderItem.renderItemIntoGUI(mainStack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
            RenderUtils.drawExhiEnchants(mainStack, x.toFloat(), y.toFloat())
        }

        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()

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
        val additionalWidth = font.getStringWidth("${target.name}  $hp hp").coerceAtLeast(75)
        if(arrisRoundedValue.get()){
            RenderUtils.drawRoundedCornerRect(0f, 0f, 45f + additionalWidth, 40f, 7f, Color(0, 0, 0, 110).rgb)
        } else {
            RenderUtils.drawRect(0f, 0f, 45f + additionalWidth, 1f, ColorUtils.rainbow())
            RenderUtils.drawRect(0f, 1f, 45f + additionalWidth, 40f, Color(0, 0, 0, 110).rgb)
        }

        RenderUtils.drawHead(target.skin, 5, 5, 30, 30)

        // info text
        font.drawString(target.name, 40, 5, Color.WHITE.rgb)
        "$hp hp".also {
            font.drawString(it, 40 + additionalWidth - font.getStringWidth(it), 5, Color.LIGHT_GRAY.rgb)
        }

        // hp bar
        val yPos = 5 + font.FONT_HEIGHT + 3f
        RenderUtils.drawRect(40f, yPos, 40 + (easingHP / target.maxHealth) * additionalWidth, yPos + 4, Color.GREEN.rgb)
        RenderUtils.drawRect(40f, yPos + 9, 40 + (target.totalArmorValue / 20F) * additionalWidth, yPos + 13, Color(77, 128, 255).rgb)
        if (shadowValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()

            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                // the part to blur for the epic glow fr
                RenderUtils.drawRoundedCornerRect(0f, 0f, 45f + additionalWidth, 40f, 7f, Color(0, 0, 0, 110).rgb)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                // the part to cut
                RenderUtils.drawRoundedCornerRect(0f, 0f, 45f + additionalWidth, 40f, 7f, Color(0, 0, 0, 110).rgb)
                GL11.glPopMatrix()
            })

            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
    }

    private fun drawTenacity(target: EntityLivingBase) {
            val font = fontValue.get()

        val additionalWidth = font.getStringWidth(target.name).coerceAtLeast(75)
        RenderUtils.drawRoundedCornerRect(0f, 0f, 45f + additionalWidth, 40f, 7f, Color(0, 0, 0, 110).rgb)

        // circle player avatar
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)

        // info text
        font.drawCenteredString(target.name, 40 + (additionalWidth / 2f), 5f, Color.WHITE.rgb, false)
        "${decimalFormat.format((easingHP / target.maxHealth) * 100)}%".also {
            font.drawString(it, (40f + (easingHP / target.maxHealth) * additionalWidth - font.getStringWidth(it)).coerceAtLeast(40f), 28f - font.FONT_HEIGHT, Color.WHITE.rgb, false)
        }

        // hp bar
        RenderUtils.drawRoundedCornerRect(40f, 28f, 40f + additionalWidth, 33f, 2.5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawRoundedCornerRect(40f, 28f, 40f + (easingHP / target.maxHealth) * additionalWidth, 33f, 2.5f, ColorUtils.rainbow().rgb)
    }

    private fun drawTenacity5(target: EntityLivingBase) {
        val additionalWidth = Fonts.fontTenacityBold40.getStringWidth(target.name).coerceAtLeast(75)

        //colors
        val c1 = ColorUtils.interpolateColorsBackAndForth(17, 0, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c2 = ColorUtils.interpolateColorsBackAndForth(17, 90, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c3 = ColorUtils.interpolateColorsBackAndForth(17, 270, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c4 = ColorUtils.interpolateColorsBackAndForth(17, 180, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);

        // glow
        GL11.glTranslated(-renderX * scale, -renderY * scale, 0.0)
        GL11.glPushMatrix()
        ShadowUtils.shadow(8F, { GL11.glPushMatrix(); GL11.glTranslated(renderX * scale, renderY * scale, 0.0); RoundedUtil.drawGradientRound(0f * scale, 5f * scale, 59f + additionalWidth.toFloat() * scale, 45f * scale, 6F, c1, c2, c3, c4); GL11.glPopMatrix(); }, {})
        GL11.glPopMatrix()
        GL11.glTranslated(renderX * scale, renderY * scale, 0.0)

        // background
        RoundedUtil.drawGradientRound(0f, 5f, 59f + additionalWidth.toFloat(), 45f, 6F, c1, c2, c3, c4);

        // circle player avatar
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 7, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 7, 40f, 8f, 8, 8, 30, 30, 64f, 64f)
        GL11.glPopMatrix()

        // text
        Fonts.fontTenacityBold40.drawCenteredString(target.name, 47 + (additionalWidth / 2f), 1f + Fonts.fontTenacityBold40.FONT_HEIGHT, Color.WHITE.rgb, false)
        val infoStr = ((((easingHP / target.maxHealth) * 100).roundToInt()).toString() + "% - " + ((mc.thePlayer.getDistanceToEntityBox(target)).roundToInt()).toString() + "M")
        Fonts.fontTenacity40.drawString(infoStr, 47f + ((additionalWidth - Fonts.fontTenacity40.getStringWidth(infoStr)) / 2f), 45f - (Fonts.fontTenacity40.FONT_HEIGHT), Color.WHITE.rgb, false)

        //hp bar
        RenderUtils.drawRoundedCornerRect(46f, 24f, 46f + additionalWidth, 29f, 2.5f, Color(60, 60, 60, 130).rgb)
        RenderUtils.drawRoundedCornerRect(46f, 24f, 46f + (easingHP / target.maxHealth) * additionalWidth, 29f, 2.5f, Color(240, 240, 240, 250).rgb)
    }

    private fun drawChill(entity: EntityPlayer) {
        updateAnim(entity.health)

        val name = entity.name
        val health = entity.health
        val tWidth = (45F + Fonts.font40.getStringWidth(name).coerceAtLeast(Fonts.font72.getStringWidth(decimalFormat.format(health)))).coerceAtLeast(150F)

        val reColorBg = Color(bgColor.red / 255.0F, bgColor.green / 255.0F, bgColor.blue / 255.0F, bgColor.alpha / 255.0F * 1F)
        val reColorBar = Color(barColor.red / 255.0F, barColor.green / 255.0F, barColor.blue / 255.0F, barColor.alpha / 255.0F * 1F)
        val reColorText = Color(1F, 1F, 1F, 1F)

        val floatX = renderX.toFloat()
        val floatY = renderY.toFloat()

        // background
        RenderUtils.drawRoundedRect(0F, 0F, tWidth, 48F, 7F, reColorBg.rgb)
        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        // circle player avatar
        val hurtPercent = entity.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.2f * hurtPercent * 2)
        } else {
            0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
        }
        val size = 45

        //draw head
        GL11.glPushMatrix()
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        Stencil.write(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtils.fastRoundedRect(4F, 4F, 34F, 34F, 7F)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        Stencil.erase(true)
        drawHead(entity.skin, 4, 4, 30, 30, 1F - getFadeProgress())
        Stencil.dispose()
        GL11.glPopMatrix()

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        // name + health
        Fonts.font40.drawString(name, 38F, 6F, reColorText.rgb, false)
        numberRenderer.renderChar(
            health,
            floatX,
            floatY,
            38F,
            17F,
            0F,
            0F,
            false,
            chillFontSpeed.get(),
            reColorText.rgb
        )

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        // health bar
        RenderUtils.drawRoundedRect(4F, 38F, tWidth - 4F, 44F, 3F, reColorBar.darker().darker().darker().rgb)
        Stencil.write(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtils.fastRoundedRect(4F, 38F, tWidth - 4F, 44F, 3F)
        GL11.glDisable(GL11.GL_BLEND)
        Stencil.erase(true)
        RenderUtils.drawRect(4F, 38F, 4F + (easingHealth / entity.maxHealth) * (tWidth - 8F), 44F, reColorBar.rgb)
        Stencil.dispose()

        GL11.glScalef(1F, 1F, 1F)
        GL11.glTranslated(renderX, renderY, 0.0)
    }

    private fun drawChillLite(entity: EntityPlayer) {
        updateAnim(entity.health)

        val name = entity.name
        val health = entity.health
        val tWidth = (45F + Fonts.font40.getStringWidth(name).coerceAtLeast(Fonts.font40.getStringWidth(decimalFormat.format(health)))).coerceAtLeast(90F)
        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)

        // background
        RenderUtils.drawRoundedRect(0F, 0F, tWidth, 38F, 7F, bgColor.rgb)
        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        // head
        if (playerInfo != null) {
            Stencil.write(false)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            RenderUtils.fastRoundedRect(4F, 4F, 34F, 34F, 7F)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            Stencil.erase(true)
            drawHead(playerInfo.locationSkin, 4, 4, 30, 30, 1F - getFadeProgress())
            Stencil.dispose()
        }

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        // name + health
        Fonts.font40.drawString(name, 38F, 6F, getColor(-1).rgb)
        numberRenderer.renderChar(health, renderX.toFloat(), renderY.toFloat(), 38F, 17F, 0f,0f, false, chillFontSpeed.get(), getColor(-1).rgb)

    }

    private fun drawStitch(target: EntityLivingBase) {
        val tWidth = (110F + Fonts.fontTenacityBold40.getStringWidth(target.name)).coerceAtLeast(120F)
        // background
        RenderUtils.drawRoundedRect(0F, 0F, tWidth, 65F, 7F, Color(255, 255, 255, 40).rgb)
        // circle player avatar
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glPushMatrix()
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle((tWidth.toInt()/2) - 15, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle((tWidth.toInt()/2) - 15, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)
        GL11.glPopMatrix()
        // name
        Fonts.fontTenacityBold40.drawCenteredString(target.name, tWidth/2F, 39F, getColor(-1).rgb, false)

        "${ndecimalFormat.format((easingHP / target.maxHealth) * 100)}%".also {
            Fonts.font32.drawString(it, ((easingHP / target.maxHealth) * (tWidth - 5) - Fonts.font32.getStringWidth(it)).coerceAtLeast(40f), 60f - Fonts.font32.FONT_HEIGHT, Color.WHITE.rgb, false)
        }

        // hp bar
        RenderUtils.drawRoundedCornerRect(5f, 58f, (tWidth - 5), 62f, 2.5f, Color(0, 0, 0, 150).rgb)
        RenderUtils.drawRoundedCornerRect(5f, 58f, (easingHP / target.maxHealth) * (tWidth - 5), 62f, 2.5f, ColorUtils.rainbow().rgb)

    }

    private fun drawRemix(entity: EntityPlayer) {
        updateAnim(entity.health)

        // background
        RenderUtils.newDrawRect(0F, 0F, 146F, 49F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(1F, 1F, 145F, 48F, getColor(Color(35, 35, 35)).rgb)

        // health bar
        RenderUtils.newDrawRect(4F, 40F, 142F, 45F, getColor(Color.red.darker().darker()).rgb)
        RenderUtils.newDrawRect(4F, 40F, 4F + (easingHealth / entity.maxHealth).coerceIn(0F, 1F) * 138F, 45F, barColor.rgb)

        // head
        RenderUtils.newDrawRect(4F, 4F, 38F, 38F, getColor(Color(150, 150, 150)).rgb)
        RenderUtils.newDrawRect(5F, 5F, 37F, 37F, getColor(Color(0, 0, 0)).rgb)

        // armor bar
        RenderUtils.newDrawRect(40F, 36F, 141.5F, 38F, getColor(Color.blue.darker()).rgb)
        RenderUtils.newDrawRect(40F, 36F, 40F + (entity.getTotalArmorValue().toFloat() / 20F).coerceIn(0F, 1F) * 101.5F, 38F, getColor(Color.blue).rgb)

        // armor item background
        RenderUtils.newDrawRect(40F, 16F, 58F, 34F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(41F, 17F, 57F, 33F, getColor(Color(95, 95, 95)).rgb)

        RenderUtils.newDrawRect(60F, 16F, 78F, 34F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(61F, 17F, 77F, 33F, getColor(Color(95, 95, 95)).rgb)

        RenderUtils.newDrawRect(80F, 16F, 98F, 34F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(81F, 17F, 97F, 33F, getColor(Color(95, 95, 95)).rgb)

        RenderUtils.newDrawRect(100F, 16F, 118F, 34F, getColor(Color(25, 25, 25)).rgb)
        RenderUtils.newDrawRect(101F, 17F, 117F, 33F, getColor(Color(95, 95, 95)).rgb)

        // name
        Fonts.minecraftFont.drawStringWithShadow(entity.name, 41F, 5F, getColor(-1).rgb)

        // ping
        if (mc.netHandler.getPlayerInfo(entity.uniqueID) != null) {
            // actual head
            drawHead(mc.netHandler.getPlayerInfo(entity.uniqueID).locationSkin, 5, 5, 32, 32, 1F - getFadeProgress())

            val responseTime = mc.netHandler.getPlayerInfo(entity.uniqueID).responseTime.toInt()
            val stringTime = "${responseTime.coerceAtLeast(0)}ms"

            var j = 0

            if (responseTime < 0)
                j = 5
            else if (responseTime < 150)
                j = 0
            else if (responseTime < 300)
                j = 1
            else if (responseTime < 600)
                j = 2
            else if (responseTime < 1000)
                j = 3
            else
                j = 4

            mc.textureManager.bindTexture(Gui.icons)
            RenderUtils.drawTexturedModalRect(132, 18, 0, 176 + j * 8, 10, 8, 100.0F)

            GL11.glPushMatrix()
            GL11.glTranslatef(142F - Fonts.minecraftFont.getStringWidth(stringTime) / 2F, 28F, 0F)
            GL11.glScalef(0.5F, 0.5F, 0.5F)
            Fonts.minecraftFont.drawStringWithShadow(stringTime, 0F, 0F, getColor(-1).rgb)
            GL11.glPopMatrix()
        }

        // armor items
        GL11.glPushMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f - getFadeProgress())
        RenderHelper.enableGUIStandardItemLighting()

        val renderItem = mc.renderItem

        var x = 41
        var y = 17

        for (index in 3 downTo 0) {
            val stack = entity.inventory.armorInventory[index] ?: continue

            if (stack.getItem() == null)
                continue

            renderItem.renderItemAndEffectIntoGUI(stack, x, y)
            x += 20
        }

        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()
    }

    private fun drawWaterMelon(target: EntityLivingBase) {
        // background rect
        RenderUtils.drawRoundedCornerRect(
            -1.5f, 2.5f, 152.5f, 52.5f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -1f, 2f, 152f, 52f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0.5f, 1.5f, 151.5f, 51.5f,
            5.0f, Color(0, 0, 0, 40).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0f, 1f, 151.0f, 51.0f,
            5.0f, Color(0, 0, 0, 60).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            0.5f, 0.5f, 150.5f, 50.5f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            1f, 0f, 150.0f, 50.0f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        // head size based on hurt
        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) {
            1f
        } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35
        // draw head
        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)

        GL11.glPopMatrix()
        // draw name of target
        FontLoaders.F20.DisplayFonts("${target.name}", 45f, 12f, Color.WHITE.rgb, FontLoaders.F20)
        val df = DecimalFormat("0.00");
        // draw armour percent
        FontLoaders.F14.DisplayFonts(
            "Armor ${(df.format(PlayerUtils.getAr(target) * 100))}%",
            45f,
            24f,
            Color(200, 200, 200).rgb,
            FontLoaders.F14
        )
        // draw bar
        RenderUtils.drawRoundedCornerRect(45f, 32f, 145f, 42f, 5f, Color(0, 0, 0, 100).rgb)
        RenderUtils.drawRoundedCornerRect(
            45f,
            32f,
            45f + (easingHP / target.maxHealth) * 100f,
            42f,
            5f,
            ColorUtils.rainbow().rgb
        )
        // draw hp as text
        FontLoaders.F14.DisplayFont2(
            FontLoaders.F14,
            "${((df.format((easingHP / target.maxHealth) * 100)))}%",
            80f,
            34f,
            Color(255, 255, 255).rgb,
            true
        )
    }

    private fun drawSparklingWater(target: EntityLivingBase) {
        // background
        RenderUtils.drawRoundedCornerRect(
            -1.5f, 2.5f, 152.5f, 52.5f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -1f, 2f, 152f, 52f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0.5f, 1.5f, 151.5f, 51.5f,
            5.0f, Color(0, 0, 0, 40).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0f, 1f, 151.0f, 51.0f,
            5.0f, Color(0, 0, 0, 60).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            0.5f, 0.5f, 150.5f, 50.5f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            1f, 0f, 150.0f, 50.0f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        // draw entity
        if(target.hurtTime > 1) {
            GL11.glColor4f(1f, 0f, 0f, 0.5f)
            RenderUtils.drawEntityOnScreen(25, 48, 32, target)
        } else {
            GL11.glColor4f(1f, 1f, 1f, 1f)
            RenderUtils.drawEntityOnScreen(25, 45, 30, target)
        }

        // target text
        FontLoaders.F20.DisplayFonts("${target.name}", 45f, 6f, Color.WHITE.rgb, FontLoaders.F20)
        val df = DecimalFormat("0.00");
        // armour text
        FontLoaders.F14.DisplayFonts(
            "Armor ${(df.format(PlayerUtils.getAr(target) * 100))}%",
            45f,
            40f,
            Color(200, 200, 200).rgb,
            FontLoaders.F14
        )//bar
        RenderUtils.drawRoundedCornerRect(45f, 23f, 145f, 33f, 5f, Color(0, 0, 0, 100).rgb)
        RenderUtils.drawRoundedCornerRect(
            45f,
            23f,
            45f + (easingHP / target.maxHealth) * 100f,
            33f,
            5f,
            ColorUtils.rainbow().rgb
        )
        FontLoaders.F14.DisplayFont2(
            FontLoaders.F14,
            "${((df.format((easingHP / target.maxHealth) * 100)))}%",
            80f,
            25f,
            Color(255, 255, 255).rgb,
            true
        )
/*
        // draw items
         GlStateManager.resetColor()
        GL11.glPushMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f - getFadeProgress())
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()
        val renderItem = mc.renderItem
        var x = 45
        var y = 28
        for (index in 3 downTo 0) {
            val stack = entity.inventory.armorInventory[index] ?: continue
            if (stack.item == null)
                continue
            renderItem.renderItemIntoGUI(stack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
            RenderUtils.drawExhiEnchants(stack, x.toFloat(), y.toFloat())
            x += 16
        }
        val mainStack = entity.heldItem
        if (mainStack != null && mainStack.item != null) {
            renderItem.renderItemIntoGUI(mainStack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
            RenderUtils.drawExhiEnchants(mainStack, x.toFloat(), y.toFloat())
        }
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()
         */
    }

    private fun drawRice(entity: EntityLivingBase) {
        updateAnim(entity.health)

        val font = Fonts.font40
        val name = "Name: ${entity.name}"
        val info = "Distance: ${decimalFormat2.format(mc.thePlayer.getDistanceToEntityBox(entity))}"
        val healthName = decimalFormat2.format(easingHealth)

        val length = (font.getStringWidth(name).coerceAtLeast(font.getStringWidth(info)).toFloat() + 40F).coerceAtLeast(125F)
        val maxHealthLength = font.getStringWidth(decimalFormat2.format(entity.maxHealth)).toFloat()

        // background
        RenderUtils.drawRoundedRect(0F, 0F, 10F + length, 55F, 8F, bgColor.rgb)

        // particle engine
        if (riceParticle.get()) {
            // adding system
            if (gotDamaged) {
                for (j in 0..(generateAmountValue.get())) {
                    val parSize = RandomUtils.nextFloat(minParticleSize.get(), maxParticleSize.get())
                    val parDistX = RandomUtils.nextFloat(-particleRange.get(), particleRange.get())
                    val parDistY = RandomUtils.nextFloat(-particleRange.get(), particleRange.get())
                    val firstChar = RandomUtils.random(1, "${if (riceParticleCircle.get().equals("none", true)) "" else "c"}${if (riceParticleRect.get().equals("none", true)) "" else "r"}${if (riceParticleTriangle.get().equals("none", true)) "" else "t"}")
                    val drawType = ShapeType.getTypeFromName(when (firstChar) {
                        "c" -> "c_${riceParticleCircle.get().lowercase(Locale.getDefault())}"
                        "r" -> "r_${riceParticleRect.get().lowercase(Locale.getDefault())}"
                        else -> "t_${riceParticleTriangle.get().lowercase(Locale.getDefault())}"
                    }) ?: break

                    particleList.add(
                        Particle(
                            BlendUtils.blendColors(
                                floatArrayOf(0F, 1F),
                                arrayOf<Color>(Color.white, barColor),
                                if (RandomUtils.nextBoolean()) RandomUtils.nextFloat(0.5F, 1.0F) else 0F),
                            parDistX, parDistY, parSize, drawType)
                    )
                }
                gotDamaged = false
            }

            // render and removing system
            val deleteQueue = mutableListOf<Particle>()

            particleList.forEach { particle ->
                if (particle.alpha > 0F)
                    particle.render(20F, 20F, riceParticleFade.get(), riceParticleSpeed.get(), riceParticleFadingSpeed.get(), riceParticleSpin.get())
                else
                    deleteQueue.add(particle)
            }

            particleList.removeAll(deleteQueue)
        }

        // custom head
        val scaleHT = (entity.hurtTime.toFloat() / entity.maxHurtTime.coerceAtLeast(1).toFloat()).coerceIn(0F, 1F)
        if (mc.netHandler.getPlayerInfo(entity.uniqueID) != null)
            drawHead(mc.netHandler.getPlayerInfo(entity.uniqueID).locationSkin,
                5F + 15F * (scaleHT * 0.2F),
                5F + 15F * (scaleHT * 0.2F),
                1F - scaleHT * 0.2F,
                30, 30,
                1F, 0.4F + (1F - scaleHT) * 0.6F, 0.4F + (1F - scaleHT) * 0.6F,
                1F - getFadeProgress())

        // player's info
        GlStateManager.resetColor()
        font.drawString(name, 39F, 11F, getColor(-1).rgb)
        font.drawString(info, 39F, 23F, getColor(-1).rgb)

        // gradient health bar
        val barWidth = (length - 5F - maxHealthLength) * (easingHealth / entity.maxHealth).coerceIn(0F, 1F)
        Stencil.write(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        if (gradientRoundedBarValue.get()) {
            if (barWidth > 0F)
                RenderUtils.fastRoundedRect(5F, 42F, 5F + barWidth, 48F, 3F)
        } else
            RenderUtils.quickDrawRect(5F, 42F, 5F + barWidth, 48F)

        GL11.glDisable(GL11.GL_BLEND)
        Stencil.erase(true)
        when (colorModeValue.get().lowercase(Locale.getDefault())) {
            "custom", "health" -> RenderUtils.drawRect(5F, 42F, length - maxHealthLength, 48F, barColor.rgb)
            else -> for (i in 0 until gradientLoopValue.get()) {
                val barStart = i.toDouble() / gradientLoopValue.get().toDouble() * (length - 5F - maxHealthLength).toDouble()
                val barEnd = (i + 1).toDouble() / gradientLoopValue.get().toDouble() * (length - 5F - maxHealthLength).toDouble()
                RenderUtils.drawGradientSideways(5.0 + barStart, 42.0, 5.0 + barEnd, 48.0, getColorAtIndex(i), getColorAtIndex(i + 1))
            }
        }
        Stencil.dispose()
        GlStateManager.resetColor()
        font.drawString(healthName, 10F + barWidth, 41F, getColor(-1).rgb)
        if (shadowValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                // the part to blur for the epic glow
                val font = Fonts.fontSFUI40
                val name = "Name: ${entity.name}"
                val info = "Distance: ${decimalFormat2.format(mc.thePlayer.getDistanceToEntityBox(entity))}"
                val length = (font.getStringWidth(name).coerceAtLeast(font.getStringWidth(info)).toFloat() + 40F).coerceAtLeast(125F)
                RenderUtils.originalRoundedRect(0F, 0F, 10F + length, 55F, 8F, shadowOpaque.rgb)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                // the part to cut
                val font = Fonts.fontSFUI40
                val name = "Name: ${entity.name}"
                val info = "Distance: ${decimalFormat2.format(mc.thePlayer.getDistanceToEntityBox(entity))}"
                val length = (font.getStringWidth(name).coerceAtLeast(font.getStringWidth(info)).toFloat() + 40F).coerceAtLeast(125F)

                RenderUtils.originalRoundedRect(0F, 0F, 10F + length, 55F, 8F, shadowOpaque.rgb)

                GL11.glPopMatrix()
            })

            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
    }

    private fun drawSlowly(entity: EntityLivingBase) {
        val font = Fonts.minecraftFont
        val healthString = "${decimalFormat2.format(entity.health)} ❤"
        val length = 60.coerceAtLeast(font.getStringWidth(entity.name)).coerceAtLeast(font.getStringWidth(healthString)).toFloat() + 10F

        updateAnim(entity.health)

        RenderUtils.drawRect(0F, 0F, 32F + length, 36F, bgColor.rgb)

        if (mc.netHandler.getPlayerInfo(entity.uniqueID) != null)
            drawHead(mc.netHandler.getPlayerInfo(entity.uniqueID).locationSkin, 1, 1, 30, 30, 1F - getFadeProgress())

        font.drawStringWithShadow(entity.name, 33F, 2F, getColor(-1).rgb)
        font.drawStringWithShadow(healthString, length + 31F - font.getStringWidth(healthString).toFloat(), 22F, barColor.rgb)

        RenderUtils.drawRect(0F, 32F, (easingHealth / entity.maxHealth.toFloat()).coerceIn(0F, entity.maxHealth.toFloat()) * (length + 32F), 36F, barColor.rgb)
        if (shadowValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                val font = Fonts.minecraftFont
                val healthString = "${decimalFormat2.format(entity.health)} ❤"
                val length = 60.coerceAtLeast(font.getStringWidth(entity.name)).coerceAtLeast(font.getStringWidth(healthString)).toFloat() + 10F

                RenderUtils.newDrawRect(0F, 0F, 32F + length, 36F, shadowOpaque.rgb)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                val font = Fonts.minecraftFont
                val healthString = "${decimalFormat2.format(entity.health)} ❤"
                val length = 60.coerceAtLeast(font.getStringWidth(entity.name)).coerceAtLeast(font.getStringWidth(healthString)).toFloat() + 10F
                RenderUtils.newDrawRect(0F, 0F, 32F + length, 36F, shadowOpaque.rgb)

                GL11.glPopMatrix()
            })

            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

    }

    private class CharRenderer(val small: Boolean) {
        var moveY = FloatArray(20)
        var moveX = FloatArray(20)

        private val numberList = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")

        private val deFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))

        init {
            for (i in 0..19) {
                moveX[i] = 0F
                moveY[i] = 0F
            }
        }

        fun renderChar(number: Float, orgX: Float, orgY: Float, initX: Float, initY: Float, scaleX: Float, scaleY: Float, shadow: Boolean, fontSpeed: Float, color: Int): Float {
            val reFormat = deFormat.format(number.toDouble()) // string
            val fontRend = if (small) Fonts.font40 else Fonts.font72
            val delta = RenderUtils.deltaTime
            val scaledRes = ScaledResolution(mc)

            var indexX = 0
            var indexY = 0
            var animX = 0F

            val cutY = initY + fontRend.FONT_HEIGHT.toFloat() * (3F / 4F)

            GL11.glEnable(3089)
            RenderUtils.makeScissorBox(0F, orgY + initY - 4F * scaleY, scaledRes.scaledWidth.toFloat(), orgY + cutY - 4F * scaleY)
            for (char in reFormat.toCharArray()) {
                moveX[indexX] = AnimationUtils.animate(animX, moveX[indexX], fontSpeed * 0.025F * delta)
                animX = moveX[indexX]

                val pos = numberList.indexOf("$char")
                val expectAnim = (fontRend.FONT_HEIGHT.toFloat() + 2F) * pos
                val expectAnimMin = (fontRend.FONT_HEIGHT.toFloat() + 2F) * (pos - 2)
                val expectAnimMax = (fontRend.FONT_HEIGHT.toFloat() + 2F) * (pos + 2)

                if (pos >= 0) {
                    moveY[indexY] = AnimationUtils.animate(expectAnim, moveY[indexY], fontSpeed * 0.02F * delta)

                    GL11.glTranslatef(0F, initY - moveY[indexY], 0F)
                    numberList.forEachIndexed { index, num ->
                        if ((fontRend.FONT_HEIGHT.toFloat() + 2F) * index >= expectAnimMin && (fontRend.FONT_HEIGHT.toFloat() + 2F) * index <= expectAnimMax) {
                            fontRend.drawString(num, initX + moveX[indexX], (fontRend.FONT_HEIGHT.toFloat() + 2F) * index, color, shadow)
                        }
                    }
                    GL11.glTranslatef(0F, -initY + moveY[indexY], 0F)
                } else {
                    moveY[indexY] = 0F
                    fontRend.drawString("$char", initX + moveX[indexX], initY, color, shadow)
                }

                animX += fontRend.getStringWidth("$char")
                indexX++
                indexY++
            }
            GL11.glDisable(3089)

            return animX
        }
    }

    fun drawHead(skin: ResourceLocation, x: Int = 2, y: Int = 2, width: Int, height: Int, alpha: Float = 1F) {
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, width, height,
            64F, 64F)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }

    fun drawHead(skin: ResourceLocation, x: Float, y: Float, scale: Float, width: Int, height: Int, red: Float, green: Float, blue: Float, alpha: Float = 1F) {
        GL11.glPushMatrix()
        GL11.glTranslatef(x, y, 0F)
        GL11.glScalef(scale, scale, scale)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GL11.glColor4f(red.coerceIn(0F, 1F), green.coerceIn(0F, 1F), blue.coerceIn(0F, 1F), alpha.coerceIn(0F, 1F))
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(0, 0, 8F, 8F, 8, 8, width, height,
            64F, 64F)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glPopMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    fun getColor(color: Color) = ColorUtils.reAlpha(color, color.alpha / 255F * (1F - getFadeProgress()))
    fun getColor(color: Int) = getColor(Color(color))

    open fun updateAnim(targetHealth: Float) {
        if (noAnimValue.get())
            easingHealth = targetHealth
        else
            easingHealth += ((targetHealth - easingHealth) / 2.0F.pow(10.0F - globalAnimSpeed.get())) * RenderUtils.deltaTime
    }

    fun getFadeProgress() = animProgress

    override fun handleDamage(ent: EntityPlayer) {
        gotDamaged = true
    }

    fun getTBorder(): Border? {
        return when (modeValue.get().lowercase()) {
            "astolfo" -> Border(0F, 0F, 140F, 60F)
            "astolfo2" -> Border(0F, 0F, 160F, 60F)
            "vape" -> Border(0F, 0F, 110F, 40F)
            "liquid" -> Border(0F, 0F, (38 + mc.thePlayer.name.let(Fonts.font40::getStringWidth)).coerceAtLeast(118).toFloat(), 36F)
            "fdp" -> Border(0F, 0F, 150F, 47F)
            "flux" -> Border(0F, 0F, (38 + mc.thePlayer.name.let(Fonts.font40::getStringWidth))
                .coerceAtLeast(70)
                .toFloat(), 34F)
            "rise" -> {
                when (modeRise.get().lowercase()) {
                    "original" -> Border(0F, 0F, 150F, 50F)
                    "new1" -> Border(0F, 0F, 150F, 50F)
                    "new2" -> Border(0F, 0F, 150F, 45F)
                    "rise6" -> Border(0F, 0F, 150F, 50F)
                    else -> null
                }
            }
            "zamorozka" -> Border(0F, 0F, 150F, 55F)
            "arris" -> Border(0F, 0F, 120F, 40F)
            "tenacity" -> Border(0F, 0F, 120F, 40F)
            "tenacity5" -> Border(-2F, 3F, 62F + mc.thePlayer.name.let(Fonts.font40::getStringWidth).coerceAtLeast(75).toFloat(), 50F)
            "tenacitynew" -> Border(0F, 5F, 125F, 45F)
            "chill" -> Border(0F, 0F, 120F, 48F)
            "chilllite" -> Border(0F, 0F, 90F, 38F)
            "stitch" -> Border(0F, 0F, 150F, 65F)
            "remix" -> Border(0F, 0F, 146F, 49F)
            "rice" -> Border(0F, 0F, 135F, 55F)
            "slowly" -> Border(0F, 0F, 102F, 36F)
            "exhibition" -> Border(0F, 0F, 126F, 45F)
            "exhibitionold" -> Border(2F, 1F, 122F, 40F)
            "watermelon" -> Border(0F, 0F, 120F, 48F)
            "sparklingwater" -> Border(0F, 0F, 120F, 48F)
            "bar" -> Border(3F, 22F, 115F, 42F)
            else -> null
        }
    }

    private fun getColorAtIndex(i: Int): Int {
        return (when (colorModeValue.get()) {
            "Rainbow" -> ColorUtils.getRainbowOpaque(waveSecondValue.get(), saturationValue.get(), brightnessValue.get(), i * gradientDistanceValue.get())
            "Slowly" -> ColorUtils.slowlyRainbow(
                System.nanoTime(),
                i * gradientDistanceValue.get(),
                saturationValue.get(),
                brightnessValue.get()
            ).rgb
            "Fade" -> ColorUtils.fade(Color(redValue.get(), greenValue.get(), blueValue.get()), i * gradientDistanceValue.get(), 100).rgb
            else -> -1
        })
    }

}
