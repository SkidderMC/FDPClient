/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.extensions.getFullName
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.*
import net.minecraft.world.WorldSettings
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
    private val hiddenNameValue = BoolValue("HiddenName", false)
    private val armorValue = BoolValue("Armor", false)
    private val pingValue = BoolValue("Ping", false)
    private val needHitValue = BoolValue("NeedHit", false)
    private val noClipValue = BoolValue("NoClip", false)
    private val czechHekValue = BoolValue("CzechMatrix", false)
    private val czechHekPingCheckValue = BoolValue("PingCheck", true).displayable { czechHekValue.get() }
    private val czechHekGMCheckValue = BoolValue("GamemodeCheck", true).displayable { czechHekValue.get() }
    private val reusedEntityIdValue = BoolValue("ReusedEntityId", false)
    private val spawnInCombatValue = BoolValue("SpawnInCombat", false)
    private val duplicateInWorldValue = BoolValue("DuplicateInWorld", false)
    private val duplicateInTabValue = BoolValue("DuplicateInTab", false)
    private val duplicateCompareModeValue = ListValue("DuplicateCompareMode", arrayOf("OnTime", "WhenSpawn"), "OnTime").displayable { duplicateInTabValue.get() || duplicateInWorldValue.get() }
    private val fastDamageValue = BoolValue("FastDamage", false)
    private val fastDamageTicksValue = IntegerValue("FastDamageTicks", 5, 1, 20).displayable { fastDamageValue.get() }
    private val removeFromWorld = BoolValue("RemoveFromWorld", false)
    private val removeIntervalValue = IntegerValue("Remove-Interval", 20, 1, 100).displayable { removeFromWorld.get() }
    private val debugValue = BoolValue("Debug", false)
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
    private val noClip = mutableListOf<Int>()
    private val hasRemovedEntities = mutableListOf<Int>()
    private val regex = Regex("\\w{3,16}")
    private var wasAdded = mc.thePlayer != null

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        if (removeFromWorld.get() && mc.thePlayer.ticksExisted > 0 && mc.thePlayer.ticksExisted % removeIntervalValue.get() == 0) {
            val ent: MutableList<EntityPlayer> = ArrayList()
            for (entity in mc.theWorld.playerEntities) {
                if (entity !== mc.thePlayer && isBot(entity)) ent.add(entity)
            }
            if (ent.isEmpty()) return
            for (e in ent) {
                mc.theWorld.removeEntity(e)
                if (debugValue.get()) displayChatMessage("§7[§a§lAnti Bot§7] §fRemoved §r" + e.name + " §fdue to it being a bot.")
            }
        }
    }

    @JvmStatic
    fun isBot(entity: EntityLivingBase): Boolean {
        // Check if entity is a player
        if (entity !is EntityPlayer || entity === mc.thePlayer) {
            return false
        }

        // Check if anti bot is enabled
        if (!state) {
            return false
        }

        if (validNameValue.get() && !entity.name.matches(regex)) {
            return true
        }
        
        if (hiddenNameValue.get() && ( entity.getName().contains("\u00A7") || (entity.hasCustomName() && entity.getCustomNameTag().contains(entity.getName()) ))){
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

        if(noClipValue.get() && noClip.contains(entity.entityId)) {
            return true
        }

        if(reusedEntityIdValue.get() && hasRemovedEntities.contains(entity.entityId)) {
            return false
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

            for (networkPlayerInfo in mc.netHandler.playerInfoMap) {
                val networkName = stripColor(networkPlayerInfo.getFullName())

                if (if (equals) targetName == networkName else targetName.contains(networkName)) {
                    return false
                }
            }

            return true
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
            if (alwaysInRadiusRemoveValue.get()) {
                mc.theWorld.removeEntity(entity)
            }
            return true
            
        }

        return entity.name.isEmpty() || entity.name == mc.thePlayer.name
    }

    override fun onDisable() {
        clearAll()
        super.onDisable()
    }

    private fun processEntityMove(entity: Entity, onGround: Boolean) {
        if (entity is EntityPlayer) {
            if (onGround && !ground.contains(entity.entityId)) {
                ground.add(entity.entityId)
            }

            if (!onGround && !air.contains(entity.entityId)) {
                air.add(entity.entityId)
            }

            if (onGround) {
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

            if (!noClip.contains(entity.entityId)) {
                val cb = mc.theWorld.getCollidingBoundingBoxes(entity, entity.entityBoundingBox.contract(0.0625, 0.0625, 0.0625))
//                alert("NOCLIP[${cb.size}] ${entity.displayName.unformattedText} ${entity.posX} ${entity.posY} ${entity.posZ}")
                if(cb.isNotEmpty()) {
                    noClip.add(entity.entityId)
                }
            }

            if ((!livingTimeValue.get() || entity.ticksExisted > livingTimeTicksValue.get() || !alwaysInRadiusWithTicksCheckValue.get()) && !notAlwaysInRadius.contains(entity.entityId) && mc.thePlayer.getDistanceToEntity(entity) > alwaysRadiusValue.get()) {
                notAlwaysInRadius.add(entity.entityId)
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        if (czechHekValue.get()) {

            val packet = event.packet

            if (packet is S41PacketServerDifficulty) wasAdded = false
            if (packet is S38PacketPlayerListItem) {
                val packetListItem = event.packet as S38PacketPlayerListItem
                val data = packetListItem.entries[0]
                if (data.profile != null && data.profile.name != null) {
                    if (!wasAdded) wasAdded =
                        data.profile.name == mc.thePlayer.name else if (!mc.thePlayer.isSpectator && !mc.thePlayer.capabilities.allowFlying && (!czechHekPingCheckValue.get() || data.ping != 0) && (!czechHekGMCheckValue.get() || data.gameMode != WorldSettings.GameType.NOT_SET)) {
                        event.cancelEvent()
                        if (debugValue.get()) displayChatMessage("§7[§a§lAnti Bot/§6Matrix§7] §fPrevented §r" + data.profile.name + " §ffrom spawning.")
                    }
                }
            }
        }
        val packet = event.packet

        if(packet is S18PacketEntityTeleport) {
            processEntityMove(mc.theWorld.getEntityByID(packet.entityId) ?: return, packet.onGround)
        } else if (packet is S14PacketEntity) {
            processEntityMove(packet.getEntity(mc.theWorld) ?: return, packet.onGround)
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
            if(FDPClient.combatManager.inCombat && !hasRemovedEntities.contains(packet.entityID)) {
                spawnInCombat.add(packet.entityID)
            }
        } else if (packet is S13PacketDestroyEntities) {
            hasRemovedEntities.addAll(packet.entityIDs.toTypedArray())
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

        if (entity is EntityLivingBase && !hitted.contains(entity.entityId)) {
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
        noClip.clear()
        hasRemovedEntities.clear()
    }

}
