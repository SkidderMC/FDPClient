package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S18PacketEntityTeleport
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@ModuleInfo(name = "HackerDetector", description = "Detect SIGMA Hackers.", category = ModuleCategory.PLAYER)
class HackerDetector : Module() {
    private val GRAVITY_FRICTION = 0.9800000190734863

    private val combatCheck= BoolValue("Combat",true)
    private val movementCheck= BoolValue("Movement",true)
    private val debugMode= BoolValue("Debug",false)
    private val notify= BoolValue("Notify",true)
    private val report= BoolValue("AutoReport",true)
    private val vlValue= IntegerValue("VL",300,100,500)

    private val datas=HashMap<EntityPlayer,HackerData>()
    private val hackers=ArrayList<String>()

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        //this takes a bit time so we do it async
        Thread { doGC() }.start()
    }

    private fun doGC(){
        val needRemove=ArrayList<EntityPlayer>()
        for((player,_) in datas){
            if(player.isDead){
                needRemove.add(player)
            }
        }
        for(player in needRemove){
            datas.remove(player)
            if(debugMode.get()){
                chat("[GC] REMOVE ${player.name}")
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S19PacketEntityStatus){
            val packet=event.packet
            if(combatCheck.get()&&packet.opCode.toInt()==2){
                Thread { checkCombatHurt(packet.getEntity(mc.theWorld)) }.start()
            }
        }else if(event.packet is S0BPacketAnimation){
            val packet=event.packet
            val entity=mc.theWorld.getEntityByID(packet.entityID)
            if(entity !is EntityPlayer||packet.animationType!=0) return
            val data=datas[entity] ?: return
            data.tempAps++
        }else if(movementCheck.get()){
            if(event.packet is S18PacketEntityTeleport){
                val packet=event.packet
                val entity=mc.theWorld.getEntityByID(packet.entityId)
                if(entity !is EntityPlayer) return
                Thread{ checkPlayer(entity) }.start()
            }else if(event.packet is S14PacketEntity){
                val packet=event.packet
                val entity=packet.getEntity(mc.theWorld)
                if(entity !is EntityPlayer) return
                Thread{ checkPlayer(entity) }.start()
            }
        }
    }

    override fun onEnable() {
        datas.clear()
        hackers.clear()
    }

    @EventTarget
    fun onWorld(event: WorldEvent){
        datas.clear()
    }

    fun isHacker(entity: EntityLivingBase):Boolean {
        if (entity !is EntityPlayer) return false
        return hackers.contains(entity.name)
    }

    private fun checkPlayer(player: EntityPlayer){
        if(player.equals(mc.thePlayer)||EntityUtils.isFriend(player)) return
        if(datas[player]==null) datas[player] = HackerData(player)
        val data=datas[player] ?: return
        data.update()
        if(data.aliveTicks<20) return

        //settings
        var minAirTicks = 10
        if(player.isPotionActive(Potion.jump)){
            minAirTicks+=player.getActivePotionEffect(Potion.jump).amplifier*3
        }
        val maxMotionY = 0.47 //for strict check u can change this to 0.42
        val maxOffset = 0.07
        var passed=true
        
        if(player.hurtTime>0){
            //velocity
            if (player.hurtResistantTime in 7..11
                && player.prevPosX == player.posX && player.posZ == player.lastTickPosZ
                && !mc.theWorld.checkBlockCollision(player.entityBoundingBox.expand(0.05, 0.0, 0.05))) {
                flag("velocity",50,data,"NO KNOCKBACK")
            }
            if (player.hurtResistantTime in 7..11
                && player.lastTickPosY == player.posY) {
                flag("velocity",50,data,"NO KNOCKBACK")
            }
            return
        }

        //phase
//        if(mc.theWorld.checkBlockCollision(player.entityBoundingBox)){
//            flag("phase",50,data,"COLLIDE")
//            passed=false
//        }

        //killaura from jigsaw
        if (data.aps >= 10) {
            flag("killaura",30,data,"HIGH APS(aps=${data.aps})")
            passed=false
        }
        if (data.aps > 2 && data.aps == data.preAps && data.aps != 0) {
            flag("killaura",30,data,"STRANGE APS(aps=${data.aps})")
            passed=false
        }
        if (abs(player.rotationYaw - player.prevRotationYaw) > 50 && player.swingProgress != 0F
            && data.aps >= 3) {
            flag("killaura",30,data,"YAW RATE(aps=${data.aps},yawRot=${abs(player.rotationYaw - player.prevRotationYaw)})")
            passed=false
        }

        //flight
        if(player.ridingEntity==null&&data.airTicks>(minAirTicks/2)){
            if (abs(data.motionY - data.lastMotionY) < (if(data.airTicks >= 115){1E-3}else{5E-3})){
                flag("fly",20,data,"GLIDE(diff=${abs(data.motionY - data.lastMotionY)})")
                passed=false
            }
            if(data.motionY > maxMotionY){
                flag("fly",20,data,"YAXIS(motY=${data.motionY})")
                passed=false
            }
            if(data.airTicks > minAirTicks&&data.motionY>0){
                flag("fly",30,data,"YAXIS(motY=${data.motionY})")
                passed=false
            }
            //gravity check from ACR
//            val gravitatedY = (data.lastMotionY - 0.08) * GRAVITY_FRICTION
//            val offset = abs(gravitatedY - data.motionY)
//            if (offset > maxOffset) {
//                flag("fly",15,data,"GRAVITY(offset=$offset)")
//                passed=false
//            }
        }

        //speed
        val distanceXZ=abs(data.motionXZ)
        if(data.airTicks==0){ //onGround
            var limit = 0.37
            if(data.groundTicks < 5) limit += 0.1
            if(player.isBlocking) limit *= 0.45
            if(player.isSneaking) limit *= 0.68
            if(player.isPotionActive(Potion.moveSpeed)){ //server will send real player potionData?i hope that
                limit += player.getActivePotionEffect(Potion.moveSpeed).amplifier
                limit *= 1.5
            }
            if (distanceXZ > limit) {
                flag("speed",20,data,"GROUND SPEED(speed=$distanceXZ,limit=$limit)")
            }
        }else{
            val multiplier = 0.985
            var predict = 0.36 * multiplier.pow(data.airTicks + 1)
            if (data.airTicks >= 115)
                predict = 0.08.coerceAtLeast(predict);
            var limit=0.05
            if(player.isPotionActive(Potion.moveSpeed)){
                predict += player.getActivePotionEffect(Potion.moveSpeed).amplifier * 0.05
                limit *= 1.2
            }
            if(player.isPotionActive(Potion.jump)) {
                predict += player.getActivePotionEffect(Potion.jump).amplifier * 0.05
            }
            if(player.isBlocking)
                predict *= 0.7

            if (distanceXZ - predict > limit) {
                flag("speed",20,data,"AIR SPEED(speed=$distanceXZ,limit=$limit,predict=$predict)")
            }
        }
//        if (abs(data.motionX) > 0.42
//            || abs(data.motionZ) > 0.42){
//            flag("speed",30,data,"HIGH SPEED")
//            passed=false
//        }
//        if (player.isBlocking && (abs(data.motionX) > 0.2 || abs(data.motionZ) > 0.2)) {
//            flag("speed",30,data,"HIGH SPEED(BLOCKING)") //blocking is just noslow lol
//            passed=false
//        }

        //reduce vl
        if(passed){
            data.vl-=1
        }
    }

    private fun flag(type: String,vl: Int,data: HackerData,msg: String){
        if(!data.useHacks.contains(type)) data.useHacks.add(type)
        //display debug message
        if(debugMode.get()){
            chat("§f${data.player.name} §euse §2$type §7$msg §c${data.vl}+${vl}")
        }
        data.vl+=vl

        if(data.vl>vlValue.get()){
            var use=""
            for(typ in data.useHacks){
                use+="$typ,"
            }
            use=use.substring(0,use.length-1)
            chat("§f${data.player.name} §eusing hack §a$use")
            if(notify.get()){
                LiquidBounce.hud.addNotification(Notification(name,"${data.player.name} might use hack ($use)",NotifyType.WARNING))
            }
            data.vl=-vlValue.get()

            if(report.get()){
                val autoReportModule=LiquidBounce.moduleManager.getModule(AutoReport::class.java) as AutoReport
                autoReportModule.doReport(data.player)
            }
        }
    }

    private fun checkCombatHurt(entity: Entity){
        if(entity !is EntityLivingBase) return
        var attacker:EntityPlayer?=null
        var attackerCount=0

        for(worldEntity in mc.theWorld.loadedEntityList){
            if(worldEntity !is EntityPlayer||worldEntity.getDistanceToEntity(entity)>7||worldEntity.equals(entity)) continue
            attackerCount++
            attacker=worldEntity
        }

        //multi attacker may cause false result
        if(attackerCount!=1) return
        if(attacker!! == entity||EntityUtils.isFriend(attacker)) return //i and my friend is hacker lol
        val data=datas[attacker] ?: return

        //reach check
        val reach=attacker.getDistanceToEntity(entity)
        if(reach>3.7){
            flag("killaura",70,data,"(reach=$reach)")
        }

        //aim check
        val yawDiff=calculateYawDifference(attacker,entity)
        if(yawDiff>50){
            flag("killaura",100,data,"(yawDiff=$yawDiff)")
        }
    }

    private fun calculateYawDifference(from: EntityLivingBase, to: EntityLivingBase): Double {
        val x = to.posX - from.posX
        val z = to.posZ - from.posZ
        return if (x == 0.0 && z == 0.0) {
            from.rotationYaw.toDouble()
        } else {
            val theta = atan2(-x, z)
            val yaw=Math.toDegrees((theta + 6.283185307179586) % 6.283185307179586)
            abs(180 - abs(abs(yaw - from.rotationYaw) - 180));
        }
    }
}

