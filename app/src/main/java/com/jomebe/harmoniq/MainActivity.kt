package com.jomebe.harmoniq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jomebe.harmoniq.auth.AuthState
import com.jomebe.harmoniq.ui.AppViewModel
import com.jomebe.harmoniq.ui.components.AuroraBackground
import com.jomebe.harmoniq.ui.components.MiniPlayer
import com.jomebe.harmoniq.ui.screens.HomeScreen
import com.jomebe.harmoniq.ui.screens.LibraryScreen
import com.jomebe.harmoniq.ui.screens.PlayerScreen
import com.jomebe.harmoniq.ui.screens.ProfileScreen
import com.jomebe.harmoniq.ui.screens.SearchScreen
import com.jomebe.harmoniq.ui.theme.HarmoniqTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel> {
        AppViewModel.factory((application as HarmoniqApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HarmoniqTheme { HarmoniqApp(viewModel) } }
    }
}

private data class Tab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun HarmoniqApp(viewModel: AppViewModel) {
    val ui by viewModel.uiState.collectAsState()
    val library by viewModel.library.collectAsState()
    val auth by viewModel.authState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showPlayer by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.completeSignIn(it.data)
    }
    val recoveryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.retryAuthorization()
    }

    LaunchedEffect(auth) {
        if (auth is AuthState.RecoveryRequired) recoveryLauncher.launch((auth as AuthState.RecoveryRequired).intent)
    }
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    BackHandler(showPlayer) { showPlayer = false }
    val tabs = listOf(
        Tab("홈", Icons.Default.Home),
        Tab("검색", Icons.Default.Search),
        Tab("보관함", Icons.Default.LibraryMusic),
        Tab("프로필", Icons.Default.Person)
    )

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                Column {
                    ui.queue.current?.let { track ->
                        MiniPlayer(track, onOpen = { showPlayer = true }, onNext = viewModel::playNext)
                    }
                    NavigationBar {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(tab.icon, tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            AuroraBackground {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (selectedTab) {
                        0 -> HomeScreen(ui.personalized, ui.popular) { track, source ->
                            viewModel.play(track, source)
                            showPlayer = true
                        }
                        1 -> SearchScreen(
                            ui.searchQuery,
                            ui.searchResults,
                            viewModel::updateSearchQuery,
                            viewModel::search
                        ) { track, source ->
                            viewModel.play(track, source)
                            showPlayer = true
                        }
                        2 -> LibraryScreen(library.history, library.saved) { track, source ->
                            viewModel.play(track, source)
                            showPlayer = true
                        }
                        else -> ProfileScreen(
                            auth,
                            onSignIn = { signInLauncher.launch(viewModel.signInIntent()) },
                            onSignOut = viewModel::signOut,
                            onClearHistory = viewModel::clearHistory
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showPlayer && ui.queue.current != null,
            enter = slideInVertically(tween(320)) { it } + fadeIn(),
            exit = slideOutVertically(tween(260)) { it } + fadeOut()
        ) {
            ui.queue.current?.let { track ->
                PlayerScreen(
                    track = track,
                    isSaved = library.saved.any { it.id == track.id },
                    onClose = { showPlayer = false },
                    onPrevious = viewModel::playPrevious,
                    onNext = viewModel::playNext,
                    onEnded = viewModel::onTrackEnded,
                    onToggleSaved = {
                        if (library.saved.any { it.id == track.id }) viewModel.unsave(track) else viewModel.save(track)
                    }
                )
            }
        }

        if (ui.isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}
