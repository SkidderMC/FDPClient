package net.ccbluex.liquidbounce.ui.cape

import net.minecraft.util.ResourceLocation

interface ICape {

    val name: String

    val cape: ResourceLocation

    fun finalize()
}