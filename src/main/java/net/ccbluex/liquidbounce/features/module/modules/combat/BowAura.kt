/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Items
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color

@ModuleInfo(name = "BowAura", category = ModuleCategory.COMBAT)
object BowAura : Module() {

    private val silentValue = BoolValue("Silent", true)
    private val predictValue = BoolValue("Predict", true)
    private val throughWallsValue = BoolValue("ThroughWalls", false)
    private val predictSizeValue = FloatValue("PredictSize", 2F, 0.1F, 5F).displayable { predictValue.get() }
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Direction"), "Direction")
    private val maxDistance = FloatValue("MaxDistance",100F,5F,200F)
    private val markValue = BoolValue("Mark", true)

    private var target: Entity? = null

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        target = null
        val invBow = bow ?: return
        target = getTarget(priorityValue.get()) ?: return
        mc.thePlayer!!.inventory.currentItem = invBow
        mc.gameSettings.keyBindUseItem.pressed = true
        RotationUtils.faceBow(target, silentValue.get(), predictValue.get(), predictSizeValue.get())
        if (mc.thePlayer.isUsingItem&&mc.thePlayer.itemInUseDuration > 20){
            mc.gameSettings.keyBindUseItem.pressed = false
            mc.thePlayer.stopUsingItem()
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }
    @EventTarget fun onRender3D(event: Render3DEvent){
        if (!markValue.get()||target==null) return
        RenderUtils.drawPlatform(target, Color(37, 126, 255, 70))
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
    private fun getTarget(priorityMode: String): Entity? {
        val targets = mc.theWorld!!.loadedEntityList.filter {
            it is EntityLivingBase&&
                    EntityUtils.isSelected(it, true)&&
                    (throughWallsValue.get() || mc.thePlayer!!.canEntityBeSeen(it))&&
                    mc.thePlayer.getDistanceToEntity(it)<=maxDistance.get()
        }
        return when {
            priorityMode.equals("distance", true) -> targets.minByOrNull { mc.thePlayer!!.getDistanceToEntity(it) }
            priorityMode.equals("direction", true) -> targets.minByOrNull { RotationUtils.getRotationDifference(it) }
            priorityMode.equals("health", true) -> targets.minByOrNull { (it as EntityLivingBase).health }
            else -> null
        }
    }
}
