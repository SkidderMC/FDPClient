/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.visual.HideClans
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import java.util.Locale
import java.util.concurrent.ThreadLocalRandom

object TargetModule : Module("Target", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL, gameDetecting = false) {
    private val targetNameValue = text("Name", "")
    private val allEntities by boolean("AllEntities", true)
    private val automatic by boolean("Automatic", false)
    private val mode by choices(
        "Mode",
        arrayOf("LowArmor", "BreakArmor", "Health", "Both", "BrowseAllPlayers", "SwitchVisibleHit", "SwitchVisibleTime"),
        "LowArmor"
    ) { automatic }
    private val browseMode by choices("BrowseMode", arrayOf("Hits", "Time"), "Hits") {
        automatic && mode == "BrowseAllPlayers"
    }
    private val browseHits by int("BrowseHits", 5, 1..20) {
        automatic && (mode == "BrowseAllPlayers" && browseMode == "Hits" || mode == "SwitchVisibleHit")
    }
    private val browseTimeMs by int("BrowseTimeMs", 3000, 1..10000) {
        automatic && (mode == "BrowseAllPlayers" && browseMode == "Time" || mode == "SwitchVisibleTime")
    }
    private val bothHealthWeight by float("BothHealthWeight", 1F, 0.1F..5F) { automatic && mode == "Both" }
    private val bothArmorWeight by float("BothArmorWeight", 1F, 0.1F..5F) { automatic && mode == "Both" }
    private val considerDurability by boolean("ConsiderDurability", true) {
        automatic && (mode == "BreakArmor" || mode == "Both")
    }
    private val breakArmorPriority by float("BreakArmorPriority", 5F, 0F..10F) {
        automatic && (mode == "BreakArmor" || mode == "Both")
    }

    var playerValue by boolean("Player", true)
    var animalValue by boolean("Animal", true)
    var mobValue by boolean("Mob", true)
    var invisibleValue by boolean("Invisible", false)
    var deadValue by boolean("Dead", false)

    private var lockedTargetName = ""
    private var browseClanKey = ""
    private val browsedPlayers = linkedSetOf<String>()
    private val processedBrowsedPlayers = linkedSetOf<String>()
    private var browseHitCount = 0
    private var lastBrowseSwitchTime = 0L
    private var previousAttackPressed = false
    private var observedHitThisTick: EntityPlayer? = null
    private var breakArmorTargetName = ""
    private var breakArmorArmorMask = 0

    override val tag: String?
        get() {
            if (automatic) {
                return lockedTargetName.takeIf { it.isNotEmpty() }?.let { "[A] $it" } ?: "[A]"
            }

            return targetNameValue.get().trim().takeIf { it.isNotEmpty() }?.let { "| $it" }
        }

    private val onUpdate = handler<UpdateEvent> {
        updateTarget()
    }

    override fun onDisable() {
        clearRuntimeState()
    }

    @JvmStatic
    fun isManagingVisibility(): Boolean {
        return handleEvents() && currentTargetName().isNotEmpty()
    }

    @JvmStatic
    fun shouldForceRender(entity: Entity?): Boolean {
        return isManagingVisibility() && isSelectedTarget(entity)
    }

    @JvmStatic
    fun shouldHideEntity(entity: Entity?): Boolean {
        if (!isManagingVisibility() || entity == null || entity == mc.thePlayer) {
            return false
        }

        if (isSelectedTarget(entity)) {
            return false
        }

        return allEntities || entity is EntityPlayer
    }

    @JvmStatic
    fun isSelectedTarget(entity: Entity?): Boolean {
        if (entity !is EntityPlayer) {
            return false
        }

        val targetName = normalizeName(currentTargetName())
        if (targetName.isEmpty()) {
            return false
        }

        return normalizeName(entity.name) == targetName ||
            normalizeName(entity.displayName?.unformattedText) == targetName ||
            normalizeName(entity.displayName?.formattedText) == targetName
    }

    private fun updateTarget() {
        observedHitThisTick = pollObservedHitPlayer()

        if (automatic) {
            updateAutomaticTarget()
        } else {
            lockedTargetName = targetNameValue.get().trim()
            browseHitCount = 0
            lastBrowseSwitchTime = 0L
            breakArmorTargetName = ""
            breakArmorArmorMask = 0
        }
    }

