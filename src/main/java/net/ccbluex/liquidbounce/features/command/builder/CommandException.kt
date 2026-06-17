/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.builder

/**
 * Thrown by the command builder layer when parsing/validation/execution fails.
 *
 * In LiquidBounce nextgen this carried a translation key plus arguments which were resolved
 * against the language files. FDPClient 1.8.9 has no command translation system, so instead we
 * carry an already-formatted, human readable [message]. The dispatcher catches this and prints it
 * to chat in red (§c) instead of letting an exception bubble up to the chat input handler.
 *
 * This keeps every builder command crash-safe: a bad argument produces a chat message, never a
 * Minecraft crash.
 *
 * @param message ready to display error text (without color codes; the dispatcher adds §c)
 * @param usageInfo optional usage line(s) to print after the error
 */
class CommandException(
    message: String,
    val usageInfo: List<String>? = null,
    cause: Throwable? = null
) : Exception(message, cause)
