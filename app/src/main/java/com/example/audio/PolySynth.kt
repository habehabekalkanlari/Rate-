package com.example.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

enum class SynthWaveform {
    SAW,
    SQUARE,
    TRIANGLE,
    SINE
}

class PolySynth(
    private val sampleRate: Int = 44100,
    private val maxVoices: Int = 8
) {

    class Voice(
        var active: Boolean = false,
        var midiNote: Int = 60,
        var frequency: Float = 261.63f,
        var velocity: Float = 0.8f,
        var phase: Float = 0f,
        var phaseIncrement: Float = 0f,
        var envStage: EnvStage = EnvStage.OFF,
        var envLevel: Float = 0f,
        var filterBuf0: Float = 0f,
        var filterBuf1: Float = 0f,
        var age: Long = 0
    )

    enum class EnvStage {
        ATTACK,
        DECAY,
        SUSTAIN,
        RELEASE,
        OFF
    }

    private val voices = Array(maxVoices) { Voice() }
    private var voiceCounter = 0L

    var waveform: SynthWaveform = SynthWaveform.SAW
    var attackSec: Float = 0.015f
    var decaySec: Float = 0.15f
    var sustainLevel: Float = 0.65f
    var releaseSec: Float = 0.25f

    var filterCutoffNorm: Float = 0.85f   // 0.05 to 0.99
    var filterResonance: Float = 0.3f     // 0.0 to 0.9

    fun noteOn(midiNote: Int, velocity: Float = 0.8f) {
        // Reuse voice if playing this note
        var voice = voices.firstOrNull { it.active && it.midiNote == midiNote }
        if (voice == null) {
            // Find free voice or steal oldest
            voice = voices.firstOrNull { !it.active } ?: voices.minByOrNull { it.age }!!
        }

        val freq = midiToFreq(midiNote)
        voice.active = true
        voice.midiNote = midiNote
        voice.frequency = freq
        voice.velocity = velocity
        voice.phaseIncrement = freq / sampleRate
        voice.envStage = EnvStage.ATTACK
        voice.age = ++voiceCounter
    }

    fun noteOff(midiNote: Int) {
        voices.forEach { v ->
            if (v.active && v.midiNote == midiNote && v.envStage != EnvStage.RELEASE && v.envStage != EnvStage.OFF) {
                v.envStage = EnvStage.RELEASE
            }
        }
    }

    fun allNotesOff() {
        voices.forEach {
            it.active = false
            it.envStage = EnvStage.OFF
            it.envLevel = 0f
        }
    }

    fun process(buffer: FloatArray, numFrames: Int, gain: Float = 0.7f, pan: Float = 0f) {
        val leftGain = gain * (1f - pan.coerceIn(-1f, 1f).coerceAtLeast(0f))
        val rightGain = gain * (1f + pan.coerceIn(-1f, 1f).coerceAtMost(0f))

        val attackRate = 1f / (attackSec.coerceAtLeast(0.002f) * sampleRate)
        val decayRate = 1f / (decaySec.coerceAtLeast(0.01f) * sampleRate)
        val releaseRate = 1f / (releaseSec.coerceAtLeast(0.01f) * sampleRate)

        val cutoffFreq = (50f + 16000f * filterCutoffNorm.pow(2)).coerceIn(40f, 18000f)
        val f = 2f * sin(PI.toFloat() * cutoffFreq / sampleRate)
        val q = 1.0f - filterResonance.coerceIn(0f, 0.95f)

        for (i in 0 until numFrames) {
            var sumSample = 0f

            for (v in voices) {
                if (!v.active) continue

                // ADSR logic
                when (v.envStage) {
                    EnvStage.ATTACK -> {
                        v.envLevel += attackRate
                        if (v.envLevel >= 1.0f) {
                            v.envLevel = 1.0f
                            v.envStage = EnvStage.DECAY
                        }
                    }
                    EnvStage.DECAY -> {
                        v.envLevel -= decayRate * (1.0f - sustainLevel)
                        if (v.envLevel <= sustainLevel) {
                            v.envLevel = sustainLevel
                            v.envStage = EnvStage.SUSTAIN
                        }
                    }
                    EnvStage.SUSTAIN -> {
                        v.envLevel = sustainLevel
                    }
                    EnvStage.RELEASE -> {
                        v.envLevel -= releaseRate * sustainLevel
                        if (v.envLevel <= 0.001f) {
                            v.envLevel = 0f
                            v.envStage = EnvStage.OFF
                            v.active = false
                        }
                    }
                    EnvStage.OFF -> {
                        v.active = false
                    }
                }

                if (!v.active) continue

                // Oscillator waveform
                val p = v.phase
                val rawSample = when (waveform) {
                    SynthWaveform.SAW -> 2f * (p - kotlin.math.floor(p + 0.5f))
                    SynthWaveform.SQUARE -> if (p < 0.5f) 0.8f else -0.8f
                    SynthWaveform.TRIANGLE -> 4f * kotlin.math.abs(p - 0.5f) - 1.0f
                    SynthWaveform.SINE -> sin(2f * PI.toFloat() * p)
                }

                // Advance phase
                v.phase = (v.phase + v.phaseIncrement) % 1.0f

                // Resonant lowpass state variable filter per voice
                v.filterBuf0 += f * (rawSample - v.filterBuf0 + q * (v.filterBuf0 - v.filterBuf1))
                v.filterBuf1 += f * (v.filterBuf0 - v.filterBuf1)
                val filtered = v.filterBuf1.coerceIn(-1.5f, 1.5f)

                sumSample += filtered * v.envLevel * v.velocity
            }

            buffer[i * 2] += sumSample * leftGain
            buffer[i * 2 + 1] += sumSample * rightGain
        }
    }

    private fun midiToFreq(midi: Int): Float {
        return 440.0f * (2.0f.pow((midi - 69).toFloat() / 12.0f))
    }
}
