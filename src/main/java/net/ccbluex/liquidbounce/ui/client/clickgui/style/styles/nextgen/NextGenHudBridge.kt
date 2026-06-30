/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/** Adapts the live 1.8.9 game state to the browser HUD protocol. */
object NextGenHudBridge : MinecraftInstance {

    private val parser = JsonParser()

    fun playerData(): JsonObject = mc.thePlayer?.let(::playerData) ?: emptyPlayerData()

    fun targetData(target: EntityLivingBase): JsonObject =
        if (target is EntityPlayer) playerData(target) else livingData(target)

    fun playerInventory(): JsonObject = JsonObject().apply {
        val player = mc.thePlayer
        add("armor", stacks(player?.inventory?.armorInventory?.asIterable() ?: emptyList()))
        add("main", stacks(player?.inventory?.mainInventory?.asIterable() ?: emptyList()))
        add("crafting", JsonArray().apply {
            if (player != null) {
                for (slot in 1..4) add(itemStack(player.inventoryContainer.getSlot(slot)?.stack))
            }
        })
        add("enderChest", JsonArray().apply {
            if (player != null) {
                for (slot in 0 until player.inventoryEnderChest.sizeInventory) {
                    add(itemStack(player.inventoryEnderChest.getStackInSlot(slot)))
                }
            }
        })
    }

    fun crosshair(): JsonObject {
        val hit = mc.objectMouseOver
        val type = when (hit?.typeOfHit) {
            MovingObjectPosition.MovingObjectType.BLOCK -> "block"
            MovingObjectPosition.MovingObjectType.ENTITY -> "entity"
            else -> "miss"
        }

        return JsonObject().apply {
            addProperty("type", type)
            add("pos", vector(hit?.hitVec?.xCoord ?: 0.0, hit?.hitVec?.yCoord ?: 0.0, hit?.hitVec?.zCoord ?: 0.0))
            hit?.blockPos?.let { blockPos ->
                add("blockPos", vector(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble()))
                addProperty("side", hit.sideHit?.name?.lowercase() ?: "up")
                addProperty("isInsideBlock", false)
            }
            hit?.entityHit?.let { addProperty("entityId", it.entityId) }
        }
    }

    fun keybinds(): JsonArray = JsonArray().apply {
        mc.gameSettings.keyBindings.forEach { keyBind ->
            add(JsonObject().apply {
                addProperty("bindName", keyBind.keyDescription)
                add("key", printableKey(keyBind.keyCode))
            })
        }
    }

    fun session(): JsonObject = JsonObject().apply {
        val session = mc.session
        addProperty("username", session?.username ?: "Player")
        addProperty("type", session?.sessionType?.name?.lowercase() ?: "legacy")
        addProperty("service", "mojang")
        addProperty("avatar", "")
        addProperty("online", mc.theWorld != null)
        addProperty("uuid", session?.playerID ?: "")
    }

    fun components(@Suppress("UNUSED_PARAMETER") themeId: String?): JsonArray = JsonArray().apply {
        componentNames().forEachIndexed { index, componentName ->
            val definition = readJson("components/${componentName.lowercase()}.json") ?: return@forEachIndexed
            add(JsonObject().apply {
                addProperty("name", definition.get("name")?.asString ?: componentName)
                addProperty("id", "${componentName.lowercase()}-$index")
                add("settings", componentSettings(definition))
            })
        }
    }

    fun blockCounter(): JsonObject {
        val hotbar = mc.thePlayer?.inventory?.mainInventory?.take(9).orEmpty()
        val blocks = hotbar.filter { it?.item is ItemBlock && it.stackSize > 0 }
        return JsonObject().apply {
            if (blocks.isEmpty()) return@apply
            addProperty("nextBlock", identifier(blocks.first()))
            addProperty("count", blocks.sumOf { it.stackSize })
        }
    }

    fun itemTexture(identifier: String): ByteArray {
        val location = runCatching { ResourceLocation(identifier) }.getOrNull()
            ?: return transparentPng()
        val candidates = arrayOf(
            ResourceLocation(location.resourceDomain, "textures/items/${location.resourcePath}.png"),
            ResourceLocation(location.resourceDomain, "textures/blocks/${location.resourcePath}.png"),
        )
        for (candidate in candidates) {
            val image = runCatching {
                mc.resourceManager.getResource(candidate).inputStream.use(ImageIO::read)
            }.getOrNull() ?: continue
            return image.toPng()
        }
        return transparentPng()
    }

    fun effectTexture(effectId: String): ByteArray {
        val id = effectId.substringAfterLast(':').toIntOrNull() ?: return transparentPng()
        val potion = Potion.potionTypes.getOrNull(id) ?: return transparentPng()
        if (!potion.hasStatusIcon()) return transparentPng()

        val atlas = runCatching {
            mc.resourceManager.getResource(ResourceLocation("textures/gui/container/inventory.png"))
                .inputStream.use(ImageIO::read)
        }.getOrNull() ?: return transparentPng()
        val icon = potion.statusIconIndex
        val x = icon % 8 * 18
        val y = 198 + icon / 8 * 18
        if (x + 18 > atlas.width || y + 18 > atlas.height) return transparentPng()
        return atlas.getSubimage(x, y, 18, 18).toPng()
    }

