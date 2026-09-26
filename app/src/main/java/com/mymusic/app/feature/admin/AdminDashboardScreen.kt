package com.mymusic.app.feature.admin

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
import com.mymusic.app.core.ui.components.RoleBadge
import com.mymusic.app.core.ui.components.SongCard
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.User

enum class AdminTab { OVERVIEW, PENDING, USERS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", color = RoleAdmin) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpotifyTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAdminData() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = SpotifyTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpotifyBlack)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = SpotifyDarkGray,
                contentColor = SpotifyGreen
            ) {
                AdminTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                                color = if (selectedTab == tab) SpotifyGreen else SpotifyTextSecondary
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                AdminTab.OVERVIEW -> AdminOverview(uiState)
                AdminTab.PENDING -> AdminPendingContent(uiState, viewModel::approveSong, viewModel::rejectSong)
                AdminTab.USERS -> AdminUsersContent(
                    users = viewModel.filteredUsers(),
                    query = uiState.userSearchQuery,
                    onQueryChange = viewModel::onUserSearchQueryChange,
                    onBan = viewModel::banUser,
                    onPromoteMod = viewModel::promoteToModerator,
                    onPromoteArtist = viewModel::promoteToArtist
                )
            }
        }
    }
}

@Composable
private fun AdminOverview(uiState: AdminUiState) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard("Total Users", uiState.totalUsers.toString(), Icons.Default.People, Modifier.weight(1f))
                AdminStatCard("Pending Songs", uiState.pendingSongs.size.toString(), Icons.Default.HourglassFull, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SpotifyGray), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = RoleAdmin, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = SpotifyTextPrimary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
        }
    }
}

@Composable
private fun AdminPendingContent(
    uiState: AdminUiState,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (uiState.pendingSongs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending approvals", color = SpotifyTextSecondary)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(uiState.pendingSongs) { song ->
                Column {
                    SongCard(
                        title = song.title,
                        artistName = song.artistName,
                        coverUrl = song.coverUrl,
                        onPlayClick = {}
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onApprove(song.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = SpotifyBlack),
                            modifier = Modifier.weight(1f)
                        ) { Text("Approve") }
                        OutlinedButton(
                            onClick = { onReject(song.id) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Reject", color = SpotifyError) }
                    }
                    Divider(color = SpotifyDivider)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminUsersContent(
    users: List<User>,
    query: String,
    onQueryChange: (String) -> Unit,
    onBan: (String) -> Unit,
    onPromoteMod: (String) -> Unit,
    onPromoteArtist: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search users...", color = SpotifyTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = SpotifyTextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(users) { user ->
                AdminUserItem(user, onBan, onPromoteMod, onPromoteArtist)
            }
        }
    }
}

@Composable
private fun AdminUserItem(
    user: User,
    onBan: (String) -> Unit,
    onPromoteMod: (String) -> Unit,
    onPromoteArtist: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(SpotifyGray, shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                user.displayName.firstOrNull()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = SpotifyTextPrimary
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName, style = MaterialTheme.typography.titleSmall, color = SpotifyTextPrimary)
            Text(user.email, style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
            RoleBadge(user.role)
        }
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, "More", tint = SpotifyTextSecondary)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SpotifyGray)
            ) {
                DropdownMenuItem(
                    text = { Text("Ban user", color = SpotifyError) },
                    onClick = { onBan(user.id); expanded = false },
                    leadingIcon = { Icon(Icons.Default.Block, null, tint = SpotifyError) }
                )
                DropdownMenuItem(
                    text = { Text("Make Moderator", color = SpotifyTextPrimary) },
                    onClick = { onPromoteMod(user.id); expanded = false },
                    leadingIcon = { Icon(Icons.Default.Shield, null, tint = RoleModerator) }
                )
                DropdownMenuItem(
                    text = { Text("Make Artist", color = SpotifyTextPrimary) },
                    onClick = { onPromoteArtist(user.id); expanded = false },
                    leadingIcon = { Icon(Icons.Default.MicExternalOn, null, tint = RoleArtist) }
                )
            }
        }
    }
    Divider(color = SpotifyDivider, modifier = Modifier.padding(horizontal = 16.dp))
}
