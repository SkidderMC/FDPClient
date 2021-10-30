package net.ccbluex.liquidbounce.ui.ultralight.support

import com.labymedia.ultralight.input.UltralightKey
import com.labymedia.ultralight.input.UltralightMouseEventButton
import org.lwjgl.input.Keyboard

object UltralightUtils {
    fun getButtonByButtonID(key: Int): UltralightMouseEventButton? {
        return when (key) {
            0 -> UltralightMouseEventButton.LEFT
            1 -> UltralightMouseEventButton.RIGHT
            2 -> UltralightMouseEventButton.MIDDLE
            else -> null // idk when this will happen lol
        }
    }

    // TODO: fix some thing to make this usable
//    fun lwjgl2ToUltralightModifiers():Int{
//        var ultralightModifiers = 0
//        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)||Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
//            ultralightModifiers = ultralightModifiers or UltralightInputModifier.ALT_KEY
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)||Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
//            ultralightModifiers = ultralightModifiers or UltralightInputModifier.CTRL_KEY
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_LMETA)||Keyboard.isKeyDown(Keyboard.KEY_RMETA)) {
//            ultralightModifiers = ultralightModifiers or UltralightInputModifier.META_KEY
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
//            ultralightModifiers = ultralightModifiers or UltralightInputModifier.SHIFT_KEY
//        }
//        return ultralightModifiers
//    }

    fun lwjgl2ToUltralightKey(key: Int) = when (key) {
        Keyboard.KEY_SPACE -> UltralightKey.SPACE
        Keyboard.KEY_APOSTROPHE -> UltralightKey.OEM_7
        Keyboard.KEY_COMMA -> UltralightKey.OEM_COMMA
        Keyboard.KEY_MINUS -> UltralightKey.OEM_MINUS
        Keyboard.KEY_PERIOD -> UltralightKey.OEM_PERIOD
        Keyboard.KEY_SLASH -> UltralightKey.OEM_2
        Keyboard.KEY_0 -> UltralightKey.NUM_0
        Keyboard.KEY_1 -> UltralightKey.NUM_1
        Keyboard.KEY_2 -> UltralightKey.NUM_2
        Keyboard.KEY_3 -> UltralightKey.NUM_3
        Keyboard.KEY_4 -> UltralightKey.NUM_4
        Keyboard.KEY_5 -> UltralightKey.NUM_5
        Keyboard.KEY_6 -> UltralightKey.NUM_6
        Keyboard.KEY_7 -> UltralightKey.NUM_7
        Keyboard.KEY_8 -> UltralightKey.NUM_8
        Keyboard.KEY_9 -> UltralightKey.NUM_9
        Keyboard.KEY_SEMICOLON -> UltralightKey.OEM_1
        Keyboard.KEY_EQUALS, Keyboard.KEY_NUMPADEQUALS -> UltralightKey.OEM_PLUS
        Keyboard.KEY_A -> UltralightKey.A
        Keyboard.KEY_B -> UltralightKey.B
        Keyboard.KEY_C -> UltralightKey.C
        Keyboard.KEY_D -> UltralightKey.D
        Keyboard.KEY_E -> UltralightKey.E
        Keyboard.KEY_F -> UltralightKey.F
        Keyboard.KEY_G -> UltralightKey.G
        Keyboard.KEY_H -> UltralightKey.H
        Keyboard.KEY_I -> UltralightKey.I
        Keyboard.KEY_J -> UltralightKey.J
        Keyboard.KEY_K -> UltralightKey.K
        Keyboard.KEY_L -> UltralightKey.L
        Keyboard.KEY_M -> UltralightKey.M
        Keyboard.KEY_N -> UltralightKey.N
        Keyboard.KEY_O -> UltralightKey.O
        Keyboard.KEY_P -> UltralightKey.P
        Keyboard.KEY_Q -> UltralightKey.Q
        Keyboard.KEY_R -> UltralightKey.R
        Keyboard.KEY_S -> UltralightKey.S
        Keyboard.KEY_T -> UltralightKey.T
        Keyboard.KEY_U -> UltralightKey.U
        Keyboard.KEY_V -> UltralightKey.V
        Keyboard.KEY_W -> UltralightKey.W
        Keyboard.KEY_X -> UltralightKey.X
        Keyboard.KEY_Y -> UltralightKey.Y
        Keyboard.KEY_Z -> UltralightKey.Z
        Keyboard.KEY_LBRACKET -> UltralightKey.OEM_4
        Keyboard.KEY_BACKSLASH -> UltralightKey.OEM_5
        Keyboard.KEY_RBRACKET -> UltralightKey.OEM_6
        Keyboard.KEY_GRAVE -> UltralightKey.OEM_3
        Keyboard.KEY_ESCAPE -> UltralightKey.ESCAPE
        Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> UltralightKey.RETURN
        Keyboard.KEY_TAB -> UltralightKey.TAB
        Keyboard.KEY_BACK -> UltralightKey.BACK
        Keyboard.KEY_INSERT -> UltralightKey.INSERT
        Keyboard.KEY_DELETE -> UltralightKey.DELETE
        Keyboard.KEY_RIGHT -> UltralightKey.RIGHT
        Keyboard.KEY_LEFT -> UltralightKey.LEFT
        Keyboard.KEY_DOWN -> UltralightKey.DOWN
        Keyboard.KEY_UP -> UltralightKey.UP
        Keyboard.KEY_PRIOR -> UltralightKey.PRIOR
        Keyboard.KEY_NEXT -> UltralightKey.NEXT
        Keyboard.KEY_HOME -> UltralightKey.HOME
        Keyboard.KEY_END -> UltralightKey.END
        Keyboard.KEY_CAPITAL -> UltralightKey.CAPITAL
        Keyboard.KEY_SCROLL -> UltralightKey.SCROLL
        Keyboard.KEY_NUMLOCK -> UltralightKey.NUMLOCK
//        PRINT_SCREEN -> UltralightKey.SNAPSHOT
        Keyboard.KEY_PAUSE -> UltralightKey.PAUSE
        Keyboard.KEY_F1 -> UltralightKey.F1
        Keyboard.KEY_F2 -> UltralightKey.F2
        Keyboard.KEY_F3 -> UltralightKey.F3
        Keyboard.KEY_F4 -> UltralightKey.F4
        Keyboard.KEY_F5 -> UltralightKey.F5
        Keyboard.KEY_F6 -> UltralightKey.F6
        Keyboard.KEY_F7 -> UltralightKey.F7
        Keyboard.KEY_F8 -> UltralightKey.F8
        Keyboard.KEY_F9 -> UltralightKey.F9
        Keyboard.KEY_F10 -> UltralightKey.F10
        Keyboard.KEY_F11 -> UltralightKey.F11
        Keyboard.KEY_F12 -> UltralightKey.F12
        Keyboard.KEY_F13 -> UltralightKey.F13
        Keyboard.KEY_F14 -> UltralightKey.F14
        Keyboard.KEY_F15 -> UltralightKey.F15
        Keyboard.KEY_F16 -> UltralightKey.F16
        Keyboard.KEY_F17 -> UltralightKey.F17
        Keyboard.KEY_F18 -> UltralightKey.F18
        Keyboard.KEY_F19 -> UltralightKey.F19
//        Keyboard.KEY_F20 -> UltralightKey.F20
//        Keyboard.KEY_F21 -> UltralightKey.F21
//        Keyboard.KEY_F22 -> UltralightKey.F22
//        Keyboard.KEY_F23 -> UltralightKey.F23
//        Keyboard.KEY_F24 -> UltralightKey.F24
        Keyboard.KEY_NUMPAD0 -> UltralightKey.NUMPAD0
        Keyboard.KEY_NUMPAD1 -> UltralightKey.NUMPAD1
        Keyboard.KEY_NUMPAD2 -> UltralightKey.NUMPAD2
        Keyboard.KEY_NUMPAD3 -> UltralightKey.NUMPAD3
        Keyboard.KEY_NUMPAD4 -> UltralightKey.NUMPAD4
        Keyboard.KEY_NUMPAD5 -> UltralightKey.NUMPAD5
        Keyboard.KEY_NUMPAD6 -> UltralightKey.NUMPAD6
        Keyboard.KEY_NUMPAD7 -> UltralightKey.NUMPAD7
        Keyboard.KEY_NUMPAD8 -> UltralightKey.NUMPAD8
        Keyboard.KEY_NUMPAD9 -> UltralightKey.NUMPAD9
        Keyboard.KEY_DECIMAL -> UltralightKey.DECIMAL
        Keyboard.KEY_DIVIDE -> UltralightKey.DIVIDE
        Keyboard.KEY_MULTIPLY -> UltralightKey.MULTIPLY
        Keyboard.KEY_SUBTRACT -> UltralightKey.SUBTRACT
        Keyboard.KEY_ADD -> UltralightKey.ADD
        Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT -> UltralightKey.SHIFT
        Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL -> UltralightKey.CONTROL
        Keyboard.KEY_LMENU, Keyboard.KEY_RMENU -> UltralightKey.MENU
        Keyboard.KEY_LMETA -> UltralightKey.LWIN
        Keyboard.KEY_RMETA -> UltralightKey.RWIN
        else -> UltralightKey.UNKNOWN
    }
}