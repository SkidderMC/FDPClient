package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.extensions.getFullName
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.*
import java.util.*

@ModuleInfo(name = "AntiBot", category = ModuleCategory.MISC)
object AntiBot : Module() {

    private val tabValue = BoolValue("Tab", true)
    private val tabModeValue = ListValue("TabMode", arrayOf("Equals", "Contains"), "Contains").displayable { tabValue.get() }
    private val entityIDValue = BoolValue("EntityID", true)
    private val colorValue = BoolValue("Color", false)
    private val livingTimeValue = BoolValue("LivingTime", false)
    private val livingTimeTicksValue = IntegerValue("LivingTimeTicks", 40, 1, 200).displayable { livingTimeValue.get() }
    private val groundValue = BoolValue("Ground", true)
    private val airValue = BoolValue("Air", false)
    private val invalidGroundValue = BoolValue("InvalidGround", true)
    private val swingValue = BoolValue("Swing", false)
    private val healthValue = BoolValue("Health", false)
    private val derpValue = BoolValue("Derp", true)
    private val wasInvisibleValue = BoolValue("WasInvisible", false)
    private val validNameValue = BoolValue("ValidName", true)
    private val armorValue = BoolValue("Armor", false)
    private val pingValue = BoolValue("Ping", false)
    private val needHitValue = BoolValue("NeedHit", false)
    private val spawnInCombatValue = BoolValue("SpawnInCombat", false)
    private val duplicateInWorldValue = BoolValue("DuplicateInWorld", false)
    private val duplicateInTabValue = BoolValue("DuplicateInTab", false)
    private val duplicateCompareModeValue = ListValue("DuplicateCompareMode", arrayOf("OnTime", "WhenSpawn"), "OnTime").displayable { duplicateInTabValue.get() || duplicateInWorldValue.get() }
    private val fastDamageValue = BoolValue("FastDamage", false)
    private val fastDamageTicksValue = IntegerValue("FastDamageTicks", 5, 1, 20).displayable { fastDamageValue.get() }
    private val alwaysInRadiusValue = BoolValue("AlwaysInRadius", false)
    private val alwaysRadiusValue = FloatValue("AlwaysInRadiusBlocks", 20f, 5f, 30f).displayable { alwaysInRadiusValue.get() }
    private val alwaysInRadiusRemoveValue = BoolValue("AlwaysInRadiusRemove", false).displayable { alwaysInRadiusValue.get() }
    private val alwaysInRadiusWithTicksCheckValue = BoolValue("AlwaysInRadiusWithTicksCheck", false).displayable { alwaysInRadiusValue.get() && livingTimeValue.get() }

    private val ground = mutableListOf<Int>()
    private val air = mutableListOf<Int>()
    private val invalidGround = mutableMapOf<Int, Int>()
    private val swing = mutableListOf<Int>()
    private val invisible = mutableListOf<Int>()
    private val hitted = mutableListOf<Int>()
    private val spawnInCombat = mutableListOf<Int>()
    private val notAlwaysInRadius = mutableListOf<Int>()
    private val lastDamage = mutableMapOf<Int, Int>()
    private val lastDamageVl = mutableMapOf<Int, Float>()
    private val duplicate = mutableListOf<UUID>()
    private val regex = Regex("\\w{3,16}")

    fun isBot(entity: EntityLivingBase): Boolean {
        // Check if entity is a player
        if (entity !is EntityPlayer) {
            return false
        }

        // Check if anti bot is enabled
        if (!state) {
            return false
        }

        if (validNameValue.get() && !entity.name.matches(regex)) {
            return true
        }

        // Anti Bot checks
        if (colorValue.get() && !entity.displayName.formattedText.replace("§r", "").contains("§")) {
            return true
        }

        if (livingTimeValue.get() && entity.ticksExisted < livingTimeTicksValue.get()) {
            return true
        }

        if (groundValue.get() && !ground.contains(entity.entityId)) {
            return true
        }

        if (airValue.get() && !air.contains(entity.entityId)) {
            return true
        }

        if (swingValue.get() && !swing.contains(entity.entityId)) {
            return true
        }

        if (healthValue.get() && (entity.health > 20F || entity.health <= 0F)) {
            return true
        }

        if (spawnInCombatValue.get() && spawnInCombat.contains(entity.entityId)) {
            return true
        }

        if (entityIDValue.get() && (entity.entityId >= 1000000000 || entity.entityId <= -1)) {
            return true
        }

        if (derpValue.get() && (entity.rotationPitch > 90F || entity.rotationPitch < -90F)) {
            return true
        }

        if (wasInvisibleValue.get() && invisible.contains(entity.entityId)) {
            return true
        }

        if (armorValue.get()) {
            if (entity.inventory.armorInventory[0] == null && entity.inventory.armorInventory[1] == null &&
                entity.inventory.armorInventory[2] == null && entity.inventory.armorInventory[3] == null) {
                return true
            }
        }

        if (pingValue.get()) {
            if (mc.netHandler.getPlayerInfo(entity.uniqueID)?.responseTime == 0) {
                return true
            }
        }

        if (needHitValue.get() && !hitted.contains(entity.entityId)) {
            return true
        }

        if (invalidGroundValue.get() && invalidGround.getOrDefault(entity.entityId, 0) >= 10) {
            return true
        }

        if (tabValue.get()) {
            val equals = tabModeValue.equals("Equals")
            val targetName = stripColor(entity.displayName.formattedText)

            if (targetName != null) {
                for (networkPlayerInfo in mc.netHandler.playerInfoMap) {
                    val networkName = stripColor(networkPlayerInfo.getFullName()) ?: continue

                    if (if (equals) targetName == networkName else targetName.contains(networkName)) {
                        return false
                    }
                }

                return true
            }
        }

        if (duplicateCompareModeValue.equals("WhenSpawn") && duplicate.contains(entity.gameProfile.id)) {
            return true
        }

        if (duplicateInWorldValue.get() && duplicateCompareModeValue.equals("OnTime") && mc.theWorld.loadedEntityList.count { it is EntityPlayer && it.name == it.name } > 1) {
            return true
        }

        if (duplicateInTabValue.get() && duplicateCompareModeValue.equals("OnTime") && mc.netHandler.playerInfoMap.count { entity.name == it.gameProfile.name } > 1) {
            return true
        }

        if (fastDamageValue.get() && lastDamageVl.getOrDefault(entity.entityId, 0f) > 0) {
            return true
        }

        if (alwaysInRadiusValue.get() && !notAlwaysInRadius.contains(entity.entityId)) {
            return true
        }

        return entity.name.isEmpty() || entity.name == mc.thePlayer.name
    }