class HackerData(val player:EntityPlayer){
    var aliveTicks=0
    // Ticks in air
    var airTicks = 0

    // Ticks on ground
    var groundTicks = 0

    // motion of the movement
    var motionX = 0.0
    var motionY = 0.0
    var motionZ = 0.0
    var motionXZ = 0.0

    // Previous motion of the movement
    var lastMotionX = 0.0
    var lastMotionY = 0.0
    var lastMotionZ = 0.0
    var lastMotionXZ = 0.0

    // combat check
    var aps = 0
    var preAps = 0
    var tempAps = 0
    private val apsTimer=MSTimer()

    var vl=0
    var useHacks=ArrayList<String>()

    fun update(){
        aliveTicks++
        if (apsTimer.hasTimePassed(1000)) {
            preAps = aps;
            aps = tempAps;
            tempAps = 0;
        }

        if(calculateGround()){
            groundTicks++
            airTicks=0
        }else{
            airTicks++
            groundTicks=0
        }

        this.lastMotionX = this.motionX
        this.lastMotionY = this.motionY
        this.lastMotionZ = this.motionZ
        this.lastMotionXZ = this.motionXZ
        this.motionX = player.posX-player.prevPosX
        this.motionY = player.posY-player.prevPosY
        this.motionZ = player.posZ-player.prevPosZ
        this.motionXZ = sqrt(motionX*motionX + motionZ*motionZ)
    }

    private fun calculateGround(): Boolean {
        val playerBoundingBox = player.entityBoundingBox
        val blockHeight = 1
        val customBox = AxisAlignedBB(playerBoundingBox.maxX, player.posY-blockHeight, playerBoundingBox.maxZ, playerBoundingBox.minX, player.posY, playerBoundingBox.minZ)
        return Minecraft.getMinecraft().theWorld.checkBlockCollision(customBox)
    }
}
