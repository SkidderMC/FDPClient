/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB

@ModuleInfo(name = "NoFall", category = ModuleCategory.PLAYER, autoDisable = EnumAutoDisableType.FLAG)
object NoFall : Module() {

    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.nofalls", NoFallMode::class.java)
        .map { it.newInstance() as NoFallMode }
        .sortedBy { it.modeName }

    val mode: NoFallMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Vanilla") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    
    private val noVoid = BoolValue("NoVoid", false)

    var launchX = 0.0
    var launchY = 0.0
    var launchZ = 0.0
    var launchYaw = 0f
    var launchPitch = 0f

    var antiDesync = false

    var needReset = true

    var wasTimer = false

    override fun onEnable() {
        needReset = true
        launchX = mc.thePlayer.posX
        launchY = mc.thePlayer.posY
        launchZ = mc.thePlayer.posZ
        launchYaw = mc.thePlayer.rotationYaw
        launchPitch = mc.thePlayer.rotationPitch
        wasTimer = false

        mode.onEnable()
    }

    override fun onDisable() {
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.capabilities.flySpeed = 0.05f
        mc.thePlayer.noClip = false

        mc.timer.timerSpeed = 1F
        mc.thePlayer.speedInAir = 0.02F
        mode.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.0f
            wasTimer = false
        }
        mode.onUpdate(event)
        if (!state || FDPClient.moduleManager[FreeCam::class.java]!!.state) {
            return
        }

        if (mc.thePlayer.isSpectator || mc.thePlayer.capabilities.allowFlying || mc.thePlayer.capabilities.disableDamage) {
            return
        }

        if (BlockUtils.collideBlock(mc.thePlayer.entityBoundingBox) { it is BlockLiquid } || BlockUtils.collideBlock(
                AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY, mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.01, mc.thePlayer.entityBoundingBox.minZ)
            ) { it is BlockLiquid }) {
            return
        }
        
        if (checkVoid() && noVoid.get()) return

        mode.onNoFall(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        mode.onMotion(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        mode.onJump(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        mode.onStep(event)
    }

    override val tag: String
        get() = modeValue.get()
        
    private fun checkVoid(): Boolean {
        var i = (-(mc.thePlayer.posY-1.4857625)).toInt()
        var dangerous = true
        while (i <= 0) {
            dangerous = mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(mc.thePlayer.motionX * 0.5, i.toDouble(), mc.thePlayer.motionZ * 0.5)).isEmpty()
            i++
            if (!dangerous) break
        }
        return dangerous
    }


    /**
     * 读取mode中的value并和本体中的value合并
     * 所有的value必须在这个之前初始化
     */
    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                //it.add(value.displayable { modeValue.equals(mode.modeName) })
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}
