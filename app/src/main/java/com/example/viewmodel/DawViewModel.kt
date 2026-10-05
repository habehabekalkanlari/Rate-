package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.WavExporter
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.ProjectJsonConverter
import com.example.data.ProjectRepository
import com.example.model.Clip
import com.example.model.ClipState
import com.example.model.DawProject
import com.example.model.DrumType
import com.example.model.EffectsConfig
import com.example.model.GenrePresets
import com.example.model.MelodicNote
import com.example.model.Track
import com.example.model.TrackType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class DawTab(val title: String) {
    SESSION("Session"),
    SEQUENCER("Sequencer"),
    PADS("Pads"),
    KEYS("Keys"),
    MIXER("Mixer"),
    FX("FX Rack"),
    PROJECTS("Projects")
}

class DawViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    val savedProjects: StateFlow<List<ProjectEntity>>

    val audioEngine: AudioEngine = AudioEngine()

    private val _currentTab = MutableStateFlow(DawTab.SESSION)
    val currentTab: StateFlow<DawTab> = _currentTab.asStateFlow()

    private val _project = MutableStateFlow(GenrePresets.createDefaultProject())
    val project: StateFlow<DawProject> = _project.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId: StateFlow<String?> = _selectedTrackId.asStateFlow()

    private val _selectedClipIndex = MutableStateFlow<Int?>(0)
    val selectedClipIndex: StateFlow<Int?> = _selectedClipIndex.asStateFlow()

    // Tap tempo timing history
    private val tapTimestamps = mutableListOf<Long>()

    // Status toast/message
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Exporting state
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ProjectRepository(db.projectDao())
        savedProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        audioEngine.currentProject = _project.value
        audioEngine.bpm = _project.value.bpm
        audioEngine.swing = _project.value.swing
        audioEngine.masterVolume = _project.value.masterVolume
        applyEffectsToEngine(_project.value.effects)

        // Select first track by default
        _selectedTrackId.value = _project.value.tracks.firstOrNull()?.id

        // Listen for live recording events from audio engine
        audioEngine.onLiveRecordStep = { trackId, step, drumType, midiNote ->
            recordLiveStep(trackId, step, drumType, midiNote)
        }

        // Start real-time audio thread
        audioEngine.start()
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
    }

    fun selectTab(tab: DawTab) {
        _currentTab.value = tab
    }

    fun togglePlay() {
        val playing = audioEngine.togglePlayPause()
        _isPlaying.value = playing
    }

    fun stopPlayback() {
        audioEngine.pause()
        audioEngine.rewind()
        _isPlaying.value = false
    }

    fun toggleRecording() {
        val rec = !audioEngine.isRecording
        audioEngine.isRecording = rec
        _isRecording.value = rec
        if (rec && !_isPlaying.value) {
            audioEngine.play()
            _isPlaying.value = true
        }
    }

    fun toggleMetronome() {
        audioEngine.metronomeEnabled = !audioEngine.metronomeEnabled
    }

    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(50, 220)
        audioEngine.bpm = clamped
        _project.value = _project.value.copy(bpm = clamped)
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)
        if (tapTimestamps.size > 5) {
            tapTimestamps.removeAt(0)
        }
        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                val diff = tapTimestamps[i] - tapTimestamps[i - 1]
                if (diff in 200..2000) {
                    intervals.add(diff)
                }
            }
            if (intervals.isNotEmpty()) {
                val avgInterval = intervals.average()
                val calculatedBpm = (60000.0 / avgInterval).toInt().coerceIn(50, 220)
                setBpm(calculatedBpm)
                _statusMessage.value = "Tap Tempo: $calculatedBpm BPM"
            }
        }
    }

    fun setMasterVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1.2f)
        audioEngine.masterVolume = clamped
        _project.value = _project.value.copy(masterVolume = clamped)
    }

    fun setSwing(swing: Float) {
        val clamped = swing.coerceIn(0f, 0.5f)
        audioEngine.swing = clamped
        _project.value = _project.value.copy(swing = clamped)
    }

    // --- Session Grid Operations ---

    fun launchClip(trackId: String, sceneIndex: Int) {
        val current = _project.value
        val updatedTracks = current.tracks.map { track ->
            if (track.id == trackId) {
                val targetClip = track.clips.find { it.sceneIndex == sceneIndex }
                val updatedClips = track.clips.map { clip ->
                    when {
                        clip.sceneIndex == sceneIndex -> clip.copy(state = ClipState.PLAYING)
                        clip.state == ClipState.PLAYING -> clip.copy(state = ClipState.STOPPED)
                        else -> clip
                    }
                }
                track.copy(
                    clips = updatedClips,
                    activeClipIndex = if (targetClip != null) track.clips.indexOf(targetClip) else null
                )
            } else track
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
        if (!_isPlaying.value) {
            togglePlay()
        }
    }

    fun stopTrackClip(trackId: String) {
        val current = _project.value
        val updatedTracks = current.tracks.map { track ->
            if (track.id == trackId) {
                track.copy(
                    activeClipIndex = null,
                    clips = track.clips.map { it.copy(state = ClipState.STOPPED) }
                )
            } else track
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
    }

    /**
     * Ableton Live Scene Launch: Launches all clips in a horizontal scene row simultaneously
     */
    fun launchScene(sceneIndex: Int) {
        val current = _project.value
        val updatedTracks = current.tracks.map { track ->
            val clipInScene = track.clips.find { it.sceneIndex == sceneIndex }
            val updatedClips = track.clips.map { clip ->
                if (clip.sceneIndex == sceneIndex) {
                    clip.copy(state = ClipState.PLAYING)
                } else if (clip.state == ClipState.PLAYING) {
                    clip.copy(state = ClipState.STOPPED)
                } else clip
            }
            track.copy(
                clips = updatedClips,
                activeClipIndex = if (clipInScene != null) track.clips.indexOf(clipInScene) else null
            )
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
        if (!_isPlaying.value) {
            togglePlay()
        }
    }

    fun stopAllClips() {
        val current = _project.value
        val updatedTracks = current.tracks.map { track ->
            track.copy(
                activeClipIndex = null,
                clips = track.clips.map { it.copy(state = ClipState.STOPPED) }
            )
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
    }

    // --- Mixer Operations ---

    fun setTrackVolume(trackId: String, volume: Float) {
        val current = _project.value
        val updatedTracks = current.tracks.map {
            if (it.id == trackId) it.copy(volume = volume.coerceIn(0f, 1.5f)) else it
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
    }

    fun setTrackPan(trackId: String, pan: Float) {
        val current = _project.value
        val updatedTracks = current.tracks.map {
            if (it.id == trackId) it.copy(pan = pan.coerceIn(-1f, 1f)) else it
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
    }

    fun toggleTrackMute(trackId: String) {
        val current = _project.value
        val updatedTracks = current.tracks.map {
            if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
    }

    fun toggleTrackSolo(trackId: String) {
        val current = _project.value
        val updatedTracks = current.tracks.map {
            if (it.id == trackId) it.copy(isSolo = !it.isSolo) else it
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
    }

    // --- Sequencer & Clip Editor Operations ---

    fun selectTrack(trackId: String) {
        _selectedTrackId.value = trackId
    }

    fun selectClip(clipIndex: Int) {
        _selectedClipIndex.value = clipIndex
    }

    fun toggleDrumStep(drumType: DrumType, step: Int) {
        val trackId = _selectedTrackId.value ?: return
        val clipIdx = _selectedClipIndex.value ?: 0
        val current = _project.value

        val updatedTracks = current.tracks.map { track ->
            if (track.id == trackId && track.type == TrackType.DRUMS) {
                val updatedClips = track.clips.mapIndexed { idx, clip ->
                    if (idx == clipIdx) {
                        val currentSteps = clip.drumSteps[drumType]?.toMutableList()
                            ?: MutableList(16) { false }
                        if (step in 0 until currentSteps.size) {
                            currentSteps[step] = !currentSteps[step]
                            if (currentSteps[step]) {
                                audioEngine.triggerDrumLive(drumType, 0.9f)
                            }
                        }
                        val newMap = clip.drumSteps.toMutableMap()
                        newMap[drumType] = currentSteps
                        clip.copy(drumSteps = newMap)
                    } else clip
                }
                track.copy(clips = updatedClips)
            } else track
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
    }

    fun toggleMelodicNote(step: Int, midiNote: Int) {
        val trackId = _selectedTrackId.value ?: return
        val clipIdx = _selectedClipIndex.value ?: 0
        val current = _project.value

        val targetTrack = current.tracks.find { it.id == trackId } ?: return

        val updatedTracks = current.tracks.map { track ->
            if (track.id == trackId) {
                val updatedClips = track.clips.mapIndexed { idx, clip ->
                    if (idx == clipIdx) {
                        val existing = clip.melodicNotes.find { it.step == step && it.midiNote == midiNote }
                        val newNotes = if (existing != null) {
                            clip.melodicNotes.filter { !(it.step == step && it.midiNote == midiNote) }
                        } else {
                            audioEngine.triggerNoteLive(midiNote, 0.8f, targetTrack.type)
                            clip.melodicNotes + MelodicNote(step, midiNote)
                        }
                        clip.copy(melodicNotes = newNotes)
                    } else clip
                }
                track.copy(clips = updatedClips)
            } else track
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
    }

    fun clearClip(trackId: String, clipIndex: Int) {
        val current = _project.value
        val updatedTracks = current.tracks.map { track ->
            if (track.id == trackId) {
                val updatedClips = track.clips.mapIndexed { idx, clip ->
                    if (idx == clipIndex) {
                        clip.copy(drumSteps = emptyMap(), melodicNotes = emptyList())
                    } else clip
                }
                track.copy(clips = updatedClips)
            } else track
        }
        updateProjectAndEngine(current.copy(tracks = updatedTracks))
        _statusMessage.value = "Clip cleared"
    }

    // --- Live Trigger & Record ---

    fun triggerDrumPad(type: DrumType, velocity: Float = 1.0f) {
        val recordTrack = _selectedTrackId.value
        audioEngine.triggerDrumLive(type, velocity, recordTrack)
    }

    fun triggerPianoKey(midiNote: Int, velocity: Float = 0.8f) {
        val trackId = _selectedTrackId.value
        val track = _project.value.tracks.find { it.id == trackId }
        val trackType = track?.type ?: TrackType.SYNTH_LEAD
        audioEngine.triggerNoteLive(midiNote, velocity, trackType, trackId)
    }

    fun releasePianoKey(midiNote: Int) {
        val trackId = _selectedTrackId.value
        val track = _project.value.tracks.find { it.id == trackId }
        val trackType = track?.type ?: TrackType.SYNTH_LEAD
        audioEngine.releaseNoteLive(midiNote, trackType)
    }

    private fun recordLiveStep(trackId: String, step: Int, drumType: DrumType?, midiNote: Int?) {
        val clipIdx = _selectedClipIndex.value ?: 0
        val current = _project.value

        val updatedTracks = current.tracks.map { track ->
            if (track.id == trackId) {
                val updatedClips = track.clips.mapIndexed { idx, clip ->
                    if (idx == clipIdx) {
                        if (drumType != null) {
                            val steps = clip.drumSteps[drumType]?.toMutableList() ?: MutableList(16) { false }
                            if (step in 0 until steps.size) steps[step] = true
                            val newMap = clip.drumSteps.toMutableMap()
                            newMap[drumType] = steps
                            clip.copy(drumSteps = newMap)
                        } else if (midiNote != null) {
                            val notes = clip.melodicNotes.filter { !(it.step == step && it.midiNote == midiNote) } +
                                    MelodicNote(step, midiNote)
                            clip.copy(melodicNotes = notes)
                        } else clip
                    } else clip
                }
                track.copy(clips = updatedClips)
            } else track
        }
        _project.value = current.copy(tracks = updatedTracks)
        audioEngine.currentProject = _project.value
    }

    // --- Effects Management ---

    fun updateEffects(update: EffectsConfig.() -> EffectsConfig) {
        val newFx = update(_project.value.effects)
        _project.value = _project.value.copy(effects = newFx)
        applyEffectsToEngine(newFx)
    }

    private fun applyEffectsToEngine(fx: EffectsConfig) {
        audioEngine.effects.apply {
            filterCutoffNorm = fx.filterCutoff
            filterResonance = fx.filterResonance
            filterIsHighPass = fx.filterType == 1
            delayTimeSec = fx.delayTimeSec
            delayFeedback = fx.delayFeedback
            delayMix = fx.delayMix
            reverbSize = fx.reverbSize
            reverbMix = fx.reverbMix
            drive = fx.drive
        }
    }

    // --- Project Database Persistence ---

    fun saveProject(customName: String? = null) {
        viewModelScope.launch {
            val proj = _project.value
            val name = customName?.takeIf { it.isNotBlank() } ?: proj.name
            val json = ProjectJsonConverter.toJson(proj.copy(name = name))
            val entity = ProjectEntity(
                name = name,
                genre = "Live Studio Session",
                bpm = proj.bpm,
                projectDataJson = json
            )
            val newId = repository.saveProject(entity)
            _project.value = proj.copy(id = newId, name = name)
            _statusMessage.value = "Project \"$name\" saved!"
        }
    }

    fun loadProjectFromDb(entity: ProjectEntity) {
        val loaded = ProjectJsonConverter.fromJson(entity.projectDataJson)
        if (loaded != null) {
            updateProjectAndEngine(loaded.copy(id = entity.id, name = entity.name))
            _selectedTrackId.value = loaded.tracks.firstOrNull()?.id
            _selectedClipIndex.value = 0
            _statusMessage.value = "Loaded \"${entity.name}\""
            _currentTab.value = DawTab.SESSION
        } else {
            _statusMessage.value = "Failed to parse project data"
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            _statusMessage.value = "Project deleted"
        }
    }

    fun loadPreset(name: String) {
        val preset = when (name.lowercase()) {
            "synthwave" -> GenrePresets.createSynthwaveProject()
            "trap" -> GenrePresets.createTrapProject()
            else -> GenrePresets.createTechnoProject()
        }
        updateProjectAndEngine(preset)
        _selectedTrackId.value = preset.tracks.firstOrNull()?.id
        _selectedClipIndex.value = 0
        _statusMessage.value = "Loaded ${preset.name} preset"
    }

    fun exportToWav(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val file = WavExporter.exportProjectToWav(context, _project.value, barsToRender = 4)
                _statusMessage.value = "Exported: ${file.name}"
                onReady(file)
            } catch (e: Exception) {
                _statusMessage.value = "Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    private fun updateProjectAndEngine(newProject: DawProject) {
        _project.value = newProject
        audioEngine.currentProject = newProject
        audioEngine.bpm = newProject.bpm
        audioEngine.swing = newProject.swing
        audioEngine.masterVolume = newProject.masterVolume
        applyEffectsToEngine(newProject.effects)
    }
}
