package com.example.model

import java.util.UUID

object GenrePresets {

    fun createDefaultProject(): DawProject = createTechnoProject()

    fun createTechnoProject(): DawProject {
        val scenes = listOf(
            Scene(0, "1. Intro Groove", 0xFF20C997),
            Scene(1, "2. Acid Bassline", 0xFFFF922B),
            Scene(2, "3. Peak Energy", 0xFFFF6B6B),
            Scene(3, "4. Deep Breakdown", 0xFF845EF7)
        )

        // Drum Clips
        val drumTrackId = "track_drums"
        val drumClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Intro 4x4",
                sceneIndex = 0,
                trackId = drumTrackId,
                colorHex = 0xFFFF6B6B,
                state = ClipState.PLAYING,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(0, 0, 1, 0,  0, 0, 1, 0,  0, 0, 1, 0,  0, 0, 1, 0),
                    DrumType.CLAP to patternOf(0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Drive Beat",
                sceneIndex = 1,
                trackId = drumTrackId,
                colorHex = 0xFFFF6B6B,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.SNARE to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
                    DrumType.HIHAT_OPEN to patternOf(0, 0, 1, 0,  0, 0, 1, 0,  0, 0, 1, 0,  0, 0, 1, 0),
                    DrumType.CLAP to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Peak Drum Roll",
                sceneIndex = 2,
                trackId = drumTrackId,
                colorHex = 0xFFFF6B6B,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.SNARE to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 1, 1),
                    DrumType.HIHAT_CLOSED to patternOf(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
                    DrumType.HIHAT_OPEN to patternOf(0, 0, 1, 0,  0, 0, 1, 0,  0, 0, 1, 0,  0, 0, 1, 0),
                    DrumType.CRASH to patternOf(1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0),
                    DrumType.CLAP to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Breakdown Hats",
                sceneIndex = 3,
                trackId = drumTrackId,
                colorHex = 0xFFFF6B6B,
                drumSteps = mapOf(
                    DrumType.HIHAT_CLOSED to patternOf(0, 1, 0, 1,  0, 1, 0, 1,  0, 1, 0, 1,  0, 1, 0, 1),
                    DrumType.HIHAT_OPEN to patternOf(0, 0, 1, 0,  0, 0, 0, 0,  0, 0, 1, 0,  0, 0, 0, 0),
                    DrumType.RIMSHOT to patternOf(0, 0, 0, 1,  0, 0, 0, 0,  0, 0, 0, 1,  0, 0, 0, 0)
                )
            )
        )

