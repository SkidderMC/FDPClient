package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.FloatValue
import net.ccbluex.liquidbounce.features.IntegerValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.path.PathUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "InfinityAura", description = "lol", category = ModuleCategory.COMBAT)
class InfinityAura : Module() {
    private val targetsValue= IntegerValue("Targets",3,1,10)
    private val cpsValue= IntegerValue("CPS",1,1,10)
    private val distValue= IntegerValue("Distance",30,20,100)
    private val moveDistValue= FloatValue("MoveDist",1F,0.3F,5F)
    private val antiFlag= BoolValue("AntiFlag",true)
    private val noRegen= BoolValue("NoRegen",true)

    private val timer=MSTimer()
    private var points=ArrayList<Vec3>()
    private var needAntiTP=false
    private var lastPos:Vec3?=null
    private var thread: Thread? = null

    private fun getDelay():Int{
        return 1000/cpsValue.get()
    }

    override fun onEnable() {
        timer.reset()
        points.clear()
        needAntiTP=false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(needAntiTP&&lastPos!=null){
            val path=PathUtils.findBlinkPath(lastPos!!.xCoord,lastPos!!.yCoord,lastPos!!.zCoord,moveDistValue.get().toDouble())
            path.forEach {
                val f = mc.thePlayer.width / 2.0F
                val f1 = mc.thePlayer.height
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(it.xCoord,it.yCoord,it.zCoord,true))
            }
            mc.thePlayer.setPosition(lastPos!!.xCoord,lastPos!!.yCoord,lastPos!!.zCoord)
            needAntiTP=false
            return
        }

        if(!timer.hasTimePassed(getDelay().toLong())) return

        if(thread == null || !thread!!.isAlive) {
            thread = Thread {
                // do it async because a* pathfinding need some time
                doTpAura()
            }
            points.clear()
            timer.reset()
            thread!!.start()
        }else{
            timer.reset()
        }
    }

    private fun doTpAura(){
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

        points.add(playerPos)
        for(entity in targets){
            count++
            if(count>targetsValue.get()) break

            val path=PathUtils.findBlinkPath(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,entity.posX,entity.posY,entity.posZ,distValue.get().toDouble())

            path.forEach {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(it.xCoord,it.yCoord,it.zCoord,true))
                points.add(it)
            }

            mc.thePlayer.swingItem()
            mc.playerController.attackEntity(mc.thePlayer,entity)

            for(i in path.size-1 downTo 0){
                val it=path[i]
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(it.xCoord,it.yCoord,it.zCoord,true))
                points.add(it)
            }
        }

        lastPos=playerPos
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(antiFlag.get()&&event.packet is S08PacketPlayerPosLook){
            val packet=event.packet
            if(mc.thePlayer.getDistance(packet.x,packet.y,packet.z)<distValue.get()){
                needAntiTP=true
                event.cancelEvent()
            }
        }
        val isMovePacket=(event.packet is C04PacketPlayerPosition||event.packet is C03PacketPlayer.C06PacketPlayerPosLook)
        if(needAntiTP&&isMovePacket){
            event.cancelEvent()
        }
        if(noRegen.get()&&event.packet is C03PacketPlayer&&!isMovePacket){
            event.cancelEvent()
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