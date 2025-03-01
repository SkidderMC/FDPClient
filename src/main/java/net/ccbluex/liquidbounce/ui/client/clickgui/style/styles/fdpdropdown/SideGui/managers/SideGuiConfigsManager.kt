/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers

import net.ccbluex.liquidbounce.FDPClient.fileManager
import net.ccbluex.liquidbounce.config.SettingsUtils.applyScript
import net.ccbluex.liquidbounce.handler.api.ClientApi
import net.ccbluex.liquidbounce.handler.api.autoSettingsList
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.utils.client.ClientUtils.displayChatMessage
import org.lwjgl.input.Mouse
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

object SideGuiConfigsManager {
    var showLocalConfigs = false
    var wasMousePressed = false

    fun drawConfigsCategory(mouseX: Int, mouseY: Int, alpha: Int, drag: Drag, rectWidth: Float) {
        val buttonToggleWidth = 70f
        val buttonToggleHeight = 20f
        val buttonSpacing = 10f

        val xStart = drag.x + 25
        val openFolderButtonWidth = buttonToggleWidth * 2
        val openFolderButtonX = xStart
        val openFolderButtonY = drag.y + 30
        val isOpenFolderHovered = DrRenderUtils.isHovering(
            openFolderButtonX, openFolderButtonY,
            openFolderButtonWidth, buttonToggleHeight,
            mouseX, mouseY
        )
        val openFolderButtonColor = if (isOpenFolderHovered) Color(70, 70, 70, alpha).rgb else Color(50, 50, 50, alpha).rgb
        DrRenderUtils.drawRect2(
            openFolderButtonX.toDouble(),
            openFolderButtonY.toDouble(),
            openFolderButtonWidth.toDouble(),
            buttonToggleHeight.toDouble(),
            openFolderButtonColor
        )
        Fonts.InterBold_26.drawString("OPEN FOLDER", openFolderButtonX + 10, openFolderButtonY + 5, DrRenderUtils.applyOpacity(-1, alpha / 255f))

        val onlineButtonX = xStart
        val onlineButtonY = openFolderButtonY + buttonToggleHeight + buttonSpacing
        val isOnlineHovered = DrRenderUtils.isHovering(onlineButtonX, onlineButtonY, buttonToggleWidth, buttonToggleHeight, mouseX, mouseY)
        val onlineButtonColor = when {
            !showLocalConfigs -> Color(100, 150, 100, alpha).rgb
            isOnlineHovered   -> Color(70, 70, 70, alpha).rgb
            else              -> Color(50, 50, 50, alpha).rgb
        }
        DrRenderUtils.drawRect2(
            onlineButtonX.toDouble(),
            onlineButtonY.toDouble(),
            buttonToggleWidth.toDouble(),
            buttonToggleHeight.toDouble(),
            onlineButtonColor
        )
        Fonts.InterBold_26.drawString("ONLINE", onlineButtonX + 10, onlineButtonY + 5, DrRenderUtils.applyOpacity(-1, alpha / 255f))

        val localButtonX = onlineButtonX + buttonToggleWidth + buttonSpacing
        val localButtonY = onlineButtonY
        val isLocalHovered = DrRenderUtils.isHovering(localButtonX, localButtonY, buttonToggleWidth, buttonToggleHeight, mouseX, mouseY)
        val localButtonColor = when {
            showLocalConfigs -> Color(100, 150, 100, alpha).rgb
            isLocalHovered   -> Color(70, 70, 70, alpha).rgb
            else             -> Color(50, 50, 50, alpha).rgb
        }
        DrRenderUtils.drawRect2(
            localButtonX.toDouble(),
            localButtonY.toDouble(),
            buttonToggleWidth.toDouble(),
            buttonToggleHeight.toDouble(),
            localButtonColor
        )
        Fonts.InterBold_26.drawString("LOCAL", localButtonX + 10, localButtonY + 5, DrRenderUtils.applyOpacity(-1, alpha / 255f))

        if (!wasMousePressed && Mouse.isButtonDown(0)) {
            when {
                isOpenFolderHovered -> openFolder()
                isOnlineHovered     -> showLocalConfigs = false
                isLocalHovered      -> showLocalConfigs = true
            }
            wasMousePressed = true
        }
        if (!Mouse.isButtonDown(0)) wasMousePressed = false

        drawConfigList(mouseX, mouseY, alpha, localButtonY + buttonToggleHeight + buttonSpacing, drag, rectWidth)
    }