    private fun updateAutomaticTarget() {
        if (mode == "BrowseAllPlayers") {
            updateBrowseCacheFromCurrentHit()
            selectBrowseTarget()
            return
        }

        if (mode == "SwitchVisibleHit" || mode == "SwitchVisibleTime") {
            updateSwitchVisibleTarget()
            return
        }

        selectBestTarget()
    }

    private fun selectBestTarget() {
        val bestTarget = selectablePlayers()
            .minByOrNull { priorityScore(it) }

        if (bestTarget == null) {
            lockedTargetName = ""
            breakArmorTargetName = ""
            breakArmorArmorMask = 0
            return
        }

        if (mode == "BreakArmor" && normalizeName(bestTarget.name) == normalizeName(lockedTargetName)) {
            val currentMask = armorMask(bestTarget)
            if (breakArmorArmorMask != 0 && currentMask != 0 && currentMask and breakArmorArmorMask == breakArmorArmorMask) {
                return
            }
        }

        lockedTargetName = bestTarget.name
        if (mode == "BreakArmor") {
            breakArmorTargetName = bestTarget.name
            breakArmorArmorMask = armorMask(bestTarget)
        } else {
            breakArmorTargetName = ""
            breakArmorArmorMask = 0
        }
    }

    private fun updateBrowseCacheFromCurrentHit() {
        val hitPlayer = observedHitPlayer() ?: return
        if (HideClans.isAlly(hitPlayer)) {
            return
        }

        val clanKey = HideClans.clanKey(hitPlayer)
        if (clanKey.isEmpty()) {
            return
        }

        if (browseClanKey != clanKey) {
            browseClanKey = clanKey
            browsedPlayers.clear()
            processedBrowsedPlayers.clear()
        }

        val players = selectablePlayers()
            .filter { HideClans.clanKey(it) == clanKey }
            .mapTo(linkedSetOf()) { normalizeName(it.name) }

        browsedPlayers.clear()
        browsedPlayers += players
    }

    private fun selectBrowseTarget() {
        if (browsedPlayers.isEmpty()) {
            lockedTargetName = ""
            return
        }

        val currentTarget = findPlayerByName(lockedTargetName)
        val attackHit = observedTargetHit(currentTarget)
        if (attackHit) {
            browseHitCount++
        }

        val now = System.currentTimeMillis()
        if (lastBrowseSwitchTime == 0L) {
            lastBrowseSwitchTime = now
        }

        val shouldSwitch = lockedTargetName.isEmpty() || when (browseMode) {
            "Hits" -> browseHitCount >= browseHits
            else -> now - lastBrowseSwitchTime >= browseTimeMs
        }

        if (!shouldSwitch && currentTarget != null && normalizeName(currentTarget.name) in browsedPlayers) {
            return
        }

        normalizeName(lockedTargetName).takeIf { it.isNotEmpty() }?.let {
            processedBrowsedPlayers += it
        }

        if (processedBrowsedPlayers.containsAll(browsedPlayers)) {
            processedBrowsedPlayers.clear()
        }

        val nextTarget = selectablePlayers()
            .filter { normalizeName(it.name) in browsedPlayers && normalizeName(it.name) !in processedBrowsedPlayers }
            .minByOrNull { mc.thePlayer.getDistanceToEntity(it) }

        lockedTargetName = nextTarget?.name.orEmpty()
        browseHitCount = 0
        lastBrowseSwitchTime = now
    }

    private fun updateSwitchVisibleTarget() {
        val now = System.currentTimeMillis()
        val currentTarget = findPlayerByName(lockedTargetName)

        if (lockedTargetName.isEmpty() || currentTarget == null) {
            lockedTargetName = randomVisibleTargetName(excludingCurrent = false)
            lastBrowseSwitchTime = now
            browseHitCount = 0
            return
        }

        if (mode == "SwitchVisibleHit" && observedTargetHit(currentTarget)) {
            browseHitCount++
        }

        if (lastBrowseSwitchTime == 0L) {
            lastBrowseSwitchTime = now
        }

        val shouldSwitch = when (mode) {
            "SwitchVisibleHit" -> browseHitCount >= browseHits
            else -> now - lastBrowseSwitchTime >= browseTimeMs
        }

        if (!shouldSwitch) {
            return
        }

        lockedTargetName = randomVisibleTargetName(excludingCurrent = true)
        browseHitCount = 0
        lastBrowseSwitchTime = now
    }

