/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraft.world.WorldSettings
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@ModuleInfo(name = "KillAura", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {

    /**
     * OPTIONS
     */

    // CPS - Attack speed
    private val maxCPS: IntegerValue = object : IntegerValue("MaxCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCPS.get()
            if (i > newValue) set(i)

            attackDelay = getAttackDelay(minCPS.get(), this.get())
        }
    }

    private val minCPS: IntegerValue = object : IntegerValue("MinCPS", 5, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCPS.get()
            if (i < newValue) set(i)

            attackDelay = getAttackDelay(this.get(), maxCPS.get())
        }
    }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val combatDelayValue = BoolValue("1.9CombatDelay", false)

    // Range
    val rangeValue = object : FloatValue("Range", 3.7f, 1f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
        }
    }
    private val throughWallsRangeValue = object : FloatValue("ThroughWallsRange", 1.5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = rangeValue.get()
            if (i < newValue) set(i)
        }
    }
    private val discoverRangeValue = FloatValue("DiscoverRange", 6f, 0f, 15f)
    private val rangeSprintReducementValue = FloatValue("RangeSprintReducement", 0f, 0f, 0.4f)

    // Modes
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Direction", "LivingTime", "Armor"), "Distance")
    private val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Single")

    // Bypass
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val keepSprintValue = BoolValue("KeepSprint", true)

    // AutoBlock
    val autoBlockValue = ListValue("AutoBlock", arrayOf("Range", "Fake", "Off"),"Off")
    private val autoBlockRangeValue = object : FloatValue("AutoBlockRange", 2.5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { !autoBlockValue.equals("Off") }
    private val autoBlockPacketValue = ListValue("AutoBlockPacket", arrayOf("AfterTick", "AfterAttack", "Vanilla"),"AfterTick").displayable { !autoBlockValue.equals("Off") }
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", true).displayable { !autoBlockValue.equals("Off") }
    private val blockRate = IntegerValue("BlockRate", 100, 1, 100).displayable { !autoBlockValue.equals("Off") }

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)
    private val raycastIgnoredValue = BoolValue("RayCastIgnored", false).displayable { raycastValue.get() }
    private val livingRaycastValue = BoolValue("LivingRayCast", true).displayable { raycastValue.get() }

    // Bypass
    private val aacValue = BoolValue("AAC", true)
    private val aacPitchValue = IntegerValue("AAC-Pitch", 15, 0, 90)

    // Turn Speed
    private val maxTurnSpeed: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeed.get()
            if (v > newValue) set(v)
        }
    }

    private val minTurnSpeed: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeed.get()
            if (v < newValue) set(v)
        }
    }

    private val silentRotationValue = BoolValue("SilentRotation", true).displayable { maxTurnSpeed.get()>0f }
    private val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Slient").displayable { silentRotationValue.get() && maxTurnSpeed.get()>0f }
    private val strafeOnlyGroundValue = BoolValue("StrafeOnlyGround",true).displayable { rotationStrafeValue.displayable && !rotationStrafeValue.equals("Off") }
    private val randomCenterValue = BoolValue("RandomCenter", false).displayable { maxTurnSpeed.get()>0f }
    private val outborderValue = BoolValue("Outborder", false).displayable { maxTurnSpeed.get()>0f }
    private val hitableValue = BoolValue("AlwaysHitable",true).displayable { maxTurnSpeed.get()>0f }
    private val fovValue = FloatValue("FOV", 180f, 0f, 180f)

    // Predict
    private val predictValue = BoolValue("Predict", true).displayable { maxTurnSpeed.get()>0f }

    private val maxPredictSize: FloatValue = object : FloatValue("MaxPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSize.get()
            if (v > newValue) set(v)
        }
    }.displayable { predictValue.displayable && predictValue.get() } as FloatValue

    private val minPredictSize: FloatValue = object : FloatValue("MinPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSize.get()
            if (v < newValue) set(v)
        }
    }.displayable { predictValue.displayable && predictValue.get() } as FloatValue

    // Bypass
    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f)
    private val fakeSwingValue = BoolValue("FakeSwing", true).displayable { failRateValue.get()!=0f }
    private val noInventoryAttackValue = BoolValue("NoInvAttack", false)
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500)
    private val switchDelayValue = IntegerValue("SwitchDelay",300 ,1, 2000).displayable { targetModeValue.equals("Switch") }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50).displayable { targetModeValue.equals("Multi") }

    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Liquid","FDP","Block","Jello","None"),"FDP")
    private val fakeSharpValue = BoolValue("FakeSharp", true)
    private val circleValue=BoolValue("Circle",true)
    private val circleRed = IntegerValue("CircleRed", 255, 0, 255).displayable { circleValue.get() }
    private val circleGreen = IntegerValue("CircleGreen", 255, 0, 255).displayable { circleValue.get() }
    private val circleBlue = IntegerValue("CircleBlue", 255, 0, 255).displayable { circleValue.get() }
    private val circleAlpha = IntegerValue("CircleAlpha", 255, 0, 255).displayable { circleValue.get() }
    private val circleAccuracy = IntegerValue("CircleAccuracy", 15, 0, 60).displayable { circleValue.get() }

    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    private var currentTarget: EntityLivingBase? = null
    private var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    private val inRangeDiscoveredTargets = mutableListOf<EntityLivingBase>()

    // Attack delay
    private val attackTimer = MSTimer()
    private val switchTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Container Delay
    private var containerOpen = -1L

    // Fake block status
    var blockingStatus = false

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        updateTarget()
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        target = null
        currentTarget = null
        hitable = false
        prevTargetEntities.clear()
        attackTimer.reset()
        clicks = 0

        stopBlocking()
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.isRiding)
            return

        if (event.eventState == EventState.POST) {
            // AutoBlock
            if (!autoBlockValue.equals("off") && discoveredTargets.isNotEmpty() && (!autoBlockPacketValue.equals("AfterAttack")||discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it)>maxRange }.isNotEmpty()) && canBlock) {
                val target=discoveredTargets[0]
                if(mc.thePlayer.getDistanceToEntityBox(target) < autoBlockRangeValue.get())
                    startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target)<maxRange))
            }
            
            target ?: return
            currentTarget ?: return

            // Update hitable
            updateHitable()

            return
        }

        if (rotationStrafeValue.equals("Off"))
            update()

        if (target != null && currentTarget != null) {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        }
    }

    /**
     * Strafe event
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (rotationStrafeValue.equals("Off") && !mc.thePlayer.isRiding)
            return

        update()

        if(strafeOnlyGroundValue.get()&&!mc.thePlayer.onGround)
            return

        if (discoveredTargets.isNotEmpty() && RotationUtils.targetRotation != null) {
            when (rotationStrafeValue.get().lowercase()) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt_float(f)

                        if (f < 1.0F)
                            f = 1.0F

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                    event.cancelEvent()
                }
                "silent" -> {
                    update()

                    RotationUtils.targetRotation.applyStrafeToPlayer(event)
                    event.cancelEvent()
                }
            }
        }
    }

    fun update() {
        if (cancelRun || (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())))
            return

        // Update target
        updateTarget()

        if (discoveredTargets.isEmpty()) {
            stopBlocking()
            return
        }

        // Target
        currentTarget = target

        if (!targetModeValue.equals("Switch") && EntityUtils.isSelected(currentTarget, true))
            target = currentTarget
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
            return
        }

        if (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (!rotationStrafeValue.equals("Off") && !mc.thePlayer.isRiding)
            return

        if (mc.thePlayer.isRiding)
            update()

        if (target != null && currentTarget != null) {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circleValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(1F)
            GL11.glColor4f(circleRed.get().toFloat() / 255.0F, circleGreen.get().toFloat() / 255.0F, circleBlue.get().toFloat() / 255.0F, circleAlpha.get().toFloat() / 255.0F)
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 61 - circleAccuracy.get()) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(cos(i * Math.PI / 180.0).toFloat() * rangeValue.get(), (sin(i * Math.PI / 180.0).toFloat() * rangeValue.get()))
            }
            GL11.glVertex2f(cos(360 * Math.PI / 180.0).toFloat() * rangeValue.get(), (sin(360 * Math.PI / 180.0).toFloat() * rangeValue.get()))

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
        }
        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) && currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = getAttackDelay(minCPS.get(), maxCPS.get())
        }

        when(markValue.get().lowercase()){
            "liquid" -> {
                discoveredTargets.forEach {
                    RenderUtils.drawPlatform(it, if (it.hurtTime<=0) Color(37, 126, 255, 170) else Color(255, 0, 0, 170))
                }
            }
            "block" -> {
                discoveredTargets.forEach {
                    val bb=it.entityBoundingBox
                    it.entityBoundingBox=bb.expand(0.2,0.2,0.2)
                    RenderUtils.drawEntityBox(it, if (it.hurtTime<=0) Color.GREEN else Color.RED, true, true, 4f)
                    it.entityBoundingBox=bb
                }
            }
            "fdp" -> {
                val drawTime = (System.currentTimeMillis() % 1500).toInt()
                val drawMode=drawTime>750
                var drawPercent=drawTime/750.0
                //true when goes up
                if(!drawMode){
                    drawPercent=1-drawPercent
                }else{
                    drawPercent-=1
                }
                drawPercent=EaseUtils.easeInOutQuad(drawPercent)
                discoveredTargets.forEach {
                    GL11.glPushMatrix()
                    GL11.glDisable(3553)
                    GL11.glEnable(2848)
                    GL11.glEnable(2881)
                    GL11.glEnable(2832)
                    GL11.glEnable(3042)
                    GL11.glBlendFunc(770, 771)
                    GL11.glHint(3154, 4354)
                    GL11.glHint(3155, 4354)
                    GL11.glHint(3153, 4354)
                    GL11.glDisable(2929)
                    GL11.glDepthMask(false)

                    val bb=it.entityBoundingBox
                    val radius=(bb.maxX-bb.minX)+0.3
                    val height=bb.maxY-bb.minY
                    val x = it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                    val y = (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
                    val z = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    GL11.glLineWidth((radius*5f).toFloat())
                    GL11.glBegin(3)
                    for (i in 0..360) {
                        val rainbow = Color(Color.HSBtoRGB((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)).toFloat() % 1.0f, 0.7f, 1.0f))
                        GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                        GL11.glVertex3d(x + radius * cos(i * 6.283185307179586 / 45.0), y, z + radius * sin(i * 6.283185307179586 / 45.0))
                    }
                    GL11.glEnd()

                    GL11.glDepthMask(true)
                    GL11.glEnable(2929)
                    GL11.glDisable(2848)
                    GL11.glDisable(2881)
                    GL11.glEnable(2832)
                    GL11.glEnable(3553)
                    GL11.glPopMatrix()
                }
            }
            "jello" -> {
                discoveredTargets.forEach {
                    val drawTime = (System.currentTimeMillis() % 2000).toInt()
                    val drawMode=drawTime>1000
                    var drawPercent=drawTime/1000.0
                    //true when goes up
                    if(!drawMode){
                        drawPercent=1-drawPercent
                    }else{
                        drawPercent-=1
                    }
                    drawPercent=EaseUtils.easeInOutQuad(drawPercent)
                    val points = mutableListOf<Vec3>()
                    val bb=it.entityBoundingBox
                    val radius=bb.maxX-bb.minX
                    val height=bb.maxY-bb.minY
                    val posX = it.lastTickPosX + (it.posX - it.lastTickPosX) * mc.timer.renderPartialTicks
                    var posY = it.lastTickPosY + (it.posY - it.lastTickPosY) * mc.timer.renderPartialTicks
                    if(drawMode){
                        posY-=0.5
                    }else{
                        posY+=0.5
                    }
                    val posZ = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * mc.timer.renderPartialTicks
                    for(i in 0..360 step 7){
                        points.add(Vec3(posX - sin(i * Math.PI / 180F) * radius,posY+height*drawPercent,posZ + cos(i * Math.PI / 180F) * radius))
                    }
                    points.add(points[0])
                    //draw
                    mc.entityRenderer.disableLightmap()
                    GL11.glPushMatrix()
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glBegin(GL11.GL_LINE_STRIP)
                    val baseMove=(if(drawPercent>0.5){1-drawPercent}else{drawPercent})*2
                    val min=(height/60)*20*(1-baseMove)*(if(drawMode){-1}else{1})
                    for(i in 0..20) {
                        var moveFace=(height/60F)*i*baseMove
                        if(drawMode){
                            moveFace=-moveFace
                        }
                        val firstPoint=points[0]
                        GL11.glVertex3d(
                            firstPoint.xCoord - mc.renderManager.viewerPosX, firstPoint.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                            firstPoint.zCoord - mc.renderManager.viewerPosZ
                        )
                        GL11.glColor4f(1F, 1F, 1F, 0.7F*(i/20F))
                        for (vec3 in points) {
                            GL11.glVertex3d(
                                vec3.xCoord - mc.renderManager.viewerPosX, vec3.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                                vec3.zCoord - mc.renderManager.viewerPosZ
                            )
                        }
                        GL11.glColor4f(0F,0F,0F,0F)
                    }
                    GL11.glEnd()
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                    GL11.glDisable(GL11.GL_BLEND)
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glPopMatrix()
                }
            }
//            "route" -> {
//                if (espAnimation > target!!.eyeHeight + 0.4 || espAnimation < 0) {
//                    isUp = !isUp
//                }
//                if (isUp) {
//                    espAnimation += 0.05 * 60 / Minecraft.getDebugFPS()
//                } else {
//                    espAnimation -= 0.05 * 60 / Minecraft.getDebugFPS()
//                }
//                if (isUp) {
//                    esp(target!!, event.partialTicks, RouteRadiusValue.get())
//                } else {
//                    esp(target!!, event.partialTicks, RouteRadiusValue.get())
//                }
//            }
        }
    }

    /**
     * Handle entity move
     */
