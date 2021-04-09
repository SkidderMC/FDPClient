package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.special.tip.Tip
import java.lang.StringBuilder

class TipManager : Listenable {
    private val tips=HashMap<String,Tip>()

    private var tipStr=""
    // 1 up 0 no -1 down
    private var animStatus=0L
    private var animStartTime=0L

    fun addTip(tip:Tip){
        tips[tip.id] = tip
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        val notInTimeTips=ArrayList<Tip>()

        val cacheSb=StringBuilder()
        for(entry in tips){
            val tip=entry.value
            if(tip.inTime()){
                cacheSb.append(tip.msg)
                cacheSb.append(" |")
            }else{
                notInTimeTips.add(tip)
            }
        }
        var str=cacheSb.toString()
        if(str.isEmpty()) return
        str=str.substring(0,str.length-2)

        if(tipStr=="" && str!=""){
            animStatus=1
            animStartTime=System.currentTimeMillis()
        }
        if(str=="" && tipStr!=""){
            animStatus=-1
            animStartTime=System.currentTimeMillis()
        }

        if(notInTimeTips.size>0){
            for(tip in notInTimeTips){
                tips.remove(tip.id)
            }
        }
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent){

    }

    override fun handleEvents(): Boolean {
        return true
    }
}