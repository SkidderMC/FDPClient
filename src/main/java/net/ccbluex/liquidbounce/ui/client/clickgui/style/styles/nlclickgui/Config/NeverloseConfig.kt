package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config

import java.io.File

data class NeverloseConfig(
    val name: String,
    val file: File,
    var isExpanded: Boolean = false
) {
    val author: String
        get() = "Local"
}
