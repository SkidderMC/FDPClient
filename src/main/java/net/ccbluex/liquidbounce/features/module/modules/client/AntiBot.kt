/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.angleDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.network.play.server.S13PacketDestroyEntities
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S20PacketEntityProperties
import net.minecraft.potion.Potion
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

object AntiBot : Module("AntiBot", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL) {

    private val tab by boolean("Tab", true)
        .describe("Flag players not present in the tab list as bots.")
    private val tabMode by choices("TabMode", arrayOf("Equals", "Contains"), "Contains") { tab }
        .describe("How tab names are matched against the player.")

    private val entityID by boolean("EntityID", true)
        .describe("Flag players with an out-of-range entity ID.")
    private val invalidUUID by boolean("InvalidUUID", true)
        .describe("Flag players whose UUID is missing from tab.")
    private val color by boolean("Color", false)
        .describe("Flag players with no color codes in their name.")
    private val nameCheck by boolean("Name", true)
        .describe("Flag blank names and the local profile when tab validation is disabled.")

    private val livingTime by boolean("LivingTime", false)
        .describe("Flag players that have existed too briefly.")
    private val livingTimeTicks by int("LivingTimeTicks", 40, 1..200) { livingTime }
        .describe("Minimum ticks a player must exist to be trusted.")

    private val capabilities by boolean("Capabilities", true)
        .describe("Flag players with invalid creative or fly flags.")
    private val ground by boolean("Ground", true)
        .describe("Flag players never seen on the ground.")
    private val air by boolean("Air", false)
        .describe("Flag players never seen in the air.")
    private val invalidGround by boolean("InvalidGround", true)
        .describe("Flag players with impossible ground states.")
    private val invalidSpeed by boolean("InvalidSpeed", false)
        .describe("Flag players moving at suspicious speeds.")
    private val swing by boolean("Swing", false)
        .describe("Flag players never seen swinging their arm.")
    private val health by boolean("Health", false)
        .describe("Flag players with out-of-range health values.")
    private val derp by boolean("Derp", true)
        .describe("Flag players with impossible pitch rotations.")
    private val wasInvisible by boolean("WasInvisible", false)
        .describe("Flag players that have been invisible.")
    private val armor by boolean("Armor", false)
        .describe("Flag players wearing no armor at all.")
    private val ping by boolean("Ping", false)
        .describe("Flag players reporting a ping of zero.")
    private val needHit by boolean("NeedHit", false)
        .describe("Only trust players that you have hit.")
    private val duplicateInWorld by boolean("DuplicateInWorld", false)
        .describe("Flag players with duplicate names in the world.")
    private val duplicateInTab by boolean("DuplicateInTab", false)
        .describe("Flag players with duplicate names in the tab list.")
    private val duplicateProfile by boolean("DuplicateProfile", false)
        .describe("Flag players sharing a name but differing UUID.")
    private val properties by boolean("Properties", false)
        .describe("Flag players that never sent entity properties.")

    private val alwaysInRadius by boolean("AlwaysInRadius", false)
        .describe("Flag players always staying within a radius.")
    private val alwaysRadius by float("AlwaysInRadiusBlocks", 20f, 3f..30f)
    { alwaysInRadius }
        .describe("Radius in blocks for the always-in-radius check.")
    private val alwaysRadiusTick by int("AlwaysInRadiusTick", 50, 1..100)
    { alwaysInRadius }
        .describe("Ticks within radius before a player is trusted.")

    private val alwaysBehind by boolean("AlwaysBehind", false)
        .describe("Flag players that are always behind you.")
    private val alwaysBehindRadius by float("AlwaysBehindInRadiusBlocks", 10f, 3f..30f)
    { alwaysBehind }
        .describe("Radius in blocks for the always-behind check.")
    private val behindRotDiffToIgnore by float("BehindRotationDiffToIgnore", 90f, 1f..180f)
    { alwaysBehind }
        .describe("Angle difference above which a player counts as behind.")

    private val tabGroup = Configurable("Tab")
    private val nameGroup = Configurable("Name")
    private val movementGroup = Configurable("Movement")
    private val combatGroup = Configurable("Combat")
    private val lifetimeGroup = Configurable("Lifetime")
    private val positionGroup = Configurable("Position")

