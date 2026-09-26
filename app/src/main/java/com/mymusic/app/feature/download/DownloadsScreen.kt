package com.mymusic.app.feature.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mymusic.app.core.ui.components.EmptyState
import com.mymusic.app.core.ui.components.SongCard
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = { Text("Downloads", color = SpotifyTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpotifyTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpotifyBlack)
            )
        }
    ) { padding ->
        if (uiState.downloads.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Download,
                title = "No downloads",
                subtitle = "Songs you download will appear here for offline listening",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                item {
                    Text(
                        "${uiState.downloads.size} songs downloaded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpotifyTextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(uiState.downloads) { song ->
                    SongCard(
                        title = song.title,
                        artistName = song.artistName,
                        coverUrl = song.coverUrl,
                        onPlayClick = { onSongClick(song) },
                        onMoreClick = { viewModel.deleteDownload(song.id) }
                    )
                }
            }
        }
    }
}
