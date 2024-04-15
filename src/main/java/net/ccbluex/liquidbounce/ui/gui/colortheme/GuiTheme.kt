/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.gui.colortheme

import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme.fadespeed
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme.textValue
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme.updown
import net.ccbluex.liquidbounce.ui.font.cf.FontLoaders
import net.ccbluex.liquidbounce.utils.MouseUtils.mouseWithinBounds
import net.ccbluex.liquidbounce.utils.animations.animLinear
import net.ccbluex.liquidbounce.utils.animations.animSmooth
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiTheme : GuiScreen() {
    private var text = false
    private var textsmooth = 0F
    private var scroll = 0F
    private var animScroll = 0F
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val wheel = Mouse.getDWheel()
        if (wheel != 0) {
            if (wheel > 0) {
                scroll += 15f
            } else {
                scroll -= 15f
            }
        }
        if (scroll < -200F) {
            scroll = -200F
        }
        if (scroll > 0F) {
            scroll = 0F
        }
        animScroll = animScroll.animSmooth(scroll, 0.5F)
        text = textValue.get()
        RenderUtils.drawRoundedRect(10F, 10F, 628F, 428F, 5F, Color(0, 0, 0, 150).rgb, 3F, ClientTheme.getColor(1).rgb)
        RenderUtils.drawImage(ResourceLocation("fdpclient/gui/design/arrowup.png"), 160, 381, 25, 25)
        RenderUtils.drawImage(ResourceLocation("fdpclient/gui/design/arrowdown.png"), 160, 410, 25, 25)
        FontLoaders.F40.drawStringWithShadow("ClientTheme", 20.0, 25.0, ClientTheme.getColor(1).rgb)
        RenderUtils.drawRect(10F, 65F, 638F, 66F, ClientTheme.getColor(1).rgb)
        RenderUtils.drawRect(10F, 345.0f, 638F, 346.0f, ClientTheme.getColor(1).rgb)
        GL11.glPushMatrix()
        Stencil.write(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtils.fastRoundedRect(21F, 66.5F, 635F, 345F, 0F)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        Stencil.erase(true)
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
            RenderUtils.customRounded(x1 + 2, y1 + 35F + animScroll, x2 - 2, y2 - 2.5F + animScroll, 0F, 0F, 10F, 10F, Color(20, 20, 20).rgb)
            if (ClientTheme.ClientColorMode.get() == mode) {
                RenderUtils.drawRoundedOutline(x1, y1 - 0.5F + animScroll, x2, y2 - 1.3F + animScroll, 23.5F, 4F, Color(255, 255, 255).rgb)
            }
            FontLoaders.F18.drawStringWithShadow(text, textX, textY + animScroll, ClientTheme.getColorFromName(mode, 1).rgb)
        }

        val modes = listOf(
            "Cherry", "Water", "Magic", "DarkNight", "Sun",
            "Tree", "Flower", "Loyoi", "Cero", "Soniga",
            "May", "Mint", "Azure", "Rainbow", "Astolfo",
            "Pumpkin", "Polarized", "Sundae", "Terminal", "Coral",
            "Fire", "Aqua", "Peony"
        )

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

        val gradientColors = List(4) { i -> ClientTheme.getColorFromName(modes[i], i * 90).rgb }

        for (i in modes.indices) {
            val mode = modes[i]
            val position = modePositions[i]
            val textPosition = textPositions[i]
            drawMode(mode, position.first, position.second, position.first + 104.4f, position.second + 74f, gradientColors, mode, textPosition.first, textPosition.second)
        }

        Stencil.dispose()
        GL11.glPopMatrix()
        //Text
        val textColor =
            BlendUtils.blendColors(floatArrayOf(0F, 1F), arrayOf(Color(255, 0, 0), Color(0, 255, 0)), textsmooth).rgb
        textsmooth = textsmooth.animLinear((if (text) 0.2F else -0.2F) * RenderUtils.deltaTime * 0.045F, 0F, 1F)
        RenderUtils.drawRoundedRect(25F, 350.0f, 40F, 365.0f, 5F, textColor)
        RenderUtils.drawRoundedOutline(25F, 350.0f, 40F, 365.0f, 7F, 1F, Color.WHITE.rgb)
        FontLoaders.F30.drawStringWithShadow("Text White Color", 43.0, 351.5, Color(255, 255, 255).rgb)
        FontLoaders.F30.drawStringWithShadow("Fade Side : " + updown.get(), 25.0, 376.5, Color(255, 255, 255).rgb)
        FontLoaders.F30.drawStringWithShadow("FadeSpeed : " + fadespeed.get(), 25.0, 401.5, Color(255, 255, 255).rgb)
        GlStateManager.resetColor()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseWithinBounds(mouseX, mouseY, 25F, 70F + animScroll, 122F, 140F + animScroll)) {
            ClientTheme.ClientColorMode.set("Cherry")
        }

        if (mouseWithinBounds(mouseX, mouseY, 150F, 70F + animScroll, 247F, 140F + animScroll)) {
            ClientTheme.ClientColorMode.set("Water")
        }

        if (mouseWithinBounds(mouseX, mouseY, 275F, 70F + animScroll, 372F, 140F + animScroll)) {
            ClientTheme.ClientColorMode.set("Magic")
        }

        if (mouseWithinBounds(mouseX, mouseY, 400F, 70F + animScroll, 497F, 140F + animScroll)) {
            ClientTheme.ClientColorMode.set("DarkNight")
        }

        if (mouseWithinBounds(mouseX, mouseY, 525F, 70F + animScroll, 622F, 140F + animScroll)) {
            ClientTheme.ClientColorMode.set("Sun")
        }

        //Line 2

        if (mouseWithinBounds(mouseX, mouseY, 25F, 165F + animScroll, 122F, 235F + animScroll)) {
            ClientTheme.ClientColorMode.set("Tree")
        }

        if (mouseWithinBounds(mouseX, mouseY, 150F, 165F + animScroll, 247F, 235F + animScroll)) {
            ClientTheme.ClientColorMode.set("Flower")
        }

        if (mouseWithinBounds(mouseX, mouseY, 275F, 165F + animScroll, 372F, 235F + animScroll)) {
            ClientTheme.ClientColorMode.set("Loyoi")
        }

        if (mouseWithinBounds(mouseX, mouseY, 400F, 165F + animScroll, 497F, 235F + animScroll)) {
            ClientTheme.ClientColorMode.set("Cero")
        }

        if (mouseWithinBounds(mouseX, mouseY, 525F, 165F + animScroll, 622F, 235F + animScroll)) {
            ClientTheme.ClientColorMode.set("Soniga")
        }

        //Line 3

        if (mouseWithinBounds(mouseX, mouseY, 25F, 260F + animScroll, 122F, 330F + animScroll)) {
            ClientTheme.ClientColorMode.set("May")
        }
        if (mouseWithinBounds(mouseX, mouseY, 150F, 260F + animScroll, 247F, 330F + animScroll)) {
            ClientTheme.ClientColorMode.set("Mint")
        }
        if (mouseWithinBounds(mouseX, mouseY, 275F, 260F + animScroll, 372F, 330F + animScroll)) {
            ClientTheme.ClientColorMode.set("Azure")
        }
        if (mouseWithinBounds(mouseX, mouseY, 400F, 260F + animScroll, 497F, 330F + animScroll)) {
            ClientTheme.ClientColorMode.set("Rainbow")
        }
        if (mouseWithinBounds(mouseX, mouseY, 525F, 260F + animScroll, 622F, 330F + animScroll)) {
            ClientTheme.ClientColorMode.set("Astolfo")
        }

        //Line 4
        if (animScroll < -75F) {
            if (mouseWithinBounds(mouseX, mouseY, 25F, 355F + animScroll, 122F, 425F + animScroll)) {
                ClientTheme.ClientColorMode.set("Pumpkin")
            }
            if (mouseWithinBounds(mouseX, mouseY, 150F, 355F + animScroll, 247F, 425F + animScroll)) {
                ClientTheme.ClientColorMode.set("Polarized")
            }
            if (mouseWithinBounds(mouseX, mouseY, 275F, 355F + animScroll, 372F, 425F + animScroll)) {
                ClientTheme.ClientColorMode.set("Sundae")
            }
            if (mouseWithinBounds(mouseX, mouseY, 400F, 355F + animScroll, 497F, 425F + animScroll)) {
                ClientTheme.ClientColorMode.set("Terminal")
            }
            if (mouseWithinBounds(mouseX, mouseY, 525F, 355F + animScroll, 622F, 425F + animScroll)) {
                ClientTheme.ClientColorMode.set("Coral")
            }
        }
        //Line 5
        if (animScroll < -115F) {
            if (mouseWithinBounds(mouseX, mouseY, 25F, 450F + animScroll, 122F, 520F + animScroll)) {
                ClientTheme.ClientColorMode.set("Fire")
            }
            if (mouseWithinBounds(mouseX, mouseY, 150F, 450F + animScroll, 247F, 520F + animScroll)) {
                ClientTheme.ClientColorMode.set("Aqua")
            }
            if (mouseWithinBounds(mouseX, mouseY, 275F, 450F + animScroll, 372F, 520F + animScroll)) {
                ClientTheme.ClientColorMode.set("Peony")
            }

        }

        if (mouseWithinBounds(mouseX, mouseY, 25F, 350.0f, 40F, 365.0f)) {
            textValue.set(!textValue.get())
        }
        if (mouseWithinBounds(mouseX, mouseY, 90f, 375.0f, 140.0f, 390.0f)) {
            updown.set(!updown.get())
        }
        if (mouseWithinBounds(mouseX, mouseY, 160F, 380F, 180F, 400F)) {
            if (fadespeed.get() != 20)
                fadespeed.set(fadespeed.get() + 1)
        }
        if (mouseWithinBounds(mouseX, mouseY, 160F, 410F, 180F, 430F)) {
            if (fadespeed.get() != 0)
                fadespeed.set(fadespeed.get() - 1)
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}