    private fun randomVisibleTargetName(excludingCurrent: Boolean): String {
        val current = normalizeName(lockedTargetName)
        val candidates = selectablePlayers()
            .filter { isRenderableCandidate(it) }
            .filter { !excludingCurrent || normalizeName(it.name) != current }

        if (candidates.isEmpty()) {
            return if (excludingCurrent) randomVisibleTargetName(excludingCurrent = false) else ""
        }

        return candidates[ThreadLocalRandom.current().nextInt(candidates.size)].name
    }

    private fun selectablePlayers(): List<EntityPlayer> {
        val localPlayer = mc.thePlayer ?: return emptyList()
        val world = mc.theWorld ?: return emptyList()

        return world.playerEntities
            .asSequence()
            .filterIsInstance<EntityPlayer>()
            .filter { it != localPlayer }
            .filter { it.health > 0F && !it.isDead }
            .filter { !it.isInvisible }
            .filter { !it.isSpectator }
            .filter { !isBot(it) }
            .filter { !HideClans.isAlly(it) }
            .toList()
    }

    private fun priorityScore(player: EntityPlayer): Float {
        val distanceScore = (mc.thePlayer?.getDistanceToEntity(player) ?: 0F) * 0.001F
        return when (mode) {
            "BreakArmor" -> breakArmorScore(player) + distanceScore
            "Health" -> player.health + distanceScore
            "Both" -> player.health * bothHealthWeight + breakArmorScore(player) * bothArmorWeight + distanceScore
            else -> lowArmorScore(player) + distanceScore
        }
    }

    private fun breakArmorScore(player: EntityPlayer): Float {
        val equippedPieces = (0..3).count { player.getCurrentArmor(it) != null }
        if (equippedPieces <= 0) {
            return Float.MAX_VALUE
        }

        val remainingDurability = if (considerDurability) lowArmorScore(player) else 0F
        val pieceWeight = 10000F + breakArmorPriority * 1000F
        return -(equippedPieces * pieceWeight + remainingDurability)
    }

    private fun lowArmorScore(player: EntityPlayer): Float {
        return (0..3).sumOf { slot ->
            player.getCurrentArmor(slot).remainingDurability().toDouble()
        }.toFloat()
    }

    private fun ItemStack?.remainingDurability(): Int {
        val stack = this ?: return 0
        val maxDamage = stack.maxDamage
        if (maxDamage <= 0) {
            return 0
        }

        return (maxDamage - stack.itemDamage.coerceIn(0, maxDamage)).coerceAtLeast(0)
    }

    private fun armorMask(player: EntityPlayer): Int {
        var mask = 0
        for (slot in 0..3) {
            if (player.getCurrentArmor(slot) != null) {
                mask = mask or (1 shl slot)
            }
        }
        return mask
    }

    private fun observedHitPlayer(): EntityPlayer? {
        return observedHitThisTick
    }

    private fun pollObservedHitPlayer(): EntityPlayer? {
        val attackPressed = mc.gameSettings.keyBindAttack.isKeyDown
        val hitPlayer = if (attackPressed && !previousAttackPressed) {
            mc.objectMouseOver?.entityHit as? EntityPlayer
        } else {
            null
        }

        previousAttackPressed = attackPressed
        return hitPlayer
    }

    private fun observedTargetHit(target: EntityPlayer?): Boolean {
        val hitPlayer = observedHitPlayer() ?: return false
        return target != null && normalizeName(hitPlayer.name) == normalizeName(target.name)
    }

    private fun isRenderableCandidate(player: EntityPlayer): Boolean {
        val localPlayer = mc.thePlayer ?: return false
        return localPlayer.getDistanceToEntity(player) <= 10F && player.health > 0F && !player.isInvisible
    }

    private fun findPlayerByName(name: String): EntityPlayer? {
        val normalized = normalizeName(name)
        if (normalized.isEmpty()) {
            return null
        }

        return mc.theWorld?.playerEntities
            ?.filterIsInstance<EntityPlayer>()
            ?.firstOrNull { normalizeName(it.name) == normalized }
    }

    private fun currentTargetName(): String {
        return if (automatic) lockedTargetName else targetNameValue.get().trim()
    }

    private fun clearRuntimeState() {
        lockedTargetName = ""
        browseClanKey = ""
        browsedPlayers.clear()
        processedBrowsedPlayers.clear()
        browseHitCount = 0
        lastBrowseSwitchTime = 0L
        previousAttackPressed = false
        observedHitThisTick = null
        breakArmorTargetName = ""
        breakArmorArmorMask = 0
    }

    private fun normalizeName(name: String?): String {
        return stripColor(name.orEmpty()).trim().lowercase(Locale.ROOT)
    }
}