    private fun playerData(player: EntityPlayer): JsonObject = livingData(player).apply {
        addProperty("selectedSlot", player.inventory.currentItem)
        addProperty("gameMode", if (player === mc.thePlayer) {
            mc.playerController?.currentGameType?.name?.lowercase() ?: "survival"
        } else "survival")
        addProperty("food", player.foodStats.foodLevel.coerceIn(0, 20))
        addProperty("experienceLevel", player.experienceLevel)
        addProperty("experienceProgress", player.experience.coerceIn(0f, 1f))
        add("mainHandStack", itemStack(player.heldItem))
        add("offHandStack", itemStack(null))
        add("armorItems", stacks(player.inventory.armorInventory.asIterable()))
        add("scoreboard", if (player === mc.thePlayer) scoreboard() else JsonNull.INSTANCE)
    }

    private fun livingData(entity: EntityLivingBase): JsonObject = JsonObject().apply {
        addProperty("username", entity.name ?: "Entity")
        addProperty("uuid", entity.uniqueID?.toString() ?: "")
        add("position", vector(entity.posX, entity.posY, entity.posZ))
        add("blockPosition", vector(entity.position.x.toDouble(), entity.position.y.toDouble(), entity.position.z.toDouble()))
        add("velocity", vector(entity.motionX, entity.motionY, entity.motionZ))
        addProperty("selectedSlot", 0)
        addProperty("gameMode", "survival")
        addProperty("health", entity.health.finite())
        addProperty("actualHealth", entity.health.finite())
        addProperty("maxHealth", entity.maxHealth.finite())
        addProperty("absorption", entity.absorptionAmount.finite())
        addProperty("yaw", entity.rotationYaw.finite())
        addProperty("pitch", entity.rotationPitch.finite())
        addProperty("armor", entity.totalArmorValue.coerceIn(0, 20))
        addProperty("food", 20)
        addProperty("air", entity.air.coerceAtLeast(0))
        addProperty("maxAir", 300)
        addProperty("experienceLevel", 0)
        addProperty("experienceProgress", 0f)
        add("effects", effects(entity.activePotionEffects.filterIsInstance<PotionEffect>()))
        add("mainHandStack", itemStack(entity.heldItem))
        add("offHandStack", itemStack(null))
        add("armorItems", JsonArray().apply { repeat(4) { add(itemStack(null)) } })
        add("scoreboard", JsonNull.INSTANCE)
    }

    private fun emptyPlayerData(): JsonObject = livingDataFallback().apply {
        add("mainHandStack", itemStack(null))
        add("offHandStack", itemStack(null))
        add("armorItems", JsonArray().apply { repeat(4) { add(itemStack(null)) } })
        add("scoreboard", JsonNull.INSTANCE)
    }

    private fun livingDataFallback(): JsonObject = JsonObject().apply {
        addProperty("username", mc.session?.username ?: "Player")
        addProperty("uuid", mc.session?.playerID ?: "")
        add("position", vector(0.0, 0.0, 0.0))
        add("blockPosition", vector(0.0, 0.0, 0.0))
        add("velocity", vector(0.0, 0.0, 0.0))
        addProperty("selectedSlot", 0)
        addProperty("gameMode", "survival")
        addProperty("health", 0f)
        addProperty("actualHealth", 0f)
        addProperty("maxHealth", 20f)
        addProperty("absorption", 0f)
        addProperty("yaw", 0f)
        addProperty("pitch", 0f)
        addProperty("armor", 0)
        addProperty("food", 20)
        addProperty("air", 300)
        addProperty("maxAir", 300)
        addProperty("experienceLevel", 0)
        addProperty("experienceProgress", 0f)
        add("effects", JsonArray())
    }

    private fun itemStack(stack: ItemStack?): JsonObject = JsonObject().apply {
        if (stack == null || stack.item == null) {
            addProperty("identifier", "minecraft:air")
            addProperty("count", 0)
            addProperty("damage", 0)
            addProperty("maxDamage", 0)
            addProperty("displayName", "Air")
            return@apply
        }

        addProperty("identifier", identifier(stack))
        addProperty("count", stack.stackSize)
        addProperty("damage", stack.itemDamage.coerceAtLeast(0))
        addProperty("maxDamage", stack.maxDamage.coerceAtLeast(0))
        addProperty("displayName", stack.displayName ?: identifier(stack))
        val enchantments = EnchantmentHelper.getEnchantments(stack)
        if (enchantments.isNotEmpty()) {
            add("enchantments", JsonObject().apply {
                enchantments.forEach { (id, level) ->
                    val enchantment = Enchantment.getEnchantmentById(id)
                    addProperty(enchantment?.name ?: id.toString(), level)
                }
            })
        }
    }

