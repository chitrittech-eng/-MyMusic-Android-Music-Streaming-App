package com.mymusic.app.feature.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mymusic.app.core.ui.components.SongCard
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: String,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(playlistId) { viewModel.loadPlaylist(playlistId) }

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpotifyTextPrimary)
                    }
                },
                actions = {
                    if (uiState.isOwner) {
                        IconButton(onClick = { viewModel.toggleEdit() }) {
                            Icon(Icons.Default.Edit, "Edit", tint = SpotifyTextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpotifyBlack)
            )
        }
    ) { padding ->
        val playlist = uiState.playlist
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = playlist?.coverUrl,
                        contentDescription = playlist?.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(200.dp).background(SpotifyGray)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(playlist?.name ?: "", style = MaterialTheme.typography.headlineMedium, color = SpotifyTextPrimary)
                    Text(
                        "${playlist?.ownerName} • ${uiState.songs.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyTextSecondary
                    )
                    if (!playlist?.description.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(playlist!!.description, style = MaterialTheme.typography.bodyMedium, color = SpotifyTextSecondary)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onPlayAll(uiState.songs) },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = SpotifyBlack),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Play")
                        }
                        if (!uiState.isOwner) {
                            OutlinedButton(
                                onClick = { viewModel.toggleFollow() },
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Icon(
                                    if (uiState.isFollowing) Icons.Default.Check else Icons.Default.Add,
                                    null, modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (uiState.isFollowing) "Following" else "Follow")
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SpotifyGreen)
                    }
                }
            } else {
                items(uiState.songs) { song ->
                    SongCard(
                        title = song.title,
                        artistName = song.artistName,
                        coverUrl = song.coverUrl,
                        onPlayClick = { onSongClick(song) },
                        onMoreClick = if (uiState.isOwner) {
                            { viewModel.removeSong(song.id) }
                        } else null
                    )
                }
            }
        }
    }
}
