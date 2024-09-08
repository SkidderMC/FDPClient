/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.APIConnecter
import net.ccbluex.liquidbounce.utils.CPSCounter.mouseWithinBounds
import net.ccbluex.liquidbounce.utils.ClientThemesUtils.ClientColorMode
import net.ccbluex.liquidbounce.utils.ClientThemesUtils.ThemeFadeSpeed
import net.ccbluex.liquidbounce.utils.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.ClientThemesUtils.getColorFromName
import net.ccbluex.liquidbounce.utils.ClientThemesUtils.textValue
import net.ccbluex.liquidbounce.utils.ClientThemesUtils.updown
import net.ccbluex.liquidbounce.utils.extensions.animLinear
import net.ccbluex.liquidbounce.utils.extensions.animSmooth
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedGradientRectCorner
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedOutline
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.fastRoundedRect
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.Pair

class GuiTheme : GuiScreen() {
    // Variables for controlling text visibility and animations
    private var text = false
    private var textsmooth = 0F
    private var scroll = 0F
    private var animScroll = 0F
    private var smooth = arrayOf(0F, 0F, 0F, 0F)

    // Called to render the screen with various UI elements
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Mouse scroll logic for adjusting the scroll value
        val wheel = Mouse.getDWheel()
        if (wheel != 0) {
            scroll += if (wheel > 0) 30F else -30F
        }

        // Clamp scroll value to avoid over-scrolling
        scroll = scroll.coerceIn(-200F, 0F)

        // Smooth scroll animation, animates the scroll value to the target smoothly
        animScroll = animScroll.animSmooth(scroll, 0.5F)

        text = textValue // Update text visibility based on textValue
        drawRoundedRect(10F, 10F, 628F, 428F, 5F, Color(0, 0, 0, 150).rgb, 3F, getColor(1).rgb)

        // Draw navigation arrows
        drawImage(APIConnecter.callImage("arrowup", "design"), 160, 381, 25, 25)
        drawImage(APIConnecter.callImage("arrowdown", "design"), 160, 410, 25, 25)

        // Draw the title and separator bars
        Fonts.font35.drawStringWithShadow("ClientTheme", 20.0F, 25.0F, getColor(1).rgb)
        RenderUtils.drawRect(10F, 65F, 638F, 66F, getColor(1).rgb)
        RenderUtils.drawRect(10F, 345.0F, 638F, 346.0F, getColor(1).rgb)

        // Prepare to render modes with clipping
        glPushMatrix()
        Stencil.write(false)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        fastRoundedRect(21F, 66.5F, 635F, 345F, 0F)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        Stencil.erase(true)

        // Render each mode with animation and gradient colors
        fun drawMode(
            mode: String,
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            gradientColors: List<Int>,
            text: String,
            textX: Double,
            textY: Double
        ) {
            // Draw gradient rectangle for the mode
            drawRoundedGradientRectCorner(x1 + 2, y1 + 1.5F + animScroll, x2 - 2, y2 - 2.5F + animScroll, 20F, gradientColors[0], gradientColors[1])

            // If the mode is selected, animate its border
            if (ClientColorMode == mode) {
                smooth[0] = AnimationUtils.animate(x1, smooth[0], 0.015F * RenderUtils.deltaTime)
                smooth[1] = AnimationUtils.animate(y1, smooth[1], 0.015F * RenderUtils.deltaTime)
                smooth[2] = AnimationUtils.animate(x2, smooth[2], 0.015F * RenderUtils.deltaTime)
                smooth[3] = AnimationUtils.animate(y2, smooth[3], 0.015F * RenderUtils.deltaTime)
                drawRoundedOutline(smooth[0], smooth[1] - 0.5F + animScroll, smooth[2], smooth[3] - 1.3F + animScroll, 23.5F, 4F, Color(255, 255, 255).rgb)
            }
            // Draw mode name
            Fonts.font20.drawStringWithShadow(text, textX.toFloat(), (textY + animScroll).toFloat(), getColorFromName(mode, 1).rgb)
        }

        // List of mode names and their positions
        val modes = listOf(
            "Zywl", "Water", "Magic", "DarkNight", "Sun",
            "Tree", "Flower", "Loyoi", "Cero", "Soniga",
            "May", "Mint", "Azure", "Rainbow", "Astolfo",
            "Pumpkin", "Polarized", "Sundae", "Terminal", "Coral",
            "Fire", "Aqua", "Peony"
        )

