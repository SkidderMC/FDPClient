/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.utils.extensions.toLowerCamelCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModuleDescriptionAuditTest {

    @Test
    fun `every built-in module has a fallback description`() {
        val sourceRoot = File("src/main/java/net/ccbluex/liquidbounce/features/module/modules")
        val names = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "java") }
            .flatMap { file ->
                val source = file.readText()
                (DIRECT_MODULE.findAll(source).map { it.groupValues[1] } +
                    NAMED_MODULE.findAll(source).map { it.groupValues[1] } +
                    CHART_RECORDER.findAll(source).map { it.groupValues[1] })
            }
            .toList()

        assertEquals(EXPECTED_MODULE_COUNT, names.size)
        assertEquals(names.size, names.map(String::lowercase).distinct().size)

        val language = File("src/main/resources/assets/minecraft/fdpclient/lang/en_US.json")
        val translations = language.reader().use {
            JsonParser().parse(it).asJsonObject["translations"].asJsonObject
        }
        val missing = names.filterNot {
            translations.has("module.${it.toLowerCamelCase()}.description")
        }

        assertTrue("Missing fallback descriptions: ${missing.sorted()}", missing.isEmpty())
    }

    private companion object {
        const val EXPECTED_MODULE_COUNT = 297
        val DIRECT_MODULE = Regex(
            """(?s)(?::|extends)\s+Module\s*\(\s*\"([^\"]+)\"\s*,\s*Category\.[A-Z]+"""
        )
        val NAMED_MODULE = Regex("""(?s):\s+Module\s*\(\s*name\s*=\s*\"([^\"]+)\"""")
        val CHART_RECORDER = Regex(""":\s+ChartRecorderModule\s*\(\s*\"([^\"]+)\"""")
    }
}
