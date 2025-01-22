/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.entity.Entity
import java.util.function.Predicate
import kotlin.reflect.KProperty

class EntityLookup<T : Entity>(
    private val owner: Listenable,
    private val entityClass: Class<in T> = Entity::class.java,
    private val updateCycle: Int = 1,
    private var predicates: Array<Predicate<in T>> = emptyArray(),
): Listenable, MinecraftInstance {

    private var ticks = 0

    private var entities: Collection<T> = emptyList()

    private fun clear() {
        if (entities.isNotEmpty())
            entities = emptyList()

        ticks = 0
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Collection<T> = entities

    private val onUpdate = handler<UpdateEvent> {
        ticks++
        ticks %= updateCycle

        if (ticks != 0)
            return@handler

        @Suppress("UNCHECKED_CAST")
        entities = mc.theWorld?.loadedEntityList
            ?.filter { entity ->
                entityClass.isAssignableFrom(entity.javaClass)
                        && predicates.all { it.test(entity as T) }
            } as? Collection<T> ?: emptyList()
    }

    fun filter(predicate: Predicate<in T>): EntityLookup<T> {
        this.predicates += predicate
        return this
    }

    fun filterNot(predicate: Predicate<in T>): EntityLookup<T> {
        this.predicates += predicate.negate()
        return this
    }

    override val parent: Listenable = owner

    override fun handleEvents(): Boolean {
        return owner.handleEvents().also {
            if (!it) clear()
        }
    }

}

inline fun <reified T : Entity> Listenable.EntityLookup(updateCycle: Int = 1): EntityLookup<T> {
    return EntityLookup(owner = this, T::class.java, updateCycle)
}

inline fun <reified T : Entity> Listenable.EntityLookup(updateCycle: Int = 1, predicate: Predicate<in T>): EntityLookup<T> {
    return EntityLookup(owner = this, T::class.java, updateCycle, arrayOf(predicate))
}