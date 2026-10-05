package com.example.model

enum class TrackType(val displayName: String) {
    DRUMS("Drums"),
    BASS("Bass"),
    SYNTH_LEAD("Synth Lead"),
    CHORDS("Chords/Keys"),
    FX("FX / Sample")
}

enum class DrumType(val displayName: String, val shortName: String, val defaultPitch: Float) {
    KICK("Kick Drum", "KCK", 55f),
    SNARE("Snare Drum", "SNR", 180f),
    HIHAT_CLOSED("Hi-Hat Closed", "HHC", 8000f),
    HIHAT_OPEN("Hi-Hat Open", "HHO", 7500f),
    CLAP("Hand Clap", "CLP", 1200f),
    RIMSHOT("Rimshot / Perc", "RIM", 950f),
    TOM("808 Tom", "TOM", 110f),
    CRASH("Crash Cymbal", "CRS", 6000f)
}

enum class ClipState {
    STOPPED,
    QUEUED,
    PLAYING
}

data class MelodicNote(
    val step: Int,             // 0..15 (or 0..31)
    val midiNote: Int,         // 36..84 (C2 to C6)
    val durationSteps: Int = 1,
    val velocity: Float = 0.8f
)

data class Clip(
    val id: String,
    val name: String,
    val sceneIndex: Int,
    val trackId: String,
    val colorHex: Long,
    val state: ClipState = ClipState.STOPPED,
    // For Drum tracks: Map instrument to 16-step booleans
    val drumSteps: Map<DrumType, List<Boolean>> = emptyMap(),
    // For Synth / Bass / Chords tracks: Melodic notes
    val melodicNotes: List<MelodicNote> = emptyList(),
    val lengthSteps: Int = 16
)

data class Track(
    val id: String,
    val name: String,
    val type: TrackType,
    val colorHex: Long,
    val volume: Float = 0.8f,
    val pan: Float = 0.0f,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val isArmed: Boolean = false,
    val clips: List<Clip> = emptyList(),
    val activeClipIndex: Int? = null,
    val currentVuLevel: Float = 0f
)

data class Scene(
    val index: Int,
    val name: String,
    val colorHex: Long
)

data class EffectsConfig(
    val filterCutoff: Float = 0.85f,      // 0.05 to 0.99
    val filterResonance: Float = 0.2f,   // 0.0 to 0.85
    val filterType: Int = 0,             // 0 = LP, 1 = HP
    val delayTimeSec: Float = 0.25f,     // 0.05 to 0.75
    val delayFeedback: Float = 0.35f,    // 0.0 to 0.85
    val delayMix: Float = 0.2f,          // 0.0 to 1.0
    val reverbSize: Float = 0.5f,        // 0.0 to 0.95
    val reverbMix: Float = 0.25f,        // 0.0 to 0.9
    val drive: Float = 0.15f             // 0.0 to 0.9
)

data class DawProject(
    val id: Long = 0,
    val name: String = "Untitled Jam",
    val bpm: Int = 124,
    val swing: Float = 0.0f,
    val masterVolume: Float = 0.85f,
    val tracks: List<Track> = emptyList(),
    val scenes: List<Scene> = emptyList(),
    val effects: EffectsConfig = EffectsConfig()
)
