package com.mymusic.app.feature.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mymusic.app.core.ui.components.SongCard
import com.mymusic.app.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.playerState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = { Text("Queue", color = SpotifyTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpotifyTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpotifyBlack)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (state.currentSong != null) {
                item {
                    Text(
                        "Now playing",
                        style = MaterialTheme.typography.labelLarge,
                        color = SpotifyGreen,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item {
                    state.currentSong?.let { song ->
                        SongCard(
                            title = song.title,
                            artistName = song.artistName,
                            coverUrl = song.coverUrl,
                            isPlaying = true,
                            onPlayClick = {}
                        )
                    }
                }
            }
            if (state.queue.isNotEmpty()) {
                item {
                    Text(
                        "Next in queue",
                        style = MaterialTheme.typography.labelLarge,
                        color = SpotifyTextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                itemsIndexed(state.queue.drop(1)) { _, song ->
                    SongCard(
                        title = song.title,
                        artistName = song.artistName,
                        coverUrl = song.coverUrl,
                        onPlayClick = { viewModel.playSong(song, state.queue) }
                    )
                }
            }
        }
    }
}
