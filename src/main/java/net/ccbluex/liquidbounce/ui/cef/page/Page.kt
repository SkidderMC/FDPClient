package net.ccbluex.liquidbounce.ui.cef.page

import java.io.File

open class Page {

    var url: String
        private set

    constructor(file: File) {
        url = "file:///" + file.absolutePath
    }

    constructor(url: String) {
        this.url = url
    }
}