/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.disableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawExhiEnchants
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTexturedModalRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.enableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.quickDrawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.quickDrawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetCaps
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

object NameTags : Module("NameTags", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {

    private val typeValue = choices("Mode", arrayOf("3DTag", "2DTag"), "2DTag")
        .describe("Style of name tag rendering to use.")

    private val renderSelf by boolean("RenderSelf", false)
        .describe("Show a name tag above your own player.")
    private val health by boolean("Health", true)
        .describe("Display entity health on the name tag.")
    private val healthFromScoreboard by boolean("HealthFromScoreboard", false) { health }
        .describe("Read health from the scoreboard instead.")
    private val absorption by boolean("Absorption", false) { health || healthBar }
        .describe("Include absorption hearts in the health value.")
    private val roundedHealth by boolean("RoundedHealth", true) { health }
        .describe("Round the displayed health to whole numbers.")

    private val healthPrefix by boolean("HealthPrefix", false) { health }
        .describe("Show text before the health value.")
    private val healthPrefixText by text("HealthPrefixText", "") { health && healthPrefix }
        .describe("Text placed before the health value.")

    private val healthSuffix by boolean("HealthSuffix", true) { health }
        .describe("Show text after the health value.")
    private val healthSuffixText by text("HealthSuffixText", " ❤") { health && healthSuffix }
        .describe("Text placed after the health value.")

    private val indicator by boolean("Indicator", false)
        .describe("Show a friend or enemy indicator symbol.")
    private val ping by boolean("Ping", false)
        .describe("Display each player ping on the name tag.")
    private val healthBar by boolean("Bar", false)
        .describe("Draw a colored health bar under the tag.")
    private val distance by boolean("Distance", false)
        .describe("Display the distance to the entity.")
    private val armor by boolean("Armor", true)
        .describe("Render the entity worn armor items.")
    private val showArmorDurability by choices("Armor Durability", arrayOf("None", "Value", "Percentage"), "None") { armor }
        .describe("How to display armor durability.")
    private val enchant by boolean("Enchant", true) { armor }
        .describe("Show enchantment levels on armor items.")
    private val bot by boolean("Bots", true)
        .describe("Also render name tags for detected bots.")
    private val potion by boolean("Potions", true)
        .describe("Show active potion effect icons.")
    private val clearNames by boolean("ClearNames", false)
        .describe("Strip color codes from displayed names.")
    private val font by font("Font", Fonts.fontSemibold40)
        .describe("Font used to render the name tag text.")
    private val scale by float("Scale", 1F, 1F..4F)
        .describe("Size multiplier for the name tags.")
    private val fontShadow by boolean("Shadow", true)
        .describe("Draw a shadow behind the tag text.")

    private val background by boolean("Background", true)
        .describe("Draw a filled background behind the tag.")
    private val backgroundColor by color("BackgroundColor", Color.BLACK.withAlpha(70)) { background }
        .describe("Color of the name tag background.")

    private val border by boolean("Border", false)
        .describe("Draw an outline around the tag background.")
    private val borderColor by color("BorderColor", Color.BLACK.withAlpha(100)) { border }
        .describe("Color of the name tag border.")

    private val borderWidth by float("Border Width", 0F, 0F..8F) { background }
        .describe("Thickness of the background border.")
    private val backgroundRadius by float("Background Radius", 0F, 0F..16F) { background }
        .describe("Corner rounding of the tag background.")

    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200).onChanged { value ->
        maxRenderDistanceSq = value.toDouble().pow(2)
    }
        .describe("Maximum distance at which name tags are drawn.")

    private val onLook by boolean("OnLook", false)
        .describe("Only show tags for entities you look at.")
    private val maxAngleDifference by float("MaxAngleDifference", 90f, 5.0f..90f) { onLook }
        .describe("Max angle from your aim to show a tag.")

    private val thruBlocks by boolean("ThruBlocks", true)
        .describe("Show tags even when blocked by terrain.")

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    private val inventoryBackground = ResourceLocation("textures/gui/container/inventory.png")
    private val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))

    // Cache for health color and formatted strings
    private var cachedHealthColor: Int = 0xFFFFFF
    private var cachedHealthPrefix = ""
    private var cachedHealthSuffix = ""

    private val entities by EntityLookup<EntityLivingBase>()
        .filter { bot || !isBot(it) }
        .filter { !onLook || mc.thePlayer.isLookingOnEntity(it, maxAngleDifference.toDouble()) }
        .filter { thruBlocks || isEntityHeightVisible(it) }

    val onRender3D = handler<Render3DEvent> {
        if (mc.theWorld == null || mc.thePlayer == null) return@handler

        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()

        // Disable lighting and depth test
        glDisable(GL_LIGHTING)
        glDisable(GL_DEPTH_TEST)

        glEnable(GL_LINE_SMOOTH)

        // Enable blending
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        for (entity in entities) {
            val isRenderingSelf =
                entity is EntityPlayerSP && (mc.gameSettings.thirdPersonView != 0 || FreeCam.handleEvents())
            if (!isRenderingSelf || !renderSelf) {
                if (!isSelected(entity, false)) continue
            }

            val name = entity.displayName.unformattedText ?: continue

            val distanceSquared = mc.thePlayer.getDistanceSqToEntity(entity)

            // In case user has FreeCam enabled, we restore the position back to normal,
            // so it renders the name-tag at the player's body position instead of the FreeCam position.
            if (isRenderingSelf) {
                FreeCam.restoreOriginalPosition()
            }

            if (distanceSquared <= maxRenderDistanceSq) {
                when (typeValue.get().lowercase(Locale.getDefault())) {
                    "2dtag" -> renderNameTag2D(entity, isRenderingSelf, if (clearNames) ColorUtils.stripColor(name) else name)
                    "3dtag" -> renderNameTag3D(entity, isRenderingSelf, if (clearNames) ColorUtils.stripColor(name) else name)
                }
                if (isRenderingSelf) {
                    FreeCam.useModifiedPosition()
                }
            }
        }

        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)

        glPopMatrix()
        glPopAttrib()

        // Reset color
        glColor4f(1F, 1F, 1F, 1F)
    }

    private fun renderNameTag2D(entity: EntityLivingBase, isRenderingSelf: Boolean, name: String) {
        var tag = name
        val fontRenderer = mc.fontRendererObj
        var scale = (mc.thePlayer.getDistanceToEntity(entity) / 2.5f).coerceAtLeast(4.0f)
        scale /= 200f
        tag = entity.displayName.formattedText

        var bot = ""
        bot = if (isBot(entity)) {
            "§7[Bot] "
        } else {
            ""
        }

        val healthText = if (health) " §a" + entity.health.toInt() + "" else ""
        val distanceText = if (distance) "§a[§f" + mc.thePlayer.getDistanceToEntity(entity).toInt() + "§a] " else ""
        val HEALTH: Int = entity.health.toInt()
        val COLOR1: String = when {
            HEALTH > 20 -> "§9"
            HEALTH >= 11 -> "§a"
            HEALTH >= 4 -> "§e"
            else -> "§4"
        }

        val hp = " [$COLOR1$HEALTH §c❤§f]"
        glPushMatrix()
        glTranslatef(
            (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX).toFloat(),
            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + entity.eyeHeight + 0.6).toFloat(),
            (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ).toFloat()
        )

        glNormal3f(0.0f, 1.0f, 0.0f)
        glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        glScalef(-scale, -scale, scale)
        RenderUtils.setGLCap(GL_LIGHTING, false)
        RenderUtils.setGLCap(GL_DEPTH_TEST, false)
        RenderUtils.setGLCap(GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        val text = distanceText + bot + tag + healthText + hp
        val stringWidth = fontRenderer.getStringWidth(text) / 2
        if (background) {
            if (backgroundRadius > 0F || borderWidth > 0F) {
                val bx1 = (-stringWidth - 1).toFloat()
                val by1 = -14F
                val bx2 = (stringWidth + 1).toFloat()
                val by2 = -4F
                RenderUtils.drawRoundedRect(
                    bx1,
                    by1,
                    bx2 - bx1,
                    by2 - by1,
                    backgroundRadius,
                    Integer.MIN_VALUE,
                    borderWidth,
                    (if (border) borderColor.rgb else Integer.MIN_VALUE)
                )
            } else {
                Gui.drawRect((-stringWidth - 1), -14, (stringWidth + 1), -4, Integer.MIN_VALUE)
            }
        }
        fontRenderer.drawString(text, (-stringWidth).toFloat(), (fontRenderer.FONT_HEIGHT - 22).toFloat(), 16777215, fontShadow)
        RenderUtils.revertAllCaps()
        glColor4f(1f, 1f, 1f, 1f)
        glPopMatrix()
    }

    private fun renderNameTag3D(entity: EntityLivingBase, isRenderingSelf: Boolean, name: String) {
        val thePlayer = mc.thePlayer ?: return

        // Set local fontRenderer
        val fontRenderer = font

        // Push matrix
        glPushMatrix()

        // Translate to player position
        val renderManager = mc.renderManager
        val rotateX = if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f

        val (x, y, z) = entity.interpolatedPosition(entity.lastTickPos) - renderManager.renderPos
        glTranslated(x, y + entity.eyeHeight.toDouble() + 0.55, z)

        glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(renderManager.playerViewX * rotateX, 1F, 0F, 0F)

        // Disable lighting and depth test
        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        // Enable blending
        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Determine friend or enemy status
        val isFriend = indicator && (entity is EntityPlayer && entity.isClientFriend())

        // Cache health color and formatted strings
        cachedHealthColor = calculateHealthColor(entity)
        cachedHealthPrefix = if (healthPrefix) healthPrefixText else ""
        cachedHealthSuffix = if (isFriend) " §9❤" else healthSuffixText

        // Modify tag
        val bot = isBot(entity)
        val nameColor = if (bot) "§3" else if (entity.isInvisible) "§6" else if (entity.isSneaking) "§4" else "§7"
        val symbolColor = if (isFriend) "§a" else "§c"
        val symbol = if (indicator) "$symbolColor✦" else ""

        val playerPing = if (entity is EntityPlayer) entity.getPing() else 0
        val playerDistance = thePlayer.getDistanceToEntity(entity)

        val distanceText = if (distance && !isRenderingSelf) "§7${playerDistance.roundToInt()} m " else ""
        val pingText = if (ping && entity is EntityPlayer) "§7[" + (if (playerPing > 200) "§c" else if (playerPing > 100) "§e" else "§a") + playerPing + "ms§7] " else ""
        val healthText = if (health) " " + getHealthString(entity) else ""
        val botText = if (bot) " §c§lBot" else ""

        val text = "$symbol $distanceText$pingText$nameColor$name$healthText$botText"

        // Scale based on distance
        val scale = ((playerDistance / 4F).coerceAtLeast(1F) / 150F) * scale

        glScalef(-scale, -scale, scale)

        val width = fontRenderer.getStringWidth(text) * 0.5f
        fontRenderer.drawString(
            text, 1F + -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, 0xFFFFFF, fontShadow
        )

        val dist = width + 4F - (-width - 2F)

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)

        val bgColor = if (background) {
            // Background
            backgroundColor
        } else {
            // Transparent
            Color(0, 0, 0, 0)
        }

        val bgX1 = -width - 2F
        val bgY1 = -2F
        val bgX2 = width + 4F
        val bgY2 = fontRenderer.FONT_HEIGHT + 2F + if (healthBar) 2F else 0F

        if (backgroundRadius > 0F || borderWidth > 0F) {
            RenderUtils.drawRoundedRect(
                bgX1,
                bgY1,
                bgX2 - bgX1,
                bgY2 - bgY1,
                backgroundRadius,
                bgColor.rgb,
                borderWidth,
                (if (border) borderColor else bgColor).rgb
            )
        } else if (border) quickDrawBorderedRect(
            bgX1,
            bgY1,
            bgX2,
            bgY2,
            2F,
            borderColor.rgb,
            bgColor.rgb
        )
        else quickDrawRect(
            bgX1, bgY1, bgX2, bgY2, bgColor.rgb
        )

        if (healthBar) {
            quickDrawRect(
                -width - 2F,
                fontRenderer.FONT_HEIGHT + 3F,
                -width - 2F + dist,
                fontRenderer.FONT_HEIGHT + 4F,
                Color(50, 50, 50).rgb
            )
            quickDrawRect(
                -width - 2F,
                fontRenderer.FONT_HEIGHT + 3F,
                -width - 2F + (dist * (getHealth(entity, healthFromScoreboard) / entity.maxHealth).coerceIn(0F, 1F)),
                fontRenderer.FONT_HEIGHT + 4F,
                cachedHealthColor
            )
        }

        glEnable(GL_TEXTURE_2D)

        fontRenderer.drawString(
            text, 1F + -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, Color.white.rgb, fontShadow
        )

        var foundPotion = false

        if (potion && entity is EntityPlayer) {
            val potions =
                entity.activePotionEffects.map { Potion.potionTypes[it.potionID] }.filter { it.hasStatusIcon() }
            if (potions.isNotEmpty()) {
                foundPotion = true

                color(1.0F, 1.0F, 1.0F, 1.0F)
                disableLighting()
                enableTexture2D()

                val minX = (potions.size * -20) / 2

                glPushMatrix()
                enableRescaleNormal()
                for ((index, potion) in potions.withIndex()) {
                    color(1.0F, 1.0F, 1.0F, 1.0F)
                    mc.textureManager.bindTexture(inventoryBackground)
                    val i1 = potion.statusIconIndex
                    drawTexturedModalRect(minX + index * 20, -22, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18, 0F)
                }
                disableRescaleNormal()
                glPopMatrix()

                enableAlpha()
                disableBlend()
                enableTexture2D()
            }
        }

        if (armor && entity is EntityPlayer) {
            RenderHelper.enableGUIStandardItemLighting()
            for (index in 0..4) {
                val itemStack = entity.getEquipmentInSlot(index) ?: continue

                mc.renderItem.zLevel = -147F
                mc.renderItem.renderItemAndEffectIntoGUI(
                    itemStack, -50 + index * 20, if (potion && foundPotion) -42 else -22
                )
            }
            RenderHelper.disableStandardItemLighting()

            enableAlpha()
            disableBlend()
            enableTexture2D()
        }

        if (enchant && entity is EntityPlayer) {
            glPushMatrix()
            for (index in 0..4) {
                if (entity.getEquipmentInSlot(index) == null)
                    continue

                mc.renderItem.renderItemOverlays(mc.fontRendererObj, entity.getEquipmentInSlot(index), -50 + index * 20, if (potion && foundPotion) -42 else -22)
                drawExhiEnchants(entity.getEquipmentInSlot(index), -50f + index * 20f, if (potion && foundPotion) -42f else -22f)
            }

            // Disable lighting and depth test
            glDisable(GL_LIGHTING)
            glDisable(GL_DEPTH_TEST)

            glEnable(GL_LINE_SMOOTH)

            // Enable blending
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            glPopMatrix()
        }

        if (showArmorDurability != "None" && entity is EntityPlayer) {

            val posX = if (potion && entity.activePotionEffects.isNotEmpty()) 10 else 12
            val posY = if (potion && entity.activePotionEffects.isNotEmpty()) -50 else -35

            val armorItems = entity.inventory.armorInventory
            var armorDurabilityText = ""

            for (item in armorItems) {
                if (item != null) {
                    val durabilityValue = item.maxDamage - item.itemDamage
                    val durabilityPercentage = (durabilityValue.toFloat() / item.maxDamage.toFloat()) * 100.0f

                    armorDurabilityText += when (showArmorDurability) {
                        "Value" -> " $durabilityValue"
                        "Percentage" -> " ${String.format("%.0f%%", durabilityPercentage)}"
                        else -> ""
                    }
                }
            }

            val width = fontRenderer.getStringWidth(armorDurabilityText.trim()) * 0.5f
            fontRenderer.drawString(
                armorDurabilityText.trim(),
                posX - width,
                posY.toFloat(),
                0xFFFFFF, fontShadow
            )
        }

        glDisable(GL_DEPTH_TEST)

        // Reset OpenGL caps
        resetCaps()

        // Reset color
        resetColor()
        glColor4f(1F, 1F, 1F, 1F)

        // Pop matrix
        glPopMatrix()
    }

    private fun calculateHealthColor(entity: EntityLivingBase): Int {
        return when {
            entity.health <= 0 -> 0xFF0000
            else -> {
                val healthRatio = (getHealth(entity, healthFromScoreboard, absorption) / entity.maxHealth).coerceIn(0.0F, 1.0F)
                val red = (255 * (1 - healthRatio)).toInt()
                val green = (255 * healthRatio).toInt()
                Color(red, green, 0).rgb
            }
        }
    }

    private fun getHealthString(entity: EntityLivingBase): String {
        return if (indicator) {
            val isFriend = entity is EntityPlayer && entity.isClientFriend()

            val friendColor = "§9"
            val enemyColor = "§c"

            val prefixColor = if (isFriend) friendColor else enemyColor
            val suffixColor = if (isFriend) friendColor else enemyColor

            val prefix = if (healthPrefix) "$prefixColor$healthPrefixText" else ""
            val suffix = if (healthSuffix) "$suffixColor$healthSuffixText" else ""

            val result = getHealth(entity, healthFromScoreboard, absorption)

            val healthPercentage = (getHealth(entity, healthFromScoreboard) / entity.maxHealth).coerceIn(0.0F, 1.0F)
            val healthColor = when {
                entity.health <= 0 -> enemyColor
                healthPercentage >= 0.75 -> friendColor
                healthPercentage >= 0.5 -> if (isFriend) "§e" else enemyColor
                healthPercentage >= 0.25 -> if (isFriend) "§6" else enemyColor
                else -> enemyColor
            }

            "$healthColor$prefix${if (roundedHealth) result.roundToInt() else decimalFormat.format(result)}$suffix"
        } else {
            val prefix = if (healthPrefix) healthPrefixText else ""
            val suffix = if (healthSuffix) healthSuffixText else ""

            val result = getHealth(entity, healthFromScoreboard, absorption)

            val healthPercentage = (getHealth(entity, healthFromScoreboard) / entity.maxHealth).coerceIn(0.0F, 1.0F)
            val healthColor = when {
                entity.health <= 0 -> "§4"
                healthPercentage >= 0.75 -> "§a"
                healthPercentage >= 0.5 -> "§e"
                healthPercentage >= 0.25 -> "§6"
                else -> "§c"
            }

            "$healthColor$prefix${if (roundedHealth) result.roundToInt() else decimalFormat.format(result)}$suffix"
        }
    }

    fun shouldRenderNameTags(entity: Entity) =
        handleEvents() && entity is EntityLivingBase && (ESP.handleEvents() && ESP.renderNameTags || isSelected(
            entity,
            false
        ) && (bot || !isBot(entity)))
}