    init {
        moveValues(tabGroup,
            "Tab", "TabMode", "EntityID", "InvalidUUID", "DuplicateInTab",
            "DuplicateProfile", "Properties")

        moveValues(nameGroup, "Color", "Name", "DuplicateInWorld")

        moveValues(movementGroup,
            "Ground", "Air", "InvalidGround", "InvalidSpeed", "Swing")

        moveValues(combatGroup,
            "Capabilities", "Health", "Derp", "WasInvisible", "Armor", "Ping", "NeedHit")

        moveValues(lifetimeGroup, "LivingTime", "LivingTimeTicks")

        moveValues(positionGroup,
            "AlwaysInRadius", "AlwaysInRadiusBlocks", "AlwaysInRadiusTick", "AlwaysBehind",
            "AlwaysBehindInRadiusBlocks", "BehindRotationDiffToIgnore")

        addValues(listOf(
            tabGroup, nameGroup, movementGroup, combatGroup, lifetimeGroup, positionGroup,
        ))
    }
    private val groundList = mutableSetOf<Int>()
    private val airList = mutableSetOf<Int>()
    private val invalidGroundList = mutableMapOf<Int, Int>()
    private val invalidSpeedList = mutableSetOf<Int>()
    private val swingList = mutableSetOf<Int>()
    private val invisibleList = mutableSetOf<Int>()  // Changed from MutableList to MutableSet for O(1) operations
    private val propertiesList = mutableSetOf<Int>()
    private val hitList = mutableSetOf<Int>()
    private val notAlwaysInRadiusList = mutableSetOf<Int>()
    private val alwaysBehindList = mutableSetOf<Int>()
    private val worldPlayerNames = mutableSetOf<String>()
    private val worldDuplicateNames = mutableSetOf<String>()
    private val tabPlayerNames = mutableSetOf<String>()
    private val tabDuplicateNames = mutableSetOf<String>()
    private val entityTickMap = mutableMapOf<Int, Int>()

    val botList = mutableSetOf<UUID>()

    private val transparentPredicates by lazy {
        listOf(
            BotPredicate("Age", { livingTime }) { it.ticksExisted < livingTimeTicks },
            BotPredicate("Name", { nameCheck }) {
                it.name.isBlank() || (!tab && it.name == mc.thePlayer?.name)
            },
            BotPredicate("Armor", { armor }) {
                it.inventory.armorInventory.all { stack -> stack == null }
            },
            BotPredicate("Radius", { alwaysInRadius }) {
                it.entityId !in notAlwaysInRadiusList
            }
        )
    }

    // Periodic cleanup counter to prevent unbounded growth
    private var cleanupTicks = 0

    fun isBot(entity: EntityLivingBase): Boolean {
        // Check if entity is a player
        if (entity !is EntityPlayer)
            return false

        // Check if anti bot is enabled
        if (!handleEvents())
            return false

        if (transparentPredicates.evaluate(entity).detected)
            return true

        // Anti Bot checks
        if (color && "§" !in entity.displayName.formattedText.replace("§r", ""))
            return true

        if (ground && entity.entityId !in groundList)
            return true

        if (air && entity.entityId !in airList)
            return true

        if (swing && entity.entityId !in swingList)
            return true

        if (health && (entity.health > 20F || entity.health < 0F))
            return true

        if (entityID && (entity.entityId >= 1000000000 || entity.entityId <= 0))
            return true

        if (derp && (entity.rotationPitch > 90F || entity.rotationPitch < -90F))
            return true

        if (wasInvisible && entity.entityId in invisibleList)
            return true

        if (properties && entity.entityId !in propertiesList)
            return true

        if (ping) {
            if (entity.getPing() == 0) return true
        }

        if (invalidUUID && mc.netHandler.getPlayerInfo(entity.uniqueID) == null) {
            return true
        }

        if (capabilities && (entity.isSpectator || entity.capabilities.isFlying || entity.capabilities.allowFlying
                    || entity.capabilities.disableDamage || entity.capabilities.isCreativeMode)
        )
            return true

        if (invalidSpeed && entity.entityId in invalidSpeedList)
            return true

        if (needHit && entity.entityId !in hitList)
            return true

        if (invalidGround && invalidGroundList.getOrDefault(entity.entityId, 0) >= 10)
            return true

        if (alwaysBehind && entity.entityId in alwaysBehindList)
            return true

        if (duplicateProfile) {
            return mc.netHandler.playerInfoMap.count {
                it.gameProfile.name == entity.gameProfile.name
                        && it.gameProfile.id != entity.gameProfile.id
            } == 1
        }

        if (duplicateInWorld) {
            for (player in mc.theWorld.playerEntities.filterNotNull()) {
                val playerName = player.name

                if (worldPlayerNames.contains(playerName)) {
                    worldDuplicateNames.add(playerName)
                } else {
                    worldPlayerNames.add(playerName)
                }
            }

            if (worldDuplicateNames.isNotEmpty()) {
                return mc.theWorld.playerEntities.count { it.name in worldDuplicateNames } > 1
            }
        }

        if (duplicateInTab) {
            for (networkPlayerInfo in mc.netHandler.playerInfoMap.filterNotNull()) {
                val playerName = stripColor(networkPlayerInfo.getFullName())

                if (tabPlayerNames.contains(playerName)) {
                    tabDuplicateNames.add(playerName)
                } else {
                    tabPlayerNames.add(playerName)
                }
            }

            if (tabDuplicateNames.isNotEmpty()) {
                return mc.netHandler.playerInfoMap.count { stripColor(it.getFullName()) in tabDuplicateNames } > 1
            }
        }

        if (tab) {
            val equals = tabMode == "Equals"
            val targetName = stripColor(entity.displayName.formattedText)

            val shouldReturn = mc.netHandler.playerInfoMap.any { networkPlayerInfo ->
                val networkName = stripColor(networkPlayerInfo.getFullName())
                if (equals) {
                    targetName == networkName
                } else {
                    networkName in targetName
                }
            }
            return !shouldReturn
        }

        return false
    }

