/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.center
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0APacketAnimation
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugRecorder : Module("DebugRecorder", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Generic", "Aim", "CPS", "Combat"), "Generic")
        .describe("Which kind of per-tick data to capture for offline analysis.")
    private val maxRecords by int("MaxRecords", 10000, 100..100000)
        .describe("Stop appending once this many records are captured.")

    private val records = mutableListOf<JsonObject>()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var lastYaw = 0f
    private var lastPitch = 0f
    private var attackTarget: EntityLivingBase? = null

    override fun onEnable() {
        records.clear()
        attackTarget = null
        val rotation = RotationUtils.serverRotation
        lastYaw = rotation.yaw
        lastPitch = rotation.pitch
        chat("Recording $mode...")
    }

    val onUpdate = handler<UpdateEvent> {
        if (records.size >= maxRecords) {
            return@handler
        }

        when (mode) {
            "Generic" -> recordGeneric()
            "Aim" -> recordAim()
            "Combat" -> recordCombat()
        }

        val rotation = RotationUtils.serverRotation
        lastYaw = rotation.yaw
        lastPitch = rotation.pitch
    }

    val onPacket = handler<PacketEvent> { event ->
        if (mode != "CPS" || records.size >= maxRecords) {
            return@handler
        }

        if (event.eventType == EventState.SEND && event.packet is C0APacketAnimation) {
            push(JsonObject().apply {
                addProperty("type", "swing")
                addProperty("time", System.currentTimeMillis())
            })
        }
    }

    val onAttack = handler<AttackEvent> { event ->
        if (records.size >= maxRecords) {
            return@handler
        }

        val target = event.targetEntity as? EntityLivingBase

        if (mode == "CPS") {
            push(JsonObject().apply {
                addProperty("type", "attack")
                addProperty("time", System.currentTimeMillis())
                addProperty("hit", target != null)
            })
        }

        if (mode == "Combat" && target != null) {
            attackTarget = target
            val player = mc.thePlayer ?: return@handler
            val rotation = RotationUtils.serverRotation
            val targetRotation = RotationUtils.toRotation(target.hitBox.center)
            push(JsonObject().apply {
                addProperty("type", "hit")
                addProperty("time", System.currentTimeMillis())
                addProperty("yaw", rotation.yaw)
                addProperty("pitch", rotation.pitch)
                addProperty("diff_yaw", RotationUtils.angleDifference(rotation.yaw, targetRotation.yaw))
                addProperty("diff_pitch", RotationUtils.angleDifference(rotation.pitch, targetRotation.pitch))
                addProperty("distance", player.getDistanceToEntityBox(target))
                addProperty("hurt_time", target.hurtTime)
            })
        }
    }

    override fun onDisable() {
        if (records.isEmpty()) {
            chat("No data recorded.")
            return
        }

        runCatching {
            val folder = File(FileManager.dir, "debug-recorder/$mode")
            folder.mkdirs()

            val baseName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ROOT).format(Date())
            var file = File(folder, "$baseName.json")
            var idx = 0
            while (file.exists()) {
                file = File(folder, "${baseName}_${idx++}.json")
            }

            val array = JsonArray()
            records.forEach(array::add)
            file.bufferedWriter().use { gson.toJson(array, it) }
            file.absolutePath
        }.onSuccess { path ->
            chat("Recorded ${records.size} entries to $path")
        }.onFailure {
            chat("Failed to write recording: ${it.message}")
        }

        records.clear()
        attackTarget = null
    }

    private fun recordGeneric() {
        val player = mc.thePlayer ?: return
        push(JsonObject().apply {
            addProperty("time", System.currentTimeMillis())
            addProperty("x", player.posX)
            addProperty("y", player.posY)
            addProperty("z", player.posZ)
            addProperty("motion_x", player.motionX)
            addProperty("motion_y", player.motionY)
            addProperty("motion_z", player.motionZ)
            addProperty("yaw", player.rotationYaw)
            addProperty("pitch", player.rotationPitch)
            addProperty("on_ground", player.onGround)
            addProperty("health", player.health)
        })
    }

    private fun recordAim() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return
        val rotation = RotationUtils.serverRotation

        val obj = JsonObject().apply {
            addProperty("time", System.currentTimeMillis())
            addProperty("yaw", rotation.yaw)
            addProperty("pitch", rotation.pitch)
            addProperty("last_yaw", lastYaw)
            addProperty("last_pitch", lastPitch)
            addProperty("turn_speed_h", RotationUtils.angleDifference(rotation.yaw, lastYaw))
            addProperty("turn_speed_v", RotationUtils.angleDifference(rotation.pitch, lastPitch))
            addProperty("health", player.health)
        }

        world.loadedEntityList.toList().asSequence()
            .filterIsInstance<EntityLivingBase>()
            .filter { it != player && isSelected(it, true) }
            .minByOrNull { player.getDistanceToEntityBox(it) }
            ?.let { target ->
                val targetRotation = RotationUtils.toRotation(target.hitBox.center)
                obj.apply {
                    addProperty("target_yaw", targetRotation.yaw)
                    addProperty("target_pitch", targetRotation.pitch)
                    addProperty("diff_yaw", RotationUtils.angleDifference(rotation.yaw, targetRotation.yaw))
                    addProperty("diff_pitch", RotationUtils.angleDifference(rotation.pitch, targetRotation.pitch))
                    addProperty("distance", player.getDistanceToEntityBox(target))
                }
            }

        push(obj)
    }

    private fun recordCombat() {
        val target = attackTarget ?: return
        val player = mc.thePlayer ?: return

        if (target.isDead || target.health <= 0f) {
            attackTarget = null
            return
        }

        val rotation = RotationUtils.serverRotation
        val targetRotation = RotationUtils.toRotation(target.hitBox.center)
        push(JsonObject().apply {
            addProperty("type", "track")
            addProperty("time", System.currentTimeMillis())
            addProperty("yaw", rotation.yaw)
            addProperty("pitch", rotation.pitch)
            addProperty("turn_speed_h", RotationUtils.angleDifference(rotation.yaw, lastYaw))
            addProperty("turn_speed_v", RotationUtils.angleDifference(rotation.pitch, lastPitch))
            addProperty("diff_yaw", RotationUtils.angleDifference(rotation.yaw, targetRotation.yaw))
            addProperty("diff_pitch", RotationUtils.angleDifference(rotation.pitch, targetRotation.pitch))
            addProperty("distance", player.getDistanceToEntityBox(target))
            addProperty("hurt_time", target.hurtTime)
        })
    }

    private fun push(obj: JsonObject) {
        records.add(obj)
    }
}
