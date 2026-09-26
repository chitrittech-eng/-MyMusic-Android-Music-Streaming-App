package com.mymusic.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymusic.app.core.ui.components.PrimaryButton
import com.mymusic.app.core.ui.components.SecondaryButton
import com.mymusic.app.core.ui.theme.*

@Composable
fun WelcomeScreen(
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "♪",
                fontSize = 80.sp,
                color = SpotifyGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Millions of songs.\nFree forever.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyTextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Sign up free",
                onClick = onSignUpClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryButton(
                text = "Log in",
                onClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By signing up, you agree to our Terms of Service and Privacy Policy.",
                style = MaterialTheme.typography.bodySmall,
                color = SpotifyTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
