/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemHoe
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemSpade
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import java.awt.Color
import java.util.UUID

object MurderMystery : Module("MurderMystery", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Classic", "Infection"), "Classic")
        .describe("Game mode logic used to detect the murderer.")

    private val detectDetective by boolean("Detective", true)
        .describe("Detect and highlight the bow-holding detective.")
    private val esp by boolean("ESP", true)
        .describe("Highlight detected murderers and detectives.")
    private val box by boolean("Box", true) { esp }
        .describe("Draw a solid box for highlighted players.")
    private val chatAlert by boolean("Chat", true)
        .describe("Print a chat alert on role detection.")
    private val sound by boolean("Sound", true)
        .describe("Play a sound on role detection.")

    private val murdererColor by color("MurdererColor", Color(203, 9, 9))
        .describe("Highlight color used for the murderer.")
    private val detectiveColor by color("DetectiveColor", Color(0, 144, 255))
        .describe("Highlight color used for the detective.")

    private val murdererPlayers = HashSet<UUID>()
    private val bowPlayers = HashSet<UUID>()

    private val knownSwordItems = setOf(
        Items.golden_carrot, Items.carrot, Items.carrot_on_a_stick, Items.bone, Items.blaze_rod,
        Items.pumpkin_pie, Items.name_tag, Items.apple, Items.feather, Items.cookie, Items.shears,
        Items.stick, Items.quartz, Items.cooked_beef, Items.netherbrick, Items.cooked_chicken,
        Items.reeds, Items.flint, Items.bread, Items.dye, Items.leather, Items.book, Items.boat,
        Items.speckled_melon, Items.prismarine_shard
    )

    private val knownNonSwordItems = setOf(Items.wooden_shovel, Items.golden_shovel)

    override fun onDisable() {
        reset()
    }

    private fun reset() {
        murdererPlayers.clear()
        bowPlayers.clear()
    }

    val onWorld = handler<WorldEvent> {
        reset()
    }

    val onUpdate = handler<UpdateEvent> {
        val world = mc.theWorld ?: return@handler
        val self = mc.thePlayer ?: return@handler

        for (entity in world.playerEntities.filterNotNull()) {
            if (entity === self) {
                continue
            }

            val stack = entity.getEquipmentInSlot(0) ?: continue

            when {
                isBow(stack) -> handleBow(entity)
                isSword(stack) -> handleSword(entity)
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (!esp) {
            return@handler
        }

        val world = mc.theWorld ?: return@handler

        for (entity in world.playerEntities.filterNotNull()) {
            val id = entity.gameProfile.id ?: continue

            val color = when {
                id in murdererPlayers -> murdererColor
                detectDetective && id in bowPlayers -> detectiveColor
                else -> continue
            }

            drawEntityBox(entity, color, box)
        }
    }

    private fun handleSword(entity: EntityPlayer) {
        val id = entity.gameProfile.id ?: return

        if (mode == "Infection" && murdererPlayers.isNotEmpty()) {
            return
        }

        if (murdererPlayers.add(id)) {
            if (chatAlert) {
                val name = entity.gameProfile.name ?: "Unknown"
                chat(if (mode == "Infection") "§c$name §3is the first infected." else "§cMurderer: §f$name")
            }

            if (sound) {
                mc.thePlayer?.playSound("mob.villager.hit", 1f, 1f)
            }
        }
    }

    private fun handleBow(entity: EntityPlayer) {
        if (!detectDetective) {
            return
        }

        val id = entity.gameProfile.id ?: return

        if (bowPlayers.add(id)) {
            if (chatAlert) {
                val name = entity.gameProfile.name ?: "Unknown"
                chat("§9$name §3has a bow.")
            }

            if (sound) {
                mc.thePlayer?.playSound("random.bow", 1f, 1f)
            }
        }
    }

    private fun isBow(stack: ItemStack): Boolean {
        return stack.item is ItemBow || stack.item == Items.arrow
    }

    private fun isSword(stack: ItemStack): Boolean {
        val item = stack.item

        return when {
            item in knownNonSwordItems -> false
            item in knownSwordItems -> true
            item is ItemSword -> true
            item is ItemPickaxe -> true
            item is ItemSpade -> true
            item is ItemAxe -> true
            item is ItemHoe -> true
            item is ItemTool -> true
            else -> false
        }
    }
}
