/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.aac.AAC
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.aac.LAAC
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.grim.OldGrim
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.intave.IntaveNew
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.intave.IntaveOld
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.other.None
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.other.Rewi

object NoWeb : Module("NoWeb", Category.MOVEMENT) {

    private val noWebModes = arrayOf(
        // Vanilla
        None,

        // AAC
        AAC, LAAC,

        // Intave
        IntaveOld,
        IntaveNew,

        // Grim
        OldGrim,

        // Other
        Rewi,
    )

    private val modes = noWebModes.map { it.modeName }.toTypedArray()

    val mode by choices(
        "Mode", modes, "None"
    )

    val onUpdate = handler<UpdateEvent> {
        modeModule.onUpdate()
    }

    override val tag
        get() = mode

    private val modeModule
        get() = noWebModes.find { it.modeName == mode }!!
}
