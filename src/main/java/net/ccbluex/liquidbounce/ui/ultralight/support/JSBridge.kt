package net.ccbluex.liquidbounce.ui.ultralight.support

object JSBridge {
    /**
     * make @param clazz to Any can bypass js engine type check
     */
    fun instanceOf(clazz: Any, obj: Any): Boolean {
        return if(clazz is Class<*>){
            clazz.isInstance(obj)
        }else{
            false
        }
    }

    fun equal(obj1: Any, obj2: Any): Boolean{
        return obj1.equals(obj2)
    }
}