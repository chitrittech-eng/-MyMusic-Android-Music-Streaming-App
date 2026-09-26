package com.mymusic.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mymusic.app.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onDownloadsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onModeratorClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = SpotifyTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpotifyTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpotifyBlack)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { SettingsSectionHeader("Playback") }

            item {
                var qualityExpanded by remember { mutableStateOf(false) }
                val qualityOptions = listOf("Low (24 kbps)", "Normal (96 kbps)", "High (160 kbps)", "Very High (320 kbps)")
                SettingsDropdown(
                    title = "Audio quality",
                    subtitle = qualityOptions.getOrElse(uiState.audioQuality) { "Normal" },
                    icon = Icons.Default.HighQuality,
                    expanded = qualityExpanded,
                    options = qualityOptions,
                    onExpandChange = { qualityExpanded = it },
                    onOptionSelected = { viewModel.setAudioQuality(it); qualityExpanded = false }
                )
            }

            item {
                SettingsSlider(
                    title = "Crossfade",
                    subtitle = "${uiState.crossfadeDuration}s",
                    icon = Icons.Default.SwapHoriz,
                    value = uiState.crossfadeDuration.toFloat(),
                    valueRange = 0f..12f,
                    steps = 11,
                    onValueChange = { viewModel.setCrossfade(it.toInt()) }
                )
            }

            item {
                SettingsToggle(
                    title = "Normalize volume",
                    subtitle = "Set the same volume level for all songs",
                    icon = Icons.Default.VolumeUp,
                    checked = uiState.normalizeVolume,
                    onCheckedChange = { viewModel.setNormalizeVolume(it) }
                )
            }

            item { SettingsSectionHeader("Privacy") }

            item {
                SettingsToggle(
                    title = "Private session",
                    subtitle = "Start a private session to listen without updating your history",
                    icon = Icons.Default.VisibilityOff,
                    checked = uiState.privateSession,
                    onCheckedChange = { viewModel.setPrivateSession(it) }
                )
            }

            item { SettingsSectionHeader("Storage") }

            item {
                SettingsItem(
                    title = "Downloads",
                    subtitle = "Manage your downloaded songs",
                    icon = Icons.Default.Download,
                    onClick = onDownloadsClick
                )
            }

            // Role-specific options
            if (uiState.userRole == "admin") {
                item { SettingsSectionHeader("Administration") }
                item {
                    SettingsItem(
                        title = "Admin Panel",
                        subtitle = "Manage users and content",
                        icon = Icons.Default.AdminPanelSettings,
                        tint = RoleAdmin,
                        onClick = onAdminClick
                    )
                }
                item {
                    SettingsItem(
                        title = "Moderator Queue",
                        subtitle = "Review reported content",
                        icon = Icons.Default.Shield,
                        tint = RoleModerator,
                        onClick = onModeratorClick
                    )
                }
            } else if (uiState.userRole == "moderator") {
                item { SettingsSectionHeader("Moderation") }
                item {
                    SettingsItem(
                        title = "Moderator Queue",
                        subtitle = "Review reported content",
                        icon = Icons.Default.Shield,
                        tint = RoleModerator,
                        onClick = onModeratorClick
                    )
                }
            }

            item { SettingsSectionHeader("Account") }

            item {
                SettingsItem(
                    title = "Log out",
                    subtitle = null,
                    icon = Icons.Default.Logout,
                    tint = SpotifyError,
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = SpotifyTextSecondary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color = SpotifyTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = tint)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = SpotifyTextDisabled)
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = SpotifyTextPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = SpotifyTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SpotifyBlack,
                checkedTrackColor = SpotifyGreen,
                uncheckedThumbColor = SpotifyTextSecondary,
                uncheckedTrackColor = SpotifyLightGray
            )
        )
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    subtitle: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = SpotifyTextPrimary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = SpotifyTextPrimary, modifier = Modifier.weight(1f))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = SpotifyTextPrimary,
                activeTrackColor = SpotifyGreen,
                inactiveTrackColor = SpotifyLightGray
            )
        )
    }
}

@Composable
private fun SettingsDropdown(
    title: String,
    subtitle: String,
    icon: ImageVector,
    expanded: Boolean,
    options: List<String>,
    onExpandChange: (Boolean) -> Unit,
    onOptionSelected: (Int) -> Unit
) {
    Box {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onExpandChange(true) }.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = SpotifyTextPrimary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = SpotifyTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SpotifyTextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = SpotifyTextDisabled)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) },
            modifier = Modifier.background(SpotifyGray)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, color = SpotifyTextPrimary) },
                    onClick = { onOptionSelected(index) }
                )
            }
        }
    }
}