        // Positions for rendering modes and their text labels
        val modePositions = listOf(
            Pair(22f, 68F), Pair(147f, 68F), Pair(272f, 68F), Pair(397f, 68F), Pair(522f, 68F),
            Pair(22f, 163.0f), Pair(147f, 163.0f), Pair(272f, 163.0f), Pair(397f, 163.0f), Pair(522f, 163.0f),
            Pair(22f, 258.0f), Pair(147f, 258.0f), Pair(272f, 258.0f), Pair(397f, 258.0f), Pair(522f, 258.0f),
            Pair(22f, 353f), Pair(147f, 353f), Pair(272f, 353f), Pair(397f, 353f), Pair(522f, 353f),
            Pair(22f, 448f), Pair(147f, 448f), Pair(272F, 448F)
        )

        val textPositions = listOf(
            Pair(58.0, 145.0), Pair(184.0, 145.0), Pair(310.0, 145.0), Pair(427.0, 145.0), Pair(565.0, 145.0),
            Pair(60.0, 240.0), Pair(184.0, 240.0), Pair(310.0, 240.0), Pair(434.0, 240.0), Pair(560.0, 240.0),
            Pair(60.0, 335.0), Pair(187.0, 335.0), Pair(310.0, 335.0), Pair(428.0, 335.0), Pair(560.0, 335.0),
            Pair(53.0, 430.0), Pair(180.0, 430.0), Pair(305.0, 430.0), Pair(428.0, 430.0), Pair(563.0, 430.0),
            Pair(58.0, 525.0), Pair(185.0, 525.0), Pair(305.0, 525.0)
        )

        // Render all modes with the associated gradients and labels
        for (i in modes.indices) {
            val mode = modes[i]
            val position = modePositions[i]
            val textPosition = textPositions[i]
            val gradientColors = List(4) { getColorFromName(mode, it * 90).rgb }
            drawMode(mode, position.first, position.second, position.first + 104.4f, position.second + 74f, gradientColors, mode, textPosition.first, textPosition.second)
        }

        Stencil.dispose() // Cleanup after rendering
        glPopMatrix()

        // Render a rounded rectangle and its outline for text-related UI elements
        val textColor = ColorUtils.blendColors(
            floatArrayOf(0F, 1F),
            arrayOf(Color(255, 0, 0), Color(0, 255, 0)),
            textsmooth
        ).rgb

        textsmooth = textsmooth.animLinear((if (text) 0.2F else -0.2F) * RenderUtils.deltaTime * 0.045F, 0F, 1F)

        drawRoundedRect(25F, 350.0f, 40F, 365.0f, 5F, textColor)
        drawRoundedOutline(25F, 350.0f, 40F, 365.0f, 7F, 1F, Color.WHITE.rgb)

