/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.io.APIConnectorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*

object GuiCapeManager : AbstractScreen() {

    val customCape = BoolValue("CustomCape", true)
    val styleValue = ListValue(
        "Mode",
        arrayOf(
            "classic", "classic2", "aurora", "forest", "rose", "lavender",
            "ocean", "modern1", "modern2", "lava", "citrus", "fire", "blue", "abstract", "owner"
        ),
        "classic"
    ).apply {
        setSupport { customCape.get() }
    }

    private val capeCache = hashMapOf<String, CapeStyle>()
    private var nowCape: CapeStyle? = null

    fun getCapeLocation(value: String): ResourceLocation? {
        val upperValue = value.uppercase(Locale.getDefault())
        if (capeCache[upperValue] == null) {
            capeCache[upperValue] = CapeStyle.valueOf(upperValue)
        }
        return capeCache[upperValue]?.location
    }

    enum class CapeStyle(val location: ResourceLocation?) {
        NONE(APIConnectorUtils.callImage("none", "cape")),
        CLASSIC(ResourceLocation("fdpclient/cape/classic.png")),
        CLASSIC2(ResourceLocation("fdpclient/cape/classic2.png")),
        AURORA(APIConnectorUtils.callImage("aurora", "cape")),
        FOREST(APIConnectorUtils.callImage("forest", "cape")),
        ROSE(APIConnectorUtils.callImage("rose", "cape")),
        LAVENDER(APIConnectorUtils.callImage("lavender", "cape")),
        OCEAN(APIConnectorUtils.callImage("ocean", "cape")),
        MODERN1(APIConnectorUtils.callImage("modern1", "cape")),
        MODERN2(APIConnectorUtils.callImage("modern2", "cape")),
        LAVA(APIConnectorUtils.callImage("lava", "cape")),
        CITRUS(APIConnectorUtils.callImage("citrus", "cape")),
        FIRE(APIConnectorUtils.callImage("fire", "cape")),
        BLUE(APIConnectorUtils.callImage("blue", "cape")),
        ABSTRACT(APIConnectorUtils.callImage("abstract", "cape")),
        OWNER(APIConnectorUtils.callImage("owner", "cape")),
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
    }

    override fun initGui() {
        +GuiButton(0, 0, 0, mc.fontRendererObj.getStringWidth("< QUIT") + 10, 20, "< QUIT")
        +GuiButton(1, (width * 0.3).toInt(), (height * 0.5).toInt(), mc.fontRendererObj.getStringWidth("<-") + 10, 20, "<-")
        +GuiButton(2, (width * 0.7).toInt(), (height * 0.5).toInt(), mc.fontRendererObj.getStringWidth("->") + 10, 20, "->")
        updateCapeStyle()
    }

    private fun updateCapeStyle() {
        nowCape = CapeStyle.valueOf(styleValue.value.uppercase(Locale.getDefault()))
    }

    override fun actionPerformed(button: GuiButton) {
        fun next(index: Int) {
            var chooseIndex = index
            if (chooseIndex >= styleValue.values.size) {
                chooseIndex = 0
            }

            if (chooseIndex < 0) {
                chooseIndex = styleValue.values.size - 1
            }

            styleValue.value = styleValue.values[chooseIndex]
            updateCapeStyle()
        }

        when (button.id) {
            0 -> mc.displayGuiScreen(null)
            1 -> next(styleValue.values.indexOf(nowCape?.name?.lowercase(Locale.getDefault())) - 1)
            2 -> next(styleValue.values.indexOf(nowCape?.name?.lowercase(Locale.getDefault())) + 1)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        assumeNonVolatile = false

        this.drawDefaultBackground()

        glPushMatrix()
        Fonts.fontSemibold35.drawCenteredString(
            if (nowCape == null) "§cNONE" else "§a${nowCape!!.name}",
            width * 0.50f,
            height * 0.23f,
            -1
        )
        glScalef(2f, 2f, 2f)
        Fonts.fontSemibold35.drawCenteredString("Cape Manager", width * 0.25f, height * 0.03f, -1)
        glPopMatrix()

        super.drawScreen(mouseX, mouseY, partialTicks)

        mc.thePlayer ?: return
        glEnable(GL_CULL_FACE)
        resetColor()
        glColor4f(1F, 1F, 1F, 1F)
        enableColorMaterial()
        pushMatrix()
        glTranslatef(width * 0.5f - 60, height * 0.3f, 0f)
        glScalef(2f, 2f, 2f)
        glTranslatef(30f, 100f, 0f)
        translate(0F, 0F, 50F)
        scale(-50F, 50F, 50F)
        rotate(180F, 0F, 0F, 1F)

        val renderYawOffset = mc.thePlayer.renderYawOffset
        val rotationYaw = mc.thePlayer.rotationYaw
        val rotationPitch = mc.thePlayer.rotationPitch
        val prevRotationYawHead = mc.thePlayer.prevRotationYawHead
        val rotationYawHead = mc.thePlayer.rotationYawHead
        val armor0 = mc.thePlayer.inventory.armorInventory[0]
        val armor1 = mc.thePlayer.inventory.armorInventory[1]
        val armor2 = mc.thePlayer.inventory.armorInventory[2]
        val armor3 = mc.thePlayer.inventory.armorInventory[3]
        val current = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem]

        rotate(135F, 0F, 1F, 0F)
        RenderHelper.enableStandardItemLighting()
        rotate(-135F, 0F, 1F, 0F)
        rotate(0f, 1F, 0F, 0F)

        mc.thePlayer.renderYawOffset = 180f
        mc.thePlayer.rotationYaw = 180f
        mc.thePlayer.rotationPitch = 0f
        mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw
        mc.thePlayer.prevRotationYawHead = mc.thePlayer.rotationYaw
        mc.thePlayer.inventory.armorInventory[0] = null
        mc.thePlayer.inventory.armorInventory[1] = null
        mc.thePlayer.inventory.armorInventory[2] = null
        mc.thePlayer.inventory.armorInventory[3] = null
        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = null

        translate(0F, 0F, 0F)

        val renderManager = mc.renderManager
        renderManager.setPlayerViewY(180F)
        renderManager.isRenderShadow = false
        renderManager.renderEntityWithPosYaw(mc.thePlayer, 0.0, 0.0, 0.0, 0F, 1F)
        renderManager.isRenderShadow = true

        mc.thePlayer.renderYawOffset = renderYawOffset
        mc.thePlayer.rotationYaw = rotationYaw
        mc.thePlayer.rotationPitch = rotationPitch
        mc.thePlayer.prevRotationYawHead = prevRotationYawHead
        mc.thePlayer.rotationYawHead = rotationYawHead
        mc.thePlayer.inventory.armorInventory[0] = armor0
        mc.thePlayer.inventory.armorInventory[1] = armor1
        mc.thePlayer.inventory.armorInventory[2] = armor2
        mc.thePlayer.inventory.armorInventory[3] = armor3
        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = current

        popMatrix()
        RenderHelper.disableStandardItemLighting()
        disableRescaleNormal()
        setActiveTexture(OpenGlHelper.lightmapTexUnit)
        disableTexture2D()
        setActiveTexture(OpenGlHelper.defaultTexUnit)
        resetColor()

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        assumeNonVolatile = false
    }

    override fun doesGuiPauseGame() = false
}