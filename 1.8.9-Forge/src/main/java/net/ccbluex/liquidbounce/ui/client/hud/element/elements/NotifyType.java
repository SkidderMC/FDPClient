package net.ccbluex.liquidbounce.ui.client.hud.element.elements;

import java.awt.*;

public enum NotifyType {
    OKAY(new Color(0,255,127),"check-circle"),
    WARN(new Color(255,75,0),"close-circle"),
    INFO(new Color(0, 160, 255),"information");

    public Color renderColor;
    public String icon;

    NotifyType(Color renderColor,String icon){
        this.renderColor=renderColor;
        this.icon=icon;
    }
}
