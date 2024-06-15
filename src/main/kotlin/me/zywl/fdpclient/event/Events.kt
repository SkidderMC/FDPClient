/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package me.zywl.fdpclient.event

import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

/**
 * Called when player attacks other entity
 *
 * @param targetEntity Attacked entity
 */
data class AttackEvent(val targetEntity: Entity) : CancellableEvent()

/**
 * Called when player killed other entity
 *
 * @param targetEntity Attacked entity
 */
data class EntityKilledEvent(val targetEntity: EntityLivingBase) : Event()

/**
 * Called when minecraft get bounding box of block
 *
 * @param blockPos block position of block
 * @param block block itself
 * @param boundingBox vanilla bounding box
 */
data class BlockBBEvent(val blockPos: BlockPos, val block: Block, var boundingBox: AxisAlignedBB?) : Event() {
    val x = blockPos.x
    val y = blockPos.y
    val z = blockPos.z
}

/**
 * Called when player clicks a block
 */
data class ClickBlockEvent(val clickedBlock: BlockPos?, val enumFacing: EnumFacing?) : Event()

/**
 * Called when player teleports
 */
data class TeleportEvent(
    val response: C03PacketPlayer? = null,
    val posX: Double,
    val posY: Double,
    val posZ: Double,
    var yaw: Float = 0f,
    var pitch: Float = 0f
) : CancellableEvent()

/**
 * Called when client is shutting down
 */
class ClientShutdownEvent : Event()

/**
 * Called when player jumps
 *
 * @param motion jump motion (y motion)
 */
data class JumpEvent(var motion: Float, var yaw: Float) : CancellableEvent()

/**
 * Called when user press a key once
 *
 * @param key Pressed key
 */
data class KeyEvent(val key: Int) : Event()

/**
 * Called in "onUpdateWalkingPlayer"
 *
 * @param eventState PRE or POST
 */
data class MotionEvent(val eventState: EventState) : Event() {
    fun isPre() = eventState == EventState.PRE
}

/**
 * Called when entity is going to be rendered
 */
data class RenderEntityEvent(val entity: Entity, val x: Double, val y: Double, val z: Double, val entityYaw: Float,
                             val partialTicks: Float) : Event()

/**
 * Called when a model updates
 */
data class UpdateModelEvent(val player: EntityPlayer, val model: ModelPlayer) : Event()

/**
 * Called when an entity receives damage
 */
data class EntityDamageEvent(val damagedEntity: Entity): Event()

/**
 * Called after motion
 */
class PostMotionEvent: Event()

/**
 * Called in "onLivingUpdate" when the player is using a use item.
 *
 * @param strafe the applied strafe slow down
 * @param forward the applied forward slow down
 */
data class SlowDownEvent(var strafe: Float, var forward: Float) : Event()

/**
 * Called in "moveFlying"
 */
data class StrafeEvent(val strafe: Float, val forward: Float, val friction: Float) : CancellableEvent()

/**
 * Called when an other entity moves
 */
data class EntityMovementEvent(val movedEntity: Entity) : Event()

/**
 * Called when player moves
 *
 * @param x motion
 * @param y motion
 * @param z motion
 */
data class MoveEvent(var x: Double, var y: Double, var z: Double) : CancellableEvent() {
    var isSafeWalk: Boolean = false

    fun zero() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    fun zeroXZ() {
        x = 0.0
        z = 0.0
    }
}

/**
 * Called when receive or send a packet
 */
data class PacketEvent(val packet: Packet<*>, val type: Type) : CancellableEvent() {
    enum class Type {
        RECEIVE,
        SEND
    }

    fun isServerSide() = type == Type.RECEIVE
}

/**
 * Called when a block tries to push you
 */
class PushOutEvent : CancellableEvent()

/**
 * Called when screen is going to be rendered
 */
data class Render2DEvent(val partialTicks: Float, val scaledResolution: ScaledResolution) : Event()

/**
 * Called when world is going to be rendered
 */
data class Render3DEvent(val partialTicks: Float) : Event()

/**
 * Called when the screen changes
 */
data class ScreenEvent(val guiScreen: GuiScreen?) : Event()

/**
 * Called when the session changes
 */
class SessionEvent : Event()

/**
 * Called when player is going to step
 */
data class StepEvent(var stepHeight: Float, val eventState: EventState) : Event()

/**
 * Called when a text is going to be rendered
 */
data class TextEvent(var text: String?) : Event()

/**
 * tick... tack... tick... tack
 */
class TickEvent : Event()

/**
 * Called when minecraft player will be updated
 */
class UpdateEvent : Event()

/**
 * Called when the world changes
 */
data class WorldEvent(val worldClient: WorldClient?) : Event()

/**
 * Called when window clicked
 */
data class ClickWindowEvent(val windowId: Int, val slotId: Int, val mouseButtonClicked: Int, val mode: Int) : CancellableEvent()