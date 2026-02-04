package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations

enum class Direction {
    FORWARDS,
    BACKWARDS;

    fun opposite(): Direction = if (this == FORWARDS) BACKWARDS else FORWARDS
}
