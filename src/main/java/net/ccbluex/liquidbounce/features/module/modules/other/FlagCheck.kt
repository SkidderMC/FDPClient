/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils.disableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPosBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.enableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetCaps
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.init.Blocks
import net.minecraft.network.login.server.S00PacketDisconnect
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

object FlagCheck : Module("FlagCheck", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = true) {

    // TODO: Model & Wireframe Render
    private val renderServerPos by choices(
        "RenderServerPos-Mode",
        arrayOf("None", "Box"),
        "None",
    ).subjective()

    private val resetFlagCounterTicks by int("ResetCounterTicks", 5000, 1000..10000)

    private val ghostBlockCheck by boolean("GhostBlock-Check", true)
    private val ghostBlockDelay by int("GhostBlockDelay", 750, 500..1000)
    { ghostBlockCheck }

    private val rubberbandCheck by boolean("Rubberband-Check", false)
    private val rubberbandThreshold by float("RubberBandThreshold", 5.0f, 0.05f..10.0f)
    { rubberbandCheck }

    private val colors = ColorSettingsInteger(
        this,
        "TextColor",
        applyMax = true
    ) { renderServerPos == "Box" }

    private val boxColors = ColorSettingsInteger(
        this,
        "BoxColor",
    ) { renderServerPos == "Box" }.with(r = 255, g = 255)

    private val scale by float("Scale", 1F, 1F..6F) { renderServerPos == "Box" }
    private val font by font("Font", Fonts.fontSemibold40) { renderServerPos == "Box" }
    private val fontShadow by boolean("Shadow", true) { renderServerPos == "Box" }

    private var lastCheckTime = 0L

    private var flagCount = 0
    private var lastYaw = 0F
    private var lastPitch = 0F

    private var blockPlacementAttempts = mutableMapOf<BlockPos, Long>()
    private var successfulPlacements = mutableSetOf<BlockPos>()

    private fun clearFlags() {
        flagCount = 0
        blockPlacementAttempts.clear()
        successfulPlacements.clear()
        lastServerPos = null
        serverPosTime = 0L
    }

    private var lagbackDetected = false
    private var forceRotateDetected = false

    private var lastServerPos: Vec3? = null
    private var serverPosTime = 0L

    private var lastMotionX = 0.0
    private var lastMotionY = 0.0
    private var lastMotionZ = 0.0

    private var lastPosX = 0.0
    private var lastPosY = 0.0
    private var lastPosZ = 0.0

    private var resetTicks = 0

    override fun onDisable() {
        resetTicks = 0
        clearFlags()
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val packet = event.packet

        if (player.ticksExisted <= 100)
            return@handler

        if (player.isDead || (player.capabilities.isFlying && player.capabilities.disableDamage && !player.onGround))
            return@handler

        if (packet is S08PacketPlayerPosLook) {
            val deltaYaw = calculateAngleDelta(packet.yaw, lastYaw)
            val deltaPitch = calculateAngleDelta(packet.pitch, lastPitch)

            lastServerPos = Vec3(packet.x, packet.y, packet.z)
            serverPosTime = System.currentTimeMillis()

            if (deltaYaw > 90 || deltaPitch > 90) {
                forceRotateDetected = true
                flagCount++
                chat("§dDetected §3Force-Rotate §e(${deltaYaw.roundToLong()}° | ${deltaPitch.roundToLong()}°) §b(§c${flagCount}x§b)")
            } else {
                forceRotateDetected = false
            }

            if (!forceRotateDetected) {
                lagbackDetected = true
                flagCount++
                chat("§dDetected §3Lagback §b(§c${flagCount}x§b)")
            }

            if (player.ticksExisted % 3 == 0) {
                lagbackDetected = false
            }

            lastYaw = mc.thePlayer.rotationYawHead
            lastPitch = mc.thePlayer.rotationPitch
        }

        if (packet is C08PacketPlayerBlockPlacement) {
            val blockPos = packet.position
            blockPlacementAttempts[blockPos] = System.currentTimeMillis()
            successfulPlacements.add(blockPos)
        }

        when (packet) {
            is S01PacketJoinGame, is S00PacketDisconnect -> {
                clearFlags()
            }
        }
    }

    private fun calculateAngleDelta(newAngle: Float, oldAngle: Float): Float {
        var delta = newAngle - oldAngle
        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360
        return abs(delta)
    }

    /**
     * Rubberband, Invalid Health/Hunger & GhostBlock Checks
     */
    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (player.isDead || mc.currentScreen is GuiGameOver || player.ticksExisted <= 100) {
            return@handler
        }

        // LastServerPos Resets | After 5 second
        if (lastServerPos != null && System.currentTimeMillis() - serverPosTime > 5000) {
            lastServerPos = null
        }

