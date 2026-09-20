/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class NextGenHudComponentManagerTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var stateFile: File

    @Before
    fun setUp() {
        stateFile = File(temporaryFolder.root, "hud-components.json")
        NextGenHudComponentManager.resetForTests(stateFile)
    }

    @After
    fun tearDown() {
        NextGenHudComponentManager.resetForTests()
    }

    @Test
    fun `catalog exposes every described component and enforces singleton semantics`() {
        val initial = NextGenHudComponentManager.components()
        val catalog = NextGenHudComponentManager.catalog(NextGenHudComponentManager.THEME_ID)

        assertEquals(19, initial.size())
        assertEquals(19, catalog.size())
        assertTrue(catalog.all { it.asJsonObject.get("description").asString.isNotBlank() })
        assertEquals(initial.size(), initial.map { it.asJsonObject.get("id").asString }.toSet().size)
        assertTrue(initial.all { component ->
            component.asJsonObject.settings().let { settings ->
                settings.has("opacity") && settings.has("elementScale") && settings.has("accentColor") &&
                    settings.has("backgroundColor") && settings.has("textColor") && settings.has("shadow")
            }
        })
        val watermarkSettings = initial.component("Watermark").settings()
        assertTrue(watermarkSettings.has("showPlayerName"))
        assertTrue(watermarkSettings.has("showAnticheat"))
        assertTrue(watermarkSettings.has("showBiomeLight"))
        val arrayListSettings = initial.component("ArrayList").settings()
        assertTrue(arrayListSettings.has("textMode"))
        assertTrue(arrayListSettings.has("maxTextGradientColors"))
        assertTrue(arrayListSettings.has("inactiveModulesStyle"))

        val arrayListId = initial.component("ArrayList").get("id").asString
        assertEquals(
            NextGenHudComponentManager.MutationResult.CONFLICT,
            NextGenHudComponentManager.addComponent(arrayListId),
        )

        val textId = initial.component("Text").get("id").asString
        assertEquals(
            NextGenHudComponentManager.MutationResult.UPDATED,
            NextGenHudComponentManager.addComponent(textId),
        )
        assertEquals(
            NextGenHudComponentManager.MutationResult.UPDATED,
            NextGenHudComponentManager.addComponent(textId),
        )

        val textComponents = NextGenHudComponentManager.components().components("Text")
        assertEquals(2, textComponents.size)
        assertTrue(textComponents.all { it.settings().get("enabled").asBoolean })
        assertNotEquals(textComponents[0].get("id").asString, textComponents[1].get("id").asString)
    }

    @Test
    fun `settings are transactional bounded and persisted across reloads`() {
        val textId = NextGenHudComponentManager.components().component("Text").get("id").asString
        assertEquals(
            NextGenHudComponentManager.MutationResult.UPDATED,
            NextGenHudComponentManager.addComponent(textId),
        )

        assertEquals(
            NextGenHudComponentManager.MutationResult.UPDATED,
            NextGenHudComponentManager.updateAlignment(
                textId,
                """{
                    "horizontalAlignment":"Right",
                    "horizontalOffset":12.5,
                    "verticalAlignment":"Bottom",
                    "verticalOffset":24
                }""",
            ),
        )

        val settings = requireNotNull(NextGenHudComponentManager.componentSettings(textId))
        settings.setting("Size").addProperty("value", 1000)
        assertEquals(
            NextGenHudComponentManager.MutationResult.UPDATED,
            NextGenHudComponentManager.updateSettings(textId, settings.toString()),
        )
        assertEquals(100, NextGenHudComponentManager.components().componentById(textId).settings().get("size").asInt)

        val arrayList = NextGenHudComponentManager.components().component("ArrayList")
        val arrayListId = arrayList.get("id").asString
        val invalidSettings = requireNotNull(
            NextGenHudComponentManager.componentSettings(arrayListId)
        )
        invalidSettings.setting("ShowTags").addProperty("value", false)
        invalidSettings.setting("Order").addProperty("value", "NotAChoice")
        assertEquals(
            NextGenHudComponentManager.MutationResult.INVALID,
            NextGenHudComponentManager.updateSettings(arrayListId, invalidSettings.toString()),
        )
        val unchanged = NextGenHudComponentManager.components().componentById(arrayListId).settings()
        assertTrue(unchanged.get("showTags").asBoolean)
        assertEquals("Descending", unchanged.get("order").asString)

        val disableSettings = requireNotNull(NextGenHudComponentManager.componentSettings(textId))
        disableSettings.setting("Enabled").addProperty("value", false)
        assertEquals(
            NextGenHudComponentManager.MutationResult.UPDATED,
            NextGenHudComponentManager.updateSettings(textId, disableSettings.toString()),
        )
        val disabled = NextGenHudComponentManager.components().componentById(textId)
        assertFalse(disabled.settings().get("enabled").asBoolean)
        assertEquals("Center", disabled.settings().getAsJsonObject("alignment")
            .get("horizontalAlignment").asString)

        assertEquals(
            NextGenHudComponentManager.MutationResult.UPDATED,
            NextGenHudComponentManager.addComponent(textId),
        )
        assertTrue(stateFile.isFile)

        NextGenHudComponentManager.resetForTests(stateFile)
        val restored = NextGenHudComponentManager.components().componentById(textId)
        assertTrue(restored.settings().get("enabled").asBoolean)
        assertEquals(100, restored.settings().get("size").asInt)
    }

    @Test
    fun `invalid registry entries and corrupt state fall back safely`() {
        val inventory = NextGenHudComponentManager.components().component("InventoryStatistics")
        val id = inventory.get("id").asString
        val settings = requireNotNull(NextGenHudComponentManager.componentSettings(id))
        settings.setting("Items").add("value", JsonArray().apply { add(JsonPrimitive("invalid identifier")) })

        assertEquals(
            NextGenHudComponentManager.MutationResult.INVALID,
            NextGenHudComponentManager.updateSettings(id, settings.toString()),
        )

        stateFile.writeText("{broken", Charsets.UTF_8)
        NextGenHudComponentManager.resetForTests(stateFile)
        assertEquals(19, NextGenHudComponentManager.components().size())
    }

    @Test
    fun `broken watermark background from the first modern HUD build is migrated`() {
        val watermark = NextGenHudComponentManager.components().component("Watermark")
        val id = watermark.get("id").asString
        val settings = requireNotNull(NextGenHudComponentManager.componentSettings(id))
        settings.setting("Background Color").addProperty("value", 2_852_450_320L)

        assertEquals(
            NextGenHudComponentManager.MutationResult.UPDATED,
            NextGenHudComponentManager.updateSettings(id, settings.toString()),
        )
        assertEquals(
            2_852_450_320L,
            NextGenHudComponentManager.components().component("Watermark").settings().get("backgroundColor").asLong,
        )

        NextGenHudComponentManager.resetForTests(stateFile)
        assertEquals(
            2_853_180_704L,
            NextGenHudComponentManager.components().component("Watermark").settings().get("backgroundColor").asLong,
        )
    }

    @Test
    fun `specific component routes win over the generic theme route`() {
        NextGenClickGuiServer.start()
        try {
            val (catalogStatus, catalogBody) = request("GET", "/api/v1/client/components/fdpclient/catalog")
            assertEquals(200, catalogStatus)
            assertEquals(19, JsonArrayParser.parse(catalogBody).size())

            val unknownStatus = request(
                "GET",
                "/api/v1/client/components/00000000-0000-0000-0000-000000000000/settings",
            ).first
            assertEquals(404, unknownStatus)

            val textId = NextGenHudComponentManager.components().component("Text").get("id").asString
            val invalidAlignmentStatus = request(
                "POST",
                "/api/v1/client/components/$textId/alignment",
                "{\"horizontalAlignment\":\"Invalid\"}",
            ).first
            assertEquals(400, invalidAlignmentStatus)
        } finally {
            NextGenClickGuiServer.stop()
        }
    }

    private fun JsonArray.components(name: String): List<JsonObject> = map { it.asJsonObject }
        .filter { it.get("name").asString == name }

    private fun JsonArray.component(name: String): JsonObject = components(name).single()

    private fun JsonArray.componentById(id: String): JsonObject = map { it.asJsonObject }
        .single { it.get("id").asString == id }

    private fun JsonObject.settings(): JsonObject = getAsJsonObject("settings")

    private fun JsonObject.setting(name: String): JsonObject = getAsJsonArray("value")
        .map { it.asJsonObject }
        .single { it.get("name").asString == name }

    private fun request(method: String, path: String, body: String? = null): Pair<Int, String> {
        val connection = URL("http://127.0.0.1:${NextGenClickGuiServer.port}$path")
            .openConnection() as HttpURLConnection
        connection.connectTimeout = 2_000
        connection.readTimeout = 2_000
        connection.requestMethod = method
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        }

        val status = connection.responseCode
        val stream = if (status >= 400) connection.errorStream else connection.inputStream
        val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        connection.disconnect()
        return status to response
    }

    private object JsonArrayParser {
        private val parser = com.google.gson.JsonParser()
        fun parse(value: String): JsonArray = parser.parse(value).asJsonArray
    }
}
