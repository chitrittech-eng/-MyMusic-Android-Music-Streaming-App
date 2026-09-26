package com.mymusic.app.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mymusic.app.core.common.Extensions.toReadableDuration
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.RepeatMode

@Composable
fun NowPlayingScreen(
    onCollapse: () -> Unit,
    onQueueClick: () -> Unit,
    onAddToPlaylist: (String) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.playerState.collectAsStateWithLifecycle()
    val isLiked by viewModel.isLiked.collectAsStateWithLifecycle()

    val song = state.currentSong ?: return

    LaunchedEffect(song.id) { viewModel.checkLiked(song.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyDarkGray)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Default.KeyboardArrowDown, "Collapse", tint = SpotifyTextPrimary, modifier = Modifier.size(32.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Now Playing", style = MaterialTheme.typography.labelMedium, color = SpotifyTextSecondary)
            }
            IconButton(onClick = { onAddToPlaylist(song.id) }) {
                Icon(Icons.Default.MoreVert, "More", tint = SpotifyTextPrimary)
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Album art
        AsyncImage(
            model = song.coverUrl,
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SpotifyGray)
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // Song info + like
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = SpotifyTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotifyTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { viewModel.toggleLike(song.id) }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) SpotifyGreen else SpotifyTextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        Slider(
            value = state.progress,
            onValueChange = { viewModel.seekTo((it * state.durationMs).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = SpotifyTextPrimary,
                activeTrackColor = SpotifyTextPrimary,
                inactiveTrackColor = SpotifyLightGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(state.currentPositionMs.toReadableDuration(), style = MaterialTheme.typography.labelSmall, color = SpotifyTextSecondary)
            Text(state.durationMs.toReadableDuration(), style = MaterialTheme.typography.labelSmall, color = SpotifyTextSecondary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    Icons.Default.Shuffle,
                    "Shuffle",
                    tint = if (state.isShuffled) SpotifyGreen else SpotifyTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { viewModel.previous() }) {
                Icon(Icons.Default.SkipPrevious, "Previous", tint = SpotifyTextPrimary, modifier = Modifier.size(40.dp))
            }
            FloatingActionButton(
                onClick = { viewModel.playPause() },
                containerColor = SpotifyTextPrimary,
                contentColor = SpotifyBlack,
                modifier = Modifier.size(64.dp)
            ) {
                if (state.isBuffering) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = SpotifyBlack, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            IconButton(onClick = { viewModel.next() }) {
                Icon(Icons.Default.SkipNext, "Next", tint = SpotifyTextPrimary, modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = { viewModel.cycleRepeat() }) {
                Icon(
                    imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (state.repeatMode != RepeatMode.OFF) SpotifyGreen else SpotifyTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Queue + lyrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onQueueClick) {
                Icon(Icons.Default.QueueMusic, null, tint = SpotifyTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Queue", color = SpotifyTextSecondary, style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
