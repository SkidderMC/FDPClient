/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

class ModuleModernApiAuditTest {

    @Test
    fun `modules do not use retired render facades`() {
        val violations = kotlinSources(MODULE_ROOT).flatMap { source ->
            Files.readAllLines(source).mapIndexedNotNull { index, line ->
                LEGACY_RENDER_REFERENCES.firstOrNull(line::contains)?.let {
                    "${source.fileName}:${index + 1} uses $it"
                }
            }
        }

        assertTrue(violations.joinToString("\n"), violations.isEmpty())
    }

    @Test
    fun `retired tick scheduler is absent from production sources`() {
        val violations = kotlinSources(MAIN_ROOT).filter { source ->
            Files.readAllLines(source).any { "WaitTickUtils" in it }
        }

        assertTrue(
            violations.joinToString("\n") { "${it.fileName} still references WaitTickUtils" },
            violations.isEmpty(),
        )
    }

    private fun kotlinSources(root: Path): List<Path> =
        Files.walk(root).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }.toList()
        }

    private companion object {
        val MAIN_ROOT: Path = Paths.get("src/main/java")
        val MODULE_ROOT: Path = MAIN_ROOT.resolve("net/ccbluex/liquidbounce/features/module/modules")

        val LEGACY_RENDER_REFERENCES = listOf(
            "RenderUtils.glColor",
            "RenderUtils.drawAxisAlignedBB",
            "RenderUtils.drawFilledBox",
            "RenderUtils.renderNameTag",
            "RenderUtils.color(",
            "RenderUtils.resetColor",
            "RenderUtils.drawBlockDamageText",
            "RenderUtils.drawLine",
            "RenderUtils.drawCrystal",
            "RenderUtils.drawZavz",
            "RenderUtils.drawJello",
            "RenderUtils.drawFDP",
            "RenderUtils.drawLies",
            "RenderUtils.drawTexturedModalRect",
            "RenderUtils.checkSetupFBO",
            "RenderUtils.glStateManagerColor",
        )
    }
}
