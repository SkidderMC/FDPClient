package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PathUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "InfinityAura", description = "lol", category = ModuleCategory.COMBAT)
class InfinityAura : Module() {
    private val targetsValue=IntegerValue("Targets",3,1,10)
    private val cpsValue=IntegerValue("CPS",1,1,10)
    private val distValue=IntegerValue("Distance",30,20,100)

    private val timer=MSTimer()
    private var points=ArrayList<Vec3>()

    private fun getDelay():Int{
        return 1000/cpsValue.get()
    }

    override fun onEnable() {
        timer.reset()
        points.clear()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(!timer.hasTimePassed(getDelay().toLong())) return
        timer.reset()

        points.clear()
        val targets=ArrayList<EntityLivingBase>()
        for(entity in mc.theWorld.loadedEntityList){
            if(entity is EntityLivingBase&&EntityUtils.isSelected(entity,true)
                &&mc.thePlayer.getDistanceToEntity(entity)<distValue.get()){
                targets.add(entity)
            }
        }
        if(targets.size==0) return
        targets.sortBy { mc.thePlayer.getDistanceToEntity(it) }
        var count=0
        val playerPos=Vec3(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ)
        for(entity in targets){
            count++
            if(count>targetsValue.get()) break

            val path=PathUtils.findBlinkPath(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,entity.posX,entity.posY,entity.posZ)
            val passedPos=ArrayList<Vec3>()

            points.add(playerPos)
            path.forEach {
                val f = mc.thePlayer.width / 2.0F;
                val f1 = mc.thePlayer.height;
                if(!mc.theWorld.checkBlockCollision(AxisAlignedBB(it.xCoord - f, it.yCoord, it.zCoord - f, it.xCoord + f, it.yCoord + f1, it.zCoord + f))){
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(it.xCoord,it.yCoord,it.zCoord,true))
                    points.add(it)
                }else{
                    passedPos.add(it)
                }
            }
            mc.thePlayer.swingItem()
            mc.netHandler.addToSendQueue(C02PacketUseEntity(entity,C02PacketUseEntity.Action.ATTACK))
            for(i in path.size-1 downTo 0){
                val it=path[i]
                if(!passedPos.contains(it)){
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(it.xCoord,it.yCoord,it.zCoord,true))
                    points.add(it)
                }
            }
            points.add(playerPos)
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        synchronized(points) {
            if(points.isEmpty()) return

            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            RenderUtils.glColor(Color.WHITE)
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ
            for (pos in points) GL11.glVertex3d(
                pos.xCoord - renderPosX,
                pos.yCoord - renderPosY,
                pos.zCoord - renderPosZ
            )
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }
}