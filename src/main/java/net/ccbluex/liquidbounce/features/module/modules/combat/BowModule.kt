/*
 * ZAVZ Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.combat


import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Items
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color
@ModuleInfo(name = "BowModule", category = ModuleCategory.COMBAT)
class BowModule : Module() {

    // AutoBow
    private val AutoBowValue = BoolValue("AutoBow", false)
    private val waitForBowAimbot = BoolValue("AutoBow-WaitForBowAimbot", true).displayable { AutoBowValue.get() }

    // BowAimbot
    private val BowAimbotValue = BoolValue("BowAimbot Options", false)
    private val silentValue = BoolValue("BowAimbot-Silent", true).displayable { BowAimbotValue.get() }
    private val predictValue = BoolValue("BowAimbot-Predict", true).displayable { BowAimbotValue.get() }
    private val throughWallsValue = BoolValue("BowAimbot-ThroughWalls", false).displayable { BowAimbotValue.get() }
    private val predictSizeValue = FloatValue("BowAimbot-PredictSize", 2F, 0.1F, 5F).displayable { predictValue.get() }
    private val priorityValue = ListValue("BowAimbot-Priority", arrayOf("Health", "Distance", "Direction"), "Direction").displayable { BowAimbotValue.get() }
    private val markValue = BoolValue("BowAimbot-Mark", true).displayable { BowAimbotValue.get() }

    //FastBow
    private val fastBowValue = BoolValue("FastBow", false)
    private val packetsValue = IntegerValue("FastBow-Packets", 20, 3, 20).displayable { fastBowValue.get() }
    private val delay = IntegerValue("FastBow-Delay", 0, 0, 500).displayable { fastBowValue.get() }

    //BowAura
    private val bowAuraValue = BoolValue("BowAura Options", false)
    private val AuraSilentValue = BoolValue("Silent", true)
    private val AuraPredictValue = BoolValue("Predict", true)
    private val AuraThroughWallsValue = BoolValue("ThroughWalls", false)
    private val AuraPredictSizeValue = FloatValue("PredictSize", 2F, 0.1F, 5F).displayable { predictValue.get() }
    private val AuraPriorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Direction"), "Direction")
    private val AuraMaxDistance = FloatValue("MaxDistance",100F,5F,200F)
    private val AuraMarkValue = BoolValue("Mark", true)


    val timer = MSTimer()

    private var target: Entity? = null

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (fastBowValue.get()) {
            if (!mc.thePlayer.isUsingItem) {
                return
            }

            if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().item is ItemBow) {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.currentEquippedItem, 0F, 0F, 0F))

                val yaw = if (RotationUtils.targetRotation != null)
                    RotationUtils.targetRotation.yaw
                else
                    mc.thePlayer.rotationYaw

                val pitch = if (RotationUtils.targetRotation != null)
                    RotationUtils.targetRotation.pitch
                else
                    mc.thePlayer.rotationPitch
                for (i in 0 until packetsValue.get())
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, true))
                if (timer.hasTimePassed(delay.get().toLong())) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                    timer.reset()
                }
                mc.thePlayer.itemInUseCount = mc.thePlayer.inventory.getCurrentItem().maxItemUseDuration - 1
            }
        }

        if (AutoBowValue.get()) {
            if (mc.thePlayer.isUsingItem && mc.thePlayer.heldItem?.item == Items.bow &&
                mc.thePlayer.itemInUseDuration > 20 && (!waitForBowAimbot.get() || hasTarget())) {
                mc.thePlayer.stopUsingItem()
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
        }

        if (!BowAimbotValue.get()) {
            return
        }

        target = null

        if (mc.thePlayer.itemInUse?.item is ItemBow) {
            val entity = getTarget(throughWallsValue.get(), priorityValue.get()) ?: return

            target = entity
            RotationUtils.faceBow(target, silentValue.get(), predictValue.get(), predictSizeValue.get())
        }

        if (bowAuraValue.get()) {
            target = null
            val invBow = bow ?: return
            target = getBowAaura(AuraPriorityValue.get()) ?: return
            mc.thePlayer!!.inventory.currentItem = invBow
            mc.gameSettings.keyBindUseItem.pressed = true
            RotationUtils.faceBow(target, AuraSilentValue.get(), AuraPredictValue.get(), AuraPredictSizeValue.get())
            if (mc.thePlayer.isUsingItem&&mc.thePlayer.itemInUseDuration > 20){
                mc.gameSettings.keyBindUseItem.pressed = false
                mc.thePlayer.stopUsingItem()
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
        }

    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!BowAimbotValue.get()) {
            return
        }

        if (target != null && !priorityValue.equals("Multi") && markValue.get()) {
            RenderUtils.drawPlatform(target, Color(37, 126, 255, 70))
        }

        if (!bowAuraValue.get()) {
        if (!AuraMarkValue.get()|| target ==null) return
        RenderUtils.drawPlatform(target, Color(37, 126, 255, 70))
        }
    }

    override fun onDisable() { target = null }

    private val bow: Int?
        get() {
            var arrow = false
            for (inv in mc.thePlayer.inventory.mainInventory){
                if (inv != null&&inv.item==Items.arrow) {
                    arrow = true
                    break
                }
            }
            if (!arrow&&!mc.playerController.isInCreativeMode) return null
            for (i in 0..8) {
                val itemStack = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
                if (itemStack.item is ItemBow) {
                    return i
                }
            }
            return null
        }

    private fun getTarget(throughWalls: Boolean, priorityMode: String): Entity? {
        val targets = mc.theWorld.loadedEntityList.filter {
            it is EntityLivingBase && EntityUtils.isSelected(it, true) &&
                    (throughWalls || mc.thePlayer.canEntityBeSeen(it))
        }

        return when (priorityMode.uppercase()) {
            "DISTANCE" -> targets.minByOrNull { mc.thePlayer.getDistanceToEntity(it) }
            "DIRECTION" -> targets.minByOrNull { RotationUtils.getRotationDifference(it) }
            "HEALTH" -> targets.minByOrNull { (it as EntityLivingBase).health }
            else -> null
        }
    }

    private fun getBowAaura(priorityMode: String): Entity? {
        val targets = mc.theWorld!!.loadedEntityList.filter {
            it is EntityLivingBase&&
                    EntityUtils.isSelected(it, true)&&
                    (AuraThroughWallsValue.get() || mc.thePlayer!!.canEntityBeSeen(it))&&
                    mc.thePlayer.getDistanceToEntity(it)<= AuraMaxDistance.get()
        }
        return when {
            priorityMode.equals("distance", true) -> targets.minByOrNull { mc.thePlayer!!.getDistanceToEntity(it) }
            priorityMode.equals("direction", true) -> targets.minByOrNull { RotationUtils.getRotationDifference(it) }
            priorityMode.equals("health", true) -> targets.minByOrNull { (it as EntityLivingBase).health }
            else -> null
        }
    }
    fun hasTarget() = target != null && mc.thePlayer.canEntityBeSeen(target)

}