    private fun drawConfigList(mouseX: Int, mouseY: Int, alpha: Int, startY: Float, drag: Drag, rectWidth: Float) {
        var configX = drag.x + 25
        var configY = startY
        val buttonWidth = (rectWidth - 50) / 4 - 10
        val buttonHeight = 20f
        val configsPerRow = 4
        var configCount = 0

        if (showLocalConfigs) {
            val localConfigs = fileManager.settingsDir.listFiles { _, name -> name.endsWith(".txt") }
            if (!localConfigs.isNullOrEmpty()) {
                for (file in localConfigs) {
                    drawSingleConfigButton(mouseX, mouseY, alpha, configX, configY, buttonWidth, buttonHeight) {
                        val configName = file.name.removeSuffix(".txt")
                        Fonts.InterBold_26.drawString(configName, configX + 5, configY + 5, DrRenderUtils.applyOpacity(-1, alpha / 255f))
                        if (DrRenderUtils.isHovering(configX, configY, buttonWidth, buttonHeight, mouseX, mouseY) && Mouse.isButtonDown(0)) {
                            loadLocalConfig(configName, file)
                        }
                    }
                    configX += buttonWidth + 10
                    configCount++
                    if (configCount % configsPerRow == 0) {
                        configX = drag.x + 25
                        configY += buttonHeight + 5
                    }
                }
            } else {
                Fonts.InterBold_26.drawString("No local configurations available.", configX, configY, DrRenderUtils.applyOpacity(-1, alpha / 255f))
            }
        } else {
            if (!autoSettingsList.isNullOrEmpty()) {
                for (autoSetting in autoSettingsList!!) {
                    drawSingleConfigButton(mouseX, mouseY, alpha, configX, configY, buttonWidth, buttonHeight) {
                        Fonts.InterBold_26.drawString(autoSetting.name, configX + 5, configY + 5, DrRenderUtils.applyOpacity(-1, alpha / 255f))
                        if (DrRenderUtils.isHovering(configX, configY, buttonWidth, buttonHeight, mouseX, mouseY) && Mouse.isButtonDown(0)) {
                            loadOnlineConfig(autoSetting.settingId, autoSetting.name)
                        }
                    }
                    configX += buttonWidth + 10
                    configCount++
                    if (configCount % configsPerRow == 0) {
                        configX = drag.x + 25
                        configY += buttonHeight + 5
                    }
                }
            } else {
                Fonts.InterBold_26.drawString("No online configurations available.", configX, configY, DrRenderUtils.applyOpacity(-1, alpha / 255f))
            }
        }
    }

    private inline fun drawSingleConfigButton(
        mouseX: Int,
        mouseY: Int,
        alpha: Int,
        configX: Float,
        configY: Float,
        width: Float,
        height: Float,
        drawContent: () -> Unit
    ) {
        val isHovered = DrRenderUtils.isHovering(configX, configY, width, height, mouseX, mouseY)
        val buttonColor = if (isHovered) Color(70, 70, 70, alpha).rgb else Color(50, 50, 50, alpha).rgb
        DrRenderUtils.drawRect2(configX.toDouble(), configY.toDouble(), width.toDouble(), height.toDouble(), buttonColor)
        drawContent()
    }

    private fun loadLocalConfig(configName: String, file: File) {
        try {
            displayChatMessage("Loading local configuration: $configName...")
            val localConfigContent = Files.readAllBytes(file.toPath()).toString(StandardCharsets.UTF_8)
            applyScript(localConfigContent)
            displayChatMessage("Local configuration $configName loaded successfully!")
        } catch (e: IOException) {
            displayChatMessage("Error loading local configuration: ${e.message}")
        }
    }

    private fun loadOnlineConfig(settingId: String, configName: String) {
        try {
            displayChatMessage("Loading configuration: $configName...")
            val configScript = ClientApi.getSettingsScript("legacy", settingId)
            applyScript(configScript)
            displayChatMessage("Configuration $configName loaded successfully!")
        } catch (e: Exception) {
            displayChatMessage("Error loading configuration: ${e.message}")
        }
    }

    private fun openFolder() {
        try {
            Desktop.getDesktop().open(fileManager.settingsDir)
            displayChatMessage("Opening configuration folder...")
        } catch (e: IOException) {
            displayChatMessage("Error opening folder: ${e.message}")
        }
    }
}