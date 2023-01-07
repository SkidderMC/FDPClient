package net.ccbluex.liquidbounce.script

import com.sun.jna.Native
import jdk.nashorn.api.scripting.ClassFilter
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.URL
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object ScriptSafetyManager {

    private val level = ProtectionLevel.valueOf((System.getProperty("fdp.script.safety") ?: "safe").uppercase()).level
    private val restrictedClasses: Map<Class<*>, Int>
    private val restrictedChilds: Map<Class<*>, Pair<String, Int>>

    val classFilter = ClassFilter { name ->
        try {
            !isRestricted(Class.forName(name))
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    init {
        ClientUtils.logInfo("[FDPScriptAPI] Script safety level: ${ProtectionLevel.values().find { it.level == level }?.name}($level)") // maybe we should think about performance here?

        val restrictedClasses = mutableMapOf<Class<*>, Int>()
        val restrictedChilds = mutableMapOf<Class<*>, Pair<String, Int>>()

        // this can be used by a malicious script to remote control the user
        restrictedClasses[ScriptSafetyManager::class.java] = ProtectionLevel.HARMFUL.level
        restrictedClasses[ClassLoader::class.java] = ProtectionLevel.HARMFUL.level
        restrictedClasses[Native::class.java] = ProtectionLevel.HARMFUL.level
        restrictedClasses[Runtime::class.java] = ProtectionLevel.HARMFUL.level
        restrictedChilds[System::class.java] = Pair("loadLibrary", ProtectionLevel.HARMFUL.level)
        restrictedChilds[System::class.java] = Pair("load", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("forName", ProtectionLevel.HARMFUL.level)
        // block the reflection api
        restrictedChilds[Class::class.java] = Pair("getDeclaredField", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getDeclaredMethod", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getField", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getMethod", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getDeclaredFields", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getDeclaredMethods", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getFields", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getMethods", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getConstructor", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getDeclaredConstructor", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getConstructors", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("getDeclaredConstructors", ProtectionLevel.HARMFUL.level)
        restrictedChilds[Class::class.java] = Pair("newInstance", ProtectionLevel.HARMFUL.level)

        // blocks privacy collection or other sensitive information sent to the server by the script
        restrictedClasses[URL::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[Socket::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[URLConnection::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[HttpUtils::class.java] = ProtectionLevel.DANGER.level

        // some trojan scripts may wipe user's data
        restrictedClasses[File::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[Path::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[FileUtils::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[Files::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[InputStream::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[OutputStream::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[com.google.common.io.Files::class.java] = ProtectionLevel.DANGER.level
        restrictedClasses[net.ccbluex.liquidbounce.utils.FileUtils::class.java] = ProtectionLevel.DANGER.level

        // make it UNMODIFIABLE
        this.restrictedClasses = Collections.unmodifiableMap(restrictedClasses)
        this.restrictedChilds = Collections.unmodifiableMap(restrictedChilds)
    }

    fun isRestricted(classIn: Class<*>): Boolean {
        var klass = classIn
        while (klass != Any::class.java) {
            if (isRestrictedSimple(klass)) {
                return true
            }
            if(klass.superclass != null) {
                klass = klass.superclass
            } else {
                break
            }
        }
        return false
    }

    fun isRestricted(classIn: Class<*>, child: String): Boolean {
        var klass = classIn
        while (klass != Any::class.java) {
            if (isRestrictedSimple(klass, child)) {
                return true
            }
            if(klass.superclass != null) {
                klass = klass.superclass
            } else {
                break
            }
        }
        return false
    }

    fun isRestrictedSimple(klass: Class<*>): Boolean {
        return if(restrictedClasses.containsKey(klass) && restrictedClasses[klass]!! > level) {
            warnRestricted(klass.name, "")
            true
        } else {
            false
        }
    }

    fun isRestrictedSimple(klass: Class<*>, child: String): Boolean {
        return if(isRestrictedSimple(klass)) {
            warnRestricted(klass.name, "")
            true
        } else if(restrictedChilds.containsKey(klass) && restrictedChilds[klass]!!.first == child && restrictedChilds[klass]!!.second > level) {
            warnRestricted(klass.name, child)
            true
        } else {
            false
        }
    }

    private val alerted = mutableListOf<String>()

    private fun warnRestricted(klass: String, child: String = "") {
        val message = klass + (if(child.isNotEmpty()) ".$child" else "")
        if(!alerted.contains(message)) {
            alerted.add(message)
            ClientUtils.logWarn("[FDPScriptAPI] \n" +
                    "========= WARNING =========\n" +
                    "The script tried to make a restricted call: $message,\n" +
                    "please add a jvm argument to disable this check: -Dfdp.script.safety=HARMFUL\n" +
                    "===========================")
        }
    }

    enum class ProtectionLevel(val level: Int) {
        SAFE(0),
        DANGER(1),
        HARMFUL(2)
    }
}