package com.mymusic.app.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mymusic.app.core.ui.theme.*
import com.mymusic.app.domain.model.Song
import com.mymusic.app.feature.admin.AdminDashboardScreen
import com.mymusic.app.feature.album.AlbumScreen
import com.mymusic.app.feature.artist.ArtistScreen
import com.mymusic.app.feature.auth.*
import com.mymusic.app.feature.dashboard.DashboardScreen
import com.mymusic.app.feature.download.DownloadsScreen
import com.mymusic.app.feature.home.HomeScreen
import com.mymusic.app.feature.library.LibraryScreen
import com.mymusic.app.feature.moderator.ModeratorScreen
import com.mymusic.app.feature.player.*
import com.mymusic.app.feature.playlist.PlaylistScreen
import com.mymusic.app.feature.search.SearchScreen
import com.mymusic.app.feature.settings.SettingsScreen
import com.mymusic.app.feature.upload.UploadScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object SignUp : Screen("signup")
    object Login : Screen("login")
    object ForgotPassword : Screen("forgot_password")
    object RoleSelection : Screen("role_selection")

    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")

    object NowPlaying : Screen("now_playing")
    object Queue : Screen("queue")

    object Artist : Screen("artist/{artistId}") {
        fun createRoute(id: String) = "artist/$id"
    }
    object Album : Screen("album/{albumId}") {
        fun createRoute(id: String) = "album/$id"
    }
    object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(id: String) = "playlist/$id"
    }

    object Upload : Screen("upload")
    object Dashboard : Screen("dashboard")
    object Downloads : Screen("downloads")
    object Settings : Screen("settings")
    object AdminPanel : Screen("admin")
    object Moderator : Screen("moderator")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Search, "Search", Icons.Default.Search),
    BottomNavItem(Screen.Library, "Library", Icons.Default.LibraryMusic)
)

