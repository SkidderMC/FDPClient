/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import io.netty.buffer.Unpooled
import net.minecraft.client.gui.GuiScreenBook
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload

/**
 * Fills the open book-and-quill with your text across a chosen number of pages and, if
 * enabled, signs it — so you can stamp out books without typing. Only fires while a writable
 * book screen is open.
 */
object BookBot : Module("BookBot", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val text by text("Text", "FDPClient")
    private val pageCount by int("Pages", 50, 1..100)
    private val sign by boolean("Sign", true)
    private val title by text("Title", "Book") { sign }
    private val delay by int("Delay", 1000, 0..5000)

    private val timer = MSTimer()

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (mc.currentScreen !is GuiScreenBook || !timer.hasTimePassed(delay.toLong())) {
            return@handler
        }

        val held = player.heldItem ?: return@handler
        if (held.item != Items.writable_book) {
            return@handler
        }

        val pages = NBTTagList()
        repeat(pageCount) {
            pages.appendTag(NBTTagString(text))
        }

        val tag = NBTTagCompound()
        tag.setTag("pages", pages)
        if (sign) {
            tag.setString("author", player.name)
            tag.setString("title", title)
        }

        val book = ItemStack(Items.writable_book)
        book.tagCompound = tag

        val buffer = PacketBuffer(Unpooled.buffer())
        buffer.writeItemStackToBuffer(book)
        sendPacket(C17PacketCustomPayload(if (sign) "MC|BSign" else "MC|BEdit", buffer))

        timer.reset()
    }
}
