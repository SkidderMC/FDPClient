/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * http://proxy.liulihaocai.pw/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "LongJump", description = "Allows you to jump further.", category = ModuleCategory.MOVEMENT)
class LongJump : Module() {
    private val modeValue = ListValue("Mode", arrayOf("NCP", "AACv1", "AACv2", "AACv3", "Mineplex", "Mineplex2", "Mineplex3", "RedeSky", "RedeSky2", "RedeSky3"), "NCP")
    private val ncpBoostValue = FloatValue("NCPBoost", 4.25f, 1f, 10f)

    //redesky
    private val rsJumpMovementValue = FloatValue("RedeSkyJumpMovement",0.13F,0.05F,0.25F)
    private val rsMotionYValue = FloatValue("RedeSkyMotionY",0.81F,0.05F,1F)
    private val rsMoveReducerValue = BoolValue("RedeSkyMovementReducer", true)
    private val rsReduceMovementValue = FloatValue("RedeSkyReduceMovement",0.08F,0.05F,0.25F)
    private val rsMotYReducerValue = BoolValue("RedeSkyMotionYReducer", true)
    private val rsReduceYMotionValue = FloatValue("RedeSkyReduceYMotion",0.15F,0.01F,0.20F)
    private val rsUseTimerValue = BoolValue("RedeSkyTimer", true)
    private val rsTimerValue = FloatValue("RedeSkyTimer",0.30F,0.1F,1F)
    //redesky2
    private val rs2AirSpeedValue = FloatValue("RedeSky2AirSpeed",0.1F,0.05F,0.25F)
    private val rs2MinAirSpeedValue = FloatValue("RedeSky2MinAirSpeed",0.08F,0.05F,0.25F)
    private val rs2ReduceAirSpeedValue = FloatValue("RedeSky2ReduceAirSpeed",0.16F,0.05F,0.25F)
    private val rs2AirSpeedReducerValue = BoolValue("RedeSky2AirSpeedReducer", true)
    private val rs2YMotionValue = FloatValue("RedeSky2YMotion",0.08F,0.01F,0.20F)
    private val rs2MinYMotionValue = FloatValue("RedeSky2MinYMotion",0.04F,0.01F,0.20F)
    private val rs2ReduceYMotionValue = FloatValue("RedeSky2ReduceYMotion",0.15F,0.01F,0.20F)
    private val rs2YMotionReducerValue = BoolValue("RedeSky2YMotionReducer", true)
    private val rs3JumpTimeValue=IntegerValue("RedeSky3JumpTime",500,300,1500)
    private val rs3BoostValue=FloatValue("RedeSky3Boost",1F,0.3F,1.5F)
    private val rs3HeightValue=FloatValue("RedeSky3Height",1F,0.3F,1.5F)
    private val autoJumpValue = BoolValue("AutoJump", true)
    private val autoCloseValue = BoolValue("AutoClose", true)
    private var jumped = false
    private var hasJumped=false
    private var canBoost = false
    private var teleported = false
    private var canMineplexBoost = false
    private var timer=MSTimer()

    var airTicks=0

    override fun onEnable() {
        airTicks=0
        hasJumped=false
    }

