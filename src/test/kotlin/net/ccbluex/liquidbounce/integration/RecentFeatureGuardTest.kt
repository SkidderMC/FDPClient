/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.integration

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.charset.StandardCharsets

class RecentFeatureGuardTest {

    @Test
    fun `clickgui contains recent interaction and allocation fixes`() {
        assertContains("nextgen-theme/src/routes/clickgui/Module.svelte", "contain: layout", "class=\"expand-arrow\"")
        assertContains("nextgen-theme/src/routes/clickgui/Panel.svelte", "width: 2px")
        assertContains("nextgen-theme/src/routes/clickgui/Search.svelte", "searchableModules", "replaceAll")
        assertContains("nextgen-theme/src/integration/rest.ts", "lastTypingState")
        assertContains("nextgen-theme/src/integration/ws.ts", "PING_PAYLOAD")
    }

    @Test
    fun `menu tooltips and modals escape scrolling containers`() {
        assertContains("nextgen-theme/src/routes/menu/common/ToolTip.svelte", "use:portal", "position: fixed")
        assertContains("nextgen-theme/src/routes/menu/common/modal/Modal.svelte", "use:portal")
        assertContains("nextgen-theme/src/integration/util.ts", "function portal")
    }

    @Test
    fun `menu header remains mounted between menu routes`() {
        assertContains("nextgen-theme/src/App.svelte", "menuRoutes", "<MenuContent>", "{#key \$location}")
        assertTrue(Files.isRegularFile(path("nextgen-theme/src/routes/menu/common/MenuContent.svelte")))
    }

    @Test
    fun `packaged theme references only existing assets`() {
        val root = path("src/main/resources/assets/minecraft/fdpclient/nextgen-clickgui")
        val html = String(Files.readAllBytes(root.resolve("index.html")), StandardCharsets.UTF_8)
        val references = "(?:src|href)=\"([^\"]+)\"".toRegex()
            .findAll(html)
            .map { it.groupValues[1] }
            .filterNot { it.startsWith("http") || it.startsWith("data:") }
            .toList()

        assertTrue("No local assets referenced by packaged index", references.isNotEmpty())
        references.forEach { reference ->
            assertTrue("Missing packaged UI asset: $reference", Files.isRegularFile(root.resolve(reference)))
        }
    }

    @Test
    fun `xray background opacity has a registered vertex alpha mixin`() {
        assertContains("src/main/java/net/ccbluex/liquidbounce/features/module/modules/visual/XRay.kt", "BackgroundOpacity")
        assertContains("src/main/resources/fdpclient.forge.mixins.json", "render.MixinWorldRenderer")
        assertContains(
            "src/main/java/net/ccbluex/liquidbounce/injection/forge/mixins/render/MixinWorldRenderer.java",
            "currentBackgroundAlpha",
            "putColorRGBA",
        )
    }

    @Test
    fun `recent movement lifecycle fixes remain present`() {
        assertContains("src/main/java/net/ccbluex/liquidbounce/features/module/modules/movement/FastBreak.kt", "VANILLA_BLOCK_HIT_DELAY")
        assertContains("src/main/java/net/ccbluex/liquidbounce/features/module/modules/movement/SnapTap.kt", "allowsMovementOverride")
        assertContains("src/main/java/net/ccbluex/liquidbounce/features/module/modules/player/scaffolds/Scaffold.kt", "canJumpTwoBlocksHigh")
        assertContains("src/main/java/net/ccbluex/liquidbounce/utils/movement/FallingPlayer.kt", "nextVanillaAirMotionY")
    }

    @Test
    fun `chunk replacement clears stale subscriber state before scanning`() {
        val source = read("src/main/java/net/ccbluex/liquidbounce/utils/block/ChunkScanner.kt")
        val clearIndex = source.indexOf("subscribers.forEach { it.clearChunk")
        val enqueueIndex = source.indexOf("chunkWork.add(ChunkWork(chunk))")
        assertTrue("Chunk clear must happen before replacement scan", clearIndex >= 0 && clearIndex < enqueueIndex)
    }

    @Test
    fun `recent visual and command features remain exposed`() {
        assertContains("src/main/java/net/ccbluex/liquidbounce/features/module/modules/combat/EasyPearl.kt", "ShowDistance")
        assertContains("src/main/java/net/ccbluex/liquidbounce/features/module/modules/visual/Trajectories.kt", "LineColorMode")
        assertContains("src/main/java/net/ccbluex/liquidbounce/features/module/modules/visual/Chams.kt", "glPushAttrib")
        assertContains("src/main/java/net/ccbluex/liquidbounce/features/command/commands/TeleportCommand.kt", "isFinite")
        assertTrue(Files.isRegularFile(path("src/main/java/net/ccbluex/liquidbounce/features/command/commands/VClipCommand.kt")))
    }

    private fun assertContains(file: String, vararg fragments: String) {
        val source = read(file)
        fragments.forEach { fragment -> assertTrue("$file is missing '$fragment'", fragment in source) }
    }

    private fun read(file: String): String =
        String(Files.readAllBytes(path(file)), StandardCharsets.UTF_8)

    private fun path(file: String): Path = Paths.get(file)
}