        // GhostBlock Checks | Checks is disabled when using VerusFly Disabler, to prevent false flag.
        if (ghostBlockCheck && (!Disabler.handleEvents() || (Disabler.handleEvents() && !Disabler.verusFly))) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastCheckTime > 2000) {
                lastCheckTime = currentTime

                blockPlacementAttempts.entries.removeIf { (blockPos, timestamp) ->
                    if (currentTime - timestamp > ghostBlockDelay) {
                        // Returns if blockpos is < 0
                        if (blockPos < BlockPos.ORIGIN) return@removeIf false
                        val block = world.getBlockState(blockPos).block

                        if (block == Blocks.air && successfulPlacements.contains(blockPos)) {
                            flagCount++
                            chat("§dDetected §3GhostBlock §b(§c${flagCount}x§b)")
                            successfulPlacements.clear()
                            return@removeIf true
                        }
                    }
                    false
                }
            }
        }

        // Invalid Health/Hunger bar Checks (This is a known lagback by Intave AC)
        val invalidReason = mutableListOf<String>()
        if (player.health <= 0.0f) invalidReason.add("Health")
        if (player.foodStats.foodLevel <= 0) invalidReason.add("Hunger")

        if (invalidReason.isNotEmpty()) {
            flagCount++
            val reasonString = invalidReason.joinToString(" §8|§e ")
            chat("§dDetected §3Invalid §e$reasonString §b(§c${flagCount}x§b)")
            invalidReason.clear()
        }

        // Rubberband Checks
        if (!rubberbandCheck || (player.capabilities.isFlying && player.capabilities.disableDamage && !player.onGround))
            return@handler

        val motionX = player.motionX
        val motionY = player.motionY
        val motionZ = player.motionZ

        val deltaX = player.posX - lastPosX
        val deltaY = player.posY - lastPosY
        val deltaZ = player.posZ - lastPosZ

        val distanceTraveled = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

        val rubberbandReason = mutableListOf<String>()

        if (distanceTraveled > rubberbandThreshold) {
            rubberbandReason.add("Invalid Position")
        }

        if (abs(motionX) > rubberbandThreshold || abs(motionY) > rubberbandThreshold || abs(motionZ) > rubberbandThreshold) {
            if (!player.isCollided && !player.onGround) {
                rubberbandReason.add("Invalid Motion")
            }
        }

        if (rubberbandReason.isNotEmpty()) {
            flagCount++
            val reasonString = rubberbandReason.joinToString(" §8|§e ")
            chat("§dDetected §3Rubberband §8(§e$reasonString§8) §b(§c${flagCount}x§b)")
            rubberbandReason.clear()
        }

        // Update last position and motion
        lastPosX = player.prevPosX
        lastPosY = player.prevPosY
        lastPosZ = player.prevPosZ

        lastMotionX = motionX
        lastMotionY = motionY
        lastMotionZ = motionZ
    }

    val onRender3D = handler<Render3DEvent> {
        val player = mc.thePlayer ?: return@handler
        val renderManager = mc.renderManager
        val pos = lastServerPos ?: return@handler

        if (renderServerPos != "Box") return@handler

        val remainingTime = ((6000 - (System.currentTimeMillis() - serverPosTime)) / 1000).coerceAtLeast(0)
        val text = "Last Position: ${remainingTime}sec"

        // TODO: Fade effect
        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()

        val (x, y, z) = pos - renderManager.renderPos

        // Translate to block position
        glTranslated(x, y + 2.5, z)

        glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(renderManager.playerViewX, 1F, 0F, 0F)

        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)
        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val fontRenderer = font

        // Scale
        val scale =
            (((player.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord) / 8F).coerceIn(1.5, 5.0) / 100F) * scale)
        glScaled(-scale, -scale, scale)

        // Draw text
        val width = fontRenderer.getStringWidth(text) * 0.5f
        fontRenderer.drawString(
            text, -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, colors.color().rgb, fontShadow
        )

        resetCaps()
        glPopMatrix()
        glPopAttrib()

        drawPosBox(
            lastServerPos!!.xCoord,
            lastServerPos!!.yCoord,
            lastServerPos!!.zCoord,
            0.8F, 2F,
            boxColors.color(),
            true
        )
    }

    val onTick = handler<GameTickEvent> {
        if (mc.thePlayer == null || mc.theWorld == null)
            return@handler

        if (resetTicks >= resetFlagCounterTicks) {
            clearFlags()
            resetTicks = 0
            return@handler
        }

        if (mc.thePlayer.ticksExisted > 100) {
            resetTicks++
        }
    }

    val onWorld = handler<WorldEvent> {
        clearFlags()
    }
}