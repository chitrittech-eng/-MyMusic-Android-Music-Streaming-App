package com.mymusic.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MicExternalOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymusic.app.core.common.Constants
import com.mymusic.app.core.ui.components.PrimaryButton
import com.mymusic.app.core.ui.theme.*

@Composable
fun RoleSelectionScreen(
    uiState: AuthUiState,
    onRoleSelected: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(Constants.ROLE_LISTENER) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = "How will you use MyMusic?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose your role. You can change it later in settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = SpotifyTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            RoleCard(
                icon = Icons.Default.Headphones,
                title = "Listener",
                description = "Discover and play music, create playlists, follow artists",
                isSelected = selectedRole == Constants.ROLE_LISTENER,
                onClick = { selectedRole = Constants.ROLE_LISTENER }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RoleCard(
                icon = Icons.Default.MicExternalOn,
                title = "Artist",
                description = "Upload your music, manage your artist profile, view analytics",
                isSelected = selectedRole == Constants.ROLE_ARTIST,
                onClick = { selectedRole = Constants.ROLE_ARTIST }
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Continue",
                onClick = { onRoleSelected(selectedRole) },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (isSelected) SpotifyGreen else SpotifyLightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (isSelected) SpotifyGreen.copy(alpha = 0.1f) else SpotifyDarkGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) SpotifyGreen else SpotifyTextSecondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) SpotifyGreen else SpotifyTextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextSecondary
                )
            }
        }
    }
}
