/*
*  LiquidLite Ghost Client -> FDP
*/

package net.ccbluex.liquidbounce.features.module.modules.ghost

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeLook
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import kotlin.math.*
import kotlin.random.Random

@ModuleInfo(name = "LegitAura", category = ModuleCategory.GHOST)
object LegitAura : Module() {

    private val cpsMode = ListValue("CPSMode", arrayOf("Normal", "Jitter", "Butterfly"), "Normal")
    private val legitJitterValue = ListValue("LegitJitterMode", arrayOf("Jitter1", "Jitter2", "Jitter3", "SimpleJitter"), "Jitter1").displayable { cpsMode.equals("Jitter")}
    private val legitButterflyValue = ListValue("LegitButterflyMode", arrayOf("Butterfly1", "Butterfly2"), "Butterfly1").displayable {  cpsMode.equals("Butterfly")}

    private val minCpsValue: IntegerValue = object : IntegerValue("MinCPS", 10, 1, 20){
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCpsValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { cpsMode.equals("Normal") } as IntegerValue
    private val maxCpsValue: IntegerValue = object : IntegerValue("MaxCPS", 12, 1, 20){
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCpsValue.get()
            if (i > newValue) set(i)
        }
    }.displayable { cpsMode.equals("Normal") } as IntegerValue

