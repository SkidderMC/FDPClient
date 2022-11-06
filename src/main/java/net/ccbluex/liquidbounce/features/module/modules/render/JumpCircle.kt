package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.render.Render
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "JumpCircle",  category = ModuleCategory.RENDER)
class JumpCircle : Module() {
    var  r = IntegerValue("Red",255,0,255)
    var  g = IntegerValue("green",255,0,255)
    var  b = IntegerValue("blue",255,0,255)
    private val astolfoRainbowOffset = IntegerValue("AstolfoOffset", 5, 1, 20)
    private val astolfoRainbowIndex = IntegerValue("AstolfoIndex", 109, 1, 300)
    /*   var  radius = FloatValue("radius",0.5f,0.1f,10f)
       var  b = IntegerValue("blue",255,0,255)*/

    private val points = mutableMapOf<Int, MutableList<Render>>()
    var jump=false;
    var entityjump=false;

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        points.forEach {
            for (point in it.value) {
                point.draw()
                if (point.alpha < 0F) {
                    it.value.remove(point)
                }
            }
        }
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround && !jump) {
            jump = true
        }
        if (mc.thePlayer.onGround && jump) {
            updatePoints(mc.thePlayer);
            jump = false
        }
    }

    fun updatePoints(entity: EntityLivingBase) {
        val counter = intArrayOf(0)
        (points[entity.entityId] ?: mutableListOf<Render>().also { points[entity.entityId] = it }).add(Render(entity.posX, entity.entityBoundingBox.minY, entity.posZ,System.currentTimeMillis(),
            Colors.astolfoRainbow(counter[0] * 100, astolfoRainbowOffset.get(), astolfoRainbowIndex.get())))
        counter[0] = counter[0] + 1
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        points.clear()
    }

    override fun onDisable() {
        points.clear()
    }
}