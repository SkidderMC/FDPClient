/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts.minecraftFont
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.colorFromDisplayName
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.interpolate
import net.ccbluex.liquidbounce.utils.render.RenderUtils.isInViewFrustum
import net.ccbluex.liquidbounce.utils.render.RenderUtils.newDrawRect
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager.disableBlend
import net.minecraft.client.renderer.GlStateManager.disableRescaleNormal
import net.minecraft.client.renderer.GlStateManager.enableBlend
import net.minecraft.client.renderer.GlStateManager.enableRescaleNormal
import net.minecraft.client.renderer.GlStateManager.popMatrix
import net.minecraft.client.renderer.GlStateManager.pushMatrix
import net.minecraft.client.renderer.GlStateManager.scale
import net.minecraft.client.renderer.GlStateManager.translate
import net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate
import net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting
import net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.awt.Color
import java.awt.Color.getHSBColor
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.text.DecimalFormat
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

object ESP2D : Module("ESP2D", Category.VISUAL) {

    val outline by boolean("Outline", true)
    val boxMode by choices("Mode", arrayOf("Box", "Corners"), "Box")

    val healthBar by boolean("Health-bar", true)
    val hpBarMode by choices("HBar-Mode", arrayOf("Dot", "Line"), "Dot")

    val absorption by boolean("Render-Absorption", true)

    val armorBar by boolean("Armor-bar", true)
    val armorBarMode by choices("ABar-Mode", arrayOf("Total", "Items"), "Total")

    val healthNumber by boolean("HealthNumber", true)
    val hpMode by choices("HP-Mode", arrayOf("Health", "Percent"), "Health")

    val armorNumber by boolean("ItemArmorNumber", true)
    val armorItems by boolean("ArmorItems", true)
    val armorDur by boolean("ArmorDurability", true)

    val hover by boolean("Details-HoverOnly", false)
    val tags by boolean("Tags", true)
    val tagsBG by boolean("Tags-Background", true)
    val itemTags by boolean("Item-Tags", true)

    val outlineFont by boolean("OutlineFont", true)
    val clearName by boolean("Use-Clear-Name", false)

    val localPlayer by boolean("Local-Player", true)
    val droppedItems by boolean("Dropped-Items", false)

    val colorMode by choices(
        "Color Mode",
        arrayOf("Custom", "Theme", "Fade", "Rainbow", "Random"),
        "Custom"
    )
    private val color by color("Color", Color.WHITE)  { colorMode == "Custom" || colorMode == "Fade" }

    val fontScale by float("Font-Scale", 0.5f, 0f..1f)
    val colorTeam by boolean("Team", false)

    private val viewport: IntBuffer = GLAllocation.createDirectIntBuffer(16)
    private val modelview: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val projection: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val vector: FloatBuffer = GLAllocation.createDirectFloatBuffer(4)

    private val background by boolean("Background", true)
    private val backgroundColor by color("BackgroundColor", Color.BLACK.withAlpha(120)) { background }

    private val black: Int = Color.BLACK.rgb

    private val dFormat = DecimalFormat("0.0")

    val collectedEntities: MutableList<Entity?> = mutableListOf()

    override fun onDisable() {
        collectedEntities.clear()
    }

