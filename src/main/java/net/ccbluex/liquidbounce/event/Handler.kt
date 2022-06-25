package net.ccbluex.liquidbounce.event

interface Handler {

    val target: Class<out Event>

    fun invoke(event: Event)
}

class HandlerFunction<T : Event>(private val func: (T) -> Unit, override val target: Class<T>) : Handler {

    override fun invoke(event: Event) {
        func(event as T)
    }
}