    fun matchedTransparentPredicates(entity: EntityLivingBase): List<String> {
        if (entity !is EntityPlayer || !handleEvents()) return emptyList()
        return transparentPredicates.evaluate(entity).matchedRules
    }

    val onUpdate = handler<UpdateEvent>(always = true) {
        val world = mc.theWorld ?: return@handler

        world.loadedEntityList.forEach { entity ->
            if (entity !is EntityPlayer) return@forEach
            val profile = entity.gameProfile ?: return@forEach

            if (isBot(entity)) {
                if (profile.id !in botList) {
                    botList += profile.id
                }
            } else {
                if (profile.id in botList) {
                    botList -= profile.id
                }
            }
        }

        // Periodic cleanup every 100 ticks (~5 seconds) to prevent memory leaks
        cleanupTicks++
        if (cleanupTicks >= 100) {
            cleanupTicks = 0
            performPeriodicCleanup(world)
        }
    }

    /**
     * Removes stale entity data from tracking collections
     * Called every ~5 seconds to prevent unbounded memory growth
     */
    private fun performPeriodicCleanup(world: net.minecraft.world.World) {
        val validEntityIds = world.loadedEntityList
            .filterIsInstance<EntityPlayer>()
            .mapTo(mutableSetOf()) { it.entityId }

        // Remove invalid entity IDs from all collections
        groundList.retainAll(validEntityIds)
        airList.retainAll(validEntityIds)
        invalidGroundList.keys.retainAll(validEntityIds)
        invalidSpeedList.retainAll(validEntityIds)
        swingList.retainAll(validEntityIds)
        invisibleList.retainAll(validEntityIds)
        propertiesList.retainAll(validEntityIds)
        hitList.retainAll(validEntityIds)
        notAlwaysInRadiusList.retainAll(validEntityIds)
        alwaysBehindList.retainAll(validEntityIds)
        entityTickMap.keys.retainAll(validEntityIds)

        // Clean up bot list - remove UUIDs of players no longer in world
        val validUUIDs = world.playerEntities
            .mapNotNullTo(mutableSetOf()) { it.gameProfile?.id }
        botList.retainAll(validUUIDs)

        // Clean up duplicate name tracking
        val currentWorldPlayers = world.playerEntities.mapTo(mutableSetOf()) { it.name }
        worldPlayerNames.retainAll(currentWorldPlayers)
        worldDuplicateNames.retainAll(currentWorldPlayers)

        val currentTabPlayers = mc.netHandler?.playerInfoMap
            ?.mapNotNullTo(mutableSetOf()) { stripColor(it.getFullName()) }
            ?: emptySet()
        tabPlayerNames.retainAll(currentTabPlayers)
        tabDuplicateNames.retainAll(currentTabPlayers)
    }

