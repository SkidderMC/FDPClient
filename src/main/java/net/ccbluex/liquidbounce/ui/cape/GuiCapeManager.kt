/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.cape

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import org.lwjgl.opengl.GL11
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

object GuiCapeManager : GuiScreen() {
    private val jsonFile = File(LiquidBounce.fileManager.capesDir, "cape.json")

    private val embeddedCapes = mutableListOf<ICape>()

    var nowCape: ICape?
    val capeList = mutableListOf<ICape>()

    init {
        arrayOf("classic", "classic2", "aurora", "forest", "rose", "lavender", "ocean", "modern1", "modern2", "lava", "citrus", "fire", "nightlife", "abstract", "blur", "owner").forEach {
            try {
                embeddedCapes.add(loadCapeFromResource(it, "assets/minecraft/fdpclient/cape/$it.png"))
            } catch (e: Throwable){
                System.out.println("Failed to load Capes")
            }
        }
        nowCape = embeddedCapes.random()
        pushEmbeddedCape()
    }

    private fun pushEmbeddedCape() {
        capeList.addAll(embeddedCapes)
    }

    fun load() {
        capeList.clear()

        pushEmbeddedCape()

        // add capes from files
        for (file in LiquidBounce.fileManager.capesDir.listFiles()) {
            if (file.isFile && !file.name.equals(jsonFile.name)) {
                try {
                    val args = file.name.split(".").toTypedArray()
                    val name = java.lang.String.join(".", *args.copyOfRange(0, args.size - 1))
                    capeList.add(if(args.last() == "gif") {
                        loadGifCapeFromFile(name, file)
                    } else {
                        loadCapeFromFile(name, file)
                    })
                } catch (e: Exception) {
                    ClientUtils.logError("Occurred an error while loading cape from file: ${file.name}")
                    e.printStackTrace()
                }
            }
        }

        if (!jsonFile.exists()) {
            return
        }

        val json = JsonParser().parse(jsonFile.reader(Charsets.UTF_8)).asJsonObject

        if (json.has("name")) {
            val name = json.get("name").asString
            if (!name.equals("NONE")) {
                val result = capeList.find { it.name == name } ?: embeddedCapes.random()
                nowCape = result
            }
        }
    }

    fun save() {
        val json = JsonObject()

        json.addProperty("name", if (nowCape != null) { nowCape!!.name } else { "NONE" })

        jsonFile.writeText(FileManager.PRETTY_GSON.toJson(json), Charsets.UTF_8)
    }

    private fun loadCapeFromResource(name: String, loc: String) = SingleImageCape(name, ImageIO.read(GuiCapeManager::class.java.classLoader.getResourceAsStream(loc)))

    private fun loadCapeFromFile(name: String, file: File) = SingleImageCape(name, ImageIO.read(file))

    private fun loadGifCapeFromResource(name: String, loc: String) = GifCape(name, GuiCapeManager::class.java.classLoader.getResourceAsStream(loc))

    private fun loadGifCapeFromFile(name: String, file: File) = GifCape(name, FileInputStream(file))

    override fun onGuiClosed() {
        save()
    }

    // render
    override fun initGui() {
        this.buttonList.add(GuiButton(0, 0, 0, mc.fontRendererObj.getStringWidth("< QUIT") + 10, 20, "< QUIT"))
        this.buttonList.add(GuiButton(1, (width * 0.3).toInt(), (height * 0.5).toInt(), mc.fontRendererObj.getStringWidth("<-") + 10, 20, "<-"))
        this.buttonList.add(GuiButton(2, (width * 0.7).toInt(), (height * 0.5).toInt(), mc.fontRendererObj.getStringWidth("->") + 10, 20, "->"))
    }

    override fun actionPerformed(p_actionPerformed_1_: GuiButton) {
        fun next(index: Int) {
            var chooseIndex = index
            if (chooseIndex >= capeList.size) {
                chooseIndex = -1
            }

            if (chooseIndex < -1) {
                chooseIndex = capeList.size - 1
            }

            nowCape = if (chooseIndex != -1) {
                capeList[chooseIndex]
            } else {
                null
            }
        }

        when (p_actionPerformed_1_.id) {
            0 -> mc.displayGuiScreen(null)
            1 -> next(capeList.indexOf(nowCape) - 1)
            2 -> next(capeList.indexOf(nowCape) + 1)
        }
    }

    override fun drawScreen(p_drawScreen_1_: Int, p_drawScreen_2_: Int, p_drawScreen_3_: Float) {
        // draw background
        this.drawDefaultBackground()

        GL11.glPushMatrix()
        mc.fontRendererObj.drawCenteredString(if (nowCape == null) { "§cNONE" } else { "§a${nowCape!!.name}" }, width * 0.50f, height * 0.23f, -1, false)
        GL11.glScalef(2f, 2f, 2f)
        mc.fontRendererObj.drawCenteredString("Cape Manager", width * 0.25f, height * 0.03f, -1, false)
        GL11.glPopMatrix()

        // draw buttons
        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_)

        // draw entity
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
    }

    override fun doesGuiPauseGame() = false
}
