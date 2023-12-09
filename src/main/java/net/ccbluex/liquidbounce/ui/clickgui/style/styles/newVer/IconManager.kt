package net.ccbluex.liquidbounce.ui.client.gui.newVer

import net.minecraft.util.ResourceLocation

object IconManager {
    private val path = "fdpclient/ui/clickgui/new/"
    @JvmField
    val removeIcon = ResourceLocation(path + "error.png")
    val add = ResourceLocation(path + "import.png")
    val back = ResourceLocation(path + "back.png")
    val docs = ResourceLocation(path + "docs.png")
    val download = ResourceLocation(path + "download.png")
    val folder = ResourceLocation(path + "folder.png")
    val online = ResourceLocation(path + "online.png")
    val reload = ResourceLocation(path + "reload.png")
    val search = ResourceLocation(path + "search.png")
}