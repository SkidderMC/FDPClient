/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.config.RefreshableRangeValue
import net.ccbluex.liquidbounce.config.ConfigMigration
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.DoubleRangeValue
import net.ccbluex.liquidbounce.config.DoubleValue
import net.ccbluex.liquidbounce.config.KeyBindActionMode
import net.ccbluex.liquidbounce.config.KeyBindValue
import net.ccbluex.liquidbounce.config.LongRangeValue
import net.ccbluex.liquidbounce.config.LongValue
import net.ccbluex.liquidbounce.config.ModeValueGroup
import net.ccbluex.liquidbounce.config.MutableListValue
import net.ccbluex.liquidbounce.config.Vec2Value
import net.ccbluex.liquidbounce.config.ValueOrganizer
import net.ccbluex.liquidbounce.features.command.builder.ParameterBuilder
import net.ccbluex.liquidbounce.features.command.builder.buildCommand
import net.ccbluex.liquidbounce.file.gson.Exclude
import net.ccbluex.liquidbounce.file.gson.GsonProfiles
import net.ccbluex.liquidbounce.event.ClientChange
import net.ccbluex.liquidbounce.event.ClientChangeBus
import net.ccbluex.liquidbounce.utils.math.geometry.Face
import net.ccbluex.liquidbounce.utils.math.geometry.Line
import net.ccbluex.liquidbounce.utils.math.geometry.Plane
import net.ccbluex.liquidbounce.utils.math.geometry.Ray
import net.ccbluex.liquidbounce.utils.math.geometry.approximatelyEquals
import net.ccbluex.liquidbounce.utils.render.Color4b
import net.ccbluex.liquidbounce.utils.render.DynamicAtlasAllocator
import net.ccbluex.liquidbounce.utils.sorting.ComparatorChain
import net.ccbluex.liquidbounce.utils.rotation.ModernRotationEngine
import net.ccbluex.liquidbounce.utils.client.AnticheatModeAdvisor
import net.ccbluex.liquidbounce.utils.client.AnticheatProfile
import net.ccbluex.liquidbounce.utils.client.ModeRisk
import net.ccbluex.liquidbounce.handler.api.PresetCatalog
import net.ccbluex.liquidbounce.handler.api.CatalogPreset
import net.ccbluex.liquidbounce.handler.api.PresetCatalogService
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
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
        verifyAtlasAllocator()
        verifyChangeBus()
        verifyValueTypes()
        verifyModeChoices()
        verifyAutomaticValueOrganization()
        verifyPresetCatalogMatching()
        verifyComparatorChain()
        verifyNeuralSmoother()
        verifyAnticheatAdvice()
        println("Foundation verification passed")
    }

    private fun verifyAnticheatAdvice() {
        check(AnticheatModeAdvisor.resolve("Auto", "Grim") == AnticheatProfile.GRIM)
        check(AnticheatModeAdvisor.resolve("Auto", "NoCheatPlus") == AnticheatProfile.NCP)

        val all = arrayOf("Legit", "Jump", "Grim", "GrimVertical", "Cancel")
        val filtered = AnticheatModeAdvisor.filteredModes("Velocity", "Grim", null, all)
        check(filtered.contentEquals(arrayOf("Grim", "Legit", "GrimVertical", "Jump")))
        check(AnticheatModeAdvisor.filteredModes("Velocity", "Auto", null, all).contentEquals(all))

        val safe = AnticheatModeAdvisor.assess("Criticals", "Jump", "Grim", null)
        val unsafe = AnticheatModeAdvisor.assess("Criticals", "Packet", "Grim", null)
        check(safe.risk == ModeRisk.RECOMMENDED)
        check(unsafe.risk == ModeRisk.LIKELY_DETECTED && unsafe.recommendedMode == "Jump")
        check(AnticheatModeAdvisor.resolve("Auto", "Hypixel Watchdog") == AnticheatProfile.WATCHDOG)
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

    private fun verifyAtlasAllocator() {
        val allocator = DynamicAtlasAllocator(16, 16, maxPages = 1, padding = 0)
        val first = checkNotNull(allocator.allocate(8, 8))
        val second = checkNotNull(allocator.allocate(8, 8))
        check(first != second)
        check(first.page == 0 && second.page == 0)
        check(allocator.usage().allocationCount == 2)
        allocator.free(first)
        allocator.free(second)
        check(allocator.usage().allocationCount == 0)
        check(allocator.allocate(16, 16) != null)
        check(allocator.allocate(17, 1) == null)
        allocator.clear()
        check(allocator.pageCount == 0)
        check(allocator.allocate(16, 16) != null)
    }

    private fun verifyChangeBus() {
        val received = ArrayList<ClientChange>()
        val unsubscribe = ClientChangeBus.subscribe(received::add)
        ClientChangeBus.publish(ClientChange.Configuration("verification"))
        unsubscribe()
        ClientChangeBus.publish(ClientChange.Configuration("ignored"))
        check(received == listOf(ClientChange.Configuration("verification")))
    }

    private fun verifyValueTypes() {
        val double = DoubleValue("Double", 1.25, 0.0..2.0)
        check(double.set(3.0, saveImmediately = false) && double.get() == 2.0)
        double.fromJson(JsonPrimitive(0.75))
        check(double.get() == 0.75)

        val long = LongValue("Long", 5L, 0L..10L)
        long.fromJson(JsonPrimitive(9L))
        check(long.get() == 9L)

        val doubles = DoubleRangeValue("DoubleRange", 1.0..2.0, 0.0..5.0)
        doubles.fromText("3.0..4.0")
        check(doubles.get().start == 3.0 && doubles.get().endInclusive == 4.0)

        val longs = LongRangeValue("LongRange", 1L..2L, 0L..5L)
        longs.fromJson(longs.toJson())
        check(longs.get() == (1L..2L))

        val list = MutableListValue("Entries", listOf("one"))
        list.add("two", saveImmediately = false)
        list.update(0, "first", saveImmediately = false)
        check(list.get() == listOf("first", "two"))
        list.removeAt(1, saveImmediately = false)
        check(list.get() == listOf("first"))

        val bind = KeyBindValue("Bind", 12, KeyBindActionMode.HOLD)
        val restored = KeyBindValue("Bind")
        restored.fromJson(bind.toJson())
        check(restored.get() == 12 && restored.actionMode == KeyBindActionMode.HOLD)

        val vector = Vec2Value("Position", doubleArrayOf(2.0, 4.0), useLocateButton = true)
        vector.fromText("6.5,8.5")
        check(vector.x == 6.5 && vector.y == 8.5 && vector.useLocateButton)

        val root = Configurable("Root")
        val nested = Configurable("Nested")
        val child = nested.int("Child", 0, 0..10)
        root.addValue(nested)
        val legacy = JsonObject().apply { addProperty("Child", 7) }
        val migrated = ConfigMigration.findValue(legacy, nested)!!.asJsonObject
        child.fromJson(migrated["Child"])
        check(child.get() == 7)
    }

    private fun verifyModeChoices() {
        val lifecycle = mutableListOf<String>()
        val group = ModeValueGroup("Mode", arrayOf("A", "B"), "A")
        val first = group.mode("A", { lifecycle += "A+" }, { lifecycle += "A-" }) {
            int("Amount", 1, 0..5)
        }
        val second = group.mode("B", { lifecycle += "B+" }, { lifecycle += "B-" })
        check(first.selected && !second.selected && lifecycle == listOf("A+"))
        group.modeValue.set("B", saveImmediately = false)
        check(!first.selected && second.selected && lifecycle == listOf("A+", "A-", "B+"))
    }

    private fun verifyAutomaticValueOrganization() {
        val root = Configurable("Legacy")
        root.float("Range", 3f, 1f..6f)
        root.float("ScanDistance", 6f, 1f..12f)
        root.int("Delay", 2, 0..20)
        root.int("Cooldown", 4, 0..20)
        root.color("BoxColor", 0xFFFFFFFF.toInt())

        val groups = ValueOrganizer.organize(root)
        check(groups.map { it.name } == listOf("Range", "Timing"))
        check(root.findDeep("Range") != null && root.findDeep("Cooldown") != null)
        check(root.values.filterIsInstance<Configurable>().flatMap { it.values }.size == 4)
        check(root.values.any { it.name == "BoxColor" })

        val explicit = Configurable("Explicit")
        explicit.addValue(Configurable("Custom"))
        explicit.int("One", 1, 0..2)
        explicit.int("Two", 1, 0..2)
        explicit.int("Three", 1, 0..2)
        check(ValueOrganizer.organize(explicit).isEmpty())
    }

    private fun verifyPresetCatalogMatching() {
        val preset = CatalogPreset(
            id = "example",
            url = "presets/example.txt",
            sha256 = PresetCatalogService.digest("safe".toByteArray()),
            servers = listOf("*.example.net"),
        )
        val catalog = PresetCatalog(presets = listOf(preset))
        check(PresetCatalogService.findForServer(catalog, "play.example.net:25565") === preset)
        check(PresetCatalogService.findForServer(catalog, "notexample.net") == null)
        check(PresetCatalogService.domainMatches("example.net", "*.example.net"))
    }

    private fun verifyComparatorChain() {
        data class Candidate(val group: Int, val score: Int)
        val comparator = ComparatorChain<Candidate>(compareBy { it.group }, compareBy { it.score })
        val sorted = listOf(Candidate(1, 3), Candidate(0, 9), Candidate(1, 1)).sortedWith(comparator)
        check(sorted == listOf(Candidate(0, 9), Candidate(1, 1), Candidate(1, 3)))
    }

    private fun verifyNeuralSmoother() {
        val calm = ModernRotationEngine.neuralFactor(0.05f, 0.02f, 0f, 0.1f, 0f, 0.8f, 1f)
        val tracking = ModernRotationEngine.neuralFactor(0.8f, 0.1f, 0.5f, 0.8f, 0.7f, 0.8f, 1f)
        check(calm in 0.005f..1f && tracking in 0.005f..1f)
        check(tracking > calm)
    }

    private class SerializationProbe(
        val visible: String,
        @field:Exclude val hidden: String
    )


    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 1.0E-9) {
        check(abs(expected - actual) <= tolerance) { "Expected $expected, got $actual" }
    }
}
