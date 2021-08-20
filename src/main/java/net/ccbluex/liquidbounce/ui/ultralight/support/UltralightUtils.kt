package net.ccbluex.liquidbounce.ui.ultralight.support

import com.labymedia.ultralight.input.UltralightMouseEventButton

object UltralightUtils {
    fun getButtonByButtonID(key: Int): UltralightMouseEventButton? {
        return when(key){
            0 -> UltralightMouseEventButton.LEFT
            1 -> UltralightMouseEventButton.RIGHT
            2 -> UltralightMouseEventButton.MIDDLE
            else -> null // idk when this will happen lol
        }
    }
}