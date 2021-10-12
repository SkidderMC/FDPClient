/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PathUtils
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "InfiniteAura", category = ModuleCategory.COMBAT)
class InfiniteAura : Module() {
    private val modeValue=ListValue("Mode", arrayOf("Aura","Click"),"Aura")
    private val targetsValue=IntegerValue("Targets",3,1,10).displayable { modeValue.equals("Aura") }
    private val cpsValue=IntegerValue("CPS",1,1,10)
    private val distValue=IntegerValue("Distance",30,20,100)
    private val moveDistanceValue=FloatValue("MoveDistance",5F,2F,15F)
    private val noRegen=BoolValue("NoRegen",true)
    private val tpBack=BoolValue("TPBack",true)
    private val doSwing=BoolValue("Swing",true).displayable { modeValue.equals("Aura") }
    //private val AutoBlock=BoolValue("AutoBlock",true).displayable { modeValue.equals("Aura") }
    private val voidCheck=BoolValue("IgnoreInVoid",true)
    private val path=BoolValue("PathRender",true)

    private val timer=MSTimer()
    private var points=ArrayList<Vec3>()
    private var thread: Thread? = null
    var blockingStatus = false

    private fun getDelay():Int{
        return 1000/cpsValue.get()
    }

    override fun onEnable() {
        timer.reset()
        points.clear()
    }

    override fun onDisable() {
        //stopBlocking()
        timer.reset()
        points.clear()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(!timer.hasTimePassed(getDelay().toLong())) return
        when(modeValue.get().lowercase()){
            "aura" -> {
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

            "click" -> {
                if(mc.gameSettings.keyBindAttack.isKeyDown&&(thread == null || !thread!!.isAlive)) {
                    thread = Thread {
                        // do it async because a* pathfinding need some time
                        val entity=RaycastUtils.raycastEntity(distValue.get().toDouble()) { entity -> entity != null && EntityUtils.isSelected(entity, true) } ?: return@Thread
                        if(mc.thePlayer.getDistanceToEntity(entity)<3)
                            return@Thread

                        hit(entity as EntityLivingBase)
                    }
                    timer.reset()
                    thread!!.start()
                }
                points.clear()
            }
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
        //stopBlocking()
        if(targets.size==0) return
        //startBlocking()
        targets.sortBy { mc.thePlayer.getDistanceToEntity(it) }
        var count=0
        val playerPos=Vec3(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ)

        points.add(playerPos)
        for(entity in targets){
            count++
            if(count>targetsValue.get()) break

            hit(entity)
        }
    }
    
    /*
    private fun startBlocking() {

        if(blockingStatus)
            return

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
    }

    
    private fun stopBlocking() {
        if (blockingStatus) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, if(MovementUtils.isMoving()) BlockPos(-1,-1,-1) else BlockPos.ORIGIN, EnumFacing.DOWN))
            blockingStatus = false
        }
    }
    */
    
    private fun isVoid(entity: Entity): Boolean {
        if (entity.posY < 0.0) {
            return true
        }
        var off = 0
        while (off < entity.posY.toInt() + 2) {
            val bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox.offset(entity.posX.toDouble(), (-off).toDouble(), entity.posZ.toDouble())
            if (mc.theWorld!!.getCollidingBoundingBoxes(entity as Entity, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
            off += 2
        }
        return true
    }

    private fun hit(entity: EntityLivingBase){
        if(isVoid(entity) && voidCheck.get())
            return;

        //startBlocking()

        val path= PathUtils.findBlinkPath(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,entity.posX,entity.posY,entity.posZ,moveDistanceValue.get().toDouble())

        path.forEach {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(it.xCoord,it.yCoord,it.zCoord,true))
            points.add(it)
        }

//            val it=Vec3(entity.posX,entity.posY,entity.posZ)
//            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(it.xCoord,it.yCoord,it.zCoord,true))
//            points.add(it)

        if(doSwing.get())
        {
            mc.thePlayer.swingItem()
        }
        else
        {
            mc.netHandler.addToSendQueue(C0APacketAnimation())
        }
        mc.playerController.attackEntity(mc.thePlayer,entity)
        if(tpBack.get()){
        for(i in path.size-1 downTo 0){
            val vec=path[i]
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vec.xCoord,vec.yCoord,vec.zCoord,true))
        }
        }else{
        	mc.thePlayer.setPositionAndUpdate(path[path.size].xCoord, path[path.size].yCoord, path[path.size].zCoord);
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S08PacketPlayerPosLook){
            timer.reset()
        }
        val isMovePacket=(event.packet is C04PacketPlayerPosition||event.packet is C03PacketPlayer.C06PacketPlayerPosLook)
        if(noRegen.get()&&event.packet is C03PacketPlayer&&!isMovePacket){
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        synchronized(points) {
            if(points.isEmpty()||!path.get()) return
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            GL11.glPushMatrix()
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glShadeModel(GL11.GL_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDepthMask(false)
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)

            for (vec in points){
                val x = vec.xCoord - renderPosX
                val y = vec.yCoord - renderPosY
                val z = vec.zCoord - renderPosZ
                val width = 0.3
                val height = mc.thePlayer.getEyeHeight().toDouble()
                GL11.glLoadIdentity()
                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2)
                RenderUtils.glColor(Color.WHITE)
                GL11.glLineWidth(2F)
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y + height, z - width)
                GL11.glVertex3d(x + width, y + height, z - width)
                GL11.glVertex3d(x + width, y, z - width)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y, z + width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x + width, y, z + width)
                GL11.glVertex3d(x + width, y + height, z + width)
                GL11.glVertex3d(x - width, y + height, z + width)
                GL11.glVertex3d(x - width, y, z + width)
                GL11.glVertex3d(x + width, y, z + width)
                GL11.glVertex3d(x + width, y, z - width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x + width, y + height, z + width)
                GL11.glVertex3d(x + width, y + height, z - width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x - width, y + height, z + width)
                GL11.glVertex3d(x - width, y + height, z - width)
                GL11.glEnd()
            }


            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPopMatrix()
            GL11.glColor4f(1F, 1F, 1F, 1F)
        }
    }

    override val tag: String
        get() = modeValue.get()
}
