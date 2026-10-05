package com.example.audio

import com.example.model.DrumType
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

class DrumSynthesizer(private val sampleRate: Int = 44100) {

    private class ActiveDrum(
        val type: DrumType,
        val velocity: Float,
        var sampleIndex: Int = 0,
        val totalSamples: Int
    )

    private val activeDrums = ConcurrentLinkedQueue<ActiveDrum>()
    private val random = Random(42)

    fun trigger(type: DrumType, velocity: Float = 1.0f) {
        val durationSec = when (type) {
            DrumType.KICK -> 0.35f
            DrumType.SNARE -> 0.28f
            DrumType.HIHAT_CLOSED -> 0.07f
            DrumType.HIHAT_OPEN -> 0.45f
            DrumType.CLAP -> 0.22f
            DrumType.RIMSHOT -> 0.12f
            DrumType.TOM -> 0.40f
            DrumType.CRASH -> 0.85f
        }
        val totalSamples = (durationSec * sampleRate).toInt()
        activeDrums.add(ActiveDrum(type, velocity.coerceIn(0.1f, 1.2f), 0, totalSamples))
    }

    /**
     * Synthesizes and mixes drum output into the provided float buffer.
     */
    fun process(buffer: FloatArray, numFrames: Int, gain: Float = 1.0f) {
        if (activeDrums.isEmpty()) return

        val iterator = activeDrums.iterator()
        while (iterator.hasNext()) {
            val drum = iterator.next()
            val total = drum.totalSamples
            val vel = drum.velocity * gain

            for (i in 0 until numFrames) {
                val sIdx = drum.sampleIndex
                if (sIdx >= total) break

                val sample = generateSample(drum.type, sIdx, total) * vel
                // Mix in stereo
                buffer[i * 2] += sample
                buffer[i * 2 + 1] += sample

                drum.sampleIndex++
            }

            if (drum.sampleIndex >= total) {
                iterator.remove()
            }
        }
    }

    private fun generateSample(type: DrumType, sampleIndex: Int, totalSamples: Int): Float {
        val t = sampleIndex.toFloat() / sampleRate
        val progress = sampleIndex.toFloat() / totalSamples

        return when (type) {
            DrumType.KICK -> {
                // Pitch envelope: drops exponentially from 150 Hz to 45 Hz
                val pitch = 45f + 110f * exp(-t * 28f)
                val phase = 2f * PI.toFloat() * pitch * t
                val env = exp(-t * 9f)
                val body = sin(phase) * env
                // Transient click at the start
                val click = if (t < 0.005f) (1f - t / 0.005f) * (random.nextFloat() * 2f - 1f) * 0.4f else 0f
                (body * 1.2f + click).coerceIn(-1f, 1f)
            }
            DrumType.SNARE -> {
                // Tone body at 180Hz + snappy noise
                val tonePitch = 180f * exp(-t * 12f)
                val tonePhase = 2f * PI.toFloat() * tonePitch * t
                val toneEnv = exp(-t * 22f)
                val tone = sin(tonePhase) * toneEnv * 0.6f

                // White noise burst
                val noiseEnv = exp(-t * 15f)
                val noise = (random.nextFloat() * 2f - 1f) * noiseEnv * 0.75f
                tone + noise
            }
            DrumType.HIHAT_CLOSED -> {
                // Metallic high frequency noise with fast decay
                val env = exp(-t * 85f)
                val noise = (random.nextFloat() * 2f - 1f)
                val metal = sin(2f * PI.toFloat() * 7500f * t) * 0.3f +
                        sin(2f * PI.toFloat() * 9200f * t) * 0.3f
                (noise * 0.7f + metal * 0.3f) * env * 0.65f
            }
            DrumType.HIHAT_OPEN -> {
                // Metallic shimmer with longer decay
                val env = exp(-t * 9f)
                val noise = (random.nextFloat() * 2f - 1f)
                val metal = sin(2f * PI.toFloat() * 7200f * t) * 0.4f +
                        sin(2f * PI.toFloat() * 8800f * t) * 0.3f
                (noise * 0.6f + metal * 0.4f) * env * 0.6f
            }
            DrumType.CLAP -> {
                // Multi-burst transient
                val env = exp(-t * 14f)
                val burst = when {
                    t < 0.012f -> 0.7f
                    t in 0.012f..0.024f -> 0.9f
                    t in 0.024f..0.038f -> 1.0f
                    else -> 0.6f
                }
                val noise = (random.nextFloat() * 2f - 1f)
                noise * env * burst * 0.7f
            }
            DrumType.RIMSHOT -> {
                // High resonant ping
                val env = exp(-t * 40f)
                val pitch = 950f * exp(-t * 10f)
                val ping = sin(2f * PI.toFloat() * pitch * t)
                ping * env * 0.8f
            }
            DrumType.TOM -> {
                // 808 Pitch drop tom
                val pitch = 85f + 95f * exp(-t * 16f)
                val phase = 2f * PI.toFloat() * pitch * t
                val env = exp(-t * 8f)
                sin(phase) * env * 0.85f
            }
            DrumType.CRASH -> {
                // Sustained high-frequency shimmer
                val env = exp(-t * 3.5f)
                val noise = (random.nextFloat() * 2f - 1f)
                val metal = sin(2f * PI.toFloat() * 5800f * t) * 0.3f +
                        sin(2f * PI.toFloat() * 7600f * t) * 0.2f
                (noise * 0.7f + metal * 0.3f) * env * 0.55f
            }
        }
    }
}
