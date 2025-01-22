/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
@file:Suppress("NOTHING_TO_INLINE")
package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.nbt.*

inline operator fun NBTTagCompound.set(key: String, value: Byte) {
    setByte(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: Short) {
    setShort(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: Int) {
    setInteger(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: Long) {
    setLong(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: Float) {
    setFloat(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: Double) {
    setDouble(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: String) {
    setString(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: Boolean) {
    setBoolean(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: ByteArray) {
    setByteArray(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: IntArray) {
    setIntArray(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: NBTTagCompound) {
    setTag(key, value)
}

inline operator fun NBTTagCompound.set(key: String, value: NBTTagList) {
    setTag(key, value)
}

inline fun NBTTagCompound(builderAction: NBTTagCompound.() -> Unit): NBTTagCompound {
    return NBTTagCompound().apply(builderAction)
}

inline fun NBTTagList(builderAction: NBTTagList.() -> Unit): NBTTagList {
    return NBTTagList().apply(builderAction)
}

inline fun NBTTagList.appendTag(builderAction: NBTTagCompound.() -> Unit) {
    appendTag(NBTTagCompound().apply(builderAction))
}
