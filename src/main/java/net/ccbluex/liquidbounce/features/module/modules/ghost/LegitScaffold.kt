/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

/*
    Made by: Dg636
    2/12/24
 */
package net.ccbluex.liquidbounce.features.module.modules.ghost

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeLook
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo(name = "LegitScaffold",  category = ModuleCategory.GHOST)
object LegitScaffold : Module() {
    val modeValue = ListValue("Mode", arrayOf("Simple", "SpeedBridge", "Breezily", "JitterBridge", "TellyBridge","GodBridge"), "Simple")

    val safewalkValue = ListValue("SafewalkType", arrayOf("Sneak", "Safewalk", "None"), "Safewalk").displayable { modeValue.equals("Simple") }
    val derpValue = BoolValue("SimpleDerpBridge", false).displayable { modeValue.equals("Simple") }

    // Sprint
    val sprintValue = ListValue("Sprint-Mode", arrayOf("Always", "NoSpeedPot", "OFF"), "Always")

    // Visuals
    private val counter = BoolValue("Counter", true)
    private val counterDisplayValue = ListValue("Counter-Mode", arrayOf("FDP", "Rise", "Simple"), "FDP").displayable { counter.get() }
    private val barrier = ItemStack(Item.getItemById(166), 0, 0)


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
                        rpitch = 83.2f
                    }
                } else {
                    if (safewalkValue.equals("None")) {
                        rpitch = 76.3f
                    } else {
                        rpitch = 78.1f
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

            "godbridge" -> {
                var rpitch = 0.0
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    rpitch = 75.8
                } else  {
                    rpitch = 75.6
                }

                if (rpitch == 75.0) {
                    playerRot = Rotation(((camYaw / 45).roundToInt() * 45f) - 135, rpitch.toFloat())
                    correctControls(3)
                } else {
                    playerRot = Rotation(((camYaw / 45).roundToInt() * 45f) - 180, rpitch.toFloat())
                    correctControls(1)
                }

                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 80f)
            }
            "jitterbridge" -> {
                var rpitch = 0f
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    rpitch = 77.4f
                } else  {
                    rpitch = 77.1f
                }

                playerRot = Rotation(camYaw + 180, rpitch)
                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 80f)

                correctControls(1)

                mc.gameSettings.keyBindJump.pressed = true
            }
            "tellybridge" -> {

                var rpitch = 0f
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    rpitch = 75.1f
                } else  {
                    rpitch = 75.5f
                }

                if (mc.thePlayer.onGround) {
                    playerRot = Rotation(camYaw, 80f)
                    correctControls(0)
                } else {
                    Rotation(camYaw + 180, 0f).toPlayer(mc.thePlayer)
                    playerRot = Rotation(camYaw + 180, rpitch)
                    correctControls(1)
                }

                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 180f)

                mc.gameSettings.keyBindJump.pressed = true
            }
        }

        lockRotation.toPlayer(mc.thePlayer)
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val scaledResolution = ScaledResolution(mc)
        if (counter.get()) {
            when (counterDisplayValue.get().lowercase()) {
                "fdp" -> {
                    GlStateManager.pushMatrix()
                    val info = LanguageManager.getAndFormat("ui.scaffold.blocks", blocksAmount)
                    val slot = InventoryUtils.findAutoBlockBlock()
                    val height = event.scaledResolution.scaledHeight
                    val width = event.scaledResolution.scaledWidth
                    val w2 = (mc.fontRendererObj.getStringWidth(info))
                    RenderUtils.drawRoundedCornerRect(
                        (width - w2 - 20) / 2f,
                        height * 0.8f - 24f,
                        (width + w2 + 18) / 2f,
                        height * 0.8f + 12f,
                        3f,
                        Color(43, 45, 48).rgb
                    )
                    mc.fontRendererObj.drawCenteredString(
                        "▼",
                        width / 2.0f + 2f,
                        height * 0.8f + 8f,
                        Color(43, 45, 48).rgb
                    )
                    var stack = barrier
                    if (slot != -1) {
                        if (mc.thePlayer.inventory.getCurrentItem() != null) {
                            val handItem = mc.thePlayer.inventory.getCurrentItem().item
                            if (handItem is ItemBlock && InventoryUtils.canPlaceBlock(handItem.block)) {
                                stack = mc.thePlayer.inventory.getCurrentItem()
                            }
                        }
                        if (stack == barrier) {
                            stack = mc.thePlayer.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                            if (stack == null) {
                                stack = barrier
                            }
                        }
                    }

                    RenderHelper.enableGUIStandardItemLighting()
                    mc.renderItem.renderItemIntoGUI(stack, width / 2 - 9, (height * 0.8 - 20).toInt())
                    RenderHelper.disableStandardItemLighting()
                    mc.fontRendererObj.drawCenteredString(info, width / 2f, height * 0.8f, Color.WHITE.rgb, false)
                    GlStateManager.popMatrix()
                }

                "rise" -> {
                    GlStateManager.pushMatrix()
                    val info = blocksAmount.toString()
                    val slot = InventoryUtils.findAutoBlockBlock()
                    val height = event.scaledResolution.scaledHeight
                    val width = event.scaledResolution.scaledWidth
                    val w2 = (mc.fontRendererObj.getStringWidth(info))
                    RenderUtils.drawRoundedCornerRect(
                        (width - w2 - 20) / 2f,
                        height * 0.8f - 24f,
                        (width + w2 + 18) / 2f,
                        height * 0.8f + 12f,
                        5f,
                        Color(20, 20, 20, 100).rgb
                    )
                    var stack = barrier
                    if (slot != -1) {
                        if (mc.thePlayer.inventory.getCurrentItem() != null) {
                            val handItem = mc.thePlayer.inventory.getCurrentItem().item
                            if (handItem is ItemBlock && InventoryUtils.canPlaceBlock(handItem.block)) {
                                stack = mc.thePlayer.inventory.getCurrentItem()
                            }
                        }
                        if (stack == barrier) {
                            stack = mc.thePlayer.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                            if (stack == null) {
                                stack = barrier
                            }
                        }
                    }

                    RenderHelper.enableGUIStandardItemLighting()
                    mc.renderItem.renderItemIntoGUI(stack, width / 2 - 9, (height * 0.8 - 20).toInt())
                    RenderHelper.disableStandardItemLighting()
                    mc.fontRendererObj.drawCenteredString(info, width / 2f, height * 0.8f, Color.WHITE.rgb, false)
                    GlStateManager.popMatrix()
                }

                "simple" -> {
                    Fonts.minecraftFont.drawString(
                        blocksAmount.toString() + " Blocks",
                        scaledResolution.scaledWidth / 1.95f,
                        (scaledResolution.scaledHeight / 2 + 20).toFloat(),
                        -1,
                        true
                    )
                }
            }
        }
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

    val canSprint: Boolean
        get() = MovementUtils.isMoving() && when (sprintValue.get().lowercase()) {
            "always" -> true
            "nospeedpot" -> !mc.thePlayer.isPotionActive(Potion.moveSpeed)
            else -> false
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

    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock && InventoryUtils.canPlaceBlock((itemStack.item as ItemBlock).block)) {
                    amount += itemStack.stackSize
                }
            }
            return amount
        }
}