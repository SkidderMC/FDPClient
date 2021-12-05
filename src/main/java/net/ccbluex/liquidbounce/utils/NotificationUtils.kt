package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import net.minecraftforge.fml.common.FMLCommonHandler
import org.lwjgl.opengl.Display
import java.awt.*
import java.net.URL
import javax.swing.JOptionPane


object NotificationUtils {
    private val pop = PopupMenu()
    private val menu = MenuItem("Quit")
    private val icon: URL = URL("http://icons.iconarchive.com/icons/papirus-team/papirus-apps/512/minecraft-icon.png")
    private val tray: SystemTray = SystemTray.getSystemTray()
    private val image: Image = Toolkit.getDefaultToolkit().createImage(icon)
    private val trayIcon = TrayIcon(image, "Minecraft", pop)
    init {
        if (isSuppored()) {
            trayIcon.isImageAutoSize = true
            menu.addActionListener {
                val n = JOptionPane.showConfirmDialog(Display.getParent(), "Do you want to quit?", "Quit", JOptionPane.YES_NO_OPTION)
                if (n == JOptionPane.YES_OPTION) {
                    if (!LiquidBounce.isStarting || !LiquidBounce.isLoadingConfig) {
                        LiquidBounce.stopClient()
                        FMLCommonHandler.instance().exitJava(0, true)
                    } else {
                        JOptionPane.showMessageDialog(Display.getParent(), "FDPClient is Loading!!")
                    }
                }
            }
            pop.add(menu)
            tray.add(trayIcon)
        }
    }

    fun isSuppored(): Boolean {
        return SystemTray.isSupported()
    }

    fun displayTray(title: String, text: String) {
        displayTray(title, text, TrayIcon.MessageType.INFO)
    }

    fun displayTray(title: String, text: String, messageType: TrayIcon.MessageType) {
        trayIcon.displayMessage(title, text, messageType)
    }
}