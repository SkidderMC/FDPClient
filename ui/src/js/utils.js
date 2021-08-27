const LanguageManager = java.importClass("net.ccbluex.liquidbounce.ui.i18n.LanguageManager").INSTANCE

function translate(str) {
    return LanguageManager.replace(str).replace(/%/g, "")
}