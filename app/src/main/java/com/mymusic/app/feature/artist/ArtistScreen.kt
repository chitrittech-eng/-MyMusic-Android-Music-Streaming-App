package com.mymusic.app.feature.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mymusic.app.core.common.Extensions.formatStreamCount
import com.mymusic.app.core.ui.components.*
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistId: String,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (String) -> Unit,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(artistId) { viewModel.loadArtist(artistId) }

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
        val artist = uiState.artist
        if (uiState.isLoading || artist == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpotifyGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = artist.photoUrl,
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .background(SpotifyGray)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = SpotifyTextPrimary
                        )
                        if (artist.verified) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = SpotifyGreen, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Verified Artist", style = MaterialTheme.typography.labelMedium, color = SpotifyGreen)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${artist.monthlyListeners.formatStreamCount()} monthly listeners",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SpotifyTextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.toggleFollow() },
                                colors = ButtonDefaults.outlinedButtonColors(),
                                border = ButtonDefaults.outlinedButtonBorder,
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (uiState.isFollowing) "Following" else "Follow")
                            }
                        }

                        if (artist.bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = artist.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SpotifyTextSecondary
                            )
                        }
                    }
                }

                if (uiState.topSongs.isNotEmpty()) {
                    item { SectionHeader(title = "Popular") }
                    items(uiState.topSongs.take(5)) { song ->
                        SongCard(
                            title = song.title,
                            artistName = song.artistName,
                            coverUrl = song.coverUrl,
                            onPlayClick = { onSongClick(song) }
                        )
                    }
                }

                if (uiState.albums.isNotEmpty()) {
                    item { SectionHeader(title = "Albums") }
                    items(uiState.albums) { album ->
                        SongCard(
                            title = album.title,
                            artistName = album.artistName,
                            coverUrl = album.coverUrl,
                            onPlayClick = { onAlbumClick(album.id) }
                        )
                    }
                }
            }
        }
    }
}
