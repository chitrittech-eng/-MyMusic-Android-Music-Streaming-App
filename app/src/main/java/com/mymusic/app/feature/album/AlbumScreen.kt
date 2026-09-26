package com.mymusic.app.feature.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    albumId: String,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(albumId) { viewModel.loadAlbum(albumId) }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpotifyBlack)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpotifyGreen)
            }
        } else {
            val album = uiState.album
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
                            model = album?.coverUrl,
                            contentDescription = album?.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(200.dp).background(SpotifyGray)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(album?.title ?: "", style = MaterialTheme.typography.headlineMedium, color = SpotifyTextPrimary)
                        TextButton(onClick = { album?.artistId?.let { onArtistClick(it) } }) {
                            Text(album?.artistName ?: "", color = SpotifyTextSecondary)
                        }
                        val year = album?.releaseDate?.let {
                            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
                        } ?: ""
                        Text(
                            "${album?.genre ?: ""} • $year • ${uiState.songs.size} songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextSecondary
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { onPlayAll(uiState.songs) },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = SpotifyBlack),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Play")
                        }
                    }
                }

                items(uiState.songs) { song ->
                    SongCard(
                        title = song.title,
                        artistName = song.artistName,
                        coverUrl = song.coverUrl,
                        onPlayClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}
