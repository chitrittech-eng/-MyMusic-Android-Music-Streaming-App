package com.mymusic.app.feature.moderator

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.LicenseType
import com.mymusic.app.domain.model.Report
import com.mymusic.app.domain.model.Song
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorScreen(
    onBack: () -> Unit,
    viewModel: ModeratorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reports", "Song Approvals")

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = { Text("Moderation Queue", color = RoleModerator) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpotifyTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadReports() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = SpotifyTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpotifyBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tab row ──────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SpotifyBlack,
                contentColor = SpotifyGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SpotifyGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    title,
                                    color = if (selectedTab == index) SpotifyGreen else SpotifyTextSecondary
                                )
                                // Badge count
                                val count = if (index == 0) uiState.reports.size else uiState.pendingSongs.size
                                if (count > 0) {
                                    Badge(containerColor = if (index == 0) SpotifyError else SpotifyWarning) {
                                        Text("$count", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            } else {
                when (selectedTab) {
                    0 -> ReportsTab(
                        reports = uiState.reports,
                        onDismiss = { viewModel.resolveReport(it, "dismissed") },
                        onBanUser = { viewModel.resolveReport(it, "banned") },
                        onRemoveContent = { viewModel.resolveReport(it, "content_removed") }
                    )
                    1 -> SongApprovalsTab(
                        songs = uiState.pendingSongs,
                        onApprove = viewModel::approveSong,
                        onReject = viewModel::rejectSong
                    )
                }
            }
        }
    }
}

// ── Reports Tab ───────────────────────────────────────────────────────────────

@Composable
private fun ReportsTab(
    reports: List<Report>,
    onDismiss: (String) -> Unit,
    onBanUser: (String) -> Unit,
    onRemoveContent: (String) -> Unit
) {
    if (reports.isEmpty()) {
        EmptyState(icon = Icons.Default.CheckCircle, message = "No pending reports")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reports) { report ->
                ReportCard(
                    report = report,
                    onDismiss = { onDismiss(report.id) },
                    onBanUser = { onBanUser(report.id) },
                    onRemoveContent = { onRemoveContent(report.id) }
                )
            }
        }
    }
}

// ── Song Approvals Tab ────────────────────────────────────────────────────────

@Composable
private fun SongApprovalsTab(
    songs: List<Song>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (songs.isEmpty()) {
        EmptyState(icon = Icons.Default.LibraryMusic, message = "No songs pending approval")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
                SongApprovalCard(song = song, onApprove = { onApprove(song.id) }, onReject = { onReject(song.id) })
            }
        }
    }
}

@Composable
private fun SongApprovalCard(
    song: Song,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val licenseType = LicenseType.fromValue(song.licenseType)
    val licenseColor = when (licenseType) {
        LicenseType.ARTIST_OWNED -> SpotifyGreen
        LicenseType.CREATIVE_COMMONS -> SpotifyWarning
        LicenseType.ROYALTY_FREE_LICENSED -> SpotifyGreen
        LicenseType.PUBLIC_DOMAIN -> SpotifyTextSecondary
    }
    val confirmedDate = song.rightsConfirmedAt?.let {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(it))
    } ?: "—"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpotifyGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & artist
            Text(song.title, style = MaterialTheme.typography.titleMedium, color = SpotifyTextPrimary, fontWeight = FontWeight.SemiBold)
            Text("by ${song.artistName}", style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
            if (song.genre.isNotBlank()) {
                Text(song.genre, style = MaterialTheme.typography.labelSmall, color = SpotifyTextDisabled)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = SpotifyLightGray, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            // ── Rights metadata ── shown prominently so moderators must notice it
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.VerifiedUser, null, tint = licenseColor, modifier = Modifier.size(16.dp))
                Text(
                    text = "License: ${song.licenseType.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = licenseColor,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!song.licenseSource.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.Link, null, tint = SpotifyTextSecondary, modifier = Modifier.size(14.dp))
                    Text("Source: ${song.licenseSource}", style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    if (song.rightsConfirmed) Icons.Default.CheckCircle else Icons.Default.Warning,
                    null,
                    tint = if (song.rightsConfirmed) SpotifyGreen else SpotifyError,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (song.rightsConfirmed) "Rights confirmed on $confirmedDate (${song.agreementVersion})"
                           else "⚠ Rights NOT confirmed — do not approve",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (song.rightsConfirmed) SpotifyTextSecondary else SpotifyError
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons — Approve disabled if rights not confirmed
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SpotifyError)
                ) {
                    Text("Reject", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onApprove,
                    enabled = song.rightsConfirmed,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotifyGreen,
                        contentColor = SpotifyBlack,
                        disabledContainerColor = SpotifyLightGray,
                        disabledContentColor = SpotifyTextDisabled
                    )
                ) {
                    Text("Approve", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = SpotifyGreen, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Queue is clear!", style = MaterialTheme.typography.titleLarge, color = SpotifyTextPrimary)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = SpotifyTextSecondary)
        }
    }
}

@Composable
private fun ReportCard(
    report: Report,
    onDismiss: () -> Unit,
    onBanUser: () -> Unit,
    onRemoveContent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpotifyGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Flag, null, tint = SpotifyError, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Report: ${report.targetType.replaceFirstChar { it.uppercaseChar() }}",
                    style = MaterialTheme.typography.titleSmall,
                    color = SpotifyTextPrimary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Reason: ${report.reason}", style = MaterialTheme.typography.bodyMedium, color = SpotifyTextSecondary)
            Text("Target ID: ${report.targetId}", style = MaterialTheme.typography.bodySmall, color = SpotifyTextDisabled)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Dismiss", color = SpotifyTextSecondary, style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onRemoveContent,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyWarning, contentColor = SpotifyBlack)
                ) {
                    Text("Remove", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onBanUser,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyError, contentColor = SpotifyTextPrimary)
                ) {
                    Text("Ban", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
