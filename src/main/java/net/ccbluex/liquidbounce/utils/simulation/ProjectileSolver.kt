/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.simulation

import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow
import kotlin.math.sqrt

enum class ProjectileArc {
    LOW,
    HIGH
}

enum class ProjectileStrategy {
    SITUATIONAL,
    DRAG_EXACT,
    POLYNOMIAL
}

data class ProjectileSolution(
    val velocity: Vec3,
    val flightTicks: Double,
    val arc: ProjectileArc,
    val speedError: Double,
    val strategy: ProjectileStrategy = ProjectileStrategy.DRAG_EXACT,
)

/** Solves a drag-affected ballistic path without depending on a combat module. */
class ProjectileSolver(
    val gravity: Double,
    val drag: Double,
    val maximumFlightTicks: Double = 120.0,
    val speedTolerance: Double = 1.0E-6,
    val bisectionIterations: Int = 64
) {
    init {
        require(gravity.isFinite() && gravity >= 0.0) { "Projectile gravity must be finite and non-negative" }
        require(drag.isFinite() && drag > 0.0 && drag <= 1.0) { "Projectile drag must be in (0, 1]" }
        require(maximumFlightTicks.isFinite() && maximumFlightTicks > 0.0) { "Maximum flight time must be positive" }
        require(speedTolerance.isFinite() && speedTolerance > 0.0) { "Speed tolerance must be positive" }
        require(bisectionIterations in 8..128) { "Bisection iterations must be between 8 and 128" }
    }

    fun solve(
        origin: Vec3,
        target: Vec3,
        launchSpeed: Double,
        arc: ProjectileArc = ProjectileArc.LOW,
        strategy: ProjectileStrategy = ProjectileStrategy.SITUATIONAL,
    ): ProjectileSolution? {
        require(launchSpeed.isFinite() && launchSpeed > 0.0) { "Launch speed must be positive" }
        require(origin.xCoord.isFinite() && origin.yCoord.isFinite() && origin.zCoord.isFinite())
        require(target.xCoord.isFinite() && target.yCoord.isFinite() && target.zCoord.isFinite())

        val displacement = target.subtract(origin)
        return when (strategy) {
            ProjectileStrategy.DRAG_EXACT -> solveDragExact(displacement, launchSpeed, arc)
            ProjectileStrategy.POLYNOMIAL -> solvePolynomial(origin, target, launchSpeed, arc)
            ProjectileStrategy.SITUATIONAL ->
                solveDragExact(displacement, launchSpeed, arc)
                    ?: solvePolynomial(origin, target, launchSpeed, arc)
        }
    }

    private fun solveDragExact(
        displacement: Vec3,
        launchSpeed: Double,
        arc: ProjectileArc,
    ): ProjectileSolution? {
        val roots = ArrayList<Double>(2)
        val samples = (maximumFlightTicks * 8.0).toInt().coerceIn(128, 4_096)
        var previousTime = MINIMUM_TIME
        var previousError = speedAt(displacement, previousTime) - launchSpeed

        for (index in 1..samples) {
            val time = MINIMUM_TIME + (maximumFlightTicks - MINIMUM_TIME) * index / samples
            val error = speedAt(displacement, time) - launchSpeed

            if (abs(error) <= speedTolerance) addRoot(roots, time)
            if (previousError * error < 0.0) {
                addRoot(roots, bisect(displacement, launchSpeed, previousTime, time, previousError))
            }
            previousTime = time
            previousError = error
        }

        if (roots.isEmpty()) return null
        val selectedTime = if (arc == ProjectileArc.LOW) roots.min() else roots.max()
        val velocity = requiredVelocity(displacement, selectedTime)
        return ProjectileSolution(
            velocity = velocity,
            flightTicks = selectedTime,
            arc = arc,
            speedError = abs(length(velocity) - launchSpeed),
            strategy = ProjectileStrategy.DRAG_EXACT,
        )
    }

    /**
     * Closed-form vacuum approximation used only when the drag-exact root finder has no solution.
     * A candidate is retained only when replaying it through the real discrete drag model still
     * lands close to the requested point, preventing the fallback from inventing impossible shots.
     */
    private fun solvePolynomial(
        origin: Vec3,
        target: Vec3,
        launchSpeed: Double,
        arc: ProjectileArc,
    ): ProjectileSolution? {
        val displacement = target.subtract(origin)
        val horizontal = sqrt(displacement.xCoord * displacement.xCoord + displacement.zCoord * displacement.zCoord)

        if (gravity <= 1.0E-12) {
            val distance = length(displacement)
            if (distance <= 1.0E-9) return null
            val flightTicks = distance / launchSpeed
            val velocity = Vec3(
                displacement.xCoord / distance * launchSpeed,
                displacement.yCoord / distance * launchSpeed,
                displacement.zCoord / distance * launchSpeed,
            )
            return validatedPolynomial(origin, target, velocity, flightTicks, arc, launchSpeed)
        }
        if (horizontal <= 1.0E-9) return null

        val speedSquared = launchSpeed * launchSpeed
        val discriminant = speedSquared * speedSquared - gravity *
            (gravity * horizontal * horizontal + 2.0 * displacement.yCoord * speedSquared)
        if (discriminant < 0.0) return null

        val sign = if (arc == ProjectileArc.LOW) -1.0 else 1.0
        val tangent = (speedSquared + sign * sqrt(discriminant)) / (gravity * horizontal)
        val pitch = atan(tangent)
        val horizontalSpeed = launchSpeed * cos(pitch)
        if (abs(horizontalSpeed) <= 1.0E-9) return null

        val velocity = Vec3(
            displacement.xCoord / horizontal * horizontalSpeed,
            launchSpeed * sin(pitch),
            displacement.zCoord / horizontal * horizontalSpeed,
        )
        return validatedPolynomial(origin, target, velocity, horizontal / horizontalSpeed, arc, launchSpeed)
    }

    private fun validatedPolynomial(
        origin: Vec3,
        target: Vec3,
        velocity: Vec3,
        flightTicks: Double,
        arc: ProjectileArc,
        launchSpeed: Double,
    ): ProjectileSolution? {
        if (!flightTicks.isFinite() || flightTicks <= 0.0 || flightTicks > maximumFlightTicks) return null
        val missDistanceSquared = positionAt(origin, velocity, flightTicks).squareDistanceTo(target)
        if (missDistanceSquared > POLYNOMIAL_MAXIMUM_MISS * POLYNOMIAL_MAXIMUM_MISS) return null

        return ProjectileSolution(
            velocity,
            flightTicks,
            arc,
            abs(length(velocity) - launchSpeed),
            ProjectileStrategy.POLYNOMIAL,
        )
    }

    fun positionAt(origin: Vec3, initialVelocity: Vec3, ticks: Double): Vec3 {
        require(ticks.isFinite() && ticks >= 0.0) { "Projectile time must be finite and non-negative" }
        val factors = factors(ticks)
        return origin.addVector(
            initialVelocity.xCoord * factors.velocitySum,
            initialVelocity.yCoord * factors.velocitySum - gravity * factors.gravitySum,
            initialVelocity.zCoord * factors.velocitySum
        )
    }

    private fun requiredVelocity(displacement: Vec3, ticks: Double): Vec3 {
        val factors = factors(ticks)
        return Vec3(
            displacement.xCoord / factors.velocitySum,
            (displacement.yCoord + gravity * factors.gravitySum) / factors.velocitySum,
            displacement.zCoord / factors.velocitySum
        )
    }

    private fun speedAt(displacement: Vec3, ticks: Double): Double = length(requiredVelocity(displacement, ticks))

    private fun bisect(
        displacement: Vec3,
        launchSpeed: Double,
        lowerStart: Double,
        upperStart: Double,
        lowerErrorStart: Double
    ): Double {
        var lower = lowerStart
        var upper = upperStart
        var lowerError = lowerErrorStart
        repeat(bisectionIterations) {
            val middle = (lower + upper) * 0.5
            val middleError = speedAt(displacement, middle) - launchSpeed
            if (abs(middleError) <= speedTolerance) return middle
            if (lowerError * middleError <= 0.0) {
                upper = middle
            } else {
                lower = middle
                lowerError = middleError
            }
        }
        return (lower + upper) * 0.5
    }

    private fun factors(ticks: Double): MotionFactors {
        if (abs(drag - 1.0) <= 1.0E-12) {
            return MotionFactors(ticks, ticks * (ticks - 1.0) * 0.5)
        }
        val velocitySum = (1.0 - drag.pow(ticks)) / (1.0 - drag)
        val gravitySum = (ticks - velocitySum) / (1.0 - drag)
        return MotionFactors(velocitySum, gravitySum)
    }

    private fun addRoot(roots: MutableList<Double>, root: Double) {
        if (roots.none { abs(it - root) <= ROOT_TIME_TOLERANCE }) roots.add(root)
    }

    private fun length(vector: Vec3): Double = sqrt(vector.xCoord * vector.xCoord +
        vector.yCoord * vector.yCoord + vector.zCoord * vector.zCoord)

    private data class MotionFactors(val velocitySum: Double, val gravitySum: Double)

    companion object {
        private const val MINIMUM_TIME = 1.0E-3
        private const val ROOT_TIME_TOLERANCE = 1.0E-4
        private const val POLYNOMIAL_MAXIMUM_MISS = 0.75
    }
}
