/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addPrint
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.TranslateActions
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.vitox.Particle.roundToPlace
import java.awt.Color
import kotlin.math.floor

@ElementInfo(name = "Print")
class Prints(x: Double = 520.0, y: Double = 245.0) : Element(x = x, y = y) {

    // Example print notification
    private val exampleNotification = Print("Example Print", 0.0f, PrintType.INFO)

    // Holds the active prints to be rendered
    private var prints: List<Print>? = null

    // Maps entity health for comparison
    private val healthMap = mutableMapOf<EntityLivingBase, Float>()

    // Combat manager to track current combat target
    private val combatManager = CombatManager

    /**
     * Draws the element on the HUD, rendering active prints and handling their fade state.
     */
    override fun drawElement(): Border {
        // Reset position for each print before drawing
        var yPosition = y.toFloat() // Start position for prints
        var index = 0

        // Filter and update active prints
        prints = hud.prints.filterNot { it.removing }

        prints?.forEach { print ->
            // Set index and update position dynamically
            print.index = index
            print.y = yPosition + (index * 11) // Adjust the Y position based on index

            // Handle the translation of the print (animation)
            print.translate.translate(0f, (hud.prints.size * 11 - index * 11).toFloat(), 1.5)
            print.drawPrint()

            // If the print is in the END fade state, remove it
            if (print.fadeState == PrintFadeState.END) {
                hud.removePrint(print)
                index--
            }

            index++
        }

        // Display example notification in HUD Designer mode
        if (mc.currentScreen is GuiHudDesigner) {
            if (!hud.prints.contains(exampleNotification)) {
                addPrint(exampleNotification)
            }
            exampleNotification.fadeState = PrintFadeState.STAY
            exampleNotification.x = exampleNotification.textLength + 8.0f
            return Border(
                -exampleNotification.x + 12 + exampleNotification.textLength,
                0f,
                -exampleNotification.x - 35,
                20 + 11f * hud.notifications.size
            )
        }

        return Border(0f, 0f, 0f, 0f)
    }

    /**
     * Updates the element's logic, particularly the health of combat targets.
     * If health changes, adds a new print notification showing the damage dealt.
     */
    override fun livingupdateElement() {
        // Get the current combat target
        val target = combatManager.target

        if (target != null && target != mc.thePlayer) {
            // Track previous health of the target
            val previousHealth = healthMap[target] ?: target.health
            val currentHealth = target.health

            // If health has changed, calculate the damage and add a print notification
            if (previousHealth != currentHealth) {
                val remaining = floor((previousHealth - (previousHealth - currentHealth)).coerceAtLeast(0.0F)).toInt()
                val damageText = "Hurt ${target.name} for ${roundToPlace(previousHealth - currentHealth, 1)} hp ($remaining remaining)."

                // Add the damage print to the HUD
                hud.addPrint(Print(damageText, 3000f, PrintType.SUCCESS))

                // Update health map with the new value
                healthMap[target] = currentHealth
            } else {
                // If health didn't change, just update the map
                healthMap[target] = currentHealth
            }
        }
    }

