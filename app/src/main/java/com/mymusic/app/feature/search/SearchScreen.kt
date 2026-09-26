package com.mymusic.app.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mymusic.app.core.ui.components.*
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.Album
import com.mymusic.app.domain.model.Artist
import com.mymusic.app.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSongClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineMedium,
            color = SpotifyTextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        SearchBar(
            query = uiState.query,
            onQueryChange = viewModel::onQueryChange,
            onClear = viewModel::clearQuery,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.query.isBlank()) {
            // Show categories
            Text(
                text = "Browse all",
                style = MaterialTheme.typography.titleLarge,
                color = SpotifyTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.categories) { cat ->
                    CategoryTile(
                        name = cat.name,
                        color = parseHex(cat.color),
                        imageUrl = cat.imageUrl,
                        onClick = { onCategoryClick(cat.id) }
                    )
                }
            }
        } else if (uiState.isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpotifyGreen)
            }
        } else if (!uiState.hasResults) {
            EmptyState(
                icon = Icons.Default.Search,
                title = "No results for \"${uiState.query}\"",
                subtitle = "Try different keywords or check the spelling",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                if (uiState.songs.isNotEmpty()) {
                    item { SectionHeader(title = "Songs") }
                    items(uiState.songs.take(5)) { song ->
                        SongCard(
                            title = song.title,
                            artistName = song.artistName,
                            coverUrl = song.coverUrl,
                            onPlayClick = { onSongClick(song) }
                        )
                    }
                }
                if (uiState.artists.isNotEmpty()) {
                    item { SectionHeader(title = "Artists") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.artists) { artist ->
                                ArtistCard(
                                    name = artist.name,
                                    imageUrl = artist.photoUrl,
                                    onClick = { onArtistClick(artist.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (uiState.albums.isNotEmpty()) {
                    item { SectionHeader(title = "Albums") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.albums) { album ->
                                SquareCard(
                                    title = album.title,
                                    subtitle = album.artistName,
                                    imageUrl = album.coverUrl,
                                    onClick = { onAlbumClick(album.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("What do you want to listen to?", color = SpotifyTextSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = SpotifyTextSecondary) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, "Clear", tint = SpotifyTextSecondary)
                }
            }
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = SpotifyTextPrimary,
            unfocusedTextColor = SpotifyTextPrimary,
            focusedBorderColor = SpotifyGreen,
            unfocusedBorderColor = SpotifyLightGray,
            focusedContainerColor = SpotifyDarkGray,
            unfocusedContainerColor = SpotifyDarkGray,
            cursorColor = SpotifyGreen
        )
    )
}

private fun parseHex(hex: String): androidx.compose.ui.graphics.Color {
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
