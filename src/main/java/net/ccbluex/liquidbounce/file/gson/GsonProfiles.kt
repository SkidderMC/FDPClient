/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.gson

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/** Prevents an implementation field from entering any persisted or shared JSON representation. */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Exclude

private object AnnotatedFieldExclusion : ExclusionStrategy {
    override fun shouldSkipField(field: FieldAttributes): Boolean = field.getAnnotation(Exclude::class.java) != null
    override fun shouldSkipClass(clazz: Class<*>): Boolean = false
}

/**
 * Gson instances are separated by output purpose so changing shared output cannot silently alter
 * local files. Adapters may be added to a single profile without affecting the others.
 */
object GsonProfiles {
    @JvmField
    val localFile: Gson = baseBuilder().setPrettyPrinting().create()

    @JvmField
    val shared: Gson = baseBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    @JvmField
    val interop: Gson = baseBuilder().create()

    private fun baseBuilder(): GsonBuilder = GsonBuilder()
        .addSerializationExclusionStrategy(AnnotatedFieldExclusion)
        .addDeserializationExclusionStrategy(AnnotatedFieldExclusion)
}
