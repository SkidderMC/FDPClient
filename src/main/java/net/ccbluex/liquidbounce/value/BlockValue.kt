package net.ccbluex.liquidbounce.value

/**
 * Block value represents a value with a block
 */
class BlockValue(name: String, value: Int, canDisplay: () -> Boolean, suffix: String) : IntegerValue(name, value, 1, 197)