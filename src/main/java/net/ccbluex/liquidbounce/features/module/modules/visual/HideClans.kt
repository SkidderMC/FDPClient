/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import java.util.Locale

object HideClans : Module("HideClans", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val showAllies by boolean("ShowAllies", false)
    private val showAlliesMode by choices("ShowAlliesMode", arrayOf("Nearest", "SemiAuto", "Manual"), "Nearest") { showAllies }
    private val showAlliesCount by int("ShowAlliesCount", 5, 1..14) { showAllies && showAlliesMode != "Manual" }
    private val manualShow by text("ManualShow", "") { showAllies && showAlliesMode == "Manual" }

    private val hiddenAllies = linkedSetOf<String>()
    private val semiAutoVisibleAllies = linkedSetOf<String>()
    private var lastShowAllies = false
    private var lastShowAlliesMode = ""

    override val tag: String
        get() = if (!showAllies) {
            "Blatant"
        } else {
            when (showAlliesMode) {
                "Nearest" -> "Automatic"
                "SemiAuto" -> "Semi Automatic"
                else -> "Manual"
            }
        }

    private val onUpdate = handler<UpdateEvent> {
        updateHiddenAllies()
    }

    override fun onDisable() {
        hiddenAllies.clear()
        semiAutoVisibleAllies.clear()
    }

    @JvmStatic
    fun isClanEntity(entity: Entity?): Boolean {
        return entity is EntityPlayer && entity != mc.thePlayer && isAlly(entity)
    }

    @JvmStatic
    fun shouldHideEntity(entity: Entity?): Boolean {
        return handleEvents() && entity is EntityPlayer && hiddenAllies.contains(normalizeName(entity.name))
    }

    @JvmStatic
    fun isAlly(entity: EntityPlayer): Boolean {
        val localPlayer = mc.thePlayer ?: return false
        if (entity == localPlayer) {
            return false
        }

        if (entity.isClientFriend()) {
            return true
        }

        val localTeam = localPlayer.team
        val entityTeam = entity.team

        if (localTeam != null && entityTeam != null) {
            val localRegistered = localTeam.registeredName.orEmpty()
            val entityRegistered = entityTeam.registeredName.orEmpty()

            if (localRegistered.isNotEmpty() && localRegistered == entityRegistered) {
                return true
            }

            if (localTeam.isSameTeam(entityTeam)) {
                return true
            }
        }

        val localClanTag = clanTag(localPlayer)
        val entityClanTag = clanTag(entity)
        if (localClanTag.isNotEmpty() && localClanTag == entityClanTag) {
            return true
        }

        return isAllyColor(localPlayer.displayName?.formattedText) && isAllyColor(entity.displayName?.formattedText)
    }

    @JvmStatic
    fun clanKey(entity: EntityPlayer): String {
        val team = entity.team
        val registered = normalizeTag(team?.registeredName)
        if (registered.isNotEmpty()) {
            return "team:$registered"
        }

        val tag = clanTag(entity)
        return if (tag.isNotEmpty()) "tag:$tag" else ""
    }

    private fun updateHiddenAllies() {
        val localPlayer = mc.thePlayer ?: return clearState()
        val world = mc.theWorld ?: return clearState()

        if (!showAllies || !lastShowAllies || lastShowAlliesMode != showAlliesMode) {
            semiAutoVisibleAllies.clear()
        }

        lastShowAllies = showAllies
        lastShowAlliesMode = showAlliesMode

        val allies = world.playerEntities
            .asSequence()
            .filterIsInstance<EntityPlayer>()
            .filter { it != localPlayer && isAlly(it) }
            .map { AllyInfo(it, normalizeName(it.name), localPlayer.getDistanceToEntity(it)) }
            .toList()

        val visibleAllies = when {
            !showAllies -> emptySet()
            showAlliesMode == "Nearest" -> allies
                .sortedBy { it.distance }
                .take(showAlliesCount)
                .mapTo(linkedSetOf()) { it.normalizedName }
            showAlliesMode == "SemiAuto" -> {
                if (semiAutoVisibleAllies.isEmpty()) {
                    allies
                        .sortedBy { it.distance }
                        .take(showAlliesCount)
                        .mapTo(semiAutoVisibleAllies) { it.normalizedName }
                }
                semiAutoVisibleAllies
            }
            else -> parseManualShowNames()
        }

        hiddenAllies.clear()
        allies
            .map { it.normalizedName }
            .filterTo(hiddenAllies) { it !in visibleAllies }
    }

    private fun clearState() {
        hiddenAllies.clear()
        semiAutoVisibleAllies.clear()
    }

    private fun parseManualShowNames(): Set<String> {
        return manualShow
            .split(',', ';', ' ')
            .asSequence()
            .map(::normalizeName)
            .filter { it.isNotEmpty() }
            .toSet()
    }

    private fun clanTag(entity: EntityPlayer): String {
        return normalizeTag(entity.displayName?.formattedText?.replace(entity.name, ""))
    }

    private fun isAllyColor(formattedName: String?): Boolean {
        val text = formattedName ?: return false
        val hasGreen = text.contains("\u00A7a", true) || text.contains("\u00A72", true)
        val hasRed = text.contains("\u00A7c", true) || text.contains("\u00A74", true)
        return hasGreen && !hasRed
    }

    private fun normalizeTag(tag: String?): String {
        return stripColor(tag.orEmpty())
            .lowercase(Locale.ROOT)
            .filter { it.isLetterOrDigit() }
    }

    private fun normalizeName(name: String?): String {
        return stripColor(name.orEmpty()).trim().lowercase(Locale.ROOT)
    }

    private data class AllyInfo(
        val player: EntityPlayer,
        val normalizedName: String,
        val distance: Float
    )
}