    val hitRange: FloatValue = object : FloatValue("AttackRange", 3.05f, 2f, 6f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = rotationRange.get()
            if (i < newValue) set(i)
        }
    }
    private val swingRange: FloatValue = object : FloatValue("SwingRange", 4.5f, 3f, 7f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = hitRange.get()
            if (i > newValue) set(i)
        }
    }
    private val rotationRange = FloatValue("RotationRange", 5f, 4f, 7f)

    private val priorityMode = ListValue("Priority", arrayOf("FOV", "Distance", "LowestHealth", "HighestHealth", "HurtTicks"), "FOV")
    private val fovValue = IntegerValue("FOV", 180, 1, 180)

    private val autoblockMode = ListValue("AutoBlock", arrayOf("Always", "Smart", "Spam", "Blink", "Fake", "None"), "Smart")
    private val autoblockRange = FloatValue("AutoBlockRange", 2f, 1f, 5f).displayable {!autoblockMode.equals("None")}


    private val rotationMode = ListValue("RotationMode", arrayOf("Advanced", "Simple", "Smooth", "LockView"), "Advanced")
    private val turnSpeedValue = IntegerValue("RotationSpeed", 40, 1, 180).displayable { !rotationMode.equals("LockView") }
    private val turnSpeedRandomValue = FloatValue("RotationSpeedRandom", 6f, 0f, 15f).displayable { !rotationMode.equals("LockView") }
    private val aimLocationValue = ListValue("AimLocation", arrayOf("LiquidBounce", "Full", "HalfUp", "HalfDown", "CenterSimple", "CenterLine", "CenterLarge", "CenterDot", "MidRange", "HeadRange"), "CenterLarge").displayable { rotationMode.equals("Simple") || rotationMode.equals("Smooth") }
    private val jitterValue = FloatValue("JitterAmount", 1f, 0f, 3f).displayable { rotationMode.equals("Simple") || rotationMode.equals("Smooth") }
    private val smoothMode = ListValue("SmoothMode", arrayOf("Custom", "Line", "Quad", "Sine", "QuadSine"), "QuadSine").displayable { rotationMode.equals("Smooth") }
    private val customSmoothValue = FloatValue("CustomSmoothSpeed", 2.4f, 1f, 4f).displayable { rotationMode.equals("Smooth") && smoothMode.equals("Custom") }

    private val playerPredictValue = FloatValue("PlayerPredictAmount", 1.2f, -2f, 3f)
    private val opPredictValue = FloatValue("TargetPredictAmount", 1.5f, -2f, 3f)

    private val movementFixValue = BoolValue("MovementFix", true)

    private val markValue = ListValue("Mark", arrayOf("Liquid", "Block", "OtherBlock", "Rise", "Eternal"), "OtherBlock")
    private val blockMarkExpandValue = FloatValue("BlockExpand", 0f, 0.5f, 1f).displayable { markValue.equals("Block") || markValue.equals("OtherBlock") }


    private var currentTarget: EntityLivingBase? = null


    private var advancedAimPointX = 0f
    private var advancedAimPointY = 0f
    private var advancedAimPointZ = 0f

    private var advancedAimVelo = 0f

    private var advancedAimSlowRot = Rotation(0f,0f)

    // current player rot and goal rot
    private var aimRotation = Rotation(0f,0f)
    private var playerRotation = Rotation(0f,0f)

    // for rotation speed calc
    private var rotationDistance = 0f
    private var rotationLimit = 0f

    // final limited rotation to apply to the player
    private var targetRotation = Rotation(0f,0f)

    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    private val inRangeDiscoveredTargets = mutableListOf<EntityLivingBase>()
    private val autoblockRangeTargets = mutableListOf<EntityLivingBase>()

    // clicker
    private var leftDelay = 50L
    private var leftLastSwing = 0L
    private var cDelay = 0
    private var delayNum = 0

    override fun onDisable() {
        FDPClient.moduleManager[FreeLook::class.java]!!.disable()
        autoblockRangeTargets.clear()
        mc.gameSettings.keyBindUseItem.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
        currentTarget = null

        // AutoDisable MoveFix
        MovementUtils.updateControls()
    }

    override fun onEnable() {
        currentTarget = null
        FDPClient.moduleManager[FreeLook::class.java]!!.enable()
    }

    val displayBlocking: Boolean
        get() = !autoblockMode.equals("None") && autoblockRangeTargets.isNotEmpty()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        updateTarget()
        if (discoveredTargets.isEmpty()) {
            mc.thePlayer.rotationYaw = FreeLook.cameraYaw
            mc.thePlayer.rotationPitch = FreeLook.cameraPitch
            currentTarget = null
            mc.gameSettings.keyBindUseItem.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
            BlinkUtils.setBlinkState(off = true, release = true)
            return
        } else {
            if (!FreeLook.isEnabled) {
                FDPClient.moduleManager[FreeLook::class.java]!!.enable()
            }
            if (inRangeDiscoveredTargets.isNotEmpty()) {
                if (!inRangeDiscoveredTargets.contains(currentTarget)) {
                    currentTarget = inRangeDiscoveredTargets.first()
                }
            }
        }

        if (mc.thePlayer.ticksExisted % 2 == 0 && autoblockMode.equals("Blink") && autoblockRangeTargets.isNotEmpty()) {
            BlinkUtils.setBlinkState(all = true)
        } else if (autoblockMode.equals("Blink") && autoblockRangeTargets.isEmpty()) {
            BlinkUtils.setBlinkState(off = true, release = true)
        }

        val entity = currentTarget ?: inRangeDiscoveredTargets.getOrNull(0)?: return
        currentTarget = entity as EntityLivingBase?

        if (rotationMode.equals("Advanced")) {
            entity.hitBox.offset((entity.posX - entity.lastTickPosX) * 1.4f,
                (entity.posY - entity.lastTickPosY) * 1.4f,
                (entity.posZ - entity.lastTickPosZ) * 1.4f)
            entity.hitBox.offset(mc.thePlayer.motionX * -1f * 2.7f,
                mc.thePlayer.motionY * -1f * 2.7f,
                mc.thePlayer.motionX * -1f * 2.7f)
        } else {

            entity.hitBox.offset((entity.posX - entity.lastTickPosX) * opPredictValue.get(),
                (entity.posY - entity.lastTickPosY) * opPredictValue.get(),
                (entity.posZ - entity.lastTickPosZ) * opPredictValue.get())
            entity.hitBox.offset(mc.thePlayer.motionX * -1f * playerPredictValue.get(),
                mc.thePlayer.motionY * -1f * playerPredictValue.get(),
                mc.thePlayer.motionX * -1f * playerPredictValue.get())
        }



        // ka rot
        killauraRotations(entity)

        // blink ab pt2
        if (mc.thePlayer.ticksExisted % 2 == 0 && autoblockMode.equals("Blink") && autoblockRangeTargets.isNotEmpty()) {
            BlinkUtils.releasePacket()
        }

        if (movementFixValue.get()) {
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindRight) || GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
                || GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || GameSettings.isKeyDown(mc.gameSettings.keyBindBack)) {

                var movingYaw = FreeLook.cameraYaw
                var forward = 1.0
                if (GameSettings.isKeyDown(mc.gameSettings.keyBindBack)) {
                    forward = -0.5
                }
                if (GameSettings.isKeyDown(mc.gameSettings.keyBindForward)) {
                    forward = 0.5
                }
                if (GameSettings.isKeyDown(mc.gameSettings.keyBindForward) && GameSettings.isKeyDown(mc.gameSettings.keyBindBack)) {
                    forward = 0.0
                }
                if (forward < 0) {
                    movingYaw += 180
                }
                if (GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) {
                    movingYaw += 90 * forward.toFloat()
                }
                if (GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) {
                    movingYaw -= 90 * forward.toFloat()
                }


                var yawDiff = ((movingYaw - mc.thePlayer.rotationYaw) / 45).roundToInt()
                if (yawDiff > 4) yawDiff -= 8
                if (yawDiff < -4) yawDiff += 8
                mc.gameSettings.keyBindForward.pressed = abs(yawDiff) <= 1
                mc.gameSettings.keyBindBack.pressed = abs(yawDiff) >= 3
                mc.gameSettings.keyBindRight.pressed = yawDiff in 1..3
                mc.gameSettings.keyBindLeft.pressed = yawDiff in -3..-1
            }
        }
    }

    private fun killauraRotations(entity: EntityLivingBase) {
        playerRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        when (rotationMode.get().lowercase()) {
            "simple","smooth" -> {
                aimRotation = RotationUtils.calculateCenter(
                    aimLocationValue.get(),
                    "Cubic",
                    0.05,
                    entity.hitBox,
                    false,
                    true
                )!!.rotation

                rotationDistance = RotationUtils.getRotationDifference(playerRotation,  aimRotation).toFloat()
                if (rotationMode.equals("Simple")) {
                    rotationLimit = turnSpeedValue.get().toFloat()
                } else {
                    rotationLimit = applySmoothing(rotationDistance.toDouble(), smoothMode.get().lowercase()).toFloat()
                }
                rotationLimit = rotationLimit + RandomUtils.nextFloat(-turnSpeedRandomValue.get(), turnSpeedRandomValue.get())

                if (rotationLimit < 0)
                    rotationLimit = 0.1f

                targetRotation = RotationUtils.limitAngleChange( playerRotation, aimRotation, rotationLimit )
            }
            "lockview" -> {
                aimRotation = RotationUtils.calculateCenter("CenterDot", "Cubic", 0.05, entity.hitBox, false, true)!!.rotation
                targetRotation = aimRotation
            }
            "advanced" -> {

                if (mc.thePlayer.ticksExisted % 16 == 0) {
                    advancedAimPointX = RandomUtils.nextFloat(-0.3f, 0.3f)
                    advancedAimPointY = RandomUtils.nextFloat(-0.3f, 0.3f)
                    advancedAimPointZ = RandomUtils.nextFloat(-0.3f, 0.3f)
                }

                entity.hitBox.offset(advancedAimPointX.toDouble(), advancedAimPointY.toDouble(), advancedAimPointZ.toDouble())

                aimRotation = RotationUtils.calculateCenter("LiquidBounce", "Horizontal", 0.05, entity.hitBox, false, true)!!.rotation
                rotationDistance = RotationUtils.getRotationDifference(playerRotation,  aimRotation).toFloat()
                rotationLimit = applySmoothing(rotationDistance.toDouble(), "line").toFloat() /1.5f

                aimRotation = RotationUtils.calculateCenter("CenterLine", "Horizontal", 0.05, entity.hitBox, true, true)!!.rotation
                rotationDistance = RotationUtils.getRotationDifference(playerRotation,  aimRotation).toFloat()
                rotationLimit = (rotationLimit + applySmoothing(rotationDistance.toDouble()/1.5, "quad").toFloat())

                advancedAimVelo += rotationLimit + RandomUtils.nextFloat(-3f, 2f)
                advancedAimVelo *= 0.3f

                aimRotation = RotationUtils.calculateCenter("HeadRange", "Horizontal", 0.05, entity.hitBox, false, true)!!.rotation
                targetRotation = RotationUtils.limitAngleChange(playerRotation, aimRotation, advancedAimVelo * 0.5f )
                aimRotation = RotationUtils.calculateCenter("CenterDot", "Horizontal", 0.05, entity.hitBox, false, true)!!.rotation
                targetRotation = RotationUtils.limitAngleChange(targetRotation, aimRotation, advancedAimVelo * 0.3f )

                if (RotationUtils.getRotationDifference(advancedAimSlowRot,  targetRotation).toFloat() > 125f) {
                    advancedAimVelo *= 0.7f
                    advancedAimSlowRot = targetRotation
                }
            }
        }

        targetRotation.toPlayer(mc.thePlayer)

        // jitter
        when (rotationMode.get().lowercase()) {
            "simple", "smooth" -> {
                mc.thePlayer.rotationYaw += RandomUtils.nextFloat(-jitterValue.get(), jitterValue.get())
                mc.thePlayer.rotationPitch += RandomUtils.nextFloat(-jitterValue.get(), jitterValue.get())
            }
            "advanced" -> {
                mc.thePlayer.rotationYaw += RandomUtils.nextFloat(-1f, 1f)
                mc.thePlayer.rotationPitch += RandomUtils.nextFloat(-1f, 1f)
            }
            else -> null
        }

        // keep pitch legit
        if (mc.thePlayer.rotationPitch > 90) {
            mc.thePlayer.rotationPitch = 90F
        } else if (mc.thePlayer.rotationPitch < -90) {
            mc.thePlayer.rotationPitch = -90F
        }

    }

    private fun applySmoothing(diffAngle: Double, mode: String): Double {
        val maxRotSpeed = (turnSpeedValue.get() + turnSpeedRandomValue.get() / 2)
        val minRotSpeed = (turnSpeedValue.get() - turnSpeedRandomValue.get() / 2)
        return when(mode) {
            "custom" -> diffAngle / customSmoothValue.get()
            "line" -> (diffAngle / 360) * maxRotSpeed + (1 - diffAngle / 360) * minRotSpeed
            "quad" -> (diffAngle / 360.0).pow(2.0) * maxRotSpeed + (1 - (diffAngle / 360.0).pow(2.0)) * minRotSpeed
            "sine" -> (-cos(diffAngle / 180 * 3.142) * 0.5 + 0.5) * maxRotSpeed + (cos(diffAngle / 360 * 3.142) * 0.5 + 0.5) * minRotSpeed
            "quadsine" -> (-cos(diffAngle / 180 * 3.142) * 0.5 + 0.5).pow(2.0) * maxRotSpeed + (1 - (-cos(diffAngle / 180 * 3.142) * 0.5 + 0.5).pow(2.0)) * minRotSpeed
            else -> 2.0
        }
    }



    @EventTarget
    fun on3DRender(event: Render3DEvent) {
        discoveredTargets.forEach {
            when (markValue.get().lowercase()) {
                "liquid" -> {
                    RenderUtils.drawPlatform(
                        it,
                        if (it.hurtTime <= 0) Color(37, 126, 255, 170) else Color(255, 0, 0, 170)
                    )
                }
                "block", "otherblock" -> {
                    val bb = it.entityBoundingBox
                    it.entityBoundingBox = it.entityBoundingBox.expand(
                        blockMarkExpandValue.get().toDouble(),
                        blockMarkExpandValue.get().toDouble(),
                        blockMarkExpandValue.get().toDouble())
                    RenderUtils.drawEntityBox(
                        it,
                        if (it.hurtTime <= 0) if (it == currentTarget) Color(25, 230, 0, 170) else Color(10, 250, 10, 170) else Color(255, 0, 0, 170),
                        markValue.equals("Block"),
                        true,
                        4f
                    )
                    it.entityBoundingBox = bb
                }
                "rise" -> {
                    val everyTime = 3000
                    val drawTime = (System.currentTimeMillis() % everyTime).toInt()
                    val drawMode = drawTime > (everyTime / 2)
                    var drawPercent = drawTime / (everyTime / 2.0)
                    // true when goes up
                    if (!drawMode) {
                        drawPercent = 1 - drawPercent
                    } else {
                        drawPercent -= 1
                    }
                    drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                    mc.entityRenderer.disableLightmap()
                    GL11.glPushMatrix()
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_CULL_FACE)
                    GL11.glShadeModel(7425)
                    mc.entityRenderer.disableLightmap()

                    val bb = it.hitBox
                    val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * 0.5f
                    val height = bb.maxY - bb.minY
                    val x =
                        it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                    val y =
                        (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
                    val z =
                        it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    val eased = (height / 3) * (if (drawPercent > 0.5) {
                        1 - drawPercent
                    } else {
                        drawPercent
                    }) * (if (drawMode) {
                        -1
                    } else {
                        1
                    })
                    for (i in 5..360 step 5) {
                        val color = ClientTheme.getColor(1)
                        val x1 = x - sin(i * Math.PI / 180F) * radius
                        val z1 = z + cos(i * Math.PI / 180F) * radius
                        val x2 = x - sin((i - 5) * Math.PI / 180F) * radius
                        val z2 = z + cos((i - 5) * Math.PI / 180F) * radius
                        GL11.glBegin(GL11.GL_QUADS)
                        RenderUtils.glColor(color, 0f)
                        GL11.glVertex3d(x1, y + eased, z1)
                        GL11.glVertex3d(x2, y + eased, z2)
                        RenderUtils.glColor(color, 150f)
                        GL11.glVertex3d(x2, y, z2)
                        GL11.glVertex3d(x1, y, z1)
                        GL11.glEnd()
                    }

                    GL11.glEnable(GL11.GL_CULL_FACE)
                    GL11.glShadeModel(7424)
                    GL11.glColor4f(1f, 1f, 1f, 1f)
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                    GL11.glDisable(GL11.GL_BLEND)
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glPopMatrix()
                }

                "eternal" -> {
                    val radius = 0.15f
                    val side = 4
                    GL11.glPushMatrix()
                    GL11.glTranslated(
                        it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX,
                        (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + it.height * 1.1,
                        it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    )
                    GL11.glRotatef(-it.width, 0.0f, 1.0f, 0.0f)
                    GL11.glRotatef((mc.thePlayer.ticksExisted + mc.timer.renderPartialTicks) * 5, 0f, 1f, 0f)
                    RenderUtils.glColor(if (it.hurtTime <= 0) Color(80, 255, 80) else Color(255, 0, 0))
                    RenderUtils.enableSmoothLine(1.5F)
                    val c = Cylinder()
                    GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
                    c.draw(0F, radius, 0.3f, side, 1)
                    c.drawStyle = 100012
                    GL11.glTranslated(0.0, 0.0, 0.3)
                    c.draw(radius, 0f, 0.3f, side, 1)
                    GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
                    GL11.glTranslated(0.0, 0.0, -0.3)
                    c.draw(0F, radius, 0.3f, side, 1)
                    GL11.glTranslated(0.0, 0.0, 0.3)
                    c.draw(radius, 0F, 0.3f, side, 1)
                    RenderUtils.disableSmoothLine()
                    GL11.glPopMatrix()
                }
            }
        }

        val swingDiff = System.currentTimeMillis() - leftLastSwing

        if (inRangeDiscoveredTargets.isEmpty()) return
        // clicker
        if (swingDiff >= leftDelay && mc.playerController.curBlockDamageMP == 0F) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)
            leftLastSwing = System.currentTimeMillis()
            leftDelay = updateClicks().toLong()
        }

        mc.gameSettings.keyBindUseItem.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)

        if (autoblockRangeTargets.isEmpty()) return
        // autoblock
        when (autoblockMode.get().lowercase()) {
            "always" -> {
                if (swingDiff >= leftDelay * 0.1 && swingDiff <= leftDelay * 0.7) {
                    mc.gameSettings.keyBindUseItem.pressed = true
                }
            }

            "smart" -> {
                if (swingDiff >= leftDelay * 0.1 && swingDiff <= leftDelay * 0.6 && mc.thePlayer.hurtTime <= 3) {
                    mc.gameSettings.keyBindUseItem.pressed = true
                }
            }

            "spam", "blink" -> {
                mc.gameSettings.keyBindUseItem.pressed = mc.thePlayer.ticksExisted % 2 == 0
            }

            else -> null
        }

    }





    private fun updateTarget() {
        // Settings
        val fov = fovValue.get()

        // Find possible targets
        discoveredTargets.clear()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !EntityUtils.isSelected(entity, true))
                continue


            var distance = mc.thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= rotationRange.get() && (fov.toFloat() == 180F || entityFov <= fov))
                discoveredTargets.add(entity)
        }

        // Sort targets by priority
        when (priorityMode.get().lowercase()) {
            "distance" -> discoveredTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "lowesthealth" -> discoveredTargets.sortBy { it.health + it.absorptionAmount } // Sort by health
            "highesthealth" -> discoveredTargets.sortBy { -it.health - it.absorptionAmount } // Sort by health
            "fov" -> discoveredTargets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "hurtticks" -> discoveredTargets.sortBy { it.hurtResistantTime } // hurt resistant time
        }

        inRangeDiscoveredTargets.clear()
        inRangeDiscoveredTargets.addAll(discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it) < swingRange.get()})

        autoblockRangeTargets.clear()
        autoblockRangeTargets.addAll(discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it) < autoblockRange.get()})
    }

    private fun updateClicks(): Int {

        when (cpsMode.get().lowercase()) {
            "normal" -> {
                cDelay = TimeUtils.randomClickDelay(minCpsValue.get(), maxCpsValue.get()).toInt()
            }

            "jitter" -> {
                when (legitJitterValue.get().lowercase()) {
                    "jitter1" -> {
                        if (1 == Random.nextInt(1, 5))
                            delayNum = 0

                        if (delayNum == 0) {
                            if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(98, 110)
                            } else if (Random.nextInt(1, 2) == 1) {
                                cDelay = Random.nextInt(125, 138)
                            } else {
                                cDelay = Random.nextInt(148, 153)
                            }

                            delayNum = 1
                        } else {
                            if (Random.nextInt(1, 4) !== 1) {
                                if (Random.nextBoolean()) {
                                    cDelay = Random.nextInt(65, 69)
                                } else {
                                    if (Random.nextInt(1, 5) == 1) {
                                        cDelay = Random.nextInt(81, 87)
                                    } else {
                                        cDelay = Random.nextInt(97, 101)
                                    }
                                }
                            }
                        }
                    }

                    "jitter2" -> {
                        if (Random.nextInt(1, 14) <= 3) {
                            if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(98, 102)
                            } else {
                                cDelay = Random.nextInt(114, 117)
                            }
                        } else {
                            if (Random.nextInt(1, 4) == 1) {
                                cDelay = Random.nextInt(64, 69)
                            } else {
                                cDelay = Random.nextInt(83, 85)
                            }

                        }
                    }

                    "jitter3" -> {
                        if (Random.nextInt(1, 5) == 1 && delayNum == 0) {
                            delayNum = 1
                            if (Random.nextInt(1, 4) == 1) {
                                cDelay = Random.nextInt(114, 118)
                            } else {
                                cDelay = Random.nextInt(98, 104)
                            }
                        } else {
                            if (delayNum == 1) {
                                delayNum = 0
                                cDelay = Random.nextInt(65, 70)
                            } else {
                                cDelay = Random.nextInt(84, 88)
                            }
                        }
                    }

                    "simplejitter" -> {
                        if (Random.nextInt(1, 5) == 1) {
                            if (Random.nextBoolean()) {
                                cDelay = Random.nextInt(105, 110)
                            } else {
                                cDelay = Random.nextInt(120, 128)
                            }
                        } else {
                            if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(76, 79)
                            } else {
                                if (Random.nextInt(1, 3) == 1) {
                                    cDelay = 78
                                } else {
                                    cDelay = 77
                                }
                            }
                        }
                    }
                }
            }

            "butterfly" -> {
                when (legitButterflyValue.get().lowercase()) {
                    "butterfly1" -> {
                        if (Random.nextInt(1, 7) == 1) {
                            cDelay = Random.nextInt(80, 104)
                        } else {
                            if (Random.nextInt(1, 7) <= 2) {
                                cDelay = 117
                            } else {
                                cDelay = Random.nextInt(114, 119)
                            }
                        }
                    }

                    "butterfly2" -> {
                        if (Random.nextInt(1, 10) == 1) {
                            cDelay = Random.nextInt(225, 250)
                        } else {
                            if (Random.nextInt(1, 6) == 1) {
                                cDelay = Random.nextInt(89, 94)
                            } else if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(95, 103)
                            } else if (Random.nextInt(1, 3) == 1) {
                                cDelay = Random.nextInt(115, 123)
                            } else {
                                if (Random.nextBoolean()) {
                                    cDelay = Random.nextInt(131, 136)
                                } else {
                                    cDelay = Random.nextInt(165, 174)
                                }
                            }
                        }
                    }
                }
            }
        }
        return cDelay
    }
}
    