    private fun identifier(stack: ItemStack): String =
        Item.itemRegistry.getNameForObject(stack.item)?.toString() ?: "minecraft:air"

    private fun stacks(source: Iterable<ItemStack?>): JsonArray = JsonArray().apply {
        source.forEach { add(itemStack(it)) }
    }

    private fun effects(source: Collection<PotionEffect>): JsonArray = JsonArray().apply {
        source.sortedBy { it.potionID }.forEach { effect ->
            val potion = Potion.potionTypes.getOrNull(effect.potionID) ?: return@forEach
            add(JsonObject().apply {
                addProperty("effect", "minecraft:${effect.potionID}")
                addProperty("localizedName", runCatching { I18n.format(potion.name) }.getOrDefault(potion.name))
                addProperty("duration", if (effect.isPotionDurationMax) -1 else effect.duration)
                addProperty("amplifier", effect.amplifier)
                addProperty("ambient", effect.getIsAmbient())
                addProperty("infinite", effect.isPotionDurationMax)
                addProperty("visible", effect.getIsShowParticles())
                addProperty("showIcon", potion.hasStatusIcon())
                addProperty("color", potion.liquidColor)
            })
        }
    }

    private fun scoreboard(): JsonElement {
        val world = mc.theWorld ?: return JsonNull.INSTANCE
        val objective = world.scoreboard.getObjectiveInDisplaySlot(1) ?: return JsonNull.INSTANCE
        val scores = world.scoreboard.getSortedScores(objective)
            .filter { it.playerName != null && !it.playerName.startsWith("#") }
            .takeLast(15)
        return JsonObject().apply {
            addProperty("header", objective.displayName ?: "")
            add("entries", JsonArray().apply {
                scores.forEach { score ->
                    val team = world.scoreboard.getPlayersTeam(score.playerName)
                    add(JsonObject().apply {
                        addProperty("name", ScorePlayerTeam.formatPlayerName(team, score.playerName))
                        addProperty("score", score.scorePoints.toString())
                    })
                }
            })
        }
    }

    private fun printableKey(keyCode: Int): JsonObject {
        val translation = NextGenClickGuiBridge.minecraftKey(keyCode)
        val localized = when {
            keyCode < 0 -> "Mouse ${keyCode + 101}"
            keyCode == Keyboard.KEY_NONE -> "None"
            else -> Keyboard.getKeyName(keyCode)?.replace('_', ' ') ?: "None"
        }
        return JsonObject().apply {
            addProperty("translationKey", translation)
            addProperty("localized", localized)
        }
    }

    private fun componentNames(): List<String> {
        val metadata = readJson("metadata.json") ?: return emptyList()
        return metadata.getAsJsonArray("components")?.mapNotNull {
            runCatching { it.asString }.getOrNull()
        }.orEmpty()
    }

    private fun componentSettings(definition: JsonObject): JsonObject = JsonObject().apply {
        addProperty("enabled", definition.get("enabled")?.asBoolean ?: false)
        add("alignment", definition.get("alignment")?.let(::copyJson) ?: JsonObject())
        definition.getAsJsonArray("values")?.forEach { element ->
            val setting = element.asJsonObject
            val name = setting.get("name")?.asString ?: return@forEach
            add(protocolName(name), settingValue(setting))
        }
    }

    private fun settingValue(setting: JsonObject): JsonElement {
        val direct = setting.get("value")?.let(::copyJson)
        if (direct != null) return direct
        val nested = setting.getAsJsonArray("values") ?: return JsonNull.INSTANCE
        return JsonObject().apply {
            nested.forEach { element ->
                val child = element.asJsonObject
                val childName = child.get("name")?.asString ?: return@forEach
                add(protocolName(childName), settingValue(child))
            }
        }
    }

    private fun protocolName(name: String): String =
        name.replace("-", "").replace(" ", "").replaceFirstChar { it.lowercase() }

    private fun readJson(path: String): JsonObject? = runCatching {
        NextGenHudBridge::class.java.getResourceAsStream(
            "/assets/minecraft/fdpclient/nextgen-clickgui/$path"
        )?.bufferedReader(Charsets.UTF_8)?.use { parser.parse(it).asJsonObject }
    }.getOrNull()

    private fun copyJson(element: JsonElement): JsonElement = parser.parse(element.toString())

    private fun vector(x: Double, y: Double, z: Double): JsonObject = JsonObject().apply {
        addProperty("x", x.finite())
        addProperty("y", y.finite())
        addProperty("z", z.finite())
    }

    private fun Float.finite(): Float = if (isFinite()) this else 0f
    private fun Double.finite(): Double = if (isFinite()) this else 0.0

    private fun BufferedImage.toPng(): ByteArray = ByteArrayOutputStream().use { output ->
        ImageIO.write(this, "png", output)
        output.toByteArray()
    }

    private fun transparentPng(): ByteArray = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB).toPng()
}
