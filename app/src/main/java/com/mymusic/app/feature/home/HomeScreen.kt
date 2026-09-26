package com.mymusic.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mymusic.app.core.ui.components.*
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onSeeAllSongs: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        color = SpotifyTextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        Icon(Icons.Default.Notifications, "Notifications", tint = SpotifyTextPrimary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings", tint = SpotifyTextPrimary)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                // Recently Played
                if (uiState.recentlyPlayed.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Recently played",
                            onSeeAllClick = { onSeeAllSongs("recently_played") }
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.recentlyPlayed) { song ->
                                SquareCard(
                                    title = song.title,
                                    subtitle = song.artistName,
                                    imageUrl = song.coverUrl,
                                    onClick = { onSongClick(song) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Featured / Popular
                if (uiState.featuredSongs.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Popular right now",
                            onSeeAllClick = { onSeeAllSongs("popular") }
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.featuredSongs) { song ->
                                SquareCard(
                                    title = song.title,
                                    subtitle = song.artistName,
                                    imageUrl = song.coverUrl,
                                    onClick = { onSongClick(song) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // New Releases
                if (uiState.newReleases.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "New releases",
                            onSeeAllClick = { onSeeAllSongs("new_releases") }
                        )
                    }
                    items(uiState.newReleases.take(5)) { song ->
                        SongCard(
                            title = song.title,
                            artistName = song.artistName,
                            coverUrl = song.coverUrl,
                            onPlayClick = { onSongClick(song) },
                            onMoreClick = { /* more options sheet */ }
                        )
                    }
                }

                // Browse categories
                if (uiState.categories.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Browse all")
                    }
                    item {
                        val chunked = uiState.categories.chunked(2)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunked.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { cat ->
                                        CategoryTile(
                                            name = cat.name,
                                            color = parseColor(cat.color),
                                            imageUrl = cat.imageUrl,
                                            onClick = { onSeeAllSongs("category_${cat.id}") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseColor(hex: String): androidx.compose.ui.graphics.Color {
    return try {
        val value = hex.trimStart('#').toLong(16)
        val r = ((value shr 16) and 0xFF) / 255f
        val g = ((value shr 8) and 0xFF) / 255f
        val b = (value and 0xFF) / 255f
        androidx.compose.ui.graphics.Color(r, g, b)
    } catch (_: Exception) {
        SpotifyGreen
    }
}
