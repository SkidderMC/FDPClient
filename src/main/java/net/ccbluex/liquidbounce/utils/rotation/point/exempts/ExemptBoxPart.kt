/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point.exempts

import net.minecraft.util.Vec3

enum class ExemptBoxPart(val tag: String) : ExemptPoint {

    HEAD("Head") {
        override fun predicate(context: ExemptContext, point: Vec3): Boolean {
            val length = (context.box.maxY - context.box.minY) / entries.size
            return point.yCoord <= context.box.maxY &&
                point.yCoord > context.box.maxY - length
        }
    },
    BODY("Body") {
        override fun predicate(context: ExemptContext, point: Vec3): Boolean {
            val length = (context.box.maxY - context.box.minY) / entries.size
            return point.yCoord <= context.box.maxY - length &&
                point.yCoord >= context.box.minY + length
        }
    },
    FEET("Feet") {
        override fun predicate(context: ExemptContext, point: Vec3): Boolean {
            val length = (context.box.maxY - context.box.minY) / entries.size
            return point.yCoord >= context.box.minY &&
                point.yCoord < context.box.minY + length
        }
    };

    fun isHigherThan(other: ExemptBoxPart) = entries.indexOf(this) < entries.indexOf(other)

    companion object {
        val tags: Array<String> = entries.map { it.tag }.toTypedArray()

        fun fromTag(tag: String): ExemptBoxPart? = entries.find { it.tag.equals(tag, true) }
    }
}
