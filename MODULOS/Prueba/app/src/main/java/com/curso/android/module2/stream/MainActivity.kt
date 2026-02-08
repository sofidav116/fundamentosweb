package com.curso.android.module2.stream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.curso.android.module2.stream.data.repository.MusicRepository
import com.curso.android.module2.stream.ui.navigation.HomeDestination
import com.curso.android.module2.stream.ui.navigation.LibraryDestination
import com.curso.android.module2.stream.ui.navigation.PlayerDestination
import com.curso.android.module2.stream.ui.navigation.SearchDestination
import com.curso.android.module2.stream.ui.navigation.HighlightsDestination
import com.curso.android.module2.stream.ui.screens.HomeScreen
import com.curso.android.module2.stream.ui.screens.LibraryScreen
import com.curso.android.module2.stream.ui.screens.PlayerScreen
import com.curso.android.module2.stream.ui.screens.SearchScreen
import com.curso.android.module2.stream.ui.theme.StreamUITheme
import org.koin.compose.koinInject
import kotlin.reflect.KClass

/**
 * ================================================================================
 * MAIN ACTIVITY - Punto de Entrada de la UI
 * ================================================================================
 *
 * SINGLE ACTIVITY ARCHITECTURE
 * ----------------------------
 * En apps Compose modernas, típicamente usamos UNA sola Activity.
 * Toda la navegación se maneja internamente con Navigation Compose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            StreamUITheme {
                StreamUIApp()
            }
        }
    }
}

/**
 * ================================================================================
 * BOTTOM NAVIGATION ITEM
 * ================================================================================
 */
data class BottomNavItem(
    val route: KClass<*>,
    val label: String,
    val selectedIcon: @Composable () -> ImageVector,
    val unselectedIcon: @Composable () -> ImageVector
)

@Composable
fun getBottomNavItems(): List<BottomNavItem> {
    val libraryIcon = ImageVector.vectorResource(R.drawable.ic_library)
    return listOf(
        BottomNavItem(
            route = HomeDestination::class,
            label = "Home",
            selectedIcon = { Icons.Filled.Home },
            unselectedIcon = { Icons.Outlined.Home }
        ),
        BottomNavItem(
            route = SearchDestination::class,
            label = "Search",
            selectedIcon = { Icons.Filled.Search },
            unselectedIcon = { Icons.Outlined.Search }
        ),
        BottomNavItem(
            route = LibraryDestination::class,
            label = "Library",
            selectedIcon = { libraryIcon },
            unselectedIcon = { libraryIcon }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamUIApp() {

    val navController = rememberNavController()
    val repository: MusicRepository = koinInject()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = getBottomNavItems()
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route) == true
    }

    val topBarTitle = when {
        currentDestination?.hasRoute(HomeDestination::class) == true -> "StreamUI"
        currentDestination?.hasRoute(SearchDestination::class) == true -> "Search"
        currentDestination?.hasRoute(LibraryDestination::class) == true -> "Your Library"
        currentDestination?.hasRoute(HighlightsDestination::class) == true -> "Highlights"
        currentDestination?.hasRoute(PlayerDestination::class) == true -> "Now Playing"
        else -> "StreamUI"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.hasRoute(item.route)
                            } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(
                                        when (item.route) {
                                            HomeDestination::class -> HomeDestination
                                            SearchDestination::class -> SearchDestination
                                            LibraryDestination::class -> LibraryDestination
                                            else -> HomeDestination
                                        }
                                    ) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon() else item.unselectedIcon(),
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = HomeDestination,
                modifier = Modifier.padding(paddingValues)
            ) {

                composable<HomeDestination> {
                    HomeScreen(
                        onSongClick = { song ->
                            navController.navigate(PlayerDestination(songId = song.id))
                        }
                    )
                }

                composable<SearchDestination> {
                    SearchScreen(
                        onSongClick = { song ->
                            navController.navigate(PlayerDestination(songId = song.id))
                        },
                        onBackClick = { }
                    )
                }

                composable<LibraryDestination> {
                    LibraryScreen(
                        onPlaylistClick = { }
                    )
                }

                /**
                 * DESTINO: Highlights Screen
                 * --------------------------
                 * Pantalla sin argumentos.
                 * No forma parte del BottomNavigation.
                 */
                composable<HighlightsDestination> {
                    // TODO: Implementar HighlightsScreen
                }

                composable<PlayerDestination> { backStackEntry ->
                    val destination = backStackEntry.toRoute<PlayerDestination>()
                    val song = repository.getSongById(destination.songId)

                    PlayerScreen(
                        song = song,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
