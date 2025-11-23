/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
@file:Suppress("unused")
package net.ccbluex.liquidbounce.features.module.modules.other

import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoArmor
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner.canBeSortedTo
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner.isStackUseful
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.chestStealerCurrentSlot
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.chestStealerLastSlot
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.countSpaceInInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.hasSpaceInInventory
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.timing.TickedActions.awaitTicked
import net.ccbluex.liquidbounce.utils.timing.TickedActions.clickNextTick
import net.ccbluex.liquidbounce.utils.timing.TickedActions.isTicked
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.EntityLiving.getArmorPosition
import net.minecraft.init.Blocks
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraft.util.BlockPos
import net.minecraft.block.BlockContainer
import net.minecraft.client.gui.inventory.GuiFurnace
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.client.renderer.GlStateManager.enableDepth
import net.minecraft.client.renderer.GlStateManager.popMatrix
import net.minecraft.client.renderer.GlStateManager.pushMatrix
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.inventory.ContainerFurnace
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S2FPacketSetSlot
import org.lwjgl.BufferUtils
import org.lwjgl.BufferUtils.createFloatBuffer
import org.lwjgl.util.glu.GLU
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS
import org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX
import org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX
import org.lwjgl.opengl.GL11.GL_VIEWPORT
import org.lwjgl.opengl.GL11.glGetFloat
import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL11.glPopAttrib
import org.lwjgl.opengl.GL11.glPushAttrib
import java.awt.Color
import kotlin.math.sqrt

