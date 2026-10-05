package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DawHeader
import com.example.ui.screens.ClipEditorScreen
import com.example.ui.screens.DrumPadsScreen
import com.example.ui.screens.FxRackScreen
import com.example.ui.screens.MixerScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SessionView
import com.example.ui.screens.SynthKeysScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DawTab
import com.example.viewmodel.DawViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainDawApp()
            }
        }
    }
}

@Composable
fun MainDawApp(viewModel: DawViewModel = viewModel()) {
    val context = LocalContext.current
    val project by viewModel.project.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val selectedTrackId by viewModel.selectedTrackId.collectAsStateWithLifecycle()
    val selectedClipIndex by viewModel.selectedClipIndex.collectAsStateWithLifecycle()
    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val currentStep by viewModel.audioEngine.currentStepFlow.collectAsStateWithLifecycle()
    val currentBar by viewModel.audioEngine.currentBarFlow.collectAsStateWithLifecycle()
    val masterVu by viewModel.audioEngine.masterVuFlow.collectAsStateWithLifecycle()
    val waveform by viewModel.audioEngine.waveformFlow.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Toast/Snackbar for messages
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    // Android Back Button navigates back to Session view if on a sub-screen
    BackHandler(enabled = currentTab != DawTab.SESSION) {
        viewModel.selectTab(DawTab.SESSION)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF141518)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF141518))
        ) {
            // Master DAW Header & Transport Bar
            DawHeader(
                isPlaying = isPlaying,
                isRecording = isRecording,
                metronomeEnabled = viewModel.audioEngine.metronomeEnabled,
                bpm = project.bpm,
                currentBar = currentBar,
                currentStep = currentStep,
                masterVu = masterVu,
                waveform = waveform,
                selectedTab = currentTab,
                onTogglePlay = { viewModel.togglePlay() },
                onStop = { viewModel.stopPlayback() },
                onToggleRecord = { viewModel.toggleRecording() },
                onToggleMetronome = { viewModel.toggleMetronome() },
                onBpmChange = { viewModel.setBpm(it) },
                onTapTempo = { viewModel.tapTempo() },
                onSelectTab = { viewModel.selectTab(it) }
            )

            // Screen View Router
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    DawTab.SESSION -> {
                        SessionView(
                            viewModel = viewModel,
                            project = project,
                            currentStep = currentStep,
                            isPlaying = isPlaying
                        )
                    }
                    DawTab.SEQUENCER -> {
                        ClipEditorScreen(
                            viewModel = viewModel,
                            project = project,
                            selectedTrackId = selectedTrackId,
                            selectedClipIndex = selectedClipIndex,
                            currentStep = currentStep,
                            isPlaying = isPlaying
                        )
                    }
                    DawTab.PADS -> {
                        DrumPadsScreen(
                            viewModel = viewModel,
                            isRecording = isRecording
                        )
                    }
                    DawTab.KEYS -> {
                        SynthKeysScreen(
                            viewModel = viewModel
                        )
                    }
                    DawTab.MIXER -> {
                        MixerScreen(
                            viewModel = viewModel,
                            project = project,
                            masterVu = masterVu
                        )
                    }
                    DawTab.FX -> {
                        FxRackScreen(
                            viewModel = viewModel,
                            effects = project.effects
                        )
                    }
                    DawTab.PROJECTS -> {
                        ProjectsScreen(
                            viewModel = viewModel,
                            project = project,
                            savedProjects = savedProjects,
                            isExporting = isExporting
                        )
                    }
                }
            }
        }
    }
}
