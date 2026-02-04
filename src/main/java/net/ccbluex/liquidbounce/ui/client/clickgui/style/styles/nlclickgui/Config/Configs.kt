package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.SettingsUtils
import net.ccbluex.liquidbounce.handler.api.ClientApi
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils.isHovering
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.ArrayList
import kotlin.math.ceil

class Configs {

    private var posx: Int = 0
    private var posy: Int = 0
    private var scy: Int = 0
    private var areaWidth: Float = 0f

    private var showLocalConfigs = false
    private val interactiveAreas: MutableList<ButtonArea> = ArrayList()


    var contentHeight = 0

    private var onlineConfigsCache: List<*>? = null
    @Volatile
    private var isLoadingOnline = false

    fun setBounds(posx: Int, posy: Int, areaWidth: Float) {
        this.posx = posx
        this.posy = posy
        this.areaWidth = areaWidth
    }

    fun setScroll(scy: Int) {
        this.scy = scy
    }

    fun draw(mx: Int, my: Int) {
        interactiveAreas.clear()
        val baseX = posx + 10
        val baseY = posy + scy + 10

        val alpha = 255
        val buttonHeight = 20
        val buttonSpacing = 10
        val buttonToggleWidth = 70

        val openFolderWidth = buttonToggleWidth * 2

        drawButton(baseX, baseY, openFolderWidth, buttonHeight, mx, my, NeverloseGui.getInstance().light, false)
        Fonts.InterBold_26.drawString("OPEN FOLDER", (baseX + 10).toFloat(), (baseY + 5).toFloat(), applyTextColor(alpha, false))
        interactiveAreas.add(ButtonArea(baseX.toFloat(), baseY.toFloat(), openFolderWidth.toFloat(), buttonHeight.toFloat()) {
            openFolder()
        })

        val togglesY = baseY + buttonHeight + buttonSpacing

        val onlineActive = !showLocalConfigs
        drawToggle(baseX, togglesY, buttonToggleWidth, buttonHeight, mx, my, onlineActive)
        Fonts.InterBold_26.drawString("ONLINE", (baseX + 10).toFloat(), (togglesY + 5).toFloat(), applyTextColor(alpha, onlineActive))
        interactiveAreas.add(ButtonArea(baseX.toFloat(), togglesY.toFloat(), buttonToggleWidth.toFloat(), buttonHeight.toFloat()) {
            showLocalConfigs = false
            if (onlineConfigsCache == null) {
                loadOnlineConfigsAsync()
            }
        })

        val localX = baseX + buttonToggleWidth + buttonSpacing

        val localActive = showLocalConfigs
        drawToggle(localX, togglesY, buttonToggleWidth, buttonHeight, mx, my, localActive)
        Fonts.InterBold_26.drawString("LOCAL", (localX + 10).toFloat(), (togglesY + 5).toFloat(), applyTextColor(alpha, localActive))
        interactiveAreas.add(ButtonArea(localX.toFloat(), togglesY.toFloat(), buttonToggleWidth.toFloat(), buttonHeight.toFloat()) {
            showLocalConfigs = true
        })

        val listStartY = togglesY + buttonHeight + buttonSpacing

        if (!showLocalConfigs && onlineConfigsCache == null && !isLoadingOnline) {
            loadOnlineConfigsAsync()
        }

        drawConfigList(mx, my, listStartY, alpha)

        contentHeight = listStartY - (posy + scy) + listHeight
    }

    private fun loadOnlineConfigsAsync() {
        if (isLoadingOnline) return
        isLoadingOnline = true

        Thread {
            try {
                val configs = ClientApi.getSettingsList("legacy")
                synchronized(this) {
                    onlineConfigsCache = configs
                    isLoadingOnline = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isLoadingOnline = false
            }
        }.start()
    }

    fun click(mx: Int, my: Int, mb: Int) {
        if (mb != 0) {
            return
        }
        for (area in interactiveAreas) {
            if (isHovering(area.x, area.y, area.width, area.height, mx.toFloat().toInt(), my.toFloat().toInt())) {
                area.action.invoke()
                break
            }
        }
    }



    private fun drawToggle(x: Int, y: Int, width: Int, height: Int, mx: Int, my: Int, active: Boolean) {
        val hovered = isHovering(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), mx.toFloat().toInt(),
            my.toFloat().toInt()
        )
        val base = if (NeverloseGui.getInstance().light) Color(220, 222, 225) else Color(50, 50, 50)
        val activeColor = Color(100, 150, 100)
        val hoverColor = if (NeverloseGui.getInstance().light) Color(200, 200, 205) else Color(70, 70, 70)
        val fill = if (active) activeColor else if (hovered) hoverColor else base
        drawButton(x, y, width, height, fill)
    }