    val onRender2D = handler<Render2DEvent> { event ->
        if (mc.theWorld == null) return@handler

        try {
            GL11.glPushMatrix()
            collectEntities()

            val partialTicks = event.partialTicks
            val scaledResolution = ScaledResolution(mc)
            val scaleFactor = scaledResolution.scaleFactor
            val scaling = scaleFactor.toDouble() / scaleFactor.toDouble().pow(2.0)

            GL11.glScaled(scaling, scaling, scaling)

            val fr = minecraftFont
            val renderMng = mc.renderManager
            val entityRenderer = mc.entityRenderer

            val doOutline = outline
            val doHealthBar = healthBar
            val doArmorBar = armorBar

            val entities = collectedEntities.toList()
            for (i in entities.indices) {
                val entity = entities.getOrNull(i) ?: continue

                if (entity.isDead || !entity.isEntityAlive) continue

                val colorRGB = getColor(entity).rgb

                if (isInViewFrustum(entity)) {
                    val x = interpolate(entity.posX, entity.lastTickPosX, event.partialTicks.toDouble())
                    val y = interpolate(entity.posY, entity.lastTickPosY, event.partialTicks.toDouble())
                    val z = interpolate(entity.posZ, entity.lastTickPosZ, event.partialTicks.toDouble())

                    val width = entity.width.toDouble() / 1.5
                    val height = entity.height.toDouble() + if (entity.isSneaking) -0.3 else 0.2
                    val aabb = AxisAlignedBB(
                        x - width, y, z - width,
                        x + width, y + height, z + width
                    )
                    val corners = listOf(
                        Vector3d(aabb.minX, aabb.minY, aabb.minZ),
                        Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
                        Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
                        Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
                        Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
                        Vector3d(aabb.minX, aabb.maxY, aabb.maxZ),
                        Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
                        Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
                    )

                    try {
                        entityRenderer.setupCameraTransform(partialTicks, 0)
                    } catch (e: Exception) {
                        continue
                    }

                    var bbScreen: Vector4d? = null

                    for (corner in corners) {
                        val vec = project2D(
                            scaleFactor,
                            corner.x - renderMng.viewerPosX,
                            corner.y - renderMng.viewerPosY,
                            corner.z - renderMng.viewerPosZ
                        ) ?: continue

                        if (vec.z in 0.0..1.0) {
                            if (bbScreen == null) {
                                bbScreen = Vector4d(vec.x, vec.y, vec.z, 0.0)
                            }
                            bbScreen.x = min(vec.x, bbScreen.x)
                            bbScreen.y = min(vec.y, bbScreen.y)
                            bbScreen.z = max(vec.x, bbScreen.z)
                            bbScreen.w = max(vec.y, bbScreen.w)
                        }
                    }

                    bbScreen?.let { pos ->
                        try {
                            entityRenderer.setupOverlayRendering()
                        } catch (e: Exception) {
                            return@let
                        }

                        val minX = pos.x
                        val minY = pos.y
                        val maxX = pos.z
                        val maxY = pos.w

                        if (minX.isNaN() || minY.isNaN() || maxX.isNaN() || maxY.isNaN() ||
                            minX == maxX || minY == maxY) {
                            return@let
                        }

                        if (doOutline) {
                            if (boxMode == "Box") {
                                newDrawRect(minX - 1.0, minY, minX + 0.5, maxY + 0.5, black)
                                newDrawRect(minX - 1.0, minY - 0.5, maxX + 0.5, minY + 1.0, black)
                                newDrawRect(maxX - 1.0, minY, maxX + 0.5, maxY + 0.5, black)
                                newDrawRect(minX - 1.0, maxY - 1.0, maxX + 0.5, maxY + 0.5, black)

                                newDrawRect(minX - 0.5, minY, minX, maxY, colorRGB)
                                newDrawRect(minX, maxY - 0.5, maxX, maxY, colorRGB)
                                newDrawRect(minX - 0.5, minY, maxX, minY + 0.5, colorRGB)
                                newDrawRect(maxX - 0.5, minY, maxX, maxY, colorRGB)
                            } else {
                                newDrawRect(minX - 1.0, minY, minX + (maxX - minX) / 4.0, minY + 0.5, black)
                                newDrawRect(minX - 1.0, maxY, minX + (maxX - minX) / 4.0, maxY - 0.5, black)
                                newDrawRect(maxX + 0.5 - (maxX - minX) / 4.0, minY, maxX + 0.5, minY + 0.5, black)
                                newDrawRect(maxX + 0.5 - (maxX - minX) / 4.0, maxY, maxX + 0.5, maxY - 0.5, black)

                                newDrawRect(minX, minY, minX + (maxX - minX) / 4.0, minY + 0.5, colorRGB)
                                newDrawRect(minX, maxY - 0.5, minX + (maxX - minX) / 4.0, maxY, colorRGB)
                                newDrawRect(maxX - (maxX - minX) / 4.0, minY, maxX, minY + 0.5, colorRGB)
                                newDrawRect(maxX - (maxX - minX) / 4.0, maxY - 0.5, maxX, maxY, colorRGB)
                            }
                        }

                        val isLiving = entity is EntityLivingBase
                        val isPlayer = entity is EntityPlayer

                        if (entity is EntityLivingBase && doHealthBar) {
                            val eLiving = entity
                            var hp = eLiving.health.toDouble()
                            val maxHp = eLiving.maxHealth.toDouble()
                            if (hp > maxHp) hp = maxHp

                            if (maxHp <= 0 || hp.isNaN() || maxHp.isNaN()) {
                                return@let
                            }

                            val ratio = hp / maxHp
                            val fullHeight = maxY - minY
                            val barHeight = fullHeight * ratio

                            val healthCol = ColorUtils.getHealthColor(ratio.toFloat(), ratio.toFloat()).rgb

                            if (hpBarMode.equals("Dot", ignoreCase = true) && fullHeight >= 60) {
                                val segment = (fullHeight + 0.5) / 10.0
                                val unit = maxHp / 10.0
                                for (k in 0 until 10) {
                                    val segmentHP = ((hp - k * unit).coerceIn(0.0, unit)) / unit
                                    val segHei = (fullHeight / 10.0 - 0.5) * segmentHP
                                    newDrawRect(
                                        minX - 3.0,
                                        maxY - segment * k,
                                        minX - 2.0,
                                        maxY - segment * k - segHei,
                                        healthCol
                                    )
                                }
                            } else {
                                newDrawRect(minX - 3.0, maxY, minX - 2.0, maxY - barHeight, healthCol)
                                val ab = eLiving.absorptionAmount
                                if (absorption && ab > 0f) {
                                    val abCol = Color(Potion.absorption.liquidColor).rgb
                                    val abHei = fullHeight / 6.0 * ab.toDouble() / 2.0
                                    newDrawRect(
                                        minX - 3.0,
                                        maxY,
                                        minX - 2.0,
                                        maxY - abHei,
                                        abCol
                                    )
                                }
                            }

                            if (healthNumber && (!hover || entity === mc.thePlayer || isHovering(minX, maxX, minY, maxY, scaledResolution))) {
                                val disp = if (hpMode.equals("Health", true))
                                    "${dFormat.format(eLiving.health.toDouble())} ❤"
                                else
                                    "${(ratio * 100).toInt()}%"
                                drawScaledString(
                                    disp,
                                    minX - 4.0 - minecraftFont.getStringWidth(disp) * fontScale,
                                    (maxY - barHeight) - minecraftFont.FONT_HEIGHT / 2f * fontScale,
                                    fontScale.toDouble(),
                                    -1
                                )
                            }
                        }

                        if (isLiving && doArmorBar) {
                            val eLiving = entity
                            if (armorBarMode.equals("Items", ignoreCase = true)) {
                                val slotHeight = (maxY - minY) / 4.0 + 0.25
                                for (slot in 4 downTo 1) {
                                    val stack = eLiving.getEquipmentInSlot(slot)
                                    if (stack != null) {
                                        newDrawRect(maxX + 1.5, maxY + 0.5 - slotHeight * slot,
                                            maxX + 3.5, maxY + 0.5 - slotHeight * (slot - 1),
                                            backgroundColor.rgb)
                                        val durRatio = ItemUtils.getItemDurability(stack).toDouble() / stack.maxDamage
                                        newDrawRect(
                                            maxX + 2.0,
                                            maxY + 0.5 - slotHeight * (slot - 1) - 0.25,
                                            maxX + 3.0,
                                            maxY + 0.5 - slotHeight * (slot - 1) - 0.25 - (slotHeight - 0.25) * durRatio,
                                            Color(0, 255, 255).rgb
                                        )
                                    }
                                }
                            } else {
                                val armorVal = eLiving.totalArmorValue.toFloat()
                                val armorHeight = (maxY - minY) * armorVal / 20.0
                                newDrawRect(maxX + 1.5, minY - 0.5, maxX + 3.5, maxY + 0.5, backgroundColor.rgb)
                                if (armorVal > 0f) {
                                    newDrawRect(maxX + 2.0, maxY, maxX + 3.0, maxY - armorHeight, Color(0, 255, 255).rgb)
                                }
                            }
                        }

                        if (entity is EntityItem && armorNumber) {
                            val stack = entity.entityItem
                            if (stack.isItemStackDamageable) {
                                val maxD = stack.maxDamage
                                val curD = (maxD - stack.itemDamage).toFloat()
                                val height = (maxY - minY) * (curD / maxD.toDouble())
                                newDrawRect(maxX + 1.5, minY - 0.5, maxX + 3.5, maxY + 0.5, backgroundColor.rgb)
                                newDrawRect(maxX + 2.0, maxY, maxX + 3.0, maxY - height, Color(0, 255, 255).rgb)
                                if (armorNumber && (!hover || entity.entityItem == mc.thePlayer!!.heldItem || isHovering(minX, maxX, minY, maxY, scaledResolution))) {
                                    drawScaledString(
                                        curD.toInt().toString(),
                                        maxX + 4.0,
                                        (maxY - height) - minecraftFont.FONT_HEIGHT / 2f * fontScale,
                                        fontScale.toDouble(),
                                        -1
                                    )
                                }
                            }
                        }

                        if (isLiving && armorItems && (!hover || entity === mc.thePlayer || isHovering(minX, maxX, minY, maxY, scaledResolution))) {
                            val eLiving = entity
                            val yDist = (maxY - minY) / 4.0
                            for (slot in 4 downTo 1) {
                                val stack = eLiving.getEquipmentInSlot(slot)
                                stack?.let {
                                    renderItemStack(it, maxX + 4.0, minY + yDist * (4 - slot) + yDist / 2.0 - 5.0)
                                    if (armorDur) {
                                        drawScaledCenteredString(
                                            ItemUtils.getItemDurability(it).toString(),
                                            maxX + 4.0 + 4.5,
                                            minY + yDist * (4 - slot) + yDist / 2.0 + 4.0,
                                            fontScale.toDouble(),
                                            -1
                                        )
                                    }
                                }
                            }
                        }

                        if (isLiving && tags) {
                            val eLiving = entity
                            val name = if (clearName) eLiving.name else eLiving.displayName.formattedText
                            if (tagsBG) {
                                newDrawRect(
                                    minX + (maxX - minX) / 2.0 - (minecraftFont.getStringWidth(name) / 2f + 2f) * fontScale,
                                    minY - 1.0 - (minecraftFont.FONT_HEIGHT + 2f) * fontScale,
                                    minX + (maxX - minX) / 2.0 + (minecraftFont.getStringWidth(name) / 2f + 2f) * fontScale,
                                    minY - 1.0 + 2f * fontScale,
                                    -0x60000000
                                )
                            }
                            drawScaledCenteredString(
                                name,
                                minX + (maxX - minX) / 2.0,
                                minY - 1.0 - minecraftFont.FONT_HEIGHT * fontScale,
                                fontScale.toDouble(),
                                -1
                            )
                        }

                        if (entity is EntityLivingBase && itemTags) {
                            val eLiving = entity
                            val stack = eLiving.heldItem
                            stack?.let {
                                val itemName = it.displayName
                                if (tagsBG) {
                                    newDrawRect(
                                        minX + (maxX - minX) / 2.0 - (minecraftFont.getStringWidth(itemName) / 2f + 2f) * fontScale,
                                        maxY + 1.0 - 2f * fontScale,
                                        minX + (maxX - minX) / 2.0 + (minecraftFont.getStringWidth(itemName) / 2f + 2f) * fontScale,
                                        maxY + 1.0 + (minecraftFont.FONT_HEIGHT + 2f) * fontScale,
                                        -0x60000000
                                    )
                                }
                                drawScaledCenteredString(
                                    itemName,
                                    minX + (maxX - minX) / 2.0,
                                    maxY + 1.0,
                                    fontScale.toDouble(),
                                    -1
                                )
                            }
                        } else if (entity is EntityItem && itemTags) {
                            val itemName = entity.entityItem.displayName
                            if (tagsBG) {
                                newDrawRect(
                                    minX + (maxX - minX) / 2.0 - (minecraftFont.getStringWidth(itemName) / 2f + 2f) * fontScale,
                                    maxY + 1.0 - 2f * fontScale,
                                    minX + (maxX - minX) / 2.0 + (minecraftFont.getStringWidth(itemName) / 2f + 2f) * fontScale,
                                    maxY + 1.0 + (minecraftFont.FONT_HEIGHT + 2f) * fontScale,
                                    -0x60000000
                                )
                            }
                            drawScaledCenteredString(
                                itemName,
                                minX + (maxX - minX) / 2.0,
                                maxY + 1.0,
                                fontScale.toDouble(),
                                -1
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                GL11.glPopMatrix()
                GlStateManager.enableBlend()
                GlStateManager.resetColor()
                mc.entityRenderer.setupOverlayRendering()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun isHovering(minX: Double, maxX: Double, minY: Double, maxY: Double, sc: ScaledResolution): Boolean {
        return sc.scaledWidth / 2.0 in minX..maxX && sc.scaledHeight / 2.0 in minY..maxY
    }

    private fun drawOutlineStringWithoutGL(s: String, x: Float, y: Float, color: Int, fontRenderer: FontRenderer) {
        fontRenderer.drawString(stripColor(s), (x * 2 - 1).toInt(), (y * 2).toInt(), Color.BLACK.rgb)
        fontRenderer.drawString(stripColor(s), (x * 2 + 1).toInt(), (y * 2).toInt(), Color.BLACK.rgb)
        fontRenderer.drawString(stripColor(s), (x * 2).toInt(), (y * 2 - 1).toInt(), Color.BLACK.rgb)
        fontRenderer.drawString(stripColor(s), (x * 2).toInt(), (y * 2 + 1).toInt(), Color.BLACK.rgb)
        fontRenderer.drawString(s, (x * 2).toInt(), (y * 2).toInt(), color)
    }

    private fun drawScaledString(text: String, x: Double, y: Double, scale: Double, color: Int) {
        pushMatrix()
        translate(x, y, 0.0)
        scale(scale.toFloat(), scale.toFloat(), scale.toFloat())
        if (outlineFont) {
            drawOutlineStringWithoutGL(text, 0f, 0f, color, mc.fontRendererObj)
        } else {
            minecraftFont.drawStringWithShadow(text, 0F, 0F, color)
        }
        popMatrix()
    }

    private fun drawScaledCenteredString(text: String, x: Double, y: Double, scale: Double, color: Int) {
        drawScaledString(text, x - minecraftFont.getStringWidth(text) / 2f * scale.toFloat(), y, scale, color)
    }

    private fun renderItemStack(stack: ItemStack, x: Double, y: Double) {
        pushMatrix()
        translate(x, y, 0.0)
        scale(0.5, 0.5, 0.5)
        enableRescaleNormal()
        enableBlend()
        tryBlendFuncSeparate(770, 771, 1, 0)
        enableStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(stack, 0, 0)
        mc.renderItem.renderItemOverlays(minecraftFont, stack, 0, 0)
        disableStandardItemLighting()
        disableRescaleNormal()
        disableBlend()
        popMatrix()
    }

    private fun collectEntities() {
        try {
            collectedEntities.clear()
            val worldEntities = mc.theWorld?.loadedEntityList?.toList() ?: return

            worldEntities.forEach { e ->
                if (e != null && !e.isDead && (EntityUtils.isSelected(e, false)
                            || (localPlayer && e is EntityPlayerSP && mc.gameSettings.thirdPersonView != 0)
                            || (droppedItems && e is EntityItem))
                ) {
                    collectedEntities.add(e)
                }
            }
        } catch (e: Exception) {
            collectedEntities.clear()
        }
    }

    private fun project2D(scaleFactor: Int, x: Double, y: Double, z: Double): Vector3d? {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview)
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection)
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport)
        return if (GLU.gluProject(
                x.toFloat(), y.toFloat(), z.toFloat(),
                modelview, projection, viewport, vector
            )
        ) Vector3d(
            (vector.get(0) / scaleFactor).toDouble(),
            ((Display.getHeight().toFloat() - vector.get(1)) / scaleFactor).toDouble(),
            vector.get(2).toDouble()
        ) else null
    }

    private fun getColor(entity: Entity?): Color {
        if (entity !is EntityLivingBase) return Color(color.rgb)

        if (entity is EntityPlayer && entity.isClientFriend())
            return Color.BLUE
        if (colorTeam) {
            entity.colorFromDisplayName()?.let {
                return it
            }
        }

        return when (colorMode) {
            "Custom" -> Color(color.rgb)
            "Theme"  -> getColor(1)
            "Fade"   -> {
                val idx   = collectedEntities.indexOf(entity).coerceAtLeast(0)
                val total = collectedEntities.size.coerceAtLeast(1)
                fade(Color(color.rgb), idx, total)
            }
            "Rainbow" -> {
                val hue = ((System.currentTimeMillis() % 3600L) / 3600f)
                getHSBColor(hue, 1f, 1f)
            }
            "Random"  -> {
                val rnd = Random(entity.hashCode().toLong())
                Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            }
            else      -> Color(color.rgb)
        }
    }

    fun shouldCancelNameTag(entity: EntityLivingBase?): Boolean {
        if (entity == null) return false
        return state && tags && collectedEntities.contains(entity)
    }
}