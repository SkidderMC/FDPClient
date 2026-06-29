/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.simulation.PredictFeature
import net.ccbluex.liquidbounce.utils.simulation.ProjectileArc
import net.ccbluex.liquidbounce.utils.simulation.ProjectileSolver
import net.ccbluex.liquidbounce.utils.simulation.ProjectileStrategy
import net.minecraft.util.Vec3
import kotlin.math.sqrt

object SimulationVerification {
    @JvmStatic
    fun main(args: Array<String>) {
        verifyProjectileSolver()
        verifyPredictionBounds()
        println("Simulation verification passed")
    }

    private fun verifyProjectileSolver() {
        val solver = ProjectileSolver(gravity = 0.05, drag = 0.99, maximumFlightTicks = 80.0)
        val origin = Vec3(1.0, 2.0, 3.0)
        val knownVelocity = Vec3(1.5, 0.8, 0.3)
        val target = solver.positionAt(origin, knownVelocity, 12.0)
        val speed = sqrt(knownVelocity.xCoord * knownVelocity.xCoord +
            knownVelocity.yCoord * knownVelocity.yCoord + knownVelocity.zCoord * knownVelocity.zCoord)

        val solution = solver.solve(origin, target, speed, ProjectileArc.LOW)
        check(solution != null)
        check(solution.speedError <= 1.0E-5)
        check(solver.positionAt(origin, solution.velocity, solution.flightTicks).squareDistanceTo(target) <= 1.0E-8)
        check(solver.solve(origin, Vec3(1_000.0, 2.0, 3.0), 0.1) == null)

        val vacuum = ProjectileSolver(gravity = 0.0, drag = 1.0)
        val polynomial = vacuum.solve(
            Vec3(0.0, 0.0, 0.0),
            Vec3(6.0, 3.0, 0.0),
            1.5,
            strategy = ProjectileStrategy.POLYNOMIAL,
        )
        check(polynomial?.strategy == ProjectileStrategy.POLYNOMIAL)
        check(vacuum.positionAt(Vec3(0.0, 0.0, 0.0), polynomial.velocity, polynomial.flightTicks)
            .squareDistanceTo(Vec3(6.0, 3.0, 0.0)) <= 1.0E-8)
    }

    private fun verifyPredictionBounds() {
        val predictor = PredictFeature<Int>(maximumTicks = 10) { it + 2 }
        val complete = predictor.predict(0, 4)
        check(complete.finalState == 8 && complete.frames.size == 5 && !complete.stoppedEarly)

        val stopped = predictor.predict(0, 10) { it >= 6 }
        check(stopped.finalState == 6 && stopped.frames.last().tick == 3 && stopped.stoppedEarly)
        check(runCatching { predictor.predict(0, 11) }.isFailure)
    }
}
