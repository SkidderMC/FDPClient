/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.visual.ESP2D
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.resetColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.scissor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.newDrawRect
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.abs
import net.ccbluex.liquidbounce.config.BoolValue

class EspPreviewComponent(private val gui: NeverloseGui) : MinecraftInstance {

    private var posX = gui.x
    private var posY = gui.y
    private var dragX = 0
    private var dragY = 0
    private var dragging = false
    private var adsorb = true
    private var managingElements = true
    private var selectedModule: Module? = null

    private var customYaw = 0f
    private var customPitch = 0f
    private var customScale = 70f

    private var boxScale = 1.0f
    private var tagsScale = 1.9f

    private var boxOffX = 0f; private var boxOffY = 0f
    private var hpOffX = -22f; private var hpOffY = -2f
    private var armorOffX = 20f; private var armorOffY = 0f
    private var tagsOffX = -3f; private var tagsOffY = -3f

    private var controlMode = 0
    private val modeNames = listOf("Rotation", "Zoom", "Box Pos", "Box Scale", "Health", "Armor", "Tags Pos", "Tags Scale")

    private val openAnimation: Animation = EaseInOutQuad(250, 1.0, Direction.BACKWARDS)
    private val dFormat = DecimalFormat("0.0")

    fun draw(mouseX: Int, mouseY: Int) {
        if (dragging) {
            posX = mouseX + dragX
            posY = mouseY + dragY
        }

        if (adsorb && !dragging) {
            posX = gui.x
            posY = gui.y
        }

        adsorb = abs(posX - gui.x) <= 30 && abs(posY - gui.y) <= gui.h
        openAnimation.direction = if (managingElements) Direction.FORWARDS else Direction.BACKWARDS

        val previewX = posX + gui.w + 12
        val previewY = posY + 12
        val previewWidth = 230f
        val previewHeight = gui.h - 24f
        val playerAreaHeight = 205f

        val backgroundColor = if (gui.light) Color(243, 246, 249, 230) else Color(9, 13, 19, 210)
        val outlineColor = if (gui.light) Color(200, 208, 216, 180) else Color(40, 50, 64, 180)
        val iconColor = NeverloseGui.neverlosecolor
        val textColor = if (gui.light) Color(34, 34, 34) else Color(230, 230, 230)

        RoundedUtil.drawRoundOutline(
            previewX.toFloat(),
            previewY.toFloat(),
            previewWidth,
            previewHeight,
            2f,
            0.1f,
            backgroundColor,
            outlineColor
        )

        Fonts.NlIcon.nlfont_20.nlfont_20.drawString("b", (previewX + 6).toFloat(), (previewY + 6).toFloat(), iconColor.rgb)
        val title = "Interactive ESP Preview"
        Fonts.Nl_18.drawString(title, previewX + previewWidth - Fonts.Nl_18.stringWidth(title) - 6, (previewY + 7).toFloat(), textColor.rgb)
        resetColor()

        val currentModeName = modeNames[controlMode]
        val debugInfo = when (controlMode) {
            0 -> "Yaw: ${customYaw.toInt()} | Pitch: ${customPitch.toInt()}"
            1 -> "View Scale: ${customScale.toInt()}%"
            2 -> "Box X: ${boxOffX.toInt()} | Y: ${boxOffY.toInt()}"
            3 -> "Box Scale: ${(boxScale * 100).toInt()}%"
            4 -> "HP X: ${hpOffX.toInt()} | Y: ${hpOffY.toInt()}"
            5 -> "Armor X: ${armorOffX.toInt()} | Y: ${armorOffY.toInt()}"
            6 -> "Tags X: ${tagsOffX.toInt()} | Y: ${tagsOffY.toInt()}"
            7 -> "Tags Scale: ${(tagsScale * 100).toInt()}%"
            else -> ""
        }
        Fonts.Nl_16.drawCenteredString("$currentModeName [$debugInfo]", previewX + previewWidth / 2f, previewY + 22f, Color(150, 150, 150).rgb)

        drawPreviewPlayer(mouseX, mouseY, previewX, previewY, previewWidth, playerAreaHeight, backgroundColor)

        drawControls(mouseX, mouseY, previewX, previewY, previewWidth, playerAreaHeight, outlineColor, textColor)

        drawElementsManager(mouseX, mouseY, previewX, previewY, previewWidth, previewHeight, playerAreaHeight, backgroundColor, outlineColor, textColor)
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val previewX = posX + gui.w + 12
        val previewY = posY + 12
        val previewWidth = 230f
        val previewHeight = gui.h - 24f
        val playerAreaHeight = 205f

        if (isHovering(previewX.toFloat(), previewY.toFloat(), previewWidth, previewHeight, mouseX, mouseY) && mouseButton == 0 && !managingElements) {
            dragging = true
            dragX = posX - mouseX
            dragY = posY - mouseY
        }

        val controlsY = previewY + playerAreaHeight - 15f
        val centerX = previewX + previewWidth / 2f
        val btnSize = 14f
        val modeBtnWidth = 60f
        val spacing = 4f
        val startX = centerX - ((btnSize * 2) + modeBtnWidth + btnSize + (spacing * 3)) / 2f

        val leftBtnX = startX
        val modeBtnX = leftBtnX + btnSize + spacing
        val rightBtnX = modeBtnX + modeBtnWidth + spacing
        val resetBtnX = rightBtnX + btnSize + spacing

        if (mouseButton == 0 && isHovering(previewX.toFloat(), controlsY - 5, previewWidth, 20f, mouseX, mouseY)) {
            if (isHovering(leftBtnX, controlsY, btnSize, btnSize, mouseX, mouseY)) adjustCurrentValue(-1)

            if (isHovering(modeBtnX, controlsY, modeBtnWidth, btnSize, mouseX, mouseY)) {
                controlMode++
                if (controlMode >= modeNames.size) controlMode = 0
            }

            if (isHovering(rightBtnX, controlsY, btnSize, btnSize, mouseX, mouseY)) adjustCurrentValue(1)

            if (isHovering(resetBtnX, controlsY, btnSize, btnSize, mouseX, mouseY)) resetAllValues()
        }

        val manageButtonX = previewX + 20f
        val manageButtonY = previewY + previewHeight - 26f

        if (isHovering(manageButtonX, manageButtonY, 190f, 16f, mouseX, mouseY) && mouseButton == 0 && activeVisualModules().isNotEmpty()) {
            managingElements = !managingElements
        }

        if (managingElements) {
            handleElementClick(mouseX, mouseY, previewX, previewY, previewWidth, previewHeight)
        }
    }

