/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

/*
    Made by: Dg636
    2/12/24
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeLook
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.ccbluex.liquidbounce.value.*
import kotlin.math.roundToInt

@ModuleInfo(name = "Scaffold2",  category = ModuleCategory.MOVEMENT)
object Scaffold2 : Module() {
    val modeValue = ListValue("Mode", arrayOf("Simple", "SpeedBridge", "Breezily", "JitterBridge", "TellyBridge"), "Simple")

    val safewalkValue = ListValue("SafewalkType", arrayOf("Sneak", "Safewalk", "None"), "Safewalk").displayable { modeValue.equals("Simple") }
    val derpValue = BoolValue("SimpleDerpBridge", false).displayable { modeValue.equals("Simple") }


    private var playerRot = Rotation(0f, 0f)
    private var oldPlayerRot = Rotation(0f, 0f)
    private var lockRotation = Rotation(0f, 0f)
    private var camYaw = 0f
    private var camPitch = 0f

    private var prevSlot = 0

    private var fw = false
    private var bw = false
    private var left = false
    private var right = false

    private var breezily = false

    private val currentBlock: BlockPos?
        get() {
            val entity = mc.getRenderViewEntity()
            val blockPos = entity.rayTrace(4.0, mc.timer.renderPartialTicks)?.blockPos?: return null

            if (BlockUtils.canBeClicked(blockPos) && mc.theWorld.worldBorder.contains(blockPos)) {
                return blockPos
            }

            return null
        }

    override fun onEnable() {
        FDPClient.moduleManager[FreeLook::class.java]!!.enable()
        prevSlot = mc.thePlayer.inventory.currentItem
    }

    override fun onDisable() {
        FDPClient.moduleManager[FreeLook::class.java]!!.disable()
        mc.thePlayer.inventory.currentItem = prevSlot

        correctControls(0)
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        mc.gameSettings.keyBindUseItem.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
        mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
    }



    // safewalk events
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (modeValue.equals("Simple")) {
            if (safewalkValue.equals("Safewalk")) {
                event.isSafeWalk = true
            }
        }
    }

    @EventTarget
    fun onUpdate(e: UpdateEvent) {

        val blockSlot = InventoryUtils.findAutoBlockBlock()
        if (blockSlot != -1) {
            mc.thePlayer.inventory.currentItem = blockSlot - 36
            mc.rightClickDelayTimer = 1
            mc.gameSettings.keyBindUseItem.pressed = true
        }


        camYaw = FreeLook.cameraYaw
        camPitch = FreeLook.cameraPitch

        oldPlayerRot = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        when (modeValue.get().lowercase()) {
            "breezily" -> {
                var rpitch = 0f
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    rpitch = 79.6f
                } else {
                    rpitch = 76.3f
                }

                playerRot = Rotation(camYaw + 180f, rpitch)
                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 60f)

                correctControls(1)
                mc.gameSettings.keyBindRight.pressed = false
                mc.gameSettings.keyBindLeft.pressed = false

                if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air && ((camYaw / 45).roundToInt()) % 2 == 0) {
                    breezily = !breezily
                    mc.gameSettings.keyBindRight.pressed = breezily
                    mc.gameSettings.keyBindLeft.pressed = !breezily
                    // zig zag jitter
                }
            }
            "simple" -> {

                // Rotation stuff
                var rpitch = 0f
                Rotation(camYaw + 180, 0f).toPlayer(mc.thePlayer)
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    if (safewalkValue.equals("None")) {
                        rpitch = 79f
                    } else {
                        rpitch = getPitchRot()
                    }
                } else {
                    if (safewalkValue.equals("None")) {
                        rpitch = 76.3f
                    } else {
                        rpitch = getPitchRot()
                    }
                }

                // Applying rotations
                if (derpValue.get()) {
                    if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air) {
                        playerRot = Rotation(camYaw + 180f, rpitch)
                    } else {
                        if (mc.thePlayer.onGround && mc.gameSettings.keyBindJump.pressed) {
                            playerRot = Rotation(camYaw + 31, rpitch) // jump correction
                        } else {
                            playerRot = Rotation(camYaw + 45, rpitch) // normal derp
                        }
                    }

                    lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 180f)
                } else {
                    playerRot = Rotation(camYaw + 180f, rpitch)
                    lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 90f)
                }


                // Controls correction
                if (derpValue.get()) {
                    if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air) {
                        correctControls(1)
                    } else {
                        correctControls(2)
                    }
                } else {
                    correctControls(1)
                }

                // eagle
                if (safewalkValue.equals("Sneak")) {
                    mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air)
                }
            }
            "speedbridge" -> {
                var rpitch = 0.0
                if (((camYaw / 15).roundToInt()) % 6 == 0) {
                    rpitch = 78.7
                } else  {
                    rpitch = 78.9
                }

                if (rpitch == 78.7) {
                    playerRot = Rotation(camYaw - 135, rpitch.toFloat())
                    correctControls(3)
                } else {
                    playerRot = Rotation(camYaw - 180, rpitch.toFloat())
                    correctControls(1)
                }

                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 90f)

                mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air)
            }
            "jitterbridge" -> {
                Rotation(camYaw + 180, 0f).toPlayer(mc.thePlayer)
                var rpitch = getPitchRot()

                playerRot = Rotation(camYaw + 180, rpitch)
                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 80f)

                correctControls(1)

                mc.gameSettings.keyBindJump.pressed = true
            }
            "tellybridge" -> {

                if (mc.thePlayer.onGround) {
                    playerRot = Rotation(camYaw, 80f)
                    correctControls(0)
                } else {
                    Rotation(camYaw + 180, 0f).toPlayer(mc.thePlayer)
                    var rpitch = getPitchRot()
                    playerRot = Rotation(camYaw + 180, rpitch)
                    correctControls(1)
                }

                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 180f)

                mc.gameSettings.keyBindJump.pressed = true
            }
        }

        lockRotation.toPlayer(mc.thePlayer)
    }

    private fun correctControls(type: Int) {
        fw =  GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
        bw = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
        right = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
        left = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
        when (type) {
           0 -> {
               mc.gameSettings.keyBindForward.pressed = fw
               mc.gameSettings.keyBindBack.pressed = bw
               mc.gameSettings.keyBindRight.pressed = right
               mc.gameSettings.keyBindLeft.pressed = left
           }
           1 -> {
               mc.gameSettings.keyBindForward.pressed = bw
               mc.gameSettings.keyBindBack.pressed = fw
               mc.gameSettings.keyBindRight.pressed = left
               mc.gameSettings.keyBindLeft.pressed = right
           }
           2 -> {
               mc.gameSettings.keyBindForward.pressed = fw || right
               mc.gameSettings.keyBindBack.pressed = left || bw
               mc.gameSettings.keyBindRight.pressed = right || bw
               mc.gameSettings.keyBindLeft.pressed = fw || left
           }
           3 -> {
               mc.gameSettings.keyBindForward.pressed = left || bw
               mc.gameSettings.keyBindBack.pressed = fw || right
               mc.gameSettings.keyBindRight.pressed = fw || left
               mc.gameSettings.keyBindLeft.pressed = right || bw
           }
        }
    }

    private fun getPitchRot(): Float {
        var rpitch = 90f
        Rotation(mc.thePlayer.rotationYaw, rpitch).toPlayer(mc.thePlayer)
        while (currentBlock == null && rpitch > 0f) {
            rpitch -= 0.03f
            Rotation(mc.thePlayer.rotationYaw, rpitch).toPlayer(mc.thePlayer)
        }
        if (currentBlock == null) {
            return 80f
        }

        return rpitch
    }
}