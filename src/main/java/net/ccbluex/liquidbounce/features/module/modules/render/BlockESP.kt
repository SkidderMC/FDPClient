/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import java.awt.Color

@ModuleInfo(name = "BlockESP", category = ModuleCategory.RENDER)
class BlockESP : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "2D"), "Box")
    private val outlineWidthValue = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    private val blockValue = BlockValue("Block", 168)
    private val radiusValue = IntegerValue("Radius", 40, 5, 120)
    private val colorRedValue = IntegerValue("R", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorGreenValue = IntegerValue("G", 179, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorBlueValue = IntegerValue("B", 72, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorRainbowValue = BoolValue("Rainbow", false)
    private val searchTimer = MSTimer()
    private val posList: MutableList<BlockPos> = ArrayList()
    private var color = Color.CYAN
    private var thread: Thread? = null
    private val waterMode = ListValue("WaterMode", arrayOf("Box", "2D"), "Box")
    private val waterRadiusValue = IntegerValue("WaterRadius", 40, 5, 120)
    private val waterLimitValue = IntegerValue("WaterLimit", 128, 0, 1024)
    private val waterRedValue = IntegerValue("WaterR", 52, 0, 255)
    private val waterGreenValue = IntegerValue("WaterG", 129, 0, 255)
    private val waterBlueValue = IntegerValue("WaterB", 184, 0, 255)
    private val waterRainbow = BoolValue("WaterRainbow", false)
    private val waterEnable
        get() = waterLimitValue.get() != 0
    private val waterSearchTimer = MSTimer()
    private val waterPosList: MutableList<BlockPos> = ArrayList()
    private var waterThread: Thread? = null

    private val lavaMode = ListValue("LavaMode", arrayOf("Box", "2D"), "Box")
    private val lavaRadiusValue = IntegerValue("LavaRadius", 40, 5, 120)
    private val lavaLimitValue = IntegerValue("LavaLimit", 128, 0, 1024)
    private val lavaRedValue = IntegerValue("LavaR", 250, 0, 255)
    private val lavaGreenValue = IntegerValue("LavaG", 132, 0, 255)
    private val lavaBlueValue = IntegerValue("LavaB", 43, 0, 255)
    private val lavaRainbow = BoolValue("LavaRainbow", false)

    private val lavaEnable
        get() = lavaLimitValue.get() != 0
    private val lavaSearchTimer = MSTimer()
    private val lavaPosList: MutableList<BlockPos> = ArrayList()
    private var lavaThread: Thread? = null

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        color = if (colorRainbowValue.get()) rainbow() else Color(
            colorRedValue.get(),
            colorGreenValue.get(),
            colorBlueValue.get()
        )
        if (searchTimer.hasTimePassed(1000L) && (thread == null || !thread!!.isAlive)) {
            val radius = radiusValue.get()
            val selectedBlock = Block.getBlockById(blockValue.get())
            if (selectedBlock == null || selectedBlock === Blocks.air) return
            thread = Thread({
                val blockList: MutableList<BlockPos> = ArrayList()
                for (x in -radius until radius) {
                    for (y in radius downTo -radius + 1) {
                        for (z in -radius until radius) {
                            val xPos = mc.thePlayer.posX.toInt() + x
                            val yPos = mc.thePlayer.posY.toInt() + y
                            val zPos = mc.thePlayer.posZ.toInt() + z
                            val blockPos = BlockPos(xPos, yPos, zPos)
                            val block = getBlock(blockPos)
                            if (block === selectedBlock) blockList.add(blockPos)
                        }
                    }
                }
                searchTimer.reset()
                synchronized(posList) {
                    posList.clear()
                    posList.addAll(blockList)
                }
            }, "BlockESP-BlockFinder")
            thread!!.start()
        }

        if(waterEnable) if (waterSearchTimer.hasTimePassed(1000L) && (waterThread == null || !waterThread!!.isAlive)) {
            val radius = waterRadiusValue.get()
            waterThread = Thread({
                val blockList: MutableList<BlockPos> = ArrayList()
                for (x in -radius until radius) {
                    for (y in radius downTo -radius + 1) {
                        for (z in -radius until radius) {
                            val thePlayer = mc.thePlayer ?: return@Thread
                            val xPos = thePlayer.posX.toInt() + x
                            val yPos = thePlayer.posY.toInt() + y
                            val zPos = thePlayer.posZ.toInt() + z
                            val blockPos = BlockPos(xPos, yPos, zPos)
                            val block = BlockUtils.getBlock(blockPos)
                            if (block == Blocks.water && (mc.theWorld?.getBlockState(blockPos)?.getValue(BlockLiquid.LEVEL) ?: 1) == 0 && blockList.size < waterLimitValue.get()) blockList.add(blockPos)
                        }
                    }
                }
                waterSearchTimer.reset()
                synchronized(waterPosList) {
                    waterPosList.clear()
                    waterPosList.addAll(blockList)
                }
            }, "BlockESP-WaterFinder")
            waterThread!!.start()
        }

        if(lavaEnable) if (lavaSearchTimer.hasTimePassed(1000L) && (lavaThread == null || !lavaThread!!.isAlive)) {
            val radius = lavaRadiusValue.get()
            lavaThread = Thread({
                val blockList: MutableList<BlockPos> = ArrayList()
                for (x in -radius until radius) {
                    for (y in radius downTo -radius + 1) {
                        for (z in -radius until radius) {
                            val thePlayer = mc.thePlayer ?: return@Thread
                            val xPos = thePlayer.posX.toInt() + x
                            val yPos = thePlayer.posY.toInt() + y
                            val zPos = thePlayer.posZ.toInt() + z
                            val blockPos = BlockPos(xPos, yPos, zPos)
                            val block = BlockUtils.getBlock(blockPos)
                            if (block == Blocks.lava && (mc.theWorld?.getBlockState(blockPos)?.getValue(BlockLiquid.LEVEL) ?: 1) == 0 && blockList.size < lavaLimitValue.get()) blockList.add(blockPos)
                        }
                    }
                }
                lavaSearchTimer.reset()
                synchronized(lavaPosList) {
                    lavaPosList.clear()
                    lavaPosList.addAll(blockList)
                }
            }, "BlockESP-LavaFinder")
            lavaThread!!.start()
        }
        
    }
    

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        synchronized(posList) {
            for (blockPos in posList) {
                when (modeValue.get().lowercase()) {
                    "box" -> {
                        RenderUtils.drawBlockBox(blockPos, color, true, true, outlineWidthValue.get())
                    }
                    "otherbox" -> {
                        RenderUtils.drawBlockBox(blockPos, color, false, true, outlineWidthValue.get())
                    }
                    "outline" -> {
                        RenderUtils.drawBlockBox(blockPos, color, true, false, outlineWidthValue.get())
                    }
                    "2d" -> {
                        RenderUtils.draw2D(blockPos, color.rgb, Color.BLACK.rgb)
                    }
                }
            }

            if (waterEnable) synchronized(waterPosList) {
                for (blockPos in waterPosList) {
                    val color = if (waterRainbow.get()) ColorUtils.rainbow() else Color(waterRedValue.get(), waterGreenValue.get(), waterBlueValue.get())
                    when (waterMode.get().toLowerCase()) {
                        "box" -> RenderUtils.drawBlockBox(blockPos, color, true)
                        "2d" -> RenderUtils.draw2D(blockPos, color.rgb, Color.BLACK.rgb)
                    }
                }
            }

            if (lavaEnable) synchronized(lavaPosList) {
                for (blockPos in lavaPosList) {
                    val color = if (lavaRainbow.get()) ColorUtils.rainbow() else Color(lavaRedValue.get(), lavaGreenValue.get(), lavaBlueValue.get())
                    when (lavaMode.get().toLowerCase()) {
                        "box" -> RenderUtils.drawBlockBox(blockPos, color, true)
                        "2d" -> RenderUtils.draw2D(blockPos, color.rgb, Color.BLACK.rgb)
                    }
                }
            }
        }
    }

    override val tag: String
        get() = getBlockName(blockValue.get())
}
