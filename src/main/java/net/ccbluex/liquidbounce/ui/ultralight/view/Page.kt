package net.ccbluex.liquidbounce.ui.ultralight.view

import java.io.File

class Page {
    var url: String
        private set

    constructor(file: File) {
        url = "file:///" + file.absolutePath
    }

    constructor(url: String) {
        this.url = url
    }
}