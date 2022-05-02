package net.ccbluex.liquidbounce.utils;

import java.awt.*;

public class SystemUtil {
    public static void ShowSystemNotification(String title, String text, TrayIcon.MessageType type, Long delay)
            throws AWTException {
        if (SystemTray.isSupported()) {
            TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage("icon.png"), title);
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
            trayIcon.displayMessage(title, text, type);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    SystemTray.getSystemTray().remove(trayIcon);
                }
            }).start();
        }
    }
}
