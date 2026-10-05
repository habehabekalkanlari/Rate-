package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.DawProject
import com.example.model.DrumType
import com.example.model.TrackType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class AudioEngine(private val sampleRate: Int = 44100) {

    private var audioTrack: AudioTrack? = null
    private var renderThread: Thread? = null
    private val isRunning = AtomicBoolean(false)

    // Master play state
    var isPlaying: Boolean = false
        private set

    var isRecording: Boolean = false

    // Timing & Sequencer
    var bpm: Int = 124
        set(value) {
            field = value.coerceIn(50, 220)
        }

    var swing: Float = 0.0f
        set(value) {
            field = value.coerceIn(0f, 0.5f)
        }

    var masterVolume: Float = 0.85f
    var metronomeEnabled: Boolean = false

    // Sequencer position state
    private val _currentStepFlow = MutableStateFlow(0)
    val currentStepFlow: StateFlow<Int> = _currentStepFlow.asStateFlow()

    private val _currentBarFlow = MutableStateFlow(1)
    val currentBarFlow: StateFlow<Int> = _currentBarFlow.asStateFlow()

    // Real-time VU Levels (Master and Tracks)
    private val _masterVuFlow = MutableStateFlow(0f)
    val masterVuFlow: StateFlow<Float> = _masterVuFlow.asStateFlow()

    private val _trackVuFlow = MutableStateFlow<Map<String, Float>>(emptyMap())
    val trackVuFlow: StateFlow<Map<String, Float>> = _trackVuFlow.asStateFlow()

    // Real-time Waveform for visualizer (normalized floats -1f..1f)
    private val _waveformFlow = MutableStateFlow(FloatArray(64))
    val waveformFlow: StateFlow<FloatArray> = _waveformFlow.asStateFlow()

    // Audio DSP Modules
    val drumSynth = DrumSynthesizer(sampleRate)
    val bassSynth = PolySynth(sampleRate).apply {
        waveform = SynthWaveform.SAW
        filterCutoffNorm = 0.55f
        filterResonance = 0.45f
        attackSec = 0.005f
        decaySec = 0.22f
        sustainLevel = 0.4f
        releaseSec = 0.15f
    }
    val leadSynth = PolySynth(sampleRate).apply {
        waveform = SynthWaveform.SAW
        filterCutoffNorm = 0.85f
        filterResonance = 0.25f
        attackSec = 0.01f
        decaySec = 0.18f
        sustainLevel = 0.5f
        releaseSec = 0.2f
    }
    val chordsSynth = PolySynth(sampleRate).apply {
        waveform = SynthWaveform.TRIANGLE
        filterCutoffNorm = 0.75f
        filterResonance = 0.15f
        attackSec = 0.04f
        decaySec = 0.35f
        sustainLevel = 0.6f
        releaseSec = 0.4f
    }

    val effects = AudioEffects(sampleRate)

    // Metronome click synthesizer
    private var metronomeSampleIndex = -1
    private var metronomePitch = 1200f
    private val metronomeLength = (sampleRate * 0.025f).toInt()

    // Project reference for sequencer
    @Volatile
    var currentProject: DawProject? = null

    // Callback when user records notes/drums live
    var onLiveRecordStep: ((trackId: String, step: Int, drumType: DrumType?, midiNote: Int?) -> Unit)? = null

    // Sample counters
    private var sampleCounterInStep = 0
    private var currentStep = 0
    private var currentBar = 1

    fun start() {
        if (isRunning.get()) return

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBufferSize, 2048)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        isRunning.set(true)

        renderThread = Thread({
            audioLoop(bufferSize / 4) // frames per iteration
        }, "DawAudioEngineThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        isRunning.set(false)
        isPlaying = false
        renderThread?.interrupt()
        renderThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun play() {
        isPlaying = true
    }

    fun pause() {
        isPlaying = false
        bassSynth.allNotesOff()
        leadSynth.allNotesOff()
        chordsSynth.allNotesOff()
    }

    fun togglePlayPause(): Boolean {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
        return isPlaying
    }

    fun rewind() {
        currentStep = 0
        currentBar = 1
        sampleCounterInStep = 0
        _currentStepFlow.value = 0
        _currentBarFlow.value = 1
    }

    /**
     * Trigger a drum hit live (e.g. from 4x4 pads)
     */
    fun triggerDrumLive(type: DrumType, velocity: Float = 1.0f, recordTrackId: String? = null) {
        drumSynth.trigger(type, velocity)
        if (isRecording && recordTrackId != null) {
            onLiveRecordStep?.invoke(recordTrackId, currentStep, type, null)
        }
    }

    /**
     * Trigger a melodic note live (from Piano Keys)
     */
    fun triggerNoteLive(
        midiNote: Int,
        velocity: Float = 0.8f,
        trackType: TrackType = TrackType.SYNTH_LEAD,
        recordTrackId: String? = null
    ) {
        when (trackType) {
            TrackType.BASS -> bassSynth.noteOn(midiNote, velocity)
            TrackType.CHORDS -> chordsSynth.noteOn(midiNote, velocity)
            else -> leadSynth.noteOn(midiNote, velocity)
        }
        if (isRecording && recordTrackId != null) {
            onLiveRecordStep?.invoke(recordTrackId, currentStep, null, midiNote)
        }
    }

    fun releaseNoteLive(midiNote: Int, trackType: TrackType = TrackType.SYNTH_LEAD) {
        when (trackType) {
            TrackType.BASS -> bassSynth.noteOff(midiNote)
            TrackType.CHORDS -> chordsSynth.noteOff(midiNote)
            else -> leadSynth.noteOff(midiNote)
        }
    }

    private fun audioLoop(chunkFrames: Int) {
        val stereoFloatBuffer = FloatArray(chunkFrames * 2)
        val shortBuffer = ShortArray(chunkFrames * 2)
        val trackLevels = mutableMapOf<String, Float>()
        val waveSnap = FloatArray(64)
        var waveUpdateCounter = 0

        while (isRunning.get()) {
            stereoFloatBuffer.fill(0f)

            // Step advance calculation
            val samplesPer16th = (60.0f / bpm / 4.0f * sampleRate).toInt()
            val swingOffset = if (currentStep % 2 == 1) (samplesPer16th * swing * 0.4f).toInt() else 0
            val targetStepSamples = samplesPer16th + swingOffset

            if (isPlaying) {
                sampleCounterInStep += chunkFrames
                if (sampleCounterInStep >= targetStepSamples) {
                    sampleCounterInStep -= targetStepSamples
                    advanceStep()
                }
            }

            // Synthesize Instruments
            val project = currentProject
            val anySolo = project?.tracks?.any { it.isSolo } == true

            project?.tracks?.forEach { track ->
                if (track.isMuted) return@forEach
                if (anySolo && !track.isSolo) return@forEach

                val trackGain = track.volume
                val trackPan = track.pan

                when (track.type) {
                    TrackType.DRUMS -> {
                        drumSynth.process(stereoFloatBuffer, chunkFrames, trackGain)
                    }
                    TrackType.BASS -> {
                        bassSynth.process(stereoFloatBuffer, chunkFrames, trackGain, trackPan)
                    }
                    TrackType.SYNTH_LEAD -> {
                        leadSynth.process(stereoFloatBuffer, chunkFrames, trackGain, trackPan)
                    }
                    TrackType.CHORDS -> {
                        chordsSynth.process(stereoFloatBuffer, chunkFrames, trackGain, trackPan)
                    }
                    TrackType.FX -> {
                        // Ambient FX can run on chords or lead synth
                    }
                }
            } ?: run {
                // If project not ready, still allow live synth & drums
                drumSynth.process(stereoFloatBuffer, chunkFrames, 0.9f)
                leadSynth.process(stereoFloatBuffer, chunkFrames, 0.8f, 0f)
            }

            // Metronome synthesis
            if (isPlaying && metronomeEnabled && metronomeSampleIndex in 0 until metronomeLength) {
                for (i in 0 until chunkFrames) {
                    if (metronomeSampleIndex in 0 until metronomeLength) {
                        val t = metronomeSampleIndex.toFloat() / sampleRate
                        val env = 1f - (metronomeSampleIndex.toFloat() / metronomeLength)
                        val click = sin(2f * PI.toFloat() * metronomePitch * t) * env * 0.4f
                        stereoFloatBuffer[i * 2] += click
                        stereoFloatBuffer[i * 2 + 1] += click
                        metronomeSampleIndex++
                    }
                }
            }

            // Master Effects Rack (Filter, Delay, Reverb, Overdrive, Limiter)
            effects.processMasterEffects(stereoFloatBuffer, chunkFrames)

            // Calculate Master RMS & Waveform Snapshot
            var sumSquare = 0f
            var peak = 0f

            for (i in 0 until chunkFrames) {
                val left = (stereoFloatBuffer[i * 2] * masterVolume).coerceIn(-1f, 1f)
                val right = (stereoFloatBuffer[i * 2 + 1] * masterVolume).coerceIn(-1f, 1f)

                shortBuffer[i * 2] = (left * 32767f).toInt().toShort()
                shortBuffer[i * 2 + 1] = (right * 32767f).toInt().toShort()

                val absSample = max(abs(left), abs(right))
                if (absSample > peak) peak = absSample
                sumSquare += left * left + right * right
            }

            // Write to AudioTrack
            audioTrack?.write(shortBuffer, 0, shortBuffer.size)

            // Update VU & Waveform flows occasionally to not burden UI
            waveUpdateCounter++
            if (waveUpdateCounter >= 4) {
                waveUpdateCounter = 0
                val rms = sqrt(sumSquare / (chunkFrames * 2f)).coerceIn(0f, 1f)
                _masterVuFlow.value = (rms * 0.7f + peak * 0.3f).coerceIn(0f, 1f)

                // Sub-sample chunk to 64 points for waveform oscilloscope
                val stepRatio = chunkFrames / 64
                if (stepRatio > 0) {
                    for (k in 0 until 64) {
                        val idx = (k * stepRatio * 2).coerceIn(0, stereoFloatBuffer.size - 1)
                        waveSnap[k] = stereoFloatBuffer[idx]
                    }
                    _waveformFlow.value = waveSnap.copyOf()
                }
            }
        }
    }

    private fun advanceStep() {
        currentStep = (currentStep + 1) % 16
        if (currentStep == 0) {
            currentBar++
            _currentBarFlow.value = currentBar
        }
        _currentStepFlow.value = currentStep

        // Metronome trigger on quarter notes
        if (metronomeEnabled) {
            if (currentStep == 0) {
                metronomePitch = 1600f // High pitch beat 1
                metronomeSampleIndex = 0
            } else if (currentStep % 4 == 0) {
                metronomePitch = 1000f // Softer pitch beats 2, 3, 4
                metronomeSampleIndex = 0
            }
        }

        // Trigger active clips on unmuted tracks
        val project = currentProject ?: return
        val anySolo = project.tracks.any { it.isSolo }

        for (track in project.tracks) {
            if (track.isMuted) continue
            if (anySolo && !track.isSolo) continue

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
    }
}
