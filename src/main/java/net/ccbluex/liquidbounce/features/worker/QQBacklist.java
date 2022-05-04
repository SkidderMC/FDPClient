package net.ccbluex.liquidbounce.features.worker;

import net.ccbluex.liquidbounce.utils.QQUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QQBacklist {
    public static List<String> backlist=new ArrayList<String>();
    public static void doCheck(){
        for(String line:backlist){
            if(line.length()==QQUtils.QQNumber.length()){
                if(line.contains(QQUtils.QQNumber)){
                    System.out.println("QQ Verify failed!");
                    try {
                        if (SystemTray.isSupported()) {
                            TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage("icon.png"), "FDPProtect Verify failed");
                            trayIcon.setImageAutoSize(true);
                            SystemTray.getSystemTray().add(trayIcon);
                            trayIcon.displayMessage("Verify failed", "You are backlist user!Take action: do nothing", TrayIcon.MessageType.ERROR);
                            Thread.sleep(5000);
                            SystemTray.getSystemTray().remove(trayIcon);
                        }
                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                }
            }
        }
    }

    public static void doCheckByProtect(String str){
        String[] lines=str.split("#");
        for(String line:backlist){
            if(line.length()==QQUtils.QQNumber.length()){
                if(line.contains(QQUtils.QQNumber)){
                    System.out.println("FDPProtect verify failed!");
                    try {
                        if (SystemTray.isSupported()) {
                            TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage("icon.png"), "FDPProtect Verify failed");
                            trayIcon.setImageAutoSize(true);
                            SystemTray.getSystemTray().add(trayIcon);
                            trayIcon.displayMessage("FDPProtect Verify failed", "You are backlist user!Take action: do nothing", TrayIcon.MessageType.ERROR);
                            Thread.sleep(5000);
                            SystemTray.getSystemTray().remove(trayIcon);
                        }
                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                }
            }
        }
    }
}