object ChestStealer : Module("ChestStealer", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val smartDelay by boolean("SmartDelay", false)
    private val multiplier by int("DelayMultiplier", 120, 0..500) { smartDelay }
    private val smartOrder by boolean("SmartOrder", true) { smartDelay }

    private val simulateShortStop by boolean("SimulateShortStop", false)

    private val delay by intRange("Delay", 50..50, 0..500) { !smartDelay }
    private val startDelay by intRange("StartDelay", 50..100, 0..500)
    private val closeDelay by intRange("CloseDelay", 50..100, 0..500)

    private val noMove by +InventoryManager.noMoveValue
    private val noMoveAir by +InventoryManager.noMoveAirValue
    private val noMoveGround by +InventoryManager.noMoveGroundValue

    private val chestTitle by boolean("ChestTitle", true)

    private val randomSlot by boolean("RandomSlot", true)

    private val progressBar by boolean("ProgressBar", true).subjective()

    val silentGUI by boolean("SilentGUI", false).subjective()

    val silentView by boolean("SilentView", false)

    private val furnace by boolean("Furnace", false)

    val highlightSlot by boolean("Highlight-Slot", false) { !silentGUI }.subjective()
    val backgroundColor =
        color("BackgroundColor", Color(128, 128, 128)) { highlightSlot && !silentGUI }.subjective()

    val borderStrength by int("Border-Strength", 3, 1..5) { highlightSlot && !silentGUI }.subjective()
    val borderColor = color("BorderColor", Color(128, 128, 128)) { highlightSlot && !silentGUI }.subjective()

    private val chestDebug by choices("Chest-Debug", arrayOf("Off", "Text", "Notification"), "Off").subjective()
    private val itemStolenDebug by boolean("ItemStolen-Debug", false) { chestDebug != "Off" }.subjective()

    private var progress: Float? = null
        set(value) {
            field = value?.coerceIn(0f, 1f)

            if (field == null)
                easingProgress = 0f
        }

    private var easingProgress = 0f

    private var receivedId: Int? = null

    private var stacks = emptyList<ItemStack?>()

    private var currentContainerPos: BlockPos? = null

    private val MV  = createFloatBuffer(16)
    private val PRJ = createFloatBuffer(16)
    private val VP  = BufferUtils.createIntBuffer(16)
    private val OBJ = createFloatBuffer(3)

    private fun projectToScreen(x: Double, y: Double, z: Double, scaleFactor: Int): FloatArray? {
        MV.clear(); PRJ.clear(); VP.clear(); OBJ.clear()
        glGetFloat(GL_MODELVIEW_MATRIX,  MV)
        glGetFloat(GL_PROJECTION_MATRIX, PRJ)
        glGetInteger(GL_VIEWPORT,        VP)

        val ok = GLU.gluProject(
            x.toFloat(), y.toFloat(), z.toFloat(),
            MV, PRJ, VP, OBJ
        )

        return if (ok) {
            floatArrayOf(
                OBJ.get(0) / scaleFactor,
                (Display.getHeight() - OBJ.get(1)) / scaleFactor,
                OBJ.get(2)
            )
        } else null
    }

    private fun screenPointFor(blockPos: BlockPos, scaleFactor: Int): FloatArray? {
        val rm = mc.renderManager ?: return null
        val rx = rm.renderPosX
        val ry = rm.renderPosY
        val rz = rm.renderPosZ
        val cx = blockPos.x + 0.5 - rx
        val cy = blockPos.y + 0.5 - ry
        val cz = blockPos.z + 0.5 - rz

        val p = projectToScreen(cx, cy, cz, scaleFactor) ?: return null

        if (p[2] < 0f || p[2] > 1f) return null
        return p
    }

    private suspend fun shouldOperate(): Boolean {
        while (true) {
            if (!handleEvents())
                return false

            if (mc.playerController?.currentGameType?.isSurvivalOrAdventure != true)
                return false

            if (mc.currentScreen !is GuiChest)
                return false

            if (mc.thePlayer?.openContainer?.windowId != receivedId)
                return false

            // Wait till NoMove check isn't violated
            if (canClickInventory())
                return true

            // If NoMove is violated, wait a tick and check again
            // If there is no delay, very weird things happen: https://www.guilded.gg/CCBlueX/groups/1dgpg8Jz/channels/034be45e-1b72-4d5a-bee7-d6ba52ba1657/chat?messageId=94d314cd-6dc4-41c7-84a7-212c8ea1cc2a
            delay(50)
        }
    }

    suspend fun stealFromChest() {
        if (!handleEvents())
            return

        val thePlayer = mc.thePlayer ?: return

        val screen = mc.currentScreen ?: return

        if (furnace && screen is GuiFurnace) {
            val container = mc.thePlayer.openContainer as? ContainerFurnace ?: return

            val slots = container.inventorySlots
            var hasTaken = false

            for (i in slots.indices) {
                val slot = slots[i]
                val stack = slot.stack ?: continue

                clickNextTick(i, 0, 1)
                delay(delay.random().toLong())
                hasTaken = true
            }

            if (!hasTaken) {
                mc.thePlayer.closeScreen()
            }
        }

        if (screen !is GuiChest || !shouldOperate())
            return

        // Check if chest isn't a custom gui
        if (chestTitle && Blocks.chest.localizedName !in (screen.lowerChestInventory ?: return).name)
            return

        progress = 0f

        delay(startDelay.random().toLong())

        debug("Stealing items..")

        // Go through the chest multiple times, till there are no useful items anymore
        while (true) {
            if (!shouldOperate())
                return

            if (!hasSpaceInInventory())
                return

            var hasTaken = false

            val itemsToSteal = getItemsToSteal()

            run scheduler@{
                itemsToSteal.forEachIndexed { index, (slot, stack, sortableTo) ->
                    // Wait for NoMove or cancel click
                    if (!shouldOperate()) {
                        nextTick { SilentHotbar.resetSlot() }
                        chestStealerCurrentSlot = -1
                        chestStealerLastSlot = -1
                        return
                    }

                    if (!hasSpaceInInventory()) {
                        chestStealerCurrentSlot = -1
                        chestStealerLastSlot = -1
                        return@scheduler
                    }

                    hasTaken = true

                    // Set current slot being stolen for highlighting
                    chestStealerCurrentSlot = slot

                    val stealingDelay = if (smartDelay && index + 1 < itemsToSteal.size) {
                        val dist = squaredDistanceOfSlots(slot, itemsToSteal[index + 1].index)
                        val trueDelay = sqrt(dist.toDouble()) * multiplier
                        randomDelay(trueDelay.toInt(), trueDelay.toInt() + 20)
                    } else {
                        delay.random()
                    }

                    if (itemStolenDebug) debug("item: ${stack.displayName.lowercase()} | slot: $slot | delay: ${stealingDelay}ms")

                    // If target is sortable to a hotbar slot, steal and sort it at the same time, else shift + left-click
                    clickNextTick(slot, sortableTo ?: 0, if (sortableTo != null) 2 else 1) {
                        progress = (index + 1) / itemsToSteal.size.toFloat()

                        if (!AutoArmor.canEquipFromChest())
                            return@clickNextTick

                        val item = stack.item

                        if (item !is ItemArmor || thePlayer.inventory.armorInventory[getArmorPosition(stack) - 1] != null)
                            return@clickNextTick

                        // TODO: should the stealing be suspended until the armor gets equipped and some delay on top of that, maybe toggleable?
                        // Try to equip armor piece from hotbar 1 tick after stealing it
                        nextTick {
                            val hotbarStacks = thePlayer.inventory.mainInventory.take(9)

                            // Can't get index of stack instance, because it is different even from the one returned from windowClick()
                            val newIndex = hotbarStacks.indexOfFirst { it?.getIsItemStackEqual(stack) == true }

                            if (newIndex != -1)
                                AutoArmor.equipFromHotbarInChest(newIndex, stack)
                        }
                    }

                    delay(stealingDelay.toLong())

                    if (simulateShortStop && Math.random() > 0.75) {
                        val minDelays = randomDelay(150, 300)
                        val maxDelays = randomDelay(minDelays, 500)
                        val randomDelay = randomDelay(minDelays, maxDelays).toLong()

                        delay(randomDelay)
                    }
                }
            }

            // If no clicks were sent in the last loop stop searching
            if (!hasTaken) {
                progress = 1f
                delay(closeDelay.random().toLong())

                nextTick { SilentHotbar.resetSlot() }
                break
            }

            // Wait till all scheduled clicks were sent
            awaitTicked()

            // Before closing the chest, check all items once more, whether server hadn't cancelled some of the actions.
            stacks = thePlayer.openContainer.inventory
        }

        // Wait before the chest gets closed (if it gets closed out of tick loop it could throw npe)
        nextTick {
            chestStealerCurrentSlot = -1
            chestStealerLastSlot = -1
            thePlayer.closeScreen()
            progress = null

            debug("Chest closed")
        }

        awaitTicked()
    }

    private fun squaredDistanceOfSlots(from: Int, to: Int): Int {
        fun getCoords(slot: Int): IntArray {
            val x = slot % 9
            val y = slot / 9
            return intArrayOf(x, y)
        }

        val (x1, y1) = getCoords(from)
        val (x2, y2) = getCoords(to)
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)
    }

    private data class ItemTakeRecord(
        val index: Int,
        val stack: ItemStack,
        val sortableToSlot: Int?
    )

    private fun getItemsToSteal(): List<ItemTakeRecord> {
        val sortBlacklist = BooleanArray(9)

        var spaceInInventory = countSpaceInInventory()

        val itemsToSteal = stacks.dropLast(36)
            .mapIndexedNotNullTo(ArrayList(32)) { index, stack -> stack ?: return@mapIndexedNotNullTo null

                if (isTicked(index)) return@mapIndexedNotNullTo null

                val mergeableCount = mc.thePlayer.inventory.mainInventory.sumOf { otherStack -> otherStack ?: return@sumOf 0

                    if (otherStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, otherStack))
                        otherStack.maxStackSize - otherStack.stackSize
                    else 0
                }

                val canMerge = mergeableCount > 0
                val canFullyMerge = mergeableCount >= stack.stackSize

                // Clicking this item wouldn't take it from chest or merge it
                if (!canMerge && spaceInInventory <= 0) return@mapIndexedNotNullTo null

                // If stack can be merged without occupying any additional slot, do not take stack limits into account
                // TODO: player could theoretically already have too many stacks in inventory before opening the chest so no more should even get merged
                // TODO: if it can get merged but would also need another slot, it could simulate 2 clicks, one which maxes out the stack in inventory and second that puts excess items back
                if (InventoryCleaner.handleEvents() && !isStackUseful(stack, stacks, noLimits = canFullyMerge))
                    return@mapIndexedNotNullTo null

                var sortableTo: Int? = null

                // If stack can get merged, do not try to sort it, normal shift + left-click will merge it
                if (!canMerge && InventoryCleaner.handleEvents() && InventoryCleaner.sort) {
                    for (hotbarIndex in 0..8) {
                        if (sortBlacklist[hotbarIndex])
                            continue

                        if (!canBeSortedTo(hotbarIndex, stack.item))
                            continue

                        val hotbarStack = stacks.getOrNull(stacks.size - 9 + hotbarIndex)

                        // If occupied hotbar slot isn't already sorted or isn't strictly best, sort to it
                        if (!canBeSortedTo(hotbarIndex, hotbarStack?.item) || !isStackUseful(
                                hotbarStack,
                                stacks,
                                strictlyBest = true
                            )
                        ) {
                            sortableTo = hotbarIndex
                            sortBlacklist[hotbarIndex] = true
                            break
                        }
                    }
                }

                // If stack gets fully merged, no slot in inventory gets occupied
                if (!canFullyMerge) spaceInInventory--

                ItemTakeRecord(index, stack, sortableTo)
            }.also { it -> if (randomSlot) it.shuffle()

                // Prioritise armor pieces with lower priority, so that as many pieces can get equipped from hotbar after chest gets closed
                it.sortByDescending { it.stack.item is ItemArmor }

                // Prioritize items that can be sorted
                it.sortByDescending { it.sortableToSlot != null }

                // Fully prioritise armor pieces when it is possible to equip armor while in chest
                if (AutoArmor.canEquipFromChest())
                    it.sortByDescending { it.stack.item is ItemArmor }

                if (smartOrder) {
                    sortBasedOnOptimumPath(it)
                }
            }

        return itemsToSteal
    }

    private fun sortBasedOnOptimumPath(itemsToSteal: MutableList<ItemTakeRecord>) {
        for (i in itemsToSteal.indices) {
            var nextIndex = i
            var minDistance = Int.MAX_VALUE
            var next: ItemTakeRecord? = null
            for (j in i + 1 until itemsToSteal.size) {
                val distance = squaredDistanceOfSlots(itemsToSteal[i].index, itemsToSteal[j].index)
                if (distance < minDistance) {
                    minDistance = distance
                    next = itemsToSteal[j]
                    nextIndex = j
                }
            }
            if (next != null) {
                itemsToSteal[nextIndex] = itemsToSteal[i + 1]
                itemsToSteal[i + 1] = next
            }
        }
    }

    // Progress bar
    val onRender2D = handler<Render2DEvent> { event ->
        if (!progressBar || mc.currentScreen !is GuiChest)
            return@handler

        val progress = progress ?: return@handler

        val (scaledWidth, scaledHeight) = ScaledResolution(mc)

        val minX = scaledWidth * 0.3f
        val maxX = scaledWidth * 0.7f
        val minY = scaledHeight * 0.75f
        val maxY = minY + 10f

        easingProgress += (progress - easingProgress) / 6f * event.partialTicks

        drawRect(minX - 2, minY - 2, maxX + 2, maxY + 2, Color(200, 200, 200).rgb)
        drawRect(minX, minY, maxX, maxY, Color(50, 50, 50).rgb)
        drawRect(
            minX,
            minY,
            minX + (maxX - minX) * easingProgress,
            maxY,
            Color.HSBtoRGB(easingProgress / 5, 1f, 1f) or 0xFF0000
        )
    }

    val onPacket = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            is S2DPacketOpenWindow -> {
                receivedId = null
                progress = null

                if (furnace && packet.windowTitle.unformattedText.lowercase().contains("furnace")) {
                    receivedId = packet.windowId
                    stacks = emptyList()
                }
            }

            is C0DPacketCloseWindow, is S2EPacketCloseWindow -> {
                receivedId = null
                progress = null
                currentContainerPos = null
            }

            is S30PacketWindowItems -> {
                // Chests never have windowId 0
                val packetWindowId = packet.func_148911_c()

                if (packetWindowId == 0)
                    return@handler

                if (receivedId != packetWindowId) {
                    debug("Chest opened with ${stacks.size} items")
                }

                receivedId = packetWindowId

                stacks = packet.itemStacks.toList()
            }

            is S2FPacketSetSlot -> {
                val wid = packet.func_149175_c()
                if (wid != receivedId) return@handler

                val slot = packet.func_149173_d()
                if (slot < 0) return@handler

                val list = stacks.toMutableList()
                while (slot >= list.size) list.add(null)
                list[slot] = packet.func_149174_e()
                stacks = list
            }

            is C08PacketPlayerBlockPlacement -> {
                val pos = packet.position ?: return@handler
                val state = mc.theWorld?.getBlockState(pos) ?: return@handler
                if (state.block is BlockContainer) {
                    currentContainerPos = pos
                }
            }
        }
    }

    private fun fallbackContainerPos(): BlockPos? {
        val om = mc.objectMouseOver ?: return null
        val pos = om.blockPos ?: return null
        val state = mc.theWorld?.getBlockState(pos) ?: return null
        return if (state.block is BlockContainer) pos else null
    }

    val onRender2DSilentView = handler<Render2DEvent> { _ ->
        if (!silentView) return@handler

        if (mc.currentScreen !is GuiChest) return@handler
        val player = mc.thePlayer ?: return@handler
        val container = player.openContainer ?: return@handler
        if (progress == null) return@handler

        val pos = currentContainerPos ?: fallbackContainerPos() ?: return@handler

        val sr = ScaledResolution(mc)
        val sf = sr.scaleFactor
        val centerX = sr.scaledWidth / 2f
        val centerY = sr.scaledHeight / 2f

        val chestSlots = container.inventorySlots.filter { it.inventory != player.inventory }
        if (chestSlots.isEmpty()) return@handler

        val columns = 9
        val rows = (chestSlots.size + 8) / 9
        val slotSize = 18
        val pad = 5

        val boxW = (columns * slotSize + pad * 2).toFloat()
        val boxH = (rows * slotSize + pad * 2).toFloat()

        fun clamp(v: Float, a: Float, b: Float) = v.coerceIn(a, b)
        var x0 = centerX - boxW / 2f
        var y0 = centerY - boxH / 2f
        x0 = clamp(x0, 5f, sr.scaledWidth - boxW - 5f)
        y0 = clamp(y0, 5f, sr.scaledHeight - boxH - 5f)

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        pushMatrix()
        try {
            drawRect(x0 - 1, y0 - 1, x0 + boxW + 1, y0 + boxH + 1, Color(0, 0, 0, 180).rgb)
            drawRect(x0, y0, x0 + boxW, y0 + boxH, Color(40, 40, 40, 160).rgb)

            val itemRender: RenderItem = mc.renderItem
            RenderHelper.enableGUIStandardItemLighting()
            GlStateManager.disableDepth()
            itemRender.zLevel = 200.0f

            chestSlots.forEach { slot ->
                val stack = slot.stack ?: return@forEach
                val col = slot.slotNumber % 9
                val row = slot.slotNumber / 9
                val ix = (x0 + pad + col * slotSize).toInt()
                val iy = (y0 + pad + row * slotSize).toInt()
                itemRender.renderItemAndEffectIntoGUI(stack, ix, iy)
                itemRender.renderItemOverlayIntoGUI(mc.fontRendererObj, stack, ix, iy, null)
            }
        } catch (_: Throwable) {
        } finally {
            mc.renderItem.zLevel = 0.0f
            enableDepth()
            RenderHelper.disableStandardItemLighting()
            color(1f, 1f, 1f, 1f)
            popMatrix()
            glPopAttrib()
        }
    }

    private fun debug(message: String) {
        if (chestDebug == "Off") return

        when (chestDebug.lowercase()) {
            "text" -> chat(message)
            "notification" -> hud.addNotification(Notification(message, "debug", Type.INFO, 500))
        }
    }
}