/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.colortheme

import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme.fadespeed
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme.textValue
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme.updown
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.newVer.extensions.animLinear
import net.ccbluex.liquidbounce.ui.font.cf.FontLoaders
import net.ccbluex.liquidbounce.utils.MouseUtils.mouseWithinBounds
import net.ccbluex.liquidbounce.utils.render.BlendUtils
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
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val wheel = Mouse.getDWheel()
        if (wheel != 0) {
                if (wheel > 0) {
                    scroll += 15f
                } else {
                    scroll -= 15f
                }
        }
        if (scroll < -100F) {
            scroll = -100F
        }
        if (scroll > 0F) {
            scroll = 0F
        }
        text = textValue.get()
        RenderUtils.drawRoundedRect(10F, 10F, 628F, 428F, 5F, Color(0, 0, 0, 150).rgb, 3F, ClientTheme.getColor(1).rgb)
        RenderUtils.drawImage(ResourceLocation("fdpclient/misc/arrowup.png"), 160, 381, 25, 25)
        RenderUtils.drawImage(ResourceLocation("fdpclient/misc/arrowdown.png"), 160, 410, 25, 25)
        FontLoaders.SF40.drawStringWithShadow("ClientTheme", 20.0, 25.0, ClientTheme.getColor(1).rgb)
        RenderUtils.drawRect(10F, 65F, 638F, 66F, ClientTheme.getColor(1).rgb)
        RenderUtils.drawRect(10F, 345.0f, 638F, 346.0f, ClientTheme.getColor(1).rgb)
        GL11.glPushMatrix()
        Stencil.write(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtils.fastRoundedRect(22.15F, 66.5F, 630F, 345F, 0F)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        Stencil.erase(true)
        /**
         * Line 1
         */
        //Zywl
        if (ClientTheme.ClientColorMode.equals("Zywl"))
            RenderUtils.drawRoundedOutline(
                22F,
                68F + scroll,
                126.4F,
                142F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            24F,
            69.5F + scroll,
            124.5F,
            140F + scroll,
            20F,
            ClientTheme.getColorFromName("Zywl", 0).rgb,
            ClientTheme.getColorFromName("Zywl", 90).rgb,
            ClientTheme.getColorFromName("Zywl", 180).rgb,
            ClientTheme.getColorFromName("Zywl", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Zywl",
            58.0,
            145.0 + scroll,
            ClientTheme.getColorFromName("Zywl", 1).rgb
        )
        //FDP
        if (ClientTheme.ClientColorMode.equals("FDP"))
            RenderUtils.drawRoundedOutline(
                147F,
                68F + scroll,
                251.4F,
                142F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            149F,
            69.5F + scroll,
            249.5F,
            140F + scroll,
            20F,
            ClientTheme.getColorFromName("FDP", 0).rgb,
            ClientTheme.getColorFromName("FDP", 90).rgb,
            ClientTheme.getColorFromName("FDP", 180).rgb,
            ClientTheme.getColorFromName("FDP", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "FDP",
            184.0,
            145.0 + scroll,
            ClientTheme.getColorFromName("FDP", 1).rgb
        )
        //Magic
        if (ClientTheme.ClientColorMode.equals("Magic"))
            RenderUtils.drawRoundedOutline(
                272.0f,
                68F + scroll,
                251.4F + 125F,
                142F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            274F,
            69.5F + scroll,
            374.5F,
            140F + scroll,
            20F,
            ClientTheme.getColorFromName("Magic", 0).rgb,
            ClientTheme.getColorFromName("Magic", 90).rgb,
            ClientTheme.getColorFromName("Magic", 180).rgb,
            ClientTheme.getColorFromName("Magic", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Magic",
            310.0,
            145.0 + scroll,
            ClientTheme.getColorFromName("Magic", 1).rgb
        )
        //DarkNight
        if (ClientTheme.ClientColorMode.equals("DarkNight"))
            RenderUtils.drawRoundedOutline(
                397.0f,
                68F + scroll,
                501.4f,
                142F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            399F,
            69.5F + scroll,
            499.5F,
            140F + scroll,
            20F,
            ClientTheme.getColorFromName("DarkNight", 0).rgb,
            ClientTheme.getColorFromName("DarkNight", 90).rgb,
            ClientTheme.getColorFromName("DarkNight", 180).rgb,
            ClientTheme.getColorFromName("DarkNight", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "DarkNight",
            427.0,
            145.0 + scroll,
            ClientTheme.getColorFromName("DarkNight", 1).rgb
        )
        //Sun
        if (ClientTheme.ClientColorMode.equals("Sun"))
            RenderUtils.drawRoundedOutline(
                522.0f,
                68F + scroll,
                626.4f,
                142F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            524F,
            69.5F + scroll,
            624.5F,
            140F + scroll,
            20F,
            ClientTheme.getColorFromName("Sun", 0).rgb,
            ClientTheme.getColorFromName("Sun", 90).rgb,
            ClientTheme.getColorFromName("Sun", 180).rgb,
            ClientTheme.getColorFromName("Sun", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow("Sun", 565.0, 145.0 + scroll, ClientTheme.getColorFromName("Sun", 1).rgb)

        /**
         * Line 2
         */

        //Tree
        if (ClientTheme.ClientColorMode.equals("Tree"))
            RenderUtils.drawRoundedOutline(
                22f,
                163.0f + scroll,
                126.4f,
                237.0f + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            24F,
            164.5F + scroll,
            124.5F,
            235F + scroll,
            20F,
            ClientTheme.getColorFromName("Tree", 0).rgb,
            ClientTheme.getColorFromName("Tree", 90).rgb,
            ClientTheme.getColorFromName("Tree", 180).rgb,
            ClientTheme.getColorFromName("Tree", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow("Tree", 60.0, 240.0 + scroll, ClientTheme.getColorFromName("Tree", 1).rgb)
        //Flower
        if (ClientTheme.ClientColorMode.equals("Flower"))
            RenderUtils.drawRoundedOutline(
                147.0f,
                163.0f + scroll,
                251.4f,
                237f + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            149F,
            164.5F + scroll,
            249.5F,
            235F + scroll,
            20F,
            ClientTheme.getColorFromName("Flower", 0).rgb,
            ClientTheme.getColorFromName("Flower", 90).rgb,
            ClientTheme.getColorFromName("Flower", 180).rgb,
            ClientTheme.getColorFromName("Flower", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Flower",
            184.0,
            240.0 + scroll,
            ClientTheme.getColorFromName("Flower", 1).rgb
        )
        //Loyoi
        if (ClientTheme.ClientColorMode.equals("Loyoi"))
            RenderUtils.drawRoundedOutline(
                272.0f,
                163.0f + scroll,
                376.4f,
                237f + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            274F,
            164.5F + scroll,
            374.5F,
            235F + scroll,
            20F,
            ClientTheme.getColorFromName("Loyoi", 0).rgb,
            ClientTheme.getColorFromName("Loyoi", 90).rgb,
            ClientTheme.getColorFromName("Loyoi", 180).rgb,
            ClientTheme.getColorFromName("Loyoi", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Loyoi",
            310.0,
            240.0 + scroll,
            ClientTheme.getColorFromName("Loyoi", 1).rgb
        )
        //Cero
        if (ClientTheme.ClientColorMode.equals("Cero"))
            RenderUtils.drawRoundedOutline(
                397.0f,
                163.0f + scroll,
                501.4f,
                237f + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            399F,
            164.5F + scroll,
            499.5F,
            235F + scroll,
            20F,
            ClientTheme.getColorFromName("Cero", 0).rgb,
            ClientTheme.getColorFromName("Cero", 90).rgb,
            ClientTheme.getColorFromName("Cero", 180).rgb,
            ClientTheme.getColorFromName("Cero", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Cero",
            434.0,
            240.0 + scroll,
            ClientTheme.getColorFromName("Cero", 1).rgb
        )
        //Soniga
        if (ClientTheme.ClientColorMode.equals("Soniga"))
            RenderUtils.drawRoundedOutline(
                522.0f,
                163.0f + scroll,
                626.4f,
                237f + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            524F,
            164.5F + scroll,
            624.5F,
            235F + scroll,
            20F,
            ClientTheme.getColorFromName("Soniga", 0).rgb,
            ClientTheme.getColorFromName("Soniga", 90).rgb,
            ClientTheme.getColorFromName("Soniga", 180).rgb,
            ClientTheme.getColorFromName("Soniga", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Soniga",
            560.0,
            240.0 + scroll,
            ClientTheme.getColorFromName("Soniga", 1).rgb
        )

        /**
         * Line 3
         */

        if (ClientTheme.ClientColorMode.equals("May"))
            RenderUtils.drawRoundedOutline(
                22f,
                258.0f + scroll,
                126.4f,
                332.0f + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            24F,
            259.5f + scroll,
            124.5F,
            330.0f + scroll,
            20F,
            ClientTheme.getColorFromName("May", 0).rgb,
            ClientTheme.getColorFromName("May", 90).rgb,
            ClientTheme.getColorFromName("May", 180).rgb,
            ClientTheme.getColorFromName("May", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow("May", 60.0, 335.0 + scroll, ClientTheme.getColorFromName("May", 1).rgb)
        //Flower
        if (ClientTheme.ClientColorMode.equals("Mint"))
            RenderUtils.drawRoundedOutline(
                147.0f,
                258.0f + scroll,
                251.4f,
                332.0f + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            149F,
            259.5f + scroll,
            249.5F,
            330.0f + scroll,
            20F,
            ClientTheme.getColorFromName("Mint", 0).rgb,
            ClientTheme.getColorFromName("Mint", 90).rgb,
            ClientTheme.getColorFromName("Mint", 180).rgb,
            ClientTheme.getColorFromName("Mint", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Mint",
            187.0,
            335.0 + scroll,
            ClientTheme.getColorFromName("Mint", 1).rgb
        )
        //Azure
        if (ClientTheme.ClientColorMode.equals("Azure"))
            RenderUtils.drawRoundedOutline(
                272.0f,
                258.0f + scroll,
                376.4f,
                332F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            274F,
            259.5F + scroll,
            374.5F,
            330F + scroll,
            20F,
            ClientTheme.getColorFromName("Azure", 0).rgb,
            ClientTheme.getColorFromName("Azure", 90).rgb,
            ClientTheme.getColorFromName("Azure", 180).rgb,
            ClientTheme.getColorFromName("Azure", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Azure",
            310.0,
            335.0 + scroll,
            ClientTheme.getColorFromName("Azure", 1).rgb
        )
        //Rainbow
        if (ClientTheme.ClientColorMode.equals("Rainbow"))
            RenderUtils.drawRoundedOutline(
                397.0f,
                258.0f + scroll,
                501.4f,
                332F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            399F,
            259.5F + scroll,
            499.5F,
            330F + scroll,
            20F,
            ClientTheme.getColorFromName("Rainbow", 0).rgb,
            ClientTheme.getColorFromName("Rainbow", 90).rgb,
            ClientTheme.getColorFromName("Rainbow", 180).rgb,
            ClientTheme.getColorFromName("Rainbow", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Rainbow",
            428.0,
            335.0 + scroll,
            ClientTheme.getColorFromName("Rainbow", 1).rgb
        )
        if (ClientTheme.ClientColorMode.equals("Astolfo"))
            RenderUtils.drawRoundedOutline(
                522.0f,
                258.0f + scroll,
                626.4f,
                332F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            524.0f,
            259.5F + scroll,
            624.5f,
            330F + scroll,
            20F,
            ClientTheme.getColorFromName("Astolfo", 0).rgb,
            ClientTheme.getColorFromName("Astolfo", 90).rgb,
            ClientTheme.getColorFromName("Astolfo", 180).rgb,
            ClientTheme.getColorFromName("Astolfo", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Astolfo",
            560.0,
            335.0 + scroll,
            ClientTheme.getColorFromName("Astolfo", 1).rgb
        )

        /**
         * Line 4
         */

        if (ClientTheme.ClientColorMode.equals("Pumpkin"))
            RenderUtils.drawRoundedOutline(
                22f,
                353F + scroll,
                126.4f,
                427F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            24F,
            354.5F + scroll,
            124.5F,
            425F + scroll,
            20F,
            ClientTheme.getColorFromName("Pumpkin", 0).rgb,
            ClientTheme.getColorFromName("Pumpkin", 90).rgb,
            ClientTheme.getColorFromName("Pumpkin", 180).rgb,
            ClientTheme.getColorFromName("Pumpkin", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Pumpkin",
            53.0,
            430.0 + scroll,
            ClientTheme.getColorFromName("Pumpkin", 1).rgb
        )
        //Flower
        if (ClientTheme.ClientColorMode.equals("Polarized"))
            RenderUtils.drawRoundedOutline(
                147.0f,
                353F + scroll,
                251.4f,
                427F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            149F,
            354.5F + scroll,
            249.5F,
            425F + scroll,
            20F,
            ClientTheme.getColorFromName("Polarized", 0).rgb,
            ClientTheme.getColorFromName("Polarized", 90).rgb,
            ClientTheme.getColorFromName("Polarized", 180).rgb,
            ClientTheme.getColorFromName("Polarized", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Polarized",
            180.0,
            430.0 + scroll,
            ClientTheme.getColorFromName("Polarized", 1).rgb
        )
        //Azure
        if (ClientTheme.ClientColorMode.equals("Sundae"))
            RenderUtils.drawRoundedOutline(
                272.0f,
                353F + scroll,
                376.4f,
                427F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            274F,
            354.5F + scroll,
            374.5F,
            425F + scroll,
            20F,
            ClientTheme.getColorFromName("Sundae", 0).rgb,
            ClientTheme.getColorFromName("Sundae", 90).rgb,
            ClientTheme.getColorFromName("Sundae", 180).rgb,
            ClientTheme.getColorFromName("Sundae", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Sundae",
            305.0,
            430.0 + scroll,
            ClientTheme.getColorFromName("Sundae", 1).rgb
        )
        //Rainbow
        if (ClientTheme.ClientColorMode.equals("Terminal"))
            RenderUtils.drawRoundedOutline(
                397.0f,
                353F + scroll,
                501.4f,
                427F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            399F,
            354.5F + scroll,
            499.5F,
            425F + scroll,
            20F,
            ClientTheme.getColorFromName("Terminal", 0).rgb,
            ClientTheme.getColorFromName("Terminal", 90).rgb,
            ClientTheme.getColorFromName("Terminal", 180).rgb,
            ClientTheme.getColorFromName("Terminal", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Terminal",
            428.0,
            430.0 + scroll,
            ClientTheme.getColorFromName("Terminal", 1).rgb
        )
        if (ClientTheme.ClientColorMode.equals("Coral"))
            RenderUtils.drawRoundedOutline(
                522.0f,
                353F + scroll,
                626.4f,
                427F + scroll,
                23.5F,
                4F,
                Color(255, 255, 255).rgb
            )
        RenderUtils.drawRoundedGradientRectCorner(
            524.0f,
            354.5F + scroll,
            624.5f,
            425F + scroll,
            20F,
            ClientTheme.getColorFromName("Coral", 0).rgb,
            ClientTheme.getColorFromName("Coral", 90).rgb,
            ClientTheme.getColorFromName("Coral", 180).rgb,
            ClientTheme.getColorFromName("Coral", 270).rgb
        )
        FontLoaders.SF20.drawStringWithShadow(
            "Coral",
            563.0,
            430.0 + scroll,
            ClientTheme.getColorFromName("Coral", 1).rgb
        )

        Stencil.dispose()
        GL11.glPopMatrix()
        //Text
        val textColor =
            BlendUtils.blendColors(floatArrayOf(0F, 1F), arrayOf(Color(255, 0, 0), Color(0, 255, 0)), textsmooth).rgb
        textsmooth = textsmooth.animLinear((if (text) 0.2F else -0.2F) * RenderUtils.deltaTime * 0.045F, 0F, 1F)
        RenderUtils.drawRoundedRect(25F, 350.0f, 40F, 365.0f, 5F, textColor)
        RenderUtils.drawRoundedOutline(25F, 350.0f, 40F, 365.0f, 7F, 1F, Color.WHITE.rgb)
        FontLoaders.SF30.drawStringWithShadow("Text White Color", 43.0, 351.5, Color(255, 255, 255).rgb)
        FontLoaders.SF30.drawStringWithShadow("Fade Side : " + updown.get(), 25.0, 376.5, Color(255, 255, 255).rgb)
        FontLoaders.SF30.drawStringWithShadow("FadeSpeed : " + fadespeed.get(), 25.0, 401.5, Color(255, 255, 255).rgb)
        GlStateManager.resetColor()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseWithinBounds(mouseX, mouseY, 25F, 70F + scroll, 122F, 140F + scroll)) {
            ClientTheme.ClientColorMode.set("Zywl")
        }

        if (mouseWithinBounds(mouseX, mouseY, 150F, 70F + scroll, 247F, 140F + scroll)) {
            ClientTheme.ClientColorMode.set("FDP")
        }

        if (mouseWithinBounds(mouseX, mouseY, 275F, 70F + scroll, 372F, 140F + scroll)) {
            ClientTheme.ClientColorMode.set("Magic")
        }

        if (mouseWithinBounds(mouseX, mouseY, 400F, 70F + scroll, 497F, 140F + scroll)) {
            ClientTheme.ClientColorMode.set("DarkNight")
        }

        if (mouseWithinBounds(mouseX, mouseY, 525F, 70F + scroll, 622F, 140F + scroll)) {
            ClientTheme.ClientColorMode.set("Sun")
        }

        //Line 2

        if (mouseWithinBounds(mouseX, mouseY, 25F, 165F + scroll, 122F, 235F + scroll)) {
            ClientTheme.ClientColorMode.set("Tree")
        }

        if (mouseWithinBounds(mouseX, mouseY, 150F, 165F + scroll, 247F, 235F + scroll)) {
            ClientTheme.ClientColorMode.set("Flower")
        }

        if (mouseWithinBounds(mouseX, mouseY, 275F, 165F + scroll, 372F, 235F + scroll)) {
            ClientTheme.ClientColorMode.set("Loyoi")
        }

        if (mouseWithinBounds(mouseX, mouseY, 400F, 165F + scroll, 497F, 235F + scroll)) {
            ClientTheme.ClientColorMode.set("Cero")
        }

        if (mouseWithinBounds(mouseX, mouseY, 525F, 165F + scroll, 622F, 235F + scroll)) {
            ClientTheme.ClientColorMode.set("Soniga")
        }

        //Line 3

        if (mouseWithinBounds(mouseX, mouseY, 25F, 260F + scroll, 122F, 330F + scroll)) {
            ClientTheme.ClientColorMode.set("May")
        }
        if (mouseWithinBounds(mouseX, mouseY, 150F, 260F + scroll, 247F, 330F + scroll)) {
            ClientTheme.ClientColorMode.set("Mint")
        }
        if (mouseWithinBounds(mouseX, mouseY, 275F, 260F + scroll, 372F, 330F + scroll)) {
            ClientTheme.ClientColorMode.set("Azure")
        }
        if (mouseWithinBounds(mouseX, mouseY, 400F, 260F + scroll, 497F, 330F + scroll)) {
            ClientTheme.ClientColorMode.set("Rainbow")
        }
        if (mouseWithinBounds(mouseX, mouseY, 525F, 260F + scroll, 622F, 330F + scroll)) {
            ClientTheme.ClientColorMode.set("Astolfo")
        }

        //Line 4
        if (scroll < -75F) {
            if (mouseWithinBounds(mouseX, mouseY, 25F, 355F + scroll, 122F, 425F + scroll)) {
                ClientTheme.ClientColorMode.set("Pumpkin")
            }
            if (mouseWithinBounds(mouseX, mouseY, 150F, 355F + scroll, 247F, 425F + scroll)) {
                ClientTheme.ClientColorMode.set("Polarized")
            }
            if (mouseWithinBounds(mouseX, mouseY, 275F, 355F + scroll, 372F, 425F + scroll)) {
                ClientTheme.ClientColorMode.set("Sundae")
            }
            if (mouseWithinBounds(mouseX, mouseY, 400F, 355F + scroll, 497F, 425F + scroll)) {
                ClientTheme.ClientColorMode.set("Terminal")
            }
            if (mouseWithinBounds(mouseX, mouseY, 525F, 355F + scroll, 622F, 425F + scroll)) {
                ClientTheme.ClientColorMode.set("Coral")
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