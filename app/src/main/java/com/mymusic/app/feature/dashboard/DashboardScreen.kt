package com.mymusic.app.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mymusic.app.core.common.Extensions.formatStreamCount
import com.mymusic.app.core.ui.components.SongCard
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onUploadClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = { Text("Artist Dashboard", color = SpotifyTextPrimary) },
                actions = {
                    IconButton(onClick = onUploadClick) {
                        Icon(Icons.Default.Add, "Upload song", tint = SpotifyGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpotifyBlack)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Total Streams",
                        value = uiState.artist?.totalStreams?.formatStreamCount() ?: "0",
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Monthly Listeners",
                        value = uiState.artist?.monthlyListeners?.formatStreamCount() ?: "0",
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Followers",
                        value = (uiState.artist?.followers?.size ?: 0).toString(),
                        icon = Icons.Default.PersonAdd,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Songs",
                        value = uiState.songs.size.toString(),
                        icon = Icons.Default.MusicNote,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pending approval notice
            val pendingSongs = uiState.songs.filter { !it.isApproved }
            if (pendingSongs.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SpotifyWarning.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, null, tint = SpotifyWarning)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${pendingSongs.size} song(s) pending admin approval",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SpotifyWarning
                            )
                        }
                    }
                }
            }

            // My songs
            item {
                Text("My Songs", style = MaterialTheme.typography.titleLarge, color = SpotifyTextPrimary)
            }

            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SpotifyGreen)
                    }
                }
            } else if (uiState.songs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MusicNote, null, tint = SpotifyTextDisabled, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No songs yet", style = MaterialTheme.typography.titleMedium, color = SpotifyTextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Upload your first song to get started", style = MaterialTheme.typography.bodyMedium, color = SpotifyTextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onUploadClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = SpotifyBlack)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Song")
                        }
                    }
                }
            } else {
                items(uiState.songs) { song ->
                    SongCard(
                        title = song.title,
                        artistName = "${song.plays.formatStreamCount()} plays${if (!song.isApproved) " • Pending" else ""}",
                        coverUrl = song.coverUrl,
                        onPlayClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SpotifyGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = SpotifyGreen, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = SpotifyTextPrimary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
        }
    }
}
