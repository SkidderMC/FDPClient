package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "TargetStrafe", category = ModuleCategory.MOVEMENT)
class TargetStrafe : Module() {

    private val godMode = BoolValue("GodMode", false)
    private val render = BoolValue("Render", true)
    private val radiusValue = FloatValue("radius", 0.5f, 0.1f, 5.0f)
    private val modeValue = ListValue("KeyMode", arrayOf("Jump", "None"), "None")
    private val radiusMode = ListValue("radiusMode", arrayOf("TrueRadius", "Simple"), "Simple")
    private lateinit var killAura: KillAura
    private lateinit var speed: Speed
    private lateinit var fly: Fly

    var consts = 0
    var lastDist = 0.0

    override fun onInitialize() {
        killAura=LiquidBounce.moduleManager.getModule(KillAura::class.java)
        speed=LiquidBounce.moduleManager.getModule(Speed::class.java)
        fly=LiquidBounce.moduleManager.getModule(Fly::class.java)
    }

    fun onMove(event: MoveEvent) {
        val xDist = event.x
        val zDist = event.z
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist)
    }

    @EventTarget
    fun moveStrafe(event: MoveEvent) {
        onMove(event)

        if (!isVoid(0, 0) && canStrafe) {
            val strafe = RotationUtils.getRotations(killAura.target)
            setSpeed(event, lastDist, strafe.yaw, radiusValue.get(), 1.0)
        }

        if (!godMode.get())
            return
        else {
            if (canStrafe) {
                mc.gameSettings.thirdPersonView = 2
            } else {
                mc.gameSettings.thirdPersonView = 0
            }
            return
        }
    }

    val keyMode: Boolean
        get() = when (modeValue.get().toLowerCase()) {
            "jump" -> mc.gameSettings.keyBindJump.isKeyDown
            "none" -> mc.thePlayer.movementInput.moveStrafe == 0f || mc.thePlayer.movementInput.moveForward == 0f
            else -> false
        }

    val canStrafe: Boolean
        get() = (killAura.state && (speed.state || fly.state) && killAura.target != null && !mc.thePlayer.isSneaking
                && keyMode)

    val cansize: Float
        get() = when {
            radiusMode.get().toLowerCase() == "simple" ->
                45f / mc.thePlayer.getDistance(killAura.target!!.posX, mc.thePlayer.posY, killAura.target!!.posZ).toFloat()
            else -> 45f
        }
    val Enemydistance: Double
        get() = mc.thePlayer.getDistance(killAura.target!!.posX, mc.thePlayer.posY, killAura.target!!.posZ)

    val algorithm: Float
        get() = Math.max(Enemydistance - radiusValue.get(), Enemydistance - (Enemydistance - radiusValue.get() / (radiusValue.get() * 2))).toFloat()



    fun setSpeed(
        moveEvent: MoveEvent, moveSpeed: Double, pseudoYaw: Float, pseudoStrafe: Float,
        pseudoForward: Double
    ) {
        var yaw = pseudoYaw
        var forward = pseudoForward
        var strafe = pseudoStrafe
        var strafe2 = 0f

        check()

        when {
            modeValue.get().toLowerCase().equals("jump") ->
                strafe = (pseudoStrafe * 0.98 * consts).toFloat()
            modeValue.get().toLowerCase().equals("none") ->
                strafe = consts.toFloat()
        }

        if (forward != 0.0) {
            if (strafe > 0.0) {
                if (radiusMode.get().toLowerCase() == "trueradius")
                    yaw += (if (forward > 0.0) -cansize else cansize)
                strafe2 += (if (forward > 0.0) -45 / algorithm else 45 / algorithm)
            } else if (strafe < 0.0) {
                if (radiusMode.get().toLowerCase() == "trueradius")
                    yaw += (if (forward > 0.0) cansize else -cansize)
                strafe2 += (if (forward > 0.0) 45 / algorithm else -45 / algorithm)
            }
            strafe = 0.0f
            if (forward > 0.0)
                forward = 1.0
            else if (forward < 0.0)
                forward = -1.0

        }
        if (strafe > 0.0)
            strafe = 1.0f
        else if (strafe < 0.0)
            strafe = -1.0f


        val mx = Math.cos(Math.toRadians(yaw + 90.0 + strafe2))
        val mz = Math.sin(Math.toRadians(yaw + 90.0 + strafe2))
        moveEvent.x = forward * moveSpeed * mx + strafe * moveSpeed * mz
        moveEvent.z = forward * moveSpeed * mz - strafe * moveSpeed * mx
    }

    private fun check() {
        if (mc.thePlayer.isCollidedHorizontally || checkVoid()) {
            if (consts < 2) consts += 1
            else {
                consts = -1
            }
        }
        when (consts) {
            0 -> {
                consts = 1
            }
            2 -> {
                consts = -1
            }
        }
    }

    private fun checkVoid(): Boolean {
        for (x in -1..0) {
            for (z in -1..0) {
                if (isVoid(x, z)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isVoid(X: Int, Z: Int): Boolean {
        val fly = LiquidBounce.moduleManager.getModule(Fly::class.java) as Fly
        if (fly.state) {
            return false
        }
        if (mc.thePlayer.posY < 0.0) {
            return true
        }
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox.offset(X.toDouble(), (-off).toDouble(), Z.toDouble())
            if (mc.theWorld!!.getCollidingBoundingBoxes(mc.thePlayer as Entity, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
            off += 2
        }
        return true
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val target = LiquidBounce.combatManager.target
        if (render.get()) {
            target?:return
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
            GL11.glLineWidth(1.0f)

            GL11.glBegin(3)
            val x = target.lastTickPosX + (target.posX - target.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
            val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
            val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
            for (i in 0..360) {
                val rainbow = Color(Color.HSBtoRGB((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)).toFloat() % 1.0f, 0.7f, 1.0f))
                GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                GL11.glVertex3d(x + radiusValue.get() * cos(i * 6.283185307179586 / 45.0), y, z + radiusValue.get() * sin(i * 6.283185307179586 / 45.0))
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
}