        // Render various UI text labels
        Fonts.font35.drawStringWithShadow("Text White Color", 43.0F, 351.5F, Color(255, 255, 255).rgb)
        Fonts.font35.drawStringWithShadow("Fade Side : $updown", 25.0F, 376.5F, Color(255, 255, 255).rgb)
        Fonts.font35.drawStringWithShadow("FadeSpeed : $ThemeFadeSpeed", 25.0F, 401.5F, Color(255, 255, 255).rgb)
        GlStateManager.resetColor() // Reset the color state after rendering
    }

    // Handle mouse click events to interact with the UI
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // Logic for selecting different color modes when the user clicks within specific bounds
        if (mouseWithinBounds(mouseX, mouseY, 25F, 70F + animScroll, 122F, 140F + animScroll)) {
            ClientColorMode = "Zywl"
        }
        if (mouseWithinBounds(mouseX, mouseY, 150F, 70F + animScroll, 247F, 140F + animScroll)) {
            ClientColorMode = "Water"
        }
        if (mouseWithinBounds(mouseX, mouseY, 275F, 70F + animScroll, 372F, 140F + animScroll)) {
            ClientColorMode = "Magic"
        }
        if (mouseWithinBounds(mouseX, mouseY, 400F, 70F + animScroll, 497F, 140F + animScroll)) {
            ClientColorMode = "DarkNight"
        }
        if (mouseWithinBounds(mouseX, mouseY, 525F, 70F + animScroll, 622F, 140F + animScroll)) {
            ClientColorMode = "Sun"
        }

        // Handle clicks for other rows of modes
        // Line 2 (Tree, Flower, Loyoi, Cero, Soniga)
        if (mouseWithinBounds(mouseX, mouseY, 25F, 165F + animScroll, 122F, 235F + animScroll)) {
            ClientColorMode = "Tree"
        }
        if (mouseWithinBounds(mouseX, mouseY, 150F, 165F + animScroll, 247F, 235F + animScroll)) {
            ClientColorMode = "Flower"
        }
        if (mouseWithinBounds(mouseX, mouseY, 275F, 165F + animScroll, 372F, 235F + animScroll)) {
            ClientColorMode = "Loyoi"
        }
        if (mouseWithinBounds(mouseX, mouseY, 400F, 165F + animScroll, 497F, 235F + animScroll)) {
            ClientColorMode = "Cero"
        }
        if (mouseWithinBounds(mouseX, mouseY, 525F, 165F + animScroll, 622F, 235F + animScroll)) {
            ClientColorMode = "Soniga"
        }

        //Line 3 (May, Mint, Azure, Rainbow, Astolfo)
        if (mouseWithinBounds(mouseX, mouseY, 25F, 260F + animScroll, 122F, 330F + animScroll)) {
            ClientColorMode = "May"
        }
        if (mouseWithinBounds(mouseX, mouseY, 150F, 260F + animScroll, 247F, 330F + animScroll)) {
            ClientColorMode = "Mint"
        }
        if (mouseWithinBounds(mouseX, mouseY, 275F, 260F + animScroll, 372F, 330F + animScroll)) {
            ClientColorMode = "Azure"
        }
        if (mouseWithinBounds(mouseX, mouseY, 400F, 260F + animScroll, 497F, 330F + animScroll)) {
            ClientColorMode = "Rainbow"
        }
        if (mouseWithinBounds(mouseX, mouseY, 525F, 260F + animScroll, 622F, 330F + animScroll)) {
            ClientColorMode = "Astolfo"
        }

        //Line 4 (Pumpkin, Polarized, Sundae, Terminal, Coral)
        if (animScroll < -75F) {
            if (mouseWithinBounds(mouseX, mouseY, 25F, 355F + animScroll, 122F, 425F + animScroll)) {
                ClientColorMode = "Pumpkin"
            }
            if (mouseWithinBounds(mouseX, mouseY, 150F, 355F + animScroll, 247F, 425F + animScroll)) {
                ClientColorMode = "Polarized"
            }
            if (mouseWithinBounds(mouseX, mouseY, 275F, 355F + animScroll, 372F, 425F + animScroll)) {
                ClientColorMode = "Sundae"
            }
            if (mouseWithinBounds(mouseX, mouseY, 400F, 355F + animScroll, 497F, 425F + animScroll)) {
                ClientColorMode = "Terminal"
            }
            if (mouseWithinBounds(mouseX, mouseY, 525F, 355F + animScroll, 622F, 425F + animScroll)) {
                ClientColorMode = "Coral"
            }
        }
        //Line 5 (Fire, Aqua, Peony)
        if (animScroll < -115F) {
            if (mouseWithinBounds(mouseX, mouseY, 25F, 450F + animScroll, 122F, 520F + animScroll)) {
                ClientColorMode = "Fire"
            }
            if (mouseWithinBounds(mouseX, mouseY, 150F, 450F + animScroll, 247F, 520F + animScroll)) {
                ClientColorMode = "Aqua"
            }
            if (mouseWithinBounds(mouseX, mouseY, 275F, 450F + animScroll, 372F, 520F + animScroll)) {
                ClientColorMode = "Peony"
            }
        }

        // Handle text-related click interactions
        if (mouseWithinBounds(mouseX, mouseY, 25F, 350.0f, 40F, 365.0f)) {
            textValue = !textValue
        }
        if (mouseWithinBounds(mouseX, mouseY, 90F, 375.0f, 140.0f, 390.0f)) {
            updown = !updown
        }
        if (mouseWithinBounds(mouseX, mouseY, 160F, 380F, 180F, 400F)) {
            if (ThemeFadeSpeed != 20) {
                ThemeFadeSpeed += 1
            }
        }
        if (mouseWithinBounds(mouseX, mouseY, 160F, 410F, 180F, 430F)) {
            if (ThemeFadeSpeed != 0) {
                ThemeFadeSpeed -= 1
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}