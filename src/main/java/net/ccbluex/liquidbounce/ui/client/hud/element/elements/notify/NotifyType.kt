package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import java.awt.Color

enum class NotifyType(var renderColor: Color, var icon: String) {
    OKAY(Color(0, 255, 127), "check-circle"),
    WARN(Color(255, 75, 0), "close-circle"),
    INFO(Color(0, 160, 255),"information");
}


enum class FadeState { IN, STAY, OUT, END }