    /**
     * Inner class to handle Print notifications in HUD.
     */
    class Print(
        var message: String,
        private val timer: Float,
        var type: PrintType,
        val alphaData: AlphaData = AlphaData("")
    ) {
        var size: Int = 0
        var index: Int = 0
        var x: Float = 0f
        var y: Float = 0f
        var textLength: Int = 0
        var fadeState: PrintFadeState
        var removing: Boolean = false
        var translate: TranslateActions
        private var removingTranslate: TranslateActions
        private var stayTime = 0f
        private var typeMessage: String? = null

        init {
            fadeState = PrintFadeState.IN
            translate = TranslateActions(0f, 0f)
            removingTranslate = TranslateActions(0f, 0f)
        }

        /**
         * Draws the print notification on screen.
         */
        fun drawPrint() {
            // Set the type icon based on the print type.
            typeMessage = when (type) {
                PrintType.STATE -> "V"
                PrintType.ERROR -> "U"
                PrintType.INFO -> "M"
                PrintType.SUCCESS -> "T"
                PrintType.NONE -> ""
            }

            textLength = 60
            val width = textLength + 8f

            // Draw notification only if fully visible.
            if (150 - removingTranslate.x > 30) {
                GlStateManager.pushMatrix()
                GlStateManager.resetColor()

                if (message.isNotEmpty() && type != PrintType.NONE) {
                    // Draw background gradient
                    drawBackgroundGradient(width)

                    // Draw the text and icons for each print type.
                    drawTextAndIcons(width)
                }
                GlStateManager.popMatrix()
            }

            // Handle fade states (IN, STAY, OUT, END)
            updateFadeState()
        }

        /**
         * Draws the background gradient for the notification.
         */
        private fun drawBackgroundGradient(width: Float) {
            val typeMessageWidth = typeMessage?.let { Fonts.fontIcons35.getStringWidth(it).toFloat() } ?: 0f
            val messageWidth = Fonts.font15.getStringWidth(message).toFloat()
            val totalWidth = -width + typeMessageWidth + messageWidth + 10f

            RenderUtils.drawGradientSI(
                totalWidth,
                y - 5,
                -width - 36,
                y - 16f,
                Color(0, 0, 0, 0).rgb,
                Color(0, 0, 0, (150 - removingTranslate.x.toInt())).rgb
            )
        }

        /**
         * Draws the text and icons for the notification based on its type.
         */
        private fun drawTextAndIcons(width: Float) {
            when (type) {
                PrintType.INFO -> {
                    typeMessage?.let {
                        Fonts.fontIcons35.drawString(
                            it,
                            -width - 29,
                            y - 12f,
                            Color(0, 131, 193, (150 - removingTranslate.x.toInt())).rgb
                        )
                    }
                    Fonts.font15.drawString(
                        message,
                        -width - 29 + (typeMessage?.let { Fonts.fontIcons35.getStringWidth(it) } ?: (0 * 2)),
                        y - 12f,
                        Color(255, 255, 255, (150 - removingTranslate.x.toInt())).rgb
                    )
                }
                PrintType.ERROR, PrintType.SUCCESS -> {
                    typeMessage?.let {
                        Fonts.fontIcons35.drawString(
                            it,
                            -width - 32,
                            y - 13f,
                            Color(0, 131, 193, (150 - removingTranslate.x.toInt())).rgb
                        )
                    }
                    Fonts.font15.drawString(
                        message,
                        -width - 32 + (typeMessage?.let { Fonts.fontIcons35.getStringWidth(it) } ?: 0),
                        y - 12f,
                        Color(255, 255, 255, (150 - removingTranslate.x.toInt())).rgb
                    )
                }
                PrintType.STATE -> {
                    typeMessage?.let {
                        Fonts.fontIcons35.drawString(
                            it,
                            -width - 32,
                            y - 12f,
                            Color(0, 131, 193, (150 - removingTranslate.x.toInt())).rgb
                        )
                    }
                    Fonts.font15.drawString(
                        message,
                        -width - 32 + (typeMessage?.let { Fonts.fontIcons35.getStringWidth(it) } ?: 0),
                        y - 12f,
                        Color(255, 255, 255, (150 - removingTranslate.x.toInt())).rgb
                    )
                }
                PrintType.NONE -> {
                    // No action needed for NONE, just return
                    return
                }
            }
        }

        /**
         * Updates the fade state of the notification (IN, STAY, OUT, END).
         */
        private fun updateFadeState() {
            when (fadeState) {
                PrintFadeState.IN -> {
                    size += 1
                    stayTime = timer
                    fadeState = PrintFadeState.STAY
                }
                PrintFadeState.STAY -> {
                    if (stayTime > 0) {
                        stayTime -= RenderUtils.deltaTime.toFloat()
                    } else {
                        fadeState = PrintFadeState.OUT
                    }
                }
                PrintFadeState.OUT -> {
                    removing = index == 0 || removingTranslate.x > 0
                    if (removing) {
                        removingTranslate.translate(150f, 0f, 1.0)
                    }
                    if (150 - removingTranslate.x <= 1) {
                        fadeState = PrintFadeState.END
                    }
                }
                PrintFadeState.END -> {}
            }
        }
    }
}

/**
 * Enum to define the type of the notification.
 */
enum class PrintType {
    ERROR, SUCCESS, INFO, STATE, NONE
}

/**
 * Class to manage alpha data for players in HUD.
 */
class AlphaData(val playerName: String) {
    var translate: TranslateActions = TranslateActions(0f, 0f)
}

/**
 * Enum to define the fade state of the notification.
 */
enum class PrintFadeState {
    IN, STAY, OUT, END
}