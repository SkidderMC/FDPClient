package net.ccbluex.liquidbounce.utils

import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.net.URL


object NotificationUtils {
    private val icon: URL = URL("http://icons.iconarchive.com/icons/papirus-team/papirus-apps/512/minecraft-icon.png")
    private val tray: SystemTray = SystemTray.getSystemTray()
    private val image: Image = Toolkit.getDefaultToolkit().createImage(icon)
    private val trayIcon = TrayIcon(image, "Minecraft")
    init {
        if (isSuppored()) {
            trayIcon.isImageAutoSize = true
            tray.add(trayIcon)
        }
    }

    fun isSuppored(): Boolean {
        return SystemTray.isSupported()
    }

    fun displayTray(Title: String, Text: String) {
        trayIcon.displayMessage(Title, Text, TrayIcon.MessageType.INFO)
    }
}