    private fun adjustCurrentValue(direction: Int) {
        val multiplier = if (direction > 0) 1 else -1
        val moveSpeed = 1f

        when (controlMode) {
            0 -> customYaw += 45f * multiplier
            1 -> customScale = (customScale + (5f * multiplier)).coerceIn(30f, 180f)
            2 -> boxOffX += moveSpeed * multiplier
            3 -> boxScale = (boxScale + (0.05f * multiplier)).coerceAtLeast(0.1f)
            4 -> hpOffX += moveSpeed * multiplier
            5 -> armorOffX += moveSpeed * multiplier
            6 -> tagsOffX += moveSpeed * multiplier
            7 -> tagsScale = (tagsScale + (0.05f * multiplier)).coerceAtLeast(0.1f)
        }
    }

    private fun resetAllValues() {
        customYaw = 0f; customPitch = 0f
        customScale = 70f
        boxScale = 1.0f
        tagsScale = 1.9f
        boxOffX = 0f; boxOffY = 0f
        hpOffX = -22f; hpOffY = -2f
        armorOffX = 20f; armorOffY = 0f
        tagsOffX = -3f; tagsOffY = -3f
        controlMode = 0
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) {
            dragging = false
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {}

    private fun drawControls(mouseX: Int, mouseY: Int, previewX: Int, previewY: Int, previewWidth: Float, playerAreaHeight: Float, outlineColor: Color, textColor: Color) {
        val controlsY = previewY + playerAreaHeight - 15f
        val centerX = previewX + previewWidth / 2f
        val btnSize = 14f
        val modeBtnWidth = 60f
        val spacing = 4f
        val startX = centerX - ((btnSize * 2) + modeBtnWidth + btnSize + (spacing * 3)) / 2f

        var currentX = startX
        val labels = listOf("<", modeNames[controlMode], ">", "R")
        val widths = listOf(btnSize, modeBtnWidth, btnSize, btnSize)

        for (i in labels.indices) {
            val w = widths[i]
            val isHover = isHovering(currentX, controlsY, w, btnSize, mouseX, mouseY)
            val col = if (labels[i] == "R") Color(255, 50, 50, 100) else NeverloseGui.neverlosecolor

            RoundedUtil.drawRound(currentX, controlsY, w, btnSize, 3f, if (isHover) col else Color(0, 0, 0, 100))
            Fonts.Nl_16.drawCenteredString(labels[i], currentX + w / 2f, controlsY + 3f, Color.WHITE.rgb)
            currentX += w + spacing
        }
    }

    private fun drawElementsManager(mouseX: Int, mouseY: Int, previewX: Int, previewY: Int, previewWidth: Float, previewHeight: Float, playerAreaHeight: Float, backgroundColor: Color, outlineColor: Color, textColor: Color) {
        val visuals = activeVisualModules()
        if (selectedModule !in visuals) selectedModule = visuals.firstOrNull()

        val manageButtonX = previewX + 20f
        val manageButtonY = previewY + previewHeight - 26f
        val manageButtonWidth = 190f
        val manageButtonHeight = 16f
        val hoveringManage = isHovering(manageButtonX, manageButtonY, manageButtonWidth, manageButtonHeight, mouseX, mouseY)

        val manageBg = if (visuals.isEmpty()) Color(backgroundColor.red, backgroundColor.green, backgroundColor.blue, 90) else backgroundColor
        val manageOutline = when {
            visuals.isEmpty() -> Color(outlineColor.red, outlineColor.green, outlineColor.blue, 120)
            hoveringManage || managingElements -> NeverloseGui.neverlosecolor
            else -> outlineColor
        }

        RoundedUtil.drawRoundOutline(manageButtonX, manageButtonY, manageButtonWidth, manageButtonHeight, 3f, 0.1f, manageBg, manageOutline)
        val manageLabel = if (visuals.isEmpty()) "No visual modules active" else if (managingElements) "Close visual manager" else "Manage active visuals"
        val manageLabelY = manageButtonY + (manageButtonHeight - Fonts.Nl_16.height) / 2f
        Fonts.Nl_16.drawCenteredString(manageLabel, manageButtonX + manageButtonWidth / 2f, manageLabelY, textColor.rgb)

        val progress = openAnimation.getOutput().toFloat()
        if (progress <= 0.05f || visuals.isEmpty()) return

        val panelMaxHeight = (previewHeight - playerAreaHeight - manageButtonHeight - 30f).coerceAtLeast(80f)
        val panelHeight = panelMaxHeight * progress
        val panelY = previewY + playerAreaHeight + 14f

        GL11.glPushMatrix()
        scissor(previewX.toDouble(), panelY.toDouble(), previewWidth.toDouble(), panelHeight.toDouble())
        GL11.glEnable(GL11.GL_SCISSOR_TEST)

        RoundedUtil.drawRoundOutline(previewX.toFloat(), panelY, previewWidth, panelHeight, 4f, 0.1f, backgroundColor, outlineColor)
        drawManagerHeader(mouseX, mouseY, previewX, previewWidth, panelY, textColor)

        val moduleButtons = moduleButtons(previewX, panelY, previewWidth)
        drawModuleSelector(moduleButtons, mouseX, mouseY, textColor, outlineColor, backgroundColor)

        val valueButtons = valueButtons(previewX, panelY, previewWidth, moduleButtons, panelHeight)
        drawValueButtons(valueButtons, textColor, outlineColor, backgroundColor)

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GL11.glPopMatrix()
    }

    private fun drawPreviewPlayer(mouseX: Int, mouseY: Int, previewX: Int, previewY: Int, previewWidth: Float, playerAreaHeight: Float, backgroundColor: Color) {
        val playerAreaY = previewY + 10
        val playerAreaX = previewX + 8
        val playerAreaWidth = previewWidth - 16f

        RoundedUtil.drawRound(playerAreaX.toFloat(), playerAreaY.toFloat(), playerAreaWidth, playerAreaHeight - 8f, 3f, Color(0, 0, 0, 35))

        val hoveringPlayer = isHovering(playerAreaX.toFloat(), playerAreaY.toFloat(), playerAreaWidth, playerAreaHeight - 25f, mouseX, mouseY)

        if (hoveringPlayer && (Mouse.isButtonDown(0) || Mouse.isButtonDown(1))) {
            val dx = Mouse.getDX() * 0.5f
            val dy = Mouse.getDY() * 0.5f

            when (controlMode) {
                0 -> { customYaw += dx; customPitch = (customPitch - dy).coerceIn(-180f, 180f) }
                2 -> { boxOffX += dx; boxOffY -= dy }
                4 -> { hpOffX += dx; hpOffY -= dy }
                5 -> { armorOffX += dx; armorOffY -= dy }
                6 -> { tagsOffX += dx; tagsOffY -= dy }
            }
        }

        val entityX = (previewX + previewWidth / 2).toInt()
        val entityY = (playerAreaY + playerAreaHeight - 28f).toInt()
        GlStateManager.pushMatrix()

        drawEntityOnScreen(entityX, entityY, customScale.toInt(), customYaw, customPitch, mc.thePlayer)
        GlStateManager.popMatrix()

        drawEspPreview(entityX, entityY, backgroundColor)
    }

    private fun drawEspPreview(x: Int, y: Int, backgroundColor: Color) {
        if (!ESP2D.state) return

        val scaleFactor = customScale.toDouble() / 90.0
        val halfWidth = 30.0 * scaleFactor * boxScale.toDouble()
        val height = 170.0 * scaleFactor * boxScale.toDouble()

        val baseX = x.toDouble()
        val baseBottomY = y.toDouble()

        val baseMinX = baseX - halfWidth
        val baseMaxX = baseX + halfWidth
        val baseMaxY = baseBottomY + (2.0 * scaleFactor)
        val baseMinY = baseBottomY - height - (5.0 * scaleFactor)

        val black = Color.BLACK.rgb
        val color = ESP2D.getColor(mc.thePlayer)
        val colorRGB = color.rgb

        if (ESP2D.outline) {
            val minX = baseMinX + boxOffX
            val maxX = baseMaxX + boxOffX
            val minY = baseMinY + boxOffY
            val maxY = baseMaxY + boxOffY

            val boxMode = ESP2D.boxMode
            if (boxMode.equals("Box", ignoreCase = true)) {
                newDrawRect(minX - 1.0, minY, minX + 0.5, maxY + 0.5, black)
                newDrawRect(minX - 1.0, minY - 0.5, maxX + 0.5, minY + 1.0, black)
                newDrawRect(maxX - 1.0, minY, maxX + 0.5, maxY + 0.5, black)
                newDrawRect(minX - 1.0, maxY - 1.0, maxX + 0.5, maxY + 0.5, black)

                newDrawRect(minX - 0.5, minY, minX, maxY, colorRGB)
                newDrawRect(minX, maxY - 0.5, maxX, maxY, colorRGB)
                newDrawRect(minX - 0.5, minY, maxX, minY + 0.5, colorRGB)
                newDrawRect(maxX - 0.5, minY, maxX, maxY, colorRGB)
            } else if (boxMode.equals("Corners", ignoreCase = true)) {
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

        if (ESP2D.healthBar) {
            val minX = baseMinX + hpOffX
            val maxY = baseMaxY + hpOffY
            val minY = baseMinY + hpOffY

            val fullHeight = maxY - minY
            val barHeight = fullHeight
            val healthCol = ColorUtils.getHealthColor(1f, 1f).rgb

            val offset = 8.0 * scaleFactor
            val barWidth = 2.0

            if (ESP2D.hpBarMode.equals("Dot", ignoreCase = true) && fullHeight >= 10) {
                val segment = (fullHeight + 0.5) / 10.0
                val unit = 20.0 / 10.0
                for (k in 0 until 10) {
                    val segmentHP = ((20.0 - k * unit).coerceIn(0.0, unit)) / unit
                    val segHei = (fullHeight / 10.0 - 0.5) * segmentHP
                    newDrawRect(minX - offset, maxY - segment * k, minX - (offset - barWidth), maxY - segment * k - segHei, healthCol)
                }
            } else {
                newDrawRect(minX - offset, maxY, minX - (offset - barWidth), maxY - barHeight, healthCol)
                if (ESP2D.absorption) {
                    val abHei = fullHeight / 6.0 * 4.0 / 2.0
                    newDrawRect(minX - offset, maxY, minX - (offset - barWidth), maxY - abHei, Color(Potion.absorption.liquidColor).rgb)
                }
            }

            if (ESP2D.healthNumber) {
                val hpDisp = if (ESP2D.hpMode.equals("Health", true)) "20.0 ❤" else "100%"
                val scale = ESP2D.fontScale
                val fontRenderer = mc.fontRendererObj
                drawScaledString(hpDisp, minX - (offset + 2.0) - fontRenderer.getStringWidth(hpDisp) * scale, (maxY - barHeight) - fontRenderer.FONT_HEIGHT / 2f * scale, scale.toDouble(), -1)
            }
        }

        if (ESP2D.armorBar || (ESP2D.armorItems && mc.thePlayer.inventory.armorInventory.isNotEmpty())) {
            val maxX = baseMaxX + armorOffX
            val minY = baseMinY + armorOffY
            val maxY = baseMaxY + armorOffY

            if (ESP2D.armorBar) {
                if (ESP2D.armorBarMode.equals("Items", ignoreCase = true)) {
                    val slotHeight = (maxY - minY) / 4.0
                    for (slot in 0..3) {
                        newDrawRect(maxX + 1.5, maxY - slotHeight * (slot + 1), maxX + 3.5, maxY - slotHeight * slot, backgroundColor.rgb)
                        newDrawRect(maxX + 2.0, maxY - slotHeight * (slot + 1) + 0.5, maxX + 3.0, maxY - slotHeight * slot - 0.5, Color(0, 255, 255).rgb)
                    }
                } else {
                    val armorHeight = (maxY - minY)
                    newDrawRect(maxX + 1.5, minY - 0.5, maxX + 3.5, maxY + 0.5, backgroundColor.rgb)
                    newDrawRect(maxX + 2.0, maxY, maxX + 3.0, maxY - armorHeight, Color(0, 255, 255).rgb)
                }
            }

            if (ESP2D.armorItems) {
                val yDist = (maxY - minY) / 4.0
                for (slot in 3 downTo 0) {
                    val stack = mc.thePlayer.inventory.armorInventory[slot]
                    if (stack != null) {
                        val renderY = minY + yDist * (3 - slot) + yDist / 2.0 - 8.0
                        renderItemStack(stack, maxX + 4.0, renderY)
                        if (ESP2D.armorDur) {
                            val dur = ItemUtils.getItemDurability(stack).toString()
                            val scale = ESP2D.fontScale
                            val fontRenderer = mc.fontRendererObj
                            drawScaledCenteredString(dur, maxX + 4.0 + 8.0, renderY + 12.0, scale.toDouble(), -1)
                        }
                    }
                }
            }
        }

        if (ESP2D.tags) {
            val textXCenter = baseX + tagsOffX
            val textYBase = baseMinY + tagsOffY

            val name = if (ESP2D.clearName) stripColor(mc.thePlayer.name) else mc.thePlayer.displayName.formattedText
            val scale = ESP2D.fontScale * tagsScale
            val fontRenderer = mc.fontRendererObj
            val textWidth = fontRenderer.getStringWidth(name).toDouble() * scale.toDouble()

            val textY = textYBase - (10.0 * scaleFactor) - fontRenderer.FONT_HEIGHT * scale

            if (ESP2D.tagsBG) {
                newDrawRect(textXCenter - textWidth / 2.0 - 2.0, textY - 2.0, textXCenter + textWidth / 2.0 + 2.0, textY + fontRenderer.FONT_HEIGHT * scale, -0x60000000)
            }
            drawScaledCenteredString(name, textXCenter, textY, scale.toDouble(), -1)
        }

        if (ESP2D.itemTags) {
            val stack = mc.thePlayer.heldItem
            if (stack != null) {
                val textXCenter = baseX + tagsOffX
                val textYBase = baseMaxY + (boxOffY * 0.1)

                val itemName = stack.displayName
                val scale = ESP2D.fontScale * tagsScale
                val fontRenderer = mc.fontRendererObj
                val textWidth = fontRenderer.getStringWidth(itemName).toDouble() * scale.toDouble()
                val textY = textYBase + (4.0 * scaleFactor)

                if (ESP2D.tagsBG) {
                    newDrawRect(textXCenter - textWidth / 2.0 - 2.0, textY - 2.0, textXCenter + textWidth / 2.0 + 2.0, textY + fontRenderer.FONT_HEIGHT * scale, -0x60000000)
                }
                drawScaledCenteredString(itemName, textXCenter, textY, scale.toDouble(), -1)
            }
        }
    }

    private fun drawOutlineStringWithoutGL(s: String, x: Float, y: Float, color: Int, fontRenderer: FontRenderer) {
        fontRenderer.drawString(stripColor(s), (x * 2 - 1).toInt(), (y * 2).toInt(), Color.BLACK.rgb)
        fontRenderer.drawString(stripColor(s), (x * 2 + 1).toInt(), (y * 2).toInt(), Color.BLACK.rgb)
        fontRenderer.drawString(stripColor(s), (x * 2).toInt(), (y * 2 - 1).toInt(), Color.BLACK.rgb)
        fontRenderer.drawString(stripColor(s), (x * 2).toInt(), (y * 2 + 1).toInt(), Color.BLACK.rgb)
        fontRenderer.drawString(s, (x * 2).toInt(), (y * 2).toInt(), color)
    }

    private fun drawScaledString(text: String, x: Double, y: Double, scale: Double, color: Int) {
        GL11.glPushMatrix()
        GL11.glTranslated(x, y, 0.0)
        GL11.glScaled(scale, scale, scale)
        if (ESP2D.outlineFont) {
            drawOutlineStringWithoutGL(text, 0f, 0f, color, mc.fontRendererObj)
        } else {
            mc.fontRendererObj.drawStringWithShadow(text, 0f, 0f, color)
        }
        GL11.glPopMatrix()
    }

    private fun drawScaledCenteredString(text: String, x: Double, y: Double, scale: Double, color: Int) {
        val width = mc.fontRendererObj.getStringWidth(text) * scale
        drawScaledString(text, x - width / 2.0, y, scale, color)
    }

    private fun renderItemStack(stack: ItemStack, x: Double, y: Double) {
        GL11.glPushMatrix()
        GL11.glTranslated(x, y, 0.0)
        GL11.glScalef(0.5f, 0.5f, 0.5f)
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(stack, 0, 0)
        mc.renderItem.renderItemOverlays(mc.fontRendererObj, stack, 0, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GL11.glPopMatrix()
    }

    private fun drawManagerHeader(mouseX: Int, mouseY: Int, previewX: Int, previewWidth: Float, panelY: Float, textColor: Color) {
        Fonts.Nl_16.drawString("Visual modules", previewX + 10f, panelY + 8f, textColor.rgb)
        val closeIconX = previewX + previewWidth - 16f
        val closeIconY = panelY + 5f
        val hoveringClose = isHovering(closeIconX, closeIconY, 12f, 12f, mouseX, mouseY)
        Fonts.Nl_16_ICON.drawString("m", closeIconX, closeIconY, if (hoveringClose) NeverloseGui.neverlosecolor.rgb else textColor.rgb)
        if (hoveringClose && Mouse.isButtonDown(0)) managingElements = false
    }

    private fun drawModuleSelector(moduleButtons: List<ButtonArea<Module>>, mouseX: Int, mouseY: Int, textColor: Color, outlineColor: Color, backgroundColor: Color) {
        moduleButtons.forEach { button ->
            val selected = button.target == selectedModule
            val buttonBackground = when {
                selected -> NeverloseGui.neverlosecolor
                button.target.state -> Color(44, 120, 168, 110)
                else -> backgroundColor
            }
            val buttonOutline = if (selected || button.target.state) NeverloseGui.neverlosecolor else outlineColor

            RoundedUtil.drawRoundOutline(button.x, button.y, button.w, button.h, 2f, 0.1f, buttonBackground, buttonOutline)
            val textY = button.y + (button.h - Fonts.Nl_16.height) / 2f
            Fonts.Nl_16.drawCenteredString(button.target.name, button.x + button.w / 2f, textY, textColor.rgb)

            if (isHovering(button.x, button.y, button.w, button.h, mouseX, mouseY) && Mouse.isButtonDown(0)) selectedModule = button.target
        }
    }

    private fun drawValueButtons(valueButtons: List<ButtonArea<BoolValue>>, textColor: Color, outlineColor: Color, backgroundColor: Color) {
        valueButtons.forEach { button ->
            val enabled = button.target.get()
            val buttonBackground = if (enabled) NeverloseGui.neverlosecolor else backgroundColor
            val buttonOutline = if (enabled) NeverloseGui.neverlosecolor else outlineColor

            RoundedUtil.drawRoundOutline(button.x, button.y, button.w, button.h, 2f, 0.1f, buttonBackground, buttonOutline)
            val textY = button.y + (button.h - Fonts.Nl_16.height) / 2f
            Fonts.Nl_16.drawCenteredString(button.target.name, button.x + button.w / 2f, textY, textColor.rgb)
        }
    }

    private fun handleElementClick(mouseX: Int, mouseY: Int, previewX: Int, previewY: Int, previewWidth: Float, previewHeight: Float) {
        val progress = openAnimation.getOutput().toFloat()
        if (progress <= 0.05f) return
        val playerAreaHeight = 205f
        val manageButtonHeight = 16f
        val panelY = previewY + playerAreaHeight + 14f
        val panelHeight = (previewHeight - playerAreaHeight - manageButtonHeight - 30f).coerceAtLeast(80f) * progress
        val moduleButtons = moduleButtons(previewX, panelY, previewWidth)
        moduleButtons.firstOrNull { isHovering(it.x, it.y, it.w, it.h, mouseX, mouseY) }?.let { selectedModule = it.target; return }

        val valueButtons = valueButtons(previewX, panelY, previewWidth, moduleButtons, panelHeight)
        valueButtons.firstOrNull { isHovering(it.x, it.y, it.w, it.h, mouseX, mouseY) }?.let { it.target.set(!it.target.get()); return }
    }

    private fun activeVisualModules(): List<Module> = ModuleManager[Category.VISUAL].filter { it.state }

    private fun moduleButtons(previewX: Int, panelY: Float, previewWidth: Float): List<ButtonArea<Module>> {
        val buttons = mutableListOf<ButtonArea<Module>>()
        var xOffset = 0f
        var yOffset = 26f
        for (module in activeVisualModules()) {
            val labelWidth = Fonts.Nl_16.stringWidth(module.name) + 10f
            if (xOffset + labelWidth > previewWidth - 16f) { xOffset = 0f; yOffset += 16f }
            buttons += ButtonArea(module, previewX + 8f + xOffset, panelY + yOffset, labelWidth, 14f)
            xOffset += labelWidth + 4f
        }
        return buttons
    }

    private fun valueButtons(previewX: Int, panelY: Float, previewWidth: Float, moduleButtons: List<ButtonArea<Module>>, panelHeight: Float): List<ButtonArea<BoolValue>> {
        val buttons = mutableListOf<ButtonArea<BoolValue>>()
        val values = selectedModule?.values.orEmpty().filterIsInstance<BoolValue>()
        var xOffset = 0f
        val startY = (moduleButtons.maxOfOrNull { it.y + it.h } ?: (panelY + 26f)) + 10f
        var yOffset = startY - panelY
        for (value in values) {
            val labelWidth = Fonts.Nl_16.stringWidth(value.name) + 6f
            if (xOffset + labelWidth > previewWidth - 16f) { xOffset = 0f; yOffset += 14f }
            if (panelY + yOffset + 12f <= panelY + panelHeight - 8f) buttons += ButtonArea(value, previewX + 8f + xOffset, panelY + yOffset, labelWidth, 12f)
            xOffset += labelWidth + 5f
        }
        return buttons
    }

    private data class ButtonArea<T>(val target: T, val x: Float, val y: Float, val w: Float, val h: Float)
    private fun isHovering(x: Float, y: Float, w: Float, h: Float, mouseX: Int, mouseY: Int): Boolean = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
}