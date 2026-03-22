package net.ccbluex.liquidbounce.features.module.modules

interface NamedMode {
    val modeName: String
}

fun <T : NamedMode> Array<out T>.modeNames() = asList().modeNames()

fun <T : NamedMode> Iterable<T>.modeNames() = map(NamedMode::modeName).toTypedArray()

fun <T : NamedMode> Array<out T>.selectedMode(name: String) = asList().selectedMode(name)

fun <T : NamedMode> Iterable<T>.selectedMode(name: String) = first { it.modeName == name }
