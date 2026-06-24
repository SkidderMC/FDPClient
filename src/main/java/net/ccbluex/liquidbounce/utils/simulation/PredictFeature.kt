/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.simulation

data class PredictionFrame<S>(val tick: Int, val state: S)

data class PredictionResult<S>(
    val frames: List<PredictionFrame<S>>,
    val stoppedEarly: Boolean
) {
    val finalState: S get() = frames.last().state
}

/** Bounded N-tick simulation facade. The supplied step function owns the concrete physics state. */
class PredictFeature<S>(
    val maximumTicks: Int = 40,
    private val step: (S) -> S
) {
    init {
        require(maximumTicks > 0) { "Prediction tick limit must be positive" }
    }

    fun predict(
        initialState: S,
        ticks: Int,
        stopWhen: (S) -> Boolean = { false }
    ): PredictionResult<S> {
        require(ticks in 0..maximumTicks) { "Prediction ticks must be between 0 and $maximumTicks" }
        val frames = ArrayList<PredictionFrame<S>>(ticks + 1)
        var state = initialState
        frames.add(PredictionFrame(0, state))

        for (tick in 1..ticks) {
            state = step(state)
            frames.add(PredictionFrame(tick, state))
            if (stopWhen(state)) return PredictionResult(frames, stoppedEarly = true)
        }
        return PredictionResult(frames, stoppedEarly = false)
    }
}
