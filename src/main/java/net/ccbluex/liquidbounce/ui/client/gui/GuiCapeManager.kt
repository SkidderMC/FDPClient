/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.APIConnecter
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

object GuiCapeManager : GuiScreen() {

    val customCape = BoolValue("CustomCape", true)
    val styleValue = ListValue(
        "Mode",
        arrayOf(
            "classic", "classic2", "aurora", "forest", "rose", "lavender",
            "ocean", "modern1", "modern2", "lava", "citrus", "fire", "blue", "abstract", "owner"
        ),
        "classic"
    ) { customCape.get() }

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
        NONE(APIConnecter.callImage("none", "cape")),
        CLASSIC(ResourceLocation("fdpclient/cape/classic.png")),
        CLASSIC2(ResourceLocation("fdpclient/cape/classic2.png")),
        AURORA(APIConnecter.callImage("aurora", "cape")),
        FOREST(APIConnecter.callImage("forest", "cape")),
        ROSE(APIConnecter.callImage("rose", "cape")),
        LAVENDER(APIConnecter.callImage("lavender", "cape")),
        OCEAN(APIConnecter.callImage("ocean", "cape")),
        MODERN1(APIConnecter.callImage("modern1", "cape")),
        MODERN2(APIConnecter.callImage("modern2", "cape")),
        LAVA(APIConnecter.callImage("lava", "cape")),
        CITRUS(APIConnecter.callImage("citrus", "cape")),
        FIRE(APIConnecter.callImage("fire", "cape")),
        BLUE(APIConnecter.callImage("blue", "cape")),
        ABSTRACT(APIConnecter.callImage("abstract", "cape")),
        OWNER(APIConnecter.callImage("owner", "cape")),
    }

    override fun onGuiClosed() {

    }

    override fun initGui() {
        this.buttonList.add(GuiButton(0, 0, 0, mc.fontRendererObj.getStringWidth("< QUIT") + 10, 20, "< QUIT"))
        this.buttonList.add(GuiButton(1, (width * 0.3).toInt(), (height * 0.5).toInt(), mc.fontRendererObj.getStringWidth("<-") + 10, 20, "<-"))
        this.buttonList.add(GuiButton(2, (width * 0.7).toInt(), (height * 0.5).toInt(), mc.fontRendererObj.getStringWidth("->") + 10, 20, "->"))
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
        this.drawDefaultBackground()

        GL11.glPushMatrix()
        Fonts.font35.drawCenteredStringWithShadow(
            if (nowCape == null) "§cNONE" else "§a${nowCape!!.name}",
            width * 0.50f,
            height * 0.23f,
            -1
        )
        GL11.glScalef(2f, 2f, 2f)
        Fonts.font35.drawCenteredStringWithoutShadow("Cape Manager", width * 0.25f, height * 0.03f, -1)
        GL11.glPopMatrix()

        super.drawScreen(mouseX, mouseY, partialTicks)

        mc.thePlayer ?: return
        GL11.glEnable(GL11.GL_CULL_FACE)
        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)
        GlStateManager.enableColorMaterial()
        GlStateManager.pushMatrix()
        GL11.glTranslatef(width * 0.5f - 60, height * 0.3f, 0f)
        GL11.glScalef(2f, 2f, 2f)
        GL11.glTranslatef(30f, 100f, 0f)
        GlStateManager.translate(0F, 0F, 50F)
        GlStateManager.scale(-50F, 50F, 50F)
        GlStateManager.rotate(180F, 0F, 0F, 1F)

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

        GlStateManager.rotate(135F, 0F, 1F, 0F)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135F, 0F, 1F, 0F)
        GlStateManager.rotate(0f, 1F, 0F, 0F)

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

        GlStateManager.translate(0F, 0F, 0F)

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

        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.resetColor()

        RenderUtils.drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))
    }

    override fun doesGuiPauseGame() = false
}
