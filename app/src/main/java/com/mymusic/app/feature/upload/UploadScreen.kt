package com.mymusic.app.feature.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mymusic.app.core.ui.components.PrimaryButton
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.LicenseType
import com.mymusic.app.feature.auth.authTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onBack: () -> Unit,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.setAudioUri(it) } }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.setCoverUri(it) } }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            TopAppBar(
                title = { Text("Upload song", color = SpotifyTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpotifyTextPrimary)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Cover art picker ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SpotifyGray)
                    .border(1.dp, SpotifyLightGray, RoundedCornerShape(8.dp))
                    .clickable { coverPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.coverUri != null) {
                    AsyncImage(
                        model = uiState.coverUri,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, null, tint = SpotifyTextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to add cover art", style = MaterialTheme.typography.bodyMedium, color = SpotifyTextSecondary)
                    }
                }
            }

            // ── Audio file picker ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (uiState.audioUri != null) SpotifyGreen.copy(alpha = 0.15f) else SpotifyGray)
                    .border(1.dp, if (uiState.audioUri != null) SpotifyGreen else SpotifyLightGray, RoundedCornerShape(8.dp))
                    .clickable { audioPickerLauncher.launch("audio/*") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (uiState.audioUri != null) Icons.Default.CheckCircle else Icons.Default.AudioFile,
                        null,
                        tint = if (uiState.audioUri != null) SpotifyGreen else SpotifyTextSecondary
                    )
                    Text(
                        text = if (uiState.audioUri != null) "Audio file selected" else "Select audio file (MP3)",
                        color = if (uiState.audioUri != null) SpotifyGreen else SpotifyTextSecondary
                    )
                }
            }

            // ── Song title ───────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Song title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = authTextFieldColors()
            )

            // ── Genre dropdown ───────────────────────────────────────────────
            val genres = listOf("Pop", "Hip-Hop", "Rock", "Electronic", "R&B", "Latin", "Country", "Classical", "Jazz", "Other")
            var genreExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = genreExpanded,
                onExpandedChange = { genreExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.genre,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Genre") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = authTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = genreExpanded,
                    onDismissRequest = { genreExpanded = false },
                    modifier = Modifier.background(SpotifyGray)
                ) {
                    genres.forEach { genre ->
                        DropdownMenuItem(
                            text = { Text(genre, color = SpotifyTextPrimary) },
                            onClick = {
                                viewModel.updateGenre(genre)
                                genreExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Lyrics ───────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.lyrics,
                onValueChange = viewModel::updateLyrics,
                label = { Text("Lyrics (optional)") },
                minLines = 3,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
                colors = authTextFieldColors()
            )

            // ── Rights & Licensing section ───────────────────────────────────
            HorizontalDivider(color = SpotifyLightGray, thickness = 1.dp)

            Text(
                "Rights & Licensing",
                style = MaterialTheme.typography.titleSmall,
                color = SpotifyTextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            // License Type dropdown
            val licenseLabels = mapOf(
                LicenseType.ARTIST_OWNED to "I own this track (Artist-owned)",
                LicenseType.CREATIVE_COMMONS to "Creative Commons",
                LicenseType.ROYALTY_FREE_LICENSED to "Royalty-Free (licensed library)",
                LicenseType.PUBLIC_DOMAIN to "Public Domain"
            )
            var licenseExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = licenseExpanded,
                onExpandedChange = { licenseExpanded = it }
            ) {
                OutlinedTextField(
                    value = licenseLabels[uiState.licenseType] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("License type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = licenseExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = authTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = licenseExpanded,
                    onDismissRequest = { licenseExpanded = false },
                    modifier = Modifier.background(SpotifyGray)
                ) {
                    LicenseType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(licenseLabels[type] ?: type.value, color = SpotifyTextPrimary) },
                            onClick = {
                                viewModel.updateLicenseType(type)
                                licenseExpanded = false
                            }
                        )
                    }
                }
            }

            // License Source — only shown when track is not artist-owned
            if (uiState.licenseType != LicenseType.ARTIST_OWNED) {
                OutlinedTextField(
                    value = uiState.licenseSource,
                    onValueChange = viewModel::updateLicenseSource,
                    label = { Text("License source (e.g. Jamendo, Free Music Archive)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authTextFieldColors()
                )
            }

            // ── Rights agreement checkbox ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (uiState.rightsCheckboxAccepted)
                            SpotifyGreen.copy(alpha = 0.08f)
                        else
                            SpotifyGray
                    )
                    .border(
                        width = 1.dp,
                        color = if (uiState.rightsCheckboxAccepted) SpotifyGreen else SpotifyLightGray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { viewModel.toggleRightsCheckbox() }
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = uiState.rightsCheckboxAccepted,
                    onCheckedChange = { viewModel.toggleRightsCheckbox() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SpotifyGreen,
                        uncheckedColor = SpotifyTextSecondary
                    )
                )
                Text(
                    text = buildAnnotatedString {
                        append("I confirm that I have all necessary rights, licences, or permissions to upload and distribute this track on MyMusic, ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = SpotifyTextPrimary)) {
                            append("and that it does not infringe any third-party copyright.")
                        }
                        append(" I understand that false declarations may result in content removal and account suspension. (Agreement v1.0)")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextSecondary
                )
            }

            // ── Error message ────────────────────────────────────────────────
            if (uiState.error != null) {
                Text(uiState.error!!, color = SpotifyError, style = MaterialTheme.typography.bodySmall)
            }

            // ── Upload progress ──────────────────────────────────────────────
            if (uiState.isUploading) {
                LinearProgressIndicator(
                    progress = { uiState.uploadProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = SpotifyGreen,
                    trackColor = SpotifyLightGray
                )
                Text(
                    "Uploading ${(uiState.uploadProgress * 100).toInt()}%...",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextSecondary
                )
            }

            // ── Submit — disabled until rights checkbox is ticked ────────────
            PrimaryButton(
                text = "Upload song",
                onClick = viewModel::uploadSong,
                enabled = uiState.title.isNotBlank()
                        && uiState.audioUri != null
                        && uiState.rightsCheckboxAccepted
                        && !uiState.isUploading,
                isLoading = uiState.isUploading
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
