package com.unsmah.pathpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.unsmah.pathpulse.ui.history.HistoryScreen
import com.unsmah.pathpulse.ui.history.TrackDetailScreen
import com.unsmah.pathpulse.ui.live.LiveMapScreen
import com.unsmah.pathpulse.ui.map.MapScreen
import com.unsmah.pathpulse.ui.reports.ReportsScreen
import com.unsmah.pathpulse.ui.settings.SettingsScreen
import com.unsmah.pathpulse.ui.track.TrackScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: com.unsmah.pathpulse.ui.MainViewModel,
    onStartTracking: (Long) -> Unit,
    onStopTracking: () -> Unit,
    onPauseTracking: () -> Unit,
    onResumeTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Track,
        modifier = modifier
    ) {
        composable(Screen.Track) {
            TrackScreen(
                viewModel = viewModel,
                onStartTracking = onStartTracking,
                onStopTracking = onStopTracking,
                onPauseTracking = onPauseTracking,
                onResumeTracking = onResumeTracking,
                onNavigateToHistory = { navController.navigate(Screen.History) },
                onNavigateToMap = { navController.navigate(Screen.Map) }
            )
        }

        composable(Screen.History) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateToTrackDetail = { trackId ->
                    navController.navigate("${Screen.TrackDetail}/$trackId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Screen.TrackDetail}/{trackId}",
            arguments = listOf(navArgument("trackId") { type = NavType.LongType })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getLong("trackId") ?: return@composable
            TrackDetailScreen(
                trackId = trackId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Map) {
            MapScreen(
                viewModel = viewModel,
                onNavigateToLiveMap = { navController.navigate(Screen.LiveMap) }
            )
        }

        composable(Screen.LiveMap) {
            LiveMapScreen(
                engine = viewModel.mapEngine,
                tileLayer = viewModel.tileLayer
            )
        }

        composable(Screen.Reports) {
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

object Screen {
    const val Track = "track"
    const val History = "history"
    const val TrackDetail = "track_detail"
    const val Map = "map"
    const val LiveMap = "live_map"
    const val Reports = "reports"
    const val Settings = "settings"
}