//    @EventTarget
//    fun onEntityMove(event: EntityMovementEvent) {
//        val movedEntity = event.movedEntity
//
//        if (target == null || movedEntity != currentTarget)
//            return
//
//        updateHitable()
//    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        target ?: return
        currentTarget ?: return

        // Settings
        val failRate = failRateValue.get()
        val swing = swingValue.get()
        val openInventory = aacValue.get() && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Close inventory when open
        if (openInventory)
            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

        // Check is not hitable or check failrate
        if (!hitable || failHit) {
            if (!swing.equals("none",true) && (fakeSwingValue.get() || failHit)) {
                if(swing.equals("packet",true)){
                    mc.netHandler.addToSendQueue(C0APacketAnimation())
                }else{
                    mc.thePlayer.swingItem()
                }
            }
        } else {
            // Attack
            if (!targetModeValue.equals("Multi")) {
                attackEntity(currentTarget!!)
            } else {
                inRangeDiscoveredTargets.forEachIndexed { index, entity ->
                    if(limitedMultiTargetsValue.get()==0 || index<limitedMultiTargetsValue.get())
                        attackEntity(entity)
                }
            }

            if(targetModeValue.equals("Switch")){
                if(switchTimer.hasTimePassed(switchDelayValue.get().toLong())){
                    prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
                    switchTimer.reset()
                }
            }else{
                prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
            }

            if (target == currentTarget)
                target = null
        }

        // Open inventory
        if (openInventory)
            mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Reset fixed target to null
        target = null

        // Settings
        val hurtTime = hurtTimeValue.get()
        val fov = fovValue.get()
        val switchMode = targetModeValue.equals("Switch")

        // Find possible targets
        discoveredTargets.clear()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !EntityUtils.isSelected(entity, true) || (switchMode && prevTargetEntities.contains(entity.entityId)))
                continue

            val distance = mc.thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= discoverRangeValue.get() && (fov == 180F || entityFov <= fov) && entity.hurtTime <= hurtTime)
                discoveredTargets.add(entity)
        }

        // Sort targets by priority
        when (priorityValue.get().lowercase()) {
            "distance" -> discoveredTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> discoveredTargets.sortBy { it.health } // Sort by health
            "direction" -> discoveredTargets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> discoveredTargets.sortBy { -it.ticksExisted } // Sort by existence
            "armor" -> discoveredTargets.sortBy { it.totalArmorValue } // Sort by armor
        }

        inRangeDiscoveredTargets.clear()
        inRangeDiscoveredTargets.addAll(discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it)<getRange(it) })

        // Cleanup last targets when no targets found and try again
        if (inRangeDiscoveredTargets.isEmpty()&&prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
            return
        }

        // Find best target
        for (entity in discoveredTargets) {
            // Update rotations to current target
            if (!updateRotations(entity)) // when failed then try another target
                continue

            // Set target to current entity
            if(mc.thePlayer.getDistanceToEntityBox(entity) < maxRange)
                target = entity
            
            return
        }
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: EntityLivingBase) {
        // Call attack event
        val event=AttackEvent(entity)
        LiquidBounce.eventManager.callEvent(event)
        if(event.isCancelled)
            return

        // Stop blocking
        if (!autoBlockPacketValue.equals("Vanilla")&&(mc.thePlayer.isBlocking || blockingStatus)) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            blockingStatus = false
        }

        // Attack target
        val swing=swingValue.get()
        if(swing.equals("packet",true)){
            mc.netHandler.addToSendQueue(C0APacketAnimation())
        }else if(swing.equals("normal",true)){
            mc.thePlayer.swingItem()
        }

        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

        if (keepSprintValue.get()) {
            // Critical Effect
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder &&
                !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && !mc.thePlayer.isRiding)
                mc.thePlayer.onCriticalHit(entity)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F)
                mc.thePlayer.onEnchantmentCritical(entity)
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
                mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
        }

        // Extra critical effects
        val criticals = LiquidBounce.moduleManager[Criticals::class.java]

        for (i in 0..2) {
            // Critical Effect
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(criticals.delayValue.get().toLong()) && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava && !mc.thePlayer.isInWeb)
                mc.thePlayer.onCriticalHit(target)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, target!!.creatureAttribute) > 0.0f || fakeSharpValue.get())
                mc.thePlayer.onEnchantmentCritical(target)
        }

        // Start blocking after attack
        if (mc.thePlayer.isBlocking || (!autoBlockValue.equals("off") && canBlock)) {
            if(autoBlockPacketValue.equals("AfterTick"))
                return

            if (!(blockRate.get() > 0 && Random().nextInt(100) <= blockRate.get()))
                return

            startBlocking(entity, interactAutoBlockValue.get())
        }
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        if(maxTurnSpeed.get() <= 0F)
            return true

        var boundingBox = entity.entityBoundingBox

        if (predictValue.get())
            boundingBox = boundingBox.offset(
                (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
            )

        val (_, rotation) = RotationUtils.searchCenter(
            boundingBox,
            outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
            randomCenterValue.get(),
            predictValue.get(),
            mc.thePlayer.getDistanceToEntityBox(entity) < throughWallsRangeValue.get()
        ) ?: return false

        val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, rotation,
            (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

        if (silentRotationValue.get())
            RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 90 - aacPitchValue.get() else 0)
        else
            limitedRotation.toPlayer(mc.thePlayer)

        return true
    }

    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        if(hitableValue.get()){
            hitable = true
            return
        }
        // Disable hitable check if turn speed is zero
        if(maxTurnSpeed.get() <= 0F) {
            hitable = true
            return
        }

        val reach = maxRange.toDouble()

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach) {
                (!livingRaycastValue.get() || it is EntityLivingBase && it !is EntityArmorStand) &&
                        (EntityUtils.isSelected(it, true) || raycastIgnoredValue.get() || aacValue.get() && mc.theWorld.getEntitiesWithinAABBExcludingEntity(it, it.entityBoundingBox).isNotEmpty())
            }

            if (raycastValue.get() && raycastedEntity is EntityLivingBase
                && !EntityUtils.isFriend(raycastedEntity))
                currentTarget = raycastedEntity

            hitable = if(maxTurnSpeed.get() > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget, reach)
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if(autoBlockValue.equals("range") && mc.thePlayer.getDistanceToEntityBox(interactEntity)>autoBlockRangeValue.get())
            return

        if(blockingStatus)
            return

        if (interact) {
            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, interactEntity.positionVector))
            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, C02PacketUseEntity.Action.INTERACT))
        }

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
    }


    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            blockingStatus = false
        }
    }

    /**
     * Attack Delay
     */
    private fun getAttackDelay(minCps: Int, maxCps: Int):Long{
        var delay=TimeUtils.randomClickDelay(minCps.coerceAtMost(maxCps), minCps.coerceAtLeast(maxCps))
        if(combatDelayValue.get()){
            var value=4.0
            if(mc.thePlayer.inventory.getCurrentItem()!=null){
                val currentItem=mc.thePlayer.inventory.getCurrentItem().item
                if(currentItem is ItemSword){
                    value-=2.4
                }else if(currentItem is ItemPickaxe){
                    value-=2.8
                }else if(currentItem is ItemAxe){
                    value-=3
                }
            }
            delay=delay.coerceAtLeast((1000 / value).toLong())
        }
        return delay
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0 ||
            aacValue.get() && entity.hurtTime > 3

    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    /**
     * Range
     */
    private val maxRange: Float
        get() = max(rangeValue.get(), throughWallsRangeValue.get())

    private fun getRange(entity: Entity) =
        (if (mc.thePlayer.getDistanceToEntityBox(entity) >= throughWallsRangeValue.get()) rangeValue.get() else throughWallsRangeValue.get()) - if (mc.thePlayer.isSprinting) rangeSprintReducementValue.get() else 0F

    /**
     * HUD Tag
     */
    override val tag: String
        get() = "${minCPS.get()}-${maxCPS.get()}, " +
                "$maxRange${if(!autoBlockValue.equals("Off")){"-${autoBlockRangeValue.get()}"}else{""}}-${discoverRangeValue.get()}, " +
                "${if(targetModeValue.equals("Switch")){ "SW" }else{targetModeValue.get().substring(0,1).uppercase()}}, " +
                priorityValue.get().substring(0,1).uppercase()
}