        // Bass Clips
        val bassTrackId = "track_bass"
        val bassClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Sub Pulse",
                sceneIndex = 0,
                trackId = bassTrackId,
                colorHex = 0xFF339AF0,
                state = ClipState.PLAYING,
                melodicNotes = listOf(
                    MelodicNote(0, 36), MelodicNote(4, 36),
                    MelodicNote(8, 36), MelodicNote(12, 39)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Acid 303 Riff",
                sceneIndex = 1,
                trackId = bassTrackId,
                colorHex = 0xFF339AF0,
                melodicNotes = listOf(
                    MelodicNote(0, 36), MelodicNote(2, 36), MelodicNote(3, 48),
                    MelodicNote(6, 39), MelodicNote(8, 41), MelodicNote(10, 43),
                    MelodicNote(12, 36), MelodicNote(14, 46)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Peak Rolling Bass",
                sceneIndex = 2,
                trackId = bassTrackId,
                colorHex = 0xFF339AF0,
                melodicNotes = listOf(
                    MelodicNote(0, 36), MelodicNote(1, 36), MelodicNote(2, 36), MelodicNote(3, 48),
                    MelodicNote(4, 36), MelodicNote(5, 36), MelodicNote(6, 39), MelodicNote(7, 36),
                    MelodicNote(8, 41), MelodicNote(9, 36), MelodicNote(10, 43), MelodicNote(11, 36),
                    MelodicNote(12, 36), MelodicNote(13, 36), MelodicNote(14, 46), MelodicNote(15, 36)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Deep Drone",
                sceneIndex = 3,
                trackId = bassTrackId,
                colorHex = 0xFF339AF0,
                melodicNotes = listOf(
                    MelodicNote(0, 36, durationSteps = 8),
                    MelodicNote(8, 39, durationSteps = 8)
                )
            )
        )

        // Synth Lead Clips
        val synthTrackId = "track_lead"
        val synthClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Minimal Hook",
                sceneIndex = 0,
                trackId = synthTrackId,
                colorHex = 0xFF51CF66,
                state = ClipState.PLAYING,
                melodicNotes = listOf(
                    MelodicNote(2, 60), MelodicNote(6, 63),
                    MelodicNote(10, 65), MelodicNote(14, 67)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Melodic Drive",
                sceneIndex = 1,
                trackId = synthTrackId,
                colorHex = 0xFF51CF66,
                melodicNotes = listOf(
                    MelodicNote(0, 60), MelodicNote(3, 63), MelodicNote(6, 67),
                    MelodicNote(8, 70), MelodicNote(11, 67), MelodicNote(14, 65)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Peak Arpeggio",
                sceneIndex = 2,
                trackId = synthTrackId,
                colorHex = 0xFF51CF66,
                melodicNotes = listOf(
                    MelodicNote(0, 60), MelodicNote(2, 63), MelodicNote(4, 67), MelodicNote(6, 72),
                    MelodicNote(8, 70), MelodicNote(10, 67), MelodicNote(12, 65), MelodicNote(14, 63)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Atmosphere",
                sceneIndex = 3,
                trackId = synthTrackId,
                colorHex = 0xFF51CF66,
                melodicNotes = listOf(
                    MelodicNote(0, 72, durationSteps = 4),
                    MelodicNote(8, 70, durationSteps = 4)
                )
            )
        )

        // Chords Clips
        val chordsTrackId = "track_chords"
        val chordClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Stab Cm",
                sceneIndex = 0,
                trackId = chordsTrackId,
                colorHex = 0xFFFCC419,
                state = ClipState.STOPPED,
                melodicNotes = listOf(
                    MelodicNote(4, 60), MelodicNote(4, 63), MelodicNote(4, 67),
                    MelodicNote(12, 60), MelodicNote(12, 63), MelodicNote(12, 67)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Dub Chords",
                sceneIndex = 1,
                trackId = chordsTrackId,
                colorHex = 0xFFFCC419,
                melodicNotes = listOf(
                    MelodicNote(2, 60), MelodicNote(2, 63), MelodicNote(2, 67),
                    MelodicNote(6, 58), MelodicNote(6, 61), MelodicNote(6, 65),
                    MelodicNote(10, 60), MelodicNote(10, 63), MelodicNote(10, 67),
                    MelodicNote(14, 62), MelodicNote(14, 65), MelodicNote(14, 69)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Anthem Chords",
                sceneIndex = 2,
                trackId = chordsTrackId,
                colorHex = 0xFFFCC419,
                melodicNotes = listOf(
                    MelodicNote(0, 60), MelodicNote(0, 63), MelodicNote(0, 67),
                    MelodicNote(4, 60), MelodicNote(4, 63), MelodicNote(4, 67),
                    MelodicNote(8, 58), MelodicNote(8, 62), MelodicNote(8, 65),
                    MelodicNote(12, 63), MelodicNote(12, 67), MelodicNote(12, 70)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Ambient Pad",
                sceneIndex = 3,
                trackId = chordsTrackId,
                colorHex = 0xFFFCC419,
                melodicNotes = listOf(
                    MelodicNote(0, 60, durationSteps = 8),
                    MelodicNote(0, 63, durationSteps = 8),
                    MelodicNote(0, 67, durationSteps = 8),
                    MelodicNote(8, 58, durationSteps = 8),
                    MelodicNote(8, 62, durationSteps = 8),
                    MelodicNote(8, 65, durationSteps = 8)
                )
            )
        )

        val tracks = listOf(
            Track(
                id = drumTrackId,
                name = "Drums 909",
                type = TrackType.DRUMS,
                colorHex = 0xFFFF6B6B,
                volume = 0.9f,
                pan = 0f,
                clips = drumClips,
                activeClipIndex = 0
            ),
            Track(
                id = bassTrackId,
                name = "Acid Bass",
                type = TrackType.BASS,
                colorHex = 0xFF339AF0,
                volume = 0.85f,
                pan = -0.1f,
                clips = bassClips,
                activeClipIndex = 0
            ),
            Track(
                id = synthTrackId,
                name = "Pluck Lead",
                type = TrackType.SYNTH_LEAD,
                colorHex = 0xFF51CF66,
                volume = 0.8f,
                pan = 0.15f,
                clips = synthClips,
                activeClipIndex = 0
            ),
            Track(
                id = chordsTrackId,
                name = "Dub Chords",
                type = TrackType.CHORDS,
                colorHex = 0xFFFCC419,
                volume = 0.75f,
                pan = 0.0f,
                clips = chordClips,
                activeClipIndex = null
            )
        )

        return DawProject(
            id = 1,
            name = "Berlin Underground",
            bpm = 128,
            swing = 0.05f,
            masterVolume = 0.85f,
            tracks = tracks,
            scenes = scenes,
            effects = EffectsConfig(
                filterCutoff = 0.82f,
                filterResonance = 0.35f,
                delayTimeSec = 0.23f,
                delayFeedback = 0.4f,
                delayMix = 0.25f,
                reverbSize = 0.6f,
                reverbMix = 0.3f,
                drive = 0.2f
            )
        )
    }

    fun createSynthwaveProject(): DawProject {
        val scenes = listOf(
            Scene(0, "1. Sunset Cruise", 0xFFFF007F),
            Scene(1, "2. Neon Drive", 0xFF00E5FF),
            Scene(2, "3. Outrun Highway", 0xFFFFD700),
            Scene(3, "4. Midnight Echo", 0xFF9D00FF)
        )

        val drumTrackId = "synthwave_drums"
        val drumClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Linn Beat 1",
                sceneIndex = 0,
                trackId = drumTrackId,
                colorHex = 0xFFFF007F,
                state = ClipState.PLAYING,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  0, 0, 1, 0,  1, 0, 0, 0,  0, 0, 0, 0),
                    DrumType.SNARE to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Linn Beat 2",
                sceneIndex = 1,
                trackId = drumTrackId,
                colorHex = 0xFFFF007F,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.SNARE to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
                    DrumType.TOM to patternOf(0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 1, 0)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Highway Full",
                sceneIndex = 2,
                trackId = drumTrackId,
                colorHex = 0xFFFF007F,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 1,  0, 0, 1, 0,  1, 0, 0, 1,  0, 0, 1, 0),
                    DrumType.SNARE to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
                    DrumType.HIHAT_OPEN to patternOf(0, 0, 0, 0,  0, 0, 1, 0,  0, 0, 0, 0,  0, 0, 1, 0),
                    DrumType.CRASH to patternOf(1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Half-Time",
                sceneIndex = 3,
                trackId = drumTrackId,
                colorHex = 0xFFFF007F,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0),
                    DrumType.SNARE to patternOf(0, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 0, 1, 0,  1, 0, 1, 0,  1, 0, 1, 0,  1, 0, 1, 0)
                )
            )
        )

        val bassTrackId = "synthwave_bass"
        val bassClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Running 16th",
                sceneIndex = 0,
                trackId = bassTrackId,
                colorHex = 0xFF00E5FF,
                state = ClipState.PLAYING,
                melodicNotes = listOf(
                    MelodicNote(0, 41), MelodicNote(1, 41), MelodicNote(2, 41), MelodicNote(3, 41),
                    MelodicNote(4, 41), MelodicNote(5, 41), MelodicNote(6, 41), MelodicNote(7, 41),
                    MelodicNote(8, 38), MelodicNote(9, 38), MelodicNote(10, 38), MelodicNote(11, 38),
                    MelodicNote(12, 45), MelodicNote(13, 45), MelodicNote(14, 43), MelodicNote(15, 43)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Octave Bounce",
                sceneIndex = 1,
                trackId = bassTrackId,
                colorHex = 0xFF00E5FF,
                melodicNotes = listOf(
                    MelodicNote(0, 41), MelodicNote(2, 53), MelodicNote(4, 41), MelodicNote(6, 53),
                    MelodicNote(8, 38), MelodicNote(10, 50), MelodicNote(12, 45), MelodicNote(14, 57)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Turbo Bass",
                sceneIndex = 2,
                trackId = bassTrackId,
                colorHex = 0xFF00E5FF,
                melodicNotes = listOf(
                    MelodicNote(0, 41), MelodicNote(1, 53), MelodicNote(2, 41), MelodicNote(3, 53),
                    MelodicNote(4, 41), MelodicNote(5, 53), MelodicNote(6, 41), MelodicNote(7, 53),
                    MelodicNote(8, 38), MelodicNote(9, 50), MelodicNote(10, 38), MelodicNote(11, 50),
                    MelodicNote(12, 45), MelodicNote(13, 57), MelodicNote(14, 43), MelodicNote(15, 55)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Low Drone",
                sceneIndex = 3,
                trackId = bassTrackId,
                colorHex = 0xFF00E5FF,
                melodicNotes = listOf(
                    MelodicNote(0, 41, durationSteps = 16)
                )
            )
        )

        val synthTrackId = "synthwave_lead"
        val synthClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Sunset Arp",
                sceneIndex = 0,
                trackId = synthTrackId,
                colorHex = 0xFFFFD700,
                state = ClipState.PLAYING,
                melodicNotes = listOf(
                    MelodicNote(0, 65), MelodicNote(2, 69), MelodicNote(4, 72), MelodicNote(6, 76),
                    MelodicNote(8, 62), MelodicNote(10, 65), MelodicNote(12, 69), MelodicNote(14, 74)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Hero Melody",
                sceneIndex = 1,
                trackId = synthTrackId,
                colorHex = 0xFFFFD700,
                melodicNotes = listOf(
                    MelodicNote(0, 72, durationSteps = 3), MelodicNote(4, 74, durationSteps = 2),
                    MelodicNote(6, 76, durationSteps = 3), MelodicNote(10, 72, durationSteps = 2),
                    MelodicNote(12, 69, durationSteps = 4)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Hyper Lead",
                sceneIndex = 2,
                trackId = synthTrackId,
                colorHex = 0xFFFFD700,
                melodicNotes = listOf(
                    MelodicNote(0, 72), MelodicNote(2, 74), MelodicNote(4, 76), MelodicNote(6, 77),
                    MelodicNote(8, 76), MelodicNote(10, 74), MelodicNote(12, 72), MelodicNote(14, 69)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Soft Echo",
                sceneIndex = 3,
                trackId = synthTrackId,
                colorHex = 0xFFFFD700,
                melodicNotes = listOf(
                    MelodicNote(4, 76, durationSteps = 4),
                    MelodicNote(12, 74, durationSteps = 4)
                )
            )
        )

        val tracks = listOf(
            Track(id = drumTrackId, name = "LinnDrum 80s", type = TrackType.DRUMS, colorHex = 0xFFFF007F, volume = 0.85f, clips = drumClips, activeClipIndex = 0),
            Track(id = bassTrackId, name = "Synth Bass", type = TrackType.BASS, colorHex = 0xFF00E5FF, volume = 0.9f, clips = bassClips, activeClipIndex = 0),
            Track(id = synthTrackId, name = "Neon Lead", type = TrackType.SYNTH_LEAD, colorHex = 0xFFFFD700, volume = 0.8f, clips = synthClips, activeClipIndex = 0)
        )

        return DawProject(
            id = 2,
            name = "Synthwave 1984",
            bpm = 115,
            swing = 0.0f,
            masterVolume = 0.85f,
            tracks = tracks,
            scenes = scenes,
            effects = EffectsConfig(
                filterCutoff = 0.9f,
                filterResonance = 0.2f,
                delayTimeSec = 0.32f,
                delayFeedback = 0.45f,
                delayMix = 0.3f,
                reverbSize = 0.7f,
                reverbMix = 0.35f,
                drive = 0.1f
            )
        )
    }

    fun createTrapProject(): DawProject {
        val scenes = listOf(
            Scene(0, "1. 808 Foundation", 0xFFFA5252),
            Scene(1, "2. Fast Roll Drop", 0xFFBE4BDB),
            Scene(2, "3. Full Energy", 0xFF4C6EF5),
            Scene(3, "4. Chilled Outro", 0xFF12B886)
        )

        val drumTrackId = "trap_drums"
        val drumClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "808 Kick & Clap",
                sceneIndex = 0,
                trackId = drumTrackId,
                colorHex = 0xFFFA5252,
                state = ClipState.PLAYING,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 1, 0,  0, 0, 0, 0),
                    DrumType.CLAP to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 0, 1, 0,  1, 0, 1, 0,  1, 0, 1, 0,  1, 0, 1, 0)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Hi-Hat Rolls",
                sceneIndex = 1,
                trackId = drumTrackId,
                colorHex = 0xFFFA5252,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  0, 0, 1, 0,  0, 0, 1, 0,  0, 1, 0, 0),
                    DrumType.CLAP to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
                    DrumType.HIHAT_OPEN to patternOf(0, 0, 0, 1,  0, 0, 0, 0,  0, 0, 0, 1,  0, 0, 0, 0)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Hard Banger",
                sceneIndex = 2,
                trackId = drumTrackId,
                colorHex = 0xFFFA5252,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  0, 0, 1, 0,  0, 0, 1, 0,  1, 0, 0, 0),
                    DrumType.SNARE to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.CLAP to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
                    DrumType.CRASH to patternOf(1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Slow Bounce",
                sceneIndex = 3,
                trackId = drumTrackId,
                colorHex = 0xFFFA5252,
                drumSteps = mapOf(
                    DrumType.KICK to patternOf(1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0),
                    DrumType.RIMSHOT to patternOf(0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0),
                    DrumType.HIHAT_CLOSED to patternOf(1, 0, 0, 1,  0, 0, 1, 0,  0, 1, 0, 0,  1, 0, 1, 0)
                )
            )
        )

        val bassTrackId = "trap_808"
        val bassClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Deep 808 Glide",
                sceneIndex = 0,
                trackId = bassTrackId,
                colorHex = 0xFFBE4BDB,
                state = ClipState.PLAYING,
                melodicNotes = listOf(
                    MelodicNote(0, 36, durationSteps = 4),
                    MelodicNote(10, 38, durationSteps = 3)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "808 Slide Drop",
                sceneIndex = 1,
                trackId = bassTrackId,
                colorHex = 0xFFBE4BDB,
                melodicNotes = listOf(
                    MelodicNote(0, 36, durationSteps = 3),
                    MelodicNote(6, 43, durationSteps = 2),
                    MelodicNote(10, 41, durationSteps = 3),
                    MelodicNote(14, 48, durationSteps = 2)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Heavy Drill 808",
                sceneIndex = 2,
                trackId = bassTrackId,
                colorHex = 0xFFBE4BDB,
                melodicNotes = listOf(
                    MelodicNote(0, 36, durationSteps = 3),
                    MelodicNote(4, 36, durationSteps = 2),
                    MelodicNote(7, 39, durationSteps = 3),
                    MelodicNote(11, 41, durationSteps = 2),
                    MelodicNote(14, 38, durationSteps = 2)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Low Drone",
                sceneIndex = 3,
                trackId = bassTrackId,
                colorHex = 0xFFBE4BDB,
                melodicNotes = listOf(
                    MelodicNote(0, 36, durationSteps = 16)
                )
            )
        )

        val synthTrackId = "trap_flute"
        val synthClips = listOf(
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Dark Bell",
                sceneIndex = 0,
                trackId = synthTrackId,
                colorHex = 0xFF4C6EF5,
                state = ClipState.PLAYING,
                melodicNotes = listOf(
                    MelodicNote(0, 60), MelodicNote(3, 63), MelodicNote(6, 67),
                    MelodicNote(9, 70), MelodicNote(12, 67), MelodicNote(14, 65)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Flute Melody",
                sceneIndex = 1,
                trackId = synthTrackId,
                colorHex = 0xFF4C6EF5,
                melodicNotes = listOf(
                    MelodicNote(0, 72, durationSteps = 2), MelodicNote(3, 75, durationSteps = 2),
                    MelodicNote(6, 79, durationSteps = 3), MelodicNote(10, 77, durationSteps = 2),
                    MelodicNote(13, 75, durationSteps = 3)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Fast Trap Arp",
                sceneIndex = 2,
                trackId = synthTrackId,
                colorHex = 0xFF4C6EF5,
                melodicNotes = listOf(
                    MelodicNote(0, 72), MelodicNote(2, 75), MelodicNote(4, 79), MelodicNote(6, 84),
                    MelodicNote(8, 82), MelodicNote(10, 79), MelodicNote(12, 75), MelodicNote(14, 74)
                )
            ),
            Clip(
                id = UUID.randomUUID().toString(),
                name = "Minimal Chime",
                sceneIndex = 3,
                trackId = synthTrackId,
                colorHex = 0xFF4C6EF5,
                melodicNotes = listOf(
                    MelodicNote(0, 72, durationSteps = 4),
                    MelodicNote(8, 70, durationSteps = 4)
                )
            )
        )

        val tracks = listOf(
            Track(id = drumTrackId, name = "808 Kit", type = TrackType.DRUMS, colorHex = 0xFFFA5252, volume = 0.9f, clips = drumClips, activeClipIndex = 0),
            Track(id = bassTrackId, name = "808 Sub", type = TrackType.BASS, colorHex = 0xFFBE4BDB, volume = 0.95f, clips = bassClips, activeClipIndex = 0),
            Track(id = synthTrackId, name = "Dark Pluck", type = TrackType.SYNTH_LEAD, colorHex = 0xFF4C6EF5, volume = 0.75f, clips = synthClips, activeClipIndex = 0)
        )

        return DawProject(
            id = 3,
            name = "Atlanta Heat 808",
            bpm = 140,
            swing = 0.12f,
            masterVolume = 0.85f,
            tracks = tracks,
            scenes = scenes,
            effects = EffectsConfig(
                filterCutoff = 0.85f,
                filterResonance = 0.25f,
                delayTimeSec = 0.21f,
                delayFeedback = 0.35f,
                delayMix = 0.2f,
                reverbSize = 0.45f,
                reverbMix = 0.25f,
                drive = 0.28f
            )
        )
    }

    private fun patternOf(vararg values: Int): List<Boolean> {
        return values.map { it == 1 }
    }
}
