/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.impl

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.colormode
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.generateColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.BlackStyle.sliderValueHeld
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Main
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.PasswordField
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.GuiEvents
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTexture
import net.ccbluex.liquidbounce.utils.render.RenderUtils.updateTextureCache
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.blendColors
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCustomShapeWithRadius
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.stream.Collectors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SettingComponents(private val module: Module) : Component() {
    var settingHeightScissor: Animation? = null
    private val keySettingAnimMap = HashMap<Module, Array<Animation>>()
    private val sliderintMap = HashMap<IntegerValue, Float>()
    private val sliderintAnimMap = HashMap<IntegerValue, Array<Animation>>()
    private val sliderfloatMap = HashMap<FloatValue, Float>()
    private val sliderfloatAnimMap = HashMap<FloatValue, Array<Animation>>()
    private val toggleAnimation = HashMap<BoolValue, Array<Animation>>()
    private val modeSettingAnimMap = HashMap<ListValue, Array<Animation>>()
    private val modeSettingClick = HashMap<ListValue, Boolean>()
    private val modesHoverAnimation = HashMap<ListValue, HashMap<String, Animation>>()
    var binding: Module? = null
    var draggingNumber: Value<*>? = null
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var rectHeight: Float = 0f
    var panelLimitY: Float = 0f
    var alphaAnimation: Int = 0
    var settingSize: Double = 0.0
    private var selectedField: PasswordField? = null
    private var selectedStringSetting: TextValue? = null

    private val colorSettingAnimMap = HashMap<ColorValue, Array<Animation>>()
    private val colorPickerAnimationMap = HashMap<ColorValue, Animation>()

    init {
        keySettingAnimMap[module] = arrayOf(
            EaseInOutQuad(250, 1.0, Direction.BACKWARDS),
            DecelerateAnimation(225, 1.0, Direction.BACKWARDS)
        )

        for (setting in module.values) {
            if (setting is FloatValue) {
                sliderfloatMap[setting] = 0f
                sliderfloatAnimMap[setting] = arrayOf(
                    DecelerateAnimation(250, 1.0, Direction.BACKWARDS),
                    DecelerateAnimation(200, 1.0, Direction.BACKWARDS)
                )
            }
            if (setting is IntegerValue) {
                sliderintMap[setting] = 0f
                sliderintAnimMap[setting] = arrayOf(
                    DecelerateAnimation(250, 1.0, Direction.BACKWARDS),
                    DecelerateAnimation(200, 1.0, Direction.BACKWARDS)
                )
            }
            if (setting is BoolValue) {
                toggleAnimation[setting] = arrayOf(
                    DecelerateAnimation(225, 1.0, Direction.BACKWARDS),
                    DecelerateAnimation(200, 1.0, Direction.BACKWARDS)
                )
            }
            if (setting is ListValue) {
                modeSettingClick[setting] = false
                modeSettingAnimMap[setting] = arrayOf(
                    DecelerateAnimation(225, 1.0, Direction.BACKWARDS),
                    EaseInOutQuad(250, 1.0, Direction.BACKWARDS)
                )

                val modeMap = HashMap<String, Animation>()
                for (mode in setting.values) {
                    modeMap[mode] = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)
                }
                modesHoverAnimation[setting] = modeMap
            }
            if (setting is ColorValue) {
                colorSettingAnimMap[setting] = arrayOf(
                    DecelerateAnimation(250, 1.0, Direction.BACKWARDS),
                    DecelerateAnimation(200, 1.0, Direction.BACKWARDS)
                )
                colorPickerAnimationMap[setting] = DecelerateAnimation(300, 1.0, Direction.FORWARDS)

                setting.showPicker = false
                setting.hueSliderY
            }
        }
    }

    override fun initGui() {
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (binding != null) {
            selectedField = null
            selectedStringSetting = null
            if (keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
                binding!!.keyBind = Keyboard.KEY_NONE
            }
            binding!!.keyBind = keyCode
            binding = null
            return
        }

        if (selectedField != null) {
            // ESC key => stop focusing
            if (keyCode == 1) {
                selectedField = null
                selectedStringSetting = null
                return
            }
            selectedField!!.textboxKeyTyped(typedChar, keyCode)
            selectedStringSetting!!.set(selectedField!!.textValue, true)
        }
    }

    fun handle(mouseX: Int, mouseY: Int, button: Int, type: GuiEvents) {
        val textColor = Color(255, 255, 255, alphaAnimation)
        val darkRectColor = Color(48, 50, 55, alphaAnimation)
        val darkRectColorDisabled = Color(52, 52, 52, alphaAnimation)
        val darkRectHover = DrRenderUtils.brighter(darkRectColor, .8f)

        val accent = colormode.equals("Color", ignoreCase = true)
        val index = 0
        val color2 = Color(generateColor(index).rgb)
        val colors = arrayOf(color2, color2)

        val accentedColor = DrRenderUtils.applyOpacity(colors[0], alphaAnimation / 255f)
        val accentedColor2 = DrRenderUtils.applyOpacity(colors[1], alphaAnimation / 255f)

        var count = 0.0

        for (setting in module.values.stream().filter { obj: Value<*> -> obj.shouldRender() }
            .collect(Collectors.toList())) {
            val settingY = roundToHalf(y + (count * rectHeight)).toFloat()

            // ----- FloatValue -----
            if (setting is FloatValue) {

                val value = round(setting.value.toDouble(), 0.01).toFloat().toString()
                val regularFontWidth = Fonts.InterMedium_18.stringWidth(setting.name + ": ").toFloat()
                val valueFontWidth = Fonts.InterMedium_18.stringWidth(value).toFloat()

                val titleX = x + width / 2f - (regularFontWidth + valueFontWidth) / 2f
                val titleY = (settingY + Fonts.InterMedium_18.getMiddleOfBox(rectHeight)
                        - Fonts.InterMedium_18.getMiddleOfBox(rectHeight) / 2f + 1)

                GlStateManager.color(1f, 1f, 1f, 1f)
                Fonts.InterMedium_18.drawString(setting.name + ": ", titleX, titleY, textColor.rgb)
                Fonts.InterBold_18.drawString(value, titleX + regularFontWidth, titleY, textColor.rgb)

                val hoverAnimation = sliderfloatAnimMap[setting]!![0]
                val selectAnimtion = sliderfloatAnimMap[setting]!![1]

                val totalSliderWidth = width - 10
                val hoveringSlider = isClickable(settingY + 17)
                        && DrRenderUtils.isHovering(x + 5, settingY + 17, totalSliderWidth, 6f, mouseX, mouseY)

                if (type == GuiEvents.RELEASE) {
                    draggingNumber = null
                }
                hoverAnimation.setDirection(
                    if (hoveringSlider || draggingNumber === setting) Direction.FORWARDS else Direction.BACKWARDS
                )
                selectAnimtion.setDirection(
                    if (draggingNumber === setting) Direction.FORWARDS else Direction.BACKWARDS
                )

                if (type == GuiEvents.CLICK && hoveringSlider && button == 0) {
                    draggingNumber = setting
                }

                val currentValue = setting.value.toDouble()
                if (draggingNumber != null && draggingNumber === setting) {
                    val percent = min(1.0, max(0.0, ((mouseX - (x + 5)) / totalSliderWidth).toDouble())).toFloat()
                    val newValue = ((percent * (setting.maximum - setting.minimum))
                            + setting.minimum).toDouble()
                    setting.set(newValue)
                }

                val sliderMath = ((currentValue - setting.minimum)
                        / (setting.maximum - setting.minimum)).toFloat()

                // Animate the slider position
                val oldSlider = sliderfloatMap[setting]!!
                val targetSlider = totalSliderWidth * sliderMath
                sliderfloatMap[setting] =
                    DrRenderUtils.animate(targetSlider.toDouble(), oldSlider.toDouble(), .1).toFloat()

                val sliderY = (settingY + 18)
                drawCustomShapeWithRadius(
                    x + 5, sliderY, totalSliderWidth, 3f, 1.5f,
                    DrRenderUtils.applyOpacity(darkRectHover, (.4f + (.2 * hoverAnimation.output)).toFloat())
                )
                drawCustomShapeWithRadius(
                    x + 5, sliderY, max(4.0, sliderfloatMap[setting]!!.toDouble()).toFloat(), 3f, 1.5f,
                    if (accent) accentedColor2 else textColor
                )

                DrRenderUtils.setAlphaLimit(0f)
                DrRenderUtils.fakeCircleGlow(
                    (x + 4 + max(4.0, sliderfloatMap[setting]!!.toDouble())).toFloat(),
                    sliderY + 1.5f, 6f, Color.BLACK, .3f
                )
                DrRenderUtils.drawGoodCircle(
                    (x + 4 + max(4.0, sliderfloatMap[setting]!!.toDouble())),
                    (sliderY + 1.5f).toDouble(), 3.75f,
                    if (accent) accentedColor2.rgb else textColor.rgb
                )

                count += .5
            }

            // ----- IntegerValue -----
            if (setting is IntegerValue) {
                val value = roundX(setting.value.toDouble(), 1.0).toFloat().toString()

                val regularFontWidth = Fonts.InterMedium_18.stringWidth(setting.name + ": ").toFloat()
                val valueFontWidth = Fonts.InterMedium_18.stringWidth(value).toFloat()

                val titleX = x + width / 2f - (regularFontWidth + valueFontWidth) / 2f
                val titleY = (settingY + Fonts.InterMedium_18.getMiddleOfBox(rectHeight)
                        - Fonts.InterMedium_18.getMiddleOfBox(rectHeight) / 2f + 1)

                GlStateManager.color(1f, 1f, 1f, 1f)
                Fonts.InterMedium_18.drawString(setting.name + ": ", titleX, titleY, textColor.rgb)
                Fonts.InterBold_18.drawString(value, titleX + regularFontWidth, titleY, textColor.rgb)

                val hoverAnimation = sliderintAnimMap[setting]!![0]
                val selectAnimtion = sliderintAnimMap[setting]!![1]

                val totalSliderWidth = width - 10
                val hoveringSlider = isClickable(settingY + 17)
                        && DrRenderUtils.isHovering(x + 5, settingY + 17, totalSliderWidth, 6f, mouseX, mouseY)

                if (type == GuiEvents.RELEASE) {
                    draggingNumber = null
                }
                hoverAnimation.setDirection(
                    if (hoveringSlider || draggingNumber === setting) Direction.FORWARDS else Direction.BACKWARDS
                )
                selectAnimtion.setDirection(
                    if (draggingNumber === setting) Direction.FORWARDS else Direction.BACKWARDS
                )

                if (type == GuiEvents.CLICK && hoveringSlider && button == 0) {
                    draggingNumber = setting
                }

                val currentValue = setting.value.toDouble()
                if (draggingNumber != null && draggingNumber === setting) {
                    val percent = min(1.0, max(0.0, ((mouseX - (x + 5)) / totalSliderWidth).toDouble())).toFloat()
                    val newValue = ((percent * (setting.maximum - setting.minimum))
                            + setting.minimum).toDouble()
                    setting.set(newValue)
                }

                val sliderMath = ((currentValue - setting.minimum)
                        / (setting.maximum - setting.minimum)).toFloat()

                // Animate the slider position
                val oldSlider = sliderintMap[setting]!!
                val targetSlider = totalSliderWidth * sliderMath
                sliderintMap[setting] =
                    DrRenderUtils.animate(targetSlider.toDouble(), oldSlider.toDouble(), .1).toFloat()

                val sliderY = (settingY + 18)
                drawCustomShapeWithRadius(
                    x + 5, sliderY, totalSliderWidth, 3f, 1.5f,
                    DrRenderUtils.applyOpacity(darkRectHover, (.4f + (.2 * hoverAnimation.output)).toFloat())
                )
                drawCustomShapeWithRadius(
                    x + 5, sliderY, max(4.0, sliderintMap[setting]!!.toDouble()).toFloat(), 3f, 1.5f,
                    if (accent) accentedColor2 else textColor
                )

                DrRenderUtils.setAlphaLimit(0f)
                DrRenderUtils.fakeCircleGlow(
                    (x + 4 + max(4.0, sliderintMap[setting]!!.toDouble())).toFloat(),
                    sliderY + 1.5f, 6f, Color.BLACK, .3f
                )
                DrRenderUtils.drawGoodCircle(
                    (x + 4 + max(4.0, sliderintMap[setting]!!.toDouble())),
                    (sliderY + 1.5f).toDouble(), 3.75f,
                    if (accent) accentedColor2.rgb else textColor.rgb
                )

                count += .5
            }

            // ----- BoolValue -----
            if (setting is BoolValue) {
                val toggleAnim =
                    toggleAnimation[setting]!![0]
                val hoverAnim =
                    toggleAnimation[setting]!![1]

                DrRenderUtils.resetColor()
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
                GlStateManager.enableBlend()

                Fonts.InterMedium_18.drawString(
                    setting.name,
                    roundToHalf((x + 4).toDouble()).toInt().toFloat(),
                    settingY + 5,
                    textColor.rgb
                )

                val switchWidth = 16f
                val hoveringSwitch = isClickable(settingY + Fonts.InterMedium_18.getMiddleOfBox(rectHeight) - 1)
                        && DrRenderUtils.isHovering(
                    x + width - (switchWidth + 6),
                    settingY + Fonts.InterMedium_18.getMiddleOfBox(rectHeight) - 1,
                    switchWidth, 8f, mouseX, mouseY
                )

                hoverAnim.setDirection(if (hoveringSwitch) Direction.FORWARDS else Direction.BACKWARDS)

                if (type == GuiEvents.CLICK && hoveringSwitch && button == 0) {
                    setting.toggle()
                }

                toggleAnim.setDirection(if (setting.get()) Direction.FORWARDS else Direction.BACKWARDS)
                DrRenderUtils.resetColor()

                val accentCircle = if (accent)
                    DrRenderUtils.applyOpacity(accentedColor, .8f)
                else
                    DrRenderUtils.darker(textColor, .8f)

                drawCustomShapeWithRadius(
                    x + width - (switchWidth + 5.5f),
                    settingY + Fonts.InterMedium_18.getMiddleOfBox(rectHeight) + 2,
                    switchWidth, 4.5f, 2f,
                    DrRenderUtils.interpolateColorC(
                        DrRenderUtils.applyOpacity(darkRectHover, .5f),
                        accentCircle, toggleAnim.output.toFloat()
                    )
                )

                DrRenderUtils.fakeCircleGlow(
                    ((x + width - (switchWidth + 3))
                            + ((switchWidth - 5) * toggleAnim.output)).toFloat(),
                    settingY + Fonts.InterMedium_18.getMiddleOfBox(rectHeight) + 4,
                    6f, Color.BLACK, .3f
                )

                DrRenderUtils.resetColor()
                drawCustomShapeWithRadius(
                    (x + width - (switchWidth + 6) + ((switchWidth - 5) * toggleAnim.output)).toFloat(),
                    settingY + Fonts.InterMedium_18.getMiddleOfBox(rectHeight) + 1,
                    6.5f, 6.5f, 3f, textColor
                )
            }

            // ----- ListValue -----
            if (setting is ListValue) {
                val hoverAnim = modeSettingAnimMap[setting]!![0]
                val openAnim = modeSettingAnimMap[setting]!![1]

                val hoveringModeRect = isClickable(settingY + 5)
                        && DrRenderUtils.isHovering(x + 5, settingY + 5, width - 10, rectHeight + 7, mouseX, mouseY)

                if (type == GuiEvents.CLICK && hoveringModeRect && button == 1) {
                    modeSettingClick[setting] = !modeSettingClick[setting]!!
                }
                hoverAnim.setDirection(if (hoveringModeRect) Direction.FORWARDS else Direction.BACKWARDS)
                openAnim.setDirection(if (modeSettingClick[setting]!!) Direction.FORWARDS else Direction.BACKWARDS)

                val math = (setting.values.size - 1) * rectHeight
                drawCustomShapeWithRadius(
                    x + 5,
                    (settingY + rectHeight + 2 + (12 * openAnim.output)).toFloat(),
                    width - 10,
                    (math * openAnim.output).toFloat(),
                    3f,
                    DrRenderUtils.applyOpacity(darkRectHover, (.35f * openAnim.output).toFloat())
                )

                if (!openAnim.isDone && type == GuiEvents.DRAW) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST)
                    DrRenderUtils.scissor(
                        (x + 5).toDouble(),
                        (settingY + 7 + rectHeight + (3 * openAnim.output)).toFloat().toDouble(),
                        (width - 10).toDouble(),
                        (math * openAnim.output).toFloat().toDouble()
                    )
                }

                var modeCount = 0f
                for (mode in setting.values) {
                    if (mode.equals(setting.get(), ignoreCase = true)) continue

                    val modeY = ((settingY + rectHeight + 11
                            + ((8 + (modeCount * rectHeight)) * openAnim.output))
                            ).toFloat()
                    DrRenderUtils.resetColor()

                    val hoveringMode = isClickable(modeY - 5)
                            && openAnim.direction == Direction.FORWARDS
                            && DrRenderUtils.isHovering(x + 5, modeY - 5, width - 10, rectHeight, mouseX, mouseY)

                    val modeHover = modesHoverAnimation[setting]!![mode]
                    modeHover!!.setDirection(if (hoveringMode) Direction.FORWARDS else Direction.BACKWARDS)

                    if (modeHover.finished(Direction.FORWARDS) || !modeHover.isDone) {
                        drawCustomShapeWithRadius(
                            x + 5, modeY - 5, width - 10, rectHeight, 3f,
                            DrRenderUtils.applyOpacity(textColor, (.2f * modeHover.output).toFloat())
                        )
                    }

                    if (type == GuiEvents.CLICK && button == 0 && hoveringMode) {
                        modeSettingClick[setting] = !modeSettingClick[setting]!!
                        setting.set(mode, true)
                    }
                    if (openAnim.isDone && openAnim.direction == Direction.FORWARDS || !openAnim.isDone) {
                        Fonts.InterMedium_18.drawString(
                            mode,
                            x + 13,
                            modeY,
                            DrRenderUtils.applyOpacity(textColor, openAnim.output.toFloat()).rgb
                        )
                    }
                    modeCount++
                }

                if (!openAnim.isDone && type == GuiEvents.DRAW) {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST)
                }
                if (settingHeightScissor!!.isDone
                    && openAnim.isDone
                    && GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)
                ) {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST)
                }

                drawCustomShapeWithRadius(
                    x + 5, settingY + 5, width - 10, rectHeight + 7, 3f,
                    DrRenderUtils.applyOpacity(darkRectHover, .45f)
                )

                if (!hoverAnim.isDone || hoverAnim.finished(Direction.FORWARDS)) {
                    drawCustomShapeWithRadius(
                        x + 5, settingY + 5, width - 10, rectHeight + 7, 3f,
                        DrRenderUtils.applyOpacity(textColor, (.2f * hoverAnim.output).toFloat())
                    )
                }

                val selectRectWidth = ((width - 10) * openAnim.output).toFloat()
                if (openAnim.isDone && openAnim.direction == Direction.FORWARDS
                    || !openAnim.isDone
                ) {
                    drawCustomShapeWithRadius(
                        x + 5 + ((width - 10) / 2f - selectRectWidth / 2f),
                        settingY + rectHeight + 10.5f,
                        max(2.0, selectRectWidth.toDouble()).toFloat(), 1.5f, .5f,
                        if (accent) accentedColor2 else textColor
                    )
                }

                Fonts.InterMedium_14.drawString(
                    setting.name,
                    x + 13,
                    settingY + 9,
                    textColor.rgb
                )

                DrRenderUtils.resetColor()
                Fonts.InterBold_18.drawString(
                    setting.get(),
                    x + 13,
                    (settingY + 17.5).toFloat(),
                    textColor.rgb
                )

                DrRenderUtils.resetColor()
                DrRenderUtils.drawClickGuiArrow(
                    x + width - 15,
                    settingY + 17,
                    5f,
                    openAnim,
                    textColor.rgb
                )

                count += 1 + ((math / rectHeight) * openAnim.output)
            }

            // ----- TextValue -----
            if (setting is TextValue) {

                DrRenderUtils.resetColor()
                Fonts.InterMedium_16.drawString(
                    setting.name,
                    x + 5,
                    settingY + 2,
                    textColor.rgb
                )

                // Create the PasswordField (which might just be a text box in your code)
                val stringSettingField = PasswordField(
                    "Type Here...",
                    0,
                    (x + 5).toInt(),
                    (settingY + 15).toInt(),
                    (width - 10).toInt(),
                    10,
                    Fonts.InterMedium_18
                )

                // Use renamed methods to avoid ambiguous calls:
                // (Assuming PasswordField was updated to have updateText(...) and updateTextColor(...))
                stringSettingField.updateText(setting.get())
                stringSettingField.setFocused(selectedStringSetting === setting)
                stringSettingField.bottomBarColor = textColor.rgb
                stringSettingField.updateTextColor(textColor.rgb)
                stringSettingField.placeHolderTextX = (x + 30).toDouble()

                if (type == GuiEvents.CLICK) {
                    stringSettingField.mouseClicked(mouseX, mouseY, button)
                }
                if (stringSettingField.isFocused()) {
                    selectedField = stringSettingField
                    selectedStringSetting = setting
                } else if (selectedStringSetting === setting) {
                    selectedStringSetting = null
                    selectedField = null
                }

                stringSettingField.drawTextBox()
                setting.set(stringSettingField.textValue, true)

                count++
            }

            if (setting is ColorValue) {
                val currentColor = setting.selectedColor()
                val text = setting.name + ": " + "#%08X".format(currentColor.rgb)
                Fonts.InterMedium_18.drawString(text, x + 5, settingY + 3, textColor.rgb)

                val previewSize = 9
                val previewX2 = x + width - 10
                val previewX1 = previewX2 - previewSize
                val previewY1 = settingY + 2
                val previewY2 = previewY1 + previewSize

                drawRect(previewX1, previewY1, previewX2, previewY2, currentColor.rgb)

                val rainbowPreviewX2 = previewX1 - previewSize
                val rainbowPreviewX1 = rainbowPreviewX2 - previewSize
                if (rainbowPreviewX1 > x + 4) {
                    drawRect(
                        rainbowPreviewX1,
                        previewY1,
                        rainbowPreviewX2,
                        previewY2,
                        ColorUtils.rainbow(setting.opacitySliderY).rgb
                    )
                }

                val rainbow = setting.rainbow

                val hoveringColorPreview = isClickable(settingY + 2)
                        && DrRenderUtils.isHovering(
                    previewX1, previewY1,
                    previewSize.toFloat(),
                    previewSize.toFloat(),
                    mouseX, mouseY
                )
                val hoveringRainbowPreview = isClickable(settingY + 2)
                        && rainbowPreviewX1 > x + 4
                        && DrRenderUtils.isHovering(
                    rainbowPreviewX1, previewY1,
                    previewSize.toFloat(),
                    previewSize.toFloat(),
                    mouseX, mouseY
                )

                if (type == GuiEvents.CLICK && button in arrayOf(0, 1)) {
                    if (hoveringColorPreview) {
                        if (button == 0 && rainbow) setting.rainbow = false
                        if (button == 1) setting.showPicker = !setting.showPicker
                    }
                    if (hoveringRainbowPreview) {
                        if (button == 0) setting.rainbow = true
                        if (button == 1) setting.showPicker = !setting.showPicker
                    }
                }

                val colorPickerWidth = 75
                val colorPickerHeight = 50
                val hueSliderWidth = 7
                val hueSliderHeight = 50
                val spacingBetweenSliders = 5

                val colorPickerStartX = (x + 5).toInt()
                val colorPickerStartY = (settingY + 15).toInt()
                val colorPickerEndX = colorPickerStartX + colorPickerWidth
                val colorPickerEndY = colorPickerStartY + colorPickerHeight

                val hueSliderX = colorPickerEndX + spacingBetweenSliders
                val hueSliderStartY = colorPickerStartY
                val hueSliderEndY = colorPickerStartY + hueSliderHeight

                val opacityStartX = hueSliderX + hueSliderWidth + spacingBetweenSliders
                val opacityEndX = opacityStartX + hueSliderWidth

                val hue = if (rainbow) {
                    Color.RGBtoHSB(currentColor.red, currentColor.green, currentColor.blue, null)[0]
                } else {
                    setting.hueSliderY
                }

                if (setting.showPicker) {
                    drawRect(
                        colorPickerStartX,
                        colorPickerStartY,
                        colorPickerEndX,
                        colorPickerEndY,
                        darkRectHover.rgb
                    )

                    setting.updateTextureCache(
                        id = 0,
                        hue = hue,
                        width = colorPickerWidth,
                        height = colorPickerHeight,
                        generateImage = { image, _ ->
                            for (px in 0 until colorPickerWidth) {
                                for (py in 0 until colorPickerHeight) {
                                    val localS = px / colorPickerWidth.toFloat()
                                    val localB = 1.0f - (py / colorPickerHeight.toFloat())
                                    val rgb = Color.HSBtoRGB(hue, localS, localB)
                                    image.setRGB(px, py, rgb)
                                }
                            }
                        },
                        drawAt = { id ->
                            drawTexture(id, colorPickerStartX, colorPickerStartY, colorPickerWidth, colorPickerHeight)
                        }
                    )

                    val markerX = (colorPickerStartX..colorPickerEndX).lerpWith(setting.colorPickerPos.x)
                    val markerY = (colorPickerStartY..colorPickerEndY).lerpWith(setting.colorPickerPos.y)
                    RenderUtils.drawBorder(markerX - 2f, markerY - 2f, markerX + 3f, markerY + 3f, 1.5f, Color.WHITE.rgb)

                    setting.updateTextureCache(
                        id = 1,
                        hue = hue,
                        width = hueSliderWidth,
                        height = hueSliderHeight,
                        generateImage = { image, _ ->
                            for (y in 0 until hueSliderHeight) {
                                for (x in 0 until hueSliderWidth) {
                                    val localHue = y / hueSliderHeight.toFloat()
                                    val rgb = Color.HSBtoRGB(localHue, 1.0f, 1.0f)
                                    image.setRGB(x, y, rgb)
                                }
                            }
                        },
                        drawAt = { id ->
                            drawTexture(id, hueSliderX, hueSliderStartY, hueSliderWidth, hueSliderHeight)
                        }
                    )

                    setting.updateTextureCache(
                        id = 2,
                        hue = currentColor.rgb.toFloat(),
                        width = hueSliderWidth,
                        height = hueSliderHeight,
                        generateImage = { image, _ ->
                            val gridSize = 1
                            for (y in 0 until hueSliderHeight) {
                                for (x in 0 until hueSliderWidth) {
                                    val gridX = x / gridSize
                                    val gridY = y / gridSize
                                    val checkerboardColor =
                                        if ((gridY + gridX) % 2 == 0) Color.WHITE.rgb else Color.BLACK.rgb
                                    val alpha = ((1 - y.toFloat() / hueSliderHeight.toFloat()) * 255).roundToInt()
                                    val finalColor = blendColors(
                                        Color(checkerboardColor),
                                        currentColor.withAlpha(alpha)
                                    )
                                    image.setRGB(x, y, finalColor.rgb)
                                }
                            }
                        },
                        drawAt = { id ->
                            drawTexture(id, opacityStartX, hueSliderStartY, hueSliderWidth, hueSliderHeight)
                        }
                    )

                    val hueMarkerY = (hueSliderStartY..hueSliderEndY).lerpWith(hue)
                    val opacityMarkerY = (hueSliderStartY..hueSliderEndY).lerpWith(1 - setting.opacitySliderY)
                    RenderUtils.drawBorder(
                        hueSliderX - 1f,
                        hueMarkerY - 1f,
                        hueSliderX + hueSliderWidth + 1f,
                        hueMarkerY + 1f,
                        1.5f,
                        Color.WHITE.rgb
                    )
                    RenderUtils.drawBorder(
                        opacityStartX - 1f,
                        opacityMarkerY - 1f,
                        opacityEndX + 1f,
                        opacityMarkerY + 1f,
                        1.5f,
                        Color.WHITE.rgb
                    )

                    val inColorPicker =
                        (mouseX in colorPickerStartX until colorPickerEndX
                                && mouseY in colorPickerStartY until colorPickerEndY)
                    val inHueSlider =
                        (mouseX in hueSliderX - 1..hueSliderX + hueSliderWidth + 1
                                && mouseY in hueSliderStartY until hueSliderEndY)
                    val inOpacitySlider =
                        (mouseX in opacityStartX - 1..opacityEndX + 1
                                && mouseY in hueSliderStartY until hueSliderEndY)

                    val sliderType = setting.lastChosenSlider
                    if ((type == GuiEvents.CLICK && button == 0 && (inColorPicker || inHueSlider || inOpacitySlider))
                        || (sliderValueHeld == setting && setting.lastChosenSlider != null)
                    ) {

                        if (inColorPicker && (sliderType == null || sliderType == ColorValue.SliderType.COLOR)) {
                            val newS = ((mouseX - colorPickerStartX) / colorPickerWidth.toFloat()).coerceIn(0f, 1f)
                            val newB = (1.0f - (mouseY - colorPickerStartY) / colorPickerHeight.toFloat()).coerceIn(0f, 1f)
                            setting.colorPickerPos.x = newS
                            setting.colorPickerPos.y = 1 - newB
                        }

                        var finalColor = Color(
                            Color.HSBtoRGB(
                                setting.hueSliderY,
                                setting.colorPickerPos.x,
                                1 - setting.colorPickerPos.y
                            )
                        )

                        if (inHueSlider && (sliderType == null || sliderType == ColorValue.SliderType.HUE)) {
                            setting.hueSliderY =
                                ((mouseY - hueSliderStartY) / hueSliderHeight.toFloat()).coerceIn(0f, 1f)
                            finalColor = Color(
                                Color.HSBtoRGB(
                                    setting.hueSliderY,
                                    setting.colorPickerPos.x,
                                    1 - setting.colorPickerPos.y
                                )
                            )
                        }

                        if (inOpacitySlider && (sliderType == null || sliderType == ColorValue.SliderType.OPACITY)) {
                            setting.opacitySliderY =
                                1 - ((mouseY - hueSliderStartY) / hueSliderHeight.toFloat()).coerceIn(0f, 1f)
                        }

                        finalColor = finalColor.withAlpha((setting.opacitySliderY * 255).roundToInt())

                        sliderValueHeld = setting

                        withDelayedSave {
                            setting.set(finalColor, true)
                        }

                        if (button == 0) {
                            setting.lastChosenSlider = when {
                                inColorPicker   -> ColorValue.SliderType.COLOR
                                inHueSlider     -> ColorValue.SliderType.HUE
                                inOpacitySlider -> ColorValue.SliderType.OPACITY
                                else            -> null
                            }
                        }
                    }
                    count += (colorPickerHeight / rectHeight) + 0.5f

                } else {
                    count += 0.2f
                }
            }

            // Render the key bind
            val bind = Keyboard.getKeyName(module.keyBind)
            val hoveringBindRect = isClickable(
                y + Fonts.InterBold_18.getMiddleOfBox(rectHeight) - 1
            ) && DrRenderUtils.isHovering(
                x + width - (Fonts.InterBold_18.stringWidth(bind) + 10),
                y + Fonts.InterBold_18.getMiddleOfBox(rectHeight) - 1,
                (Fonts.InterBold_18.stringWidth(bind) + 8).toFloat(),
                (Fonts.InterBold_18.height + 6).toFloat(),
                mouseX, mouseY
            )

            if (type == GuiEvents.CLICK && hoveringBindRect && button == 0) {
                binding = module
                return
            }

            val animations =
                keySettingAnimMap[module]!!
            animations[1].setDirection(if (binding === module) Direction.FORWARDS else Direction.BACKWARDS)
            animations[0].setDirection(if (hoveringBindRect) Direction.FORWARDS else Direction.BACKWARDS)

            // (Any extra code for rendering the bind rectangle is commented out below)
            /*
             int offsetX = 10;
             float bindButtonY = y + 4; // Adjust as needed

             RoundedUtil.drawRound(
                 x + width - (Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.stringWidth(bind) + 12) + offsetX,
                 bindButtonY,
                 (float) (Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.stringWidth(bind) + 8),
                 Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.getHeight() + 6,
                 5,
                 DrRenderUtils.applyOpacity(darkRectHover, (float) (.4 + (.2 * animations[0].getOutput())))
             );

             Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.drawString(
                 bind,
                 x + width - (Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.stringWidth(bind) + 9) + offsetX,
                 bindButtonY + Fonts.SFBOLD.SFBOLD_18.SFBOLD_18.getMiddleOfBox(rectHeight) + 1,
                 DrRenderUtils.interpolateColor(
                     textColor.getRGB(),
                     accentedColor2.getRGB(),
                     (float) animations[1].getOutput()
                 )
             );
            */
            count++
        }
        settingSize = count
    }

    override fun drawScreen(mouseX: Int, mouseY: Int) {
        handle(mouseX, mouseY, -1, GuiEvents.DRAW)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        handle(mouseX, mouseY, button, GuiEvents.CLICK)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        handle(mouseX, mouseY, state, GuiEvents.RELEASE)
    }

    private fun withDelayedSave(block: () -> Unit) {
        block()
    }

    /**
     * Returns whether we can safely interact with a setting at the given y-position,
     * preventing clicks from “spilling over” the visible region.
     */
    fun isClickable(y: Float): Boolean {
        return y > panelLimitY && y < panelLimitY + 17 + Main.allowedClickGuiHeight
    }

    companion object {
        var scale: Float = 0f
    }
}