    private fun drawButton(x: Int, y: Int, width: Int, height: Int, mx: Int, my: Int, light: Boolean, active: Boolean) {
        val hovered = isHovering(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), mx.toFloat().toInt(),
            my.toFloat().toInt()
        )
        val base = if (light) Color(220, 222, 225) else Color(50, 50, 50)
        val hover = if (light) Color(200, 200, 205) else Color(70, 70, 70)
        val fill = if (active) Color(100, 150, 100) else if (hovered) hover else base
        drawButton(x, y, width, height, fill)
    }

    private fun drawButton(x: Int, y: Int, width: Int, height: Int, fill: Color) {
        RoundedUtil.drawRound(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 3f, fill)
    }

    private fun drawConfigList(mx: Int, my: Int, startY: Int, alpha: Int) {
        val buttonWidth = (areaWidth - 50) / 4f - 10f
        val buttonHeight = 20f
        val configsPerRow = 4
        var configX = (posx + 10).toFloat()
        var configY = startY.toFloat()
        var configCount = 0

        val standardTextColor = applyTextColor(alpha, false)

        if (showLocalConfigs) {
            val localConfigs = FDPClient.fileManager.settingsDir.listFiles { _, name -> name.endsWith(".txt") }
            if (localConfigs != null && localConfigs.isNotEmpty()) {
                for (file in localConfigs) {
                    drawConfigButton(mx, my, buttonWidth, buttonHeight, configX, configY) { loadLocalConfig(file) }
                    Fonts.InterBold_26.drawString(file.name.replace(".txt", ""), configX + 5, configY + 5, standardTextColor)
                    configX += buttonWidth + 10
                    configCount++
                    if (configCount % configsPerRow == 0) {
                        configX = (posx + 10).toFloat()
                        configY += buttonHeight + 5
                    }
                }
            } else {
                Fonts.InterBold_26.drawString("No local configurations available.", configX, configY, standardTextColor)
            }
        } else {
            if (isLoadingOnline) {
                Fonts.InterBold_26.drawString("Loading online configs...", configX, configY, standardTextColor)
                return
            }

            val remoteSettings = onlineConfigsCache

            if (remoteSettings != null && remoteSettings.isNotEmpty()) {
                val safeList: List<*>
                synchronized(this) { safeList = ArrayList(remoteSettings) }

                for (autoSetting in safeList) {
                    val settingName = getSettingName(autoSetting)
                    val settingId = getSettingId(autoSetting)
                    drawConfigButton(mx, my, buttonWidth, buttonHeight, configX, configY) {
                        loadOnlineConfig(settingId, settingName)
                    }
                    Fonts.InterBold_26.drawString(settingName, configX + 5, configY + 5, standardTextColor)
                    configX += buttonWidth + 10
                    configCount++
                    if (configCount % configsPerRow == 0) {
                        configX = (posx + 10).toFloat()
                        configY += buttonHeight + 5
                    }
                }
            } else {
                Fonts.InterBold_26.drawString("No online configurations or failed to load.", configX, configY, standardTextColor)
            }
        }
    }

    private fun drawConfigButton(mx: Int, my: Int, width: Float, height: Float, configX: Float, configY: Float, action: () -> Unit) {
        val hovered = isHovering(configX, configY, width, height, mx.toFloat().toInt(), my.toFloat().toInt())
        val base = if (NeverloseGui.getInstance().light) Color(220, 222, 225) else Color(50, 50, 50)
        val hover = if (NeverloseGui.getInstance().light) Color(200, 200, 205) else Color(70, 70, 70)
        val fill = if (hovered) hover else base
        RoundedUtil.drawRound(configX, configY, width, height, 3f, fill)
        interactiveAreas.add(ButtonArea(configX, configY, width, height, action))
    }

    private val listHeight: Int
        get() {
            var itemCount = 0
            val rowHeight = 25
            itemCount = if (showLocalConfigs) {
                val localConfigs = FDPClient.fileManager.settingsDir.listFiles { _, name -> name.endsWith(".txt") }
                localConfigs?.size ?: 0
            } else {
                onlineConfigsCache?.size ?: 0
            }
            if (itemCount == 0) {
                return rowHeight + 5
            }
            val rows = ceil(itemCount / 4.0).toInt()
            return rows * rowHeight
        }

    private fun loadLocalConfig(file: File) {
        val configName = file.name.replace(".txt", "")
        try {
            ClientUtils.displayChatMessage("Loading local configuration: $configName...")
            val localConfigContent = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
            SettingsUtils.applyScript(localConfigContent)
            ClientUtils.displayChatMessage("Local configuration $configName loaded successfully!")
        } catch (e: IOException) {
            ClientUtils.displayChatMessage("Error loading local configuration: ${e.message}")
        }
    }

    private fun loadOnlineConfig(settingId: String, configName: String) {
        Thread {
            try {
                ClientUtils.displayChatMessage("Downloading configuration: $configName...")
                val configScript = ClientApi.getSettingsScript("legacy", settingId)
                SettingsUtils.applyScript(configScript)
                ClientUtils.displayChatMessage("Configuration $configName loaded successfully!")
            } catch (e: Exception) {
                ClientUtils.displayChatMessage("Error loading configuration: ${e.message}")
            }
        }.start()
    }

    private fun getSettingName(autoSetting: Any?): String {
        return try {
            val method: Method = autoSetting!!.javaClass.getMethod("getName")
            val value = method.invoke(autoSetting)
            value?.toString() ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }

    private fun getSettingId(autoSetting: Any?): String {
        return try {
            val method: Method = autoSetting!!.javaClass.getMethod("getSettingId")
            val value = method.invoke(autoSetting)
            value?.toString() ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }

    private fun openFolder() {
        try {
            Desktop.getDesktop().open(FDPClient.fileManager.settingsDir)
            ClientUtils.displayChatMessage("Opening configuration folder...")
        } catch (e: IOException) {
            ClientUtils.displayChatMessage("Error opening folder: ${e.message}")
        }
    }

    private fun applyTextColor(alpha: Int, isActive: Boolean): Int {
        if (isActive) {
            return Color(255, 255, 255, alpha).rgb
        }
        return if (NeverloseGui.getInstance().light) {
            Color(30, 30, 30, alpha).rgb
        } else {
            Color(255, 255, 255, alpha).rgb
        }
    }

    private fun applyTextColor(alpha: Int): Int = applyTextColor(alpha, false)

    private data class ButtonArea(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val action: () -> Unit
    )
}
