/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.integration

import net.ccbluex.liquidbounce.utils.FoundationVerification
import net.ccbluex.liquidbounce.utils.SimulationVerification
import net.ccbluex.liquidbounce.utils.WorldFoundationVerification
import net.ccbluex.liquidbounce.utils.rotation.RotationVerification
import org.junit.Test

class VerificationSuiteTest {

    @Test
    fun `foundations pass under junit`() = FoundationVerification.main(emptyArray())

    @Test
    fun `simulation foundations pass under junit`() = SimulationVerification.main(emptyArray())

    @Test
    fun `world foundations pass under junit`() = WorldFoundationVerification.main(emptyArray())

    @Test
    fun `rotation foundations pass under junit`() = RotationVerification.main(emptyArray())
}
