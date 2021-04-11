package net.ccbluex.liquidbounce.features.special.tip

data class Tip(val id: String, val msg: String){
    private val initTime=System.currentTimeMillis()

    fun inTime(): Boolean{
        return (System.currentTimeMillis()-initTime)<150
    }
}