    override fun onDisable() {
        clearAll()
        super.onDisable()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val packet = event.packet

        if (packet is S14PacketEntity) {
            val entity = packet.getEntity(mc.theWorld)

            if (entity is EntityPlayer) {
                if (packet.onGround && !ground.contains(entity.entityId)) {
                    ground.add(entity.entityId)
                }

                if (!packet.onGround && !air.contains(entity.entityId)) {
                    air.add(entity.entityId)
                }

                if (packet.onGround) {
                    if (entity.prevPosY != entity.posY) {
                        invalidGround[entity.entityId] = invalidGround.getOrDefault(entity.entityId, 0) + 1
                    }
                } else {
                    val currentVL = invalidGround.getOrDefault(entity.entityId, 0) / 2
                    if (currentVL <= 0) {
                        invalidGround.remove(entity.entityId)
                    } else {
                        invalidGround[entity.entityId] = currentVL
                    }
                }

                if (entity.isInvisible && !invisible.contains(entity.entityId)) {
                    invisible.add(entity.entityId)
                }

                if ((!livingTimeValue.get() || entity.ticksExisted > livingTimeTicksValue.get() || !alwaysInRadiusWithTicksCheckValue.get()) && !notAlwaysInRadius.contains(entity.entityId) && mc.thePlayer.getDistanceToEntity(entity) > alwaysRadiusValue.get()) {
                    notAlwaysInRadius.add(entity.entityId)
                    if (alwaysInRadiusRemoveValue.get()) {
                        mc.theWorld.removeEntity(entity)
                    }
                }
            }
        } else if (packet is S0BPacketAnimation) {
            val entity = mc.theWorld.getEntityByID(packet.entityID)

            if (entity != null && entity is EntityLivingBase && packet.animationType == 0 &&
                !swing.contains(entity.entityId)) {
                swing.add(entity.entityId)
            }
        } else if (packet is S38PacketPlayerListItem) {
            if (duplicateCompareModeValue.equals("WhenSpawn") && packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                packet.entries.forEach { entry ->
                    val name = entry.profile.name
                    if (duplicateInWorldValue.get() && mc.theWorld.playerEntities.any { it.name == name } ||
                        duplicateInTabValue.get() && mc.netHandler.playerInfoMap.any { it.gameProfile.name == name }) {
                        duplicate.add(entry.profile.id)
                    }
                }
            }
        } else if (packet is S0CPacketSpawnPlayer) {
            if(LiquidBounce.combatManager.inCombat) {
                spawnInCombat.add(packet.entityID)
            }
        }

        if (packet is S19PacketEntityStatus && packet.opCode.toInt() == 2 || packet is S0BPacketAnimation && packet.animationType == 1) {
            val entity = if (packet is S19PacketEntityStatus) { packet.getEntity(mc.theWorld) } else if (packet is S0BPacketAnimation) { mc.theWorld.getEntityByID(packet.entityID) } else { null } ?: return

            if (entity is EntityPlayer) {
                lastDamageVl[entity.entityId] = lastDamageVl.getOrDefault(entity.entityId, 0f) + if (entity.ticksExisted - lastDamage.getOrDefault(entity.entityId, 0) <= fastDamageTicksValue.get()) {
                     1f
                } else {
                    -0.5f
                }
                lastDamage[entity.entityId] = entity.ticksExisted
            }
        }
    }

    @EventTarget
    fun onAttack(e: AttackEvent) {
        val entity = e.targetEntity

        if (entity != null && entity is EntityLivingBase && !hitted.contains(entity.entityId)) {
            hitted.add(entity.entityId)
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        clearAll()
    }

    private fun clearAll() {
        hitted.clear()
        swing.clear()
        ground.clear()
        invalidGround.clear()
        invisible.clear()
        lastDamage.clear()
        lastDamageVl.clear()
        notAlwaysInRadius.clear()
        duplicate.clear()
        spawnInCombat.clear()
    }
}
