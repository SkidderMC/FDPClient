package net.ccbluex.liquidbounce.utils

import net.minecraft.item.ItemStack

object SpoofItemUtils : MinecraftInstance() {
    //From CrossSine
    var spoofSlot = 0
    var spoofing = false
    fun startSpoof(slot : Int) {
        if (!spoofing) {
            spoofSlot = slot
            spoofing = true
        }
    }
    fun stopSpoof() {
        for (i in 0..8) {
            if (i == spoofSlot) {
                mc.thePlayer.inventory.currentItem = i
            }
        }
        spoofing = false
    }
    fun getSlot() : Int {
        return if (spoofing) spoofSlot else mc.thePlayer.inventory.currentItem
    }
    fun getStack(): ItemStack? {
        return if (spoofing) mc.thePlayer.inventory.getStackInSlot(spoofSlot) else mc.thePlayer.inventory.getCurrentItem()
    }
    fun setSlot(slot: Int) {
        spoofSlot = slot
    }
}