    override fun onDisable() {
        when(modeValue.get().toLowerCase()){
            "redesky" -> {
                mc.timer.timerSpeed = 1F
            }
            "redesky2" -> {
                mc.thePlayer.speedInAir = 0.02F
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val thePlayer = mc.thePlayer ?: return

        if (jumped) {
            val mode = modeValue.get()

            if(!thePlayer.onGround){
                airTicks++
            }else{
                airTicks=0
            }

            if (thePlayer.onGround || thePlayer.capabilities.isFlying) {
                jumped = false
                canMineplexBoost = false

                if (mode.equals("NCP", ignoreCase = true)) {
                    thePlayer.motionX = 0.0
                    thePlayer.motionZ = 0.0
                }
                return
            }
            run {
                when (mode.toLowerCase()) {
                    "ncp" -> {
                        MovementUtils.strafe(MovementUtils.getSpeed() * if (canBoost) ncpBoostValue.get() else 1f)
                        canBoost = false
                    }
                    "aacv1" -> {
                        thePlayer.motionY += 0.05999
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1.08f)
                    }
                    "aacv2", "mineplex3" -> {
                        thePlayer.jumpMovementFactor = 0.09f
                        thePlayer.motionY += 0.0132099999999999999999999999999
                        thePlayer.jumpMovementFactor = 0.08f
                        MovementUtils.strafe()
                    }
                    "aacv3" -> {
                        if (thePlayer.fallDistance > 0.5f && !teleported) {
                            val value = 3.0
                            var x = 0.0
                            var z = 0.0

                            when (thePlayer.horizontalFacing) {
                                EnumFacing.NORTH -> z = -value
                                EnumFacing.EAST -> x = +value
                                EnumFacing.SOUTH -> z = +value
                                EnumFacing.WEST -> x = -value
                            }

                            thePlayer.setPosition(thePlayer.posX + x, thePlayer.posY, thePlayer.posZ + z)
                            teleported = true
                        }
                    }
                    "mineplex" -> {
                        thePlayer.motionY += 0.0132099999999999999999999999999
                        thePlayer.jumpMovementFactor = 0.08f
                        MovementUtils.strafe()
                    }
                    "mineplex2" -> {
                        if (!canMineplexBoost)
                            return@run

                        thePlayer.jumpMovementFactor = 0.1f
                        if (thePlayer.fallDistance > 1.5f) {
                            thePlayer.jumpMovementFactor = 0f
                            thePlayer.motionY = (-10f).toDouble()
                        }

                        MovementUtils.strafe()
                    }
                    "redesky" -> {
                        if (!mc.thePlayer.onGround) {
                            if (rsMoveReducerValue.get()) {
                                thePlayer.jumpMovementFactor = rsJumpMovementValue.get() -(airTicks*(rsReduceMovementValue.get()/100))
                            } else {
                                thePlayer.jumpMovementFactor = rsJumpMovementValue.get()
                            }
                            if (rsMotYReducerValue.get()){
                                thePlayer.motionY += (rsMotionYValue.get() / 10F)-(airTicks*(rsReduceYMotionValue.get()/100))
                            }else{
                                thePlayer.motionY += rsMotionYValue.get() / 10F
                            }
                            if (rsUseTimerValue.get()) {
                                mc.timer.timerSpeed = rsTimerValue.get()
                            }
                        }
                    }
                    "redesky2" -> {
                        if (!mc.thePlayer.onGround){
                            if(rs2YMotionReducerValue.get()){
                                val motY=rs2YMotionValue.get()-(airTicks*(rs2ReduceYMotionValue.get()/100))
                                if(motY<rs2MinYMotionValue.get()){
                                    mc.thePlayer.motionY += rs2MinYMotionValue.get()
                                }else{
                                    mc.thePlayer.motionY += motY
                                }
                            }else{
                                mc.thePlayer.motionY += rs2YMotionValue.get();
                            }
                            //as reduce
                            if(rs2AirSpeedReducerValue.get()){
                                val airSpeed=rs2AirSpeedValue.get()-(airTicks*(rs2ReduceAirSpeedValue.get()/100))
                                if(airSpeed<rs2MinAirSpeedValue.get()){
                                    mc.thePlayer.speedInAir = rs2MinAirSpeedValue.get()
                                }else{
                                    mc.thePlayer.speedInAir = airSpeed
                                }
                            }else{
                                mc.thePlayer.speedInAir = rs2AirSpeedValue.get();
                            }
                        }
                    }
                    "redesky3" -> {
                        if(!timer.hasTimePassed(rs3JumpTimeValue.get().toLong())){
                            mc.thePlayer.motionY+=rs3HeightValue.get()/10F
                            MovementUtils.move(rs3BoostValue.get()/10F)
                        }
                    }
                }
            }
        }
        if (autoJumpValue.get() && thePlayer.onGround && MovementUtils.isMoving()) {
            jumped = true
            if(hasJumped&&autoCloseValue.get()){
                state=false
                return
            }
            thePlayer.jump()
            hasJumped=true
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return
        val mode = modeValue.get()

        if (mode.equals("mineplex3", ignoreCase = true)) {
            if (thePlayer.fallDistance != 0.0f)
                thePlayer.motionY += 0.037
        } else if (mode.equals("ncp", ignoreCase = true) && !MovementUtils.isMoving() && jumped) {
            thePlayer.motionX = 0.0
            thePlayer.motionZ = 0.0
            event.zeroXZ()
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent) {
        jumped = true
        canBoost = true
        teleported = false

        timer.reset()

        if (state) {
            when (modeValue.get().toLowerCase()) {
                "mineplex" -> event.motion = event.motion * 4.08f
                "mineplex2" -> {
                    if (mc.thePlayer!!.isCollidedHorizontally) {
                        event.motion = 2.31f
                        canMineplexBoost = true
                        mc.thePlayer!!.onGround = false
                    }
                }
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}