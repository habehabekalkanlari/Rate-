package com.example.audio

import android.content.Context
import com.example.model.DawProject
import com.example.model.TrackType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavExporter {

    /**
     * Renders N bars of the current project into a standard 16-bit Stereo 44.1kHz WAV file.
     */
    suspend fun exportProjectToWav(
        context: Context,
        project: DawProject,
        barsToRender: Int = 4
    ): File = withContext(Dispatchers.IO) {
        val sampleRate = 44100
        val channels = 2
        val bitsPerSample = 16
        val bytesPerSample = bitsPerSample / 8

        // Calculate total frames: 4 beats per bar, 16th notes
        val secondsPerBeat = 60.0 / project.bpm
        val totalSeconds = secondsPerBeat * 4.0 * barsToRender
        val totalFrames = (totalSeconds * sampleRate).toInt()

        // Create audio DSP instances
        val drumSynth = DrumSynthesizer(sampleRate)
        val bassSynth = PolySynth(sampleRate).apply {
            waveform = SynthWaveform.SAW
            filterCutoffNorm = 0.6f
            filterResonance = 0.4f
        }
        val leadSynth = PolySynth(sampleRate).apply {
            waveform = SynthWaveform.SAW
            filterCutoffNorm = 0.85f
            filterResonance = 0.25f
        }
        val chordsSynth = PolySynth(sampleRate).apply {
            waveform = SynthWaveform.TRIANGLE
            filterCutoffNorm = 0.75f
            filterResonance = 0.15f
        }
        val effects = AudioEffects(sampleRate).apply {
            filterCutoffNorm = project.effects.filterCutoff
            filterResonance = project.effects.filterResonance
            delayTimeSec = project.effects.delayTimeSec
            delayFeedback = project.effects.delayFeedback
            delayMix = project.effects.delayMix
            reverbSize = project.effects.reverbSize
            reverbMix = project.effects.reverbMix
            drive = project.effects.drive
        }

        val exportFile = File(context.cacheDir, "${project.name.replace(" ", "_")}_export.wav")
        val pcmData = ByteArray(totalFrames * channels * bytesPerSample)
        val pcmBuffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

        val chunkSize = 512
        val floatChunk = FloatArray(chunkSize * channels)
        val samplesPer16th = (secondsPerBeat / 4.0 * sampleRate).toInt()

        var currentStep = 0
        var sampleCounterInStep = 0

        var framesRendered = 0
        while (framesRendered < totalFrames) {
            val framesToRender = minOf(chunkSize, totalFrames - framesRendered)
            floatChunk.fill(0f)

            // Step trigger logic
            sampleCounterInStep += framesToRender
            if (sampleCounterInStep >= samplesPer16th) {
                sampleCounterInStep -= samplesPer16th

                // Trigger active notes
                for (track in project.tracks) {
                    if (track.isMuted) continue
                    val activeClipIndex = track.activeClipIndex ?: continue
                    val clip = track.clips.getOrNull(activeClipIndex) ?: continue

                    when (track.type) {
                        TrackType.DRUMS -> {
                            clip.drumSteps.forEach { (drumType, steps) ->
                                if (steps.getOrNull(currentStep) == true) {
                                    drumSynth.trigger(drumType, 0.95f)
                                }
                            }
                        }
                        TrackType.BASS -> {
                            clip.melodicNotes.filter { it.step == currentStep }.forEach { note ->
                                bassSynth.noteOn(note.midiNote, note.velocity)
                            }
                        }
                        TrackType.SYNTH_LEAD -> {
                            clip.melodicNotes.filter { it.step == currentStep }.forEach { note ->
                                leadSynth.noteOn(note.midiNote, note.velocity)
                            }
                        }
                        TrackType.CHORDS -> {
                            clip.melodicNotes.filter { it.step == currentStep }.forEach { note ->
                                chordsSynth.noteOn(note.midiNote, note.velocity)
                            }
                        }
                        TrackType.FX -> {}
                    }
                }
                currentStep = (currentStep + 1) % 16
            }

            // Synthesize
            for (track in project.tracks) {
                if (track.isMuted) continue
                when (track.type) {
                    TrackType.DRUMS -> drumSynth.process(floatChunk, framesToRender, track.volume)
                    TrackType.BASS -> bassSynth.process(floatChunk, framesToRender, track.volume, track.pan)
                    TrackType.SYNTH_LEAD -> leadSynth.process(floatChunk, framesToRender, track.volume, track.pan)
                    TrackType.CHORDS -> chordsSynth.process(floatChunk, framesToRender, track.volume, track.pan)
                    TrackType.FX -> {}
                }
            }

            // Effects
            effects.processMasterEffects(floatChunk, framesToRender)

            // Write PCM
            for (i in 0 until framesToRender) {
                val l = (floatChunk[i * 2] * project.masterVolume).coerceIn(-1f, 1f)
                val r = (floatChunk[i * 2 + 1] * project.masterVolume).coerceIn(-1f, 1f)
                pcmBuffer.putShort((l * 32767f).toInt().toShort())
                pcmBuffer.putShort((r * 32767f).toInt().toShort())
            }

            framesRendered += framesToRender
        }

        // Write WAV with RIFF Header
        FileOutputStream(exportFile).use { fos ->
            writeWavHeader(fos, totalFrames * channels * bytesPerSample, sampleRate, channels, bitsPerSample)
            fos.write(pcmData)
        }

        exportFile
    }

    private fun writeWavHeader(
        fos: FileOutputStream,
        totalAudioLen: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)

        // RIFF/WAVE header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // Subchunk1Size for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat 1 = PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte() // Block align
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        fos.write(header, 0, 44)
    }
}
