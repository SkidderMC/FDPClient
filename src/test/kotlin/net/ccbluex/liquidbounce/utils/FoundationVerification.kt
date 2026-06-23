/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.config.RefreshableRangeValue
import net.ccbluex.liquidbounce.features.command.builder.ParameterBuilder
import net.ccbluex.liquidbounce.features.command.builder.buildCommand
import net.ccbluex.liquidbounce.file.gson.Exclude
import net.ccbluex.liquidbounce.file.gson.GsonProfiles
import net.ccbluex.liquidbounce.utils.math.geometry.Face
import net.ccbluex.liquidbounce.utils.math.geometry.Line
import net.ccbluex.liquidbounce.utils.math.geometry.Plane
import net.ccbluex.liquidbounce.utils.math.geometry.Ray
import net.ccbluex.liquidbounce.utils.math.geometry.approximatelyEquals
import net.ccbluex.liquidbounce.utils.render.Color4b
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.random.Random

object FoundationVerification {
    @JvmStatic
    fun main(args: Array<String>) {
        verifyGeometry()
        verifyColorPacking()
        verifyRefreshableRange()
        verifyGsonProfiles()
        verifyCommandDsl()
        println("Foundation verification passed")
    }

    private fun verifyGeometry() {
        val line = Line(Vec3(0.0, 0.0, 0.0), Vec3(2.0, 0.0, 0.0))
        check(line.project(Vec3(3.0, 4.0, 0.0)).approximatelyEquals(Vec3(3.0, 0.0, 0.0)))
        assertClose(16.0, line.distanceSquared(Vec3(3.0, 4.0, 0.0)))

        val plane = Plane(Vec3(0.0, 0.0, 2.0), Vec3(0.0, 0.0, 4.0))
        val ray = Ray(Vec3(0.0, 0.0, 0.0), Vec3(0.0, 0.0, 1.0))
        check(plane.intersect(ray)?.approximatelyEquals(Vec3(0.0, 0.0, 2.0)) == true)
        check(plane.intersect(Ray(Vec3(0.0, 0.0, 0.0), Vec3(1.0, 0.0, 0.0))) == null)

        val face = Face(listOf(
            Vec3(-1.0, -1.0, 2.0), Vec3(1.0, -1.0, 2.0),
            Vec3(1.0, 1.0, 2.0), Vec3(-1.0, 1.0, 2.0)
        ))
        check(face.intersect(ray)?.approximatelyEquals(Vec3(0.0, 0.0, 2.0)) == true)
        check(face.intersect(Ray(Vec3(2.0, 0.0, 0.0), Vec3(0.0, 0.0, 1.0))) == null)

        val boxHit = Ray(Vec3(-2.0, 0.5, 0.5), Vec3(1.0, 0.0, 0.0))
            .intersect(AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
        check(boxHit?.approximatelyEquals(Vec3(0.0, 0.5, 0.5)) == true)
    }

    private fun verifyColorPacking() {
        val color = Color4b.fromRgba(12, 34, 56, 78)
        check(color.red == 12 && color.green == 34 && color.blue == 56 && color.alpha == 78)
        check(color.packed == 0x4E0C2238)
        check(color.withAlpha(255).alpha == 255)
        check(Color4b.fromRgba(0, 0, 0).interpolate(Color4b.fromRgba(100, 50, 20), 0.5f) ==
            Color4b.fromRgba(50, 25, 10))
    }

    private fun verifyRefreshableRange() {
        val value = RefreshableRangeValue("Delay", 2f..4f, 0f..10f, " ticks")
        val first = value.refresh(Random(7))
        check(value.sample == first)
        check(first in 2f..4f)
        check(value.toJson().asJsonArray.let { it[0].asFloat == 2f && it[1].asFloat == 4f })

        value.set(6f..6f, saveImmediately = false)
        check(value.sample == 6f)
    }

    private fun verifyGsonProfiles() {
        val json = GsonProfiles.localFile.toJson(SerializationProbe("visible", "secret"))
        check("visible" in json)
        check("secret" !in json)
    }

    private fun verifyCommandDsl() {
        var result = 0
        val command = buildCommand("sum") {
            val left = parameter<Int>("left") { verifiedBy(ParameterBuilder.INTEGER_VALIDATOR) }
            val right = parameter<Int>("right") { verifiedBy(ParameterBuilder.INTEGER_VALIDATOR) }
            executes { result = left.cast(values) + this[right] }
        }

        check(command.usage() == "sum <left> <right>")
        command.handler?.invoke(command, arrayOf(4, 5))
        check(result == 9)
    }


    private class SerializationProbe(
        val visible: String,
        @field:Exclude val hidden: String
    )


    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 1.0E-9) {
        check(abs(expected - actual) <= tolerance) { "Expected $expected, got $actual" }
    }
}