@Composable
fun AppNavigation(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()

    var showPlayer by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    val startDestination = when {
        showSplash -> Screen.Splash.route
        authState.isAuthenticated -> Screen.Home.route
        authState.needsRoleSelection -> Screen.RoleSelection.route
        else -> Screen.Welcome.route
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route
        ) {
            // ── Auth ──────────────────────────────────────────────────────
            composable(Screen.Splash.route) {
                SplashScreen(onFinished = {
                    showSplash = false
                    val dest = when {
                        authState.isAuthenticated -> Screen.Home.route
                        authState.needsRoleSelection -> Screen.RoleSelection.route
                        else -> Screen.Welcome.route
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                    onLoginClick = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    uiState = authState,
                    onSignUp = { name, email, pass -> authViewModel.signUp(name, email, pass) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    uiState = authState,
                    onLogin = { email, pass -> authViewModel.signIn(email, pass) },
                    onForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onSendReset = authViewModel::sendPasswordReset,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.RoleSelection.route) {
                RoleSelectionScreen(
                    uiState = authState,
                    onRoleSelected = { role ->
                        authViewModel.selectRole(role)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Main app with bottom nav ──────────────────────────────────
            composable(Screen.Home.route) {
                MainScaffold(
                    navController = navController,
                    playerState = playerState,
                    onExpand = { showPlayer = true },
                    onPlayPause = playerViewModel::playPause,
                    onNext = playerViewModel::next,
                    currentRoute = Screen.Home.route
                ) {
                    HomeScreen(
                        onSongClick = { song ->
                            playerViewModel.playSong(song)
                        },
                        onArtistClick = { navController.navigate(Screen.Artist.createRoute(it)) },
                        onAlbumClick = { navController.navigate(Screen.Album.createRoute(it)) },
                        onSeeAllSongs = { },
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }
            }
            composable(Screen.Search.route) {
                MainScaffold(
                    navController = navController,
                    playerState = playerState,
                    onExpand = { showPlayer = true },
                    onPlayPause = playerViewModel::playPause,
                    onNext = playerViewModel::next,
                    currentRoute = Screen.Search.route
                ) {
                    SearchScreen(
                        onSongClick = { song -> playerViewModel.playSong(song) },
                        onArtistClick = { navController.navigate(Screen.Artist.createRoute(it)) },
                        onAlbumClick = { navController.navigate(Screen.Album.createRoute(it)) },
                        onCategoryClick = { }
                    )
                }
            }
            composable(Screen.Library.route) {
                MainScaffold(
                    navController = navController,
                    playerState = playerState,
                    onExpand = { showPlayer = true },
                    onPlayPause = playerViewModel::playPause,
                    onNext = playerViewModel::next,
                    currentRoute = Screen.Library.route
                ) {
                    LibraryScreen(
                        onPlaylistClick = { navController.navigate(Screen.Playlist.createRoute(it)) },
                        onLikedSongsClick = { }
                    )
                }
            }

            // ── Detail screens ───────────────────────────────────────────
            composable(
                route = Screen.Artist.route,
                arguments = listOf(navArgument("artistId") { type = NavType.StringType })
            ) { backStack ->
                ArtistScreen(
                    artistId = backStack.arguments?.getString("artistId") ?: "",
                    onBack = { navController.popBackStack() },
                    onSongClick = { song -> playerViewModel.playSong(song) },
                    onAlbumClick = { navController.navigate(Screen.Album.createRoute(it)) }
                )
            }
            composable(
                route = Screen.Album.route,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType })
            ) { backStack ->
                AlbumScreen(
                    albumId = backStack.arguments?.getString("albumId") ?: "",
                    onBack = { navController.popBackStack() },
                    onSongClick = { song -> playerViewModel.playSong(song) },
                    onArtistClick = { navController.navigate(Screen.Artist.createRoute(it)) },
                    onPlayAll = { songs -> songs.firstOrNull()?.let { playerViewModel.playSong(it, songs) } }
                )
            }
            composable(
                route = Screen.Playlist.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStack ->
                PlaylistScreen(
                    playlistId = backStack.arguments?.getString("playlistId") ?: "",
                    onBack = { navController.popBackStack() },
                    onSongClick = { song -> playerViewModel.playSong(song) },
                    onPlayAll = { songs -> songs.firstOrNull()?.let { playerViewModel.playSong(it, songs) } }
                )
            }

            // ── Queue ─────────────────────────────────────────────────────
            composable(Screen.Queue.route) {
                QueueScreen(onBack = { navController.popBackStack() })
            }

            // ── Artist features ───────────────────────────────────────────
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onUploadClick = { navController.navigate(Screen.Upload.route) },
                    onSongClick = { song -> playerViewModel.playSong(song) }
                )
            }
            composable(Screen.Upload.route) {
                UploadScreen(onBack = { navController.popBackStack() })
            }

            // ── Settings & admin ─────────────────────────────────────────
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onDownloadsClick = { navController.navigate(Screen.Downloads.route) },
                    onAdminClick = { navController.navigate(Screen.AdminPanel.route) },
                    onModeratorClick = { navController.navigate(Screen.Moderator.route) }
                )
            }
            composable(Screen.Downloads.route) {
                DownloadsScreen(
                    onBack = { navController.popBackStack() },
                    onSongClick = { song -> playerViewModel.playSong(song) }
                )
            }
            composable(Screen.AdminPanel.route) {
                AdminDashboardScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Moderator.route) {
                ModeratorScreen(onBack = { navController.popBackStack() })
            }
        }

        // Auth redirect side-effect
        LaunchedEffect(authState.isAuthenticated, authState.needsRoleSelection) {
            if (!showSplash) {
                when {
                    authState.isAuthenticated -> {
                        val currentRoute = navController.currentDestination?.route
                        if (currentRoute in listOf(Screen.Welcome.route, Screen.Login.route, Screen.SignUp.route, Screen.RoleSelection.route)) {
                            navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                        }
                    }
                    authState.needsRoleSelection -> {
                        navController.navigate(Screen.RoleSelection.route) { popUpTo(0) { inclusive = true } }
                    }
                    !authState.isAuthenticated && authState.currentUser == null -> {
                        val currentRoute = navController.currentDestination?.route
                        if (currentRoute != Screen.Welcome.route && currentRoute != Screen.Login.route &&
                            currentRoute != Screen.SignUp.route && currentRoute != Screen.ForgotPassword.route) {
                            navController.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                        }
                    }
                }
            }
        }

        // Full-screen player overlay
        AnimatedVisibility(
            visible = showPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NowPlayingScreen(
                onCollapse = { showPlayer = false },
                onQueueClick = {
                    showPlayer = false
                    navController.navigate(Screen.Queue.route)
                },
                onAddToPlaylist = { }
            )
        }
    }
}

@Composable
private fun MainScaffold(
    navController: androidx.navigation.NavController,
    playerState: com.mymusic.app.domain.model.PlayerState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = SpotifyBlack,
        bottomBar = {
            Column {
                MiniPlayer(
                    playerState = playerState,
                    onExpand = onExpand,
                    onPlayPause = onPlayPause,
                    onNext = onNext
                )
                NavigationBar(
                    containerColor = SpotifyBlack,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) SpotifyTextPrimary else SpotifyTextSecondary
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    color = if (selected) SpotifyTextPrimary else SpotifyTextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SpotifyGray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