    // Alternative for isBot() check.
    val onPacket = handler<PacketEvent>(always = true) { event ->
        if (mc.thePlayer == null || mc.theWorld == null)
            return@handler

        val packet = event.packet

        if (packet is S14PacketEntity) {
            val entity = packet.getEntity(mc.theWorld)

            if (entity is EntityPlayer) {
                if (entity.onGround && entity.entityId !in groundList)
                    groundList += entity.entityId

                if (!entity.onGround && entity.entityId !in airList)
                    airList += entity.entityId

                if (entity.onGround) {
                    // A claimed ground state combined with vertical movement is physically
                    // impossible, so accumulate a violation; legit standing/flat-walking keeps posY == prevPosY.
                    if (entity.posY != entity.prevPosY) {
                        invalidGroundList[entity.entityId] = invalidGroundList.getOrDefault(entity.entityId, 0) + 1
                    }
                } else {
                    // Decay with a half-life so transient anomalies fade instead of latching.
                    val newViolations = invalidGroundList.getOrDefault(entity.entityId, 0) / 2

                    if (newViolations <= 0) {
                        invalidGroundList.remove(entity.entityId)
                    } else {
                        invalidGroundList[entity.entityId] = newViolations
                    }
                }

                if ((entity.isInvisible || entity.isInvisibleToPlayer(mc.thePlayer)) && entity.entityId !in invisibleList)
                    invisibleList += entity.entityId

                if (alwaysInRadius) {
                    val distance = mc.thePlayer.getDistanceToEntity(entity)
                    val currentTicks = entityTickMap.getOrDefault(entity.entityId, 0)

                    if (distance < alwaysRadius) {
                        entityTickMap[entity.entityId] = currentTicks + 1
                    } else {
                        entityTickMap[entity.entityId] = 0
                    }

                    if (entityTickMap[entity.entityId]!! >= alwaysRadiusTick) {
                        notAlwaysInRadiusList -= entity.entityId
                    } else {
                        if (entity.entityId !in notAlwaysInRadiusList) {
                            notAlwaysInRadiusList += entity.entityId
                        }
                    }
                }

                if (alwaysBehind) {
                    val distance = mc.thePlayer.getDistanceToEntity(entity)
                    val rotationToEntity = toRotation(entity.hitBox.center, false, mc.thePlayer).fixedSensitivity().yaw
                    val angleDifferenceToEntity = abs(angleDifference(rotationToEntity, serverRotation.yaw))

                    if (distance < alwaysBehindRadius && angleDifferenceToEntity > behindRotDiffToIgnore) {
                        alwaysBehindList += entity.entityId
                    } else {
                        if (entity.entityId in alwaysBehindList) {
                            alwaysBehindList -= entity.entityId
                        }
                    }
                }

                if (invalidSpeed) {
                    val deltaX = entity.posX - entity.prevPosX
                    val deltaZ = entity.posZ - entity.prevPosZ
                    val speed = sqrt(deltaX * deltaX + deltaZ * deltaZ)


                    if (speed in 0.45..0.46 && (!entity.isSprinting || !entity.isMoving ||
                                entity.getActivePotionEffect(Potion.moveSpeed) == null)
                    ) {
                        invalidSpeedList += entity.entityId
                    }
                }
            }
        }

        if (packet is S0BPacketAnimation) {
            val entity = mc.theWorld.getEntityByID(packet.entityID)

            if (entity != null && entity is EntityLivingBase && packet.animationType == 0
                && entity.entityId !in swingList
            )
                swingList += entity.entityId
        }

        if (packet is S20PacketEntityProperties) {
            propertiesList += packet.entityId
        }

        if (packet is S13PacketDestroyEntities) {
            for (entityID in packet.entityIDs) {
                // Remove [entityID] from every list upon deletion
                groundList -= entityID
                airList -= entityID
                invalidGroundList -= entityID
                swingList -= entityID
                invisibleList -= entityID
                notAlwaysInRadiusList -= entityID
                propertiesList -= entityID
            }
        }
    }

    val onAttack = handler<AttackEvent>(always = true) { e ->
        val entity = e.targetEntity

        if (entity != null && entity is EntityLivingBase && entity.entityId !in hitList)
            hitList += entity.entityId
    }

    val onWorld = handler<WorldEvent>(always = true) {
        clearAll()
    }

    private fun clearAll() {
        hitList.clear()
        swingList.clear()
        groundList.clear()
        invalidGroundList.clear()
        invalidSpeedList.clear()
        invisibleList.clear()
        notAlwaysInRadiusList.clear()
        worldPlayerNames.clear()
        worldDuplicateNames.clear()
        tabPlayerNames.clear()
        tabDuplicateNames.clear()
        alwaysBehindList.clear()
        entityTickMap.clear()
        botList.clear()
    }

}
