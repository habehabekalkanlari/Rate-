package com.example.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

class AudioEffects(private val sampleRate: Int = 44100) {

    // Delay parameters
    var delayTimeSec: Float = 0.25f
    var delayFeedback: Float = 0.35f
    var delayMix: Float = 0.2f

    // Reverb parameters
    var reverbSize: Float = 0.5f
    var reverbMix: Float = 0.25f

    // Drive parameters
    var drive: Float = 0.15f

    // Master Filter parameters
    var filterCutoffNorm: Float = 0.85f   // 0.05 to 0.99
    var filterResonance: Float = 0.2f     // 0.0 to 0.85
    var filterIsHighPass: Boolean = false

    // Delay buffer (1 second maximum at 44.1kHz stereo)
    private val delayBufferSize = sampleRate * 2
    private val delayBufferL = FloatArray(delayBufferSize)
    private val delayBufferR = FloatArray(delayBufferSize)
    private var delayWriteIndex = 0

    // Reverb comb & allpass buffers
    private val combL1 = FloatArray(1116)
    private val combL2 = FloatArray(1188)
    private val combL3 = FloatArray(1277)
    private val combL4 = FloatArray(1356)
    private var idxL1 = 0
    private var idxL2 = 0
    private var idxL3 = 0
    private var idxL4 = 0

    // Filter state
    private var filtL0 = 0f
    private var filtL1 = 0f
    private var filtR0 = 0f
    private var filtR1 = 0f

    fun processMasterEffects(buffer: FloatArray, numFrames: Int) {
        val delaySamples = (delayTimeSec.coerceIn(0.02f, 0.8f) * sampleRate).toInt().coerceIn(1, delayBufferSize - 1)
        val fb = delayFeedback.coerceIn(0f, 0.85f)
        val dMix = delayMix.coerceIn(0f, 1f)
        val rMix = reverbMix.coerceIn(0f, 0.85f)
        val drv = 1f + drive * 4f

        val cutoffFreq = (50f + 16000f * filterCutoffNorm.pow(2)).coerceIn(40f, 18000f)
        val f = 2f * sin(PI.toFloat() * cutoffFreq / sampleRate)
        val q = 1.0f - filterResonance.coerceIn(0f, 0.85f)

        for (i in 0 until numFrames) {
            var inL = buffer[i * 2]
            var inR = buffer[i * 2 + 1]

            // 1. Overdrive / Saturation
            if (drive > 0.01f) {
                inL = tanh(inL * drv)
                inR = tanh(inR * drv)
            }

            // 2. Filter (State Variable Lowpass or Highpass)
            filtL0 += f * (inL - filtL0 + q * (filtL0 - filtL1))
            filtL1 += f * (filtL0 - filtL1)
            filtR0 += f * (inR - filtR0 + q * (filtR0 - filtR1))
            filtR1 += f * (filtR0 - filtR1)

            val postFiltL = if (filterIsHighPass) inL - filtL0 else filtL1
            val postFiltR = if (filterIsHighPass) inR - filtR0 else filtR1

            // 3. Stereo Delay
            val readIdx = (delayWriteIndex - delaySamples + delayBufferSize) % delayBufferSize
            val delayedL = delayBufferL[readIdx]
            val delayedR = delayBufferR[readIdx]

            // Ping-pong cross feedback
            delayBufferL[delayWriteIndex] = (postFiltL + delayedR * fb).coerceIn(-2f, 2f)
            delayBufferR[delayWriteIndex] = (postFiltR + delayedL * fb).coerceIn(-2f, 2f)
            delayWriteIndex = (delayWriteIndex + 1) % delayBufferSize

            val mixDelayL = postFiltL * (1f - dMix) + delayedL * dMix
            val mixDelayR = postFiltR * (1f - dMix) + delayedR * dMix

            // 4. Studio Reverb
            val revOutL = processReverb(mixDelayL, mixDelayR)
            val outL = mixDelayL * (1f - rMix * 0.7f) + revOutL * rMix
            val outR = mixDelayR * (1f - rMix * 0.7f) + revOutL * rMix

            // 5. Soft-Knee Master Limiter to prevent clipping
            buffer[i * 2] = softLimit(outL)
            buffer[i * 2 + 1] = softLimit(outR)
        }
    }

    private fun processReverb(inL: Float, inR: Float): Float {
        val input = (inL + inR) * 0.35f
        val damp = reverbSize * 0.75f

        val o1 = combL1[idxL1]
        combL1[idxL1] = input + o1 * damp
        idxL1 = (idxL1 + 1) % combL1.size

        val o2 = combL2[idxL2]
        combL2[idxL2] = input + o2 * damp
        idxL2 = (idxL2 + 1) % combL2.size

        val o3 = combL3[idxL3]
        combL3[idxL3] = input + o3 * damp
        idxL3 = (idxL3 + 1) % combL3.size

        val o4 = combL4[idxL4]
        combL4[idxL4] = input + o4 * damp
        idxL4 = (idxL4 + 1) % combL4.size

        return (o1 + o2 + o3 + o4) * 0.25f
    }

    private fun softLimit(x: Float): Float {
        val absX = abs(x)
        return if (absX < 0.75f) {
            x
        } else {
            val sign = if (x >= 0f) 1f else -1f
            val compressed = 0.75f + 0.25f * tanh((absX - 0.75f) * 2f)
            sign * compressed.coerceIn(0f, 0.99f)
        }
    }
}
